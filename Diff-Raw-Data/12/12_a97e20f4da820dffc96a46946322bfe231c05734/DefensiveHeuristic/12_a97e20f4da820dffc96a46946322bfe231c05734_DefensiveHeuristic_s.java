 package gameengine.players;
 
 import core.ai.Heuristic;
 import gameengine.model.Board;
 import gameengine.model.Chip;
 import gameengine.model.Queen;
 
 public class DefensiveHeuristic implements Heuristic<Board> {
 
     @Override
     public double evaluate(Board board) {
         int turnIndicator = toggelTurn(board.getTurnIndicator());
         if (isMyQueenDead(board, turnIndicator))
             return Double.NEGATIVE_INFINITY;
         if (isOtherQueenDead(board, turnIndicator))
             return Double.POSITIVE_INFINITY;

         //TODO fix the evaluation of the heuristic
         return 10;
     }
 
     private boolean isMyQueenDead(Board board, int turnIndicator) {
         Chip chip = board.getChips().get(turnIndicator);
         if (!isQueen(chip) || !isMine(chip, turnIndicator))
             return true;
         else return false;
     }
 
     private boolean isOtherQueenDead(Board board, int turnIndicator) {
         Chip chip = board.getChips().get(toggelTurn(turnIndicator));
         if (!isQueen(chip) || isMine(chip, turnIndicator))
             return true;
         else return false;
     }
 
     private boolean isQueen(Chip chip) {
         return (chip instanceof Queen);
     }
 
     private boolean isMine(Chip chip, int turnIndicator) {
         return (chip.getOwner().getTurnIndicator() == turnIndicator);
     }

    private int toggelTurn(int turnIndicator) {
        return (turnIndicator + 1) % 2;
    }
 }
