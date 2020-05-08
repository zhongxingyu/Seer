 /*******************************************************************************
  * Copyright (c) 2008, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.match.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.EMFPlugin;
 import org.eclipse.emf.compare.match.EMFCompareMatchMessages;
 import org.eclipse.emf.compare.match.engine.GenericMatchEngine;
 import org.eclipse.emf.compare.match.engine.IMatchEngine;
 
 /* (non-javadoc) we make use of the ordering of the engines, do not change Map and List implementations. */
 /**
  * This registry will be initialized with all the match engines that could be parsed from the extension points
  * if Eclipse is running according to {@link EMFPlugin#IS_ECLIPSE_RUNNING}, else it will contain only the two
  * generic ones. Clients can add their own match engines in the registry for standalone usage.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public final class MatchEngineRegistry extends HashMap<String, List<Object>> {
 	/** Singleton instance of the registry. */
 	public static final MatchEngineRegistry INSTANCE = new MatchEngineRegistry();
 
 	/** Wild card for file extensions. */
 	private static final String ALL_EXTENSIONS = "*"; //$NON-NLS-1$
 
 	/** Name of the extension point to parse for engines. */
 	private static final String MATCH_ENGINES_EXTENSION_POINT = "org.eclipse.emf.compare.match.engine"; //$NON-NLS-1$
 
 	/** Serial version UID is used when deserializing Objects. */
 	private static final long serialVersionUID = 2237008034183610765L;
 
 	/** Externalized here to avoid too many distinct usages. */
 	private static final String TAG_ENGINE = "matchengine"; //$NON-NLS-1$
 
 	/**
 	 * As this is a singleton, hide the default constructor. Access the instance through the field
 	 * {@link #INSTANCE}.
 	 */
 	private MatchEngineRegistry() {
 		if (EMFPlugin.IS_ECLIPSE_RUNNING) {
 			parseExtensionMetadata();
 		} else {
 			// Add both generic engines
 			putValue(ALL_EXTENSIONS, new GenericMatchEngine());
 		}
 	}
 
 	/**
 	 * This will return the list of engines available for a given fileExtension. Engines must have been
 	 * registered through an extension point for this to return anything else than an empty list. Note that
 	 * engines registered against {@value #ALL_EXTENSIONS} will always be returned at the end of this list.
 	 * 
 	 * @param fileExtension
 	 *            Extension of the file we seek the matching engines for.
 	 * @return The list of available engines.
 	 */
 	public List<MatchEngineDescriptor> getDescriptors(String fileExtension) {
 		final List<Object> specific = get(fileExtension);
 		final List<Object> candidates = new ArrayList<Object>(get(ALL_EXTENSIONS));
 		if (specific != null) {
 			candidates.addAll(0, specific);
 		}
 
 		final List<MatchEngineDescriptor> engines = new ArrayList<MatchEngineDescriptor>(candidates.size());
 		for (final Object value : candidates) {
 			if (value instanceof MatchEngineDescriptor) {
 				engines.add((MatchEngineDescriptor)value);
 			}
 		}
 		return engines;
 	}
 
 	/**
 	 * Returns the highest priority {@link IMatchEngine} registered against the given file extension. Specific
 	 * engines will always come before generic ones regardless of their priority. If engines have been
 	 * manually added to the list, the latest added will be returned.
 	 * 
 	 * @param fileExtension
 	 *            The extension of the file we need a {@link IMatchEngine} for.
 	 * @return The best {@link IMatchEngine} for the given file extension.
 	 */
 	public IMatchEngine getHighestEngine(String fileExtension) {
 		final List<Object> engines = get(fileExtension);
		int highestPriority = -1;
 		IMatchEngine highest = null;
 		if (engines != null) {
 			for (final Object engine : engines) {
 				if (engine instanceof MatchEngineDescriptor) {
 					final MatchEngineDescriptor desc = (MatchEngineDescriptor)engine;
 					if (desc.getPriorityValue() > highestPriority) {
 						highest = desc.getEngineInstance();
						highestPriority = desc.getPriorityValue();
 					}
 				} else if (engine instanceof IMatchEngine) {
 					highest = (IMatchEngine)engine;
					break;
 				}
 			}
 		}
 
 		// couldn't find a specific engine, search through the generic ones
 		if (highest == null) {
 			for (final Object engine : get(ALL_EXTENSIONS)) {
 				if (engine instanceof MatchEngineDescriptor) {
 					final MatchEngineDescriptor desc = (MatchEngineDescriptor)engine;
 					if (desc.getPriorityValue() > highestPriority) {
 						highest = desc.getEngineInstance();
						highestPriority = desc.getPriorityValue();
 					}
 				} else if (engine instanceof IMatchEngine) {
 					highest = (IMatchEngine)engine;
					break;
 				}
 			}
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
 		if (value instanceof IMatchEngine || value instanceof MatchEngineDescriptor) {
 			List<Object> values = get(key);
 			if (values != null) {
 				values.add(value);
 			} else {
 				values = new ArrayList<Object>();
 				values.add(value);
 				super.put(key, values);
 			}
 		} else
 			throw new IllegalArgumentException(EMFCompareMatchMessages.getString(
 					"MatchEngineRegistry.IllegalEngine", value.getClass().getName())); //$NON-NLS-1$
 	}
 
 	/**
 	 * This will parse the given {@link IConfigurationElement configuration element} and return a descriptor
 	 * for it if it describes and engine.
 	 * 
 	 * @param configElement
 	 *            Configuration element to parse.
 	 * @return {@link MatchEngineDescriptor} wrapped around <code>configElement</code> if it describes an
 	 *         engine, <code>null</code> otherwise.
 	 */
 	private MatchEngineDescriptor parseEngine(IConfigurationElement configElement) {
 		if (!configElement.getName().equals(TAG_ENGINE))
 			return null;
 		final MatchEngineDescriptor desc = new MatchEngineDescriptor(configElement);
 		return desc;
 	}
 
 	/**
 	 * This will parse the currently running platform for extensions and store all the match engines that can
 	 * be found.
 	 */
 	private void parseExtensionMetadata() {
 		final IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(
 				MATCH_ENGINES_EXTENSION_POINT).getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			final IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
 			for (int j = 0; j < configElements.length; j++) {
 				final MatchEngineDescriptor desc = parseEngine(configElements[j]);
 				final String[] fileExtensions = desc.getFileExtension().split(","); //$NON-NLS-1$
 				for (final String ext : fileExtensions) {
 					putValue(ext, desc);
 				}
 			}
 		}
 	}
 }
