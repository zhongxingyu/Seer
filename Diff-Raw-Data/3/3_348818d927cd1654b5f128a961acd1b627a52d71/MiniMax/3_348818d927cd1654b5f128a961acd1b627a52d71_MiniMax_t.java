 package core.ai.searches;
 
 import core.ai.Action;
 import core.ai.Heuristic;
 import core.ai.InformedState;
 import core.ai.PlayersEnviroment;
 import core.ai.SearchMetrics;
 import core.ai.State;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MiniMax {
 
     private Heuristic heuristic;
     private PlayersEnviroment enviroment;
     private State newState;
     private double alpha;
     private double beta;
     private double bestEvaluation;
     private InformedState bestMove;
     private long initTime;
     private long stopTime;
     private static int expandedStates;
 
     public MiniMax(Heuristic heuristic, PlayersEnviroment enviroment) {
         this.heuristic = heuristic;
         this.enviroment = enviroment;
         this.expandedStates = 0;
     }
 
     public State getNewState() {
         return newState;
     }
 
     public InformedState searchNextState(InformedState state, int maxDepth) {
         setStartTime();
         initSearchParameters();
         for (InformedState child : getChilds(state)) {
             expandedStates++;
             if (bestMove == null) bestMove = child;
             alpha = Math.max(alpha, minimax(child, maxDepth, alpha, beta));
             if (alpha > bestEvaluation) {
                 bestMove = child;
                 bestEvaluation = alpha;
             }
         }
         setStopTime();
         return bestMove;
     }
 
     private void initSearchParameters() {
         alpha = -Double.MAX_VALUE;
         beta = Double.MAX_VALUE;
         bestEvaluation = -Double.MAX_VALUE;
         bestMove = null;
     }
 
     private double minimax(InformedState state, int maxDepth, double alpha, double beta) {
        expandedStates++;
         if (maxDepth <= 0 || isTerminalState(state))
             return heuristic.evaluate(state);
         if (isMyTurn(state))
             return executeMaxTurn(state, maxDepth, alpha, beta);
         else
             return executeMinTurn(state, maxDepth, alpha, beta);
     }
 
     private boolean isTerminalState(InformedState state) {
         return enviroment.isFinalState(state);
     }
 
     private boolean isMyTurn(InformedState state) {
         return enviroment.isTurnOf(state);
     }
 
     private double executeMaxTurn(InformedState state, int maxDepth, double alpha, double beta) {
         for (InformedState child : getChilds(state)) {
             alpha = Math.max(alpha, minimax(child, maxDepth - 1, alpha, beta));
             if (alpha >= beta) return beta;
         }
         return alpha;
     }
 
     private double executeMinTurn(InformedState state, int maxDepth, double alpha, double beta) {
         for (InformedState child : getChilds(state)) {
             beta = Math.min(beta, minimax(child, maxDepth - 1, alpha, beta));
             if (alpha >= beta) return alpha;
         }
         return beta;
     }
 
     private List<InformedState> getChilds(InformedState state) {
         List<InformedState> childs = new ArrayList<>();
         for (Action action : (List<Action>) enviroment.getApplicableActions(state))
             childs.add((InformedState) action.execute(state));
         return childs;
     }
 
     private void setStartTime() {
         this.initTime = System.currentTimeMillis();
     }
 
     private void setStopTime() {
         this.stopTime = System.currentTimeMillis();
     }
 
     public SearchMetrics getSearchMetrics() {
         SearchMetrics metrics = new SearchMetrics();
         metrics.setExpandedStates(expandedStates);
         metrics.setSearchTime(stopTime - initTime);
         return metrics;
     }
 }
