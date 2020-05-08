 package edu.ualberta.med.scannerconfig.preferences.scanner;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.IntegerFieldEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
 import edu.ualberta.med.scannerconfig.dmscanlib.ScanLib;
 import edu.ualberta.med.scannerconfig.dmscanlib.ScanLibResult;
 import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
 import edu.ualberta.med.scannerconfig.widgets.AdvancedRadioGroupFieldEditor;
 
 public class Scanner extends FieldEditorPreferencePage implements
     IWorkbenchPreferencePage, SelectionListener {
 
     private Map<String, IntegerFieldEditor> intFieldMap = new HashMap<String, IntegerFieldEditor>();
 
     Button selectScannerBtn;
     AdvancedRadioGroupFieldEditor dpiRadio, driverTypeRadio;
 
     IntegerFieldEditor brightnessInputField, contrastInputField;
 
     public Scanner() {
         super(GRID);
         setPreferenceStore(ScannerConfigPlugin.getDefault()
             .getPreferenceStore());
     }
 
     @Override
     public void init(IWorkbench workbench) {
         setPreferenceStore(ScannerConfigPlugin.getDefault()
             .getPreferenceStore());
     }
 
     @Override
     public void createFieldEditors() {
         selectScannerBtn = new Button(getFieldEditorParent(), SWT.NONE);
         selectScannerBtn.setText("Select Scanner");
         selectScannerBtn.setImage(ScannerConfigPlugin.getDefault()
             .getImageRegistry().get(ScannerConfigPlugin.IMG_SCANNER));
         selectScannerBtn.addSelectionListener(this);
 
         driverTypeRadio = new AdvancedRadioGroupFieldEditor(
             PreferenceConstants.SCANNER_DRV_TYPE, "Driver Type", 2,
             new String[][] {
                 { "WIA", PreferenceConstants.SCANNER_DRV_TYPE_WIA },
                 { "TWAIN", PreferenceConstants.SCANNER_DRV_TYPE_TWAIN } },
             getFieldEditorParent(), true);
         addField(driverTypeRadio);
 
         dpiRadio = new AdvancedRadioGroupFieldEditor(
             PreferenceConstants.SCANNER_DPI, "DPI", 5, new String[][] {
                 { PreferenceConstants.SCANNER_300_DPI,
                     PreferenceConstants.SCANNER_300_DPI },
                 { PreferenceConstants.SCANNER_400_DPI,
                     PreferenceConstants.SCANNER_400_DPI },
                 { PreferenceConstants.SCANNER_600_DPI,
                     PreferenceConstants.SCANNER_600_DPI } },
             getFieldEditorParent(), true);
 
         addField(dpiRadio);
 
         brightnessInputField = new IntegerFieldEditor(
             PreferenceConstants.SCANNER_BRIGHTNESS, "Brightness:",
             getFieldEditorParent());
         brightnessInputField.setValidRange(-1000, 1000);
         addField(brightnessInputField);
         intFieldMap.put(brightnessInputField.getPreferenceName(),
             brightnessInputField);
 
         contrastInputField = new IntegerFieldEditor(
             PreferenceConstants.SCANNER_CONTRAST, "Contrast:",
             getFieldEditorParent());
         contrastInputField.setValidRange(-1000, 1000);
         addField(contrastInputField);
         intFieldMap.put(contrastInputField.getPreferenceName(),
             contrastInputField);
     }
 
     private void setEnableAllWidgets(boolean enableSettings) {
         if (enableSettings) {
            ScanLibResult result = ScanLib.getInstance().getScannerCapability();
             int scannerCap = result.getValue();
             dpiRadio.setEnabledArray(new boolean[] {
                 (scannerCap & ScanLib.CAP_DPI_300) != 0,
                 (scannerCap & ScanLib.CAP_DPI_400) != 0,
                 (scannerCap & ScanLib.CAP_DPI_600) != 0 }, 0,
                 getFieldEditorParent());
         } else {
             dpiRadio.setEnabled(false, getFieldEditorParent());
         }
 
         selectScannerBtn.setEnabled(true);
         driverTypeRadio.setEnabled(enableSettings, getFieldEditorParent());
         brightnessInputField.setEnabled(enableSettings, getFieldEditorParent());
         contrastInputField.setEnabled(enableSettings, getFieldEditorParent());
     }
 
     @Override
     public void widgetSelected(SelectionEvent e) {
         if (e.getSource() != selectScannerBtn)
             return;
 
         ScanLibResult scanlibResult = ScanLib.getInstance()
             .selectSourceAsDefault();
         ScanLibResult scannerCapResult = ScanLib.getInstance()
             .getScannerCapability();
 
         int scannerCap = scannerCapResult.getValue();
 
         if (scanlibResult.getResultCode() != ScanLib.SC_SUCCESS) {
             // just stay with the last selected source
             if ((scannerCap & ScanLib.CAP_IS_SCANNER) != 0) {
                 return;
             }
             setEnableAllWidgets(false);
            ScannerConfigPlugin.openError("Scanning Source Error",
                scanlibResult.getMessage());
             return;
         }
 
         IPreferenceStore prefs = ScannerConfigPlugin.getDefault()
             .getPreferenceStore();
 
         String drvSetting = null;
         boolean[] drvRadioSettings = new boolean[] { false, false };
 
         if ((scannerCap & ScanLib.CAP_IS_WIA) != 0) {
             drvSetting = PreferenceConstants.SCANNER_DRV_TYPE_WIA;
             drvRadioSettings[1] = true;
         } else {
             drvSetting = PreferenceConstants.SCANNER_DRV_TYPE_TWAIN;
             drvRadioSettings[0] = true;
         }
 
         prefs.setValue(PreferenceConstants.SCANNER_DRV_TYPE, drvSetting);
         driverTypeRadio.setSelectionArray(drvRadioSettings);
         driverTypeRadio.doLoad();
 
         boolean[] dpiRadioSettings = new boolean[] { false, false, false };
 
         if ((scannerCap & ScanLib.CAP_DPI_300) != 0) {
             dpiRadioSettings[0] = true;
 
         }
 
         if ((scannerCap & ScanLib.CAP_DPI_400) != 0) {
             dpiRadioSettings[1] = true;
 
         }
 
         if ((scannerCap & ScanLib.CAP_DPI_600) != 0) {
             dpiRadioSettings[2] = true;
         }
 
         dpiRadio.setEnabledArray(dpiRadioSettings, -1, getFieldEditorParent());
         dpiRadio.doLoad();
 
         setEnableAllWidgets(true);
     }
 
     @Override
     public void widgetDefaultSelected(SelectionEvent e) {
         // do nothing
     }
 
 }
