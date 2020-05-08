 package vdrm.pred.pred;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.UUID;
 import vdrm.base.common.IPredictor;
 import vdrm.base.data.ITask;
 import vdrm.pred.dao.ITaskDao;
 import vdrm.pred.dao.TaskDao;
 import vdrm.pred.miner.IPatternMiner;
 import vdrm.pred.miner.TreePatternMiner;
 
 public class Predictor implements IPredictor{
 
 	IPatternMiner miner;
 	HashMap<UUID,ITask> taskMap;
 	private static final double MIN_CREDIBILITY = 0.8;
 	private int counter; //add only a limited (MAX_INT) number of tasks to the tree
 	
 	public Predictor() {
 		miner = new TreePatternMiner();
 		taskMap = new HashMap<UUID,ITask>();
 		counter = 0;
 		
 		//databaseInit(); //COMENTEAZA LINIA ASTA daca nu vrei init din baza de date :D
 	}
 
 	public synchronized ArrayList<ITask> predict(ITask task) {
 		UUID id = task.getTaskHandle();
 		
 		ArrayList<UUID> pattern;
 		pattern = miner.getPattern(id, MIN_CREDIBILITY);
 		if(pattern==null) {
 			return null;
 		}
 		
 		int patternSize = pattern.size();
 		ArrayList<ITask> tasks = new ArrayList<ITask>();
		ITask temp;
 		//we do not include the first task in the prediction, because the first 
 		//task is the real task (it is not predicted)
 		for(int i=1;i<patternSize;i++) {
 			temp = taskMap.get(pattern.get(i));
			temp.setPredicted(true);
			tasks.add(temp);
 		}
 		return tasks;
 	}
 
 	@Override
 	public synchronized void addTaskToDatabase(ITask task) {
 		
 		//add task to tree and dynamically update the tree
 		counter++;
 		if(counter<Integer.MAX_VALUE) {
 			UUID taskId = task.getTaskHandle();
 			taskMap.put(taskId,task);
 			miner.addElement(taskId);
 		}
 	}
 	
 	private void databaseInit() {
 		ITaskDao dao = new TaskDao();
 		ArrayList<ITask> history = dao.getTaskHistory();
 		for(ITask tsk : history) {
 			addTaskToDatabase(tsk);
 		}
 	}
 }
