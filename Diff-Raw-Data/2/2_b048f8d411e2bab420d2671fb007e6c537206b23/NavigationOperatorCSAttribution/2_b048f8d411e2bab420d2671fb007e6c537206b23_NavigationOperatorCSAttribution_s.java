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
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.ocl.examples.pivot.CollectionType;
 import org.eclipse.ocl.examples.pivot.OCLExpression;
 import org.eclipse.ocl.examples.pivot.PivotConstants;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.scoping.AbstractAttribution;
 import org.eclipse.ocl.examples.pivot.scoping.EnvironmentView;
 import org.eclipse.ocl.examples.pivot.scoping.ScopeView;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ElementCS;
 import org.eclipse.ocl.examples.xtext.base.scoping.BaseScopeView;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NavigationOperatorCS;
 
 public class NavigationOperatorCSAttribution extends AbstractAttribution
 {
 	public static final NavigationOperatorCSAttribution INSTANCE = new NavigationOperatorCSAttribution();
 
 	@Override
 	public ScopeView computeLookup(EObject target, EnvironmentView environmentView, ScopeView scopeView) {
 		NavigationOperatorCS targetElement = (NavigationOperatorCS)target;
 		EObject child = scopeView.getChild();
 		if (child == targetElement.getArgument()) {
 			OCLExpression source = PivotUtil.getPivot(OCLExpression.class, targetElement.getSource());
 			if (source != null) {
 				Type type = source.getType();
 				if (!targetElement.getName().equals(PivotConstants.COLLECTION_NAVIGATION_OPERATOR)) {
 					if (type instanceof CollectionType) {		// collection->implicit-collect(object-operation)
 						Type elementType = ((CollectionType)type).getElementType();
 						while (elementType instanceof CollectionType) {
 							elementType = ((CollectionType)elementType).getElementType();		// implicit-collect flattens
 						}
 						environmentView.addElementsOfScope(elementType, scopeView);
 					}
					else {										// object.object-operation
 						environmentView.addElementsOfScope(type, scopeView);		
 						environmentView.addElementsOfScope(type.getPackage(), scopeView);						
 					}
 				}
 				else if (scopeView.getContainmentFeature() != PivotPackage.Literals.OPERATION_CALL_EXP__ARGUMENT){
 					if (type instanceof CollectionType) {		// collection->collection-operation
 						environmentView.addElementsOfScope(type, scopeView);
 					}
 					else {										// object.oclAsSet()->collection-operation
 						MetaModelManager metaModelManager = environmentView.getMetaModelManager();
 						Type setType = metaModelManager.getSetType(type);
 						environmentView.addElementsOfScope(setType, scopeView);
 					}
 				}
 				else {
 					if (type instanceof CollectionType) {		// collection->iteration-operation(iterator-feature)
 						if (InvocationExpCSAttribution.isIteration(environmentView.getMetaModelManager(), targetElement.getArgument(), type)) {
 							environmentView.addElementsOfScope(((CollectionType)type).getElementType(), scopeView);
 						}
 						else {
 							return scopeView.getParent();
 						}
 					}
 					else {										// object.oclAsSet()->iteration-operation(iterator-feature)
 						environmentView.addElementsOfScope(type, scopeView);		// FIXME Only if iteration
 					}
 					
 				}
 			}
 			return scopeView.getParent();
 		}
 		else {
 			ElementCS parent = targetElement.getLogicalParent();
 			BaseScopeView.computeLookups(environmentView, parent, target, PivotPackage.Literals.CALL_EXP__SOURCE, null);
 			return null;
 		}
 	}
 }
