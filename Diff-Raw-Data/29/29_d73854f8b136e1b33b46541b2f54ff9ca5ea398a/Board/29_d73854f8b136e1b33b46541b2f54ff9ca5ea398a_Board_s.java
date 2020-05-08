 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 
 public class Board {
 	private ArrayList<BoardCell> cells = new ArrayList<BoardCell>();
 	private ArrayList<String> list=new ArrayList<String>();
 	private Map<Character,String> rooms= new HashMap<Character, String>();
 
 	int[] boardDim;
 	//int doorways=0;
 	HashSet<Integer> targetList=new HashSet<Integer>();
 	public Board(String string, String string2) throws BadConfigFormatException, FileNotFoundException {
 		boardDim=new int[2];
 		boardDim[0]=boardDim[1] = 0;
 		int yAxis=0;
 		Scanner input=new Scanner(new BufferedReader(new FileReader(string)));
 		input.useDelimiter(",");
 		String iter=new String();
 		String doorString = "udlr"; //fastest way to search
 		while(input.hasNext()){
 			iter=input.next();
 			boardDim[0]=iter.contains("\n")?boardDim[1]+1:0;
 			boardDim[1]+=(boardDim[0]!=0)?1:0;
 			//System.out.println(boardDim[0]+","+boardDim[1]);
 			BoardCell e=null;
 			iter=iter.replaceAll("[\\n\\r]", ""); //regex to remove returns and newlines (yes, they're different)
 			if(iter.equals('w')){
 				e=new WalkwayCell(boardDim[0],boardDim[1]);
 			} else {
 				e=new RoomCell(boardDim[0],boardDim[1],iter.charAt(0));
 				if(iter.length()>1){
//					System.out.print(iter.charAt(1));
 					if(doorString.contains(String.valueOf(iter.charAt(1)))){ //see if the key character for the door is in the string "udlr"
 						//Creates the door using that character
 						((RoomCell) e).setRoomDirection(iter.charAt(1));
 					}
 				}
 			}
 			cells.add(e);
 		}
 	}
 
 
 
 
 	//}
 	public Board()	 {
 		boardDim=new int[2];
 		boardDim[0]=0;
 		boardDim[1]=0;
 		try {
 			Scanner input=new Scanner(new BufferedReader(new FileReader("etc/Clue_map.csv")));
 			input.useDelimiter(",");
 			String iter=new String();
 			String doorString = "udlr"; //fastest way to search
 			while(input.hasNext()){
 				iter=input.next();
 				boardDim[0]+=(iter.contains("\n"))?1:0;
 				boardDim[1]=(!iter.contains("\n"))?boardDim[1]+1:0;
				System.out.println(boardDim[0]+","+boardDim[1]);
 				if(iter.contains("\n")){
 				}
 				BoardCell e=null;
 				iter=iter.replaceAll("[\\n\\r]", ""); //regex to remove returns and newlines (yes, they're different)
 				if(iter.equals('w')){
 					e=new WalkwayCell(boardDim[1],boardDim[0]);
 				} else {
 					e=new RoomCell(boardDim[1],boardDim[0],iter.charAt(0));
 					if(iter.length()>1){
//						System.out.print(iter.charAt(1));
 						if(doorString.contains(String.valueOf(iter.charAt(1)))){ //see if the key character for the door is in the string "udlr"
 							//Creates the door using that character
 							((RoomCell) e).setRoomDirection(iter.charAt(1));
 						}
 					}
 				}
 				cells.add(e);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	public void loadConfigFiles(){
 
 	}
 	public int calcIndex(int row, int column){	// turns a 2d board into a 1d list
		return column+row*(boardDim[1]+1);
 	}
 	public void AddRoomCell(BoardCell b){
 		cells.add(b);
 	}
 	public RoomCell getRoomCellAt(int row, int column){
 		return ((RoomCell)cells.get(calcIndex(row,column))) ; 
 	}
 
 	public ArrayList<BoardCell> getCells() {
 		return cells;
 	}
 	public void setCells(ArrayList<BoardCell> cells) {
 		this.cells = cells;
 	}
 	public Map<Character, String> getRooms() {
 		try{
 			Scanner input=new Scanner(new BufferedReader(new FileReader("etc/ClueLegend.txt")));
 			while(  input.hasNext()){
 				String cha= input.next();
 				char c=cha.charAt(0);
 				String r=input.nextLine();
 				rooms.put(c, r);
 			}
 		}catch (FileNotFoundException e){
 			System.out.println("Could not open Key");
 			System.exit(0);}
 		return rooms;
 	}
 	public void setRooms(Map<Character, String> rooms) {
 		this.rooms = rooms;
 	}
 
 	public BoardCell getCellAt(int calcIndex) {
 		return cells.get(calcIndex);
 	}
 
 	public HashSet<Integer> calcAdjacencies(int loc,int steps,HashSet<Integer> adjlist){
 		//This function needs to do a recursive loop that counts down the steps to check if the space to the right, left, top and bottom
 		//are valid places, if so it adds to adjlist, we need not worry about copies since this is a hashset.
 		if(steps!=0){
 			if((loc-boardDim[0])>=0){
 				//Go up?
 				adjlist.addAll(calcAdjacencies(loc-boardDim[0],steps-1,adjlist));
 			}
 			if(loc+boardDim[0]<(boardDim[0]*boardDim[1])){
 				//Go down?
 				adjlist.addAll(calcAdjacencies(loc+boardDim[0],steps-1,adjlist));
 			}
 			if((loc)%boardDim[0]!=0 && (loc-1)>=0){
 				//Go left?
 				adjlist.addAll(calcAdjacencies(loc-1,steps-1,adjlist));
 			}
 			if((loc+1)%boardDim[0]!=0 && (loc+1)<=(boardDim[0]*boardDim[1])){
 				//Go right?
 				adjlist.addAll(calcAdjacencies(loc+1,steps-1,adjlist));
 			}
 			return adjlist;
 		} else if(steps==0 && !getCellAt(loc).isRoom()){
 			if(!adjlist.contains(loc)){
 				adjlist.add(loc);
 			}
 			return adjlist;
 		} else {
 			return adjlist;
 		}
 	}
 
 
 	public HashSet<Integer> getAdjList(int calcIndex) {
 		HashSet<Integer> adjlist = new HashSet<Integer>();
 		//adjlist.add(i);
 		return calcAdjacencies(calcIndex,0,adjlist); 
 
 	}
 	public void calcTargets(int i, int j, int k) {
 		calcAdjacencies(calcIndex(i,j),k,new HashSet<Integer>());
 	}
 	public Set<BoardCell> getTargets() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	public int getNumColumns() {
 
 		return boardDim[1];
 	}
 	public int getNumRows() {

 		return boardDim[0];
 	}
 	public void loadRoomConfig() {
 		// TODO Auto-generated method stub
 
 	}
 	public void loadBoardConfig() {
 		// TODO Auto-generated method stub
 
 	}
 
 
 }
 
 
