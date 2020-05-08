 //=============================================================================
 // Copyright 2006-2010 Daniel W. Dyer
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //=============================================================================
 package com.myrontuttle.evolve;
 
 //import java.security.GeneralSecurityException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import java.util.Random;
 
 
 /**
  * Base class for {@link EvolutionEngine} implementations.
  * @param <T> The type of entity evolved by the evolution engine.
  * @author Daniel Dyer
  * @see CandidateFactory
  * @see FitnessEvaluator
  */
 public abstract class AbstractEvolutionEngine<T> implements EvolutionEngine<T>
 {
     // A single multi-threaded worker is shared among multiple evolution engine instances.
     private static FitnessEvaluationWorker concurrentWorker = null;
     private static ExpressionWorker concurrentExpressionWorker = null;
 
     private final Set<EvolutionObserver<? super T>> observers = new CopyOnWriteArraySet<EvolutionObserver<? super T>>();
 
     private final Random rng;
     private final CandidateFactory<T> candidateFactory;
     private final FitnessEvaluator<? super T> fitnessEvaluator;
     private final ExpressionStrategy<T> expressionStrategy;
     private final ExpressedFitnessEvaluator<T> expressedFitnessEvaluator;
 
     private volatile boolean singleThreaded = false;
 
     protected List<TerminationCondition> satisfiedTerminationConditions;
     private TerminationCondition[] terminationConditions;
     private int currentGenerationIndex;
     private long startTime;
 
 
     /**
      * Creates a new evolution engine by specifying the various components required by
      * an evolutionary algorithm.
      * @param candidateFactory Factory used to create the initial population that is
      * iteratively evolved.
      * @param fitnessEvaluator A function for assigning fitness scores to candidate
      * solutions.
      * @param expressionStrategy A strategy for expressing candidates.
      * @param rng The source of randomness used by all stochastic processes (including
      * evolutionary operators and selection strategies).
      */
     protected AbstractEvolutionEngine(CandidateFactory<T> candidateFactory,
                                       ExpressedFitnessEvaluator<T> expressedFitnessEvaluator,
                                       ExpressionStrategy<T> expressionStrategy,
                                       Random rng) {
         this.candidateFactory = candidateFactory;
         this.expressedFitnessEvaluator = expressedFitnessEvaluator;
         this.expressionStrategy = expressionStrategy;
         this.rng = rng;
 
         this.fitnessEvaluator = null;
     }
 
     /**
      * Creates a new evolution engine by specifying the various components required by
      * an evolutionary algorithm.
      * @param candidateFactory Factory used to create the initial population that is
      * iteratively evolved.
      * @param fitnessEvaluator A function for assigning fitness scores to candidate
      * solutions.
      * @param expressionStrategy A strategy for expressing candidates.
      * @param rng The source of randomness used by all stochastic processes (including
      * evolutionary operators and selection strategies).
      */
     protected AbstractEvolutionEngine(CandidateFactory<T> candidateFactory,
                                       FitnessEvaluator<? super T> fitnessEvaluator,
                                       Random rng) {
         this.candidateFactory = candidateFactory;
         this.fitnessEvaluator = fitnessEvaluator;
         this.expressionStrategy = null;
         this.expressedFitnessEvaluator = null;
         this.rng = rng;
     }
     
     protected int getCurrentGenerationIndex() {
     	return currentGenerationIndex;
     }
     
     protected long getStartTime() {
     	return startTime;
     }
     
     protected TerminationCondition[] getTerminationConditions() {
     	return terminationConditions;
     }
     
     /**
      * {@inheritDoc}
      */
     public T evolve(int populationSize,
                     int eliteCount,
                     TerminationCondition... conditions) {
         return evolve(populationSize,
                       eliteCount,
                       Collections.<T>emptySet(),
                       conditions);
     }
 
 
     /**
      * {@inheritDoc}
      */
     public T evolve(int populationSize,
                     int eliteCount,
                     Collection<T> seedCandidates,
                     TerminationCondition... conditions)
     {
         return evolvePopulation(populationSize,
                                 eliteCount,
                                 seedCandidates,
                                 conditions).get(0).getCandidate();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<EvaluatedCandidate<T>> evolvePopulation(int populationSize,
                                                         int eliteCount,
                                                         TerminationCondition... conditions)
     {
         return evolvePopulation(populationSize,
                                 eliteCount,
                                 Collections.<T>emptySet(),
                                 conditions);
     }
 
 
     /**
      * {@inheritDoc}
      */
     public List<EvaluatedCandidate<T>> evolvePopulation(int populationSize,
                                                         int eliteCount,
                                                         Collection<T> seedCandidates,
                                                         TerminationCondition... conditions) {
         if (eliteCount < 0 || eliteCount >= populationSize) {
             throw new IllegalArgumentException("Elite count must be non-negative and less than population size.");
         }
         if (conditions.length == 0) {
             throw new IllegalArgumentException("At least one TerminationCondition must be specified.");
         }
 
         satisfiedTerminationConditions = null;
         currentGenerationIndex = 0;
         startTime = System.currentTimeMillis();
 
         List<T> population = candidateFactory.generateInitialPopulation(populationSize,
                                                                         seedCandidates,
                                                                         rng);
         
         List<EvaluatedCandidate<T>> evaluatedPopulation;
         if (includeExpression()) {
 
             // Express each candidate in the population
             List<ExpressedCandidate<T>> expressedPopulation = expressPopulation(population);
             
             ExpressedPopulation<T> expressedStats = 
             		new ExpressedPopulation<T>(
             				expressedPopulation,
             				fitnessEvaluator.isNatural(),
             				expressedPopulation.size(),
             				eliteCount,
             				currentGenerationIndex,
             				startTime);
             
             notifyPopulationExpressed(expressedPopulation, expressedStats);
             
             //Calculate the fitness scores for each member of the expressed population.
             evaluatedPopulation = evaluateExpressedPopulation(expressedPopulation);
         } else {
 
             //Calculate the fitness scores for each member of the population.
         	evaluatedPopulation = evaluatePopulation(population);
         }
         EvolutionUtils.sortEvaluatedPopulation(evaluatedPopulation, fitnessEvaluator.isNatural());
         PopulationStats<T> stats = EvolutionUtils.getPopulationStats(evaluatedPopulation,
                                                   fitnessEvaluator.isNatural(),
                                                   eliteCount,
                                                   currentGenerationIndex,
                                                   startTime);
         
         // Notify observers of the state of the population.
         notifyPopulationChange(stats);
 
         List<TerminationCondition> satisfiedConditions = EvolutionUtils.shouldContinue(stats, conditions);
         while (satisfiedConditions == null)
         {
             ++currentGenerationIndex;
             evaluatedPopulation = nextEvolutionStep(evaluatedPopulation, eliteCount, rng);
             EvolutionUtils.sortEvaluatedPopulation(evaluatedPopulation, fitnessEvaluator.isNatural());
             stats = EvolutionUtils.getPopulationStats(evaluatedPopulation,
                                                     fitnessEvaluator.isNatural(),
                                                     eliteCount,
                                                     currentGenerationIndex,
                                                     startTime);
             // Notify observers of the state of the population.
             notifyPopulationChange(stats);
             satisfiedConditions = EvolutionUtils.shouldContinue(stats, conditions);
         }
         this.satisfiedTerminationConditions = satisfiedConditions;
         return evaluatedPopulation;
     }
     
 
     /**
      * Evolves an existing list of expressed candidates to the next expression of
      * candidates.
      */
     public List<ExpressedCandidate<T>> evolveToExpression(
     										ExpressedPopulation<T> pop,
     										int populationSize,
             								TerminationCondition... conditions) {
     	
     	if (pop.getExpressedCandidates().isEmpty()) {
     		 if (pop.getEliteCount() < 0 || pop.getEliteCount() >= populationSize) {
     			 throw new IllegalArgumentException("Elite count must be non-negative and less than population size.");
     		 }
     		 if (conditions.length == 0) {
     			 throw new IllegalArgumentException("At least one TerminationCondition must be specified.");
     		 }
 
     		 satisfiedTerminationConditions = null;
     		 currentGenerationIndex = 0;
     		 startTime = System.currentTimeMillis();
 
     		 List<T> population = candidateFactory.generateInitialPopulation(populationSize,
     	                                                                        rng);
     		 // Express each candidate in the population
              List<ExpressedCandidate<T>> expressedPopulation = expressPopulation(population);
              
              ExpressedPopulation<T> expressedStats = 
              		new ExpressedPopulation<T>(
              				expressedPopulation,
              				fitnessEvaluator.isNatural(),
              				expressedPopulation.size(),
              				pop.getEliteCount(),
              				currentGenerationIndex,
              				startTime);
              
              notifyPopulationExpressed(expressedPopulation, expressedStats);
              
              return expressedPopulation;
     	} else {
 
    		this.terminationConditions = conditions;
 
         	++currentGenerationIndex;
 
             return nextExpressionStep(pop.getExpressedCandidates(), pop.getEliteCount(), rng);
     	}
     }
 
     /**
      * Indicates whether to include gene expression in evolution
      * @return Indicates gene expression use
      */
     protected boolean includeExpression() {
     	if (expressionStrategy != null && expressedFitnessEvaluator != null) {
     		return true;
     	} else {
     		return false;
     	}
     }
     
     /**
      * This method performs a single step/iteration of the evolutionary process.
      * @param evaluatedPopulation The population at the beginning of the process.
      * @param eliteCount The number of the fittest individuals that must be preserved.
      * @param rng A source of randomness.
      * @return The updated population after the evolutionary process has proceeded
      * by one step/iteration.
      */
     protected abstract List<EvaluatedCandidate<T>> nextEvolutionStep(List<EvaluatedCandidate<T>> evaluatedPopulation,
                                                                      int eliteCount,
                                                                      Random rng);
 
     /**
      * This method performs a single step/iteration of the evolutionary process up to
      * the expression of the candidates.
      * @param expressedPopulation The population at the beginning of the process.
      * @param eliteCount The number of the fittest individuals that must be preserved.
      * @param rng A source of randomness.
      * @return The updated population after the evolutionary process has proceeded
      * by one step/iteration.
      */
     protected abstract List<ExpressedCandidate<T>> nextExpressionStep(List<ExpressedCandidate<T>> expressedPopulation,
                                                                      int eliteCount,
                                                                      Random rng);
 
     /**
      * Takes a population and expresses each member based on an ExpressionStrategy
      * @param population The population to express
      * @return The expressed population
      */
     protected List<ExpressedCandidate<T>> expressPopulation(List<T> population) {
         List<ExpressedCandidate<T>> expressedPopulation = 
         		new ArrayList<ExpressedCandidate<T>>(population.size());
 
         if (singleThreaded) {
         	// Do fitness evaluations on the request thread.
             for (T candidate : population) {
             	expressedPopulation.add(expressionStrategy.express(candidate));
             }
         } else {
             // Divide the required number of expressions equally among the
             // available processors and coordinate the threads so that we do not
             // proceed until all threads have finished processing.
             try {
                 List<Future<ExpressedCandidate<T>>> results = 
                 		new ArrayList<Future<ExpressedCandidate<T>>>(population.size());
                 // Submit tasks for execution and wait until all threads have finished 
                 // expressions.
                 for (T candidate : population) {
                     results.add(
                     	getSharedExpressionWorker().submit(
                     		new ExpressionTask<T>(expressionStrategy,
                                                   candidate)));
                 }
                 for (Future<ExpressedCandidate<T>> result : results) {
                     expressedPopulation.add(result.get());
                 }
             } catch (ExecutionException ex) {
                 throw new IllegalStateException("Expression task execution failed.", ex);
             } catch (InterruptedException ex) {
                 // Restore the interrupted status, allows methods further up the call-stack
                 // to abort processing if appropriate.
                 Thread.currentThread().interrupt();
             }
         }
         
         return expressedPopulation;
     }
 
     /**
      * Takes a population, assigns a fitness score to each member and returns
      * the members with their scores attached, sorted in descending order of
      * fitness (descending order of fitness score for natural scores, ascending
      * order of scores for non-natural scores).
      * @param population The population to evaluate (each candidate is assigned
      * a fitness score).
      * @return The evaluated population (a list of candidates with attached fitness
      * scores).
      */
     protected List<EvaluatedCandidate<T>> evaluatePopulation(List<T> population) {
         List<EvaluatedCandidate<T>> evaluatedPopulation = new ArrayList<EvaluatedCandidate<T>>(population.size());
 
         if (singleThreaded)  {
         	// Do fitness evaluations on the request thread.
             for (T candidate : population) {
                 evaluatedPopulation.add(new EvaluatedCandidate<T>(candidate,
                                                                   fitnessEvaluator.getFitness(candidate, population)));
             }
         } else {
             // Divide the required number of fitness evaluations equally among the
             // available processors and coordinate the threads so that we do not
             // proceed until all threads have finished processing.
             try {
                 List<T> unmodifiablePopulation = Collections.unmodifiableList(population);
                 List<Future<EvaluatedCandidate<T>>> results = new ArrayList<Future<EvaluatedCandidate<T>>>(population.size());
                 // Submit tasks for execution and wait until all threads have finished fitness evaluations.
                 for (T candidate : population) {
                     results.add(getSharedWorker().submit(new FitnessEvalutationTask<T>(fitnessEvaluator,
                                                                                        candidate,
                                                                                        unmodifiablePopulation)));
                 }
                 for (Future<EvaluatedCandidate<T>> result : results) {
                     evaluatedPopulation.add(result.get());
                 }
             } catch (ExecutionException ex) {
                 throw new IllegalStateException("Fitness evaluation task execution failed.", ex);
             } catch (InterruptedException ex) {
                 // Restore the interrupted status, allows methods further up the call-stack
                 // to abort processing if appropriate.
                 Thread.currentThread().interrupt();
             }
         }
 
         return evaluatedPopulation;
     }
 
     /**
      * Takes a population, assigns a fitness score to each member and returns
      * the members with their scores attached, sorted in descending order of
      * fitness (descending order of fitness score for natural scores, ascending
      * order of scores for non-natural scores).
      * @param population The population to evaluate (each candidate is assigned
      * a fitness score).
      * @return The evaluated population (a list of candidates with attached fitness
      * scores).
      */
     protected List<EvaluatedCandidate<T>> evaluateExpressedPopulation(List<ExpressedCandidate<T>> population) {
         List<EvaluatedCandidate<T>> evaluatedPopulation = new ArrayList<EvaluatedCandidate<T>>(population.size());
 
         if (singleThreaded)  {
         	// Do fitness evaluations on the request thread.
             for (ExpressedCandidate<T> candidate : population) {
                 evaluatedPopulation.add(new EvaluatedCandidate<T>(candidate,
                             expressedFitnessEvaluator.getFitness(candidate, population)));
             }
         } else {
             // Divide the required number of fitness evaluations equally among the
             // available processors and coordinate the threads so that we do not
             // proceed until all threads have finished processing.
             try {
                 List<ExpressedCandidate<T>> unmodifiablePopulation = Collections.unmodifiableList(population);
                 List<Future<EvaluatedCandidate<T>>> results = new ArrayList<Future<EvaluatedCandidate<T>>>(population.size());
                 // Submit tasks for execution and wait until all threads have finished fitness evaluations.
                 for (ExpressedCandidate<T> candidate : population) {
                     results.add(getSharedWorker().submit(new ExpressedFitnessEvalutationTask<T>(expressedFitnessEvaluator,
                                                                                        candidate,
                                                                                        unmodifiablePopulation)));
                 }
                 for (Future<EvaluatedCandidate<T>> result : results) {
                     evaluatedPopulation.add(result.get());
                 }
             } catch (ExecutionException ex) {
                 throw new IllegalStateException("Fitness evaluation task execution failed.", ex);
             } catch (InterruptedException ex) {
                 // Restore the interrupted status, allows methods further up the call-stack
                 // to abort processing if appropriate.
                 Thread.currentThread().interrupt();
             }
         }
 
         return evaluatedPopulation;
     }
 
 
     /**
      * <p>Returns a list of all {@link TerminationCondition}s that are satisfied by the current
      * state of the evolution engine.  Usually this list will contain only one item, but it
      * is possible that mutliple termination conditions will become satisfied at the same
      * time.  In this case the condition objects in the list will be in the same order that
      * they were specified when passed to the engine.</p>
      *
      * <p>If the evolution has not yet terminated (either because it is still in progress or
      * because it hasn't even been started) then an IllegalStateException will be thrown.</p>
      *
      * <p>If the evolution terminated because the request thread was interrupted before any
      * termination conditions were satisfied then this method will return an empty list.</p>
      *
      * @throws IllegalStateException If this method is invoked on an evolution engine before
      * evolution is started or while it is still in progress.
      *
      * @return A list of statisfied conditions.  The list is guaranteed to be non-null.  The
      * list may be empty because it is possible for evolution to terminate without any conditions
      * being matched.  The only situation in which this occurs is when the request thread is
      * interrupted.
      */
     public List<TerminationCondition> getSatisfiedTerminationConditions()
     {
         if (satisfiedTerminationConditions == null)
         {
             throw new IllegalStateException("EvolutionEngine has not terminated.");
         }
         else
         {
             return Collections.unmodifiableList(satisfiedTerminationConditions);
         }
     }
 
 
     /**
      * Adds a listener to receive status updates on the evolution progress.
      * Updates are dispatched synchronously on the request thread.  Observers should
      * complete their processing and return in a timely manner to avoid holding up
      * the evolution.
      * @param observer An evolution observer call-back.
      * @see #removeEvolutionObserver(EvolutionObserver)
      */
     public void addEvolutionObserver(EvolutionObserver<? super T> observer)
     {
         observers.add(observer);
     }
 
 
     /**
      * Removes an evolution progress listener.
      * @param observer An evolution observer call-back.
      * @see #addEvolutionObserver(EvolutionObserver)
      */
     public void removeEvolutionObserver(EvolutionObserver<? super T> observer)
     {
         observers.remove(observer);
     }
 
 
     /**
      * Send the population stats to all registered observers.
      * @param stats Information about the current state of the population.
      */
     protected void notifyPopulationChange(PopulationStats<T> stats) {
         for (EvolutionObserver<? super T> observer : observers) {
             observer.populationUpdate(stats);
         }
     }
 
     /**
      * Send the expressed population to the Expression Strategy for extra processing
      * @param stats Information about the current state of the population.
      */
     protected void notifyPopulationExpressed(List<ExpressedCandidate<T>> expressedPopulation,
     											ExpressedPopulation<T> stats) {
         expressionStrategy.populationExpressed(expressedPopulation, stats);
     }
 
     /**
      * By default, fitness evaluations are performed on separate threads (as many as there are
      * available cores/processors).  Use this method to force evaluation to occur synchronously
      * on the request thread.  This is useful in restricted environments where programs are not
      * permitted to start or control threads.  It might also lead to better performance for
      * programs that have extremely lightweight/trivial fitness evaluations.
      * @param singleThreaded If true, fitness evaluations will be performed synchronously on the
      * request thread.  If false, fitness evaluations will be performed by worker threads.
      */
     public void setSingleThreaded(boolean singleThreaded)
     {
         this.singleThreaded = singleThreaded;
     }
 
     /**
      * Lazily create the multi-threaded worker for expressions.
      */
     private static synchronized ExpressionWorker getSharedExpressionWorker() {
         if (concurrentExpressionWorker == null) {
         	concurrentExpressionWorker = new ExpressionWorker();
         }
         return concurrentExpressionWorker;
     }
     
     /**
      * Lazily create the multi-threaded worker for fitness evaluations.
      */
     private static synchronized FitnessEvaluationWorker getSharedWorker()
     {
         if (concurrentWorker == null)
         {
             concurrentWorker = new FitnessEvaluationWorker();
         }
         return concurrentWorker;
     }
 }
