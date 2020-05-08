 /*******************************************************************************
  * Copyright (c) 2005, 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Maik Schreiber - initial API and implementation
  *    Mikael Petterson adaptation.
  *******************************************************************************/
 
 package net.sourceforge.eclipseccase.ui.preferences;
 
 import net.sourceforge.eclipseccase.ClearCasePlugin;
 
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.swt.layout.FillLayout;
 
 import net.sourceforge.eclipseccase.ui.DialogArea;
 
 import org.eclipse.swt.widgets.Group;
 
 import org.eclipse.jface.preference.FieldEditor;
 
 import org.eclipse.jface.preference.StringFieldEditor;
 
 import net.sourceforge.eclipseccase.IClearCasePreferenceConstants;
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 public class UcmPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
 	
 	
 	
 	private BooleanFieldEditor useUcm;
 	private BooleanFieldEditor silentPrevent;
 	private StringFieldEditor preventCheckout;
 	private StringFieldEditor activityPattern;
 	
 	private TextAreaFieldEditor activityId;
 
 	/**
 	 * Creates a new instance.
 	 */
 	public UcmPreferencePage() {
 		setDescription(PreferenceMessages.getString("UcmPreferences.Description")); //$NON-NLS-1$
		// Set the preference store for the preference page.
		setPreferenceStore(new ClearCasePreferenceStore());
 		
 	}
 
 	@Override
 	protected Control createContents(Composite parent) {
 		Composite composite = new Composite(parent, SWT.LEFT);
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 1;
 		composite.setLayout(gridLayout);
 		
 		useUcm = new BooleanFieldEditor(IClearCasePreferenceConstants.USE_UCM, PreferenceMessages.getString("UcmPreferences.UseUcm"), //$NON-NLS-1$
 				composite);
 		addFieldEditor(useUcm);
 		
 		activityPattern = new StringFieldEditor(IClearCasePreferenceConstants.ACTIVITY_PATTERN, PreferenceMessages.getString("UcmPreferences.label.activityPattern"), composite);
 		addFieldEditor(activityPattern);
 		
 	    activityId = new TextAreaFieldEditor(IClearCasePreferenceConstants.ACTIVITY_MSG_FORMAT, PreferenceMessages.getString("UcmPreferences.activityFormatMsg"), composite);
 		addFieldEditor(activityId);
 		
 		return composite;
 	}
 
 	public void init(IWorkbench arg0) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	//Needs to be done for each fieldeditor.
 	private void addFieldEditor(FieldEditor fieldEditor){
 		
 		fieldEditor.setPreferencePage(this);
 		fieldEditor.setPreferenceStore(getPreferenceStore());
 		fieldEditor.load();
 	}
 	
 	protected void performDefaults() {
 		useUcm.loadDefault();
 		activityPattern.loadDefault();
 		activityId.loadDefault();
 		super.performDefaults();
 	}
 
 	public boolean performOk() {
 		useUcm.store();
 		activityPattern.store();
 		activityId.store();
 		return super.performOk();
 	}
 
 }
