 /*******************************************************************************
  * Copyright (c) 2011, 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements a property testers that looks to see if the element can be 
  * transitioned to its previous state
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands.testers;
 
 import java.util.List;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyExtended;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewExtended;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class PreviousStatePropertyTester extends PropertyTester {
 
 	/**
 	 * Executes the property test determined by the parameter <code>property</code>.
 	 * 
 	 * @param aReceiver
 	 *            the receiver of the property test
 	 * @param aProperty
 	 *            the property to test
 	 * @param aArgs
 	 *            additional arguments to evaluate the property. If no arguments are specified in the <code>test</code>
 	 *            expression an array of length 0 is passed
 	 * @param aExpectedValue
 	 *            the expected value of the property. The value is either of type <code>java.lang.String</code> or a
 	 *            boxed base type. If no value was specified in the <code>test</code> expressions then <code>null</code>
 	 *            is passed
 	 * @return returns <code>true</code> if the property is equal to the expected value; otherwise <code>false</code> is
 	 *         returned
 	 * @see org.eclipse.core.expressions.IPropertyTester#test(Object receiver, String property, Object[] args, Object
 	 *      expectedValue)
 	 */
 	//NOTE:  This method is overriden, but we do not use the parameters
 	public boolean test(Object aReceiver, String aProperty, Object[] aArgs, Object aExpectedValue) {
 
 		if (null != R4EUIModelController.getActiveReview() && !R4EUIModelController.getActiveReview().isReadOnly()) {
 			final List<IR4EUIModelElement> selectedElements = UIUtils.getCommandUIElements();
 			if (null != selectedElements && selectedElements.size() > 0) {
 				return testElement(selectedElements.get(0));
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method testElement
 	 * 
 	 * @param aElement
 	 *            - IR4EUIModelElement
 	 * @return boolean
 	 */
 	private boolean testElement(IR4EUIModelElement aElement) {
 		if (aElement.isEnabled()) {
 			if (aElement instanceof R4EUIReviewExtended) {
				if (((R4EUIReviewExtended) aElement).isOpen()
						&& null != ((R4EUIReviewExtended) aElement).getPreviousPhase()) {
 					return true;
 				}
 			} else if (aElement instanceof R4EUIReviewBasic) {
 				if (((R4EUIReviewBasic) aElement).isOpen()
 						&& ((R4EReviewState) ((R4EUIReviewBasic) aElement).getReview().getState()).getState().equals(
 								R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 					return true;
 				}
 			} else if (aElement instanceof R4EUIAnomalyExtended) {
 				if (!((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState()
 						.equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)
 						&& null != ((R4EUIAnomalyExtended) aElement).getPreviousState()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
