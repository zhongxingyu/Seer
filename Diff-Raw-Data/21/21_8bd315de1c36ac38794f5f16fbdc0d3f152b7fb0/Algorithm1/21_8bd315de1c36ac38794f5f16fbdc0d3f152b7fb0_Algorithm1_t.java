 package vdrm.disp.alg;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import vdrm.base.common.IAlgorithm;
 import vdrm.base.common.IPredictor;
 import vdrm.base.data.IServer;
 import vdrm.base.data.ITask;
 import vdrm.base.impl.BaseCommon;
 import vdrm.base.impl.Server;
 import vdrm.base.impl.Sorter;
 import vdrm.disp.util.VDRMLogger;
 import vdrm.onservice.IOpenNebulaService;
 import vdrm.onservice.OpenNebulaService;
 import vdrm.pred.pred.Predictor;
 
 /***
  * TODO: findNewPosition can use insertSortedServer method! May be the same as
  * 		reorder server list, but called when adding a new task. Might be able to
  * 		make a single method out of the 2.
  * TODO: 
  * @author Vlad & Robi
  *
  */
 public class Algorithm1 implements IAlgorithm{
 
 	private ArrayList<IServer> emptyServers;
 	private ArrayList<IServer> inUseServers;
 	private ArrayList<IServer> fullServers;
 	private ArrayList<ITask> unassignedTasks;
 	private ArrayList<ITask> predictedTasks;
 	private IPredictor predictor;
 	private Sorter sortingService;
 	
 	private VDRMLogger logger;
 	
 	private IOpenNebulaService onService;
 	
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
 		
 		//initialize logger
 		logger = new VDRMLogger();
 		
 		//initialize OpenNebula
 		onService = new OpenNebulaService();
 		
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
 		
 		//initialize logger
 		logger = new VDRMLogger();
 		
 		//initialize OpenNebula
 		onService = new OpenNebulaService();
 		
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
 					newTask = predictedTasks.get(0);
 					newTask.setPredicted(false);
 					
 					// OpenNebula
 					if(newTask.getServerId() != null){
 						onService.DeployTask(newTask);
 						logger.logInfo("Predicted task " + newTask.getTaskHandle() + " deployed on server.");
 					}
 					
 					predictedTasks.remove(0);
 					
					predictor.addEntry(newTask);
					
 					//since the prediction was correct, we exit algorithm and wait for next task
 					return;					
 				}
 			else { 	
 				//prediction was not correct
 				
 				//since we used an optimistic approach, we have to remove the tasks from the servers
 				IServer tempServer;
 				ITask tempTask;
 				for(i=0;i<predictedTasks.size();i++) {
 					tempTask = predictedTasks.get(i);
 					tempServer = tempTask.getServer();
 					tempServer.removeTask(tempTask);
 				}
 				
 				//clear list of predicted tasks
 				predictedTasks = new ArrayList<ITask>();
 				
 				//add this task to unassigned tasks list
 				unassignedTasks.add(newTask);
 				
 				logger.logInfo("Prediction was not correct.");
 			}
 		}
 		else
 		{
 			unassignedTasks.add(newTask);
 			logger.logInfo("Task " + newTask.getTaskHandle() + " (normal) added to unassigned tasks list.");
 		}
 
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
 		
 		//run consolidation algorithm (distribute tasks from unassignedTasks list to servers
 		//this algorithm assigns to every task a server reference and to every server a task 
 		//reference (the task is actually added to the list of a server, but the task is not 
 		//actually deployed to a physical server)
			
 		consolidateTasks();
 		logger.logInfo("Consolidation algorithm ran.");
 		
 		//dispatch tasks which are not predicted to real servers
 		for(i=0;i<unassignedTasks.size();i++) {
 			ITask tempTask = unassignedTasks.get(i);
 			if(tempTask.isPredicted())
 				predictedTasks.add(tempTask);
 			else {
 				
 				// OpenNebula
 				if(tempTask.getServerId() != null){
 					onService.DeployTask(tempTask);
 					logger.logInfo("Task " + newTask.getTaskHandle() + " (normal) deployed on server.");
 				}
 			}		
 		}
 		
 		//clear unassigned tasks list if there are no previously unassigned tasks
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
 	 * This method deals with placing a server in the correct list
 	 * First, if the server is in the inUseServers list, it is removed 
 	 * and its former position is saved. 
 	 * Then, if the server is full, it is placed in the fullServers list.
 	 * If not, a new position is searched starting from the current 
 	 * position and moving towards the front of the list 
 	 * @param tempServer - the server that needs to be repositioned
 	 */
 	private void findNewPosition(IServer tempServer) {
 		int poz = 0;
 		logger.logInfo("\n\n*** Starting to find new position for server "+ tempServer.getServerID() +".");
 		
 		if(inUseServers.contains(tempServer)){
 			logger.logInfo("Server " + tempServer.getServerID() + " found in 'in use list'. removing and resorting...");
 			poz = inUseServers.indexOf(tempServer);
 			inUseServers.remove(tempServer);
 		}
 		
 		if(tempServer.isFull()){ 
 			fullServers.add(tempServer);
 			logger.logInfo("Server " + tempServer.getServerID() + " is full. Adding it to the full servers list.");
 		}
 		else {
 			boolean ok = false;
 			if(inUseServers.isEmpty() && inUseServers.contains(tempServer) == false) {
 				inUseServers.add(tempServer);
 				ok = true;
 				logger.logInfo("Server " + tempServer.getServerID() + " added to the empty 'in use servers' list.");
 				return;
 			}
 			if(poz>0){ 
 				poz--;
 				while(ok==false && poz>=0) {	
 					if(tempServer.compareTo(inUseServers.get(poz))<0 && inUseServers.contains(tempServer) == false) {
 						inUseServers.add(poz+1,tempServer);
 						ok = true;
 						logger.logInfo("Server " + tempServer.getServerID() + " added to the 'in use servers' list at position "+ poz+1 + ".");
 					}
 					poz--;
 				}	
 				// place it last
 				if(poz==0 && inUseServers.contains(tempServer) == false){
 					inUseServers.add(inUseServers.size()+1,tempServer);
 					logger.logInfo("Server " + tempServer.getServerID() + " added last to the 'in use servers' list.");
 				}
 			}else if(poz==0){
 				poz = inUseServers.size()-1;
 				if(poz > 0){
 					while(ok==false&&poz>=0){
 						int compareResult = tempServer.compareTo(inUseServers.get(poz));
 						if(compareResult < 0 || compareResult == 0){
 							inUseServers.add(poz,tempServer);
 							ok=true;
 							logger.logInfo("Server " + tempServer.getServerID() + " added to the 'in use servers' list at position "+ poz+1 + ".");
 						}
 						poz--;
 					}
 				}else 
 					if(poz == 0)
 					{
 						int compareResult = tempServer.compareTo(inUseServers.get(0));
 						if(compareResult <= 0)
 							inUseServers.add(poz+1, tempServer);
 						else
 							inUseServers.add(0, tempServer);
 						logger.logInfo("Server " + tempServer.getServerID() + " added to the 'in use servers' list at position "+ poz+1 + "(only one server was in the list).");
 					}else
 					{
 						inUseServers.add(0,tempServer);
 						logger.logWarning("* Server " + tempServer.getServerID() + " added to the 'in use servers' list at position 0.");
 					}
 				
 			}
 		}
 		
 		logger.logInfo("*** Finished findNewPosition for server "+ tempServer.getServerID() +".");
 	}
 	
 	/**
 	 * There are a number of tasks in the unassignedTasks list
 	 * This method assigns each task to a server (and also a server to a task)
 	 * OPTIMISTIC approach!
 	 */
 	private void consolidateTasks()
 	{
 		logger.logInfo("\n\n*** Starting to consolidate tasks.");
 		
 		ArrayList<ITask> tempList = new ArrayList<ITask>();
 		for(ITask tsk : unassignedTasks) {
 			tempList.add(tsk);
 		}
 		
 		//sort the array in an ascending order (according to cpu, then mem, then hdd)
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
 				
 				//we must move this server to its new position in the lists
 				findNewPosition(tempServer);
 				
 				tempList.remove(0);
 				if(!tempList.isEmpty())
 					tempTask = tempList.get(0);
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
 					//we must use a new server
 					if(emptyServers.size() == 0) {
 						Date d = new Date();
 						logger.logSevere("SEVERE: Task " + tempTask.getTaskHandle().toString() + " cannot be deployed at this moment (" +
 								d.toString());
 						
 						//unassignedTasks.add(tempTask);
 						//waitingTasksInQueue = true;
 						
 						// notify that there are no more free resources (stop sending tasks) 
 						BaseCommon.Instance().getResourceAllocateEvent().setChanged();
 						BaseCommon.Instance().getResourceAllocateEvent().notifyObservers();
 						return;
 					}else{
 						IServer newServer = emptyServers.get(0);
 						newServers.add(newServer);
 						emptyServers.remove(0);
 						
 						tempTask.setServer(newServer);
 						newServer.addTask(tempTask);
 						tempList.remove(tempListSize);
 						ok = true;
 						
 //						if(newServer.isFull())
 //							fullServers.add(newServer);
 						logger.logInfo("Found a place for task " + tempTask.getTaskHandle() + ", on server "+ newServer.getServerID() +".(3)");
 					}
 				}
 			}
 			
 			//all tasks have been deployed
 			if(newServers.size()>0) {
 				for(i=0;i<newServers.size();i++)
 					//we must move the servers to their new position in the lists
 					findNewPosition(newServers.get(i));
 			}
 			logger.logInfo("*** Finished consolidateTask.");
 		}	
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
 		logger.logInfo("\n\n*** Starting end tasks.");
 		IServer server = t.getServer();
 		if(server != null){
 			// remove task from server (this is the point of this method, DUUUH !)
 			onService.FinishTask(t);
 			server.removeTask(t);
 			t.setServer(null);
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
 					if((server.getTotalNumberOfTasks() + server.getNumberOfPredictedTasks()) == 0){
 						server.OrderStandBy();
 						inUseServers.remove(server);
 						emptyServers.add(server);
 					}
 				}
 			}else{
 				
 				if((server.getTotalNumberOfTasks() + server.getNumberOfPredictedTasks()) == 0){
 					logger.logInfo("FT: Server is now empty. Order it to sleep. (2)");
 					server.OrderStandBy();
 					inUseServers.remove(server);
 					emptyServers.add(server);
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
 		logger.logInfo("*** Finished endTasks.");
 	}
 
 	/***
 	 * Using the last 2 servers in the inUseList, try to fill a server in order to
 	 * maximize the number of full servers.
 	 * 
 	 * Dear future me: 
 	 * Please optimize this method the following way:
 	 * if last server or second to last server contains the bingo task, great!
 	 * if not, get the lowest task from one server and search again for the bingo task from the other server with
 	 *         the remaining resources.
 	 * continue doing this. Until when?
 	 */
 	@Override
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
 				for(ITask t:lastServer.getTasks()){
 					if(!t.isPredicted() && server.compareAvailableResources(t) && !server.isFull()){
 						logger.logInfo("FT: Added task "+ t.getTaskHandle() +" to server "+ server.getServerID()+ ".(1)");
 						//remove task from current server
 						lastServer.removeTask(t);
 						// set the new server
 						t.setServer(server);
 						
 						// add task to new server
 						server.addTask(t);
 						onService.MigrateTask(t, server);
 						
 						if(server.isFull()){
 							logger.logInfo("FT: Server is full. Adding it to the full servers list.(2)");
 							fullServers.add(server);
 							break;
 						}
 						onService.MigrateTask(t, server);
 					}
 				}
 				
 				if(lastServer.isEmpty()){
 					logger.logInfo("FT: Last server is empty. Assigning it to the empty servers list...");
 					inUseServers.remove(inUseServers.indexOf(lastServer));
 					emptyServers.add(lastServer);
 				}
 				
 				// server is not full. start with second to last server
 				if(!server.isFull()){
 					int timesTried = 0;
 					logger.logInfo("FT: Server is not full, starting to get tasks from second to last server.");
 					ITask t;
 					while(( t = secondToLastServer.GetNextLowestDemandingTask())!=null && (!server.isFull()) 
 							&& timesTried < (secondToLastServer.getTotalNumberOfTasks() + secondToLastServer.getNumberOfPredictedTasks() )){
 						if( !t.isPredicted() && server.compareAvailableResources(t) && !server.isFull()){
 							secondToLastServer.removeTask(t);
 							t.setServer(server);
 							server.addTask(t);
 							onService.MigrateTask(t, server);
 							logger.logInfo("FT: Added task "+ t.getTaskHandle() +" to server "+ server.getServerID()+ ".(2)");
 							if(server.isFull()){
 								logger.logInfo("FT: Server is full. Adding it to the full servers list.(2)");
 								fullServers.add(server);
 								break;
 							}
 							timesTried++;
 						}
 					}
 				}
 				
 				if(secondToLastServer.isEmpty()){
 					logger.logInfo("FT: Second to last server is empty. Assigning it to the empty servers list...");
 					inUseServers.remove(inUseServers.indexOf(secondToLastServer));
 					emptyServers.add(secondToLastServer);
 				}
 			}else{
 				for(ITask t:secondToLastServer.getTasks()){
 					if( !t.isPredicted() && server.compareAvailableResources(t) && !server.isFull()){
 						//remove task from current server
 						secondToLastServer.removeTask(t);
 						// set the new server
 						t.setServer(server);
 						
 						// add task to new server
 						server.addTask(t);
 						onService.MigrateTask(t, server);
 						if(server.isFull()){
 							fullServers.add(server);
 							break;
 						}
 						onService.MigrateTask(t, server);
 					}
 				}
 				
 				if(secondToLastServer.isEmpty()){
 					inUseServers.remove(inUseServers.indexOf(secondToLastServer));
 					emptyServers.add(secondToLastServer);
 				}
 				
 				// server is not full. start with second to last server
 				if(!server.isFull()){
 					int timesTried = 0;
 					ITask t;
 					while(( t = lastServer.GetNextLowestDemandingTask())!=null && (!server.isFull())
 							&& timesTried < (secondToLastServer.getTotalNumberOfTasks() + secondToLastServer.getNumberOfPredictedTasks() )){
 						if(!t.isPredicted() && server.compareAvailableResources(t) && !server.isFull()){
 							lastServer.removeTask(t);
 							t.setServer(server);
 							server.addTask(t);
 							onService.MigrateTask(t, server);
 							if(server.isFull()){
 								fullServers.add(server);
 								break;
 							}
 							timesTried++;
 						}
 						
 					}
 				}
 				if(lastServer.isEmpty()){
 					inUseServers.remove(inUseServers.indexOf(lastServer));
 					emptyServers.add(lastServer);
 				}
 			}
 			logger.logInfo("*** Finished findMaximumUtilizationPlacement.");
 		}
 		return null;
 	}
 
 	@Override
 	public synchronized boolean redistributeTasks(IServer server) {
 		int noPlaceFound = 0;
 		boolean found = false;
 		
 		logger.logInfo("\n\n*** Starting findMaximumUtilizationPlacement.");
 		
 		while((!server.isEmpty() || emptyServers.size()==0 ) 
 				&& noPlaceFound < (server.getTotalNumberOfTasks() + server.getNumberOfPredictedTasks()) ){
 			found = false;
 			ITask t = server.GetNextLowestDemandingTask();
 			// if the task is predicted, don't move it
 			if(!t.isPredicted()){
 				for(IServer s:inUseServers){
 					if(s != server){
 						if(s.compareAvailableResources(t)){
 							
 							// TODO migrate_to_new_host(s); -- OpenNebula
 							onService.MigrateTask(t, s);
 							
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
 			}else{
 				noPlaceFound++;
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
 	@Override
 	public synchronized void reorderServerList(IServer server, int direction) {
 		logger.logInfo(" Started to reorder servers...");
 		sortingService = new Sorter();
 		if(direction < 0){
 			// TODO: Repair this method
 //			sortingService.insertSortedServerGoingRightDesc(server, inUseServers,
 //					inUseServers.indexOf(server));
 			inUseServers = sortingService.insertSortServersDescending(inUseServers);
 		}else{
 			inUseServers = sortingService.insertSortedServerDesc(server, inUseServers,
 								inUseServers.indexOf(server));
 		}
 		logger.logInfo(" Finished reordering servers.");
 	}
 
 	@Override
 	public synchronized void tryToFillServer(IServer server) {
 		logger.logInfo("\n\n*** Starting trying to fill server.");
 		ArrayList<Integer> availableResources = new ArrayList<Integer>();
 		IServer lastServer = inUseServers.get(inUseServers.size()-1);
 		
 		if(server != lastServer){
 			availableResources = server.GetAvailableResources();
 		}
 		
 		ITask bingoTask = lastServer.GetTaskWithResources(availableResources);
 		
 		if(bingoTask != null && bingoTask.isPredicted() == false){
 			logger.logInfo(" Perfect task found. Server will be full.");
 			// OPEN NEBULA
 			onService.MigrateTask(bingoTask, server);
 			
 			fullServers.add(server);
 			inUseServers.remove(server);
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
 }
