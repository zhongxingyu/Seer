 /* -*- c-basic-offset: 2; -*- */
 
 import java.util.Comparator;
 import java.util.PriorityQueue;
 
 public class AStar {
 
   public static final int INITIAL_PQ_CAPACITY = 59;
   protected CostFunction g, h;
   protected TerminationConditions.TerminationCondition termCond;
 
   private PriorityQueue<BoardState> candidates;
 
   public AStar(final CostFunction g, final CostFunction h, final TerminationConditions.TerminationCondition termCond) {
     this.g = g;
     this.h = h;
     this.termCond = termCond;
     // init PriorityQueue
     candidates = new PriorityQueue<BoardState>(
       INITIAL_PQ_CAPACITY, new Comparator<BoardState>() {
         @Override
         public int compare(final BoardState s, final BoardState t) {
           if (s.score == t.score) return 0;
           else return (s.score < t.score) ? -1 : 1;
         }
       });
   }
 
   public void findPath(final Board board, final Point destination) {
     candidates.clear();
     candidates.add(board.getBoardState());
     final Point origin = board.getRobotPosition();
 
     while (true) {
       // A* main
       BoardState curState = candidates.poll();
       Board curBoard = curState.board;
 
      System.out.println(curState.move);
      System.out.println(curBoard);
      System.out.println(curBoard.state);
      System.out.println(curBoard.getAvailableMoves());
 
       for (Robot.Move candMove : curBoard.getAvailableMoves()) {
         BoardState newState = curState.board.getBoardState();
         Board newBoard = newState.board;
         Point newPosition = newBoard.getRobotPosition();
         newState.parentState = curState;
         newState.copyVisits(curState);
         newState.move = candMove;
         newBoard.tick(candMove);
         newState.visits[newBoard.getRobotPosition().r][newBoard.getRobotPosition().c]++;
         newState.score =
           g.compute(newState, origin, newPosition) +
           h.compute(newState, newPosition, destination);
         candidates.add(newState);
 
        System.out.println(candMove + ": " + newState.score);
       }
 
       // Termination condition
       if (termCond.isTrue(candidates, curState)) break;
     }
   }
 
   /**
    * Interface for defining a cost function, e.g., g and h.
    */
   public interface CostFunction {
     public double compute(BoardState board, Point start, Point end);
   }
 }
