 /*****************************************************************
 
   Organization: Lawrence Livermore National Lab (LLNL)
    Directorate: Computation
     Department: Computing Applications and Research
       Division: S&T Global Security
         Matrix: Atmospheric, Earth and Energy Division
        Program: PCMDI
        Project: Earth Systems Grid
   First Author: Gavin M. Bell (gavin@llnl.gov)
 
    Description:
 
 *****************************************************************/
 package esg.node.core;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 
 public abstract class AbstractDataNodeManager implements DataNodeManager {
 
     private static Log log = LogFactory.getLog(AbstractDataNodeManager.class);
 
     private Map<String,Gateway> gateways=null;
     private Map<String,DataNodeComponent> components=null;
     
     public AbstractDataNodeManager() {
 	gateways = new HashMap<String,Gateway>();
 	components = new HashMap<String,DataNodeComponent>();
     }
 
     public abstract void init();
 
     //-------------------------------------------
     //DataNodeManager Interface Implementations...
     //-------------------------------------------
     public boolean registerComponent(DataNodeComponent component) {
 	if (component == null) return false; 
 	if ((component.getName() == null) || (component.getName().equals(DataNodeComponent.ANONYMOUS)) ) {
 	    log.warn("Will not register a component without a name... call setMyName(<name>)");
 	    return false;
 	}
 	
 	log.trace("Registering Component: "+component.getName());
 	if (component instanceof Gateway) {
 	    log.warn("WARNING: Will not register gateway ["+component.getName()+"] as a component!");
 	    return false;
 	}
 	components.put(component.getName(), component);
 
 	//Note: Casting because this method is not exposed by
 	//interface but by the AbstractDataNodeComponent abstract
 	//class)
 	((AbstractDataNodeComponent)component).setDataNodeManager(this);
 	sendJoinNotification(component);
 	component.addESGListener(this);
 	return true;
     }
     
     public void removeComponent(String componentName) {
 	DataNodeComponent component = components.remove(componentName);
 	if(component == null) {
 	    log.trace("No component mapping to "+componentName+" (nothing to remove)");
 	    return;
 	}
 	component.removeESGListener(this);
 	sendUnjoinNotification(component);
     }
     
     //overloaded delegation of above
     public void removeComponent(DataNodeComponent component) {
 	removeComponent(component.getName());
     }
 
     //TODO: Maybe think about opening up the visibility of this method.
     protected DataNodeComponent getComponent(String name) {
 	return components.get(name);
     }
     
     public int numOfComponents() { return components.size(); }
     public String[] getComponentNames() { 
 	return components.keySet().toArray(new String[] {""}); 
     }
     
     //-------------------------------------------
     //The Gateway Proxy object is a type of DataNodeComponent but with
     //special logic for handling it's connectivity and life cycle.  It
     //is treated separely here for more *semantic* reasons than
     //anything else.  I want to enforce the thinking that gateway
     //stubs represent a different type of beast than "ordinary"
     //components.
     //-------------------------------------------
     public boolean registerGateway(Gateway gateway) {
 	if (gateway == null) return false;
 	if ( (gateway.getName() == null)  || (gateway.getName().equals(DataNodeComponent.ANONYMOUS)) ) {
 	    log.warn("Will not register a gateway without a name... call setMyName(<name>)");
 	    return false;
 	}
 
 	log.trace("Registering Gateway: "+gateway.getName());
 	gateways.put(gateway.getName(), gateway);
 	sendJoinNotification(gateway);
 	gateway.addESGListener(this);
 	return true;
     }
     
     public void removeGateway(String gatewayName) {
 	Gateway gateway = gateways.remove(gatewayName);	
 	if(gateway == null) {
 	    log.trace("No gateway mapping to "+gatewayName+" (nothing to remove)");
 	    return;
 	}
 	log.trace("Removing Gateway: "+gatewayName);
 	gateway.removeESGListener(this);
 	sendUnjoinNotification(gateway);
     }
 
     //overloaded delegation of above
     public void removeGateway(Gateway gateway) {
 	removeGateway(gateway.getName());
     }
 
     //For communicating with a specific gateway...
    protected Gateway getGateway(String name) {
 	return gateways.get(name);
     }
 
     //For getting the list of active gateways...
     public List<Gateway> getGateways() {
 	return new ArrayList<Gateway>(gateways.values());
     }
 
     public int numOfGateways() { return gateways.size(); }
     public String[] getGatewayNames() { return gateways.keySet().toArray(new String[] {""}); }
 
 
 
     //--------------------------------------------
     //Event dispatching to all registered ESGListeners
     //calling their esgActionPerformed method
     //--------------------------------------------
 
     private void sendJoinNotification(DataNodeComponent component) {
 	log.trace("Sending Join Notifications for: "+component.getName());
 	ESGJoinEvent joinEvent = new ESGJoinEvent(this,
 					       component.getName(),
 					       component,
 					       ESGJoinEvent.JOIN);	
 	fireESGEvent(joinEvent);
     }
 
     private void sendUnjoinNotification(DataNodeComponent component) {
 	log.trace("Sending UN-Join Notifications for :"+component.getName());
 	ESGJoinEvent unjoinEvent = new ESGJoinEvent(this,
 					       component.getName(),
 					       component,
 					       ESGJoinEvent.UNJOIN);	
 	fireESGEvent(unjoinEvent);
     }
 
 
     //TODO: Think about how I could get a subset view of the
     //subscription list to use for firing events to a subset of
     //entities that adhere to a particular interface.  I.E. get all
     //the objects in the list that implement the FooListener interface
     //for me to call back to.  In the mean time we push events to
     //everyone and let them deal with proper selection for handling
 
     protected void fireESGEvent(ESGEvent esgEvent) {
 	Collection<? extends ESGListener> esgListeners = components.values();
 	log.trace("Firing ESGEvent: "+esgEvent);
 	for(ESGListener listener: esgListeners) {
 	    listener.esgActionPerformed(esgEvent);
 	}
     }
 
 
     //-------------------------------------------
     //ESGListener Interface Implementation...
     //-------------------------------------------
     public void esgActionPerformed(ESGEvent event) {
 	//TODO:
 	//Just stubbed for now...
 	log.debug("DNM: Got An Event!!!!: "+event+"\nmessage: "+event.getMessage());
     }
 
     //TODO: YES!!! this is horrendous, I will use string builder and
     //be a bit nicer of memory and time when cleaning up ;-)
     //Promise.... Seriously! :-)
     public String toString() {
 	String[] names;
 	String out = ""; 
 
 	out += this.getClass().getName()+":\n";
 	out += "Number of Components: "+numOfComponents()+"\n";
 	names = getComponentNames();
 	for(String dncName : names) { out +="\t"+dncName+"\n"; }
 	out += "Number of Gateways  : "+numOfGateways()+"\n";
 	names = getGatewayNames();
 	for(String gwayName : names) { out +="\t"+gwayName+"\n"; }
 	return out;
     }
 
 }
