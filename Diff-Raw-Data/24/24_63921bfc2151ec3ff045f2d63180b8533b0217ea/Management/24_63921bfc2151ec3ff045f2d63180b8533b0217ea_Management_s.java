 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controllers;
 
import api.deprecated.ActivationJSON;
import api.deprecated.BaseJSON;
import api.deprecated.CreateResponseJSON;
import api.deprecated.QuestionJSON;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Random;
 
import javax.persistence.EntityManager;

 import models.Choice;
 import models.Poll;
 import notifiers.MailNotifier;
import play.db.jpa.JPASupport;
 import play.mvc.Controller;
import play.mvc.Http.StatusCode;
 
 /**
  * Controller which takes care of functions that poll administrator uses
  * @author OpenARS Server API team
  */
 @Deprecated
 public class Management extends Controller {
 
     private static Gson gson = new Gson();
 
     /**
      * Method for creating a question
      * URL: <server>/newPoll
      * Method: POST
      * Request body: QuestionJSON with these fields set: question, answers, multipleAllowed, email
      */
     public static void createQuestion() {
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(request.body));
         try {
             String json = reader.readLine();
             QuestionJSON questionMsg = gson.fromJson(json, QuestionJSON.class);
 
             Poll question = questionMsg.makeModelFromJSON();
 
             // generate data and save question, try until we have unique poll ID
             do {
                 question.token = String.valueOf(new Random(System.currentTimeMillis()).nextInt(999999));
             } while (!Poll.find("byToken", question.token).fetch().isEmpty());
 
             //question.generateAdminKey(8);
             question.save();
 
             // send mail to the creator of question 
             MailNotifier.sendPollIDLink(question);
             MailNotifier.sendAdminLink(question);
 
             // retrieve answers from JSON and save them into database
             for (String a: questionMsg.answers) {
                 new Choice(question, a).save();
             }
 
             //renderJSON(new CreateResponseJSON(question.toJson()));
 
         } catch (IOException ex) {
             ex.printStackTrace();
             renderJSON(new String());
         }
 
     }
 
     /**
      * Method for activating the question. It responds with "activated" when
      * adminKey is correct, "not activated" when it is not correct or 
      * "The question does not exist!" if the poll does not exist. <br/>
      * URL: <server>/activation/{id}/{adminKey} <br/>
      * Method: POST <br/>
      * Parameter {id} - poll ID <br/>
      * Parameter {adminKey} - randomly generated string at question creation <br/>
      * Request body: ActivationJSON with duration set
      */
     public static void activation() {
         long urlID = params.get("id", Long.class).longValue();
         String adminKey = params.get("adminKey");
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(request.body));
         try {
             // deserialize json from request body
             String json = reader.readLine();
             ActivationJSON activationMsg = gson.fromJson(json, ActivationJSON.class);
 
             // retrieve and activate the question
             Poll question = Poll.find("byPollID", urlID).first();
 
             if (question == null) {
                 renderJSON("The question does not exist!");
             }
         	/*
             // only when provided adminKey is correct
             /*if (question.adminKey.equals(adminKey)) {
                 //question.activateFor(activationMsg.getDuration());
                 question.save();
                 renderJSON("activated");
             } else {
                 renderJSON("not activated");
             }*/
 
         } catch (IOException ex) {
             ex.printStackTrace();
             renderJSON(new String());
         }
     }
 
     /**
      * Method used to check if admin link for the question is correct.
      * It responds with true if it is correct or false otherwise. <br/>
      * URL: <server>/checkAdminKey/{id}/{adminKey} <br/>
      * Method: GET
      * Parameter {id} - poll ID <br/>
      * Parameter {adminKey} - randomly generated string at question creation <br/>
      */
    /* public static void checkAdminLink() {
         long urlID = params.get("id", Long.class).longValue();
         String adminKey = params.get("adminKey");
         // retrieve and activate the question
         Poll question = Poll.find("byPollID", urlID).first();
        /*
         if (question != null && adminKey != null && question.adminKey.equals(adminKey)) {
             renderJSON(new BaseJSON());
         } else {
         	response.status = StatusCode.FORBIDDEN;
             renderJSON(new BaseJSON("Invalid admin link"));
         }
        */
     }
     */
 }
