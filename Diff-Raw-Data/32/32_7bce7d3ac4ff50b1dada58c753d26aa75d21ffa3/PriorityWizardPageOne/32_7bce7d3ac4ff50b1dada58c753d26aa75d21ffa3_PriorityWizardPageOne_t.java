 package de.hswt.hrm.misc.ui.prioritywizard;
 
 import java.net.URL;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.e4.xwt.forms.XWTForms;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.Section;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.PageContainerFillLayout;
 import de.hswt.hrm.misc.comment.model.Comment;
 import de.hswt.hrm.misc.priority.model.Priority;
 import de.hswt.hrm.summary.model.Summary;
 import de.hswt.hrm.summary.service.SummaryService;
 
 public class PriorityWizardPageOne extends WizardPage {
 
     private Optional<Priority> priority;
     private Composite container;
     private Text nameText;
     private Text descText;
 
     private Collection<Priority> priorities;
     private boolean first = true;
 
 //    @Inject
 //    private PriorityService prioService;
 
     private static final Logger LOG = LoggerFactory.getLogger(PriorityWizardPageOne.class);
 
     public PriorityWizardPageOne(String title, Optional<Priority> priority) {
         super(title);
         this.priority = priority;
         setDescription(createDescription());
         setTitle("Priority Wizard");
     }
 
     private String createDescription() {
         if (priority.isPresent()) {
             return "Change a Priority";
         }
         return "Add a new Priority";
     }
 
     @Override
     public void createControl(Composite parent) {
         parent.setLayout(new PageContainerFillLayout());
         URL url = PriorityWizardPageOne.class.getClassLoader().getResource(
                 "de/hswt/hrm/misc/ui/prioritywizard/PriorityWizardWindow"
                         + IConstants.XWT_EXTENSION_SUFFIX);
         try {
             container = (Composite) XWTForms.load(parent, url);
         }
         catch (Exception e) {
             LOG.error("An error occured: ", e);
         }
 
         nameText = (Text) XWT.findElementByName(container, "name");
         descText = (Text) XWT.findElementByName(container, "desc");
         
 
         if (this.priority.isPresent()) {
             updateFields();
         }
 //  TODO      try {
 //            this.priorities = prioService.findAll();
 //        }
 //        catch (DatabaseException e) {
 //            LOG.error("An error occured", e);
 //        }
        
         
         FormUtil.initSectionColors((Section) XWT.findElementByName(container, "Mandatory"));
         addKeyListener(nameText);
         addKeyListener(descText);
         setControl(container);
       // setPageComplete(false);
        checkPageComplete();
 
     }
 
     private void addKeyListener(Text text) {
         text.addKeyListener(new KeyListener() {
 
             @Override
             public void keyReleased(KeyEvent e) {
                 checkPageComplete();
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
             }
         });
     }
 
     private void updateFields() {
         Priority e = priority.get();
         nameText.setText(e.getName());
         descText.setText(e.getText());
     }
 
     private void checkPageComplete() {
 
//        if (first) {
//            first = false;
//            setPageComplete(false);
//            return;
//        }
 
         setErrorMessage(null);
 
         if (descText.getText().isEmpty()) {
             setErrorMessage("Description must not be empty");
         }
 
         else if (nameText.getText().isEmpty()) {
             setErrorMessage("Name must not be empty...");
         }
 
         else if (isAlreadyPresent(nameText.getText())) {
             setErrorMessage("A Priority with name " + nameText.getText() + " is already present");
         }
 
     }
 
     private boolean isAlreadyPresent(String text) {
 
         boolean present = false;
 
         if (text == null | text.isEmpty()) {
             present = true;
         }
 
        if(!priority.isPresent()){
	        for (Priority e : this.priorities) {
	            if (e.getName().equals(text)) {
	                present = true;
	            }
	        }
        }else{
        	for (Priority e : this.priorities) {
	            if (e.getName().equals(text) && e.getId() != priority.get().getId()) {
	                present = true;
	            }
	        }     	
        	
         }
 
         return present;
 
     }
 
     @Override
     public void setErrorMessage(String newMessage) {
         if (newMessage == null || newMessage.isEmpty()) {
             setPageComplete(true);
         }
         else {
             setPageComplete(false);
         }
         super.setErrorMessage(newMessage);
     }
 
     public String getName() {
         return nameText.getText();
     }
 
     public String getDesc() {
         return descText.getText();
     }
 
 }
