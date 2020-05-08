 
 import jaima.util.Metrics;
 import jaima.game.Player;
 import jaima.game.strategies.*;
 import com.game.pente.*;
 
 /**
  * Write a description of class ticTacToe here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Pente
 {
     private Pente() { }
     
     public static void main(String[] args)
     {
         System.out.print("\f");
         Metrics.clearAll();
         
         Strategy[] strategies = {
 //             new Guess(),
 //            new Ask("Bill"),
             new MiniMax(3),
           new AlphaBeta(1),
 //             new Guess(),
 //            new Ask("Bill"),
 //            new MiniMax(3),
 //             new AlphaBeta(9),
         };
         
         int i;
         penteState board = new penteState("XXXXXXXXWXXXWXBXXXXBXXXXX");
         board.setTurn(0);
         Player p = board.toMove();
         while(board.terminal() == false) {
             System.out.println(board.prettyPrint());
             Strategy s = strategies[board.getTurn()];
             jaima.game.Move m = s.choose(board);
             String string = new String();
             string = s.toString();
             string += "'s move: ";
             string += m.prettyPrint();
             string += "\n";
             System.out.println(string);
             i = board.getTurn();
             board = board.result(m);
             board.setTurn(1 - i);
             p = board.toMove();
         }
         
         System.out.println(board.prettyPrint());
         
         String announce = "Game result: tie";
         if(board.utility(p) > 0) {
             announce = "Winner is " + strategies[0];
         } else if(board.utility(p) < 0) {
             announce = "Winner is " + strategies[1];
         }
         System.out.println(announce);
         System.out.println(Metrics.getAll());
     }
}
