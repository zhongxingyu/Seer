 import java.awt.*;
 import java.util.ArrayList;
 
 public class Submarine extends Ship{
 	
 	private ArrayList<Missle> bullets;
 	private boolean moving;
 	
 	public Submarine(int x, int y,int color){
 		super(x,y,color);
 		bullets = new ArrayList<Missle>();
 	}
 	
 	public boolean needsRepaint(){
 		if (moving)
 			return true;
 		for(int index = 0; index<bullets.size();index++){
 			if(bullets.get(index).needsRepaint()){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void MouseMoved(int x, int y){
 		for(int index = 0; index<bullets.size(); index++){
 			bullets.get(index).MouseMoved(x,y);
 		}
 	}
 	public void MouseClicked(int x, int y){
		bullets.add(new Missle(this.x,this.y,x,y));
 	}
 	
 	protected void base(Graphics g,int size)
 	{	
 		//x and y are the center of the ship
 		
 		//Body
 		g.setColor(Color.gray.brighter());
 		g.fillOval(x, y-25/2, 100, 25);
 		g.setColor(Color.black);
 		g.drawOval(x, y-25/2, 100, 25);	
 				
 		//Command Center
 		g.setColor(getColor().darker().darker());
 		g.fillOval(x+42, y-6, 15, 15);
 		g.setColor(getColor().darker());
 		g.fillRect(x+49, y-12, 3, 25);
 		
 		for(int index=0; index<bullets.size(); index++){
 			bullets.get(index).drawBullet(g);
 		}
 		for(int index=0; index<bullets.size(); index++){
 			if(bullets.get(index).isdone()){
 				bullets.remove(index);
 				moving = true;
 			}
 		}
 	}
 }
