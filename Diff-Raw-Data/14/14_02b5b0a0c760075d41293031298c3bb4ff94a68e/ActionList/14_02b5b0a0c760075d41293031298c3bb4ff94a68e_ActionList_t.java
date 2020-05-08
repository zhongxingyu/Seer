 package org.kisst.gft.action;
 
 import org.kisst.cfg4j.LayeredProps;
 import org.kisst.cfg4j.Props;
 import org.kisst.cfg4j.SimpleProps;
 import org.kisst.gft.GftContainer;
 import org.kisst.gft.task.Task;
 
 public class ActionList  implements Action {
 	private final Action[] actions;
 	
 	public ActionList(GftContainer gft, Props props) {
 
 		String actions=props.getString("actions");
 		String[] parts=actions.split(",");
 		this.actions=new Action[parts.length];
 		int i=0;
 		for (String name: parts) {
			name=name.trim();
 			LayeredProps lprops=new LayeredProps();
 			SimpleProps top=new SimpleProps();
 			top.put("action",gft.actions.get(name));
 			top.put("channel",props);
 			lprops.addLayer(top);
 			if (props.get(name,null) instanceof Props)
 				lprops.addLayer(props.getProps(name));
 			lprops.addLayer(gft.actions.get(name));
 			lprops.addLayer(props);
 				
 			Action a=ActionFactory.createAction(gft, lprops);
 			if (a==null)
 				throw new RuntimeException("Unknown action "+name);
			this.actions[i++]=a;
 		}
 	}
 
 	public Object execute(Task task) {
 		for (Action a: actions)
 			a.execute(task);
 		return null;
 	}
 }
