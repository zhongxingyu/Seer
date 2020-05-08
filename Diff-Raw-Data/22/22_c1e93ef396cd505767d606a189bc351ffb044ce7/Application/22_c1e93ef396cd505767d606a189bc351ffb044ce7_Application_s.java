 package controllers;
 
 import java.util.List;
 import models.Answer;
 import models.Question;
 import models.Tag;
 import play.Logger;
 import play.mvc.Controller;
 
 public class Application extends Controller {
 
     public static void index() {
         render();
     }
 
     public static void read(String query) {
         Logger.info("question entered: " + query);
         List<Tag> tags = Tag.findOrCreateTagsForText(query);
         Question question = Question.findOrCreateTagByText(query, tags);
         show(question.id);
     }
 
     public static void show(Long id) {
         Question question = Question.findById(id);
         List<Answer> answers = Answer.getAnswersForTags(question.tags);
        if(answers.isEmpty()){
             question.answered = false;
             question.save();
         }
         render(question, answers);
     }
 
     public static void setBestAnswer(Long aid, Long qid) {
         Question q = Question.findById(qid);
         Answer a = Answer.findById(aid);
         a.setTags(q.tags);
         a.save();
         q.answered = true;
         q.save();
         show(q.id);
     }
 }
