 package edu.ualberta.med.scannerconfig.preferences.scanner.plateposition;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.StringFieldEditor;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
 import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
 import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
 import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.PlateGrid.Orientation;
 import edu.ualberta.med.scannerconfig.widgets.AdvancedRadioGroupFieldEditor;
 import edu.ualberta.med.scannerconfig.widgets.IPlateGridWidgetListener;
 import edu.ualberta.med.scannerconfig.widgets.PlateGridWidget;
 
 public class PlateSettings extends FieldEditorPreferencePage implements
     IWorkbenchPreferencePage, IPlateImageListener, IPlateGridWidgetListener {
 
     private enum Settings {
         LEFT() {
             @Override
             public String toString() {
                 return "Left";
             }
         },
         TOP() {
             @Override
             public String toString() {
                 return "Top";
             }
         },
         RIGHT() {
             @Override
             public String toString() {
                 return "Right";
             }
         },
         BOTTOM() {
             @Override
             public String toString() {
                 return "Bottom";
             }
         },
         GAPX() {
             @Override
             public String toString() {
                 return "Cell Gap Horizontal";
             }
         },
         GAPY() {
             @Override
             public String toString() {
                 return "Cell Gap Vertical";
             }
         },
         BARCODE() {
             @Override
             public String toString() {
                 return "Barcode";
             }
         }
     };
 
     private static final String NOT_ENABLED_STATUS_MSG = "Plate is not enabled";
 
     private static final String ALIGN_STATUS_MSG = "Align grid with barcodes";
 
     private static final String SCAN_REQ_STATUS_MSG = "A scan is required";
 
     protected ListenerList changeListeners = new ListenerList();
 
     protected int plateId;
     private boolean isEnabled;
 
     private Map<Settings, StringFieldEditor> plateFieldEditors;
     private Map<Settings, Text> plateTextControls;
     private Canvas canvas;
     private PlateGridWidget plateGridWidget = null;
 
     private BooleanFieldEditor enabledFieldEditor;
     private AdvancedRadioGroupFieldEditor orientationFieldEditor;
     private Button scanBtn;
     private Button refreshBtn;
 
     private Label statusLabel;
 
     private boolean internalUpdate;
 
     private PlateImageMgr plateImageMgr;
 
     public PlateSettings(int plateId) {
         super(GRID);
         this.plateId = plateId;
         internalUpdate = false;
 
         setPreferenceStore(ScannerConfigPlugin.getDefault()
             .getPreferenceStore());
 
         plateImageMgr = PlateImageMgr.instance();
         plateImageMgr.addScannedImageChangeListener(this);
     }
 
     @Override
     public void dispose() {
         plateImageMgr.removeScannedImageChangeListener(this);
         if (plateGridWidget != null) {
             plateGridWidget.dispose();
         }
     }
 
     @Override
     public void setValid(boolean enable) {
         super.setValid(enable);
 
         getContainer().updateButtons();
         Button applyButton = getApplyButton();
         if (applyButton != null)
             applyButton.setEnabled(true);
     }
 
     @Override
     public void init(IWorkbench workbench) {
         setPreferenceStore(ScannerConfigPlugin.getDefault()
             .getPreferenceStore());
     }
 
     @Override
     protected Control createContents(final Composite parent) {
 
         Composite top = new Composite(parent, SWT.NONE);
         top.setLayout(new GridLayout(2, false));
         top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
         Control s = super.createContents(top);
         GridData gd = (GridData) s.getLayoutData();
         if (gd == null) {
             gd = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
             s.setLayoutData(gd);
         }
         gd.verticalAlignment = SWT.BEGINNING;
 
         Composite right = new Composite(top, SWT.NONE);
         right.setLayout(new GridLayout(1, false));
         gd = new GridData(SWT.FILL, SWT.FILL, true, true);
         gd.widthHint = 200;
         right.setLayoutData(gd);
 
         createCanvasComp(right);
 
         statusLabel = new Label(right, SWT.BORDER);
         statusLabel
             .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 
         Composite buttonComposite = new Composite(right, SWT.NONE);
         buttonComposite.setLayout(new GridLayout(2, false));
 
         scanBtn = new Button(buttonComposite, SWT.NONE);
         scanBtn.setText("Scan");
         scanBtn.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 PlateImageMgr.instance().scanPlateImage();
             }
         });
         refreshBtn = new Button(buttonComposite, SWT.NONE);
         refreshBtn.setText("Refresh");
         refreshBtn.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 PlateSettings.this.notifyChangeListener(
                     IPlateSettingsListener.REFRESH, 0);
             }
         });
 
         if (System.getProperty("os.name").startsWith("Windows")
             && ScannerConfigPlugin.getDefault().getPlateEnabled(plateId)) {
             setEnabled(true);
         } else {
             setEnabled(false);
         }
 
         return top;
     }
 
     @Override
     protected void createFieldEditors() {
         StringFieldEditor fe;
 
         enabledFieldEditor = new BooleanFieldEditor(
             PreferenceConstants.SCANNER_PALLET_ENABLED[plateId - 1], "Enable",
             getFieldEditorParent());
         addField(enabledFieldEditor);
 
         String[] prefsArr = PreferenceConstants.SCANNER_PALLET_CONFIG[plateId - 1];
 
         plateFieldEditors = new HashMap<Settings, StringFieldEditor>();
         plateTextControls = new HashMap<Settings, Text>();
         Composite parent = getFieldEditorParent();
 
         orientationFieldEditor = new AdvancedRadioGroupFieldEditor(
             PreferenceConstants.SCANNER_PALLET_ORIENTATION[plateId - 1],
             "Orientation",
             2,
             new String[][] {
                 { PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE,
                     PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE },
                 { PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT,
                     PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT } },
             parent, true);
         addField(orientationFieldEditor);
 
         int count = 0;
         for (Settings setting : Settings.values()) {
             if (setting.equals(Settings.BARCODE)) {
                 if (!getPreferenceStore().getBoolean(
                     PreferenceConstants.SCANNER_PLATE_SHOW_BARCODE_PREF))
                     continue;
                 fe = new StringFieldEditor(
                     PreferenceConstants.SCANNER_PLATE_BARCODES[plateId - 1],
                     "Barcode:", getFieldEditorParent());
             } else {
                 fe = new DoubleFieldEditor(prefsArr[count], setting + ":",
                     parent);
                 ((DoubleFieldEditor) fe).setValidRange(0, 20);
                 addField(fe);
             }
             Text text = fe.getTextControl(parent);
             plateTextControls.put(setting, text);
             plateFieldEditors.put(setting, fe);
 
             if (!setting.equals(Settings.BARCODE)) {
                 text.addModifyListener(new ModifyListener() {
                     @Override
                     public void modifyText(ModifyEvent e) {
                         notifyChangeListener(
                             IPlateSettingsListener.TEXT_CHANGE, 0);
                     }
 
                 });
             }
             ++count;
         }
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent event) {
         super.propertyChange(event);
         Object source = event.getSource();
         if (source == enabledFieldEditor) {
             boolean enabled = enabledFieldEditor.getBooleanValue();
             if (enabled) {
                 // set default size
                 internalUpdate(0, 0, 4, 3, 0, 0, Orientation.LANDSCAPE);
             }
             setEnabled(enabled);
         } else if (source == orientationFieldEditor) {
             notifyChangeListener(
                 IPlateSettingsListener.ORIENTATION,
                 event.getNewValue().equals(
                     PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE) ? 0
                     : 1);
         }
     }
 
     /* create canvas and plate widget */
     private void createCanvasComp(Composite parent) {
 
         canvas = new Canvas(parent, SWT.BORDER | SWT.NO_BACKGROUND);
         canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         canvas.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
 
         if (plateGridWidget == null) {
             plateGridWidget = new PlateGridWidget(this, canvas);
             plateGridWidget.addPlateWidgetChangeListener(this);
         }
     }
 
     private void internalUpdate(double left, double top, double right,
         double bottom, double gapX, double gapY, Orientation o) {
         internalUpdate = true;
 
         ((DoubleFieldEditor) plateFieldEditors.get(Settings.GAPX))
             .setValidRange(0, (right - left) / 2.0 / PlateGrid.MAX_COLS);
         ((DoubleFieldEditor) plateFieldEditors.get(Settings.GAPY))
             .setValidRange(0, (bottom - top) / 2.0 / PlateGrid.MAX_COLS);
 
         plateTextControls.get(Settings.LEFT).setText(String.valueOf(left));
         plateTextControls.get(Settings.TOP).setText(String.valueOf(top));
         plateTextControls.get(Settings.RIGHT).setText(String.valueOf(right));
         plateTextControls.get(Settings.BOTTOM).setText(String.valueOf(bottom));
         plateTextControls.get(Settings.GAPX).setText(String.valueOf(gapX));
         plateTextControls.get(Settings.GAPY).setText(String.valueOf(gapY));
        plateTextControls.get(Settings.BARCODE).setText(
            getPreferenceStore().getString(
                PreferenceConstants.SCANNER_PLATE_BARCODES[plateId - 1]));
 
         boolean[] orientationSettings = new boolean[] {
             o == Orientation.LANDSCAPE, o == Orientation.PORTRAIT };
 
         orientationFieldEditor.setSelectionArray(orientationSettings);
         internalUpdate = false;
     }
 
     private String formatInput(String s) {
         try {
             Double.parseDouble(s);
             return s;
         } catch (NumberFormatException e) {
             return "0";
         }
     }
 
     public double getLeft() {
         return Double.parseDouble(formatInput(plateTextControls.get(
             Settings.LEFT).getText()));
     }
 
     public double getTop() {
         return Double.parseDouble(formatInput(plateTextControls.get(
             Settings.TOP).getText()));
     }
 
     public double getRight() {
         return Double.parseDouble(formatInput(plateTextControls.get(
             Settings.RIGHT).getText()));
     }
 
     public double getBottom() {
         return Double.parseDouble(formatInput(plateTextControls.get(
             Settings.BOTTOM).getText()));
     }
 
     public double getGapX() {
         return Double.parseDouble(formatInput(plateTextControls.get(
             Settings.GAPX).getText()));
     }
 
     public double getGapY() {
         return Double.parseDouble(formatInput(plateTextControls.get(
             Settings.GAPY).getText()));
     }
 
     public Orientation getOrientation() {
         IPreferenceStore prefs = ScannerConfigPlugin.getDefault()
             .getPreferenceStore();
 
         return prefs.getString(
             PreferenceConstants.SCANNER_PALLET_ORIENTATION[plateId - 1])
             .equals(PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE) ? Orientation.LANDSCAPE
             : Orientation.PORTRAIT;
     }
 
     public boolean isEnabled() {
         return isEnabled;
     }
 
     public double getWidth() {
         return Double.parseDouble(plateTextControls.get(Settings.RIGHT)
             .getText())
             - Double
                 .parseDouble(plateTextControls.get(Settings.LEFT).getText());
     }
 
     public double getHeight() {
         return Double.parseDouble(plateTextControls.get(Settings.BOTTOM)
             .getText())
             - Double.parseDouble(plateTextControls.get(Settings.TOP).getText());
     }
 
     private void setEnabled(boolean enabled) {
         isEnabled = enabled;
 
         for (Text text : plateTextControls.values()) {
             if (text != null)
                 text.setEnabled(enabled);
         }
 
         orientationFieldEditor.setEnabled(isEnabled, getFieldEditorParent());
         statusLabel.setText(isEnabled ? SCAN_REQ_STATUS_MSG
             : NOT_ENABLED_STATUS_MSG);
         notifyChangeListener(IPlateSettingsListener.ENABLED, enabled ? 1 : 0);
 
     }
 
     public void removePlateSettingsChangeListener(
         IPlateSettingsListener listener) {
         changeListeners.remove(listener);
     }
 
     public void addPlateBaseChangeListener(IPlateSettingsListener listener) {
         changeListeners.add(listener);
     }
 
     private void notifyChangeListener(final int message, final int detail) {
         if (internalUpdate)
             return;
 
         Object[] listeners = changeListeners.getListeners();
         for (int i = 0; i < listeners.length; ++i) {
             final IPlateSettingsListener l = (IPlateSettingsListener) listeners[i];
             SafeRunnable.run(new SafeRunnable() {
                 @Override
                 public void run() {
                     Event e = new Event();
                     e.type = message;
                     e.detail = detail;
                     l.plateGridChange(e);
                 }
             });
         }
     }
 
     private void saveSettings() {
         setEnabled(enabledFieldEditor.getBooleanValue());
     }
 
     @Override
     public boolean performOk() {
         saveSettings();
         return super.performOk();
     }
 
     @Override
     protected void performApply() {
         saveSettings();
         super.performApply();
     }
 
     @Override
     public void plateImageNew() {
         if (statusLabel == null)
             return;
         statusLabel.setText(ALIGN_STATUS_MSG);
         Rectangle imgBounds = plateImageMgr.getScannedImage().getBounds();
         double widthInches = imgBounds.width
             / (double) PlateImageMgr.PLATE_IMAGE_DPI;
         double heightInches = imgBounds.height
             / (double) PlateImageMgr.PLATE_IMAGE_DPI;
 
         ((DoubleFieldEditor) plateFieldEditors.get(Settings.LEFT))
             .setValidRange(0, widthInches);
         ((DoubleFieldEditor) plateFieldEditors.get(Settings.TOP))
             .setValidRange(0, heightInches);
         ((DoubleFieldEditor) plateFieldEditors.get(Settings.RIGHT))
             .setValidRange(0, widthInches);
         ((DoubleFieldEditor) plateFieldEditors.get(Settings.BOTTOM))
             .setValidRange(0, heightInches);
     }
 
     @Override
     public void sizeChanged() {
         statusLabel.setText(ALIGN_STATUS_MSG);
         PlateGrid<Double> r = plateGridWidget.getConvertedPlateRegion();
 
         double left = r.getLeft();
         double top = r.getTop();
         double width = r.getWidth();
         double height = r.getHeight();
 
         internalUpdate(left, top, left + width, top + height, r.getGapX(),
             r.getGapY(), r.getOrientation());
     }
 
     @Override
     public void plateImageDeleted() {
         if (statusLabel == null)
             return;
         statusLabel.setText(SCAN_REQ_STATUS_MSG);
     }
 }
