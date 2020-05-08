 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 /**
  * This will handle Player vs. Player Game Play.
  * 
  * 
  * @author Mike Cutalo
  */
 public class PVP extends Turn{
 	
 	private final String SERVERNAME = "bill.kutztown.edu";
 	private final int PORT = 15009;	
 	private PrintWriter out;
 	private BufferedReader in;
 	
 	private Player localPlayer;
 	private Player remotePlayer;
 	
 	
 	public PVP(){
 		localPlayer = new Player();
 		remotePlayer = new Player();
 	}
 	
 	public void connetToServer() throws UnknownHostException, IOException, InterruptedException{
 		
 		Socket socket = new Socket(SERVERNAME, PORT);		
 		System.out.println("Starting connection ...");
 		
 		out = new PrintWriter(socket.getOutputStream(), true);
 		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 					
 		getData();		
 		
 		System.out.println("Closing Socket");
 		socket.close();
 	}
 	
 	public void sendBoardData()
 	{
 		System.out.println("Sending Board data now...");
 		
 		for(int i=0; i < 10; i++)
 		{
 			for(int j=0; j < 10; j++)
 			{
 				if(localPlayer.getPlayerBoard().getBoard()[i][j].getOccupyingShip() == ' '){
 					out.println(i+""+j+"E");
 				}else{
 					out.println(i+""+j+""+localPlayer.getPlayerBoard().getBoard()[i][j].getOccupyingShip());	
 				}				
 			}
 		}
 		
 		out.println("00x");
 	}
 	
 	public void sendData(String data){
 		out.println(data);
 	}
 
 	public void createShips(){
 		int [] shipMaxHit = {5,4,3,3,2};
 		char [] shipInit = {'A','B','S','D','P'};
		String [] shipType ={"Aircraft Carrier", "BattleSship","Submarine","Destroyer","Patrol Boat"};		
 		Ship newShip = new Ship();
		
		for(int i=0; i<5; i++){
			newShip.setMaxHit(shipMaxHit[i]);
			newShip.setBoatInit(shipInit[i]);
			newShip.setBoatType(shipType[i]);
			
			remotePlayer.allShips.put(shipInit[i], newShip);
			newShip = new Ship();
		}
		
 	}
 	
 	public void getData() throws InterruptedException{
 		String inputData;
 		boolean boardDataDone = false;
 		while(true){
 			try {
 				inputData = in.readLine();
 				
 				if(inputData.equals(null)){
 					System.out.println("Sleeping GetData");
 					Thread.sleep(500);
 				}else{
 					
 					System.out.println("Data from server:" + inputData);
 										
 					
 					if(boardDataDone == false){
 						int row = Integer.parseInt(String.valueOf(inputData.charAt(0)));
 						int col = Integer.parseInt(String.valueOf(inputData.charAt(1)));
 						char what = inputData.charAt(2);
 						
 						if(what == 'A'){
 							remotePlayer.getPlayerBoard().getBoard()[row][col].setOccupyingShip('A');
 						}else if(what == 'B'){
 							remotePlayer.getPlayerBoard().getBoard()[row][col].setOccupyingShip('B');
 						}else if(what == 'S'){
 							remotePlayer.getPlayerBoard().getBoard()[row][col].setOccupyingShip('S');
 						}else if(what == 'D'){
 							remotePlayer.getPlayerBoard().getBoard()[row][col].setOccupyingShip('D');
 						}else if(what == 'P'){
 							remotePlayer.getPlayerBoard().getBoard()[row][col].setOccupyingShip('P');
 						}else if(what == 'x'){
 							
							createShips();
 							this.setOnLinePlayer(remotePlayer);
 							this.setPVP(true);
							
 							this.startListening();
 							boardDataDone = true;
							
 						}
 					}
 					
 					
 					
 					
 					
 					
 					
 										
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}		
 	}
 	
 
 	//Getters & Setters//
 	public Player getLocalPlayer() {
 		return localPlayer;
 	}
 
 	public void setLocalPlayer(Player localPlayer) {
 		this.localPlayer = localPlayer;
 	}
 
 	public Player getRemotePlayer() {
 		return remotePlayer;
 	}
 
 	public void setRemotePlayer(Player remotePlayer) {
 		this.remotePlayer = remotePlayer;
 	}
 }
