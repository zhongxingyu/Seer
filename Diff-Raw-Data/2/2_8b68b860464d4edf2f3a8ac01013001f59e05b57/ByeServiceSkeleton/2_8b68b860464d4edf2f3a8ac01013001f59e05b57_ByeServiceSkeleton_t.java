 
 /**
  * ByeServiceSkeleton.java
  *
  * This file was auto-generated from WSDL
  * by the Apache Axis2 version: 1.5.1  Built on : Oct 19, 2009 (10:59:00 EDT)
  */
    package com.gmail.kostianych;
     /**
      *  ByeServiceSkeleton java skeleton for the axisService
      */
     public class ByeServiceSkeleton implements com.gmail.kostianych.ws.ByeServiceSkeletonInterface {
         
          
         /**
          * Auto generated method signature
          * 
                                      * @param sayBye
          */
         
                  public com.gmail.kostianych.SayByeResponseDocument sayBye
                   (
                   com.gmail.kostianych.SayByeDocument sayBye
                   )
             {
                 //TODO : fill this with the necessary business logic
                 //throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#sayBye");
 		String input = sayBye.getSayBye().getMsg();
 		com.gmail.kostianych.SayByeResponseDocument sayByeResponseDocument = com.gmail.kostianych.SayByeResponseDocument.Factory.newInstance();
 		com.gmail.kostianych.SayByeResponseDocument.SayByeResponse sayByeResponse = com.gmail.kostianych.SayByeResponseDocument.SayByeResponse.Factory.newInstance();
 		sayByeResponse.setReturn("Bye " + input);
 		sayByeResponseDocument.setSayByeResponse(sayByeResponse);
 		return sayByeResponseDocument;
         }
      
     }
     
