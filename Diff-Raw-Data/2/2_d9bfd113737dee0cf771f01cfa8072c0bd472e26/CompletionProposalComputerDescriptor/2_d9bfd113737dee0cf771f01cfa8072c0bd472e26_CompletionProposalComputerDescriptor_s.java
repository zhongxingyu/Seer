 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.text.completion;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IContributor;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.InvalidRegistryObjectException;
 import org.eclipse.core.runtime.PerformanceStats;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.osgi.framework.Bundle;
 
 /**
  * The description of an extension to the
  * <code>org.eclipse.dltk.ui.javaCompletionProposalComputer</code> extension
  * point. Instances are immutable. Instances can be obtained from a
  * {@link CompletionProposalComputerRegistry}.
  * 
  * @see CompletionProposalComputerRegistry
  * 
  */
 final class CompletionProposalComputerDescriptor {
 	/** The default category id. */
 	private static final String DEFAULT_CATEGORY_ID = "org.eclipse.dltk.ui.defaultProposalCategory"; //$NON-NLS-1$
 	/** The extension schema name of the category id attribute. */
 	private static final String CATEGORY_ID = "categoryId"; //$NON-NLS-1$
 	/** The extension schema name of the partition type attribute. */
 	private static final String TYPE = "type"; //$NON-NLS-1$
 	/** The extension schema name of the class attribute. */
 	private static final String CLASS = "class"; //$NON-NLS-1$
 	/** The extension schema name of the activate attribute. */
 	private static final String ACTIVATE = "activate"; //$NON-NLS-1$
 	/** The extension schema name of the partition child elements. */
 	private static final String PARTITION = "partition"; //$NON-NLS-1$
 
 	public static final String TOOLKITID = "toolkitId";
 
 	/** Set of Script partition types. */
 	private static final Set PARTITION_SET = new HashSet();
 	/** The name of the performance event used to trace extensions. */
 	private static final String PERFORMANCE_EVENT = DLTKUIPlugin.getPluginId()
 			+ "/perf/content_assist/extensions"; //$NON-NLS-1$
 	/**
 	 * If <code>true</code>, execution time of extensions is measured and the
 	 * data forwarded to core's {@link PerformanceStats} service.
 	 */
 	private static final boolean MEASURE_PERFORMANCE = PerformanceStats
 			.isEnabled(PERFORMANCE_EVENT);
 	/**
 	 * Independently of the {@link PerformanceStats} service, any operation that
 	 * takes longer than {@value} milliseconds will be flagged as an violation.
 	 * This timeout does not apply to the first invocation, as it may take
 	 * longer due to plug-in initialization etc. See also
 	 * {@link #fIsReportingDelay}.
 	 */
	private static final long MAX_DELAY = 5000;
 
 	/* log constants */
 	private static final String COMPUTE_COMPLETION_PROPOSALS = "computeCompletionProposals()"; //$NON-NLS-1$
 	private static final String COMPUTE_CONTEXT_INFORMATION = "computeContextInformation()"; //$NON-NLS-1$
 	private static final String SESSION_STARTED = "sessionStarted()"; //$NON-NLS-1$
 	private static final String SESSION_ENDED = "sessionEnded()"; //$NON-NLS-1$
 
 	static {
 		if (DLTKCore.DEBUG) {
 			System.err.println("TODO: Add langauge dependent code here...");
 		}
 	}
 
 	/** The identifier of the extension. */
 	private final String fId;
 	/** The name of the extension. */
 	private final String fName;
 	/**
 	 * The class name of the provided
 	 * <code>IScriptCompletionProposalComputer</code>.
 	 */
 	private final String fClass;
 	/** The activate attribute value. */
 	private final boolean fActivate;
 	/** The partition of the extension (element type: {@link String}). */
 	private final Set fPartitions;
 	/** The configuration element of this extension. */
 	private final IConfigurationElement fElement;
 	/** The registry we are registered with. */
 	private final CompletionProposalComputerRegistry fRegistry;
 	/** The computer, if instantiated, <code>null</code> otherwise. */
 	private IScriptCompletionProposalComputer fComputer;
 	/** The ui category. */
 	private final CompletionProposalCategory fCategory;
 	/**
 	 * The first error message in the most recent operation, or
 	 * <code>null</code>.
 	 */
 	private String fLastError;
 	/**
 	 * Tells whether to inform the user when <code>MAX_DELAY</code> has been
 	 * exceeded. We start timing execution after the first session because the
 	 * first may take longer due to plug-in activation and initialization.
 	 */
 	private boolean fIsReportingDelay = false;
 	/** The start of the last operation. */
 	private long fStart;
 	private String fToolkitID;
 
 	/**
 	 * Creates a new descriptor.
 	 * 
 	 * @param element
 	 *            the configuration element to read
 	 * @param registry
 	 *            the computer registry creating this descriptor
 	 */
 	CompletionProposalComputerDescriptor(IConfigurationElement element,
 			CompletionProposalComputerRegistry registry, List categories)
 			throws InvalidRegistryObjectException {
 		Assert.isLegal(registry != null);
 		Assert.isLegal(element != null);
 
 		fRegistry = registry;
 		fElement = element;
 		IExtension extension = element.getDeclaringExtension();
 		fId = extension.getUniqueIdentifier();
 		checkNotNull(fId, "id"); //$NON-NLS-1$
 
 		String name = extension.getLabel();
 		if (name.length() == 0)
 			fName = fId;
 		else
 			fName = name;
 
 		Set partitions = new HashSet();
 		IConfigurationElement[] children = element.getChildren(PARTITION);
 		if (children.length == 0) {
 			fPartitions = PARTITION_SET; // add to all partition types if no
 											// partition is configured
 		} else {
 			for (int i = 0; i < children.length; i++) {
 				String type = children[i].getAttribute(TYPE);
 				checkNotNull(type, TYPE);
 				partitions.add(type);
 			}
 			fPartitions = Collections.unmodifiableSet(partitions);
 		}
 
 		String activateAttribute = element.getAttribute(ACTIVATE);
 		fActivate = Boolean.valueOf(activateAttribute).booleanValue();
 
 		fClass = element.getAttribute(CLASS);
 		checkNotNull(fClass, CLASS);
 
 		String categoryId = element.getAttribute(CATEGORY_ID);
 		if (categoryId == null)
 			categoryId = DEFAULT_CATEGORY_ID;
 		CompletionProposalCategory category = null;
 		for (Iterator it = categories.iterator(); it.hasNext();) {
 			CompletionProposalCategory cat = (CompletionProposalCategory) it
 					.next();
 			if (cat.getId().equals(categoryId)) {
 				category = cat;
 				break;
 			}
 		}
 
 		String toolkitId = element.getAttribute(TOOLKITID);
 		this.fToolkitID = toolkitId; 
 		if (category == null) {
 			// create a category if it does not exist
 			fCategory = new CompletionProposalCategory(categoryId, fName,
 					registry, toolkitId);
 			categories.add(fCategory);
 		} else {
 			fCategory = category;
 		}
 	}
 	public String getLanguageToolkitID() {
 		return this.fToolkitID;
 	}
 
 	/**
 	 * Checks an element that must be defined according to the extension point
 	 * schema. Throws an <code>InvalidRegistryObjectException</code> if
 	 * <code>obj</code> is <code>null</code>.
 	 */
 	private void checkNotNull(Object obj, String attribute)
 			throws InvalidRegistryObjectException {
 		if (obj == null) {
 			Object[] args = { getId(), fElement.getContributor().getName(),
 					attribute };
 			String message = Messages
 					.format(
 							ScriptTextMessages.CompletionProposalComputerDescriptor_illegal_attribute_message,
 							args);
 			IStatus status = new Status(IStatus.WARNING, DLTKUIPlugin
 					.getPluginId(), IStatus.OK, message, null);
 			DLTKUIPlugin.log(status);
 			throw new InvalidRegistryObjectException();
 		}
 	}
 
 	/**
 	 * Returns the identifier of the described extension.
 	 * 
 	 * @return Returns the id
 	 */
 	public String getId() {
 		return fId;
 	}
 
 	/**
 	 * Returns the name of the described extension.
 	 * 
 	 * @return Returns the name
 	 */
 	public String getName() {
 		return fName;
 	}
 
 	/**
 	 * Returns the partition types of the described extension.
 	 * 
 	 * @return the set of partition types (element type: {@link String})
 	 */
 	public Set getPartitions() {
 		return fPartitions;
 	}
 
 	/**
 	 * Returns a cached instance of the computer as described in the extension's
 	 * xml. The computer is {@link #createComputer() created} the first time
 	 * that this method is called and then cached.
 	 * 
 	 * @return a new instance of the completion proposal computer as described
 	 *         by this descriptor
 	 * @throws CoreException
 	 *             if the creation fails
 	 * @throws InvalidRegistryObjectException
 	 *             if the extension is not valid any longer (e.g. due to plug-in
 	 *             unloading)
 	 */
 	private synchronized IScriptCompletionProposalComputer getComputer()
 			throws CoreException, InvalidRegistryObjectException {
 		if (fComputer == null && (fActivate || isPluginLoaded()))
 			fComputer = createComputer();
 		return fComputer;
 	}
 
 	private boolean isPluginLoaded() {
 		Bundle bundle = getBundle();
 		return bundle != null && bundle.getState() == Bundle.ACTIVE;
 	}
 
 	private Bundle getBundle() {
 		String namespace = fElement.getDeclaringExtension().getContributor()
 				.getName();
 		Bundle bundle = Platform.getBundle(namespace);
 		return bundle;
 	}
 
 	/**
 	 * Returns a new instance of the computer as described in the extension's
 	 * xml. Note that the safest way to access the computer is by using the
 	 * {@linkplain #computeCompletionProposals(ContentAssistInvocationContext, IProgressMonitor) computeCompletionProposals}
 	 * and
 	 * {@linkplain #computeContextInformation(ContentAssistInvocationContext, IProgressMonitor) computeContextInformation}
 	 * methods. These delegate the functionality to the contributed computer,
 	 * but handle instance creation and any exceptions thrown.
 	 * 
 	 * @return a new instance of the completion proposal computer as described
 	 *         by this descriptor
 	 * @throws CoreException
 	 *             if the creation fails
 	 * @throws InvalidRegistryObjectException
 	 *             if the extension is not valid any longer (e.g. due to plug-in
 	 *             unloading)
 	 */
 	public IScriptCompletionProposalComputer createComputer()
 			throws CoreException, InvalidRegistryObjectException {
 		return (IScriptCompletionProposalComputer) fElement
 				.createExecutableExtension(CLASS);
 	}
 
 	/**
 	 * Safely computes completion proposals through the described extension. If
 	 * the extension is disabled, throws an exception or otherwise does not
 	 * adhere to the contract described in
 	 * {@link IScriptCompletionProposalComputer}, an empty list is returned.
 	 * 
 	 * @param context
 	 *            the invocation context passed on to the extension
 	 * @param monitor
 	 *            the progress monitor passed on to the extension
 	 * @return the list of computed completion proposals (element type:
 	 *         {@link org.eclipse.jface.text.contentassist.ICompletionProposal})
 	 */
 	public List computeCompletionProposals(
 			ContentAssistInvocationContext context, IProgressMonitor monitor) {
 		if (!isEnabled())
 			return Collections.EMPTY_LIST;
 
 		IStatus status;
 		try {
 			IScriptCompletionProposalComputer computer = getComputer();
 			if (computer == null) // not active yet
 				return Collections.EMPTY_LIST;
 
 			try {
 				PerformanceStats stats = startMeter(context, computer);
 				List proposals = computer.computeCompletionProposals(context,
 						monitor);
 				stopMeter(stats, COMPUTE_COMPLETION_PROPOSALS);
 
 				if (proposals != null) {
 					fLastError = computer.getErrorMessage();
 					return proposals;
 				}
 			} finally {
 				fIsReportingDelay = true;
 
 				
 			}
 			status = createAPIViolationStatus(COMPUTE_COMPLETION_PROPOSALS);
 		} catch (InvalidRegistryObjectException x) {
 			status = createExceptionStatus(x);
 		} catch (CoreException x) {
 			status = createExceptionStatus(x);
 		} catch (RuntimeException x) {
 			status = createExceptionStatus(x);
 		} finally {
 			monitor.done();
 		}
 
 		fRegistry.informUser(this, status);
 
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * Safely computes context information objects through the described
 	 * extension. If the extension is disabled, throws an exception or otherwise
 	 * does not adhere to the contract described in
 	 * {@link IScriptCompletionProposalComputer}, an empty list is returned.
 	 * 
 	 * @param context
 	 *            the invocation context passed on to the extension
 	 * @param monitor
 	 *            the progress monitor passed on to the extension
 	 * @return the list of computed context information objects (element type:
 	 *         {@link org.eclipse.jface.text.contentassist.IContextInformation})
 	 */
 	public List computeContextInformation(
 			ContentAssistInvocationContext context, IProgressMonitor monitor) {
 		if (!isEnabled())
 			return Collections.EMPTY_LIST;
 
 		IStatus status;
 		try {
 			IScriptCompletionProposalComputer computer = getComputer();
 			if (computer == null) // not active yet
 				return Collections.EMPTY_LIST;
 
 			PerformanceStats stats = startMeter(context, computer);
 			List proposals = computer.computeContextInformation(context,
 					monitor);
 			stopMeter(stats, COMPUTE_CONTEXT_INFORMATION);
 
 			if (proposals != null) {
 				fLastError = computer.getErrorMessage();
 				return proposals;
 			}
 
 			status = createAPIViolationStatus(COMPUTE_CONTEXT_INFORMATION);
 		} catch (InvalidRegistryObjectException x) {
 			status = createExceptionStatus(x);
 		} catch (CoreException x) {
 			status = createExceptionStatus(x);
 		} catch (RuntimeException x) {
 			status = createExceptionStatus(x);
 		} finally {
 			monitor.done();
 		}
 
 		fRegistry.informUser(this, status);
 
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * Notifies the described extension of a proposal computation session start.
 	 * <p>
 	 * <em>
 	 * Note: This method is called every time code assist is invoked and
 	 * is <strong>not</strong> filtered by partition type.
 	 * </em>
 	 * </p>
 	 */
 	public void sessionStarted() {
 		if (!isEnabled())
 			return;
 
 		IStatus status;
 		try {
 			IScriptCompletionProposalComputer computer = getComputer();
 			if (computer == null) // not active yet
 				return;
 
 			PerformanceStats stats = startMeter(SESSION_STARTED, computer);
 			computer.sessionStarted();
 			stopMeter(stats, SESSION_ENDED);
 
 			return;
 		} catch (InvalidRegistryObjectException x) {
 			status = createExceptionStatus(x);
 		} catch (CoreException x) {
 			status = createExceptionStatus(x);
 		} catch (RuntimeException x) {
 			status = createExceptionStatus(x);
 		}
 
 		fRegistry.informUser(this, status);
 	}
 
 	/**
 	 * Notifies the described extension of a proposal computation session end.
 	 * <p>
 	 * <em>
 	 * Note: This method is called every time code assist is invoked and
 	 * is <strong>not</strong> filtered by partition type.
 	 * </em>
 	 * </p>
 	 */
 	public void sessionEnded() {
 		if (!isEnabled())
 			return;
 
 		IStatus status;
 		try {
 			IScriptCompletionProposalComputer computer = getComputer();
 			if (computer == null) // not active yet
 				return;
 
 			PerformanceStats stats = startMeter(SESSION_ENDED, computer);
 			computer.sessionEnded();
 			stopMeter(stats, SESSION_ENDED);
 
 			return;
 		} catch (InvalidRegistryObjectException x) {
 			status = createExceptionStatus(x);
 		} catch (CoreException x) {
 			status = createExceptionStatus(x);
 		} catch (RuntimeException x) {
 			status = createExceptionStatus(x);
 		}
 
 		fRegistry.informUser(this, status);
 	}
 
 	private PerformanceStats startMeter(Object context,
 			IScriptCompletionProposalComputer computer) {
 		final PerformanceStats stats;
 		if (MEASURE_PERFORMANCE) {
 			stats = PerformanceStats.getStats(PERFORMANCE_EVENT, computer);
 			stats.startRun(context.toString());
 		} else {
 			stats = null;
 		}
 
 		if (fIsReportingDelay) {
 			fStart = System.currentTimeMillis();
 		}
 
 		return stats;
 	}
 
 	private void stopMeter(final PerformanceStats stats, String operation) {
 		if (MEASURE_PERFORMANCE) {
 			stats.endRun();
 			if (stats.isFailure()) {
 				IStatus status = createPerformanceStatus(operation);
 				fRegistry.informUser(this, status);
 				return;
 			}
 		}
 
 		if (fIsReportingDelay) {
 			long current = System.currentTimeMillis();
 			if (current - fStart > MAX_DELAY) {
 				IStatus status = createPerformanceStatus(operation);
 				fRegistry.informUser(this, status);
 			}
 		}
 	}
 
 	private IStatus createExceptionStatus(InvalidRegistryObjectException x) {
 		// extension has become invalid - log & disable
 		String blame = createBlameMessage();
 		String reason = ScriptTextMessages.CompletionProposalComputerDescriptor_reason_invalid;
 		return new Status(IStatus.INFO, DLTKUIPlugin.getPluginId(), IStatus.OK,
 				blame + " " + reason, x); //$NON-NLS-1$
 	}
 
 	private IStatus createExceptionStatus(CoreException x) {
 		// unable to instantiate the extension - log & disable
 		String blame = createBlameMessage();
 		String reason = ScriptTextMessages.CompletionProposalComputerDescriptor_reason_instantiation;
 		return new Status(IStatus.ERROR, DLTKUIPlugin.getPluginId(),
 				IStatus.OK, blame + " " + reason, x); //$NON-NLS-1$
 	}
 
 	private IStatus createExceptionStatus(RuntimeException x) {
 		// misbehaving extension - log & disable
 		String blame = createBlameMessage();
 		String reason = ScriptTextMessages.CompletionProposalComputerDescriptor_reason_runtime_ex;
 		return new Status(IStatus.WARNING, DLTKUIPlugin.getPluginId(),
 				IStatus.OK, blame + " " + reason, x); //$NON-NLS-1$
 	}
 
 	private IStatus createAPIViolationStatus(String operation) {
 		String blame = createBlameMessage();
 		Object[] args = { operation };
 		String reason = Messages
 				.format(
 						ScriptTextMessages.CompletionProposalComputerDescriptor_reason_API,
 						args);
 		return new Status(IStatus.WARNING, DLTKUIPlugin.getPluginId(),
 				IStatus.OK, blame + " " + reason, null); //$NON-NLS-1$
 	}
 
 	private IStatus createPerformanceStatus(String operation) {
 		String blame = createBlameMessage();
 		Object[] args = { operation };
 		String reason = Messages
 				.format(
 						ScriptTextMessages.CompletionProposalComputerDescriptor_reason_performance,
 						args);
 		return new Status(IStatus.WARNING, DLTKUIPlugin.getPluginId(),
 				IStatus.OK, blame + " " + reason, null); //$NON-NLS-1$
 	}
 
 	private String createBlameMessage() {
 		Object[] args = { getName(),
 				fElement.getDeclaringExtension().getContributor().getName() };
 		String disable = Messages
 				.format(
 						ScriptTextMessages.CompletionProposalComputerDescriptor_blame_message,
 						args);
 		return disable;
 	}
 
 	/**
 	 * Returns the enablement state of the described extension.
 	 * 
 	 * @return the enablement state of the described extension
 	 */
 	private boolean isEnabled() {
 		return fCategory.isEnabled();
 	}
 
 	CompletionProposalCategory getCategory() {
 		return fCategory;
 	}
 
 	/**
 	 * Returns the error message from the described extension.
 	 * 
 	 * @return the error message from the described extension
 	 */
 	public String getErrorMessage() {
 		return fLastError;
 	}
 
 	/**
 	 * Returns the contributor of the described extension.
 	 * 
 	 * @return the contributor of the described extension
 	 */
 	IContributor getContributor() {
 		try {
 			return fElement.getContributor();
 		} catch (InvalidRegistryObjectException e) {
 			return null;
 		}
 	}
 
 }
