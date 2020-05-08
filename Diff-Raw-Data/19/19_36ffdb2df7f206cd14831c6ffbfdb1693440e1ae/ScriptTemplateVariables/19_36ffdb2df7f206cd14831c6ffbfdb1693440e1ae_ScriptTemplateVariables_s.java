 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
 package org.eclipse.dltk.ui.templates;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.jface.text.templates.TemplateContext;
 import org.eclipse.jface.text.templates.TemplateVariableResolver;
 
 public final class ScriptTemplateVariables {
 	private ScriptTemplateVariables() {
 	}
 
 	public static class File extends TemplateVariableResolver {
 		public static final String NAME = "file"; //$NON-NLS-1$
 
 		public File() {
 			super(NAME, TemplateMessages.Variable_File_Description);
 		}
 
 		protected String resolve(TemplateContext context) {
 			ISourceModule module = getSourceModule(context);
 			return (module == null) ? null : module.getElementName();
 		}
 
 		protected boolean isUnambiguous(TemplateContext context) {
 			return resolve(context) != null;
 		}
 	}
 
 	public static class Language extends TemplateVariableResolver {
 		public static final String NAME = "language"; //$NON-NLS-1$
 
 		public Language() {
 			super(NAME, TemplateMessages.Variable_Language_Description);
 		}
 
 		protected String resolve(TemplateContext context) {
 			String name = null;
 			ISourceModule module = getSourceModule(context);
 			IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 					.getLanguageToolkit(module);
 
 			name = toolkit.getLanguageName();
 
 			return name;
 		}
 
 		protected boolean isUnambiguous(TemplateContext context) {
 			return resolve(context) != null;
 		}
 	}
 
 	public static class Interpreter extends TemplateVariableResolver {
 		public static final String NAME = "interpreter"; //$NON-NLS-1$
 
 		public Interpreter() {
 			super(NAME, TemplateMessages.Variable_Interpreter_Description);
 		}
 
 		protected String resolve(TemplateContext context) {
 			String path = null;
 
 			try {
 				ISourceModule module = getSourceModule(context);
 				IInterpreterInstall install = ScriptRuntime
 						.getInterpreterInstall(module.getScriptProject());
 				
				path = install.getRawInstallLocation().getAbsolutePath();				
 			} catch (CoreException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 
 			return path;
 		}
 		
 		protected boolean isUnambiguous(TemplateContext context) {
 			return resolve(context) != null;
 		}
 	}
 
 	private static ISourceModule getSourceModule(TemplateContext context) {
 		return ((ScriptTemplateContext) context).getSourceModule();
 	}
 
 }
