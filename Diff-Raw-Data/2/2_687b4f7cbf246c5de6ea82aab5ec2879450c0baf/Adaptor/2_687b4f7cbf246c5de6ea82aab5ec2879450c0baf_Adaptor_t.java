 package loadbalance;
 
 import jobs.JobQueue;
 import policy.ReceiverInitTransferPolicy;
 import policy.SenderInitTransferPolicy;
 import policy.TransferPolicy;
 import jobs.Job;
 import jobs.TransferManager;
 import jobs.WorkerThread;
 import state.HardwareMonitor;
 import state.State;
 import state.StateManager;
 
 
 public class Adaptor {
 	State localState, remoteState;
 	StateManager stateManager;
 	TransferManager transferManager;
 	TransferChecker transferChecker;
 	HardwareMonitor hardwareMonitor;
 	WorkerThread workerThread;
 	
 	TransferPolicy transferPolicy;
 	final int THRESHOLD = 2;
 	final int POLL_LIM = 1;
 	
 	public Adaptor(int serverPort){
 		workerThread = new WorkerThread();
 		workerThread.start();
 		stateManager = new StateManager(serverPort);
 		transferManager = new TransferManager(serverPort + 1, this);
 		hardwareMonitor = new HardwareMonitor();
 		transferChecker = new TransferChecker();
 	}
 	
 	public void tryConnect(String hostname, int port){
 		stateManager.tryConnect(hostname, port);
 		transferManager.tryConnect(hostname, port + 1);
 	}
 
     public JobQueue getJobQueue() {
         return workerThread.getJobQueue();
     }
     
     public synchronized void processJobRequest(){
     	if(transferPolicy == null) return;
     	
     	Job job = workerThread.getJobQueue().popIfLengthExceed(THRESHOLD, transferPolicy.selectionPolicy);
     	if(job != null)
     		transferManager.sendJob(job);
     }
 
     public WorkerThread getWorkerThread() {
         return workerThread;
     }
     
     public void addJob(Job job){
        job.loadJobFromFile();
        job.saveJobToFile();
     	getJobQueue().append(job);
     }
 
     public class TransferChecker extends Thread {
 		private int SLEEP_TIME;
 		
 		public TransferChecker(){
 			this(2000);
 		}
 		
 		public TransferChecker(int sleep_time){
 			this.SLEEP_TIME = sleep_time;
 			start();
 		}
 		
 		public synchronized void checkForAvailableTransfer(){
 			localState = new state.State(workerThread.getJobQueueSize(), 0, hardwareMonitor.getCpuUtilization());
 			stateManager.setState(localState);
 			remoteState = stateManager.getRemoteState();
 			
 			// transferPolicy = (new SenderInitTransferPolicy(workerThread.getJobQueue(), remoteState));
 			transferPolicy = (new ReceiverInitTransferPolicy(workerThread.getJobQueue(), remoteState));
 			
 			Job job = transferPolicy.getJobIfTransferable();
 			if(job != null)
 				transferManager.sendJob(job);
 		}
 		
 		@Override
 		public void run() {
 			while(true){
 				this.checkForAvailableTransfer();
 				try {
 					sleep(this.SLEEP_TIME);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
