 package gameLogic;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 public class AntEater extends Item { // ready
 
 	public int hunger; // TODO csak protoban public, egybk private
 	private Field nbField = null;
 	private int fromWhere; // TODO j attibrtum!
 	
 	AntEater(Field field) {
 		super(field);
 		this.hunger = 10;
 	}
 	
 	AntEater(Field field, int hunger) {
 		super(field);
 		this.hunger = hunger;
 	}
 	
 	public void eat() {
 		this.dereg();
 		this.reg();
 		this.hunger--;
 	}
 
 	public void arrivedHome() {
 		if (this.hunger <= 0)
 			this.dereg();
 	}
 	public void push(){
 		this.dereg();
 		this.reg();
 	}
 
 	@Override
 	public void act() {
 		
 		this.nbField = this.chooseByOdor();
 		
 		if (this.nbField != null) {
 			ItemManagableByItem i = this.nbField.getItem();
 			
 			if (i != null)
 				i.antEaterInteract(this);
 			else {
 				this.dereg();
 				this.reg();
 			}
 		}
 	}
 	
 	private Field chooseByOdor() { // TODO j fvny
 		
		Field chosen = null;
 		Odor o = null;
 		int oddsSoFar = 0;
 		
 		/// calculate odds
 		Map<Field, Integer> odds = new HashMap<Field, Integer>();
 		for (int i=0; i<6; ++i) {
 			this.nbField = this.field.getNeighbour(i);
 			if (this.nbField != null) {
 				o = this.nbField.getOdor();
 				if (this.hunger <= 0) // if not hungry, goes to colony
 					oddsSoFar += o.getColony();
 				else
 					oddsSoFar += o.getAnt();
 				odds.put(this.nbField, oddsSoFar);
 			} else { // fromWhere got to be recovered <-- odds.size() = neighbours.size()
 				odds.put(null, 0);
 			}
 		}
 		
 		/// rand
 		Random gen = new Random();
 		int randomNum = gen.nextInt(oddsSoFar);
 		this.fromWhere = 0;
 		
 		for (Map.Entry<Field, Integer> entry : odds.entrySet()) {
 			if (randomNum <= entry.getValue()) {
 				return entry.getKey();
 			}
 			this.fromWhere++;
 		}
 		
 		return null; // cannot happen
 	}
 
 	@Override
 	public void antInteract(Ant ant) {
 		ant.eaten();
 		this.hunger--;
 	}
 
 	@Override
 	public void antEaterInteract(AntEater antEater) {
 		// nothing happens here
 	}
 
 	@Override
 	public void killerSprayInteract() {
 		// nothing happens here
 	}
 
 	@Override
 	public void stoneInteract(Stone stone) {
 		// nothing happens here
 	}
 	
 	public int getFromWhere() { // TODO j fvny!
 		return this.fromWhere;
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
 	
 }
