 package org.kisst.gft.filetransfer;
 
 import java.lang.reflect.Constructor;
 
 import org.kisst.cfg4j.Props;
 import org.kisst.gft.GftContainer;
 import org.kisst.gft.action.Action;
 import org.kisst.gft.action.ActionList;
 import org.kisst.gft.task.Task;
 import org.kisst.gft.task.TaskDefinition;
 import org.kisst.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 public class Channel implements TaskDefinition {
	final static Logger logger=LoggerFactory.getLogger(Channel.class); 

 	public final GftContainer gft;
 	public final String name;
 	public final Action action;
 	public final Action errorAction;
 	public final Props props;
 	public final SshHost src;
 	public final SshHost dest;
 	public final String srcdir;
 	public final String destdir;
 	public final String mode;
 	
 	public Channel(GftContainer gft, Props props) {
 		this.gft=gft;
 		this.src=gft.sshhosts.get(props.getString("src.host"));
 		this.dest=gft.sshhosts.get(props.getString("dest.host"));
 		this.srcdir=props.getString("src.dir", "");
 		this.destdir=props.getString("dest.dir", "");
 		this.mode=props.getString("mode", "push");
 		if (!("pull".equals(mode) || "push".equals(mode)))
 			throw new RuntimeException("mode should be push or pull, not "+mode);
 		this.props=props;
 		this.name=props.getLocalName();
 		this.action=new ActionList(this, props);
 		Object errorProps=props.get("error",null);
		if (errorProps==null)
			errorProps=gft.props.get("gft.global.error",null); // TODO: this is a dirty hack
 		if (errorProps instanceof Props) 
 			this.errorAction=new ActionList(this, (Props) errorProps);
 		else if (errorProps==null)
 			this.errorAction=null;
 		else
 			throw new RuntimeException("property error should be a map in channel "+name);
 	}
 	public String toString() { return "Channel("+name+")";}
	public Object execute(Task task) { 
		action.execute(task);
		return null;
	}
 	
 	public void run(Task task) {
 		try {
 			action.execute(task);
 			task.setStatus(Task.DONE);
 		}
 		catch (RuntimeException e) {
 			task.setLastError(e);
			try {
				if (errorAction!=null)
					errorAction.execute(task);
			}
			catch(RuntimeException e2) { 
				logger.error("Could not perform the error actions ",e);
				// ignore this error which occurred 
			}
 			throw e;
 		}
 	}
 	
 	public Action createAction(Props props) {
 		String classname=props.getString("class",null);
 		if (classname==null)
 			return null;
 		if (classname.indexOf('.')<0)
 			classname="org.kisst.gft.action."+classname;
 		if (classname.startsWith(".")) // Prefix a class in the default package with a .
 			classname=classname.substring(1);
 		Constructor<?> c=ReflectionUtil.getConstructor(classname, new Class<?>[] {Channel.class, Props.class} );
 		if (c!=null)
 			return (Action) ReflectionUtil.createObject(c, new Object[] {this, props} );
 
 		c=ReflectionUtil.getConstructor(classname, new Class<?>[] {GftContainer.class, Props.class} );
 		if (c==null)
 			return (Action) ReflectionUtil.createObject(classname);
 		else
 			return (Action) ReflectionUtil.createObject(c, new Object[] {gft, props} );
 		
 	}
 }
