 package no.anderska.wta;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import no.anderska.wta.dto.AnswerResponseDTO;
 import no.anderska.wta.dto.AnswerResponseDTO.AnswerStatus;
 import no.anderska.wta.dto.QuestionDTO;
 import no.anderska.wta.dto.QuestionCategoryDTO;
 import no.anderska.wta.servlet.PlayerHandler;
 import no.anderska.wta.servlet.QuestionChecker;
 
 public class GameEngine implements QuestionChecker {
 
     private final PlayerHandler playerHandler;
     private final Map<Integer,QuestionCategoryEngine> engines;
     private final Map<Integer,List<QuestionStatus>> questionByCategory;
     private final Map<Integer,QuestionStatus> allQuestions;
 
     public GameEngine(List<QuestionCategoryEngine> categories, PlayerHandler playerHandler) {
         this.playerHandler = playerHandler;
         this.engines = initializeEngines(categories);
         this.questionByCategory = new Hashtable<>();
         this.allQuestions = new Hashtable<>();
         initializeQuestions();
     }
 
     private void initializeQuestions() {
         int idbase = 0;
         Set<Entry<Integer, QuestionCategoryEngine>> entrySet = engines.entrySet();
         for (Entry<Integer, QuestionCategoryEngine> entry : entrySet) {
             idbase+=1000000;
             
             List<Question> myQuestions = entry.getValue().myQuestions();
             List<QuestionStatus> questionsThisCategory = new ArrayList<>();
             for (Question question : myQuestions) {
                 QuestionStatus qs = new QuestionStatus(
                         idbase+question.getId(), question, entry.getValue());
                 questionsThisCategory.add(qs);
                 allQuestions.put(qs.getMyId(), qs);
             }
             questionByCategory.put(entry.getKey(),questionsThisCategory);
             
         }
     }
 
     private Map<Integer, QuestionCategoryEngine> initializeEngines(
             List<QuestionCategoryEngine> categories) {
         Map<Integer, QuestionCategoryEngine> result = new Hashtable<>();
         int num = 0;
         for (QuestionCategoryEngine questionCategoryEngine : categories) {
             result.put(++num, questionCategoryEngine);
         }
         return result;
     }
 
     @Override
     public AnswerResponseDTO checkAnswer(String gamerId, String questionId, String answer) {
         int qid;
         try {
             qid = Integer.parseInt(questionId);
         } catch (NumberFormatException | NullPointerException e) {
             return AnswerResponseDTO.create(AnswerStatus.MISSING_PARAMETER).withDescription("Unknown question id");
         }
         
         try {
             long playerId = Long.parseLong(gamerId);
             if (!playerHandler.playerPlaying(playerId)) {
                 return AnswerResponseDTO.create(AnswerStatus.MISSING_PARAMETER).withDescription("Unknown player id");
             }
         } catch (NumberFormatException | NullPointerException e) {
             return AnswerResponseDTO.create(AnswerStatus.MISSING_PARAMETER).withDescription("Unknown player id");
         }
         
         
         QuestionStatus questionStatus = allQuestions.get(qid);
         if (questionStatus == null) {
             return AnswerResponseDTO.create(AnswerStatus.MISSING_PARAMETER).withDescription("Unknown question id");            
         }
        boolean correctAnswer = questionStatus.getEngine().checkAnswer(gamerId, questionStatus.getQuestion().getId(), answer);
         AnswerStatus status = correctAnswer ? AnswerStatus.OK : AnswerStatus.WRONG;
         int points = questionStatus.getQuestion().getPoints();
         if (!correctAnswer) {
             points = points * -1;
         }
         playerHandler.addPoints(Long.parseLong(gamerId), points);
         return AnswerResponseDTO.create(status);
     }
 
     @Override
     public List<QuestionCategoryDTO> allCategories() {
         List<QuestionCategoryDTO> result = new ArrayList<>();
         for (Entry<Integer, QuestionCategoryEngine> entry : engines.entrySet()) {
             result.add(QuestionCategoryDTO.create(entry.getKey(),entry.getValue().getDescription()));
         }
         return result;
     }
 
     @Override
     public List<QuestionDTO> listCategory(int categoryId) {
         List<QuestionStatus> myQuestions = questionByCategory.get(categoryId);
         if (myQuestions == null) {
             return new ArrayList<>();
         }
         ArrayList<QuestionDTO> result = new ArrayList<>();
         for (QuestionStatus questionStatus : myQuestions) {
             result.add(QuestionDTO.factory()
                 .withId(questionStatus.getMyId())
                 .withPoint(questionStatus.getQuestion().getPoints())
                 .withText(questionStatus.getQuestion().getText())
                 .withAnswered(!questionStatus.isAvailible())
                 .create());
         }
         
         return result;
     }
 
 }
