 package de.hswt.hrm.misc.ui.commentwizard;
 
 import java.net.URL;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.e4.xwt.forms.XWTForms;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.Section;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.PageContainerFillLayout;
 import de.hswt.hrm.i18n.I18n;
 import de.hswt.hrm.i18n.I18nFactory;
 import de.hswt.hrm.misc.comment.model.Comment;
 import de.hswt.hrm.misc.comment.service.CommentService;
 import de.hswt.hrm.summary.model.Summary;
 
 public class CommentWizardPageOne extends WizardPage {
 
     private Optional<Comment> comment;
     private Composite container;
     private Text nameText;
     private Text descText;
 
     private Collection<Comment> comments;
     private boolean first = true;
     @Inject
     private CommentService commentService;
 
     private static final Logger LOG = LoggerFactory.getLogger(CommentWizardPageOne.class);
     private static final I18n I18N = I18nFactory.getI18n(CommentWizardPageOne.class);
 
     public CommentWizardPageOne(String title, Optional<Comment> comment) {
         super(title);
         this.comment = comment;
         setDescription(createDescription());
         setTitle(I18N.tr("Comment Wizard"));
     }
 
     private String createDescription() {
         if (comment.isPresent()) {
             return I18N.tr("Edit a comment");
         }
         return I18N.tr("Add a new comment");
     }
 
     @Override
     public void createControl(Composite parent) {
         parent.setLayout(new PageContainerFillLayout());
         URL url = CommentWizardPageOne.class.getClassLoader().getResource(
                 "de/hswt/hrm/misc/ui/commentwizardxwt/CommentWizardWindow"
                         + IConstants.XWT_EXTENSION_SUFFIX);
         try {
             container = (Composite) XWTForms.load(parent, url);
         }
         catch (Exception e) {
             LOG.error("An error occured: ", e);
         }
 
         nameText = (Text) XWT.findElementByName(container, "name");
         descText = (Text) XWT.findElementByName(container, "desc");
         
         translate();
         
         if (this.comment.isPresent()) {
             updateFields();
         }
         try {
             this.comments = commentService.findAll();
         }
         catch (DatabaseException e) {
             LOG.error("An error occured", e);
         }
        
         
         FormUtil.initSectionColors((Section) XWT.findElementByName(container, "Mandatory"));
         addKeyListener(nameText);
         addKeyListener(descText);
         setControl(container);
 //        setPageComplete(false);
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
         Comment e = comment.get();
         nameText.setText(e.getName());
         descText.setText(e.getText());
     }
 
     private void checkPageComplete() {
 
         if (first && descText.getText().isEmpty() && nameText.getText().isEmpty()) {
             first = false;
             setPageComplete(false);
             return;
         }
 
 
         setErrorMessage(null);
 
         if (descText.getText().isEmpty()) {
             setErrorMessage(I18N.tr("Field is mandatory")+": "+I18N.tr("Description"));
         }
 
         else if (nameText.getText().isEmpty()) {
             setErrorMessage(I18N.tr("Field is mandatory")+": "+I18N.tr("Name"));
         }
 
         else if (isAlreadyPresent(nameText.getText())) {
            setErrorMessage(I18N.tr("A comment with name") + " '"+ nameText.getText()+ "' " + I18N.tr("is already present."));
         }
 
     }
 
     private boolean isAlreadyPresent(String text) {
 
         boolean present = false;
 
         if (text == null | text.isEmpty()) {
             present = true;
         }
 
         if(!comment.isPresent()){
 	        for (Comment e : this.comments) {
 	            if (e.getName().equals(text)) {
 	                present = true;
 	            }
 	        }
         }else{
         	for (Comment e : this.comments) {
 	            if (e.getName().equals(text) && e.getId() != comment.get().getId()) {
 	                present = true;
 	            }
 	        }     	
         	
         }
         return present;
 
     }
     
     private void translate() {
         // Section
         setSectionText("Mandatory", I18N.tr("Comment"));
         // Labels
         setLabelText("lblName", I18N.tr("Name"));
         setLabelText("text", I18N.tr("Text"));
     }
     
     private void setSectionText (String sectionName, String text) {
         Section s = (Section) XWT.findElementByName(container, sectionName);
         if (s == null) {
             LOG.error("Section '" + sectionName + "' not found.");
             return;
         }
         s.setText(text);
     }
     
     private void setLabelText(String labelName, String text) {
         Label l = (Label) XWT.findElementByName(container, labelName);
         if (l == null) {
             LOG.error("Label '" + labelName + "' not found.");
             return;
         }
         l.setText(text);
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
