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
 		soldier.setPrevious_place(soldier.getMy_place());
 		soldier.setMy_place(this);
 		this.startBattle(soldier);
 		System.out.println(soldier.toString() + " en way");
 	}
 
 	@Override
 	public void conqueredBy(Soldier soldier) {
 		this.getSoldiers().add(soldier);
 	}
 }
