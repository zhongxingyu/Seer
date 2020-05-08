 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.core;
 
 import org.eclipse.dltk.compiler.problem.IProblem;
 
 public interface JavaScriptProblems {
 
 	public static final int UNKNOWN_TYPE = IProblem.TypeRelated + 1;
 	public static final int DEPRECATED_TYPE = IProblem.TypeRelated + 2;
 
 	public static final int UNDEFINED_METHOD = IProblem.MethodRelated + 1;
 	public static final int WRONG_PARAMETER_COUNT = IProblem.MethodRelated + 2;
 	public static final int DEPRECATED_METHOD = IProblem.MethodRelated + 3;
 
 	public static final int UNDEFINED_PROPERTY = IProblem.FieldRelated + 1;
	public static final int DEPRECATED_PROPERTY = IProblem.FieldRelated + 2;
 
 }
