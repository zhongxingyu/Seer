 /*******************************************************************************
  * Copyright (c) 2012, 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.text;
 
 import static com.google.common.collect.Iterables.filter;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.io.ByteStreams;
 import com.google.common.io.Closeables;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.io.UnsupportedEncodingException;
 import java.util.Collections;
 import java.util.EventObject;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareNavigator;
 import org.eclipse.compare.ICompareNavigator;
 import org.eclipse.compare.IStreamContentAccessor;
 import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
 import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
 import org.eclipse.compare.internal.CompareHandlerService;
 import org.eclipse.compare.internal.MergeSourceViewer;
 import org.eclipse.compare.internal.Utilities;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.AttributeChange;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Conflict;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.command.ICompareCommandStack;
 import org.eclipse.emf.compare.command.ICompareCopyCommand;
 import org.eclipse.emf.compare.domain.ICompareEditingDomain;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareIDEUIPlugin;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.DynamicObject;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.RedoAction;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.UndoAction;
 import org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.provider.AttributeChangeNode;
 import org.eclipse.emf.compare.ide.ui.internal.util.SWTUtil;
 import org.eclipse.emf.compare.rcp.EMFCompareRCPPlugin;
 import org.eclipse.emf.compare.rcp.ui.internal.EMFCompareConstants;
 import org.eclipse.emf.compare.utils.EMFComparePredicates;
 import org.eclipse.emf.compare.utils.IEqualityHelper;
 import org.eclipse.emf.compare.utils.ReferenceUtil;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.change.util.ChangeRecorder;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.command.ChangeCommand;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.actions.ActionFactory;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class EMFCompareTextMergeViewer extends TextMergeViewer implements IPropertyChangeListener, CommandStackListener {
 
 	private static final String BUNDLE_NAME = EMFCompareTextMergeViewer.class.getName();
 
 	private DynamicObject fDynamicObject;
 
 	private ActionContributionItem fCopyDiffLeftToRightItem;
 
 	private ActionContributionItem fCopyDiffRightToLeftItem;
 
 	private final UndoAction fUndoAction;
 
 	private final RedoAction fRedoAction;
 
 	private final DelayedExecutor fDelayedExecutor;
 
 	private final ScheduledExecutorService fExecutorService;
 
 	/**
 	 * @param parent
 	 * @param configuration
 	 */
 	public EMFCompareTextMergeViewer(Composite parent, CompareConfiguration configuration) {
 		super(parent, configuration);
 		setContentProvider(new EMFCompareTextMergeViewerContentProvider(configuration));
 
 		fRedoAction = new RedoAction();
 		fUndoAction = new UndoAction();
 
 		fExecutorService = Executors.newSingleThreadScheduledExecutor();
 		fDelayedExecutor = new DelayedExecutor(fExecutorService);
 
 		editingDomainChange(null, getEditingDomain());
 		configuration.addPropertyChangeListener(this);
 	}
 
 	public void propertyChange(PropertyChangeEvent event) {
 		if (EMFCompareConstants.EDITING_DOMAIN.equals(event.getProperty())) {
 			editingDomainChange((ICompareEditingDomain)event.getOldValue(), (ICompareEditingDomain)event
 					.getNewValue());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.TextMergeViewer#flushContent(java.lang.Object,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	protected void flushContent(Object oldInput, IProgressMonitor monitor) {
 		try {
 			fExecutorService.awaitTermination(1, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			EMFCompareIDEUIPlugin.getDefault().log(e);
 		}
 		super.flushContent(oldInput, monitor);
 	}
 
 	/**
 	 * @param oldValue
 	 * @param newValue
 	 */
 	protected void editingDomainChange(ICompareEditingDomain oldValue, ICompareEditingDomain newValue) {
 		if (oldValue != null) {
 			oldValue.getCommandStack().removeCommandStackListener(this);
 		}
 		if (newValue != oldValue) {
 			if (newValue != null) {
 				ICompareCommandStack commandStack = newValue.getCommandStack();
 				commandStack.addCommandStackListener(this);
 				setLeftDirty(commandStack.isLeftSaveNeeded());
 				setRightDirty(commandStack.isRightSaveNeeded());
 			}
 			fUndoAction.setEditingDomain(newValue);
 			fRedoAction.setEditingDomain(newValue);
 		}
 	}
 
 	@SuppressWarnings("resource")
 	public void commandStackChanged(EventObject event) {
 		if (fUndoAction != null) {
 			fUndoAction.update();
 		}
 		if (fRedoAction != null) {
 			fRedoAction.update();
 		}
 		if (getEditingDomain() != null) {
 			ICompareCommandStack commandStack = getEditingDomain().getCommandStack();
 			setLeftDirty(commandStack.isLeftSaveNeeded());
 			setRightDirty(commandStack.isRightSaveNeeded());
 		}
 
 		IMergeViewerContentProvider contentProvider = (IMergeViewerContentProvider)getContentProvider();
 
 		final String leftValueFromModel = getString((IStreamContentAccessor)contentProvider
 				.getLeftContent(getInput()));
 		final String rightValueFromModel = getString((IStreamContentAccessor)contentProvider
 				.getRightContent(getInput()));
 
 		SWTUtil.safeAsyncExec(new Runnable() {
 			public void run() {
				String leftValueFromWidget = getContents(true, Charsets.UTF_8.name());
				String rightValueFromWidget = getContents(false, Charsets.UTF_8.name());
 				IEqualityHelper equalityHelper = getComparison().getEqualityHelper();
 				if (!equalityHelper.matchingAttributeValues(leftValueFromModel, leftValueFromWidget)
 						|| !equalityHelper.matchingAttributeValues(rightValueFromModel, rightValueFromWidget)) {
 					// only refresh if values are different to avoid select-all of the text.
 					refresh();
 				}
 			}
 		});
 	}
 
 	@SuppressWarnings("resource")
 	// closed by Closeables
 	private String getString(IStreamContentAccessor contentAccessor) {
 		String ret = null;
 		InputStream content = null;
 		try {
 			content = contentAccessor.getContents();
			ret = new String(ByteStreams.toByteArray(content), Charsets.UTF_8.name());
 		} catch (CoreException e) {
 		} catch (IOException e) {
 		} finally {
 			Closeables.closeQuietly(content);
 		}
 		return ret;
 	}
 
 	/**
 	 * @return the fEditingDomain
 	 */
 	protected final ICompareEditingDomain getEditingDomain() {
 		return (ICompareEditingDomain)getCompareConfiguration().getProperty(
 				EMFCompareConstants.EDITING_DOMAIN);
 	}
 
 	/**
 	 * @return the fComparison
 	 */
 	protected final Comparison getComparison() {
 		return (Comparison)getCompareConfiguration().getProperty(EMFCompareConstants.COMPARE_RESULT);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.TextMergeViewer#copy(boolean)
 	 */
 	@Override
 	protected void copy(final boolean leftToRight) {
 		final List<Diff> differences;
 
 		if (getComparison().isThreeWay()) {
 			differences = ImmutableList.copyOf(filter(getComparison().getDifferences(),
 					new Predicate<Diff>() {
 						public boolean apply(Diff diff) {
 							final boolean unresolved = diff.getState() == DifferenceState.UNRESOLVED;
 							final boolean nonConflictual = diff.getConflict() == null;
 							final boolean fromLeftToRight = leftToRight
 									&& diff.getSource() == DifferenceSource.LEFT;
 							final boolean fromRightToLeft = !leftToRight
 									&& diff.getSource() == DifferenceSource.RIGHT;
 							return unresolved && nonConflictual && (fromLeftToRight || fromRightToLeft);
 						}
 					}));
 		} else {
 			differences = ImmutableList.copyOf(filter(getComparison().getDifferences(), EMFComparePredicates
 					.hasState(DifferenceState.UNRESOLVED)));
 		}
 
 		if (differences.size() > 0) {
 			final Command copyCommand = getEditingDomain().createCopyCommand(differences, leftToRight,
 					EMFCompareRCPPlugin.getDefault().getMergerRegistry());
 
 			getEditingDomain().getCommandStack().execute(copyCommand);
 			refresh();
 		}
 	}
 
 	protected void copyDiff(boolean leftToRight) {
 		Object input = getInput();
 		if (input instanceof AttributeChangeNode) {
 			final AttributeChange attributeChange = ((AttributeChangeNode)input).getTarget();
 
 			final Command copyCommand = getEditingDomain().createCopyCommand(
 					Collections.singletonList(attributeChange), leftToRight,
 					EMFCompareRCPPlugin.getDefault().getMergerRegistry());
 			getEditingDomain().getCommandStack().execute(copyCommand);
 
 			refresh();
 		}
 	}
 
 	/**
 	 * Inhibits this method to avoid asking to save on each input change!!
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#doSave(java.lang.Object,
 	 *      java.lang.Object)
 	 */
 	@Override
 	protected boolean doSave(Object newInput, Object oldInput) {
 		return false;
 	}
 
	protected String getContents(boolean isLeft, String charsetName) {
		try {
			return new String(getContents(isLeft), Charsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			// UTF_8 is a standard charset guaranteed to be supported by all Java platform
			// implementations
		}
		return null; // can not happen.
	}

 	private void updateModel(final AttributeChange diff, final EAttribute eAttribute,
 			final IEqualityHelper equalityHelper, final EObject eObject, boolean isLeft) {
 		final String oldValue = getStringValue(eObject, eAttribute);
		String newValue = getContents(isLeft, Charsets.UTF_8.name());
 
 		final boolean oldAndNewEquals = equalityHelper.matchingAttributeValues(newValue, oldValue);
 		if (eObject != null && !oldAndNewEquals && getCompareConfiguration().isLeftEditable()) {
 			// Save the change on left side
 			getEditingDomain().getCommandStack().execute(
 					new UpdateModelAndRejectDiffCommand(getEditingDomain().getChangeRecorder(), eObject,
 							eAttribute, newValue, diff, isLeft));
 		}
 	}
 
 	private String getStringValue(final EObject eObject, final EAttribute eAttribute) {
 		final EDataType eAttributeType = eAttribute.getEAttributeType();
 		final Object value;
 		if (eObject == null) {
 			value = null;
 		} else {
 			value = ReferenceUtil.safeEGet(eObject, eAttribute);
 		}
 		return EcoreUtil.convertToString(eAttributeType, value);
 	}
 
 	/**
 	 * @return the fDynamicObject
 	 */
 	public DynamicObject getDynamicObject() {
 		if (fDynamicObject == null) {
 			this.fDynamicObject = new DynamicObject(this);
 		}
 		return fDynamicObject;
 	}
 
 	@SuppressWarnings("restriction")
 	protected final MergeSourceViewer getAncestorSourceViewer() {
 		return (MergeSourceViewer)getDynamicObject().get("fAncestor"); //$NON-NLS-1$
 	}
 
 	@SuppressWarnings("restriction")
 	protected final MergeSourceViewer getLeftSourceViewer() {
 		return (MergeSourceViewer)getDynamicObject().get("fLeft"); //$NON-NLS-1$
 	}
 
 	@SuppressWarnings("restriction")
 	protected final MergeSourceViewer getRightSourceViewer() {
 		return (MergeSourceViewer)getDynamicObject().get("fRight"); //$NON-NLS-1$
 	}
 
 	@SuppressWarnings("restriction")
 	protected final CompareHandlerService getHandlerService() {
 		return (CompareHandlerService)getDynamicObject().get("fHandlerService"); //$NON-NLS-1$
 	}
 
 	protected final void setHandlerService(@SuppressWarnings("restriction") CompareHandlerService service) {
 		getDynamicObject().set("fHandlerService", service); //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.TextMergeViewer#createControls(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	protected void createControls(Composite composite) {
 		super.createControls(composite);
 		attachListeners(getAncestorSourceViewer());
 		attachListeners(getLeftSourceViewer());
 		attachListeners(getRightSourceViewer());
 	}
 
 	protected void attachListeners(final MergeSourceViewer viewer) {
 		final StyledText textWidget = viewer.getSourceViewer().getTextWidget();
 		textWidget.addFocusListener(new FocusListener() {
 
 			public void focusLost(FocusEvent e) {
 				setActiveViewer(viewer, false);
 			}
 
 			public void focusGained(FocusEvent e) {
 				setActiveViewer(viewer, true);
 			}
 		});
 
 		textWidget.addVerifyListener(new VerifyListener() {
 			public void verifyText(VerifyEvent e) {
 				fDelayedExecutor.schedule(new Runnable() {
 					public void run() {
 						// When we leave the current input
 						Object oldInput = getInput();
 						if (oldInput instanceof AttributeChangeNode) {
 							final AttributeChange diff = ((AttributeChangeNode)oldInput).getTarget();
 							final EAttribute eAttribute = diff.getAttribute();
 							final Match match = diff.getMatch();
 							final IEqualityHelper equalityHelper = match.getComparison().getEqualityHelper();
 
 							updateModel(diff, eAttribute, equalityHelper, match.getLeft(), true);
 							updateModel(diff, eAttribute, equalityHelper, match.getRight(), false);
 						}
 					}
 				});
 			}
 		});
 	}
 
 	private void setActiveViewer(MergeSourceViewer viewer, boolean activate) {
 		// connectContributedActions(viewer, activate);
 		if (activate) {
 			// fFocusPart = viewer;
 			connectGlobalActions(viewer);
 		} else {
 			connectGlobalActions(null);
 		}
 	}
 
 	private void connectGlobalActions(final MergeSourceViewer part) {
 		if (getHandlerService() != null) {
 			if (part != null) {
 				part.updateActions();
 			}
 			getHandlerService().updatePaneActionHandlers(new Runnable() {
 				public void run() {
 					if (part == null) {
 						getHandlerService().setGlobalActionHandler(ActionFactory.UNDO.getId(), null);
 						getHandlerService().setGlobalActionHandler(ActionFactory.REDO.getId(), null);
 					} else {
 						getHandlerService().setGlobalActionHandler(ActionFactory.UNDO.getId(), fUndoAction);
 						getHandlerService().setGlobalActionHandler(ActionFactory.REDO.getId(), fRedoAction);
 					}
 				}
 			});
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.TextMergeViewer#createToolItems(org.eclipse.jface.action.ToolBarManager)
 	 */
 	@SuppressWarnings("restriction")
 	@Override
 	protected void createToolItems(ToolBarManager toolBarManager) {
 		// avoid super to avoid NPE in org.eclipse.compare.internal.ViewerDescriptor.createViewer
 		CompareHandlerService handlerService = CompareHandlerService.createFor(getCompareConfiguration()
 				.getContainer(), getLeftSourceViewer().getSourceViewer().getControl().getShell());
 		setHandlerService(handlerService);
 
 		// Copy actions
 		CompareConfiguration cc = getCompareConfiguration();
 		if (cc.isRightEditable()) {
 			Action copyLeftToRight = new Action() {
 				@Override
 				public void run() {
 					copyDiff(true);
 					navigate(true);
 				}
 			};
 			Utilities.initAction(copyLeftToRight, getResourceBundle(), "action.CopyDiffLeftToRight."); //$NON-NLS-1$
 			fCopyDiffLeftToRightItem = new ActionContributionItem(copyLeftToRight);
 			fCopyDiffLeftToRightItem.setVisible(true);
 			toolBarManager.appendToGroup("merge", fCopyDiffLeftToRightItem); //$NON-NLS-1$
 			handlerService.registerAction(copyLeftToRight, "org.eclipse.compare.copyLeftToRight"); //$NON-NLS-1$
 		}
 
 		if (cc.isLeftEditable()) {
 			Action copyRightToLeft = new Action() {
 				@Override
 				public void run() {
 					copyDiff(false);
 					navigate(true);
 				}
 			};
 			Utilities.initAction(copyRightToLeft, getResourceBundle(), "action.CopyDiffRightToLeft."); //$NON-NLS-1$
 			fCopyDiffRightToLeftItem = new ActionContributionItem(copyRightToLeft);
 			fCopyDiffRightToLeftItem.setVisible(true);
 			toolBarManager.appendToGroup("merge", fCopyDiffRightToLeftItem); //$NON-NLS-1$
 			handlerService.registerAction(copyRightToLeft, "org.eclipse.compare.copyRightToLeft"); //$NON-NLS-1$
 		}
 
 		// Navigation
 		final Action nextDiff = new Action() {
 			@Override
 			public void run() {
 				endOfContentReached(true);
 			}
 		};
 		Utilities.initAction(nextDiff, getResourceBundle(), "action.NextDiff.");
 		ActionContributionItem contributionNextDiff = new ActionContributionItem(nextDiff);
 		contributionNextDiff.setVisible(true);
 		toolBarManager.appendToGroup("navigation", contributionNextDiff);
 
 		final Action previousDiff = new Action() {
 			@Override
 			public void run() {
 				endOfContentReached(false);
 			}
 		};
 		Utilities.initAction(previousDiff, getResourceBundle(), "action.PrevDiff.");
 		ActionContributionItem contributionPreviousDiff = new ActionContributionItem(previousDiff);
 		contributionPreviousDiff.setVisible(true);
 		toolBarManager.appendToGroup("navigation", contributionPreviousDiff);
 	}
 
 	/**
 	 * Called by the framework when the last (or first) diff of the current content viewer has been reached.
 	 * This will open the content viewer for the next (or previous) diff displayed in the structure viewer.
 	 * 
 	 * @param next
 	 *            <code>true</code> if we are to open the next structure viewer's diff, <code>false</code> if
 	 *            we should go to the previous instead.
 	 */
 	protected void endOfContentReached(boolean next) {
 		final Control control = getControl();
 		if (control != null && !control.isDisposed()) {
 			final ICompareNavigator navigator = getCompareConfiguration().getContainer().getNavigator();
 			if (navigator instanceof CompareNavigator && ((CompareNavigator)navigator).hasChange(next)) {
 				navigator.selectChange(next);
 			}
 		}
 	}
 
 	/**
 	 * Called by the framework to navigate to the next (or previous) difference. This will open the content
 	 * viewer for the next (or previous) diff displayed in the structure viewer.
 	 * 
 	 * @param next
 	 *            <code>true</code> if we are to open the next structure viewer's diff, <code>false</code> if
 	 *            we should go to the previous instead.
 	 */
 	protected void navigate(boolean next) {
 		final Control control = getControl();
 		if (control != null && !control.isDisposed()) {
 			final ICompareNavigator navigator = getCompareConfiguration().getContainer().getNavigator();
 			if (navigator instanceof CompareNavigator && ((CompareNavigator)navigator).hasChange(next)) {
 				navigator.selectChange(next);
 			}
 		}
 	}
 
 	@Override
 	protected ResourceBundle getResourceBundle() {
 		return ResourceBundle.getBundle(BUNDLE_NAME);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.TextMergeViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
 	 */
 	@Override
 	protected void handleDispose(DisposeEvent event) {
 		try {
 			fExecutorService.awaitTermination(1, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			EMFCompareIDEUIPlugin.getDefault().log(e);
 		}
 		fExecutorService.shutdown();
 
 		getCompareConfiguration().removePropertyChangeListener(this);
 
 		editingDomainChange(getEditingDomain(), null);
 
 		super.handleDispose(event);
 	}
 
 	/**
 	 * Command to directly modify the semantic model and reject the related difference.
 	 * 
 	 * @author cnotot
 	 */
 	private static class UpdateModelAndRejectDiffCommand extends ChangeCommand implements ICompareCopyCommand {
 
 		private boolean isLeft;
 
 		private Diff difference;
 
 		private Object value;
 
 		private EStructuralFeature feature;
 
 		private EObject owner;
 
 		public UpdateModelAndRejectDiffCommand(ChangeRecorder changeRecorder, EObject owner,
 				EStructuralFeature feature, Object value, Diff difference, boolean isLeft) {
 			super(changeRecorder, ImmutableSet.<Notifier> builder().add(owner).addAll(
 					getAffectedDiff(difference)).build());
 			this.owner = owner;
 			this.feature = feature;
 			this.value = value;
 			this.difference = difference;
 			this.isLeft = isLeft;
 		}
 
 		@Override
 		public void doExecute() {
 			owner.eSet(feature, value);
 			for (Diff affectedDiff : getAffectedDiff(difference)) {
 				affectedDiff.setState(DifferenceState.DISCARDED);
 			}
 		}
 
 		private static Set<Diff> getAffectedDiff(Diff diff) {
 			EList<Conflict> conflicts = diff.getMatch().getComparison().getConflicts();
 			for (Conflict conflict : conflicts) {
 				EList<Diff> conflictualDifferences = conflict.getDifferences();
 				if (conflictualDifferences.contains(diff)) {
 					return ImmutableSet.copyOf(conflictualDifferences);
 				}
 			}
 			return ImmutableSet.of(diff);
 		}
 
 		public boolean isLeftToRight() {
 			return !isLeft;
 		}
 
 	}
 
 }
