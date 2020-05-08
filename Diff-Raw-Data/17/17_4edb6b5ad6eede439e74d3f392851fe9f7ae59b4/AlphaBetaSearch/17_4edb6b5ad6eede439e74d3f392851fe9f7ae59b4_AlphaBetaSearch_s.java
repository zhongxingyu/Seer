 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package itschess;
 
 /**
  *
  * @author Ian
  */
 public class AlphaBetaSearch {
 
     public static byte depth = 0;
     static byte[][] possibleMoves;
     static Board board;
     public static String[] moves = new String[4];
      
     public static void alphaBetaSearch()
     {
         byte a = -100; byte b = 100;//should these be ints or Integers?
         board = new Board();
         int v = maxValue(a, b);
         
     }
     
     public static byte maxValue(byte a, byte b)
     {//a = alpha, b = beta
         byte v = -100;//initial max value
         byte mv = -99;//holds the current max
         while(!board.done)
         {
            if(depth != 3)//Begining by testing only to depth 3
             {
             	
                 board.move(depth);
                 depth++;
                 mv = minValue(a, b);
             }
             else
             {
             	depth--;
                 mv = (byte) board.eval();
                 return mv;
             }
             
             if(mv > v)
             {
                 moves[depth] = board.chessMoves[depth];
                 v = mv;
             }
             if(v >= b)      
             {
                 board.undo(depth);
                 return v;  
             }
             if(v > a)
                 a = v;
             
             
             board.undo(depth);
             //depth--;
         }
         depth--;
         //board.done = false;
         return v;
     }
     
     public static byte minValue(byte a, byte b)
     {//a = alpha, b = beta
         byte v = 100;//initial min value
         byte mv = 99;//holds the current min
         while(!board.done)
         {
            if(depth != 3)//Begining by testing only to depth 3
             {
                 board.move(depth);
                 depth++;
                 mv = maxValue(a, b);
             }
             else
             {	
                 depth--;
                 mv = (byte)board.eval();
                 return mv;
             }
             
             if(mv < v)
             {
                 moves[depth] = board.chessMoves[depth];
                 v = mv;
             }
             if(v <= a)      
             {
                 board.undo(depth);
                 depth--;
                 return v;
             }
             if(v < b)
                 b = v;
             
             
             board.undo(depth);
             //depth--;
         }
         
         depth--;
         //board.done = false;
         return v;
     }
     
     public byte utility(Board board)
     {
         //return 200(k) + 9(q) + 5(r) + 3(b+n) + (p) - 0.5(d+s+i) + 0.1(m);
         return 0;
     }
     
     
 
 }
