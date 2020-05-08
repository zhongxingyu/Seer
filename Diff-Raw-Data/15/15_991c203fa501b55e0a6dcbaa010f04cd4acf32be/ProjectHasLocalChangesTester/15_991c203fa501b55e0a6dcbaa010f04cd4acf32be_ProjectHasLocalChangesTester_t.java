 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.ui.commands;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommandWithResult;
 
 /**
  * Property tester to test if a project space has local changes.
  * 
  * @author koegel
  */
 public class ProjectHasLocalChangesTester extends PropertyTester {
 
 	/**
 	 * {@inheritDoc}
 	 * 
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object,
	 *      java.lang.String, java.lang.Object[], java.lang.Object)
 	 */
	public boolean test(Object receiver, String property, Object[] args,
			final Object expectedValue) {
		if (receiver instanceof ProjectSpace
				&& expectedValue instanceof Boolean) {
 			final ProjectSpace projectSpace = (ProjectSpace) receiver;
 
 			EMFStoreCommandWithResult<Boolean> command = new EMFStoreCommandWithResult<Boolean>() {
 				@Override
 				protected Boolean doRun() {
					Boolean hasLocalChanges = new Boolean(!projectSpace
							.getLocalOperations().getOperations().isEmpty());
 					return hasLocalChanges.equals(expectedValue);
 				}
 			};
 
 			return command.run(false);
 		}
 		return false;
 	}
 
 }
