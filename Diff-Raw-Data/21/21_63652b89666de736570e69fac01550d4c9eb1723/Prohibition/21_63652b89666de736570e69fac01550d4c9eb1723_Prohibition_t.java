 package tuplespace;
 
 import java.util.Date;
 
 import net.jini.core.entry.Entry;
 
 public class Prohibition implements TimeEntry {
 	
 
 	public Prohibition() {
 
 	}
 
 	public String agent;
 	public String prohibition;
 	public String sanction;
 	public Integer clock;
 	public Date time;
 	
 	public Prohibition(String agent, String prohibition, String sanction, Integer clock) {
 		
 		this.agent = agent;
 		this.prohibition = prohibition;
 		this.sanction = sanction;
 		this.clock = clock;
 		this.time = new Date();
 	}
 	
	public Prohibition(String agent2) {
		this.agent = agent;
	}

 	@Override
 	public String toString() {
 		return "Prohibition [agent=" + agent + ", prohibition=" + prohibition
 				+ ", sanction=" + sanction + ", clock=" + clock + ", time="
 				+ time + "]";
 	}
 
 }
