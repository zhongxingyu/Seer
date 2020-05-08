 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.preferences;
 
 
 import org.eclipse.dltk.ruby.internal.ui.RubyUI;
 import org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPreferencePage;
 import org.eclipse.dltk.ui.preferences.EditorConfigurationBlock;
 import org.eclipse.dltk.ui.preferences.IPreferenceConfigurationBlock;
 import org.eclipse.dltk.ui.preferences.OverlayPreferenceStore;
 import org.eclipse.dltk.ui.preferences.PreferencesMessages;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 
 public class RubyEditorPreferencesPage extends
 		AbstractConfigurationBlockPreferencePage {
 
 	/*
 	 * @see org.eclipse.ui.internal.editors.text.AbstractConfigureationBlockPreferencePage#getHelpId()
 	 */
 	protected String getHelpId() {
 		return "";
 	}
 
 	/*
 	 * @see org.eclipse.ui.internal.editors.text.AbstractConfigurationBlockPreferencePage#setDescription()
 	 */
 	protected void setDescription() {
 		String description = PreferencesMessages.PythonEditorPreferencePage_general;
 		setDescription(description);
 	}
 
 	protected Label createDescriptionLabel(Composite parent) {
 		return null;
 	}
 
 	/*
 	 * @see org.org.eclipse.ui.internal.editors.text.AbstractConfigurationBlockPreferencePage#setPreferenceStore()
 	 */
 	protected void setPreferenceStore() {
 		setPreferenceStore(RubyUI.getDefault().getPreferenceStore());
 	}
 
 	/*
 	 * @see org.eclipse.ui.internal.editors.text.AbstractConfigureationBlockPreferencePage#createConfigurationBlock(org.eclipse.ui.internal.editors.text.OverlayPreferenceStore)
 	 */
 	protected IPreferenceConfigurationBlock createConfigurationBlock(
 			OverlayPreferenceStore overlayPreferenceStore) {
		return new EditorConfigurationBlock(this, overlayPreferenceStore, true);
 	}
 }
