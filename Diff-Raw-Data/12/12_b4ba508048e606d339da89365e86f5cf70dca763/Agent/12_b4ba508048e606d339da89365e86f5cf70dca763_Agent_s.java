 package engine.agent;
 
 import engine.util.StringUtil;
 import gui.panels.subcontrolpanels.TracePanel;
 
 import java.util.concurrent.Semaphore;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import transducer.TReceiver;
 
 /**
  * Superclass for all threaded Agents. Agents have a thread devoted to their scheduler and subclasses must override pickAndExecuteAnAction(). 
  * They can also register with transducers
  */
 public abstract class Agent implements TReceiver {
 	/** The Agent's name */
 	protected String name;
 
 	/** Semaphore controlling whether the Agent scheduler continues */
 	private Semaphore stateChange = new Semaphore(1, true);
 
 	/** The thread devoted to the Agent */
 	private AgentThread agentThread;
 
 	/** The transducer for communication */
 	protected Transducer transducer;
 
 	/** The linked TracePanel */
 	protected TracePanel tracePanel;
 
 	/**
 	 * Default constructor
 	 */
 	protected Agent() {
 		this(null, null);
 	}
 
 	/**
 	 * Constructor with a name
 	 * 
 	 * @param agentName the name of the agent
 	 */
 	protected Agent(String agentName) {
 		this(agentName, null);
 	}
 
 	protected Agent(String agentName, Transducer ft) {
 		name = agentName;
 		transducer = ft;
 	}
 
 	/**
 	 * Return agent name for messages. Default is to return java instance name, unless the name is specified.
 	 */
 	public String getName() {
 		if (name == null) {
 			name = StringUtil.shortName(this);
 		}
 
 		return name;
 	}
 
//	/**
//	 * Returns the name
//	 */
//	public String toString() {
//		return getName();
//	}
 
 	/**
 	 * Returns the transducer
 	 */
 	protected Transducer getTransducer() {
 		return transducer;
 	}
 
 	/**
 	 * Sets the TracePanel
 	 */
 	public void setTracePanel(TracePanel tracePanel) {
 		this.tracePanel = tracePanel;
 	}
 
 	/**
 	 * This should be called whenever state has changed that might cause the agent to do something.
 	 */
 	protected void stateChanged() {
 		stateChange.release();
 	}
 
 	/**
 	 * Agents must implement this scheduler to perform any actions appropriate for the current state. Will be called whenever a state change has occurred, and will be called repeated as long as it
 	 * returns true.
 	 * 
 	 * @return true if some action was executed that might have changed the state.
 	 */
 	public abstract boolean pickAndExecuteAnAction();
 
 	/**
 	 * Agents must implement this method in order to communicate with the transducer. This allows them to listen to events fired by the front end.
 	 * 
 	 * NOTE: All implementations of this method should be synchronized!
 	 */
 	@Override
 	public abstract void eventFired(TChannel channel, TEvent event, Object[] args);
 
 	/**
 	 * Start agent scheduler thread. Should be called once at init time.
 	 */
 	public synchronized void startThread() {
 		if (agentThread == null) {
 			agentThread = new AgentThread(getName());
 			agentThread.start();
 		} else {
 			agentThread.interrupt();
 		}
 	}
 
 	/**
 	 * Stop agent thread. To be used only in special circumstances. To pause an Agent during execution, call Agent.setPowerState() instead.
 	 */
 	public void stopAgent() {
 		if (agentThread != null) {
 			agentThread.stopThread();
 			agentThread = null;
 		}
 	}
 
 	/**
 	 * Agent scheduler thread, calls pickAndExecuteAnAction() whenever a state change has been signalled.
 	 */
 	private class AgentThread extends Thread {
 		/** Boolean controlling the thread execution */
 		private volatile boolean goOn = false;
 
 		/**
 		 * Default constructor
 		 * 
 		 * @param name the thread name
 		 */
 		private AgentThread(String name) {
 			super(name);
 		}
 
 		/**
 		 * Runs the thread and calls the Agent scheduler
 		 */
 		@Override
 		public void run() {
 			goOn = true;
 
 			// run forever until stopThread() is called
 			while (goOn) {
 				try {
 					// see if there is something to do, else block
 					stateChange.acquire();
 
 					// loop while something was done
 					while (pickAndExecuteAnAction())
 						;
 				} catch (InterruptedException e) {
 					// used when stopThread() is called to exit the scheduler
 					System.out.println("Agent thread stopped for " + getName() + "!");
 				} catch (Exception e) {
 					System.out.println("Unexpected exception caught in Agent thread!");
 					e.printStackTrace();
 					System.exit(1);
 				}
 			}
 		}
 
 		/**
 		 * Stops the thread. Used only internally, outside classes should call Agent.stopAgent() instead. Note that this permanently stops the thread, whereas Agent.setPowerState() sets the temporal
 		 * state.
 		 */
 		private void stopThread() {
 			// tell the agent to stop
 			goOn = false;
 
 			// force exit the scheduler
 			this.interrupt();
 		}
 	}
 }
