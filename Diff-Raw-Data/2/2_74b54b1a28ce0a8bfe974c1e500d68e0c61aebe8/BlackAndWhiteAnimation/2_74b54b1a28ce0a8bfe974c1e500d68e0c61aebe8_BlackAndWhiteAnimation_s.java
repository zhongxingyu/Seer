 package se.chalmers.kangaroo.view;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 
 import se.chalmers.kangaroo.model.creatures.BlackAndWhiteCreature;
 import se.chalmers.kangaroo.model.creatures.Creature;
 
 public class BlackAndWhiteAnimation implements Animation{
 	
 	private BlackAndWhiteCreature creature;
 	private Image sheet;
 	private int height;
 	private int width;
 	
 	public BlackAndWhiteAnimation(Creature bw){
 		this.creature = (BlackAndWhiteCreature)bw;
 		this.sheet = Toolkit.getDefaultToolkit().getImage("resources/sheets/blackandwhite_64_32.png");
 		this.height = 32;
 		this.width = 32;
 	}
 	@Override
 	public void drawSprite(Graphics g, int x, int y) {
 		if( creature.isKillable() ) {
 			g.drawImage(sheet, x, y, x+width, y+height, width, 0, 2*width, height, null, null);
 		} else {
			g.drawImage(sheet, x, y, x+width, y+height, width, 0, 2*width, height, null, null);
 		}
 
 
 }
 }
