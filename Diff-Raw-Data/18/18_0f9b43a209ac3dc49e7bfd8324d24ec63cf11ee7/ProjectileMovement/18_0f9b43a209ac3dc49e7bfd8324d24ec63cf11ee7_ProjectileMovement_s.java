 package qp.main.threads;
 
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 
 import qp.main.entity.Enemy;
 import qp.main.entity.Projectile;
 import qp.main.gui.frame.Frame;
 
 public class ProjectileMovement implements Runnable{
 	private Projectile p;
 	public ProjectileMovement(Projectile p){
 		this.p = p;
 	}
 	public void run(){
 		try{
 			while(p.exists){
 				if(!(p.x - p.width > 0 && p.x< Frame.WIDTH && p.y + p.height < Frame.HEIGHT && p.y > 0 )){
 					p.remove();
 					p.exists = false;
 				}
 				try{
 					if(!Enemy.getEnemies().isEmpty()){
 					for(Enemy e: (ArrayList<Enemy>) Enemy.getEnemies().clone()){
 						if(e.intersects(p.x,p.y,p.width,p.height)){
							e.remove();
 							p.remove();
 						}
 					}
 					}
 				}catch(Exception e){
					if (e instanceof NullPointerException || e instanceof ConcurrentModificationException)
 						break;
 					e.printStackTrace();
 				}
 				Thread.sleep(2);
 				p.move();
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 }
