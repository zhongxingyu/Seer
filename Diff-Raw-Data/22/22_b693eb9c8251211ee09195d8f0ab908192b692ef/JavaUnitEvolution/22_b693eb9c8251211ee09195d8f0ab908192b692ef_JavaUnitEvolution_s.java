 package de.ubercode.javaunitevolution.core;
 
 import java.lang.reflect.*;
 import java.util.*;
 
 import javassist.util.proxy.*;
 
 import org.apache.log4j.*;
 import org.jgap.*;
 import org.jgap.gp.*;
 import org.jgap.gp.impl.*;
 
 /**
  * The API of the Java Unit Evolution framework.
  */
 public class JavaUnitEvolution {
     static IGPProgram currentProgram;
     private static Logger LOGGER = Logger.getLogger(JavaUnitEvolution.class);
     private static GPGenotype gp;
     private static Method methodToEvolve;
     private static List<Method> operations;
     private static boolean finished = false;
     private static int timeout = 300000;
     private static int populationSize = 500;
     private static int maxInitDepth = 6;
     private static int maxCrossoverDepth = 17;
     private static int maxInitialNodes = 20;
 
     /**
      * Evolves an implementation of the supplied abstract class using the
      * supplied test class as a fitness measure. This method should be called
      * from a test class and the returned object should be tested. When the
      * test class is executed in any test runner, an implementation will be
      * evolved and tested automatically.
      * @param classToEvolve The abstract class whose implementation is to be
      *                      evolved. Its first abstract method will be evolved
      *                      while all others are ignored, it is therefore
      *                      recommended to include only one abstract method in
      *                      the signature. All other methods have to be static
      *                      and will be used by the evolved implementation to
      *                      pass the tests.
      * @param testClass The JUnit 4 test class whose test cases will be used to
      *                  measure the fitness of each evolved implementation.
      *                  It is vital for the success of the evolutionary process
      *                  that this class contains sufficient reasonable test
      *                  cases.
      * @return The evolved implementation of the class.
      */
     public static <T> T evolve(Class<T> classToEvolve, Class<?> testClass) {
         if (gp == null) {
             // This is executed if the evolutionary process has not yet begun.
             methodToEvolve = null;
             operations = new LinkedList<Method>();
            for (Method method: classToEvolve.getDeclaredMethods()) {
                 int modifiers = method.getModifiers();
                 if (Modifier.isPublic(modifiers)
                     && Modifier.isAbstract(modifiers)) {
                     if (methodToEvolve != null)
                         LOGGER.warn("Evolution of only one method is"
                                     + "allowed, ignoring " + method);
                     else
                         methodToEvolve = method;
                 } else if (Modifier.isPublic(modifiers)
                            && Modifier.isStatic(modifiers))
                     operations.add(method);
                 else
                     LOGGER.warn("Ignored method " + method);
             }
             if (methodToEvolve == null)
                 throw new RuntimeException("Unable to find a method suitable"
                                            + "for evolution on "
                                            + classToEvolve + ", such methods "
                                            + "have to be public and abstract");
 
             LOGGER.info("The following Method will be evolved: "
                         + methodToEvolve.toString());
 
             if (operations.isEmpty())
                 throw new RuntimeException("Unable to find any operations on "
                                            + classToEvolve + ", such methods "
                                            + "have to be public and static");
 
             StringBuilder operationsStringBuilder = new StringBuilder();
            for (Method operation: operations) {
                 operationsStringBuilder.append(operation.toString());
                 if (operation != operations.get(operations.size() - 1))
                     operationsStringBuilder.append(", ");
             }
             LOGGER.info("The following operations will be used: "
                         + operationsStringBuilder.toString());
             
             try {
                 GPProblem problem = new GenericProblem(createConfig(testClass),
                                                        methodToEvolve,
                                                        operations);
                 gp = problem.create();
                 gp.setVerboseOutput(true);
                 evolveProgram();
                 finished = true;
             } catch (InvalidConfigurationException e) {
                 throw new RuntimeException("Invalid configuration", e);
             }
         }
 
         if (!finished)
             /*
              * This is the implementation returned to each test case
              * during the fitness function's execution.
              */
             return createImplementation(currentProgram.getChromosome(0),
                                         classToEvolve);
 
         // This is the final implementation, capable of passing all unit tests. 
         // TODO: Find a way to store the implementation in a .class file.        
         return createImplementation(gp.getAllTimeBest().getChromosome(0),
                                     classToEvolve);
     }
 
     private static GPConfiguration createConfig(Class<?> testClass)
     throws InvalidConfigurationException {		
         GPConfiguration config = new GPConfiguration();
         config.setGPFitnessEvaluator(new DeltaGPFitnessEvaluator());
         config.setPopulationSize(populationSize);
         config.setMaxInitDepth(maxInitDepth);
         config.setMaxCrossoverDepth(maxCrossoverDepth);
         config.setFitnessFunction(new GenericFitnessFunction(testClass));
         config.setStrictProgramCreation(true);
         return config;
     }
 
     private static void evolveProgram()
         throws InvalidConfigurationException {
         LOGGER.info("Evolving an implementation of " + methodToEvolve
                     + ", this might take a while... (Timeout: " + timeout
                     + " ms)");
         EvolutionThread et = new EvolutionThread(gp);
         TimeoutThread tt = new TimeoutThread(timeout, et);
         et.start();
         tt.start();
         try {
             et.join();
         } catch (InterruptedException e) {
             throw new RuntimeException("Unexpected thread interruption", e);
         }
         tt.interrupt();
         if (et.isFinished())
             LOGGER.info("Program evolved.");
         else
             LOGGER.info("Unable to evolve a workable program within the "
                         + "specified amount of time. Try increasing the "
                         + "timeout or supplying more test cases.");
     }
 
     private static <T> T createImplementation(ProgramChromosome chromosome,
                                               Class<T> clazz) {
         ProxyFactory factory = new ProxyFactory();
         factory.setSuperclass(clazz);
         factory.setFilter(
                 new MethodFilter() {
                     @Override
                     public boolean isHandled(Method method) {
                         return Modifier.isAbstract(method.getModifiers());
                     }
                 }
             );
 
         try {
             MethodHandler handler =
                 new ProgramChromosomeMethodHandler(chromosome, methodToEvolve);
             return clazz.cast(factory.create(new Class<?>[0], new Object[0],
                                              handler));
         } catch (Exception e) {
             throw new RuntimeException("Unable to invoke generated program", e);
         }		
     }
 
     /**
      * @return The maximum number of milliseconds the evolutionary process may
      *         last.
      */
     public static int getTimeout() {
         return timeout;
     }
 
     /**
      * @param timeout The maximum number of milliseconds the evolutionary
      *                process may last.
      */
     public static void setTimeout(int timeout) {
         JavaUnitEvolution.timeout = timeout;
     }
 
     /**
      * @return The size of the genetic programming population.
      */
     public static int getPopulationSize() {
         return populationSize;
     }
 
     /**
      * @param populationSize The size of the genetic programming population.
      */
     public static void setPopulationSize(int populationSize) {
         JavaUnitEvolution.populationSize = populationSize;
     }
 
     /**
      * @return The maximum depth of initial program trees.
      */
     public static int getMaxInitDepth() {
         return maxInitDepth;
     }
 
     /**
      * @param maxInitDepth The maximum depth of initial program trees.
      */
     public static void setMaxInitDepth(int maxInitDepth) {
         JavaUnitEvolution.maxInitDepth = maxInitDepth;
     }
 
     /**
      * @return The maximum depth of subtrees of the program tree that can be
      *         used for crossover.
      */
     public static int getMaxCrossoverDepth() {
         return maxCrossoverDepth;
     }
 
     /**
      * @param maxCrossoverDepth The maximum depth of subtrees of the program
      *                          tree that can be used for crossover.
      */
     public static void setMaxCrossoverDepth(int maxCrossoverDepth) {
         JavaUnitEvolution.maxCrossoverDepth = maxCrossoverDepth;
     }
 
     /**
      * @return maxInitialNodes The maximum number of nodes of initial program
      *                         trees.
      */
     public static int getMaxInitialNodes() {
         return maxInitialNodes;
     }
 
     /**
      * @param maxInitialNodes The maximum number of nodes of initial program
      *                        trees.
      */
     public static void setMaxInitialNodes(int maxInitialNodes) {
         JavaUnitEvolution.maxInitialNodes = maxInitialNodes;
     }
 }
