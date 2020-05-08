 package agents;
 
 import pplgg.MapManager;
 import pplgg.PPLGG.Terrain;
 
 public class RoomAgent extends AbstractAgent {
 
 	int roomWidth;
 	int roomHeight;
 	
 	
	public RoomAgent() {
		// TODO Auto-generated constructor stub
	}
 
	public RoomAgent(MapManager newManager) {
		super(newManager);
 		roomWidth = 3;
 		roomHeight = 3;
 		moveTo((int) (Math.random() * askForMapWidth()), (int)(Math.random() * askForMapHeight()));
 		// TODO Auto-generated constructor stub
 	}
 	
	public RoomAgent(MapManager newManager,int minWidth,int maxWidth,int minHeight, int maxHeight, int tokens){
	super(newManager);
	// generates the room sizes for this particular agent instance
	roomWidth = minWidth + (int) (Math.random() * (maxWidth-minWidth));
	roomHeight = minHeight + (int) (Math.random() * (maxHeight-minHeight));
	
	// Initiates the room agent at a random place on the map
	moveTo((int) (Math.random() * askForMapWidth()), (int)(Math.random() * askForMapHeight()));
	
	this.tokens = tokens;
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
