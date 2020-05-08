 import java.awt.Dimension;
 import java.awt.Point;
 
 
 public class game {
 	
 	public static player player = new player(20,20);
 	
 	private static Point playerPos;
 	private static int posX, posY;
 	public game(){
 		new map(new Dimension(40,40));
 		refresh();
 		map.dig(player.position);
 		Window.panel.timer.start();
 	}
 	
 	
 	
 	
 	public static void refresh(){
 		playerPos = player.getPosition();
 		posX= Math.max(0, Math.min(map.size.width-Window.panel.size.width,playerPos.x-Window.panel.size.width/2));
 		posY= Math.max(0, Math.min(map.size.height-Window.panel.size.height,playerPos.y-Window.panel.size.height/2));
 		Window.panel.refresh(new Point(posX,posY), playerPos);
 	}
 	
 	
 	public static void main(String[] args){
 		Dimension size = new Dimension(630, 630);
 		new Window(size);

 		new game();
 		
 	}
 }
