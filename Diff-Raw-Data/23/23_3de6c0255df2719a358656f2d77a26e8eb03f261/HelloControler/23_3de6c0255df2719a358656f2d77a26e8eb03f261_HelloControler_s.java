 package ${package}.controllers;
 
import vinna.outcome.Outcome;
import vinna.outcome.StringOutcome;
 
 public class HelloControler {
 
  public Outcome sayHello(String name) {
    return new StringOutcome(String.format("Hello %s !", name));
   }
 }
