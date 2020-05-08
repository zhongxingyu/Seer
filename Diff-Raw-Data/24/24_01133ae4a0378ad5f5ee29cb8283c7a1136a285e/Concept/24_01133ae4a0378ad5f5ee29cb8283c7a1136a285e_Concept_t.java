 package concept;
 
 import java.awt.Color;
 
 import br.com.etyllica.core.video.Graphic;
 
 public class Concept extends Fountain{
 
 	protected boolean onMouse = false;
 	
 	protected int mapX = 0;
 	protected int mapY = 0;
 	
 	public Concept(int x, int y) {
 		super(x, y);
 	}
 
 	protected Color colorFill = Color.GREEN;
 	protected Color colorDraw = Color.BLACK;
 	
 	public void draw(Graphic g){
 		g.setColor(colorFill);
 		g.fillRect(x+mapX*TILE_SIZE, y+mapY*TILE_SIZE, TILE_SIZE, TILE_SIZE);
 		
 		g.setColor(colorDraw);
 		g.drawRect(x+mapX*TILE_SIZE, y+mapY*TILE_SIZE, TILE_SIZE, TILE_SIZE);
 	}
 	
 	@Override
 	public boolean onMouse(int mx, int my) {
 
 		if(mx + 1 < x+mapX*TILE_SIZE)	return false;
		if(mx > x+mapX*TILE_SIZE + xTile)	return false;
 
 		if(my + 1 < y+mapY*TILE_SIZE)	return false;
		if(my > y+mapY*TILE_SIZE + yTile)	return false;
 
 		return true;
 
 	}
 
 	public boolean isOnMouse() {
 		return onMouse;
 	}
 
 	public void setOnMouse(boolean onMouse) {
 				
 		this.onMouse = onMouse;
 
 		if(!onMouse){
 			colorDraw = Color.BLACK;	
 		}else{			
 			colorDraw = Color.WHITE;
 		}
 		
 	}
 
 	public void setMapX(int mapX) {
 		this.mapX = mapX;
 	}
 
 	public void setMapY(int mapY) {
 		this.mapY = mapY;
 	}
 	
 }
