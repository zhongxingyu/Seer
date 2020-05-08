 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.internal.core.util;
 
 import org.eclipse.dltk.core.Flags;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ITypeHierarchy;
 import org.eclipse.dltk.core.ModelException;
 
 public class MethodOverrideTester {
 
 	private final IType fFocusType;
 	private final ITypeHierarchy fHierarchy;
 
 	public MethodOverrideTester(IType focusType, ITypeHierarchy hierarchy) {
 		fFocusType = focusType;
 		fHierarchy = hierarchy;
 
 	}
 
 	public IType getFocusType() {
 		return fFocusType;
 	}
 
 	public ITypeHierarchy getTypeHierarchy() {
 		return fHierarchy;
 	}
 
 	/**
 	 * Finds the method that declares the given method. A declaring method is
 	 * the 'original' method declaration that does not override nor implement a
 	 * method. <code>null</code> is returned it the given method does not
 	 * override a method. When searching, super class are examined before
 	 * implemented interfaces.
 	 * 
 	 * @param testVisibility
 	 *            If true the result is tested on visibility. Null is returned
 	 *            if the method is not visible.
 	 * @throws ModelException
 	 */
 	public IMethod findDeclaringMethod(IMethod overriding,
 			boolean testVisibility) throws ModelException {
 		IMethod result = null;
 		IMethod overridden = findOverriddenMethod(overriding, testVisibility);
 		while (overridden != null) {
 			result = overridden;
 			overridden = findOverriddenMethod(result, testVisibility);
 		}
 		return result;
 	}
 
 	/**
 	 * Finds the method that is overridden by the given method. First the super
 	 * class is examined and then the implemented interfaces.
 	 * 
 	 * @param testVisibility
 	 *            If true the result is tested on visibility. Null is returned
 	 *            if the method is not visible.
 	 * @throws ModelException
 	 */
 	public IMethod findOverriddenMethod(IMethod overriding,
 			boolean testVisibility) throws ModelException {
 		int flags = overriding.getFlags();
 		if (Flags.isPrivate(flags) || Flags.isStatic(flags)
 				|| overriding.isConstructor()) {
 			return null;
 		}
 
 		IType type = overriding.getDeclaringType();
 		IType[] superClass = fHierarchy.getSuperclass(type);
 		if (superClass != null) {
 			for (int q = 0; q < superClass.length; ++q) {
 				IMethod res = findOverriddenMethodInHierarchy(superClass[q],
 						overriding);
 				if (res != null && !Flags.isPrivate(res.getFlags())) {
 
 					return res;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Finds the directly overridden method in a type and its super types. First
 	 * the super class is examined and then the implemented interfaces. With
 	 * generics it is possible that 2 methods in the same type are overidden at
 	 * the same time. In that case, the first overridden method found is
 	 * returned.
 	 * 
 	 * @param type
 	 *            The type to find methods in
 	 * @param overriding
 	 *            The overriding method
 	 * @return The first overridden method or <code>null</code> if no method is
 	 *         overridden
 	 * @throws ModelException
 	 */
 	public IMethod findOverriddenMethodInHierarchy(IType type,
 			IMethod overriding) throws ModelException {
 		IMethod method = findOverriddenMethodInType(type, overriding);
 		if (method != null) {
 			return method;
 		}
 		IType[] superClass = fHierarchy.getSuperclass(type);
 		if (superClass != null) {
 			for (int q = 0; q < superClass.length; ++q) {
				IMethod res = findOverriddenMethodInHierarchy(superClass[q],
						overriding);
 				if (res != null) {
 					return res;
 				}
 			}
 		}
 
 		return method;
 	}
 
 	/**
 	 * Finds an overridden method in a type. WWith generics it is possible that
 	 * 2 methods in the same type are overidden at the same time. In that case
 	 * the first overridden method found is returned.
 	 * 
 	 * @param overriddenType
 	 *            The type to find methods in
 	 * @param overriding
 	 *            The overriding method
 	 * @return The first overridden method or <code>null</code> if no method is
 	 *         overridden
 	 * @throws ModelException
 	 */
 	public IMethod findOverriddenMethodInType(IType overriddenType,
 			IMethod overriding) throws ModelException {
 		IMethod[] overriddenMethods = overriddenType.getMethods();
 		for (int i = 0; i < overriddenMethods.length; i++) {
 			if (isSubsignature(overriding, overriddenMethods[i])) {
 				return overriddenMethods[i];
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Finds an overriding method in a type.
 	 * 
 	 * @param overridingType
 	 *            The type to find methods in
 	 * @param overridden
 	 *            The overridden method
 	 * @return The overriding method or <code>null</code> if no method is
 	 *         overriding.
 	 * @throws ModelException
 	 */
 	public IMethod findOverridingMethodInType(IType overridingType,
 			IMethod overridden) throws ModelException {
 		IMethod[] overridingMethods = overridingType.getMethods();
 		for (int i = 0; i < overridingMethods.length; i++) {
 			if (isSubsignature(overridingMethods[i], overridden)) {
 				return overridingMethods[i];
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Tests if a method is a subsignature of another method.
 	 * 
 	 * @param overriding
 	 *            overriding method (m1)
 	 * @param overridden
 	 *            overridden method (m2)
 	 * @return <code>true</code> iff the method <code>m1</code> is a
 	 *         subsignature of the method <code>m2</code>. This is one of the
 	 *         requirements for m1 to override m2. Note that subsignature is
 	 *         <em>not</em> symmetric!
 	 * @throws ModelException
 	 */
 	public boolean isSubsignature(IMethod overriding, IMethod overridden)
 			throws ModelException {
 		if (!overridden.getElementName().equals(overriding.getElementName())) {
 			return false;
 		}
 		return true;
 	}
 }
