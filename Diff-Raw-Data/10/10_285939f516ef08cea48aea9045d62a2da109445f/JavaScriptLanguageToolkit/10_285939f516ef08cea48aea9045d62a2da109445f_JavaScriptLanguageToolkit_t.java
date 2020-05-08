 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.core;
 
 import java.text.MessageFormat;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelStatus;
 import org.eclipse.dltk.internal.core.util.Messages;
 
 public class JavaScriptLanguageToolkit implements IDLTKLanguageToolkit {
 	private static JavaScriptLanguageToolkit sInstance = new JavaScriptLanguageToolkit();
 
 	public JavaScriptLanguageToolkit() {
 	}
 
 	public IStatus validateSourceModule(String name) {
 		if (name == null) {
 			return new Status(IStatus.ERROR, JavaScriptPlugin.PLUGIN_ID, -1,
 					Messages.convention_unit_nullName, null);
 		}
 		if (!isJavaScriptLikeFileName(name)) {
 			return new Status(IStatus.ERROR, JavaScriptPlugin.PLUGIN_ID, -1,
 					MessageFormat
 							.format(Messages.convention_unit_notScriptName,
 									new String[] { getJavaScriptExtension(),
 											"Python" }), null);
 		}
 		return IModelStatus.VERIFIED_OK;
 	}
 
 	private String getJavaScriptExtension() {
 		return "js";
 	}
 
 	private boolean isJavaScriptLikeFileName(String name) {
 		// TODO: Add more correct checking here.
 		if (name.endsWith("." + getJavaScriptExtension())) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean languageSupportZIPBuildpath() {
 		return false;
 	}
 
 	public boolean validateSourcePackage(IPath path) {
 		return true;
 	}
 
 	public String getNatureId() {
 		return JavaScriptNature.NATURE_ID;
 	}
 
 	public IStatus validateSourceModule(IResource resource) {
 		return validateSourceModule(resource.getName());
 	}
 
 	public IStatus validateSourceModule(IPath resource) {
 		return validateSourceModule(resource.lastSegment());
 	}
 	
 	public IStatus validateSourceModule(IModelElement parent, String str) {
 		return validateSourceModule(str);
 	}
 
 	public IStatus validateSourceModuleName(String str) {
 		return validateSourceModule(str);
 	}
 
 	public static IDLTKLanguageToolkit getDefault() {
 		return sInstance;
 	}
 	
 	public String getDelimeterReplacerString() {
 		return ".";
 	}
 
 	public String[] getLanguageFileExtensions() {
 		return new String[] { "js" };
 	}
 
 	public String getLanguageName()
 	{
 		return "Javascript";
 	}

	public boolean isScriptLikeFileName(String name) {
		String[] exts = getLanguageFileExtensions();
		for (int i = 0; i < exts.length; ++i) {
			if (name.endsWith("." + exts[i])) {
				return true;
			}
		}
		return false;
	}
 }
