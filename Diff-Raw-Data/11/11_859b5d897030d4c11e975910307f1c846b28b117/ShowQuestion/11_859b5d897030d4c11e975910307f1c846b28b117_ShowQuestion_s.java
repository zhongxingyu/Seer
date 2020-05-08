 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.preppa.web.pages.contribution.question;
 
 import com.preppa.web.components.questiontypes.multichoice.ShowMultiChoice;
 import com.preppa.web.data.QuestionDAO;
 import com.preppa.web.data.VoteDAO;
 import com.preppa.web.entities.Question;
 import com.preppa.web.entities.QuestionAnswer;
 import com.preppa.web.entities.User;
 import com.preppa.web.entities.Vote;
 import com.preppa.web.pages.Index;
 import com.preppa.web.utils.ContentType;
 import java.sql.Timestamp;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.tapestry5.Block;
 import org.apache.tapestry5.annotations.ApplicationState;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.InjectComponent;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.corelib.components.Zone;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.json.JSONObject;
 
 
 /**
  *
  * @author newtonik
  */
 public class ShowQuestion {
     @ApplicationState
     private User user;
     @Property
     private Question ques;
     @Component
     private ShowMultiChoice showquestion;
     @Inject
     private QuestionDAO questionDAO;
     @Property
     private String example;
     @InjectPage
     private Index index;
     private Integer qid;
     @Property
     @Persist
     private QuestionAnswer questionanswer;
     @Inject
     private VoteDAO voteDAO;
    @InjectComponent
     private Zone voteupZone;
     @Inject
    private Block voteBlock;
     @Property
     @Persist
     private Integer votes;
     @Inject
     private HttpServletRequest _request;
    @Inject
     private Block upSuccess;
     @Inject
     private Block downSuccess;
     @Inject
    private Block voted;
 
     void onActivate(int id) {
         if (id > 0) {
             this.ques = questionDAO.findById(id);
             if (this.ques != null) {
                 /*example = question.getSentence().getSentence();
                 vid = question.getId();*/
                 qid = id;
                 this.votes = voteDAO.findSumByQuestionId(ques.getId());
             } else {
                 // return index;
             }
         }
         //return this;
     }
 
     Integer onPassivate() {
         return this.qid;
     }
 
     public List<QuestionAnswer> getAllAnswers() {
         List<QuestionAnswer> returnVal;
 
         returnVal = ques.getChoices();
 
         return returnVal;
     }
 
     public void setquestion(Question question) {
         this.ques = question;
         this.qid = question.getId();
     }
 
     /*Block onActionFromVoteUp() {
      String  hostname = _request.getRemoteHost();
      if(!(voteDAO.checkVoted(ContentType.Question, ques.getId(), user)))
      {
          Vote v = new Vote();
          v.setContentId(ques.getId());
          v.setSource(hostname);
          if(user != null)
              v.setUser(user);
          
          v.setValue(1);
           v.setContentTypeId(ContentType.Question);
 
          Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
          v.setCreatedAt(now);
 
          voteDAO.doSave(v);
 
          JSONObject json = new JSONObject();
          json.put("vote", "down");
          //decrement the vote
          votes++;
 
          return upSuccess;
      }//return new TextStreamResponse("text/json", json.toString());
      else
      {
          return voted;
      }
  }
     Block onActionFromVoteDown() {
 
      String  hostname = _request.getRemoteHost();
     // System.out.println(_request.getRequestURL());
 
      if(!(voteDAO.checkVoted(ContentType.Question, ques.getId(), user)))
      {
          Vote v = new Vote();
          v.setContentId(ques.getId());
          v.setSource(hostname);
          if(user != null)
              v.setUser(user);
 
          v.setValue(-1);
          v.setContentTypeId(ContentType.Question);
 
          Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
          v.setCreatedAt(now);
 
          voteDAO.doSave(v);
          //update the vote
          votes--;
 
 
          JSONObject json = new JSONObject();
          json.put("vote", "down");
 
      //return new TextStreamResponse("text/json", json.toString());
          return downSuccess;
      }
      else
      {
          return voted;
      }
  }*/
 }
