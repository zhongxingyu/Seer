 package controllers;
 
 import models.Streams;
 import play.mvc.Controller;
 import play.mvc.Result;
 import iteratee.F;
 import iteratee.Iteratees;
 import iteratee.JIteratee;
 
 public class RealTime extends Controller {
 
     public static Result index(String role) {
         return ok(views.html.realtime.render(role));
     }
 
     public static Result feed(final String role, final int lowerBound, final int higherBound) {
 
         Iteratees.Enumeratee<Streams.Event, Streams.Event> secure = Iteratees.Enumeratee.map(new F.Function<Streams.Event, Streams.Event>() {
             @Override
             public Streams.Event apply(Streams.Event o) {
                 for (Streams.SystemStatus status : F.caseClassOf(Streams.SystemStatus.class, o)) {
                     if (role.equals("MANAGER")) {
                         return status;
                     }
                 }
                 for (Streams.Operation operation : F.caseClassOf(Streams.Operation.class ,o)) {
                     if (operation.level.equals("public")) {
                         return operation;
                     } else {
                         if (role.equals("MANAGER")) {
                             return operation;
                         }
                     }
                 }
                 return o;
             }
         });
 
         Iteratees.Enumeratee<Streams.Event, Streams.Event> inBounds = Iteratees.Enumeratee.map(new F.Function<Streams.Event, Streams.Event>() {
             @Override
             public Streams.Event apply(Streams.Event o) {
                 for (Streams.SystemStatus status : F.caseClassOf(Streams.SystemStatus.class, o)) {
                     return status;
                 }
                 for (Streams.Operation operation : F.caseClassOf(Streams.Operation.class ,o)) {
                     if (operation.amount > lowerBound && operation.amount < higherBound) {
                         return operation;
                     }
                 }
                 return o;
             }
         });
 
         Iteratees.Enumeratee<Streams.Event, String> asJson = Iteratees.Enumeratee.map(new F.Function<Streams.Event, String>() {
             @Override
             public String apply(Streams.Event o) {
                 for (Streams.SystemStatus status : F.caseClassOf(Streams.SystemStatus.class, o)) {
                     return "{\"type\":\"status\", \"message\":\"" + status.message + "\"}";
                 }
                 for (Streams.Operation operation : F.caseClassOf(Streams.Operation.class ,o)) {
                     return "{\"type\":\"operation\", \"amount\":" + operation.amount + ", \"visibility\":\"" + operation.level + "\"}";
                 }
                 return "";
             }
         });
 
        // TODO : it seems that enumeratee are buffering data ... GGRRRR
        //return JIteratee.eventSource(Streams.events.through(secure, inBounds).through(asJson));
        return JIteratee.eventSource(Streams.events);
     }
 }
