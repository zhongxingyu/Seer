 import java.awt.Color;
 import java.awt.Graphics;
 
 /**
  * The main bullet-throwing object.
  */
 public abstract class Speaker{
 	protected double counter = 0;
 	protected int xLoc = 0, yLoc = 0;
 	protected int WIDTH = 50, HEIGHT = 50;
 	protected Color REG_COLOR = Color.WHITE;
 	protected double LIMIT = (3.14 / 2);
 	protected boolean increment = false;
	protected double angleRate = (3.14 / 40);
 
 	/**
 	 * Sets the location of the speaker.
 	 *
 	 * @param x New xLoc of speaker.
 	 * @param y New yLoc of speaker.
 	 */
 	public void setLoc(int x, int y){
 		xLoc = x;
 		yLoc = y;
 	}
 
 	public void paint(Graphics g){
 		g.setColor(REG_COLOR);
 		g.fillRect(xLoc - WIDTH / 2, yLoc - HEIGHT / 2, WIDTH, HEIGHT);
 	}
 
 	/**
 	 * Spawns a bullet.
 	 */
 	public abstract void spawn();
 
 	/**
 	 * Does something.
 	 */
 	public void animate(){
 		if (increment){
			counter += angleRate;
 		} else{
			counter -= angleRate;
 		}
 		if (Math.abs(counter) >= LIMIT){
 			increment = !increment;
 		}
 	}
 }
