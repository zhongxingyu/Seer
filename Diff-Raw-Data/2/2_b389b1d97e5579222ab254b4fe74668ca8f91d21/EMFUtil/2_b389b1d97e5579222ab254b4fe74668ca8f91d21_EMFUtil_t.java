 /**
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  * 
  */
 package org.obeonetwork.dsl.smartdesigner.design.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.FrameworkUtil;
 
 /**
  * Utility class for EMF objects.
  * 
  * @author Stephane Drapeau - Obeo
  * 
  */
 public class EMFUtil {
 
 	/**
	 * Predefined feature names used to retrieve names from {@link EObject}s.
 	 */
 	private static final Collection<String> FEATURE_NAMES = Arrays
 			.asList(new String[] { "name", "nom", "label", "libellé", "libelle" });
 
 	/**
 	 * Tries to find a label for the given object.
 	 * 
 	 * @param object
 	 *            the {@link EObject}
 	 * @return a label for the given object
 	 */
 	public static String retrieveNameFrom(EObject object) {
 		if (object == null) {
 			return "";
 		} else {
 			EClass eClass = object.eClass();
 			// First attempt : try to use a predefined feature name
 			for (String featureName : FEATURE_NAMES) {
 				String result = getAttributeValue(object,
 						eClass.getEStructuralFeature(featureName));
 				if (result != null) {
 					return result;
 				}
 			}
 			// Second attempt : try to use the first non null string feature
 			for (EStructuralFeature f : eClass.getEAllAttributes()) {
 				if (String.class == ((EAttribute) f).getEType()
 						.getInstanceClass()) {
 					String result = getAttributeValue(object, f);
 					if (result != null) {
 						return result;
 					}
 				}
 			}
 			// At last, use the EClass name
 			return eClass.getName();
 		}
 	}
 
 	/**
 	 * Retrieves e feature value.
 	 * 
 	 * @param eObject
 	 *            the object to read.
 	 * @param feature
 	 *            the feature to read.
 	 * @return the feature value or null if the feature is not an
 	 *         {@link EAttribute}.
 	 */
 	private static String getAttributeValue(EObject eObject,
 			EStructuralFeature feature) {
 		if (feature instanceof EAttribute) {
 			Object rawResult = eObject.eGet(feature);
 			if (rawResult != null) {
 				return String.valueOf(rawResult);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Get the image corresponding to the EObject <code>eObject</code> (looks
 	 * for the image in the .edit plugin).
 	 * 
 	 * @param eObject
 	 *            The EObject for which we are looking for an image.
 	 * @return the image corresponding to the EObject <code>eObject</code>.
 	 */
 	public static Image getImage(EObject eObject) {
 		if (eObject == null) {
 			return null;
 		}
 
 		Image result = null;
 		Bundle bundle = FrameworkUtil.getBundle(eObject.getClass());
 		ImageDescriptor imageDescriptor = AbstractUIPlugin
 				.imageDescriptorFromPlugin(
 						bundle.getSymbolicName() + ".edit",
 						"icons/full/obj16/"
 								+ eObject.getClass().getSimpleName()
 										.replace("Impl", "") + ".gif");
 		if (imageDescriptor == null) {
 			return null;
 		}
 		result = imageDescriptor.createImage(true);
 		return result;
 	}
 
 	/**
 	 * Returns the references existing between <code>sourceElement</code> and
 	 * <code>targetElement</code>.
 	 * 
 	 * @param sourceElement
 	 * @param targetElement
 	 * @return the list of references existing between
 	 *         <code>sourceElement</code> and <code>targetElement</code>.
 	 *         Returns an empty list if there is no reference between
 	 *         <code>sourceElement</code> and <code>targetElement</code>.
 	 */
 	public static List<EReference> getReferencesBetween(EObject sourceElement,
 			EObject targetElement) {
 		EList<EReference> references = sourceElement.eClass()
 				.getEAllReferences();
 		List<EReference> result = new ArrayList<EReference>();
 		for (EReference ref : references) {
 			List<String> implementedInterfaces = getImplementedInterfaces(targetElement
 					.getClass());
 			if (implementedInterfaces.contains(ref.getEReferenceType()
 					.getInstanceClassName())) {
 				result.add(ref);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the list of interfaces implemented by the class
 	 * <code>clazz</code>.
 	 * 
 	 * @param clazz
 	 * @return
 	 */
 	private static List<String> getImplementedInterfaces(Class clazz) {
 		List<String> list = new ArrayList();
 		Class[] classes = clazz.getInterfaces();
 		for (Class c : classes) {
 			list.add(c.getCanonicalName());
 			list.addAll(getImplementedInterfaces(c));
 		}
 		return list;
 	}
 
 	/**
 	 * Set a reference from <code>sourceElement</code> to
 	 * <code>targetElement</code>.
 	 * 
 	 * @param sourceElement
 	 * @param targetElement
 	 * @param reference
 	 */
 	public static void setEReference(EObject sourceElement,
 			EObject targetElement, EReference reference) {
 		if (reference.isMany()) {
 			EList<EObject> list = new BasicEList<EObject>();
 			if (sourceElement.eGet(reference) instanceof List) {
 				for (Object o : (List) sourceElement.eGet(reference)) {
 					list.add((EObject) o);
 				}
 			}
 			list.add(targetElement);
 			sourceElement.eSet(reference, list);
 		} else {
 			sourceElement.eSet(reference, targetElement);
 		}
 	}
 
 }
