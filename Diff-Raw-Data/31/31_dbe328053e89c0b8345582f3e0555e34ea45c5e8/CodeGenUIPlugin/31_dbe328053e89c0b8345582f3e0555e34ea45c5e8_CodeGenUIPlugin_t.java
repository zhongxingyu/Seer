 /*
 * Copyright (c) 2005, 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.codegen;
 
 import java.text.MessageFormat;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.util.CodegenEmitters;
 import org.eclipse.gmf.codegen.util.EmitterSource;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 public class CodeGenUIPlugin extends AbstractUIPlugin {
 
 	private static CodeGenUIPlugin plugin;
 
 	private EmitterSource<GenEditorGenerator, CodegenEmitters> emitterSource;
 
 	public CodeGenUIPlugin() {
 		plugin = this;
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		if (emitterSource != null) {
 			emitterSource.dispose();
 			emitterSource = null;
 		}
 		super.stop(context);
 		plugin = null;
 	}
 
 	public static CodeGenUIPlugin getDefault() {
 		return plugin;
 	}
 
 	public static String getBundleString(String key) {
 		return Platform.getResourceBundle(getDefault().getBundle()).getString(key);
 	}
 
 	public static String getBundleString(String key, Object[] args) {
 		String val = getBundleString(key);
 		if (val == null) {
 			return key;
 		}
 		return MessageFormat.format(val, args);
 	}
 
 	public static IStatus createStatus(int statusCode, String message, Exception ex) {
 		return new Status(statusCode, getPluginID(), 0, message, ex);
 	}
 
 	public static IStatus createError(String message, Exception ex) {
 		return createStatus(IStatus.ERROR, message, ex);
 	}
 
 	public static IStatus createWarning(String message) {
 		return createStatus(IStatus.WARNING, message, null);
 	}
 
 	public static IStatus createInfo(String message) {
 		return createStatus(IStatus.INFO, message, null);
 	}
 
 	public static String getPluginID() {
 		return getDefault().getBundle().getSymbolicName();
 	}
 
 	public static boolean needsReconcile() {
 		return !Boolean.FALSE.toString().equals(Platform.getDebugOption(getPluginID() + "/reconcile"));
 	}
 
 	public CodegenEmitters getEmitters(GenEditorGenerator genModel) {
 		if (emitterSource == null) {
 			emitterSource = new EmitterSource<GenEditorGenerator, CodegenEmitters>() {
 				@Override
 				protected CodegenEmitters newEmitters(GenEditorGenerator genModel) {
 					return new CodegenEmitters(!genModel.isDynamicTemplates(), genModel.getTemplateDirectory());
 				}
 			};
 		}
 		return emitterSource.getEmitters(genModel, genModel.isDynamicTemplates());
 	}
 }
