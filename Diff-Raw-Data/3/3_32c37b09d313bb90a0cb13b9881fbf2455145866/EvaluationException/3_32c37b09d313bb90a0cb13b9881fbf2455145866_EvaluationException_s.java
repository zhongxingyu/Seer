 /*******************************************************************************
  * Copyright (c) 2001, 2007 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.core.tagmatcher;
 
 /**
  * Wraps an exception thrown during algorithm evaluation
  * @author cbateman
  *
  */
 public class EvaluationException extends RuntimeException {
 
     /**
      * 
      */
     private static final long serialVersionUID = -3250608181004283586L;
 
     /**
      * 
      */
     public EvaluationException() {
         super();
     }
 
     /**
      * @param message
      */
     public EvaluationException(String message) {
         super(message);
     }
 
     /**
      * @param cause
      */
     public EvaluationException(Throwable cause) {
         super(cause);
     }
 
     /**
      * @param message
      * @param cause
      */
     public EvaluationException(String message, Throwable cause) {
         super(message, cause);
     }
 
 }
