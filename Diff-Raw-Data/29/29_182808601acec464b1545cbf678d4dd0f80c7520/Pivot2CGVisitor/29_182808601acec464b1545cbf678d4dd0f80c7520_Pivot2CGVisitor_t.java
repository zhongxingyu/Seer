 /**
  * <copyright>
  * 
  * Copyright (c) 2013 CEA LIST and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   E.D.Willink(CEA LIST) - Initial API and implementation
  * 
  * </copyright>
  */
 package org.eclipse.ocl.examples.codegen.analyzer;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGClass;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGCollectionExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGCollectionPart;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGConstant;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGConstantExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGConstraint;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGConstructorExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGConstructorPart;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGEcoreClassConstructorExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGEcoreDataTypeConstructorExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGEcoreOperationCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGEcorePropertyCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGElement;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGExecutorConstructorPart;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGExecutorOperation;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGExecutorOperationCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGExecutorProperty;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGExecutorPropertyCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGFinalVariable;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGIfExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGInteger;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGIsInvalidExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGIsUndefinedExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGIterationCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGIterator;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGLetExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGLibraryIterateCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGLibraryIterationCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGLibraryOperationCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGLibraryPropertyCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGModelFactory;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGNamedElement;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGOperation;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGOperationCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGPackage;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGParameter;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGProperty;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGPropertyCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGReal;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGString;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGTupleExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGTuplePart;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGTuplePartCallExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGTypeExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGTypeId;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGTypedElement;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGValuedElement;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGVariable;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGVariableExp;
 import org.eclipse.ocl.examples.codegen.cgmodel.CGBuiltInIterationCallExp;
 import org.eclipse.ocl.examples.codegen.generator.CodeGenerator;
 import org.eclipse.ocl.examples.codegen.generator.GenModelException;
 import org.eclipse.ocl.examples.codegen.generator.GenModelHelper;
 import org.eclipse.ocl.examples.codegen.generator.IterationHelper;
import org.eclipse.ocl.examples.domain.elements.DomainOperation;
 import org.eclipse.ocl.examples.domain.ids.TuplePartId;
 import org.eclipse.ocl.examples.domain.ids.TypeId;
 import org.eclipse.ocl.examples.domain.library.LibraryIteration;
 import org.eclipse.ocl.examples.domain.library.LibraryOperation;
 import org.eclipse.ocl.examples.domain.library.LibraryProperty;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.domain.values.Unlimited;
 import org.eclipse.ocl.examples.domain.values.UnlimitedValue;
 import org.eclipse.ocl.examples.library.oclany.OclAnyOclIsInvalidOperation;
 import org.eclipse.ocl.examples.library.oclany.OclAnyOclIsUndefinedOperation;
 import org.eclipse.ocl.examples.pivot.BooleanLiteralExp;
 import org.eclipse.ocl.examples.pivot.CollectionItem;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralExp;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralPart;
 import org.eclipse.ocl.examples.pivot.CollectionRange;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.ConstructorExp;
 import org.eclipse.ocl.examples.pivot.ConstructorPart;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.EnumLiteralExp;
 import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
 import org.eclipse.ocl.examples.pivot.IfExp;
 import org.eclipse.ocl.examples.pivot.IntegerLiteralExp;
 import org.eclipse.ocl.examples.pivot.InvalidLiteralExp;
 import org.eclipse.ocl.examples.pivot.IterateExp;
 import org.eclipse.ocl.examples.pivot.Iteration;
 import org.eclipse.ocl.examples.pivot.IteratorExp;
 import org.eclipse.ocl.examples.pivot.LetExp;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.NullLiteralExp;
 import org.eclipse.ocl.examples.pivot.OCLExpression;
 import org.eclipse.ocl.examples.pivot.OpaqueExpression;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.OperationCallExp;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.PropertyCallExp;
 import org.eclipse.ocl.examples.pivot.RealLiteralExp;
 import org.eclipse.ocl.examples.pivot.StateExp;
 import org.eclipse.ocl.examples.pivot.StringLiteralExp;
 import org.eclipse.ocl.examples.pivot.TupleLiteralExp;
 import org.eclipse.ocl.examples.pivot.TupleLiteralPart;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypeExp;
 import org.eclipse.ocl.examples.pivot.TypedElement;
 import org.eclipse.ocl.examples.pivot.UnlimitedNaturalLiteralExp;
 import org.eclipse.ocl.examples.pivot.UnspecifiedValueExp;
 import org.eclipse.ocl.examples.pivot.Variable;
 import org.eclipse.ocl.examples.pivot.VariableDeclaration;
 import org.eclipse.ocl.examples.pivot.VariableExp;
 import org.eclipse.ocl.examples.pivot.ecore.EObjectOperation;
 import org.eclipse.ocl.examples.pivot.ecore.EObjectProperty;
 import org.eclipse.ocl.examples.pivot.internal.impl.TuplePartImpl;
 import org.eclipse.ocl.examples.pivot.library.CompositionProperty;
