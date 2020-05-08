 package buffs;
 import java.util.Random;
 import main.*;
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Rectangle;
 
 import states.Gameplay;
 public class TutorZone {
 	public Rectangle TutorZone;
 	public int posX, posY, width, height;
 	public Random r;
 	public TutorZone()
 	{
 		r = new Random();
 		posX = -100;
 		posY = -100;
 		width = 100;
 		height = 50;
 		TutorZone = new Rectangle(posX, posY, width, height);
 	}
 	public void render(Graphics g)
 	{
 		TutorZone.setLocation(posX, posY);
 		Color color = new Color(0,50,75,100);
 		g.setColor(color);
 		g.fill(TutorZone);
 	}
 	public void spawn()
 	{
 		posX = r.nextInt(640-width);
		posY = r.nextInt(480-height);
 	}
 	public void gainGPA()
 	{
		Gameplay.logic.gpa += .002;
 	}
 	public void removeTutorZone()
 	{
 		posX = -100;
 		posY = -100;
		Gameplay.buffSound.play();
 	}
 }
