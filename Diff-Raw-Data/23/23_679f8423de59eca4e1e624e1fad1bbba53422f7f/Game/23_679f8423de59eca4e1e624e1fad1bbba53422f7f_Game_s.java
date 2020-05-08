 package quoridor;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.util.Scanner;
 
 import quoridor.Command.CommandType;
 import quoridor.Move.MoveType;
 
 import util.Two;
 
 
 /**
  * Game is in charge of storing Game states, asking for input to update them, playing, saving and displaying Games. 
  * 
  * <h2>Goals</h2>
  * <ul>
  * <li>Stores Games States.</li>
  * <li>Asks for input or asks for move generation to play Games.</li>
  * <li>Updates Game States (makes moves).</li>
  * <li>Plays Games.</li>
  * <li>Displays Games.</li>
  * <li>Saves Games.</li>
  * </ul> 
  * 
  * <h2>Implementation</h2>
  * <ul>
  * <li>See different functions for now. Implementation is not final yet, </li>
  * <li>so I'm waiting for something more stable before I feel comfortable talking about how this class is implemented.</li>
  * </ul>
  * 
  * 
  * @author Sacha Braud <sacha.beraud@gmail.com>
  *
  */
 
 
 public class Game {
 	
 	
 	/**
 	 * Players playing the Game
 	 */
 	public final Two <Player> players;
 	public Player myTurn;
 	
 	int size = 9;
 	//boolean undoFlag;
 	
 	
 	LinkedList<Wall> walls = new LinkedList<Wall>();
 	LinkedList<Move> moves = new LinkedList<Move>();
 	LinkedList<Move> redoMoves = new LinkedList<Move>();
 	
 	
     public Two <Player> players ()
     {
         return players;
     }
 	
     public Player myTurn(){    	
     	return myTurn;
     }
     
     public void changeTurn(){
     	myTurn = players.other(myTurn);
     }
     
 	/**
 	 * Constructor, creates the players based on names sent by factory.
 	 */
 	public Game(Two<Player> players){
 		this.players = players;
 	}
 	
 	
 	/**
 	 * Initializes the Game based on information from a load file or input on the command line.
 	 */
 	public void initGame(LinkedList<Move> moves) {
 		Point p1 = new Point('e' - 'a', 9);
 		Point p2 = new Point('e' - 'a', 1);
 		players._1.pawn = p1;
 		players._2.pawn = p2;
 		players._1.positions.add(p1);
 		players._2.positions.add(p2);
 		players._1.goal = 1;
 		players._2.goal = 9;
 		myTurn = players._1();
 		
 		if(moves != null){
 			for(Move move: moves){
 				move(move, myTurn);
 			}
 		}
 	}
 	
 	
 	/**
 	 * Checks if a Player's Pawn is at coordinates x,y.
 	 * @param x the letter coordinate on the board.
 	 * @param y the number coordinate on the board.
 	 * @return The Player whose Pawn is at (x,y).
 	 */
 	public Player pawnAt(int x, int y){
 		
 		if(players._1.pawn.x() == x && players._1.pawn.y() == y){
 			return players._1();
 		} else if(players._2.pawn.x() == x && players._2.pawn.y() == y) {
 			return players._2();
 		} else {
 			return null;
 		}
 	}
 	
 	
 	/**
 	 * Checks if a wall is at coordinates x,y and with direction dir.
 	 * @param x the letter coordinate on the board.
 	 * @param y the number coordinate on the board.
 	 * @param dir the direction of the wall.
 	 * @return True if a wall is at coordinates x,y and with direction dir, false otherwise.
 	 */
 	public boolean isWallAt(int x, int y, MoveType dir){
 		boolean returnValue = false;
 		
 		for(Wall myWall : walls){
 			
 			if(dir.equals(MoveType.HORIZONTAL)){
 				if(dir.equals(myWall.dir()) && (myWall.pos().x() == x || myWall.pos().x() == x - 1) && myWall.pos().y() == y)
 					returnValue = true;
 			} else if (dir.equals(MoveType.VERTICAL)){
 				if(dir.equals(myWall.dir()) && myWall.pos().x() == x && (myWall.pos().y() == y || myWall.pos().y() == y - 1))
 					returnValue = true;
 			}
 			
 		}	
 		return returnValue;
 	}
 	
 	
 
 	
 	
 	/**
 	 * Checks if a Game is new. not used for now.
 	 * @return True if this game is new, false otherwise.
 	 */
 	public boolean isNew(){
 		
 		return false;
 	}
 	
 	/**
 	 * Checks if a game is over, soon to be done.
 	 * @return True if a game is over, (if a player is in their goal space), false if not.
 	 */
 	public boolean isOver(){	
 		return (players._1().goalDistance() == 0 || players._2().goalDistance() == 0);
 	}
 	
 	/**
 	 * @return The winner of the Game.
 	 */
 	public Player winner(){
 		if(players._1().pawn().y() == 1){
 			return players._1();
 		} else {
 			return players._2();
 		}
 	}
 	
 	/**
 	 * @return The loser of the Game.
 	 */
 	public Player loser(){
 		if(players._1().pawn().y() == 1){
 			return players._2();
 		} else {
 			return players._1();
 		}
 	}
 
 	/**
 	 * Moves a pawn from a position to another, or place a wall.
 	 * @param move The move with coordinates a player is making.
 	 * @param p the player making the move.
 	 */
 	public void move(Move move, Player p) {
 		if(move.direction().equals(MoveType.PAWN)){
 			placePawn(move, p);
 			p.positions.add(move.coord());
 		} else{
 			placeWall(move);
 			p.deductWall();
 		}
 		changeTurn();
 		moves.add(move);
 	}
 
 	/**
 	 * Moves a pawn from a position to another.
 	 * @param move The move with coordinates a player is making.
 	 * @param p The player making the move.
 	 */
 	public void placePawn(Move move, Player p){
 		p.pawn = new Point(move.coord().x(), move.coord().y());
 	}
 	
 	
 	/**
 	 * Places a wall
 	 * @param move The coordinates and direction of the wall to be placed.
 	 */
 	public void placeWall(Move move) {
 		walls.add(new Wall(move.coord(), move.direction()));
 	}
 	
 	
 	
 	public boolean undo(){
 		if(moves.size() > 0){
 			if(players.other(myTurn()).type().equals("AI")){
 				changeTurn();
 				if(moves.getLast().direction().equals(MoveType.PAWN)){
 					myTurn().positions.removeLast();
 					myTurn().pawn = myTurn().positions.getLast();
 					redoMoves.add(moves.removeLast());
 				} else {
 					walls.removeLast();
 					redoMoves.add(moves.removeLast());
 				}
 				changeTurn();
 				if(moves.getLast().direction().equals(MoveType.PAWN)){
 					myTurn().positions.removeLast();
 					myTurn().pawn = myTurn().positions.getLast();
 					redoMoves.add(moves.removeLast());
 				} else {
 					walls.removeLast();
 					redoMoves.add(moves.removeLast());
 				}
 			} else {
 				changeTurn();
 				if(moves.getLast().direction().equals(MoveType.PAWN)){
 					myTurn().positions.removeLast();
 					myTurn().pawn = myTurn().positions.getLast();
 					redoMoves.add(moves.removeLast());
 				} else {
 					walls.removeLast();
 					redoMoves.add(moves.removeLast());
 				}
 			}
 			display();
 			System.out.println("Make a move "+ myTurn().name +": ");
 			return true;
 		} else {
 			System.out.println("You need to make a move before undoing it.");
 			return false;
 		}
 		
 	}
 	
 	public boolean redo(){
 		if(redoMoves.size() > 0){
 			if(players.other(myTurn()).type().equals("AI")){
 				move(redoMoves.removeLast(), myTurn());
 				move(redoMoves.removeLast(), myTurn());
 				display();
 				System.out.println("Make a move "+ myTurn().name +": ");
 				return true;
 				
 			}else {
 				move(redoMoves.removeLast(), myTurn());
 				display();
 				System.out.println("Make a move "+ myTurn().name +": ");
 				return true;
 			}
 		} else {
 			System.out.println("You need to undo a move before redoing it.");
 			return false;
 		}
 		
 	}
 	
 
 	/**
 	 * Plays the Game. With a !isOver() loop. keeps asking for prompts for now and displaying.
 	 * Soon will be AI and stuff.
 	 */
 	public void play() {
 		
 		display();
 		System.out.println("Make a move "+ myTurn().name +": ");
 		while(!isOver()){
 			Move move;
 			if(myTurn().type().equals("AI")){
 				AI ai = new AI(this);
 				move = ai.createMove();
 			} else {
 				move = getInput();
 				if(move == null)
 					return;
 			}
 
 			if(isValid(move, myTurn())){
 				move(move, myTurn());
 				redoMoves = new LinkedList<Move>();
 				display();
 				System.out.println("Make a move "+ myTurn().name +": ");
 			} else {
 				System.out.println("Invalid move noob, try again: ");
 			}
 
 		}
 		
 		System.out.println("GG. Winner is " + winner().name + ".");
 	}
 	
 	
 	/**
 	 * Saves a Game. right now it just prints all the moves that have been done.
 	 */
 	public void save(String fileName) {
 		System.out.println("Saving a game...");
 		String line = "";
 		DataOutputStream dos;
 		
 		for(Move myMove: moves){
 			line +=  myMove.toString() + " ";
 		}
 
 		try {
 			File outFile = new File(fileName);
 			dos = new DataOutputStream(new FileOutputStream(outFile));
 			dos.writeBytes(line);
 			dos.close();
 		} catch (FileNotFoundException ex) {
 			System.out.println("Impossible to create file "+fileName);
 			return;
 		} catch (IOException ex) {
 			System.out.println("IO Exception");
 			return;
 		}
 
 		System.out.println(line+" has been saved in " + fileName); // to change to print to file and it's GG, I think
 	}
 
 
 	/**
 	 * Used to get the users moves and commands.
 	 * @return String that the user input on the command line.
 	 */
 	public Move getInput(){
 		Scanner input = new Scanner (System.in);	
 		String line = input.nextLine ().toLowerCase ();
 		Command command = new Command(line);
 		
 		while(command.type().equals(CommandType.INVALID)){
 			line = input.nextLine ().toLowerCase ();
 			command = new Command(line);
 		}
 		if(command.type().equals(CommandType.NEW_GAME)){
 			if(YesNoPrompt(command.type())){
 				Two<Player> newPlayers = GameFactory.getPlayers();
 				GameFactory.newGame(newPlayers);
 			}
 			return null;
 			
 		} else if(command.type().equals(CommandType.LOAD_GAME)){
 			if(YesNoPrompt(command.type())){
 				Two<Player> newPlayers = GameFactory.getPlayers();
 				GameFactory.loadGame(command.fileName(),newPlayers);
 			}
 			return null;
 			
 		} else if(command.type().equals(CommandType.NEW_WITH_MOVES)){
 			if(YesNoPrompt(command.type())){
 				Two<Player> newPlayers = GameFactory.getPlayers();
 				GameFactory.newGameWithMoves(command.moves(), newPlayers);
 			}
 			return null;
 			
 		} else if(command.type().equals(CommandType.MOVE)){
 			return command.move();
 		} else if(command.type().equals(CommandType.SAVE_GAME)){
 			save(command.fileName());
 		} else if(command.type().equals(CommandType.UNDO)){
 			undo();
 		} else if(command.type().equals(CommandType.REDO)){
 			redo();
 		}
 		
 		
 		return getInput();
 	}
 	
 	public boolean YesNoPrompt(CommandType type){
 		Scanner input = new Scanner (System.in);	
 		
 		if(type.equals(CommandType.NEW_GAME))
 			System.out.println("Are you sure you want to create a new game? All unsaved data will be lost.");
 		else if(type.equals(CommandType.NEW_WITH_MOVES))
 			System.out.println("Are you sure you want to create a new game with move initialization? All unsaved data will be lost.");
 		else if(type.equals(CommandType.LOAD_GAME))
 			System.out.println("Are you sure you want to load a game? All unsaved data will be lost.");
 		String line = input.nextLine ().toLowerCase ();
 		if(line.charAt(0) == 'y'){
 			return true;
 		}
 		
 		return false;
 	}
 	
 	
 	
 	/**
 	 * Displays the game (the board with the pawns and walls).
 	 */
 	public void display() {
 		LinkedList<String> strings = new LinkedList<String>();
 		String line;
 		String temp;
 		int i,j,k;
 		
 		//letters index
 		temp = "   ";
 		line = temp + "  a" + temp +"b" + temp +"c" +temp +"d" +temp + "e" +temp +"f" +temp +"g" +temp +"h" +temp +"i";
 		strings.add(line);
 		
 		for(i=0;i<10;i++){
 
 			line = "";
 			
 			for(j=0;j<10;j++){
 				if( ((i == 0) || (i == 9) || (isWallAt(j-1,i+1,MoveType.HORIZONTAL))) && j !=0  ) 
 					
 					temp = "---"; 
 				else
 					temp = "   ";
 				line += temp + "*";
 				
 			}
 			strings.add(line);
 			if(i < 9){
 				line = (i+1)+ "  |";  //print number index
 				for(k=0;k<9;k++){
 					Player p = pawnAt(k,i+1);
 					if( p != null) //check if pawn at (k,i)
 						if(p == players._1()) // check if player 1
 							temp = " X ";
 						else  // else player 2
 							temp = " O ";
 					else
 						temp = "   ";
 					
 					line +=temp;
 					if(isWallAt(k+1,i+1,MoveType.VERTICAL) || k == 8) //check if vertical wall
 						line += "|";
 					else
 						line += " ";
 				}
 				strings.add(line);
 			}
 		}
 		
 		//Actually prints everything
 		for(String myString : strings) {
 			System.out.println(myString);
 		}	
 	}
 	
 	
 	
 	/**
 	 * Check the validity of a move for a particular game state.
 	 * @param move the move to be checked for validity
 	 * @return validity of this move.
 	 */
 	public boolean isValid (Move move, Player p){
 		
		return (isValidJump(move, p) && isInBoard(move))
				|| (isValidWallPlace(move, p) && isInBoard(move))
				|| (isAdjacent(move, p) && isNotBlocked(move,p) && !samePlace(move, p) && isInBoard(move));
 	}
 
 	
 	/**
 	 * Checks if a wall placement is valid or not.
 	 * @param move A move (wall placement) to be checked for validity.
 	 * @return True if this wall placement is valid according to Game state, False if not.
 	 */
 	public boolean isValidWallPlace(Move move, Player p){
 		if(p.wallsLeft() >= 0 && !move.direction().equals(MoveType.PAWN)
 				&& !isCrossing(new Wall(move.coord(), move.direction()))){
 			if (isValidPath(new Wall(move.coord(), move.direction()))) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks if a pawn move is a valid jump move.
 	 * @param move A move to be checked for validity.
 	 * @return True if this move is a valid jump, False if not.
 	 */
 	public boolean isValidJump(Move move, Player p){
 		Move temp = new Move(players().other(p).pawn().x(), players().other(p).pawn().y(), MoveType.PAWN);
 		return isAdjacent(temp, p) && isAdjacent(move, players.other(p)) && isNotBlocked(move, players.other(p));
 	}
 	
 	
 	
 	/**
 	 * Checks if a Pawn move is adjacent to its previous position.
 	 * @param move A move to be checked for validity.
 	 * @return True if this move is adjacent to pawn's previous position, False if not.
 	 */
 	public boolean isAdjacent(Move move, Player p){
 		boolean isAdjLetter = Math.abs(move.coord().x() - p.pawn().x()) == 1;
 		boolean isSameLetter = move.coord().x() == p.pawn().x();
 		boolean isAdjNumber = Math.abs(move.coord().y() - p.pawn().y()) == 1;
 		boolean isSameNumber = move.coord().y() == p.pawn().y();
 		
 		return  (isAdjLetter && isSameNumber) || (isSameLetter && isAdjNumber) ;
 	}
 	
 	
 	/**
 	 * Checks whether a pawn can't make a move because there is a wall in between previous and wanted position.
 	 * It assumes the move is adjacent because it is used in combination ("&&") with isAdjacent anyway.
 	 * @param move the move to be made
 	 * @param p the player making the move
 	 * @return true if path is clear, false if there is a wall
 	 */
 	public boolean isNotBlocked(Move move, Player p) {
 		if(move.coord().x() - p.pawn().x() == 1 && isWallAt(move.coord().x(), move.coord().y(), MoveType.VERTICAL)){
 			return false;
 		} else if (move.coord().x() - p.pawn().x() == -1 && isWallAt(p.pawn().x(), move.coord().y(), MoveType.VERTICAL)) {
 			return false;
 		} else if (move.coord().y() - p.pawn().y() == 1 && isWallAt(move.coord().x(), move.coord().y(), MoveType.HORIZONTAL)) {
 			return false;
 		} else if (move.coord().y() - p.pawn().y() == -1 && isWallAt(move.coord().x(), p.pawn().y(), MoveType.HORIZONTAL)) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	public boolean isInBoard(Move m) {
 		if(m.direction() == MoveType.HORIZONTAL){
 			if(m.coord().x() >= 0 && m.coord().x() <=7 && m.coord().y() > 1 && m.coord().y() <= 9)
 				return true;
 			else
 				return false;
 			
 		} else if (m.direction() == MoveType.VERTICAL){
 			if(m.coord().x() > 0 && m.coord().x() <=8 && m.coord().y() >= 1 && m.coord().y() <= 8)
 				return true;
 			else
 				return false;
 			
 		} else {
 			if(m.coord().x() >= 0 && m.coord().x() <= 8 && m.coord().y() >= 1 && m.coord().y() <= 9)
 				return true;
 			else
 				return false;	
 		}
 	}
 	
 	
 	
 	public boolean isCrossing(Wall w){
 
 		if(w.dir().equals(MoveType.VERTICAL)){
 			if( (isWallAt(w.pos().x()-1, w.pos().y()+1,MoveType.HORIZONTAL)
 					&& isWallAt(w.pos().x(), w.pos().y()+1,MoveType.HORIZONTAL))
 					|| (isWallAt(w.pos().x(), w.pos().y(),MoveType.VERTICAL)
 						|| 	isWallAt(w.pos().x(), w.pos().y()+1,MoveType.VERTICAL))){
 						return true;
 					} else {
 						return false;
 					}
 		} else if (w.dir().equals(MoveType.HORIZONTAL)){
 			if( (isWallAt(w.pos().x()+1, w.pos().y()-1,MoveType.VERTICAL)
 					&& isWallAt(w.pos().x()+1, w.pos().y(),MoveType.VERTICAL))
 					|| (isWallAt(w.pos().x(), w.pos().y(),MoveType.HORIZONTAL)
 							|| isWallAt(w.pos().x()+1, w.pos().y(),MoveType.HORIZONTAL))){
 						return true;
 					} else {
 						return false;
 					}
 		}
 		return false;
 	}
 	
 	public boolean samePlace(Move m, Player p){
 		
 		if(m.coord().equals(p.pawn()))
 			return true;
 		else
 			return false;
 				
 	}
 	
 	
 	public boolean isValidPath(Wall w){
 		boolean valid;
 		walls.add(w);
 		if ((shortestPath(players._1) != null) && (shortestPath(players._2) != null))
 			valid = true;
 		else
 			valid = false;
 		walls.remove(w);
 		return valid;
 	}
 	
 	public LinkedList <Move> shortestPath (Player player) {
 		Comparator <WeightedMove> comparator = new WeightedMoveComparator();
 		PriorityQueue <WeightedMove> open = new PriorityQueue <WeightedMove> (1024, comparator);
 		LinkedList <WeightedMove> closed = new LinkedList <WeightedMove> ();
 		WeightedMove current = null;
 		WeightedMove next = null;
 		Player tempPlayer = new Human("");//TODO add a constructor for player, or change isnotblocked to take two points or something
 		tempPlayer.goal = player.goal;
 		Point tempPawn;
 		int gcost = 0;
 		
 		WeightedMove initial = new WeightedMove(player.pawn().x(), player.pawn().y(), MoveType.PAWN, gcost, player, null);
 		open.add(initial);
 		while(open.peek() != null) {
 			current = open.poll();
 			if (current.weight() == Integer.MAX_VALUE) {	//TODO avoid early exit with peek and check of weight
 				return null;
 			}
 			gcost = current.gcost() + 1;
 			tempPawn = new Point(current.coord().x(), current.coord().y());
 			tempPlayer.pawn = tempPawn;
 			if (tempPlayer.goalDistance() != 0) {
 				next = new WeightedMove(current.coord().x() + 1, current.coord().y(), MoveType.PAWN, gcost, player, current);
 				if (!isNotBlocked(next, tempPlayer) || next.coord().x() >= 9) {
 					next = new WeightedMove(current.coord().x() + 1, current.coord().y(), MoveType.PAWN, Integer.MAX_VALUE, player, current);
 				}
 				if (!(open.contains(next) || closed.contains(next)))
 					open.offer(next);
 				
 				next = new WeightedMove(current.coord().x(), current.coord().y() + 1, MoveType.PAWN, gcost, player, current);
 				if (!isNotBlocked(next, tempPlayer) || next.coord().y() > 9) {
 					next = new WeightedMove(current.coord().x(), current.coord().y() + 1, MoveType.PAWN, Integer.MAX_VALUE, player, current);
 				}
 				if (!(open.contains(next) || closed.contains(next)))
 					open.offer(next);
 				
 				next = new WeightedMove(current.coord().x() - 1, current.coord().y(), MoveType.PAWN, gcost, player, current);
 				if (!isNotBlocked(next, tempPlayer) || next.coord().x() <= 1) {
 					next = new WeightedMove(current.coord().x() - 1, current.coord().y(), MoveType.PAWN, Integer.MAX_VALUE, player, current);
 				}
 				if (!(open.contains(next) || closed.contains(next)))
 					open.offer(next);
 				
 				next = new WeightedMove(current.coord().x(), current.coord().y() - 1, MoveType.PAWN, gcost, player, current);
 				if (!isNotBlocked(next, tempPlayer) || next.coord().y() < 1) {
 					next = new WeightedMove(current.coord().x(), current.coord().y() - 1, MoveType.PAWN, Integer.MAX_VALUE, player, current);
 				}
 				if (!(open.contains(next) || closed.contains(next)))
 					open.offer(next);
 			} else {
 				open = new PriorityQueue <WeightedMove> (1, comparator);
 			}
 			closed.add(current);
 		}
 		LinkedList <Move> result = new LinkedList <Move> ();
 		result.add(current);
 		while (current.parent() != null) {
 			current = current.parent();
 			result.addFirst(current);
 		}
 		return result;
 		
 	}
 	
 }
