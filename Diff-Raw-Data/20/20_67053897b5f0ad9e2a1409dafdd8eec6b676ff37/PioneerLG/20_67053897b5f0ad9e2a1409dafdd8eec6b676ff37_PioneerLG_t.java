 package robot;
 
 import javaclient3.PlayerException;
 
 final public class PioneerLG extends Pioneer2dx {
 	protected Gripper grip = null;
 
 	public PioneerLG(String name, int port, int id) {
 		super(name, port, id);
 		try {
 			this.laser 	  = new LaserUrg (this.playerclient);
 		} catch (PlayerException e) {
			System.err.println ("PioneerLG: > Error connecting to Player: ");
 			System.err.println ("    [ " + e.toString() + " ]");
 			System.exit (1);
 		}
 		this.playerclient.runThreaded (-1, -1);
 	}
 
 }
