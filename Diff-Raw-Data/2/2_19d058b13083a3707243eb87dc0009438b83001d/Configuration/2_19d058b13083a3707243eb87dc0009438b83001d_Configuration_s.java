 package M2;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import M0.Trace;
 
 public class Configuration extends Element {
 
 	private ArrayList<Composant> components;
 	private ArrayList<Connecteur> connectors;
 	private ArrayList<Attachment> attachments;
 	private ArrayList<Binding> bindings;
 	private ArrayList<ServiceConfig> servicesRequis;
 	private ArrayList<ServiceConfig> servicesFournis;
 	private HashMap<String, PortConfigRequis> portsRequis;
 	private HashMap<String, PortConfigFourni> portsFourni;
 	
 	private ArrayList<String> tags;
 	
 	
 	
 	public Configuration(String name) {
 		super(name, null);
 		
 		this.components = new ArrayList<Composant>();
 		this.connectors = new ArrayList<Connecteur>();
 		this.attachments = new ArrayList<Attachment>();
 		this.bindings = new ArrayList<Binding>();
 		this.servicesRequis = new ArrayList<ServiceConfig>();
 		this.servicesFournis = new ArrayList<ServiceConfig>();
 		this.portsRequis = new HashMap<String, PortConfigRequis>();
 		this.portsFourni = new HashMap<String, PortConfigFourni>();
 		this.tags = new ArrayList<String>();
 	}
 	
 	public void setSubconf(Configuration subconf) {
 		// Une configuration n'a pas de sous-configuration
 		this.subConfiguration = null;
 	}
 	
 	public void setParent(Element parent) {
 		this.parent = parent;
 	}
 	
 	public void addComponent(Composant c) {
 		if(this.tags.contains(c.getName())) {
 			Trace.printError("Tag " + c.getName() + " is not available, component as not been created");
 		}
 		else {
 			this.components.add(c);
 			this.tags.add(c.getName());
 		}
 	}
 	
 	public void addConnector(Connecteur c) {
 		if(this.tags.contains(c.getName())) {
 			Trace.printError("Tag " + c.getName() + " is not available, connector as not been created");
 		}
 		else {
 			this.connectors.add(c);
 			this.tags.add(c.getName());
 		}
 	}
 	
 	public void addService(ServiceConfigFourni sc) {
 		if(this.tags.contains(sc.getName())) {
 			Trace.printError("Tag " + sc.getName() + " is not available, service as not been created");
 		}
 		else {
 			this.servicesFournis.add(sc);
 			this.tags.add(sc.getName());
 		}
 	}
 	
 	
 	public void addService(ServiceConfigRequis sc) {
 		if(this.tags.contains(sc.getName())) {
 			Trace.printError("Tag " + sc.getName() + " is not available, service as not been created");
 		}
 		else {
 			this.servicesRequis.add(sc);
 			this.tags.add(sc.getName());
 		}
 	}
 	
 	public void addPort(PortConfigFourni pc) {
 		if(this.tags.contains(pc.getName())) {
 			Trace.printError("Tag " + pc.getName() + " is not available, service as not been created");
 		}
 		else {
 			this.portsFourni.put(pc.getName(), pc);
 			this.tags.add(pc.getName());
 		}
 	}
 	
 	public void addPort(PortConfigRequis pc) {
 		if(this.tags.contains(pc.getName())) {
 			Trace.printError("Tag " + pc.getName() + " is not available, service as not been created");
 		}
 		else {
 			this.portsRequis.put(pc.getName(), pc);
 			this.tags.add(pc.getName());
 		}
 	}
 	
 	
 	// Getters
 
 	public PortConfigRequis getPortR(String name) {
 		return this.portsRequis.get(name);
 	}
 	
 	public PortConfigFourni getPortF(String name) {
 		return this.portsFourni.get(name);
 	}
 	
 	
 	public Composant getComposant(String name) {
 		Iterator<Composant> it = this.components.iterator();
 		while(it.hasNext()) {
 			Composant currentComponent = it.next();
 			if(currentComponent.getName().equals(name)) {
 				return currentComponent;
 			}
 		}
 		
 		return null;
 	}
 	
 	public Connecteur getConnector(String name) {
 		Iterator<Connecteur> it = this.connectors.iterator();
 		while(it.hasNext()) {
 			Connecteur currentConnector = it.next();
 			if(currentConnector.getName().equals(name)) {
 				return currentConnector;
 			}
 		}
 		
 		return null;
 	}
 	
 	// Attachments (2)
 	
 	public Attachment addLink(String name, RoleFrom r, PortFourni p) {
 		Attachment a = null;
 		if(this.tags.contains(name)) {
 			Trace.printError("Tag " + name + " is not available, service as not been created");
 		}
 		else {
			if (r.getConfig() == this && p.getConfig() == this) {
 				a = new Attachment(name);
 				a.bind(r, p);
 				this.attachments.add(a);
 				this.tags.add(a.getName());
 			}
 		}
 		return a;
 	}
 	
 	public Attachment addLink(String name, RoleTo r, PortRequis p) {
 		Attachment a = null;
 		if(this.tags.contains(name)) {
 			Trace.printError("Tag " + name + " is not available, service as not been created");
 		}
 		else {
 			a = new Attachment(name);
 			a.bind(r, p);
 			this.attachments.add(a);
 			this.tags.add(a.getName());
 		}
 		return a;
 	}
 	
 	
 	// Bindings (4)
 	
 	public Binding addLink(String name, RoleFrom r, PortConfigFourni p) {
 		Binding b = null;
 
 		if(this.tags.contains(name)) {
 			Trace.printError("Tag " + name + " is not available, service as not been created");
 		}
 		else {
 			b = new Binding(name);
 			b.bind(r, p);
 			this.bindings.add(b);
 			this.tags.add(b.getName());
 		}
 		return b;
 	}
 	
 	public Binding addLink(String name, RoleTo r, PortConfigRequis p) {
 		Binding b = null;
 
 		if(this.tags.contains(name)) {
 			Trace.printError("Tag " + name + " is not available, service as not been created");
 		}
 		else {
 			b = new Binding(name);
 			b.bind(r, p);
 			this.bindings.add(b);
 			this.tags.add(b.getName());
 		}
 		return b;
 		
 	}
 	
 	public Binding addLink(String name, PortConfigRequis pc, PortRequis p) {
 		Binding b = null;
 		
 		if(pc == null) {
 			Trace.printError("While trying to bind, portConfigRequis is null");
 		}
 		else if(p == null) {
 			Trace.printError("While trying to bind, portRequis is null");
 		}
 		else if(this.tags.contains(name)) {
 			Trace.printError("Tag " + name + " is not available, service as not been created");
 		}
 		else {
 			b = new Binding(name);
 			b.bind(pc, p);
 			this.bindings.add(b);
 			this.tags.add(b.getName());
 		}
 		return b;
 	}
 	
 	public Binding addLink(String name, PortConfigFourni pc, PortFourni p) {
 		Binding b = null;
 
 		if(this.tags.contains(name)) {
 			Trace.printError("Tag " + name + " is not available, service as not been created");
 		}
 		else {
 			b = new Binding(name);
 			b.bind(pc, p);
 			this.bindings.add(b);
 			this.tags.add(b.getName());
 		}
 		return b;
 	}
 	
 	public void addLink(Binding b) {
 		if(this.tags.contains(b.getName())) {
 			Trace.printError("Binding already added or name already picked");
 		}
 		else {
 			this.bindings.add(b);
 			this.tags.add(b.getName());
 		}
 	}
 	
 	
 	// Links management
 	
 	public void callAttachments(Interface sender, String message) {
 
 		ArrayList<Interface> receiver = new ArrayList<Interface>();
 		
 		// Find all receiver in attachments list
 		Iterator<Attachment> itAttach = this.attachments.iterator();
 		while(itAttach.hasNext()) {
 			Interface dest = itAttach.next().getReceiver(sender);
 			if(dest != null) {
 				receiver.add(dest);
 			}
 		}
 		
 		// Activate all receivers
 		Iterator<Interface> itIface = receiver.iterator();
 		while(itIface.hasNext()) {
 			Interface dest = itIface.next();
 			dest.activate(message);
 		}
 	}
 	
 	public void callBindings(Interface sender, String message) {
 		
 		// TODO
 		// si portconfigrequis, les destinataires ne sont que ceux qui sont au niveau du dessous
 		// si portconfigfourni, que au dessus
 
 		ArrayList<Interface> receiver = new ArrayList<Interface>();
 		
 		// Find all receiver in bindings list
 		Iterator<Binding> itBind = this.bindings.iterator();
 		while(itBind.hasNext()) {
 			Interface rec = itBind.next().getReceiver(sender);
 			if(rec != null) {
 				
 				boolean wrongWay = false;
 				if(sender instanceof PortConfigRequis && sender.getConfig().getParent() != null) {
 					if(sender.getConfig().getParent().equals(rec.getConfig())) {
 						wrongWay = true;
 					}
 				}
 				if(sender instanceof PortConfigFourni && rec.getConfig().getParent() != null) {
 					if(sender.getConfig().equals(rec.getConfig().getParent())) {
 						wrongWay = true;
 					}
 				}
 				
 				if(!wrongWay) {
 					receiver.add(rec);
 				}
 			}
 		}
 		
 		// Activate all receivers
 		Iterator<Interface> itIface = receiver.iterator();
 		while(itIface.hasNext()) {
 			itIface.next().activate(message);
 		}
 	}
 
 }
