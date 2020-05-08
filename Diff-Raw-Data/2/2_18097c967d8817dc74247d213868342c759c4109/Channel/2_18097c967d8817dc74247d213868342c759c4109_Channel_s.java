 package org.kisst.gft.filetransfer;
 
 import org.kisst.cfg4j.Props;
 import org.kisst.gft.GftContainer;
 import org.kisst.gft.action.Action;
 import org.kisst.gft.action.ActionList;
 import org.kisst.gft.task.Task;
 import org.kisst.gft.task.TaskDefinition;
 
 public class Channel implements TaskDefinition {
 	public final String name;
 	public final Action action;
 	public final Action errorAction;
 	public final Props props;
 	
 	public Channel(GftContainer gft, Props props) {
 		this.action=new ActionList(gft, props);
 		this.props=props;
 		this.name=props.getLocalName();
		Object errorProps=props.get("error");
 		if (errorProps instanceof Props) 
 			this.errorAction=new ActionList(gft, (Props) errorProps);
 		else if (errorProps==null)
 			this.errorAction=null;
 		else
 			throw new RuntimeException("property error should be a map in channel "+name);
 	}
 	public String toString() { return "Channel("+name+")";}
 	public Object execute(Task task) { action.execute(task); return null; }
 	
 	public void run(Task task) {
 		try {
 			action.execute(task);
 			task.setStatus(Task.DONE);
 		}
 		catch (RuntimeException e) {
 			task.setLastError(e);
 			errorAction.execute(task);
 			throw e;
 		}
 	}
 }
