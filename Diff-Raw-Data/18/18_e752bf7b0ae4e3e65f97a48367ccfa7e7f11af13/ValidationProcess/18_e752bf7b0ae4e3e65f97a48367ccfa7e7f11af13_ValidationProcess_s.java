 package model;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.NativeArray;
import sun.org.mozilla.javascript.internal.Scriptable;
 
 /**
  * This class initiates the validation process. As an input it takes the
  * Rules.js where the rules are defined and an Invoice i, a plain java object.
  * Then it validates the values of the Invoice i based on the rules in Rules.js.
  * If there are any errors, they will be saved into a Set or List. That way,
  * wicket can take those errors and output them. This happens in the
  * InvoiceForm.java.
  *
  * @author Dave, e0726371
  */
 public class ValidationProcess {
 
     Object[] errorMessages;
     Context cx = Context.enter();
     Scriptable scope = cx.initStandardObjects();
 
     public ValidationProcess() {
     }
 
     /**
      * The Method reads a JavaScript document, where rules for Validation are
      * definied. Then it validates the bean (Invoice i) according to the rules
      * in the JavaScript file.
      *
      * @param filename is the JavaScript document that contains the rules
      * @param i is the Invoice that will be validated
      */
     public void validate(String filename, Invoice i) {
         try {
 
             /*
              * 1. Read the javascript file
              */
            String inputFile = readFile("C:\\Users\\Dave\\Documents\\NetBeansProjects\\Invoice2XML\\src\\java\\rules\\" + filename);
 
             /*
              * 2. Initialize Rhino and evaluate
              */
             //Creates and enters a Context. A Context stores information about the execution environment of a script.
             cx = Context.enter();
             //Initializes the standard objects (Object, Function, etc.). This must be done before scripts can be executed.
             scope = cx.initStandardObjects();
             //evaluate: The inputFile will be associated with the scope
             cx.evaluateString(scope, inputFile, "<cmd>", 1, null);
 
 
             /*
              * 3. set the local variables of the javascript file to the value of the invoice i
              */
             setVariables(i);
 
             /*
              * 4. Call the functions with the given input and saves errors from javascript into errorMessages Array.
              */
             callFunctions();
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * readFile() is an efficient Method to read a file into a String.
      *
      * @param path is the path of the file
      * @return String returns a String containing the characters of the given
      * file.
      */
     private String readFile(String path) throws IOException {
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
 
     /**
      * setVariables() sets the local javascript variables to the values of the
      * invoice that was filled in the form. Scope.get() gets the Objects from
      * the javascript file and scope.put() updates to objects according to the
      * values of your invoice.
      *
      * @param i is the Invoice
      */
     private void setVariables(Invoice i) {
         //get and set price
         Object a = scope.get("price", scope);
         a = i.getPrice();
         scope.put("price", scope, a);
 
         //get and set recipient
         Object b = scope.get("recipient", scope);
         b = i.getRecipient();
         scope.put("recipient", scope, b);
 
         //get and set details
         Object c = scope.get("details", scope);
         c = i.getDetails();
         scope.put("details", scope, c);
 
         //get and set tax
         Object d = scope.get("tax", scope);
         d = i.getTax();
         scope.put("tax", scope, d);
     }
 
     /**
      * callFunctions() calls the callFunctions() Method inside the javascript
      * file. Returning errors will be saved in the errorMessages String.
      */
     private void callFunctions() {
         Object functionArgs[] = {}; //no arguments are needed. SEE .js file
 
         Function callFunctions = (Function) scope.get("callFunctions", scope);
         Object result = callFunctions.call(cx, scope, scope, functionArgs);
         NativeArray arr = (NativeArray) result;
         errorMessages = new Object[(int) arr.getLength()];
         for (Object o : arr.getIds()) {
             int index = (Integer) o;
             errorMessages[index] = arr.get(index, null);
         }
     }
 
     public Object[] geterrorMessages() {
         return errorMessages;
     }
 
     public void seterrorMessages(Object[] eM) {
         this.errorMessages = eM;
     }
 }
