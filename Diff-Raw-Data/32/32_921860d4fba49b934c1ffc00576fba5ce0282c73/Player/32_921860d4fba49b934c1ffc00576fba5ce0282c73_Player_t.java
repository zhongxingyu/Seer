 package mapobjects;
 
 import imageloader.ImageLoader;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 public class Player extends MoveableObject{
 	private int maxbombs = 3;
 	private int actualbombs = 0;
 	private int lives = 1;
 	private String bombtype = "standart";
 
 	public Player(int x,int y,boolean v,boolean d,boolean c,String p,ImageLoader gr){
 		super(x,y,v,d,c,p,gr);
 	}
 
 	//TODO UseWeapon or LayBomB,ChangeWeapon
 
 	@Override
 	public void move() {
 		if (direction.UP.is()) {
 			if(hasObjectCollision(posX,posY-speed,map.getCollisionMap(),"UP")){}
 			else{posY-=speed;}
 			animation.change("playerUp");
 		}
 
 		if (direction.DOWN.is()) {
 			if(hasObjectCollision(posX,posY+speed,map.getCollisionMap(),"DOWN")){}
 			else{posY+=speed;}
 			animation.change("playerDown");
 		}
 
 		if (direction.LEFT.is()) {
 			if(hasObjectCollision(posX-speed,posY,map.getCollisionMap(),"LEFT")){}
 			else{posX-=speed;}
 			animation.change("playerLeft");
 		}
 
 		if (direction.RIGHT.is()) {
 			if(hasObjectCollision(posX+speed,posY,map.getCollisionMap(),"RIGHT")){}
 			else{posX+=speed;}
 			animation.change("playerRight");
 		}
 	}
 
 	public void layBomb(){
 		if(!reachedMaxBombs()){
 			map.getMapObjects().get(1).add(new Bomb(
 					getPosX(),
 					getPosY(),
 					true,
 					false,
 					false,
 					"simplebomb",
 					map.getGraphics()));
 			addBomb();
 		}
 	}
 
 	@Override
 	public boolean hasObjectCollision(int x, int y,BufferedImage cm,String dir){
 		if(x<0 || y<0 || x>cm.getWidth()-50 || y>cm.getHeight()-50){return true;}		
 		int t=20;
 		int cornercounter = 0;
 		boolean collision = false;
 		boolean upleft = false;
 		boolean upright = false;
 		boolean downleft = false;
 		boolean downright = false;
 		BufferedImage collTest = cm.getSubimage(x, y, 50, 50);
 		for(int i=0; i<collTest.getWidth(); i++){
 			for(int j=0; j<collTest.getHeight(); j++){
 				Color test = new Color(collTest.getRGB(i, j));
 				if (test.equals(Color.yellow)) {
 					map.finishMap();
 				}
 				else if(test.equals(Color.black)){
					if(i<t && j<t){upleft = true;System.out.println("upleft true");}
					else if(i>50-t && j<t){upright = true;System.out.println("upright true");}
					else if(i<t && j>50-t){downleft = true;System.out.println("downleft true");}
					else if(i>50-t && j>50-t){downright = true;System.out.println("downright true");}
 					else{return true;}
				}
			}
		}	
		
 					if(upleft ^ upright ^ downleft ^ downright){
 						if(upleft){
 							if(dir.equals("UP")){
 								if(simpleHasColl(posX+1,posY,cm)){return true;}
 								else{
 									this.posX+=1;
 									return true;
 								}
 							}
 
 							if(dir.equals("LEFT")){
 								if(simpleHasColl(posX,posY+1,cm)){return true;}
 								else{
 									this.posY+=1;
 									return true;
 								}
 							}
 						}
 
 						if(upright){
 							if(dir.equals("UP")){
 								if(simpleHasColl(posX-1,posY,cm)){return true;}
 								else{
 									this.posX-=1;
 									return true;
 								}
 								
 							}
 
 							if(dir.equals("RIGHT")){
 								if(simpleHasColl(posX,posY+1,cm)){return true;}
 								else{
 									this.posY+=1;
 									return true;
 								}
 							}
 						}
 
 						if(downleft){
 							if(dir.equals("DOWN")){
 								if(simpleHasColl(posX+1,posY,cm)){return true;}
 								else{
 									this.posX+=1;
 									return true;
 								}
 							}
 
 							if(dir.equals("LEFT")){
 								if(simpleHasColl(posX,posY-1,cm)){return true;}
 								else{
 									this.posY-=1;
 									return true;
 								}
 							}
 						}
 
 						if(downright){
 							if(dir.equals("DOWN")){
 								if(simpleHasColl(posX-1,posY,cm)){return true;}
 								else{
 									this.posX-=1;
 									return true;
 								}
 							}
 
 							if(dir.equals("RIGHT")){
 								if(simpleHasColl(posX,posY-1,cm)){return true;}
 								else{
 									this.posY-=1;
 									return true;
 								}
 								
 							}
 						}
					
				}//else{return true;}
 
			
		
 		return false;
 	}
 	
 	public boolean simpleHasColl(int x, int y,BufferedImage cm){
 		if(x<0 || y<0 || x>cm.getWidth()-50 || y>cm.getHeight()-50){return true;}		
 		BufferedImage collTest = cm.getSubimage(x, y, 50, 50);
 		for(int i=0; i<collTest.getWidth(); i++){
 			for(int j=0; j<collTest.getHeight(); j++){
 				Color test = new Color(collTest.getRGB(i, j));
 				if (test.equals(Color.black)){return true;}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void update() {
 		animation.animate();
 		move();
 	}
 
 	@Override
 	public void draw(Graphics2D g2d,ImageLoader gr,Graphics2D cm) {	
 		g2d.drawImage(animation.getCurrentImage(),posX,posY,null);
 
 		cm.setPaint(Color.green);
 		cm.fillRect(posX, posY, 50, 50);	
 	}
 
 	public boolean reachedMaxBombs(){
 		return(actualbombs+1>maxbombs);
 	}
 
 	public void addBomb() {
 		actualbombs++;
 	}
 
 	public void removeBomb() {
 		actualbombs--;
 	}
 }
