 /*
  * Test.java
  * 
  */
 
 package no.hist.aitel.chess.board;
 
 /**
  *
  * @author martin
  */
 
 public class Test {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Board b  = new Board();
 
         b.movePiece(11, 27);
         b.movePiece(48, 40);
         b.movePiece(3, 19);
         b.movePiece(49, 41);
 //        b.movePiece(19, 35);
         b.movePiece(14, 22);
         b.movePiece(55, 47);
        b.movePiece(19, 23);
 
 
         System.out.println(b.toString());
 //        System.out.println(getRank(63));
 
         // 56  57  58  59  60  61  62  63
         // 48  49  50  51  52  53  54  55
         // 40  41  42  43  44  45  46  47
         // 32  33  34  35  36  37  38  39
         // 24  25  26  27  28  29  30  31
         // 16  17  18  19  20  21  22  23
         // 8   9   10  11  12  13  14  15
         // 0   1   2   3   4   5   6   7
     }
 
 
 }
 
