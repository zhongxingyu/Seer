 package edu.ualberta.med.scannerconfig.preferences;
 
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IntegerFieldEditor;
 import org.eclipse.jface.preference.RadioGroupFieldEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 import edu.ualberta.med.scanlib.ScanLib;
 import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
 
 public class Scanner extends FieldEditorPreferencePage implements
     IWorkbenchPreferencePage {
 
     public Scanner() {
         super(GRID);
         setPreferenceStore(ScannerConfigPlugin.getDefault()
             .getPreferenceStore());
     }
 
     @Override
     public void createFieldEditors() {
         Button b = new Button(getFieldEditorParent(), SWT.NONE);
         b.setText("Select Scanner");
         b.setImage(ScannerConfigPlugin.getDefault().getImageRegistry().get(
             ScannerConfigPlugin.IMG_SCANNER));
         b.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 int scanlibReturn = ScanLib.getInstance()
                     .slSelectSourceAsDefault();
 
                 if (scanlibReturn != ScanLib.SC_SUCCESS) {
                     ScannerConfigPlugin.openError("Source Selection Error",
                         ScanLib.getErrMsg(scanlibReturn));
                 }
 
             }
         });
 
         RadioGroupFieldEditor rgFe = new RadioGroupFieldEditor(
             PreferenceConstants.SCANNER_DRV_TYPE, "Driver Type", 2,
             new String[][] {
                 { "TWAIN", PreferenceConstants.SCANNER_DRV_TYPE_TWAIN },
                 { "WIA", PreferenceConstants.SCANNER_DRV_TYPE_WIA } },
             getFieldEditorParent(), true);
         addField(rgFe);
 
         rgFe = new RadioGroupFieldEditor(PreferenceConstants.SCANNER_DPI,
             "DPI", 5, new String[][] { { "300", "300" }, { "400", "400" },
                 { "600", "600" }, { "720", "720" }, { "800", "800" } },
             getFieldEditorParent(), true);
         addField(rgFe);
 
         IntegerFieldEditor intFe = new IntegerFieldEditor(
             PreferenceConstants.SCANNER_BRIGHTNESS, "Brightness:",
             getFieldEditorParent());
         intFe.setValidRange(-1000, 1000);
         addField(intFe);
 
         intFe = new IntegerFieldEditor(PreferenceConstants.SCANNER_CONTRAST,
             "Contrast:", getFieldEditorParent());
         intFe.setValidRange(-1000, 1000);
         addField(intFe);
 
         intFe = new IntegerFieldEditor(PreferenceConstants.DLL_DEBUG_LEVEL,
             "Decode Library Debug Level:", getFieldEditorParent());
         intFe.setValidRange(0, 9);
         addField(intFe);
 
         intFe = new IntegerFieldEditor(PreferenceConstants.LIBDMTX_EDGE_THRESH,
             "Decode Edge Threshold:", getFieldEditorParent());
         intFe.setValidRange(0, 100);
         addField(intFe);
 
         intFe = new IntegerFieldEditor(PreferenceConstants.LIBDMTX_SCAN_GAP,
             "Decode Scan Gap:", getFieldEditorParent());
         intFe.setValidRange(0, 10000);
         addField(intFe);
 
         intFe = new IntegerFieldEditor(PreferenceConstants.LIBDMTX_SQUARE_DEV,
             "Decode Square Deviation:", getFieldEditorParent());
         intFe.setValidRange(0, 90);
         addField(intFe);

        intFe = new IntegerFieldEditor(PreferenceConstants.LIBDMTX_CORRECTIONS,
            "Decode Corrections:", getFieldEditorParent());
        intFe.setValidRange(0, 100);
        addField(intFe);
     }
 
     @Override
     public void init(IWorkbench workbench) {
     }
 
 }
