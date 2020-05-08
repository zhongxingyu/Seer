 package main.board;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.TreeSet;
 
 import main.board.RoomCell.DoorDirection;
 
 public class Board {
 
 	ArrayList<BoardCell> cells;
 	Map<Character, String> rooms;
 	HashMap<BoardCell, List<Integer>> adj = new HashMap<BoardCell, List<Integer>>();
 	LinkedList<Integer> adjList;
 	TreeSet<BoardCell> targets;
 	boolean visited[][];
 	int numRows;
 	int numColumns;
 
 	public Board() {
 		cells = new ArrayList<BoardCell>();
 		rooms = new HashMap<Character, String>();
 
 		targets = new TreeSet<BoardCell>();
 		visited = new boolean[numRows][numColumns];
 		for (int i = 0; i < numRows; i++) {
 			for (int j = 0; j < numColumns; j++) {
 				visited[i][j] = false;
 			}
 		}
 	}
 
 	// ---DONE---
 	public void loadConfigFiles(String path) throws BadConfigException {
 		FileReader reader;
 		Scanner scn;
 		try {
 			reader = new FileReader(path);
 			scn = new Scanner(reader);
 			String line;
 			String temp;
 
 			while (scn.hasNextLine()) {
 				line = scn.nextLine();
 				numColumns = (line.length() + 1) / 2;
 				numRows++;
 				temp = "";
 				for (int indx = 0; indx < line.length(); indx++) {
 					// last index in a line
 					if (indx == line.length() - 1) {
 						temp += line.charAt(indx);
 						if (temp.equals("W")) {
 							WalkWayCell wc = new WalkWayCell(temp);
 							cells.add(wc);
 						} else if (temp.length() > 1) {
 							RoomCell rc = new RoomCell(temp);
 							rc.initial = temp.charAt(0);
 							if (temp.charAt(1) == 'U') {
 								rc.setDoorDir(DoorDirection.UP);
 							}
 							else if (temp.charAt(1) == 'D') {
 								rc.setDoorDir(DoorDirection.DOWN);
 							}
 							else if (temp.charAt(1) == 'R') {
 								rc.setDoorDir(DoorDirection.RIGHT);
 							}
 							else if (temp.charAt(1) == 'L') {
 								rc.setDoorDir(DoorDirection.LEFT);
 							}
 							rc.setDoorway(true);
 							cells.add(rc);
 						} else {
 							RoomCell rc = new RoomCell(temp);
 							rc.initial = temp.charAt(0);
 							cells.add(rc);
 						}
 						temp = "";
 					}
 
 					else if (line.charAt(indx) != ',') {
 						temp += line.charAt(indx);
 					}
 
 					else {
 						if (temp.equals("W")) {
 							WalkWayCell wc = new WalkWayCell(temp);
 							cells.add(wc);
 						} else if (temp.length() > 1) {
 							RoomCell rc = new RoomCell(temp);
 							rc.initial = temp.charAt(0);
 							if (temp.charAt(1) == 'U') {
 								rc.setDoorDir(DoorDirection.UP);
 							}
 							if (temp.charAt(1) == 'D') {
 								rc.setDoorDir(DoorDirection.DOWN);
 							}
 							if (temp.charAt(1) == 'R') {
 								rc.setDoorDir(DoorDirection.RIGHT);
 							}
 							if (temp.charAt(1) == 'L') {
 								rc.setDoorDir(DoorDirection.LEFT);
 							}
 							rc.setDoorway(true);
 							cells.add(rc);
 						} else {
 							RoomCell rc = new RoomCell(temp);
 							rc.initial = temp.charAt(0);
 							rc.setRoom(true);
 							cells.add(rc);
 						}
 						temp = "";
 					}
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	// ---DONE---
 	public void loadLegend(String path) {
 		FileReader reader;
 		Scanner scn;
 		try {
 			reader = new FileReader(path);
 			scn = new Scanner(reader);
 			String line;
 			char init;
 			String rom;
 			while (scn.hasNext()) {
 				line = scn.nextLine();
 				init = line.charAt(0);
 				rom = line.substring(2);
 				rooms.put(init, rom);
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	// ---Get The Index Of A Cell Using Row and Col---
 	public int calcRoomIndex(int row, int col) {
 		return row * numColumns + col;
 	}
 
 	// ---Get Room Cell Using Row & Col---
 	public RoomCell getRoomCellAt(int row, int col) {
 		if (cells.get(calcRoomIndex(row, col)).isRoom()){//getClass().equals(RoomCell.class)) {
 			return (RoomCell) cells.get(calcRoomIndex(row, col));
 		} else {
 			return null;
 		}
 	}
 
 	// ---Get Rows Of The Board---
 	public int getNumRows() {
 		return numRows;
 	}
 
 	// ---Get Columns Of The Board---
 	public int getNumColumns() {
 		return numColumns;
 	}
 
 	// ---Get Board Cell Row---
 	public int getRow(int indx, int BoardCols) {
 		return (int) (indx / BoardCols);
 	}
 
 	// ---Get Board Cell Column---
 	public int getColumn(int indx, int BoardCols) {
 		return indx % BoardCols;
 	}
 
 	// ---Get Board Cell Using INDX---
 	public BoardCell getCellAt(int indx) {
 		return cells.get(indx);
 	}
 
 	// ---Get Board Cell Using ROW & COL---
 	public BoardCell getCellAt(int row, int col) {
 		return cells.get(calcRoomIndex(row, col));
 	}
 
 	// ---Calculate The Adjacency List---
 	public void calcAdjacencies() {
 		BoardCell bc;
 		RoomCell rc;
 		
 		for (int i = 0; i < numRows; i++) {
 			for (int j = 0; j < numColumns; j++) {
 				bc = getCellAt(i, j);
 				adjList = new LinkedList<Integer>();
 				// ---inside a room
 				if (bc.isRoom()) {
 					// ---door cell
 					if (bc.isDoorway()) {
 						rc = (RoomCell) bc;
 						if (rc.getDoorDir().equals(DoorDirection.UP)) {
 							adjList.add(numColumns * (i - 1) + j);
 						} else if (rc.getDoorDir().equals(DoorDirection.DOWN)) {
 							adjList.add(numColumns * (i + 1) + j);
 						} else if (rc.getDoorDir().equals(DoorDirection.RIGHT)) {
 							adjList.add(numColumns * i + j + 1);
 						} else if (rc.getDoorDir().equals(DoorDirection.LEFT)) {
 							adjList.add(numColumns * i + j - 1);
 						}
 						
 					}
 					// ---not door cell
 					/*else {
 						drs = doorsOfRoom(numColumns * i +j);
 						System.out.println(numColumns*i+j + " " +drs.toString());
 						for (int k = 0; k < drs.size(); k++) {
 							rc = (RoomCell) getCellAt(drs.get(k));
 							if (rc.getDoorDir().equals(DoorDirection.UP)) {
 								adjList.add(numColumns * (i - 1) + j);
 							} else if (rc.getDoorDir().equals(DoorDirection.DOWN)) {
 								adjList.add(numColumns * (i + 1) + j);
 							} else if (rc.getDoorDir().equals(DoorDirection.RIGHT)) {
 								adjList.add(numColumns * i + j + 1);
 							} else if (rc.getDoorDir().equals(DoorDirection.LEFT)) {
 								adjList.add(numColumns * i + j - 1);
 							}
 						}
 						
 					}*/
 				} else if (bc.isWalkway()) {
 					//directions
 					boolean top = false;
 					boolean bottom = false;
 					boolean right = false;
 					boolean left = false;
 					//cell type
 					boolean room = false;
 					boolean drWay = false;
 					
 					if ((j + 1) == numColumns) {
 						right = true;
 					}
 					
 					if ((j - 1) == -1) {
 						left = true;
 					}
 		
 					if (i - 1 == -1) {
 						top = true;
 					}
 					
 					if ((i + 1) == numRows) {
 						bottom = true;
 					}
 					
 					//left
 					if (i%numColumns != 0) {
 						if (getCellAt(i - 1).isDoorway()){
 							drWay = true;
 						}
 						else if (getCellAt(i - 1).isRoom()) {
 							room = true;
 						}
 					}
 					//right
 					if((i+1)%numColumns != 0){
 						if (getCellAt(i + 1).isDoorway()){
 							drWay = true;
 						}
 						else if (getCellAt(i + 1).isRoom()) {
 							room = true;
 						}
 					}
 					//top
 					if(j%numRows != 0){
 						if (getCellAt(j - 1).isDoorway()){
 							drWay = true;
 						}
 						else if (getCellAt(j - 1).isRoom()) {
 							room = true;
 						}
 					}
 					//down
 					if((j+1)%numRows != 0){
 						if (getCellAt(j + 1).isDoorway()){
 							drWay = true;
 						}
 						else if (getCellAt(j + 1).isRoom()) {
 							room = true;
 						}
 					}
 					
 					if (!room || drWay){
 						if (!right) {
 							adjList.add(numColumns * i + j + 1);
 						}
 						if (!left) {
 							adjList.add(numColumns * i + j - 1);
 						}
 						if (!top) {
 							adjList.add(numColumns * (i - 1) + j);
 						}
 						if (!bottom) {
 							adjList.add(numRows * (i + 1) + j);
 						}
 					}
 				}
 
 				adj.put(getCellAt(numColumns * i + j), adjList);
 				//checking...
 				//System.out.println(adj.get(getCellAt(numColumns*i+j)));
 				//System.out.println(bc.isRoom());
 			}
 		}
 	}
 
 	// ---List Of Adjacency---
 	public LinkedList<Integer> getAdjList(int indx) {
 		return (LinkedList<Integer>) adj.get(indx);
 	}
 
 	// ---Calculate Targets---
 	public void calcTargets(int indx, int steps) {
 		int rowT = this.getRow(indx, numColumns);
 		int colT = this.getColumn(indx, numColumns);
 
 		if ((visited[rowT][colT] == false && steps == 0)) {
 			targets.add(getCellAt(numColumns * rowT + colT));
 			return;
 		} else {
 			for (int k : getAdjList(numColumns * rowT + colT)) {
 				visited[rowT][colT] = true;
 				if (!visited[(int) (k / numColumns)][k % numColumns]) {
 					calcTargets(k, steps - 1);
 				}
 				visited[rowT][colT] = false;
 			}
 		}
 
 	}
 
 	// ---Set Of Targets---
 	public Set<BoardCell> getTargets() {
 		return targets;
 	}
 
 	// ---Get All Doors Of A Room---
 	/*public LinkedList<Integer> doorsOfRoom(int indx) {
 		LinkedList<Integer> doors = new LinkedList<Integer>();
 		char roomInitial = cells.get(indx).toString().charAt(0);
 		
 		for (int i = 0; i < numRows; i++) {
 			for (int j = 0; j < numColumns; j++) {
 				if (getCellAt(i, j).toString().length() > 1 && 
 						getCellAt(i, j).toString().charAt(0) == roomInitial) {
 					doors.add(numColumns * i + j);
 				}
 			}
 		}
 		
 		return doors;
 	}*/
 
 	
 	// ......."Main" is: ONLY FOR TESTING AND DEBUGGING .~_~.
 	public static void main(String[] args) throws FileNotFoundException {
 		Board b = new Board();
 
 		FileReader redr = new FileReader("work1.csv");
 		Scanner s = new Scanner(redr);
 		try {
 			b.loadConfigFiles("work1.csv");
 		} catch (BadConfigException e) {
 			e.printStackTrace();
 		}
 		b.loadLegend("initials.csv");
 		//System.out.println(b.numRows);
 		//System.out.println(b.numColumns);
 		//System.out.println(b.cells.size());
 		//System.out.println(b.getColumn(25, 23));
 		//System.out.println(b.getRow(25, 23));
 		//System.out.println(b.cells.get(0).isDoorway());
 		b.calcAdjacencies();
		System.out.println(b.getCellAt(3));
 		//System.out.println(b.getCellAt(320));
 		//System.out.println(b.adj.get(b.getCellAt(46)).toString());
 
 	}
 
 }
