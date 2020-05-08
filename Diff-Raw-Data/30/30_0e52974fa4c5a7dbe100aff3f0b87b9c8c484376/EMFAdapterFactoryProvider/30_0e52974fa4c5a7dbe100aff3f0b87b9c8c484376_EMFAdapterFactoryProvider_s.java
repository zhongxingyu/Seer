 /*  
  * Copyright (c) 2006, Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 
 package org.eclipse.emf.compare.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 
 /**
  * 
 * @author Cedric Brun  cedric.brun@obeo.fr 
  * 
  */
 public final class EMFAdapterFactoryProvider {
 
 	private static ComposedAdapterFactory adapterFactory;
 
 	/**
 	 * 
 	 * @return the default EMF factory list
 	 */
 	public final static List<AdapterFactory> createFactoryList() {
 		final List<AdapterFactory> factories = new ArrayList<AdapterFactory>();
 		// this is my provider generated in the .Edit plugin for me. Replace
 		// with yours.
 		// these are EMF generic providers
 		factories.add(new ResourceItemProviderAdapterFactory());
 		factories.add(new ReflectiveItemProviderAdapterFactory());
 
 
 		// ... add other provider adapter factories for your model as needed
 		// if you don't know what to add, look at the creation of the adapter
 		// factory
 		// in your generated editor
 		return factories;
 	}
 
 	/**
 	 * 
 	 * @return the adapter factory
 	 */
 	public final static ComposedAdapterFactory getAdapterFactory() {
 		if (adapterFactory == null) {
 			adapterFactory = new ComposedAdapterFactory(createFactoryList());
 		}
 		return adapterFactory;
 	}
 }
