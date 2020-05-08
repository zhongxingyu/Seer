 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  ******************************************************************************/
 package org.eclipse.dltk.python.internal.ui.text.folding;
 
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.dltk.python.core.PythonNature;
 import org.eclipse.dltk.python.internal.ui.PythonUI;
 import org.eclipse.dltk.python.ui.text.IPythonPartitions;
 import org.eclipse.dltk.ui.text.folding.AbstractASTFoldingStructureProvider;
 import org.eclipse.jface.text.rules.IPartitionTokenScanner;
 
 /**
  */
 public class PythonFoldingStructureProvider extends
 		AbstractASTFoldingStructureProvider {
 
 	public String getCommentPartition() {
 		return IPythonPartitions.PYTHON_COMMENT;
 	}
 
 	protected ILog getLog() {
 		return PythonUI.getDefault().getLog();
 	}
 
 	protected String getPartition() {
 		return IPythonPartitions.PYTHON_PARTITIONING;
 	}
 
 	protected IPartitionTokenScanner getPartitionScanner() {
 		return PythonUI.getDefault().getTextTools().getPartitionScanner();
 	}
 
 	protected String[] getPartitionTypes() {
		return IPythonPartitions.PYTHON_PARITION_TYPES;
 	}
 
 	protected String getNatureId() {
 		return PythonNature.NATURE_ID;
 	}
 }
