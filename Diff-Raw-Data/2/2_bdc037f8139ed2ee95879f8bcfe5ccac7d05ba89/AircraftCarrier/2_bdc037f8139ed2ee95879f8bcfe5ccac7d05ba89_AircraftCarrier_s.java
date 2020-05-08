 import java.awt.*;
 
 public class AircraftCarrier extends Ship{
 	
 	public AircraftCarrier(int x, int y,int color){
 		super(x,y,color);
 	}
 	
 	protected void base(Graphics g,int size)
 	{	
 		//x and y are the center of the ship
 		
 		//Upper Deck
 		g.setColor(Color.gray.brighter());
 		int xarray[] = {x,x+50,x+125,x+250,x+300,x+300,x,x};
 		int yarray[] = {y-10,y-10,y-25,y-25,y-10,y+25,y+25,y-10};
 		g.fillPolygon(xarray,yarray,8);
 		g.setColor(Color.black);
 		g.drawPolygon(xarray,yarray,8);
 		
 		//lines
 		g.setColor(Color.white);
 		for(int q = x+5; q <= x+295; q += 25)
 			g.fillRect(q,y+5,18,3);
 		
 		//Command Center
 		g.setColor(getColor().darker());
 		g.fillRect(x+235,y-23,15,15);
 		g.setColor(getColor());
		g.fillRect(x+240,y-25,2,20);		
 	}
 
 	public void MouseMoved(int x, int y) {	
 	}
 
 	public void MouseClicked(int x, int y) {
 	}
 }
