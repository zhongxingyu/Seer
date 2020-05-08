 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.templates;
 
 import org.eclipse.dltk.internal.ui.editor.ScriptSourceViewer;
 import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.jface.text.templates.ContextTypeRegistry;
 import org.eclipse.jface.text.templates.Template;
 import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
 
 public abstract class ScriptTemplatePreferencePage extends
 		TemplatePreferencePage implements IWorkbenchPreferencePage {
 	protected class ScriptEditTemplateDialog extends EditTemplateDialog {
 		public ScriptEditTemplateDialog(Shell parent, Template template,
 				boolean edit, boolean isNameModifiable,
 				ContextTypeRegistry registry) {
 			super(parent, template, edit, isNameModifiable, registry);
 		}
 
 		// protected SourceViewer createViewer(Composite parent) {
 		// return ScriptTemplatePreferencePage.this.createViewer(parent);
 		// }
 
 		/**
 		 * Creates the viewer to be used to display the pattern. Subclasses may
 		 * override.
 		 * 
 		 * @param parent
 		 *            the parent composite of the viewer
 		 * @return a configured <code>SourceViewer</code>
 		 */
 		protected SourceViewer createViewer(Composite parent) {
 			IPreferenceStore store = getPreferenceStore();
 			SourceViewer viewer = new ScriptSourceViewer(parent, null, null,
 					false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, store);
 			SourceViewerConfiguration origConfig = createSourceViewerConfiguration();
 			SourceViewerConfiguration configuration = new CodeTemplateSourceViewerConfigurationAdapter(
 					origConfig, getTemplateProcessor());
 			IDocument document = new Document();
			setDocumentParticioner(document);
 
 			viewer.configure(configuration);
 			if (origConfig instanceof ScriptSourceViewerConfiguration) {
 				final String fontKey = ((ScriptSourceViewerConfiguration) origConfig)
 						.getFontPropertyPreferenceKey();
 				viewer.getTextWidget().setFont(JFaceResources.getFont(fontKey));
 			}
 			viewer.setDocument(document);
 			return viewer;
 		}
 	}
	
	public ScriptTemplatePreferencePage()
	{
 		setPreferenceStore();
		
 		ScriptTemplateAccess tplAccess = getTemplateAccess();
 		setTemplateStore(tplAccess.getTemplateStore());
 		setContextTypeRegistry(tplAccess.getContextTypeRegistry());
 	}
	
	
 	protected Template editTemplate(Template template, boolean edit,
 			boolean isNameModifiable) {
 		EditTemplateDialog dialog = new ScriptEditTemplateDialog(getShell(),
 				template, edit, isNameModifiable, getContextTypeRegistry());
 		if (dialog.open() == Window.OK) {
 			return dialog.getTemplate();
 		}
 		return null;
 	}
 
 	protected SourceViewer createViewer(Composite parent) {
 		IPreferenceStore store = getPreferenceStore();
 		ScriptSourceViewerConfiguration configuration = createSourceViewerConfiguration();
 
 		IDocument document = new Document();
		setDocumentParticioner(document);
 
 		SourceViewer viewer = new ScriptSourceViewer(parent, null, null, false,
 				SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, store);
 
 		viewer.configure(configuration);
 		viewer.setEditable(false);
 		viewer.setDocument(document);
 
 		Control control = viewer.getControl();
 		control.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
 				| GridData.FILL_VERTICAL));
 
 		// Font font = JFaceResources
 		// .getFont(TPreferenceConstants.EDITOR_TEXT_FONT);
 		// viewer.getTextWidget().setFont(font);
 
 		// new ScriptSourcePreviewerUpdater(viewer, configuration, store);
 
 		return viewer;
 	}
 
 	protected void updateViewerInput() {
 		IStructuredSelection selection = (IStructuredSelection) getTableViewer()
 				.getSelection();
 		SourceViewer viewer = getViewer();
 
 		if (selection.size() == 1
 				&& selection.getFirstElement() instanceof TemplatePersistenceData) {
 			TemplatePersistenceData data = (TemplatePersistenceData) selection
 					.getFirstElement();
 			Template template = data.getTemplate();
 			String contextId = template.getContextTypeId();
 
 			IDocument doc = viewer.getDocument();
 
 			String start = null;
 			if ("rdoc".equals(contextId)) { //$NON-NLS-1$
 				start = "/**" + doc.getLegalLineDelimiters()[0]; //$NON-NLS-1$
 			} else
 				start = ""; //$NON-NLS-1$
 
 			doc.set(start + template.getPattern());
 			int startLen = start.length();
 			viewer.setDocument(doc, startLen, doc.getLength() - startLen);
 
 		} else {
 			viewer.getDocument().set(""); //$NON-NLS-1$
 		}
 	}
 
 	protected boolean isShowFormatterSetting() {
 		return false;
 	}
 
 	protected abstract ScriptSourceViewerConfiguration createSourceViewerConfiguration();
 
 	protected abstract ScriptTemplateAccess getTemplateAccess();
	
	protected abstract void setDocumentParticioner(IDocument document);
 
 	protected abstract void setPreferenceStore();
 }
