 package de.hswt.hrm.place.ui.Wizard;
 
 import java.net.URL;
 import java.util.HashMap;
 
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.e4.xwt.forms.XWTForms;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 
 public class PlaceWizardPageOne extends WizardPage {
 	
 	private Composite container;
 
 
     protected PlaceWizardPageOne(String pageName) {
         super(pageName);
         setDescription(createDiscription());
     }
 
     private String createDiscription() {
         StringBuffer sb = new StringBuffer();
        sb.append("Neuen Standort hinzuf�gen");
         return sb.toString();
     }
 
     @Override
     public void createControl(Composite parent) {
     	
     	URL url = PlaceWizardPageOne.class.getClassLoader().getResource(
                 "de/hswt/hrm/place/ui/xwt/PlaceWizardWindow" + IConstants.XWT_EXTENSION_SUFFIX);
     	try {
     		container = (Composite) XWTForms.load(parent, url);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	setKeyListener();
         setControl(container);
         setPageComplete(false);
     }
 
     public HashMap<String,Text> getMandatoryWidgets() {
         HashMap<String, Text> widgets = new HashMap<String, Text>();
         widgets.put("name", (Text) XWT.findElementByName(container, "name"));
         widgets.put("street", (Text) XWT.findElementByName(container, "street"));
         widgets.put("streetNumber", (Text) XWT.findElementByName(container, "streetNumber"));
         widgets.put("zipCode", (Text) XWT.findElementByName(container, "zipCode"));
         widgets.put("city", (Text) XWT.findElementByName(container, "city"));
         widgets.put("location", (Text) XWT.findElementByName(container, "location"));
         widgets.put("area", (Text) XWT.findElementByName(container, "area"));
         
         return widgets; 
     }
     
     @Override
     public boolean isPageComplete(){
     	for(Text textField : getMandatoryWidgets().values()){
     		if(textField.getText().length() == 0){
     			return false;    			
     		}    		
     	}
     	return true;
     }
 
     public void setKeyListener() {
         HashMap<String,Text> widgets = getMandatoryWidgets();
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
     }
 }
