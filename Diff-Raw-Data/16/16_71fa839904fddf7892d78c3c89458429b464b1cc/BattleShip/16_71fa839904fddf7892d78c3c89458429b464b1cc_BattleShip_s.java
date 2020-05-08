 import java.awt.*;
 import java.util.*;
 
 public class BattleShip extends Ship {
 	private ArrayList<Turret> guns;
 	private ImageStorage is;
 	public BattleShip(int x, int y,int color){
 		super(x,y,color);
 		guns = new ArrayList<Turret>();
 		is = new ImageStorage();
 		initArray();
 	}	
 	private void initArray(){
 		guns.add(new Turret(x+70,y+25,270,0,size,WEST));
 		guns.add(new Turret(x+35,y+25,270,0,size,WEST));
 		guns.add(new Turret(x+145,y+25,270,0,size,WEST));
 		guns.add(new Turret(x+170,y+25,270,0,size,WEST));
 	}
 	public void addX(int x){
 		super.addX(x);
 		for(int index = 0; index<guns.size(); index++){
 			guns.get(index).addX(x);
 		}
 	}
 	public void addY(int y){
 		super.addY(y);
 		for(int index = 0; index<guns.size(); index++){
 			guns.get(index).addY(y);
 		}
 	}
 	public void MouseMoved(int x, int y){
 		for(int index = 0; index<guns.size(); index++){
 			guns.get(index).MouseMoved(x,y);
 		}
 	}
 	public void MouseClicked(int x, int y){
 		for(int index = 0; index<guns.size(); index++){
 			guns.get(index).MouseClicked(x,y);
 		}
 	}
 	public boolean needsRepaint(){
 		if(super.needsRepaint()){
 			return true;
 		}
 		for(int index = 0; index<guns.size();index++){
 			if(guns.get(index).needsRepaint()){
 				return true;
 			}
 		}
 		return false;
 	}
 	public void drawShip(Graphics g){
 		super.drawShip(g);
 		drawGuns(g);
 	}
 	
 	private void drawGuns(Graphics g){
 		for(int index = 0; index<guns.size(); index++){
 			guns.get(index).drawGun(g);
 		}
 	}
 	
 	protected void base(Graphics g, int size)
 	{	
 		//x and y are the center of the ship
		//g.setColor(getColor());
		//g.fillRect(x+90, y+15, 40, 20);
		//is.drawBattleShip(g,x,y);
		is.drawBattleShip((Graphics2D)g, rotate(45,x,y));
 	}
 }
