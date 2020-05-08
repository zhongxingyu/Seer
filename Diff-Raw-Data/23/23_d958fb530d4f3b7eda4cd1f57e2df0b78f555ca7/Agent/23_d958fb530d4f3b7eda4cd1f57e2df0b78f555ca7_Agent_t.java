 package agent;
 
 import javax.swing.*;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.concurrent.*;
 
 /** Base class for simple agents */
 public abstract class Agent {
 	Semaphore stateChange = new Semaphore(1,true);//binary semaphore, fair
 	private AgentThread agentThread;
 
     // The following is copied from Client class.
 
     public enum Type{//Each different agent needs to be enumerated here.
         CONVEYORAGENT, FEEDERAGENT, GANTRYAGENT, KITROBOTAGENT, LANEAGENT, NESTAGENT, PARTSROBOTAGENT, STANDAGENT, VISIONAGENT
     }
     protected Socket server; //connection to server
     public Type type; //type of client
     public PrintWriter output; //output stream to server
     public BufferedReader input; //input stream from server
     public String currentCommand; //current command string from server
     public ArrayList<String> parsedCommand; //current command parsed into strings
     boolean connected;
 
 	protected Agent(Type t) {
         connected = false;
         type = t;
         connect(); //connects to server
         Thread inputThread = new Thread(independentInput);
         inputThread.run();
 	}
 
     public void connect(){
         try {
             server = new Socket("127.0.0.1", 12321); //connects to server on localhost
             input = new BufferedReader(new InputStreamReader(server.getInputStream()));//opens inputStream
             output = new PrintWriter(new OutputStreamWriter(server.getOutputStream()));//opens outputStream
             connected = true;
             System.out.println("connected to server!");
         } catch (Exception e) {
             System.out.println("Host unavailable");
         }
     }
 
 
     Runnable independentInput = new Runnable(){
         public void run(){
             for(;;){
                 if(connected)
                     try {
                         input.readLine(); //reads from input each time there is a new string
                         parseInput();
                        System.out.println(input.readLine());
                     } catch (Exception e) {
                         System.out.println("inputStream not open");
                     }
             }//end for
         }//end run
     };
 
     public void parseInput(){
         //TODO parseinput
         parsedCommand = new ArrayList<String>(Arrays.asList(currentCommand.split(" "))); //puts string into array list
 
     }
 
     // End of Client copy code
 
 	/** This should be called whenever state has changed that might cause
 	 * the agent to do something. */
 	public void stateChanged() {
 		stateChange.release(); 
 	}
 
 	/** Agents must implement this scheduler to perform any actions appropriate for the
 	 * current state.  Will be called whenever a state change has occurred,
 	 * and will be called repeated as long as it returns true.
 	 * @return true if and only if some action was executed that might have changed the
 	 * state.
 	 */
 	protected abstract boolean pickAndExecuteAnAction();
 
 	/** Return agent name for messages.  Default is to return java instance
 	 * name. */
 	public String getName() {
 		return StringUtil.shortName(this);
 	}
 
 	/** The simulated action code */
 	protected void Do(String msg) {
 	    print(msg, null);
 	}
 	/** Print message */
 	protected void print(String msg) {
 		print(msg, null);
 	}
 	
     protected void debug(String msg){
     	if(true){
     		print(msg, null);
     	}
     }
 
 	/** Print message with exception stack trace */
 	protected void print(String msg, Throwable e) {
 		StringBuffer sb = new StringBuffer();
 		sb.append(getName());
 		sb.append(": ");
 		sb.append(msg);
 		sb.append("\n");
 		if (e != null) {
 			sb.append(StringUtil.stackTraceString(e));
 		}
 		System.out.print(sb.toString());
 	}
 
 	/** Start agent scheduler thread.  Should be called once at init time. */
 	public synchronized void startThread() {
 		if (agentThread == null) {
 			agentThread = new AgentThread(getName());
 			agentThread.start(); // causes the run method to execute in the AgentThread below
 		} else {
 			agentThread.interrupt();//don't worry about this for now
 		}
 	}
 
 	/** Stop agent scheduler thread. */
 	//In this implementation, nothing calls stopThread().
 	//When we have a user interface to agents, this can be called.
 	public void stopThread() {
 		if (agentThread != null) {
 			agentThread.stopAgent();
 			agentThread = null;
 		}
 	}
 
 	/** Agent scheduler thread, calls respondToStateChange() whenever a state
 	 * change has been signalled. */
 	private class AgentThread extends Thread {
 		private volatile boolean goOn = false;
 
 		private AgentThread(String name) {
 			super(name);
 		}
 
 		public void run() {
 			goOn = true;
 
 			while (goOn) {
 				try {
 				    // The agent sleeps here until someone calls, stateChanged(),
 				    // which causes a call to stateChange.give(), which wakes up agent.
 					stateChange.acquire();
 						//The next while clause is the key to the control flow.
 						//When the agent wakes up it will call respondToStateChange()
 						//repeatedly until it returns FALSE.
 						//You will see that pickAndExecuteAnAction() is the agent scheduler.
 					while (pickAndExecuteAnAction());
 				} catch (InterruptedException e) {
 					// no action - expected when stopping or when deadline changed
 				} catch (Exception e) {
 					print("Unexpected exception caught in Agent thread:", e);
 				}
 			}
 		}
 
 		private void stopAgent() {
 			goOn = false;
 			this.interrupt();
 		}
 	}
 }
 
