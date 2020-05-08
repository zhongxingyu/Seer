 /**
  * <copyright>
  *
  * Copyright (c) 2012 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  */
 package org.eclipse.ocl.examples.xtext.essentialocl.cs2pivot;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.ocl.examples.pivot.BooleanLiteralExp;
 import org.eclipse.ocl.examples.pivot.CollectionItem;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralExp;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralPart;
 import org.eclipse.ocl.examples.pivot.CollectionRange;
 import org.eclipse.ocl.examples.pivot.ConstructorExp;
 import org.eclipse.ocl.examples.pivot.ConstructorPart;
 import org.eclipse.ocl.examples.pivot.ExpressionInOcl;
 import org.eclipse.ocl.examples.pivot.IfExp;
 import org.eclipse.ocl.examples.pivot.IntegerLiteralExp;
 import org.eclipse.ocl.examples.pivot.NullLiteralExp;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.OperationCallExp;
 import org.eclipse.ocl.examples.pivot.ParameterableElement;
 import org.eclipse.ocl.examples.pivot.PivotConstants;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.RealLiteralExp;
 import org.eclipse.ocl.examples.pivot.StringLiteralExp;
 import org.eclipse.ocl.examples.pivot.TemplateParameter;
 import org.eclipse.ocl.examples.pivot.TupleLiteralExp;
 import org.eclipse.ocl.examples.pivot.TupleLiteralPart;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.UnlimitedNaturalLiteralExp;
 import org.eclipse.ocl.examples.pivot.Variable;
 import org.eclipse.ocl.examples.pivot.VariableExp;
 import org.eclipse.ocl.examples.pivot.context.ParserContext;
 import org.eclipse.ocl.examples.pivot.scoping.EnvironmentView;
 import org.eclipse.ocl.examples.pivot.scoping.ScopeFilter;
 import org.eclipse.ocl.examples.pivot.utilities.BaseResource;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.CS2Pivot;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.CS2PivotConversion;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.Continuation;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.BooleanLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.CollectionLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.CollectionLiteralPartCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.CollectionTypeCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ConstructorExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ConstructorPartCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ContextCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ExpSpecificationCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.IfExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.IndexExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.InfixExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.InvalidLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.InvocationExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.LiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NameExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NavigatingArgCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NestedExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NullLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NumberLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.OperatorCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.PrefixExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.PrimitiveLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.SelfExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.StringLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.TupleLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.TupleLiteralPartCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.TypeLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.TypeNameExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.UnlimitedNaturalLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.VariableCS;
 
 public class EssentialOCLContainmentVisitor extends AbstractEssentialOCLContainmentVisitor
 {
 	private static final class NotOperationFilter implements ScopeFilter
 	{
 		public static NotOperationFilter INSTANCE = new NotOperationFilter();
 		
 		public int compareMatches(EObject match1, Map<TemplateParameter, ParameterableElement> bindings1,
 				EObject match2, Map<TemplateParameter, ParameterableElement> bindings2) {
 			return 0;
 		}
 
 		public boolean matches(EnvironmentView environmentView,Type forType, EObject eObject) {
 			return !(eObject instanceof Operation);
 		}
 	}
 
 	public EssentialOCLContainmentVisitor(CS2PivotConversion context) {
 		super(context);
 	}
 
 	@Override
 	public Continuation<?> visitBooleanLiteralExpCS(BooleanLiteralExpCS csElement) {
 		BooleanLiteralExp pivotElement = context.refreshModelElement(BooleanLiteralExp.class, PivotPackage.Literals.BOOLEAN_LITERAL_EXP, csElement);
 		pivotElement.setBooleanSymbol(Boolean.valueOf(csElement.getName()));
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitCollectionLiteralExpCS(CollectionLiteralExpCS csElement) {
 		CollectionLiteralExp pivotElement = context.refreshModelElement(CollectionLiteralExp.class, PivotPackage.Literals.COLLECTION_LITERAL_EXP, csElement);
 		context.refreshPivotList(CollectionLiteralPart.class, pivotElement.getPart(), csElement.getOwnedParts());
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitCollectionLiteralPartCS(CollectionLiteralPartCS csElement) {
 		if (csElement.getLastExpressionCS() == null) {
 			context.refreshModelElement(CollectionItem.class, PivotPackage.Literals.COLLECTION_ITEM, csElement);	
 		}
 		else {
 			context.refreshModelElement(CollectionRange.class, PivotPackage.Literals.COLLECTION_RANGE, csElement);
 		}
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitCollectionTypeCS(CollectionTypeCS csElement) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitConstructorExpCS(ConstructorExpCS csElement) {
 		ConstructorExp pivotElement = context.refreshModelElement(ConstructorExp.class, PivotPackage.Literals.CONSTRUCTOR_EXP, csElement);
 		pivotElement.setValue(csElement.getValue());
 		context.refreshPivotList(ConstructorPart.class, pivotElement.getPart(), csElement.getOwnedParts());
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitConstructorPartCS(ConstructorPartCS csElement) {
 		context.refreshModelElement(ConstructorPart.class, PivotPackage.Literals.CONSTRUCTOR_PART, csElement);	
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitContextCS(ContextCS csElement) {
 		ExpressionInOcl pivotElement = context.refreshModelElement(ExpressionInOcl.class, PivotPackage.Literals.EXPRESSION_IN_OCL, csElement);
		pivotElement.setBodyExpression(null);
		pivotElement.setMessageExpression(null);
 		Resource resource = csElement.eResource();
 		if (resource instanceof BaseResource) {	
 			ParserContext parserContext = ((BaseResource)resource).getParserContext();
 			if (parserContext != null) {
 				parserContext.initialize(context, pivotElement);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitExpCS(ExpCS csElement) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitExpSpecificationCS(ExpSpecificationCS csElement) {
 		ExpressionInOcl pivotElement = context.refreshModelElement(ExpressionInOcl.class, PivotPackage.Literals.EXPRESSION_IN_OCL, csElement);
 		pivotElement.getLanguage().add(PivotConstants.OCL_LANGUAGE);
 //		pivotElement.getBody().add(csElement.getExprString());
 		pivotElement.getMessage().add(null);
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitIfExpCS(IfExpCS csElement) {
 		context.refreshModelElement(IfExp.class, PivotPackage.Literals.IF_EXP, csElement);
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitIndexExpCS(IndexExpCS csElement) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitInfixExpCS(InfixExpCS csElement) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitInvalidLiteralExpCS(InvalidLiteralExpCS csElement) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitInvocationExpCS(InvocationExpCS csElement) {
 		CS2Pivot.setElementType(csElement.getPathName(), PivotPackage.Literals.OPERATION, csElement, null);
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitLiteralExpCS(LiteralExpCS csElement) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitNameExpCS(NameExpCS csElement) {
 		CS2Pivot.setElementType(csElement.getPathName(), PivotPackage.Literals.ELEMENT, csElement, NotOperationFilter.INSTANCE);
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitNavigatingArgCS(NavigatingArgCS csElement) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitNestedExpCS(NestedExpCS csElement) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitNullLiteralExpCS(NullLiteralExpCS csElement) {
 		context.refreshModelElement(NullLiteralExp.class, PivotPackage.Literals.NULL_LITERAL_EXP, csElement);
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitNumberLiteralExpCS(NumberLiteralExpCS csElement) {
 		Number number = csElement.getName();
 		if (number instanceof BigDecimal) {
 			RealLiteralExp pivotElement = context.refreshModelElement(RealLiteralExp.class, PivotPackage.Literals.REAL_LITERAL_EXP, csElement);
 			pivotElement.setRealSymbol((BigDecimal) number);
 		}
 		else {
 			BigInteger bigInteger = (BigInteger) number;
 			if (bigInteger.signum() < 0) {
 				IntegerLiteralExp pivotElement = context.refreshModelElement(IntegerLiteralExp.class, PivotPackage.Literals.INTEGER_LITERAL_EXP, csElement);
 				pivotElement.setIntegerSymbol(bigInteger);
 			}
 			else {
 				UnlimitedNaturalLiteralExp pivotElement = context.refreshModelElement(UnlimitedNaturalLiteralExp.class, PivotPackage.Literals.UNLIMITED_NATURAL_LITERAL_EXP, csElement);
 				pivotElement.setUnlimitedNaturalSymbol(bigInteger);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitOperatorCS(OperatorCS csElement) {
 		OperationCallExp pivotElement = context.refreshModelElement(OperationCallExp.class, PivotPackage.Literals.OPERATION_CALL_EXP, csElement);
 		context.refreshName(pivotElement, csElement.getName());
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitPrefixExpCS(PrefixExpCS csElement) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitPrimitiveLiteralExpCS(PrimitiveLiteralExpCS csElement) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitSelfExpCS(SelfExpCS csElement) {
 		context.refreshModelElement(VariableExp.class, PivotPackage.Literals.VARIABLE_EXP, csElement);
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitStringLiteralExpCS(StringLiteralExpCS csElement) {
 		StringLiteralExp pivotElement = context.refreshModelElement(StringLiteralExp.class, PivotPackage.Literals.STRING_LITERAL_EXP, csElement);
 		List<String> names = csElement.getName();
 		if (names.size() == 0) {
 			pivotElement.setStringSymbol("");
 		}
 		else if (names.size() == 1) {
 			pivotElement.setStringSymbol(names.get(0));
 		}
 		else {
 			StringBuilder s = new StringBuilder();
 			for (String name : names) {
 				s.append(name);
 			}
 			pivotElement.setStringSymbol(s.toString());
 		}
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitTupleLiteralExpCS(TupleLiteralExpCS csElement) {
 		TupleLiteralExp pivotElement = context.refreshModelElement(TupleLiteralExp.class, PivotPackage.Literals.TUPLE_LITERAL_EXP, csElement);	
 		context.refreshPivotList(TupleLiteralPart.class, pivotElement.getPart(), csElement.getOwnedParts());
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitTupleLiteralPartCS(TupleLiteralPartCS csElement) {
 		refreshNamedElement(TupleLiteralPart.class, PivotPackage.Literals.TUPLE_LITERAL_PART, csElement);	
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitTypeLiteralExpCS(TypeLiteralExpCS csElement) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitTypeNameExpCS(TypeNameExpCS csElement) {
 		CS2Pivot.setElementType(csElement.getPathName(), PivotPackage.Literals.TYPE, csElement, null);
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitUnlimitedNaturalLiteralExpCS(UnlimitedNaturalLiteralExpCS csElement) {
 		UnlimitedNaturalLiteralExp pivotElement = context.refreshModelElement(UnlimitedNaturalLiteralExp.class, PivotPackage.Literals.UNLIMITED_NATURAL_LITERAL_EXP, csElement);
 		pivotElement.setName("*");
 		pivotElement.setUnlimitedNaturalSymbol(BigInteger.valueOf(-1));
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitVariableCS(VariableCS csElement) {
 		refreshNamedElement(Variable.class, PivotPackage.Literals.VARIABLE, csElement);
 		return null;
 	}
 }
