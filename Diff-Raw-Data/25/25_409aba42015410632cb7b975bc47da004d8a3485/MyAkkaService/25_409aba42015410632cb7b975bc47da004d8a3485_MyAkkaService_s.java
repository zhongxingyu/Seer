 package com.aestas.blog.akka.actor;
 import akka.actor.TypedActor;
 import akka.dispatch.Future;
 
 /**
  * User: lfi
  */
 public class MyAkkaService extends TypedActor implements MyService  {
 
 
     public Future<String> getServiceData(final String id) {
         return future("hello" + id);
     }
 }
