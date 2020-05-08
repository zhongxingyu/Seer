 package testpackage.shared.ship;
 
 import java.io.Serializable;
 
 public class Loser implements Serializable {
	private static final long serialVersionUID = 1L;
 	public String reason;
 	public int playernr;
 	
 	Loser(String reason, int playernr) {
 		this.reason = reason;
 		this.playernr = playernr;
 	}
 	
 	Loser(int playernr) {
 		this.playernr = playernr;
 	}
 	
 	Loser() {}
 }
