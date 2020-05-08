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
    //Test t = new Test();
     System.out.println("APP: " + command);
     return ok(
       interpret.render(y86.processCMD(command))
     );
   }
 
  public static Result getState() {
    Test t = new Test();
     y86 asm = new y86();
    ArrayList temp = asm.getState();
    Gson gson = new Gson();
    String JSON = gson.toJson(temp);
    System.out.println(JSON);
     return ok(
      state.render(JSON)
     );
   }
 
   public static Result getRegisters() {
     return ok(
       registers.render()
     );
   }
   
 }
