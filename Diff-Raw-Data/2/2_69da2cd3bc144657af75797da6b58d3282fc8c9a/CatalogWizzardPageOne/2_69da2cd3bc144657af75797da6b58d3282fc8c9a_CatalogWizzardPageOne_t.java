 package de.hswt.hrm.catalog.ui.wizzard;
 
 import java.net.URL;
 import java.util.HashMap;
 
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.e4.xwt.forms.XWTForms;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.Section;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.catalog.model.Activity;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.catalog.model.ICatalogItem;
 import de.hswt.hrm.catalog.model.Target;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.PageContainerFillLayout;
 import de.hswt.hrm.i18n.I18n;
 import de.hswt.hrm.i18n.I18nFactory;
 
 public class CatalogWizzardPageOne extends WizardPage {
 
     private static final Logger LOG = LoggerFactory.getLogger(CatalogWizzardPageOne.class);
     private static final I18n I18N = I18nFactory.getI18n(CatalogWizzardPageOne.class);
 
     private Composite container;
     private HashMap<String, Text> textFields;
     private HashMap<String, Button> buttons;
     private Optional<ICatalogItem> item;
 
     public CatalogWizzardPageOne(String pageName, Optional<ICatalogItem> item) {
         super(pageName);
         this.item = item;
         setDescription(createDiscription());
         setTitle(I18N.tr("Catalog Wizard"));
     }
 
     public void createControl(Composite parent) {
         parent.setLayout(new PageContainerFillLayout());
         URL url = CatalogWizzardPageOne.class.getClassLoader().getResource(
                "de/hswt/hrm/catalog/ui/xwt/CatalogWizardWindow" + IConstants.XWT_EXTENSION_SUFFIX);
 
         try {
             container = (Composite) XWTForms.load(parent, url);
         }
         catch (Exception e) {
             LOG.error("Coult not load Wizzard XWT file.", e);
             return;
         }
 
         if (item.isPresent()) {
             updateFields(item.get());
         }
         FormUtil.initSectionColors((Section) XWT.findElementByName(container, "Mandatory"));
         setKeyListener();
         setControl(container);
         setPageComplete(false);
 
     }
 
     public void setKeyListener() {
         HashMap<String, Text> widgets = getTextWidgets();
         HashMap<String, Button> buttons = getButtons();
         for (Text text : widgets.values()) {
 
             text.addKeyListener(new KeyListener() {
 
                 @Override
                 public void keyPressed(KeyEvent e) {
                 }
 
                 @Override
                 public void keyReleased(KeyEvent e) {
                     getWizard().getContainer().updateButtons();
                 }
             });
         }
 
         for (Button b : buttons.values()) {
             b.addSelectionListener(new SelectionListener() {
 
                 @Override
                 public void widgetSelected(SelectionEvent e) {
                     getWizard().getContainer().updateButtons();
 
                 }
 
                 @Override
                 public void widgetDefaultSelected(SelectionEvent e) {
                     // TODO Auto-generated method stub
 
                 }
             });
         }
 
     }
 
     private HashMap<String, Text> getTextWidgets() {
         if (textFields == null) {
             textFields = new HashMap<>();
             textFields.put(Fields.NAME, (Text) XWT.findElementByName(container, Fields.NAME));
             textFields.put(Fields.DESCRIPTION,
                     (Text) XWT.findElementByName(container, Fields.DESCRIPTION));
         }
 
         return textFields;
     }
 
     private HashMap<String, Button> getButtons() {
         if (buttons == null) {
             buttons = new HashMap<>();
             buttons.put(Fields.ACTIVITY, (Button) XWT.findElementByName(container, Fields.ACTIVITY));
             buttons.put(Fields.CURRENT, (Button) XWT.findElementByName(container, Fields.CURRENT));
             buttons.put(Fields.TARGET, (Button) XWT.findElementByName(container, Fields.TARGET));
         }
 
         return buttons;
     }
 
     private String createDiscription() {
         if (item.isPresent()) {
             return I18N.tr("Edit a catalog.");
         }
 
         return I18N.tr("Add a new catalog");
     }
 
     public ICatalogItem getItem() {
 
         return updateItem(item);
     }
 
     private ICatalogItem updateItem(Optional<ICatalogItem> item) {
 
         HashMap<String, Text> w = getTextWidgets();
         HashMap<String, Button> b = getButtons();
         Text name = (Text) w.get(Fields.NAME);
         String s = name.getText();
         Text desc = (Text) w.get(Fields.DESCRIPTION);
         String s2 = desc.getText();
         ICatalogItem ic = null;
 
         for (Button bu : b.values()) {
             if (bu.getSelection() && bu.getText().equalsIgnoreCase("maßnahme")) {
                 if (item.isPresent()) {
                     ic = item.get();
                     Activity a = (Activity) ic;
                     a.setName(s);
                     a.setText(s2);
                     return a;
                 }
                 else {
                     return new Activity(s, s2);
 
                 }
             }
             else if (bu.getSelection() && bu.getText().equalsIgnoreCase("soll")) {
                 if (item.isPresent()) {
                     ic = item.get();
                     Target t = (Target) ic;
                     t.setName(s);
                     t.setText(s2);
                     return t;
                 }
                 else {
                     return new Target(s, s2);
 
                 }
             }
             else if (bu.getSelection() && bu.getText().equalsIgnoreCase("ist")) {
                 if (item.isPresent()) {
                     ic = item.get();
                     Current c = (Current) ic;
                     c.setName(s);
                     c.setText(s2);
                     return c;
                 }
                 else {
                     return new Current(s, s2);
 
                 }
             }
         }
 
         return null;
 
     }
 
     @Override
     public boolean isPageComplete() {
 
         boolean oneButtonisSelected = false;
 
         for (Button b : getButtons().values()) {
             if (b.getSelection()) {
                 oneButtonisSelected = b.getSelection();
             }
         }
 
         if (!oneButtonisSelected) {
             setErrorMessage("Soll/Ist/Maßnahme muss ausgewählt sein.");
             return false;
         }
 
         else if (oneButtonisSelected) {
             for (Text textField : getTextWidgets().values()) {
                 if (textField.getText().length() == 0) {
                     setErrorMessage("Feld \""+textField.getToolTipText()+"\" darf nicht leer sein."); 
                     return false;
                 }
             }
             setErrorMessage(null);
             return true;
         }
         setErrorMessage(null);
         return true;
 
     }
 
     private void updateFields(ICatalogItem i) {
         HashMap<String, Text> widgets = getTextWidgets();
         widgets.get(Fields.NAME).setText(i.getName());
         widgets.get(Fields.DESCRIPTION).setText(i.getText());
 
         HashMap<String, Button> buttons = getButtons();
 
         buttons.get(Fields.ACTIVITY).setEnabled(false);
         buttons.get(Fields.CURRENT).setEnabled(false);
         buttons.get(Fields.TARGET).setEnabled(false);
 
         if (i instanceof Activity) {
             buttons.get(Fields.ACTIVITY).setSelection(true);
         }
         else if (i instanceof Current) {
             buttons.get(Fields.CURRENT).setSelection(true);
         }
         else if (i instanceof Target) {
             buttons.get(Fields.TARGET).setSelection(true);
         }
 
     }
 
     private static final class Fields {
         public static final String CURRENT = "current";
         public static final String TARGET = "target";
         public static final String ACTIVITY = "activity";
         public static final String NAME = "name";
         public static final String DESCRIPTION = "desc";
 
     }
 }
