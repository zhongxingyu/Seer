 /*
 *  Nokia Data Gathering
 *
 *  Copyright (C) 2011 Nokia Corporation
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/
 */
 
 package models.utils;
 
 import controllers.util.Constants;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import models.Category;
 import models.DefaultAnswer;
 import models.NdgUser;
 import models.Question;
 import models.QuestionOption;
 import models.Survey;
 
 public class SurveyDuplicator {
 
     public static Survey plainCopy(Survey origin, String newId) {
         Survey copy = new Survey();
         copy.available = Constants.SURVEY_BUILDING;
         copy.lang = origin.lang;
         copy.ndgUser = origin.ndgUser;
         copy.surveyId = newId;
         copy.title = origin.title;
         copy.uploadDate = new Date();
 
         copy.categoryCollection = copyCategories(origin.categoryCollection, copy);
 
         return copy;
     }
 
     public static Survey addDemoSurveyToNewUser(Survey origin, String newId, NdgUser user) {
         Survey copy = new Survey();
         copy.available = Constants.SURVEY_BUILDING;
         copy.lang = origin.lang;
         copy.ndgUser = user;
         copy.surveyId = newId;
         copy.title = origin.title;
         copy.uploadDate = new Date();
 
         copy.categoryCollection = copyCategories(origin.categoryCollection, copy);
 
         return copy;
     }
 
     private static List<Category> copyCategories(List<Category> origin, Survey newSurvey){
         List<Category> copy = new ArrayList<Category>();
 
         Category copiedCategory = null;
         for(Category category : origin){
             copiedCategory = new Category();
             copiedCategory.survey = newSurvey;
             copiedCategory.label = category.label;
             copiedCategory.objectName = category.objectName;
             copiedCategory.categoryIndex = category.categoryIndex;
             copiedCategory.questionCollection = copyQuestions(category.questionCollection, copiedCategory);
             copy.add(copiedCategory);
         }
 
         return copy;
     }
 
     private static List<Question> copyQuestions(List<Question> origin, Category newCategory) {
         List<Question> copy = new ArrayList<Question>();
         for (Question question : origin) {
             Question copiedQuestion = new Question();
             copiedQuestion.constraintText = question.constraintText;
             copiedQuestion.relevant = question.relevant;
             copiedQuestion.hint = question.hint;
             copiedQuestion.label = question.label;
             copiedQuestion.objectName = question.objectName;
             copiedQuestion.questionType = question.questionType;
             copiedQuestion.readonly = question.readonly;
             copiedQuestion.required = question.required;
             copiedQuestion.category = newCategory;
             copiedQuestion.questionOptionCollection = copyQuestionOptions(question.questionOptionCollection, copiedQuestion);
             if( question.defaultAnswer != null ){
                 copiedQuestion.defaultAnswer = copyDefaultAnswer( question.defaultAnswer, copiedQuestion );
             }
 
             copy.add(copiedQuestion);
         }
         return copy;
     }
 
     private static Collection<QuestionOption> copyQuestionOptions(Collection<QuestionOption> origin, Question newQuestion) {
         Collection<QuestionOption> copy = new ArrayList<QuestionOption>();
         for (QuestionOption questionOption : origin) {
            QuestionOption copiedOption = new QuestionOption();
            copiedOption.optionIndex = questionOption.optionIndex;
            copiedOption.label = questionOption.label;
            copiedOption.optionValue = questionOption.optionValue;
            copiedOption.question = newQuestion;
            copy.add(copiedOption);
         }
         return copy;
     }
 
     private static DefaultAnswer copyDefaultAnswer(DefaultAnswer origin, Question newQuestion ){
         DefaultAnswer newAnswer = new DefaultAnswer();
         newAnswer.binaryData = origin.binaryData;
         newAnswer.textData = origin.textData;
         newAnswer.questionCollection.add( newQuestion );
         newAnswer.save();
 
         return newAnswer;
     }
 
 }
