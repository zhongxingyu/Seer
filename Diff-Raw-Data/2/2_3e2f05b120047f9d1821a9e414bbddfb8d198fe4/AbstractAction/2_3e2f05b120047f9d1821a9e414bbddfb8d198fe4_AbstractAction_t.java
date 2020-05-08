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
 package de.uniluebeck.imis.casi.simulation.model.actionHandling;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.logging.Logger;
 
 import de.uniluebeck.imis.casi.CASi;
 import de.uniluebeck.imis.casi.simulation.model.AbstractComponent;
 import de.uniluebeck.imis.casi.simulation.model.SimulationTime;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.AtomicAction;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.ComplexAction;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.IActionListener;
 import de.uniluebeck.imis.casi.utils.Listenable;
 import de.uniluebeck.imis.casi.utils.Tools;
 
 /**
  * A template for an action that can be performed by agents.
  * 
  * @author Tobias Mende
  * 
  */
 public abstract class AbstractAction implements Listenable<IActionListener>,
 		Serializable {
 	/** the id for serialization */
 	private static final long serialVersionUID = -4600404747026813557L;
 	/** the development logger */
 	private static final Logger log = Logger.getLogger(AbstractAction.class
 			.getName());
 	/** flag for checking whether the action was initialized or not */
 	protected boolean initialized;
 
 	/**
 	 * A pool of states in which this action can be
 	 */
 	public enum STATE {
 		/** the action was added to a todo list of an agent */
 		SCHEDULED,
 		/** the action was started but not finished yet */
 		ONGOING,
 		/** the action was completed */
 		COMPLETED,
 		/** The action can't be performed at the moment */
 		INTERRUPTED, UNKNOWN,
 		/** The action is not used in any list at the moment */
 		RAW
 	}
 
 	/** A pool of action types */
 	public enum TYPE {
 		/** the action is only a template */
 		ABSTRACT,
 		/** the action is real action (ready to be scheduled) */
 		NORMAL
 	}
 
 	/** the type of this action */
 	private TYPE type = TYPE.NORMAL;
 
 	/** the state of this action */
 	private STATE state = STATE.RAW;
 
 	/** the priority of this action */
 	protected int priority = 5;
 
 	/** the duration of this action in seconds */
 	private int duration = -1;
 
 	/** The earliest time for starting with this action */
 	private SimulationTime earliestStartTime;
 	/** The latest time when this simulation should be finished */
 	private SimulationTime deadline;
 
 	/** A collection of listeners that listen for events */
 	private transient Collection<IActionListener> listeners = new ArrayList<IActionListener>();
 
 	/**
 	 * Method for performing and continuing this action. Should be called in
 	 * every tick in which the action must be performed.
 	 * 
 	 * @param performer
 	 *            the component which performs this action
 	 * 
 	 * @return <code>true</code> if the action and all its subactions are
 	 *         completed, <code>false</code> otherwise.
 	 * @throws IllegalAccessException
 	 *             if the action is abstract or not performable
 	 */
 	public abstract boolean perform(AbstractComponent performer)
 			throws IllegalAccessException;
 
 	/**
 	 * Method for internal performing the action. Must be implemented in actions
 	 * to customize the behavior. Is ignored by complex actions.
 	 * 
 	 * @param performer
 	 *            the component which performs this action
 	 * 
 	 * @return <code>true</code> if the action is completed, <code>false</code>
 	 *         otherwise. Should return <code>false</code> if the action should
 	 *         be completed automatically after a period of time (duration)
 	 */
 	protected abstract boolean internalPerform(AbstractComponent performer);
 
 	/**
 	 * Method for scheduling this action
 	 * 
 	 * @throws IllegalAccessException
 	 *             if the action is abstract
 	 */
 	public final void schedule() throws IllegalAccessException {
 		if (!isPerformable()) {
 			throw new IllegalAccessException(
 					"Can't schedule an abstract action");
 		}
 		setState(STATE.SCHEDULED);
 	}
 
 	/* === Getter and Setter */
 
 	/**
 	 * Setter for the state of this action
 	 * 
 	 * @param state
 	 *            the state to set
 	 * @throws IllegalStateException
 	 *             if the provided state is invalid according to the current
 	 *             state
 	 */
 	public final synchronized void setState(STATE state)
 			throws IllegalStateException {
 		if ((state.equals(STATE.UNKNOWN) && !this.state.equals(STATE.UNKNOWN))) {
 			throw new IllegalStateException("Can't set state to " + state);
 		}
 		STATE oldState = this.state;
 		this.state = state;
 		if (!oldState.equals(state)) {
 			for (IActionListener listener : listeners) {
 				listener.stateChanged(state);
 			}
 		}
 	}
 
 	/**
 	 * Setter for the deadline for this action.
 	 * 
 	 * @param deadline
 	 *            the deadline to set
 	 */
 	public void setDeadline(SimulationTime deadline) {
 		this.deadline = deadline;
 	}
 
 	/**
 	 * Sets the earliest time when this action should be performed
 	 * 
 	 * @param earliestStartTime
 	 *            the earliest start time to set
 	 */
 	public void setEarliestStartTime(SimulationTime earliestStartTime) {
 		this.earliestStartTime = earliestStartTime;
 	}
 
 	/**
 	 * Getter for the deadline of this action
 	 * 
 	 * @return the deadline
 	 */
 	public SimulationTime getDeadline() {
 		return deadline;
 	}
 
 	/**
 	 * Getter for the earliest start time of this action
 	 * 
 	 * @return the earliest start time
 	 */
 	public SimulationTime getEarliestStartTime() {
 		return earliestStartTime;
 	}
 
 	/**
 	 * Setter for the priority of this action.
 	 * 
 	 * @param priority
 	 *            a value between <code>0</code> and <code>10</code>
 	 */
 	public void setPriority(int priority) {
 		if (priority > 10) {
 			this.priority = 10;
 			CASi.SIM_LOG.warning(priority
 					+ " is an invalid value. Priority was set to "
 					+ this.priority);
 		} else if (priority < 0) {
 			this.priority = 0;
 			CASi.SIM_LOG.warning(priority
 					+ " is an invalid value. Priority was set to "
 					+ this.priority);
 		} else {
 			this.priority = priority;
 		}
 	}
 
 	/**
 	 * Getter for the priority of this action
 	 * 
 	 * @return the priority
 	 */
 	public int getPriority() {
 		return priority;
 	}
 
 	/**
 	 * Setter for the duration of this action
 	 * 
 	 * @param duration
 	 *            the duration in minutes
 	 * @throws UnsupportedOperationException
 	 *             if the this action consists of subactions
 	 */
 	public void setDuration(int duration) throws UnsupportedOperationException {
 		if (isComplex()) {
 			throw new UnsupportedOperationException(
 					"Can't set duration for a complex action");
 		}
 		this.duration = duration * 60;
 	}
 
 	/**
 	 * Getter for the duration of this action
 	 * 
 	 * @return the duration in minutes, if this action is atomic or the sum of
 	 *         duration of the subactions if this action is complex.
 	 */
 	public abstract int getDuration();
 
 	/* === Type Getter === */
 
 	/**
 	 * Getter for the state of this action
 	 * 
 	 * @return the state
 	 */
 	public STATE getState() {
 		return state;
 	}
 
 	/**
 	 * Setter for the type
 	 * 
 	 * @param type
 	 *            the type to set
 	 * @throws IllegalStateException
 	 *             if the provided type is illegal according to the current type
 	 */
 	public void setType(TYPE type) throws IllegalStateException {
 		if (type.equals(TYPE.ABSTRACT) && !isAbstract()) {
 			throw new IllegalStateException("Can't set type to " + type);
 		}
 		this.type = type;
 	}
 
 	/**
 	 * Getter for the type
 	 * 
 	 * @return the type
 	 */
 	public TYPE getType() {
 		return type;
 	}
 
 	/**
 	 * Checks whether the action is atomic or not.
 	 * 
 	 * @return <code>true</code> if the action hasn't any subactions,
 	 *         <code>false</code> otherwise-
 	 */
 	public final boolean isAtomic() {
 		return (this instanceof AtomicAction);
 	}
 
 	/**
 	 * Checks whether the action is abstract or not.
 	 * 
 	 * @return <code>true</code> if the type is TYPE.ABSTRACT,
 	 *         <code>false</code> otherwise.
 	 */
 	public final boolean isAbstract() {
 		return type.equals(TYPE.ABSTRACT);
 	}
 
 	/**
 	 * 
 	 * @return <code>true</code> if this actions consists of a list of
 	 *         subactions, <code>false</code> otherwise.
 	 */
 	public final boolean isComplex() {
 		return (this instanceof ComplexAction);
 	}
 
 	/**
 	 * Checks whether this action is performable.
 	 * 
 	 * @return <code>true</code> if the action can be performed,
 	 *         <code>false</code> otherwise
 	 */
 	public final boolean isPerformable() {
 		return ((this instanceof ComplexAction) || (this instanceof AtomicAction))
 				&& !type.equals(TYPE.ABSTRACT);
 	}
 
 	/**
 	 * Method for checking whether this action is completed.
 	 * 
 	 * @return <code>true</code> if the action is completed or
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean isCompleted() {
 		return state.equals(STATE.COMPLETED);
 	}
 
 	/* === Listener Handling === */
 	@Override
 	public final void addListener(IActionListener listener) {
 		if (!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	}
 
 	@Override
 	public final void removeListener(IActionListener listener) {
 		listeners.remove(listener);
 	}
 
 	/**
 	 * Getter for the duration in seconds
 	 * 
 	 * @return the duration
 	 */
 	public int getDurationSeconds() {
 		return duration;
 	}
 
 	/**
 	 * Decrements the duration by one second.
 	 * 
 	 * @throws IllegalAccessException
 	 *             if this method isn't called by a direct child
 	 */
 	protected void decrementDurationTime() throws IllegalAccessException {
 		duration--;
 	}
 
 	/**
 	 * Method for cloning an agent and its components to a relay new agent with
 	 * state IDLE.
 	 * 
 	 * @return the clone
 	 */
 	public AbstractAction clone() {
 		AbstractAction newAction;
 		try {
 			newAction = (AbstractAction) Tools.deepCopy(this);
 			newAction.setType(TYPE.NORMAL);
 			newAction.listeners = new ArrayList<IActionListener>();
 			return newAction;
 		} catch (Exception e) {
 			log.severe("An error occured while cloning the action: "
 					+ e.getLocalizedMessage());
 			log.severe(e.fillInStackTrace().toString());
 		}
 		return null;
 	}
 
 	/**
 	 * This isn't a complete equals method... further additions should be made
 	 * in the concrete actions.
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (super.equals(obj)) {
 			return true;
 		}
 		if (!(obj instanceof AbstractAction)) {
 			return false;
 		}
 		AbstractAction other = (AbstractAction) obj;
 		if (!type.equals(other.getType())) {
 			return false;
 		}
 		if (deadline != null && other.getDeadline() != null
 				&& !deadline.equals(other.getDeadline())) {
 			return false;
 		}
 		if (earliestStartTime != null && other.getEarliestStartTime() != null
 				&& !earliestStartTime.equals(other.getDeadline())) {
 			return false;
 		}
 		if (priority != other.getPriority() || duration != other.getDuration()) {
 			return false;
 		}
 		if ((deadline != null && other.getDeadline() == null)
 				|| (deadline == null && other.getDeadline() != null)) {
 			return false;
 		}
 		if ((earliestStartTime != null && other.getEarliestStartTime() == null)
 				|| (earliestStartTime == null && other.getEarliestStartTime() != null)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Can be used to describe a task that should be executed before performing
 	 * this action
 	 * 
 	 * @param performer
 	 *            the performer
 	 * @return <code>true</code> if the initialization was performed successful
 	 *         and the action should be performed, <code>false</code> if
 	 *         something doesn't work as expected and the action should be
 	 *         interrupted.
 	 */
 	protected boolean preActionTask(AbstractComponent performer) {
 		return true;
 	}
 
 	/**
 	 * Can be used to describe a task that should be performed just after
 	 * completing this action.
 	 * 
 	 * @param performer
 	 *            the performer of the action
 	 */
 	protected void postActionTask(AbstractComponent performer) {
 	}
 
 	/**
 	 * Sets the state of the action to {@link STATE}.SCHEDULED
 	 */
 	public void reset() {
 		initialized = false;
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer b = new StringBuffer();
 		b.append(this.getClass().getSimpleName());
 		b.append("[");
 		b.append(type.toString() + ", ");
 		b.append(state.toString() + ", ");
 		b.append("Dur: " + duration + ",");
 		b.append("S: " + earliestStartTime + ",");
 		b.append("D: " + deadline + ",");
 		b.append("P: " + priority);
 		b.append("]");
 		return b.toString();
 	}
 
 	/**
 	 * Getter for the name of the action
 	 * 
 	 * @return the simple name of the action
 	 */
 	public String getName() {
 		return this.getClass().getSimpleName();
 	}
 	
 	/**
 	 * Getter for extended information about this action
	 * @return a description of the action
 	 */
 	public abstract String getInformationDescription();
 
 }
