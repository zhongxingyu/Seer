 package creeps;
 
 import interactions.SimpleDamage;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import model.AI;
 import model.Creep;
 import model.Entity;
 import model.Interaction;
 import model.Location;
 import model.Model;
 import ais.SimpleCreepAI;
 
 public class SimpleCreep extends Creep {
 
 	private final Interaction attack = new SimpleDamage (5);
 	
 	public SimpleCreep(Location location, Model model) {
 		super(location, model);
 	}
 
 	@Override
 	protected AI makeAI() {
 		return new SimpleCreepAI ();
 	}
 
 	@Override
 	public void interact(Interaction i) {
 		dead = true;
 	}
 	
 	/*public void update() {
 		Action a = ai.getNext (this);
 		if (a.state == Action.State.ATTACK) {
 			Entity attacking = a.interacting;
 			attacking.interact(attack);
 		} else if (a.state == Action.State.MOVE) {
 			location.moveLeft (1);
 		}
 	}*/
 
 	@Override
 	protected Image getSprite() {
 		Image i = new BufferedImage (32, 32, BufferedImage.TYPE_INT_ARGB);
 		Graphics g = i.getGraphics();
 		g.setColor(Color.BLUE);
 		g.fillRect(0, 0, 32, 32);
 		return i;
 	}
 }
