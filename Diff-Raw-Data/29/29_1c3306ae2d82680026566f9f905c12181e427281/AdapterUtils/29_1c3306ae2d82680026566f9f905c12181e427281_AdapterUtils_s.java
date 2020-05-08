 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * Useful methods for EMF adapter factories handling.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public final class AdapterUtils {
 	/** Name of the extension point to parse for adapter factories. */
 	private static final String ADAPTER_FACTORY_EXTENSION_POINT = "org.eclipse.emf.edit.itemProviderAdapterFactories"; //$NON-NLS-1$
 
 	/** Keeps track of all the {@link AdapterFactory adapter factories} this plug-in knows of. */
 	private static final Map<String, AdapterFactoryDescriptor> FACTORIES = new HashMap<String, AdapterFactoryDescriptor>();
 
 	/** This static initializer will parse the extension point to seek for factory classes and populate the factories list. */
 	static {
 		parseExtensionMetadata();
 	}
 
 	/**
	 * Utility classes don't need to be instantiated.
 	 */
 	private AdapterUtils() {
 		// prevents instantiation.
 	}
 
 	/**
 	 * Returns the factory described by the given <code>nsURI</code>, <code>null</code> if it cannot be found.
 	 * 
 	 * @param nsURI
 	 *            <code>nsURI</code> of the desired {@link AdapterFactory}.
 	 * @return The factory described by the fiven <code>nsURI</code>, <code>null</code> if it cannot be found.
 	 */
 	public static AdapterFactory findAdapterFactory(String nsURI) {
 		AdapterFactory adapterFactory = null;
 		if (FACTORIES.containsKey(nsURI))
 			adapterFactory = FACTORIES.get(nsURI).getAdapterInstance();
 		return adapterFactory;
 	}
 
 	/**
	 * Returnq the adapter factory for the given {@link EObject} or <code>null</code> if it cannot be found.
 	 * 
 	 * @param eObj
 	 *            {@link EObject} we seek the {@link AdapterFactory} for.
 	 * @return Specific {@link AdapterFactory} adapted to the {@link EObject}, <code>null</code> if it cannot be found.
 	 */
 	public static AdapterFactory findAdapterFactory(EObject eObj) {
 		final String uri = eObj.eClass().getEPackage().getNsURI();
 		return findAdapterFactory(uri);
 	}
 
 	/**
 	 * This will parse {@link #ADAPTER_FACTORY_EXTENSION_POINT} for information about factories and populate the {@link #FACTORIES known factories}
 	 * list.
 	 */
 	private static void parseExtensionMetadata() {
 		final IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(ADAPTER_FACTORY_EXTENSION_POINT).getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			final IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
 			for (int j = 0; j < configElements.length; j++) {
 				final AdapterFactoryDescriptor desc = parseAdapterFactory(configElements[j]);
 				FACTORIES.put(desc.getNsURI(), desc);
 			}
 		}
 
 	}
 
 	/**
 	 * Creates an {@link AdapterFactoryDescriptor} given the {@link IConfigurationElement configuration element} to parse its {@link AdapterFactory}
 	 * from.
 	 * 
 	 * @param configElement
 	 *            The {@link IConfigurationElement configuration element} to parse its {@link AdapterFactory} from.
 	 * @return The descriptor for the {@link AdapterFactory} defined by the given {@link IConfigurationElement configuration element}.
 	 */
 	private static AdapterFactoryDescriptor parseAdapterFactory(IConfigurationElement configElement) {
 		return new AdapterFactoryDescriptor(configElement);
 	}
 }
 
 /**
  * Describes an {@link AdapterFactory}, keeping track of its <code>nsURI</code>, <code>className</code> and the {@link IConfigurationElement}
  * it's been created from.
  * 
 * @author Laurent Goubet <a href="mailto:laurent.goubet@obeo.fr">laurent.goubet@obeo.fr</a>
  */
 final class AdapterFactoryDescriptor {
 	/** Name space URI of the wrapped {@link AdapterFactory}. */
 	private final String nsURI;
 	
 	/** Keeps a reference to the configuration element that describes the {@link AdapterFactory factory}. */
 	private final IConfigurationElement element;
 
 	/** This descriptor's wrapped factory. */
 	private AdapterFactory factory;
 
 	/**
 	 * Constructs a new descriptor from an IConfigurationElement.
 	 * 
 	 * @param configElements
 	 *            Configuration of the {@link AdapterFactory}.
 	 */
 	public AdapterFactoryDescriptor(IConfigurationElement configElements) {
 		element = configElements;
 		this.nsURI = element.getAttribute("uri"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the {@link AdapterFactory} <code>nsURI</code>.
 	 * 
 	 * @return The {@link AdapterFactory} <code>nsURI</code>.
 	 */
 	public String getNsURI() {
 		return nsURI;
 	}
 
 	/**
 	 * Returns the enclosed {@link AdapterFactory}'s instance.
 	 * 
 	 * @return The corresponding {@link AdapterFactory} instance.
 	 */
 	public AdapterFactory getAdapterInstance() {
 		if (factory == null) {
 			try {
 				factory = (AdapterFactory)element.createExecutableExtension("class"); //$NON-NLS-1$
 			} catch (CoreException e) {
 				e.printStackTrace();
 			}
 		}
 		return factory;
 	}
 }
