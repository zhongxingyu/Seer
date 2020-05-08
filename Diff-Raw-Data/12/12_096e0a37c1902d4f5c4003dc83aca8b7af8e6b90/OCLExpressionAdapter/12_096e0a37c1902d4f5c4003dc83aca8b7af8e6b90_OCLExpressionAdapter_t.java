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
 package org.eclipse.gmf.internal.validate.ocl;
 
 import java.text.MessageFormat;
 import java.util.HashSet;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EParameter;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.ETypedElement;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.gmf.internal.validate.DebugOptions;
 import org.eclipse.gmf.internal.validate.DefUtils;
 import org.eclipse.gmf.internal.validate.EDataTypeConversion;
 import org.eclipse.gmf.internal.validate.GMFValidationPlugin;
 import org.eclipse.gmf.internal.validate.Messages;
 import org.eclipse.gmf.internal.validate.StatusCodes;
 import org.eclipse.gmf.internal.validate.Trace;
 import org.eclipse.gmf.internal.validate.expressions.AbstractExpression;
 import org.eclipse.gmf.internal.validate.expressions.IEvaluationEnvironment;
 import org.eclipse.gmf.internal.validate.expressions.IParseEnvironment;
 import org.eclipse.ocl.Environment;
 import org.eclipse.ocl.ParserException;
 import org.eclipse.ocl.Query;
 import org.eclipse.ocl.ecore.CallOperationAction;
 import org.eclipse.ocl.ecore.CollectionType;
 import org.eclipse.ocl.ecore.Constraint;
 import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
 import org.eclipse.ocl.ecore.SendSignalAction;
 import org.eclipse.ocl.ecore.TypeType;
 import org.eclipse.ocl.expressions.ExpressionsFactory;
 import org.eclipse.ocl.expressions.Variable;
 
 class OCLExpressionAdapter extends AbstractExpression {
 	/**
 	 * The OCL language identifier.
 	 */
 	public static final String OCL = "ocl"; //$NON-NLS-1$
 	
 	private Query<EClassifier, EClass, EObject> query;
 	private Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, 
 				CallOperationAction, SendSignalAction, Constraint, EClass, EObject> env;
 	
 	public OCLExpressionAdapter(String body, EClassifier context, IParseEnvironment extEnv) {
 		super(body, context, extEnv);
 		
 		try {
 			org.eclipse.ocl.ecore.OCL ocl = null;			
 			if(extEnv != null) {
 				EcoreEnvironmentFactory factory = EcoreEnvironmentFactory.INSTANCE;
 				if(extEnv.getImportRegistry() != null) { 					
 					factory = new EcoreEnvironmentFactory(extEnv.getImportRegistry());
 				}
 				
 				ocl = org.eclipse.ocl.ecore.OCL.newInstance(factory);
 				this.env = ocl.getEnvironment(); 
 				
 				for(String varName : extEnv.getVariableNames()) {
 					EClassifier type = extEnv.getTypeOf(varName);
 					
 					Variable<EClassifier, EParameter> varDecl = ExpressionsFactory.eINSTANCE.createVariable();
 					varDecl.setName(varName);
 					varDecl.setType(type);
 					env.addElement(varDecl.getName(), varDecl, true);
 				}
 			} else {
				EPackage.Registry registry = new EPackageRegistryImpl();
				for (Object nextRegistryObject : EPackage.Registry.INSTANCE.values()) {
					if (nextRegistryObject instanceof EPackage) {
						EPackage ePackage = (EPackage) nextRegistryObject;
						registry.put(ePackage.getNsURI(), ePackage);
					}
				}
				if (context.eResource() != null && context.eResource().getResourceSet() != null) {
					ResourceSet resourceSet = context.eResource().getResourceSet();
 					EcoreUtil.resolveAll(resourceSet);
 					HashSet<EPackage> ePackages = new HashSet<EPackage>();
 					for (Resource resource : resourceSet.getResources()) {
 						ePackages.addAll(EcoreUtil.<EPackage> getObjectsByType(resource.getContents(), EcorePackage.Literals.EPACKAGE));
 					}
 					for (EPackage ePackage : ePackages) {
 						registry.put(ePackage.getNsURI(), ePackage);
 					}
 				}
 				ocl = org.eclipse.ocl.ecore.OCL.newInstance(new EcoreEnvironmentFactory(registry));
 				this.env = ocl.getEnvironment();
 			}
 
 			org.eclipse.ocl.ecore.OCL.Helper helper = ocl.createOCLHelper();
 			helper.setContext(context);			
 			this.query = ocl.createQuery(helper.createQuery(body));
 			
 		} catch (ParserException e) {
 			setInvalidOclExprStatus(e);
 		} catch (IllegalArgumentException e) {
 			setInvalidOclExprStatus(e);				
 		} catch(RuntimeException e) {				
 			setStatus(GMFValidationPlugin.createStatus(
 					IStatus.ERROR, StatusCodes.UNEXPECTED_PARSE_ERROR, 
 					Messages.unexpectedExprParseError, e));
 			GMFValidationPlugin.log(getStatus());
 			Trace.catching(DebugOptions.EXCEPTIONS_CATCHING, e);				
 		}
 	}
 	
 	public String getLanguage() {	
 		return OCL;
 	}
 	
 	public boolean isLooselyTyped() {	
 		return false;
 	}
 	
 	public boolean isAssignableTo(EClassifier ecoreType) {
 		if(env == null) {
 			return false;
 		}
 		
 		EClassifier oclType =  env.getUMLReflection().getOCLType(ecoreType);
 		if(oclType == null) {
 			return false;
 		}
 		return isOclConformantTo(oclType);			
 	}
 	
 	public boolean isAssignableToElement(ETypedElement typedElement) {
 		if(env == null || typedElement.getEType() == null) {
 			return false;
 		}
 		EClassifier oclType = env.getUMLReflection().getOCLType(typedElement);
 		if(oclType == null) {
 			return false;
 		}
 		return isOclConformantTo(oclType);
 	}
 	
 	public EClassifier getResultType() {	
 		return (query != null) ? query.getExpression().getType() : super.getResultType();
 	}
 	
 	protected Object doEvaluate(Object context) {
 		return filterOCLInvalid((query != null) ? query.evaluate(context) : null);
 	}
 	
 	protected Object doEvaluate(Object context, IEvaluationEnvironment extEnvironment) {
 		if(query != null) {
 			query.getEvaluationEnvironment().clear();			
 			for (String varName : extEnvironment.getVariableNames()) {
 				query.getEvaluationEnvironment().add(varName, extEnvironment.getValueOf(varName));
 			}
 		}
 
 		return doEvaluate(context);
 	}
 	
 	private Object filterOCLInvalid(Object object) {
 		return (env != null && object == env.getOCLStandardLibrary().getOclInvalid()) ? null : object;
 	}
 
 	private boolean isOclConformantTo(EClassifier anotherOclType) {
 		EClassifier thisOclType = getResultType();
 		
 		boolean isTargetCollection = anotherOclType instanceof CollectionType; 
 		if(isTargetCollection) {
 			CollectionType oclCollectionType = (CollectionType)anotherOclType;
 			if(oclCollectionType.getElementType() != null) {
 				anotherOclType = oclCollectionType.getElementType();
 			}
 		}
 		
 		if(thisOclType instanceof CollectionType) {
 			if(!isTargetCollection) {
 				return false; // can't assign CollectionType to scalar
 			}
 			CollectionType thisOclCollectionType = (CollectionType)thisOclType;
 			if(thisOclCollectionType.getElementType() != null) {
 				thisOclType = thisOclCollectionType.getElementType();
 			}
 		}
 
 		// handle OCL TypeType
 		if(thisOclType instanceof TypeType) {
 			EClassifier thisRefferedClassifier = ((TypeType)thisOclType).getReferredType();	
 			if(thisRefferedClassifier != null) {
 				return DefUtils.getCanonicalEClassifier(anotherOclType).isInstance(thisRefferedClassifier);
 			}
 		}
 		
 		// Note: in OCL, Double extends Integer
 		if ((thisOclType.getInstanceClass() == Integer.class ||
 				thisOclType.getInstanceClass() == int.class) && 
 			(anotherOclType.getInstanceClass() == Double.class || 
 				anotherOclType.getInstanceClass() == double.class)) {
 			return true;
 		}
 		
 		if(thisOclType instanceof EDataType && anotherOclType instanceof EDataType) {
 			if(new EDataTypeConversion().isConvertable((EDataType)anotherOclType, (EDataType)thisOclType)) {
 				return true;
 			}
 		}
 		
 		return DefUtils.checkTypeAssignmentCompatibility(anotherOclType, thisOclType);			
 	}
 	
 	
 	void setInvalidOclExprStatus(Exception exception) {
 		String message = MessageFormat.format(
 				Messages.invalidExpressionBody, 
 				new Object[] { getBody(), exception.getLocalizedMessage() });
 		
 		setStatus(GMFValidationPlugin.createStatus(
 				IStatus.ERROR, StatusCodes.INVALID_VALUE_EXPRESSION, message, exception));
 		Trace.catching(DebugOptions.EXCEPTIONS_CATCHING, exception);			
 	}
 }
