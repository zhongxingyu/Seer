 /* -*- c-basic-offset: 2; -*- */
 
 import java.util.*;
 import java.io.*;
 
 public class AStar {
 
   public static final int INITIAL_PQ_CAPACITY = 59;
   protected CostFunction g, h;
   protected TerminationConditions.TerminationCondition termCond;
 
   public AStar(CostFunction g, CostFunction h, TerminationConditions.TerminationCondition termCond) {
     this.g = g;
     this.h = h;
     this.termCond = termCond;
     // init PriorityQueue
     candidates = new PriorityQueue<BoardState>(
       INITIAL_PQ_CAPACITY, new Comparator<BoardState>() {
         public int compare(BoardState s, BoardState t) {
           if (s.score == t.score) return 0;
           else return (s.score < t.score) ? -1 : 1;
         }
       });
   }
 
   private PriorityQueue<BoardState> candidates;
   private Board board;
   private Point destination;
 
   public void findPath(Board board, Point destination) {
     candidates.clear();
 
     this.board = board;
     this.destination = destination;
     candidates.add(board.getBoardState());
 
     while (true) {
       // A* main
       BoardState nextMove = candidates.poll();
       board.update(nextMove);
       for (BoardState candMove : board.getAvailableMoves()) {
         candMove.parentState = nextMove;
         candMove.score =
           g.compute(board, board.getRobotPosition(), candMove.position) +
           h.compute(board, candMove.position, destination);
         candidates.add(candMove);
       }
 
       // Termination condition
      if (terminationCondition.isTrue(candidates, board, lastMove)) break;
     }
   }
 
   /**
    * Interface for defining a cost function, e.g., g and h.
    */
   public interface CostFunction {
     public double compute(Board board, Point start, Point end);
   }
 }
