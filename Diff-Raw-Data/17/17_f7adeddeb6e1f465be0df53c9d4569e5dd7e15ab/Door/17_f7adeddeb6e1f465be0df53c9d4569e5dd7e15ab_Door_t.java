 package main.interactables;
 
 
 public class Door extends Interactable {
 	
 	private boolean active;
 	
 	public Door(int x, int y, int sceneX, int sceneY, boolean active) {
 		
		super(x, y, sceneX, sceneY, 0);		
 		
 		this.active = active;
		
		if (active) {
			spriteID = 1;
		}
 	}
	
 	@Override
 	public void activate() {
 
		if (active) {
			spriteID = 0;
		} else {
			spriteID = 1;
		}
		
 		active = !active;
 	}
 
 	public boolean isOpen() {
 		
 		return active;
 	}
 }
