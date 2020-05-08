 package com.forum.repository;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.jsoup.Jsoup;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class QuestionValidation {
     private static final int MINIMUM_CHARACTERS = 20;
     private static final int JAVA_SPACE = 32;
     private static final int HTML_SPACE = 160;
     private static final int ENTER_KEY_LIMIT = 6;
 
     public boolean isQuestionValid(String questionWithTag) {
         String question= Jsoup.parse(questionWithTag).text();
         if (question == null || question.equals("")) return false;
         question = getPlainText(question);
         question = reduceBlanks(question);
        if (question == "" || question.length() < MINIMUM_CHARACTERS) return false;
        return true;
     }
 
     private String getPlainText(String question) {
         question = question.replaceAll("\\<.*?>", "");
         return StringEscapeUtils.unescapeHtml(question);
     }
 
     public String trimSpecialSymbolsAndSpaces(String question) {
         question = question.replaceAll("[|]", " ").replaceAll("!", " ").replaceAll("\\\\", " ").replaceAll("[()]", " ").replaceAll(":", " ").replaceAll("'", " ").replaceAll("&", " ");
         question = question.replaceAll("( )+", " ");
         question = question.trim();
         return question;
     }
 
     private String reduceBlanks(String question) {
         int spaceCount = 0;
         int enterCount = 0;
 
         StringBuilder refactoredQuestion = new StringBuilder(question.length());
 
         for (int i = 0; i < question.length(); i++) {
             boolean spaceCharacter = question.charAt(i) == JAVA_SPACE || question.charAt(i) == HTML_SPACE;
             if (spaceCharacter) {
                 spaceCount++;
             }
 
 
             if (enterCount > ENTER_KEY_LIMIT) {
                 refactoredQuestion.append(question.charAt(i));
                 break;
             }
             if (!spaceCharacter || spaceCount <= 1) {
                 refactoredQuestion.append(question.charAt(i));
             }
 
             if (!spaceCharacter) spaceCount = 0;
         }
         return refactoredQuestion.toString();
     }
 
     public String insertApostrophe(String question) {
         StringBuilder refactoredQuestion = new StringBuilder(question.length());
         String apostrophe = "'";
         for (int i = 0; i < question.length(); i++) {
             refactoredQuestion.append(question.charAt(i));
             if (question.charAt(i) == '\'') {
                 refactoredQuestion.append(apostrophe);
             }
         }
         return refactoredQuestion.toString();
     }
 }