import org.eclipse.ocl.examples.pivot.library.ConstrainedOperation;
 import org.eclipse.ocl.examples.pivot.library.ConstrainedProperty;
 import org.eclipse.ocl.examples.pivot.library.ExplicitNavigationProperty;
 import org.eclipse.ocl.examples.pivot.library.ImplicitNonCompositionProperty;
 import org.eclipse.ocl.examples.pivot.library.StaticProperty;
 import org.eclipse.ocl.examples.pivot.library.TuplePartProperty;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.util.AbstractExtendingVisitor;
 import org.eclipse.ocl.examples.pivot.util.Visitable;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 
 /**
  * A CGElementVisitor handles the Pivot AST visits on behalf of a CodeGenAnalyzer.
  * Derived visitors may support an extended AST.
  */
 public class Pivot2CGVisitor extends AbstractExtendingVisitor<CGNamedElement, CodeGenAnalyzer>
 {
 	protected final @NonNull CodeGenerator codeGenerator;
 	protected final @NonNull MetaModelManager metaModelManager;
 	protected final @NonNull GenModelHelper genModelHelper;
 
 	protected static class Variables
 	{
 		private @Nullable Variables outerVariables;
 		/*
 		 * The pivot to CG parameter map assists in construction of ExpressionInOcl before/without an Operation.
 		 */
 		private final @NonNull Map<Variable, CGParameter> cgParameters = new HashMap<Variable, CGParameter>();
 
 		private @NonNull Map<VariableDeclaration, CGVariable> cgVariables = new HashMap<VariableDeclaration, CGVariable>();
 		
 		public Variables(@Nullable Variables outerVariables) {
 			this.outerVariables = outerVariables;
 		}
 
 		public CGParameter getParameter(@NonNull Variable pVariable) {
 			CGParameter cgVariable = cgParameters.get(pVariable);
 			if (cgVariable != null) {
 				return cgVariable;
 			}
 			else if (outerVariables != null) {
 				return outerVariables.getParameter(pVariable);
 			}
 			else {
 				return null;
 			}
 		}
 
 		public CGVariable getVariable(@NonNull VariableDeclaration pVariable) {
 			CGVariable cgVariable = cgVariables.get(pVariable);
 			if (cgVariable != null) {
 				return cgVariable;
 			}
 			else if (outerVariables != null) {
 				return outerVariables.getVariable(pVariable);
 			}
 			else {
 				return null;
 			}
 		}
 
 		public void putParameter(@NonNull Variable aParameter, @NonNull CGParameter cgParameter) {
 			cgParameters.put(aParameter, cgParameter);
 			cgVariables.put(aParameter, cgParameter);
 		}
 
 		public void putVariable(@NonNull VariableDeclaration pVariable, @NonNull CGVariable cgVariable) {
 			cgVariables.put(pVariable, cgVariable);
 		}
 	}
 	
 	/**
 	 * Mapping from a pivot Variable to a CG Variable, maintained as a stack that is pushed when inline operations are exapanded.
 	 */
 	private @NonNull Variables variablesStack = new Variables(null);
 
 	private @NonNull Map<OpaqueExpression, ExpressionInOCL> inlinePrototypes = new HashMap<OpaqueExpression, ExpressionInOCL>();
 	
 	public Pivot2CGVisitor(@NonNull CodeGenAnalyzer analyzer) {
 		super(analyzer);
 		codeGenerator = context.getCodeGenerator();
 		metaModelManager = codeGenerator.getMetaModelManager();
 		genModelHelper = codeGenerator.getGenModelHelper();
 	}
 
 	protected void addParameter(@NonNull Variable aParameter, @NonNull CGParameter cgParameter) {
 		variablesStack.putParameter(aParameter, cgParameter);
 	}
 
 	public @Nullable CGVariable basicGetParameter(@NonNull Variable aParameter) {
 		return variablesStack.getParameter(aParameter);
 	}
 
 /*	public @NonNull CGParameter createCGParameter(@NonNull VariableDeclaration pVariable) {
 		CGParameter cgParameter = CGModelFactory.eINSTANCE.createCGParameter();
 		setPivot(cgParameter, pVariable);
 		cgParameter.setTypeId(context.getTypeId(TypeId.OCL_VOID));			// FIXME Java-specific
 		return cgParameter;
 	} */
 
 	protected @NonNull CGLetExp createCGLetExp(@NonNull CGVariable cgVariable, @NonNull CGValuedElement cgIn) {
 		CGLetExp cgLetExp = CGModelFactory.eINSTANCE.createCGLetExp();
 		cgLetExp.setInit(cgVariable);
 		cgLetExp.setIn(cgIn);
 		cgLetExp.setPivot(cgVariable.getPivot());
 		cgLetExp.setTypeId(cgIn.getTypeId());
 		return cgLetExp;
 	}
 
 	public @NonNull CGVariable createCGVariable(@NonNull Variable pVariable) {
 		CGVariable cgVariable = variablesStack.getVariable(pVariable);
 		if (cgVariable == null) {
 			CGFinalVariable cgVariable2 = CGModelFactory.eINSTANCE.createCGFinalVariable();
 			cgVariable = cgVariable2;
 			variablesStack.putVariable(pVariable, cgVariable);
 		}
 		else {
 			assert cgVariable.eContainer() == null;
 		}
 		setPivot(cgVariable, pVariable);
 //		cgVariable.setInit(doVisit(CGValuedElement.class, pVariable.getInitExpression()));
 		return cgVariable;
 	}
 
 	protected CGVariable createCGVariable(@NonNull Variable contextVariable, @NonNull OCLExpression source) {
 		CGVariable cgVariable = createCGVariable(contextVariable);
 		cgVariable.setInit((CGValuedElement) source.accept(this));
 		return cgVariable;
 	}
 
 	public @NonNull <T extends CGElement> T doVisit(@NonNull Class<T> requiredClass, @Nullable Element pElement) {
 		if (pElement == null) {
 			throw new NullPointerException("null source for mapping to " + requiredClass.getName());
 		}
 		CGNamedElement cgElement = pElement.accept(this);
 		if (cgElement == null) {
 			throw new NullPointerException("null result of mapping to " + requiredClass.getName());
 		}
 		Class<? extends CGNamedElement> actualClass = cgElement.getClass();
 		if (!requiredClass.isAssignableFrom(actualClass)) {
 			throw new ClassCastException("cannot cast " + actualClass.getName() + " result of mapping to " + requiredClass.getName());
 		}
 		@SuppressWarnings("unchecked") T cgElement2 = (T) cgElement;
 		return cgElement2;
 	}
 
 	public @NonNull CodeGenAnalyzer getAnalyzer() {
 		return context;
 	}
 
 	public @NonNull CGIterator getIterator(@NonNull VariableDeclaration pVariable) {
 		CGParameter cgParameter = (CGParameter) variablesStack.getVariable(pVariable);
 		if (cgParameter == null) {
 			cgParameter = CGModelFactory.eINSTANCE.createCGIterator();
 			setPivot(cgParameter, pVariable);
 			cgParameter.setTypeId(context.getTypeId(TypeId.OCL_VOID));			// FIXME Java-specific
 			variablesStack.putVariable(pVariable, cgParameter);
 		}
 		return (CGIterator) cgParameter;
 	}
 
 	public @NonNull CGParameter getParameter(@NonNull Variable aParameter) {
 		CGParameter cgParameter = variablesStack.getParameter(aParameter);
 		if (cgParameter == null) {
 			cgParameter = CGModelFactory.eINSTANCE.createCGParameter();
 			context.setNames(cgParameter, aParameter);
 			setPivot(cgParameter, aParameter);
 			cgParameter.setTypeId(context.getTypeId(TypeId.OCL_VOID));			// FIXME Java-specific
 			addParameter(aParameter, cgParameter);
 			cgParameter.setRequired(aParameter.isRequired());
 			if (aParameter.isRequired()) {
 				cgParameter.setNonNull();
 			}
 		}
 		return cgParameter;
 	}
 
 	public @NonNull CGVariable getSelfParameter(@NonNull Variable aParameter) {
 		return getParameter(aParameter);
 	}
 
 	public @NonNull CGVariable getVariable(@NonNull VariableDeclaration pVariable) {
 		CGVariable cgVariable = variablesStack.getVariable(pVariable);
 		if (cgVariable == null) {
 			cgVariable = CGModelFactory.eINSTANCE.createCGFinalVariable();
 			setPivot(cgVariable, pVariable);
 			variablesStack.putVariable(pVariable, cgVariable);
 		}
 		return cgVariable;
 	}
 
 	public @NonNull Variables getVariablesStack() {
 		return variablesStack;
 	}
 
 	protected @Nullable CGValuedElement inlineOperationCall(@NonNull OperationCallExp callExp, @NonNull OpaqueExpression bodyExpression) {
 		ExpressionInOCL prototype = inlinePrototypes.get(bodyExpression);
 		if (prototype == null) {
 			try {
 				prototype = PivotUtil.getExpressionInOCL(callExp, bodyExpression);
 			}
 			catch (Exception e) {
 				// FIXME log error
 				e.printStackTrace();
 			}
 			if (prototype == null) {
 				return null;
 			}
 			inlinePrototypes.put(bodyExpression, prototype);
 		}
 		variablesStack = new Variables(variablesStack);
 		try {
 			Variable contextVariable = DomainUtil.nonNullModel(prototype.getContextVariable());
 			CGVariable cgContextVariable = getParameter(contextVariable);
 			OCLExpression source = callExp.getSource();
 			CGValuedElement cgSource = doVisit(CGValuedElement.class, source);
 			cgContextVariable.setInit(cgSource);
 			cgSource.setName(cgContextVariable.getName());
 			List<OCLExpression> arguments = callExp.getArgument();
 			List<Variable> parameterVariables = prototype.getParameterVariable();
 			int iMax = Math.min(arguments.size(), parameterVariables.size());
 			for (int i = 0; i < iMax; i++) {
 				OCLExpression argument = arguments.get(i);
 				CGValuedElement cgArgument = doVisit(CGValuedElement.class, argument);
 				@SuppressWarnings("null")@NonNull Variable parameterVariable = parameterVariables.get(i);
 				CGVariable cgParameterVariable = getParameter(parameterVariable);
 				cgParameterVariable.setInit(cgArgument);
 				cgArgument.setName(cgParameterVariable.getName());
 			}
 			//
 			CGValuedElement cgResult = doVisit(CGValuedElement.class, prototype);
 			for (int i = iMax-1; i >= 0; i--) {
 				@SuppressWarnings("null")@NonNull Variable parameterVariable = parameterVariables.get(i);
 				CGVariable cgParameter = getParameter(parameterVariable);
 				cgResult = createCGLetExp(cgParameter, cgResult);
 			}
 			cgResult = createCGLetExp(cgContextVariable, cgResult);
 			return cgResult;
 		}
 		finally {
 			Variables outerVariables = variablesStack.outerVariables;
 			if (outerVariables != null) {
 				variablesStack = outerVariables;
 			}
 		}
 	}
 
 	protected boolean isEcoreProperty(@NonNull LibraryProperty libraryProperty) {
 		return (libraryProperty instanceof ExplicitNavigationProperty)
 			|| (libraryProperty instanceof CompositionProperty)
 			|| (libraryProperty instanceof ImplicitNonCompositionProperty)
 			|| (libraryProperty instanceof StaticProperty)
 			|| (libraryProperty instanceof ConstrainedProperty)
 			|| (libraryProperty instanceof EObjectProperty);
 	}
 
 	protected void setPivot(@NonNull CGNamedElement cgElement, @NonNull NamedElement pivotElement) {
 		cgElement.setPivot(pivotElement);
 		cgElement.setName(pivotElement.getName());
 	}
 
 	protected void setPivot(@NonNull CGTypedElement cgElement, @NonNull TypedElement pivotElement) {
 		cgElement.setPivot(pivotElement);
 		TypeId pivotTypeId = pivotElement.getTypeId();
 		cgElement.setTypeId(context.getTypeId(pivotTypeId));
 		cgElement.setName(pivotElement.getName());
 	}
 
 /*	@Override
 	public @Nullable CGElement visitAssociationClassCallExp(@NonNull AssociationClassCallExp element) {
 //		CGElement thisAnalysis = context.getCurrentAnalysis();
 		return super.visitAssociationClassCallExp(element);
 	} */
 
 	@Override
 	public @Nullable CGConstantExp visitBooleanLiteralExp(@NonNull BooleanLiteralExp element) {
 		CGConstant constant = context.getBoolean(element.isBooleanSymbol());
 		CGConstantExp cgLiteralExp = context.createCGConstantExp(element, constant);
 		setPivot(cgLiteralExp, element);
 //		thisAnalysis.initHashSource(DomainUtil.nonNullModel(Boolean.valueOf(element.isBooleanSymbol())));
 		return cgLiteralExp;
 	}
 
 	@Override
 	public @Nullable CGClass visitClass(@NonNull org.eclipse.ocl.examples.pivot.Class element) {
 		CGClass cgClass = CGModelFactory.eINSTANCE.createCGClass();
 		setPivot(cgClass, element);
 		for (Constraint pivotConstraint : element.getOwnedInvariant()) {
 			CGConstraint cgConstraint = doVisit(CGConstraint.class, pivotConstraint);
 			cgClass.getInvariants().add(cgConstraint);
 		}
 		for (Operation pivotOperation : element.getOwnedOperation()) {
 			CGOperation cgOperation = doVisit(CGOperation.class, pivotOperation);
 			cgClass.getOperations().add(cgOperation);
 		}
 		for (Property pivotProperty : element.getOwnedAttribute()) {
 			CGProperty cgProperty = doVisit(CGProperty.class, pivotProperty);
 			cgClass.getProperties().add(cgProperty);
 		}
 		return cgClass;
 	}
 
 	@Override
 	public @Nullable CGCollectionPart visitCollectionItem(@NonNull CollectionItem element) {
 		CGCollectionPart cgCollectionPart = CGModelFactory.eINSTANCE.createCGCollectionPart();
 		setPivot(cgCollectionPart, element);
 		cgCollectionPart.setFirst(doVisit(CGValuedElement.class, element.getItem()));
 		return cgCollectionPart;
 	} 
 
 	@Override
 	public @Nullable CGCollectionExp visitCollectionLiteralExp(@NonNull CollectionLiteralExp element) {
 		CGCollectionExp cgCollectionExp = CGModelFactory.eINSTANCE.createCGCollectionExp();
 		setPivot(cgCollectionExp, element);
 		cgCollectionExp.setName(element.getKind().getName());
 		List<CGCollectionPart> cgParts = cgCollectionExp.getParts();
 		for (CollectionLiteralPart pivotPart : element.getPart()) {
 			cgParts.add((CGCollectionPart) pivotPart.accept(this));
 		}
 		context.getTypeId(element.getTypeId());
 		return cgCollectionExp;
 	}
 
 	@Override
 	public @Nullable CGCollectionPart visitCollectionRange(@NonNull CollectionRange element) {
 		CGCollectionPart cgCollectionPart = CGModelFactory.eINSTANCE.createCGCollectionPart();
 		setPivot(cgCollectionPart, element);
 		cgCollectionPart.setFirst(doVisit(CGValuedElement.class, element.getFirst()));
 		cgCollectionPart.setLast(doVisit(CGValuedElement.class, element.getLast()));
 		cgCollectionPart.setTypeId(context.getTypeId(TypeId.INTEGER_RANGE));
 		return cgCollectionPart;
 	}
 
 	@Override
 	public @Nullable CGConstraint visitConstraint(@NonNull Constraint element) {
 		CGConstraint cgConstraint = CGModelFactory.eINSTANCE.createCGConstraint();
 		setPivot(cgConstraint, element);
 		OpaqueExpression specification = element.getSpecification();
 		if (specification != null) {
 			ExpressionInOCL expressionInOCL = PivotUtil.getExpressionInOCL(element, specification);
 			if (expressionInOCL != null) {
 				Variable contextVariable = expressionInOCL.getContextVariable();
 				if (contextVariable != null) {
 					getSelfParameter(contextVariable);
 				}
 				for (@SuppressWarnings("null")@NonNull Variable parameterVariable : expressionInOCL.getParameterVariable()) {
 					getParameter(parameterVariable);
 				}
 				cgConstraint.setBody(doVisit(CGValuedElement.class, expressionInOCL.getBodyExpression()));
 				OCLExpression messageExpression = expressionInOCL.getMessageExpression();
 				if (messageExpression != null) {
 					cgConstraint.setMessage(doVisit(CGValuedElement.class, messageExpression));
 				}
 			}
 		}
 		return cgConstraint;
 	}
 
 	@Override
 	public @Nullable CGConstructorExp visitConstructorExp(@NonNull ConstructorExp element) {
 		CGConstructorExp cgConstructorExp = null;
 		Type type = element.getType();
 		if (type != null) {
 			EObject eTarget = type.getETarget();
 			if (eTarget instanceof EDataType) {
 				CGEcoreDataTypeConstructorExp cgEConstructorExp = CGModelFactory.eINSTANCE.createCGEcoreDataTypeConstructorExp();
 				cgEConstructorExp.setEDataType((EDataType)eTarget);
 				cgEConstructorExp.setString(element.getValue());
 				cgConstructorExp = cgEConstructorExp;
 			}
 			else if (eTarget instanceof EClass) {
 				CGEcoreClassConstructorExp cgEConstructorExp = CGModelFactory.eINSTANCE.createCGEcoreClassConstructorExp();
 				cgEConstructorExp.setEClass((EClass)eTarget);
 				cgConstructorExp = cgEConstructorExp;
 //				LocalContext localContext = globalContext.getLocalContext(element);
 //				if (localContext != null) {
 //					CGExecutorType cgExecutorType = localContext.getExecutorType(type);
 //					cgTypeExp.setReferredType(cgExecutorType);
 //					cgConstructorExp.getDependsOn().add(cgExecutorType);
 //				}
 			}
 		}
 		if (cgConstructorExp != null) {
 			setPivot(cgConstructorExp, element);
 //			context.setNames(cgConstructorExp, element);
 			List<CGConstructorPart> cgParts = cgConstructorExp.getParts();
 			for (ConstructorPart pivotPart : element.getPart()) {
 				cgParts.add((CGConstructorPart) pivotPart.accept(this));
 			}
 		}
 		return cgConstructorExp;
 	}
 
 	@Override
 	public @Nullable CGConstructorPart visitConstructorPart(@NonNull ConstructorPart element) {
 		CGConstructorPart cgConstructorPart = CGModelFactory.eINSTANCE.createCGConstructorPart();
 		setPivot(cgConstructorPart, element);
 		cgConstructorPart.setInit(doVisit(CGValuedElement.class, element.getInitExpression()));
 		Property referredProperty = element.getReferredProperty();
 		if (referredProperty != null) {
 			CGExecutorConstructorPart cgExecutorConstructorPart = context.getExecutorConstructorPart(referredProperty);
 			cgConstructorPart.getDependsOn().add(cgExecutorConstructorPart);
 		}
 		return cgConstructorPart;
 	}
 
 	@Override
 	public @Nullable CGConstantExp visitEnumLiteralExp(@NonNull EnumLiteralExp element) {
 //		CGConstant constant = context.getEnumerationLiteral(element.getReferredEnumLiteral());
 		CGConstant constant = context.getElementId(element.getReferredEnumLiteral().getEnumerationLiteralId());
 		CGConstantExp cgLiteralExp = context.createCGConstantExp(element, constant);
 		setPivot(cgLiteralExp, element);
 //		thisAnalysis.initHashSource(DomainUtil.nonNullModel(Boolean.valueOf(element.isBooleanSymbol())));
 		return cgLiteralExp;
 	}
 
 	@Override
 	public @NonNull CGValuedElement visitExpressionInOCL(@NonNull ExpressionInOCL element) {
 		Variable contextVariable = element.getContextVariable();
 		if (contextVariable != null) {
 			CGVariable cgContext = getParameter(contextVariable);
 			cgContext.setNonInvalid();
 //			cgContext.setNonNull();
 		}
 		for (@SuppressWarnings("null")@NonNull Variable parameterVariable : element.getParameterVariable()) {
 			@SuppressWarnings("unused") CGVariable cgParameter = getParameter(parameterVariable);
 		}
 		CGValuedElement cgBody = doVisit(CGValuedElement.class, element.getBodyExpression());
 //		cgOperation.getDependsOn().add(cgBody);
 		return cgBody;
 	}
 
 	@Override
 	public @NonNull CGIfExp visitIfExp(@NonNull IfExp element) {
 		CGIfExp cgIfExp = CGModelFactory.eINSTANCE.createCGIfExp();
 		setPivot(cgIfExp, element);
 		CGValuedElement cgCondition = doVisit(CGValuedElement.class, element.getCondition());
 		CGValuedElement cgThenExpression = doVisit(CGValuedElement.class, element.getThenExpression());
 		CGValuedElement cgElseExpression = doVisit(CGValuedElement.class, element.getElseExpression());
 		cgIfExp.setCondition(cgCondition);
 		cgIfExp.setThenExpression(cgThenExpression);
 		cgIfExp.setElseExpression(cgElseExpression);
 //		cgIfExp.getDependsOn().add(cgCondition);
 //		cgIfExp.getDependsOn().add(cgThenExpression);
 //		cgIfExp.getDependsOn().add(cgElseExpression);
 		return cgIfExp;
 	}
 
 	@Override
 	public @Nullable CGConstantExp visitIntegerLiteralExp(@NonNull IntegerLiteralExp element) {
 		Number integerSymbol = element.getIntegerSymbol();
 		CGInteger constant = context.getInteger(integerSymbol != null ? integerSymbol : 0);
 		CGConstantExp cgLiteralExp = context.createCGConstantExp(element, constant);
 		setPivot(cgLiteralExp, element);
 		return cgLiteralExp;
 	}
 
 	@Override
 	public @Nullable CGConstantExp visitInvalidLiteralExp(@NonNull InvalidLiteralExp element) {
 		CGConstantExp cgLiteralExp = context.createCGConstantExp(element, context.getInvalid());
 		setPivot(cgLiteralExp, element);
 		return cgLiteralExp;
 	}
 
 	@Override
 	public @NonNull CGIterationCallExp visitIterateExp(@NonNull IterateExp element) {
 		Iteration pivotIteration = element.getReferredIteration();
 		CGValuedElement cgSource = doVisit(CGValuedElement.class, element.getSource());
 		LibraryIteration libraryIteration = (LibraryIteration) pivotIteration.getImplementation();
 		IterationHelper iterationHelper = codeGenerator.getIterationHelper(pivotIteration);
 		if (iterationHelper != null) {
 			CGBuiltInIterationCallExp cgBuiltInIterationCallExp = CGModelFactory.eINSTANCE.createCGBuiltInIterationCallExp();
 			cgBuiltInIterationCallExp.setReferredIteration(pivotIteration);
 			cgBuiltInIterationCallExp.setSource(cgSource);
 			for (@SuppressWarnings("null")@NonNull Variable iterator : element.getIterator()) {
 				CGIterator cgIterator = getIterator(iterator);
 				cgIterator.setTypeId(context.getTypeId(iterator.getTypeId()));
 				cgIterator.setRequired(iterator.isRequired());
 				if (iterator.isRequired()) {
 					cgIterator.setNonNull();
 				}
 				cgIterator.setNonInvalid();
 				cgBuiltInIterationCallExp.getIterators().add(cgIterator);
 			}
 			if (pivotIteration.getOwnedParameter().get(0).isRequired()) {
 				cgBuiltInIterationCallExp.getBody().setRequired(true);
 			}
 			cgBuiltInIterationCallExp.setInvalidating(false);
 			cgBuiltInIterationCallExp.setValidating(false);
 //			cgBuiltInIterationCallExp.setNonNull();
 			setPivot(cgBuiltInIterationCallExp, element);
 			@SuppressWarnings("null")@NonNull Variable accumulator = element.getResult();
 			CGIterator cgAccumulator = getIterator(accumulator);
 			cgAccumulator.setTypeId(context.getTypeId(accumulator.getTypeId()));
 			cgAccumulator.setRequired(accumulator.isRequired());
 			if (accumulator.isRequired()) {
 				cgAccumulator.setNonNull();
 			}
 			cgAccumulator.setInit(doVisit(CGValuedElement.class, accumulator.getInitExpression()));
 			cgAccumulator.setNonInvalid();
 			cgBuiltInIterationCallExp.setAccumulator(cgAccumulator);
 			cgBuiltInIterationCallExp.setBody(doVisit(CGValuedElement.class, element.getBody()));
 /*			CGTypeId cgAccumulatorId = iterationHelper.getAccumulatorTypeId(context, cgBuiltInIterationCallExp);
 			if (cgAccumulatorId != null) {
 				CGIterator cgAccumulator = CGModelFactory.eINSTANCE.createCGIterator();
 				cgAccumulator.setName("accumulator");
 				cgAccumulator.setTypeId(cgAccumulatorId);
 //				cgAccumulator.setRequired(true);
 				cgAccumulator.setNonNull();
 				cgAccumulator.setNonInvalid();
 				cgBuiltInIterationCallExp.setAccumulator(cgAccumulator);
 //				variablesStack.putVariable(pVariable, cgAccumulator);
 //				cgAccumulator.setNonInvalid();
 			} */
 			return cgBuiltInIterationCallExp;
 		}
 		CGLibraryIterateCallExp cgLibraryIterateCallExp = CGModelFactory.eINSTANCE.createCGLibraryIterateCallExp();
 		cgLibraryIterateCallExp.setLibraryIteration(libraryIteration);
 		cgLibraryIterateCallExp.setReferredIteration(pivotIteration);
 		setPivot(cgLibraryIterateCallExp, element);
 		cgLibraryIterateCallExp.setInvalidating(pivotIteration.isInvalidating());
 		cgLibraryIterateCallExp.setValidating(pivotIteration.isValidating());
 		cgLibraryIterateCallExp.setSource(cgSource);
 		for (@SuppressWarnings("null")@NonNull Variable iterator : element.getIterator()) {
 			cgLibraryIterateCallExp.getIterators().add(getIterator(iterator));
 		}
 		Variable result = element.getResult();
 		if (result != null) {
 			CGIterator cgResult = getIterator(result);
 			cgLibraryIterateCallExp.setResult(cgResult);
 			CGValuedElement cgInitExpression = doVisit(CGValuedElement.class, result.getInitExpression());
 			cgResult.setInit(cgInitExpression);
 		}
 		cgLibraryIterateCallExp.setBody(doVisit(CGValuedElement.class, element.getBody()));
 		cgLibraryIterateCallExp.setRequired(pivotIteration.isRequired());
 //		cgIterationCallExp.setOperation(getOperation(element.getReferredOperation()));
 		return cgLibraryIterateCallExp;
 	}
 
 	@Override
 	public @NonNull CGIterationCallExp visitIteratorExp(@NonNull IteratorExp element) {
 		Iteration pivotIteration = element.getReferredIteration();
 		CGValuedElement cgSource = doVisit(CGValuedElement.class, element.getSource());
 		LibraryIteration libraryIteration = (LibraryIteration) pivotIteration.getImplementation();
 		IterationHelper iterationHelper = codeGenerator.getIterationHelper(pivotIteration);
 		if (iterationHelper != null) {
 			CGBuiltInIterationCallExp cgBuiltInIterationCallExp = CGModelFactory.eINSTANCE.createCGBuiltInIterationCallExp();
 			cgBuiltInIterationCallExp.setReferredIteration(pivotIteration);
 			cgBuiltInIterationCallExp.setSource(cgSource);
 			for (@SuppressWarnings("null")@NonNull Variable iterator : element.getIterator()) {
 				CGIterator cgIterator = getIterator(iterator);
 				cgIterator.setTypeId(context.getTypeId(iterator.getTypeId()));
 				cgIterator.setRequired(iterator.isRequired());
 				if (iterator.isRequired()) {
 					cgIterator.setNonNull();
 				}
 				cgIterator.setNonInvalid();
 				cgBuiltInIterationCallExp.getIterators().add(cgIterator);
 			}
 			cgBuiltInIterationCallExp.setInvalidating(false);
 			cgBuiltInIterationCallExp.setValidating(false);
 //			cgBuiltInIterationCallExp.setNonNull();
 			setPivot(cgBuiltInIterationCallExp, element);
 			CGTypeId cgAccumulatorId = iterationHelper.getAccumulatorTypeId(context, cgBuiltInIterationCallExp);
 			if (cgAccumulatorId != null) {
 				CGIterator cgAccumulator = CGModelFactory.eINSTANCE.createCGIterator();
 				cgAccumulator.setName("accumulator");
 				cgAccumulator.setTypeId(cgAccumulatorId);
 //				cgAccumulator.setRequired(true);
 				cgAccumulator.setNonNull();
 				cgAccumulator.setNonInvalid();
 				cgBuiltInIterationCallExp.setAccumulator(cgAccumulator);
 //				variablesStack.putVariable(pVariable, cgAccumulator);
 //				cgAccumulator.setNonInvalid();
 			}
 			cgBuiltInIterationCallExp.setBody(doVisit(CGValuedElement.class, element.getBody()));
 			if (pivotIteration.getOwnedParameter().get(0).isRequired()) {
 				cgBuiltInIterationCallExp.getBody().setRequired(true);
 			}
 			cgBuiltInIterationCallExp.setRequired(pivotIteration.isRequired());
 			return cgBuiltInIterationCallExp;
 		}
 		CGLibraryIterationCallExp cgLibraryIterationCallExp = CGModelFactory.eINSTANCE.createCGLibraryIterationCallExp();
 		cgLibraryIterationCallExp.setLibraryIteration(libraryIteration);
 		cgLibraryIterationCallExp.setReferredIteration(pivotIteration);
 		setPivot(cgLibraryIterationCallExp, element);
 		cgLibraryIterationCallExp.setInvalidating(pivotIteration.isInvalidating());
 		cgLibraryIterationCallExp.setValidating(pivotIteration.isValidating());
 		cgLibraryIterationCallExp.setSource(cgSource);
 		for (@SuppressWarnings("null")@NonNull Variable iterator : element.getIterator()) {
 			cgLibraryIterationCallExp.getIterators().add(getIterator(iterator));
 		}
 		cgLibraryIterationCallExp.setBody(doVisit(CGValuedElement.class, element.getBody()));
 		cgLibraryIterationCallExp.setRequired(pivotIteration.isRequired());
 //		cgIterationCallExp.setOperation(getOperation(element.getReferredOperation()));
 		return cgLibraryIterationCallExp;
 	}
 
 	@Override
 	public @Nullable CGLetExp visitLetExp(@NonNull LetExp element) {
 		CGLetExp cgLetExp = CGModelFactory.eINSTANCE.createCGLetExp();
 		setPivot(cgLetExp, element);
 		Variable variable = element.getVariable();
 		CGValuedElement initExpression = doVisit(CGValuedElement.class, variable.getInitExpression());
 		initExpression.setName(variable.getName());
 		CGFinalVariable cgVariable = (CGFinalVariable) createCGVariable(variable);		// FIXME Lose cast
 		cgVariable.setInit(initExpression);
 //		initExpression.setVariableValue(cgVariable);
 //		variables.put(variable, cgVariable);
 		cgLetExp.setInit(cgVariable);
 		cgLetExp.setIn(doVisit(CGValuedElement.class, element.getIn()));
 		return cgLetExp;
 	}
 
 /*	@Override
 	public @Nullable CGElement visitMessageExp(@NonNull MessageExp element) {
 //		CGElement thisAnalysis = context.getCurrentAnalysis();
 		return super.visitMessageExp(element);
 	} */
 
 	@Override
 	public @Nullable CGConstantExp visitNullLiteralExp(@NonNull NullLiteralExp element) {
 		CGConstantExp cgLiteralExp = context.createCGConstantExp(element, context.getNull());
 		setPivot(cgLiteralExp, element);
 		return cgLiteralExp;
 	}
 
 	@Override
 	public @Nullable CGOperation visitOperation(@NonNull Operation element) {
 		CGOperation cgOperation = CGModelFactory.eINSTANCE.createCGOperation();
 		setPivot(cgOperation, element);
 		cgOperation.setRequired(element.isRequired());
 		OpaqueExpression specification = element.getBodyExpression();
 		if (specification != null) {
 			ExpressionInOCL expressionInOCL = PivotUtil.getExpressionInOCL(element, specification);
 			if (expressionInOCL != null) {
 				Variable contextVariable = expressionInOCL.getContextVariable();
 				if (contextVariable != null) {
 					getSelfParameter(contextVariable);
 				}
 				for (@SuppressWarnings("null")@NonNull Variable parameterVariable : expressionInOCL.getParameterVariable()) {
 					getParameter(parameterVariable);
 				}
 				cgOperation.setBody(doVisit(CGValuedElement.class, expressionInOCL.getBodyExpression()));
 			}
 		}
 		return cgOperation;
 	}
 
 	@Override
 	public @NonNull CGValuedElement visitOperationCallExp(@NonNull OperationCallExp element) {
 		Operation pivotOperation = DomainUtil.nonNullState(element.getReferredOperation());
 //		if ("allOwnedElements".equals(pivotOperation.getName())) {
 //			System.out.println("Got it");
 //		}
 		OCLExpression pSource = element.getSource();
 		CGValuedElement cgSource = pSource != null ? doVisit(CGValuedElement.class, pSource) : null;
 		LibraryOperation libraryOperation = metaModelManager.getImplementation(pivotOperation);
 		if (libraryOperation instanceof OclAnyOclIsInvalidOperation) {
 			CGIsInvalidExp cgIsInvalidExp = CGModelFactory.eINSTANCE.createCGIsInvalidExp();
 			cgIsInvalidExp.setSource(cgSource);
 			setPivot(cgIsInvalidExp, element);
 			cgIsInvalidExp.setInvalidating(false);
 			cgIsInvalidExp.setValidating(true);
 			return cgIsInvalidExp;
 		}
 		if (libraryOperation instanceof OclAnyOclIsUndefinedOperation) {
 			CGIsUndefinedExp cgIsUndefinedExp = CGModelFactory.eINSTANCE.createCGIsUndefinedExp();
 			cgIsUndefinedExp.setSource(cgSource);
 			setPivot(cgIsUndefinedExp, element);
 			cgIsUndefinedExp.setInvalidating(false);
 			cgIsUndefinedExp.setValidating(true);
 			return cgIsUndefinedExp;
 		}
		if ((libraryOperation instanceof ConstrainedOperation) && (pSource != null)) {
			DomainOperation finalOperation = codeGenerator.isFinal(pivotOperation, pSource.getType());
			if (finalOperation != null) {
				OpaqueExpression bodyExpression = pivotOperation.getBodyExpression();
				if (bodyExpression != null) {
					CGValuedElement cgOperationCallExp = inlineOperationCall(element, bodyExpression);
					if (cgOperationCallExp != null) {
						return cgOperationCallExp;
					}
				}
			}
		}
 		OpaqueExpression bodyExpression = null; //pivotOperation.getBodyExpression();
 		if (bodyExpression != null) {
 			CGValuedElement cgOperationCallExp = inlineOperationCall(element, bodyExpression);
 			if (cgOperationCallExp != null) {
 				return cgOperationCallExp;
 			}
 		}
 		CGOperationCallExp cgOperationCallExp = null;
 		if ((libraryOperation != null) && !(libraryOperation instanceof EObjectOperation)) {
 			CGLibraryOperationCallExp cgLibraryOperationCallExp = CGModelFactory.eINSTANCE.createCGLibraryOperationCallExp();
 			cgLibraryOperationCallExp.setLibraryOperation(libraryOperation);
 			cgOperationCallExp = cgLibraryOperationCallExp;
 		}
 		else {		// FIXME proper alternatives
 			EOperation eOperation = (EOperation) pivotOperation.getETarget();
 			if (eOperation != null) {
 				try {
 					genModelHelper.getOperationAccessor(pivotOperation);
 					CGEcoreOperationCallExp cgEcoreOperationCallExp = CGModelFactory.eINSTANCE.createCGEcoreOperationCallExp();
 					cgEcoreOperationCallExp.setEOperation(eOperation);
 					cgOperationCallExp = cgEcoreOperationCallExp;
 				} catch (GenModelException e) {}
 			}
 		}
 		if (cgOperationCallExp == null) {
 			CGExecutorOperationCallExp cgExecutorOperationCallExp = CGModelFactory.eINSTANCE.createCGExecutorOperationCallExp();
 			CGExecutorOperation cgExecutorOperation = context.getExecutorOperation(pivotOperation);
 			cgExecutorOperationCallExp.setExecutorOperation(cgExecutorOperation);
 			cgOperationCallExp = cgExecutorOperationCallExp;
 			cgOperationCallExp.getDependsOn().add(cgExecutorOperation);
 		}
 		cgOperationCallExp.setReferredOperation(pivotOperation);
 		setPivot(cgOperationCallExp, element);
 		cgOperationCallExp.setInvalidating(pivotOperation.isInvalidating());
 		cgOperationCallExp.setValidating(pivotOperation.isValidating());
 		cgOperationCallExp.setRequired(pivotOperation.isRequired());
 		cgOperationCallExp.setSource(cgSource);
 //		cgOperationCallExp.getDependsOn().add(cgSource);
 		for (OCLExpression pArgument : element.getArgument()) {
 			CGValuedElement cgArgument = doVisit(CGValuedElement.class, pArgument);
 			cgOperationCallExp.getArguments().add(cgArgument);
 //			cgOperationCallExp.getDependsOn().add(cgArgument);
 		}
 //		cgOperationCallExp.setOperation(getOperation(element.getReferredOperation()));
 		return cgOperationCallExp;
 	}
 
 	@Override
 	public @Nullable CGPackage visitPackage(@NonNull org.eclipse.ocl.examples.pivot.Package element) {
 		CGPackage cgPackage = CGModelFactory.eINSTANCE.createCGPackage();
 		setPivot(cgPackage, element);
 		for (Type pivotType : element.getOwnedType()) {
 			CGClass cgClass = doVisit(CGClass.class, pivotType);
 			cgPackage.getClasses().add(cgClass);
 		}
 		return cgPackage;
 	}
 
 	@Override
 	public @Nullable CGProperty visitProperty(@NonNull Property element) {
 		CGProperty cgProperty = CGModelFactory.eINSTANCE.createCGProperty();
 		setPivot(cgProperty, element);
 		cgProperty.setRequired(element.isRequired());
 		OpaqueExpression specification = element.getDefaultExpression();
 		if (specification != null) {
 			ExpressionInOCL expressionInOCL = PivotUtil.getExpressionInOCL(element, specification);
 			if (expressionInOCL != null) {
 				Variable contextVariable = expressionInOCL.getContextVariable();
 				if (contextVariable != null) {
 					getSelfParameter(contextVariable);
 				}
 				cgProperty.setBody(doVisit(CGValuedElement.class, expressionInOCL.getBodyExpression()));
 			}
 		}
 		return cgProperty;
 	}
 
 	@Override
 	public @NonNull CGPropertyCallExp visitPropertyCallExp(@NonNull PropertyCallExp element) {
 		Property pivotProperty = DomainUtil.nonNullModel(element.getReferredProperty());
 		LibraryProperty libraryProperty = metaModelManager.getImplementation(pivotProperty);
 		CGPropertyCallExp cgPropertyCallExp = null;
 		if (isEcoreProperty(libraryProperty)) {
 			EStructuralFeature eStructuralFeature = (EStructuralFeature) pivotProperty.getETarget();
 			if (eStructuralFeature != null) {
 				try {
 					genModelHelper.getGetAccessor(eStructuralFeature);
 					CGEcorePropertyCallExp cgEcorePropertyCallExp = CGModelFactory.eINSTANCE.createCGEcorePropertyCallExp();
 					cgEcorePropertyCallExp.setEStructuralFeature(eStructuralFeature);
 					cgPropertyCallExp = cgEcorePropertyCallExp;
 				} catch (GenModelException e) {
 				}
 			}
 		}
 		else if (libraryProperty instanceof TuplePartProperty) {
 			CGTuplePartCallExp cgTuplePartCallExp = CGModelFactory.eINSTANCE.createCGTuplePartCallExp();
 			cgTuplePartCallExp.setPivotTuplePartId(((TuplePartImpl) pivotProperty).getTuplePartId());
 			cgPropertyCallExp = cgTuplePartCallExp;
 		}
 		else {
 			CGLibraryPropertyCallExp cgLibraryPropertyCallExp = CGModelFactory.eINSTANCE.createCGLibraryPropertyCallExp();
 			cgLibraryPropertyCallExp.setLibraryProperty(libraryProperty);
 			cgPropertyCallExp = cgLibraryPropertyCallExp;
 		}
 		if (cgPropertyCallExp == null) {
 			CGExecutorPropertyCallExp cgExecutorPropertyCallExp = CGModelFactory.eINSTANCE.createCGExecutorPropertyCallExp();					
 			CGExecutorProperty cgExecutorProperty = context.getExecutorProperty(pivotProperty);
 			cgExecutorPropertyCallExp.setExecutorProperty(cgExecutorProperty);
 			cgPropertyCallExp = cgExecutorPropertyCallExp;
 			cgPropertyCallExp.getDependsOn().add(cgExecutorProperty);
 		}
 		cgPropertyCallExp.setReferredProperty(pivotProperty);
 		setPivot(cgPropertyCallExp, element);
 		cgPropertyCallExp.setRequired(pivotProperty.isRequired());
 		CGValuedElement cgSource = doVisit(CGValuedElement.class, element.getSource());
 		cgPropertyCallExp.setSource(cgSource);
 		return cgPropertyCallExp;
 	}
 
 	@Override
 	public @Nullable CGConstantExp visitRealLiteralExp(@NonNull RealLiteralExp element) {
 		Number realSymbol = element.getRealSymbol();
 		@SuppressWarnings("null")
 		CGReal cgReal = context.getReal(realSymbol != null ? realSymbol : Double.valueOf(0.0));
 		CGConstantExp cgLiteralExp = context.createCGConstantExp(element, cgReal);
 		setPivot(cgLiteralExp, element);
 		return cgLiteralExp;
 	}
 
 	@Override
 	public @Nullable CGNamedElement visitStateExp(@NonNull StateExp element) {
 //		CGElement thisAnalysis = context.getCurrentAnalysis();
 		return super.visitStateExp(element);
 	}
 
 	@Override
 	public @Nullable CGConstantExp visitStringLiteralExp(@NonNull StringLiteralExp element) {
 		String stringSymbol = element.getStringSymbol();
 		CGString cgString = context.getString(stringSymbol != null ? stringSymbol : "");
 		CGConstantExp cgLiteralExp = context.createCGConstantExp(element, cgString);
 		return cgLiteralExp;
 	}
 
 	@Override
 	public @Nullable CGTupleExp visitTupleLiteralExp(@NonNull TupleLiteralExp element) {
 		CGTupleExp cgTupleExp = CGModelFactory.eINSTANCE.createCGTupleExp();
 		setPivot(cgTupleExp, element);
 		List<CGTuplePart> cgParts = cgTupleExp.getParts();
 		for (TupleLiteralPart pivotPart : element.getPart()) {
 			cgParts.add((CGTuplePart) pivotPart.accept(this));
 		}
 		context.getTypeId(element.getTypeId());
 		return cgTupleExp;
 	}
 
 	@Override
 	public @Nullable CGTuplePart visitTupleLiteralPart(@NonNull TupleLiteralPart element) {
 		CGTuplePart cgTuplePart = CGModelFactory.eINSTANCE.createCGTuplePart();
 		setPivot(cgTuplePart, element);
 		cgTuplePart.setInit(doVisit(CGValuedElement.class, element.getInitExpression()));
 		TuplePartId partId = element.getPartId();
 		if (partId != null) {
 			context.getElementId(partId);
 		}
 		return cgTuplePart;
 	}
 
 	@Override
 	public @Nullable CGTypeExp visitTypeExp(@NonNull TypeExp pTypeExp) {
 //		Type referredType = pTypeExp.getReferredType();
 		CGTypeExp cgTypeExp = CGModelFactory.eINSTANCE.createCGTypeExp();
 //		setPivot(cgTypeExp, pTypeExp);
 		cgTypeExp.setPivot(pTypeExp);
 //		cgTypeExp.setReferredType(context.getT
 //		TypeId pivotTypeId = pTypeExp.getTypeId();
 //		cgTypeExp.setTypeId(context.getTypeId(pivotTypeId)); -- no need to reify the metaclassid
 		cgTypeExp.setName(pTypeExp.getName());
 		return cgTypeExp;
 	}
 
 	@Override
 	public @Nullable CGConstantExp visitUnlimitedNaturalLiteralExp(@NonNull UnlimitedNaturalLiteralExp element) {
 		Number unlimitedNaturalSymbol = element.getUnlimitedNaturalSymbol();
 		CGConstantExp cgLiteralExp;
 		if (unlimitedNaturalSymbol instanceof UnlimitedValue) {
 			cgLiteralExp = context.createCGConstantExp(element, context.getInfinity());
 		}
 		else if (unlimitedNaturalSymbol instanceof Unlimited) {
 			cgLiteralExp = context.createCGConstantExp(element, context.getInfinity());
 		}
 		else if (unlimitedNaturalSymbol != null) {
 			cgLiteralExp = context.createCGConstantExp(element, context.getInteger(unlimitedNaturalSymbol));
 		}
 		else {
 			cgLiteralExp = context.createCGConstantExp(element, context.getInteger(0));
 		}
 		setPivot(cgLiteralExp, element);
 		return cgLiteralExp;
 	}
 
 	@Override
 	public @Nullable CGNamedElement visitUnspecifiedValueExp(@NonNull UnspecifiedValueExp element) {
 //		CGElement thisAnalysis = context.getCurrentAnalysis();
 		return super.visitUnspecifiedValueExp(element);
 	}
 
 	@Override
 	public @Nullable CGVariableExp visitVariableExp(@NonNull VariableExp pVariableExp) {
 		VariableDeclaration referredVariable = pVariableExp.getReferredVariable();
 		CGVariableExp cgVariableExp = CGModelFactory.eINSTANCE.createCGVariableExp();
 		setPivot(cgVariableExp, pVariableExp);
 		if (referredVariable != null) {
 			cgVariableExp.setReferredVariable(getVariable(referredVariable));
 		}
 		return cgVariableExp;
 	}
 
 	@Nullable
 	public CGValuedElement visiting(@NonNull Visitable visitable) {
 		throw new UnsupportedOperationException(getClass().getSimpleName() + ": " + visitable.getClass().getSimpleName());
 	}
 }
