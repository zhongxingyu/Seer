 package genlab.core.exec;
 
 import genlab.core.model.exec.IAlgoExecution;
import genlab.core.usermachineinteraction.GLLogger;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
 			}
 			// run the task
 			messages.debugTech(getName()+" running task: "+exec.getName(), getClass());
 			exec.run();
 			messages.debugTech(getName()+" ran task: "+exec.getName(), getClass());
 			
			
 		}
 
 	}
 	
 	
 
 	
 }
