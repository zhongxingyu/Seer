 package concubattles;
 
 import ar.edu.unq.tpi.pconc.Channel;
 
 public class Way extends Place {
 
 	public Way(Channel<String> controlChannel) {
 		super(controlChannel);
 
 	}
 
 	@Override
 	public Place getNextPlace(Place previous_place) {
 		if (this.getRoads().get(0) == previous_place) {
 			return this.getRoads().get(1);
 		} else {
 			return this.getRoads().get(0);
 		}
 	}
 
 	@Override
 	public void receive(Soldier soldier) {
//		System.out.println("Soldado en way");
 		soldier.setPrevious_place(soldier.getMy_place());
 		soldier.setMy_place(this);
 		this.startBattle(soldier);
 		System.out.println(soldier.toString() + " en way");
//		this.getSoldiers().get(0).setPrevious_place(this.getSoldiers().get(0).getMy_place());
//		this.getSoldiers().get(0).setMy_place(this);
 	}
 
 	@Override
 	public void conqueredBy(Soldier soldier) {
 		this.getSoldiers().add(soldier);
 	}
 }
