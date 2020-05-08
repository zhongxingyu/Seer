 package com.preppa.web.components.questiontypes.gridin;
 
 import com.preppa.web.data.GridinDAO;
 import com.preppa.web.entities.Gridin;
 import com.preppa.web.entities.GridinAnswer;
 import com.preppa.web.entities.User;
 import java.io.File;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.tapestry5.Block;
 import org.apache.tapestry5.annotations.ApplicationState;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.InjectComponent;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Mixins;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.corelib.components.Radio;
 import org.apache.tapestry5.corelib.components.RadioGroup;
 import org.apache.tapestry5.corelib.components.Zone;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.Context;
 import org.apache.tapestry5.upload.components.Upload;
 import org.apache.tapestry5.upload.services.UploadedFile;
 import org.chenillekit.tapestry.core.components.Editor;
 import org.springframework.security.annotation.Secured;
 
 /**
  *
  * @author nwt
  */
 @Secured("ROLE_USER")
 public class NewGridin {
 
     @ApplicationState
     private User user;
     @Inject
     private GridinDAO gridinDAO;
     @Property
     private Gridin question;
     @Property
     private String fTitle;
     @Component
     private Form gridinForm;
     @Property
     private String fQuestion;
     @Component(parameters = {"value=fQuestion"})
     private Editor questionBody;
     private List<GridinAnswer> answers;
     @Property
     private String fRangehigh;
     @Property
     private String fRangelow;
     @Component
     private Form rangeform;
     @Component
     private Form singleform;
     @Component
     private RadioGroup chooserange;
     @Component(parameters = {"event=onclick"})
     @Mixins({"ck/OnEvent"})
     private Radio yesradio;
     @Component(parameters = {"event=onclick"})
     @Mixins({"ck/OnEvent"})
     private Radio noradio;
     @Component
     private RadioGroup chooseimage;
     @Component(parameters = {"event=onclick"})
     @Mixins({"ck/OnEvent"})
     private Radio yesimage;
     @Component(parameters = {"event=onclick"})
     @Mixins({"ck/OnEvent"})
     private Radio noimage;
     @Property
     private String answertype;
     @InjectComponent
     private Zone rangezone;
     @Inject
     @Property
     private Block rangeblock;
     @Property
     private String fSingle;
     @Inject
     private Block testblock;
     @Property
     private String fAnswer;
     @Property
     private String fDescription;
     @Property
     private String hasimage;
     @Property
     private UploadedFile imageupload;
     @Component
     private Upload upload;
     @Inject
     private Context context;
     @InjectPage
     private com.preppa.web.pages.contribution.gridin.ShowGridin showgridin;
 
 
     public void NewGridin() {
         this.question = new Gridin();
     }
 
     void onValidateFormFromGridinForm() {
         System.out.println("Validating " + answertype);
         if (answertype == null) {
             gridinForm.recordError("You need to select an answer");
         } else {
             if ((answertype.equals("single")) && (fAnswer == null)) {
                 gridinForm.recordError("Please enter an answer!");
             }
             if ((answertype.equals("range")) && ((fRangehigh == null) || fRangelow == null)) {
                 gridinForm.recordError("Please Enter the ranges for your answer below!");
             }
 
         }
 
 
     }
 
     @CommitAfter
     Object onSuccessFromGridinForm() {
         question.setTitle(fTitle);
         question.setQuestion(fQuestion);
         question.setUser(user);
         question.setUpdatedBy(user);
 
 
 
         GridinAnswer a = new GridinAnswer();
         if (answertype.equals("range")) {
 
             a.setRange(true);
             a.setDescription(fDescription);
             a.setHighAnswer(fRangehigh);
             a.setLowAnswer(fRangelow);
         } else {
             a.setRange(false);
             a.setAnswer(fAnswer);
             a.setDescription(fDescription);
         }
         if (question.getAnswers() == null) {
             List<GridinAnswer> answerlist = new ArrayList<GridinAnswer>();
             answerlist.add(a);
             question.setAnswers(answerlist);
         } else {
             question.getAnswers().add(a);
         }
 
          Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
         question.setUpdatedAt(now);
         question.setCreatedAt(now);
         gridinDAO.doSave(question);
         if(hasimage.equals("yes")) {
            String impath = context.getRealFile("/").getPath() + "/images/gridin" + question.getId() + ".jpg";
             System.out.println(impath);
             File copied = new File( context.getRealFile("/").getPath() + "/images/gridin"  + question.getId() + ".jpg");
             imageupload.write(copied);
             question.setImagePath(impath);
             question.setImage(Boolean.TRUE);
         }
         showgridin.setGridin(question);
         return showgridin;
     }
 
     Block onClickFromYesRadio() {
         System.out.println("Yes has been clicked");
 
         return rangeblock;
     }
 
     Block onActionFromTestLink() {
         return testblock;
     }
 }
