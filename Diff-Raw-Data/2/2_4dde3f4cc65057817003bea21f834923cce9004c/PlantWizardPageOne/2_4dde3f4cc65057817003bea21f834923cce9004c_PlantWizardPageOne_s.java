 package de.hswt.hrm.plant.ui.shared.ui.wizzard;
 
 import java.net.URL;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import org.apache.commons.validator.routines.RegexValidator;
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.e4.xwt.forms.XWTForms;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.Section;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.PageContainerFillLayout;
 import de.hswt.hrm.i18n.I18n;
 import de.hswt.hrm.i18n.I18nFactory;
 import de.hswt.hrm.plant.model.Plant;
 
 public class PlantWizardPageOne extends WizardPage {
 
     private static final Logger LOG = LoggerFactory.getLogger(PlantWizardPageOne.class);
     private static final I18n I18N = I18nFactory.getI18n(PlantWizardPageOne.class);
     private static final int FIRST_CONSTRUCTION_YEAR = 1985;
 
     private Composite container;
     private Optional<Plant> plant;
 
     private RegexValidator numberVal = new RegexValidator("[0-9]+([.,]{1}[0-9]+)?");
 
     public PlantWizardPageOne(String title, Optional<Plant> plant) {
         super(title);
         this.plant = plant;
         setDescription(createDescription());
         setTitle(I18N.tr("Plant Wizard"));
     }
 
     private String createDescription() {
         if (plant.isPresent()) {
             return I18N.tr("Edit a plant.");
         }
         return I18N.tr("Add a new plant.");
     }
 
     public void createControl(Composite parent) {
         parent.setLayout(new PageContainerFillLayout());
 
         URL url = PlantWizardPageOne.class.getClassLoader().getResource(
                 "de/hswt/hrm/plant/ui/shared/xwt/PlantWizardWindow"
                         + IConstants.XWT_EXTENSION_SUFFIX);
         try {
             container = (Composite) XWTForms.load(parent, url);
         }
         catch (Exception e) {
             LOG.error("An error occured: ", e);
         }
 
         translate(container);
         loadConstructionYears();
 
         if (this.plant.isPresent()) {
             updateFields(container);
         }
         FormUtil.initSectionColors((Section) XWT.findElementByName(container, "Mandatory"));
         FormUtil.initSectionColors((Section) XWT.findElementByName(container, "Optional"));
         setControl(container);
         setKeyListener();
         setPageComplete(false);
     }
 
     private void loadConstructionYears() {
         int actualYear = Calendar.getInstance().get(Calendar.YEAR);
         Combo constYearCombo = (Combo) XWT.findElementByName(container, "constructionYear");
         if (constYearCombo == null) {
             LOG.error("ConstructionYearCombo could not be found.");
             return;
         }
         for (int i = FIRST_CONSTRUCTION_YEAR; i <= actualYear; i++) {
             constYearCombo.add(String.valueOf(i));
         }
     }
 
     private void updateFields(Composite c) {
         Plant p = plant.get();
         Text t = (Text) XWT.findElementByName(c, "description");
         t.setText(p.getDescription());
         t = (Text) XWT.findElementByName(c, "area");
         t.setText(p.getArea());
         t = (Text) XWT.findElementByName(c, "location");
         t.setText(p.getLocation());
         t = (Text) XWT.findElementByName(c, "manufactor");
         t.setText(p.getManufactor().or(""));
         Combo combo = (Combo) XWT.findElementByName(c, "constructionYear");
         combo.select(combo.indexOf(p.getConstructionYear().orNull().toString()));
         t = (Text) XWT.findElementByName(c, "type");
         t.setText(p.getType().or(""));
         t = (Text) XWT.findElementByName(c, "airPerformance");
         t.setText(p.getAirPerformance().or(""));
         t = (Text) XWT.findElementByName(c, "motorPower");
         t.setText(p.getMotorPower().or(""));
         t = (Text) XWT.findElementByName(c, "ventilatorPerformance");
         t.setText(p.getVentilatorPerformance().or(""));
         t = (Text) XWT.findElementByName(c, "motorRPM");
         t.setText(p.getMotorRpm().or(""));
         t = (Text) XWT.findElementByName(c, "current");
         t.setText(p.getCurrent().or(""));
         t = (Text) XWT.findElementByName(c, "voltage");
         t.setText(p.getVoltage().or(""));
         t = (Text) XWT.findElementByName(c, "note");
         t.setText(p.getNote().or(""));
     }
 
     public HashMap<String, Text> getMandatoryWidgets() {
         HashMap<String, Text> widgets = new HashMap<String, Text>();
         widgets.put("description", (Text) XWT.findElementByName(container, "description"));
         return widgets;
     }
 
     public HashMap<String, Text> getOptionalWidgets() {
         HashMap<String, Text> widgets = new HashMap<String, Text>();
         widgets.put("manufactor", (Text) XWT.findElementByName(container, "manufactor"));
         widgets.put("type", (Text) XWT.findElementByName(container, "type"));
         widgets.put("airPerformance", (Text) XWT.findElementByName(container, "airPerformance"));
         widgets.put("motorPower", (Text) XWT.findElementByName(container, "motorPower"));
         widgets.put("ventilatorPerformance",
                 (Text) XWT.findElementByName(container, "ventilatorPerformance"));
         widgets.put("motorRPM", (Text) XWT.findElementByName(container, "motorRPM"));
         widgets.put("current", (Text) XWT.findElementByName(container, "current"));
         widgets.put("voltage", (Text) XWT.findElementByName(container, "voltage"));
         widgets.put("note", (Text) XWT.findElementByName(container, "note"));
         return widgets;
     }
 
     public HashMap<String, Combo> getOptionalCombos() {
         HashMap<String, Combo> combos = new HashMap<String, Combo>();
         combos.put("constructionYear", (Combo) XWT.findElementByName(container, "constructionYear"));
         return combos;
     }
 
     @Override
     public boolean isPageComplete() {
         // Mandatory: just one textfield
         Text description = getMandatoryWidgets().get("description");
         if (description.getText().length() == 0) {
             setErrorMessage(I18N.tr("Field is mandatory") + ": "
                     + I18N.tr(XWT.getElementName((Object) description)));
             return false;
         }
         // Optional
         HashMap<String, Text> optWidgets = getOptionalWidgets();
         // Sorted array
         Text[] optArray = { optWidgets.get("airPerformance"), optWidgets.get("motorPower"),
                 optWidgets.get("ventilatorPerformance"), optWidgets.get("motorRPM"),
                 optWidgets.get("current"), optWidgets.get("voltage") };
         for (int i = 0; i < optArray.length; i++) {
             if (!checkValidity(optArray[i])) {
                 setErrorMessage(I18N.tr("Invalid input for field") + " "
                         + I18N.tr(XWT.getElementName((Object) optArray[i])));
                 return false;
             }
         }
         setErrorMessage(null);
         return true;
     }
 
     private boolean checkValidity(Text textField) {
         if (textField.getText().length() == 0) {
             return true;
         }
         String textFieldName = XWT.getElementName((Object) textField);
         boolean isInvalidNumber = (textFieldName.equals("airPerformance")
                 || textFieldName.equals("motorPower")
                 || textFieldName.equals("ventilatorPerformance")
                 || textFieldName.equals("motorRPM") || textFieldName.equals("current") || textFieldName
                     .equals("voltage")) && !numberVal.isValid(textField.getText());
         return !isInvalidNumber;
     }
 
     public void setKeyListener() {
         KeyListener listener = new KeyListener() {
 
             @Override
             public void keyPressed(KeyEvent e) {
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
                 getWizard().getContainer().updateButtons();
             }
         };
         addListenerToWidgets(getMandatoryWidgets(), listener);
         addListenerToWidgets(getOptionalWidgets(), listener);
     }
 
     private void addListenerToWidgets(HashMap<String, Text> widgets, KeyListener listener) {
         for (Text text : widgets.values()) {
             text.addKeyListener(listener);
         }
     }
 
     private void translate(Composite container) {
         // Sections
         setSectionText(container, "Mandatory", I18N.tr("Mandatory"));
         setSectionText(container, "Optional", I18N.tr("Optional"));
         // Labels
         setLabelText(container, "lblDescription", I18N.tr("Description") + ":");
         setLabelText(container, "lblScheme", I18N.tr("Scheme") + ":");
         setLabelText(container, "lblManufactor", I18N.tr("Manufactor") + ":");
         setLabelText(container, "lblConstructionYear", I18N.tr("Construction Year") + ":");
         setLabelText(container, "lblType", I18N.tr("Type") + ":");
         setLabelText(container, "lblAirPerformance", I18N.tr("Air Performance") + ":");
         setLabelText(container, "lblMotorPower", I18N.tr("Motor Power") + ":");
         setLabelText(container, "lblVentilatorPerformance", I18N.tr("Ventilator Performance") + ":");
         setLabelText(container, "lblMotorRPM", I18N.tr("Motor RPM") + ":");
         setLabelText(container, "lblCurrent", I18N.tr("Current") + ":");
         setLabelText(container, "lblVoltage", I18N.tr("Voltage") + ":");
         setLabelText(container, "lblNote", I18N.tr("Notes") + ":");
     }
 
     private void setLabelText(Composite container, String labelName, String text) {
         Label l = (Label) XWT.findElementByName(container, labelName);
         if (l == null) {
             LOG.error("Label '" + labelName + "' not found.");
             return;
         }
         l.setText(text);
     }
 
     private void setSectionText(Composite container, String sectionName, String text) {
         Section s = (Section) XWT.findElementByName(container, sectionName);
         if (s == null) {
             LOG.error("Section '" + sectionName + "' not found.");
             return;
         }
         s.setText(text);
     }
 
 }
