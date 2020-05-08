 package com.aestas.blog.akka.client;
 
 import akka.actor.TypedActor;
 import akka.dispatch.Future;
 import com.aestas.blog.akka.actor.MyAkkaService;
 import com.aestas.blog.akka.actor.MyService;
 
 import static akka.actor.Actors.remote;
 
 /**
  * User: lfi
  */
 public class AkkaClient {
 
     public static void main(String[] args) {
         // Client-managed Remote Typed Actor
 //        MyService actor = (MyService) TypedActor
 //                        .newRemoteInstance(MyService.class, MyAkkaService.class, 1000, "localhost", 9919);
 
         // Server-managed Remote TypedActor
         // Client usage
         MyService actor = remote()
                 .typedActorFor(MyService.class, "com.aestas.blog.akka.actor.MyAkkaService", 5000L, "localhost", 9919);
 
         Future<String> f =  actor.getServiceData("100");
         String res = f.await().resultOrException().get();
         System.out.println(res);
        System.out.println(actor.getTestValue().await().resultOrException().get());
        System.exit(0);
     }
 }
