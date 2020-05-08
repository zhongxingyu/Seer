 /**
  * <copyright>
  *
  * Copyright (c) 2011 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.ocl.examples.codegen.common;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.ids.BuiltInTypeId;
 import org.eclipse.ocl.examples.domain.ids.CollectionTypeId;
 import org.eclipse.ocl.examples.domain.ids.TypeId;
 import org.eclipse.ocl.examples.domain.values.Unlimited;
 import org.eclipse.ocl.examples.pivot.DataType;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
 import org.eclipse.ocl.examples.pivot.IntegerLiteralExp;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.Namespace;
 import org.eclipse.ocl.examples.pivot.OpaqueExpression;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.OperationCallExp;
 import org.eclipse.ocl.examples.pivot.Parameter;
 import org.eclipse.ocl.examples.pivot.ParserException;
 import org.eclipse.ocl.examples.pivot.PivotConstants;
 import org.eclipse.ocl.examples.pivot.PivotFactory;
 import org.eclipse.ocl.examples.pivot.PrimitiveType;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.RealLiteralExp;
 import org.eclipse.ocl.examples.pivot.Root;
 import org.eclipse.ocl.examples.pivot.SelfType;
 import org.eclipse.ocl.examples.pivot.StringLiteralExp;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.UnlimitedNaturalLiteralExp;
 import org.eclipse.ocl.examples.pivot.ValueSpecification;
 import org.eclipse.ocl.examples.pivot.context.ClassContext;
 import org.eclipse.ocl.examples.pivot.context.DiagnosticContext;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.manager.PackageServer;
 import org.eclipse.ocl.examples.pivot.manager.TypeServer;
 import org.eclipse.ocl.examples.pivot.prettyprint.PrettyPrintOptions;
 import org.eclipse.ocl.examples.pivot.prettyprint.PrettyPrinter;
 import org.eclipse.ocl.examples.pivot.util.Visitable;
 import org.eclipse.ocl.examples.pivot.utilities.Pivot2Moniker;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.essentialocl.cs2pivot.EssentialOCLLeft2RightVisitor;
 
 public class PivotQueries
 {
 	private static final Logger logger = Logger.getLogger(EssentialOCLLeft2RightVisitor.class);
 	
 	/**
 	 * Workaround Acceleo's lack of BigInteger support
 	 */
 	public static @Nullable String asIntegerStringOrNull(@NonNull Element element) {
 		if (element instanceof IntegerLiteralExp) {
 			return element.toString();
 		}
 		if (element instanceof UnlimitedNaturalLiteralExp) {
 			Number unlimitedNaturalSymbol = ((UnlimitedNaturalLiteralExp)element).getUnlimitedNaturalSymbol();
 			if (!(unlimitedNaturalSymbol instanceof Unlimited)) {
 				return element.toString();
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Workaround Acceleo's lack of BigDecimal support
 	 */
 	public static @Nullable String asRealStringOrNull(@NonNull Element element) {
 		if (element instanceof RealLiteralExp) {
 			return element.toString();
 		}
 		return null;
 	}
 
 	protected static @NonNull ExpressionInOCL createExpressionInOCLError(@NonNull String string) {
 		ExpressionInOCL expressionInOCL = PivotFactory.eINSTANCE.createExpressionInOCL();
 		StringLiteralExp stringLiteral = PivotFactory.eINSTANCE.createStringLiteralExp();
 		stringLiteral.setStringSymbol(string);
 		expressionInOCL.setMessageExpression(stringLiteral);
 		return expressionInOCL;
 	}
 
 	protected static @NonNull PrettyPrintOptions.Global createOptions(@NonNull Visitable element) {
 		Namespace scope = null;
 		if (element instanceof EObject) {
 			for (EObject eObject = (EObject) element; eObject != null; ) {
 				if (eObject instanceof Root) {
 					break;
 				}
 				if (eObject instanceof Type) {
 					scope = (Namespace) eObject;
 					break;
 				}
 				if (eObject instanceof org.eclipse.ocl.examples.pivot.Package) {
 					scope = (Namespace) eObject;
 					break;
 				}
 				if (eObject instanceof ExpressionInOCL) {
 					eObject = ((ExpressionInOCL)eObject).getContextVariable().getType();
 				}
 				else {
 					eObject = eObject.eContainer();
 				}
 			}
 		}
 		PrettyPrintOptions.Global createOptions = PrettyPrinter.createOptions(scope);
 		createOptions.setLinelength(80);
 		if (element instanceof EObject) {
 			Resource resource = EcoreUtil.getRootContainer((EObject)element).eResource();
 			if (resource != null) {
 				MetaModelManager metaModelManager = MetaModelManager.getAdapter(resource.getResourceSet());
 				if (metaModelManager != null) {
 					createOptions.setMetaModelManager(metaModelManager);
 				}
 			}
 		}
 		return createOptions;
 	}
 	
 	protected int getAllSuperClasses(@NonNull Map<Type, Integer> results, @NonNull Type aClass) {
 		Integer depth = results.get(aClass);
 		if (depth != null) {
 			return depth;
 		}
 		int myDepth = 0;
 		for (Type superClass : aClass.getSuperClass()) {
 			assert superClass != null;
 			int superDepth = getAllSuperClasses(results, superClass);
 			if (superDepth >= myDepth) {
 				myDepth = superDepth+1;
 			}
 		}
 		results.put(aClass, myDepth);
 		return myDepth;
 	}
 	
 	public @Nullable Type getBehavioralType(@NonNull Type type) {
 		if (type instanceof DataType) {
 			DataType dataType = (DataType) type;
 			return dataType.getBehavioralType();
 		}
 		return null;
 	}
 	
 	public int getDepth(@NonNull Type aClass) {
 		Map<Type, Integer> results = new HashMap<Type, Integer>();
 		return getAllSuperClasses(results, aClass);
 	}
 
 	/**
 	 * Return an OCL AST from a ValueSpecification in the context of a NamedElement. If it is necessary
 	 * to parse OCL concrete syntax and errors result an ExpressionInOCL is returned with a null
 	 * contextVariable, a null bodyExpression, and a StringLiteral messageExpression
 	 * containing the error messages.
 	 */
 	public static @Nullable ExpressionInOCL getExpressionInOCL(@NonNull NamedElement contextElement, @NonNull ValueSpecification specification) {
 		if (specification instanceof ExpressionInOCL) {
 			return (ExpressionInOCL) specification;
 		}
 		else if (specification instanceof OpaqueExpression) {
 			Resource resource = contextElement.eResource();
 			ResourceSet resourceSet = resource.getResourceSet();
 			MetaModelManager metaModelManager = MetaModelManager.getAdapter(resourceSet);
 			ClassContext parserContext = null;
 			if (metaModelManager != null) {
 				parserContext = (ClassContext)metaModelManager.getParserContext(contextElement);
 //				if (contextElement instanceof Property) {
 //					parserContext = new PropertyContext(metaModelManager, null, (Property) contextElement);
 //				}
 //				else if (contextElement instanceof Operation) {
 //					parserContext = new OperationContext(metaModelManager, null, (Operation) contextElement, null);
 //				}
 //				else if (contextElement instanceof org.eclipse.ocl.examples.pivot.Class) {
 //					parserContext = new ClassContext(metaModelManager, null, (org.eclipse.ocl.examples.pivot.Class) contextElement);
 //				}
 			}
 			if (parserContext == null) {
 				logger.error("Unknown context type");
 				return null;
 			}
 			OpaqueExpression opaqueExpression = (OpaqueExpression) specification;
 			String expression = PivotUtil.getBody(opaqueExpression);
 			if (expression == null) {
 				return createExpressionInOCLError("Missing expression");
 			}
 			ExpressionInOCL expressionInOCL = null;
 			try {				
 				expressionInOCL = parserContext.parse(expression);
 			} catch (ParserException e) {
 				String message = e.getMessage();
 				if (message == null) {
 					message = "";
 				}
 				logger.error(message);
 				return createExpressionInOCLError(message);
 			}
 			if (expressionInOCL != null) {
 				String messageExpression = PivotUtil.getMessage(opaqueExpression);
 				if ((messageExpression != null) && (messageExpression.trim().length() > 0)) {
 					try {
 						parserContext = new DiagnosticContext(parserContext, null);
 						parserContext.parse(messageExpression);
 					} catch (ParserException e) {
 						logger.error("Failed to parse \"" + messageExpression + "\"", e);
 					}
 				}
 			}
 			return expressionInOCL;
 		}
 		else {
 			Resource resource = contextElement.eResource();
 			ResourceSet resourceSet = resource.getResourceSet();
 			MetaModelManager metaModelManager = MetaModelManager.getAdapter(resourceSet);
 			ExpressionInOCL expressionInOCL = PivotFactory.eINSTANCE.createExpressionInOCL();
 			expressionInOCL.setBodyExpression(metaModelManager.createInvalidExpression());
 			return expressionInOCL;
 		}
 	}
 
 	public static String getFragmentURI(Element element) {
 		return EcoreUtil.getURI(element).fragment().toString();
 	}
 
 	public static @NonNull String getMoniker(@NonNull Element element) {
 		return Pivot2Moniker.toString(element);
 	}
 	
 	public @NonNull LinkedHashSet<Operation> getOperations(@NonNull Type type) {
 		ResourceSet resourceSet = type.eResource().getResourceSet();
 		MetaModelManager metaModelManager = MetaModelManager.getAdapter(resourceSet);
 		LinkedHashSet<Operation> operations = new LinkedHashSet<Operation>();
 		for (Operation operation : metaModelManager.getMemberOperations(type, false)) {
 			operations.add(operation);
 		}
 		for (Operation operation : metaModelManager.getMemberOperations(type, true)) {
 			operations.add(operation);
 		}
 //		if (type.getName().equals("MultiplicityElement")) {
 //			System.out.println("here " + operations.size());
 //		}
 		return operations;
 	}
 	
 	public @NonNull LinkedHashSet<Property> getProperties(@NonNull Type type) {
 		ResourceSet resourceSet = type.eResource().getResourceSet();
 		MetaModelManager metaModelManager = MetaModelManager.getAdapter(resourceSet);
 		LinkedHashSet<Property> properties = new LinkedHashSet<Property>();
 		for (Property property : metaModelManager.getMemberProperties(type, false)) {
 			properties.add(property);
 		}
 		for (Property property : metaModelManager.getMemberProperties(type, true)) {
 			properties.add(property);
 		}
 		return properties;
 	}
 	
 	public String getSignature(@NonNull Operation anOperation) {
 		Type owningType = anOperation.getOwningType();
 		if (owningType == null) {
 			return "null";
 		}
 		String qualifiedSignature = PrettyPrinter.printType(anOperation, (Namespace)owningType);	// FIXME cast
 		int index = qualifiedSignature.indexOf("::");
 		return index > 0 ? qualifiedSignature.substring(index+2) : qualifiedSignature;	// FIXME with PrettyPrintOptions
 	}
 	
 	public @NonNull Set<? extends Type> getTypes(@NonNull org.eclipse.ocl.examples.pivot.Package pPackage) {
 		Resource eResource = pPackage.eResource();
 		assert eResource != null;
 		MetaModelManager metaModelManager = PivotUtil.findMetaModelManager(eResource);
 		assert metaModelManager != null;
 		Set<Type> types = new HashSet<Type>();
 		PackageServer packageServer = metaModelManager.getPackageServer(pPackage);
 		for (TypeServer typeServer : packageServer.getMemberTypes()) {
 			if (!PivotConstants.ORPHANAGE_NAME.equals(typeServer.getName())) {
 				Type type = null;
 				for (DomainType partialType : typeServer.getPartialTypes()) {
 					if (partialType.getPackage() == pPackage) {
 						type = (Type) partialType;
 						break;
 					}
 				}
 				if (type == null) {
 					type = typeServer.getPivotType();
 				}
 				if (type != null) {
 					types.add(type);
 				}
 			}
 		}
 		return types;
 	}
 	
 	public static @NonNull Boolean isBinarySelf(@NonNull OperationCallExp callExp) {
 		Operation operation = callExp.getReferredOperation();
 		if (operation == null) {
 			return false;
 		}
 		List<Parameter> parameters = operation.getOwnedParameter();
 		if (parameters.size() != 1) {
 			return false;
 		}
 		return parameters.get(0).getType() instanceof SelfType;
 	}
 	
 	public static @NonNull Boolean isBoxed(@NonNull Type type) {
 		TypeId typeId = type.getTypeId();
 		if (typeId == TypeId.BOOLEAN) {
 			return false;
 		}
 		if (typeId == TypeId.STRING) {
 			return false;
 		}
 		if (type instanceof PrimitiveType) {
 			return true;
 		}
 		if (typeId instanceof CollectionTypeId) {
 			return true;
 		}
 		if (type instanceof org.eclipse.ocl.examples.pivot.Enumeration) {
 			return true;
 		}
 		return false;
 	}
 	
 	public static @NonNull Boolean isBuiltInType(@NonNull Type type) {
//		System.out.println(DomainUtil.debugSimpleName(type) + " + " + DomainUtil.debugSimpleName(type.getTypeId()) + " + " + type.getTypeId());
 		return type.getTypeId() instanceof BuiltInTypeId;
 	}
 	
 	public static @NonNull String prettyPrint(@NonNull Element element) {
 		PrettyPrintOptions.Global createOptions = createOptions(element);
 		return PrettyPrinter.print(element, createOptions);
 	}
 	
 	public static @NonNull String prettyPrintName(@NonNull Element element) {
 		PrettyPrintOptions.Global createOptions = createOptions(element);
 		return PrettyPrinter.printName(element, createOptions);
 	}
 	
 }
