 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.preppa.web.components.questiontypes.multichoice;
 
 import com.preppa.web.data.FlagDAO;
 import com.preppa.web.data.LongDualPassageDAO;
 import com.preppa.web.data.QuestionDAO;
 import com.preppa.web.data.UserObDAO;
import com.preppa.web.data.VoteDAO;
 import com.preppa.web.entities.Flag;
 import com.preppa.web.entities.LongDualPassage;
 import com.preppa.web.entities.Question;
 import com.preppa.web.entities.QuestionAnswer;
 import com.preppa.web.entities.Tag;
 import com.preppa.web.entities.User;
 import com.preppa.web.pages.Index;
import com.preppa.web.utils.Constants;
 import com.preppa.web.utils.ContentFlag;
 import com.preppa.web.utils.ContentType;
 import com.preppa.web.utils.FlagStatus;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import org.apache.tapestry5.Block;
 import org.apache.tapestry5.annotations.ApplicationState;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
 import org.apache.tapestry5.annotations.IncludeStylesheet;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.SetupRender;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.springframework.security.annotation.Secured;
 
 /**
  *
  * @author newtonik
  */
 @IncludeStylesheet(value = {"context:styles/flag.css"})
 @IncludeJavaScriptLibrary(value = {"context:js/showquestion.js"})
 public class ShowMultiChoice {
 
     @ApplicationState
     private User user;
     @Property
     @Parameter(required = true)
     private Question question;
     @Inject
     private QuestionDAO questionDAO;
     @Property
     private String example;
     @InjectPage
     private Index index;
     private Integer qid;
     @Property
     private QuestionAnswer questionanswer;
     @Parameter
     private Integer pid;
     @Inject
     private LongDualPassageDAO passageDAO;
     @Property
     private List<Question> allQuestions;
     private LongDualPassage passage;
     private List<QuestionAnswer> returnVal;
     @Property
     private List<Tag> tags;
     @Property
     private User author;
     /* Question Flags */
     @Component
     private Form flagform;
     @Inject
     private Block flagresponse;
     private List<Flag> questionflags;
     @Inject
     @Property
     private Block flagblock;
     @Property
     private String reason;
     @Property
     private String reasonDesc;
     @Inject
     private FlagDAO flagDAO;
     @Inject
     private UserObDAO userDAO;
     @Property
     private ContentType contType;
     @Property
     private Integer votes;
    @Property
    private Integer votecount;
    @Inject
    private VoteDAO voteDAO;
    @Property
    private boolean isApproved;
 
     @SetupRender
     void intializeQuestion() {
         if (question != null) {
             this.question = questionDAO.doRetrieve(question.getId(), false);
             returnVal = question.getChoices();
             tags = question.getTaglist();
             author = question.getUser();
             questionflags = question.getFlags();
             contType = ContentType.Question;
            votecount = voteDAO.findVoteByContentId(contType, question.getId());
            if (votecount >= Constants.getApprovalThreshhold()) {
                isApproved = true;
            }
            else {
                isApproved = false;
            }
         }
        
     }
 
     public List<QuestionAnswer> getAllAnswers() {
 
         if (returnVal == null) {
             returnVal = question.getChoices();
         }
         return returnVal;
     }
 
     void setquestion(Question question) {
         this.question = question;
         this.qid = question.getId();
     }
 
     @CommitAfter
     @Secured("ROLE_USER")
     Block onSuccessFromFlagForm() {
         if (reason != null) {
             Flag f = new Flag();
             if (reason.equals("A")) {
                 f.setFlagtype(ContentFlag.Inappropriate);
             } else if (reason.equals("B")) {
                 f.setFlagtype(ContentFlag.Spam);
             } else if (reason.equals("C")) {
                 f.setFlagtype(ContentFlag.Attention);
             } else if (reason.equals("D")) {
                 f.setFlagtype(ContentFlag.Incorrect);
             } else if (reason.equals("E")) {
 
                 f.setFlagtype(ContentFlag.Copyright);
             } else {
                 System.out.println(reason);
                 f.setFlagtype(ContentFlag.Attention);
             }
 
             f.setDescription(reasonDesc);
             f.setContentType(ContentType.Question);
 
             author = userDAO.doRetrieve(user.getId(), false);
             System.out.println("author id is " + user.getId());
             f.setFlagger(author);
             f.setStatus(FlagStatus.NEW);
             f.setQuestion(question);
 
 
 
             if (questionflags == null) {
                 questionflags = new ArrayList<Flag>();
                 questionflags.add(f);
                 question.setFlags(questionflags);
 
             } else {
                 //questionflags.add(f);
                 question.getFlags().add(f);
 
             }
             Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
 
             f.setUpdatedAt(now);
             f.setCreatedAt(now);
 
             //flagDAO.doSave(f);
             //question.setUpdatedAt(now);
             questionDAO.doSave(question);
 
         }
 
         return flagresponse;
     }
 
     Block onActionFromRemoveFlagBox() {
         return null;
     }
 
     Block onActionFromCloseFlagBlock() {
         return flagblock;
     }
 
     public Date getUpdatedAt() {
                 return question.getUpdatedAt(); 
      }
 
 }
