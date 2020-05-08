 import org.newdawn.slick.geom.*;
 import org.newdawn.slick.*;
 
 public class CommandBox {
 	
 	// original position
 	int x;
 	int y;
 	
 	// current position
 	int cur_x;
 	int cur_y;
 	
 	// java rectangle
 	Color rect_color;
 	Color text_color;
 	static int width = 120;
 	static int height = 25;
 	Rectangle rect;
 	//ShapeRenderer renderer;
 	
 	// command string
 	String str;
 	
 	public CommandBox(int x, int y, String str) {
 		this.x = x;
 		this.y = y;
 		this.cur_x = x;
 		this.cur_y = y;
 		this.str = str;
 		rect_color = Color.green;
 		text_color = Color.black;
 		
 		rect = new Rectangle(x, y, width, height);
 		//renderer = new ShapeRenderer();
 	}
 	                     
 	public void render() {
 		
 		// draw rectangle
 		rect.setX(cur_x);
 		rect.setY(cur_y);
 		ShapeRenderer.fill(rect);
 	}
 	                  
 
 }
