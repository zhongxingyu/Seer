 //This thread handles incoming packets as they arrive, and stores the data for later 
 //reference by GameRender
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 
 public class ClientStreamReader implements Runnable {
 
 	public boolean running = true;
 	
 	private ObjectInputStream inStream;
 	
 	public double gameTime;
 	public int gameState;
 	public int playerNumber;
 	
 	public int[][] playerArray;
 	public int[] ballArray;
 	public int ballPossessor;
 	public double chargeTime;
 	public int blueScore;
 	public int redScore;
 	public int goalPosition;
 	public int gamePeriod;
 	public ClientStreamReader(ObjectInputStream in) {
 		inStream = in;
 		
 		//recieve player number
 		try {
 			playerNumber = inStream.readInt();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		ballArray = new int[3];
 		
 		//start the thread
 		Thread t = new Thread(this, "client stream reader");
 		t.start();
 	}
 
 	@Override
 	public void run() {
 		while(running){
 			try{
 				if(inStream.available() > 0){
 					
 					gameTime = inStream.readDouble();
 					gamePeriod = inStream.readInt();
 					gameState = inStream.readInt();
 					playerArray = (int[][]) inStream.readObject();
 					
 					for(int i=0;i<3;i++){
 						ballArray[i] = inStream.readInt();
 					}
 					
 					ballPossessor = inStream.readInt();
 					chargeTime = inStream.readDouble();
 					blueScore = inStream.readInt();
 					redScore = inStream.readInt();
 					goalPosition = inStream.readInt();
 				}
 			} catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 }
