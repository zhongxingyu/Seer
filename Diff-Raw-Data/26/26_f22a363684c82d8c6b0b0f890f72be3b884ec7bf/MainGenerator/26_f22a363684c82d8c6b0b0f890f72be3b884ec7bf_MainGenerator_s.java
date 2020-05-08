 package osmo.tester.generator;
 
 import osmo.tester.generator.algorithm.GenerationAlgorithm;
 import osmo.tester.generator.strategy.ExitStrategy;
 import osmo.tester.generator.testsuite.TestSuite;
 import osmo.tester.log.Logger;
 import osmo.tester.model.FSM;
 import osmo.tester.model.FSMTransition;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * The main test generator class.
  * Takes as input the finite state machine model parsed by {@link osmo.tester.parser.MainParser}.
  * Runs test generation on this model using the defined algorithms, exit strategies, etc.
  *
  * @author Teemu Kanstren
  */
 public class MainGenerator {
   private static Logger log = new Logger(MainGenerator.class);
   /** Test generation history. Initialized from the given model to enable sharing the object with model and generator. */
   private TestSuite testLog = null;
   /** The set of enabled transitions in the current state is passed to this algorithm to pick one to execute. */
   private final GenerationAlgorithm algorithm;
   /** Defines when test suite generation should be stopped. Invoked between each test case. */
   private final ExitStrategy suiteStrategy;
   /** Defines when test case generation should be stopped. Invoked between each test step. */
   private final ExitStrategy testStrategy;
 
   /**
    * Constructor.
    *
    * @param algorithm The set of enabled transitions in the current state is passed to this algorithm to pick one to execute.
    * @param suiteStrategy Defines when test suite generation should be stopped. Invoked between each test case.
    * @param testStrategy Defines when test case generation should be stopped. Invoked between each test step.
    */
   public MainGenerator(GenerationAlgorithm algorithm, ExitStrategy suiteStrategy, ExitStrategy testStrategy) {
     this.algorithm = algorithm;
     this.suiteStrategy = suiteStrategy;
     this.testStrategy = testStrategy;
   }
 
   /**
    * Invoked to start the test generation using the configured parameters.
    *
    * @param fsm Describes the test model in an FSM format.
    */
   public void generate(FSM fsm) {
     testLog = fsm.getTestSuite();
     //TODO: check sanity of testSize, steps, etc.
     log.debug("Starting test suite generation");
     beforeSuite(fsm);
    while (!suiteStrategy.exitNow(testLog, false)) {
       log.debug("Starting new test generation");
       beforeTest(fsm);
      while (!testStrategy.exitNow(testLog, true)) {
         List<FSMTransition> enabled = getEnabled(fsm);
         //TODO: throw exception if no suitable one available + add tests
         FSMTransition next = algorithm.choose(testLog, enabled);
         log.debug("Taking transition "+next.getName());
         execute(fsm, next);
       }
       afterTest(fsm);
       log.debug("Finished new test generation");
     }
     afterSuite(fsm);
     log.debug("Finished test suite generation");
   }
 
   private void beforeSuite(FSM fsm) {
     Collection<Method> befores = fsm.getBeforeSuites();
     invokeAll(befores, "@BeforeSuite", fsm);
   }
 
   private void afterSuite(FSM fsm) {
     Collection<Method> afters = fsm.getAfterSuites();
     invokeAll(afters, "@AfterSuite", fsm);
   }
 
   private void beforeTest(FSM fsm) {
     //update history
     testLog.startTest();
     Collection<Method> befores = fsm.getBefores();
     invokeAll(befores, "@Before", fsm);
   }
 
   private void afterTest(FSM fsm) {
     Collection<Method> afters = fsm.getAfters();
     invokeAll(afters, "@After", fsm);
     //update history
     testLog.endTest();
   }
 
   /**
    * Invokes the given set of methods on the target test object.
    *
    * @param methods The methods to be invoked.
    * @param name  The annotation name describing the methods, used in error messages.
    * @param fsm The model object on which to invoke the methods.
    */
   private void invokeAll(Collection<Method> methods, String name, FSM fsm) {
     for (Method method : methods) {
       try {
         method.invoke(fsm.getModel());
       } catch (Exception e) {
         throw new RuntimeException("Error while calling "+name+" method ("+method.getName()+")", e);
       }
     }
   }
 
   /**
    * Goes through all {@link osmo.tester.annotation.Transition} tagged methods in the given test model object,
    * invokes all associated {@link osmo.tester.annotation.Guard} tagged methods matching those transitions,
    * returning the set of {@link osmo.tester.annotation.Transition} methods that have no guards returning a value
    * of {@code false}.
    *
    * @param fsm Describes the test model.
    * @return The list of enabled {@link osmo.tester.annotation.Transition} methods.
    */
   private List<FSMTransition> getEnabled(FSM fsm) {
     Object obj = fsm.getModel();
     Collection<FSMTransition> allTransitions = fsm.getTransitions();
     List<FSMTransition> enabled = new ArrayList<FSMTransition>();
     enabled.addAll(allTransitions);
     for (FSMTransition transition : allTransitions) {
       for (Method guard : transition.getGuards()) {
         try {
           Boolean result = (Boolean)guard.invoke(obj);
           if (!result) {
             enabled.remove(transition);
           }
         } catch (Exception e) {
           throw new RuntimeException("Failed to invoke guard method.", e);
         }
       }
     }
     if (enabled.size() == 0) {
       throw new IllegalStateException("No transition available.");
     }
     return enabled;
   }
 
   /**
    * Executes the given transition on the given model.
    *
    * @param fsm The model on which to execute the transition.
    * @param transition  The transition to be executed.
    */
   public void execute(FSM fsm, FSMTransition transition) {
     Method method = transition.getTransition();
     try {
       method.invoke(fsm.getModel());
     } catch (Exception e) {
       throw new RuntimeException("Exception while running transition ('"+transition.getName()+"'):", e);
     }
     testLog.add(transition);
   }
 
   public TestSuite getTestLog() {
     return testLog;
   }
 }
