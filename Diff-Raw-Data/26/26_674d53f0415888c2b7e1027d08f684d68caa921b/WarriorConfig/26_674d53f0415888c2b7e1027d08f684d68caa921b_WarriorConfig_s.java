 package com.csg.warrior.raydotcom;
 
 public class WarriorConfig {
     // TODO place the configurations in XML.
 
     public String getKeyNotUploadedReply() {
         return "User must upload mobile key";
     }
 
     public String getSignUpSuccessReply() {
         return "Sign up successful";
     }
 
     public String getKeyStatusUrl() {
         return "http://localhost:8080/testserver/key-status";
     
     public String getWarriorKeyRequestURL() {
     	return "http://localhost:8080/testserver/key-request";
     }
 }
