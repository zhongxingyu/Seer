 package ube.core.task;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.taskdefs.condition.Condition;
 
 public class UBELauncher extends Task {
 
	private final List<Condition> conditions = new ArrayList<Condition>();
 
 	public void add(Condition c) {
 		conditions.add(c);
 	}
 
	@Override
 	public void execute() {
 
		boolean areTrue = true;
		for (Iterator<Condition> iterator = conditions.iterator(); iterator
				.hasNext();) {
			Condition condition = iterator.next();
			areTrue = areTrue && condition.eval();
			System.out.println("Condition:" + condition.eval());
		}
		if (areTrue) {
			System.out.println("Conditions are TRUE");
		} else {
			System.out.println("Any Conditions are FALSE");
 		}
 		// Launch the UBE System
 		System.out.println("Hallo UBE");
 		// Read the ube build files

 	}
 }
