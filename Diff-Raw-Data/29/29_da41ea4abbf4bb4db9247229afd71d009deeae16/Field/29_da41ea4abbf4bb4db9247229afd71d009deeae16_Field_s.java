 package gameLogic;
 
import java.util.List;
 import java.util.Vector;
 
 /***********
 	 0 1   
 	5 I 2
 	 4 3
  ***********/
 
 public class Field { // ready
 
 	private Vector<Field> neighbours = new Vector<Field>(6);
 	private Odor odor = new Odor();
 	public Item item = null; // TODO csak protoban public, egybk private
 	
 	Field() {}
 
 	public void decreaseAntOdor() {
 		int ant, food, hill, colony;
 		
 		ant = this.odor.getAnt();
 		food = this.odor.getFood();
 		hill = this.odor.getHill();
 		colony = this.odor.getColony();
 		
 		ant = (ant <= 0) ? (ant=0) : --ant; // ant odor decreased
 		
 		this.odor = new Odor(ant, food, hill, colony);
 	}
 
 	public void register(Item item) {
 		this.item = item;
 	}
 
 	public void deregister() {
 		this.item = null;
 	}
 
 	public ItemManagableByItem getItem() {
 		return this.item;
 	}
 
 	public Odor getOdor() {
 		return this.odor;
 	}
 	
 	public void addNeighbour(Field nb, int place) {
 		this.neighbours.add(place, nb);
 	}
 
 	public Field getNeighbour(int index) {
 		return this.neighbours.get(index);
 	}
 
 	public void dropOdor(Odor smell) {
 		int ant, food, hill, colony;
 		
 		ant = this.odor.getAnt() + smell.getAnt();
 		food = this.odor.getFood() + smell.getFood();
 		hill = this.odor.getHill() + smell.getHill();
 		colony = this.odor.getColony() + smell.getColony();
 		
 		this.odor = new Odor(ant, food, hill, colony);
 	}
 
 }
