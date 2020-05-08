 /*******************************************************************************
  * Copyright (c) 2012 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Maximilian Koegel
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.model.impl;
 
 import java.util.List;
 
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 
 /**
 * Wraps a call to apply operations on a project space. Ususally used to wrap model changes into Display.syncExec calls
  * to avoid bad editors throwing exceptions if an update is not executed within the UI Thread.
  * 
  * @author koegel
  * 
  */
 public interface IApplyChangesWrapper {
 
 	/**
 	 * Wrap a call to apply changes.
 	 * 
 	 * @param callback the actual code to execute operation application
 	 * @param projectSpace the project space
 	 * @param operations the operations
 	 * @param addOperations true if operations should be added as recorded operations
 	 */
 	void wrapApplyChanges(IApplyChangesCallback callback, ProjectSpace projectSpace,
 		List<AbstractOperation> operations, boolean addOperations);
 }
