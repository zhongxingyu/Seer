 /**
  * <copyright>
  *
  * Copyright (c) 2010 E.D.Willink and others.
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
  * $Id: OCLinEcoreDeclarationVisitor.java,v 1.8 2011/05/14 10:38:08 ewillink Exp $
  */
 package org.eclipse.ocl.examples.xtext.completeocl.pivot2cs;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.eclipse.ocl.examples.pivot.Class;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.Namespace;
 import org.eclipse.ocl.examples.pivot.OpaqueExpression;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.UMLReflection;
 import org.eclipse.ocl.examples.pivot.ValueSpecification;
 import org.eclipse.ocl.examples.pivot.prettyprint.PrettyPrintExprVisitor;
 import org.eclipse.ocl.examples.pivot.prettyprint.PrettyPrintNameVisitor;
 import org.eclipse.ocl.examples.pivot.prettyprint.PrettyPrintOptions;
 import org.eclipse.ocl.examples.pivot.prettyprint.PrettyPrintTypeVisitor;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TypedRefCS;
 import org.eclipse.ocl.examples.xtext.base.pivot2cs.Pivot2CSConversion;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.BodyCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.ClassifierContextDeclCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.CompleteOCLCSTPackage;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.CompleteOCLDocumentCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.ContextConstraintCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.ContextDeclCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.ContextSpecificationCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.DerCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.InitCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.InvCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.OperationContextDeclCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.PackageDeclarationCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.PostCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.PreCS;
 import org.eclipse.ocl.examples.xtext.completeocl.completeOCLCST.PropertyContextDeclCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.pivot2cs.EssentialOCLDeclarationVisitor;
 
 public class CompleteOCLDeclarationVisitor extends EssentialOCLDeclarationVisitor
 {
 	public CompleteOCLDeclarationVisitor(Pivot2CSConversion context) {
 		super(context);
 	}
 
 	@Override
	public ElementCS visitClass(Class object) {
 		return visitType(object);
 	}
 
 	@Override
 	public ElementCS visitConstraint(Constraint object) {
 		String stereotype = object.getStereotype();
 		ContextConstraintCS csElement = null;
 		if (UMLReflection.BODY.equals(stereotype)) {
 			csElement = context.refreshNamedElement(BodyCS.class, CompleteOCLCSTPackage.Literals.BODY_CS, object);
 		}
 		else if (UMLReflection.DERIVATION.equals(stereotype)) {
 			csElement = context.refreshNamedElement(DerCS.class, CompleteOCLCSTPackage.Literals.DER_CS, object);
 		}
 		else if (UMLReflection.INITIAL.equals(stereotype)) {
 			csElement = context.refreshNamedElement(InitCS.class, CompleteOCLCSTPackage.Literals.INIT_CS, object);
 		}
 		else if (UMLReflection.INVARIANT.equals(stereotype)) {
 			csElement = context.refreshNamedElement(InvCS.class, CompleteOCLCSTPackage.Literals.INV_CS, object);
 		}
 		else if (UMLReflection.POSTCONDITION.equals(stereotype)) {
 			csElement = context.refreshNamedElement(PostCS.class, CompleteOCLCSTPackage.Literals.POST_CS, object);
 		}
 		else if (UMLReflection.PRECONDITION.equals(stereotype)) {
 			csElement = context.refreshNamedElement(PreCS.class, CompleteOCLCSTPackage.Literals.PRE_CS, object);
 		}
 		if (csElement != null) {
 			csElement.setStereotype(stereotype);
 			Namespace namespace = PrettyPrintNameVisitor.getNamespace(object);
 			ValueSpecification specification = object.getSpecification();
 			ContextSpecificationCS csSpec = context.refreshElement(ContextSpecificationCS.class, CompleteOCLCSTPackage.Literals.CONTEXT_SPECIFICATION_CS, specification);
 			csElement.setSpecification(csSpec);
 			if (specification instanceof OpaqueExpression) {
 				PrettyPrintOptions.Global prettyPrintOptions = PrettyPrintTypeVisitor.createOptions(namespace);
 				prettyPrintOptions.setMetaModelManager(context.getMetaModelManager());
 				String expr = PrettyPrintExprVisitor.prettyPrint(specification, prettyPrintOptions);		
 				csSpec.setExprString(expr);
 				OpaqueExpression opaqueExpression = (OpaqueExpression)specification;
 				String message = PivotUtil.getMessage(opaqueExpression);
 				if ((message != null) && (message.length() > 0)) {
 					ContextSpecificationCS csMessageElement = context.refreshElement(ContextSpecificationCS.class, CompleteOCLCSTPackage.Literals.CONTEXT_SPECIFICATION_CS, opaqueExpression);
 					csMessageElement.setExprString(message);
 					csElement.setMessageSpecification(csMessageElement);
 				}
 			}
 		}
 		return csElement;
 	}
 
 	@Override
 	public ElementCS visitOperation(Operation object) {
 		Namespace owningType = (Namespace) object.getOwningType();
 		org.eclipse.ocl.examples.pivot.Class savedScope = context.setScope((org.eclipse.ocl.examples.pivot.Class)owningType);
 		OperationContextDeclCS csContext = context.refreshElement(OperationContextDeclCS.class, CompleteOCLCSTPackage.Literals.OPERATION_CONTEXT_DECL_CS, object);
 		csContext.setOperation(object);
 //		csContext.getNamespace().add(owningType);
 		Type type = object.getType();
 		if (type != null) {
 			TypedRefCS typeRef = context.visitReference(TypedRefCS.class, type);
 			csContext.setOwnedType(typeRef);
 		}
 		context.importPackage(object.getOwningType().getPackage());
 		context.refreshList(csContext.getRules(), context.visitDeclarations(ContextConstraintCS.class, object.getOwnedRules(), null));
 		context.setScope(savedScope);
 		return csContext;
 	}
 
 	@Override
 	public ElementCS visitPackage(org.eclipse.ocl.examples.pivot.Package object) {
 		ElementCS csElement;
 		if (object.eContainer() == null) {
 			CompleteOCLDocumentCS csDocument = context.refreshElement(CompleteOCLDocumentCS.class, CompleteOCLCSTPackage.Literals.COMPLETE_OCL_DOCUMENT_CS, object);
			context.refreshList(csDocument.getPackages(), context.visitDeclarations(PackageDeclarationCS.class, object.getNestedPackages(), null));
 			csElement = csDocument;
 		}
 		else {
 			PackageDeclarationCS csPackage = context.refreshElement(PackageDeclarationCS.class, CompleteOCLCSTPackage.Literals.PACKAGE_DECLARATION_CS, object);
 //			context.refreshList(csPackage.getOwnedType(), context.visitDeclarations(ClassifierCS.class, object.getOwnedTypes(), null));
 			csPackage.setPackage(object);
 			context.importPackage(object);
 			List<ContextDeclCS> contexts = new ArrayList<ContextDeclCS>();
 			for (Type type : object.getOwnedTypes()) {
 				ClassifierContextDeclCS classifierContext = context.visitDeclaration(ClassifierContextDeclCS.class, type);
 				if (classifierContext !=  null) {
 					contexts.add(classifierContext);
 				}
 				for (Operation operation : type.getOwnedOperations()) {
 					OperationContextDeclCS operationContext = context.visitDeclaration(OperationContextDeclCS.class, operation);
 					if (operationContext !=  null) {
 						contexts.add(operationContext);
 					}
 				}
 				for (Property property : type.getOwnedAttributes()) {
 					PropertyContextDeclCS propertyContext = context.visitDeclaration(PropertyContextDeclCS.class, property);
 					if (propertyContext !=  null) {
 						contexts.add(propertyContext);
 					}
 				}
 			}
 			context.refreshList(csPackage.getContexts(), contexts);
 			csElement = csPackage;
 		}
 		return csElement;
 	}
 
 	@Override
 	public ElementCS visitProperty(Property object) {
 		Namespace owningType = (Namespace) object.getOwningType();
 		org.eclipse.ocl.examples.pivot.Class savedScope = context.setScope((org.eclipse.ocl.examples.pivot.Class)owningType);
 		PropertyContextDeclCS csContext = context.refreshElement(PropertyContextDeclCS.class, CompleteOCLCSTPackage.Literals.PROPERTY_CONTEXT_DECL_CS, object);
 		csContext.setProperty(object);
 //		csContext.getNamespace().add(owningType);
 		Type type = object.getType();
 		if (type != null) {
 			TypedRefCS typeRef = context.visitReference(TypedRefCS.class, type);
 			csContext.setOwnedType(typeRef);
 		}
 		context.importPackage(object.getOwningType().getPackage());
 		context.refreshList(csContext.getRules(), context.visitDeclarations(ContextConstraintCS.class, object.getOwnedRules(), null));
 		context.setScope(savedScope);
 		return csContext;
 	}
 
 	@Override
 	public ElementCS visitType(Type object) {
 		ClassifierContextDeclCS csContext = context.refreshElement(ClassifierContextDeclCS.class, CompleteOCLCSTPackage.Literals.CLASSIFIER_CONTEXT_DECL_CS, object);
 		csContext.setClassifier(object);
 		context.importPackage(object.getPackage());
 		context.refreshList(csContext.getRules(), context.visitDeclarations(ContextConstraintCS.class, object.getOwnedRules(), null));
 		return csContext;
 	}
 }
