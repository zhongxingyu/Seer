 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Maximilian Koegel - initial API and implementation
  * Edgar Mueller - API annotations
  ******************************************************************************/
 package org.eclipse.emf.emfstore.server.exceptions;
 
 /**
 * Indicates that the EMFStore XML RPC server failed to initialize.
  * 
  * @author Marco van Meegen
  * 
  * @noextend This class is not intended to be subclassed by clients.
  * @noinstantiate This class is not intended to be instantiated by clients.
 * 
 * @since 1.3
  */
 public class ESServerInitException extends ESException {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param message
 	 *            the message
 	 * @param cause
 	 *            the causing exception
 	 */
 	public ESServerInitException(String message, Throwable cause) {
 		super(message, cause);
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param message
 	 *            the message
 	 */
 	public ESServerInitException(String message) {
 		super(message);
 	}
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param cause
 	 *            the causing exception
 	 */
 	public ESServerInitException(Throwable cause) {
 		super(cause);
 	}
 }
