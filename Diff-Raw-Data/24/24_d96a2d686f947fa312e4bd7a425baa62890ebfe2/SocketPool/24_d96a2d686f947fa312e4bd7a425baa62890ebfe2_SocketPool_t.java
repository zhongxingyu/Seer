 package com.transfer.socketManager;
 
 import com.transfer.reportManager.ReportSocketManager;
 import com.transfer.taskManager.IQueue;
 import com.transfer.taskManager.TaskManager;
 import com.transfer.util.IClient;
 import com.transfer.util.ITask;
 
 public class SocketPool {
 	private IClient _Client = null;
 	
 	//Max socket count
	private final int MAX_SOCKET_COUNT = 1;
 	//current socket count
 	private int CURRENT_SOCKET_COUNT = 0;
 
 	public SocketPool(IClient client){
 		_Client = client;
 		
 		//add SocketPool to SocketPoolManager
 		SocketPoolManager.add(_Client, this);
 		
 		run();
 	}
 	
 	/**
 	 * Run
 	 */
 	public void run(){
 		IQueue queue = TaskManager.GetInstance(_Client);
 		if(queue == null)
 			return;
 		
 		if(queue.HasTasks()){
 			if(CURRENT_SOCKET_COUNT >= MAX_SOCKET_COUNT)
 				return;
 
 			ITask task = queue.Dequeue();
 
 			CURRENT_SOCKET_COUNT++;
 			new DataSocket(this, task);
 		}
 	}
 	
 	/**
 	 * Reduce socket count
 	 */
 	public synchronized void reduceSocketCount(){
 		CURRENT_SOCKET_COUNT --;
 		
 		if(CURRENT_SOCKET_COUNT == 0){
			if(TaskManager.GetInstance(_Client).HasTasks()){
				SocketPoolManager.getInstance(_Client).run();
			}else{
				SocketPoolManager.remove(_Client);
				TaskManager.RemoveTaskQueue(_Client);
				
				if(ReportSocketManager.IsExisted(_Client))
					ReportSocketManager.RemoveMessageSocket(_Client);
			}
 		}
 	}
 	
 	/**
 	 * get Socket count
 	 * @return
 	 */
 	public synchronized int getSocketCount(){
 		return CURRENT_SOCKET_COUNT;
 	}
 }
