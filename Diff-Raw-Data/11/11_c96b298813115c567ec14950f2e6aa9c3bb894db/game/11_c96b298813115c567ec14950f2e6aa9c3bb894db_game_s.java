 import java.awt.Dimension;
 import java.awt.Point;
 
 
 public class game {
 	
 	public static player player = new player(20,20);
 	
 	private static Point playerPos;
 	private static int posX, posY;
 	public game(){
 		new map(new Dimension(100,100));
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
 	public static void player_move(player p,int dx, int dy ){
 		int fuel_move = -1;
 		int fuel_dig = -2;
 		if(p.position.x +dx < 0 || p.position.x +dx >= map.size.width){
 			dx = 0;
 		}
 		if(p.position.y +dy < 0 || p.position.y + dy >= map.size.height){
 			dy=0;
 		}
 		if((dy!=0) || (dx!=0)){
 			int delta_fuel = 0;
 			int delta_money = 0;
 			int last_tile = map.dig(new Point(p.position.x+dx, p.position.y+dy));
 			if(last_tile == 0){
 				delta_fuel = fuel_move;
 			} else {
 				delta_money = last_tile - 1;
 				delta_fuel = fuel_dig;
 			}
 			p.move(dx, dy, delta_fuel, delta_money);
 			try {
 				Thread.sleep(100*last_tile);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			game.refresh();
 		}
 	}
 }
