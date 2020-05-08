 package packet;
 
public class AliveRequest extends BasicPacket {
 	private int id;
 	
 	public AliveRequest() {
 		super();		
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 }
