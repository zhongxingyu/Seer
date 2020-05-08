 package jbehave.core;
 
 import java.io.PrintStream;
 import java.io.PrintWriter;
 
 import jbehave.core.behaviour.BehaviourClass;
 import jbehave.core.listener.PlainTextListener;
 import jbehave.core.util.Timer;
 
 /**
  * This is the entry point to run one or more {@link BehaviourClass}.
  * 
  * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
  */
 public class Run {
     private boolean succeeded = true;
     private final PrintWriter writer;
 
     public Run(PrintStream out) {
         this.writer = new PrintWriter(out);
     }
 
     public static void main(String[] args) {
         try {
             Run run = new Run(System.out);
             for (int i = 0; i < args.length; i++) {
                 run.verifyBehaviour(Class.forName(args[i]));
             }
             System.exit(run.succeeded() ? 0 : 1);
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     public boolean succeeded() {
         return succeeded;
     }
 
     public void verifyBehaviour(Class classToVerify) {
         PlainTextListener textListener = new PlainTextListener(new PrintWriter(writer), new Timer());
         new BehaviourClass(classToVerify).verifyTo(textListener);
         textListener.printReport();
        succeeded = succeeded && !textListener.hasBehaviourFailures();
     }
 }
