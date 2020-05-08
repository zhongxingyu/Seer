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
 package org.eclipse.emf.emfstore.client.model;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.client.model.exceptions.InvalidHandleException;
 import org.eclipse.emf.emfstore.client.model.impl.OperationRecorder;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.CompositeOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.semantic.SemanticCompositeOperation;
 
 /**
  * A handle to control a composite operation during recording.
  * 
  * @author koegel
  */
 public class CompositeOperationHandle {
 
 	private boolean isValid;
 	private CompositeOperation compositeOperation;
 	private OperationRecorder operationRecorder;
 	private Set<EObject> removedElements;
 
 	/**
 	 * Default constructor.
 	 * 
	 * @param operationManager
 	 *            the change tracker this composite is tracked on
 	 * @param compositeOperation
 	 *            the composite operation to be handled
 	 */
 	public CompositeOperationHandle(OperationRecorder operationRecorder, CompositeOperation compositeOperation) {
 		this.operationRecorder = operationRecorder;
 		removedElements = new HashSet<EObject>();
 		removedElements.addAll(operationRecorder.getRemovedElements());
 		this.compositeOperation = compositeOperation;
 		isValid = true;
 	}
 
 	/**
 	 * Returns whether the handle is still valid.
 	 * 
 	 * @return false if the composite operation is already completed or aborted.
 	 */
 	public boolean isValid() {
 		return isValid;
 	}
 
 	/**
 	 * Aborts a composite operation. The state before starting the composite
 	 * operation will be recovered.
 	 * 
 	 * @throws InvalidHandleException
 	 *             if the handle is invalid
 	 */
 	public void abort() throws InvalidHandleException {
 		checkAndCloseHandle();
 		operationRecorder.getRemovedElements().retainAll(removedElements);
 		operationRecorder.abortCompositeOperation();
 		dropAllReferences();
 	}
 
 	private void dropAllReferences() {
 		compositeOperation = null;
 		operationRecorder = null;
 		removedElements = null;
 
 	}
 
 	private void checkAndCloseHandle() throws InvalidHandleException {
 		if (!isValid) {
 			throw new InvalidHandleException();
 		}
 		isValid = false;
 	}
 
 	/**
 	 * Completes a composite operation.
 	 * 
 	 * @param name
 	 *            the name for the operation
 	 * @param description
 	 *            the description of the operation
 	 * @param modelElementId
 	 *            the id of the model element that is most important for the
 	 *            operation
 	 * @throws InvalidHandleException
 	 *             if the handle is invalid
 	 */
 	public void end(String name, String description, ModelElementId modelElementId) throws InvalidHandleException {
 		checkAndCloseHandle();
 		compositeOperation.setCompositeName(name);
 		compositeOperation.setCompositeDescription(description);
 		compositeOperation.setClientDate(new Date());
 		compositeOperation.setReversed(false);
 		compositeOperation.setModelElementId(modelElementId);
 		operationRecorder.endCompositeOperation();
 		dropAllReferences();
 	}
 
 	/**
 	 * Completes a the given semantic composite operation.
 	 * 
 	 * @param semanticCompositeOperation
 	 *            a semanticCompositeOperation that was executed and represents
 	 *            the composite
 	 * @throws InvalidHandleException
 	 *             if the handle is invalid
 	 */
 	public void end(SemanticCompositeOperation semanticCompositeOperation) throws InvalidHandleException {
 		checkAndCloseHandle();
 		semanticCompositeOperation.setClientDate(new Date());
 		semanticCompositeOperation.setReversed(false);
 		semanticCompositeOperation.getSubOperations().addAll(compositeOperation.getSubOperations());
 		operationRecorder.endCompositeOperation(semanticCompositeOperation);
 	}
 }
