 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.python.internal.ui;
 
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.python.core.PythonConstants;
 import org.eclipse.dltk.python.core.PythonLanguageToolkit;
 import org.eclipse.dltk.python.internal.ui.editor.PythonEditor;
 import org.eclipse.dltk.python.internal.ui.text.SimplePythonSourceViewerConfiguration;
 import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;
 import org.eclipse.dltk.ui.ScriptElementLabels;
 import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
 import org.eclipse.dltk.ui.text.ScriptTextTools;
 import org.eclipse.dltk.ui.viewsupport.ScriptUILabelProvider;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 public class PythonUILanguageToolkit implements IDLTKUILanguageToolkit {
 	private static ScriptElementLabels sInstance = new ScriptElementLabels() {
 	};
 
 	public ScriptElementLabels getScriptElementLabels() {
 		return sInstance;
 	}
 
 	public IPreferenceStore getPreferenceStore() {
 		return PythonUI.getDefault().getPreferenceStore();
 	}
 
 	public IDLTKLanguageToolkit getCoreToolkit() {
 		return PythonLanguageToolkit.getDefault();
 	}
 
 	public IDialogSettings getDialogSettings() {
 		return PythonUI.getDefault().getDialogSettings();
 	}
 
 	public String getPartitioningId() {
 		return PythonConstants.PYTHON_PARTITIONING;
 	}
 
 	public String getEditorId(Object inputElement) {
 		return PythonEditor.EDITOR_ID;
 	}
 
 	public String getInterpreterContainerId() {
 		return "org.eclipse.dltk.python.launching.INTERPRETER_CONTAINER";
 	}
 
	public ScriptUILabelProvider createScripUILabelProvider() {
 		return null;
 	}
 
 	public boolean getProvideMembers(ISourceModule element) {
 		return true;
 	}
 
 	public ScriptTextTools getTextTools() {
 		return PythonUI.getDefault().getTextTools();
 	}
 
 	public ScriptSourceViewerConfiguration createSourceViewerConfiguration() {
 		return new SimplePythonSourceViewerConfiguration(getTextTools()
 				.getColorManager(), getPreferenceStore(), null,
 				getPartitioningId(), false);
 	}
 
 	private static final String INTERPRETERS_PREFERENCE_PAGE_ID = "org.eclipse.dltk.python.preferences.interpreters";
 	private static final String DEBUG_PREFERENCE_PAGE_ID = "org.eclipse.dltk.python.preferences.debug";
 
 	public String getInterpreterPreferencePage() {
 		return INTERPRETERS_PREFERENCE_PAGE_ID;
 	}
 
 	public String getDebugPreferencePage() {
 		return DEBUG_PREFERENCE_PAGE_ID;
 	}
 }
