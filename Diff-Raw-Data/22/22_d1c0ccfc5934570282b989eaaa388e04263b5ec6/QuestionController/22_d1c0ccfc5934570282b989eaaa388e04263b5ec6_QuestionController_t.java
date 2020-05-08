 package ac.hw.services.collabquiz.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 
 import ac.hw.services.collabquiz.Logic.QuestionDifficulty;
 import ac.hw.services.collabquiz.dao.IQuestionRepository;
 import ac.hw.services.collabquiz.dao.impl.QuestionRepository;
 import ac.hw.services.collabquiz.entities.Question;
 
 
 import javax.annotation.PostConstruct;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 @Controller("questionController")
 @ManagedBean(name = "questionController")
 @ViewScoped
 public class QuestionController extends BasePageController {
 
     
     private IQuestionRepository questionRepository;
 
     private final List<Question> questions = new ArrayList<Question>();
 
     // Creating new question
     private Question newQuestion = new Question();
 
     private Question selectedQuestion;
 
     // Question selected using checkbox
     private Question[] selectedQuestions;
 
     public QuestionController() {
         postConstruct();
     }
 
     
     public void postConstruct() {
         log.debug("postConstruct ()");
         questionRepository = new QuestionRepository();
 
         try {
             List<Question> storedQuestions = questionRepository.list();
             questions.addAll(storedQuestions);

         } catch (Exception ex) {
             log.error("Error loading questions from repository, none loaded", ex);
         }
 
         if (log.isDebugEnabled())
             log.debug("Loaded " + questions.size() + " questions from DB");
     }
 
     public List<Question> getQuestions() {
         return questions;
     }
 
     public void growl() {
         FacesContext context = FacesContext.getCurrentInstance();
         context.addMessage(null, new FacesMessage("Second Message", "Additional Info Here..."));
     }
 
     public IQuestionRepository getQuestionRepository() {
         return questionRepository;
     }
 
     public void setQuestionRepository(IQuestionRepository questionRepository) {
         this.questionRepository = questionRepository;
     }
 
     public Question getSelectedQuestion() {
         return selectedQuestion;
     }
 
     public void setSelectedQuestion(Question selectedQuestion) {
         this.selectedQuestion = selectedQuestion;
     }
 
     public Question[] getSelectedQuestions() {
         return selectedQuestions;
     }
 
     public void setSelectedQuestions(Question[] selectedQuestions) {
         this.selectedQuestions = selectedQuestions;
     }
 
     public Question getNewQuestion() {
         return newQuestion;
     }
 
     public void addQuestion() {
         log.debug("Inserting new question with question text: " + newQuestion.getQuestionText() + " , and category ID: " + newQuestion.getCategoryID());
         questionRepository.insert(newQuestion);
         questions.add(newQuestion);
         newQuestion = new Question();
     }
 
     public void deleteQuestion() {
         log.debug("Deleting selected categories: " + Arrays.toString(selectedQuestions));
 
         for (Question current : selectedQuestions) {
             log.debug("Deleting: " + current);
             questionRepository.physicalDelete(current);
             questions.remove(current);
         }
     }
 
     public QuestionDifficulty[] getAvailableDifficulties() {
         return QuestionDifficulty.values();
     }
 }
