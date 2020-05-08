 package edu.ualberta.med.biobank.barcodegenerator.views;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.UUID;
 
 import javax.imageio.ImageIO;
 import javax.print.PrintService;
 import javax.print.PrintServiceLookup;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceStore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.part.ViewPart;
 
 import edu.ualberta.med.biobank.barcodegenerator.Activator;
 import edu.ualberta.med.biobank.barcodegenerator.preferences.PreferenceConstants;
 import edu.ualberta.med.biobank.barcodegenerator.preferences.PreferenceInitializer;
 import edu.ualberta.med.biobank.barcodegenerator.template.Template;
 import edu.ualberta.med.biobank.barcodegenerator.template.TemplateStore;
 import edu.ualberta.med.biobank.barcodegenerator.template.presets.cbsr.CBSRData;
 import edu.ualberta.med.biobank.barcodegenerator.template.presets.cbsr.CBSRTemplate;
 import edu.ualberta.med.biobank.barcodegenerator.template.presets.cbsr.exceptions.CBSRGuiVerificationException;
 import edu.ualberta.med.biobank.barcodegenerator.template.presets.cbsr.exceptions.CBSRPdfGenException;
 
 public class LabelPrinterView extends ViewPart {
 
     public static final String ID = "edu.ualberta.med.biobank.barcodegenerator.views.LabelPrinterView";
     private Composite top = null;
     private Composite composite = null;
     private Composite composite3 = null;
     private Composite composite4 = null;
     private Label label = null;
     private Text projectTitleText = null;
     private Label label1 = null;
     private Text logoText = null;
     private Button logoButton = null;
     private Group group = null;
     private Canvas logoCanvas = null;
     private Group group1 = null;
     private Composite composite5 = null;
     private Label label2 = null;
     private Label label3 = null;
     private Label label4 = null;
     private Label label5 = null;
     private Label label6 = null;
     private Text label1Text = null;
     private Button value1Checkbox = null;
     private Button label1Checkbox = null;
     private Button printBarcode1Checkbox = null;
     private Text value1Text = null;
     private Composite composite6 = null;
     private Label label7 = null;
     private Text patientIDText = null;
     private Button label2Checkbox = null;
     private Text label2Text = null;
     private Button value2Checkbox = null;
     private Text value2Text = null;
     private Button printBarcode2Checkbox = null;
     private Button label3Checkbox = null;
     private Text label3Text = null;
     private Button value3Checkbox = null;
     private Text value3Text = null;
     private Button printBarcode3Checkbox = null;
     private Group group2 = null;
 
     private Label label8 = null;
     private Button sampleTypeCheckbox = null;
     private Text sampleTypeText = null;
     private Label intendedPrinter = null;
     private Label label9 = null;
     private Combo templateCombo = null;
     private Combo printerCombo = null;
     private Group group3 = null;
     private Group group4 = null;
     private Button exitButton = null;
     private Button printButton = null;
     private Button savePdfButton = null;
     private CLabel cLabel = null;
     private Shell shell;
     private IPreferenceStore perferenceStore;
     private TemplateStore templateStore;
     private Template selectedTemplate;
 
     @Override
     public void createPartControl(Composite parent) {
 
         // TODO implement errors for templateStore
        try {
            templateStore = new TemplateStore();
        } catch (ClassNotFoundException e) {
            // FIXME
            e.printStackTrace();
        }
 
         loadPreferenceStore();
 
         shell = parent.getShell();
         top = new Composite(parent, SWT.NONE);
         top.setBackground(new Color(Display.getCurrent(), 237, 236, 235));
         top.setLayout(new GridLayout());
         brandingGroup();
         patientInfoGroup();
         sampleTextGroup();
         actionButtonGroup();
     }
 
     private void loadPreferenceStore() {
         perferenceStore = null;
 
         if (Activator.getDefault() != null)
             perferenceStore = Activator.getDefault().getPreferenceStore();
 
         if (perferenceStore == null) {
             System.err.println("WARNING: preference store was NULL!");
             perferenceStore = new PreferenceStore("barcodegen.properties");
             PreferenceInitializer.setDefaults(perferenceStore);
         }
     }
 
     @Override
     public void setFocus() {
 
     }
 
     /**
      * This method initializes composite
      * 
      */
     private void createComposite() {
         composite = new Composite(group3, SWT.NONE);
         composite.setBackground(new Color(Display.getCurrent(), 237, 56, 235));
         composite.setLayout(new FillLayout());
         composite.setForeground(new Color(Display.getCurrent(), 0, 0, 0));
         createComposite3();
         createComposite4();
     }
 
     /**
      * This method initializes composite3
      * 
      */
     private void createComposite3() {
         GridData gridData21 = new GridData();
         gridData21.grabExcessHorizontalSpace = true;
         gridData21.verticalAlignment = GridData.CENTER;
         gridData21.horizontalAlignment = GridData.FILL;
         GridData gridData1 = new GridData();
         gridData1.horizontalAlignment = GridData.FILL;
         gridData1.grabExcessHorizontalSpace = true;
         gridData1.verticalAlignment = GridData.CENTER;
         GridData gridData = new GridData();
         gridData.horizontalAlignment = GridData.FILL;
         gridData.verticalAlignment = GridData.CENTER;
         gridData.grabExcessHorizontalSpace = true;
         GridLayout gridLayout = new GridLayout();
         gridLayout.numColumns = 3;
         gridLayout.makeColumnsEqualWidth = false;
         composite3 = new Composite(composite, SWT.NONE);
         composite3.setLayout(gridLayout);
         label = new Label(composite3, SWT.NONE);
         label.setText("Project Title:");
         projectTitleText = new Text(composite3, SWT.BORDER);
         projectTitleText.setLayoutData(gridData);
         projectTitleText.setTextLimit(12);
         projectTitleText.setText(perferenceStore
             .getString(PreferenceConstants.PROJECT_TITLE));
         @SuppressWarnings("unused")
         Label filler = new Label(composite3, SWT.NONE);
         label1 = new Label(composite3, SWT.NONE);
         label1.setText("Logo:");
         logoText = new Text(composite3, SWT.BORDER);
         logoText.setEditable(false);
         logoText.setLayoutData(gridData1);
         logoText.setText(perferenceStore
             .getString(PreferenceConstants.LOGO_FILE_LOCATION));
         logoButton = new Button(composite3, SWT.NONE);
         logoButton.setText("Browse...");
         logoButton.addSelectionListener(new SelectionListener() {
             public void widgetSelected(SelectionEvent event) {
                 FileDialog fd = new FileDialog(shell, SWT.OPEN);
                 fd.setText("Select Logo");
                 String[] filterExt = { "*.png" };
                 fd.setFilterExtensions(filterExt);
                 String selected = fd.open();
                 if (selected != null) {
                     logoText.setText(selected);
                     logoCanvas.redraw();
                 }
 
             }
 
             public void widgetDefaultSelected(SelectionEvent event) {
                 widgetSelected(event);
             }
         });
 
         label9 = new Label(composite3, SWT.NONE);
         label9.setText("Template:");
         templateCombo = new Combo(composite3, SWT.DROP_DOWN | SWT.BORDER);
         templateCombo.setLayoutData(gridData21);
 
         for (String s : templateStore.getTemplateNames()) {
             templateCombo.add(s);
         }
 
         if (templateCombo.getItemCount() > 0)
             templateCombo.select(0);
 
         for (int i = 0; i < templateCombo.getItemCount(); i++) {
             if (templateCombo.getItem(i).equals(
                 perferenceStore.getString(PreferenceConstants.TEMPLATE_NAME))) {
                 templateCombo.select(i);
                 break;
             }
         }
 
         templateCombo.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 loadSelectedTemplate();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
 
         filler = new Label(composite3, SWT.NONE);
 
         label9 = new Label(composite3, SWT.NONE);
         label9.setText("Intended Printer:");
         intendedPrinter = new Label(composite3, SWT.NONE);
         intendedPrinter.setForeground(new Color(shell.getDisplay(), 255, 0, 0));
         intendedPrinter.setText("default");
         loadSelectedTemplate();
 
         filler = new Label(composite3, SWT.NONE);
 
         label9 = new Label(composite3, SWT.NONE);
         label9.setText("Printer:");
         printerCombo = new Combo(composite3, SWT.DROP_DOWN | SWT.BORDER);
         printerCombo.setLayoutData(gridData21);
 
         PrintService[] services = PrintServiceLookup.lookupPrintServices(null,
             null);
 
         for (PrintService ps : services) {
             printerCombo.add(ps.getName());
         }
         if (printerCombo.getItemCount() > 0)
             printerCombo.select(0);
 
         for (int i = 0; i < printerCombo.getItemCount(); i++) {
             if (printerCombo.getItem(i).equals(
                 perferenceStore.getString(PreferenceConstants.PRINTER_NAME))) {
                 printerCombo.select(i);
                 break;
             }
         }
 
     }
 
     private void loadSelectedTemplate() {
 
         if (templateCombo.getSelectionIndex() >= 0) {
 
             selectedTemplate = templateStore.getTemplate(templateCombo
                 .getItem(templateCombo.getSelectionIndex()));
 
             // load gui elements that use template data
             if (selectedTemplate != null)
                 intendedPrinter.setText(selectedTemplate.getIntendedPrinter());
 
         } else
             selectedTemplate = null;
     }
 
     /**
      * This method initializes composite4
      * 
      */
     private void createComposite4() {
         GridLayout gridLayout1 = new GridLayout();
         composite4 = new Composite(composite, SWT.NONE);
         composite4.setLayout(gridLayout1);
         createGroup();
     }
 
     /**
      * This method initializes group
      * 
      */
     private void createGroup() {
         GridData gridData2 = new GridData();
         gridData2.horizontalAlignment = GridData.FILL;
         gridData2.grabExcessHorizontalSpace = true;
         gridData2.grabExcessVerticalSpace = true;
         gridData2.verticalAlignment = GridData.FILL;
         group = new Group(composite4, SWT.NONE);
         group.setLayout(new GridLayout());
         group.setText("Logo");
         createLogoCanvas();
         group.setLayoutData(gridData2);
     }
 
     /**
      * This method initializes logoCanvas
      * 
      */
     private void createLogoCanvas() {
         GridData gridData3 = new GridData();
         gridData3.grabExcessHorizontalSpace = true;
         gridData3.horizontalAlignment = GridData.FILL;
         gridData3.verticalAlignment = GridData.FILL;
         gridData3.grabExcessVerticalSpace = true;
         logoCanvas = new Canvas(group, SWT.NONE);
         logoCanvas
             .setBackground(new Color(Display.getCurrent(), 255, 255, 255));
         logoCanvas.setLayoutData(gridData3);
         logoCanvas.addPaintListener(new PaintListener() {
 
             @Override
             public void paintControl(PaintEvent e) {
                 if (new File(logoText.getText()).exists()) {
                     Image image = new Image(shell.getDisplay(), logoText
                         .getText());
                     if (image != null) {
                         e.gc.drawImage(image, 0, 0, image.getBounds().width,
                             image.getBounds().height, 0, 0,
                             logoCanvas.getBounds().width,
                             logoCanvas.getBounds().height);
                         return;
                     }
 
                 }
                 e.gc.drawString("No logo", 0, 0);
             }
 
         });
         logoCanvas.redraw();
     }
 
     /**
      * This method initializes group1
      * 
      */
     private void patientInfoGroup() {
 
         GridData gridData = new GridData();
         gridData.horizontalAlignment = GridData.FILL;
         gridData.grabExcessHorizontalSpace = true;
         gridData.grabExcessVerticalSpace = false;
         gridData.verticalAlignment = GridData.FILL;
 
         group1 = new Group(top, SWT.NONE);
         group1.setText(" Info Fields");
         group1.setLayoutData(gridData);
         group1.setLayout(new GridLayout());
         createComposite6();
         createComposite5();
     }
 
     /**
      * This method initializes composite5
      * 
      */
     private void createComposite5() {
         GridData gridData11 = new GridData();
         gridData11.horizontalAlignment = GridData.BEGINNING;
         gridData11.verticalAlignment = GridData.CENTER;
         GridData gridData10 = new GridData();
         gridData10.horizontalAlignment = GridData.FILL;
         gridData10.verticalAlignment = GridData.CENTER;
         GridData gridData9 = new GridData();
         gridData9.horizontalAlignment = GridData.FILL;
         gridData9.verticalAlignment = GridData.CENTER;
         GridData gridData8 = new GridData();
         gridData8.horizontalAlignment = GridData.FILL;
         gridData8.verticalAlignment = GridData.CENTER;
         GridData gridData5 = new GridData();
         gridData5.horizontalAlignment = GridData.FILL;
         gridData5.verticalAlignment = GridData.CENTER;
         GridData gridData7 = new GridData();
         gridData7.horizontalAlignment = GridData.FILL;
         gridData7.grabExcessHorizontalSpace = true;
         gridData7.verticalAlignment = GridData.FILL;
         GridData gridData6 = new GridData();
         gridData6.horizontalAlignment = GridData.FILL;
         gridData6.grabExcessVerticalSpace = false;
         gridData6.grabExcessHorizontalSpace = true;
         gridData6.verticalAlignment = GridData.FILL;
         GridLayout gridLayout2 = new GridLayout();
         gridLayout2.numColumns = 5;
         gridLayout2.makeColumnsEqualWidth = false;
         composite5 = new Composite(group1, SWT.NONE);
         composite5.setLayout(gridLayout2);
         label2 = new Label(composite5, SWT.NONE);
         label2.setText("Enable:");
         label3 = new Label(composite5, SWT.NONE);
         label3.setText("Label (Patient Name/PHN/etc):");
         label4 = new Label(composite5, SWT.NONE);
         label4.setText("Enable:");
         label5 = new Label(composite5, SWT.NONE);
         label5.setText("Value (eg BOB MARLEY):");
         label6 = new Label(composite5, SWT.NONE);
         label6.setText("Print Barcode:");
 
         label1Checkbox = new Button(composite5, SWT.CHECK);
         label1Checkbox.setSelection(perferenceStore
             .getBoolean(PreferenceConstants.LABEL_CHECKBOX_1));
         label1Text = new Text(composite5, SWT.BORDER);
         label1Text.setLayoutData(gridData6);
         label1Text.setTextLimit(12);
         label1Text.setText(perferenceStore
             .getString(PreferenceConstants.LABEL_TEXT_1));
 
         value1Checkbox = new Button(composite5, SWT.CHECK);
         value1Checkbox.setSelection(perferenceStore
             .getBoolean(PreferenceConstants.VALUE_CHECKBOX_1));
         value1Text = new Text(composite5, SWT.BORDER);
         value1Text.setLayoutData(gridData7);
         value1Text.setTextLimit(24);
 
         printBarcode1Checkbox = new Button(composite5, SWT.CHECK);
         printBarcode1Checkbox.setLayoutData(gridData11);
         printBarcode1Checkbox.setSelection(perferenceStore
             .getBoolean(PreferenceConstants.BARCODE_CHECKBOX_1));
 
         label2Checkbox = new Button(composite5, SWT.CHECK);
         label2Checkbox.setSelection(perferenceStore
             .getBoolean(PreferenceConstants.LABEL_CHECKBOX_2));
         label2Text = new Text(composite5, SWT.BORDER);
         label2Text.setLayoutData(gridData8);
         label2Text.setTextLimit(12);
         label2Text.setText(perferenceStore
             .getString(PreferenceConstants.LABEL_TEXT_2));
 
         value2Checkbox = new Button(composite5, SWT.CHECK);
         value2Checkbox.setSelection(perferenceStore
             .getBoolean(PreferenceConstants.VALUE_CHECKBOX_2));
         value2Text = new Text(composite5, SWT.BORDER);
         value2Text.setLayoutData(gridData5);
         value2Text.setTextLimit(24);
         printBarcode2Checkbox = new Button(composite5, SWT.CHECK);
         printBarcode2Checkbox.setSelection(perferenceStore
             .getBoolean(PreferenceConstants.BARCODE_CHECKBOX_2));
 
         label3Checkbox = new Button(composite5, SWT.CHECK);
         label3Checkbox.setSelection(perferenceStore
             .getBoolean(PreferenceConstants.LABEL_CHECKBOX_3));
         label3Text = new Text(composite5, SWT.BORDER);
         label3Text.setLayoutData(gridData10);
         label3Text.setTextLimit(12);
         label3Text.setText(perferenceStore
             .getString(PreferenceConstants.LABEL_TEXT_3));
         value3Checkbox = new Button(composite5, SWT.CHECK);
         value3Checkbox.setSelection(perferenceStore
             .getBoolean(PreferenceConstants.VALUE_CHECKBOX_3));
         value3Text = new Text(composite5, SWT.BORDER);
         value3Text.setLayoutData(gridData9);
         value3Text.setTextLimit(24);
         printBarcode3Checkbox = new Button(composite5, SWT.CHECK);
         printBarcode3Checkbox.setSelection(perferenceStore
             .getBoolean(PreferenceConstants.BARCODE_CHECKBOX_3));
     }
 
     /**
      * This method initializes composite6
      * 
      */
     private void createComposite6() {
         GridData gridData4 = new GridData();
         gridData4.horizontalAlignment = GridData.FILL;
         gridData4.grabExcessHorizontalSpace = false;
         gridData4.horizontalSpan = 4;
         gridData4.horizontalIndent = 9;
         gridData4.widthHint = 150;
         gridData4.verticalAlignment = GridData.CENTER;
         GridLayout gridLayout3 = new GridLayout();
         gridLayout3.verticalSpacing = 2;
         gridLayout3.numColumns = 5;
         composite6 = new Composite(group1, SWT.NONE);
         composite6.setLayout(gridLayout3);
         label7 = new Label(composite6, SWT.NONE);
         label7.setText("Patient ID:");
         patientIDText = new Text(composite6, SWT.BORDER);
         patientIDText.setLayoutData(gridData4);
         patientIDText.setTextLimit(12);
         patientIDText.addListener(SWT.Verify, new Listener() {
             public void handleEvent(Event e) {
                 if (!e.text.matches("[{a-zA-Z0-9}]*")) {
                     e.doit = false;
                     return;
                 }
             }
         });
     }
 
     /**
      * This method initializes group2
      * 
      */
     private void sampleTextGroup() {
 
         GridData gridData = new GridData();
         gridData.horizontalAlignment = GridData.FILL;
         gridData.grabExcessHorizontalSpace = true;
         gridData.verticalAlignment = GridData.FILL;
 
         GridLayout gridLayout5 = new GridLayout();
         gridLayout5.numColumns = 4;
 
         group2 = new Group(top, SWT.NONE);
 
         sampleTypeCheckbox = new Button(group2, SWT.CHECK | SWT.LEFT);
         sampleTypeCheckbox.setText("Enable");
         sampleTypeCheckbox.setSelection(perferenceStore
             .getBoolean(PreferenceConstants.SAMPLETYPE_CHECKBOX));
         cLabel = new CLabel(group2, SWT.NONE);
         cLabel.setText("Sample Type (on labels):");
         sampleTypeText = new Text(group2, SWT.BORDER | SWT.V_SCROLL
             | SWT.SINGLE);
         sampleTypeText.setText(perferenceStore
             .getString(PreferenceConstants.SAMPLETYPE_TEXT));
         sampleTypeText.setTextLimit(25);
         label8 = new Label(group2, SWT.LEFT | SWT.HORIZONTAL);
         label8.setText("");
         @SuppressWarnings("unused")
         Label filler61 = new Label(group2, SWT.NONE);
 
         group2.setLayout(gridLayout5);
         group2.setText("Additonal Configuration");
         group2.setLayoutData(gridData);
 
     }
 
     /**
      * This method initializes group3
      * 
      */
     private void brandingGroup() {
 
         GridData gridData = new GridData();
         gridData.horizontalAlignment = GridData.FILL;
         gridData.grabExcessHorizontalSpace = true;
         gridData.grabExcessVerticalSpace = false;
         gridData.verticalAlignment = GridData.FILL;
 
         group3 = new Group(top, SWT.NONE);
         group3.setLayoutData(gridData);
         group3.setText("Branding");
         createComposite();
         group3.setLayout(new GridLayout());
     }
 
     /**
      * This method initializes group4
      * 
      */
     private void actionButtonGroup() {
         GridLayout gridLayout5 = new GridLayout();
         gridLayout5.numColumns = 6;
         gridLayout5.makeColumnsEqualWidth = true;
 
         GridData gridData = new GridData();
         gridData.horizontalAlignment = GridData.FILL;
         gridData.grabExcessHorizontalSpace = true;
         gridData.grabExcessVerticalSpace = false;
         gridData.verticalAlignment = GridData.FILL;
 
         GridData gridData7 = new GridData();
         gridData7.grabExcessHorizontalSpace = true;
         gridData7.horizontalAlignment = GridData.FILL;
 
         group4 = new Group(top, SWT.NONE);
         group4.setText("Actions");
 
         exitButton = new Button(group4, SWT.NONE);
         exitButton.setText("Exit Label Maker");
         exitButton.addSelectionListener(exitButtonListener);
         exitButton.setLayoutData(gridData7);
 
         new Label(group4, SWT.NONE);
         new Label(group4, SWT.NONE);
         new Label(group4, SWT.NONE);
 
         savePdfButton = new Button(group4, SWT.NONE);
         savePdfButton.setText("Print to PDF");
         savePdfButton.addSelectionListener(savePdfListener);
         savePdfButton.setLayoutData(gridData7);
 
         printButton = new Button(group4, SWT.NONE);
         printButton.setText("Print Label Sheet");
         printButton.addSelectionListener(printButtonListener);
         printButton.setLayoutData(gridData7);
 
         group4.setLayout(gridLayout5);
         group4.setLayoutData(gridData);
 
     }
 
     // called after a successful print or save.
     private void updateSavePreferences() {
 
         perferenceStore.setValue(PreferenceConstants.LOGO_FILE_LOCATION,
             logoText.getText());
         perferenceStore.setValue(PreferenceConstants.PROJECT_TITLE,
             projectTitleText.getText());
 
         if (templateCombo.getSelectionIndex() >= 0)
             perferenceStore.setValue(PreferenceConstants.TEMPLATE_NAME,
                 templateCombo.getItem(templateCombo.getSelectionIndex()));
 
         if (printerCombo.getSelectionIndex() >= 0)
             perferenceStore.setValue(PreferenceConstants.PRINTER_NAME,
                 printerCombo.getItem(printerCombo.getSelectionIndex()));
 
         perferenceStore.setValue(PreferenceConstants.LABEL_CHECKBOX_1,
             label1Checkbox.getSelection());
         perferenceStore.setValue(PreferenceConstants.LABEL_CHECKBOX_2,
             label2Checkbox.getSelection());
         perferenceStore.setValue(PreferenceConstants.LABEL_CHECKBOX_3,
             label3Checkbox.getSelection());
 
         perferenceStore.setValue(PreferenceConstants.LABEL_TEXT_1,
             label1Text.getText());
         perferenceStore.setValue(PreferenceConstants.LABEL_TEXT_2,
             label2Text.getText());
         perferenceStore.setValue(PreferenceConstants.LABEL_TEXT_3,
             label3Text.getText());
 
         perferenceStore.setValue(PreferenceConstants.VALUE_CHECKBOX_1,
             value1Checkbox.getSelection());
         perferenceStore.setValue(PreferenceConstants.VALUE_CHECKBOX_2,
             value2Checkbox.getSelection());
         perferenceStore.setValue(PreferenceConstants.VALUE_CHECKBOX_3,
             value3Checkbox.getSelection());
 
         perferenceStore.setValue(PreferenceConstants.BARCODE_CHECKBOX_1,
             printBarcode1Checkbox.getSelection());
         perferenceStore.setValue(PreferenceConstants.BARCODE_CHECKBOX_2,
             printBarcode2Checkbox.getSelection());
         perferenceStore.setValue(PreferenceConstants.BARCODE_CHECKBOX_3,
             printBarcode3Checkbox.getSelection());
 
         perferenceStore.setValue(PreferenceConstants.SAMPLETYPE_CHECKBOX,
             sampleTypeCheckbox.getSelection());
         perferenceStore.setValue(PreferenceConstants.SAMPLETYPE_TEXT,
             sampleTypeText.getText());
 
     }
 
     public class BarcodeViewGuiData extends CBSRData {
 
         public BarcodeViewGuiData() throws CBSRGuiVerificationException {
 
             projectTileStr = projectTitleText.getText();
 
             if (projectTileStr == null || projectTileStr.length() == 0) {
                 throw new CBSRGuiVerificationException("Incorrect Title",
                     "A valid title is required.");
             }
 
             ByteArrayInputStream bis = null;
             try {
                 BufferedImage logoImage;
 
                 logoImage = ImageIO.read(new File(logoText.getText()));
                 ByteArrayOutputStream binaryOutputStream = new ByteArrayOutputStream();
                 if (logoImage != null) {
                     ImageIO.write(logoImage, "PNG", binaryOutputStream);
                     bis = new ByteArrayInputStream(
                         binaryOutputStream.toByteArray());
                 } else {
                     bis = null;
                 }
 
             } catch (IOException e) {
                 bis = null;
             }
             logoStream = bis;
 
             /*
              * templateFile = new File(templateText.getText()); if
              * (!templateFile.exists()) {
              * Activator.openAsyncError("Error: Could Not Find Template",
              * "A valid template file location is required."); return; }
              */
 
             patientIdStr = patientIDText.getText();
             if (patientIdStr == null || patientIdStr.length() == 0) {
                 throw new CBSRGuiVerificationException("Incorrect PatientID",
                     "A valid patient Id is required.");
 
             }
             // ------------ patient info start-----------------
             label1Str = null;
             if (label1Checkbox.getSelection()) {
                 label1Str = label1Text.getText();
             }
             value1Str = null;
             barcode1Print = false;
             if (value1Checkbox.getSelection()) {
                 value1Str = value1Text.getText();
                 barcode1Print = printBarcode1Checkbox.getSelection();
             }
 
             label2Str = null;
             if (label2Checkbox.getSelection()) {
                 label2Str = label2Text.getText();
             }
             value2Str = null;
             barcode2Print = false;
             if (value2Checkbox.getSelection()) {
                 value2Str = value2Text.getText();
                 barcode2Print = printBarcode2Checkbox.getSelection();
             }
 
             label3Str = null;
             if (label3Checkbox.getSelection()) {
                 label3Str = label3Text.getText();
             }
             value3Str = null;
             barcode3Print = false;
             if (value3Checkbox.getSelection()) {
                 value3Str = value3Text.getText();
                 barcode3Print = printBarcode3Checkbox.getSelection();
             }
             // ------------ patient info end-----------------
 
             // only need if we are printing.
             if (printerCombo.getSelectionIndex() >= 0)
                 printerNameStr = printerCombo.getItem(printerCombo
                     .getSelectionIndex());
 
             else
                 printerNameStr = null;
 
             sampleTypeStr = null;
             if (sampleTypeCheckbox.getSelection()) {
                 sampleTypeStr = sampleTypeText.getText();
             }
 
             templateCBSR = (CBSRTemplate) selectedTemplate;
 
             if (templateCBSR == null) {
                 throw new CBSRGuiVerificationException("Verifcation Issue",
                     "Could not load template.");
             }
 
             if (!(templateCBSR).jasperFileDataExists()) {
                 throw new CBSRGuiVerificationException("Verifcation Issue",
                     "Template is lacking a jasper file.");
             }
         }
     };
 
     public static String randString() {
         return UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "")
             .substring(0, 6)
             + UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "")
                 .toUpperCase().substring(0, 6);
     }
 
     private static ArrayList<String> randStringArray(int c) {
 
         ArrayList<String> l = new ArrayList<String>();
 
         for (int i = 0; i < c; i++) {
 
             l.add(randString());
         }
         return l;
     }
 
     private SelectionListener printButtonListener = new SelectionListener() {
         @Override
         public void widgetSelected(SelectionEvent e) {
             try {
                 new ProgressMonitorDialog(shell).run(true, true,
                     new PrintingOperation(new BarcodeViewGuiData()));
                 updateSavePreferences();
             } catch (InvocationTargetException e1) {
                 Error("Printing Error", e1.getMessage());
                 return;
             } catch (InterruptedException e2) {
             } catch (CBSRGuiVerificationException e1) {
                 Error("Gui Validation", e1.getMessage());
                 return;
             }
         }
 
         @Override
         public void widgetDefaultSelected(SelectionEvent e) {
             widgetSelected(e);
 
         }
     };
 
     class PrintingOperation implements IRunnableWithProgress {
 
         BarcodeViewGuiData guiData = null;
 
         public PrintingOperation(BarcodeViewGuiData guiData) {
             this.guiData = guiData;
         }
 
         public void run(IProgressMonitor monitor)
             throws InvocationTargetException, InterruptedException {
             monitor.beginTask("Printing Barcode Labels",
                 IProgressMonitor.UNKNOWN);
 
             if (guiData != null) {
                 try {
                     monitor.subTask("Sending Data to Printer");
                     guiData.templateCBSR.print(guiData, randStringArray(32));
 
                 } catch (CBSRPdfGenException e1) {
                     Error("Gui Validation", e1.getError());
                     return;
                 }
             }
 
             monitor.done();
             if (monitor.isCanceled())
                 throw new InterruptedException(
                     "Printing Operation was cancelled");
         }
     }
 
     private SelectionListener savePdfListener = new SelectionListener() {
         @Override
         public void widgetSelected(SelectionEvent e) {
             BarcodeViewGuiData guiData = null;
 
             try {
                 guiData = new BarcodeViewGuiData();
             } catch (CBSRGuiVerificationException e1) {
                 Error("Gui Validation", e1.getMessage());
                 return;
             }
 
             FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
 
             fileDialog.setFilterPath(perferenceStore
                 .getString(PreferenceConstants.PDF_DIRECTORY_PATH));
             fileDialog.setOverwrite(true);
             String pdfFilePath = fileDialog.open();
 
             if (pdfFilePath == null)
                 return;
 
             if (!pdfFilePath.contains(".pdf"))
                 pdfFilePath += ".pdf";
 
             try {
                 new ProgressMonitorDialog(shell).run(true, true,
                     new SaveOperation(guiData, pdfFilePath));
 
                 // save our recently saved file path location
                 String parentDir = new File(pdfFilePath).getParentFile()
                     .getPath();
                 if (parentDir != null)
                     perferenceStore.setValue(
                         PreferenceConstants.PDF_DIRECTORY_PATH, parentDir);
 
                 updateSavePreferences();
 
             } catch (InvocationTargetException e1) {
                 Error("Pdf Saving Error", e1.getMessage());
                 return;
             } catch (InterruptedException e2) {
             }
         }
 
         @Override
         public void widgetDefaultSelected(SelectionEvent e) {
             widgetSelected(e);
 
         }
     };
 
     class SaveOperation implements IRunnableWithProgress {
 
         BarcodeViewGuiData guiData = null;
         String pdfFilePath = null;
 
         public SaveOperation(BarcodeViewGuiData guiData, String pdfFilePath) {
             this.guiData = guiData;
             this.pdfFilePath = pdfFilePath;
         }
 
         public void run(IProgressMonitor monitor)
             throws InvocationTargetException, InterruptedException {
             monitor.beginTask("Saving Barcode Labels PDF",
                 IProgressMonitor.UNKNOWN);
 
             byte[] pdfdata = null;
 
             try {
                 monitor.subTask("Generating PDF");
                 pdfdata = guiData.templateCBSR.generatePdfCBSR(guiData,
                     randStringArray(32));
             } catch (CBSRPdfGenException e1) {
                 Error("Gui Validation", e1.getError());
                 return;
             }
 
             if (pdfdata != null) {
                 FileOutputStream fos;
                 try {
                     monitor.subTask("Saving PDF");
                     fos = new FileOutputStream(pdfFilePath);
                     fos.write(pdfdata);
                     fos.close();
 
                 } catch (FileNotFoundException e1) {
                     Error("Saving Pdf", "Could find file to save pdf to");
                     return;
                 } catch (IOException ee) {
                     Error("Saving Pdf",
                         "Problem saving file: " + ee.getMessage());
                     return;
                 }
             }
 
             monitor.done();
             if (monitor.isCanceled())
                 throw new InterruptedException(
                     "Saving to PDF operation was cancelled");
         }
     }
 
     // TODO full screen -- only allow one of the two views to exist.
 
     private SelectionListener exitButtonListener = new SelectionListener() {
         @Override
         public void widgetSelected(SelectionEvent e) {
             MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION
                 | SWT.YES | SWT.NO);
             messageBox.setMessage("Do you want to close this window?");
             messageBox.setText("Label Printer Closing");
             int response = messageBox.open();
             if (response == SWT.YES) {
                 // TODO close view
             }
         }
 
         @Override
         public void widgetDefaultSelected(SelectionEvent e) {
             widgetSelected(e);
 
         }
     };
 
     private void Error(String title, String message) {
         MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
         messageBox.setMessage(message);
         messageBox.setText(title);
         messageBox.open();
     }
 }
