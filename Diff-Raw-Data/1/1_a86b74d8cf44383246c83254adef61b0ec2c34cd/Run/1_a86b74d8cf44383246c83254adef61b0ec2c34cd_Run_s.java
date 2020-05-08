 package codemoo;
 
 import bsh.Interpreter;
 import bsh.EvalError;
 
 public class Run {
     public static void main(String[] args) {
         if (args.length < 1) {
             System.out.println("No Code Given to Run!");
             return;
         }
         if (Preload.interpret == null) {
             System.out.println("Interpreter Not Set Up!");
             return;
         }
         try {
             Preload.interpret.eval(args[0]);
         }
         catch (bsh.EvalError e) {
             System.out.print("Threw " + e);
         }
         return;
     }
 }
