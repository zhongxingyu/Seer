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
		force = 16;
		skill = 1;
		wins = 0;
 	}
 
 	/**
 	 * Sets handle
 	 * @param h handle
 	 */
	public void setHandle(String h)
 	{
 		handle = h;
 	}
 
 	/**
 	 * Sets force
 	 * @param f force
 	 */
	public void setForce(int f)
 	{
 		force = f;
 	}
 
 	/**
 	 * Sets skill
 	 * @param s skill
 	 */
	public void setSkill(int s)
 	{
 		skill = s;
 	}
 
 	/**
 	 * Adds a win
 	 */
	public void addWin()
 	{
 		wins++;
 	}
 
 	/**
 	 * Resets all values (besides handle) to zero
 	 */
	public void reset()
 	{
 		force = 0;
 		skill = 0;
 		wins = 0;
 	}
 }
