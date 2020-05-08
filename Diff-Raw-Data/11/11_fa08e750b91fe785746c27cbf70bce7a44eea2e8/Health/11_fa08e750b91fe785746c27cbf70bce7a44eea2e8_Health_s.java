 
 public class Health {
 
 	private int health;
 	private boolean alive;
 	
 	///////////////////////////
 	
 	/**
 	 * Get health value
 	 * @return int
 	 */
 	public int getHealth(){
 		return this.health;
 	}
 	/**
 	 * Set health value
 	 * @param int hp
 	 */
 	public void setHealth(int hp){
 		this.health = hp;
 	}
 	/**
 	 * Get boolean for if lives left
 	 * @return bool
 	 */
 	public boolean isAlive(){
 		return this.alive;
 	}
 	/**
 	 * set alive boolean
 	 * @param bool alive
 	 */
 	public void setAlive(boolean alive){
 		this.alive = alive;
 	}
 	///////////////////////////
 	
 	/**
 	 * Increase health by 1. Cap at 5
 	 */
 	public void increaseHealth(){
 		if (!(getHealth() == 5 || getHealth() == 0)){
 			setHealth(getHealth() + 1);
 		}
 	}
 	/**
 	 * Increase health by passed int. Cap at 5.
 	 * @param int hp
 	 */
 	public void increaseHealth(int hp){
 		setHealth(getHealth() + hp);
 		if (getHealth() > 5){
 			setHealth(5);
 		}
 	}
 	/**
 	 * Decrease health by 1
 	 */
 	public void decreaseHealth(){
 		if (getHealth() == 1){
 			setAlive(false);
 		} else {
 			setHealth(getHealth() - 1);
 		}
 	}
 	
 	///////////////////////////
 	
 	/**
 	 * Default constructor
 	 */
 	public Health(){
 		setHealth(3);
 		setAlive(true);
 	}
 	public Health(int hp){
 		setHealth(hp);
 		if (!(hp == 0)){
 			setAlive(true);
 		} else {
 			setAlive(false);
 		}
 	}

 }
