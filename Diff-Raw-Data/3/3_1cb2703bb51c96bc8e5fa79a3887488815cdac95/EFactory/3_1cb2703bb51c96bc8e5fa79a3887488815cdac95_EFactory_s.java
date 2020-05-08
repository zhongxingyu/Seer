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
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.compare.EMFCompareMessages;
 import org.eclipse.emf.compare.FactoryException;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 
 /**
  * This is a factory for an ecore metamodel. There is a factory by package. Each factory is used to create
  * instances of classifiers.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public final class EFactory {
	/** Prefix for all getter methods. Externalized to avoid to many distinct uses. */
	private static final String GETTER_PREFIX = "get"; //$NON-NLS-1$

 	/**
 	 * Utility classes don't need to (and shouldn't) be instantiated.
 	 */
 	private EFactory() {
 		// prevents instantiation
 	}
 
 	/**
 	 * Adds the new value of the given feature of the object. If the structural feature isn't a list, it
 	 * behaves like eSet.
 	 * 
 	 * @param object
 	 *            Object on which we want to add to the feature values list.
 	 * @param name
 	 *            The name of the feature to add to.
 	 * @param arg
 	 *            New value to add to the feature values.
 	 * @throws FactoryException
 	 *             Thrown if the affectation fails.
 	 */
 	@SuppressWarnings("unchecked")
 	public static void eAdd(EObject object, String name, Object arg) throws FactoryException {
 		final Object list = object.eGet(eStructuralFeature(object, name));
 		if (list != null && list instanceof List) {
 			if (arg != null) {
 				((List)list).add(arg);
 			}
 		} else {
 			eSet(object, name, arg);
 		}
 	}
 
 	/**
 	 * Gets the value of the given feature of the object.
 	 * 
 	 * @param object
 	 *            Object to retrieve the feature value from.
 	 * @param name
 	 *            The feature name, or a method defined on {@link EObject} like 'eClass', 'eResource',
 	 *            'eContainer', 'eContainingFeature', 'eContainmentFeature', 'eContents', 'eAllContents',
 	 *            'eCrossReferences'
 	 * @return Value of the given feature of the object
 	 * @throws FactoryException
 	 *             Thrown if the retrieval fails.
 	 */
 	public static Object eGet(EObject object, String name) throws FactoryException {
 		Object result = null;
 		try {
 			final EStructuralFeature feature = eStructuralFeature(object, name);
 			result = object.eGet(feature);
 		} catch (FactoryException eGet) {
 			try {
 				result = eCall(object, name);
 			} catch (FactoryException eCall) {
 				throw eGet;
 			} catch (NullPointerException e) {
 				// Thrown if "name" is null
 				throw eGet;
 			}
 		}
 		/*
 		 * Following code is only meaningful if we desire a specific hanfling of enumerations. This isn't the
 		 * case in EMF Compare.
 		 */
 		// if (result != null && result instanceof Enumerator) {
 		// result = ((Enumerator)result).getName();
 		// } else
 		// if (result != null && result instanceof EDataTypeUniqueEList) {
 		// final List<Object> list = new ArrayList<Object>();
 		// final Iterator enums = ((EDataTypeUniqueEList)result).iterator();
 		// while (enums.hasNext()) {
 		// final Object next = enums.next();
 		// if (next instanceof Enumerator) {
 		// list.add(((Enumerator)next).getName());
 		// } else {
 		// list.add(next);
 		// }
 		// }
 		// result = list;
 		// }
 		return result;
 	}
 
 	/**
 	 * Gets the value of the given feature of the object as a List.
 	 * 
 	 * @param object
 	 *            Object to retrieve the feature value from.
 	 * @param name
 	 *            Name of the feature to get the value for.
 	 * @return
 	 *            <ul>
 	 *            If the feature is :
 	 *            <li><b>a list :</b> value of the feature</li>
 	 *            <li><b>a single valued feature :</b> new list containing the value as its single element</li>
 	 *            <li><b>not a feature :</b> <code>null</code></li>
 	 *            </ul>
 	 * @throws FactoryException
 	 *             Thrown if the retrieval fails.
 	 */
 	@SuppressWarnings("unchecked")
 	public static List eGetAsList(EObject object, String name) throws FactoryException {
 		List list = new ArrayList();
 		final Object eGet = eGet(object, name);
 		if (eGet != null) {
 			if (eGet instanceof List) {
 				list = (List)eGet;
 			} else {
 				list = new BasicEList(1);
 				list.add(eGet);
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * Gets the value of the given feature of the object as a String.
 	 * 
 	 * @param object
 	 *            Object to retrieve the feature value from.
 	 * @param name
 	 *            Name of the feature to get the value for.
 	 * @return Value of the feature, <code>null</code> if this value isn't a {@link String}.
 	 * @throws FactoryException
 	 *             Thrown if the retrieval fails.
 	 */
 	public static String eGetAsString(EObject object, String name) throws FactoryException {
 		final Object eGet = eGet(object, name);
 		if (eGet != null)
 			return eGet.toString();
 		return null;
 	}
 
 	/**
 	 * Removes the value of the given feature of the object. If the structural feature isn't a list, it
 	 * behaves like eSet(object, name, null) and resets the feature even if specified value isn't equal to the
 	 * actual feature's value.
 	 * 
 	 * @param object
 	 *            Object on which we want to remove from the feature values list.
 	 * @param name
 	 *            The name of the feature to remove from.
 	 * @param arg
 	 *            Value to remove from the feature values, can be <code>null</code>.
 	 * @throws FactoryException
 	 *             Thrown if the removal fails.
 	 */
 	public static void eRemove(EObject object, String name, Object arg) throws FactoryException {
 		final Object list = object.eGet(eStructuralFeature(object, name));
 		if (list instanceof List) {
 			if (arg != null) {
 				((List<?>)list).remove(arg);
 			}
 		} else {
 			eSet(object, name, null);
 		}
 	}
 
 	/**
 	 * Sets the value of the given feature of the object to the new value.
 	 * 
 	 * @param object
 	 *            Object on which we want to set the feature value.
 	 * @param name
 	 *            The name of the feature to set.
 	 * @param arg
 	 *            New value to affect to the feature.
 	 * @throws FactoryException
 	 *             Thrown if the affectation fails.
 	 */
 	public static void eSet(EObject object, String name, Object arg) throws FactoryException {
 		final EStructuralFeature feature = eStructuralFeature(object, name);
 		if (!feature.isChangeable())
 			throw new FactoryException(EMFCompareMessages.getString("EFactory.UnSettableFeature", name)); //$NON-NLS-1$
 
 		if (feature.getEType() instanceof EEnum && arg instanceof String) {
 			final EEnumLiteral literal = ((EEnum)feature.getEType()).getEEnumLiteral((String)arg);
 			object.eSet(feature, literal);
 //			try {
 //				final Class<?> c = object.getClass().getClassLoader().loadClass(
 //						ETools.getEClassifierPath(feature.getEType()));
 //				final Method m = c.getMethod(GETTER_PREFIX, new Class[] {String.class});
 //				final Object value = m.invoke(c, new Object[] {arg});
 //				object.eSet(feature, value);
 //			} catch (ClassNotFoundException e) {
 //				throw new FactoryException(e);
 //			} catch (NoSuchMethodException e) {
 //				throw new FactoryException(e);
 //			} catch (IllegalAccessException e) {
 //				throw new FactoryException(e);
 //			} catch (InvocationTargetException e) {
 //				throw new FactoryException(e);
 //			}
 		} else {
 			if (arg == null && feature.isMany())
 				object.eSet(feature, Collections.EMPTY_LIST);
 			else if (arg == null)
 				object.eSet(feature, feature.getDefaultValue());
 			else
 				object.eSet(feature, arg);
 		}
 	}
 
 	/**
 	 * Gets the structural feature of the given feature name of the object.
 	 * 
 	 * @param object
 	 *            Object to retrieve the feature from.
 	 * @param name
 	 *            Name of the feature to retrieve.
 	 * @return The structural feature <code>name</code> of <code>object</code>.
 	 * @throws FactoryException
 	 *             Thrown if the retrieval fails.
 	 */
 	public static EStructuralFeature eStructuralFeature(EObject object, String name) throws FactoryException {
 		final EStructuralFeature structuralFeature = object.eClass().getEStructuralFeature(name);
 		if (structuralFeature != null)
 			return structuralFeature;
 		throw new FactoryException(EMFCompareMessages.getString(
 				"EFactory.FeatureNotFound", name, object.eClass().getName())); //$NON-NLS-1$
 	}
 
 	/**
 	 * This will call the method called <code>name</code> on the given <code>object</code> with the given
 	 * <code>arg</code>uments.
 	 * 
 	 * @param object
 	 *            Object to call method <code>name</code> on.
 	 * @param name
 	 *            Name of the method to call on <code>object</code>.
 	 * @param arg
 	 *            Arguments to pass to the method invocation.
 	 * @return The result of <code>name</code> invocation.
 	 * @throws FactoryException
 	 *             Thrown if the invocation fails somehow.
 	 */
 	@SuppressWarnings("unchecked")
 	private static Object eCall(Object object, String name, Object... arg) throws FactoryException {
 		try {
 			final Class<? extends Object>[] methodParams;
 			final Object[] invocationParams;
 
 			methodParams = new Class[arg.length];
 			for (int i = 0; i < arg.length; i++) {
 				methodParams[i] = arg[i].getClass();
 			}
 			invocationParams = arg;
 
 			final Method method = object.getClass().getMethod(name, methodParams);
 			return method.invoke(object, invocationParams);
 		} catch (NoSuchMethodException e) {
 			throw new FactoryException(e.getMessage());
 		} catch (IllegalAccessException e) {
 			throw new FactoryException(e.getMessage());
 		} catch (InvocationTargetException e) {
 			throw new FactoryException(e.getMessage());
 		}
 	}
 }
