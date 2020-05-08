 package vdrm.disp.alg;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.UUID;
 
 import vdrm.base.common.IAlgorithm;
 import vdrm.base.common.IPredictor;
 import vdrm.base.data.IServer;
 import vdrm.base.data.ITask;
 import vdrm.base.impl.BaseCommon;
 import vdrm.base.impl.Server;
 import vdrm.base.impl.Task;
 import vdrm.base.util.Sorter;
 import vdrm.base.util.VDRMLogger;
 import vdrm.disp.onservice.IOpenNebulaService;
 import vdrm.disp.onservice.OpenNebulaService;
 import vdrm.disp.powerservice.PowerService;
 import vdrm.pred.pred.Predictor;
 
 /***
  * TODO: findNewPosition can use insertSortedServer method! May be the same as
  * 		reorder server list, but called when adding a new task. Might be able to
  * 		make a single method out of the 2.
  * TODO: 
  * @author Vlad & Robi
  *
  */
 public class Algorithm implements IAlgorithm{
 
 	private ArrayList<IServer> emptyServers;
 	private ArrayList<IServer> inUseServers;
 	private ArrayList<IServer> fullServers;
 	private ArrayList<ITask> unassignedTasks;
 	private ArrayList<ITask> predictedTasks;
 	private IPredictor predictor;
 	private Sorter sortingService;
 	
 	private VDRMLogger logger;
 	
 	private IOpenNebulaService onService;
 	private PowerService powerService;
 	
 	private Boolean waitingTasksInQueue;
 	
 	@Override
 	public void initialize(ArrayList<IServer> servers) {
 		//initialize data structures
 		
 		// set server thresholds
 		for (IServer iServer : servers) {
 			Server s = (Server)iServer;
 			s.setCpuFreq((int) (s.getMaxCpu() 		- (s.getMaxCpu()*BaseCommon.SERVER_THRESHOLD)));
 			s.setMemoryAmount((int) (s.getMaxMem()  - (s.getMaxMem()*BaseCommon.SERVER_THRESHOLD)));
 			s.setHddSize((int) (s.getMaxHdd() 		- (s.getMaxHdd()*BaseCommon.SERVER_THRESHOLD)));
 		}
 		emptyServers = servers;
 		inUseServers = new ArrayList<IServer>();
 		fullServers = new ArrayList<IServer>();
 		unassignedTasks = new ArrayList<ITask>();
 		predictedTasks = new ArrayList<ITask>();
 		
 		//initialize prediction and predictor
 		predictor = new Predictor();
 		
 		//initialize sorting service
 		sortingService = new Sorter();
 		emptyServers = sortingService.insertSortServersDescending(emptyServers);
 		
 		//initialize logger
 		logger = new VDRMLogger();
 		
 		//initialize OpenNebula
 		onService = new OpenNebulaService();
 		
 		powerService = new PowerService();
 		
 		logger.logInfo("System initialized successfully, waiting for tasks.\n\n");
 	}
 	
 	public void initialize(ArrayList<IServer> servers,
 			ArrayList<ITask> tasks, ArrayList<ITask> history) {
 		//	initialize data structures
 		
 		// set server thresholds
 		for (IServer iServer : servers) {
 			Server s = (Server)iServer;
 			s.setCpuFreq((int) (s.getMaxCpu() 		- (s.getMaxCpu()*BaseCommon.SERVER_THRESHOLD)));
 			s.setMemoryAmount((int) (s.getMaxMem()  - (s.getMaxMem()*BaseCommon.SERVER_THRESHOLD)));
 			s.setHddSize((int) (s.getMaxHdd() 		- (s.getMaxHdd()*BaseCommon.SERVER_THRESHOLD)));
 		}
 		emptyServers = servers;
 		inUseServers = new ArrayList<IServer>();
 		fullServers = new ArrayList<IServer>();
 		unassignedTasks = new ArrayList<ITask>();
 		predictedTasks = new ArrayList<ITask>();
 		
 		//initialize prediction and predictor
 		predictor = new Predictor();
 		predictor.initialize(tasks);
 		predictor.setHistory(history);
 		
 		//initialize sorting service
 		sortingService = new Sorter();
 		emptyServers = sortingService.insertSortServersDescending(emptyServers);
 		
 		//initialize logger
 		logger = new VDRMLogger();
 		
 		//initialize OpenNebula
 		onService = new OpenNebulaService();
 		
 		powerService = new PowerService();
 		
 		logger.logInfo("System initialized successfully, waiting for tasks.\n\n");
 		
 	}
 
 	@Override
 	public synchronized void newTask(ITask newTask) {
 		logger.logInfo("\n*** Starting one iteration for newTask.");
 		
 		//we use an OPTIMISTIC approach (regarding the prediction)
 		//if predicted tasks list has tasks in it, than we need to deal with prediction
 		int i;
 		if(predictedTasks.size()>0) {
 			if(newTask.equals(predictedTasks.get(0))) { 
 					//prediction is correct
 				
 					// First magic line , these do the trick :)
 					UUID taskUUID = newTask.getTaskHandle();
 					newTask = predictedTasks.get(0);
 					newTask.setPredicted(false);
 					
 					// Second magic line
 					newTask.setTaskHandle(taskUUID);
 					
 					// OpenNebula
 					if(newTask.getServerId() != null){
 						//System.out.println("VM will be Deployed for task with UUID (1): " + newTask.getTaskHandle().toString());
 						IServer serv = new Server();
 						onService.DeployTask(newTask, false, serv);
 						logger.logInfo("Predicted task " + newTask.getTaskHandle() + " deployed on server.");
 					}
 					
 					predictedTasks.remove(0);
 					
 					predictor.addEntry(newTask);
 					
 					//since the prediction was correct, we exit algorithm and wait for next task
 					return;					
 				}
 			else { 	
 				//prediction was not correct
 				System.out.println("Prediction miss for task with UUID (3): " + newTask.getTaskHandle().toString());
 				
 				//since we used an optimistic approach, we have to remove the tasks from the servers
 				IServer tempServer;
 				ITask tempTask;
 				int size = predictedTasks.size();
 				
 				//we add all servers that have predicted tasks to a set (set => no duplicates)
 				HashSet<IServer> modifiedServers = new HashSet<IServer>();
 				for(i=0;i<size;i++) {
 					tempTask = predictedTasks.get(i);
 					tempServer = tempTask.getServer();
 					tempServer.removeTask(tempTask);
 					modifiedServers.add(tempServer);
 				}
 				
 				//we iterate over the set and move the servers into the correct list
 				for(IServer tempServ : modifiedServers) {
 					//if it was in full, it moves either to empty or to inUse (depends on isEmpty)
 					if(fullServers.contains(tempServ)) {
 						if(!tempServ.isFull()) {
 							fullServers.remove(tempServ);
 							if(tempServ.isEmpty()){ 
 								emptyServers.add(tempServ);
 								powerService.sendServerToSleep(tempServ);
 							}
 							else
 								inUseServers.add(tempServ);
 						}
 					}
 					//if it was in inUse, it either remains there or it moves to empty (depends on isEmpty)
 					else if(inUseServers.contains(tempServ)) {
 						if(tempServ.isEmpty()) {
 							inUseServers.remove(tempServ);
 							emptyServers.add(tempServ);
 							powerService.sendServerToSleep(tempServ);
 						}
 					}
 				}
 				
 				//now we reorder the inUseServer list and the emptyServer list
 				inUseServers = sortingService.insertSortServersDescending(inUseServers);
 				emptyServers = sortingService.insertSortServersDescending(emptyServers);
 				
 				//clear list of predicted tasks
 				predictedTasks = new ArrayList<ITask>();
 				
 				//add this task to unassigned tasks list
 				unassignedTasks.add(newTask);
 				
 				logger.logInfo("Prediction was not correct.");
 			}
 		}
 		else {
 			unassignedTasks.add(newTask);
 			logger.logInfo("Task " + newTask.getTaskHandle() + " (normal) added to unassigned tasks list.");
 		}
 
 		if(BaseCommon.PredictionEnabled) {
 			//run prediction and add predicted tasks to unassignedTasks list
 			ArrayList<ITask> prediction;
 			prediction = predictor.predict(newTask);
 			predictor.addEntry(newTask);
 			
 			if(prediction!=null) {
 				logger.logInfo("Prediction made on future task following task " + newTask.getTaskHandle() + ".");
 				int predSize = prediction.size();
 				for(i=0;i<predSize;i++)
 					unassignedTasks.add(prediction.get(i));
 			}
 		}
 		
 		//run consolidation algorithm (distribute tasks from unassignedTasks list to servers
 		//this algorithm assigns to every task a server reference and to every server a task 
 		//reference (the task is actually added to the list of a server, but the task is not 
 		//actually deployed to a physical server)
 		
 		boolean success;
 		success = consolidateTasks();
 		
 		if(!success) {
 			logger.logInfo("Consolidation algorithm failed -> not enough resources.");
 			predictedTasks = new ArrayList<ITask>();
 		}
 		else {
 			logger.logInfo("Consolidation algorithm ran.");
 			
 			//dispatch tasks which are not predicted to real servers
 			for(i=0;i<unassignedTasks.size();i++) {
 				ITask tempTask = unassignedTasks.get(i);
 				if(tempTask.isPredicted())
 					predictedTasks.add(tempTask);
 				else {
 					
 					// OpenNebula
 					if(tempTask.getServerId() != null) {
 						IServer serv = new Server();
 						//System.out.println("VM will be Deployed for task with UUID (2): " + tempTask.getTaskHandle().toString());
 						onService.DeployTask(tempTask,false,serv);
 						logger.logInfo("Task " + newTask.getTaskHandle() + " (normal) deployed on server.");
 					}
 				}		
 			}
 		}
 		
 		//clear unassigned tasks list
 		unassignedTasks = new ArrayList<ITask>();
 		
 		logger.logInfo("*** Finished one iteration for newTask.");
 	}
 	
 	/**
 	 * This methods received as input a list of tasks and sorts it 
 	 * in an ascending order. The criteria is the following:
 	 * Sort according to cpu. If two tasks have equal cpu, sort 
 	 * according to mem. If two tasks have equal mem, sort according
 	 * to hdd.
 	 * @param list - the list of tasks that must be sorted
 	 */
 	private void sort(ArrayList<ITask> list) {
 		list = sortingService.insertSortTasksAscending(list);
 		logger.logInfo("Sorted some tasks...");
 	}
 	
 	/**
 	 * There are a number of tasks in the unassignedTasks list
 	 * This method assigns each task to a server (and also a server to a task)
 	 * OPTIMISTIC approach!
 	 */
 	private boolean consolidateTasks()
 	{
 		logger.logInfo("\n\n*** Starting to consolidate tasks.");
 		
 		ArrayList<ITask> tempList = new ArrayList<ITask>();
 		for(ITask tsk : unassignedTasks) {
 			tempList.add(tsk);
 		}
 		
 		HashSet<IServer> modifiedServers = new HashSet<IServer>();
 		
		//sort the array of tasks in an ascending order (according to cpu, then mem, then hdd)
 		sort(tempList);
 		
 		int i = 0;
 		ITask tempTask;
 		IServer tempServer;
 		tempTask = tempList.get(0);
 		
 		while(!tempList.isEmpty() && i<inUseServers.size()) {
 			tempServer = inUseServers.get(i);
 			if(tempServer.meetsRequirments(tempTask)) {
 				//this task fits on server i
 				tempTask.setServer(tempServer);
 				tempServer.addTask(tempTask);
 				logger.logInfo("Found a place for task " + tempTask.getTaskHandle() + ", on server "+ tempServer.getServerID() +".(1)");
 				
 				//modified -> added a new task to this server
 				modifiedServers.add(tempServer);
 				
 				tempList.remove(0);
 				if(!tempList.isEmpty())
 					tempTask = tempList.get(0);
				i = 0;
 			}
 			else
 				i++;
 		}
 		
 		if(!tempList.isEmpty()) {
 			ArrayList<IServer> newServers = new ArrayList<IServer>();
 			boolean ok;
 			int tempListSize = tempList.size();
 			while(!tempList.isEmpty()) {
 				i = 0;
 				ok = false;
 				
 				//tempTask is the last task in the tempList
 				tempTask = tempList.get(tempListSize-1);
 				tempListSize--;
 				
 				while(i<newServers.size() && ok==false) {
 					tempServer = newServers.get(i);
 					if(tempServer.meetsRequirments(tempTask)) {
 						tempTask.setServer(tempServer);
 						tempServer.addTask(tempTask);						
 						tempList.remove(tempListSize);
 						ok = true;
 						logger.logInfo("Found a place for task " + tempTask.getTaskHandle() + ", on server "+ tempServer.getServerID() +".(2)");
 					}
 					else 
 						i++;
 				}
 				
 				if(ok==false) {
 					//we must use a new server (the remaining tasks do not fit on the inUseServers list)
 					if(emptyServers.size() == 0) {
 						Date d = new Date();
 						logger.logSevere("SEVERE: Task " + tempTask.getTaskHandle().toString() + " cannot be deployed at this moment (" +
 								d.toString());
 						
 						//the consolidation failed ->not enough resources
 						//we must undo the consolidation done so far
 						for(ITask auxTask : unassignedTasks) {
 							tempServer = auxTask.getServer();
 							if(tempServer != null) {
 								tempServer.removeTask(auxTask);
 							}
 						}
 						logger.logSevere("Not enough resources -> consolidation has been undone");
 						
 						// notify that there are no more free resources (stop sending tasks) 
 						BaseCommon.Instance().getResourceAllocateEvent().setChanged();
 						BaseCommon.Instance().getResourceAllocateEvent().notifyObservers();
 						
 						return false;
 					}else{
 						IServer newServer = emptyServers.get(0);
 						newServers.add(newServer);
 						emptyServers.remove(0);
 						
 						BaseCommon.Instance().ServerStarting = true;
 						powerService.wakeUpServer(newServer);
 						BaseCommon.Instance().ServerStarting = false;
 						
 						tempTask.setServer(newServer);
 						newServer.addTask(tempTask);
 						tempList.remove(tempListSize);
 						ok = true;
 						
 						//modified -> removed from empty servers list (added a new task to it)
 						modifiedServers.add(newServer);
 						
 						logger.logInfo("Found a place for task " + tempTask.getTaskHandle() + ", on server "+ newServer.getServerID() +".(3)");
 					}
 				}
 			}			
 		}	
 		//if we get here, consolidation is a success
 		//we must move the servers that have been modified to the appropriate lists
 		
 		logger.logInfo("Consolidation was a success -> now reordering server lists");
 		
 		for(IServer auxServer : modifiedServers) {
 			if(emptyServers.contains(auxServer)) {
 				if(!auxServer.isEmpty()) {
 					emptyServers.remove(auxServer);
 					
 					BaseCommon.Instance().ServerStarting = true;
 					powerService.wakeUpServer(auxServer);
 					BaseCommon.Instance().ServerStarting = false;
 					if(auxServer.isFull())
 						fullServers.add(auxServer);
 					else
 						inUseServers.add(auxServer);
 				}
 			}
 			else if(inUseServers.contains(auxServer)) {
 				if(auxServer.isFull()) {
 					inUseServers.remove(auxServer);
 					fullServers.add(auxServer);
 				}
 			}
 			else {
 				if(auxServer.isFull())
 					fullServers.add(auxServer);
 				else
 					inUseServers.add(auxServer);
 			}
 		}
 		
 		//now we sort the lists
 		inUseServers = sortingService.insertSortServersDescending(inUseServers);
 		emptyServers = sortingService.insertSortServersDescending(emptyServers);
 		
 		logger.logInfo("*** Finished consolidateTask.");
 		return true;
 	}
 
 	/***********************************************************/
 	/***********************************************************/
 	/********************* TASK ENDS ***************************/
 	/***********************************************************/
 	/***********************************************************/
 	
 	/*
 	 * Main function which gets called when a task ends.
 	 * It treats 2 cases:
 	 * 
 	 * 1) If the server has a load lower than 50%, 
 	 * 		a) If it has a small number of tasks, try to redistribute them
 	 * 			on other servers and close this server. 
 	 * 		b) If it has a large number of tasks and migration time 
 	 * 			would be large, try to fill the server with tasks from the
 	 * 			last 2 in-use servers (which can make up to a full server).
 	 * 
 	 * 2) If the server became empty because of the ended task, close it.
 	 * 		Else, try to fill the server with tasks from the
 	 * 		last 2 in-use servers (which can make up to a full server).
 	 * 		
 	 */
 	@Override
 	public synchronized void endTask(ITask t){
 		//System.out.println("Ended one task");
 		logger.logInfo("\n\n*** Starting end tasks.");
 		IServer server = t.getServer();
 		if(server != null){
 			// remove task from server (this is the point of this method, DUUUH !)
 			onService.FinishTask(t);
 			server.removeTask(t);
 			//t.setServer(null);
 			logger.logInfo("FT: Task " + t.getTaskHandle() + " has finished.");
 			
 			// if server was full, add it to the "in use"
 			if(!server.isFull() && !inUseServers.contains(server) && fullServers.contains(server)){
 				fullServers.remove(server);
 				inUseServers.add(server);
 			}
 			
 			// now make some house cleaning and ordering
 			if(server.getLoad() <= 50){
 				if(server.getTotalNumberOfTasks() > 0){
 					if(inUseServers.size() > 1){
 						logger.logInfo("FT: Server load < 50% and >1 inUseServers.");
 						if(server.getTotalNumberOfTasks() < BaseCommon.Instance().getNrOfTasksThreshold()){
 							logger.logInfo("FT: Server has few tasks, so try to empty it.");
 							if(!redistributeTasks(server)){
 								logger.logInfo("FT: Server cannot be emptied, reordering server list.");
 								if(inUseServers.size() > 0){
 									reorderServerList(server, -1);
 								}
 							}
 						}else{
 							logger.logInfo("FT: Server has many tasks, so try to fill it.(1)");
 							tryToFillServer(server);
 							reorderServerList(server, -1);
 						}
 					}else{
 						// do nothing, there is only this one server left.
 						logger.logInfo("FT: Only one server left.");
 					}
 				}else{
 					// the server is now empty, sleep
 					logger.logInfo("FT: Server is now empty. Order it to sleep.(1)");
 					if(server.getTotalNumberOfTasks() == 0){
 						server.OrderStandBy();
 						inUseServers.remove(server);
 						emptyServers.add(server);
 						powerService.sendServerToSleep(server);
 					}
 				}
 			}else{
 				
 				if(server.getTotalNumberOfTasks() == 0){
 					logger.logInfo("FT: Server is now empty. Order it to sleep. (2)");
 					//server.OrderStandBy();
 					inUseServers.remove(server);
 					emptyServers.add(server);
 					powerService.sendServerToSleep(server);
 				}
 				else{
 					logger.logInfo("FT: Server has great load, so try to fill it.(2)");
 					tryToFillServer(server);
 					reorderServerList(server, -1);
 				}
 			}
 		}else{
 			// this is not right...why is the server null?
 		}
 		emptyServers = sortingService.insertSortServersDescending(emptyServers);
 		logger.logInfo("*** Finished endTasks.");
 	}
 
 	/***
 	 * Using the last 2 servers in the inUseList, try to fill a server in order to
 	 * maximize the number of full servers.
 	 */
 	public synchronized ITask[] findMaximumUtilizationPlacement(IServer server,
 			IServer secondToLastServer, IServer lastServer) {
 		logger.logInfo("\n\n*** Starting findMaximumUtilizationPlacement.");
 		ArrayList<Integer> destinationResources = server.GetAvailableResources();
 		ITask t1, t2;
 		
 		int nrTasks1, nrTasks2;
 		nrTasks1 = lastServer.getTotalNumberOfTasks();
 		nrTasks2 = secondToLastServer.getTotalNumberOfTasks();
 		if(nrTasks1 > 0 && nrTasks2 > 0){
 			// try to empty the server with the fewest tasks
 			if(nrTasks1 <= nrTasks2){
 				logger.logInfo("FT: Last server has the least nr of tasks");
 				synchronized(lastServer.getTasks()){
 					for(ITask t:lastServer.getTasks()){
 						if(server.compareAvailableResources(t) && !server.isFull()
 								&& ((Task)t).getCanMigrate()){
 							if( t.isPredicted() || (!t.isPredicted()&& ((Task)t).isDeployed()) ){
 								logger.logInfo("FT: Added task "+ t.getTaskHandle() +" to server "+ server.getServerID()+ ".(1)");
 								//remove task from current server
 								lastServer.removeTask(t);
 								// set the new server
 								t.setServer(server);
 								
 								// add task to new server
 								server.addTask(t);
 								
 								if(!t.isPredicted()){
 									((Task)t).setCanMigrate(false);
 									MigrateTask(t, server);
 								}
 									
 									//onService.MigrateTask(t, server);
 								
 								if(server.isFull()){
 									logger.logInfo("FT: Server is full. Adding it to the full servers list.(2)");
 									fullServers.add(server);
 									break;
 								}
 							}
 							//onService.MigrateTask(t, server);
 						}
 					}
 				}
 				
 				if(lastServer.isEmpty()){
 					logger.logInfo("FT: Last server is empty. Assigning it to the empty servers list...");
 					inUseServers.remove(inUseServers.indexOf(lastServer));
 					emptyServers.add(lastServer);
 					powerService.sendServerToSleep(lastServer);
 				}
 				
 				// server is not full. start with second to last server
 				if(!server.isFull()){
 					int timesTried = 0;
 					logger.logInfo("FT: Server is not full, starting to get tasks from second to last server.");
 					ITask t;
 					while(( t = secondToLastServer.GetNextLowestDemandingTask())!=null && (!server.isFull()) 
 							&& timesTried < secondToLastServer.getTotalNumberOfTasks()){
 						if(server.compareAvailableResources(t) && !server.isFull()
 								&& ((Task)t).getCanMigrate()){
 							if( t.isPredicted() || (!t.isPredicted()&& ((Task)t).isDeployed()) ){
 								secondToLastServer.removeTask(t);
 								t.setServer(server);
 								server.addTask(t);
 								
 								if(!t.isPredicted()){
 									((Task)t).setCanMigrate(false);
 									MigrateTask(t, server);
 								}
 									//onService.MigrateTask(t, server);	
 								logger.logInfo("FT: Added task "+ t.getTaskHandle() +" to server "+ server.getServerID()+ ".(2)");
 								if(server.isFull()){
 									logger.logInfo("FT: Server is full. Adding it to the full servers list.(2)");
 									fullServers.add(server);
 									break;
 								}
 							}
 							
 						}
 						timesTried++;
 					}
 				}
 				
 				if(secondToLastServer.isEmpty()){
 					logger.logInfo("FT: Second to last server is empty. Assigning it to the empty servers list...");
 					inUseServers.remove(inUseServers.indexOf(secondToLastServer));
 					emptyServers.add(secondToLastServer);
 					powerService.sendServerToSleep(secondToLastServer);
 				}
 			}else{
 				synchronized(secondToLastServer.getTasks()){
 					for(ITask t:secondToLastServer.getTasks()){
 						if(server.compareAvailableResources(t) && !server.isFull()
 								&& ((Task)t).getCanMigrate()){
 							if( t.isPredicted() || (!t.isPredicted()&& ((Task)t).isDeployed()) ){
 								//remove task from current server
 								secondToLastServer.removeTask(t);
 								// set the new server
 								t.setServer(server);
 								
 								// add task to new server
 								server.addTask(t);
 								
 								if(!t.isPredicted()){
 									((Task)t).setCanMigrate(false);
 									MigrateTask(t, server);
 								}
 									
 									//onService.MigrateTask(t, server);
 								if(server.isFull()){
 									fullServers.add(server);
 									break;
 								}
 								//onService.MigrateTask(t, server);
 							}
 						}
 					}
 				}
 				
 				if(secondToLastServer.isEmpty()){
 					inUseServers.remove(inUseServers.indexOf(secondToLastServer));
 					emptyServers.add(secondToLastServer);
 					powerService.sendServerToSleep(secondToLastServer);
 				}
 				
 				// server is not full. start with second to last server
 				if(!server.isFull()){
 					int timesTried = 0;
 					ITask t;
 					while(( t = lastServer.GetNextLowestDemandingTask())!=null && (!server.isFull())
 							&& timesTried < secondToLastServer.getTotalNumberOfTasks()){
 						if(server.compareAvailableResources(t) && !server.isFull()
 								&& ((Task)t).getCanMigrate()){
 							if( t.isPredicted() || (!t.isPredicted()&& ((Task)t).isDeployed()) ){
 								lastServer.removeTask(t);
 								t.setServer(server);
 								server.addTask(t);
 								
 								if(!t.isPredicted()){
 									((Task)t).setCanMigrate(false);
 									MigrateTask(t, server);
 								}
 									//onService.MigrateTask(t, server);
 								
 								if(server.isFull()){
 									fullServers.add(server);
 									break;
 								}
 							}
 							
 						}
 						timesTried++;
 					}
 				}
 				if(lastServer.isEmpty()){
 					inUseServers.remove(inUseServers.indexOf(lastServer));
 					emptyServers.add(lastServer);
 					powerService.sendServerToSleep(secondToLastServer);
 				}
 			}
 			logger.logInfo("*** Finished findMaximumUtilizationPlacement.");
 		}
 		return null;
 	}
 
 	
 	public synchronized boolean redistributeTasks(IServer server) {
 		int noPlaceFound = 0;
 		boolean found = false;
 		
 		logger.logInfo("\n\n*** Starting findMaximumUtilizationPlacement.");
 		
 		while((!server.isEmpty() || emptyServers.size()==0 ) 
 				&& noPlaceFound < server.getTotalNumberOfTasks() ){
 			found = false;
 			ITask t = server.GetNextLowestDemandingTask();
 			// if the task is predicted, don't move it
 			if( ((Task)t).getCanMigrate()){
 				if( t.isPredicted() || (!t.isPredicted()&& ((Task)t).isDeployed()) ){
 					for(IServer s:inUseServers){
 						if(s != server){
 							if(s.compareAvailableResources(t)){
 								
 								if(!t.isPredicted()){
 									((Task)t).setCanMigrate(false);
 									MigrateTask(t, s);
 								}
 								// move the task to the new server
 								server.removeTask(t);
 								s.addTask(t);
 								t.setServer(s);
 								
 								
 								// if server is full, add it to the full servers list
 								if(s.isFull()){
 									fullServers.add(s);
 									inUseServers.remove(s);
 								}
 								found = true;
 								logger.logInfo("FT: Found a place to redistribute the task and make a server full");
 								break;
 							}
 						}
 					}
 				}
 			}
 				if(!found){
 					noPlaceFound++;
 					t.setUnsuccessfulPlacement(true);
 				}
 			
 		}
 		
 		if(!server.isEmpty()){
 			logger.logInfo("FT: There are still some tasks left which did not get redistributed...");
 			for(ITask t:server.getTasks()){
 				t.setUnsuccessfulPlacement(false);
 			}
 			logger.logInfo("*** Finished redistributing tasks (with false).");
 			return false;
 		}else{
 			logger.logInfo("*** Finished redistributing tasks (with true).");
 			if(server.isEmpty()){
 				inUseServers.remove(server);
 				emptyServers.add(server);
 				powerService.sendServerToSleep(server);
 			}
 			return true;
 		}
 	}
 
 	/***
 	 * This method reorders the inUseServer list. 
 	 * It is based on the fact that once a task is added/finished on a 
 	 * specific server, only that server needs to be reinserted in the 
 	 * list such that the list remains ordered.
 	 * 
 	 * If direction < 0 , a task has ended and server has to be reordered
 	 * by going right in the list until a server with a smaller utilization is found.
 	 * 
 	 * If direction > 0 , a task has been placed and server has to be
 	 * reorder by going left in the list until a server with a bigger utilization is found.
 	 */
 	public synchronized void reorderServerList(IServer server, int direction) {
 		logger.logInfo(" Started to reorder servers...");
 		if(direction < 0){
 			inUseServers = sortingService.insertSortServersDescending(inUseServers);
 		}else{
 			inUseServers = sortingService.insertSortedServerDesc(server, inUseServers,
 								inUseServers.indexOf(server));
 		}
 		logger.logInfo(" Finished reordering servers.");
 	}
 
 	public synchronized void tryToFillServer(IServer server) {
 		logger.logInfo("\n\n*** Starting trying to fill server.");
 		ArrayList<Integer> availableResources = new ArrayList<Integer>();
 		IServer lastServer = inUseServers.get(inUseServers.size()-1);
 		
 		if(server != lastServer){
 			availableResources = server.GetAvailableResources();
 		}
 		
 		ITask bingoTask = lastServer.GetTaskWithResources(availableResources);
 		
 		if(bingoTask != null && ((Task)bingoTask).getCanMigrate()){
 			if( bingoTask.isPredicted() || (!bingoTask.isPredicted()&& ((Task)bingoTask).isDeployed()) ){
 				logger.logInfo(" Perfect task found. Server will be full.");
 				// OPEN NEBULA
 				
 				if(bingoTask.isPredicted() == false){
 					((Task)bingoTask).setCanMigrate(false);
 					MigrateTask(bingoTask, server);
 				}
 				
 				fullServers.add(server);
 				inUseServers.remove(server);
 			}
 		}else{
 			if(inUseServers.size() >2 ){
 				logger.logInfo(" Perfect task not found.");
 				findMaximumUtilizationPlacement(server, 
 						inUseServers.get(inUseServers.size()-2), lastServer);
 			}else{
 				// only 2 servers, try to fill this one using the redistributeTasks method :)
 				if(inUseServers.size() == 2){
 					// if it is not the last server,  redistribute tasks from the last server
 					if(lastServer != server){
 						redistributeTasks(lastServer);
 					}else{
 						// if it is the last server,  redistribute tasks from the second to last server (which in this case is at index 0
 						redistributeTasks(inUseServers.get(0));
 					}
 				}
 			}
 		}
 		logger.logInfo("*** Finished trying to fill server.");
 	}
 	
 	private synchronized void MigrateTask(ITask task, IServer server){
 		//onService.MigrateTask(task, server);
 		((Task)task).setFutureHost(server);
 		BaseCommon.Instance().TaskStartedMigrating.setChanged();
 		BaseCommon.Instance().TaskStartedMigrating.notifyObservers(task);
 	}
 	
 	// GETTERS FOR TESTING PURPOSES
 	
 	
 	public ArrayList<IServer> getEmptyServers() {
 		return emptyServers;
 	}
 
 	public ArrayList<IServer> getInUseServers() {
 		return inUseServers;
 	}
 
 	public ArrayList<IServer> getFullServers() {
 		return fullServers;
 	}
 
 	public ArrayList<ITask> getUnassignedTasks() {
 		return unassignedTasks;
 	}
 
 	public ArrayList<ITask> getPredictedTasks() {
 		return predictedTasks;
 	}
 
 	@Override
 	public Object getVirtualizationService() {
 		if(onService != null)
 			return onService;
 		else
 			return null;
 	}
 }
