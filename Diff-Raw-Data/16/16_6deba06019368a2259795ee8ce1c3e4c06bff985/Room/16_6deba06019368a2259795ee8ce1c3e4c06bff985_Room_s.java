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
<<<<<<< HEAD
 	
 	public boolean isAvailable(){
 		return available;
 	}
=======

>>>>>>> 1d3fc9ba572430c02bf9b4cfb988ebb1ca1cfb02
 	
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
