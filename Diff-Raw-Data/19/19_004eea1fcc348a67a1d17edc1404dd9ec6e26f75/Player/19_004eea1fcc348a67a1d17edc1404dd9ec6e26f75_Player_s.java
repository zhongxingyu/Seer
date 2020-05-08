 /**
  * For player 1 and player 2 of a Shatter game
  * @author David Gay
  * @version dev
  * Spring 2012
  */
 
 public class Player
 {
 	private String handle;
 	private int force;
 	private int skill;
 	private int wins;
 	
 	/**
 	 * Default constructor
 	 */
 	public Player()
 	{
		// Nooothiinnn' to dooooo
 	}
 
 	/**
 	 * Sets handle
 	 * @param h handle
 	 */
	public setHandle(String h)
 	{
 		handle = h;
 	}
 
 	/**
 	 * Sets force
 	 * @param f force
 	 */
	public setForce(int f)
 	{
 		force = f;
 	}
 
 	/**
 	 * Sets skill
 	 * @param s skill
 	 */
	public setSkill(int s)
 	{
 		skill = s;
 	}
 
 	/**
 	 * Adds a win
 	 */
	public addWin()
 	{
 		wins++;
 	}
 
 	/**
 	 * Resets all values (besides handle) to zero
 	 */
	public reset()
 	{
 		force = 0;
 		skill = 0;
 		wins = 0;
 	}
 }
