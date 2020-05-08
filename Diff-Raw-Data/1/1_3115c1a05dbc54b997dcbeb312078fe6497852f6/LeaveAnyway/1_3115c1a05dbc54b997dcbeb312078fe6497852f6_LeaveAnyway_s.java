 import java.util.TimerTask;
 
 
 /**
  * @author Alexander Nigl
  */
 public class LeaveAnyway extends TimerTask {
 	
 	
 	private Peer peer;
 	
 	
 	/**
 	 * Create a new instance of LeaveAnyway.
 	 */
 	public LeaveAnyway(Peer peer) {
 		this.peer = peer;
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public void run() {
 		System.out.println("Bin weg!");
 		this.peer.stop();
 	}
 	
 }
