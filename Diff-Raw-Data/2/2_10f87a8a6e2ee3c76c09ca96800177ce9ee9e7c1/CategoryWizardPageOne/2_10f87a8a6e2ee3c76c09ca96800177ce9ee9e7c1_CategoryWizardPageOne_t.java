 package de.hswt.hrm.component.ui.wizard;
 
 import java.net.URL;
 import java.util.HashMap;
 
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.e4.xwt.forms.XWTForms;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.component.model.Category;
 
 public class CategoryWizardPageOne extends WizardPage {
     
     private static final Logger LOG = LoggerFactory.getLogger(CategoryWizardPageOne.class);
     
     private Composite container;
     private Optional<Category> category;
     
     public CategoryWizardPageOne(String title, Optional<Category> category) {
         super(title);
         this.category = category;
         setDescription(createDescription());
     }
     
     private String createDescription() {
         if (category.isPresent()) {
             return "Kategorie bearbeiten";
         }
         return "Neue Kategorie erstellen";
     }
 
     public void createControl(Composite parent) {
         URL url = CategoryWizardPageOne.class.getClassLoader().getResource(
                "de/hswt/hrm/component/ui/xwt/CategoryWizardWindow"+IConstants.XWT_EXTENSION_SUFFIX);
         try {
             container = (Composite) XWTForms.load(parent, url);
         } catch (Exception e) {
             LOG.error("An error occured: ",e);
         }
         if (this.category.isPresent()) {
             updateFields(container);
         }
         setControl(container);
         setKeyListener();
         setPageComplete(false);
     }
     
     private void updateFields(Composite c) {
         Category cat = category.get();
         Text t = (Text) XWT.findElementByName(c, "name");
         t.setText(cat.getName());
         t = (Text) XWT.findElementByName(c, "defaultQuantifier");
         t.setText(String.valueOf(cat.getDefaultQuantifier()));
         Button cb = (Button) XWT.findElementByName(c, "defaultBoolRating");
         cb.setSelection(cat.getDefaultBoolRating());
         t = (Text) XWT.findElementByName(c, "width");
         t.setText(String.valueOf(cat.getWidth()));
         t = (Text) XWT.findElementByName(c, "height");
         t.setText(String.valueOf(cat.getHeight()));
     }
     
     public HashMap<String, Text> getMandatoryWidgets() {
         HashMap<String, Text> widgets = new HashMap<String, Text>();
         widgets.put("name", (Text) XWT.findElementByName(container, "name"));
         widgets.put("defaultQuantifier", (Text) XWT.findElementByName(container, "defaultQuantifier"));
         widgets.put("width", (Text) XWT.findElementByName(container, "width"));
         widgets.put("height", (Text) XWT.findElementByName(container, "height"));
         return widgets;
     }
     
     public Button getBoolRatingCheckbox() {
         return (Button) XWT.findElementByName(container, "defaultBoolRating");
     }
     
     public boolean isPageComplete() {
         for (Text textField : getMandatoryWidgets().values()) {
             if (textField.getText().length() == 0) {
                 return false;
             }
         }
         return true;
     }
     
     public void setKeyListener() {
         for (Text text : getMandatoryWidgets().values()) {
             text.addKeyListener(new KeyListener() {
 
                 public void keyPressed(KeyEvent e) {
   
                 }
 
                 public void keyReleased(KeyEvent e) {
                     getWizard().getContainer().updateButtons();
                     
                 }
                 
             });
         }
     }
 
 }
