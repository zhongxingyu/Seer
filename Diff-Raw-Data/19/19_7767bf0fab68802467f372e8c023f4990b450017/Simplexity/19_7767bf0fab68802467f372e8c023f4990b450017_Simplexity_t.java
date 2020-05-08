 package game;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Simplexity implements GenGame {
 
 	private static final int WIDTH = 7;
 	private static final int HEIGHT = 6;
 	private int whoseTurn = 0;
 	
 	public int board[][] = new int[WIDTH][HEIGHT];
 	
 	public Simplexity(){
 		System.out.println("Simplexity running");
 	}
 	
 	
 	//returns -1 on fail, else returns y coordinate
 	private int doMove(int x, int type){
 		int mod = (whoseTurn==0) ? 0 : 2;
 		int y = -1;
 		
 		for(int j=0;j<board[x].length;j++){
 			
 			if(board[x][j]==0){
 				y=j;
 				break;
 			}
 		}
 
 		if(y!=-1){
 			board[x][y] = mod+type; // play move if valid
 			System.out.println("Player "+whoseTurn+" played a move at ("+x+","+y+")");
 			if (whoseTurn == 1) { //change turn
 				whoseTurn = 0;
 			} else {
 				if (whoseTurn == 0)
 					whoseTurn = 1;
 			}
 		}
 		
 		return y;
 	}
 	
 	@Override
 	public JSONObject getStatus() {
 		
 		JSONArray rBoard = new JSONArray();
 		for (int i = 0; i < WIDTH; i++) {
 			JSONArray column = new JSONArray();
 			for (int j = 0; j < HEIGHT; j++) {
 				System.out.print(board[i][j] + " ");
 				column.put(board[i][j]);
 			}
 			System.out.println("");
 			rBoard.put(column);
 		}
 		JSONObject rObj = new JSONObject();
 		try {
 			rObj.put("BOARD", rBoard);
 			rObj.put("TURN", whoseTurn);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return rObj;
 	}
 
 	@Override
 	public JSONObject doCommand(JSONObject input) throws JSONException {
 		
 		int x = -1;
 		int error = -1;
 		int id = -1;
 		boolean won = false;
 		JSONArray command = null;
 		JSONObject retObj = new JSONObject();
 
 		try {
 			id = input.getInt("ID");
 			command = input.getJSONArray("COMMAND");
 		} catch (JSONException e) {
 			// the input is malformed
 			/* HTTPResponse.FAIL */
 			System.out.println("Malformed input JSON");
 			error = 3;
 		}
 
 		String commandName = null;
 		commandName = (String) command.getString(0);
 		
 		
 		if(commandName.equalsIgnoreCase("move")){
 			
 			x = command.getInt(1);
 			int type = command.getInt(2);
 			if (whoseTurn == id) {
 				
 				int y = doMove(x,type);
 				
 				if (y!=-1) {
 					if (checkWin(x,y,id)) {
 						System.out.println("player " + id + " won!");
 						won = true;
 					}
 				}else{
 					error = 2;
 				}
 			}else{
 				error = 1; 
 			}
 		}else if(commandName.equalsIgnoreCase("doworkson!")){
 			won=true; //thatswhatsup
 		}else{
 			
 			//command not recognized
 		}
 		
 		
 		
 		//return object creation
 		JSONObject rWhat = new JSONObject();
 		try {
			rWhat.append("ERROR", error);
			rWhat.append("WON", won);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			System.out.println("Error creating return object");
 		}
 		
 		
 		
 		return rWhat;
 	}
 	
 	
 	//makes around 5,000 comparisons every run (worst case)
 	private boolean checkWin(int x, int y, int id){
 		
 				
 				if(board[x][y]==(2*id)+1 || board[x][y]==(2*id)+2){ //we have a piece of the same color here
 					
 					int c[]=new int[8];
 					int d[]=new int[8];
 					int e[]=new int[8];
 					
 					for(int i=0;i<=4;i++){ 
 						if(isLegal(x,y+i)){
 							if(board[x][y+i]==(2*id)+1 || board[x][y+i]==(2*id)+2){  // check up
 								c[0]++;
 							}
 							if(board[x][y+i]==1 || board[x][y+i]==3){  // check up
 								d[0]++;
 							}
 							if(board[x][y+i]==2 || board[x][y+i]==4){  // check up
 								e[0]++;
 							}
 						}
 						if(isLegal(x,y-i)){
 							if(board[x][y-i]==(2*id)+1 || board[x][y-i]==(2*id)+2){ // check down
 								c[1]++;
 							}
 							if(board[x][y-i]==1 || board[x][y-i]==3){ // check down
 								d[1]++;
 							}
 							if(board[x][y-i]==2 || board[x][y-i]==4){ // check down
 								e[1]++;
 							}
 						}
 						if(isLegal(x+i,y)){
 							if(board[x+i][y]==(2*id)+1 || board[x+i][y]==(2*id)+2){ // check right
 								c[2]++;
 							}
 							if(board[x+i][y]==1 || board[x+i][y]==3){ // check right
 								d[2]++;
 							}
 							if(board[x+i][y]==2 || board[x+i][y]==4){ // check right
 								e[2]++;
 							}
 						}
 						if(isLegal(x-i,y)){
 							if(board[x-i][y]==(2*id)+1 || board[x-i][y]==(2*id)+2){ // check left
 								c[3]++;
 							}
 							if(board[x-i][y]==1 || board[x-i][y]==3){ // check left
 								d[3]++;
 							}
 							if(board[x-i][y]==2 || board[x-i][y]==4){ // check left
 								e[3]++;
 							}
 						}
 						if(isLegal(x+i,y+i)){
 							if(board[x+i][y+i]==(2*id)+1 || board[x+i][y+i]==(2*id)+2){ // check up-right
 								c[4]++;
 							}
 							if(board[x+i][y+i]==1 || board[x+i][y+i]==3){ // check up-right
 								d[4]++;
 							}
 							if(board[x+i][y+i]==2 || board[x+i][y+i]==4){ // check up-right
 								e[4]++;
 							}
 						}
 						if(isLegal(x-i,y+i)){
 							if(board[x-i][y+i]==(2*id)+1 || board[x-i][y+i]==(2*id)+2){ // check up-left
 								c[5]++;
 							}
 							if(board[x-i][y+i]==1 || board[x-i][y+i]==3){ // check up-left
 								d[5]++;
 							}
 							if(board[x-i][y+i]==2 || board[x-i][y+i]==4){ // check up-left
 								e[5]++;
 							}
 						}
 						if(isLegal(x+i,y-i)){
 							if(board[x+i][y-i]==(2*id)+1 || board[x+i][y-i]==(2*id)+2){  // check down-right
 								c[6]++;
 							}
 							if(board[x+i][y-i]==1 || board[x+i][y-i]==3){  // check down-right
 								d[6]++;
 							}
 							if(board[x+i][y-i]==2 || board[x+i][y-i]==4){  // check down-right
 								e[6]++;
 							}
 						}
 						if(isLegal(x-i,y-i)){
 							if(board[x-i][y-i]==(2*id)+1 || board[x-i][y-i]==(2*id)+2){ //check down-left
 								c[7]++;
 							}
 							if(board[x-i][y-i]==1 || board[x-i][y-i]==3){ //check down-left
 								d[7]++;
 							}
 							if(board[x-i][y-i]==2 || board[x-i][y-i]==4){ //check down-left
 								e[7]++;
 							}
 						}	
 					}
 					
 					for(int i=0;i<c.length;i++){
 						if(c[i]==4){
 							System.out.println("Win by color at ("+x+","+y+") looking direction: "+i);
 							return true;
 						}
 						if(d[i]==4){
 							System.out.println("Win by squares at ("+x+","+y+") looking direction: "+i);
 							return true;
 						}
 						if(e[i]==4){
 							System.out.println("Win by circles at ("+x+","+y+") looking direction: "+i);
 							return true;
 						}
 					}
 				}
 		
 		return false; //we didn't find a winner
 	}
 	
 	private boolean isLegal(int x, int y){
 		return (x>=0 && x<WIDTH) && (y>=0 && y<HEIGHT);
 	}
 
 
 }
