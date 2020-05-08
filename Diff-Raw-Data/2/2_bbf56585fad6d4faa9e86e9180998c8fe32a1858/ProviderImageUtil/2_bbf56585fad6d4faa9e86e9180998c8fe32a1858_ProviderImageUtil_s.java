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
 package org.eclipse.emf.compare.diff.util;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.diff.metamodel.AbstractDiffExtension;
 import org.eclipse.emf.compare.util.AdapterUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 
 /**
  * Allows to retrieve a label image adapted to one of its content's features.
  * 
  * @author Cedric Brun <cedric.brun@obeo.fr>
  */
 public final class ProviderImageUtil {
 	private ProviderImageUtil() {
 		// Hides constructor
 	}
 
 	/**
 	 * Returns the image of the given feature for this object.
 	 * 
 	 * @param object
 	 *            The object containing the feature.
 	 * @param feature
 	 *            The feature which image is needed.
 	 * @param pluginAdapterFactory
 	 *            The AdapterFactory class of the plugin.
 	 * @return The image of the given feature for this object or a <code>null</code> reference if the object
 	 *         or feature isn't an instance of {@link org.eclipse.emf.ecore.EObject EObject}.
 	 * @exception IllegalArgumentException
 	 *                If the feature is not one of the {@link #eClass meta class}'s
 	 *                {@link EClass#getEAllStructuralFeatures features}.
 	 */
 	public static Object findImage(Object object, EReference feature,
 			Class<? extends AdapterFactory> pluginAdapterFactory) {
 		Object image = null;
 
 		AdapterFactory featureAdapterFactory = getAdapterFactory(object, feature);
 		if (featureAdapterFactory != null && !(featureAdapterFactory instanceof ComposedAdapterFactory)
 				&& !(pluginAdapterFactory.isInstance(featureAdapterFactory))) {
 			IItemLabelProvider itemLabelProvider = (IItemLabelProvider)featureAdapterFactory.adapt(
 					((EObject)object).eGet(feature), IItemLabelProvider.class);
 			image = itemLabelProvider.getImage(((EObject)object).eGet(feature));
 		}
 
 		return image;
 	}
 
 	public static Object getDiffExtensionImage(Object object) {
 		if (object instanceof AbstractDiffExtension)
 			return ((AbstractDiffExtension)object).getImage();
 		return null;
 	}
 
 	/**
 	 * Fetches the {@link org.eclipse.emf.common.notify.AdapterFactory AdapterFactory} corresponding to a
 	 * particular feature of a given object.
 	 * 
 	 * @param object
 	 *            The object containing the feature.
 	 * @param feature
 	 *            The feature which {@link org.eclipse.emf.common.notify.AdapterFactory AdapterFactory} is
 	 *            needed.
 	 * @return The {@link org.eclipse.emf.common.notify.AdapterFactory AdapterFactory} corresponding to the
 	 *         feature or a <code>null</code> reference if the object or feature isn't an instance of
 	 *         {@link org.eclipse.emf.ecore.EObject EObject}.
 	 * @exception IllegalArgumentException
 	 *                If the feature is not one of the {@link #eClass meta class}'s
 	 *                {@link EClass#getEAllStructuralFeatures features}.
 	 */
 	private static AdapterFactory getAdapterFactory(Object object, EStructuralFeature feature) {
 		Object featureValue = ((EObject)object).eGet(feature);
 
 		AdapterFactory featureAdapterFactory = null;
 		if (featureValue != null && (featureValue instanceof EObject))
			featureAdapterFactory = AdapterUtils.getAdapterFactory();
 
 		return featureAdapterFactory;
 	}
 }
