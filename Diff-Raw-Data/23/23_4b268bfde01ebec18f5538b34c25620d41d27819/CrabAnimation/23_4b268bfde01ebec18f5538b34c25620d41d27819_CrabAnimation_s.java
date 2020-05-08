 package se.chalmers.kangaroo.view;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 
 import se.chalmers.kangaroo.model.Direction;
 import se.chalmers.kangaroo.model.creatures.CrabCreature;
 
 public class CrabAnimation implements Animation{
 	private CrabCreature crab;
 	private int tick;
 	private int currentState;
 	private Image sheet;
 	private int width;
 	private int height;
 	
	public CrabAnimation(CrabCreature c){
		this.crab = c;
 		tick = 0;
 		currentState = 0;
 		this.sheet = Toolkit.getDefaultToolkit().getImage("../gfx/creatures/crab_256x32.png");
 		this.width = 64;
 		this.height = 32;
 	}
 	@Override
 	public void drawSprite(Graphics g, int x, int y) {
 		if(tick == 15){
 			tick = 0;
 			currentState++;
 			currentState = currentState % 2;
 		}
		int currentSprite = crab.getDirection() == Direction.DIRECTION_WEST ? currentState : currentState+2;
 		g.drawImage(sheet, x, y, x+width, y+height, currentSprite*64, 0, currentSprite*64+width, height, null, null);
 		tick++;
 	}
 	
 }
