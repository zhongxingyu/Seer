 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * 
  * @author kristebo
  *
  */
 
 public class TaskGraph{
 	private ArrayList<Task> taskList;
 
 	public TaskGraph(ArrayList<Task> taskList) {
 		this.taskList=taskList;
 
 		makeDependencies(taskList);
 		// see etter sykler
 		// gjør resten ;P
 	}
 
 	// lager grafen.
 	private void makeDependencies(ArrayList<Task> taskList) {
 
 		for (Task t: taskList){
 			if (!t.getPredecessors()==null) {
 				for (int i: t.getPredecessors()){ // antar at t.getPred...() gir en ArrayList<Integer>
 					t.get(i-1).outEdge.add(t); // sett nåværende task som barn hos hver predecessor.
 				}
 			}
 			// her kan du evt. lage deg en "else, samle på alle heads" til du skal sjekke for sykler.
 		}

 	}
 	// graf ferdig! 
 	
 	// sett inn metode for å se etter sykler.
 	// sett inn resten ;P
 }
