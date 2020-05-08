 package agent;
 
 
 import base.Message;
 import logging.Log;
 
 /**
  * 
  * abstract class to define a basic agent 
  *
  */
 @SuppressWarnings("rawtypes")
 public abstract class AbstractAgent implements Measurable 
 {
 	protected AgentID id; 
 	protected Log log;
 	protected boolean pause ;
 	protected abstract void agentPrint();
 	public abstract void step() throws Exception;
 	protected abstract void sendMessage(AgentID to, Message<?> msg);
 	public abstract void receiveMessage(Message<?> message);
 	
 	//graphical functions to select an agent on the grid
 	public abstract boolean isSelected();
 	public abstract void toggleSelected();
 	
 	public AbstractAgent(AgentID id) {
 		super();
 		this.id = id;
 	}
 	
 	public AgentID getId()
 	{
 		return id;
 	}
 
 	public void setId(AgentID id)
 	{
 		this.id = id;
 	}
 
 	public Log getLog()
 	{
 		return log;
 	}
 	public boolean isPause() {
 		return pause;
 	}
 	public void setPause(boolean pause) {
 		this.pause = pause;
 	}
 	
 	
 	
 }
