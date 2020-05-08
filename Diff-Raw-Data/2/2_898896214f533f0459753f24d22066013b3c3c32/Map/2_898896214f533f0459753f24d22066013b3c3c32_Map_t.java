 package halfzero;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 
 import static halfzero.util.Functions.*;
 
 public class Map
 {
 	private final int TILE_HEIGHT = 38, TILE_WIDTH = 76;
 	private final int MAP_LENGTH, MAP_WIDTH; 
         private final int FUDGE = 3;
 	private int offsetX = 0, offsetY = 0;
         private final int[] boundX = {0,0}, boundY = {0,0};
         private Tile center;
 	private float zoomFactor = 1; 
 	private float centerX, centerY;
 	private java.util.Map<int[], Tile> tileMap = new HashMap();
         private Grid<Tile> map;
 	
 	public Map(final int _MAP_LENGTH, final int _MAP_WIDTH)
 	{
 		MAP_LENGTH = _MAP_LENGTH;
 		MAP_WIDTH = _MAP_WIDTH;
 		
 		centerX = Display.getWidth()/2 - TILE_WIDTH*(MAP_LENGTH+MAP_WIDTH)/4;
 		centerY = Display.getHeight()/2 - TILE_HEIGHT/2 - TILE_HEIGHT*(MAP_LENGTH - MAP_WIDTH)/4;
                 
                 map = new HashGrid<Tile>(MAP_LENGTH, MAP_WIDTH);
 		
 		for(int i = 0; i < MAP_WIDTH; i++)
 		{	
 			for(int j = MAP_LENGTH - 1; j >= 0; j--)
 			{
 				int x = ((j+i)*TILE_WIDTH/2);
 				int y = ((j-i)*TILE_HEIGHT/2);
 							
 				float[] colors = {(float)Math.random(), (float)Math.random(), (float)Math.random()};
 				//tileMap.put(new int[]{x, y}, new Tile(x, y, TILE_WIDTH, TILE_HEIGHT, colors));
                                 map.set(i, j, new Tile(x, y, i, j, TILE_WIDTH, TILE_HEIGHT, colors));
 			}
 		}
                 
                 Iterator<Tile> i = map.iterator();
                 while(i.hasNext())
                     i.next().updatePoints();
                 
                 findBounds();
 	}
 	
 	public void zoomMap(int delta)
 	{
 		zoomFactor = Math.max(Math.min(zoomFactor+0.001f*delta, 3f), 0.35f);
 		centerX = Display.getWidth()/2 - zoomFactor*(TILE_WIDTH*(MAP_LENGTH+MAP_WIDTH)/4 + offsetX);
 		centerY = Display.getHeight()/2 - zoomFactor*(TILE_HEIGHT/2 + TILE_HEIGHT*(MAP_LENGTH - MAP_WIDTH)/4 + offsetY);
                 findBounds();
         }
 	
 	public void moveMap(final float moveX, final float moveY)
 	{
 		if((moveX == 0)&&(moveY == 0)) return;
 		
 		offsetX -= moveX;
 		offsetY -= moveY;
 		
 		centerX = Display.getWidth()/2 - zoomFactor*(TILE_WIDTH*(MAP_LENGTH+MAP_WIDTH)/4 + offsetX);
 		centerY = Display.getHeight()/2 - zoomFactor*(TILE_HEIGHT/2 + TILE_HEIGHT*(MAP_LENGTH - MAP_WIDTH)/4 + offsetY);
 
                 findBounds();
 	}
         
         private void findBounds() {
             if(center == null) return;
             int x=-1,y=-1, z = -1,w=-1, t=-1;
             try {
                 for (nil((x = center.i) + (t = center.j)); map.get(x, t).isOnscreenLegacy(); x++);
                 for (nil((t = center.i) + (y = center.j)); map.get(t, y).isOnscreenLegacy(); y++);
            } catch (ArrayIndexOutOfBoundsException e) {}
             x += FUDGE; y += FUDGE;
             boundX[1] = x; boundY[1] = y;
             boundX[0] = 2*center.i - x; boundY[0] = 2*center.j - y;
         }
 	
 	public void renderMap()
 	{	
 		Iterator<Tile> i = map.iterator();
 		while(i.hasNext()){
 			Tile t = i.next();
 			if(t.isOnscreen())
 			{
                             t.updatePoints();
                             t.renderTile();
 			}
 		}            
 	}
 
 	public void renderCrosshair()
 	{
 		GL11.glColor3f(0.5f, 0.5f, 1.0f);
 		GL11.glPushMatrix();
 			GL11.glBegin(GL11.GL_LINES);
 			GL11.glVertex2i(0, Display.getHeight()/2);
 			GL11.glVertex2i(Display.getWidth(), Display.getHeight()/2);
 			GL11.glEnd();
 		GL11.glPopMatrix();
 		
 		GL11.glPushMatrix();
 			GL11.glBegin(GL11.GL_LINES);
 			GL11.glVertex2i(Display.getWidth()/2, 0);
 			GL11.glVertex2i(Display.getWidth()/2, Display.getHeight());
 			GL11.glEnd();
 		GL11.glPopMatrix();
 	}
 	
 	public class Tile implements java.io.Serializable
 	{
 		private final int x1, x2, x3, x4;
 		private final int y1, y2, y3, y4;
                 private final float[] xs = {0,0,0,0}, ys = {0,0,0,0};
 		public final int x, y, h, w;
                 public final int i, j;
 		private float red = 0, green = 0, blue = 0;
                 private boolean onscreen = false;
 		
 		public Tile(final int _x, final int _y, final int _i, final int _j, final int _w, final int _h, final float[] colors)
 		{
 			x = _x; y = _y; h = _h; w = _w;
                         i = _i; j = _j;
                         
 			x1 = x; 
 			y1 = y+_h/2;			
 			x2 = x+w/2; 
 			y2 = y;
 			x3 = x; 
 			y3 = y-h/2;
 			x4 = x-w/2; 
 			y4 = y;
 			
 			red = colors[0]; green = colors[1]; blue = colors[2];
 		}
                 
                 public boolean isOnscreen() {
                     //return isOnscreenLegacy();
                     return isOnscreenNew();
                 }
 		
 		public boolean isOnscreenLegacy()
 		{
                     return (((x*zoomFactor + centerX)>-TILE_WIDTH*zoomFactor/2)
                             &&((x*zoomFactor + centerX)< Display.getWidth()+TILE_WIDTH*zoomFactor/2)
                             &&((y*zoomFactor + centerY) < Display.getHeight()+TILE_HEIGHT*zoomFactor/2)
                             &&((x1*zoomFactor + centerY) > -TILE_HEIGHT*zoomFactor/2));
 		}
                 
                 public boolean isOnscreenNew() {
                     return i >= boundX[0] && j >= boundY[0]
                         && i <= boundX[1] && j <= boundY[1];
                 }
                 
                 public void updatePoints () {
                     xs[0] = x1 * zoomFactor + centerX;
                     ys[0] = y1 * zoomFactor + centerY;
                     xs[1] = x2 * zoomFactor + centerX;
                     ys[1] = y2 * zoomFactor + centerY;
                     xs[2] = x3 * zoomFactor + centerX;
                     ys[2] = y3 * zoomFactor + centerY;
                     xs[3] = x4 * zoomFactor + centerX;
                     ys[3] = y4 * zoomFactor + centerY;
                     if(pnpoly(4, xs, ys, Display.getWidth()/2, Display.getHeight()/2))
                         center = this;
                 }
 		
 		public void renderTile()
                 {     
                     if(center == this)
                         GL11.glColor3f(1, 1, 1);
                     else GL11.glColor3f(red,green,blue);
                     
 			GL11.glPushMatrix();
 				GL11.glBegin(GL11.GL_QUADS);
 				GL11.glVertex2f(xs[0], ys[0]);
 				GL11.glVertex2f(xs[1], ys[1]);
 				GL11.glVertex2f(xs[2], ys[2]);
 				GL11.glVertex2f(xs[3], ys[3]);
 				GL11.glEnd();
 			GL11.glPopMatrix();
                         
 		}
 	}
 }
