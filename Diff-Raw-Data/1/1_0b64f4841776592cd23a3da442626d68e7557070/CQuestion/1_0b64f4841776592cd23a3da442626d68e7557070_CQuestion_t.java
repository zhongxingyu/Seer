 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.preppa.web.components;
 
 import com.preppa.web.data.LongDualPassageDAO;
 import com.preppa.web.data.LongPassageDAO;
 import com.preppa.web.data.QuestionDAO;
 import com.preppa.web.data.ShortDualPassageDAO;
 import com.preppa.web.data.ShortPassageDAO;
 import com.preppa.web.data.TagDAO;
 import com.preppa.web.entities.LongDualPassage;
 import com.preppa.web.entities.LongPassage;
 import com.preppa.web.entities.Question;
 import com.preppa.web.entities.QuestionAnswer;
 import com.preppa.web.entities.ShortDualPassage;
 import com.preppa.web.entities.ShortPassage;
 import com.preppa.web.entities.Tag;
 import com.preppa.web.pages.Index;
 import com.preppa.web.pages.contribution.question.ShowQuestion;
 import java.sql.Timestamp;
 import java.util.LinkedList;
 import java.util.List;
 import org.apache.tapestry5.FieldTranslator;
 import org.apache.tapestry5.MarkupWriter;
 import org.apache.tapestry5.ValidationException;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.corelib.components.Checkbox;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.chenillekit.tapestry.core.components.Editor;
 import org.chenillekit.tapestry.core.components.RatingField;
 import org.chenillekit.tapestry.core.components.prototype_ui.AutoComplete;
 import org.slf4j.Logger;
 
 /**
  *
  * @author nwt
  */
 public class CQuestion {
     @Property
     private Question question;
     @Inject
     private QuestionDAO questionDAO;
     @Component(parameters = {"value=fQuestion"})
     private Editor questioneditor;
     @InjectPage
     private Index indexpage;
     @Property
     private String fQuestion;
     @Property
     private String fExplanation;
     @Property
     private String fTag;
     @Component(parameters = {"value=ans1"})
     private Editor choice1;
     @Component(parameters = {"value=ans2"})
     private Editor choice2;
     @Component(parameters = {"value=ans3"})
     private Editor choice3;
     @Component(parameters = {"value=ans4"})
     private Editor choice4;
     @Component(parameters = {"value=ans5"})
     private Editor choice5;
     @Component
     private RatingField ratingField;
     @Property
     private Integer ratingValue;
     @Property
     private String ans1;
     @Property
     private String ans2;
     @Property
     private String ans3;
     @Property
     private String ans4;
     @Property
     private String ans5;
     @Property
     private Boolean c1;
     @Property
     private Boolean c2;
     @Property
     private Boolean c3;
     @Property
     private Boolean c4;
     @Property
     private Boolean c5;
     @Property
     private String correct;
      @Property
     private List<Tag> addedTags = new LinkedList<Tag>();
      @Inject
      private TagDAO tagDAO;
     @Component
     private AutoComplete autoCompleteTag;
     @Parameter
     private LongPassage longpassage;
     @Inject
     private LongDualPassageDAO longdualpassageDAO;
     @Inject
     private ShortPassageDAO shortpassageDAO;
     @Inject
     private ShortDualPassageDAO shortdualpassageDAO;
     @Inject
     private LongPassageDAO longpassageDAO;
     @Parameter
     private boolean newquestion;
     @Parameter
     private Object owner;
     @InjectPage
     private ShowQuestion show;
     @Inject
     private Logger logger;
     private boolean showpage = false;
     private ShortPassage shortpassage;
     private ShortDualPassage shortdualpassage;
     private LongDualPassage longdualpassage;
     @Property
     private Boolean mywork;
     @Component
     private Form createquestionform;
 
     public void setPageTrue() {
         showpage = true;
     }
 
     public void setPageFalse() {
         showpage = false;
     }
 
     void CreateQuestion() {
         question = new Question();
          newquestion = true;
     }
 
     void onActivate() {
         question = new Question();
     }
     Question getSubmittedQuestion() {
         return this.question;
     }
 //    void addToPassage(LongDualPassage passage) {
 //        if(passage != null) {
 //            passage.getQuestions().add(this.question);
 //        }
 //    }
     Object onPassivate() {
         return question;
     }
     void onSubmitForm() {
         if(!mywork) {
             createquestionform.recordError("You cannot submit a question to Preppa, that isn't your own work.");
         }
     }
 
     void onValidateForm() throws ValidationException {
         System.out.println("Mywork is " + mywork);
         if(mywork == false) {
             System.out.println("I'm here " + mywork);
             throw new ValidationException("You must verify that this is your own work.");
             //createquestionform.recordError("You cannot submit a question to Preppa, that isn't your own work.");
         }
         if ((c1 == null && c2 == null && c3 == null && c4 ==null && c5 == null)) {
             createquestionform.recordError("You did not specify an answer.");
         }
     }
 
     @CommitAfter
     Object onSuccess(){
     question = new Question();
     question.setExplanation(fExplanation);
     question.setQuestion(fQuestion);
     int numCorrect = 0;
     if(ans1.length() > 0) {
         QuestionAnswer ch = new QuestionAnswer(ans1);
         question.getChoices().add(ch);
 
 
     }
         if(ans2.length() > 0) {
         QuestionAnswer ch = new QuestionAnswer(ans2);
         question.getChoices().add(ch);
     }
         if(ans3.length() > 0) {
         QuestionAnswer ch = new QuestionAnswer(ans3);
         question.getChoices().add(ch);
     }
         if(ans4.length() > 0) {
         QuestionAnswer ch = new QuestionAnswer(ans4);
         question.getChoices().add(ch);
     }
         if(ans5.length() > 0) {
         QuestionAnswer ch = new QuestionAnswer(ans5);
         question.getChoices().add(ch);
     }
     
      for(Tag t: addedTags)
      {
             if(!(question.getTaglist().contains(t)))
             {
                 question.getTaglist().add(t);
             }
      }
     question.setCorrectAnswer(correct);
       numCorrect = 1;
      question.setNumCorrect(numCorrect);
      question.setDifficulty(ratingValue);
      Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
      question.setCreatedAt(now);
      question.setUpdatedAt(now);
      newquestion = true;
      
      if(owner != null)
      {
         if(!saveQuestionToObject(owner, question))
         {
             logger.debug("Just saving the question, object is null");
             questionDAO.doSave(question);
         }
      }
      else {
          questionDAO.doSave(question);
      }
 
      if (showpage == false) {
          return null;
      }
      else {
         show.setquestion(this.question);
         return show;
      }
     }
      List<Tag> onProvideCompletionsFromAutocompleteTag(String partial) {
         List<Tag> matches = tagDAO.findByPartialName(partial);
         return matches;
 
     }
         public FieldTranslator getTagTranslator()
     {
         return new FieldTranslator<Tag>()
         {
             @Override
           public String toClient(Tag value)
           {
                 String clientValue = "0";
                 if (value != null)
                 clientValue = String.valueOf(value.getName());
 
                 return clientValue;
           }
 
             @Override
           public void render(MarkupWriter writer) { }
 
             @Override
           public Class<Tag> getType() { return Tag.class; }
 
             @Override
           public Tag parse(String clientValue) throws ValidationException
           {
             Tag serverValue = null;
 //            if(clientValue == null) {
 //                Tag t = new Tag();
 //                t.setName(clientValue);
 //            }
             System.out.println(clientValue);
 
             if (clientValue != null && clientValue.length() > 0 && !clientValue.equals("0")) {
                 System.out.println(clientValue);
                 serverValue = tagDAO.findByName(clientValue).get(0);
             }
             return serverValue;
           }
 
     };
    }
         /**
          *This function determines the object passed to the component then saves a question to it.
          * @param toSave
          * @param questiontoSave
          * @return
          */
         @CommitAfter
    Boolean saveQuestionToObject(Object toSave, Question questiontoSave) {
        if(toSave instanceof LongPassage) {
           longpassage = (LongPassage)toSave;
             longpassage = longpassageDAO.findById(longpassage.getId());
             longpassage.getQuestions().add(question);
             logger.debug("Object to save is long passage");
             return true;
        }
        else if(toSave instanceof ShortPassage)
        {
            shortpassage = (ShortPassage)toSave;
            shortpassage = shortpassageDAO.findById(shortpassage.getId());
            shortpassage.getQuestions().add(question);
            logger.debug("Object to save is sshortpassage");
            return true;
        }
        else if(toSave instanceof ShortDualPassage)
        {
            shortdualpassage = (ShortDualPassage)toSave;
            shortdualpassage = shortdualpassageDAO.findById(shortdualpassage.getId());
            shortdualpassage.getQuestions().add(question);
 
            return true;
        }
        else if(toSave instanceof LongDualPassage)
        {
            longdualpassage = (LongDualPassage)toSave;
            longdualpassage = longdualpassageDAO.findById(longdualpassage.getId());
            longdualpassage.getQuestions().add(question);
           return true;
        }
        else
        {
             logger.error("Object to save is not handled");
             return false;
        }
 
    }
 }
