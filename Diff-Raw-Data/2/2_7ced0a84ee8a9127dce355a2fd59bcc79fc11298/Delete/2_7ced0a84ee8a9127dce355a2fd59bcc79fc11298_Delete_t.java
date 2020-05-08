 import java.util.List;
 import java.util.Vector;
 
 public class Delete implements UndoableCommand {
     private List<AbstractTask> deleteSpace, wholeTaskList;
     int index;
     AbstractTask deletedTask;
 
     // Initialize delete parameters
     public Delete(List<AbstractTask> deleteSpace, int index)
 	    throws IndexOutOfBoundsException {
	if (index <= 0 || index > deleteSpace.size())
 	    throw new IndexOutOfBoundsException(
 		    "index pointer is outside the delete space");
 
 	this.deleteSpace = deleteSpace;
 	this.index = index;
     }
 
     // Deletes the specified task from taskList, stores the deletedTask and
     // returns it in a list
     public List<AbstractTask> execute(List<AbstractTask> wholeTaskList)
 	    throws IllegalArgumentException {
 	if (wholeTaskList == null || wholeTaskList.size() <= 0)
 	    throw new IllegalArgumentException(
 		    "taskList cannot be empty or null");
 
 	deletedTask = deleteSpace.get(index - 1);
 	this.wholeTaskList = wholeTaskList;
 
 	wholeTaskList.remove(deletedTask);
 
 	return generateReturnList();
     }
 
     // Undoes this delete operation
     // returns the task re-added
     public List<AbstractTask> undo() {
 	// the task list from where the deleted task came from must still exist
 	assert wholeTaskList != null;
 
 	wholeTaskList.add(deletedTask);
 	return generateReturnList();
     }
 
     // Creates the list of task to be returned
     private List<AbstractTask> generateReturnList() {
 	List<AbstractTask> returnList = new Vector<AbstractTask>();
 	returnList.add(deletedTask);
 	return returnList;
     }
 }
