 /*******************************************************************************
  * TerseHandling (version 0.9.50)
  * Copyright (c) 2013 Jean Pierre Charalambos.
  * @author Jean Pierre Charalambos
  * https://github.com/remixlab
  *   
  * All rights reserved. Library that eases the creation of interactive
  * scenes released under the terms of the GNU Public License v3.0
  * which is available at http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package remixlab.tersehandling.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import remixlab.tersehandling.event.TerseEvent;
 
 /**
  * An Agent is a high-level TerseEvent parser, which holds a {@link #pool()} of
  * application objects from where it should continuously decide its input {@link #grabber()},
  * which is done by explicitly invoking {@link #updateGrabber(TerseEvent)}, if no  
  * {@link #defaultGrabber()} has been set.
  * In its simplest, non-generic, form, an agent acts as a channel between input
  * events and grabbers, transmitting just the TerseEvent (reduced
  * input event) to its {@link #grabber()}.
  * <p>
  * More specialized, generic, agents also hold Profiles, each containing a
  * mapping between TerseEvent shortcuts and user-defined actions. Hence, thanks
  * to its Profiles, generic agents further parse TerseEvents to determine the
  * user-defined action its input grabber should perform, which is done by
  * explicitly invoking {@link #handle(TerseEvent)}.
  */
 public class Agent {
 	protected TerseHandler handler;
 	protected String nm;
 	protected List<Grabbable> grabbers;
 	protected Grabbable trackedGrabber;
 	protected Grabbable defaultGrabber;
 	protected boolean agentTrckn;
 
 	public Agent(TerseHandler hndlr, String n) {
 		handler = hndlr;
 		nm = n;
 		grabbers = new ArrayList<Grabbable>();
 		setTracking(true);
 		handler.registerAgent(this);
 	}
 
 	public String name() {
 		return nm;
 	}
 
 	/**
 	 * Returns {@code true} if this agent is tracking its grabbers.
 	 * <p>
 	 * You may need to {@link #enableTracking()} first.
 	 */
 	public boolean isTracking() {
 		return agentTrckn;
 	}
 
 	public void enableTracking() {
 		setTracking(true);
 	}
 
 	public void disableTracking() {
 		setTracking(false);
 	}
 
 	/**
 	 * Sets the {@link #isTracking()} value.
 	 */
 	public void setTracking(boolean enable) {
 		agentTrckn = enable;
 		if (!isTracking())
 			setTrackedGrabber(null);
 	}
 
 	/**
 	 * Calls {@link #setTracking(boolean)} to toggle the {@link #isTracking()}
 	 * value.
 	 */
 	public void toggleTracking() {
 		setTracking(!isTracking());
 	}
 
 	public Grabbable updateGrabber(TerseEvent event) {
 		if (event == null || !handler.isAgentRegistered(this) || !isTracking())
 			return trackedGrabber();
 
		Grabbable g = trackedGrabber();
		
		//We first check if tracked grabber remains the same
		if (g != null)
			if(g.checkIfGrabsInput(event))
				return trackedGrabber();
		
 		setTrackedGrabber(null);
 		for (Grabbable mg : pool()) {
 			// take whatever. Here the first one
 			if (mg.checkIfGrabsInput(event)) {
 				setTrackedGrabber(mg);
 				return trackedGrabber();
 			}
 		}
 		return trackedGrabber();
 	}
 
 	public void enqueueEventTuple(EventGrabberTuple eventTuple) {
 		if (eventTuple != null && handler.isAgentRegistered(this))
 			handler.enqueueEventTuple(eventTuple);
 	}
 
 	public String info() {
 		String description = new String();
 		description += name();
 		description += "\n";
 		description += "Nothing to be said, except that generic Agents hold more interesting info\n";
 		return description;
 	}
 
 	// just enqueue grabber
 	public void handle(TerseEvent event) {
 		if (event == null || !handler.isAgentRegistered(this)
 				|| grabber() == null)
 			return;
 		handler.enqueueEventTuple(new EventGrabberTuple(event, grabber()));
 	}
 
 	public TerseEvent feed() {
 		return null;
 	}
 
 	public TerseHandler terseHandler() {
 		return handler;
 	}
 
 	/**
 	 * Returns a list containing references to all the active grabbers.
 	 * <p>
 	 * Used to parse all the grabbers and to check if any of them
 	 * {@link remixlab.tersehandling.core.Grabbable#grabsAgent(Agent)}.
 	 */
 	public List<Grabbable> pool() {
 		return grabbers;
 	}
 
 	/**
 	 * Removes the grabber from the {@link #pool()}.
 	 * <p>
 	 * See {@link #addInPool(Grabbable)} for details. Removing a grabber
 	 * that is not in {@link #pool()} has no effect.
 	 */
 	public boolean removeFromPool(Grabbable deviceGrabber) {
 		return pool().remove(deviceGrabber);
 	}
 
 	/**
 	 * Clears the {@link #pool()}.
 	 * <p>
 	 * Use this method only if it is faster to clear the {@link #pool()} and
 	 * then to add back a few grabbers than to remove each one
 	 * independently.
 	 */
 	public void clearPool() {
 		pool().clear();
 	}
 
 	/**
 	 * Returns true if the grabber is currently in the agents {@link #pool()}
 	 * list.
 	 * <p>
 	 * When set to false using {@link #removeFromPool(Grabbable)}, the handler no
 	 * longer {@link remixlab.tersehandling.core.Grabbable#checkIfGrabsInput(TerseEvent)}
 	 * on this grabber. Use {@link #addInPool(Grabbable)} to insert it 	 * back.
 	 */
 	public boolean isInPool(Grabbable deviceGrabber) {
 		return pool().contains(deviceGrabber);
 	}
 
 	/**
 	 * Returns the current grabber, or {@code null} if none currently grabs events.
 	 */
 	public Grabbable trackedGrabber() {
 		return trackedGrabber;
 	}
 
 	public Grabbable grabber() {
 		if (trackedGrabber() != null)
 			return trackedGrabber();
 		else
 			return defaultGrabber();
 	}
 
 	public Grabbable defaultGrabber() {
 		return defaultGrabber;
 	}
 
 	public void setDefaultGrabber(Grabbable g) {
 		defaultGrabber = g;
 	}
 
 	/**
 	 * Adds the grabber in the {@link #pool()}.
 	 * <p>
 	 * Use {@link #removeFromPool(Grabbable)} to remove the grabber from
 	 * the pool, so that it is no longer tested with
 	 * {@link remixlab.tersehandling.core.Grabbable#checkIfGrabsInput(TerseEvent)}
 	 * by the handler, and hence can no longer grab the agent focus. Use 
 	 * {@link #isInPool(Grabbable)} to know the current state of the grabber.
 	 */
 	public boolean addInPool(Grabbable deviceGrabber) {
 		if (deviceGrabber == null)
 			return false;
 		if (!isInPool(deviceGrabber)) {
 			pool().add(deviceGrabber);
 			return true;
 		}
 		return false;
 	}
 
 	
 	/**
 	 * Internal use
 	 */
 	private void setTrackedGrabber(Grabbable deviceGrabber) {
 		if (deviceGrabber == null) {
 			trackedGrabber = null;
 		} else if (isInPool(deviceGrabber)) {
 			trackedGrabber = deviceGrabber;
 		}
 	}
 }
