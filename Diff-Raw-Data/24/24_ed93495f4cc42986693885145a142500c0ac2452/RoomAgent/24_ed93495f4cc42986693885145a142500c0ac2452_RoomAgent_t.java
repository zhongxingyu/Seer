 package agents;
 
 import pplgg.MapManager;
import pplgg.Position;
 import pplgg.PPLGG.Terrain;
 
 public class RoomAgent extends AbstractAgent {
 
 	int roomWidth;
 	int roomHeight;
 	
 	
 

	public RoomAgent(MapManager newManager, int tokens, Position spawnPos, int waitingPeriod) {
        super(newManager, tokens, spawnPos, waitingPeriod);
 		roomWidth = 3;
 		roomHeight = 3;
 		moveTo((int) (Math.random() * askForMapWidth()), (int)(Math.random() * askForMapHeight()));
 		// TODO Auto-generated constructor stub
 	}
 	
 
 	@Override
 	public void performStep() {
 		// TODO Auto-generated method stub
 		boolean empty = true;
 		if ((x+roomWidth)<askForMapWidth()&&(y+roomHeight)<askForMapHeight()){
 		for (int x = 0; x < roomWidth; x++){
 		for (int y = 0; y < roomHeight; y++){
 			if (requestMapInformation( x+this.x, y+this.y )== Terrain.SOLID){
 				empty=false;
 				break;
 			}
 		}
 		}
 		
 		if (empty){
 			makeRoom();
 			setTokens(tokens--);
 		}
 		}
 		moveTo((int) (Math.random() * askForMapWidth()), (int)(Math.random() * askForMapHeight()));
 	}
 	
 	void makeRoom (){
 		
 		// creating floor and ceiling
 		for (int x=0;x<roomWidth;x++){
 			sendMapRequest(this.x+x,this.y,Terrain.SOLID);
 			sendMapRequest(this.x+x,this.y+roomHeight-1,Terrain.SOLID);
 		}
 		
 		// creating walls
 		for (int y=0;y<roomHeight;y++){
 			sendMapRequest(this.x,this.y+y,Terrain.SOLID);
 			sendMapRequest(this.x+roomWidth-1,this.y+y,Terrain.SOLID);
 		}
 	}
 
 }
