 package com.ece.superkids;
 
 import com.ece.superkids.builders.QuestionBuilder;
 import com.ece.superkids.entities.Question;
 import com.ece.superkids.enums.QuestionCategory;
 import com.ece.superkids.enums.QuestionLevel;
 import com.ece.superkids.enums.QuestionMode;
 import com.ece.superkids.enums.QuestionType;
 import com.google.gson.Gson;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class FileQuestionDatabase implements QuestionDatabase{
 
     private static final String FILE_NAME = "/Questions.txt";
     private static final String CUSTOM_FILE_NAME = "/CustomQuestions.txt";
     private Gson gson = new Gson();
     private Map<QuestionLevel, Map<QuestionCategory,List<Question>>> questions = new HashMap<QuestionLevel,
             Map<QuestionCategory,List<Question>>>();
 
     public FileQuestionDatabase(QuestionMode mode) {
         switchMode(mode);
     }
 
     public void switchMode(QuestionMode mode) {
         questions.clear();
         switch(mode){
             case DEFAULT_ONLY:
                 loadQuestionsFromFile(FILE_NAME);
                 break;
             case CUSTOM_ONLY:
                 loadQuestionsFromFile(CUSTOM_FILE_NAME);
                 break;
             case ALL:
                 loadQuestionsFromFile(FILE_NAME);
                 loadQuestionsFromFile(CUSTOM_FILE_NAME);
                 break;
             default:
         }
     }
 
     @Override
     public int getQuestionNumber(final Question question) {
         QuestionLevel level = question.getLevel();
         QuestionCategory category = question.getCategory();
         if (questions.containsKey(level) && questions.get(level).containsKey(category)) {
             return questions.get(level).get(category).indexOf(question);
         }
         return -1;
     }
 
     private void loadQuestionsFromFile(String fileName) {
         InputStream in = getClass().getResourceAsStream(fileName);
         try {
             BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             while (true) {
                 String line = reader.readLine();
                 if (line == null) {
                     break;
                 }
                 Question question = gson.fromJson(line, Question.class);
                 addQuestionToList(question);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void addQuestionToList(final Question question) {
         QuestionLevel level = question.getLevel();
         QuestionCategory category = question.getCategory();
         if (!questions.containsKey(level)) {
             questions.put(level, new HashMap<QuestionCategory, List<Question>>());
         }
         if(!questions.get(level).containsKey(category)) {
             questions.get(level).put(category, new ArrayList<Question>());
         }
         questions.get(level).get(category).add(question);
     }
 
     @Override
     public Question getQuestion(final QuestionLevel level, final QuestionCategory category, final int number) {
        if (questions.get(level).get(category).size() <= number) return null;
         return questions.get(level).get(category).get(number);
     }
 
     @Override
     public void saveQuestion(final QuestionLevel level, final QuestionCategory category, final Question question) {
         File file = new File(getClass().getResource("/" + FILE_NAME).getFile());
 
         question.setLevel(level);
         question.setCategory(category);
         try {
             FileWriter writer = new FileWriter(file, true);
             writer.write(gson.toJson(question));
             writer.write('\n');
             writer.close();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
 	@Override
 	public int getNumberOfQuestions(final QuestionLevel level, final QuestionCategory category) {
		if (!questions.get(level).containsKey(category)) return 0;
 		return questions.get(level).get(category).size();
 	}
 
 	@Override
 	public int getNumberOfQuestions(final QuestionLevel level) {
 		int sum = 0;
		if (!questions.containsKey(level)) return sum;
 
         for (List<Question> category : questions.get(level).values()) {
             sum += category.size();
         }
 
         return sum;
 	}
 }
