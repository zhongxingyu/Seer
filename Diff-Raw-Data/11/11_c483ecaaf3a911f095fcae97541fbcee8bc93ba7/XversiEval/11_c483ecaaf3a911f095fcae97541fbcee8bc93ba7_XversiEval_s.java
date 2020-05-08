 /*
 # This file XversiEval.java is part of xversi-java-parallel, a board game called reversi or othello.
 #
 # Copyright (c) 2002-2006 Chung Shin Yee <cshinyee@gmail.com>
 #
 #       http://cshinyee.blogspot.com/index.html
 #
 # This program is free software; you can redistribute it and/or
 # modify it under the terms of the GNU General Public License as
 # published by the Free Software Foundation; either version 2 of the
 # License, or (at your option) any later version.
 #
 # This program is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 # GNU General Public License for more details.
 #
 # You should have received a copy of the GNU General Public License
 # along with this program; if not, write to the Free Software
 # Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 # USA.
 #
 # The GNU General Public License is contained in the file COPYING.
 #
 */
 
 import java.rmi.* ;
 import java.rmi.server.* ;
 import java.rmi.registry.* ;
 
 
 /* evaluate a board */
 public class XversiEval extends UnicastRemoteObject implements XversiInterface {
  
   private static final long serialVersionUID = 2006L ;
 
   static Sema llock ;
 
   public int maxLevel = 5 ;
   public int lastnm = 13 ;
   private int side ;  
 
   /* create rmiregistry, rebind a remote object */
   public static void main(String[] args) {
     Power2.Init() ;
     String host = new String(args[0]) ;
     String port = new String(args[1]) ;
     llock = new Sema(1) ;
     try {
       LocateRegistry.createRegistry(Integer.parseInt(port)) ;
       XversiEval instance = new XversiEval() ;
       Naming.rebind("rmi://" + args[0] + ":"+port+"/xversiInterface" , instance) ;
       System.out.println("Registered XversiInterface") ;
 
     } catch (Exception ex) {
       System.out.println(ex) ;
     }
 
 
   }
 
 
   /* interface for requesting evaluation */
   public int getEvalScore(XversiBoard b , int c , int turn , int turnNum) throws RemoteException {
 
         
     int score ;
     score = Evaluate(b , c , turn , turnNum) ;
     System.out.println("score " + score) ;
     return score ;
 
   }
 
 
   public XversiEval() throws RemoteException { super() ; }
   
 
   /* n-level MinMax alpha-beta pruning evaluation */
   public int Evaluate(XversiBoard b , int c , int turn , int turnNum) {
 
     int score ;
 
     if(!XversiAgent.CheckSide(c))
       return -9999998 ;
 
     side = c ;
 
     if(turnNum > 64 - lastnm) return endEval(b, turn) ;
     // score = Eval(b , turn , 64 - turnNum , turnNum , -9999996) ;
     score = Eval(b , turn , maxLevel , turnNum , -9999996) ;
 
     return score ; 
   }
 
 
   /* recursively evaluate the next possible moves */
   protected int Eval(XversiBoard b , int turn , int level , int turnNum , int prevScore) {
 
     int i ;
     int numMove ;
     int move ;
     Spot s ;
     int score ;
     int bestScore ;
     XversiMoveList list ;
     XversiBoard bb ;
 
     if(level == 0) return ComputeEval(b,turn,turnNum) ;
     list = XversiAgent.GenerateMove(b , turn) ; 
     if(list == null) {
       if(XversiAgent.GenerateMove(b , (turn+1)%2) == null) {
 	//System.out.println("tn = " + turnNum) ;
 	//b.PrintBoard() ;
         return BinPieceCount(b) ;  
       }
       if(side == turn) {
 	bestScore = -9999990 ;
         return Eval(b , (turn+1)%2 , level-1 , turnNum , bestScore) ; 
       } else {
 	bestScore = 9999990 ;
         return Eval(b , (turn+1)%2 , level-1 , turnNum , bestScore) ; 
       }
     } else {
 
 
       bb = new XversiBoard() ;
       s = new Spot() ;
       numMove = list.GetNum() ;
       if(side == turn) {
         bestScore = -9999997 ; 
        for(i = 0 ; i < numMove ; i++) {
            bb.Copy(b) ;
            move = list.GetMove(i) ; 
            XversiAgent.TranslatePosition(move , s) ;
            XversiAgent.MakeMove(bb , s.x , s.y , turn) ;
            score = Eval(bb , (turn+1) % 2 , level - 1 , turnNum+1 , bestScore) ;
            if(score >= prevScore)
              return score ;
            if(score > bestScore) {
              bestScore = score ;
            }
         }
 
       } else {
         bestScore = 9999997 ;
        for(i = 0 ; i < numMove ; i++) {
            bb.Copy(b) ;
            move = list.GetMove(i) ;
            XversiAgent.TranslatePosition(move , s) ;
            XversiAgent.MakeMove(bb , s.x , s.y , turn) ;
            score = Eval(bb , (turn+1) % 2 , level - 1 , turnNum+1 , bestScore) ;
            if(score <= prevScore) {
              return score ;
            }
            if(score < bestScore) {
              bestScore = score ;
            }
         } 
 
       }
 
       return bestScore ;
 
     } 
 
   }
 
 
 
   /* recursively evaluate until end game */
   protected int endEval(XversiBoard b , int turn) {
 
     int i ;
     int numMove ;
     int move ;
     Spot s ;
     int score ;
     boolean can_draw = false ;
     XversiMoveList list ;
     XversiBoard bb ;
 
     list = XversiAgent.GenerateMove(b , turn) ;
     if(list == null) {
       if((list = XversiAgent.GenerateMove(b, (turn+1)%2)) == null) {
         if(side == 0)
           return b.GetBC() >= b.GetWC() ? 9999990 : -9999990 ;
         else
           return b.GetWC() >= b.GetBC() ? 9999990 : -9999990 ;
       } else {
         turn = (turn + 1) % 2 ;
       }      
     }
 
     bb = new XversiBoard() ;
     s = new Spot() ;
     can_draw = false ;
     if(side == turn) {
       numMove = list.GetNum() ;
      for(i = 0 ; i < numMove ; i++) {
         bb.Copy(b) ;
         move = list.GetMove(i) ;
         XversiAgent.TranslatePosition(move , s) ;
         XversiAgent.MakeMove(bb , s.x , s.y , turn) ;
         score = endEval(bb , (turn+1)%2) ;
         if(score > 0) return score ;
         if(score == 0) can_draw = true ;
       }
       if(can_draw) return 0 ;
       return -9999990 ;
     } else {
       numMove = list.GetNum() ;
      for(i = 0 ; i < numMove ; i++) {
         bb.Copy(b) ;
         move = list.GetMove(i) ;
         XversiAgent.TranslatePosition(move, s) ;
         XversiAgent.MakeMove(bb, s.x, s.y, turn) ;
         score = endEval(bb, (turn+1)%2) ;
         if(score < 0) return score ;
         if(score == 0) can_draw = true ;
       }
       if(can_draw) return 0 ;
       return 9999990 ;
     }
   }
 
 
 
   /* evaluate a board */
   protected int ComputeEval(XversiBoard b , int turn , int turnNum) {
 
     int score ;
 
     score = PieceCount(b , turn , turnNum) ;
     if(score > 9000000) return score ;
     score += Mobility(b , turn , turnNum) ;
     score += CheckCorner(b , turn) ;
     //score += CenterCheck(b , turn) ;
 
     return score ;
   }
 
 
   protected int CenterCheck(XversiBoard b , int turn) {
 
     final int d = 1 ;
     int piece ;
     int opponent ;
     int score = 0 ;
 
     opponent = (side+1) % 2 ;
 
     piece = b.GetBoard(4,4) ;
     if(piece == side) 
       score += d ;
     else if(piece == opponent)
       score -= d ;
 
     piece = b.GetBoard(4,5) ;
     if(piece == side) 
       score += d ;
     else if(piece == opponent)
       score -= d ;
 
     piece = b.GetBoard(5,4) ;
     if(piece == side) 
       score += d ;
     else if(piece == opponent)
       score -= 1 ;
     piece = b.GetBoard(5,5) ;
     if(piece == side) 
       score += d ;
     else if(piece == opponent)
       score -= 1 ;
 
     return score ;
 
   }
 
 
   /* evaluate by pieces count for end game */
   protected int BinPieceCount(XversiBoard b) {
 
     int count , count2 ;
     int temp ;
     int i , j ; 
     int k ;
     int score ;
 
     if(side == 0) {
       count  = b.GetBC() ;
       count2 = b.GetWC() ;
     } else if(side == 1) {
       count  = b.GetWC() ;
       count2 = b.GetBC() ;
     } else {
       return -9999991 ;
     }
 
 //	System.out.println("tn = " + (count+count2)) ;
 
       if(count > count2) {
         return 9999994 ;
       } else if(count < count2) {
         return -9999994;
       } else {
         return 5000000 ; 
       }
  
 
   }
 
 
   /* evaluate pieces count for mid-game */
   protected int PieceCount(XversiBoard b , int turn , int turnNum) {
 
     int count , count2 ;
     int temp ;
     int i , j ; 
     int k ;
     int score ;
 
     if(side == 0) {
       count  = b.GetBC() ;
       count2 = b.GetWC() ;
     } else if(side == 1) {
       count  = b.GetWC() ;
       count2 = b.GetBC() ;
     } else {
       return -9999991 ;
     }
 
     if(count == 0) return -9999984 ;
     if(count2 ==0) return 9999984  ;
 
     if(XversiBoard.BOARD_SQUARE == turnNum) {
       if(count > count2) {
         return 9999974 ;
       } else if(count < count2) {
         return -9999974;
       } else {
         return 0 ; 
       }
     }
 
 
     score = count - count2 ;
     if(turnNum < 40) {
       if((count+count2) / count > count + count2 - 8) {
         score *= 2 ;
       } else {
         score *= -2 ;
       }
     } else {
       score *= 3 ;
     } 
     //System.out.println("piece score: " + score) ;
     return score ;
   }
 
 
   /* evaluate mobility */
   protected int Mobility(XversiBoard b , int turn , int turnNum) {
 
     int count ;
     int count1;
     int count2;
     
     int score ;
 
     count1 = XversiAgent.CountMove(b,turn) ;
     count2 = XversiAgent.CountMove(b,(turn+1)%2) ;
     if(turn == side) 
       count = count1 - count2 ;
     else
       count = count2 - count1 ;
 
     if(turnNum <= 40) {
       score = 2*count ;
     } else {
       score = 3*count ;
     }
     
     return score ;
   }
 
   /* evaluate corners */ 
   protected int CheckCorner(XversiBoard b , int turn) {
     
     int count  ;
     int i , j ;
     int c[] = new int[4] ;
     int d ;
     int ch1, ch2 ;
     int score ;
 
     score = 0 ;
     count = 0 ;
     for(i = 1 ; i <= 8 ; i+=7) {
       for(j = 1 ; j <= 8 ; j+=7) {
         if((c[count] = b.GetBoard(i,j)) == side) {
           score += 20 ;
         } else if(c[count] == (side+1) % 2) {
           score -= 25 ;
         } else {
           ch1 = XversiAgent.CheckMove(b , i , j , side) ;
           ch2 = XversiAgent.CheckMove(b , i , j , (side+1) % 2) ;
           if(ch1 > 0 && (turn == side || ch2 <= 0)) {
             score += 20 ;
           } else if(ch2 > 0 && (turn != side || ch1 <= 0)) {
             score -= 25 ;
           }
           c[count] = -1 ;
         } 
         count++ ;
       }
     } 
 
     if(c[0] == -1) {
       if((d = b.GetBoard(2,2)) == side) {
         score -= 25 ;
       } else if(d == (side + 1) % 2) {
         score += 20 ;
       }
     } 
 
     if(c[1] == -1) {
       if((d = b.GetBoard(2,7)) == side) {
         score -= 25 ;
       } else if(d == (side + 1) % 2) {
         score += 20 ;
       }
     }
 
     if(c[2] == -1) {
     if((d = b.GetBoard(7,2)) == side) {
         score -= 25 ;
       } else if(d == (side + 1) % 2) {
         score += 20 ;
       }
     }
   
     if(c[3] == -1) {
       if((d = b.GetBoard(7,7)) == side) {
         score -= 25 ;
       } else if(d == (side + 1) % 2) {
         score += 20 ;
       }
 
     }
     //System.out.println("Corner: " + score) ;
     return score ;
  
   }
 
 }
 
