 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.core.jsflibraryregistry;
 
 /**
  * Exception class thrown by PluginProvidedJSFLibraryCreationHelper.
  * 
  * @deprecated
  */
 public class InvalidArchiveFilesCreationException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Creates an instance with the specified message.
 	 * @param msg This instance's message
 	 */
 	public InvalidArchiveFilesCreationException(String msg) {
 		super(msg);
 	}
 }
