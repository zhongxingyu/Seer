 import java.awt.Point;
 
 
 
 public class player {
 	Point position;
 	
 	int hp = 10, fuel = 100, money = 0;
 	int fuel_drill=2;
 	int fuel_move=1;
 	public player(int x, int y){
 		position = new Point(x, y);
 	}
 	
 	public void move(int dx, int dy){
 		if(position.x +dx < 0 || position.x +dx >= map.size.width){
 			dx = 0;
 		}
 		if(position.y +dy < 0 || position.y + dy >= map.size.height){
 			dy=0;
 		}
		if((dy!=0) || (dx!=0)){
 			
 			position.translate(dx, dy);
 			int last_tile = map.dig(position);
 			if(last_tile!=0){
 				fuel-= fuel_drill;
 			}else {
 			
 				fuel -= fuel_move;
 			}
 			try {
 				Thread.sleep(100*last_tile);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		
 			game.refresh();
 			System.out.println(fuel);
 		}
 	}
 
 	public Point getPosition() {
 		return position;
 	}
 
 	public void setPosition(Point position) {
 		this.position = position;
 	}
 
 	public int getHp() {
 		return hp;
 	}
 
 	public int getFuel() {
 		return fuel;
 	}
 
 	public int getMoney() {
 		return money;
 	}
 	
 	
 }
