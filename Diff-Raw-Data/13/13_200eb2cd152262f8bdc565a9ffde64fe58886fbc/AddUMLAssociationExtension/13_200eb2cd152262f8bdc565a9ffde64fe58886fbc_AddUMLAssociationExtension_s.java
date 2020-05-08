 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.examples.diff.extension.extensions;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.eclipse.emf.compare.diff.metamodel.AddModelElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.examples.diff.extension.DiffExtensionPlugin;
 import org.eclipse.emf.compare.examples.diff.extension.metamodel.diff_extension.impl.AddUMLAssociationImpl;
 import org.eclipse.emf.compare.util.EFactory;
import org.eclipse.emf.compare.util.FactoryException;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * This Diff extension detect the add of UML navigable Association. For one UML navigable extension 2 changes
  * are created by the generic diff engine : ADD association, and ADD property. Using this extension these
  * changes are hiddent by an unique change.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class AddUMLAssociationExtension extends AddUMLAssociationImpl {
 	/** TODOCBR comment. */
 	private boolean isNavigable;
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.compare.diff.metamodel.impl.AbstractDiffExtensionImpl#visit(org.eclipse.emf.compare.diff.metamodel.DiffModel)
 	 */
 	@Override
 	public void visit(DiffModel diff) {
 		/*
 		 * Let's iterate over the DiffModel and find new "Association" objects.
 		 */
 		final Iterator<EObject> it = diff.eAllContents();
 		while (it.hasNext()) {
 			final DiffElement element = (DiffElement)it.next();
 			if (element instanceof AddModelElement
 					&& isAssociation(((AddModelElement)element).getRightElement())) {
 				final EObject assoc = ((AddModelElement)element).getRightElement();
 				/*
 				 * We have an association, let's add our new higher level delta and hide the others...
 				 */
 
 				/*
 				 * Get memberEnds and check whether they are contained in the association or not. If not then
 				 * it's some kind of "special" association, meaning "Navigable" for instance.
 				 */
 				try {
 					final Collection<EObject> members = EFactory.eGetAsList(assoc, "memberEnd");
 					for (EObject member : members) {
 						/*
 						 * It's a navigable association then we should hide the property and the association
 						 * and show ourselves instead.
 						 */
 						if (member.eContainer() != assoc) {
 							isNavigable = true;
 							/*
 							 * We have to find the corresponding diff element (if it exists in order to hide
 							 * it)
 							 */
 							final Iterator diffIt = element.eContainer().eAllContents();
 							while (diffIt.hasNext()) {
 								final EObject childElem = (EObject)diffIt.next();
 								if (childElem instanceof AddModelElement && ((AddModelElement)childElem).getRightElement() == member)
 									getHideElements().add(childElem);
 									getProperties().add(childElem);
 							}
 						}
 						if (isNavigable) {
 							getHideElements().add(element);
 							copyAssociationData((AddModelElement)element);
 							if (element.eContainer() instanceof DiffGroup) {
 								final DiffGroup group = (DiffGroup)element.eContainer();
 								group.getSubDiffElements().add(this);
 							}
 						}
 
 					}
 				} catch (FactoryException e) {
 					// nothing to do, probably not some kind of UML I know...
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * TODOCBR comment.
 	 * @param element comment.
 	 */
 	private void copyAssociationData(AddModelElement element) {
 		setLeftParent(element.getLeftParent());
 		setRightElement(element.getRightElement());
 	}
 
 	/**
 	 * TODOCBR comment.
 	 * @param rightElement comment.
 	 * @return comment.
 	 */
 	// TODOCBR name shadowing
 	private boolean isAssociation(EObject rightElement) {
 		return rightElement.eClass().getName().equals("Association"); //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.compare.diff.metamodel.impl.AbstractDiffExtensionImpl#getImage()
 	 */
 	@Override
 	public Object getImage() {
 		final Object result = DiffExtensionPlugin.INSTANCE.getBundleImage("icons/obj16/addAssociation.gif"); //$NON-NLS-1$
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.compare.diff.metamodel.impl.AbstractDiffExtensionImpl#getText()
 	 */
 	@Override
 	public String getText() {
 		String result = ""; //$NON-NLS-1$
 		if (isNavigable) {
 			// TODOCBR use Messages class in root package to externalize this
 			result += "Navigable UML Association has been added";
 		} else {
 			result += "UML Association has been added";
 		}
 		return result;
 	}
 }
