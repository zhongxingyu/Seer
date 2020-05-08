 package edu.umich.sbolt;
 
 import java.awt.Rectangle;
 import java.io.File;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import lcm.lcm.LCM;
 import lcm.lcm.LCMDataInputStream;
 import lcm.lcm.LCMSubscriber;
 import sml.Agent;
 import sml.Agent.PrintEventInterface;
 import sml.Agent.RunEventInterface;
 import sml.Kernel;
 import sml.smlPrintEventId;
 import sml.smlRunEventId;
 import abolt.lcmtypes.observations_t;
 import abolt.lcmtypes.robot_action_t;
 import abolt.lcmtypes.robot_command_t;
 import abolt.lcmtypes.training_data_t;
 import abolt.lcmtypes.training_label_t;
 import april.util.TimeUtil;
 import edu.umich.sbolt.world.Pose;
 import edu.umich.sbolt.world.World;
 import edu.umich.sbolt.world.WorldObject;
 
 import com.soartech.bolt.BOLTLGSupport;
 
 public class SBolt implements LCMSubscriber, PrintEventInterface, RunEventInterface
 
 {
     private LCM lcm;
 
     private Kernel kernel;
 
     private Agent agent;
 
     private Timer timer;
 
     private TimerTask timerTask;
 
     private boolean running;
 
     private InputLinkHandler inputLinkHandler;
 
     private OutputLinkHandler outputLinkHandler;
 
     private ChatFrame chatFrame;
 
     private World world;
    
     private PrintWriter logWriter;
     
     private int throttleMS = 0;
     
     private boolean ready = true;
     
    
     
     private static boolean inputLinkLocked = false;
     public static void lockInputLink(){
     	while(inputLinkLocked){
     		try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
     	}
     	inputLinkLocked = true;
     }
     public static void unlockInputLink(){
     	inputLinkLocked = false;
     }
 
     public SBolt(String channel, String agentName, boolean headless)
     {
         // LCM Channel, listen for observations_t
         try
         {
             lcm = new LCM();
             lcm.subscribe(channel, this);
             lcm.subscribe("ROBOT_ACTION", this);
         }
         catch (IOException e)
         {
             e.printStackTrace();
             System.exit(1);
         }
 
         // Initialize Soar Agent
         kernel = Kernel.CreateKernelInNewThread();
         agent = kernel.CreateAgent(agentName);
         if (agent == null)
         {
             throw new IllegalStateException("Kernel created null agent");
         }
 
         Properties props = new Properties();
         try {
 			props.load(new FileReader("sbolt.properties"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         
         String agentSource = props.getProperty("agent");
         
         if (agentSource != null) {
         	agent.LoadProductions(agentSource);
         }
         
         String useLGProp = props.getProperty("enable-lgsoar");
         
         boolean useLG = false;
         String lgSoarDictionary = "";
         if (useLGProp != null && useLGProp.equals("true")) {
        	String lgSoarSource = props.getProperty("lgsoar-productions");
         	lgSoarDictionary = props.getProperty("lgsoar-dictionary");
         	
         	if (lgSoarSource != null && lgSoarDictionary != null) {
         		useLG = true;
         		agent.LoadProductions(lgSoarSource);
         	}
         	else {
         		System.out.println("ERROR: LGSoar misconfigured, not enabled.");
         	}
         }
         
         BOLTLGSupport lgSupport = null;
         
         if (useLG) {
         	lgSupport = new BOLTLGSupport(agent, lgSoarDictionary);
         }
         
         
         
         // !!! Important !!!
         // We set AutoCommit to false, and only commit inside of the event
         // handler
         // for the RunEvent right before the next Input Phase
         // Otherwise the system would apparently hang on a commit
         kernel.SetAutoCommit(false);
 
 		String doLog = props.getProperty("enable-log");
 		if (doLog != null && doLog.equals("true")) {
 			try {
 				logWriter = new PrintWriter(new FileWriter("sbolt-log.txt"));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, this);
 		}
 		
 		String watchLevel = props.getProperty("watch-level");
 		if (watchLevel != null) {
 			agent.ExecuteCommandLine("watch " + watchLevel);
 		}
 
 		String throttleMSString = props.getProperty("decision-throttle-ms");
 		if (throttleMSString != null) {
 			throttleMS = Integer.parseInt(throttleMSString);
 			agent.RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_DECISION_CYCLE, this, this);
 		}
         if (!headless) {
         	System.out.println("Spawn Debugger: " + agent.SpawnDebugger(kernel.GetListenerPort()));
         	// Requires the SOAR_HOME environment variable
         }
       
         world = new World();
 
         // Setup InputLink
         inputLinkHandler = new InputLinkHandler(world, this);
 
         // Setup OutputLink
         outputLinkHandler = new OutputLinkHandler(this);
 
         // Setup ChatFrame
         chatFrame = new ChatFrame(this, lgSupport);
 
         // Start broadcasting
         timerTask = new TimerTask()
         {
             @Override
             public void run()
             {
                 SBolt.this.broadcastLcmCommand();
             }
         };
 
         running = false;
         chatFrame.showFrame();
         
     }
     
     public Kernel getKernel(){
     	return kernel;
     }
 
     public Agent getAgent()
     {
         return agent;
     }
 
     public ChatFrame getChatFrame()
     {
         return chatFrame;
     }
 
     public InputLinkHandler getInputLink()
     {
         return inputLinkHandler;
     }
 
     public OutputLinkHandler getOutputLink()
     {
         return outputLinkHandler;
     }
 
     public World getWorld()
     {
         return world;
     }
 
     public void start()
     {
         if (running)
         {
             return;
         }
         running = true;
         timer = new Timer();
         timer.schedule(timerTask, 1000, 500);
         agent.RunSelf(1);
     }
 
     public void stop()
     {
         if (!running)
         {
             return;
         }
         running = false;
         agent.StopSelf();
         timer.cancel();
     }
 
     @Override
     public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
     {
     	if(channel.equals("ROBOT_ACTION") && world != null && world.getRobotArm() != null){
     		try {
     			robot_action_t action = new robot_action_t(ins);
 				world.getRobotArm().newRobotAction(action);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     	} else if(channel.equals("OBSERVATIONS")){
     		if (inputLinkHandler == null || !ready)
                 return;
             ready = false;
             SBolt.lockInputLink();
             
             observations_t obs = null;
             try
             {
                 obs = new observations_t(ins);
                 world.newObservation(obs);
             }
             catch (IOException e)
             {
                 e.printStackTrace();
                 return;
             }
             SBolt.unlockInputLink();
             ready = true;
     	}
     }
 
     /**
      * Sends out the robot_command_t command via LCM
      */
     private void broadcastLcmCommand()
     {
         synchronized (outputLinkHandler){
         	List<training_label_t> newLabels = outputLinkHandler.extractNewLabels();
         	if(newLabels != null){
             	training_data_t trainingData = new training_data_t();
             	trainingData.utime = TimeUtil.utime();
             	trainingData.num_labels = newLabels.size();
             	trainingData.labels = new training_label_t[newLabels.size()];
             	for(int i = 0; i < newLabels.size(); i++){
             		trainingData.labels[i] = newLabels.get(i);
             	}
             	lcm.publish("TRAINING_DATA", trainingData);
         	}
         }
     }
     
     public void broadcastRobotCommand(robot_command_t command){
         lcm.publish("ROBOT_COMMAND", command);
     }
 
     public static void main(String[] args)
     {    	
     	boolean headless = false;
     	if (args.length > 0 && args[0].equals("--headless")) {
     		// it might make sense to instead always make the parameter
     		// be the properties filename, and load all others there
     		// (currently, properties filename is hardcoded)
     		headless = true;
     	}
         SBolt sbolt = new SBolt("OBSERVATIONS", "sbolt", headless);
         sbolt.start();
         if (headless) {
         	sbolt.agent.RunSelfForever();
         }
     }
 	@Override
 	public void printEventHandler(int eventID, Object data, Agent agent, String message) {
 		synchronized(logWriter) {
 			logWriter.print(message);
 		}
 	}
 	@Override
 	public void runEventHandler(int arg0, Object arg1, Agent arg2, int arg3) {
 		try {
 			Thread.sleep(throttleMS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
