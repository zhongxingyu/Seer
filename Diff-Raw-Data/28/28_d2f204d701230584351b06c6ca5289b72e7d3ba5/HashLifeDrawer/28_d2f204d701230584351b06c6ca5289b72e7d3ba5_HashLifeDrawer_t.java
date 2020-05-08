 package life.hashlife;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import life.LifeDrawer;
 import life.LifeState;
 
 public class HashLifeDrawer implements LifeDrawer {
 	
 	@Override
 	public void draw(int x, int y, int zoom, LifeState state, BufferedImage b) {
 		MacroCell cell = ((HashLifeState) state).state;
 		int realSize = cell.size;
 		if(zoom < 0)
 			realSize >>= -zoom;
 		else
 			realSize <<= zoom;
 		
		x -= realSize/2;
		y -= realSize/2;

		Graphics g = b.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, b.getWidth(), b.getHeight());
		System.out.println(cell.dim);
		recDraw(b, g, x, y, zoom, cell);
 	}
 	
 	private void recDraw(BufferedImage b, Graphics g, int x, int y, int zoom, MacroCell cell) {
 
 		int size = cell.size;
 		int realSize = size;
 		if(zoom < 0)
 			realSize >>= -zoom;
 		else
 			realSize <<= zoom;
 		
 		int w = b.getWidth(), h = b.getHeight();
		if(x + realSize <= 0 || x >= w || y + realSize <= 0 || y >= h){
 			return;
 		}
 
 		if(cell.off) {
 			return;
 		}
 		
 		if(cell.dim == 0){
			g.setColor(Color.white);
 			g.fillRect(x, y, 1<<zoom, 1<<zoom);
 			return;
 		}
 		
 		if(cell.dim == -zoom) {
			int color = cell.density;
			int rgb = (new Color(color, color, color).getRGB());
 			b.setRGB(x, y, rgb);
 			return;
 		}
 		
 		int offset = realSize/2;
 		recDraw(b, g, x         , y         , zoom, cell.quad(0));
 		recDraw(b, g, x + offset, y         , zoom, cell.quad(1));
 		recDraw(b, g, x         , y + offset, zoom, cell.quad(2));
 		recDraw(b, g, x + offset, y + offset, zoom, cell.quad(3));
 
 	}
 }
