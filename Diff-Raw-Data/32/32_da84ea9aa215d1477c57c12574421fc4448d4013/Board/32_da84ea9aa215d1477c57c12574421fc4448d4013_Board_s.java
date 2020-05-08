 package clueGame;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 import java.util.Set;
 
 import clueGame.RoomCell.DoorDirection;
 
 public class Board {
 	ArrayList<BoardCell> cells;
 	Map<Character, String> rooms;
 	int numRows;
 	int numColumns;
 	private Map<Integer, LinkedList<Integer>> adjacencies;
 	private Set<BoardCell> targets;
 	private boolean[] seen;
 
 	public Board() {
 		cells = new ArrayList<BoardCell>();
 		rooms = new HashMap<Character, String>();
 		targets = new HashSet<BoardCell>();
 		seen = new boolean[numRows*numColumns];
 	}
 
 	public void loadConfigFiles(String legendFile, String boardFile) throws BadConfigFormatException {
 		loadLegend(legendFile);
 		loadBoard(boardFile);
 	}
 
 	public int calcIndex(int row, int column) {
 		return row*numColumns+column;
 		//TODO we are using rows+cols staring from 1, right?
 	}
 	public LinkedList<Integer> getAdjList(int index){
 		return adjacencies.get(index);
 	}
 	public RoomCell getRoomCellAt(int row, int column) {
 		BoardCell cell = cells.get(calcIndex(row, column));
 		if (cell instanceof RoomCell) {
 			return (RoomCell)cell;
 		}
 		else {
 			return null;
 		}
 	}
 
 	public BoardCell getCellAt(int cell) {
 		return cells.get(cell);
 	}
 
 	public ArrayList<BoardCell> getCells() {
 		return cells;
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
 
 	private void loadLegend(String legendFile) throws BadConfigFormatException {
		Pattern legendLine = Pattern.compile("[a-z],[a-z ]+", Pattern.CASE_INSENSITIVE);
 
 		try {
 			FileReader f = new FileReader(legendFile);
 			Scanner scan = new Scanner(f);
			while (scan.hasNext(legendLine)) {
				String line = scan.nextLine();
 				char abbr = line.charAt(0); // the abbreviation for the room
				String room = line.substring(2); // trim off the abbreviation and comma to get the full name
 				rooms.put(abbr, room);
 			}

			// if there is input remaining, the file format is invalid
			if (scan.hasNext()) {
				throw new BadConfigFormatException();
			}
 		} catch (FileNotFoundException e) {
 			System.out.println("I'm sorry, but the " + legendFile
 					+ " file is a figment of your imagination.");
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private void loadBoard(String boardFile) throws BadConfigFormatException {
 		numRows = 0;
 		numColumns = -1;
 		String[] spaces;
 		try {
 			FileReader f = new FileReader(boardFile);
 			Scanner scan = new Scanner(f);
 			while (scan.hasNextLine()) {
 				spaces = scan.nextLine().split(",");
 
 				// if the number of columns changes, the file is invalid
 				if (numColumns != -1 && numColumns != spaces.length) {
 					throw new BadConfigFormatException("Inconsistent number of spaces in row " + numRows);
 				}
 
 				numColumns = spaces.length;
 
 				for (int i = 0; i < numColumns; i++) {
 					String space = spaces[i];
 					if (space.equalsIgnoreCase("W")) {
 						cells.add(new WalkwayCell(numRows, i % numColumns));
 					}
 					else {
 						if (space.length() > 0 && space.length() <= 2) {
 							char initial = space.charAt(0);
 
 							if (!rooms.containsKey(initial)) {
 								throw new BadConfigFormatException("Invalid room initial '" + initial + '"');
 							}
 
 							DoorDirection direction = DoorDirection.NONE;
 
 							if (space.length() > 1) {
 								switch (space.charAt(1)) {
 								case 'R': direction = DoorDirection.RIGHT; break;
 								case 'L': direction = DoorDirection.LEFT; break;
 								case 'U': direction = DoorDirection.UP; break;
 								case 'D': direction = DoorDirection.DOWN; break;
 								default: throw new BadConfigFormatException("Invalid room direction '" + space.charAt(1) + "'");
 								}
 							}
 
 							cells.add(new RoomCell(numRows, i % numColumns, initial, direction));
 						}
 						else {
 							throw new BadConfigFormatException("Wrong length of room '" + space + "'");
 						}
 					}
 				}
 
 				numRows++;
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println("I'm sorry, but the " + boardFile
 					+ " file is a figment of your imagination.");
 		}
 
 	}
 	public void calcAdjacencies() {//TODO ripped from IntBoard.java
 		for (int i = 0; i < numRows * numColumns; i++) {
 			LinkedList<Integer> cells = new LinkedList<Integer>();
 
 			int column = i % numColumns;
 			int row = i / numColumns;
 
 			if (column > 0) { // left
 				cells.add(i - 1);
 			}
 
 			if (column < numColumns - 1) { // right
 				cells.add(i + 1);
 			}
 
 			if (row > 0) { // up
 				cells.add(i - numColumns);
 			}
 
 			if (row < numRows - 1) { // down
 				cells.add(i + numColumns);
 			}
 
 			adjacencies.put(i, cells);
 		}
 	}
 
 	public void calcTargets(int index, int moves) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public Set<BoardCell> getTargets() {
 		return targets;
 	}
 
 }
