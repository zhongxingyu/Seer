 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui.display;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.dltk.tcl.internal.debug.ui.display.messages"; //$NON-NLS-1$
 	public static String DebugScriptInterpreter_NoDebugger;
 	public static String DebugScriptInterpreter_null;
 	public static String DebugScriptInterpreter_unknownEvaluationError;
 	public static String ScriptDisplayView_consoleName;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
