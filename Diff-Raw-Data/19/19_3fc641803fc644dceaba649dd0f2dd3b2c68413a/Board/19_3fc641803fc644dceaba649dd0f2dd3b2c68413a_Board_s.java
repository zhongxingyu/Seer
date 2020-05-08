 package csci306;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 
 import javax.swing.JPanel;
 
 
 public class Board extends JPanel {
 	public static final int CELL_SIZE = 25;
 	
 	private List<BoardCell> cells ;
 	public Map<Character,String> rooms;
 	public int numRows;
 	public int numColumns;
 	public List<Player> players;
 	public List<Card> allCards;
 	public Solution solution;
 	Set<Integer> targets;
 	
 	public Board(){
 		cells = new ArrayList<BoardCell>();
 		rooms = new HashMap<Character,String>();
 		players = new ArrayList<Player>();
 		allCards = new ArrayList<Card>();
 		
 		loadConfigFiles();
 	}
 	
 	public void loadPlayers() {
 		FileReader frHPlayers;
 		FileReader frCPlayers;
 		Scanner scn;
 		String line="";
 		String [] data;
 		Player cPlayer;
 		Player hPlayer;
 		
 		// Adding the human player
 		try {
 			frHPlayers = new FileReader("Human.txt");
 			scn = new Scanner(frHPlayers);
 			line = scn.nextLine();
 			data = line.split(",");
 
 			hPlayer = new HumanPlayer();
 			// -- HumanPlayer's details
 			hPlayer.setName(data[0]);
 			hPlayer.setColor(convertColor(data[1]));
 			hPlayer.setLocation(getCellAt(calcIndex(Integer.parseInt(data[2]), Integer.parseInt(data[3]))));
 			// -- Adding him/her to the list
 			players.add(hPlayer);
 			frHPlayers.close();
 		} catch(FileNotFoundException e){
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// Adding the computer players 
 		try {
 			frCPlayers = new FileReader("People.txt");
 			scn = new Scanner(frCPlayers);
 			while (scn.hasNextLine()) {
 				line = scn.nextLine();
 				data = line.split(",");
 				cPlayer = new ComputerPlayer();
 				// -- ComputerPlayers' details
 				cPlayer.setName(data[0]);
 				cPlayer.setName(data[0]);
 				cPlayer.setColor(convertColor(data[1]));
 				cPlayer.setLocation(getCellAt(calcIndex(Integer.parseInt(data[2]), Integer.parseInt(data[3]))));
 				// -- Adding it to the list
 				players.add(cPlayer);
 				frCPlayers.close();
 			}
 		} catch(FileNotFoundException e){
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void loadConfigFiles(){
 			//insert try cat
 		FileReader readBoard = null;
 		FileReader readRooms = null;
 
 
 		try {
 			readBoard = new FileReader("CR-ClueLayout.csv");
 			readRooms = new FileReader("ClueRms");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		//read each room into rooms map
 		Scanner in = new Scanner(readRooms);
 		while(in.hasNextLine()){
 			String[] room = in.nextLine().split(", ");
 			rooms.put( room[0].charAt(0), room[1]);
 
 		}
 		
 		
 		
 		//read each cell and input
 
 		in = new Scanner(readBoard);
 		int rowIndex = 0;
 		int colIndex = 0;
 		for(;in.hasNextLine();rowIndex++){
 			colIndex = 0;
 			String[] row = in.nextLine().split(",");
 			for(;colIndex<row.length;colIndex++){
 				if(!row[colIndex].equals("W")){
 					RoomCell roomCell = new RoomCell(rowIndex,colIndex,row[colIndex]);
 					roomCell.setName(rooms.get(row[0].charAt(0)));
 					cells.add(roomCell);
 				}else{
 					cells.add(new WalkwayCell(rowIndex,colIndex));
 				}
 				
 			}
 		}
 		numRows = rowIndex;
 		numColumns = colIndex;
 	}
 	
 	public void loadCards(){
 		FileReader frCards;
 		Scanner scn;
 		String line;
 		String [] data;
 		Card card;
 		try {
 			frCards = new FileReader("Cards.txt");
 			scn = new Scanner(frCards);
 			while (scn.hasNextLine()) {
 				line = scn.nextLine();
 				data = line.split(",");
 				card = new Card(data[1],data[0]);
 				allCards.add(card);	
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public int calcIndex(int row, int col){
 		return row*numColumns+col;
 	}
 	
 	//checks if it is a room
 	public RoomCell getRoomCellAt(int row, int col){
 		
 		BoardCell c =  cells.get(calcIndex(row,col));
 		if(c.isRoom()){
 			return (RoomCell) c;	
 		}else{
 			return null;
 		}
 		
 	}
 	
 	public String getRoomName(char c){
 		return rooms.get(c);
 	}
 	
 	public RoomCell.DoorDirection doorDirectionAtIndex(int row, int col){
 		RoomCell rc = getRoomCellAt(row, col);
 		if(rc != null){
 			return rc.getDoorDirection();
 		}else{
 			return RoomCell.DoorDirection.NONE;
 		}
 		
 	}
 
 	public char roomInitialAtIndex(int row, int col){
 		RoomCell rc = getRoomCellAt(row, col);
 		if(rc != null){
 			return rc.getInitial();
 		}else{
 			return 'W';
 		}
 	}
 
 	public Map<Character, String> getRooms() {
 		return rooms;
 	}
 
 	public int getNumRows() {
 		return numRows;
 		
 	}
 
 	public int getNumColumns() {
 		return numColumns;
 	}
 
 	public BoardCell getCellAt(int calcIndex) {
 		return(cells.get(calcIndex));
 	}
 
 	public LinkedList<Integer> getAdjList(int calcIndex) {
 
 		LinkedList<Integer> newAdjMtx = new LinkedList<Integer>();
 		//check if in nondoorway room cell
 		if(cells.get(calcIndex).isRoom()   &&  !cells.get(calcIndex).isDoorway()){
 			return newAdjMtx;
 		}
 		boolean sourceIsRoom = getCellAt(calcIndex).isRoom();
 		//if not at top add one above
 		if(calcIndex >= numColumns){
 			int i = calcIndex-numColumns;
 			//check if attempting to move into nondoorway room cell
 			if((cells.get(i).isWalkway()   ||  cells.get(i).isDoorway())){
 				//if in a room cant move to room
 				if(!(getCellAt(i).isRoom() && sourceIsRoom)){
 					//if not in doorway and moving in the same direction as the door
 					if(!(cells.get(i).isDoorway() &&   ((RoomCell)(cells.get(i))).getDoorDirection() != RoomCell.DoorDirection.DOWN ))	
 						if(!(cells.get(calcIndex).isDoorway() &&   ((RoomCell)(cells.get(calcIndex))).getDoorDirection() != RoomCell.DoorDirection.UP ))
 							newAdjMtx.push(i);
 
 				}
 			}
 		}
 	
 		//if not at left side add one left
 		if(calcIndex%numColumns != 0){
 			int i = calcIndex-1;
 			//check if attempting to move into nondoorway room cell
 			if((cells.get(i).isWalkway()   ||  cells.get(i).isDoorway())){
 				if(!(getCellAt(i).isRoom() && sourceIsRoom)){
 					if(!(cells.get(i).isDoorway() &&   ((RoomCell)(cells.get(i))).getDoorDirection() != RoomCell.DoorDirection.RIGHT ))	
 						if(!(cells.get(calcIndex).isDoorway() &&   ((RoomCell)(cells.get(calcIndex))).getDoorDirection() != RoomCell.DoorDirection.LEFT ))	
 							newAdjMtx.push(i);
 				}
 			}
 		}
 			
 
 		//if not at right side add one to right
 		if(calcIndex%numColumns != numColumns-1){
 			int i = calcIndex+1;			
 			//check if attempting to move into nondoorway room cell
 			if((cells.get(i).isWalkway()   ||  cells.get(i).isDoorway())){
 				if(!(getCellAt(i).isRoom() && sourceIsRoom)){
 					if(!(cells.get(i).isDoorway() &&   ((RoomCell)(cells.get(i))).getDoorDirection() != RoomCell.DoorDirection.LEFT ))
 						if(!(cells.get(calcIndex).isDoorway() &&   ((RoomCell)(cells.get(calcIndex))).getDoorDirection() != RoomCell.DoorDirection.RIGHT ))
 							newAdjMtx.push(i);
 				}
 			}
 		}
 
 		//if not at bottom add one to bottom
 		if(calcIndex<numRows*numColumns-numColumns){
 			int i = calcIndex+numColumns;
 			//check if attempting to move into nondoorway room cell
 			if((cells.get(i).isWalkway()   ||  cells.get(i).isDoorway())){
 				if(!(getCellAt(i).isRoom() && sourceIsRoom)){
 					if(!(cells.get(i).isDoorway() &&   ((RoomCell)(cells.get(i))).getDoorDirection() != RoomCell.DoorDirection.UP))
 						if(!(cells.get(calcIndex).isDoorway() &&   ((RoomCell)(cells.get(calcIndex))).getDoorDirection() != RoomCell.DoorDirection.DOWN ))
 							newAdjMtx.push(i);
 				}
 			}
 		}
 		return newAdjMtx;
 
 	}
 
 	public void printBoard(){
 		for(int row=0;row<numRows;row++){
 			for(int cols=0;cols<numColumns;cols++){
 				System.out.print( cells.get(calcIndex(row, cols)).toString());
 				System.out.print(" ");
 			}
 			System.out.print("\n");
 		}
 		
 		System.out.print("\n");
 		System.out.print("\n");
 		System.out.print("\n");
 		System.out.print("\n");
 		System.out.print("\n");
 	}
 	
 	public void calcTargets(int startLoc, int numSteps) {
 		targets = new HashSet<Integer>();
 		calcTargets(startLoc,numSteps,new HashSet<Integer>());
 	}
 	
 	public void calcTargets(int startLoc, int numSteps, Set<Integer> seen) {
 		//if bad spot return
 		if(cells.get(startLoc).isRoom() && !cells.get(startLoc).isDoorway()){
 			return;
 		}
 		///1 0
 		//if original place then return and don't add target
 		if(seen.contains(startLoc))
 			return;
 		seen.add(startLoc);
 		if(numSteps == 0 || (cells.get(startLoc).isDoorway() && seen.size()>1)){
 			targets.add(startLoc);
 			return;
 		}
 
 		//if no more steps add target
 
 		//call calcTargets for every adjacent pair
 		LinkedList<Integer> raa = getAdjList(startLoc);
 		for (Integer adj : raa){
 			calcTargets(adj,numSteps-1,new HashSet<Integer>(seen));
 		}
 
 	}
 	
 	public Set<BoardCell> getTargets() {
 		Set<BoardCell> tempTargets = new HashSet<BoardCell>();
 		
 		for(Integer z : targets){
 			tempTargets.add(getCellAt(z));
 		}
 		
 		//set everything to not visited
 		
 		//create a new set of targets
 		//update that there is no starting location for a new dice roll
 		return tempTargets;
 	}
 
 	public boolean checkAccusation(String person, String weapon, String room){
 		if (person.equals(solution.getPerson()) && 
 				weapon.equals(solution.getWeapon()) && 
 				room.equals(solution.getRoom())) {
 			return true;
 		}
 		return false;
 	}
 
 	public void handleSuggestion(String person, String weapon, String room){
 		Collections.shuffle(players);
 		
 		for(Player p: players){
 			if(p.disproveSuggestion(person, weapon, room) != null){
 				for(Player p2: players){
 					List<Card> seen = new ArrayList<Card>(p2.getSeenCards());
 					seen.add(p.disproveSuggestion(person, weapon, room));
 					p2.setSeenCards(seen);
 				}
 			}
 		}
 	}
 	
 	public void deal(){
 		Collections.shuffle(allCards);
 		for (int i = 0; i < allCards.size(); i++) {
 			players.get(i%players.size()).getCards().add(allCards.get(i));
 		}
 	}
 	
 	public void deal(ArrayList<Card> cards){
 		Collections.shuffle(cards);
 		for (int i = 0; i < cards.size(); i++) {
 			players.get(i%players.size()).getCards().add(cards.get(i));
 		}
 	}
 	
 	public void selectAnswer(){
 		boolean hasPerson = false, hasWeapon = false, hasRoom = false;
 		Solution sol = new Solution();
 		
 		Collections.shuffle(allCards);
 		
 		for (Card c: allCards) {
 			if (!hasPerson && c.getType().equals("PERSON")) {
 				sol.setPerson(c.getName());
 				hasPerson = true;
 			}
 			else if (!hasWeapon && c.getType().equals("WEAPON")) {
 				sol.setWeapon(c.getName());
 				hasWeapon = true;
 			}
 			else if (!hasRoom && c.getType().equals("ROOM")) {
 				sol.setRoom(c.getName());
 				hasRoom = true;
 			}
 		}
 		
 		solution = sol;
 	}
 	
 	public Solution getAnswer(){
 		return solution;
 	}
 	
 	public Color convertColor(String strColor) {
 		Color color; 
 		try {     
 			// We can use reflection to convert the string to a color
 			Field field = Class.forName("java.awt.Color").getField(strColor.trim());     
 			color = (Color)field.get(null); } 
 		catch (Exception e) {  
 			color = null; // Not defined } 
 		}
 		return color;
 	}
 
 	
 	/*
 	 * DRAWING METHODS
 	 */
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		g.setColor(Color.GRAY);
 		g.fillRect(0, 0, numColumns * CELL_SIZE, numRows * CELL_SIZE);
 		for (int i = 0; i < numRows; ++i) {
 			for (int j = 0; j < numColumns; ++j) {
 				int index = calcIndex(i, j);
 				int x = j * CELL_SIZE;
 				int y = i * CELL_SIZE;
 				
 				BoardCell cell = getCellAt(index);
 				if (cell.isRoom()) {
 					RoomCell roomCell = (RoomCell) cell;
 					cell.draw(g);
					/*
					if (roomCell.displayTheName()) {
						g.setColor(Color.BLUE);
						//prepare string
						char initial = roomCell.getInitial();
						String roomName = getRoomName(initial);
						g.drawString(roomName, x, y);
					}
					*/
 				} else if (cell.isWalkway()) {
 					cell.draw(g);
 				}	
 				
 			}
 		}
 		
 		for (Player p : players) {
 			p.draw(g);
 		}
 	}
 
 }
