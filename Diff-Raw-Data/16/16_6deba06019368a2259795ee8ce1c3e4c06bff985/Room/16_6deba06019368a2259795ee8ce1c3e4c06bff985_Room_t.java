 package Logic;
 
 public class Room {
 
 	private String name;
 	private int capasity;
 	private boolean available;
 	
 	public Room(String name, int capasity) {
 		this.name = name;
 		this.capasity = capasity;
 		available = true;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getCapasity() {
 		return capasity;
 	}
 
 	public void setCapasity(int capasity) {
 		this.capasity = capasity;
 	}
 	
 	public boolean isAvailable(){
 		return available;
 	}
 	
 	public void setAvailable(boolean available){
 		this.available = available;
 	}
 	public static Room toRoom(String arg){
 		String[] args = arg.split("-");
 		String name = args[0];
 		int capasity = Integer.parseInt(args[1]);
 		Room room = new Room(name, capasity);
 		return room;
 	}
 }
