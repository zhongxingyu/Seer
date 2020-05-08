 package ube.core.task;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.taskdefs.condition.Condition;
 
 public class UBELauncher extends Task {
 
	private List conditions = new ArrayList();
 
 	public void add(Condition c) {
 		conditions.add(c);
 	}
 
 	public void execute() {
 
		for (Iterator iterator = conditions.iterator(); iterator.hasNext();) {
			Condition condition = (Condition) iterator.next();
			System.out.println("Condition:" + condition.toString());

 		}
 		// Launch the UBE System
 		System.out.println("Hallo UBE");
 		// Read the ube build files
 	}
 }
