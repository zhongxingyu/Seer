 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.core.model;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public interface IScriptThreadStreamProxy {
 	InputStream getStdin();
 
 	OutputStream getStdout();
 
 	OutputStream getStderr();

	void close();
 }
