 package M2;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import M2.exceptions.ConfigurationException;
 
 /**
  * Base element of architecture description, defines ports and services or roles.
  * <p>
  * In HADL, a configuration can represent either a component and a connector.
  *  <ul>
  *   <li>
  *    If a configuration represents a component then it defines provided and 
  *    required ports and services.
  *   </li>
  *   <li>
  *    If a configuration represents a connector then it defines from and to
  *    roles.
  *   </li>
  *  </ul>
  * </p>
  * <p>
  * Both configuration representation are represented with the same class to 
  * unify links. Concrete configuration type is checked at runtime.
  * </p>
  * @author CaterpillarTeam
  */
 public class Configuration extends Element {
 	
 	private ArrayList<Component> components;
 	private ArrayList<Connector> connectors;
 	private ArrayList<Attachment> attachments;
 	private ArrayList<Binding> bindings;
 	
 	protected Element parent;
 	private ArrayList<ConfigurationPort> reqPorts;
 	private ArrayList<ConfigurationPort> provPorts;
 	private ArrayList<ConfigurationRole> fromRoles;
 	private ArrayList<ConfigurationRole> toRoles;
 	
 	/**
 	 * Create a configuration with the given name and architectural level.
 	 * @param name the name of the configuration.
 	 * @param level the architectural level of the configuration.
 	 * @param parent the parent of the configuration.
 	 */
	public Configuration(String name, Element parent) throws ConfigurationException {
		super(name,parent);
 		components = new ArrayList<Component>();
 		connectors = new ArrayList<Connector>();
 		attachments = new ArrayList<Attachment>();
 		bindings = new ArrayList<Binding>();
 		this.parent = parent;
 		if(parent instanceof Component) {
 			provPorts = new ArrayList<ConfigurationPort>();
 			reqPorts = new ArrayList<ConfigurationPort>();
 		}
 		else if(parent instanceof Connector) {
 			fromRoles = new ArrayList<ConfigurationRole>();
 			toRoles = new ArrayList<ConfigurationRole>();
 		}
 		else if(parent == null) {
 			/*
 			 * Top level Configuration, doesn't provides any port, services and
 			 * roles. If such a behavior is needed put this in appropriate 
 			 * Element.
 			 */
 		}
 		else {
 			throw new ConfigurationException("Invalid configuration type");
 		}
 	}
 	
 	public final void setParent(Element parent) {
 		this.parent = parent;
 	}
 
 	/**
 	 * Add the given component to the sub-component list.
 	 * <p>
 	 * The given component level is updated to the current one + 1,
 	 * to ensure level values consistency.
 	 * </p>
 	 * @param component the component to add.
 	 */
 	public final void addComponent(Component component) {
 		component.setLevel(this.getLevel()+1);
 		components.add(component);
 	}
 	
 	/**
 	 * Add the given component to the sup-connector list.
 	 * <p>
 	 * The given connector level is updated to the current one + 1,
 	 * to ensure level values consistency.
 	 * </p>
 	 * @param connector
 	 */
 	public final void addConnector(Connector connector) {
 		connector.setLevel(this.getLevel() + 1);
 		connectors.add(connector);
 	}
 	
 	/**
 	 * Add the given attachment to the configuration attachments.
 	 * @param attachment the attachment to add.
 	 */
 	public final void addAttachment(Attachment attachment) {
 		attachments.add(attachment);
 	}
 	
 	/**
 	 * Add the given binding to the configuration bindings.
 	 * @param binding the binding to add.
 	 */
 	public final void addBinding(Binding binding) {
 		bindings.add(binding);
 	}
 	
 	public final void addProvPort(ConfigurationPort i) throws ConfigurationException {
 		if(representComponent()) {
 			provPorts.add(i);
 		}
 		else {
 			throw new ConfigurationException("Cannot add provided port in a configuration which doesn't represent a component");
 		}
 	}
 	
 	public final void addReqPort(ConfigurationPort i) throws ConfigurationException {
 		if(representComponent()) {
 			reqPorts.add(i);
 		}
 		else {
 			throw new ConfigurationException("Cannot add required port in a configuration which doesn't represent a component");
 		}
 	}
 	
 	public final void addFromRole(ConfigurationRole i) throws ConfigurationException {
 		if(representConnector()) {
 			fromRoles.add(i);
 		}
 		else {
 			throw new ConfigurationException("Cannot add from role in a configuratio which doesn't represent a connector");
 		}
 	}
 	
 	public final void addToRole(ConfigurationRole i) throws ConfigurationException {
 		if(representConnector()) {
 			toRoles.add(i);
 		}
 		else {
 			throw new ConfigurationException("Cannot add to role in a configuration which doesn't represent a connector");
 		}
 	}
 	
 	/**
 	 * Search in the sub-components for a component with the given name.
 	 * @param name the name of the wanted component.
 	 * @return the component matching the given name if it exists, null otherwise.
 	 */
 	public final Component getComponent(String name) {
 		Iterator<Component> it = components.iterator();
 		while(it.hasNext()) {
 			Component currentComponent = it.next();
 			if(currentComponent.getName().equals(name)) {
 				return currentComponent;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * @return all the sub-components of the configuration.
 	 */
 	public final Collection<Component> getComponents() {
 		return this.components;
 	}
 	
 	/**
 	 * Search in the sub-connectors for a connector with the given name.
 	 * @param name the name of the wanted connector.
 	 * @return the connector matching the given name if it exists, null otherwise.
 	 */
 	public final Connector getConnector(String name) {
 		Iterator<Connector> it = connectors.iterator();
 		while(it.hasNext()) {
 			Connector currentConnector = it.next();
 			if(currentConnector.getName().equals(name)) {
 				return currentConnector;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * @return all the sub-connectors of the configuration.
 	 */
 	public final Collection<Connector> getConnectors() {
 		return this.connectors;
 	}
 	
 	public final ConfigurationPort getProvPort(String portName) {
 		// TODO error if not representing a component
 		return this.getPort(portName,provPorts);
 	}
 
 	public final Collection<ConfigurationPort> getProvPorts() {
 		// TODO error if not representing a component
 		return provPorts;
 	}
 	
 	public final ConfigurationPort getReqPort(String portName) {
 		// TODO error if not representing a component
 		return this.getPort(portName,reqPorts);
 	}
 	
 	public final Collection<ConfigurationPort> getReqPorts() {
 		// TODO error if not representing a component
 		return reqPorts;
 	}
 	
 	public ConfigurationRole getFromRole(String roleName) {
 		// TODO error if not representing a connector
 		return this.getRole(roleName,fromRoles);
 	}
 
 	public Collection<ConfigurationRole> getFromRoles() {
 		// TODO error if not representing a connector
 		return fromRoles;
 	}
 	
 	public ConfigurationRole getToRole(String roleName) {
 		// TODO error if not representing a connector
 		return this.getRole(roleName,toRoles);
 	}
 
 	public Collection<ConfigurationRole> getToRoles() {
 		// TODO error if not representing a connector
 		return toRoles;
 	}
 	
 	/**
 	 * Runtime function : process the flush of the ports used by the service.
 	 * @param componentService the service calling the runtime function.
 	 */
 	public final void flush(Interface elementInterface) throws ConfigurationException {
 		System.out.println("[HADL-RUNTIME] Configuration " + this.name + " flushing " + elementInterface.getName());
 		if(elementInterface instanceof ComponentService) {
 			throw new ConfigurationException("Cannot flush a service");
 		}
 		/*
 		 * Search in the attachment list if there is any attachment matching
 		 * the interface asking for a flush.
 		 */
 		Iterator<Attachment> it = attachments.iterator();
 		while(it.hasNext()) {
 			Attachment currentAttachment = it.next();
 			/*
 			 * The flush concerns a component port.
 			 */
 			if(currentAttachment.getComponentPort().equals(elementInterface)) {
 				System.out.println("[HADL-RUNTIME] Propagating port " + currentAttachment.getComponentPort().getName() + " value to role " + currentAttachment.getConnectorRole().getName());
 				currentAttachment.getConnectorRole().setValue(currentAttachment.getComponentPort().getValue());
 				boolean upBinded = this.bind(currentAttachment.getConnectorRole(),this);
 				boolean downBinded = this.bind(currentAttachment.getConnectorRole(), currentAttachment.getConnectorRole().getParent().getSubConfig());
 				if(!downBinded) {
 					System.out.println("[HADL-RUNTIME] Calling connector glue operation");
 					/*
 					 * Glue call will call recursively flush operation.
 					 */
 					currentAttachment.getConnectorRole().getParent().getGlue().callGlue();
 				}
 			}
 			/*
 			 * The flush concerns a connector role.
 			 */
 			if(currentAttachment.getConnectorRole().equals(elementInterface)) {
 				System.out.println("[HADL-RUNTIME] Propagating role " + currentAttachment.getConnectorRole().getName() + " value to port " + currentAttachment.getComponentPort().getName());
 				currentAttachment.getComponentPort().setValue(currentAttachment.getConnectorRole().getValue());
 				/*
 				 * Bind the port if needed, but this has no more effects, there is
 				 * no glue to call.
 				 */
 				// first upbinded, then down binded
 				this.bind(currentAttachment.getComponentPort(),this);
 				this.bind(currentAttachment.getComponentPort(), currentAttachment.getComponentPort().getParent().getSubConfig());
 			}
 		}
 	}
 	
 	public final boolean bind(Interface elementInterface, Configuration bindWith) {
 		if(bindWith == null) {
 			return false;
 		}
 		boolean result = false;
 		Iterator<Binding> it = bindWith.bindings.iterator();
 		while(it.hasNext()) {
 			Binding currentBinding = it.next();
 			if(elementInterface.equals(currentBinding.getComponentPort())) {
 				System.out.println("[HADL-RUNTIME] Binding port " + elementInterface.getName() + " to port " + currentBinding.getConfigurationPort());
 				currentBinding.getConfigurationPort().setValue(currentBinding.getComponentPort().getValue());
 				result = true;
 			}
 			if(elementInterface.equals(currentBinding.getConfigurationPort())) {
 				System.out.println("[HADL-RUNTIME] Binding port " + elementInterface.getName() + " to port " + currentBinding.getComponentPort());
 				currentBinding.getComponentPort().setValue(currentBinding.getConfigurationPort().getValue());
 				result = true;
 			}
 			if(elementInterface.equals(currentBinding.getConnectorRole())) {
 				System.out.println("[HADL-RUNTIME] Binding role " + elementInterface.getName() + " to role " + currentBinding.getConfigurationRole());
 				currentBinding.getConfigurationRole().setValue(currentBinding.getConnectorRole().getValue());
 				result = true;
 			}
 			if(elementInterface.equals(currentBinding.getConfigurationRole())) {
 				System.out.println("[HADL-RUNTIME] Binding role " + elementInterface.getName() + " to role " + currentBinding.getConnectorRole());
 				currentBinding.getConnectorRole().setValue(currentBinding.getConfigurationRole().getValue());
 				result = true;
 			}
 		}
 		return result;
 	}
 	
 	private boolean representComponent() {
 		return(parent instanceof Component);
 	}
 	
 	private boolean representConnector() {
 		return(parent instanceof Connector);
 	}
 	
 	private ConfigurationPort getPort(String portName, Collection<ConfigurationPort> portCollection) {
 		Iterator<ConfigurationPort> it = portCollection.iterator();
 		while(it.hasNext()) {
 			ConfigurationPort currentPort = it.next();
 			if(currentPort.getName().equals(portName)) {
 				return currentPort;
 			}
 		}
 		return null;
 	}
 	
 	private ConfigurationRole getRole(String roleName, Collection<ConfigurationRole> roleCollection) {
 		Iterator<ConfigurationRole> it = roleCollection.iterator();
 		while(it.hasNext()) {
 			ConfigurationRole currentRole = it.next();
 			if(currentRole.getName().equals(roleName)) {
 				return currentRole;
 			}
 		}
 		return null;
 	}
 	
 }
 
