 package server;
 
 import java.net.SocketAddress;
 
 public class FireMissileCommand extends AbstractCommand {
 		
 	SocketAddress sender;
 	
 	public FireMissileCommand(SocketAddress sender, long t) {
 		super(t);
 		this.sender = sender;
 	}
 
 	public void perform(Game g, float delta) {
 		g.fireMissile(sender);
 	}
 }
