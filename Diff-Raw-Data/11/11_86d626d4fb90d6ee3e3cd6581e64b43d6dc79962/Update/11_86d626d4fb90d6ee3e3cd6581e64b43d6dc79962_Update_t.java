 import state.World;
 
 
 public class Update extends Thread {
 private final Display display;
 private final World world;
 	public Update(World w, Display d) {
     world = w;
     display = d;
 	}
 public void run(){
	while(true){
 	world.update();
 	display.repaint();
 	try {
 		Thread.sleep(100);
 	} catch (InterruptedException e) {
 		e.printStackTrace();
 	}
 
 }
 }
 }
