 /*
  * Created on 23-May-2006
  */
 package ca.neo.model.impl;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import ca.neo.model.Ensemble;
 import ca.neo.model.InstantaneousOutput;
 import ca.neo.model.Network;
 import ca.neo.model.Node;
 import ca.neo.model.Origin;
 import ca.neo.model.Probeable;
 import ca.neo.model.Projection;
 import ca.neo.model.SimulationException;
 import ca.neo.model.SimulationMode;
 import ca.neo.model.StructuralException;
 import ca.neo.model.Termination;
 import ca.neo.sim.Simulator;
 import ca.neo.sim.impl.LocalSimulator;
 import ca.neo.util.Configuration;
 import ca.neo.util.Probe;
 import ca.neo.util.TimeSeries;
 import ca.neo.util.VisiblyMutable;
 import ca.neo.util.VisiblyMutableUtils;
 
 /**
  * Default implementation of Network. 
  *  
  * @author Bryan Tripp
  */
 public class NetworkImpl implements Network, VisiblyMutable, VisiblyMutable.Listener {
 
 	public static final String DEFAULT_NAME = "Network";
 	
 	private static final long serialVersionUID = 1L;
 	private static Logger ourLogger = Logger.getLogger(NetworkImpl.class);
 	
 	private Map<String, Node> myNodeMap; //keyed on name
 	private Map<Termination, Projection> myProjectionMap; //keyed on Termination
 	private String myName;
 	private SimulationMode myMode;
 	private Simulator mySimulator;
 	private float myStepSize;
 	private Map<String, Probeable> myProbeables;
 	private Map<String, String> myProbeableStates;
 	private Map<String, Origin> myExposedOrigins;
 	private Map<String, Termination> myExposedTerminations;
 	private String myDocumentation;
 	private Map<String, Object> myMetaData;
 
 	private Map<Origin, String> myExposedOriginNames;
 	private Map<Termination, String> myExposedTerminationNames;
 	
 	private transient List<VisiblyMutable.Listener> myListeners;
 	
 	public NetworkImpl() {
 		myNodeMap = new HashMap<String, Node>(20);
 		myProjectionMap	= new HashMap<Termination, Projection>(50);
 		myName = DEFAULT_NAME;
 		myStepSize = .001f; 
 		myProbeables = new HashMap<String, Probeable>(30);
 		myProbeableStates = new HashMap<String, String>(30);
 		myExposedOrigins = new HashMap<String, Origin>(10);
 		myExposedOriginNames = new HashMap<Origin, String>(10);
 		myExposedTerminations = new HashMap<String, Termination>(10);
 		myExposedTerminationNames = new HashMap<Termination, String>(10);
 		myMode = SimulationMode.DEFAULT;
 		myMetaData = new HashMap<String, Object>(20);
 		myListeners = new ArrayList<Listener>(10);
 	}
 	
 	/**
 	 * @param simulator Simulator with which to run this Network
 	 */
 	public void setSimulator(Simulator simulator) {
 		mySimulator = simulator;
 		mySimulator.initialize(this);
 	}
 	
 	/**
 	 * @return Simulator used to run this Network (a LocalSimulator by default) 
 	 */
 	public Simulator getSimulator() {
 		if (mySimulator == null) {
 			mySimulator = new LocalSimulator();
 			mySimulator.initialize(this);
 		}
 		return mySimulator;
 	}	
 	
 	/**
 	 * @param stepSize New timestep size at which to simulate Network (some components of the network 
 	 * 		may run with different step sizes, but information is exchanged between components with 
 	 * 		this step size). Defaults to 0.001s. 
 	 */
 	public void setStepSize(float stepSize) {
 		myStepSize = stepSize;
 	}
 	
 	/**
 	 * @return Timestep size at which Network is simulated. 
 	 */
 	public float getStepSize() {
 		return myStepSize;
 	}
 	
 	/**
 	 * @see ca.neo.model.Network#addNode(ca.neo.model.Node)
 	 */
 	public void addNode(Node node) throws StructuralException {
 		if (myNodeMap.containsKey(node.getName())) {
 			throw new StructuralException("This Network already contains a Node named " + node.getName());
 		}
 		
 		myNodeMap.put(node.getName(), node);
 		node.addChangeListener(this);
 		
 		getSimulator().initialize(this);		
 		fireVisibleChangeEvent();
 	}
 	
 	/**
 	 * If the event indicates that a component node's name is changing, 
 	 * checks for name conflicts and throws an exception if there is one, 
 	 * and updates the name reference.  
 	 *  
 	 * @see ca.neo.util.VisiblyMutable.Listener#changed(ca.neo.util.VisiblyMutable.Event)
 	 */
 	public void changed(Event e) throws StructuralException {
 		if (e instanceof VisiblyMutable.NameChangeEvent) {
 			VisiblyMutable.NameChangeEvent ne = (VisiblyMutable.NameChangeEvent) e;
 			
 			if (myNodeMap.containsKey(ne.getNewName()) && !ne.getNewName().equals(ne.getOldName())) {
 				throw new StructuralException("This Network already contains a Node names " + ne.getNewName());
 			}
 			
			myNodeMap.put(ne.getNewName(), myNodeMap.get(ne.getOldName()));
			myNodeMap.remove(ne.getOldName());
 		}
 	}
 
 	/**
 	 * @see ca.neo.model.Network#getNodes()
 	 */
 	public Node[] getNodes() {
 		return myNodeMap.values().toArray(new Node[0]);
 	}
 
 	/**
 	 * @see ca.neo.model.Network#getNode(java.lang.String)
 	 */
 	public Node getNode(String name) throws StructuralException {
 		if (!myNodeMap.containsKey(name)) {
 			throw new StructuralException("No Node named " + name + " in this Network");			
 		}
 		return myNodeMap.get(name);
 	}
 
 	/**
 	 * @see ca.neo.model.Network#removeNode(java.lang.String)
 	 */
 	public void removeNode(String name) throws StructuralException {
 		if (myNodeMap.containsKey(name)) {
 			Node node = myNodeMap.remove(name);
 			node.removeChangeListener(this);
 		} else {
 			throw new StructuralException("No Node named " + name + " in this Network");
 		}
 
 		getSimulator().initialize(this);		
 		fireVisibleChangeEvent();
 	}
 
 	/**
 	 * @see ca.neo.model.Network#addProjection(ca.neo.model.Origin, ca.neo.model.Termination)
 	 */
 	public Projection addProjection(Origin origin, Termination termination) throws StructuralException {
 		if (myProjectionMap.containsKey(termination)) {
 			throw new StructuralException("There is already an Origin connected to the specified Termination");
 		}
 		
 		if (origin.getDimensions() != termination.getDimensions()) {
 			throw new StructuralException("Can't connect Origin of dimension " + origin.getDimensions() 
 					+ " to Termination of dimension " + termination.getDimensions());
 		}
 		
 		Projection result = new ProjectionImpl(origin, termination, this);
 		myProjectionMap.put(termination, result);
 		getSimulator().initialize(this);
 		fireVisibleChangeEvent();
 		
 		return result;
 	}
 
 	/**
 	 * @see ca.neo.model.Network#getProjections()
 	 */
 	public Projection[] getProjections() {
 		return (Projection[]) myProjectionMap.values().toArray(new Projection[0]);
 	}
 
 	/**
 	 * @see ca.neo.model.Network#removeProjection(ca.neo.model.Termination)
 	 */
 	public void removeProjection(Termination termination) throws StructuralException {
 		if (myProjectionMap.containsKey(termination)) {
 			myProjectionMap.remove(termination);
 		} else {
 			throw new StructuralException("The Network contains no Projection ending on the specified Termination");
 		}
 		
 		getSimulator().initialize(this);
 		fireVisibleChangeEvent();
 	}
 
 	/**
 	 * @see ca.neo.model.Node#getName()
 	 */
 	public String getName() {
 		return myName;
 	}
 	
 	/**
 	 * @param name New name of Network (must be unique within any networks of which this one 
 	 * 		will be a part) 
 	 */
 	public void setName(String name) throws StructuralException {
 		VisiblyMutableUtils.nameChanged(this, getName(), name, myListeners);
 		myName = name;
 	}
 
 	/**
 	 * @see ca.neo.model.Node#setMode(ca.neo.model.SimulationMode)
 	 */
 	public void setMode(SimulationMode mode) {
 		myMode = mode;
 
 		Iterator<Node> it = myNodeMap.values().iterator();
 		while (it.hasNext()) {
 			it.next().setMode(mode);
 		}
 	}
 
 	/**
 	 * @see ca.neo.model.Node#getMode()
 	 */
 	public SimulationMode getMode() {
 		return myMode;
 	}
 
 	/**
 	 * @see ca.neo.model.Node#run(float, float)
 	 */
 	public void run(float startTime, float endTime) throws SimulationException {
 		getSimulator().run(startTime, endTime, myStepSize);
 	}
 
 	/**
 	 * @see ca.neo.model.Resettable#reset(boolean)
 	 */
 	public void reset(boolean randomize) {
 		Iterator it = myNodeMap.keySet().iterator();
 		while (it.hasNext()) {
 			Node n = (Node) myNodeMap.get(it.next());
 			n.reset(randomize);
 		}
 	}
 
 	/**
 	 * @see ca.neo.model.Probeable#getHistory(java.lang.String)
 	 */
 	public TimeSeries getHistory(String stateName) throws SimulationException {
 		Probeable p = (Probeable) myProbeables.get(stateName);
 		String n = (String) myProbeableStates.get(stateName);
 		
 		return p.getHistory(n);
 	}
 
 	/**
 	 * @see ca.neo.model.Probeable#listStates()
 	 */
 	public Properties listStates() {
 		Properties result = new Properties();
 
 		Iterator it = myProbeables.keySet().iterator();
 		while (it.hasNext()) {
 			String key = (String) it.next();
 			Probeable p = (Probeable) myProbeables.get(key);
 			String n = (String) myProbeableStates.get(key);
 			result.put(key, p.listStates().getProperty(n));
 		}
 		
 		return result;
 	}
 
 	/**
 	 * @see ca.neo.model.Network#exposeOrigin(ca.neo.model.Origin,
 	 *      java.lang.String)
 	 */
 	public void exposeOrigin(Origin origin, String name) {
 		myExposedOrigins.put(name, new OriginWrapper(this, origin, name));
 		myExposedOriginNames.put(origin, name);
 		fireVisibleChangeEvent();
 	}
 
 	/**
 	 * @see ca.neo.model.Network#hideOrigin(java.lang.String)
 	 */
 	public void hideOrigin(String name) {
 		OriginWrapper originWr = (OriginWrapper)myExposedOrigins.remove(name);
 		if (originWr != null) {
 			myExposedOriginNames.remove(originWr.myWrapped);
 		}
 		fireVisibleChangeEvent();
 	}
 
 	/**
 	 * @see ca.neo.model.Network#getExposedOriginName(ca.neo.model.Origin)
 	 */
 	public String getExposedOriginName(Origin insideOrigin) {
 		return (String)myExposedOriginNames.get(insideOrigin);
 	}
 
 	/**
 	 * @see ca.neo.model.Network#getOrigin(java.lang.String)
 	 */
 	public Origin getOrigin(String name) throws StructuralException {
 		if ( !myExposedOrigins.containsKey(name) ) {
 			throw new StructuralException("There is no exposed Origin named " + name);
 		}
 		return (Origin) myExposedOrigins.get(name);
 	}
 
 	/**
 	 * @see ca.neo.model.Network#getOrigins()
 	 */
 	public Origin[] getOrigins() {
 		return (Origin[]) myExposedOrigins.values().toArray(new Origin[0]);
 	}
 
 	/**
 	 * @see ca.neo.model.Network#exposeTermination(ca.neo.model.Termination, java.lang.String)
 	 */
 	public void exposeTermination(Termination termination, String name) {
 		myExposedTerminations.put(name, new TerminationWrapper(this, termination, name));
 		myExposedTerminationNames.put(termination, name);
 		fireVisibleChangeEvent();
 	}
 
 	/**
 	 * @see ca.neo.model.Network#hideTermination(java.lang.String)
 	 */
 	public void hideTermination(String name) {
 		TerminationWrapper termination = (TerminationWrapper)myExposedTerminations.remove(name);
 		if (termination != null) {
 			myExposedTerminationNames.remove(termination.myWrapped);
 		}
 		fireVisibleChangeEvent();
 	}
 
 	/**
 	 * @see ca.neo.model.Network#getExposedTerminationName(ca.neo.model.Termination)
 	 */
 	public String getExposedTerminationName(Termination insideTermination) {
 		return (String)myExposedTerminationNames.get(insideTermination);
 	}	
 
 	/**
 	 * @see ca.neo.model.Network#getTermination(java.lang.String)
 	 */
 	public Termination getTermination(String name) throws StructuralException {
 		if ( !myExposedTerminations.containsKey(name) ) {
 			throw new StructuralException("There is no exposed Termination named " + name);
 		}
 		return (Termination) myExposedTerminations.get(name);
 	}
 
 	/**
 	 * @see ca.neo.model.Network#getTerminations()
 	 */
 	public Termination[] getTerminations() {
 		return (Termination[]) myExposedTerminations.values().toArray(new Termination[0]);
 	}
 
 	/**
 	 * @see ca.neo.model.Network#exposeState(ca.neo.model.Probeable, java.lang.String, java.lang.String)
 	 */
 	public void exposeState(Probeable probeable, String stateName, String name) {
 		myProbeables.put(name, probeable);
 		myProbeableStates.put(name, stateName);
 	}
 
 	/**
 	 * @see ca.neo.model.Network#hideState(java.lang.String)
 	 */
 	public void hideState(String name) {
 		myProbeables.remove(name);
 		myProbeableStates.remove(name);
 	}
 	
 	/**
 	 * Wraps an Origin with a new name (for exposing outside Network).
 	 *  
 	 * @author Bryan Tripp
 	 */
 	public class OriginWrapper implements Origin {
 		
 		private static final long serialVersionUID = 1L;
 		
 		private Node myNode;
 		private Origin myWrapped;
 		private String myName;
 		
 		public OriginWrapper(Node node, Origin wrapped, String name) {
 			myNode = node;
 			myWrapped = wrapped;
 			myName = name;
 		}
 		
 		public OriginWrapper() {
 			this(null, null, "exposed");
 		}
 		
 		public Origin getWrappedOrigin() {
 			return myWrapped;
 		}
 		
 		public void setWrappedOrigin(Origin wrapped) {
 			myWrapped = wrapped;
 		}
 
 		public String getName() {
 			return myName;
 		}
 		
 		public void setName(String name) {
 			myName = name;
 		}
 
 		public int getDimensions() {
 			return myWrapped.getDimensions();
 		}
 
 		public InstantaneousOutput getValues() throws SimulationException {
 			return myWrapped.getValues();
 		}
 
 		public Node getNode() {
 			return myNode;
 		}
 		
 		public void setNode(Node node) {
 			myNode = node;
 		}
 
 		@Override
 		public Origin clone() throws CloneNotSupportedException {
 			return (Origin) super.clone();
 		}
 		
 	}
 	
 	/**
 	 * Wraps a Termination with a new name (for exposing outside Network).
 	 *  
 	 * @author Bryan Tripp
 	 */
 	public class TerminationWrapper implements Termination {
 
 		private static final long serialVersionUID = 1L;
 		
 		private Node myNode;
 		private Termination myWrapped;
 		private String myName;
 		
 		public TerminationWrapper(Node node, Termination wrapped, String name) {
 			myNode = node;
 			myWrapped = wrapped;
 			myName = name;
 		}
 		
 		public Termination getWrappedTermination() {
 			return myWrapped;
 		}
 		
 		public String getName() {
 			return myName;
 		}
 
 		public int getDimensions() {
 			return myWrapped.getDimensions();
 		}
 
 		public void setValues(InstantaneousOutput values) throws SimulationException {
 			myWrapped.setValues(values);
 		}
 
 		public Configuration getConfiguration() {
 			return myWrapped.getConfiguration();
 		}
 
 		public void propertyChange(String propertyName, Object newValue) throws StructuralException {
 			myWrapped.propertyChange(propertyName, newValue);
 		}
 
 		public Node getNode() {
 			return myNode;
 		}
 
 		/**
 		 * @see ca.neo.model.Resettable#reset(boolean)
 		 */
 		public void reset(boolean randomize) {
 			myWrapped.reset(randomize);
 		}
 
 		@Override
 		public Termination clone() throws CloneNotSupportedException {
 			return (Termination) super.clone();
 		}
 		
 	}
 
 	/**
 	 * @see ca.neo.model.Node#getDocumentation()
 	 */
 	public String getDocumentation() {
 		return myDocumentation;
 	}
 
 	/**
 	 * @see ca.neo.model.Node#setDocumentation(java.lang.String)
 	 */
 	public void setDocumentation(String text) {
 		myDocumentation = text;
 	}
 
 	/**
 	 * @see ca.neo.model.Network#getMetaData(java.lang.String)
 	 */
 	public Object getMetaData(String key) {
 		return myMetaData.get(key);
 	}
 
 	/**
 	 * @see ca.neo.model.Network#setMetaData(java.lang.String, java.lang.Object)
 	 */
 	public void setMetaData(String key, Object value) {
 		if ( !(value instanceof Serializable) ) {
 			throw new RuntimeException("Metadata must be serializable");
 		}
 		myMetaData.put(key, value);
 	}
 
 	/**
 	 * @see ca.neo.util.VisiblyMutable#addChangeListener(ca.neo.util.VisiblyMutable.Listener)
 	 */
 	public void addChangeListener(Listener listener) {
 		if (myListeners == null) {
 			myListeners = new ArrayList<Listener>(1);
 		}
 		myListeners.add(listener);
 	}
 
 	/**
 	 * @see ca.neo.util.VisiblyMutable#removeChangeListener(ca.neo.util.VisiblyMutable.Listener)
 	 */
 	public void removeChangeListener(Listener listener) {
 		if (myListeners != null) myListeners.remove(listener);
 	}
 	
 	private void fireVisibleChangeEvent() {
 		VisiblyMutableUtils.changed(this, myListeners);
 	}
 
 	@Override
 	public Network clone() throws CloneNotSupportedException {
 		NetworkImpl result = (NetworkImpl) super.clone();
 		
 		result.myNodeMap = new HashMap<String, Node>(10);
 		for (Node oldNode : myNodeMap.values()) {
 			Node newNode = oldNode.clone();
 			result.myNodeMap.put(newNode.getName(), newNode);
 			newNode.addChangeListener(result);
 		}
 		
 		//TODO: Exposed states aren't handled currently, pending redesign of Probes (it should be possible 
 		//		to probe things that are nested deeply, in which case exposing state woulnd't be necessary) 
 //		result.myProbeables
 //		result.myProbeableStates
 		
 		//TODO: this works with a single Projection impl & no params; should add Projection.copy(Origin, Termination, Network)?  
 		result.myProjectionMap = new HashMap<Termination, Projection>(10);
 		for (Projection oldProjection : getProjections()) {
 			try {
 				Origin newOrigin = result.getNode(oldProjection.getOrigin().getNode().getName())
 					.getOrigin(oldProjection.getOrigin().getName());
 				Termination newTermination = result.getNode(oldProjection.getTermination().getNode().getName())
 					.getTermination(oldProjection.getTermination().getName());
 				Projection newProjection = new ProjectionImpl(newOrigin, newTermination, result);
 				result.myProjectionMap.put(newTermination, newProjection);
 			} catch (StructuralException e) {
 				throw new CloneNotSupportedException("Problem copying Projectio: " + e.getMessage());
 			}
 		}
 		
 		for (Origin exposed : getOrigins()) {
 			String name = exposed.getName();
 			Origin wrapped = ((OriginWrapper) exposed).getWrappedOrigin();
 			try {
 				Origin toExpose = result.getNode(wrapped.getNode().getName()).getOrigin(wrapped.getName());
 				result.exposeOrigin(toExpose, name);				
 			} catch (StructuralException e) {
 				throw new CloneNotSupportedException("Problem exposing Origin: " + e.getMessage());
 			}
 		}
 
 		for (Termination exposed : getTerminations()) {
 			String name = exposed.getName();
 			Termination wrapped = ((TerminationWrapper) exposed).getWrappedTermination();
 			try {
 				Termination toExpose = result.getNode(wrapped.getNode().getName()).getTermination(wrapped.getName());
 				result.exposeTermination(toExpose, name);
 			} catch (StructuralException e) {
 				throw new CloneNotSupportedException("Problem exposing Termination: " + e.getMessage());
 			}
 		}
 
 		result.myListeners = new ArrayList<Listener>(5);
 		
 		result.myMetaData = new HashMap<String, Object>(10);
 		for (String key : myMetaData.keySet()) {
 			Object o = myMetaData.get(key);
 			if (o instanceof Cloneable) {
 				Object copy = tryToClone((Cloneable) o);
 				result.myMetaData.put(key, copy);
 			} else {
 				result.myMetaData.put(key, o);
 			}
 		}
 		
 		//TODO: take another look at Probe design (maybe Probeables reference Probes?)  
 		result.mySimulator = mySimulator.clone();
 		result.mySimulator.initialize(result);
 		Probe[] oldProbes = mySimulator.getProbes();
 		for (int i = 0; i < oldProbes.length; i++) {
 			Probeable target = oldProbes[i].getTarget();
 			if (target instanceof Node) {
 				Node oldNode = (Node) target;
 				if (oldProbes[i].isInEnsemble()) {
 					try {
 						Ensemble oldEnsemble = (Ensemble) getNode(oldProbes[i].getEnsembleName());
 						int neuronIndex = -1;
 						for (int j = 0; j < oldEnsemble.getNodes().length && neuronIndex < 0; j++) {
 							if (oldNode == oldEnsemble.getNodes()[j]) neuronIndex = j;
 						}
 						result.mySimulator.addProbe(oldProbes[i].getEnsembleName(), neuronIndex, oldProbes[i].getStateName(), true);						
 					} catch (SimulationException e) {
 						ourLogger.warn("Problem copying Probe", e);
 					} catch (StructuralException e) {
 						ourLogger.warn("Problem copying Probe", e);
 					}
 				} else {
 					try {
 						result.mySimulator.addProbe(oldNode.getName(), oldProbes[i].getStateName(), true);
 					} catch (SimulationException e) {
 						ourLogger.warn("Problem copying Probe", e);
 					}
 				}
 			} else {
 				ourLogger.warn("Can't copy Probe on type " + target.getClass().getName() 
 						+ " (to be addressed in a future release)");
 			}
 		}
 
 		return result;
 	}
 	
 	private static Object tryToClone(Cloneable o) {
 		Object result = null;
 		
 		try {
 			Method cloneMethod = o.getClass().getMethod("clone", new Class[0]);
 			result = cloneMethod.invoke(o, new Object[0]);
 		} catch (Exception e) {
 			ourLogger.warn("Couldn't clone data of type " + o.getClass().getName(), e);
 		} 
 		
 		return result;
 	}
 
 }
