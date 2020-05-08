 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.python.internal.ui.editor;
 
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
 import org.eclipse.dltk.internal.ui.editor.ScriptOutlinePage;
 import org.eclipse.dltk.python.core.PythonLanguageToolkit;
 import org.eclipse.dltk.python.internal.ui.PythonUI;
 import org.eclipse.dltk.python.internal.ui.text.folding.PythonFoldingStructureProvider;
 import org.eclipse.dltk.python.ui.text.IPythonPartitions;
 import org.eclipse.dltk.ui.text.ScriptTextTools;
 import org.eclipse.dltk.ui.text.folding.IFoldingStructureProvider;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentExtension3;
 import org.eclipse.ui.IEditorInput;
 
 public class PythonEditor extends ScriptEditor {
 
 	public static final String EDITOR_ID = "org.eclipse.dltk.python.ui.editor.PythonEditor";
 
 	public static final String EDITOR_CONTEXT = "#PythonEditorContext";
 
 	public static final String RULER_CONTEXT = "#PythonRulerContext";
 
 	protected void initializeEditor() {
 		super.initializeEditor();
 		setEditorContextMenuId(EDITOR_CONTEXT);
 		setRulerContextMenuId(RULER_CONTEXT);
 	}
 
	public IPreferenceStore getScriptPreferenceStore() {
 		return PythonUI.getDefault().getPreferenceStore();
 	}
 
 	public ScriptTextTools getTextTools() {
 		return PythonUI.getDefault().getTextTools();
 	}
 
 	protected ScriptOutlinePage doCreateOutlinePage() {
 		return new PythonOutlinePage(this, PythonUI.getDefault()
 				.getPreferenceStore());
 	}
 
 	protected void connectPartitioningToElement(IEditorInput input,
 			IDocument document) {
 		if (document instanceof IDocumentExtension3) {
 			IDocumentExtension3 extension = (IDocumentExtension3) document;
 			if (extension
 					.getDocumentPartitioner(IPythonPartitions.PYTHON_PARTITIONING) == null) {
 				PythonDocumentSetupParticipant participant = new PythonDocumentSetupParticipant();
 				participant.setup(document);
 			}
 		}
 	}
 
 	IFoldingStructureProvider fFoldingProvider = null;
 
 	protected IFoldingStructureProvider getFoldingStructureProvider() {
 		if (fFoldingProvider == null) {
 			fFoldingProvider = new PythonFoldingStructureProvider();
 		}
 		return fFoldingProvider;
 	}
 
 	public String getEditorId() {
 		return EDITOR_ID;
 	}
 
 	public IDLTKLanguageToolkit getLanguageToolkit() {
 		return PythonLanguageToolkit.getDefault();
 	}
 
 	public String getCallHierarchyID() {
 		return null;
 	}
 
 	protected void initializeKeyBindingScopes() {
 		setKeyBindingScopes(new String[] { "org.eclipse.dltk.ui.pythonEditorScope" }); //$NON-NLS-1$
 	}
 }
