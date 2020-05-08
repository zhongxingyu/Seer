 package general;
 
 import general.PinType;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Vans
  * Date: 16.04.13
  * Time: 11:44
  */
 
 public class Dip
 {
     //int[] dipNumbers = {14,16,18,24};
     Pin[] pins;
     int x;
     int y;
     String name;
     //TileType[] filledTiles;
 
     public Dip(int pinCount,int startX, int startY)
     {
        pins = new Pin[pinCount + 1];
        
        //totally untested loop to create pins in this dip
        int pinIndex = 0;
        //distance between pins
        int distanceBtwnPins = 1;
        
        Scheme.tiles[startX-1][startY] = TileType.LEFTTOPCORNER;
        Scheme.tiles[startX-1][startY+1]=TileType.VERTICAL;
       Scheme.tiles[startX-1][startY+2]=TileType.VERTICAL;
        Scheme.tiles[startX-1][startY+3] = TileType.LEFTBOTCORNER;
       Scheme.tiles[startX][startY+3] = TileType.HORIZONTAL;
        
        int i=0;
        while (pinIndex!=pinCount/2)
        //for (int i = 0; i < pinCount/2 ; i++)
        {
     	   i++;
            //lower line
     	   x = startX + i;
     	   y = startY + 3;
     	   Scheme.tiles[x][y] = TileType.DOWNPIN;	//should this be moved to Pin's constructor?
     	   pins[ pinIndex ] = new Pin(this,x,y);
     	   pins[ pinIndex ].setType(PinType.DOWN);
     	   pinIndex++;
     	   
     	   for (int j=0;j<distanceBtwnPins;j++)
     	   {
 	    	   i++;
 	    	   x = startX + i;
 	    	   Scheme.tiles[x][y] = TileType.HORIZONTAL;
        	   }
        }
 
       Scheme.tiles[x+1][y] = TileType.RIGHTBOTCORNER;
       Scheme.tiles[x+1][y-1]=TileType.VERTICAL;
       Scheme.tiles[x+1][y-2]=TileType.VERTICAL;
       Scheme.tiles[x+1][y-3] = TileType.RIGHTTOPCORNER;
       Scheme.tiles[x][y-3] = TileType.HORIZONTAL;
        //this draws pins from right to left for insurance 
        //for (int i = pinCount/2 - 1; i >= 0; i--)
         while (pinIndex!=pinCount)
        {
            i--;
            //higher line
     	   x = startX + i;
     	   y = startY;
     	   Scheme.tiles[x][y] = TileType.UPPIN; //should this be moved to Pin's constructor?
     	   pins[ pinIndex ] = new Pin(this,x,y);
     	   pins[ pinIndex ].setType(PinType.UP);
     	   pinIndex++; 
     	   
     	   for (int j=0;j<distanceBtwnPins;j++){   
     	   i--;
     	   x = startX + i;
     	   Scheme.tiles[x][y] = TileType.HORIZONTAL;
     	   }
        }
     }
     
     public Pin getPin(int index)
     {
     	return pins[index];
     }
     
     public void setName(String name)
     {
     	this.name = name;
     	Pin centerPin = pins[(int)(pins.length*0.70)];
     	Scheme.labels[centerPin.getX()][centerPin.getY()+2] = name;   	
     }
 
 }
