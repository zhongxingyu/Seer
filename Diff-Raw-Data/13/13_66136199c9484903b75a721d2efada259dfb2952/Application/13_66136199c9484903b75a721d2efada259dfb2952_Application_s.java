 package controllers;
 
 import play.*;
 import play.mvc.*;

 import views.html.*;
 import models.*;
 
 public class Application extends Controller {
   
   public static Result index() {
     return ok(
       index.render()
     );
   }
 
   public static Result interpret(String command) {
     //Test t = new Test();
     System.out.println("APP: " + command);
     return ok(
       interpret.render(y86.processCMD(command))
     );
   }
 
   public static Result getState() {
     Test t = new Test();
     return ok(
      state.render(t.getState())
     );
   }
 
   public static Result getRegisters() {
     return ok(
       registers.render()
     );
   }
   
 }
