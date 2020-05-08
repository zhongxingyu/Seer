 package games;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Color;
 public class Player extends Rectangle{
 	public int xVel=5;
 	public int yVel=5;
 	public int x=450;
 	public int y=320;
 	int[] coords;
 	Input input;
 	public Player(){
 		super(50, 50, 50, 50);
 		coords = new int[4];
 	}
 
 	public void update(Input input){
 		coords[0] = x-25;
 		coords[1] = x+25;
 		coords[2] = y-25;
 		coords[3] = y+25;
 		if(input.isKeyDown(Input.KEY_RIGHT)){
 			x+=xVel;
 
 		}
 		if(input.isKeyDown(Input.KEY_LEFT)){
 			x-=xVel;
 		}
 		setLocation(x,y);
 
 	}
 	public int getxVel() {
 		return xVel;
 	}
 	public void setxVel(int xVel) {
 		this.xVel = xVel;
 	}
 	public int getPosition() {
 		return (x);
 	}
 	public void setPosition(int x) {
 		this.x = x;
 	}
 	public boolean checkCollision(Block obj){	
 		if(coords[0] < obj.coords[0] && coords[1] > obj.coords[0]){
 			//System.out.println("x-hit");
 			if(coords[2] > obj.coords[2]){
 				//System.out.print("1");
 				if(coords[3]-25 < obj.coords[3]+25){
					System.out.print("hit");
 					return true;
 				}
 			}
 		}
 		//System.out.println(coords[2]+" "+obj.coords[2]);
 		return false;
 	}
 }
