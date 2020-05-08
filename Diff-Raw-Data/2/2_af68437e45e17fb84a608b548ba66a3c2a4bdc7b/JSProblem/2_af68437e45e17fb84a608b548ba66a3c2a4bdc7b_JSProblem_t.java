 /*******************************************************************************
  * Copyright (c) 2009 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
package org.eclipse.dltk.javascript.parser;
 
 import org.antlr.runtime.RecognitionException;
 import org.eclipse.dltk.compiler.problem.DefaultProblem;
 import org.eclipse.dltk.compiler.problem.ProblemSeverities;
 
 public class JSProblem extends DefaultProblem {
 
 	private final Throwable cause;
 
 	public JSProblem(Throwable cause) {
 		super(
 				cause.getMessage(),
 				0,
 				null,
 				ProblemSeverities.Error,
 				0,
 				0,
 				cause instanceof RecognitionException ? ((RecognitionException) cause).line
 						: 0);
 		this.cause = cause;
 	}
 
 	public Throwable getCause() {
 		return cause;
 	}
 
 }
