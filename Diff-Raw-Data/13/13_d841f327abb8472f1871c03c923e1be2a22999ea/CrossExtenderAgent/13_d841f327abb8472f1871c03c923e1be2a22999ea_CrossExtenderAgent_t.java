 package agents;
 
 import pplgg.MapManager;
 import pplgg.PPLGG.Terrain;
 import pplgg.Position;
 
 public class CrossExtenderAgent extends AbstractAgent {
 
     private Position basePosition;
     
    public CrossExtenderAgent(MapManager newManager, int tokens, Position spawnPos, int waitingPeriod) {
        super(newManager, tokens, spawnPos, waitingPeriod); 
        
        //moveToRandomPosition();
         basePosition = new Position(x,y);
      
     }
 
     @Override
     public void performStep() {
         
        if (this.myManager.outsideBoundaries(x,y)) {
             x = basePosition.x;
             y = basePosition.y;
         }
         if (requestMapInformation( x, y ) == Terrain.EMPTY) {
             int tries = 0;
             while (tries<100 && requestMapInformation( x, y ) == Terrain.EMPTY) { 
                 moveToRandomPosition();
                 tries++;
             }
             basePosition = new Position(x,y);
         }
         else {
             int dx, dy;
             switch ((int)(4*Math.random())) {
                 case 0:
                     dx = 1;
                     dy = 0;
                     break;
                 case 1:
                     dx = 0;
                     dy = -1;
                     break;
                 case 2:
                     dx = -1;
                     dy = 0;
                     break;
                 case 3:
                     dx = 0;
                     dy = 1;
                     break;
                 default:
                     dx = 0; dy = 0;
                     break;
             }
            while (! (requestMapInformation( x, y ) == Terrain.EMPTY)) {
                x += dx;
                y += dy;
                if (myManager.outsideBoundaries(x,y)) 
                    return;
            }
            if (!myManager.outsideBoundaries(x,y)) {
                sendMapRequest( x, y, Terrain.SOLID );
                decreaseTokens( 1 );
            }
            x = basePosition.x;
            y = basePosition.y;
         }
 
         
 
     }
 
     private void moveToRandomPosition() {
         moveTo((int) (Math.random() * askForMapWidth()), (int)(Math.random() * askForMapHeight()));
     }
 
 
 
 
 
 
 
 }
