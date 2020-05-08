 import java.util.*;
 
 /*
  *      Axiom.java
  *      
  */
 
 
 public class Axiom implements BoardGame{
 
     // data members
     private HashMap<String, Cube> board;
     private ArrayList<String> moves;
     private Player[] players = new Player[2];
     private int turn = Cube.BLACK;
     
     // constructor
     public Axiom() {
         // Initial Game-state
         board = new HashMap<String, Cube>();
         Cube b1 = new Cube(-1,0,2, Cube.XUP, Cube.NONE, Cube.BLACK);
         b1.addSceptre(Cube.ZUP, Cube.BLACK);
         b1.setBoard(board); // link board to cube
         board.put(b1.getName(), b1); // link cube to board
         Cube b2 = new Cube(0,0,2, Cube.ZDOWN, Cube.NONE, Cube.BLACK);
         b2.setBoard(board);
         board.put(b2.getName(), b2);
         Cube b3 = new Cube(1,0,2, Cube.XUP, Cube.NONE, Cube.BLACK);
         b3.addSceptre(Cube.ZUP, Cube.BLACK);
         b3.setBoard(board);
         board.put(b3.getName(), b3);
         Cube b4 = new Cube(-1,0,1, Cube.ZUP, Cube.XDOWN, Cube.BLACK);
         b4.setBoard(board);
         board.put(b4.getName(), b4);
         Cube b5 = new Cube(0,0,1, Cube.YDOWN, Cube.XDOWN, Cube.BLACK);
         b5.setBoard(board);
         board.put(b5.getName(), b5);
         Cube b6 = new Cube(1,0,1, Cube.ZUP, Cube.XDOWN, Cube.BLACK);
         b6.setBoard(board);
         board.put(b6.getName(), b6);
         
         Cube w1 = new Cube(1,1,2, Cube.XDOWN, Cube.NONE, Cube.WHITE);
         w1.addSceptre(Cube.ZUP, Cube.WHITE);
         w1.setBoard(board); // link board to cube
         board.put(w1.getName(), w1); // link cube to board
         Cube w2 = new Cube(0,1,2, Cube.ZDOWN, Cube.NONE, Cube.WHITE);
         w2.setBoard(board);
         board.put(w2.getName(), w2);
         Cube w3 = new Cube(-1,1,2, Cube.XDOWN, Cube.NONE, Cube.WHITE);
         w3.addSceptre(Cube.ZUP, Cube.WHITE);
         w3.setBoard(board);
         board.put(w3.getName(), w3);
         Cube w4 = new Cube(1,1,1, Cube.ZUP, Cube.XUP, Cube.WHITE);
         w4.setBoard(board);
         board.put(w4.getName(), w4);
         Cube w5 = new Cube(0,1,1, Cube.YUP, Cube.XUP, Cube.WHITE);
         w5.setBoard(board);
         board.put(w5.getName(), w5);
         Cube w6 = new Cube(-1,1,1, Cube.ZUP, Cube.XUP, Cube.WHITE);
         w6.setBoard(board);
         board.put(w6.getName(), w6);
     }
     
     public Axiom(HashMap<String, Cube> board) {
         this.board = board;
     }
     
     public void firstPlayer(Player p1) {
     	players[0] = p1;
     }
     
     public void secondPlayer(Player p2) {
     	players[1] = p2;
     }
     
     public void swapPlayers() {
 		turn = 1 - turn;
     }
     
     public void setTurn(int turn) {
     	this.turn = turn;
     }
     
     public void setMoves(ArrayList<String> moves) {
     	this.moves = moves;
     }
     
     public String showMove(int m) {
     	return moves.get(m);
     }
     
     // interface methods 
     // Detect if the color has won in the current board
     public boolean hasWon(int color) {
     	// if it is not your turn and there are different color sceptres on one cube
     	// or if there are two of the same on the same cube and it is your turn
     	for (String k : board.keySet()) {
     		Cube c = board.get(k);
     		if (c.twoSceptreDifferentColor() && color != players[turn].getNum()) {
     			return true;
     		}
     		if (c.twoSceptreSameColor() && color != c.getFace(c.firstSceptre())) {
     			return true;
     		}
     	}
 		// or, it is not your turn and the current player has no moves
 		if (color != players[turn].getNum()) {
 			movesCheck(players[turn]);
 			if (moves.size() == 0) {
 				return true;
 			}
 		}
         return false;
     }
 
     // Detect if the game is over in the current board
     public boolean gameOver() {
     	
     	// when two sceptres on one cube
     	for (String k : board.keySet()) {
     		Cube c = board.get(k);
     		if (c.twoSceptreDifferentColor() || c.twoSceptreSameColor()) {
     			return true;
     		}
     	}
     
     	// or the current player has no moves.
     	movesCheck(players[turn]);
     	if (moves.size() == 0) {
     		return true;
     	}
     
         return false;
     }
 
 	public int freeCubes(int p) {
 		int count = 0;
 		for (String k : new HashSet<String>(board.keySet())) {
 			Cube c = board.get(k);
 			if (c.isFree() && c.getColor() == p) {
 				count++;
 			}
 		}
 		return count;
 	}
 
 	// JMARTIN2 TO DO
 	// Generate a list of strings representing the possible moves
 	// for the Player p. num of Player is color.
 	public void generateMoves(Player p) {
 
 		moves = new ArrayList<String>();
 		// piece together string
 		// evaluate each cube
 		int count = 0;
 		for (String k : new HashSet<String>(board.keySet())) {
 
 			Cube c = board.get(k);
 			
 			// CUBE MOVES
 			if (c.isFree() && c.getColor() == p.getNum()) {
 			
 				// find all locations where this cube can be placed
 				HashSet<String> spots = new HashSet<String>();
 				HashSet<String> sceptlocs = new HashSet<String>();
 				for (String r : board.keySet()) {
 					if (!r.equals(k)) {
 					
 						// find free faces not under the table.
 						Cube c2 = board.get(r);
 						for (String n : c2.freeFaces()) {
 				
 							spots.add(n);
 							count++;
 						}
 						// MHG 11/5/2011 add all sceptre locs to set
 						if (c2.firstSceptre() != Cube.NONE) {
 							sceptlocs.add(c2.getNeighborString(c2.firstSceptre()));
 						}
 					}
 				}
 				// MHG 11/5/2011 Need to remove faces where sceptres live...
 				//System.out.println("Sceptre locs");
 				for (String s : sceptlocs) {
 					//System.out.println(s);
 					spots.remove(s);
 				}
 				
 				board.remove(k);
 				//System.out.println("Free Faces");
 				for (String s : spots) {
 					//System.out.println(s);
 					if (c.secondDome() == Cube.NONE) {
 						for (int i = 0; i < 6; i++) {
 							Cube t = new Cube(s, i, -1, c.getColor());
 							t.setBoard(board);
 							board.put(s, t);
 							if (t.legal()) {
 								moves.add("C(" + c.getName() + ") C(" + s + ")" + 
 									Cube.fnames[i] + " ");
 								//System.out.println("LEGAL");
 							}
 							board.remove(s);
 						}
 					} else {
 						for (int i = 0; i < 5; i++) {
 							for (int j = ((i + 1) % 2) + i + 1; j < 6; j++) {
 								// MHG 11/5/2011 FIX 
 								// cannot be opposite faces on dome, must be adjacent
 								Cube t = new Cube(s, i, j, c.getColor());
 								t.setBoard(board);
 								board.put(s, t);
 								if (t.legal()) {
 									moves.add("C(" + c.getName() + ") C(" + s + ")" + 
 										Cube.fnames[i] + " " + Cube.fnames[j]);
 									//System.out.println("LEGAL");
 								}
 								board.remove(s);
 							}
 						}
 					}
 				}
 				board.put(k, c);
 			}
 			
 			// SCEPTRE MOVES PLEASE AVERT YOUR EYES FROM MY UGLY UGLY CODE..
 			if (c.isOccupied() && c.getFace(c.firstSceptre()) == p.getNum()) {
 			
 				// find all places where this sceptre can be placed
 				int face = c.firstSceptre();
 				
				// DIAGONAL in same plane 
				// COMPRESS THIS TO ONE CASE SOMEHOW
 				if (face == Cube.ZUP || face == Cube.ZDOWN) {
 					
 					int zmod = 1;
 					String zc = "+";
 					if (face == Cube.ZDOWN) {
 						zmod = -1;
 						zc = "-";
 					}
 					
 					// while cubes in direction, keep making moves
 					int[] xmod = {1, 1, -1, -1};
 					int[] ymod = {1, -1, 1, -1};
 					for (int i = 0; i < xmod.length; i++) {
 						Cube cur = c;
 						boolean nei = true;
 						while (nei) {
 							String neighbor = "" + (cur.getX() + xmod[i]) + "," + 
 												   (cur.getY() + ymod[i]) + "," +
 													cur.getZ();
 							Cube who = board.get(neighbor);
 							if (who != null && who.getFace(face) == Cube.EMPTY) {
 								String neighbor2 = "" + (cur.getX() + xmod[i]) + "," + 
 														(cur.getY() + ymod[i]) + "," +
 														(cur.getZ() + zmod);
 								if (board.get(neighbor2) == null) {
 									moves.add("S(" + c.getName() + ") S(" + who.getName() + ")z" + zc + " ");
 									cur = who;
 									count++;
 								} else {
 									nei = false;
 								}
 							} else {
 								nei = false;
 							}
 						}
 					}
 				}
 				if (face == Cube.XUP || face == Cube.XDOWN) {
 					
 					int xmod = 1;
 					String xc = "+";
 					if (face == Cube.XDOWN) {
 						xmod = -1;
 						xc = "-";
 					}
 					
 					// while cubes in direction, keep making moves
 					// XUP, YUP
 					int[] zmod = {1, 1, -1, -1};
 					int[] ymod = {1, -1, 1, -1};
 					for (int i = 0; i < ymod.length; i++) {
 						Cube cur = c;
 						boolean nei = true;
 						while (nei) {
 							String neighbor = "" + (cur.getX()) + "," + 
 												   (cur.getY() + ymod[i]) + "," +
 												   (cur.getZ() + zmod[i]);
 							Cube who = board.get(neighbor);
 							if (who != null && who.getFace(face) == Cube.EMPTY) {
 								String neighbor2 = "" + (cur.getX() + xmod) + "," + 
 														(cur.getY() + ymod[i]) + "," +
 														(cur.getZ() + zmod[i]);
 								if (board.get(neighbor2) == null) {
 									moves.add("S(" + c.getName() + ") S(" + who.getName() + ")x" + xc + " " );
 									cur = who;
 									count++;
 								} else {
 									nei = false;
 								}
 							} else {
 								nei = false;
 							}
 						}
 					}
 				}
 				if (face == Cube.YUP || face == Cube.YDOWN) {
 					
 					int ymod = 1;
 					String yc = "+";
 					if (face == Cube.YDOWN) {
 						ymod = -1;
 						yc = "-";
 					}
 					
 					// while cubes in direction, keep making moves
 					// XUP, YUP
 					int[] zmod = {1, 1, -1, -1};
 					int[] xmod = {1, -1, 1, -1};
 					for (int i = 0; i < xmod.length; i++) {
 						Cube cur = c;
 						boolean nei = true;
 						while (nei) {
 							nei = false;
 							String neighbor = "" + (cur.getX() + xmod[i]) + "," + 
 												   (cur.getY()) + "," +
 												   (cur.getZ() + zmod[i]);
 							Cube who = board.get(neighbor);
 							if (who != null && who.getFace(face) == Cube.EMPTY) {
 								String neighbor2 = "" + (cur.getX() + xmod[i]) + "," + 
 														(cur.getY() + ymod) + "," +
 														(cur.getZ() + zmod[i]);
 								if (board.get(neighbor2) == null) {
 									moves.add("S(" + c.getName() + ") S(" + who.getName() + ")y" + yc + " ");
 									cur = who;
 									count++;
 									nei = true;
 								} 
 							}
 						}
 					}
 				}
 				
 				// ORTHOGONAL wrapping 
 				Cube cur = c;
 				int dir = face;
 				boolean nei = true;  // Z, XUP
 				while (nei) {						
 					nei = false;
 					//System.out.println(cur);
 					if (dir == Cube.ZUP) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(Cube.XUP);
 						if (a != null) {
 							b = a.getNeighbor(Cube.ZUP);
 						}
 						// check three placements
 						if (a == null) {
 							if (cur.isEmpty(Cube.XUP)) {
 								moves.add("S(" + c.getName() + ") S(" + cur.getName() + ")x+ ");
 								nei = true;
 								dir = Cube.XUP;
 							    count++;
 							}
 						} else if (b == null) {
 							if (a.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 								cur = a;
 							    count++;
 							}						
 						} else {
 							if (b.isEmpty(Cube.XDOWN)) {
 								moves.add("S(" + c.getName() + ") S(" + b.getName() + ")x- ");
 								nei = true;
 								dir = Cube.XDOWN;
 								cur = b;
 							    count++;
 							}						
 						}
 					}
 					if (dir == Cube.XUP) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(Cube.ZDOWN);
 						if (a != null) {
 							b = a.getNeighbor(Cube.XUP);
 						}
 						// check three placements
 						if (a == null) {
 						} else if (b == null) {
 							if (a.isEmpty(Cube.XUP)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")x+ ");
 								nei = true;
 								dir = Cube.XUP;
 								cur = a;
 							    count++;
 							}						
 						} else {
 							if (b.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + b.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 								cur = b;
 							    count++;
 							}						
 						}
 					}
 					if (dir == Cube.XDOWN) {
 						Cube a = cur.getNeighbor(Cube.ZUP);
 						// check two placements
 						if (a == null) {
 							if (cur.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + cur.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 							    count++;
 							}
 						} else {
 							if (a.isEmpty(Cube.XDOWN)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")x- ");
 								nei = true;
 								dir = Cube.XDOWN;
 								cur = a;
 							    count++;
 							}						
 						}
 					}
 				}
 				cur = c;
 				dir = face;
 				nei = true;  // Z, XDOWN
 				while (nei) {						
 					nei = false;
 					//System.out.println(cur);
 					if (dir == Cube.ZUP) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(Cube.XDOWN);
 						if (a != null) {
 							b = a.getNeighbor(Cube.ZUP);
 						}
 						// check three placements
 						if (a == null) {
 							if (cur.isEmpty(Cube.XDOWN)) {
 								moves.add("S(" + c.getName() + ") S(" + cur.getName() + ")x- ");
 								nei = true;
 								dir = Cube.XDOWN;
 							    count++;
 							}
 						} else if (b == null) {
 							if (a.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 								cur = a;
 							    count++;
 							}						
 						} else {
 							if (b.isEmpty(Cube.XUP)) {
 								moves.add("S(" + c.getName() + ") S(" + b.getName() + ")x+ ");
 								nei = true;
 								dir = Cube.XUP;
 								cur = b;
 							    count++;
 							}						
 						}
 					}
 					if (dir == Cube.XDOWN) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(Cube.ZDOWN);
 						if (a != null) {
 							b = a.getNeighbor(Cube.XDOWN);
 						}
 						// check three placements
 						if (a == null) {
 						} else if (b == null) {
 							if (a.isEmpty(Cube.XDOWN)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")x- ");
 								nei = true;
 								dir = Cube.XDOWN;
 								cur = a;
 							    count++;
 							}						
 						} else {
 							if (b.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + b.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 								cur = b;
 							    count++;
 							}						
 						}
 					}
 					if (dir == Cube.XUP) {
 						Cube a = cur.getNeighbor(Cube.ZUP);
 						// check two placements
 						if (a == null) {
 							if (cur.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + cur.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 							    count++;
 							}
 						} else {
 							if (a.isEmpty(Cube.XUP)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")x+ ");
 								nei = true;
 								dir = Cube.XUP;
 								cur = a;
 							    count++;
 							}						
 						}
 					}
 				}
 				cur = c;
 				dir = face;
 				nei = true;  // Z, YUP
 				while (nei) {						
 					nei = false;
 					//System.out.println(cur);
 					if (dir == Cube.ZUP) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(Cube.YUP);
 						if (a != null) {
 							b = a.getNeighbor(Cube.ZUP);
 						}
 						// check three placements
 						if (a == null) {
 							if (cur.isEmpty(Cube.YUP)) {
 								moves.add("S(" + c.getName() + ") S(" + cur.getName() + ")y+ ");
 								nei = true;
 								dir = Cube.YUP;
 							    count++;
 							}
 						} else if (b == null) {
 							if (a.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 								cur = a;
 							    count++;
 							}						
 						} else {
 							if (b.isEmpty(Cube.YDOWN)) {
 								moves.add("S(" + c.getName() + ") S(" + b.getName() + ")y- ");
 								nei = true;
 								dir = Cube.YDOWN;
 								cur = b;
 							    count++;
 							}						
 						}
 					}
 					if (dir == Cube.YUP) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(Cube.ZDOWN);
 						if (a != null) {
 							b = a.getNeighbor(Cube.YUP);
 						}
 						// check three placements
 						if (a == null) {
 						} else if (b == null) {
 							if (a.isEmpty(Cube.YUP)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")y+ ");
 								nei = true;
 								dir = Cube.YUP;
 								cur = a;
 							    count++;
 							}						
 						} else {
 							if (b.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + b.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 								cur = b;
 							    count++;
 							}						
 						}
 					}
 					if (dir == Cube.YDOWN) {
 						Cube a = cur.getNeighbor(Cube.ZUP);
 						// check two placements
 						if (a == null) {
 							if (cur.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + cur.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 							    count++;
 							}
 						} else {
 							if (a.isEmpty(Cube.YDOWN)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")y- ");
 								nei = true;
 								dir = Cube.YDOWN;
 								cur = a;
 							    count++;
 							}						
 						}
 					}
 				}
 				cur = c;
 				dir = face;
 				nei = true;  // Z, YDOWN
 				while (nei) {						
 					nei = false;
 					//System.out.println(cur);
 					if (dir == Cube.ZUP) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(Cube.YDOWN);
 						if (a != null) {
 							b = a.getNeighbor(Cube.ZUP);
 						}
 						// check three placements
 						if (a == null) {
 							if (cur.isEmpty(Cube.YDOWN)) {
 								moves.add("S(" + c.getName() + ") S(" + cur.getName() + ")y- ");
 								nei = true;
 								dir = Cube.YDOWN;
 							    count++;
 							}
 						} else if (b == null) {
 							if (a.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 								cur = a;
 							    count++;
 							}						
 						} else {
 							if (b.isEmpty(Cube.YUP)) {
 								moves.add("S(" + c.getName() + ") S(" + b.getName() + ")y+ ");
 								nei = true;
 								dir = Cube.YUP;
 								cur = b;
 							    count++;
 							}						
 						}
 					}
 					if (dir == Cube.YDOWN) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(Cube.ZDOWN);
 						if (a != null) {
 							b = a.getNeighbor(Cube.YDOWN);
 						}
 						// check three placements
 						if (a == null) {
 						} else if (b == null) {
 							if (a.isEmpty(Cube.YDOWN)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")y- ");
 								nei = true;
 								dir = Cube.YDOWN;
 								cur = a;
 							    count++;
 							}						
 						} else {
 							if (b.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + b.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 								cur = b;
 							    count++;
 							}						
 						}
 					}
 					if (dir == Cube.YUP) {
 						Cube a = cur.getNeighbor(Cube.ZUP);
 						// check two placements
 						if (a == null) {
 							if (cur.isEmpty(Cube.ZUP)) {
 								moves.add("S(" + c.getName() + ") S(" + cur.getName() + ")z+ ");
 								nei = true;
 								dir = Cube.ZUP;
 							    count++;
 							}
 						} else {
 							if (a.isEmpty(Cube.YUP)) {
 								moves.add("S(" + c.getName() + ") S(" + a.getName() + ")y+ ");
 								nei = true;
 								dir = Cube.YUP;
 								cur = a;
 							    count++;
 							}						
 						}
 					}
 				}
 				
 				cur = c;
 				dir = face;
 				nei = true;  // CLOCKWISE, xup, yup, xdown, ydown
 				while (nei) {
 					nei = false;
 					//System.out.println(cur);
 					if (dir == Cube.XUP) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(0, 1, 0);
 						b = cur.getNeighbor(1, 1, 0);
 
 						// check three placements
 						if (b != null) {
 							if (b.isEmpty(Cube.YDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + b.getName() + ")y- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YDOWN;
 									cur = b;
 								    count++;
 								}
 							}
 						} else if (a != null) {
 							if (a.isEmpty(Cube.XUP)) {
 								String m = "S(" + c.getName() + ") S(" + a.getName() + ")x+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.XUP;
 									cur = a;
 								    count++;
 								}
 							}
 						} else {
 							if (cur.isEmpty(Cube.YUP)) {
 								String m = "S(" + c.getName() + ") S(" + cur.getName() + ")y+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YUP;
 								    count++;
 								}
 							}
 						}
 					} else if (dir == Cube.YUP) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(-1, 0, 0);
 						b = cur.getNeighbor(-1, 1, 0);
 						// check three placements
 						if (b != null) {
 							if (b.isEmpty(Cube.XUP)) {
 								String m = "S(" + c.getName() + ") S(" + b.getName() + ")x+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.XUP;
 									cur = b;
 								    count++;
 								}
 							}
 						} else if (a != null) {
 							if (a.isEmpty(Cube.YUP)) {
 								String m = "S(" + c.getName() + ") S(" + a.getName() + ")y+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YUP;
 									cur = a;
 								    count++;
 								}
 							}
 						} else {
 							if (cur.isEmpty(Cube.XDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + cur.getName() + ")x- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.XDOWN;
 								    count++;
 								}
 							}
 						}
 					} else if (dir == Cube.XDOWN) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(0, -1, 0);
 						b = cur.getNeighbor(-1, -1, 0);
 						// check three placements
 						if (b != null) {
 							if (b.isEmpty(Cube.YUP)) {
 								String m = "S(" + c.getName() + ") S(" + b.getName() + ")y+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YUP;
 									cur = b;
 								    count++;
 								}
 							}
 						} else if (a != null) {
 							if (a.isEmpty(Cube.XDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + a.getName() + ")x- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.XDOWN;
 									cur = a;
 								    count++;
 								}
 							}
 						} else {
 							if (cur.isEmpty(Cube.YDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + cur.getName() + ")y- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YDOWN;
 								    count++;
 								}
 							}
 						}
 					} else if (dir == Cube.YDOWN) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(1, 0, 0);
 						b = cur.getNeighbor(1, -1, 0);
 						// check three placements
 						if (b != null) {
 							if (b.isEmpty(Cube.XDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + b.getName() + ")x- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.XDOWN;
 									cur = b;
 								    count++;
 								}
 							}
 						} else if (a != null) {
 							if (a.isEmpty(Cube.YDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + a.getName() + ")y- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YDOWN;
 									cur = a;
 								    count++;
 								}
 							}
 						} else {
 							if (cur.isEmpty(Cube.XUP)) {
 								String m = "S(" + c.getName() + ") S(" + cur.getName() + ")x+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.XUP;
 								    count++;
 								}
 							}
 						}
 					}
 				}
 				cur = c;
 				dir = face;
 				nei = true;  // COUNTERCLOCKWISE, xup, ydown, xdown, yup
 				while (nei) {
 					nei = false;
 					//System.out.println(cur);
 					if (dir == Cube.XUP) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(0, -1, 0);
 						b = cur.getNeighbor(1, -1, 0);
 
 						// check three placements
 						if (b != null) {
 							if (b.isEmpty(Cube.YUP)) {
 								String m = "S(" + c.getName() + ") S(" + b.getName() + ")y+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YUP;
 									cur = b;
 								    count++;
 								}
 							}
 						} else if (a != null) {
 							if (a.isEmpty(Cube.XUP)) {
 								String m = "S(" + c.getName() + ") S(" + a.getName() + ")x+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.XUP;
 									cur = a;
 								    count++;
 								}
 							}
 						} else {
 							if (cur.isEmpty(Cube.YDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + cur.getName() + ")y- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YDOWN;
 								    count++;
 								}
 							}
 						}
 					} else if (dir == Cube.YUP) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(1, 0, 0);
 						b = cur.getNeighbor(1, 1, 0);
 						// check three placements
 						if (b != null) {
 							if (b.isEmpty(Cube.XDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + b.getName() + ")x- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.XDOWN;
 									cur = b;
 								    count++;
 								}
 							}
 						} else if (a != null) {
 							if (a.isEmpty(Cube.YUP)) {
 								String m = "S(" + c.getName() + ") S(" + a.getName() + ")y+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YUP;
 									cur = a;
 								    count++;
 								}
 							}
 						} else {
 							if (cur.isEmpty(Cube.XUP)) {
 								String m = "S(" + c.getName() + ") S(" + cur.getName() + ")x+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.XUP;
 								    count++;
 								}
 							}
 						}
 					} else if (dir == Cube.XDOWN) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(0, 1, 0);
 						b = cur.getNeighbor(-1, 1, 0);
 						// check three placements
 						if (b != null) {
 							if (b.isEmpty(Cube.YDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + b.getName() + ")y- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YDOWN;
 									cur = b;
 								    count++;
 								}
 							}
 						} else if (a != null) {
 							if (a.isEmpty(Cube.XDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + a.getName() + ")x- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.XDOWN;
 									cur = a;
 								    count++;
 								}
 							}
 						} else {
 							if (cur.isEmpty(Cube.YUP)) {
 								String m = "S(" + c.getName() + ") S(" + cur.getName() + ")y+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YUP;
 								    count++;
 								}
 							}
 						}
 					} else if (dir == Cube.YDOWN) {
 						Cube a = null;
 						Cube b = null;
 						a = cur.getNeighbor(-1, 0, 0);
 						b = cur.getNeighbor(-1, -1, 0);
 						// check three placements
 						if (b != null) {
 							if (b.isEmpty(Cube.XUP)) {
 								String m = "S(" + c.getName() + ") S(" + b.getName() + ")x+ ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
									dir = Cube.XUP;
 									cur = b;
 								    count++;
 								}
 							}
 						} else if (a != null) {
 							if (a.isEmpty(Cube.YDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + a.getName() + ")y- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.YDOWN;
 									cur = a;
 								    count++;
 								}
 							}
 						} else {
 							if (cur.isEmpty(Cube.XDOWN)) {
 								String m = "S(" + c.getName() + ") S(" + cur.getName() + ")x- ";
 								if (!moves.contains(m)) {
 									moves.add(m);
 									nei = true;
 									dir = Cube.XDOWN;
 								    count++;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
     public boolean makeMove(Player p, int m) {
         // take string apart and make it happen
         // look up move in moves ArrayList
         
         String choice = moves.get(m);        
         //System.out.println("Making move" + choice);
         int a = choice.indexOf(')');
         int b = choice.indexOf('(', a);            
         int e = choice.indexOf(',', b);
         int f = choice.indexOf(',', e + 1);
         int g = choice.indexOf(')', f);
         int d = choice.indexOf(' ', f);
 /*		System.out.println("a = " + a);
 		System.out.println("b = " + b);
 		System.out.println("e = " + e);
 		System.out.println("f = " + f);
 		System.out.println("g = " + g);
 		System.out.println("d = " + d); */
         String x = choice.substring(b + 1, e);
         String y = choice.substring(e + 1, f);
         String z = choice.substring(f + 1, g);
         String dm1 = choice.substring(g + 1, d);
         String dm2 = choice.substring(d + 1, choice.length());
         String actualcube = choice.substring(2, choice.indexOf(')'));
         String s = choice.substring(a + 1, a + 3);    
             
         // if Cube move
         if (choice.charAt(0) == 'C') {
             // remove from board
             int clr = board.get(actualcube).getColor();
             board.remove(actualcube);
 
             // add new cube to board
 
             int DM1 = -1;
             int DM2 = -1;
             if (dm1.equals("x+")) {
                 DM1 = Cube.XUP;
             }
             else if (dm1.equals("x-")) {
                 DM1 = Cube.XDOWN;
             }
             else if (dm1.equals("y+")) {
                 DM1 = Cube.YUP;
             }
             else if (dm1.equals("y-")) {
                 DM1 = Cube.YDOWN;
             }
             else if (dm1.equals("z+")) {
                 DM1 = Cube.ZUP;
             }
             else if (dm1.equals("z-")) {
                 DM1 = Cube.ZDOWN;
             }
         ////////////////////////////////////
             if (dm2.equals("x+")) {
                 DM2 = Cube.XUP;
             }
             else if (dm2.equals("x-")) {
                 DM2 = Cube.XDOWN;
             }
             else if (dm2.equals("y+")) {
                 DM2 = Cube.YUP;
             }
             else if (dm2.equals("y-")) {
                 DM2 = Cube.YDOWN;
             }
             else if (dm2.equals("z+")) {
                 DM2 = Cube.ZUP;
             }
             else if (dm2.equals("z-")) {
                 DM2 = Cube.ZDOWN;
             }
             Cube temp = new Cube(Integer.parseInt(x),
                                  Integer.parseInt(y), 
                                  Integer.parseInt(z), DM1, DM2, clr);
             temp.setBoard(board);
             board.put(temp.getName(), temp);
         }
         // if Sceptre move
         
         if (choice.charAt(0) == 'S') {
             
             int clr = board.get(choice.substring(2, choice.indexOf(')'))).getColor();
         	int DM1 = -1;
             // remove from current location
             if (dm1.equals("x+")) {
                 DM1 = Cube.XUP;
             }
             else if (dm1.equals("x-")) {
                 DM1 = Cube.XDOWN;
             }
             else if (dm1.equals("y+")) {
                 DM1 = Cube.YUP;
             }
             else if (dm1.equals("y-")) {
                 DM1 = Cube.YDOWN;
             }
             else if (dm1.equals("z+")) {
                 DM1 = Cube.ZUP;
             }
             else if (dm1.equals("z-")) {
                 DM1 = Cube.ZDOWN;
             }
             
             //System.out.println(actualcube);
             Cube was = board.get(actualcube);
             //System.out.println(was);
 	        int sclr = was.getFace(was.firstSceptre());
             was.removeSceptre(was.firstSceptre());
     
             
             // add to new location
             String whereto = "" + x + "," + y + "," + z;
             
            	Cube c2 = board.get(whereto);
            	//System.out.println(c2);
            	c2.addSceptre(DM1, sclr);
             
             // if sceptre currently encroaching and leaving that cube, 
             // make cube disappear
             //System.out.println("scolor = " + sclr + ", clr = " + clr);
             //System.out.println("is free? " + was.isFree());
             if (sclr != clr && was.isFree()) {
             	board.remove(actualcube);
             	//System.out.println("removed????");
             }
             
         }
         // else, print "Must move a sceptre(S) or a cube(C)"
         // ask them to make a move again
         swapPlayers();
 
 		// remove all moves
 		moves = null;
 
         // never gets another move in this game, so always return false
         return false;
     }
 
     public ArrayList<Integer> legalMoves(Player p) {
 		movesCheck(p);
 		ArrayList<Integer> t = new ArrayList<Integer>();
 		for (int i = 0; i < moves.size(); i++) {
 			t.add(i);
 		}
         return t;
     }
     
     public boolean legalMove(Player p, int m) {
 		movesCheck(p);
         return m >= 0 && m < moves.size();
     }
     
     public String showMoves(Player p) {
 		movesCheck(p);
     	String s = "";
     	int i = 0;
     	for (String tm : moves) {
     		s += i + ": " + tm + "\n";
     		i++;
     	}
     	return s;
     }
 
 	public void movesCheck(Player p) {
     	if (moves == null) {
     		generateMoves(p);
     	}	
 	}
 
     // Returns a copy of the current board, for searching
     public BoardGame clone() {
         HashMap<String, Cube> b = new HashMap<String, Cube>();
         for (String s : board.keySet()) {
             Cube c = board.get(s).clone();
             c.setBoard(b);
             b.put(s, c);
         }
         Axiom cl = new Axiom(b);
         cl.firstPlayer(players[0]);
         cl.secondPlayer(players[1]);
         cl.setTurn(turn);
         cl.setMoves(moves);
         return cl;
     }
 
     public String toString() {
         String s = "Current Axiom Board\n";
         for (String k : board.keySet()) {
             Cube c = board.get(k);
             s += c + "\n";
             if (c.isOccupied()) {
                 s += c.toSceptreString() + "\n";
             }
         }
         return s;
     }
 
     public static void main (String args[]) {
         Axiom g = new Axiom();
        Player p1 = new Player(Cube.BLACK, Cube.WHITE, Player.HUMAN, 4);
        Player p2 = new Player(Cube.WHITE, Cube.BLACK, Player.HUMAN, 3);
         g.firstPlayer(p1);
         g.secondPlayer(p2);
         Host.hostGame(g, p1, p2);
     }
 }
