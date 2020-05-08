 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package test;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import model.Invoice;
import org.mozilla.javascript.*;
 
 /**
  *
  * @author Dave
  */
 public class Testing {
 static Context cx ;
 static ScriptableObject scope;
     public static void main(String[] args) throws IOException {
         try {
 
             /*
              * 1. Read the javascript file
              */
             String inputFile = readFile("C:\\Users\\Dave\\Documents\\NetBeansProjects\\Invoice2XML\\src\\java\\rules\\Rules.js");
             /*
              * 2. Initialize Rhino and evaluate
              */
             //Creates and enters a Context. A Context stores information about the execution environment of a script.
             cx = Context.enter();
             //Initializes the standard objects (Object, Function, etc.). This must be done before scripts can be executed.
             scope = cx.initStandardObjects();
             //evaluate: The inputFile will be associated with the scope
             cx.evaluateString(scope, inputFile, "<cmd>", 1, null);
 
 
             Invoice i = new Invoice();
             i.setPrice(122);
             i.setRecipient("POST AG");
             /*
              * 3. set the local variables of the javascript file to the value of the invoice i
              */
             setVariables(i);
 
             /*
              * 4. Call the functions with the given input.
              */
             callFunctions();
 
 
 
             /*
              * 5. Save the incoming errorMessage from the javascript file into constraintViolations Set.
              */
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private static String readFile(String path) throws IOException {
         FileInputStream stream = new FileInputStream(new File(path));
         try {
             FileChannel fc = stream.getChannel();
             MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             /* Instead of using default, pass in a decoder. */
             return Charset.defaultCharset().decode(bb).toString();
         } finally {
             stream.close();
         }
     }
 
     private static void setVariables(Invoice i) {
         Object setVars = scope.get("setVariables", scope);
         if (!(setVars instanceof Function)) {
             System.err.println("f is undefined or not a function.");
         } else {
             Object functionArgs[] = {i.getPrice(), i.getRecipient()};
             Function f = (Function) setVars;
             f.call(cx, scope, scope, functionArgs);
         
             System.out.println(scope.get("price", scope));
             System.out.println(scope.get("recipient", scope));
         }
     }
     
       private static void callFunctions() {
         Object functionArgs[] = {}; //no arguments are needed. SEE .js file
         
         //call functions
         Function price = (Function) scope.get("priceRule", scope);
          System.out.println(Context.toString(price.call(cx, scope, scope, functionArgs)));
         
         Function recipient = (Function) scope.get("recipientRule", scope);
          System.out.println(Context.toString(recipient.call(cx, scope, scope, functionArgs)));
 
     }
 }
