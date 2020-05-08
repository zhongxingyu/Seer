 package BattleBases;
 
 public class Player {
 	private String name = null;
 	private int bank = 1000;
 	private Base base = new Base();
 	private Army armyQueue;
 
 	public Player(String _name, Army army) {
 		this.name = _name;
 		this.bank = 1000;
 		this.base = new Base();
 		this.armyQueue = army;
 
 	}
 
 	public Player war(Player opponent, int clockTick) {
 		Player winner = null;
 		if (this.armyQueue.getHead() != null
 				&& opponent.armyQueue.getHead() != null) {
 			this.armyQueue.getHead().warriorFight(opponent.armyQueue.getHead(), clockTick);
 			if (this.armyQueue.getHead().getHealth() <= 0) {
 				armyQueue.next();
 			}
 			if (opponent.armyQueue.getHead().getHealth() <= 0){
 				opponent.armyQueue.next();
 			}
 		} else
			if(this.armyQueue.getHead() == null){
				winner = opponent;
			} else{
				winner = this;
			}
 		return winner;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public int getHealth() {
 		if (armyQueue.getHead() != null){
 		return armyQueue.getHead().getHealth();
 	}else{
 		return 0;}
 	}
 }
