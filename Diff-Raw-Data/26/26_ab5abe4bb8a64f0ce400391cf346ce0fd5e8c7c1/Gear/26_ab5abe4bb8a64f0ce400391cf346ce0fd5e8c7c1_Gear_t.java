 package org.gears;
 
 import java.util.List;
 
 
 import org.apache.log4j.Logger;
 import org.apache.velocity.context.Context;
 import org.gears.template.Templaton;
 
 
 abstract public class Gear {
 	
 	private static final Logger LOG = Logger.getLogger(Gear.class);
 	
 	public Context attributes = Templaton.getContext();
 	protected Configuration config = Configuration.getInstance();
 	protected String gearGroup = null;
 	
 	public abstract void execute();
 	
 	public Context setDefaults() {return this.attributes;} 
 	public Context defaults()    {return this.attributes;}
 	
 	public void setAttribute(String key, Object value){
 		this.attributes.put(key, value);
 	}
 	public Object getAttribute(String key){
 		return this.attributes.get(key);
 	}
 //	public Context setAttribute(String key, Object value){
 //		return this.attributes.put(key, value);
 //	}
 	
 	public void execute(String group, Gear gear){
 		gear.setGearGroup(group);
 		gear.execute();
 		gear.setGearGroup(null);
 	}
 	
 	public void install(Gear gear) {
 		execute(null, gear);
 	}
 	
 	private List<Instance> getInstances() {
 		return (this.gearGroup == null) ? config.getInstances() : config.getInstances(this.gearGroup);
 	}
 	
 	private void setGearGroup(String gearGroup) {
 		this.gearGroup = gearGroup;
 	}
 
 	public void install(String gearGroup, Gear gear) {
 		execute(gearGroup, gear);
 	}
 	
 	public boolean render(String gearGroup, String source, String dest) {
 		this.setGearGroup(gearGroup);
 		render(source, dest);
 		this.setGearGroup(null);
 		return true;
 	}
 	
 	public boolean install(String gearGroup, String flags, String commands) {
 		this.setGearGroup(gearGroup);
 		install(flags, commands);
 		this.setGearGroup(null);
 		return true;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public boolean update() {
 		for(Instance instance : getInstances()){
 			instance.update();
 		}
 		return true;
 	}
 	
 	public boolean install(String commands) {
 		for(Instance instance : getInstances()){
 			instance.install(commands);
 		}
 		return true;
 	}
 	
 	public boolean install(String flags, String commands) {
 		for(Instance instance : getInstances()){
 			instance.install(flags, commands);
 		}
 		return true;
 	}
 	
 	public boolean openPort(String value) {
 		for(Instance instance : getInstances()){
 			instance.openPort(value);
 		}
 		return true;
 	}
 
 	
 	public boolean start(String service) {
 		for(Instance instance : getInstances()){
 			instance.start(service);
 		}
 		return true;
 	}
 	
 	
	public boolean restart(String gearGroup, Gear service) {
		setGearGroup(gearGroup);
		boolean result = restart(service.toString());
		setGearGroup(null);
		return result;
	}
	
 	public boolean restart(Gear service) {
 		return restart(service.toString());
 	}
 	
 	public boolean restart(String service) {
 		for(Instance instance : getInstances()){
 			instance.restart(service);
 		}
 		return true;
 	}
 	
 	public boolean command(String commands) {
 		boolean result = true;
 		for(Instance instance : getInstances()){
 			instance.command(commands);
 		}
 		return result;
 	}
 	
 	
 	public boolean render(String source, String dest) {
 		Context context = Templaton.getContext(this);
 		for(Instance instance : getInstances()){
 			instance.render(source, dest, context);
 		}
 		return true;
 	}
 	
 
 }
