 package loadbalance;
 
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.FileNotFoundException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 
 import jobs.*;
 import policy.ReceiverInitTransferPolicy;
 import policy.SenderInitTransferPolicy;
 import policy.SymmetricTransferPolicy;
 import policy.TransferPolicy;
 import jobs.Job;
 import jobs.JobResult;
 import jobs.MatrixAdditionJob;
 import jobs.TransferManager;
 import state.HardwareMonitor;
 import state.State;
 import state.StateManager;
 import util.LBConfiguration;
 
 
 public class Adaptor extends JFrame{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3881378470873496392L;
 	private Container pane;
 	private JLabel localLabel;
 	private JLabel remoteLabel;
 	private JLabel cpuUtilLabel;
 	private JLabel resultLabel;
 	private JLabel throttlingLabel;
 	private JButton loadButton;
 	private DefaultListModel localListModel;
 	private DefaultListModel remoteListModel;
 	public int n_job_transfer = 0;
 	public int n_request = 0;
 	
 	private boolean isPeriodicInformationPolicy = true;
 	
 	JobResult result;
 	HashSet<String> localJobIDs;
 	int n_unfinished_part;
 
 	
 	State localState, remoteState;
 	StateManager stateManager;
 	TransferManager transferManager;
 	TransferChecker transferChecker;
 	HardwareMonitor hardwareMonitor;
     WorkerThreadManager wtManager;
 
 	TransferPolicy transferPolicy;
 	final int THRESHOLD = 2;
 	final int POLL_LIM = 1;
 	
 	public Adaptor(int serverPort){
 		super("Load Balancer");
         wtManager= new WorkerThreadManager(this);
         wtManager.start();
         isPeriodicInformationPolicy = !LBConfiguration.getInformationPolicy().equals(LBConfiguration.EVENT);
 		stateManager = new StateManager(serverPort, isPeriodicInformationPolicy);
 		transferManager = new TransferManager(serverPort + 1, this);
 		hardwareMonitor = new HardwareMonitor();
 		initGUI(serverPort);
 		transferChecker = new TransferChecker();
 		localJobIDs = new HashSet<String>();
 	}
 	
 	private void initGUI(int serverPort){
 		this.setLayout(null); 
 		this.setSize(600, 400);
 		pane = this.getContentPane();
 		
 		JLabel portLabel = new JLabel("Port No: "+serverPort);
 		portLabel.setBounds(10, 0, 100, 30);
 		pane.add(portLabel);
 		
 		localLabel = new JLabel("local node");
 		localLabel.setBounds(30, 60, 100, 30);
 		
 		remoteLabel = new JLabel("remote node");
 		remoteLabel.setBounds(330, 60, 100, 30);
 		
 		cpuUtilLabel = new JLabel("0 %");
 		cpuUtilLabel.setBounds(330, 30, 150, 30);
 		cpuUtilLabel.setAlignmentX(RIGHT_ALIGNMENT);
 		
 		throttlingLabel = new JLabel("Throttling: 70 %");
 		throttlingLabel.setBounds(30, 30, 150, 30);
 		throttlingLabel.setAlignmentX(LEFT_ALIGNMENT);
 		
 		resultLabel = new JLabel("result: ");
 		resultLabel.setBounds(330, 5, 220, 25);
 		resultLabel.setAlignmentX(LEFT_ALIGNMENT);
 		
 		JList localList = new JList(localListModel = new DefaultListModel());
 		localList.setBounds(30, 90, 200, 250);
 		
 		JList remoteList = new JList(remoteListModel = new DefaultListModel());
 		remoteList.setBounds(330, 90, 200, 250);
 		
 		loadButton = new JButton("Load Work");
 		loadButton.setBounds(150, 5, 80, 30);
 		loadButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				loadButton.setEnabled(false);
 				try {
 					loadJobs(MatrixAdditionJob.splitJobs("mat1"));
 				} catch (FileNotFoundException e) {
 					System.out.println("file not found.");
 				}
 			}
 		
 		});
 		
 		pane.add(throttlingLabel);
 		pane.add(resultLabel);
 		pane.add(cpuUtilLabel);
 		pane.add(loadButton);
 		pane.add(localLabel);
 		pane.add(remoteLabel);
 		pane.add(remoteList);
 		pane.add(localList);
 		
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		this.addWindowListener(new WindowAdapter()
 	    {
 	          public void windowClosing(WindowEvent e)
 	          {
 	              stop();
 	          }
 	    });
 		this.setVisible(true);
 	}
 	
 	public void tryConnect(String hostname, int port){
 		stateManager.tryConnect(hostname, port);
 		transferManager.tryConnect(hostname, port + 1);
 	}
 
     public JobQueue getJobQueue() {
         return wtManager.getJobQueue();
     }
     
     public synchronized void processJobRequest(){
     	if(transferPolicy == null) return;
     	n_request++;
     	Job job = wtManager.getJobQueue().popIfLengthExceed(THRESHOLD, transferPolicy.selectionPolicy);
     	if(job != null)
     		sendJob(job);
     }
 
     public void stop() {
         wtManager.stop();
         hardwareMonitor.isRunning = false;
         stateManager.isRunning = false;
         transferManager.isRunning = false;
         System.exit(0);
     }
     
     public void loadJobs(List<Job> jobs){
         System.out.println("Load Job at " + new Date());
 
         result = null;
     	this.n_unfinished_part = jobs.size();
     	System.out.println("size " + this.n_unfinished_part);
     	this.localJobIDs = new HashSet<String>();
     	resultLabel.setText("result: ...processing...");
     	n_job_transfer = 0;
     	n_request = 0;
     	stateManager.n_state_transfer = 0;
 		for(Job job : jobs){
 			localJobIDs.add(job.getID());
 			addJob(job);
 		}
     }
     
     public synchronized void addJob(Job job){
     	localListModel.addElement(job.getID());
     	remoteListModel.removeElement(job.getID());
     	
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
			double cpuUtil = hardwareMonitor.getCpuUtilization();
			if(cpuUtil > 0.6) {
                 wtManager.setLowThrottling();
            } else if(cpuUtil < 0.2) {
                 wtManager.setHighThrottling();
             }
			localState = new state.State(wtManager.getJobQueueSize(), wtManager.getThrottling(), cpuUtil);
            stateManager.setState(localState);
			cpuUtilLabel.setText("CPU: " + String.format("%.2f", localState.cpuUtilization) + " %");
			throttlingLabel.setText("Throttling: " + localState.throttling + " %");
 
 			remoteState = stateManager.getRemoteState();
 
             String policy = LBConfiguration.getTransferPolicy();
             if(policy.equals(LBConfiguration.RECEIVER))
                 transferPolicy = (new ReceiverInitTransferPolicy(wtManager.getJobQueue(), remoteState));
             else if(policy.equals(LBConfiguration.SENDER))
                 transferPolicy = (new SenderInitTransferPolicy(wtManager.getJobQueue(), remoteState));
             else
 			    transferPolicy = (new SymmetricTransferPolicy(wtManager.getJobQueue(), remoteState));
 			
 			Job job = transferPolicy.getJobIfTransferable();
 			if(job != null){
 				sendJob(job);
 			}
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
 
     private void finishedAll(){
     	System.out.println("result = " + result.getResult());
     	System.out.println("n_job_transfer = " + n_job_transfer);
     	System.out.println("n_request = " + n_request);
     	System.out.println("n_state_transfer = " + stateManager.n_state_transfer);
     	resultLabel.setText("result: " + result.getResult());
     	loadButton.setEnabled(true);
 
         System.out.println("Finish all jobs at " + new Date());
     }
 
     private synchronized void sendJob(Job job){
     	if(!job.isRequest){
     		n_job_transfer++;
     		this.localListModel.removeElement(job.getID());
     		if(this.localJobIDs.contains(job.getID()))
     			this.remoteListModel.addElement(job.getID());
     	}else n_request++;
     	transferManager.sendJob(job);
     }
     
     public synchronized void jobFinished(Job job) {
     	//local job
     	if(localJobIDs.contains(job.getID())){
     		System.out.println("finished. ID: " + job.getID() +  ", n = " + this.n_unfinished_part);
     		this.localListModel.removeElement(job.getID());
     		this.remoteListModel.removeElement(job.getID());
 	    	if(result == null)
 	    		result = job.getResult();
 	    	else
 	    		result = result.aggregate(job.getResult());
     		if(--this.n_unfinished_part == 0){
     			finishedAll();
     		}
     	}
     	// remote job
     	else{
     		System.out.println("remote finished. ID: " + job.getID());
     		this.sendJob(job);
     	}
     }
 
     public List<Job> getCurRunningJobs() {
         return wtManager.getCurRunningJobs();
     }
     
     public void queueSizeChange(){
     	if(stateManager.isPeriodic) return;
     	localState = new state.State(wtManager.getJobQueueSize(), wtManager.getThrottling(), hardwareMonitor.getCpuUtilization());
 		stateManager.setState(localState);
     	stateManager.sendState();
     	//System.out.println("queue change");
     }
 }
