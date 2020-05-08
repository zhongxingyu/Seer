 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.handler;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareEditorInput;
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 
 /**
  * A property tester linked with {@link SaveComparisonModel}. It tests the editable property of both model
  * sides.
  * 
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  * @since 3.0
  */
 public class ModelSaveablePropertyTester extends PropertyTester {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.expressions.PropertyTester#test(java.lang.Object, java.lang.String,
 	 *      java.lang.Object[], java.lang.Object)
 	 */
 	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
 		if (receiver instanceof IEditorPart) {
 			IEditorInput i = ((IEditorPart)receiver).getEditorInput();
 			if (i instanceof CompareEditorInput) {
 				CompareConfiguration configuration = ((CompareEditorInput)i).getCompareConfiguration();
 				if (!configuration.isLeftEditable() || !configuration.isRightEditable()) {
 					return false;
				} else if (((CompareEditorInput)i).isDirty()) {
					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 }
