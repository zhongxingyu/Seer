 import java.awt.Dimension;
 import java.awt.Point;
 
 
 public class game {
 	
 	public static player player = new player(20,20);
 	
 	private static Point playerPos;
 	private static int posX, posY;
 	public game(){
 		new map(new Dimension(100,100));
 		map.dig(player.position);
 		
 	}
 	
 	
 	public static void refresh(){
 		playerPos = player.getPosition();
 		posX= Math.max(0, Math.min(map.size.width-Window.panel.size.width,playerPos.x-Window.panel.size.width/2));
 		posY= Math.max(0, Math.min(map.size.height-Window.panel.size.height,playerPos.y-Window.panel.size.height/2));
 		Window.panel.refresh(new Point(posX,posY), playerPos);
 	}
 	
 	
 	public static void main(String[] args){
 		Dimension size = new Dimension(630, 630);
 		new game();
 		new Window(size);
 		Window.panel.timer.start();
 	}
 	
 	public static void playerMove(player p,int dx, int dy ){
 		tile lastTile = map.dig(new Point(p.position.x+dx, p.position.y+dy));
 		if(lastTile!=null){
 			p.move(dx, dy, -1* lastTile.getFuelCost(), lastTile.getValue());
 			try {
 				Thread.sleep(100*lastTile.getValue());
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			game.refresh();
 		}
 	}
 }
