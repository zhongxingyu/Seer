 package concubattles;
 
 public class Soldier extends Thread {
 	int numerito;
 	private Castle team;
 	private int level;
 	private int experience;
 	private int experienceToNextLevel;
 	private Place my_place;
 	private Place previous_place;
 	private boolean live;
 
 	@Override
 	public String toString() {
 		return "Soldier: " + this.numerito + " of team: " + this.getTeam().id;
 	}
 
 	public Soldier(Place my_place, Castle team, int numerito) {
 		this.my_place = my_place;
 		this.team = team;
 		this.level = 1;
 		this.experience = 0;
 		this.experienceToNextLevel = 2;
 		this.live = true;
 		this.numerito = numerito;
 	}
 
 	public int fibonacci(int e) {
 
 		int f1 = 1;
 		int f2 = 0;
 		int f = 0;
 
 		for (int i = 1; i < e; i++) {
 
 			f = f1 + f2;
 			f2 = f1;
 			f1 = f;
 
 		}
 		return f;
 
 	}
 
 	public void levelUp() {
 		this.level = this.level + 1;
 	}
 
 	public Place getMyPlace() {
 		return this.my_place;
 	}
 
 	public boolean isLive() {
 		return this.live;
 	}
 
 	public void checkForLevel() {
 		if (this.getLevel() > 1) {
 			this.getTeam().createSoldier();
 		}
 	}
 
 	public void notifyCreateSoldier() {
 		// if (this.getMy_place() == this.getTeam() || this.getPrevious_place()
 		// == this.getTeam()) {
 		// this.getTeam().createSoldier();
 		if (this.getMy_place() == this.getTeam()
 				|| this.getPrevious_place() == this.getTeam()) {
 			this.getTeam().createSoldier();
 		} else {
 			this.getP(this.getTeam());
 			this.getTeam().createSoldier();
 			this.getTeam().returnPermission();
 		}
 	}
 
 	// }
 
 	/**
  * 
  */
 	public void run() {
 		try {
 			sleep(5000);
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		while (this.live) {
 			Place place = this.getMyPlace();
 			Place next_place = place.getNextPlace(this.previous_place);
 			
 				if (place instanceof Way) {					
 					this.getP(next_place);
 					this.getP(place);
 				} else {
 					this.getP(place);
 					this.getP(next_place);
 					
 				}
 			
 			if (this.isLive() && this.getTeam().live) {
 				System.out.println(this.toString() + " Moving On");
 				place.remove(this);
 				next_place.receive(this);
 				place.returnPermission();
 				next_place.returnPermission();
 				try {
 					sleep(8000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} else {
 				place.returnPermission();
 				next_place.returnPermission();
 				break;
 			}
 
 		}
 	}
 		public void getP(Place place){
 			try{
 				place.getPermission();	
 			} catch(RuntimeException e){
				
 			}
 			
 		}
 
 	/**
 	 * funcion Fibonacci para la experiencia cada soldado gana 1 de experiencia
 	 * por matar a un soldado enemigo..
 	 */
 	public void experienceUp() {
 
 		this.experience = this.experience + 1;
 		if (this.experience == this.experienceToNextLevel) {
 			this.levelUp();
 			this.experienceToNextLevel = this.fibonacci(this.level);
 			this.experience = 0;
 		}
 
 	}
 
 	public Castle getTeam() {
 		return team;
 	}
 
 	public void setTeam(Castle team) {
 		this.team = team;
 	}
 
 	public Place getMy_place() {
 		return my_place;
 	}
 
 	public void setMy_place(Place my_place) {
 		this.my_place = my_place;
 	}
 
 	public Place getPrevious_place() {
 		return previous_place;
 	}
 
 	public void setPrevious_place(Place previous_place) {
 		this.previous_place = previous_place;
 	}
 
 	public void setLevel(int level) {
 		this.level = level;
 	}
 
 	public void setLive(boolean live) {
 		this.live = live;
 	}
 
 	public boolean getLive() {
 		return this.live;
 	}
 
 	public int getLevel() {
 		return this.level;
 	}
 
 }
