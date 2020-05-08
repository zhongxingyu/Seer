 package model;
 
 /**This class represent the model for both the start- and the pause menu*/
 public class PauseMenu extends AbstractMenu{
 	private int isMarked;
 	
 	public PauseMenu() {
		this.resetIsMarked();
 		
 	}
 	
 	@Override
 	public void markButtonDown() {
 		isMarked = ++isMarked % 5;
 	}
 	
 	@Override
 	public void markButtonUp() {
 		isMarked = (isMarked + 4) %5; //add 4 to make positive (--isMarked + 5)
 	}
 	
 	public int getIsMarked(){
 		return isMarked;
 	}
 	
	public void resetIsMarked() {
		this.isMarked = 0;
	}
 
 }
