 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.ui.preferences;
 
 import org.eclipse.dltk.javascript.core.JavaScriptCorePreferences;
 import org.eclipse.dltk.ui.preferences.AbstractConfigurationBlock;
 import org.eclipse.dltk.ui.preferences.OverlayPreferenceStore;
import org.eclipse.dltk.ui.preferences.PreferencesMessages;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 public class JavaScriptErrorWarningConfigurationBlock extends
 		AbstractConfigurationBlock {
 
 	public JavaScriptErrorWarningConfigurationBlock(
 			OverlayPreferenceStore store, PreferencePage mainPreferencePage) {
 		super(store, mainPreferencePage);
 	}
 
 	public JavaScriptErrorWarningConfigurationBlock(
 			OverlayPreferenceStore overlayPreferenceStore) {
 		super(overlayPreferenceStore);
 		getPreferenceStore()
 				.addKeys(
 						new OverlayPreferenceStore.OverlayKey[] { new OverlayPreferenceStore.OverlayKey(
 								OverlayPreferenceStore.BOOLEAN,
 								JavaScriptCorePreferences.USE_STRICT_MODE) });
 	}
 
 	public Control createControl(Composite parent) {
 		final GridLayout layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.numColumns = 2;
 
 		final Composite markersComposite = new Composite(parent, SWT.NULL);
 		markersComposite.setLayout(layout);
 		markersComposite.setFont(parent.getFont());
 
 		addCheckBox(markersComposite,
				PreferencesMessages.ErrorWarning_strictMode,
 				JavaScriptCorePreferences.USE_STRICT_MODE, 0);
 		// bindControl(enableCheckbox, STRICT_MODE, null);
 		return markersComposite;
 	}
 
 }
