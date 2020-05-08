 package com.core.test;
 
 import com.core.lesson.*;
 import com.core.util.ResourceManager;
 import com.core.util.Utils;
 import org.json.JSONObject;
 import org.json.JSONException;
 import org.json.JSONArray;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Calendar;
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 public class Main {
     private static int testStartPage = 6;
 
     public static void main(String[] args) {
 
         Lesson lesson = new Lesson();
         ApplicationJSON application = new ApplicationJSON(ResourceManager.getJSON("application.json"));
         int feedBackID = application.getPageByType("FeedbackPage");
         int[] testIDs = application.getPagesByType("SpeakingQuestionPage");
         int[] surveyIDs = application.getPagesByType("SurveyPage");
         try {
             lesson.getDialog().setTitle(application.getTitle());
             //pages
             JSONArray pages = application.getPages();
             int totalTest = application.getTestsCount();
             int testCnt = 0;
             for(int i=0; i<pages.length(); i++) {
                 JSONObject page = pages.getJSONObject(i);
                 String type = page.getString("type");
                 if (type.equals("SpeakingQuestionPage")) {
                     page.put("totalTest", totalTest);
                     testCnt++;
                     page.put("id", testCnt);
                 }
                 LessonPageDescriptor p = PageFactory.createPage(type, page);
                 if (i < pages.length() - 1) {
                   p.setNextPageDescriptor(Integer.toString(i+2));
                 }
                 lesson.registerLessonPage(Integer.toString(i+1), p);
             }
             lesson.setCurrentPage("1");
 
             int ret = lesson.showModalDialog();
             HashMap submit = lesson.getModel().getPageSubmit();
             String userID = (String) ((HashMap)submit.get("1")).get("userID");
             String str = "";
             if (testIDs != null) {
                 for(int i=0; i<testIDs.length; i++) {
                     str += Utils.Hash2String((HashMap)submit.get(Integer.toString(testIDs[i])));
                 }
                 Utils.WriteFile(ResourceManager.getUserTestFile(userID), str);
             }
             for(int i=0; i<surveyIDs.length; i++) {
                 HashMap page = (HashMap)submit.get(Integer.toString(surveyIDs[i]));
                 if (page != null) {
                     Utils.WriteFile(ResourceManager.getSurveyFile(userID), Utils.Hash2CSV(page));
                 }
             }
            Utils.WriteFile(ResourceManager.getQueryFile(userID), Utils.Hash2String((HashMap)submit.get(Integer.toString(feedBackID))));
             System.out.println("Dialog return code is (0=Finish,1=Error): " + ret);
         } catch (JSONException e) {
             lesson.getDialog().setTitle("Oral Completion Test");
         }
 
         System.exit(0);
     }
 }
