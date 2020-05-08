 package com.ece.superkids.users.entities;
 
 import com.ece.superkids.questions.entities.Question;
 import com.ece.superkids.questions.enums.QuestionLevel;
 import com.ece.superkids.questions.enums.QuestionCategory;
 
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.io.Serializable;
 
 /**
  * The <code>History</code> class represents the current state of the user.
  * It maintains the history for all the questions and the scores the user answered, up to 5 attempts.
  *
  * @author Marc Adam
  */
 public class History implements Serializable {
 
     static final long serialVersionUID = 1L;
 
     private Map<String, ArrayList<State>> questionToList;
     private boolean gameStarted;
 
     private Map<QuestionLevel, ArrayList<QuestionCategory>> levelToCategories;
     
     /* whenever new categories are added these lists need to be updated, else the history won't be able to know if the user is done with the level */
     private QuestionCategory level1Categories[] = {QuestionCategory.SHAPES, QuestionCategory.COLORS, QuestionCategory.ANIMALS};
     private QuestionCategory level2Categories[] = {QuestionCategory.FOOD, QuestionCategory.GEOGRAPHY, QuestionCategory.PLANETS};
     private QuestionCategory level3Categories[] = {QuestionCategory.STATIONARY, QuestionCategory.INSTRUMENTS, QuestionCategory.BODYPARTS};
     
     /* whenever new levels are added this needs to be updated, else the history won't be able to know if the user is done with the game */
     private QuestionLevel gameLevels[] = {QuestionLevel.LEVEL_1, QuestionLevel.LEVEL_2, QuestionLevel.LEVEL_3};
 
     
     private void init() {
         levelToCategories = new HashMap<QuestionLevel, ArrayList<QuestionCategory>>();
         levelToCategories.put(QuestionLevel.LEVEL_1, new ArrayList<QuestionCategory>(Arrays.asList(level1Categories)));
         levelToCategories.put(QuestionLevel.LEVEL_2, new ArrayList<QuestionCategory>(Arrays.asList(level2Categories)));
         levelToCategories.put(QuestionLevel.LEVEL_3, new ArrayList<QuestionCategory>(Arrays.asList(level3Categories)));
     }
 
     /**
      * Create a new History object.
      */
     public History() {
         questionToList = new HashMap();
         gameStarted = false;
         init();
     }
 
     /**
      * Set the Game Started flag to true.
      * Call this when starting a new game. This is also automatically called.
      */
     public void setGameStarted() {
         gameStarted = true;
     }
     /**
      * Get the value of the Game Started flag to true.
      * @return Game started
      */
     public boolean getGameStarted() {
         return gameStarted;
     }
 
     /**
      * Get the value of Game On, whether the user should continue game or start a new game.
      * Use this function to see if you wanna show 'continue game' button or not
      * @return Game is on
      */
     public boolean getGameOn() {
         return (gameStarted && !isGameFinished());
     }
 
     /**
      * Check whether the use has finished the level
      * @param level Level to check
      * @return Level finished
      */
      public boolean isLevelFinished(QuestionLevel level) {
          if(level==null){
              return true;
          }
          ArrayList<QuestionCategory> questionCategoryList = levelToCategories.get(level);
          for(int i=0; i<questionCategoryList.size(); i++) {
              String key = questionCategoryList.get(i) + ":" + level;
              if(!questionToList.containsKey(key)) {
                  return false;
              }
          }
         return true;
     }
 
      /**
       * Checks if the whole game is finished, this goes through all the levels.
       * @return Game is finished.
       */
      public boolean isGameFinished() {
          ArrayList<QuestionLevel> gameLevelsList = new ArrayList<QuestionLevel>(Arrays.asList(gameLevels));
          for(int i=0; i<gameLevelsList.size(); i++) {
              if(!isLevelFinished(gameLevelsList.get(i))) {
                  return false;
              }
          }
          return true;
      }
 
      /**
       * Save a state to the history, to the list of attempts.
       * @param state State to save to the history.
       */
     public void saveToHistory(State state) {
         QuestionCategory category = state.getCurrentCategory();
         QuestionLevel level = state.getCurrentLevel();
         String key = category.toString() + ":" + level.toString();
         if(questionToList.containsKey(key)) {
             ArrayList<State> states = (ArrayList<State>)questionToList.get(key);
             if(states.size()==5) {
                 states.remove(0);
             }
             states.add(state);
         } else {
             ArrayList<State> states = new ArrayList();
             states.add(state);
             questionToList.put(key, states);
         }
     }
 
     /**
      * Get the history for a category and level
      * @param category Category for the questions.
      * @param level Level for the questions.
      * @return Map from questions to list of up to 5 scores.
      */
     public Map<Question, ArrayList<Integer>> getHistoryMap(QuestionCategory category, QuestionLevel level) {
         String key = category.toString() + ":" + level.toString();
         Map<Question, ArrayList<Integer>> questionToScores = new HashMap();
         if(questionToList.containsKey(key)) {
             ArrayList<State> states = (ArrayList<State>)questionToList.get(key);
             for(int i=0; i<states.size(); i++) {
                 Iterator it = states.get(i).getAllScores().entrySet().iterator();
                 while(it.hasNext()) {
                     Map.Entry pairs = (Map.Entry)it.next();
                     Question questionKey = (Question)pairs.getKey();
 
                     if(questionToScores.containsKey(questionKey)) {
 
                         ArrayList<Integer> listOfScores = (ArrayList<Integer>)questionToScores.get(questionKey);
                         listOfScores.add((Integer)pairs.getValue());
                         questionToScores.put(questionKey, listOfScores);
                     } else {
                         ArrayList<Integer> listOfScores = new ArrayList();
                         listOfScores.add((Integer)pairs.getValue());
                         questionToScores.put(questionKey, listOfScores);
                     }
                 }
             }
             return questionToScores;
             
         } else {
             return null;
         }
     }
 
     /**
      * Get the best attempt for a category and a level.
      * @param category Category of the attempt.
      * @param level Level of the best attempt.
      * @return State map from questions to scores of the best attempt.
      */
     public State getMaximumScoreState(QuestionCategory category, QuestionLevel level) {
         Map maxScoresMap = new HashMap<Question, Integer>();
         String key = category.toString() + ":" + level.toString();
         ArrayList<State> states = (ArrayList<State>)questionToList.get(key);
         int maxScore = 0;
         int maxScoreIndex = 0;
         for(int i=0; i<states.size(); i++) {
             if(states.get(i).getTotalScore()>maxScore) {
                 maxScoreIndex=i;
                 maxScore=states.get(i).getTotalScore();
             }
         }
         State maxScoreState = states.get(maxScoreIndex);
         return maxScoreState;
     }
 
     /**
      * Get the scores out of the best attempts of each of the levels and categories played by the user.
      * @return Total score
      */
     public int getTotalScore() {
         int totalScore = 0;
         for (int i= 0; i < gameLevels.length; i++) {
             QuestionLevel questionLevel = gameLevels[i];
             ArrayList<QuestionCategory> questionCategories = (ArrayList<QuestionCategory>)levelToCategories.get(questionLevel);
             for(int j=0; j<questionCategories.size(); j++) {
                 QuestionCategory questionCategory = questionCategories.get(j);
                 if(questionToList.containsKey(questionCategory+":"+questionLevel)) {
                     State state = this.getMaximumScoreState(questionCategory, questionLevel);
                     totalScore+=state.getTotalScore();
                 }
             }
         }
         return totalScore;
     }
 
     /**
      * Get the history for a category and a level.
      * @param category Category of the question.
      * @param level Category of the level.
      * @return Two dimensional array with 6 columns, first column has the questions, the 5 other columns have the scores.
      */
     public Object[][] getHistory(QuestionCategory category, QuestionLevel level) {
         Map<Question, ArrayList<Integer>> map = this.getHistoryMap(category, level);
         if(map.size()!=0) {
             Iterator it = map.entrySet().iterator();
             int counter = 0;
             while(it.hasNext())  {
                 Map.Entry pairs = (Map.Entry)it.next();
                 counter ++;
             }
             Object o[][] = new Object[counter][6];
             it = map.entrySet().iterator();
             int index = 0;
             ArrayList<Integer> scoresList;
             int scoresListSize = 0;
             while(it.hasNext()) {
                 Map.Entry pairs = (Map.Entry)it.next();
                 scoresList = (ArrayList<Integer>)pairs.getValue();
                 scoresListSize = scoresList.size();
                 o[index][0] = ((Question)pairs.getKey()).getQuestion();
                 for(int i=1; i<scoresList.size()+1; i++) {
                     o[index][i] = scoresList.get(i-1);
                 }
                 index++;
             }
             index++;
             for(int i=0; i<counter; i++) {
                 for(int j=scoresListSize+1; j<6; j++) {
                    o[i][j] = 0;
                 }
             }
             return o;
         } else {
             return null;
         }
 
     }
 
     /**
      * Get the history for a category and a level.
      * @return Two dimensional array with fake scores.
      */
     public Object[][] getHistoryTest() {
         Object o[][] = new Object[10][6];
         for(int i=0; i<o.length; i++) {
             o[i][0] = "Question" + i;
             for(int j=1; j<6; j++) {
                 o[i][j] = i*i;
             }
         }
         return o;
     }
 }
