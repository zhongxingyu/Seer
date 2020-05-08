 package miro.shared;
 
 import javax.persistence.Id;
 
 public class Lock {
 
 	@Id
 	Long id;
 	
 	private boolean isLocked = false;
 	
	public Lock(){}
	
	public Lock(Long id){
		this.id = id;
	}
	
 	public boolean isLocked(){
 		return isLocked;
 	}
 	
 	public void setLocked(boolean isLocked){
 		this.isLocked = isLocked;
 	}
 }
