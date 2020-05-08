 package org.eclipse.rmf.reqif10.pror.editor.preferences;
 
 import java.util.Map;
 
 import org.eclipse.jface.preference.ComboFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrPresentationConfiguration;
 import org.eclipse.rmf.reqif10.pror.edit.presentation.service.PresentationInterface;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.Reqif10EditorPlugin;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.service.PresentationServiceManager;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 public class ProrDefaultPresentationPage extends FieldEditorPreferencePage
 		implements IWorkbenchPreferencePage {
 
 	@Override
 	protected void createFieldEditors() {
 		setField(PreferenceConstants.P_DEFAULT_PRESENTATION_STRING, "String");
 		setField(PreferenceConstants.P_DEFAULT_PRESENTATION_XHTML, "XHTML");
 		setField(PreferenceConstants.P_DEFAULT_PRESENTATION_REAL, "Real");
 		setField(PreferenceConstants.P_DEFAULT_PRESENTATION_INTEGER, "Integer");
 		setField(PreferenceConstants.P_DEFAULT_PRESENTATION_ENUMERATION,
 				"Enumeration");
 		setField(PreferenceConstants.P_DEFAULT_PRESENTATION_BOOLEAN, "Boolean");
 		setField(PreferenceConstants.P_DEFAULT_PRESENTATION_DATE, "Date");
 	}
 
 	private void setField(String fieldName, String label) {
 		addField(new ComboFieldEditor(fieldName, label,
 				getEntryNamesAndValues(), getFieldEditorParent()));
 	}
 
 	private String[][] getEntryNamesAndValues() {
 		Map<Class<? extends ProrPresentationConfiguration>, PresentationInterface> presentationMap = PresentationServiceManager
 				.getPresentationInterfaceMap();
		String[][] result = new String[presentationMap.size() + 2][2];
 		result[0][0] = "None";
 		result[0][1] = "";
		result[1][0] = "Use Build-in";
		result[1][1] = "";
		int i = 2;
 		for (@SuppressWarnings("rawtypes")
 		Class key : presentationMap.keySet()) {
 			result[i][0] = key.getSimpleName();
 			result[i][1] = key.getCanonicalName();
 			i++;
 		}
 		return result;
 	}
 
 	@Override
 	public boolean performOk() {
 		PresentationServiceManager.clearDefaultRenderers();
 		return super.performOk();
 	}
 
 	public void init(IWorkbench workbench) {
 		// Set preference store
 		setPreferenceStore(Reqif10EditorPlugin.getPlugin().getPreferenceStore());
 		setDescription("IMPORTANT: currently, no test with respect to type mismatch is performed.");
 	}
 
 }
