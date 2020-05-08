 import java.util.List;
 
import com.sun.jmx.snmp.tasks.Task;

 public class TypeTaskPair {
     public static enum Type {
 	// list of possible operation types
	ADD, DELETE, EDIT, SEARCH, DISPLAY, INVALID
     }
 
     Type type;
    List<Task> taskList;
 
    public TypeTaskPair(Type type, List<Task> taskList) {
 	this.type = type;
 	this.taskList = taskList;
     }
 
     public Type getType() {
 	return type;
     }
 
    public List<Task> getTasks() {
 	return taskList;
     }
 
 }
