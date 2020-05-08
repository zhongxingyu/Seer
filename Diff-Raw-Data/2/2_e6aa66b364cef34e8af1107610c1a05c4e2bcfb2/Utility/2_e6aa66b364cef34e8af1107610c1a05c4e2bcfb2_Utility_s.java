 // Utility.java
 package org.eclipse.stem.core;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.stem.core.common.Identifiable;
 
 /**
  * This class contains common utility methods used in STEM.
  */
 public class Utility {
 
 	/**
 	 * EMF Save Options, for properly serializing to UTF-8
 	 */
 	private static final Map<String,String> EMF_SAVE_OPTIONS 
 		= new HashMap<String,String>();
 	static {
		EMF_SAVE_OPTIONS.put(XMLResource.OPTION_ENCODING, "UTF8");
 	}
 	
 	/**
 	 * @param identifableURI
 	 *            the {@link URI} of file with a serialized {@link Identifiable}
 	 * @return the {@link Identifiable} de-serialized from the file, or
 	 *         <code>null</code> if an error occurred.
 	 */
 	public static Identifiable getIdentifiable(final URI identifableURI) {
 		Identifiable retValue = null;
 
 		try {
 			final ResourceSet resourceSet = new ResourceSetImpl();
 
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 					.put("*", new XMIResourceFactoryImpl());
 			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
 					.put("platform", new XMIResourceFactoryImpl());
 
 			final Resource resource = resourceSet.getResource(identifableURI,
 					true);
 			retValue = (Identifiable) resource.getContents().get(0);
 		} catch (final Exception e) {
 			CorePlugin.logError(
 					"The serialized instance of an Identifiable at \""
 							+ identifableURI.toString()
 							+ "\" was not found or was of the wrong format", e);
 			retValue = null;
 		}
 		return retValue;
 	} // getIdentifiable
 
 	/**
 	 * Serialize an {@link Identifiable}
 	 * <p>
 	 * Note this code is copied from
 	 * org.eclipse.stem.internal.data.records.Record. That method should be
 	 * removed and this one used instead.
 	 * 
 	 * @param identifiable
 	 *            the {@link Identifiable} to be serialized.
 	 * @param serializationURI
 	 *            the {@link URI} that specifies where the {@link Identifiable}
 	 *            is to be serialized.
 	 * @throws IOException
 	 *             if there is a problem serializing the {@link Identifiable}
 	 */
 	public static void serializeIdentifiable(final Identifiable identifiable,
 			final URI serializationURI) throws IOException {
 		final List<Identifiable> set = new ArrayList<Identifiable>();
 		set.add(identifiable);
 		serializeIdentifiables(set, serializationURI);
 	} // serializeIdentifiable
 
 	/**
 	 * Serialize an {@link Identifiable}
 	 * 
 	 * @param identifiable
 	 *            the {@link Identifiable} to be serialized.
 	 * @param serializationURI
 	 *            the {@link URI} that specifies where the {@link Identifiable}
 	 *            is to be serialized.
 	 * @throws IOException
 	 *             if there is a problem serializing the {@link Identifiable}
 	 */
 	public static void serializeIdentifiables(final List<Identifiable> identifiables,
 			final URI serializationURI) throws IOException {
 
 		// This code could be running "stand alone" (i.e., not within eclipse),
 		// thus the default factories for the extensions are not registered as
 		// they would from their extension of
 		// "org.eclipse.emf.ecore.extension_parser". So we need to register them
 		// here.
 
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*",
 				STEMXMIResourceFactoryImpl.INSTANCE);
 		Resource.Factory.Registry.INSTANCE.getProtocolToFactoryMap().put(
 				"platform", STEMXMIResourceFactoryImpl.INSTANCE);
 
 		final ResourceSet resourceSet = new ResourceSetImpl();
 
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put("*", STEMXMIResourceFactoryImpl.INSTANCE);
 
 		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(
 				"platform", STEMXMIResourceFactoryImpl.INSTANCE);
 
 		final Resource resource = resourceSet.createResource(serializationURI);
 		
 		for (Identifiable id : identifiables) {
 			resource.getContents().add(id);
 		}
 
 		resource.save(EMF_SAVE_OPTIONS);
 	} // serializeIdentifiable
 	
 	/**
 	 * Common method for determining the geographic level of a geographic key (last segment of a URI)
 	 * @param key
 	 * @return the level of the key
 	 */
 	public  static int keyLevel(final String key) {
 		// ZZZ is special
 		if(key.equalsIgnoreCase("ZZZ")) return -1;
 		int level = 0;
 		int start = 0;
 		for (int temp = key.indexOf("-"); temp > 0;) { //$NON-NLS-1$
 			level++;
 			start += temp + 1;
 			temp = key.substring(start).indexOf("-"); //$NON-NLS-1$
 		} // for
 		return level;
 	} // keyLevel
 } // Utility
