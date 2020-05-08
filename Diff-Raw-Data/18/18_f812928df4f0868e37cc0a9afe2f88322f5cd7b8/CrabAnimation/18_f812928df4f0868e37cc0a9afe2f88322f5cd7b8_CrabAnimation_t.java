 package se.chalmers.kangaroo.view;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 
 import se.chalmers.kangaroo.model.creatures.CrabCreature;
 import se.chalmers.kangaroo.model.creatures.Creature;
 import se.chalmers.kangaroo.model.utils.Direction;
 
 public class CrabAnimation implements Animation{
 	private CrabCreature crab;
 	private int tick;
	private int currentSprite;
 	private Image sheet;
 	private int width;
 	private int height;
 	
 	public CrabAnimation(Creature c){
 		this.crab = (CrabCreature)c;
 		tick = 0;
		currentSprite = 0;
 		this.sheet = Toolkit.getDefaultToolkit().getImage("resources/sheets/crab_256x32.png");
 		this.width = 64;
 		this.height = 32;
 	}
 	@Override
 	public void drawSprite(Graphics g, int x, int y) {
		if(tick == 10){
 			tick = 0;
			currentSprite++;
			currentSprite = currentSprite % 4;
 		}
 		g.drawImage(sheet, x, y, x+width, y+height, currentSprite*64, 0, currentSprite*64+width, height, null, null);
 		tick++;
 	}
 	
 	@Override
 	public String toString(){
		return "CrabAnimation: " +tick +" "+currentSprite;
 	}
 }
