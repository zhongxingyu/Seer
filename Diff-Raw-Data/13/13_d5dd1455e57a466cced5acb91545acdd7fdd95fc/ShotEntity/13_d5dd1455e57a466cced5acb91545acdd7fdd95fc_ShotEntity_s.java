 package org.vergeman.sulfonicavenger;
 
 public class ShotEntity extends Entity {
 
 	/** Top border at which shots are outside */
 	private static int TOP_BORDER;
 
 	/** The vertical speed at which the players shot moves */
 	private float moveSpeed = -300;
 
 	private boolean display;
 
 	protected ShotEntity(Sprite sprite, int x, int y) {
 		super(sprite, x, y);
 		TOP_BORDER = -sprite.getHeight() - 10;
 		dy = moveSpeed;
 		display = false;
 	}
 
 
 	@Override
 	public void move(long delta) {
 		
 		// if we shot off the screen, remove ourselfs
 		if (y < TOP_BORDER) {
 			setDisplay(false);
 			dy=0;
 		}
 		
 		super.move(delta);
 	}
 	
	public void reinitalize(float x, float y) {
		this.x = x + 10; 
		this.y = y - 30;
 		dy=moveSpeed;
 		setDisplay(true);
 	}
 
 	@Override
 	public void collidedWith(Entity other) {
 		// TODO Auto-generated method stub
 		
 		// hit molecules or centiped!
 		if (other instanceof MoleculeEntity) {
 			setDisplay(false);
 			//store it off screen, not rendered anyway
 			this.x = -100;
 			this.y = -100;
 		}
 		
 		if (other instanceof NH3Entity) {
 			setDisplay(false);
 			//store it off screen, not rendered anyway
 			this.x = -100;
 			this.y = -100;
 		}
 		
 		if (other instanceof CentiBallEntity) {
 			setDisplay(false);
 			//store it off screen, not rendered anyway
 			this.x = -100;
 			this.y = -100;
 		}
 
 	}
 
 	@Override
 	public void reposition(int x, int y, float scale_rel) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setDisplay(boolean display) {
 		this.display = display;
 	}
 	
 	public boolean isDisplay() {
 		return display;
 	}
 
 }
