 package com.aestas.blog.akka.actor;
 
 import akka.dispatch.Future;
 
 /**
  * User: lfi
  */
 public interface MyService {
 
     Future<String> getServiceData(String id);
    Future<Integer> getTestValue();
 
 }
