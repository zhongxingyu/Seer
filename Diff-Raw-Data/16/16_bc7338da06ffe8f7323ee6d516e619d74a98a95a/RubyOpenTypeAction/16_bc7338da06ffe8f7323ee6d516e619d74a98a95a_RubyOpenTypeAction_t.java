 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.actions;
 
 import org.eclipse.dltk.ruby.internal.ui.RubyUILanguageToolkit;
 import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;
 import org.eclipse.dltk.ui.actions.OpenTypeAction;
 
 public class RubyOpenTypeAction extends OpenTypeAction {
 	protected IDLTKUILanguageToolkit getUILanguageToolkit() {
 		return RubyUILanguageToolkit.getInstance();
 	}

	protected String getOpenTypeErrorMessage() {
		return "An exception occurred while opening the class/module.";
	}

	protected String getOpenTypeErrorTitle() {
		return "Open Class/Module";
	}

	protected String getOpenTypeDialogMessage() {
		return "&Select a class/module to open (? = any character, * = any String, TZ = TimeZone):";
	}

	protected String getOpenTypeDialogTitle() {
		return "Open Class/Module";
	}
 }
