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
  * $Id: NavigationOperatorCSAttribution.java,v 1.13 2011/05/05 17:52:54 ewillink Exp $
  */
 package org.eclipse.ocl.examples.xtext.essentialocl.attributes;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.ocl.examples.pivot.CollectionType;
 import org.eclipse.ocl.examples.pivot.IterateExp;
 import org.eclipse.ocl.examples.pivot.LoopExp;
 import org.eclipse.ocl.examples.pivot.OCLExpression;
 import org.eclipse.ocl.examples.pivot.PivotConstants;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.Variable;
 import org.eclipse.ocl.examples.pivot.scoping.AbstractAttribution;
 import org.eclipse.ocl.examples.pivot.scoping.EnvironmentView;
 import org.eclipse.ocl.examples.pivot.scoping.ScopeView;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.InvocationExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NavigatingArgCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NavigationOperatorCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NavigationRole;
 
 public class NavigatingArgCSAttribution extends AbstractAttribution
 {
 	public static final @NonNull NavigatingArgCSAttribution INSTANCE = new NavigatingArgCSAttribution();
 
 	@Override
 	public ScopeView computeLookup(@NonNull EObject target, @NonNull EnvironmentView environmentView, @NonNull ScopeView scopeView) {
 		NavigatingArgCS fromArgument = (NavigatingArgCS)target;
 		NavigationRole role = fromArgument.getRole();
 		InvocationExpCS targetElement = (InvocationExpCS) fromArgument.getLogicalParent();
 		assert targetElement != null;
 		NavigationOperatorCS csNavigationOperator = NavigationUtil.getNavigationOperator(targetElement);
 		OCLExpression pivot = PivotUtil.getPivot(OCLExpression.class, targetElement);	// NB QVTr's RelationCallExp is not a CallExp
 		if (pivot instanceof LoopExp) {				// FIXME This is null for nested iteration
 			if (role == NavigationRole.EXPRESSION) {
 				for (Variable iterator : ((LoopExp)pivot).getIterator()) {
 					if (iterator.isImplicit()) {
 						environmentView.addElementsOfScope(iterator.getType(), scopeView);
 					}
 					else {
 						environmentView.addNamedElement(iterator);
 					}
 					if (environmentView.hasFinalResult()) {
 						return null;
 					}
 				}
 				if (pivot instanceof IterateExp) {
 					Variable result = ((IterateExp)pivot).getResult();
 					if (result.isImplicit()) {
 						environmentView.addElementsOfScope(result.getType(), scopeView);
 					}
 					else {
 						environmentView.addNamedElement(result);
 					}
 					if (environmentView.hasFinalResult()) {
 						return null;
 					}
 				}
 			}
 			else if (role == NavigationRole.ITERATOR) {			// Happens during save
 				List<Variable> iterators = ((LoopExp)pivot).getIterator();
 				assert iterators != null;
 				environmentView.addNamedElements(iterators);
 			}
 			else if (role == NavigationRole.ACCUMULATOR) {
 				Variable result = ((IterateExp)pivot).getResult();
 				if (result != null) {
 					environmentView.addNamedElement(result);
 				}
 			}
 		}
 		else if (pivot != null) {								// OperationCallExp
 		}
 		else {													// No pivot resolved yet
 			if (role == NavigationRole.EXPRESSION) {
 				if ((csNavigationOperator != null)  && csNavigationOperator.getName().equals(PivotConstants.COLLECTION_NAVIGATION_OPERATOR)) {
 					ExpCS csSource = csNavigationOperator.getSource();
 					OCLExpression source = PivotUtil.getPivot(OCLExpression.class, csSource);
 					if (source != null) {
 						Type type = source.getType();
 						if (type instanceof CollectionType) {		// collection->collection-operation(name...
 							CollectionType collectionType = (CollectionType)type;
 							ExpCS csArgument = csNavigationOperator.getArgument();
 							assert csArgument == targetElement;
							if (NavigationUtil.isIteration(environmentView.getMetaModelManager(), targetElement, collectionType)) {
 								environmentView.addElementsOfScope(collectionType.getElementType(), scopeView);
 							}
 						}
 					}
 				}
 			}
 		}
 		if (csNavigationOperator != null) {
 			return scopeView.getParent().getParent().getParent();	// Leapfrog over InvocationExpCS and its source operator
 		}
 		else {
 			return scopeView.getParent().getParent();				// Leapfrog over InvocationExpCS
 		}
 	}
 }
