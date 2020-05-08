 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  ******************************************************************************/
 package org.eclipse.dltk.javascript.internal.ui.templates;
 
 import org.eclipse.dltk.javascript.internal.ui.JavaScriptUI;
 import org.eclipse.dltk.javascript.internal.ui.text.JavascriptTextTools;
 import org.eclipse.dltk.javascript.internal.ui.text.SimpleJavascriptSourceViewerConfiguration;
 import org.eclipse.dltk.javascript.ui.text.IJavaScriptPartitions;
 import org.eclipse.dltk.ui.templates.ScriptTemplateAccess;
 import org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage;
 import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
 
 import org.eclipse.jface.text.IDocument;
 
 /**
  * Javascript templates preference page
  */
 public class JavaScriptTemplatePreferencePage extends
 		ScriptTemplatePreferencePage {
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#createSourceViewerConfiguration()
 	 */
 	protected ScriptSourceViewerConfiguration createSourceViewerConfiguration() {
 		return new SimpleJavascriptSourceViewerConfiguration(getTextTools()
 				.getColorManager(), getPreferenceStore(), null,
 				IJavaScriptPartitions.JS_PARTITIONING, false);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#getTemplateAccess()
 	 */
 	protected ScriptTemplateAccess getTemplateAccess() {
 		return JavaScriptTemplateAccess.getInstance();
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#setDocumentParticioner(org.eclipse.jface.text.IDocument)
 	 */
	protected void setDocumentPartitioner(IDocument document) {
 		getTextTools().setupDocumentPartitioner(document,
 				IJavaScriptPartitions.JS_PARTITIONING);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#setPreferenceStore()
 	 */
 	protected void setPreferenceStore() {
 		setPreferenceStore(JavaScriptUI.getDefault().getPreferenceStore());
 	}
 
 	private JavascriptTextTools getTextTools() {
 		return JavaScriptUI.getDefault().getTextTools();
 	}
 }
