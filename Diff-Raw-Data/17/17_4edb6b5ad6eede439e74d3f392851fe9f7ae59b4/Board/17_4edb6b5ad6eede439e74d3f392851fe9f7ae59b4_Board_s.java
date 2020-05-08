 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package itschess;
 
 /**
  *
  * @author Ian
  */
 public class Board {
 
     public byte board[][] = new byte [8][8];
     public byte attackboard[][] = new byte[8][8];
 
 
     byte fx = 0;
     byte fy = 0;
     byte sx = 0;
     byte sy = 0;
     byte tx = 0;
     byte ty = 0;
 
     byte fmove = 0;
     byte smove = 0;
     byte tmove = 0; 
     
     byte possibleLength = 0;
 
     public boolean done = false;
 
     public String[] chessMoves = new String[4];
 
     int depth = 0;
 
     public byte[][] possibleMoves;
 
     public Board ()
     {
         byte[] holder = {-3,-5,-4,-2,-1,-4,-5,-3};
         board[0] = holder; 
         for(int i = 0; i < 8; i ++)
         {
                 board[1][i] = -6;
 
         }
         for(int i = 2; i < 6; i ++)
         {
                 for(int j = 0; j < 8; j ++)
                 {
                         board[i][j] = 0;
                 }
         }
         for(int i = 0; i < 8; i ++)
         {
                 board[6][i] = 6;
 
         }
         board[7][0] = 3;
         board[7][1] = 5;
         board[7][2] = 4;
         board[7][3] = 2;
         board[7][4] = 1;
         board[7][5] = 4;
         board[7][6] = 5;
         board[7][7] = 3;
 
         for(byte i = 0; i < 8; i ++)
         {
                 for(byte j = 0; j <8; j++)
                 {
                         attackboard[i][j] = 0;
                 }
         }
     }
 
     public void move(int d)
     {
         depth = d;
         byte y = 0;
         byte x = 0;
         byte move = 0;
         //Initialize
         if(d == 0)
         {
                 y = (byte) (fy);
                 x = (byte) (fx);
                 move = fmove;
         }
         else if(d == 1)
         {
                 y = (byte)(sy);
                 x = (byte)(sx);
                 move = smove;
         }
         else if(d == 2)
         {
                 y = (byte)(ty);
                 x = (byte)(tx);
                 move = tmove;
         }
         //gatherMoves(y, x);
         //if(possibleMoves == null || move >= possibleMoves.length)
         //{
         //    if(possibleMoves != null )
         //    {
         //            y++;
         //            x++;
         //    }
         //move = 0;
         //Iterate through the board
         for(; y < 8; y++)
         {
             
             for(; x < 8; x++)
             {
                 if(depth == 1)
                 {
                     if(board[y][x] < 0)
                     {//if there is a piece to move (black turn)
 
                         gatherMoves(y, x);
                         if(smove >= possibleMoves.length)
                         {//If we've checked all the moves continue
 //                            x++; sx++; move = 0; smove = 0;
 //                            if(x == 8)
 //                            {
 //                                y++; sy++;
 //                                x=0; sx=0;
 //                            }
 //                            x--;
                             continue;
                         }
                         for(; move<possibleMoves.length; move++)
                         {
                             if(possibleMoves[move][0] != 100)
                             {//If we find a move return it
                                 movePiece(y,x,possibleMoves[move][0],possibleMoves[move][1]);
                                 move++;
                                 smove = move; sx = x; sy = y;
                                 return;
                             }
                         }
                         move = 0; smove = 0;
                     }
                    
                 }
                 else
                 {
                     if(board[y][x] > 0)
                     {//if there is a piece to move (white turn)
                         
                         gatherMoves(y, x);
 
                         if(depth == 0 && (fmove >= possibleMoves.length))
                         {//If we've checked all the moves continue
 //                            x++; fx++; move = 0; fmove = 0;
 //                            if(x == 8)
 //                            {
 //                                y++; fy++;
 //                                x=0; fx=0;
 //                            }
 
                             continue;
                         }
                         if(depth == 2 && (tmove >= possibleMoves.length))
                         {//If we've checked all the moves continue
 //                            x++; tx++; move = 0; tmove = 0;
 //                            if(x == 8)
 //                            {
 //                                y++; ty++;
 //                                x=0; tx=0;
 //                            }
 
                             continue;
                         }
                         for(; move<possibleMoves.length; move++)
                         {
                             if(possibleMoves[move][0] != 100)
                             {//If we find a move return it
                                 movePiece(y,x,possibleMoves[move][0],possibleMoves[move][1]);
                                 move++;
                                 if(depth == 0)
                                 {
                                     fmove = move; fx = x; fy = y;
                                 }
                                 if(depth == 2)
                                 {
                                     tmove = move; tx = x; ty = y;
                                 }
                                 return;
                             }
                         }
                         move = 0;
                         if(depth == 0)
                             fmove = move;
                         if(depth == 2)
                             tmove = move;
                     }
                 }
             }
             x = 0;
             
         }
         
         if(depth == 0)
                 done = true;
         else if(depth == 2)
         {
                 tx = 0;
                 ty = 0;
                 tmove = 0;
                 //done = true;
         }
         else if(depth == 1)
         {
                 sx = 0;
                 sy = 0;
                 smove =0;
                 //done = true;
         }
         //}
         //else
         //{
 //                if(possibleMoves[move][0] != 100)
 //                {
 //
 //                        movePiece(y,x,possibleMoves[move][0],possibleMoves[move][1]);
 //                }
         //}
     }
     
     public void undo(int d)
     {
             depth = d;
             if(chessMoves[depth] != null)
             {
                 String oldX = String.valueOf(chessMoves[depth].charAt(2));
                 String oldY = String.valueOf(chessMoves[depth].charAt(1));
                 String currX = String.valueOf(chessMoves[depth].charAt(4));
                 String currY = String.valueOf(chessMoves[depth].charAt(3));
                 movePiece((byte)Integer.parseInt(currY),(byte)Integer.parseInt(currX),
                         (byte)Integer.parseInt(oldY),(byte)Integer.parseInt(oldX));
             }
 
             if(depth == 1)
             {
                     tx = 0;
                     ty = 0;
             }
             else if(depth == 0)
             {
                     sx = 0;
                     sy = 0;
             }
     }
 
     public void gatherMoves(byte y, byte x)
     {
             if(board[y][x] == (byte) -6)
             {
                     possibleMoves = possibleMovesPB(y,x);
             }
             if(board[y][x] == (byte) 6)
             {
                     possibleMoves = possibleMovesP(y,x);
             }
             else if (Math.abs(board[y][x]) == (byte) 5)
             {
                     possibleMoves  = possibleMovesN(y,x);
             }
             else if (Math.abs(board[y][x]) == (byte) 4)
             {
                     possibleMoves  = possibleMovesBishop(y,x);
             }
             else if (Math.abs(board[y][x]) == (byte) 3)
             {
                     possibleMoves  = possibleMovesRook(y,x);
             }
             else if (Math.abs(board[y][x]) == (byte) 2)
             {
                     possibleMoves  = possibleMovesQ(y,x);
             }
             else if (Math.abs(board[y][x]) == (byte) 1)
             {
                     possibleMoves  = possibleMovesBishop(y,x);
             }
             else 
                     possibleMoves = null;
 
     }
 
     public void analysis(){
         byte[][] moves;
         for(byte i = 0; i < 8; i ++)
         {
             for(byte j = 0; j <8; j++)
             {
                 if(board[i][j] == (byte) -6)
                 {
                         moves = possibleMovesPB(j,i);
                         for(byte k = 0; k < moves.length; k ++)
                         {
                                 if(moves[k][0] != (byte)100)
                                 {
                                         //movePiece(i,j,moves[k][0], moves[k][1]);
                                         attackboard[moves[k][0]][moves[k][1]] = (byte) (attackboard[moves[k][0]][moves[k][1]] + 1);
                                 }
                         }
                 }
                 else if (board[i][j] == (byte) -5)
                 {
                         moves = possibleMovesN(j,i);
                         for(byte k = 0; k < moves.length; k ++)
                         {
                                 if(moves[k][0] != (byte)100)
                                 {
                                         //movePiece(i,j,moves[k][0], moves[k][1]);
                                         attackboard[moves[k][0]][moves[k][1]] = (byte) (attackboard[moves[k][0]][moves[k][1]] + 1);
                                 }
                         }
                 }
                 else if (board[i][j] == (byte) -4)
                 {
                         moves = possibleMovesBishop(j,i);
                         for(byte k = 0; k < moves.length; k ++)
                         {
                                 if(moves[k][0] != (byte)100)
                                 {
                                         //movePiece(i,j,moves[k][0], moves[k][1]);
                                         //attackboard[moves[k][0]][moves[k][1]] = (byte) (attackboard[moves[k][0]][moves[k][1]] + 1);
                                 }
                         }
                 }
             }
         }
     }
 
     public double eval(){
         int k = 0;
         int kb = 0;
         int q =0;
         int qb = 0;
         int r =0;
         int rb = 0;
         int b =0;
         int bb = 0;
         int n =0;
         int nb = 0;
         int p =0;
         int pb = 0;
         int d = 0;
         int db = 0;
 
         for(int i = 0; i < 7; i ++)
         {
                 for(int j = 0; j < 7; j++)
                 {
                         if(board[i][j] == 1)
                                 k ++;
                         else if(board[i][j] == -1)
                                 kb ++;
                         else if(board[i][j] == 2)
                                 q ++;
                         else if(board[i][j] == -2)
                                 qb ++;
                         else if(board[i][j] == 3)
                                 r ++;
                         else if(board[i][j] == -3)
                                 rb ++;
                         else if(board[i][j] == 4)
                                 b ++;
                         else if(board[i][j] == -4)
                                 bb ++;
                         else if(board[i][j] == 5)
                                 n ++;
                         else if(board[i][j] == -5)
                                 nb ++;
                         else if(board[i][j] == 6)
                         {
                                 p ++;
                                 if (i > 0 && board[i-1][j] == 6)
                                         d ++;
                         }
                         else if(board[i][j] == -6)
                         {
                                 pb ++;
                                 if (i < 6 && board[i+1][j] == -6)
                                         db ++;
                         }
                 }	
         }
         double evalNum = 200*(k - kb) + 9*(q-qb) + 5*(r-rb) + 3*((b-bb) + (n-nb)) + (p-pb) - .5*(d-db);
 
         return evalNum;
     }
 
     public void movePiece(byte y, byte x, byte y1, byte x1)
     {
             String retVal = "";
            retVal += board[y][x];
             board[y1][x1] = board[y][x];
            retVal += "" + y+ "" + x + "" + y1 + "" + x1;
             chessMoves[depth] =  retVal;
             board[y][x] = 0;
     }
 
     public byte[][] possibleMovesPB(byte y, byte x){
             byte[][] retVal = new byte[4][2];
             if(board[y+1][(byte) (x)] == 0)
             {
                     retVal[0][0] = (byte) (y+1);
                     retVal[0][1] = x;
             }
             else
             {
                     retVal[0][0] = (byte) 100;
                     retVal[0][1] = (byte) 100;
             }
             if(board[(byte) (y+2)][x] == 0)
             {
                     retVal[1][0] = (byte) (y+2);
                     retVal[1][1] = x;
             }
             else
             {
                     retVal[0][0] = (byte) 100;
                     retVal[0][1] = (byte) 100;
             }
             if(x+1 < 8 && board[(byte) (y+1)][(byte) (x+1)] > 0)
             {
 
                     retVal[2][0] = (byte) (y+1);
                     retVal[2][1] = (byte) (x+1);
             }
             else
             {
                     retVal[2][0] = (byte) 100;
                     retVal[2][1] = (byte) 100;
             }
             if(x > 0 && board[(byte) (y+1)][(byte) (x-1)] > 0 )
             {
                     retVal[3][0] = (byte) (y+1);
                     retVal[3][1] = (byte) (x-1);
             }
             else
             {
                     retVal[3][0] = (byte)100;
                     retVal[3][1] = (byte)100;
             }
             return retVal;
     }
 
     public byte[][] possibleMovesP(byte y, byte x){
             byte[][] retVal = new byte[4][2];
             if(y>0 && board[y-1][(byte) (x)] == 0)
             {
                     retVal[0][0] = (byte) (y-1);
                     retVal[0][1] = x;
             }
             else
             {
                     retVal[0][0] = (byte) 100;
                     retVal[0][1] = (byte) 100;
             }
             if(y>1 &&board[y-2][(byte) (x)] == 0 && y == 6)
             {
                     retVal[1][0] = (byte)(y-2);
                     retVal[1][1] = x;
             }
             else
             {
                     retVal[1][0] = (byte) 100;
                     retVal[1][1] = (byte) 100;
             }
             if(y>0 && x<7 && board[(byte) (y-1)][(byte) (x+1)] < 0)
             {
                     retVal[2][0] = (byte) (y-1);
                     retVal[2][1] = (byte) (x+1);
             }
             else
             {
                     retVal[2][0] = (byte) 100;
                     retVal[2][1] = (byte) 100;
             }
             if(y>0 && x >0 && board[(byte) (y-1)][(byte) (x-1)] < 0)
             {
                     retVal[3][0] = (byte) (y-1);
                     retVal[3][1] = (byte) (x-1);
             }
             else
             {
                     retVal[3][0] = (byte)100;
                     retVal[3][1] = (byte)100;
             }
             return retVal;
     }
 
     public byte[][] possibleMovesN(byte y, byte x){
         
         byte[][] retVal = new byte[8][2];
             
             boolean white;
             if( board[(byte) y][(byte) x] > 0 )
             {
                 white = true;
             }
             else
                 white = false;
             
             if(x>0 && y<6)
             {
                 if((board[(byte) y+2][(byte) x-1] == 0)
                 || (white && board[(byte) y+2][(byte) x-1] < 0)
                 || (!white && board[(byte) y+2][(byte) x-1] > 0))
                 {
                     retVal[0][0] = (byte) (y+2);
                     retVal[0][1] = (byte) (x-1);
                 }
             }
             else
             {
                     retVal[0][0] = (byte) 100;
                     retVal[0][1] = (byte) 100;
             }
             if(x<7 && y<6)
             {
                 if((board[ (byte) y+2][(byte) x+1] == 0)
                 || (white && board[(byte) y+2][(byte) x+1] < 0)
                 || (!white && board[(byte) y+2][(byte) x+1] > 0))
                 {
                     retVal[1][0] = (byte) (y+2);
                     retVal[1][1] = (byte) (x+1);
                 }
             }
             else
             {
                     retVal[1][0] = (byte) 100;
                     retVal[1][1] = (byte) 100;
             }
             if(x<6 && y<7)
             {
                 if((board[(byte) y+1][(byte) x+2] == 0)
                 || (white && board[(byte) y+1][(byte) x+2] < 0)
                 || (!white && board[(byte) y+1][(byte) x+2] > 0))
                 {
                     retVal[2][0] = (byte) (y+1);
                     retVal[2][1] = (byte) (x+2);
                 }
             }
             else
             {
                     retVal[2][0] = (byte) 100;
                     retVal[2][1] = (byte) 100;
             }
             if(x>1 && y<7)
             {
                 if((board[(byte) (y+1)][(byte) (x-2)] == 0)
                 || (white && board[(byte) (y+1)][(byte) (x-2)] < 0)
                 || (!white && board[(byte) (y+1)][(byte) (x-2)] > 0))
                 {
                     retVal[3][0] = (byte) (y+1);
                     retVal[3][1] = (byte) (x-2);
                 }
             }
             else
             {
                     retVal[3][0] = (byte)100;
                     retVal[3][1] = (byte)100;
             }
             if(x<6 && y>0 && board[(byte) (y-1)][(byte) (x+2)] == 0)
             {
                 if((board[(byte) (y-1)][(byte) (x+2)] == 0)
                 || (white && board[(byte) (y-1)][(byte) (x+2)] < 0)
                 || (!white && board[(byte) (y-1)][(byte) (x+2)] > 0))
                 {
                     retVal[2][0] = (byte) (y-1);
                     retVal[2][1] = (byte) (x+2);
                 }
             }
             else
             {
                     retVal[2][0] = (byte) 100;
                     retVal[2][1] = (byte) 100;
             }
             if(x>1 && y>0 && board[(byte) (y-1)][(byte) (x-2)] == 0)
             {
                 if((board[(byte) (y-1)][(byte) (x-2)] == 0)
                 || (white && board[(byte) (y-1)][(byte) (x-2)] < 0)
                 || (!white && board[(byte) (y-1)][(byte) (x-2)] > 0))
                 {
                     retVal[3][0] = (byte) (y-1);
                     retVal[3][1] = (byte) (x-2);
                 }
             }
             else
             {
                     retVal[3][0] = (byte)100;
                     retVal[3][1] = (byte)100;
             }
             if(x<7 && y>1 && board[(byte) (y-2)][(byte) (x+1)] == 0)
             {
                 if((board[(byte) (y-2)][(byte) (x+1)] == 0)
                 || (white && board[(byte) (y-2)][(byte) (x+1)] < 0)
                 || (!white && board[(byte) (y-2)][(byte) (x+1)] > 0))
                 {
                     retVal[2][0] = (byte) (y-2);
                     retVal[2][1] = (byte) (x+1);
                 }
             }
             else
             {
                     retVal[2][0] = (byte) 100;
                     retVal[2][1] = (byte) 100;
             }
             if(x>0 && y>1 && board[(byte) (y-2)][(byte) (x-1)] == 0)
             {
                 if((board[(byte) (y-2)][(byte) (x-1)] == 0)
                 || (white && board[(byte) (y-2)][(byte) (x-1)] < 0)
                 || (!white && board[(byte) (y-2)][(byte) (x-1)] > 0))
                 {
                     retVal[3][0] = (byte) (y-2);
                     retVal[3][1] = (byte) (x-1);
                 }
             }
             else
             {
                     retVal[3][0] = (byte)100;
                     retVal[3][1] = (byte)100;
             }
             return retVal;
     }
 
     public byte[][] possibleMovesRook(byte y, byte x)
     {//returns a byte array of the possible moves a rook can make
         byte[][] retval = new byte[14][2];
         byte sizecount = 0;
 
         for(byte i=x, m=y; i<8; i++)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 7)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i+1);
                         retval[sizecount][1] = (byte)(m);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; i>0; i--)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 1)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i-1);
                         retval[sizecount][1] = (byte)(m);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; m<8; m++)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(m != 7)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i);
                         retval[sizecount][1] = (byte)(m+1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; m>0; m--)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(m != 1)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i);
                         retval[sizecount][1] = (byte)(m-1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(int i=sizecount; i<14; i++)
         {
             retval[i][0] = (byte)(100);
             retval[i][1] = (byte)(100);
         }
 
         return retval;
     }
 
     public byte[][] possibleMovesBishop(byte y, byte x)
     {//returns a byte array of the possible moves a bishop can make
         byte[][] retval = new byte[13][2];
         //System.out.println("X: " + x + "Y: " + y);
         byte sizecount = 0;
 
         for(byte i=x, m=y; i<8 && m<8; i++,m++)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 7 && m != 7)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i+1);
                         retval[sizecount][1] = (byte)(m+1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; i>0 && m<8; i--, m++)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 1 && m != 7)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i-1);
                         retval[sizecount][1] = (byte)(m+1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; i<8 && m>0; i++, m--)
         {
             if(board[(i)][(m)] == 0 && board[i][m] < 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 7 && m != 1)
                 {
                    if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i+1);
                         retval[sizecount][1] = (byte)(m-1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; i>0 && m>0; i--, m--)
         {
             if(board[(i)][(m)] == 0 && board[i][m] < 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 1 && m != 1)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i-1);
                         retval[sizecount][1] = (byte)(m-1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         
         for(int i=sizecount; i<13; i++)
         {
             retval[i][0] = (byte)(100);
             retval[i][1] = (byte)(100);
         }
 
         return retval;
     }
 
     public byte[][] possibleMovesQ(byte y, byte x){
         byte[][] retval = new byte[27][2];
         byte sizecount = 0;
 
         for(byte i=x, m=y; i<8; i++)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 7)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i+1);
                         retval[sizecount][1] = (byte)(m);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; i>0; i--)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 1)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i-1);
                         retval[sizecount][1] = (byte)(m);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; m<8; m++)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(m != 7)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i);
                         retval[sizecount][1] = (byte)(m+1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; m>0; m--)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(m != 1)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i);
                         retval[sizecount][1] = (byte)(m-1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
 
 
         for(byte i=x, m=y; i<8 && m<8; i++,m++)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 7 && m != 7)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i+1);
                         retval[sizecount][1] = (byte)(m+1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; i>0 && m<8; i--, m++)
         {
             if(board[(i)][(m)] == 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 1 && m != 7)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i-1);
                         retval[sizecount][1] = (byte)(m+1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; i<8 && m>0; i++, m--)
         {
             if(board[(i)][(m)] == 0 && board[i][m] < 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 7 && m != 1)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i+1);
                         retval[sizecount][1] = (byte)(m-1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         for(byte i=x, m=y; i>0 && m>0; i--, m--)
         {
             if(board[(i)][(m)] == 0 && board[i][m] < 0)
             {
                 retval[sizecount][0] = (byte)(i);
                 retval[sizecount][1] = (byte)(m);
                 sizecount++;
             }
             else
             {
                 if(i != 1 && m != 1)
                 {
                     if((board[y][x] > 0 && board[i][m] < 0)
                     || (board[y][x] < 0 && board[i][m] > 0))
                     {
                         retval[sizecount][0] = (byte)(i-1);
                         retval[sizecount][1] = (byte)(m-1);
                         sizecount++;
                     }
                 }
                 break;
             }
         }
         
         
         for(int i=sizecount; i<27; i++)
         {
             retval[i][0] = (byte)(100);
             retval[i][1] = (byte)(100);
         }
 
         return retval;
 
     }
 
     @Override
     public String toString() {
             String retVal = "";
             for(int i =0; i < 8; i ++)
             {
                     for(int j = 0; j < 8; j++)
                     {
                             retVal += board[i][j] + "\t";
                     }
                     retVal += "\n";
             }
             retVal += "\n";
             for(int i =0; i < 8; i ++)
             {
                     for(int j = 0; j < 8; j++)
                     {
                             retVal += attackboard[i][j] + "\t";
                     }
                     retVal += "\n";
             }
             return retVal;
     }
 }
