 package gameLogic;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 public class Ant extends Item { // ready
 
 	public boolean carry = false; // TODO csak protoban public, egybk private
 	public Food carriedItem = null; // TODO csak protoban public, egybk privates
 	private Field nbField = null;
 	
 	Ant(Field field) {
 		super(field);
 	}
 	
 	public void eaten() {
 		this.dereg();
 	}
 
 	public void pickup(Food food) {
 		this.dereg();
 		this.reg();
 		this.carriedItem = food;
 		this.carry = true;
 	}
 
 	public void arrivedHome() {
 		if (this.carry)
 			this.dereg();
 	}
 
 	@Override
 	public void act() {
 		this.nbField = this.chooseByOdor();
 		if (this.nbField != null) {
 			ItemManagableByItem i = this.nbField.getItem();
 			
 			if (i != null)
 				i.antInteract(this);
 			else {
 				this.dereg();
 				this.reg();
 			}
 		}
 	}
 	
 	private Field chooseByOdor() { // TODO j fvny
 		
 		Odor o = null;
 		int oddsSoFar = 0;
 		
 		/// calculate odds
 		Map<Field, Integer> odds = new HashMap<Field, Integer>();
 		for (int i=0; i<6; ++i) {
 			this.nbField = this.field.getNeighbour(i);
 			if (this.nbField != null) {
 				o = this.nbField.getOdor();
 				if (this.carry)
 					oddsSoFar += o.getHill();
 				else
 					oddsSoFar += o.getAnt() + 2*o.getFood(); // two times food odor !
 				odds.put(this.nbField, oddsSoFar);
 			}
 		}
 		
 		/// rand
 		Random gen = new Random();
 		int randomNum = gen.nextInt(oddsSoFar);
 		
 		for (Map.Entry<Field, Integer> entry : odds.entrySet()) {
 			if (randomNum <= entry.getValue()) {
 				return entry.getKey();
 			}
 		}
 		
 		return null; // cannot happen
 	}
 	
 	private void reg() { // TODO j fvny!
 		this.nbField.register(this);
 		this.field = this.nbField;
 		this.nbField = null;
 	}
 	
 	private void dereg() { // TODO j fvny
 		this.field.deregister();
 		this.field = null;
 	}
 
 	@Override
 	public void antInteract(Ant ant) {
 		// nothing happens here
 	}
 
 	@Override
 	public void antEaterInteract(AntEater antEater) {
 		this.dereg();
 		antEater.eat();
 	}
 
 	@Override
 	public void killerSprayInteract() {
 		this.dereg();
 	}
 
 	@Override
 	public void stoneInteract(Stone stone) {
 		// nothing happens here
 	}
 }
