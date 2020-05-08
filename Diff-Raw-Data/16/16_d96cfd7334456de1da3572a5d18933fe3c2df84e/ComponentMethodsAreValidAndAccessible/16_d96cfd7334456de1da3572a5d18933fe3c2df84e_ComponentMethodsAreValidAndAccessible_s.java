 /**
  * Copyright (c) 2009 Anyware Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Anyware Technologies - initial API and implementation
  *
 * $Id: ComponentMethodsAreValidAndAccessible.java,v 1.3 2009/07/03 20:14:25 bcabe Exp $
  */
 package org.eclipse.pde.ds.builder.internal.validation.constraints;
 
 import java.util.*;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.validation.AbstractModelConstraint;
 import org.eclipse.emf.validation.IValidationContext;
 import org.eclipse.emf.validation.model.ConstraintStatus;
 import org.eclipse.jdt.core.*;
 import org.eclipse.pde.ds.builder.internal.validation.EMFHelper;
 import org.eclipse.pde.ds.builder.internal.validation.EnhancedConstraintStatus;
 import org.eclipse.pde.ds.scr.Component;
 import org.eclipse.pde.ds.scr.ScrPackage;
 
 public class ComponentMethodsAreValidAndAccessible extends
 		AbstractModelConstraint {
 
 	private final Collection<String> validActivateParams = Arrays.asList(
 			"Qorg.osgi.service.component.ComponentContext;",
 			"Qorg.osgi.framework.BundleContext;", "Qjava.util.Map;");
 
 	private final Collection<String> validDeactivateParams = Arrays.asList(
 			"Qorg.osgi.service.component.ComponentContext;",
 			"Qorg.osgi.framework.BundleContext;", "Qjava.util.Map;", "I",
 			"Qjava.lang.Integer");
 
 	public ComponentMethodsAreValidAndAccessible() {
 	}
 
 	@Override
 	public IStatus validate(IValidationContext ctx) {
 		Component comp = (Component) ctx.getTarget();
 		IJavaProject project = JavaCore.create(EMFHelper.getIProject(comp
 				.eResource()));
 
 		List<IStatus> statuses = new ArrayList<IStatus>();
 
 		try {
 			IType type;
 			type = project.findType(comp.getImplementation().getClass_());
 			if (type != null && type.exists()) {
 				// validate 'activate' method
 				if (comp.getActivate() != null) {
 					statuses.add(validateActivateMethod(ctx, type, comp
 							.getActivate()));
 				}
 
 				// validate 'deactivate' method
 				if (comp.getDeactivate() != null) {
 					statuses.add(validateDeactivateMethod(ctx, type, comp
 							.getDeactivate()));
 				}
 
				return ConstraintStatus.createMultiStatus(ctx, statuses);
 			} else
 				return ctx.createSuccessStatus();
 		} catch (JavaModelException e) {
 			return ctx.createFailureStatus(comp);
 		}
 	}
 
 	/**
 	 * <ol>
 	 * <li>The method takes a single argument and the type of the argument is
 	 * <code>org.osgi.service.component.ComponentContext</code>.</li>
 	 * <li>The method takes a single argument and the type of the argument is
 	 * <code>org.osgi.framework.BundleContext</code>.</li>
 	 * <li>The method takes a single argument and the type of the argument is
 	 * the <code>java.util.Map</code>.</li>
 	 * <li>The method takes two or more arguments and the type of each argument
 	 * must be <code>org.osgi.service.component.ComponentContext</code>,
 	 * <code>org.osgi.framework.BundleContext</code> or
 	 * <code>java.util.Map</code>. If multiple methods match this rule, this
 	 * implies the method name is overloaded and SCR may choose any of the
 	 * methods to call.</li>
 	 * <li>The method takes zero arguments.</li>
 	 * </ol>
 	 * 
 	 * @param ctx
 	 * @param type
 	 * @param activateMethod
 	 * @return
 	 * @throws JavaModelException
 	 */
 	private IStatus validateActivateMethod(IValidationContext ctx, IType type,
 			String activateMethod) throws JavaModelException {
 		return validateMethod(ctx, type, activateMethod,
 				ScrPackage.Literals.COMPONENT__ACTIVATE, validActivateParams);
 
 	}
 
 	/**
 	 * <ol>
 	 * <li>The method takes a single argument and the type of the argument is
 	 * <code>org.osgi.service.component.ComponentContext</code>.</li>
 	 * <li>The method takes a single argument and the type of the argument is
 	 * <code>org.osgi.framework.BundleContext</code>.</li>
 	 * <li>The method takes a single argument and the type of the argument is
 	 * the <code>java.util.Map</code>.</li>
 	 * <li>The method takes a single argument and the type of the argument is
 	 * the <code>int</code>.</li>
 	 * <li>The method takes a single argument and the type of the argument is
 	 * the <code>java.lang.Integer</code>.</li>
 	 * <li>The method takes two or more arguments and the type of each argument
 	 * must be <code>org.osgi.service.component.ComponentContext</code>,
 	 * <code>org.osgi.framework.BundleContext</code>, <code>java.util.Map</code>, <code>int</code> or <code>java.lang.Integer</code>. If multiple methods
 	 * match this rule, this implies the method name is overloaded and SCR may
 	 * choose any of the methods to call.</li>
 	 * <li>The method takes zero arguments.</li>
 	 * </ol>
 	 * 
 	 * @param ctx
 	 * @param type
 	 * @param activateMethod
 	 * @return
 	 * @throws JavaModelException
 	 */
 	private IStatus validateDeactivateMethod(IValidationContext ctx,
 			IType type, String deactivateMethod) throws JavaModelException {
 		return validateMethod(ctx, type, deactivateMethod,
 				ScrPackage.Literals.COMPONENT__DEACTIVATE,
 				validDeactivateParams);
 
 	}
 
 	private IStatus validateMethod(IValidationContext ctx, IType type,
 			String methodName, EStructuralFeature feature,
 			Collection<String> validParams) throws JavaModelException {
 
 		IStatus returnStatus = ctx.createSuccessStatus();
 		boolean methodFound = false;
 		boolean methodAccessible = false;
 		boolean wrongParameters = false;
 
 		for (IMethod method : type.getMethods()) {
 			if (method.exists() && methodName.equals(method.getElementName())) {
 				methodFound = true;
 				if (Flags.isPublic(method.getFlags())
 						|| Flags.isProtected(method.getFlags())) {
 					methodAccessible = true;
 					for (String paramType : method.getParameterTypes()) {
 						if (!validParams.contains(paramType)) {
 							wrongParameters = true;
 							break;
 						}
 					}
 				}
 			}
 		}
 		if (methodFound) {
 			if (methodAccessible) {
 				if (wrongParameters) {
 					returnStatus = new EnhancedConstraintStatus(
 							(ConstraintStatus) ctx.createFailureStatus(
 									methodName,
 									"No method with a valid signature"),
 							feature);
 				}
 			} else {
 				returnStatus = new EnhancedConstraintStatus(
 						(ConstraintStatus) ctx.createFailureStatus(methodName,
 								"The method is not accessible"), feature);
 			}
 		} else {
 			returnStatus = new EnhancedConstraintStatus((ConstraintStatus) ctx
 					.createFailureStatus(methodName,
 							"No method with this name has been found"), feature);
 		}
 
 		return returnStatus;
 
 	}
 
 }
