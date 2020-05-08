 /**
  * <copyright>
  *
  * Copyright (c) 2010,2011 E.D.Willink and others.
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
  * $Id: EssentialOCLPostOrderVisitor.java,v 1.5 2011/04/20 19:02:15 ewillink Exp $
  */
 package org.eclipse.ocl.examples.xtext.essentialocl.cs2pivot;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
 import org.eclipse.ocl.examples.pivot.AssociativityKind;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.OpaqueExpression;
 import org.eclipse.ocl.examples.pivot.PivotConstants;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.Precedence;
 import org.eclipse.ocl.examples.pivot.Variable;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil.PrecedenceComparator;
 import org.eclipse.ocl.examples.xtext.base.baseCST.PathElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.PathNameCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.SpecificationCS;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.BasicContinuation;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.CS2PivotConversion;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.Continuation;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.SingleContinuation;
 import org.eclipse.ocl.examples.xtext.base.utilities.ElementUtil;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.BinaryOperatorCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.CollectionTypeCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ContextCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.InfixExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.InvocationExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NameExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NavigatingArgCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NavigationRole;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.OperatorCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.PrefixExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.TypeNameExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.UnaryOperatorCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.VariableCS;
 import org.eclipse.xtext.nodemodel.ICompositeNode;
 import org.eclipse.xtext.nodemodel.ILeafNode;
 import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
 
 public class EssentialOCLPostOrderVisitor extends AbstractEssentialOCLPostOrderVisitor
 {
 	static final Logger logger = Logger.getLogger(EssentialOCLPostOrderVisitor.class);
 
 	protected static class ContextCSCompletion extends SingleContinuation<ContextCS>
 	{
 		public ContextCSCompletion(CS2PivotConversion context, ContextCS csElement) {
 			super(context, null, null, csElement);
 		}
 
 		@Override
 		public BasicContinuation<?> execute() {
 			context.visitLeft2Right(Element.class, csElement);
 			return null;
 		}
 	}
 
 	protected final MetaModelManager metaModelManager;
 	
 	public EssentialOCLPostOrderVisitor(CS2PivotConversion context) {
 		super(context);
 		this.metaModelManager = context.getMetaModelManager();
 	}
 
 	/**
 	 * Establish the parent-{source,argument} relationships between all infix
 	 * operators in accordance with the precedence and associativity configuration.
 	 */
 	protected void createInfixOperatorTree(InfixExpCS csInfix) {
 		//
 		//	Create the per-precedence list of operator indexes, and a
 		//	highest precedence first list of all used infix precedences.
 		//
 		Map<Precedence, List<Integer>> precedenceToOperatorIndexes = createInfixPrecedenceToOperatorIndexesMap(csInfix);
 		List<Precedence> sortedPrecedences = new ArrayList<Precedence>(precedenceToOperatorIndexes.keySet());
 		Collections.sort(sortedPrecedences, PrecedenceComparator.INSTANCE);
 		//
 		//	Build the tree leaf-to root precedence at a time.
 		//
 		List<ExpCS> csExpressions = csInfix.getOwnedExpression();
 		List<BinaryOperatorCS> csOperators = csInfix.getOwnedOperator();
 		for (Precedence precedence : sortedPrecedences) {
 			// null precedence arises when precedence or operation-to-precedence is wrong
 			boolean isLeft = precedence == null || (precedence.getAssociativity() == AssociativityKind.LEFT);
 			List<Integer> operatorIndexes = precedenceToOperatorIndexes.get(precedence);
 			int operatorCount = operatorIndexes.size();
 			int iFirst = isLeft ? 0 : operatorCount-1;
 			int iIndex = isLeft ? 1 : -1;
 			int iLast = isLeft ? operatorCount : -1;
 			for (int i = iFirst; i != iLast; i += iIndex) {
 				int operatorIndex = operatorIndexes.get(i);
 				BinaryOperatorCS csOperator = csOperators.get(operatorIndex);
 				//
 				//	Establish parent-child relationship of operator source
 				//
 				ExpCS csSource = csExpressions.get(operatorIndex);
 				while ((csSource.getParent() != null) && (csSource.getParent() != csOperator)) {
 					csSource = csSource.getParent();
 				}
 				setSource(csOperator, csSource);
 				//
 				//	Establish parent-child relationship of operator argument
 				//
 				ExpCS csArgument = csExpressions.get(operatorIndex+1);
 				while ((csArgument.getParent() != null) && (csArgument.getParent() != csOperator)) {
 					csArgument = csArgument.getParent();
 				}
 				setArgument(csOperator, csArgument);
 			}
 		}
 	}
 
 	/**
 	 * Return a map of operator indexes for each used precedence.
 	 */
 	protected Map<Precedence, List<Integer>> createInfixPrecedenceToOperatorIndexesMap(InfixExpCS csInfix) {
 		List<BinaryOperatorCS> csOperators = csInfix.getOwnedOperator();
 		int operatorCount = csOperators.size();
 		Map<Precedence, List<Integer>> precedenceToOperatorIndex = new HashMap<Precedence, List<Integer>>();
 		for (int operatorIndex = 0; operatorIndex < operatorCount; operatorIndex++) {
 			BinaryOperatorCS csOperator = csOperators.get(operatorIndex);
 			Precedence precedence = metaModelManager.getInfixPrecedence(csOperator.getName());
 			List<Integer> indexesList = precedenceToOperatorIndex.get(precedence);
 			if (indexesList == null) {
 				indexesList = new ArrayList<Integer>();
 				precedenceToOperatorIndex.put(precedence, indexesList);
 			}
 			indexesList.add(operatorIndex);
 		}
 		return precedenceToOperatorIndex;
 	}
 
 	protected void initializePrefixOperators(PrefixExpCS prefixExpCS, OperatorCS csParent) {
 		prefixExpCS.setParent(null);		// FIXME asymmetric
 		for (UnaryOperatorCS csUnaryOperator : prefixExpCS.getOwnedOperator()) {
 			if (csParent instanceof UnaryOperatorCS) {
 				setSource(csParent, csUnaryOperator);
 			}
 			else if (csParent instanceof BinaryOperatorCS) {
 				if (csParent.getSource() == prefixExpCS) {
 					setSource(csParent, csUnaryOperator);
 				}
 				else {
 					setArgument((BinaryOperatorCS) csParent, csUnaryOperator);
 				}
 			}
 			ExpCS csChild = prefixExpCS.getOwnedExpression();
 			setSource(csUnaryOperator, csChild);
 			csParent = csUnaryOperator;
 		}
 	}
 
 	protected void interleavePrefixes(InfixExpCS csElement) {
 		for (ExpCS csExp : csElement.getOwnedExpression()) {
 			if (csExp instanceof PrefixExpCS) {
 				PrefixExpCS prefixExpCS = (PrefixExpCS)csExp;
 				OperatorCS csExpressionParent = prefixExpCS.getParent();
 				initializePrefixOperators(prefixExpCS, csExpressionParent);			
 				for (UnaryOperatorCS csUnaryOperator : prefixExpCS.getOwnedOperator()) {
 					interleaveUnaryOperator(csUnaryOperator);
 				}			
 			}
 		}
 	}
 	
 	protected void interleaveUnaryOperator(UnaryOperatorCS csOperator) {
 		while (true) {
 			OperatorCS csParent = csOperator.getParent();
 			if (!(csParent instanceof BinaryOperatorCS)) {
 				break;
 			}
 			Precedence parentPrecedence = metaModelManager.getInfixPrecedence(csParent.getName());
 			Precedence unaryPrecedence = metaModelManager.getPrefixPrecedence(csOperator.getName());
 			int parentOrder = parentPrecedence != null ? parentPrecedence.getOrder().intValue() : -1;
 			int unaryOrder = unaryPrecedence != null ? unaryPrecedence.getOrder().intValue() : -1;
 			if (unaryOrder < parentOrder) {
 				break;
 			}
 			OperatorCS csGrandParent = csParent.getParent();
 			ExpCS csExp = csOperator.getSource();
 			if (csOperator == csParent.getSource()) {
 				setSource(csParent, null);			// Avoid a transient loop
 				if (csGrandParent instanceof BinaryOperatorCS) {
 					if (csGrandParent.getSource() == csParent) {
 						setSource(csGrandParent, csOperator);
 					}
 					else {
 						setArgument((BinaryOperatorCS) csGrandParent, csOperator);
 					}
 				}
 //				else if (csGrandParent == null) {
 //					setSource(null, csOperator);
 //				}
 				setSource(csOperator, csParent);
 				setSource(csParent, csExp);
 			}
 //			else if (csOperator == ((BinaryOperatorCS) csParent).getArgument()) {
 //				if (csGrandParent instanceof BinaryOperatorCS) {
 //					if (csGrandParent.getSource() == csParent) {
 //						setSource(csOperator, null);			// Avoid a transient loop
 //						setSource(csGrandParent, csExp);		
 //						setSource(csOperator, csGrandParent);
 //					}
 //				}
 //			}
 			else {
 				break;
 			}
 		}
 	}
 
 	private void setArgument(BinaryOperatorCS csParent, ExpCS csArgument) {
 		csArgument.setParent(csParent);
 		csParent.setArgument(csArgument);
 		int i = 0;
 		for (OperatorCS csOperator = csParent.getParent(); csOperator != null; csOperator = csOperator.getParent()) {
 			if (csOperator == csParent) {
 				logger.error("Operator loop established");
 			}
 			else if (i++ > 1000) {
 				logger.error("Operator depth limit exceeded");
 			}
 		}
 	}
 
 	private void setParameterRole(NavigatingArgCS csArgument, NavigationRole aRole) {
 		csArgument.setRole(aRole);
 /*		if ((csArgument.getOwnedType() == null) && (csArgument.getInit() == null)) {
 			ExpCS csExp = csArgument.getName();
 			if (csExp instanceof InfixExpCS) {
 				InfixExpCS csInfixExp = (InfixExpCS)csExp;
 				// If init without type is ever legal; Fixup a = b				
 			}
 		} */
 		ExpCS csName = csArgument.getName();
 		if (csName instanceof NameExpCS) {
 			PathNameCS csPathName = ((NameExpCS)csName).getPathName();
 			Variable parameter = context.refreshModelElement(Variable.class, PivotPackage.Literals.VARIABLE, csName);
 			ICompositeNode node = NodeModelUtils.getNode(csName);
 			ILeafNode leafNode = ElementUtil.getLeafNode(node);
 			String varName = leafNode.getText();
 			context.refreshName(parameter, varName);
 			List<PathElementCS> path = csPathName.getPath();
 			PathElementCS csPathElement = path.get(path.size()-1);
 			csPathElement.setElement(parameter);	// Resolve the reference that is actually a definition
 			csPathElement.setElementType(null);		// Indicate a definition to the syntax colouring
 		}
 	}
 
 	private void setSource(OperatorCS csParent, ExpCS csSource) {
 		if (csSource != null) {
 			csSource.setParent(csParent);
 			int i = 0;
 			for (OperatorCS csOperator = csParent.getParent(); csOperator != null; csOperator = csOperator.getParent()) {
 				if (csOperator == csParent) {
 					logger.error("Operator loop established");
 				}
 				else if (i++ > 1000) {
 					logger.error("Operator depth limit exceeded");
 				}
 			}
 			csParent.setSource(csSource);
 		}
 		else {
 			csParent.getSource().setParent(null);
 			csParent.setSource(csSource);
 		}
 	}
 
 	@Override
 	public Continuation<?> visitCollectionTypeCS(CollectionTypeCS csCollectionType) {
 		// FIXME untemplated collections need type deduction here
 /*		MetaModelManager metaModelManager = context.getMetaModelManager();
 		TypedRefCS csElementType = csCollectionType.getOwnedType();
 		Type type;
 		if (csElementType != null) {
 			Type elementType = PivotUtil.getPivot(Type.class, csElementType);
 			type = metaModelManager.getLibraryType(csCollectionType.getName(), Collections.singletonList(elementType));
 		}
 		else {
 			type = metaModelManager.getLibraryType(csCollectionType.getName());
 		}
 		context.reusePivotElement(csCollectionType, type);
 */		return null;
 	}
 
 	@Override
 	public Continuation<?> visitContextCS(ContextCS csContext) {
 		ExpCS ownedExpression = csContext.getOwnedExpression();
 		if (ownedExpression != null) {
 			return new ContextCSCompletion(context, csContext);
 		}
 		else {
 			return null;
 		}
 	}
 
 	@Override
 	public Continuation<?> visitExpCS(ExpCS csExp) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitInfixExpCS(InfixExpCS csInfixExp) {
 		//
 		//	Establish the Infix tree and the per leaf expression parent operator.
 		//
 		createInfixOperatorTree(csInfixExp);
 		//
 		//	Interleave the Prefix Operators.
 		//
 		interleavePrefixes(csInfixExp);
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitInvocationExpCS(InvocationExpCS csNavigatingExp) {
 		List<NavigatingArgCS> csArguments = csNavigatingExp.getArgument();
 		if (csArguments.size() > 0) {
 			// Last argument is always an expression
 			//	then preceding initialized terms are accumulators
 			//	 then preceding terms are iterators
 			NavigationRole role = NavigationRole.EXPRESSION;
 			for (int i = csArguments.size()-1; i >= 0; i--) {
 				NavigatingArgCS csArgument = csArguments.get(i);
 				switch (role) {
 					case EXPRESSION: {
 						csArgument.setRole(NavigationRole.EXPRESSION);
 						if ("|".equals(csArgument.getPrefix())) {
 							role = NavigationRole.ACCUMULATOR;
 						}
 						break;
 					}
 					case ACCUMULATOR: {
 						if (csArgument.getInit() != null) {
 							setParameterRole(csArgument, NavigationRole.ACCUMULATOR);
 							if (";".equals(csArgument.getPrefix())) {
 								role = NavigationRole.ITERATOR;
 							}
 						}
 						else {
 							role = NavigationRole.ITERATOR;
 							setParameterRole(csArgument, NavigationRole.ITERATOR);
 						}
 						break;
 					}
 					case ITERATOR: {
 						setParameterRole(csArgument, NavigationRole.ITERATOR);
 						break;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitOperatorCS(OperatorCS csOperator) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitPrefixExpCS(PrefixExpCS csPrefixExp) {
 		if (!(csPrefixExp.eContainer() instanceof InfixExpCS)) {
 			initializePrefixOperators(csPrefixExp, null);
 		}
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitSpecificationCS(SpecificationCS csSpecification) {
 		OpaqueExpression pivotSpecification = PivotUtil.getPivot(OpaqueExpression.class, csSpecification);
 		String exprString = csSpecification.getExprString();
 		pivotSpecification.getBody().add(exprString);
 		pivotSpecification.getLanguage().add(PivotConstants.OCL_LANGUAGE);
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitTypeNameExpCS(TypeNameExpCS object) {
 		return null;
 	}
 
 	@Override
 	public Continuation<?> visitVariableCS(VariableCS csVariable) {
 		return null;
 	}	
 }
