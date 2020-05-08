 
 package uk.ac.angus.coreskillstest.quizmanagement;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import uk.ac.angus.coreskillstest.entity.StoredFeedback;
 import uk.ac.angus.coreskillstest.entity.Question;
 import uk.ac.angus.coreskillstest.entity.Quiz;
 import uk.ac.angus.coreskillstest.entity.ResultRule;
 import uk.ac.angus.coreskillstest.quizmanagement.quizconfiguration.QuizEvent;
 
 /**
  *
  * Takes quizId and quizEvent Id and builds quiz object based on quiz event.
  * 
  * @author JWO
  */
 public class QuizPreparation
 {
     private Quiz SelectedQuiz = null;
     private QuizEvent SelectedQuizEvent = null;
     
     public QuizPreparation()
     {
         
     }
     
     public QuizPreparation(Quiz quiz, QuizEvent quizEvent)
     {
         SelectedQuiz = quiz;
         SelectedQuizEvent = quizEvent;
     }
    
     public void setQuiz(Quiz newQuiz)
     {
         SelectedQuiz = newQuiz;
     }
     
     public void setQuizEvent(QuizEvent newQuizEvent)
     {
         SelectedQuizEvent = newQuizEvent;
     }
     
     public Quiz processQuiz() throws uk.ac.angus.coreskillstest.quizmanagement.exception.CannotGenerateQuizException
     {
         if(SelectedQuiz == null || SelectedQuizEvent == null)
             throw new uk.ac.angus.coreskillstest.quizmanagement.exception.CannotGenerateQuizException("Process Quiz: Cannot generate quiz - ensure that Quiz and QuizEvent are set");
         
         processQuizByQuizEvent();
         
         return SelectedQuiz;
     }
     
     private void processQuizByQuizEvent()
     {
         List<Question> questionList;
         
         if(SelectedQuizEvent.getRandomOrder() == true)
         {
             questionList = randomiseQuestions(SelectedQuiz.getQuestions(), SelectedQuizEvent.getNumberOfQuestions());
             
             SelectedQuiz.setQuestions(questionList);
         }
         
         if(SelectedQuizEvent.getReturnResult() == true)
         {
             setDefaultFeedback(SelectedQuiz.getResultRules());
         }
     }
     
     /**
      * 
      * @param results
      * @return 
      */
     private void setDefaultFeedback(List<ResultRule> results)
     {
         StoredFeedback newFeedback = StoredFeedback.getDefaultFeedback();
         
         for(ResultRule currentRule : results)
         {
             currentRule.setFeedback(newFeedback);
         } 
     }
     
     
     /**
      * 
      * @param questionList
      * @param numberOfQuestions 
      */
     private List<Question> randomiseQuestions(List<Question> questionList, int numberOfQuestions)
     {
         List<Question> newList = new ArrayList();
         List<Integer> chosenNumbers = new ArrayList();
         
         Random randomNumber = new Random();
         int chosenQ = 0;
         boolean invalidNumber = true;
         
        //TODO: Double check logic used in picking random questions.
        
         for(int i = 1; i <= numberOfQuestions; i++)
         {    
             while(invalidNumber == true)
             {
                 chosenQ = randomNumber.nextInt(numberOfQuestions);
                 Integer checkNumber = chosenQ;
                 
                 if(!chosenNumbers.contains(checkNumber))
                {
                     invalidNumber = false;
                    chosenNumbers.add(checkNumber);
                }
             }
             Question currentQ = questionList.get(chosenQ);
             
             newList.add(currentQ);
         }
         
         return newList;
     }
 }
