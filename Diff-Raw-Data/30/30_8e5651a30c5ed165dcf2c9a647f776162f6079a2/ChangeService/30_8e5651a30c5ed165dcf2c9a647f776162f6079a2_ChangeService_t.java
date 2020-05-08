 package change;
 
 import java.util.List;
 
 /**
  * The interface between the Change Component and the other components. It manages both 
  * the filsystem changes (originated from the Filesystem component) and 
  * the group related changes (originated from the Group Management Component)
  * 
  * @author adriana
  *
  */
 public class ChangeService {
 
 	public void addChange(Change change){}
 	/**
 	 * Retrieves the history of all the changes performed in a group folder
	 * @return the list of changes
 	 */
 	public List<Change> changesHistory(){return null;} 
 	/**
 	 * Initiates a PULL operation
 	 */
 	public void pullChanges(Change change) {}
 	
 	/**
 	 * The Change component received a message from a Change component on another device.
 	 */
 	public void receivedMsg(ChangeMessage msg){}
 	
 	
 }
