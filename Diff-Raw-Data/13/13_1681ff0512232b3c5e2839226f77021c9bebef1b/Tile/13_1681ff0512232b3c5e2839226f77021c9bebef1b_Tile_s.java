 
 public class Tile implements ID{
 	private long id;
 	
 	public Tile() {
 		setID(0);
 	}
 	public Tile(long i) {
		setID(1);
 	}
 	
 	public long getID() {
 		return id;
 	}
 
 	public void setID(long id) {
 		this.id = id;
 	}
 	
 	public String toString() {
 		return (new Long(id)).toString();
 		
 	}
 
 }
