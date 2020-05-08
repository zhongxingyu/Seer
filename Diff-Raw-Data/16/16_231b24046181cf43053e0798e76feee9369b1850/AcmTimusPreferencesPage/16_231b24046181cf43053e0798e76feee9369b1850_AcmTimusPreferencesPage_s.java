 package org.abratuhi.acmtimus.preferences;
 
 import org.abratuhi.acmtimus.Activator;
 import org.eclipse.jface.preference.*;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.eclipse.ui.IWorkbench;
 
 /**
  * This class represents a preference page that is contributed to the
  * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
  * can use the field support built into JFace that allows us to create a page
  * that is small and knows how to save, restore and apply itself.
  * <p>
  * This page is used to modify preferences only. They are stored in the
  * preference store that belongs to the main plug-in class. That way,
  * preferences can be accessed directly via the preference store.
  * 
  * @author Alexei Bratuhin
  */
 
 public class AcmTimusPreferencesPage extends FieldEditorPreferencePage
 		implements IWorkbenchPreferencePage {
 
 	public AcmTimusPreferencesPage() {
 		super(GRID);
 		setPreferenceStore(Activator.getDefault().getPreferenceStore());
 		setDescription("Acm Timus - Preferences");
 	}
 
 	/**
 	 * Creates the field editors. Field editors are abstractions of the common
 	 * GUI blocks needed to manipulate various types of preferences. Each field
 	 * editor knows how to save and restore itself.
 	 */
 	public void createFieldEditors() {
 		addField(new StringFieldEditor(PreferenceConstants.PREF_USER, "Username",
 				getFieldEditorParent()));
 		
 		StringFieldEditor sfePassword = new StringFieldEditor(PreferenceConstants.PREF_PASS, "Password",
 				getFieldEditorParent());
 		sfePassword.getTextControl(getFieldEditorParent()).setEchoChar('*');
 		addField(sfePassword);
 		
		addField(new StringFieldEditor(PreferenceConstants.PREF_JUDGEID, "Judge-ID“",
 				getFieldEditorParent()));
 		
 		addField(new StringFieldEditor(PreferenceConstants.PREF_WORKSPACE, "Workspace",
 				getFieldEditorParent()));
 		
 		addField(new StringFieldEditor(PreferenceConstants.PREF_PROJECT, "Project",
 				getFieldEditorParent()));
 		
		addField(new StringFieldEditor(PreferenceConstants.PREF_PACKAGE, "Packages",
				getFieldEditorParent()));
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
 	 */
 	public void init(IWorkbench workbench) {
 	}
 
 }
