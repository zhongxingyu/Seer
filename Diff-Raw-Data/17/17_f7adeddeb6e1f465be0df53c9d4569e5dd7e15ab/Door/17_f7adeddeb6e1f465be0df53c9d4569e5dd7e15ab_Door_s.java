 package main.interactables;
 
 
 public class Door extends Interactable {
 	
 	private boolean active;
 	
 	public Door(int x, int y, int sceneX, int sceneY, boolean active) {
 		
		super(x, y, sceneX, sceneY, 0);
 		
 		this.active = active;
 	}

 	@Override
 	public void activate() {
 
 		active = !active;
 	}
 
 	public boolean isOpen() {
 		
 		return active;
 	}
 }
