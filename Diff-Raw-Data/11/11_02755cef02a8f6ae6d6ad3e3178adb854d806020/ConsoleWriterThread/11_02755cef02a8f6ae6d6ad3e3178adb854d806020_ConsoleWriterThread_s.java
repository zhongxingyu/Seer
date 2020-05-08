 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.ui.console.MessageConsoleStream;
 
 /**
  * Thread for writing output to the console.
  */
 public class ConsoleWriterThread extends Thread {
 	BufferedReader in;
 	MessageConsoleStream out;
 	private boolean terminated;
 
 	/**
 	 * Create a new console writer thread.
 	 * 
 	 * @param in
 	 * 		The InputStream to read from.
 	 * @param out
 	 * 		The Eclipse MessageConsoleStream to write output to.
 	 */
 	public ConsoleWriterThread(InputStream in, MessageConsoleStream out) {
 		this.out = out;
 		this.in = new BufferedReader(new InputStreamReader(in));
 		terminated=false;
 	}
 
 	@Override
 	public void run() {
 		try {
 			String line = null;
 			// Use line based IO. Fixes Trac #42 (localized language problem).
 			while (!terminated && (line = in.readLine()) != null) {
 				out.write(line + '\n');
 			}
 		} catch (IOException e) {
 			//Log error, but do nothing about it
 			PackagerPlugin.getDefault().getLog().log(new Status(IStatus.WARNING,
 				      PackagerPlugin.PLUGIN_ID, 0,
 				      Messages.consoleWriterThread_ioFail, e));
 		}
 	}
 
 	/**
 	 * Close/terminate this ConsoleWriterThread.
 	 */
 	public void close() {
 		terminated = true;
 	}
 
 }
