 package clueGame;
 
 import java.io.*;
 import java.util.*;
 
 
 public class Board {
 
 	//variables
 	private ArrayList<BoardCell> cells = new ArrayList<BoardCell>();
 	private Map<Character, String> rooms = new HashMap<Character, String>();
 	private int numRows, numColumns;
 
 	//Board Info
 //	private final int numRows = 25;
 //	private final int numColumns = 25;
 	private LinkedList<BoardCell> adjacencies;
 	private ArrayList<LinkedList<BoardCell>> listOfAdjacencies;
 	
 	//Scanner and File IO
 	private Scanner scan;
 	private FileReader fileIn;
 	
 
 	boolean [] visited = new boolean[numRows*numColumns];
 	Set targets = new HashSet<Integer>();
 
 
 	//Constructors	
 	public Board(){
 		//will use a known file that works for the legend and the layout
 	}
 
 	public Board(String layoutFile, String legendFile) throws FileNotFoundException, BadConfigFormatException {
 		loadConfigFiles(layoutFile, legendFile);
 	}
 
 
 	//methods
 	public void calcAdjacencies(){
 		for(int i = 0; i < numRows; ++i) {
 			for (int j = 0; j < numColumns; ++j) {
 				adjacencies = new LinkedList<BoardCell>();
				int index = calcIndex();
 				if (i-1 >= 0) {
 					int xminus1 = calcIndex(i-1, j);
 					adjacencies.add(xminus1);
 				}
 				if (j-1 >= 0) {
 					int yminus1 = calcIndex(i, j-1);
 					adjacencies.add(yminus1);
 				}
 				if (i+1 < numRows) {
 					int xplus1 = calcIndex(i+1, j);
 					adjacencies.add(xplus1);
 				}
 				if (j+1 < numColumns) {
 					int yplus1 = calcIndex(i, j+1);
 					adjacencies.add(yplus1);
 				}
 				listOfAdjacencies.add(adjacencies);
 			}
 		}
 	}
 
 	public LinkedList<BoardCell> getAdjList(int index){
 		listOfAdjacencies = new ArrayList<LinkedList<BoardCell>>();
 		calcAdjacencies();
 		return listOfAdjacencies.get(index);
 	}
 
 	public void setVisitedTrue(int index) {
 		visited[index] = true;
 	}
 
 	public void setVisitedFalse(int index) {
 		visited[index] = false;
 	}
 
 	public void startTargets(int index, int steps){
 		Arrays.fill(visited, false);
 		targets = new HashSet<BoardCell>();
 		visited[index] = true;
 		calcTargets(index, steps);
 	}
 
 	public void calcTargets(int thisCell, int steps){	
 		ArrayList<LinkedList<BoardCell>> tempAdjacencies = listOfAdjacencies;
 		LinkedList<BoardCell> adjacentCells = tempAdjacencies.get(thisCell);
 		LinkedList<BoardCell> adjacentCellsTemp = new LinkedList<BoardCell>();
 
		int j = 0;
 		for(BoardCell i: adjacentCells){
			if(!visited[j]){
 				adjacentCellsTemp.add(i);
 			} 
			++j;
 		}
		j = 0;
 		for (BoardCell i: adjacentCellsTemp) {
			setVisitedTrue(j);
 			if (steps == 1) {
 				targets.add(i);
 			} else {
				calcTargets(j, steps - 1);
 			}
 
			setVisitedFalse(j);
			++j;
 		}
 	}
 
 	public Set getTargets(){
 		return targets;
 	}
 
 
 	public int calcIndex(int row, int col){
 		if(row > numRows || row < 0 || col > numColumns || col < 0){
 			System.out.println("The row or column is out of bounds.");
 		}
 
 		return row*numColumns + col;
 	}
 
 	public void loadConfigFiles(String layoutFile, String legendFile) throws FileNotFoundException, BadConfigFormatException {
 		loadRoomConfigFiles(legendFile);
 		loadBoardConfigFiles(layoutFile);
 	}
 
 	public void loadRoomConfigFiles(String legendFile) throws BadConfigFormatException, FileNotFoundException{
 		fileIn = new FileReader(legendFile);
 		scan = new Scanner(fileIn);
 		while(scan.hasNextLine()){
 			String line = scan.nextLine();
 			String[] toSplit = line.split(", ");
 			if (toSplit.length > 2 || toSplit.length < 0) throw new BadConfigFormatException(legendFile);
 			else {
 				char c = toSplit[0].charAt(0);
 				rooms.put(c, toSplit[1]);
 			}
 		}
 	}
 
 	public void loadBoardConfigFiles(String layoutFile) throws BadConfigFormatException {
 		try {
 			fileIn = new FileReader(layoutFile);
 			scan = new Scanner(fileIn);
 			int rowCount = 0;
 			int columnCount = 0;
 			int badColumnCheck = 0;
 			boolean firstrun = true;
 			 while(scan.hasNextLine()) {
 				 String line = scan.nextLine();
 				 String[] toSplit = line.split(",");
 				 if (firstrun == true) {
 					 badColumnCheck = toSplit.length;
 				 }
 				 firstrun = false;
 				 if (toSplit.length != badColumnCheck) {
 						throw new BadConfigFormatException(layoutFile);
 				} else badColumnCheck = toSplit.length;
 				for (int i = 0; i < toSplit.length; ++i) {
 					if (toSplit[i].equals("W")) {
 						WalkwayCell w = new WalkwayCell();
 						w.isWalkway = true;
 						w.row = rowCount;
 						w.col = i;
 						cells.add(w);
 					} else {
 						RoomCell r = new RoomCell();
 						r.roomInitial = toSplit[i].charAt(0);
 						if (!rooms.containsKey(r.roomInitial)) {
 							throw new BadConfigFormatException(layoutFile);
 						}
 						r.isRoom = true;
 						r.row = rowCount;
 						r.col = i;
 						if(toSplit[i].length() >  1){
 							determineAndSetDoorwayDirection(toSplit[i].charAt(1), r);
 							r.isDoor = true;
 						} else{
 							r.setDoorDirection(RoomCell.DoorDirection.NONE);
 						}
 						cells.add(r);
 					}
 				} 
 				rowCount++;
 				columnCount++;
 //				line = scan.nextLine();
 //				toSplit = line.split(",");
 				
 			}
 			this.numColumns = columnCount;
 			this.numRows = columnCount;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	//helper function for loadBoardConfigFiles function
 	public void determineAndSetDoorwayDirection(char aLetter, RoomCell aCell){
 		switch(aLetter){
 			case 'R': aCell.setDoorDirection(RoomCell.DoorDirection.RIGHT);
 					  break;
 			case 'L': aCell.setDoorDirection(RoomCell.DoorDirection.LEFT);
 					  break;
 			case 'D': aCell.setDoorDirection(RoomCell.DoorDirection.DOWN);
 					  break;
 			case 'U': aCell.setDoorDirection(RoomCell.DoorDirection.UP);
 			          break;
 		}	
 		
 	}
 	
 	
 	//getters 
 	public RoomCell getRoomCellAt(int row, int col) {
 		int index = calcIndex(row,col);
 		RoomCell returnRoom = new RoomCell();
 		if (cells.get(index).isRoom() == true) {
 			returnRoom = (RoomCell)cells.get(index);
 		}
 		return returnRoom;
 	}
 
 	public BoardCell getCells(int index) {
 		return cells.get(index);
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
 
 
 
 }
