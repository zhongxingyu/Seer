 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.model.internal.validation;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.jem.internal.java.adapters.JavaReflectionAdaptor;
 import org.eclipse.jem.java.JavaClass;
 import org.eclipse.jem.java.JavaHelpers;
 import org.eclipse.jem.java.Method;
 import org.eclipse.jem.java.TypeKind;
 import org.eclipse.jst.j2ee.ejb.EnterpriseBean;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.provisional.core.IMessage;
 
 
 /**
  * @version 	1.0
  * @author
  */
 public abstract class AComponentVRule extends AInterfaceTypeVRule {
 	public Object getTarget(Object parent, Object clazz) {
 		if(parent == null) {
 			return null;
 		}
 
 		if((isRemote() & REMOTE) == REMOTE) {
 			return ((EnterpriseBean)parent).getRemoteInterface();
 		}
 		return ((EnterpriseBean)parent).getLocalInterface();
 	}
 	
 	public long getDefaultMethodType() {
 		return BUSINESS;
 	}
 	
 	@Override
 	public long[] getBaseTypes() {
 		return getSupertypes();
 	}
 	
 	public void validate(IEJBValidationContext vc, EnterpriseBean bean, JavaClass clazz) throws ValidationCancelledException, InvalidInputException, ValidationException {
 		if(!followRMI_IIOPInheritanceRules(bean, clazz)) {
 			// IWAD4057 = {0} must follow RMI-IIOP rules for remote interfaces. Read section 7.10.5 of the EJB 2.0 specification.
 			// IWAD4210 = {0} must follow RMI-IIOP rules for remote interfaces. Read section 10.6.9 of the EJB 2.0 specification.
 			// IWAD4326 = {0} must follow RMI-IIOP rules for remote interfaces. Read section 12.2.8 of the EJB 2.0 specification.
 			IMessage message = MessageUtility.getUtility().getMessage(vc, IMessagePrefixEjb20Constants.CHKJ2468, IEJBValidationContext.INFO, bean, clazz, this);
 			vc.addMessage(message);
 		}
 		
 		validateAppendixB(vc, bean, clazz);
 	}
 	
 	@Override
 	public void validate(IEJBValidationContext vc, EnterpriseBean bean, JavaClass clazz, Method method, List[] methodsExtendedLists) throws ValidationCancelledException, InvalidInputException, ValidationException {
 		super.validate(vc, bean, clazz, method, methodsExtendedLists); // check application exceptions
 		
 		if(isEJBInterfaceMethod(bean, method)) {
 			// IWAD4021 = {0} is provided by the container. Read section 6.5 of the EJB 2.0 specification.
 			// IWAD4110 = {0} is provided by the container. Read section 9.9 of the EJB 2.0 specification.
 			// IWAD4112 = {0} is provided by the container. Read section 9.10 of the EJB 2.0 specification.
 			IMessage message = MessageUtility.getUtility().getMessage(vc, IMessagePrefixEjb20Constants.CHKJ2469, IEJBValidationContext.INFO, bean, clazz, method, this);
 			vc.addMessage(message);
 		}
 		
 		Method match = ValidationRuleUtility.getMethod(method, method.getName(), getBeanClassMethodsExtended(methodsExtendedLists));
 		if(match == null) {
 			// IWAD4058 = {0} must exist on {1}. Read section 7.10.5 of the EJB 2.0 specificiation.
 			// IWAD4070 = {0} must exist on {1}. Read section 7.10.7 of the EJB 2.0 specification.
 			// IWAD4327 = {0} must exist on {1}. Read section 12.2.8 of the EJB 2.0 specification.
 			// IWAD4354 = {0} must exist on {1}. Read section 12.2.10 of the EJB 2.0 specification.
 			// IWAD4211 = {0} must exist on {1}. Read section 10.6.9 of the EJB 2.0 specification.
 			// IWAD4227 = {0} must exist on {1}. Read section 10.6.11 of the EJB 2.0 specification.
			IMessage message = MessageUtility.getUtility().getMessage(vc, IEJBValidatorMessageConstants.CHKJ2023, IEJBValidationContext.ERROR, bean, clazz, method, new String[]{method.getMethodElementSignature(), bean.getEjbClass().getJavaName()}, this);
 			vc.addMessage(message);
 		}
 		else {
 			if(!ValidationRuleUtility.isAssignableFrom(method.getReturnType(), match.getReturnType())) {
 				// IWAD4212 = This method must return the same type as {0}. Read section 10.6.9 of the EJB 2.0 specification.
 				// IWAD4228 = This method must return the same type as {0}. Read section 10.6.11 of the EJB 2.0 specification.
 				// IWAD4328 = This method must return the same type as {0} on {1}. Read section 12.2.8 of the EJB 2.0 specification.
 				// IWAD4355 = This method must return {0}. Read section 12.2.10 of the EJB 2.0 specification.
 				
 				boolean addMessage = true;
 				
 				//if the return type on the interface is resolvable and the match on the bean is not. flush the bean and recheck if it resolves
 				JavaClass returnType = ValidationRuleUtility.getJavaClass(method.getReturnType());
 				JavaClass beanReturnType = ValidationRuleUtility.getJavaClass(match.getReturnType());
 				if (returnType != null  && beanReturnType != null && returnType.getKind() != TypeKind.UNDEFINED_LITERAL && beanReturnType.getKind() == TypeKind.UNDEFINED_LITERAL) { 
 					// bugzilla 274340 - EJB validation is using a stale JEM cache for bean class
 					JavaReflectionAdaptor adapter = (JavaReflectionAdaptor) JavaReflectionAdaptor.retrieveAdaptorFrom(bean.getEjbClass());
 					adapter.flushReflectedValuesIfNecessary(true);
 					if (ValidationRuleUtility.getMethodExtended(bean.getEjbClass(), method.getName(), method.listParametersWithoutReturn(), method.getReturnType()) != null) {
 						addMessage = false;
 					}
 				}	
 				if (addMessage) {
 					IMessage message = MessageUtility.getUtility().getMessage(vc, IMessagePrefixEjb20Constants.CHKJ2470, IEJBValidationContext.ERROR, bean, clazz, method, new String[]{match.getReturnType().getJavaName()}, this);
 					vc.addMessage(message);
 				}
 			}
 			
 			Set exceptions = ValidationRuleUtility.getNotSubsetExceptions(bean, match, method);
 			Iterator eiterator = exceptions.iterator();
 			while(eiterator.hasNext()) {
 				JavaClass exception = (JavaClass)eiterator.next();
 				IMessage message = MessageUtility.getUtility().getMessage(vc, IMessagePrefixEjb20Constants.CHKJ2471, IEJBValidationContext.WARNING, bean, clazz, method, new String[]{exception.getJavaName(), match.getMethodElementSignature()}, this);
 				vc.addMessage(message);
 			}
 		}
 		
 		if(!followRemoteExceptionRules(bean, method)) {
 			// IWAD4056 = This method must throw java.rmi.RemoteException. Read section 7.10.5 of the EJB 2.0 specification.
 			// IWAD4069 = This method must not throw java.rmi.RemoteException. Read section 7.10.7, 18.3.8, 18.6 of the EJB 2.0 specification.
 			// IWAD4325 = This method must throw java.rmi.RemoteException. Read section 12.2.8 of the EJB 2.0 specification.
 			// IWAD4353 = This method must not throw java.rmi.RemoteException. Read section 12.2.10, 18.3.8, 18.6 of the EJB 2.0 specification.
 			// IWAD4209 = This method must throw java.rmi.RemoteException. Read section 10.6.9 of the EJB 2.0 specification.
 			IMessage message = MessageUtility.getUtility().getMessage(vc, IMessagePrefixEjb20Constants.CHKJ2503_bus, getMessageRemoteExceptionSeverity(), bean, clazz, method, this);
 			vc.addMessage(message);
 		}
 		
 		// IWAD4055 = {0} must be a legal type for RMI-IIOP. Read section 7.10.5 of the EJB 2.0 specification.
 		// IWAD4208 = {0} must be a legal type for RMI-IIOP. Read section 10.6.9 of the EJB 2.0 specification.
 		// IWAD4324 = {0} must be a legal type for RMI-IIOP. Read section 12.2.8 of the EJB 2.0 specification.
 		validateRMI_IIOPTypeRules(vc, bean, clazz, method, methodsExtendedLists, true);
 		
 		JavaHelpers oe = getOverExposedLocalType(bean, clazz, method);
 		if(oe != null) {
 			// IWAD4059 = This method must not expose the {0} type. Read section 7.10.5 of the EJB 2.0 specification.
 			// IWAD4107 = This method must not expose the {0} type. Read section 9.7.2 of the EJB 2.0 specification.
 			// IWAD4330 = This method must not expose the {0} type. Read section 12.2.8 of the EJB 2.0 specification.
 			// IWAD4128 = This method must not expose the {0} type. Read section 10.3.1, 10.3.10.1 of the EJB 2.0 specification.
 			IMessage message = MessageUtility.getUtility().getMessage(vc, IMessagePrefixEjb20Constants.CHKJ2472, IEJBValidationContext.INFO, bean, clazz, method, new String[]{oe.getQualifiedName()}, this);
 			vc.addMessage(message);
 		}
 	}
 	
 	protected void validateAppendixB(IEJBValidationContext vc, EnterpriseBean bean, JavaClass thisComponent) {
 		// The Java inheritance structure must match the EJB inheritance structure.
 		// e.g. if EJB B is a child of EJB A, then class B must be a child of class A.
 		// B could be a grandchild (or great-grandchild or ...) of A.
 		if(bean == null) {
 			return;
 		}
 		EnterpriseBean supertype = getSuperType(bean);
 		JavaClass parentComponent = null;
 		if (supertype != null) {
 			parentComponent = getComponentInterface(supertype);
 
 			if(parentComponent == null) {
 				// child uses either local, or remote, but not both interfaces
 				return;
 			}
 			
 			// Component a Xchild of parent Component
 			try {
 				ValidationRuleUtility.isValidType(thisComponent);
 				ValidationRuleUtility.isValidType(parentComponent);
 				if (!ValidationRuleUtility.isAssignableFrom(thisComponent, parentComponent)) {
 					String[] msgParm = new String[] { thisComponent.getQualifiedName(), parentComponent.getQualifiedName()};
 					IMessage message = MessageUtility.getUtility().getMessage(vc, IEJBValidatorMessageConstants.CHKJ2105, IEJBValidationContext.ERROR, bean, thisComponent, msgParm, this);
 					vc.addMessage(message);
 				}
 			}
 			catch (InvalidInputException e) {
 				String[] msgParm = { e.getJavaClass().getQualifiedName(), bean.getName()};
 				IMessage message = MessageUtility.getUtility().getMessage(vc, IEJBValidatorMessageConstants.CHKJ2849, IEJBValidationContext.WARNING, bean, msgParm, this);
 				vc.addMessage(message);
 			}
 		}
 		
 //		validateAppendixB(vc, supertype, parentComponent);
 	}
 }
