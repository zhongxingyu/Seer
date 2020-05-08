 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.compare.match;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.compare.match.engine.IMatchScope;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerPackage;
 
 /**
  * Provide a scope for any ReStructurde Models element, providing objects and features to ignore during the
  * match process (like resolved links for example).
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentScope implements IMatchScope {
 
 	/** the list of objects to be included in the scope. */
 	private final List<EObject> eObjectsInScope = new ArrayList<EObject>();
 
 	/**
 	 * The list of elements to ignore.
 	 */
 	private ArrayList<EClass> elementToIgnoreList;
 
 	/**
 	 * IntentScope constructor.
 	 * 
 	 * @param element
 	 *            the Intent element on which create this scope.
 	 */
 	public IntentScope(EObject element) {
 		initializeIgnoredFeaturesList();
 		eObjectsInScope.add(element);
 		addContainedElementsInScope(element);
 	}
 
 	/**
 	 * Adds all element contained by the given element in the list of objects in scope.
 	 * 
 	 * @param element
 	 *            the element to consider
 	 */
 	private void addContainedElementsInScope(EObject element) {
 		final Iterator<EObject> iterator = element.eAllContents();
 		while (iterator.hasNext()) {
 			final EObject next = iterator.next();
 
 			if (!isIgnoredObject(next)) {
 				eObjectsInScope.add(next);
 			}
 		}
 	}
 
 	/**
 	 * Initialize the list of features to ignore.
 	 */
 	private void initializeIgnoredFeaturesList() {
 		elementToIgnoreList = new ArrayList<EClass>();
		// elementToIgnoreList.add(ModelingUnitPackage.eINSTANCE.getTypeReference());
 		elementToIgnoreList.add(CompilerPackage.eINSTANCE.getCompilationStatus());
		elementToIgnoreList.add(CompilerPackage.eINSTANCE.getSynchronizerCompilationStatus());
 	}
 
 	/**
 	 * Indicates if the given object must be ignored during the match process (for example any resolved link
 	 * should be ignored).
 	 * 
 	 * @param eObject
 	 *            the object to consider
 	 * @return true if the given object must be ignored during the match process (for example any resolved
 	 *         link should be ignored), false otherwise.
 	 */
 	private boolean isIgnoredObject(EObject eObject) {
 		return elementToIgnoreList.contains(eObject.eClass());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.engine.IMatchScope#isInScope(org.eclipse.emf.ecore.EObject)
 	 */
 	public boolean isInScope(EObject eObject) {
 		return this.eObjectsInScope.contains(eObject);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.engine.IMatchScope#isInScope(org.eclipse.emf.ecore.resource.Resource)
 	 */
 	public boolean isInScope(Resource resource) {
 		return false;
 	}
 
 }
