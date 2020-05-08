 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui;
 
 import org.eclipse.dltk.ruby.internal.ui.text.RubyTextTools;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class RubyUI extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.dltk.ruby.ui";
 
 	// The shared instance
 	private static RubyUI plugin;
 
 	private RubyTextTools fRubyTextTools;
 
 	/**
 	 * The constructor
 	 */
 	public RubyUI() {
 		plugin = this;
 	}
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		
		new InitializeAfterLoadJob().schedule();
 		
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static RubyUI getDefault() {
 		return plugin;
 	}
 
 	public RubyTextTools getTextTools() {
 		if (fRubyTextTools == null) {
 			fRubyTextTools = new RubyTextTools(true);
 		}
 
 		return fRubyTextTools;
 	}
 }
