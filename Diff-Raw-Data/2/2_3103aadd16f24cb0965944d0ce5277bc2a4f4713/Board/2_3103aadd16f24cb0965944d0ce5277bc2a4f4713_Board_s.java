 /* Board.java */
 
 package player;
 
 import list.*;
 
 class Board {
 
 	public static final int WHITE = 1;
 	public static final int BLACK = 0;
 	public static final int LOWESTVAL = -100;
 	public static final int HIGHESTVAL = 100;
 	private Chip[][] gameboard;
 	private int numPieces;
 	private DList networks;
 
 	/**
 	 * makes a blank board
 	 */
 	public Board() {
 		gameboard = new Chip[8][8]; 
 
 	}
 
     /**
      * returns the number of pieces on the board
      */
 
 	public int numPieces() {
 		return numPieces;
 	}
 
     /**
      * returns a DList of all pieces on the board
      */
 	public DList pieces() {
 		DList pieces = new DList();
 		for (int x = 0; x < gameboard.length; x++) {
 			for (int y = 0; y < gameboard[0].length; y++) {
 				if (gameboard[x][y] != null) {
 					pieces.insertBack(gameboard[x][y]);
 				}
 			}
 		}
 		return pieces;
 	}
 
 	/**
 	 * if moveKind is ADD return addChip if moveKind is Step return moveChip
      * returns true if the move was successfully carried out
      * returns false otherwise
      *
      * color - passes the color associated with the move being passed in
      * based on turn
      * m - a Move that determines which kind of move is to be used
 	 */
 	public boolean makeMove(int color, Move m) {
 		//System.out.println("BEFORE MOVE");
 		//System.out.println("numpieces: " + numPieces());
 		//printboard(this);
 		if (m.moveKind == Move.ADD) {
 			return addChip(color, m);
 		} else if (m.moveKind == Move.STEP) {
 			return moveChip(color, m);
 		}
 		return false;
 	}
 
 	/**
 	 * checks if the move is valid
      * returns false if it isn't
      * otherwise adds chip to board and returns true
      * for each chip in the new chip's line of sight recalculate the seen chip's sight
      *
      * color, m - are whats needed to create a Chip
 	 */
 	private boolean addChip(int color, Move m) {
 		// checks if valid
 		if (isValid(color, m)) {
 			Chip c = new Chip(m.x1, m.y1, color);
 			gameboard[m.x1][m.y1] = c;
 			Chip[] chips = lineOfSight(c);
 			// for each chip in the new chip's line of sight
 			// recaluate the seen chip's sight
 			for (int i = 0; i < chips.length; i++) {
 				chips[i].clear();
 				Chip[] tmp = lineOfSight(chips[i]);
 				for (int j = 0; j < tmp.length; j++) {
 					chips[i].addC(tmp[j]);
 				}
 			}
 			numPieces++;
 			return true;
 		}
 		// returns false if not valid
 		return false;
 	}
 
 	/**
 	 * returns all valid moves for given color and move
 	 */
 	public DList validMoves(int color) {
 
 		DList allmoves = new DList();
 		for (int i = 0; i < gameboard.length; i++) {
 			for (int j = 0; j < gameboard[0].length; j++) {
 
 				if (numPieces >= 20) { // try moving if more than 20 pieces on
 										// board
 					if (gameboard[i][j] != null) { // and contains and also
 													// contains piece
 
 						for (int a = 0; a < gameboard.length; a++) {
 							for (int b = 0; b < gameboard[0].length; b++) {
 								// to every other possible space
 								Move amove = new Move(a, b, i, j);
 								if (isValid(color, amove)) {
 									allmoves.insertBack(amove);
 								} // TODO number of indentations is scary. can
 									// this change?
 							}
 						}
 					}
 				} else {
 					Move trymove = new Move(i, j); // else try adding
 					if (isValid(color, trymove)) {
 						allmoves.insertBack(trymove);
 					}
 
 				}
 			}
 		}
 		return allmoves;
 	}
 
 	/**
 	 * store and remove old chip from board
      * checks if valid (must check over here so isValid doesn't count in the old chip)
      * returns false if it isn't valid and readd the chip
      * add a new chip at location x,y
      * return true
 	 */
 	private boolean moveChip(int color, Move m) {
 		// store and remove old chip from board
 		Chip c = new Chip(m.x2, m.y2, color);
 		// checks if valid
 		if (isValid(color, m)) {
 			removeChip(gameboard[m.x2][m.y2]);
 			// add a new chip at location x,y
 			addChip(color, new Move(m.x1, m.y1));
 			return true;
 		}
 		return false;
 	}
 
     /**
      * deletes itself off the gameboard and then for each chip in sight, rebuild their
      * inSight without the existence of chip c
      *
      * c - the chip that is to be removed
      */
 	private void removeChip(Chip c) {
 		Chip[] chips = lineOfSight(c);
 		gameboard[c.getX()][c.getY()] = null;
 		numPieces--;
 		for (int i = 0; i < chips.length; i++) {
 			chips[i].clear();
 			Chip[] tmp = lineOfSight(chips[i]);
 			for (int j = 0; j < tmp.length; j++) {
 				chips[i].addC(tmp[j]);
 			}
 		}
 	}
 
 	public void undo(Move m) {
 		if (m.moveKind == Move.ADD) {
 			removeChip(gameboard[m.x1][m.y1]);
 		} else if (m.moveKind == Move.STEP) {
 			makeMove(gameboard[m.x1][m.y1].color(), new Move(m.x2, m.y2, m.x1,
 					m.y1));
 		}
 	}
 
 	/**
 	 * returns a score from -100 to 100 100 is a win for self
 	 */
 	public double value(int color) {
 		if (isFinished(color)) {
 			System.out.print("hola");
 			return 100;
 		} else if (isFinished(MachinePlayer.otherPlayer(color))) {
 			System.out.print("avoid");
 			return -100;
 		}
 		DList allNetworks = this.findNetworks(color);
 		DListNode aNode = allNetworks.front();
 		double total = 0;
 		for (int i = 0; i < allNetworks.length(); i++) {
 			System.out.println(total);
 			/*
 			 * if (((Chip) ((DList) aNode.item).front().item).color()==color) {
 			 * total+=((DList) aNode.item).length();
 			 * 
 			 * } else if (((Chip) ((DList)
 			 * aNode.item).front().item).color()==MachinePlayer
 			 * .otherPlayer(color)) { total-=((DList) aNode.item).length(); }
 			 */
 			DList aList = (DList) aNode.item;
 			if (inEndGoal((Chip) aList.front().item, color) == 1
 					|| inEndGoal((Chip) aList.back().item, color) == 2) {
 				if (((Chip) ((DList) aNode.item).front().item).color() == color) {
 					total += ((DList) aNode.item).length();
 				//	System.out.println(((DList) aNode.item).length());
 
 				} else if (((Chip) ((DList) aNode.item).front().item).color() == MachinePlayer
 						.otherPlayer(color)) {
 					total -= ((DList) aNode.item).length();
 				//	System.out.println(-1*((DList) aNode.item).length());
 				}
 
 			}
 			if (inEndGoal((Chip) aList.front().item, color) == 2
 					|| inEndGoal((Chip) aList.back().item, color) == 1) {
 				if (((Chip) ((DList) aNode.item).front().item).color() == color) {
 					total += ((DList) aNode.item).length();
 				//	System.out.println(((DList) aNode.item).length());
 
 					
 				} else if (((Chip) ((DList) aNode.item).front().item).color() == MachinePlayer
 						.otherPlayer(color)) {
 					total -= ((DList) aNode.item).length();
 				//	System.out.println(-1*((DList) aNode.item).length());
 
 				}
 			}
 
 			aNode = allNetworks.next(aNode);
 		}
 
 		if (total < 0) {
 			total = -1 * Math.sqrt(Math.abs(total));
 		} else {
 			total = Math.sqrt(total);
 		}
 
 		//System.out.println(total+"\n");
 
 		if (total >= 100) {
 			System.out.println("A");
 			return 99;
 		} else if (total <= -100) {
 			System.out.println("B");
 			return -99;
 		}
 		System.out.println("\nVALUE IS"+total+"\n");
 		return total;
 
 	}
 
 	/**
 	 * returns a bool to tell you if match is finished. Needed for min max. Use
 	 * in value code
 	 */
 	public boolean isFinished(int color) {
 		networks = findNetworks(color);
 		DListNode aNode = networks.front();
 		for (int i = 0; i < networks.length(); i++) {
 			DList aList = (DList) aNode.item;
 			if (aList.length() >= 6
 					&& inEndGoal((Chip) aList.front().item, color) == 1) {
 				if (inEndGoal((Chip) aList.back().item, color) == 2) {
 					DListNode aListNode = aList.front();
 					for (int ii = 0; ii < aList.length(); ii++) {
 						//System.out.print(aListNode.item);
 						aListNode = aList.next(aListNode);
 					}
 					return true;
 				}
 			}
 			if (aList.length() >= 6
 					&& inEndGoal((Chip) aList.front().item, color) == 2) {
 				if (inEndGoal((Chip) aList.back().item, color) == 1) {
 					DListNode aListNode = aList.front();
 					for (int ii = 0; ii < aList.length(); ii++) {
 						aListNode = aList.next(aListNode);
 					}
 					return true;
 				}
 			}
 
 			aNode = networks.next(aNode);
 		}
 
 		return false;
 	}
 
 	/**
 	 * returns true if Move for given color is valid
 	 * 
 	 * chip not in four corners
      * black pieces are not in 0-1 to 0-6 or 7-1 to 7-6 (inclusive)
      * white pieces are not in 1-0 to 6-0 or 1-7 to 6-7 (inclusive)
 	 * no chip may be placed in a occupied square a chip may not be a cluster(2+ adjacent chips)
 	 */
 	private boolean isValid(int color, Move m) {
 		if (m.moveKind == Move.QUIT) {
 			return false;
         // if square is occupied
         // return false
 		} else if (m.moveKind == Move.ADD && numPieces() >= 20) {
 			return false;
 		} else if (m.moveKind == Move.STEP && numPieces() < 20) {
 			return false;
 		} else if (gameboard[m.x1][m.y1] != null) {
 			return false;
 			// if in 0-0, 0-7, 7-0, 7-7
 			// return false
 		} else if ((m.x1 == 0 || m.x1 == 7) && (m.y1 == 0 || m.y1 == 7)) {
 			return false;
 			// else if black and in 0-1 to 0-6 or in 7-1 to 7-6
 			// return false
 		} else if (color == BLACK && (m.x1 == 0 || m.x1 == 7)
 				&& (m.y1 >= 1 && m.y1 <= 6)) {
 			return false;
 			// else if white and in 1-0 to 6-0 or in 1-7 to 6-7
 			// return false
 		} else if (color == WHITE && (m.x1 >= 1 && m.x1 <= 6)
 				&& (m.y1 == 0 || m.y1 == 7)) {
 			return false;
 			// else if is a cluster(2+ adjacent chips)
 			// return false
 		} else if (isCluster(new Chip(m.x1, m.y1, color), new Chip(m.x2, m.y2,
 				color), 0)) {
 			return false;
 			// else if moving nonexistant chips return false
 		} else if (m.moveKind == Move.STEP && gameboard[m.x2][m.y2] == null) {
 			return false;
 			// else if moving a different color than self
 			// return false
 		} else if (m.moveKind == Move.STEP
 				&& gameboard[m.x2][m.y2].color() != color) {
 			return false;
 			// else
 			// return true
 		}
 		return true;
 	}
 
 	/**
 	 * checks all neighboring spaces on the gameboard around chip c
      * do not count the space to be occupied by chip c
      * returns true if the chip has more than 1 neighbor
      * check if a neighbor has more than 1 neighbor
      * returns false otherwise
      *
      * c - where you start checking from
      * o - ignores o in the case of a move
      * n - the recursive depth
 	 */
 	private boolean isCluster(Chip c, Chip o, int n) { // ignores o chip in the
 														// case of a move.
 		for (int x = c.getX() - 1; x <= c.getX() + 1; x++) {
 			for (int y = c.getY() - 1; y <= c.getY() + 1; y++) {
 				if (x >= 0 && x <= 7 && y >= 0 && y <= 7
 						&& !(x == c.getX() && y == c.getY())
 						&& !(x == o.getX() && y == o.getY())) {
 					if (gameboard[x][y] != null
 							&& gameboard[x][y].color() == c.color()) {
 						n++;
 						if (n > 1) {
 							return true;
 						}
 
 						if (isCluster(new Chip(x, y, gameboard[x][y].color()),
 								o, n)) {
 							return true;
 						}
 
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * finds and returns an array of chips that the chip c has direct line of
 	 * sight to
 	 */
 	private Chip[] lineOfSight(Chip c) {
 		int count = 0;
 		Chip[] inLine = new Chip[8];
 		Chip[] result;
 		// searches in 8 directions
 		for (int i = -1; i <= 1; i++) {
 			for (int j = -1; j <= 1; j++) {
 				if (!(i == 0 && j == 0)) {
 					Chip tmp = search(i, j, c);
 					if (tmp != null) {
 						// adds the first chip to an array
 						inLine[count] = tmp;
 						count++;
 					}
 				}
 			}
 		}
 		// copies array into an array with length equal to the number of chips
 		// in line of sight
 		result = new Chip[count];
 		for (int i = 0; i < count; i++) {
 			result[i] = inLine[i];
 		}
 		return result;
 	}
 
 	/**
 	 * checks gameboard at coordinates x and y if its a chip return that chip
 	 * otherwise return null
 	 */
 	private Chip search(int dx, int dy, Chip c) {
 		int x = c.getX() + dx;
 		int y = c.getY() + dy;
 		while (x >= 0 && x < gameboard.length && y >= 0
 				&& y < gameboard[0].length) {
 			if (gameboard[x][y] != null) {
 				return gameboard[x][y];
 			}
 			x += dx;
 			y += dy;
 		}
 		return null;
 	}
 
 	/**
 	 * A DList of networks(represented by DLists)
      * Can only go from similarly colored chips of direct line of sight
      * Cannot pass through the same chip twice
      * cannot have more than 1 chip in each goal
 	 * cannot pass through chip without changing direction
 	 */
 	public DList findNetworks(int color) {
 		// Dlist of networks(DLists)
 		DList networks = new DList();
 		Chip chip;
 		for (int x = 0; x < gameboard.length; x++) {
 			for (int y = 0; y < gameboard[x].length; y++) {
 				chip = gameboard[x][y];
 				if (chip != null && chip.color() == color) {
 					DList net = chip.network(color);
 					DList validNet = validNetworks(net, color);
					mergeNetworks(networks, net);
 				}
 			}
 		}
 		// cut out all repetitions
 		DList clean = new DList();
 		DListNode node = networks.front();
 		for (int i = 0; i < networks.length(); i++) {
 			if (!clean.has(node.item)) {
 				clean.insertFront(node.item);
 			}
 			node = networks.next(node);
 		}
 		return clean;
 	}
 
 	/**
 	 * returns all valid networks in DList list
      * cannot have more than 1 chip in each goal
      * Cannot pass through the same chip twice
      * cannot pass through chip without changing direction
      *
      * list - the DList of networks that contains valid/invalid networks
      * color - the "color" of the network
 	 */
 	private DList validNetworks(DList list, int color) {
 		DList valid = new DList();
 		DList network;
 		DListNode curr = list.front();
 		// for each element in list
 		while (curr != null) {
 			network = (DList) curr.item;
 			// cannot have more than 1 chip in each goal
 			// Cannot pass through the same chip twice
 			// cannot pass through chip without changing direction
 			if (checkGoals(network, color) && !aligned(network)) {
 				valid.insertBack(network);
 			}
 			curr = list.next(curr);
 		}
 		return valid;
 	}
 
 	/**
      * Check if there are a valid number of chips in the goals
      * returns true if there is no more than 2
 	 */
 /*	private boolean checkGoals(DList list, int color) {
 		int goal1 = 0;
 		int goal2 = 0;
 		DListNode curr = list.front();
 		while (curr != null) {
 			if (inEndGoal((Chip) curr.item, color) == 1) {
 				goal1++;
             }
 			if (inEndGoal((Chip) curr.item, color) == 2) {
 				goal2++;
 			}
 			curr = list.next(curr);
 		}
 		return goal1 <= 1 && goal2 <= 1;
 	}
 */
     private boolean checkGoals(DList list, int color) {
         int goal1 = 0;
         int goal2 = 0;
         DListNode curr = list.front();
         while (curr != null) {
             if (color == MachinePlayer.WHITE) {
                 if (((Chip) curr.item).getX() == 0) {
                     goal1++;
                 }
                 if (((Chip) curr.item).getX() == 7) {
                     goal2++;
                 }
             } else {
                 if (((Chip) curr.item).getY() == 0) {
                     goal1++;
                 }
                 if (((Chip) curr.item).getY() == 7) {
                     goal2++;
                 }
             }
             curr = list.next(curr);
         }
         return ((goal1 <= 1) && (goal2 <= 1));
     }
 
     /** returns 1 if in end goal 1, 2 if in 2, and zero if in none.
      *
      * curr - the chip being inspected
      * color - the color determines the valid goals
      */
 	private int inEndGoal(Chip curr, int color) {
 		if (color == MachinePlayer.WHITE) {
 			if ((curr).getX() == 0) {
 				return 1;
 				// goal1++;
 			} else if ((curr).getX() == 7) {
 				return 2;
 				// goal2++;
 			}
 		} else {
 			if ((curr).getY() == 0) {
 				return 1;
 				// goal1++;
 			} else if ((curr).getY() == 7) {
 				return 2;
 				// goal2++;
 			}
 		}
 		return 0;
 	}
 
 	/**
      * returns true if there are any 3 chips aligned in a row within the network "list"
 	 */
 	private boolean aligned(DList list) {
         //a shortcut, since you cant have 3 in a row unless there are 3 chips
 		if (list.length() < 3) {
 			return false;
 		}
 		DListNode curr = list.front();
 		int x = -1;
 		int y = -1;
 		int diffx = 8;
 		int diffy = 8;
 		int vercount = 0;
 		int horcount = 0;
 		int diacount = 0;
 		while (curr != null) {
 			int tx = ((Chip) curr.item).getX();
 			int ty = ((Chip) curr.item).getY();
 			if (diffx != 8 && diffy != 8
 					&& ((tx - x) == (ty - y) || (tx - x) == -(ty - y))
 					&& ((tx - x) > 0) == (diffx > 0)
 					&& ((ty - y) > 0) == (diffy > 0)) {
 				diacount++;
 			} else {
 				diacount = 0;
 			}
 			if (tx == x) {
 				vercount++;
 			} else {
 				vercount = 0;
 			}
 			if (ty == y) {
 				horcount++;
 			} else {
 				horcount = 0;
 			}
 			if (vercount >= 2 || horcount >= 2 || diacount >= 2) {
 				return true;
 			}
 			diffx = tx - x;
 			diffy = ty - y;
 			x = tx;
 			y = ty;
 			curr = list.next(curr);
 		}
 		return false;
 	}
 
 	/**
 	 * adds all elements of list2 that are not in list1 to list1
 	 */
 	private void mergeNetworks(DList list1, DList list2) {
 		DListNode n2 = list2.front();
 		while (n2 != null) {
 			if (!list1.has(n2.item)) {
 				list1.insertBack(n2.item);
 			}
 			n2 = list2.next(n2);
 		}
 	}
 
 /* ##########EVERYTHING BELOW THIS POINT IS FOR TESTING PURPOSES ONLY########## */
 
 	/*public Chip testChip(int x, int y) {
 		return gameboard[x][y];
 	}*/
 
 	/**
 	 * /** tester method to test private methods
 	 */
 	public void tester() {
 		Board board = new Board();
 		Move m11 = new Move(1, 1);
 		Move m13 = new Move(1, 3);
 		Move m33 = new Move(3, 3);
 		Move m31 = new Move(3, 1);
 		Move m35 = new Move(3, 5);
 		Move m01 = new Move(0, 1); // white left goal
 		Move m03 = new Move(0, 3); // white left goal
 		Move m55 = new Move(5, 5);
 		Move m15 = new Move(1, 5);
 		Move m51 = new Move(5, 1);
 		/*
 		 * printboard(board); System.out.println("adding m1");
 		 * board.addChip(WHITE, m1); printboard(board);
 		 * System.out.println("adding m2"); board.addChip(WHITE, m2);
 		 * printboard(board);
 		 * 
 		 * Chip c1 = board.gameboard[1][1]; Chip c2 = board.gameboard[1][3];
 		 * System.out.println("printing c1"); c1.visualChip(c1);
 		 * System.out.println("printing c2"); c2.visualChip(c2);
 		 * 
 		 * System.out.println("removing c1"); board.removeChip(c1);
 		 * printboard(board);
 		 * 
 		 * c2 = board.gameboard[1][3]; System.out.println("printing c2");
 		 * c2.visualChip(c2);
 		 */
 		/*
 		 * board.makeMove(Board.WHITE, m11); //board.makeMove(Board.WHITE, m13);
 		 * //board.makeMove(Board.WHITE, m33); //board.makeMove(Board.WHITE,
 		 * m31); //board.makeMove(Board.WHITE, m35);
 		 * //board.makeMove(Board.WHITE, m01); //board.makeMove(Board.WHITE,
 		 * m03); //board.makeMove(Board.WHITE, m55);
 		 * //board.makeMove(Board.WHITE, m15); //board.makeMove(Board.WHITE,
 		 * m51); Chip c1 = board.gameboard[m11.x1][m11.y1];
 		 * board.printboard(board);
 		 * 
 		 * DList list = c1.network(); System.out.println(list + "\n");
 		 * 
 		 * //ripped from validNetworks DList valid = new DList(); DList network;
 		 * DListNode curr = list.front(); while (curr != null) { network =
 		 * (DList) curr.item; if (checkGoals(network, WHITE) &&
 		 * !aligned(network)) { valid.insertBack(network); } curr =
 		 * list.next(curr); } System.out.println(valid);
 		 */
 		/*
 		 * DList list1 = new DList(); DList list2 = new DList();
 		 * System.out.println("list1: " + list1); System.out.println("list2: " +
 		 * list2); mergeNetworks(list1, list2);
 		 * System.out.println("merged list1: " + list1); list1.insertBack("A");
 		 * list1.insertBack("B"); list1.insertBack("C"); list2.insertBack("A");
 		 * list2.insertBack("C"); System.out.println("list1: " + list1);
 		 * System.out.println("list2: " + list2); mergeNetworks(list1, list2);
 		 * System.out.println("merged list1: " + list1); list2.insertBack("D");
 		 * System.out.println("list1: " + list1); System.out.println("list2: " +
 		 * list2); mergeNetworks(list1, list2);
 		 * System.out.println("merged list1: " + list1); list2.insertFront("E");
 		 * System.out.println("list1: " + list1); System.out.println("list2: " +
 		 * list2); mergeNetworks(list1, list2);
 		 * System.out.println("merged list1: " + list1);
 		 */
 	}
 
 	public void printboard(Board board) {
 		System.out.println("  01234567");
 		for (int y = 0; y < board.gameboard.length; y++) {
 			System.out.print(y + " ");
 			for (int x = 0; x < board.gameboard[0].length; x++) {
 				if (board.gameboard[x][y] == null) {
 					System.out.print("X");
 				} else if (board.gameboard[x][y].color() == WHITE) {
 					System.out.print(WHITE);
 				} else {
 					System.out.print(BLACK);
 				}
 			}
 			System.out.println();
 		}
 	}
 }
