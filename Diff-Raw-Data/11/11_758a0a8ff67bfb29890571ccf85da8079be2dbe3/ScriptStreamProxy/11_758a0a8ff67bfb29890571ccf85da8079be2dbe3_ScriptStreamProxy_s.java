 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.eclipse.dltk.internal.debug.core.model.IScriptStreamProxy;
 import org.eclipse.ui.console.IOConsole;
 
 public class ScriptStreamProxy implements IScriptStreamProxy {
	private InputStream input; 
 	private OutputStream output;
 	private boolean closed = false;
 
 	public ScriptStreamProxy(IOConsole console) {
 		input = console.getInputStream();
 		output = console.newOutputStream();
 	}
 

 	public OutputStream getStderr() {
 		return output;
 	}
 
 	public OutputStream getStdout() {
 		return output;
 	}
 
 	public InputStream getStdin() {
 		return input;
 	}
 
 	public synchronized void close() {
 		if (!closed) {
 			try {
 				output.close();
 				input.close();
 				closed = true;
 			} catch (IOException e) {
				e.printStackTrace();
 			}
 		}
 	}
 }
