 package edu.ualberta.med.scannerconfig.preferences.scanner;
 
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.RadioGroupFieldEditor;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
 import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
 
 public class MultipleDpis extends FieldEditorPreferencePage implements
     IWorkbenchPreferencePage {
 
     public MultipleDpis() {
         super(GRID);
         setPreferenceStore(ScannerConfigPlugin.getDefault()
             .getPreferenceStore());
     }
 
     @Override
     public void createFieldEditors() {
         RadioGroupFieldEditor rgFe;
         String[][] validDpis = new String[][] { { "300", "300" },
             { "400", "400" }, { "600", "600" }, { "720", "720" },
             { "800", "800" } };
 
         for (int i = 0, n = PreferenceConstants.SCANNER_MULTIPLE_DPIS.length; i < n; ++i) {
             rgFe = new RadioGroupFieldEditor(
                PreferenceConstants.SCANNER_MULTIPLE_DPIS[i], "DPI " + (i + 1),
                 5, validDpis, getFieldEditorParent(), true);
             addField(rgFe);
         }
     }
 
     @Override
     public void init(IWorkbench workbench) {
     }
 
 }
