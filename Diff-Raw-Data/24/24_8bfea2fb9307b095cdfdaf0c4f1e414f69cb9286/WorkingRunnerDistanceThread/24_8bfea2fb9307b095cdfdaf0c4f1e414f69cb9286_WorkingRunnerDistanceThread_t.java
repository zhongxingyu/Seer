 package genlab.core.exec.client;
 
 import genlab.core.exec.IAlgoExecutionRemotable;
 import genlab.core.exec.server.DistantExecutionResult;
 import genlab.core.model.exec.ComputationResult;
 import genlab.core.model.exec.ComputationState;
 import genlab.core.model.exec.IAlgoExecution;
 import genlab.core.usermachineinteraction.ListOfMessages;
 import genlab.core.usermachineinteraction.ListsOfMessages;
 
 import java.rmi.RemoteException;
 import java.util.concurrent.BlockingQueue;
 
 /**
  * 
  * This thread runs computations in a distant server.
  * It waits for a task to be submitted to a queue; 
  * then it runs it in a distant server and waits for its results
  * in case of failure it resubmits the task in the local queue in case the distant transmission 
  * is the source of the problem. 
  * It loops indefinitely 
  * 
  * @author Samuel Thiriot
  *
  */
 public class WorkingRunnerDistanceThread extends Thread {
 
 	
 	private final BlockingQueue<IAlgoExecutionRemotable> readyToCompute;
 	private final BlockingQueue<IAlgoExecution> backupQueue;
	private final Object lockerQueue;
	
 	private final ListOfMessages messages = ListsOfMessages.getGenlabMessages();
 	
 	private final String serverName;
 	
 	private DistantGenlabServerManager server = null;
 	
 	/**
 	 * If true, will stop when possible.
 	 */
 	private boolean askStop = false;
 		
 	public WorkingRunnerDistanceThread(
 						String serverName,
 						String name, 
 						BlockingQueue<IAlgoExecutionRemotable> readyToCompute, 
						BlockingQueue<IAlgoExecution> backupQueue,
						Object lockerQueue,
 						DistantGenlabServerManager server) {
 		super(name);
 		
 		// save parameters
 		this.serverName = serverName;
 		this.readyToCompute = readyToCompute;
 		this.backupQueue = backupQueue;
		this.lockerQueue = lockerQueue;
 		this.server = server;
 		
 		// configure thread
 		setDaemon(true);
 		setPriority(MIN_PRIORITY);
 		
 	}
 
 	
 	protected void reinsertTaskInBackupQueue(IAlgoExecution exec) {
 		
 		exec.getExecution().getListOfMessages().warnTech("the task "+exec.getName()+" failed during the remote execution; will retry a local run by putting it back in queue ("+backupQueue.size()+" pending)", getClass());
 		backupQueue.add(exec);
		synchronized (lockerQueue) {
			lockerQueue.notify();	
		}
		
 	}
 	
 	@Override
 	public void run() {
 
 		while (!askStop) {
 		
 			IAlgoExecution exec = null;
 			try {
 				//messages.infoTech(this.getName()+" waiting for a task in the queue", getClass());
 				exec = readyToCompute.take();
 				messages.infoTech("still "+readyToCompute.size()+" tasks pending on our queue", getClass());
 
 			} catch (InterruptedException e) {
 				messages.errorTech("catched an exception from the execution: "+e.getMessage(), getClass(), e);
 				exec.getProgress().setComputationState(ComputationState.FINISHED_FAILURE);
 				try {
 					exec.getResult().getMessages().errorUser("computation died with an error: "+e.getMessage(), getClass(), e);
 				} catch (NullPointerException e2) {
 				}
 			}
 			// run the task
 			//messages.infoTech(getName()+" running task "+exec.getName()+" in server "+serverName, getClass());
 			try {
 				
 				// execute distantly
 				exec.getProgress().setProgressTotal(1);
 				exec.getProgress().setComputationState(ComputationState.STARTED);
 				
 				messages.infoTech(getName()+" running task: "+exec.getName(), getClass());
 
 				DistantExecutionResult execResult = server.getDistantServer().executeTask(exec);
 				
 				// transfer information from the distant result to...
 				// ... the results
 				ComputationResult r = new ComputationResult(
 						exec.getAlgoInstance(), 
 						exec.getProgress(), 
 						execResult.messages
 						); 
 				for (String idRes: execResult.id2result.keySet()) {
 					r.setResult(
 							exec.getAlgoInstance().getOutputInstanceForOutput(idRes), 
 							execResult.id2result.get(idRes)
 							);
 				}
 				
 				if (execResult.computationState != ComputationState.FINISHED_OK) {
 					reinsertTaskInBackupQueue(exec);
 					
 				} else {
 					exec.getExecution().getListOfMessages().addAll(r.getMessages());
 					exec.setResult(r);
 					// the messages
 					//exec.getExecution().getListOfMessages().addAll(execResult.messages);
 					// ... the progress
 					exec.getProgress().setProgressMade(1);
 					exec.getProgress().setComputationState(execResult.computationState);
 
 					messages.debugTech(getName()+" ran task: "+exec.getName(), getClass());
 
 					// might let the other react
 					Thread.yield();
 					
 				}
 
 			} catch (RemoteException e) {
 				messages.errorUser("task "+exec.getName()+" raised a distant error:"+e.getMessage(), getClass(), e);
 				reinsertTaskInBackupQueue(exec);
 				//exec.getProgress().setComputationState(ComputationState.FINISHED_FAILURE);
 				//exec.getProgress().setException(e);
 			} catch (Exception e) {
 				messages.errorUser("task "+exec.getName()+" raised an error:"+e.getMessage(), getClass(), e);
 				reinsertTaskInBackupQueue(exec);
 				//exec.getProgress().setComputationState(ComputationState.FINISHED_FAILURE);
 				//exec.getProgress().setException(e);
 			} catch (OutOfMemoryError e) {
 				messages.errorUser("no more memory while processing task "+exec.getName()+"; update the memory settings", getClass(), e);
 				reinsertTaskInBackupQueue(exec);
 				//exec.getProgress().setComputationState(ComputationState.FINISHED_FAILURE);
 				//exec.getProgress().setException(e);
 			}
 			
 		}
 		
 		messages.infoTech(getName()+": closing thread.", getClass());
 
 	}
 
 
 	public void askStop() {
 		askStop = true;
 	}
 
 	
 }
