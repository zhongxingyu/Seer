 /*
 # This file XversiBoard.java is part of xversi-java-parallel, a board game called reversi or othello.
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
 
 import java.io.Serializable ;
 
 /* board of Reversi */
 public class XversiBoard implements Serializable {
 
 public final static int BOARD_SIZE = 8 ;
 public final static int BOARD_SQUARE = 64 ;
 
 protected long board ;
 protected  long boardFilled ;
 public  int wc ;
 public  int bc ;
 
 
 /* Initialize the initial positions */
 public void Init() {
 
   board = 0 ;
   boardFilled = 0 ;
   wc = 2 ;
   bc = 2 ;
 
   board |= Power2.GetPower(28) | Power2.GetPower(35) ;
   boardFilled |= Power2.GetPower(27) | Power2.GetPower(28) | Power2.GetPower(35) | Power2.GetPower(36) ;
 
 }
 
 
 /* copy board status */
 public void Copy(XversiBoard b) {
  board = b.board ;
  boardFilled = b.boardFilled ; 
  wc = b.wc  ;
  bc = b.bc  ;
 
 }
 
 
 /* get number of white pieces */
 public int GetWC() {
   return wc ;
 }
 
 /* get number of black pieces */
 public int GetBC() {
   return bc ;
 }
 
 
 /* update board at x,y for a side */
 public boolean SetBoard(int x , int y , int c) {
 
   int position ;
   long power ;
   int t ; 
   
   if(!XversiAgent.CheckXY(x,y))
     return false ;
 
   if(c == 0) {
     if((t = GetBoard(x,y)) == 1) {
       bc++ ;
       wc-- ;
     } else if(t == -1) {
       bc++ ;
     }
 
   } else if(c == 1) {
     if((t = GetBoard(x,y)) == 0) {
       wc++ ;
       bc-- ;
     } else if(t == -1) {
       wc++ ;
     }
   } else return false ;
 
   position = XversiAgent.TranslateXY(x,y) ;
   power = Power2.GetPower(position-1) ;
   if(c == 0) {
     boardFilled |= power ;
     board &= ~power ;
   } else if(c == 1) {
     boardFilled |= power ;
     board |= power ;
   } else {
     boardFilled &= ~power ;
   }
 
   return true ;
 
 }
 
 
 /* get information of the board at x,y */
 public int GetBoard(int x , int y) {
 
   int position ;
   long power ;
 
   if(!XversiAgent.CheckXY(x,y)) 
     return -1 ;
 
   position = XversiAgent.TranslateXY(x,y) ;
   power = Power2.GetPower(position-1) ;
   if((boardFilled & power) == 0)
     return -1 ;
   else if((board & power) == 0)
     return 0 ;
   else return 1 ;
 
 
 }
 
 
 /* print the board at console */
 public void PrintBoard() {
 
   int x , y  ;
 
   for(y = BOARD_SIZE ; y > 0 ; y--) {
     for(x = 1 ; x <= BOARD_SIZE ; x++) {
       if(GetBoard(x,y) < 0)
         System.out.print('.') ;
       else if(GetBoard(x,y) == 1)
         System.out.print('X') ;
       else if(GetBoard(x,y) == 0)
         System.out.print('O') ;
       else
         System.out.print('?') ; 
     }
     System.out.println(' ') ;
   } 
   System.out.println(' ') ;
 }
 
 }
