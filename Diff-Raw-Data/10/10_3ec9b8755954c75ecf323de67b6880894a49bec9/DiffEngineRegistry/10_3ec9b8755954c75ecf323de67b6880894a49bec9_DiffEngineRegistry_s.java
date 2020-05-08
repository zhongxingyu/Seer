 /*******************************************************************************
  * Copyright (c) 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.diff.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.EMFPlugin;
 import org.eclipse.emf.compare.diff.EMFCompareDiffMessages;
 import org.eclipse.emf.compare.diff.engine.GenericDiffEngine;
 import org.eclipse.emf.compare.diff.engine.IDiffEngine;
import org.eclipse.emf.compare.match.engine.IMatchEngine;
 import org.eclipse.emf.compare.util.ModelIdentifier;
 
 /* (non-javadoc) we make use of the ordering of the engines, do not change Map and List implementations. */
 /**
  * This registry will be initialized with all the diff engines that could be parsed from the extension points
  * if Eclipse is running according to {@link EMFPlugin#IS_ECLIPSE_RUNNING}, else it will contain only the two
  * generic ones. Clients can add their own diff engines in the registry for standalone usage.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public final class DiffEngineRegistry extends HashMap<String, List<Object>> {
 	/** Singleton instance of the registry. */
 	public static final DiffEngineRegistry INSTANCE = new DiffEngineRegistry();
 
 	/** Name of the extension point to parse for engines. */
 	private static final String DIFF_ENGINES_EXTENSION_POINT = "org.eclipse.emf.compare.diff.engine"; //$NON-NLS-1$
 
 	/** Separator for extension Metadata attributes. */
 	private static final String SEPARATOR = ","; //$NON-NLS-1$
 
 	/** Serial version UID is used when deserializing Objects. */
 	private static final long serialVersionUID = 2237008034183610765L;
 
 	/** Externalized here to avoid too many distinct usages. */
 	private static final String TAG_ENGINE = "diffengine"; //$NON-NLS-1$
 
 	/** Wild card for file extensions. */
 	private static final String WILDCARD = "*"; //$NON-NLS-1$
 
 	/** Store the engine descriptors associated by a namespace pattern. */
 	private final List<DiffEngineDescriptor> nsPatternDescriptors = new ArrayList<DiffEngineDescriptor>();
 
 	/**
 	 * As this is a singleton, hide the default constructor. Access the instance through the field
 	 * {@link #INSTANCE}.
 	 */
 	private DiffEngineRegistry() {
 		if (EMFPlugin.IS_ECLIPSE_RUNNING) {
 			parseExtensionMetadata();
 		} else {
 			// Add both generic engines
 			putValue(WILDCARD, new GenericDiffEngine());
 		}
 	}
 
 	/**
 	 * This will return the list of engine descriptors available for a given model identifier. Engines must
 	 * have been registered through an extension point for this to return anything else than an empty list.
 	 * Note that engines registered against {@value #WILDCARD} will always be returned at the end of this
 	 * list.
 	 * 
 	 * @param identifier
 	 *            {@link ModelIdentifier} we seek the matching engines for.
 	 * @return The list of available {@link DiffEngineDescriptor}.
 	 * @since 1.1
 	 */
 	public List<DiffEngineDescriptor> getDescriptors(ModelIdentifier identifier) {
 		final List<Object> candidates = getEnginesForIdentifier(identifier);
 
 		candidates.addAll(get(WILDCARD));
 
 		final List<DiffEngineDescriptor> engines = new ArrayList<DiffEngineDescriptor>(candidates.size());
 		for (final Object value : candidates) {
 			if (value instanceof DiffEngineDescriptor) {
 				engines.add((DiffEngineDescriptor)value);
 			}
 		}
 
 		return engines;
 	}
 
 	/**
 	 * This will return the list of engines available for a given engine identifier. Engines must have been
 	 * registered through an extension point for this to return anything else than an empty list. Note that
 	 * engines registered against {@value #WILDCARD} will always be returned at the end of this list.
 	 * 
 	 * @param engineIdentifier
 	 *            Engine identifier we seek the differencing engines for.<br/>
 	 *            An engine identifier is a String that can describe either a file extension, a content-type
 	 *            or a namespace.
 	 * @return The list of available engines.
 	 * @deprecated use {@link DiffEngineRegistry#getDescriptors(ModelIdentifier)} instead.
 	 */
 	@Deprecated
 	public List<DiffEngineDescriptor> getDescriptors(String engineIdentifier) {
 		final List<Object> specific = get(engineIdentifier);
 		final List<Object> candidates = new ArrayList<Object>(get(WILDCARD));
 		if (specific != null) {
 			candidates.addAll(0, specific);
 		}
 
 		final List<DiffEngineDescriptor> engines = new ArrayList<DiffEngineDescriptor>(candidates.size());
 		for (final Object value : candidates) {
 			if (value instanceof DiffEngineDescriptor) {
 				engines.add((DiffEngineDescriptor)value);
 			}
 		}
 		return engines;
 	}
 
 	/**
 	 * Returns the highest priority {@link IDiffEngine} registered against the given model identifiers.
 	 * Specific engines will always come before generic ones regardless of their priority. If engines have
 	 * been manually added to the list, the latest added will be returned.
 	 * 
 	 * @param identifier
 	 *            {@link ModelIdentifier} to search on the registered {@link IDiffEngine}
 	 * @return The best {@link IDiffEngine} for the given engine identifiers.
 	 * @since 1.1
 	 */
 	public IDiffEngine getHighestEngine(ModelIdentifier identifier) {
 		IDiffEngine highest = null;
 
 		final List<Object> engines = getEnginesForIdentifier(identifier);
 
 		if (engines.size() != 0) {
 			highest = getSpecificHighestEngine(engines);
 		}
 
 		// couldn't find a specific engine, search through the generic ones
 		if (highest == null) {
 			highest = getSpecificHighestEngine(get(WILDCARD));
 		}
 		return highest;
 	}
 
 	/**
 	 * Returns the highest priority {@link IDiffEngine} registered against the given engine identifier.
 	 * Specific engines will always come before generic ones regardless of their priority. If engines have
 	 * been manually added to the list, the latest added will be returned.
 	 * 
 	 * @param engineIdentifier
	 *            An engine identifier to search on the registered {@link IMatchEngine}.<br/>
 	 *            An engine identifier is a String that can describe either a file extension, a content-type
 	 *            or a namespace.
 	 * @return The best {@link IDiffEngine} for the given file extension.
 	 * @deprecated use {@link DiffEngineRegistry#getDescriptors(ModelIdentifier)} instead.
 	 */
 	@Deprecated
 	public IDiffEngine getHighestEngine(String engineIdentifier) {
 		final List<Object> engines = get(engineIdentifier);
 		IDiffEngine highest = null;
 		if (engines != null) {
 			highest = getSpecificHighestEngine(engines);
 		}
 
 		// couldn't find a specific engine, search through the generic ones
 		if (highest == null) {
 			highest = getSpecificHighestEngine(get(WILDCARD));
 		}
 		return highest;
 	}
 
 	/**
 	 * Adds the given value in the list of engines known for the given extension.
 	 * 
 	 * @param key
 	 *            The file extension we wish to add an engine for.
 	 * @param value
 	 *            Engine to be added.
 	 */
 	public void putValue(String key, Object value) {
 		if (value instanceof IDiffEngine || value instanceof DiffEngineDescriptor) {
 			List<Object> values = get(key);
 			if (values != null) {
 				values.add(value);
 			} else {
 				values = new ArrayList<Object>();
 				values.add(value);
 				super.put(key, values);
 			}
 		} else
 			throw new IllegalArgumentException(EMFCompareDiffMessages.getString(
 					"DiffEngineRegistry.IllegalEngine", value.getClass().getName())); //$NON-NLS-1$
 	}
 
 	/**
 	 * Retrieves the engines that correspond to the given identifier.
 	 * 
 	 * @param identifier
 	 *            the {@link ModelIdentifier} we seek engines for.
 	 * @return a list of {@link IDiffEngine} and {@link DiffEngineDescriptor}
 	 */
 	private List<Object> getEnginesForIdentifier(ModelIdentifier identifier) {
 		final List<Object> candidates = new ArrayList<Object>();
 		List<Object> newCandidates;
 
 		if (identifier.getNamespace() != null) {
 			newCandidates = get(identifier.getNamespace());
 			if (newCandidates != null) {
 				candidates.addAll(newCandidates);
 			}
 
 			for (DiffEngineDescriptor desc : nsPatternDescriptors) {
 				if (identifier.getNamespace().matches(desc.getNamespacePattern())) {
 					candidates.add(desc);
 				}
 			}
 		}
 
 		if (identifier.getContentType() != null) {
 			newCandidates = get(identifier.getContentType());
 			if (newCandidates != null) {
 				candidates.addAll(newCandidates);
 			}
 		}
 
 		if (identifier.getExtension() != null) {
 			newCandidates = get(identifier.getExtension());
 			if (newCandidates != null) {
 				candidates.addAll(newCandidates);
 			}
 		}
 
 		return candidates;
 	}
 
 	/**
 	 * Returns the highest priority {@link IDiffEngine} registered among the given engine list. If engines
 	 * have been manually added to the list, the latest added will be returned.
 	 * 
 	 * @param engines
 	 *            List of engines.
	 * @return The best {@link IMatchEngine} for the given engine list.
 	 */
 	private IDiffEngine getSpecificHighestEngine(List<Object> engines) {
 		int highestPriority = -1;
 		IDiffEngine highest = null;
 
 		for (final Object engine : engines) {
 			if (engine instanceof DiffEngineDescriptor) {
 				final DiffEngineDescriptor desc = (DiffEngineDescriptor)engine;
 				if (desc.getPriorityValue() > highestPriority) {
 					highest = desc.getEngineInstance();
 					highestPriority = desc.getPriorityValue();
 				}
			} else if (engine instanceof IMatchEngine) {
 				highest = (IDiffEngine)engine;
 			}
 		}
 
 		return highest;
 	}
 
 	/**
 	 * This will parse the given {@link IConfigurationElement configuration element} and return a descriptor
 	 * for it if it describes an engine.
 	 * 
 	 * @param configElement
 	 *            Configuration element to parse.
 	 * @return {@link DiffEngineDescriptor} wrapped around <code>configElement</code> if it describes an
 	 *         engine, <code>null</code> otherwise.
 	 */
 	private DiffEngineDescriptor parseEngine(IConfigurationElement configElement) {
 		if (!configElement.getName().equals(TAG_ENGINE))
 			return null;
 		final DiffEngineDescriptor desc = new DiffEngineDescriptor(configElement);
 		return desc;
 	}
 
 	/**
 	 * This will parse the currently running platform for extensions and store all the diff engines that can
 	 * be found.
 	 */
 	private void parseExtensionMetadata() {
 		final IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(
 				DIFF_ENGINES_EXTENSION_POINT).getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			final IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
 			for (int j = 0; j < configElements.length; j++) {
 				final DiffEngineDescriptor desc = parseEngine(configElements[j]);
 
 				final String namespacePattern = desc.getNamespacePattern();
 				if (!"".equals(namespacePattern)) { //$NON-NLS-1$
 					nsPatternDescriptors.add(desc);
 				}
 
 				final String[] namespaces = desc.getNamespace().split(SEPARATOR);
 				for (final String ns : namespaces) {
 					if (!"".equals(ns)) { //$NON-NLS-1$
 						putValue(ns, desc);
 					}
 				}
 
 				final String[] contentTypes = desc.getContentType().split(SEPARATOR);
 				for (final String ct : contentTypes) {
 					if (!"".equals(ct)) { //$NON-NLS-1$
 						putValue(ct, desc);
 					}
 				}
 
 				final String[] fileExtensions = desc.getFileExtension().split(SEPARATOR);
 				for (final String ext : fileExtensions) {
 					if (!"".equals(ext)) { //$NON-NLS-1$
 						putValue(ext, desc);
 					}
 				}
 			}
 		}
 	}
 }
