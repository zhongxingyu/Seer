 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.python.ui.text;
 
 import org.eclipse.dltk.python.core.PythonConstants;
 import org.eclipse.jface.text.IDocument;
 
 public interface IPythonPartitions {
 
 	public final static String PYTHON_PARTITIONING = PythonConstants.PYTHON_PARTITIONING;
 
	public final static String PYTHON_COMMENT = "__python_comment";
	public final static String PYTHON_STRING = "__python_string";
 
	public final static String[] PYTHON_PARITION_TYPES = new String[] {
			IPythonPartitions.PYTHON_STRING, IPythonPartitions.PYTHON_COMMENT, IDocument.DEFAULT_CONTENT_TYPE
	};
 }
