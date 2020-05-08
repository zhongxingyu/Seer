 package game;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import server.Debugger;
 import server.Scoreboard;
 
 public class Checkers implements GenGame {
 
 	private int board[][] = {
 			{ 0, -1, 0, -1, 0, -1, 0, -1 }, 
 			{ -1, 0, -1, 0, -1, 0, -1, 0 }, 
 			{ 0, -1, 0, -1, 0, -1, 0, -1 },
 			{ -1, -1, -1, -1, -1, -1, -1, -1 }, 
 			{ -1, -1, -1, -1, -1, -1, -1, -1 }, 
 			{ -1, 1, -1, 1, -1, 1, -1, 1 },
 			{ 1, -1, 1, -1, 1, -1, 1, -1 },
 			{ -1, 1, -1, 1, -1, 1, -1, 1 }
 			};
 	private int whoseTurn = 0;
 	private boolean gameRunning = false;
 	private int whoWon = -1;
 
 	public Checkers() {
 		if(Debugger.DEBUG){
 			System.out.println("Checkers Game Running");
 		}
 		whoseTurn = 1;
 		gameRunning = true;
 	}
 
 	@Override
 	public JSONObject getStatus() {
 
 		JSONArray rBoard = new JSONArray();
 		for (int i = 0; i < board.length; i++) {
 			JSONArray collum = new JSONArray();
 			for (int j = 0; j < board[i].length; j++) {
 				collum.put(board[i][j]);
 			}
 			rBoard.put(collum);
 		}
 		JSONObject rObj = new JSONObject();
 		try {
 			rObj.put("BOARD", rBoard);
 			rObj.put("TURN", whoseTurn);
 			rObj.put("RUNNING", gameRunning);
 			rObj.put("WHOWON", whoWon);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return rObj;
 	}
 
 	@Override
 	public JSONObject doCommand(JSONObject input) throws JSONException {
 		
 		int x1 = 0;
 		int y1 = 0;
 		
 		
 		int id = -1;
 		int error = -1;
 		boolean won = false;
 		JSONArray command = null;
 
 		try {
 			id = input.getInt("ID");
 			command = input.getJSONArray("COMMAND");
 		} catch (JSONException e) {
 			System.err.println("Malformed JSON");
 			error = 3;
 		}
 
 		String commandName = null;
 		commandName = (String) command.getString(0);
 
 		if (commandName.equalsIgnoreCase("move")) {
 			
 			if (whoseTurn-1 == id) {
 				
 				x1 = command.getInt(1);
 				y1 = command.getInt(2);
 
 				ArrayList<Integer> coordList = new ArrayList<Integer>();
 				
 				for(int i=3;i<command.length()-3;i++){
 					coordList.add(command.getInt(i)); //this should be atleast 2
					coordList.get(i);
 				}
 				
				
 				if(validMove(x1,y1,coordList)){
 					doMove(x1,y1,coordList);
 					System.out.println("Move successful");
 					error = 0;
 					if (checkWin(id)) {
 						System.out.println("Player " + id + " won"); 
 						Scoreboard.incrementScore(Integer.toString(input.getInt("GAMEID")));
 						won = true;
 						whoWon = id;
 						gameRunning = false;
 					}
 					
 				}else{
 					error = 2;
 				}
 				
 			}
 				
 			
 		}
 
 		JSONObject rWhat = new JSONObject();
 		try {
 			rWhat.put("ERROR", error);
 			rWhat.put("WON", won);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			System.err.println("Error creating return object");
 		}
 		return rWhat;
 		
 		
 	}
 
 	private boolean checkWin(int id) {
 		
 		for(int i=0;i<8;i++){
 			for(int j=0;j<8;j++){
 				if(board[i][j]!=id && board[i][j]!=id+2 && board[i][j]!=-1){
 					return false;
 				}
 			}
 		}
 		return true;
 		
 	}
 
 	private boolean validMove(int x1, int y1, ArrayList<Integer> coordList) {
 		
		System.out.println("Checking for valid move");
		
		
 		boolean areJumps = (coordList.size()==2) ? false: true;
 		
 	
 		/*	
 		if (!areJumps) {
 			int x2 = coordList.get(0);
 			int y2 = coordList.get(1);
 
 			if (board[x1][y1] == whoseTurn) { // we have a normal piece
 				if (((y2 == y1 + ((whoseTurn == 1) ? 1 : -1)) && (x2 == x1 + 1 || x2 == x1 - 1)) && inBounds(x2, y2)) {
 					if (board[x2][y2] == 0) {
 						return true;
 					}
 				}
 
 			}
 
 			if (board[x1][y1] == whoseTurn + 2) { // we have a king piece
 				if (((y2 == y1 + 1 || y2 == y1 - 1) && (x2 == x1 + 1 || x2 == x1 - 1))
 						&& inBounds(x2, y2)) { // we have a legal move
 					if (board[x2][y2] == 0) {
 						return true;
 					}
 				}
 			}
 
 		}
 
 		return false;
 	*/
 	return true;
 	}
 
 	private void doMove(int x1, int y1, ArrayList<Integer> coordList) {
 
 		boolean areJumps = (coordList.size()==2) ? false: true;
 		
 		if (!areJumps) {
 			
 			board[coordList.get(0)][coordList.get(1)] = board[x1][y1];
 			board[x1][y1] = -1;
 			
 		}else{
 			
 			//handles first jump
 			board[coordList.get(0)][coordList.get(1)] = board[x1][y1];
 			board[x1][y1] = -1;
 			board[(x1+coordList.get(0))/2][(y1+coordList.get(1))/2] = -1;
 			
 			for(int i=2;i<coordList.size();i+=2){ //handles the rest of the jumps
 				board[coordList.get(i)][coordList.get(i+1)] = board[i-2][i-1];
 				board[x1][y1] = -1;
 				board[(coordList.get(i-2)+coordList.get(i))/2][(coordList.get(i-1)+coordList.get(i+1))/2] = -1;
 			}
 		}
 		
 		
 		if (whoseTurn == 1) {
 			whoseTurn = 0;
 		} else {
 			if (whoseTurn == 0)
 				whoseTurn = 1;
 		}
 		
 		return;
 	}
 
 	
 
 	private boolean inBounds(int x, int y) {
 		return (x>=0 && x<8) && (y>=0 && y<8);
 	}
 
 }
