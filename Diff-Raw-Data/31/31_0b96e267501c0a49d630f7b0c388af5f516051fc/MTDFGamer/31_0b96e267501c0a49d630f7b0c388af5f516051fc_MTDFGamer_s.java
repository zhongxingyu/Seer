 package player.gamer.statemachine.eggplant;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import player.gamer.statemachine.StateMachineGamer;
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.Role;
 import util.statemachine.StateMachine;
 import util.statemachine.exceptions.GoalDefinitionException;
 import util.statemachine.exceptions.MoveDefinitionException;
 import util.statemachine.exceptions.TransitionDefinitionException;
 import util.statemachine.implementation.prover.cache.CachedProverStateMachine;
 import apps.player.config.ConfigPanel;
 import apps.player.detail.DetailPanel;
 
 public class MTDFGamer extends AlphaBetaGamer {
   private EggplantConfigPanel config = new EggplantConfigPanel();
   private final long GRACE_PERIOD = 200;
   // TODO: Hashcode is NOT overridden by GDLSentence - this will only check if
   // the sentences are actually the same objects in memory
 
   @Override
   public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
     long start = System.currentTimeMillis();
 
     leafNodesSearched = statesSearched = 0;
     cacheHits = cacheMisses = 0;
 
     ValuedMove result = iterativeDeepening(getStateMachine(), getCurrentState(), getRole(), 0, 100, timeout - GRACE_PERIOD);
 
     long stop = System.currentTimeMillis();
    System.out.println(statesSearched);
     notifyObservers(new EggplantMoveSelectionEvent(result.move, result.value, stop - start, statesSearched, leafNodesSearched, cacheHits,
         cacheMisses));
     return result.move;
   }
   
   protected ValuedMove iterativeDeepening(StateMachine machine, MachineState state, Role role, int alpha, int beta, long endTime)
   throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
     ValuedMove bestMove = new ValuedMove(-1, machine.getRandomMove(state, role));
     try {
       for (int depth = 1; ; depth++) {
         expansionEvaluator = new DepthLimitedExpansionEvaluator(depth);
         //ValuedMove move = mtdf(machine, state, role, bestMove.value == -1 ? 50 : bestMove.value, 0, getCache(), endTime);
        ValuedMove move = memoizedAlphaBeta(machine, state, role, alpha, beta, 0, new HashMap<MachineState, CacheValue>(), endTime);
        if (move.value > bestMove.value)
          bestMove = move;
        System.out.println("After depth " + depth + "; best = " + bestMove + " " + statesSearched);
       }
     }
     catch (TimeUpException ex) {
     }
     return bestMove;
   }
 
   private ValuedMove mtdf(
       StateMachine machine,
       MachineState state,
       Role role,
       int initialGuess,
       int depth, 
       HashMap<MachineState, CacheValue> cache,
       long endTime)
   throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, TimeUpException {
     ValuedMove currMove = new ValuedMove(initialGuess, null); // initial guess
     int lowerBound = 0;
     int upperBound = 100;
     while (lowerBound < upperBound) {
       int guess = currMove.value;
       if (currMove.value == lowerBound)
         guess++;
       currMove = memoizedAlphaBeta(machine, state, role, guess - 1, guess, depth, cache, endTime);
       if (currMove.value < guess) {
         upperBound = currMove.value;
       }
       else {
         lowerBound = currMove.value;
       }
     }
     return currMove;
   }
 
   /* Alternate implementation of alpha beta
   private ValuedMove memoizedAlphaBeta(
       StateMachine machine,
       MachineState state,
       Role role,
       int alpha,
       int beta,
       HashMap<MachineState, CacheValue> cache)
   throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
     if (cache != null) {
       if (cache.containsKey(state)) {
         // Cache stores lower-bound in alpha, upper-bound in beta
         CacheValue cached = cache.get(state);
         if (cached.alpha >= beta) {
           cacheHits++;
           return cached.valuedMove;
         }
         if (cached.beta <= alpha) {
           cacheHits++;
           return cached.valuedMove;
         }
         if (cached.alpha > alpha)
           alpha = cached.alpha;
         if (cached.beta < beta)
           beta = cached.beta;
       }
       cacheMisses++;
 
       ValuedMove maxMove = alphaBeta(machine, state, role, alpha, beta, cache);
       if (maxMove.value <= alpha) {
         if (cache.containsKey(state)) {
           cache.get(state).beta = maxMove.value;
         }
         else {
           cache.put(state, new CacheValue(maxMove, -1, maxMove.value));
         }
       }
       else if (maxMove.value > alpha && maxMove.value < beta) {
         if (cache.containsKey(state)) {
           cache.get(state).alpha = cache.get(state).beta = maxMove.value;
         }
         else {
           cache.put(state, new CacheValue(maxMove, maxMove.value, maxMove.value));
         }
       }
       else if (maxMove.value >= beta) {
         if (cache.containsKey(state)) {
           cache.get(state).alpha = maxMove.value;
         }
         else {
           cache.put(state, new CacheValue(maxMove, maxMove.value, 101));
         }
       }
       return maxMove;
     } else {
       return alphaBeta(machine, state, role, alpha, beta, cache);
     }
   }
 
   private ValuedMove alphaBeta(StateMachine machine,
       MachineState state,
       Role role,
       int alpha,
       int beta,
       HashMap<MachineState, CacheValue> cache)
   throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
     statesSearched++;
     if (machine.isTerminal(state)) {
       leafNodesSearched++;
       return new ValuedMove(machine.getGoal(state, role), null);
     }
 
     ValuedMove maxMove = new ValuedMove(-1, null);
     if (machine.isTerminal(state)) {
       leafNodesSearched++;
       maxMove = new ValuedMove(machine.getGoal(state, role), null);
     }
     else {
       List<Move> possibleMoves = machine.getLegalMoves(state, role);
       //			Collections.shuffle(possibleMoves); // TODO: Remove this line
       for (Move move : possibleMoves) {
         List<List<Move>> jointMoves = machine.getLegalJointMoves(state, role, move);
         //				Collections.shuffle(jointMoves); // TODO: Remove this line
         int min = 101;
         int newBeta = beta;
         for (List<Move> jointMove : jointMoves) {
           MachineState nextState = machine.getNextState(state, jointMove);
           int value = memoizedAlphaBeta(machine, nextState, role, alpha, newBeta, cache).value;
           if (value < min) {
             min = value;
             if (min <= alpha)
               break;
             if (min < newBeta)
               newBeta = min;
           }
         }
         if (min > maxMove.value) {
           maxMove.value = min;
           maxMove.move = move;
           if (maxMove.value >= beta)
             break;
           if (maxMove.value > alpha)
             alpha = maxMove.value;
         }
       }
     }
     return maxMove;
   }
    */
   @Override
   public StateMachine getInitialStateMachine() {
     return new CachedProverStateMachine();
   }
 
   @Override
   public String getName() {
     return "MTD(f)";
   }
 
   @Override
   public DetailPanel getDetailPanel() {
     return new EggplantDetailPanel();
   }
 
   @Override
   public ConfigPanel getConfigPanel() {
     return config;
   }
 
   private HashMap<MachineState, CacheValue> getCache() {
     HashMap<MachineState, CacheValue> cache = null;
     if (config.useCache()) {
       cache = new HashMap<MachineState, CacheValue>();
       // cache = keptCache;
     }
     return cache;
   }
 }
