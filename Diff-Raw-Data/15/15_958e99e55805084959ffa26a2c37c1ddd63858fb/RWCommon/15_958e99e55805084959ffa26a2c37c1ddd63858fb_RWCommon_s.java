 /*******************************************************************************
  * Copyright (c) 2010 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Ericsson AB - Intial Implementation
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.core.model.serial;
 
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 import org.eclipse.mylyn.reviews.r4e.core.Activator;
 
 /**
  * @author Alvaro Sanchez-Leon
  */
 public class RWCommon {
 	
 	// ------------------------------------------------------------------------
 	// Fields
 	// ------------------------------------------------------------------------
	protected Map<String, Boolean>	fOptions	= null;
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	/**
 	 * Initialise the resource Set for reading or writing
 	 * 
 	 * @return
 	 */
 	private ResourceSet createResourceSet() {
 		// create the container resource set
 		ResourceSet resourceSet = new ResourceSetImpl();
 		// Register the appropriate resource factory to handle all file
 		// extensions.
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl() {
 					public Resource createResource(URI uri) {
 						return new XMIResourceImpl(uri) {
 							protected boolean useUUIDs() {
 								return true;
 							}
 						};
 					}
 				});
 		
 		Activator.fTracer.traceDebug("new Resourceset created: " + resourceSet);
 		return resourceSet;
 	}
 
 	/**
 	 * Create ResourceSet with a new Resource at the specified URI
 	 * 
 	 * @param aPathToResource
 	 * @return
 	 */
 	public Resource createResourceSetWithResource(URI aPathToResource) {
 		Resource groupResource = null;
 
 		if (aPathToResource != null) {
 			ResourceSet resourceSet = createResourceSet();
 
 			// Create the resource for given group
 			groupResource = resourceSet.createResource(aPathToResource);
 		}
 		return groupResource;
 	}
 
 	// /**
 	// * Crate resource previously serialised as per provided root path and group name
 	// *
 	// * @param aGroupPath
 	// * directory path ending with "/"
 	// * @param aGroupName
 	// * file name with no extension. ".xml" is default
 	// * @return
 	// */
 	// protected Resource createGroupResourceSet(String aGroupPath, String aGroupName) {
 	// Resource groupResource = null;
 	//
 	// if (aGroupPath != null && aGroupName != null) {
 	// ResourceSet resourceSet = createResourceSet();
 	// URI reviewGroupURI = URI.createFileURI(aGroupPath + aGroupName + _EXTENSION);
 	//
 	// // Create the resource for given group
 	// groupResource = resourceSet.createResource(reviewGroupURI);
 	// }
 	// return groupResource;
 	// }
 }
 	
