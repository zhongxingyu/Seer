 package chronos;
 
 import java.io.Serializable;
 
 public class Room implements Serializable, Comparable<Room> {
 
 	private static final long serialVersionUID = -9094136548925770674L;
 	private String name;
 	private String description;
 	private int capacity;
 
 	public Room(String name, int capacity, String description) {
 		this.name = name;
 		this.capacity = capacity;
 		this.description = description;
 	}
 
 	public int getCapacity() {
 		return capacity;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public int compareTo(Room o) {
 		return capacity - o.getCapacity();
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	@Override
 	public String toString() {
		return (this.name);
 	}
 }
