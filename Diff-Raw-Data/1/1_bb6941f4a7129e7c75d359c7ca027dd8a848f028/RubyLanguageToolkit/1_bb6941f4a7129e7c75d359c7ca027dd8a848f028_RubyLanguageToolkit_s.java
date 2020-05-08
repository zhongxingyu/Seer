 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.core;
 
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.AbstractLanguageToolkit;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelStatus;
 
 public class RubyLanguageToolkit extends AbstractLanguageToolkit {
 	private static final String RB_EXTENSION = "rb";
 	private static final String[] RUBY_EXTENSION_ARRAY = new String[] { RB_EXTENSION };
 	protected static RubyLanguageToolkit sToolkit = new RubyLanguageToolkit();
 	protected static Pattern[] header_patterns = {
 		Pattern.compile("#!\\s*/usr/bin/ruby", Pattern.MULTILINE),
 	};
 	public RubyLanguageToolkit() {
 
 	}
 
 	public String getRubyExtension() {
 		return RB_EXTENSION;
 	}
 
 	public boolean languageSupportZIPBuildpath() {
 		return false;
 	}
 
 	public String getNatureId() {
 		return RubyNature.NATURE_ID;
 	}
 	public IStatus validateSourceModule(IPath path) {
 		IStatus status = validateSourceModuleName(path.lastSegment());
 
 		if (status == IModelStatus.VERIFIED_OK)
 			return status;
 		
 		if ("rakefile".equalsIgnoreCase(path.lastSegment())) {
 			return IModelStatus.VERIFIED_OK;
 		}
 		
 		if (checkPatterns(path.toFile(), header_patterns, null) == IModelStatus.VERIFIED_OK)
 			return IModelStatus.VERIFIED_OK;
 
 		return status;
 	}
 	
 	public IStatus validateSourceModule(IResource resource) {
 		if (resource == null || resource.getLocation() == null)
 			return new Status(IModelStatus.ERROR, RubyPlugin.PLUGIN_ID, 1,
 					"Resource passed to validateSourceModule() is null", null);
 
 		if ("rakefile".equalsIgnoreCase(resource.getLocation().lastSegment())) {
 			return IModelStatus.VERIFIED_OK;
 		}
 		if (checkPatterns(resource.getLocation().toFile(), header_patterns, null) == IModelStatus.VERIFIED_OK)
 			return IModelStatus.VERIFIED_OK;
 
 		return validateSourceModule(resource.getName());
 	}
 
 	public String getDelimeterReplacerString() {
 		return ".";
 	}
 
 	public static IDLTKLanguageToolkit getDefault() {
 		return sToolkit;
 	}
 
 	public String[] getLanguageFileExtensions() {
 		return RUBY_EXTENSION_ARRAY;
 	}
 
 	public String getLanguageName() {
 		return "Ruby";
 	}
 
 	protected String getCorePluginID() {
 		return RubyPlugin.PLUGIN_ID;
 	}
 }
