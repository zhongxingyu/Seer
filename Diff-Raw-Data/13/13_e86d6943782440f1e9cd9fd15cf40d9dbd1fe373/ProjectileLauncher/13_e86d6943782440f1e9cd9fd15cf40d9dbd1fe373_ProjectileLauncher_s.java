 package qp.main.threads;
 
 import java.awt.event.KeyEvent;
 import java.util.Random;
 
 import qp.main.entity.Player;
 import qp.main.entity.Projectile;
 import qp.main.gui.frame.Frame;
 import qp.main.listeners.KListener;
 
 public class ProjectileLauncher implements Runnable{
 	double speed = 2.0;
 	double recoilReduction = 5.0;
 	Random rand = new Random();
 	int maj = 10, min = 5;
 	public void run(){
 		try{
 			while(true){
 				if(KListener.keys[KeyEvent.VK_UP]){
					new Projectile(Player.x + Player.width/2 - min/2,Player.y + Player.height/2 - maj/2,min,maj,rand.nextDouble()/recoilReduction,-speed);
 				}else if(KListener.keys[KeyEvent.VK_DOWN]){
					new Projectile(Player.x + Player.width/2 - min/2,Player.y + Player.height/2 - maj/2,min,maj,rand.nextDouble()/recoilReduction,speed);
 				}else if(KListener.keys[KeyEvent.VK_LEFT]){
					new Projectile(Player.x + Player.width/2 - maj/2,Player.y + Player.height/2 - min/2,maj,min,-speed,rand.nextDouble()/recoilReduction);
 				}else if(KListener.keys[KeyEvent.VK_RIGHT]){
					new Projectile(Player.x + Player.width/2 - maj/2,Player.y + Player.height/2 - min/2,maj,min,speed,rand.nextDouble()/recoilReduction);
 				}
 				Thread.sleep(100);
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 }
