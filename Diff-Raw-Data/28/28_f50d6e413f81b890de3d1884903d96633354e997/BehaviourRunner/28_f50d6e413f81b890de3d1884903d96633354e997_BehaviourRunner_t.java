 package org.jbehave.core;
 
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.Writer;
 
 import org.jbehave.core.behaviour.BehaviourClass;
 import org.jbehave.core.behaviour.BehaviourVerifier;
 import org.jbehave.core.listener.BehaviourListener;
 import org.jbehave.core.listener.PlainTextListener;
 import org.jbehave.core.util.Timer;
 
 /**
  * This is the entry point to verify one or more {@link BehaviourClass}. Classes
  * are loaded using the ClassLoader injected, which defaults to the current
  * context ClassLoader.
  * 
  * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
  * @author Mauro Talevi
  * @see BehaviourListener
  */
 public class BehaviourRunner {
     private static final String EMPTY = "";
     private boolean succeeded = true;
     private final Writer writer;
     private final ClassLoader classLoader;
 
     public BehaviourRunner() {
         this(new PrintWriter(System.out));
     }
 
     /**
      * @deprecated Use BehaviourRunner(new PrintWriter(OutputStream)) 
      */
     public BehaviourRunner(OutputStream out) {
         this(new PrintWriter(out), Thread.currentThread().getContextClassLoader());
     }
 
     /**
      * @deprecated Use BehaviourRunner(new PrintWriter(OutputStream), ClassLoader) 
      */
     public BehaviourRunner(OutputStream out, ClassLoader classLoader) {
         this(new PrintWriter(out), classLoader);
     }
 
     public BehaviourRunner(Writer writer) {
         this(writer, Thread.currentThread().getContextClassLoader());
     }
 
     public BehaviourRunner(Writer writer, ClassLoader classLoader) {
         this.writer = writer;
         this.classLoader = classLoader;
     }
 
     public boolean succeeded() {
         return succeeded;
     }
 
     public void verifyBehaviour(String behaviourLocator) throws ClassNotFoundException {
         String className = behaviourLocator;
         String methodName = EMPTY;
         int index = behaviourLocator.indexOf(':');
         if (index >= 0) {
             className = behaviourLocator.substring(0, index);
             methodName = behaviourLocator.substring(index + 1);
         }
         verifyBehaviour(classLoader.loadClass(className), methodName);
     }
 
     public void verifyBehaviour(Class classToVerify) {
         verifyBehaviour(classToVerify, EMPTY);
     }
 
     public void verifyBehaviour(Class classToVerify, String methodName) {
         BehaviourListener listener = new PlainTextListener(writer, new Timer());
         BehaviourVerifier verifier = new BehaviourVerifier(listener);
         verifier.verifyBehaviour(new BehaviourClass(classToVerify, methodName, verifier));
         listener.printReport();
         succeeded = succeeded && !listener.hasBehaviourFailures();
     }
 
     public static void main(String[] args) {
         try {
             BehaviourRunner runner = new BehaviourRunner();
             for (int i = 0; i < args.length; i++) {
                 runner.verifyBehaviour(args[i]);
             }
             System.exit(runner.succeeded() ? 0 : 1);
         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
         }
     }
 
 }
