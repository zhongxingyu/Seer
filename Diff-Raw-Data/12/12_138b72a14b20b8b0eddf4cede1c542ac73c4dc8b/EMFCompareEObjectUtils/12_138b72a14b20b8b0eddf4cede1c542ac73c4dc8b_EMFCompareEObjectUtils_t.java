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
 package org.eclipse.emf.compare.ui.util;
 
 import org.eclipse.emf.compare.diff.metamodel.ConflictingDiffElement;
 import org.eclipse.emf.compare.match.metamodel.Match3Element;
 import org.eclipse.emf.compare.ui.EMFCompareUIMessages;
 import org.eclipse.emf.compare.util.AdapterUtils;
 import org.eclipse.emf.compare.util.ClassUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * Utility class providing access to some objects description.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public final class EMFCompareEObjectUtils {
 	/** Label provider used to compute images and texts for the {@link EObject}s. */
 	private static AdapterFactoryLabelProvider labelProvider;
 
 	/**
 	 * Utility classes don't need to (and shouldn't) be instantiated.
 	 */
 	private EMFCompareEObjectUtils() {
 		// prevents instantiation
 	}
 
 	/**
 	 * Computes the image of the given {@link EObject}.
 	 * 
 	 * @param eObject
 	 *            Object for which we need the image.
 	 * @return Image of the given {@link EObject}.
 	 */
 	public static Image computeObjectImage(EObject eObject) {
 		return getLabelProvider().getImage(eObject);
 	}
 
 	/**
 	 * Computes the name of the given {@link EObject}.
 	 * 
 	 * @param eObject
 	 *            Object for which we need the name.
 	 * @return Name of the given {@link EObject}.
 	 */
 	public static String computeObjectName(EObject eObject) {
 		String objectName = getLabelProvider().getText(eObject);
		if (objectName == null || "".equals(objectName)) //$NON-NLS-1$
 			objectName = EMFCompareUIMessages.getString("EMFCompareEObjectUtils.undefinedName"); //$NON-NLS-1$
 		return objectName;
 	}
 
 	/**
 	 * Returns the ancestor element of the given {@link EObject}. Will try to invoke the method called
 	 * "getLeftParent" if the {@link EObject} is a {@link ConflictingDiffGroup}, "getOriginElement" if the
 	 * {@link EObject} is a {@link Match3Element}. <code>null</code> if neither of these methods can be
 	 * found.<br/> This method is intended to be called with a {@link ConflictingDiffGroup} or
 	 * {@link Match3Element} as argument.
 	 * 
 	 * @param object
 	 *            The {@link EObject}.
 	 * @return The right element of the given {@link EObject}.
 	 */
 	public static EObject getAncestorElement(EObject object) {
 		EObject ancestorElement = null;
 
 		if (object instanceof ConflictingDiffElement)
 			ancestorElement = ((ConflictingDiffElement)object).getOriginElement();
 		else if (object instanceof Match3Element)
 			ancestorElement = ((Match3Element)object).getOriginElement();
 
 		return ancestorElement;
 	}
 
 	/**
 	 * Returns the left element of the given {@link EObject}. Will try to invoke the method called
 	 * "getLeftElement" and, if it fails to find it, "getLeftParent". <code>null</code> if neither of these
 	 * methods can be found.<br/> This method is intended to be called with a {@link DiffElement} or
 	 * {@link MatchElement} as argument.
 	 * 
 	 * @param object
 	 *            The {@link EObject}.
 	 * @return The left element of the given {@link EObject}.
 	 */
 	public static EObject getLeftElement(EObject object) {
 		EObject leftElement = null;
 
 		leftElement = (EObject)ClassUtils.invokeMethod(object, "getLeftElement"); //$NON-NLS-1$
 		if (leftElement == null)
 			leftElement = (EObject)ClassUtils.invokeMethod(object, "getLeftParent"); //$NON-NLS-1$
 
 		return leftElement;
 	}
 
 	/**
 	 * Returns the right element of the given {@link EObject}. Will try to invoke the method called
 	 * "getRightElement" and, if it fails to find it, "getRightParent". <code>null</code> if neither of
 	 * these methods can be found.<br/> This method is intended to be called with a {@link DiffElement} or
 	 * {@link MatchElement} as argument.
 	 * 
 	 * @param object
 	 *            The {@link EObject}.
 	 * @return The right element of the given {@link EObject}.
 	 */
 	public static EObject getRightElement(EObject object) {
 		EObject rightElement = null;
 
 		rightElement = (EObject)ClassUtils.invokeMethod(object, "getRightElement"); //$NON-NLS-1$
 		if (rightElement == null)
 			rightElement = (EObject)ClassUtils.invokeMethod(object, "getRightParent"); //$NON-NLS-1$
 
 		return rightElement;
 	}
 
 	/**
 	 * Returns the label provider wrapped around {@link EMFAdapterFactoryProvider#getAdapterFactory()}.
 	 * 
 	 * @return The label provider wrapped around {@link EMFAdapterFactoryProvider#getAdapterFactory()}.
 	 */
 	private static AdapterFactoryLabelProvider getLabelProvider() {
 		if (labelProvider == null) {
 			labelProvider = new AdapterFactoryLabelProvider(AdapterUtils.getAdapterFactory());
 		}
 		return labelProvider;
 	}
 }
