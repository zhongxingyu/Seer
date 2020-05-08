 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Zend Technologies
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.index2;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.index2.AbstractIndexer;
 import org.eclipse.dltk.core.index2.IIndexer;
 import org.eclipse.dltk.core.index2.IIndexerParticipant;
 
 /**
  * Indexer instances manager
  * 
  * @author michael
  * 
  */
 public class IndexerManager {
 
 	private static final String PARTICIPANT_POINT = DLTKCore.PLUGIN_ID
 			+ ".indexerParticipant"; //$NON-NLS-1$
 	private static final String INDEXER_POINT = DLTKCore.PLUGIN_ID + ".indexer"; //$NON-NLS-1$
 	private static final String INDEXER_ATTR = "indexer"; //$NON-NLS-1$
 	private static final String PARTICIPANT_ELEMENT = "indexerParticipant"; //$NON-NLS-1$
 	private static final String CLASS_ATTR = "class"; //$NON-NLS-1$
 	private static final String NATURE_ATTR = "nature"; //$NON-NLS-1$
 	private static final String ID_ATTR = "id"; //$NON-NLS-1$
 	private static final String TARGET_ID_ATTR = "targetId"; //$NON-NLS-1$
 
	private static IConfigurationElement indexerElement;
	private static AbstractIndexer indexer;
 	private static Map<String, Map<String, IConfigurationElement>> indexerParticipants = new HashMap<String, Map<String, IConfigurationElement>>();
 
 	static {
 		IConfigurationElement[] elements = Platform.getExtensionRegistry()
 				.getConfigurationElementsFor(INDEXER_POINT);
 		for (IConfigurationElement element : elements) {
 			String name = element.getName();
 			if (INDEXER_ATTR.equals(name)) {
				indexerElement = element;
 				break;
 			}
 		}
 
 		elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
 				PARTICIPANT_POINT);
 		for (IConfigurationElement element : elements) {
 			String name = element.getName();
 			if (PARTICIPANT_ELEMENT.equals(name)) {
 				String targetId = element.getAttribute(TARGET_ID_ATTR);
 				String nature = element.getAttribute(NATURE_ATTR);
 				if (!indexerParticipants.containsKey(targetId)) {
 					indexerParticipants.put(targetId,
 							new HashMap<String, IConfigurationElement>());
 				}
 				indexerParticipants.get(targetId).put(nature, element);
 			}
 		}
 	}
 
 	public static IIndexer getIndexer() {
		if (indexer == null && indexerElement != null) {
			try {
				indexer = (AbstractIndexer) indexerElement
 						.createExecutableExtension(CLASS_ATTR);
				indexer.setId(indexerElement.getAttribute(ID_ATTR));
			} catch (CoreException e) {
				DLTKCore.error(e);
 			}
 		}
		return indexer;
 	}
 
 	public static IIndexerParticipant getIndexerParticipant(IIndexer indexer,
 			String natureId) {
 
 		Map<String, IConfigurationElement> participants = indexerParticipants
 				.get(((AbstractIndexer) indexer).getId());
 		if (participants != null) {
 			IConfigurationElement element = participants.get(natureId);
 			if (element != null) {
 				try {
 					return (IIndexerParticipant) element
 							.createExecutableExtension(CLASS_ATTR);
 				} catch (CoreException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		return null;
 	}
 }
