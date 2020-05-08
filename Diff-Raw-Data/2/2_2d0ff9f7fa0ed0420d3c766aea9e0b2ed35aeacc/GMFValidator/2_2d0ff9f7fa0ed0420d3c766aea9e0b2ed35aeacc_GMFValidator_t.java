 /*
  * Copyright (c) 2005, 2007 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: 
  *    Radek Dvorak (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.validate;
 
 import java.util.HashMap;
 
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecore.util.EObjectValidator;
 import org.eclipse.gmf.internal.validate.AnnotatedDefinitionValidator;
 import org.eclipse.gmf.internal.validate.AnnotatedOclValidator;
 import org.eclipse.gmf.internal.validate.ExternModelImport;
 import org.eclipse.gmf.internal.validate.ValidatorChain;
 
 /**
  * Validator of GMF constraint annotations. 
  * See <A href="package-summary.html"</A> details.
  */
 public class GMFValidator extends ValidatorChain {
 	
 	private static EValidator[] GMF_VALIDATORS = new EValidator[] { 
 		ExternModelImport.getImportValidator(),				
 		new AnnotatedOclValidator(),
// bug #230418		new AnnotatedDefinitionValidator() 
 	};
 	
 	private static final EValidator NO_ECORE_INSTANCE = new ValidatorChain(GMF_VALIDATORS);	
 	
 	private static EValidator[] ALL_VALIDATORS = new EValidator[] { 
 		EObjectValidator.INSTANCE, 
 		NO_ECORE_INSTANCE };
 	
 	/**
 	 * Ecore compliant validator instance.
 	 */
 	public static final EValidator INSTANCE = new ValidatorChain(ALL_VALIDATORS);	
 	
 	private GMFValidator() {
 		super(GMF_VALIDATORS);
 	}
 		
 	/**
 	 * Validates the given object using its registered EValidator and  
 	 * additionally performs validation of <code>OCL constraints annotations</code>,
 	 * value-spec and constraint definitions. 
 	 * </p>
 	 * 
 	 * @param eObject the subject for validation
 	 * @return resulting root diagnostic object containing the children diagnostic elements representing
 	 * 	the concrete constraint validation results
 	 */
 	public static Diagnostic validate(EObject eObject) {
 		Diagnostician diagnostician = new Diagnostician(new DelegateRegistry());
 		return diagnostician.validate(eObject);
 	}	
 
 	/**
 	 * Validates the given object using its registered EValidator and  
 	 * additionally performs validation of <code>OCL constraints annotations</code>,
 	 * value-spec and constraint definitions. 
 	 * </p>
 	 * 
 	 * @param eObject the subject for validation
 	 * @param options validation options
 	 * @return resulting root diagnostic object containing the children diagnostic elements representing
 	 * 	the concrete constraint validation results
 	 */	
 	public static Diagnostic validate(EObject eObject, ValidationOptions options) {
 		Diagnostician diagnostician = new Diagnostician(new DelegateRegistry(options));
 		return diagnostician.validate(eObject);
 	}
 		
 	private static class DelegateRegistry extends HashMap<EPackage, Object> implements Registry {
 		private static final long serialVersionUID = 8069287594754687573L;
 		
 		private ValidationOptions options;
 		private EValidator gmfValidator;
 		private EValidator noEcoreValidator;
 		
 		@SuppressWarnings("synthetic-access")
 		DelegateRegistry() {
 			this(null);
 			gmfValidator = GMFValidator.INSTANCE;
 			noEcoreValidator = GMFValidator.NO_ECORE_INSTANCE;
 		}
 		
 		DelegateRegistry(ValidationOptions options) {
 			this.options = options;
 		}
 		
 		@SuppressWarnings("synthetic-access")
 		private EValidator getGmfValidator() {
 			if(gmfValidator == null) {
 				gmfValidator = new ValidatorChain(ALL_VALIDATORS, options);
 			}
 			return gmfValidator;
 		}
 		
 		@SuppressWarnings("synthetic-access")
 		private EValidator getNoEcoreValidator() {
 			if(noEcoreValidator == null) {
 				noEcoreValidator = new ValidatorChain(GMF_VALIDATORS, options);
 			}
 			return noEcoreValidator;
 		}
 
 		public EValidator getEValidator(EPackage ePackage) {
 			if(containsKey(ePackage)) {
 				return (EValidator)super.get(ePackage);
 			}
 			EValidator delegateValidator = Registry.INSTANCE.getEValidator(ePackage);
 			if(delegateValidator == null || delegateValidator.getClass().equals(EObjectValidator.class)) {
 				return getGmfValidator();
 			}
 			return createDelegator(ePackage, delegateValidator);
 		}
 		
 		@SuppressWarnings("unchecked")
 		private EValidator createDelegator(Object key, EValidator delegate) {		
 			// extend custom validator retrieved from the registry only with GMF validators
 			EValidator delegatingValidator = new ValidatorChain(new EValidator[] { delegate, getNoEcoreValidator() });
 			put((EPackage)key, delegatingValidator);
 			return delegatingValidator;
 		}
 
 		public Object get(Object key) {
 			Object provider = super.get(key);
 			if(provider != null) {
 				return provider;
 			}
 			provider = Registry.INSTANCE.get(key);			
 			if(provider != null && provider instanceof EValidator) {
 				if(provider.getClass().equals(EObjectValidator.class)) {
 					return getGmfValidator();
 				}
 				provider = createDelegator(key, (EValidator)provider);
 			}
 			return provider;
 		}
 	}		
 }
