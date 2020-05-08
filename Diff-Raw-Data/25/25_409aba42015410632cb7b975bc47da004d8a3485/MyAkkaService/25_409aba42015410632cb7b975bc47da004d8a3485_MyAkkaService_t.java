 package com.aestas.blog.akka.actor;
 import akka.actor.TypedActor;
 import akka.dispatch.Future;
 
 /**
  * User: lfi
  */
 public class MyAkkaService extends TypedActor implements MyService  {
 
    private Integer testValue = 0;
 
     public Future<String> getServiceData(final String id) {

        testValue++;
         return future("hello" + id);
     }

    public Future<Integer> getTestValue() {
        return future(testValue);

    }
 }
