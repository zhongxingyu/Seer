 package de.haw.tt2p.trafficcoordination.topology;
 
 import java.util.List;
 import java.util.Random;
 
 import com.gigaspaces.annotation.pojo.SpaceId;
 
 /**
  * Roxels are the grid components of the map. They have specific types/functionality and may contain
  * a car.
  */
 public class Roxel {
 
 	public enum Type {
 		Street, House;
 	}
 
 	private Integer id;
 	private Integer x;
 	private Integer y;
 	private Integer currentCarId;
 	private Type type;
 	private List<Integer> nextRoxels;
 	private List<Integer> previousRoxels;
 
 	/**
 	 * Necessary Default constructor.
 	 */
 	public Roxel() {
 	}
 
 	public Roxel(Integer id, Integer x, Integer y, Type type, List<Integer> nextRoxels, List<Integer> previousRoxels) {
 		this.id = id;
 		this.x = x;
 		this.y = y;
 		this.type = type;
 		this.nextRoxels = nextRoxels;
 		this.previousRoxels = previousRoxels;
 	}
 
 	@SpaceId
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public Integer getX() {
 		return x;
 	}
 
 	public void setX(Integer x) {
 		this.x = x;
 	}
 
 	public Integer getY() {
 		return y;
 	}
 
 	public void setY(Integer y) {
 		this.y = y;
 	}
 
 	public List<Integer> getNextRoxels() {
 		return nextRoxels;
 	}
 
 	public void setNextRoxels(List<Integer> nextRoxels) {
 		this.nextRoxels = nextRoxels;
 	}
 
	public List<Integer> getPreviousRoxels() {
 		return previousRoxels;
 	}
 
 	public void setPreviousRoxels(List<Integer> previousRoxels) {
 		this.previousRoxels = previousRoxels;
 	}
 
 	public Type getType() {
 		return type;
 	}
 
 	public void setType(Type type) {
 		this.type = type;
 	}
 
 	public Integer getCurrentCarId() {
 		return currentCarId;
 	}
 
 	public Boolean hasCar() {
 		return currentCarId != null;
 	}
 
 	public void removeCar() {
 		currentCarId = null;
 	}
 
 	public void setCurrentCarId(Integer currentCarId) {
 		this.currentCarId = currentCarId;
 	}
 
 	public Integer getNextRoxelId() {
 		return nextRoxels.get(new Random().nextInt(nextRoxels.size()));
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Roxel(%s->%s->%s)", previousRoxels, id, nextRoxels);
 	}
 
 }
