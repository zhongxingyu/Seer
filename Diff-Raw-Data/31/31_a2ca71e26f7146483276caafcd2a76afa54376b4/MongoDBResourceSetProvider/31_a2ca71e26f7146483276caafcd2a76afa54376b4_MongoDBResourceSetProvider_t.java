 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Johannes Faltermeier
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.mongodb;
 
 import java.util.LinkedHashMap;
 
import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.URIHandler;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.emfstore.client.provider.ESResourceSetProvider;
 import org.eclipse.emf.emfstore.internal.common.ResourceFactoryRegistry;
 import org.eclipselabs.mongo.emf.ext.IResourceSetFactory;
 
 /**
  * MongoDB ResourceSet provider for EMFStore Client.
  * 
  * @author jfaltermeier
  * 
  */
 public class MongoDBResourceSetProvider implements ESResourceSetProvider {
 
	private static IResourceSetFactory resourceSetFactory;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.provider.ESResourceSetProvider#getResourceSet()
 	 */
 	public ResourceSet getResourceSet() {
 		ResourceSetImpl resourceSet = (ResourceSetImpl) resourceSetFactory.createResourceSet();
 		resourceSet.setResourceFactoryRegistry(new ResourceFactoryRegistry());
		resourceSet.setURIConverter(createURIConverter(resourceSet));
 		resourceSet.setURIResourceMap(new LinkedHashMap<URI, Resource>());
 		return resourceSet;
 	}
 
	private URIConverter createURIConverter(ResourceSetImpl resourceSet) {
		// reuse uri handlers set up by resourcesetfactory
		EList<URIHandler> uriHandler = resourceSet.getURIConverter().getURIHandlers();
		URIConverter uriConverter = new MongoURIConverter();
		uriConverter.getURIHandlers().clear();
		uriConverter.getURIHandlers().addAll(uriHandler);
		return uriConverter;
 	}
 
 	/**
 	 * Binds the resource set factory.
 	 * 
 	 * @param resourceSetFactory the resource set factory
 	 */
 	void bindResourceSetFactory(IResourceSetFactory resourceSetFactory) {
 		this.resourceSetFactory = resourceSetFactory;
 	}
 
 }
