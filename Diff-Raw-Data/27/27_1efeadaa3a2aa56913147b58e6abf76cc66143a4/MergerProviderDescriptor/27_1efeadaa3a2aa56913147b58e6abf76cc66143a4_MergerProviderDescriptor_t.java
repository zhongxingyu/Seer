 /*******************************************************************************
  * Copyright (c) 2006, 2007, 2008 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.diff.merge.service;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.diff.EMFCompareDiffMessages;
 import org.eclipse.emf.compare.diff.merge.api.IMergerProvider;
 import org.eclipse.emf.compare.util.EngineConstants;
 
 /**
  * This descriptor represents a merger provider contribution via the extension point
  * <code>org.eclipse.emf.compare.diff.mergerprovider</code>.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 /* package */class MergerProviderDescriptor implements Comparable<MergerProviderDescriptor> {
 	/** Configuration element of this descriptor. */
 	protected final IConfigurationElement element;
 
 	/** File extensions this merger takes into account. */
 	protected final String fileExtension;
 
 	/** Class name of the described merger provider. */
 	protected final String mergerProviderClassName;
 
 	/**
 	 * Priority of this descriptor. Should be one of
 	 * <ul>
 	 * <li>{@link EngineConstants#PRIORITY_HIGHEST}</li>
 	 * <li>{@link EngineConstants#PRIORITY_HIGH}</li>
 	 * <li>{@link EngineConstants#PRIORITY_NORMAL}</li>
 	 * <li>{@link EngineConstants#PRIORITY_LOW}</li>
 	 * <li>{@link EngineConstants#PRIORITY_LOWEST}</li>
 	 * </ul>
 	 */
 	protected final String priority;
 
 	/** {@link IMergerProvider} this descriptor describes. */
 	private IMergerProvider mergerProvider;
 
 	/**
 	 * Instantiate the descriptor given its configuration.
 	 * 
 	 * @param configuration
 	 *            {@link IConfigurationElement Configuration element} of this descriptor.
 	 */
 	public MergerProviderDescriptor(IConfigurationElement configuration) {
 		element = configuration;
 		fileExtension = getAttribute("fileExtension", "*"); //$NON-NLS-1$ //$NON-NLS-2$
 		priority = getAttribute("priority", "low"); //$NON-NLS-1$//$NON-NLS-2$
 		mergerProviderClassName = getAttribute("mergerProviderClass", null); //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	public int compareTo(MergerProviderDescriptor other) {
 		final int nombre1 = other.getPriorityValue(other.getPriority());
 		final int nombre2 = getPriorityValue(getPriority());
 		return nombre2 - nombre1;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		boolean isEqual = true;
 		if (this == obj) {
 			isEqual = true;
 		} else if (obj == null || getClass() != obj.getClass()) {
 			isEqual = false;
 		} else if (obj instanceof MergerProviderDescriptor) {
 			final MergerProviderDescriptor other = (MergerProviderDescriptor)obj;
 			if (mergerProviderClassName == null && other.mergerProviderClassName != null) {
 				isEqual = false;
 			} else if (!mergerProviderClassName.equals(other.mergerProviderClassName)) {
 				isEqual = false;
 			} else if (fileExtension == null && other.fileExtension != null) {
 				isEqual = false;
 			} else if (!fileExtension.equals(other.fileExtension)) {
 				isEqual = false;
 			} else if (priority == null && other.priority != null) {
 				isEqual = false;
 			} else if (!priority.equals(other.priority)) {
 				isEqual = false;
 			}
 		} else {
 			isEqual = false;
 		}
 		return isEqual;
 	}
 
 	/**
 	 * Returns the file extension this engine should handle.
 	 * 
 	 * @return The file extension this engine should handle.
 	 */
 	public String getFileExtension() {
 		return fileExtension;
 	}
 
 	/**
 	 * Returns the engine instance.
 	 * 
 	 * @return The engine instance.
 	 */
 	public IMergerProvider getMergerProviderInstance() {
 		if (mergerProvider == null) {
 			try {
 				mergerProvider = (IMergerProvider)element.createExecutableExtension("mergerProviderClass"); //$NON-NLS-1$
 			} catch (CoreException e) {
 				EMFComparePlugin.log(e, false);
 			}
 		}
 		return mergerProvider;
 	}
 
 	/**
 	 * Returns the engine priority.
 	 * 
 	 * @return The engine priority.
 	 */
 	public String getPriority() {
 		return priority.toLowerCase();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int classNameHash = 0;
 		if (mergerProviderClassName != null)
 			classNameHash = mergerProviderClassName.hashCode();
 		int extensionHash = 0;
 		if (fileExtension != null)
 			extensionHash = fileExtension.hashCode();
 		int priorityHash = 0;
 		if (priority != null)
 			priorityHash = priority.hashCode();
 
 		return (((prime + classNameHash) * prime) + extensionHash) * prime + priorityHash;
 	}
 
 	/*
 	 * created as package visibility method to allow access from MergeFactory
 	 */
 	/**
 	 * Returns the value of the priority described by the given {@link String}.<br/>Returned values
 	 * according to <code>priorityString</code> value :
 	 * <ul>
 	 * <li>&quot;lowest&quot; =&gt; {@value EngineConstants#PRIORITY_LOWEST}</li>
 	 * <li>&quot;low&quot; =&gt; {@value EngineConstants#PRIORITY_LOW}</li>
 	 * <li>&quot;high&quot; =&gt; {@value EngineConstants#PRIORITY_HIGH}</li>
 	 * <li>&quot;highest&quot; =&gt; {@value EngineConstants#PRIORITY_HIGHEST}</li>
 	 * <li>anything else =&gt; {@value EngineConstants#PRIORITY_NORMAL}</li>
 	 * </ul>
 	 * 
 	 * @param priorityString
 	 *            {@link String} value of the priority we seek.
 	 * @return <code>int</code> corresponding to the given priority {@link String}.
 	 */
 	/* package */int getPriorityValue(String priorityString) {
 		if (priorityString == null)
 			throw new IllegalArgumentException(EMFCompareDiffMessages.getString("Descriptor.IllegalPriority")); //$NON-NLS-1$
 		int priorityValue = EngineConstants.PRIORITY_NORMAL;
		if ("lowest".equals(priorityString)) { //$NON-NLS-1$
 			priorityValue = EngineConstants.PRIORITY_LOWEST;
		} else if ("low".equals(priorityString)) { //$NON-NLS-1$
 			priorityValue = EngineConstants.PRIORITY_LOW;
		} else if ("high".equals(priorityString)) { //$NON-NLS-1$
 			priorityValue = EngineConstants.PRIORITY_HIGH;
		} else if ("highest".equals(priorityString)) { //$NON-NLS-1$
 			priorityValue = EngineConstants.PRIORITY_HIGHEST;
 		}
 		return priorityValue;
 	}
 
 	/**
 	 * Returns the value of the attribute <code>name</code> of this descriptor's configuration element. if
 	 * the attribute hasn't been set, we'll return <code>defaultValue</code> instead.
 	 * 
 	 * @param name
 	 *            Name of the attribute we seek the value of.
 	 * @param defaultValue
 	 *            Value to return if the attribute hasn't been set.
 	 * @return The value of the attribute <code>name</code>, <code>defaultValue</code> if it hasn't been
 	 *         set.
 	 */
 	private String getAttribute(String name, String defaultValue) {
 		final String value = element.getAttribute(name);
 		if (value != null)
 			return value;
 		if (defaultValue != null)
 			return defaultValue;
 		throw new IllegalArgumentException(EMFCompareDiffMessages.getString(
 				"Descriptor.MissingAttribute", name)); //$NON-NLS-1$
 	}
 }
