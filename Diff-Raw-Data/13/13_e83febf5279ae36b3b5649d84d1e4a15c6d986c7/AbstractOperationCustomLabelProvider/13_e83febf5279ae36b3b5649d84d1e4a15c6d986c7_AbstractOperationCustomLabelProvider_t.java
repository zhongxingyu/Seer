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
 package org.eclipse.emf.emfstore.internal.server.model.provider;
 
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.common.IDisposable;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AbstractOperation;
 
 /**
  * Interface for a custom LabelProvider for {@link AbstractOperation}s.
  * 
  * @author Michael Kagel
  * @author emueller
  */
 public abstract class AbstractOperationCustomLabelProvider implements IDisposable {
 
 	/**
 	 * Constant for render priority meaning this provider does not want to
 	 * render the given element.
 	 */
 	protected static final int CANNOT_RENDER = 0;
 
 	/**
 	 * Default constant for a renderer that is able to render the given element.
 	 */
 	protected static final int CAN_RENDER_DEFAULT = 1;
 
 	/**
 	 * Returns the name of the given model element.
 	 * 
 	 * @param modelElement
 	 *            the model element whose name should be determined
 	 * @return the name of the given model element
 	 */
 	public abstract String getModelElementName(EObject modelElement);
 
 	/**
 	 * Returns the description of an operation.
 	 * 
 	 * @param operation
 	 *            the operation which needs to be visualized by the image
 	 * @return the description of the operation
 	 */
 	public abstract String getDescription(AbstractOperation operation);
 
 	/**
 	 * Returns the image of an operation.
 	 * 
 	 * @param operation
 	 *            the operation which needs to be visualized by the image
 	 * @return the image of the operation
 	 */
 	public abstract Object getImage(AbstractOperation operation);
 
 	/**
 	 * Checks if this provider can render the given operation.
 	 * 
 	 * @param operation
 	 *            the operation which should be checked
 	 * @return a priority for rendering this element. The higher the priority
 	 *         the more likely this provider will render the element. Returning
 	 *         {@link AbstractOperationCustomLabelProvider#CANNOT_RENDER} means
 	 *         this provider does not want to render this element.
 	 */
 	public abstract int canRender(AbstractOperation operation);
 
 }
