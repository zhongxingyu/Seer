 package concubattles;
 
 import ar.edu.unq.tpi.pconc.Channel;
 
 public class City extends Place {
 	private Castle team;
 
 	public Castle getTeam() {
 		return team;
 	}
 
 	public void setTeam(Castle team) {
 		this.team = team;
 	}
 
 	
 	public City(Channel<String> controlChannel) {
 		super(controlChannel);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
	public void receive(Soldier soldier) {		
 		soldier.setPrevious_place(soldier.getMy_place());
 		soldier.setMy_place(this);
 		if (!(this.getTeam() == soldier.getTeam())) {
 			this.startBattle(soldier);
 		} else {
 			System.out.println("Soldado" +soldier.numerito + "en ciudad");   
 			this.getSoldiers().add(soldier);
 		}
 
 	}
 
 	@Override
 	public void conqueredBy(Soldier soldier) {
 		soldier.notifyCreateSoldier();
 		this.getSoldiers().add(soldier);
 		System.out.println(soldier.toString() + " en ciudad");
 		this.setTeam(soldier.getTeam());
 	}
 
 }
