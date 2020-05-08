 package miro.shared;
 
 import javax.persistence.Id;
 
 public class Lock {
 
 	@Id
 	Long id;
 	
 	private boolean isLocked = false;
 	
 	public boolean isLocked(){
 		return isLocked;
 	}
 	
 	public void setLocked(boolean isLocked){
 		this.isLocked = isLocked;
 	}
 }
