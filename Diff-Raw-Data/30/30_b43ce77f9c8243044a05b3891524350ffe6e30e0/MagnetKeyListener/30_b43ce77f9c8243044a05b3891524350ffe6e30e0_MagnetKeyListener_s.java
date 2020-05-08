 package trollhoehle.gamejam.magnets;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Circle;
 
 public class MagnetKeyListener implements KeyListener {
 
 	private ArrayList<Player> players;
 
 	public MagnetKeyListener(ArrayList<Player> players) {
 		this.players = players;
 	}
 
 	public void inputEnded() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void inputStarted() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public boolean isAcceptingInput() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	public void setInput(Input arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void keyPressed(int arg0, char arg1) {
 		// TODO Auto-generated method stub
 		System.out.println("Key " + arg0);
 		boolean found = false;
 		for (Player player : this.players) {
 			if (player.getButton() == arg0) {
 				found = true;
 			}
 		}
 
 		if (!found) {
			try {
				this.players.add(new Player(200, 200, new Circle(200, 200, 20), new Image(
						"res/images/magnet_inactive.png"), 5, 0.15f, "Trollspieler " + arg1, arg0));
			} catch (SlickException e) {

 			}
 		}
 	}
 
 	public void keyReleased(int arg0, char arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
