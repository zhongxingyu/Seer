 package ChessAPI;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import ChessAPI.ApiVersion;
 import ChessAPI.Board;
 import ChessAPI.GameDump;
 import ChessAPI.Piece.Color;
 import ChessAPI.Piece.Type;
 import ChessAPI.Player;
 
 public class UserProgram {
 
 	public static void main(String s[]){
 		GameDump D;
 		Board b;
 		//req4 create a dump
 		b = new Board();
 		D = b.getDump();
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		String more = null;
 
 		ApiVersion.printVersionNo();
 
 		String version = ApiVersion.getVersionNo();
 		System.out.println("Current Version is : "+version);
 
 		b = new Board();
 		Player pl1 = new Player(Color.white);
 		Player pl2 = new Player(Color.black);
 
 		b.initBoard(pl1, pl2);
 		//req5 print board
 		//b.displayBoard();
 
 		//req6 move a piece, the next line moves W_Pawn from (2,1) to (3,1)
 		//pl1.moveTo(b.getSquare(2, 1), b.getSquare(3, 1));
 		//pl1.moveTo(b.getSquare(2, 1), b.getSquare(3, 1));
 		//b.displayBoard();
 
 		//b.displayBoard();
 
 		String selection = "";
 		String exit = "";
 		int playMode = 0;
 		boolean promo_success = false;
 		int opp_x = 0, opp_y = 0;
  
 		do
 		{
 			System.out.println("\n****WELCOME TO THE CHESS GAME****");
 
 			do
 			{
 				System.out.println("\nPLEASE SELECT PLAY MODE:");
 				System.out.print("1) Player Against Player\n2) Player Against AI\n3) AI Against AI\nQ) Quit");
 				try {
 					selection = br.readLine();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if (selection.equals("1")||selection.equals("2")||selection.equals("3")){
 
 					if(selection.equals("1"))
 				    	 playMode = 1;
 				    if(selection.equals("2"))
 				    	 playMode = 2;
 				    if(selection.equals("3"))
 				    	 playMode = 3;
 				}else{
 
 					if(selection.toUpperCase().equals("Q")){exit = "Q";}
 					else
 					System.out.println("Error: Wrong Input, Please Try Again");
 				}
 			}while(!selection.toUpperCase().equals("Q")&&!selection.equals("1")&&!selection.equals("2")&&!selection.equals("3"));
 
 			  if(playMode == 1){
 				  boolean result;
 				  String originalType = "";
 				  b.displayBoard();
 				  int i = 0;
 					do
 					{
 						if (i % 2 == 1)
 							System.out.println("\n\n -------------------------------- Black's Turn ---------------------------------------- ");
 						else
 							System.out.println("\n\n -------------------------------- White's Turn ---------------------------------------- ");
 
 
 						if (i % 2 == 0)
 						{
 							int s_x=0,s_y=0,d_x=0,d_y=0;
 							do {
 
 								System.out.println("White Player Please Make Move (Format:Source_Col,Source_Row,Dest_Col,Dest_Row):");
 								String move = "";
 								try {
 									 move = br.readLine();
 								} catch (IOException e) {
 									e.printStackTrace();
 								}
 								String[] moves = move.split(",");
 								s_x = Integer.parseInt(moves[1]);
 								//For printing row nos from 8 to 1 instead of 1 to 8
 								s_x = (9 - s_x);
 								char sy = moves[0].charAt(0);
 								s_y = (int) sy-96;
 
 								d_x = Integer.parseInt(moves[3]);
 								// For printing row nos from 8 to 1 instead of 1 to 8
 								d_x =  (9 - d_x);
 								char dy = moves[2].charAt(0);
 								d_y = (int) dy-96;
  
 								if (b.getSquare(s_x, s_y).getPiece() != null)
 									originalType = b.getSquare(s_x, s_y).getPiece().getType().toString();
 								else
 									originalType = "";
 
 								result = pl1.moveTo(b.getSquare(s_x, s_y), b.getSquare(d_x, d_y));
 
 							} while(result == false);
 
 							promo_success = false;
 							//System.out.println("Now moving " + originalType + " from (" + pl1.originalSquare.get_x() + ", " + pl1.originalSquare.get_y() + ") to (" + s_rand.get_x() + ", " + s_rand.get_y() + ") ");
 							if (originalType.toString() == "Pawn")
 							{
 								String promo_str = b.getSquare(d_x, d_y).getPiece().promotion;
 								int promo_pos = b.getSquare(d_x, d_y).getPiece().promo_pos;
 
 								if (promo_str != "")
 								{
 									if (promo_str == "rook")
 									{
 										if (pl1.rook[1].isPieceDead() == true)
 										{
 											pl1.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.rook[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.rook[2].isPieceDead() == true){
 											pl1.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.rook[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									////////////////////////////
 									if (promo_str == "knight")
 									{
 										if (pl1.knight[1].isPieceDead() == true)
 										{
 											pl1.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.knight[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.knight[2].isPieceDead() == true){
 											pl1.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.knight[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									////////////////////////////
 									if (promo_str == "bishop")
 									{
 										if (pl1.bishop[1].isPieceDead() == true)
 										{
 											pl1.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.bishop[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.bishop[2].isPieceDead() == true){
 											pl1.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.bishop[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										}
 									}								
 									////////////////////////////
 									if (promo_str == "queen")
 									{
 										if (pl1.queen.isPieceDead() == true)
 										{
 											pl1.queen.setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.queen.getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.queen.setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 									}
 								}
 							 }
 							if (b.getSquare(d_x, d_y).getPiece().enpass == true)
 							{
 								opp_x = b.getSquare(d_x, d_y).getPiece().en_x;
 								opp_y = b.getSquare(d_x, d_y).getPiece().en_y;
 								pl1.killPiece(b.getSquare(opp_x, opp_y));
 							}
 						}
 							//System.out.println("Now moving " + originalType + " from (" + s_x + ", " + s_y + ") to (" + d_x + ", " + d_y + ") ");
 						} else {
 							int s_x2=0,s_y2=0,d_x2=0,d_y2=0;
 							do {	
 								System.out.println("Black Player Please Make Move (Format:Source_Col,Source_Row,Dest_Col,Dest_Row):");
 								String move = "";
 								try {
 									 move = br.readLine();
 								} catch (IOException e) {
 									e.printStackTrace();
 								}
 								String[] moves = move.split(",");
 								s_x2 = Integer.parseInt(moves[1]);
 								s_x2 = (9 - s_x2);
 
 								char sy = moves[0].charAt(0);
 								s_y2 = (int) sy-96;
 
 								d_x2 = Integer.parseInt(moves[3]);
 								//For printing row nos from 8 to 1 instead of 1 to 8
 								d_x2 = (9 - d_x2);
 
 								char dy = moves[2].charAt(0);
 								d_y2 = (int) dy-96;
 
 								//System.out.println("Now moving " + originalType + " from (" + d_x2 + ", " + d_y2 + ") to (" + d_x2 + ", " + d_y2 + ") ");
 
 								if (b.getSquare(s_x2, s_y2).getPiece() != null)
 									originalType = b.getSquare(s_x2, s_y2).getPiece().getType().toString();
 								else
 									originalType = "";
 
 								result = pl2.moveTo(b.getSquare(s_x2, s_y2), b.getSquare(d_x2, d_y2));
 
 							} while(result == false);
 							//System.out.println("Now moving " + originalType + " from (" + d_x2 + ", " + d_y2 + ") to (" + d_x2 + ", " + d_y2 + ") ");
 
 							promo_success = false;
 							//System.out.println("Now moving " + originalType + " from (" + pl2.originalSquare.get_x() + ", " + pl2.originalSquare.get_y() + ") to (" + s_rand.get_x() + ", " + s_rand.get_y() + ") ");
 							if (originalType.toString() == "Pawn")
 							{
 								String promo_str = b.getSquare(d_x2, d_y2).getPiece().promotion;
 								int promo_pos = b.getSquare(d_x2, d_y2).getPiece().promo_pos;
 
 								if (promo_str != "")
 								{
 									if (promo_str == "rook")
 									{
 										if (pl2.rook[1].isPieceDead() == true)
 										{
 											pl2.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.rook[1].getColor(), b.getSquare(d_x2, d_y2).get_x(), promo_pos, null);
 											pl2.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x2, d_y2).get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl2.rook[2].isPieceDead() == true){
 											pl2.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.rook[1].getColor(), b.getSquare(d_x2, d_y2).get_x(), promo_pos, null);
 											pl2.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x2, d_y2).get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									////////////////////////////
 									if (promo_str == "knight")
 									{
 										if (pl2.knight[1].isPieceDead() == true)
 										{
 											pl2.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.knight[1].getColor(), b.getSquare(d_x2, d_y2).get_x(), promo_pos, null);
 											pl2.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x2, d_y2).get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl2.knight[2].isPieceDead() == true){
 											pl2.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.knight[1].getColor(), b.getSquare(d_x2, d_y2).get_x(), promo_pos, null);
 											pl2.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x2, d_y2).get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									////////////////////////////
 									if (promo_str == "bishop")
 									{
 										if (pl2.bishop[1].isPieceDead() == true)
 										{
 											pl2.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.bishop[1].getColor(), b.getSquare(d_x2, d_y2).get_x(), promo_pos, null);
 											pl2.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x2, d_y2).get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl2.bishop[2].isPieceDead() == true){
 											pl2.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.bishop[1].getColor(), b.getSquare(d_x2, d_y2).get_x(), promo_pos, null);
 											pl2.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x2, d_y2).get_x(), promo_pos);
 											promo_success = true;
 										}
 									}								
 									////////////////////////////
 									if (promo_str == "queen")
 									{
 										if (pl2.queen.isPieceDead() == true)
 										{
 											pl2.queen.setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.queen.getColor(), b.getSquare(d_x2, d_y2).get_x(), promo_pos, null);
 											pl2.queen.setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x2, d_y2).get_x(), promo_pos);
 											promo_success = true;
 									}
 								}
 							 }
 							}
 							if (b.getSquare(d_x2, d_y2).getPiece().enpass == true)
 							{
 								opp_x = b.getSquare(d_x2, d_y2).getPiece().en_x;
 								opp_y = b.getSquare(d_x2, d_y2).getPiece().en_y;
 								pl2.killPiece(b.getSquare(opp_x, opp_y));
 							}
 						}
 						b.displayBoard();
 						System.out.println(" ====================================================================================== ");
 						i++;
 						more = "n";
 						System.out.println("Do you want to continue ? (y/n) : ");
 						try {
 							more = br.readLine();
 						} catch (IOException e) {
 							System.out.println("Error!");
 							System.exit(1);
 						}
 					} while (more.charAt(0) == 'y');
 					//nitin playing around
 					playMode = 0;
 			  }
 
 			  if(playMode == 2){
 				  boolean result;
 				  String originalType = "";
 				  Square s_rand = null;
 				  Move randomMove = null;
 				  b.displayBoard();
 				  int i = 0;
 					do
 					{
 						if (i % 2 == 1)
 							System.out.println("\n\n -------------------------------- Black's Turn ---------------------------------------- ");
 						else
 							System.out.println("\n\n -------------------------------- White's Turn ---------------------------------------- ");
 
 
 						if (i % 2 == 0)
 						{
 							int s_x=0,s_y=0,d_x=0,d_y=0;
 							do {
 
 								System.out.println("White Player Please Make Move (Format:Source_Col,Source_Row,Dest_Col,Dest_Row):");
 								String move = "";
 								try {
 									 move = br.readLine();
 								} catch (IOException e) {
 									e.printStackTrace();
 								}
 								String[] moves = move.split(",");
 								s_x = Integer.parseInt(moves[1]);
 								s_x = (9 - s_x);
 
 								char sy = moves[0].charAt(0);
 								s_y = (int) sy-96;
 								d_x = Integer.parseInt(moves[3]);
 								d_x = (9 - d_x);
 
 								char dy = moves[2].charAt(0);
 								d_y = (int) dy-96;
 								
 								if (b.getSquare(s_x, s_y).getPiece() != null)
 									originalType = b.getSquare(s_x, s_y).getPiece().getType().toString();
 								else
 									originalType = "";
 
 								result = pl1.moveTo(b.getSquare(s_x, s_y), b.getSquare(d_x, d_y));
 
 							} while(result == false);
 
 							//////////////////
 							promo_success = false;
 							//System.out.println("Now moving " + originalType + " from (" + pl1.originalSquare.get_x() + ", " + pl1.originalSquare.get_y() + ") to (" + s_rand.get_x() + ", " + s_rand.get_y() + ") ");
 							if (originalType.toString() == "Pawn")
 							{
 								String promo_str = b.getSquare(d_x, d_y).getPiece().promotion;
 								int promo_pos = b.getSquare(d_x, d_y).getPiece().promo_pos;
 
 								if (promo_str != "")
 								{
 									if (promo_str == "rook")
 									{
 										if (pl1.rook[1].isPieceDead() == true)
 										{
 											pl1.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.rook[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.rook[2].isPieceDead() == true){
 											pl1.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.rook[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									////////////////////////////
 									if (promo_str == "knight")
 									{
 										if (pl1.knight[1].isPieceDead() == true)
 										{
 											pl1.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.knight[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.knight[2].isPieceDead() == true){
 											pl1.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.knight[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									////////////////////////////
 									if (promo_str == "bishop")
 									{
 										if (pl1.bishop[1].isPieceDead() == true)
 										{
 											pl1.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.bishop[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.bishop[2].isPieceDead() == true){
 											pl1.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.bishop[1].getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 										}
 									}								
 									////////////////////////////
 									if (promo_str == "queen")
 									{
 										if (pl1.queen.isPieceDead() == true)
 										{
 											pl1.queen.setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.queen.getColor(), b.getSquare(d_x, d_y).get_x(), promo_pos, null);
 											pl1.queen.setSquare(new_s);
 
 											Board.instance.setSquare(new_s, b.getSquare(d_x, d_y).get_x(), promo_pos);
 											promo_success = true;
 									}
 								}
 							 }
 								if (b.getSquare(d_x, d_y).getPiece().enpass == true)
 								{
 									opp_x = b.getSquare(d_x, d_y).getPiece().en_x;
 									opp_y = b.getSquare(d_x, d_y).getPiece().en_y;
 									pl1.killPiece(b.getSquare(opp_x, opp_y));
 								}
 							}		
 							////////////////////
 							//System.out.println("Now moving " + originalType + " from (" + s_x + ", " + s_y + ") to (" + d_x + ", " + d_y + ") ");
 						} else {
 							result = false;
 							randomMove = null;
 							do {
 								randomMove = pl2.selectBestMove();
 								//System.out.println("Now moving " + randomMove.getSource().getPiece().getType() + " from (" + randomMove.getSource().get_x()+ ", " + randomMove.getSource().get_y() + ") to (" + randomMove.getDestinationSquare().get_x() + ", " + randomMove.getDestinationSquare().get_y() + ") ");
 								
 								Square source_sq = null, dest_sq = null;
 								source_sq = b.getSquare(randomMove.getSource().get_x(), randomMove.getSource().get_y());
 								dest_sq = b.getSquare(randomMove.getDestinationSquare().get_x(), randomMove.getDestinationSquare().get_y());
 								result = pl2.moveTo(source_sq, dest_sq);//randomMove.getSource(), randomMove.getDestinationSquare());							
 								//System.out.println(" 1. result = " + result);
 							} while(result == false);
 							//System.out.println("Now moving " + randomMove.getSource().getPiece().getType() + " from (" + randomMove.getSource().get_x()+ ", " + randomMove.getSource().get_y() + ") to (" + randomMove.getDestinationSquare().get_x() + ", " + randomMove.getDestinationSquare().get_y() + ") ");
 							//System.out.println(" 2. result = " + result);
 							promo_success = false;
 							//System.out.println("Now moving " + originalType + " from (" + pl2.originalSquare.get_x() + ", " + pl2.originalSquare.get_y() + ") to (" + s_rand.get_x() + ", " + s_rand.get_y() + ") ");
 							
 							if (randomMove.getDestinationSquare().getPiece() != null)
 							if (randomMove.getDestinationSquare().getPiece().getType() == Type.Pawn)
 							{
 								String promo_str = randomMove.getDestinationSquare().getPiece().promotion;
 								int promo_pos = randomMove.getDestinationSquare().getPiece().promo_pos;
 
 								if (promo_str != "")
 								{
 									if (promo_str == "rook")
 									{
 										if (pl2.rook[1].isPieceDead() == true)
 										{
 											pl2.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.rook[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl2.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl2.rook[2].isPieceDead() == true){
 											pl2.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.rook[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl2.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									////////////////////////////
 									if (promo_str == "knight")
 									{
 										if (pl2.knight[1].isPieceDead() == true)
 										{
 											pl2.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.knight[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl2.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl2.knight[2].isPieceDead() == true){
 											pl2.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.knight[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl2.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									////////////////////////////
 									if (promo_str == "bishop")
 									{
 										if (pl2.bishop[1].isPieceDead() == true)
 										{
 											pl2.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.bishop[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl2.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl2.bishop[2].isPieceDead() == true){
 											pl2.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.bishop[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl2.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										}
 									}								
 									////////////////////////////
 									if (promo_str == "queen")
 									{
 										if (pl2.queen.isPieceDead() == true)
 										{
 											pl2.queen.setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl2.queen.getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl2.queen.setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 									}
 								}
 							 }
 								
 								if (b.getSquare(randomMove.getDestinationSquare().get_x(), randomMove.getDestinationSquare().get_y()).getPiece() != null)
 								{
 									if (b.getSquare(randomMove.getDestinationSquare().get_x(), randomMove.getDestinationSquare().get_y()).getPiece().enpass == true)
 									{
 										opp_x = b.getSquare(randomMove.getDestinationSquare().get_x(), randomMove.getDestinationSquare().get_y()).getPiece().en_x;
 										opp_y = b.getSquare(randomMove.getDestinationSquare().get_x(), randomMove.getDestinationSquare().get_y()).getPiece().en_y;
 										pl2.killPiece(b.getSquare(opp_x, opp_y));
 									}
 								}
 							}
 
 							/////////////////////////////////////////
 							if (promo_success == false)
 								Board.instance.setSquare(randomMove.getSource(), randomMove.getSource().get_x(), randomMove.getSource().get_y());
 						}
 						b.displayBoard();
 						System.out.println(" ====================================================================================== ");
 						i++;
 						more = "n";
 						System.out.println("Do you want to continue ? (y/n) : ");
 						try {
 							more = br.readLine();
 						} catch (IOException e) {
 							System.out.println("Error!");
 							System.exit(1);
 						}
 					} while (more.charAt(0) == 'y');
 					//nitin playing around
 					playMode = 0;				  
 			  }
 
 			  if(playMode == 3){
 					Square s_rand = null;
 					String originalType = "";
 					boolean result;
 
 					int i = 0;
 					do
 					{
 						if (i % 2 == 1)
 							System.out.println("\n\n -------------------------------- Black's Turn ---------------------------------------- ");
 						else
 							System.out.println("\n\n -------------------------------- White's Turn ---------------------------------------- ");
 
 						Move randomMove = null;
 						//Board.instance.displayBoard();
 						//b.displayBoard();
 						if (i % 2 == 0)
 						{
 							do {
 								//s_rand = pl1.randomMove();
 								randomMove = pl1.selectBestMove();
 								//System.out.println("Now moving from (" + randomMove.getSource().get_x() + ", " + randomMove.getSource().get_y());
 								if (pl1.isCastling == true)
 								{
 									result = pl1.moveTonoCheck(randomMove.getSource(), randomMove.getDestinationSquare());
 									pl1.isCastling = false;
 								} else {
 									result = pl1.moveTo(randomMove.getSource(), randomMove.getDestinationSquare());
 								}
 								//System.out.println("Now moving " + originalType + " from (" + randomMove.getSource().get_x() + ", " + randomMove.getSource().get_y() + " to (" + randomMove.getDestinationSquare().get_x() + ", " + randomMove.getDestinationSquare().get_y());
 							} while(result == false);
 
 							promo_success = false;
 //							System.out.println("Now moving " + originalType + " from (" + pl1.originalSquare.get_x() + ", " + pl1.originalSquare.get_y() + ") to (" + s_rand.get_x() + ", " + s_rand.get_y() + ") ");
 							if (randomMove.getDestinationSquare().getPiece().getType() == Type.Pawn)
 							{
 								String promo_str = randomMove.getDestinationSquare().getPiece().promotion;
 								int promo_pos = randomMove.getDestinationSquare().getPiece().promo_pos;
 
 								if (promo_str != "")
 								{
 									if (promo_str == "rook")
 									{
 										if (pl1.rook[1].isPieceDead() == true)
 										{
 											pl1.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.rook[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.rook[2].isPieceDead() == true){
 											pl1.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.rook[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									if (promo_str == "knight")
 									{
 										if (pl1.knight[1].isPieceDead() == true)
 										{
 											pl1.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.knight[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.knight[2].isPieceDead() == true){
 											pl1.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.knight[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									if (promo_str == "bishop")
 									{
 										if (pl1.bishop[1].isPieceDead() == true)
 										{
 											pl1.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.bishop[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.bishop[2].isPieceDead() == true){
 											pl1.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.bishop[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									if (promo_str == "queen")
 									{
 										if (pl1.queen.isPieceDead() == true)
 										{
 											pl1.queen.setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.queen.getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.queen.setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 
 								}
 								////////////////////////////////////////
 							if (promo_success == false)
 								Board.instance.setSquare(randomMove.getSource(), randomMove.getSource().get_x(), randomMove.getSource().get_y());
 					} else {
 							randomMove = null;
 							do {
 								//s_rand = pl2.randomMove();
 								randomMove = pl2.selectBestMove();
 
 								if (pl2.isCastling == true)
 								{
 									result = pl2.moveTonoCheck(randomMove.getSource(), randomMove.getDestinationSquare());
 									pl2.isCastling = false;
 								}
 								else {
 									result = pl2.moveTo(randomMove.getSource(), randomMove.getDestinationSquare());
 								}
 							} while(result == false);
 
 							/////////////////////////////////////////////////////////////////////////
 
 							promo_success = false;
 //							System.out.println("Now moving " + originalType + " from (" + pl1.originalSquare.get_x() + ", " + pl1.originalSquare.get_y() + ") to (" + s_rand.get_x() + ", " + s_rand.get_y() + ") ");
 							if (randomMove.getDestinationSquare().getPiece().getType() == Type.Pawn)
 							{
 								String promo_str = randomMove.getDestinationSquare().getPiece().promotion;
 								int promo_pos = randomMove.getDestinationSquare().getPiece().promo_pos;
 
 								if (promo_str != "")
 								{
 									if (promo_str == "rook")
 									{
 										if (pl1.rook[1].isPieceDead() == true)
 										{
 											pl1.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.rook[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.rook[2].isPieceDead() == true){
 											pl1.rook[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.rook[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.rook[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									if (promo_str == "knight")
 									{
 										if (pl1.knight[1].isPieceDead() == true)
 										{
 											pl1.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.knight[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.knight[2].isPieceDead() == true){
 											pl1.knight[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.knight[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.knight[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									if (promo_str == "bishop")
 									{
 										if (pl1.bishop[1].isPieceDead() == true)
 										{
 											pl1.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.bishop[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										} else if (pl1.bishop[2].isPieceDead() == true){
 											pl1.bishop[1].setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.bishop[1].getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.bishop[1].setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 									if (promo_str == "queen")
 									{
 										if (pl1.queen.isPieceDead() == true)
 										{
 											pl1.queen.setIsDead(false);
 											Square new_s = null;
 											new_s = new Square(pl1.queen.getColor(), randomMove.getDestinationSquare().get_x(), promo_pos, null);
 											pl1.queen.setSquare(new_s);
 
 											Board.instance.setSquare(new_s, randomMove.getDestinationSquare().get_x(), promo_pos);
 											promo_success = true;
 										}
 									}
 								}
 								////////////////////////////////////////
 							//////////////////////////////////////////////////////////////////////////
 						}
 					}
 							if (promo_success == false)
 								Board.instance.setSquare(randomMove.getSource(), randomMove.getSource().get_x(), randomMove.getSource().get_y());
 						}
 						Board.instance.displayBoard();
 						System.out.println(" ====================================================================================== ");
 						i++;
 						more = "n";
 						System.out.println("Do you want to continue ? (y/n) : ");
 						try {
 							more = br.readLine();
 						} catch (IOException e) {
 							System.out.println("Error!");
 							System.exit(1);
 						}
						
 					} while (more.charAt(0) == 'y');
 					//nitin playing around
 					playMode = 0;
 			  }
 		}while(!exit.equals("Q"));
 		System.out.println("\nGoodbye!");
 	}
 }
 
