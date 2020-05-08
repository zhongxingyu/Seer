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
 package org.eclipse.emf.compare.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 
 /**
  * Useful methods for EMF adapter factories handling.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 public final class AdapterUtils {
 	/**
 	 * Adapter factory instance. This contains all factories registered in the global registry.
 	 */
 	private static final ComposedAdapterFactory FACTORY = createAdapterFactory();
 
 	/**
 	 * Utility classes don't need to (and shouldn't) be instantiated.
 	 */
 	private AdapterUtils() {
 		// prevents instantiation.
 	}
 
 	/**
 	 * This will try and return an adapter of the given type associated to the given EObject.
 	 * 
 	 * @param <T>
 	 *            Type of the adapter to return.
 	 * @param eObj
 	 *            The EObject we seek an adapter for.
 	 * @param type
 	 *            The sought adapter's type.
 	 * @return An associated adapter.
 	 * @see AdapterFactory#adapt(Object, Object)
 	 * @since 0.8
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T adapt(EObject eObj, Class<? extends T> type) {
 		return (T)getAdapterFactory().adapt(eObj, type);
 	}
	
 	/**
 	 * Return a factory for a given instance.
 	 * 
 	 * @param eObj
 	 *            the instance.
 	 * @return the factory.
 	 * @deprecated This unused method will be deleted before 1.0 is released.
 	 */
 	@Deprecated
 	public static AdapterFactory findAdapterFactory(EObject eObj) {
 		return FACTORY.getFactoryForType(eObj);
 	}
 
 	/**
 	 * Returns a factory built with all the {@link AdapterFactory} instances available in the global registry.
 	 * 
 	 * @return A factory built with all the {@link AdapterFactory} instances available in the global registry.
 	 */
 	public static AdapterFactory getAdapterFactory() {
 		return FACTORY;
 	}
 
 	/**
 	 * This will try and get the IItemLabelProvider associated to the given EObject if its ItemProviderFactory
 	 * is registered, then return the image it provides.
 	 * 
 	 * @param eObj
 	 *            EObject we need an image for.
	 * @return The Image provided by the IItemLabelProvider associated with <tt>eObj</tt>,
	 *         <code>null</code> if it cannot be found.
 	 * @see IItemLabelProvider#getImage(Object)
 	 * @since 0.8
 	 */
 	public static Object getItemProviderImage(EObject eObj) {
 		final IItemLabelProvider labelProvider = adapt(eObj, IItemLabelProvider.class);
 		if (labelProvider != null)
 			return labelProvider.getImage(eObj);
 		return null;
 	}
 
 	/**
 	 * This will try and get the IItemLabelProvider associated to the given EObject if its ItemProviderFactory
 	 * is registered, then return the text it provides.
 	 * 
 	 * @param eObj
 	 *            EObject we need the text of.
	 * @return The text provided by the IItemLabelProvider associated with <tt>eObj</tt>, <code>null</code>
	 *         if it cannot be found.
 	 * @see IItemLabelProvider#getText(Object)
 	 * @since 0.8
 	 */
 	public static String getItemProviderText(EObject eObj) {
		final IItemLabelProvider labelProvider = adapt(eObj, IItemLabelProvider.class);
		if (labelProvider != null)
			return labelProvider.getText(eObj);
		return null;
 	}
 
 	/**
 	 * Returns an adapter factory containing all the global EMF registry's factories.
 	 * 
 	 * @return An adapter factory made of all the adapter factories declared in the registry.
 	 */
 	private static ComposedAdapterFactory createAdapterFactory() {
 		final List<AdapterFactory> factories = new ArrayList<AdapterFactory>();
 		factories.add(new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
 		factories.add(new ReflectiveItemProviderAdapterFactory());
 		return new ComposedAdapterFactory(factories);
 	}
 }
