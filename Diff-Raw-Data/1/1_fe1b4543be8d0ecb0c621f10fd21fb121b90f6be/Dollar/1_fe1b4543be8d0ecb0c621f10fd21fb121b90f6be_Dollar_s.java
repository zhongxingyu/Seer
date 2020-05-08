 /*******************************************************************************
  * Copyright (c) 2012 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.apps.admin.web.stubs.objects;
 
 import sun.org.mozilla.javascript.internal.Context;
 import sun.org.mozilla.javascript.internal.Function;
 import sun.org.mozilla.javascript.internal.Scriptable;
 import sun.org.mozilla.javascript.internal.ScriptableObject;
 
 /**
  * 
  */
 public class Dollar {
 	
 	private static Context CONTEXT; 
 	
 	private static ScriptableObject SCOPE;
 	
 	private static String dollarLookup = "";
 	
 	private static Function ajax_success;
 
 	private static String ajax_url;
 
 	private static Scriptable each_array;
 
 	private static Function each_opteration;
 
     private Dollar(Context context, ScriptableObject scope) {
     }
     
     public static void init(Context context, ScriptableObject scope) {
     	CONTEXT = context;
     	SCOPE = scope;
     }
 
     // JavaScript Functions
 

 	public static Object dollar(ScriptableObject name){
 		Dollar.dollarLookup = (String) Context.jsToJava(name, String.class);
 		Function elementConstructor = (Function) SCOPE.get("Element", SCOPE);
 		Object[] args = new Object[]{name};
 		Scriptable constructedElement = elementConstructor.construct(CONTEXT, SCOPE, args);
 		return constructedElement;
 	}
 	
     public static void ajax(Scriptable options){
     	Dollar.ajax_url = (String) Context.jsToJava(ScriptableObject.getProperty(options, "url"), String.class);
     	Dollar.ajax_success = (Function) Context.jsToJava(ScriptableObject.getProperty(options, "success"), Function.class);
     }
 
 	public static void each(Scriptable array, Function operation){
     	Dollar.each_array = array;
     	Dollar.each_opteration = operation;
 	}
 	
 	// Test Helper Methods
 	
 	public static String getDollarLookup() {
 		return Dollar.dollarLookup;
 	}
 
 	public static Function getAjaxSuccess() {
 		return Dollar.ajax_success;
 	}
 
 	public static String getAjaxUrl() {
 		return Dollar.ajax_url;
 	}
 
 	public static Scriptable getEachArray() {
 		return Dollar.each_array;
 	}
 
 	public static Function getEachOperation() {
 		return Dollar.each_opteration;
 	}
     
 }
