 package veeju.repl;
 
 import java.io.*;
 import veeju.parser.Parser;
 import veeju.forms.Form;
 import veeju.runtime.Object;
 import veeju.runtime.Runtime;
 
 /**
  * Read-eval-print loop.
  */
 public final class Repl {
     /**
      * The lobby environment {@link Object}.
      */
     protected final Object environment;
 
     /**
      * Creates a {@link Repl} instance with an empty environment.
      */
     public Repl() {
         this(new Runtime().getRuntimeModule());
     }
 
     /**
      * Creates a {@link Repl} instance with the {@code environment}.
      *
      * @param environment the lobby environment.
      */
     public Repl(final Object environment) {
         this.environment = environment;
     }
 
     /**
      * Prints the {@code ">>> "} prompt and reads a string from the standard
      * input.
      *
      * @return a read string.
      */
     public String read() {
         return read(">>> ");
     }
 
     /**
      * Prints the {@code prompt} and reads a string from the standard input.
      *
      * @param prompt a prompt string e.g. {@code ">>> "}.
      * @return a read string.
      */
     public String read(final String prompt) {
         System.out.print(prompt);
         BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
         try {
             return r.readLine();
         } catch (IOException e) {
             return "";
         }
     }
 
     /**
      * Evaluates a Veeyu code string then returns the result {@link Object}.
      *
      * @param code a Veeyu code string to evaluate.
      * @return the result object.
      */
     public Object evaluate(final String code) {
         final Form form = (Form) Parser.parseForm(code).getNode();
         return form.evaluate(environment);
     }
 
     /**
      * Prints the result {@code object}.
      *
      * @param object an object to print.
      */
     public void print(final Object object) {
         System.out.print("==> ");
         System.out.println(object.toString());
     }
 
     /**
      * Read-evaluate-print loop.
      */
     public void loop() {
         while (true) {
             final String input = read();
             if (input == null) {
                 System.out.println();
                 break;
             }
             if (input.equals("")) continue;
             print(evaluate(input));
         }
     }
 
     /**
      * Let's rock with REPL!
      *
      * @param args ignored arguments.
      */
     public static void main(String[] args) {
         final Repl repl = new Repl();
         repl.loop();
     }
 }
 
