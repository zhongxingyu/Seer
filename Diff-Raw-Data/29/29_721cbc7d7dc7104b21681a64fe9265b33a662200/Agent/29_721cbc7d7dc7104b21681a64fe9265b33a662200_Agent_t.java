 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.simulation.model;
 
 import java.awt.Shape;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.logging.Logger;
 
 import de.uniluebeck.imis.casi.CASi;
 import de.uniluebeck.imis.casi.simulation.engine.ISimulationClockListener;
 import de.uniluebeck.imis.casi.simulation.engine.SimulationClock;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.AbstractAction;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.IActionScheduler;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.schedulers.DefaultActionScheduler;
 import de.uniluebeck.imis.casi.utils.Tools;
 
 /**
  * 
  * @author Tobias Mende, Marvin Frick, Moritz B&uuml;rger
  * 
  *         XXX enhance this draft, it's really raw
  */
 public class Agent extends AbstractComponent implements
 		ISimulationClockListener, Serializable {
 	private static final long serialVersionUID = -1166856593367347255L;
 	private static final Logger log = Logger.getLogger(Agent.class.getName());
 
 	public enum STATE {
 		ABSTRACT, IDLE, BUSY, UNKNOWN;
 	}
 
 	// Name of the agent
 	private String name;
 	private String type;
 	private IPosition defaultPosition;
 	private AbstractAction currentAction;
 	private IActionScheduler actionScheduler;
 
 	/** Listeners, perhaps already needed in AbstractComponent */
 	private transient ArrayList<IAgentListener> agentListeners = new ArrayList<IAgentListener>();
 	private Agent.STATE state = STATE.UNKNOWN;
 
 	private Agent(String identifier) {
 		super(identifier);
 		actionScheduler = new DefaultActionScheduler();
 	}
 
 	/**
 	 * Constructor for an abstract agent.
 	 * 
 	 * @param identifier
 	 *            the id of this agent type
 	 */
 	public Agent(String identifier, String type) {
 		this(identifier);
 		this.type = type;
 		state = STATE.ABSTRACT;
 	}
 
 	/**
 	 * Constructor for an Agent.
 	 * 
 	 * @param name
 	 *            the name of the agent
 	 * @param type
 	 *            the type of the agent
 	 * @param defaultPosition
 	 *            the default position
 	 */
 	public Agent(String identifier, String name, String type,
 			IPosition defaultPosition) {
 		this(identifier);
 		this.name = name;
 		this.type = type;
 		this.defaultPosition = defaultPosition;
 	}
 
 	/**
 	 * Constructor for an agent
 	 * 
 	 * @param name
 	 *            the name of the agent
 	 * @param type
 	 *            the type of the agent
 	 */
 	public Agent(String identifier, String name, String type) {
 		this(identifier, name, type, null);
 	}
 
 	public void setState(STATE state) throws IllegalStateException {
 		if (state.equals(STATE.ABSTRACT) && this.state != STATE.UNKNOWN) {
 			// Deny changes to unknown or abstract
 			throw new IllegalStateException("Can't change the state to "
 					+ state);
 		}
 		if (state.equals(STATE.ABSTRACT)) {
 			// remove the agent as clock listener if state changes to abstract
 			SimulationClock.getInstance().removeListener(this);
 		} else if (this.state.equals(STATE.ABSTRACT)) {
 			// add the agent as clock listener if state changes from abstract to
 			// some other state
 			SimulationClock.getInstance().addListener(this);
 		}
 		state = this.state;
 	}
 
 	public boolean isTemplate() {
 		return state.equals(STATE.ABSTRACT);
 	}
 
 	public void setTodoList(Collection<AbstractAction> todoList) {
 		actionScheduler.setTodoList(todoList);
 		for(AbstractAction a : todoList) {
 			a.setState(AbstractAction.STATE.SCHEDULED);
 		}
 	}
 
 	public void addActionToPool(AbstractAction action) {
 		actionScheduler.addPoolAction(action);
 	}
 
 	public void addActionToList(AbstractAction action) {
 		action.setState(AbstractAction.STATE.SCHEDULED);
 		actionScheduler.schedule(action);
 	}
 
 	/**
 	 * Tries to set an interrupt action
 	 * 
 	 * @param action
 	 *            the action to interrupt the agent with
 	 * @return <code>true</code> if the agent could be interrupted,
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean interrupt(AbstractAction action) {
 		if (isInterruptible()) {
 			actionScheduler.addInterruptAction(action);
 			setInterruptibility(INTERRUPTIBILITY.INTERRUPT_SCHEDULED);
 			return true;
 		}
 		return false;
 	}
 
 	public void setActionPool(Collection<AbstractAction> actionPool) {
 		actionScheduler.setActionPool(actionPool);
 	}
 
 	public void addListener(IAgentListener listener) {
 		if (!agentListeners.contains(listener)) {
 			agentListeners.add(listener);
 		}
 	}
 
 	public void removeListener(IAgentListener listener) {
 		agentListeners.remove(listener);
 	}
 
 	@Override
 	public void timeChanged(SimulationTime newTime) {
 		if (actionScheduler.isInterruptScheduled()
 				&& interruptibility
 						.equals(INTERRUPTIBILITY.INTERRUPT_SCHEDULED)) {
 			// store current action as next action and perform interrupt action
 			AbstractAction tmp = currentAction;
 			currentAction = actionScheduler.getNextAction();
 			actionScheduler.addInterruptAction(tmp);
 			setInterruptibility(INTERRUPTIBILITY.INTERRUPTED);
 		} else if (currentAction == null || currentAction.isCompleted()) {
 			setInterruptibility(INTERRUPTIBILITY.INTERRUPTIBLE);
 			// need a new action to perform:
 			currentAction = actionScheduler.getNextAction();
 			if (currentAction == null) {
 				CASi.SIM_LOG.info(this.name + ": Nothing to do at the moment.");
 				return;
 			}
 		}
 		try {
 			currentAction.perform(this);
 			if(currentAction.getState().equals(AbstractAction.STATE.INTERRUPTED)) {
 				CASi.SIM_LOG.info("Current action was interrupted. Schedule for later");
 				currentAction.reset();
 				addActionToList(currentAction);
 				currentAction = null;
 			}
 		} catch (IllegalAccessException e) {
 			CASi.SIM_LOG
 					.severe("The last action wasn't performable. "
 							+ this.name
 							+ " has better things to do than solving impossible problems. E.g Searching for a Club Mate! State: "+currentAction.getState());
 			currentAction = null;
 		}
 	}
 
 	/**
 	 * Method for cloning an agent and its components to a relay new agent with
 	 * state IDLE.
 	 * 
 	 * @return the clone
 	 */
 	public Agent clone() {
 		Agent newAgent;
 		try {
 			newAgent = (Agent) Tools.deepCopy(this);
 			newAgent.setState(STATE.IDLE);
 			return newAgent;
 		} catch (Exception e) {
 			log.severe("An error occured while cloning the agent: "
 					+ e.getLocalizedMessage());
 			log.severe(e.fillInStackTrace().toString());
 		}
 		return null;
 	}
 
 	@Override
 	public void simulationPaused(boolean pause) {
 		// nothing to do here
 
 	}
 
 	@Override
 	public void simulationStopped() {
 		// nothing to do here
 
 	}
 
 	@Override
 	public void simulationStarted() {
 		// nothing to do here
 
 	}
 
 	@Override
 	public boolean contains(IPosition position) {
 		// Agent contains nothing
 		// TODO think about better way
 		return false;
 	}
 
 	@Override
 	public boolean contains(Point2D point) {
 		return getShapeRepresentation().contains(point);
 	}
 
 	@Override
 	public Shape getShapeRepresentation() {
 		return new Rectangle2D.Double(getCoordinates().getX(), getCoordinates().getY(), 1, 1);
 	}
 
 	@Override
 	public Point2D getCentralPoint() {
 		return getCoordinates();
 	}
 
 	@Override
 	public void setCoordinates(Point2D coordinates) {
 		Point2D oldPoint = super.getCoordinates();
 		super.setCoordinates(coordinates);
 		informListenersAboutPositionChange(oldPoint, coordinates);
 	}
 
	/**
	 * Informs all listener about a new position
	 * @param oldPoint where the agent came from
	 * @param newPoint where the agent goes to.
	 */
 	private void informListenersAboutPositionChange(Point2D oldPoint,
 			Point2D newPoint) {
//		log.info("Position of "+toString()+" changed from "+oldPoint+" to "+newPoint);
 		for (IAgentListener listener : agentListeners) {
 			listener.positionChanged(oldPoint, newPoint);
 		}
 	}
 	
 	public IPosition getDefaultPosition() {
 		return defaultPosition==null? getCurrentPosition(): defaultPosition;
 	}
 	
 	/**
 	 * @param defaultPosition the defaultPosition to set
 	 */
 	public void setDefaultPosition(IPosition defaultPosition) {
 		this.defaultPosition = defaultPosition;
 		coordinates = defaultPosition.getCentralPoint();
 		Point2D oldPoint = super.getCoordinates();
 		informListenersAboutPositionChange(oldPoint, coordinates);
 	}
 
 }
