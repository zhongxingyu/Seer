 package controllers;
 
 import play.*;
 import play.mvc.*;
 import java.util.*;
 import views.html.*;
 import models.*;
 import com.google.gson.*;
 
 public class Application extends Controller {
   
   public static Result index() {
     return ok(
       index.render()
     );
   }
 
   public static Result interpret(String command) {
     System.out.println("APP: " + command);
     return ok(
       interpret.render(y86.processCMD(command))
     );
   }
 
  public static Result getState(String format) {
     y86 asm = new y86();
    String result = "";
    if (format.equalsIgnoreCase("standard")) {
      ArrayList temp = asm.getState();
      Gson gson = new Gson();
      result = gson.toJson(temp);
      System.out.println(result);
    }
    else if (format.equalsIgnoreCase("output")) {
      result = asm.getOutput();
    }
     return ok(
      state.render(result)
     );
   }
 
   public static Result getRegisters() {
     return ok(
       registers.render()
     );
   }
   
 }
