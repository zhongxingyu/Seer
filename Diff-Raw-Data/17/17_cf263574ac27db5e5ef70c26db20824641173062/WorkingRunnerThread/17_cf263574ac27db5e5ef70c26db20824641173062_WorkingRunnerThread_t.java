 package genlab.core.exec;
 
import genlab.core.model.exec.ComputationState;
 import genlab.core.model.exec.IAlgoExecution;
 import genlab.core.usermachineinteraction.ListOfMessages;
 import genlab.core.usermachineinteraction.ListsOfMessages;
 
 import java.util.concurrent.BlockingQueue;
 
 public class WorkingRunnerThread extends Thread {
 
 	
 	private final BlockingQueue<IAlgoExecution> readyToCompute;
 	private final ListOfMessages messages = ListsOfMessages.getGenlabMessages();
 	
 	public WorkingRunnerThread(String name, BlockingQueue<IAlgoExecution> readyToCompute) {
 		super(name);
 		
 		this.readyToCompute = readyToCompute;
 		setDaemon(true);
 		setPriority(MIN_PRIORITY);
 	}
 
 	@Override
 	public void run() {
 
 		while (true) {
		
 			IAlgoExecution exec = null;
 			try {
 				exec = readyToCompute.take();
 			} catch (InterruptedException e) {
				messages.errorTech("catched an exception from the execution: "+e.getMessage(), getClass(), e);
				exec.getProgress().setComputationState(ComputationState.FINISHED_FAILURE);
				try {
					exec.getResult().getMessages().errorUser("computation died with an error: "+e.getMessage(), getClass(), e);
				} catch (NullPointerException e2) {
				}
 			}
 			// run the task
 			messages.debugTech(getName()+" running task: "+exec.getName(), getClass());
 			exec.run();
 			messages.debugTech(getName()+" ran task: "+exec.getName(), getClass());
 			
 		}
 
 	}
 	
 	
 
 	
 }
