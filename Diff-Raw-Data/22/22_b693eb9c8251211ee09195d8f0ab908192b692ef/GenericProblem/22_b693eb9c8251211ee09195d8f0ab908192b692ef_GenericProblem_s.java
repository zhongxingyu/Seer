 package de.ubercode.javaunitevolution.core;
 
 import java.lang.reflect.*;
 import java.util.*;
 
 import org.jgap.*;
 import org.jgap.gp.*;
 import org.jgap.gp.impl.*;
 
 /**
  * A genetic programming problem configuration that will attempt to set useful
  * operations and operands from the method that is to be evolved.
  */
 class GenericProblem extends GPProblem {
     private Method methodToEvolve;
     private List<Method> operations;
 
     /**
      * Creates a genetic programming problem that will generate operands from
      * the method that is to be evolved and operations from the available
      * operation methods respectively.
      * @param config The GP configuration to use.
      * @param methodToEvolve The method that is to be evolved. Will be used to
      *                       generate the operands of the problem.
      * @param operations Methods that can be used by the algorithm to implement
      *                   the methods. Will be used to generate the operations
      *                   of the problem.
      * @throws InvalidConfigurationException
      */
     public GenericProblem(GPConfiguration config, Method methodToEvolve,
                           List<Method> operations)
             throws InvalidConfigurationException {
         super(config);
         this.methodToEvolve = methodToEvolve;
         this.operations = operations;
     }
 
     @Override
     public GPGenotype create() throws InvalidConfigurationException {
         // Generate operands from the method's interface
         Class<?>[] types = {
                 PrimitiveUtils.toCommandGene(methodToEvolve.getReturnType())
         };
 
         List<Class<?>> argTypesList = new LinkedList<Class<?>>();
        for (Class<?> argType: methodToEvolve.getParameterTypes())
             argTypesList.add(PrimitiveUtils.toCommandGene(argType));
 
         Class<?>[][] argTypes = new Class<?>[][] {
                 argTypesList.toArray(new Class<?>[argTypesList.size()])
         };
 
         // Generate operations from interface
         GPConfiguration config = getGPConfiguration();
         List<CommandGene> nodeSetOperations = new LinkedList<CommandGene>();
        for (Method operation: operations)
             nodeSetOperations.add(new GenericCommand(config, operation));
 
         CommandGene[][] nodeSets = { nodeSetOperations.toArray(
                 new CommandGene[nodeSetOperations.size()]) };
 
         return GPGenotype.randomInitialGenotype(config, types, argTypes,
                 nodeSets, JavaUnitEvolution.getMaxInitialNodes(), true);
     }
 }
