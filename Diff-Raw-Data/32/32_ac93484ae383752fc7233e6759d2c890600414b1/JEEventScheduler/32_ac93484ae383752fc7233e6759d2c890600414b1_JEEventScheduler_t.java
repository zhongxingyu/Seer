 /* JEEventScheduler - Decompiled by JODE
  * Visit http://jode.sourceforge.net/
  */
 package ddg.kernel;
 
 import java.util.Vector;
 
 /**
  * TODO make doc
  * 
  * @author thiago - thiago@lsd.ufcg.edu.br
  */
 public final class JEEventScheduler {
 
 	private JETime now = new JETime(0L);
 	private Vector<JEEvent> EventList = new Vector<JEEvent>();
 	private Vector<JEEventHandler> HandlerList;
 	private Boolean isActive;
 	private JETime theEmulationEnd;
 
 	/**
      * 
      */
 	public JEEventScheduler() {
 		this(null);
 	}
 
 	/**
 	 * @param emulationEnd
 	 */
 	public JEEventScheduler(JETime emulationEnd) {
 
 		EventList.setSize(10000);
 		EventList.clear();
 		HandlerList = new Vector<JEEventHandler>();
 		HandlerList.setSize(100);
 		HandlerList.clear();
		isActive = Boolean.valueOf(false);
 		theEmulationEnd = emulationEnd;
 	}
 
 	/**
 	 * @param aNewEvent
 	 */
 	public void queue_event(JEEvent aNewEvent) {
 
 		JETime anEventTime = aNewEvent.getTheScheduledTime();
 
 		if (anEventTime.isEarlierThan(now())) {
 			throw new RuntimeException("ERROR: emulation time(" + now()
 					+ ") already ahead of event time(" + anEventTime
 					+ "). Event is outdated and will not be processed.");
 		}
 
 		int queue_size = EventList.size();
 
 		if (queue_size == 0) {
 			EventList.addElement(aNewEvent);
 		} else if (EventList.lastElement().getTheScheduledTime()
 				.isEarlierThan(anEventTime)) {
 			EventList.addElement(aNewEvent);
 		} else {
 
 			int queue_pos;
 
 			for (queue_pos = queue_size - 1; ((queue_pos > 0) & anEventTime
 					.isEarlierThan(EventList.elementAt(queue_pos)
 							.getTheScheduledTime())); queue_pos--) {
 				/* empty */
 			}
 
 			if (++queue_pos == 1
 					& anEventTime.isEarlierThan(EventList.elementAt(0)
 							.getTheScheduledTime())) {
 				queue_pos--;
 			}
 
 			EventList.insertElementAt(aNewEvent, queue_pos);
 		}
 	}
 
 	/**
 	 * @param anObsoleteEvent
 	 */
 	public void cancel_event(JEEvent anObsoleteEvent) {
 
 		if (anObsoleteEvent == null) {
 			throw new NullPointerException();
 		}
 		EventList.remove(anObsoleteEvent);
 	}
 
 	/**
 	 * @param aNewEventHandler
 	 * @return
 	 */
 	public JEEventScheduler register_handler(JEEventHandler aNewEventHandler) {
 
 		Integer aHandlerId = aNewEventHandler.getHandlerId();
 
 		if (HandlerList.size() < aHandlerId.intValue() - 1) {
 			HandlerList.setSize(aHandlerId.intValue() - 1);
 		}
 
 		if (HandlerList.size() > aHandlerId.intValue() - 1) {
 			HandlerList.removeElementAt(aHandlerId.intValue() - 1);
 		}
 
 		HandlerList
 				.insertElementAt(aNewEventHandler, aHandlerId.intValue() - 1);
 		return this;
 	}
 
 	/**
      * 
      */
 	public void start() {
 
 		if (!EventList.isEmpty()) {
 			schedule();
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private JEEvent peek() {
 
 		if (!EventList.isEmpty()) {
 			JEEvent aNextEvent = EventList.elementAt(0);
 			EventList.removeElementAt(0);
 			return aNextEvent;
 		}
 
 		return null;
 	}
 
 	/**
      * 
      */
 	private void schedule() {
 
		isActive = Boolean.valueOf(true);
 
 		while (!EventList.isEmpty() & isActive.booleanValue()
 				& isEarlierThanEmulationEnd(now())) {
 
 			JEEvent aNextEvent = peek();
 
 			if (aNextEvent != null) {
 
 				JETime anEventTime = aNextEvent.getTheScheduledTime();
 
 				if (anEventTime.isEarlierThan(now())) {
 					throw new RuntimeException("ERROR: emulation time(" + now()
 							+ ") " + "already ahead of event time("
 							+ anEventTime
 							+ "). Event is outdated and will not be processed.");
 				}
 
 				if (isEarlierThanEmulationEnd(anEventTime)) {
 					now = anEventTime;
 					processEvent(aNextEvent);
 				} else {
 					now = theEmulationEnd;
 				}
 			}
 		}
 
 		isActive = Boolean.valueOf(false);
 	}
 
 	private boolean isEarlierThanEmulationEnd(JETime time) {
 		return (theEmulationEnd != null) ? time.isEarlierThan(theEmulationEnd)
 				: true;
 	}
 
 	/**
 	 * @param aNextEvent
 	 */
 	private void processEvent(JEEvent aNextEvent) {
 
 		Integer aTargetHandlerId = aNextEvent.getTheTargetHandlerId();
 
 		if (HandlerList.elementAt(aTargetHandlerId.intValue() - 1) == null) {
 			throw new RuntimeException("ERROR: no Handler at vector position "
 					+ (aTargetHandlerId.intValue() - 1)
 					+ ". Something's wrong here, dude.");
 		}
 
 		HandlerList.elementAt(aTargetHandlerId.intValue() - 1).event_handler(
 				aNextEvent);
 	}
 
 	/**
 	 * @return
 	 */
 	public JETime now() {
 		return now;
 	}
 }
