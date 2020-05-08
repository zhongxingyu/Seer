 import java.awt.Image;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 
 public class Trash{
    
     private Image trashPic;
     Util.TrashType type;
     private double x, y,speed;
     int curPath;
     private boolean beenKilled=false;
 
     
     Trash(int initX, int initY, double trashSpeed, Util.TrashType type){
 	
 	if(type==Util.TrashType.paper){
 	    ImageIcon ii = new ImageIcon(this.getClass().getResource("pics/Trash/paper.png"));
 	    trashPic = ii.getImage();
 	}else if(type==Util.TrashType.plastic){
 	    ImageIcon ii = new ImageIcon(this.getClass().getResource("pics/Trash/plastic.png"));
 	    trashPic = ii.getImage();
 	}else if(type==Util.TrashType.aluminum){
 	    ImageIcon ii = new ImageIcon(this.getClass().getResource("pics/Trash/aluminum.png"));
 	    trashPic = ii.getImage();
 	}else if(type==Util.TrashType.metal){
 		    ImageIcon ii = new ImageIcon(this.getClass().getResource("pics/Trash/metal.png"));
 		    trashPic = ii.getImage();
 	}else if(type==Util.TrashType.nuclear){
 	    ImageIcon ii = new ImageIcon(this.getClass().getResource("pics/Trash/nuclear.png"));
 	    trashPic = ii.getImage();
 	}else{
 	    ImageIcon ii = new ImageIcon(this.getClass().getResource("pics/Trash/paper.png"));
 	    trashPic = ii.getImage();
 	}
 	 this.type=type;
 	 x=initX;
 	 y=initY;
 	 speed=trashSpeed;
     }
     
     public void removeImage(){
 	trashPic=null;
     }
     
     public Image getImage(){
 	return trashPic;
     }
     
     public Util.TrashType getType(){
 	return type;
     }
     
     public double getX(){
 	return x;
     }
     
     public double getY(){
 	return y;
     }
     
     public void setType(Util.TrashType newType){
 	this.type=newType;
     }
     
     public void followPath(ArrayList<Integer> X, ArrayList<Integer> Y){
 	
 	
 	double distX, distY,angle;
 	
 	distX=X.get(curPath)-x;
 	distY=Y.get(curPath)-y;
 	
 	if(Math.abs(distX)+Math.abs(distY)<speed){
 	    curPath++;
 	    if(curPath>=X.size()){
 		curPath=0;
 	    }
 	}
 	
 	angle=Math.atan2(distY, distX);
 	x=x+speed*Math.cos(angle);
 	y=y+speed*Math.sin(angle);
 	
 	
     }
     
 private boolean willTowerFire(Tower tower){
         
         if(tower.getType()==Util.TowerType.compost){
             if(this.type==Util.TrashType.food){
                 return true;
             }
         }else if(tower.getType()==Util.TowerType.incenerator){
             return true;
         }else if(tower.getType()==Util.TowerType.metal){
             if(this.type==Util.TrashType.metal){
                 return true;
             }
         }
         else if(tower.getType()==Util.TowerType.nuclear){
             if(this.type==Util.TrashType.nuclear){
                 return true;
             }
         }
         else if(tower.getType()==Util.TowerType.recycle){
             if(this.type==Util.TrashType.paper || this.type==Util.TrashType.plastic){
                 return true;
             }
         }
         
         return false;
     }
     
     public boolean detectCollisions(Tower tower, ArrayList<Integer> X, ArrayList<Integer> Y){
            
            double distX, distY,angle;
            int tempCurPath = curPath;
            double tempX = x;
            double tempY = y;
            
            if(willTowerFire(tower)){
                
                for(int i=0; i<Util.pathWidth/tower.getRate(); i++){
                    distX=X.get(tempCurPath)-tempX;
                    distY=Y.get(tempCurPath)-tempY;
 
                    if(Math.abs(distX)+Math.abs(distY)<speed){
                        tempCurPath++;
                        if(tempCurPath>=X.size()){
                            tempCurPath=0;
                        }
                    }
                    angle=Math.atan2(distY, distX);
                    tempX=tempX+speed*Math.round(Math.cos(angle));
                    tempY=tempY+speed*Math.round(Math.sin(angle));
                }
               //if any 4 corners of trash inside 4 corners of range: FIRE!
               //Tower Coords:
               //(x,y) = (curTower.getX, curTower.getY()+2*curTower.getMidY())
               //(x,y) = (curTower.getX, curTower.getY()+2*curTower.getMidY()+curTower.getRange())
               //(x,y) = (curTower.getX + curTower.getWidth, curTower.getY()+2*curTower.getMidY())
               //(x,y) = (curTower.getX + curTower.getWidth, curTower.getY()+2*curTower.getMidY()+curTower.getRange())
               //Trash Coords:
               //(x,y) = (x,y)
               //(x,y) = (x + trashPic.getWidth(null),y)
               //(x,y) = (x,y + trashPic.getHeight(null))
               //(x,y) = (x + trashPic.getWidth(null), y + trashPic.getHeight(null);
 
                if(tower.getDirection().equalsIgnoreCase("South")){
                    if(tempX+trashPic.getWidth(null)/2>=tower.getX()+tower.getWidth()/2-Math.ceil(speed/2) && tempX+trashPic.getWidth(null)/2<=tower.getX() + tower.getWidth()/2+Math.ceil(speed/2)){
                        if((tempY>=tower.getY()+tower.getBaseImage().getHeight(null) && tempY<=tower.getY()+tower.getBaseImage().getHeight(null)+tower.getRange())){
                            return true;
                        }
                    }
                }else if(tower.getDirection().equalsIgnoreCase("East")){
                    if(tempY+ trashPic.getHeight(null)/2>=tower.getY()+tower.getHeight()/2-Math.ceil(speed/2) && tempY+ trashPic.getHeight(null)/2<=tower.getY()+tower.getHeight()/2+Math.ceil(speed/2)){
                        if((tempX>=tower.getX()+tower.getBaseImage().getWidth(null) && tempX<=tower.getX()+tower.getBaseImage().getWidth(null)+tower.getRange())){
                            return true;
                        }
                    }
                }else if(tower.getDirection().equalsIgnoreCase("North")){
                    if((tempX+trashPic.getWidth(null)/2>=tower.getX()+tower.getWidth()/2-Math.ceil(speed/2) && tempX+trashPic.getWidth(null)/2<=tower.getX() + tower.getWidth()/2+Math.ceil(speed/2))){
                        if((tempY+ trashPic.getHeight(null)<=tower.getY() && tempY+ trashPic.getHeight(null)>=tower.getY()-tower.getRange())){
                            return true;
                        }
                    }
                }else if(tower.getDirection().equalsIgnoreCase("West")){
                    if((tempY+ trashPic.getHeight(null)/2>=tower.getY()+tower.getHeight()/2-Math.ceil(speed/2) && tempY+ trashPic.getHeight(null)/2<=tower.getY()+tower.getHeight()/2+Math.ceil(speed/2))){
                        if((tempX+ trashPic.getWidth(null)<=tower.getX() && tempX+ trashPic.getWidth(null)>=tower.getX()-tower.getRange())){
                            return true;
                        }
                    }
                }
            }
            return false;
        }
 
     public boolean isKilled(){
 	return beenKilled;
     }
     
     public void setKilled(){
 	beenKilled=true;
     }
 }
