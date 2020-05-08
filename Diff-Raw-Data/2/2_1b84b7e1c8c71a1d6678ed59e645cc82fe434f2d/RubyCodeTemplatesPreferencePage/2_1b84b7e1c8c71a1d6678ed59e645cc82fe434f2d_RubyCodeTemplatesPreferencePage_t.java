 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  ******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.templates;
 
 import org.eclipse.dltk.ruby.internal.ui.RubyUI;
 import org.eclipse.dltk.ruby.internal.ui.preferences.SimpleRubySourceViewerConfiguration;
 import org.eclipse.dltk.ruby.internal.ui.text.IRubyPartitions;
 import org.eclipse.dltk.ruby.internal.ui.text.RubyTextTools;
 import org.eclipse.dltk.ui.templates.ScriptTemplateAccess;
 import org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage;
 import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
 
 import org.eclipse.jface.text.IDocument;
 
 /**
  * Ruby code templates preference page
  */
 public class RubyCodeTemplatesPreferencePage extends
 		ScriptTemplatePreferencePage {
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#createSourceViewerConfiguration()
 	 */
 	protected ScriptSourceViewerConfiguration createSourceViewerConfiguration() {
 		return new SimpleRubySourceViewerConfiguration(getTextTools()
 				.getColorManager(), getPreferenceStore(), null,
 				IRubyPartitions.RUBY_PARTITIONING, false);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#getTemplateAccess()
 	 */
 	protected ScriptTemplateAccess getTemplateAccess() {
 		return RubyTemplateAccess.getInstance();
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#setDocumentParticioner(org.eclipse.jface.text.IDocument)
 	 */
	protected void setDocumentPartitioner(IDocument document) {
 		getTextTools().setupDocumentPartitioner(document,
 				IRubyPartitions.RUBY_PARTITIONING);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#setPreferenceStore()
 	 */
 	protected void setPreferenceStore() {
 		setPreferenceStore(RubyUI.getDefault().getPreferenceStore());
 	}
 
 	private RubyTextTools getTextTools() {
 		return RubyUI.getDefault().getTextTools();
 	}
 }
