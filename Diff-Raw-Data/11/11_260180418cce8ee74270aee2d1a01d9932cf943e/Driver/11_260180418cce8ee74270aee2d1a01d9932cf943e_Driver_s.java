 package game.ai;
 
 import game.Command;
 import game.Ending;
 import game.State;
 import game.StaticConfig;
 import game.config.IDriverConfig;
 import game.config.SimpleSelectorConfig;
 import game.log.Log;
 import game.stepper.MultiStepper;
 import game.stepper.SingleStepper;
 import game.ui.SimulateWindow;
 import game.util.Scoring;
 import interrupt.ExitHandler;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 
 import util.FileCommands;
 import util.MathUtil;
 import util.Pair;
 
 /**
  * The main driver of the artifical intelligence.
  * 
  * @author Tillmann Rendel
  * @author Thomas Horstmeyer
  */
 public class Driver {
   public static final int PRIORITY_QUEUE_CAPACITY = 5000;
   
   public final String name;
   
   public final IDriverConfig dconfig;
   public final StaticConfig sconfig;
   public final State initialState;
   public final MultiStepper stepper;
   public Comparator<State> comparator = new FitnessComparator();
   
   public Fitness fitness;
   public Selector strategySelector;
 
   public State bestState;
 
   public boolean finished = false;
   public boolean timed = false;
   public int lifeTime = 0;
   public long endTime;
 
   public long lastPrintInfoTime;
   public int lastPrintInfoIter;
   
   public int allIterations = 0;
   public int allLivestates = 0;
 
   public Driver(String name, IDriverConfig dconfig, StaticConfig sconfig, State initialState) {
     this.name = name;
     this.dconfig = dconfig;
     this.sconfig = sconfig;
     this.initialState = initialState;
     this.strategySelector = dconfig.strategySelector(sconfig, initialState);
     this.fitness = dconfig.fitnessFunction(sconfig, initialState);
     this.stepper = new MultiStepper(sconfig);
     this.timed = false;
   }
 
   /**
    * @param sconfig
    * @param initialState
    * @param strategySelector
    * @param fitness
    * @param lifetime
    *          max runtime in seconds
    */
   public Driver(String name, IDriverConfig dconfig, StaticConfig sconfig, State initialState, int lifetime) {
     this.name = name;
     this.dconfig = dconfig;
     this.sconfig = sconfig;
     this.initialState = initialState;
     this.strategySelector = dconfig.strategySelector(sconfig, initialState);
     this.fitness = dconfig.fitnessFunction(sconfig, initialState);
     this.stepper = new MultiStepper(sconfig);
     this.lifeTime = lifetime;
     this.timed = true;
   }
   
   public void solveGreedy(State initial) {
     endTime = System.currentTimeMillis() + (1000 * lifeTime);
     
     PriorityQueue<State> liveStateQueue = new PriorityQueue<State>(PRIORITY_QUEUE_CAPACITY, comparator);
     HashMap<Integer, State> seenStates = new HashMap<Integer, State>();
 
     strategySelector.prepareState(initial);
     liveStateQueue.add(initial);
     initial.fitness = fitness.evaluate(initial);
 
     if (bestState == null)
       bestState = initial;
 
     // TODO when to stop?
 
     int iterations = 0;
 
     // choose k, M, G more cleverly
     // choose 5000k more cleverly
     // explain final solution (what strategies)
 
     printDataHeaderGreedy(iterations);
     while (!liveStateQueue.isEmpty()) {
       // TODO why is this needed?
       if (timed && System.currentTimeMillis() >= endTime) {
         printDataRowGreedy(iterations, bestState.score, liveStateQueue.size());
         break;
       }
 
       iterations++;
 
       State state = liveStateQueue.peek();
 
       if (iterations % 5000 == 0) {
         printDataRowGreedy(iterations, bestState.score, liveStateQueue.size());
         
       }
 
       Strategy strategy = strategySelector.selectStrategy(state);
 
       if (strategy == null) {
         liveStateQueue.remove(state);
       } else {
         // apply strategy to create new state
         List<Command> commands = strategy.apply(state);
         strategy.applicationCount++;
 
         if (commands != null) {
           assert (!commands.isEmpty());
           State newState = computeNextState(state, commands, strategy);
           if (stateShouldBeConsidered(seenStates, newState)) {
             seenStates.put(newState.board.hashCode(), newState);
 
             if (newState.score > bestState.score) {
               bestState = newState;
             }
 
             if (newState.ending == Ending.None && newState.steps < newState.board.width * newState.board.height && Scoring.maximalReachableScore(newState) > bestState.score) {
               newState.fitness = fitness.evaluate(newState);
               liveStateQueue.add(newState);
               strategySelector.prepareState(newState);
             }
           }
         }
       }
     }
     
     allIterations += iterations;
     allLivestates += liveStateQueue.size();
   }
 
 
   public void solveEvolutionary(State initial) {
 //    int maxNumStates = 20000;
     int maxNumStates = (int) (10000000 * 2 / sconfig.maxStratsAprox);
     Log.println(maxNumStates);
     
     endTime = System.currentTimeMillis() + (1000 * lifeTime);
     
     ArrayList<State> liveStates = new ArrayList<State>(PRIORITY_QUEUE_CAPACITY);
     
     strategySelector.prepareState(initial);
     initial.fitness = fitness.evaluate(initial);
     liveStates.add(initial);
     double liveStatesAverageFitness = initial.fitness;
 
     if (bestState == null)
       bestState = initial;
 
     int iterations = 0;
     int keptStatesCount = 0;
     int newStatesCount = 0;
 
     printDataHeaderEvolutionary();
     
     while (!liveStates.isEmpty()) {
       if (timed && System.currentTimeMillis() >= endTime) {
         // timeout for testing
         printDataRowEvolutionary(iterations, bestState.score, liveStates.size(), keptStatesCount, newStatesCount, (int) liveStatesAverageFitness);
         break;
       }
 
       iterations++;
       printDataRowEvolutionary(iterations, bestState.score, liveStates.size(), keptStatesCount, newStatesCount, (int) liveStatesAverageFitness);
 
       ArrayList<State> newStates = new ArrayList<State>(PRIORITY_QUEUE_CAPACITY);
       double newStatesFitnessNormalized = 0;
       double newStatesNormalizer = liveStates.size();
       
       double size = liveStates.size();
       double spaceFactor = (double) 0.5*maxNumStates / size;
 
       for (int i = 0; i < liveStates.size(); ) {
         State state = liveStates.get(i);
 
         Strategy strategy = strategySelector.selectStrategy(state);
         
         if (strategy == null) {
           liveStates.remove(i);
           // do not increase 'i'
         } else {
           i++;
           
           // apply strategy to create new state
           List<Command> commands = strategy.apply(state);
           strategy.applicationCount++;
           
           if (commands != null) {
             assert (!commands.isEmpty());
 
             State newState = computeNextState(state, commands, strategy);
             
 //            if (stateShouldBeConsidered(newState)) {
 //              seenStates.put(newState.board.hashCode(), newState);
               
               if (newState.score > bestState.score) {
                 bestState = newState;
               }
               
               if (newState.ending == Ending.None &&
                   newState.steps < newState.board.width * newState.board.height && 
                   Scoring.maximalReachableScore(newState) > bestState.score) {
                 newState.fitness = fitness.evaluate(newState);
                 
                 double fitnessFactor = newState.fitness / liveStatesAverageFitness;
                 boolean keep = fitnessFactor * spaceFactor > MathUtil.RANDOM.nextDouble()
                                || liveStates.size() < 50;
                 
                 if (keep) {
                   // the kid is better than the average of all parents
                   newStates.add(newState);
                   newStatesFitnessNormalized += newState.fitness / newStatesNormalizer;
                   strategySelector.prepareState(newState);
                 }
               }
 //            }
           }
         }
       }
       
       double newStatesAverageFitness = newStatesFitnessNormalized * newStatesNormalizer / newStates.size();
       
       newStatesCount = newStates.size();
       size = liveStates.size() + newStatesCount;
       double averageFitness = (liveStatesAverageFitness * (liveStates.size() / size) + newStatesAverageFitness * (newStatesCount / size));
       
       spaceFactor = (double) 0.5*maxNumStates / size;
       
       liveStatesAverageFitness = 0;
       for (int i = 0; i < liveStates.size(); i++) {
         State s = liveStates.get(i);
         double fitnessFactor = s.fitness / averageFitness;
         boolean keep = spaceFactor * fitnessFactor > MathUtil.RANDOM.nextDouble()
                        || liveStates.size() < 50;
         if (keep) {
           newStates.add(s);
           liveStatesAverageFitness += s.fitness / liveStates.size();
         }
       }
       
       keptStatesCount = newStates.size() - newStatesCount;
       
       if (newStates.size() > newStatesCount) {
         liveStatesAverageFitness = liveStatesAverageFitness * (liveStates.size() / keptStatesCount);
         liveStatesAverageFitness = (newStatesAverageFitness * (newStatesCount / size) + liveStatesAverageFitness * (liveStates.size() / size));
       }
       else {
         liveStatesAverageFitness = newStatesAverageFitness;
       }
         
       liveStates = newStates;
     }
     
     printDataRowEvolutionary(iterations, bestState.score, liveStates.size(), keptStatesCount, newStatesCount, (int) liveStatesAverageFitness);
     
     allIterations += iterations;
     allLivestates += liveStates.size();
   }
 
   private void printDataHeaderGreedy(int iterations) {
     Log.printf(" iter    |  score  |  live   |  iter/sec  \n");
     Log.printf(" --------+---------+---------+------------\n");
 
     lastPrintInfoIter = iterations;
     lastPrintInfoTime = System.currentTimeMillis();
   }
 
   private void printDataRowGreedy(int iterations, int score, int liveStates) {
     long now = System.currentTimeMillis();
     int iterPerSec = 1000 * (iterations - lastPrintInfoIter) / (int) (now - lastPrintInfoTime+1);
 
     Log.printf("%7d  |  %5d  |  %5d  |  %7d  \n",
         iterations,
         bestState.score,
         liveStates,
         iterPerSec);
 
     lastPrintInfoIter = iterations;
     lastPrintInfoTime = now;
 
   }
 
   private void printDataHeaderEvolutionary() {
     Log.printf(" iter    |  score  |  live   |  kept   |  new    | fitness \n");
     Log.printf(" --------+---------+---------+---------+---------+---------\n");
   }
 
   private void printDataRowEvolutionary(int iterations, int score, int liveStates, int keptStates, int newStates, int fitness) {
     Log.printf("%7d  |  %5d  |  %5d  |  %5d  |  %5d  |  %7d  \n",
         iterations,
         bestState.score,
         liveStates,
         keptStates,
         newStates,
         fitness);
   }
 
   /**
    * Print known best solution to System.out
    */
   public void printSolution() {
     // TODO: make threadsafe, might be called from exit handler
     // while already outputting... (use StringBuilder perhaps?!)
 
     // print statistics
     Log.printf("\nStrategy applications:\n");
     for (Strategy strategy : strategySelector.getUsedStrategies()) {
       Log.printf("  %3dk %s\n", strategy.applicationCount / 1000, strategy);
     }
     Log.println();
 
     // print solution
     StringBuilder sb = new StringBuilder();
     if (bestState.solution != null) {
       Command[] commands = bestState.solution.allCommands();
       for (Command command : commands) {
         sb.append(command.shortName());
       }
     }
     sb.append("A");
     System.out.println(sb);
     System.out.flush();
   }
 
   public void simulateSolution() {
     if (bestState.solution != null) {
       SingleStepper stepper = new SingleStepper(sconfig);
       State st = initialState;
       st.fitness = fitness.evaluate(st);
       Log.println(st);
       Log.println();
 
       List<Solution> sols = new LinkedList<Solution>();
       Solution sol = bestState.solution;
       while (sol != null) {
         sols.add(0, sol);
         sol = sol.prefix;
       }
       
       for (Solution s : sols) {
         Log.println(s.strategy + ": " + Arrays.toString(s.commands));
         for (Command command : s.commands) {
           st = stepper.step(st, command);
           st.fitness = fitness.evaluate(st);
         }
       }
       
       Log.println(st);
       Log.println();
     }
   }
   
   public void simulationWindow() {
     State st = initialState;
     st.fitness = fitness.evaluate(st);
     SimulateWindow win = new SimulateWindow(name, fitness, stepper);
     win.addState(st);
     if (bestState.solution != null) {
 	    Command[] commands = bestState.solution.allCommands();
 	
 	    for (Command command : commands) {
 	      st = stepper.step(st, command);
 	      st.fitness = fitness.evaluate(st);
 	      st.solution = bestState.solution;
 	      win.addState(st);
 	    }
     }
     win.setVisible(true);
   }
 
   private State computeNextState(State state, List<Command> commands, Strategy strategy) {
     State result = stepper.multistep(state, commands);
     result.strats++;
     int stepsPerformed = result.steps - state.steps;
     Command[] usedCommands = new Command[stepsPerformed];
     for (int i = 0; i < stepsPerformed; i++)
       usedCommands[i] = commands.get(i);
     result.solution = new Solution(state.solution, usedCommands, strategy);
     return result;
   }
 
   public void finished() {
     this.finished = true;
     printSolution();
     simulateSolution();
   }
 
   public static Driver create(String name, IDriverConfig dconfig, StaticConfig sconfig, State state) {
     Selector selector = dconfig.strategySelector(sconfig, state);
     Fitness fitness = dconfig.fitnessFunction(sconfig, state);
     return new Driver(name, dconfig, sconfig, state);
   }
 
   public static Driver create(String name, IDriverConfig dconfig, StaticConfig sconfig, State state, int lifetime) {
     Selector selector = dconfig.strategySelector(sconfig, state);
     Fitness fitness = dconfig.fitnessFunction(sconfig, state);
     return new Driver(name, dconfig, sconfig, state, lifetime);
   }
 
   public void run() {
     
     ExitHandler.register(this);
 
     // try greedy for 2 seconds
     int lifeTime = this.lifeTime;
     this.lifeTime = 2;
     solveGreedy(initialState);
     this.lifeTime = lifeTime;
     
     Log.println();
     if (bestState != null)
       sconfig.maxStepsAprox = Math.min(
           2 * bestState.steps, 
           initialState.board.width * initialState.board.height);
     strategySelector = dconfig.strategySelector(sconfig, initialState);
     fitness = dconfig.fitnessFunction(sconfig, initialState);
     solveEvolutionary(initialState);
     
     ExitHandler.unregister(this);
     finished();
   }
 
   private boolean stateShouldBeConsidered(Map<Integer, State> seenStates, State s) {
     State oldState = seenStates.get(s.board.hashCode());
 
     if (oldState == null)
       return true;
 
     // anderes board -> behalten
     if (!s.board.equals(oldState.board))
       return true;
 
     // TODO: theoretisch koennen noch mehr verworfen werden, wenn wir mehr
     // aufheben... vllt nicht den aufwand wert.
 
     // gleiches board, weniger steps -> behalten
     if (s.steps < oldState.steps)
       return true;
 
     // gleiches board, weniger stepsUnderwater -> behalten
     if (s.stepsUnderwater < oldState.stepsUnderwater)
       return true;
 
     return false;
   }
 
   // TODO add exception handling?
   public static void main(String[] args) throws IOException {
     if (args.length > 0) {
       LinkedList<File> files = new LinkedList<File>();
       files.add(new File(args[0]));
       
       while (!files.isEmpty()) {
         File file = files.poll();
         if (file.isDirectory())
           for (File other : file.listFiles())
             files.add(other);
         else
           main(file.getName(), FileCommands.readFileAsString(file.getAbsolutePath()));
       }
     } else {
       String text = FileCommands.readAsString(new InputStreamReader(System.in));
       main("stdin", text);
     }
   }
   
   public static void main(String name, String text) {
     Pair<StaticConfig, State> p = State.parse(text);
     StaticConfig sconfig = p.a;
     State state = p.b;
     
     IDriverConfig stdConfig = new SimpleSelectorConfig();
 
    Driver d = Driver.create(name, stdConfig, sconfig, state, 10);
     d.run();
     d.simulationWindow();    
   }
 }
