 
 public class CalcMovesPawn extends CalcMoves{
 	int sx;
 	int sy;
 	int[] a = new int[50];
 	boolean white;
 	Board board;
 	int z;
 	
 	CalcMovesPawn(int SelectedTile, Board board1){
 		sx = SelectedTile/10;
 		sy = SelectedTile - sx*10;
 		white =board1.pieceAt[sx][sy].white;
 		board = board1;
 		for(int i=0;i<50;i++){
 			a[i] = -1;
 		}
 				
 	}
 	int[] GetMoves(){
 		if (white){
 			z=1;
 		}else{
 			z=-1;
 		}
 		if(board.pieceAt[sx][sy+z].piece==Piece.Empty){
 			if(isMoveLegit(sx*10 + sy,sx*10+sy+z, board)){
 				a[0] = sx*10+sy+z;
 			}
 			if(sy == ((7+z)%7) && board.pieceAt[sx][sy+2*z].piece == Piece.Empty){
 				if(isMoveLegit(sx*10 + sy, sx*10 +sy+2*z, board)){
 					a[1] = sx*10 +sy+2*z;
 				}
 			}
 		}
 		if(sx != 0){
 			if(board.pieceAt[sx-1][sy+z].white != white && board.pieceAt[sx-1][sy+z].piece != Piece.Empty){
 				if(isMoveLegit(sx*10 + sy, (sx-1)*10 + (sy+z), board)){
 					a[2] = (sx-1)*10 + (sy+z);
 				}
 			}
 		}
 		if(sx != 7){
 			if(board.pieceAt[sx+1][sy+z].white != white && board.pieceAt[sx+1][sy+z].piece != Piece.Empty){
 				if(isMoveLegit(sx*10 + sy, (sx+1)*10 + (sy+z), board)){
 					a[3] = (sx+1)*10 + (sy+z);
 				}
 			}
 		}
 		//---------------------------
 		for(int i = 1; i>-2; i=i-2){
 			if(sx+i >+0 && sx+i<=7){
 				if(board.pieceAt[sx+i][sy].piece == Piece.PAWN){
 					if(board.pieceAt[sx+i][sy].white != board.pieceAt[sx][sy].white){
 						if(board.lastMoveTo == ((sx+i)*10+sy)){
							if(Math.abs(board.lastMoveFrom - board.lastMoveTo) > 1){
 								if(isMoveLegit(sx*10 + sy, (sx+i)*10 + (sy+z), board)){
 									a[10+i] = ((sx+i)*10 + sy+z);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		//--------------
 		return a;
 				
 	}
 	int[] GetMoves2(){
 		if (white){
 			z=1;
 		}else{
 			z=-1;
 		}
 		if(sy+z >= 0 && sy+z <=7){
 			if(board.pieceAt[sx][sy+z].piece==Piece.Empty){
 				a[0] = sx*10+sy+z;
 				if(sy == ((7+z)%7)){
 					if(board.pieceAt[sx][sy+2*z].piece == Piece.Empty){
 						a[1] = sx*10 +sy+2*z;
 					}
 				}
 			}
 		
 			if(sx != 0){
 				if(board.pieceAt[sx-1][sy+z].white != white && board.pieceAt[sx-1][sy+z].piece != Piece.Empty){
 					a[2] = (sx-1)*10 + (sy+z);
 				}
 			}
 			if(sx != 7){
 				if(board.pieceAt[sx+1][sy+z].white != white && board.pieceAt[sx+1][sy+z].piece != Piece.Empty){
 					a[3] = (sx+1)*10 + (sy+z);
 				}
 			}
 			for(int i = 1; i>-2; i=i-2){
 				if(sx+i >+0 && sx+i<=7){
 					if(board.pieceAt[sx+i][sy].piece == Piece.PAWN){
 						if(board.pieceAt[sx+i][sy].white != board.pieceAt[sx][sy].white){
 							if(board.lastMoveTo == ((sx+i)*10+sy)){
 								if(Math.abs(board.lastMoveFrom - board.lastMoveTo) > 1){
 									a[10+i] = ((sx+i)*10 + sy+z);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return a;
 				
 	}
 }
