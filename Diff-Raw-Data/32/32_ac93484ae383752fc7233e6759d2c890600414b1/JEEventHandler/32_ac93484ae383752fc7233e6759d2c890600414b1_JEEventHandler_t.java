 /* JEEventHandler - Decompiled by JODE
  * Visit http://jode.sourceforge.net/
  */
 package ddg.kernel;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * TODO make doc
  * 
  * @author thiago - thiago@lsd.ufcg.edu.br
  */
 public abstract class JEEventHandler {
 
 	private static Integer theUniqueHandlerId;
 	private final Integer HandlerId;
 	protected List parameterlist;
 	private final JEEventScheduler theUniqueEventScheduler;
 
 	/**
 	 * @param scheduler
 	 */
 	public JEEventHandler(JEEventScheduler scheduler) {
 
 		if (theUniqueHandlerId != null) {
			theUniqueHandlerId = Integer.valueOf(theUniqueHandlerId.intValue() + 1);
 		} else {
			theUniqueHandlerId = Integer.valueOf(1);
 		}
 		HandlerId = theUniqueHandlerId;
 		parameterlist = new LinkedList();
 		theUniqueEventScheduler = scheduler;
 		theUniqueEventScheduler.register_handler(this);
 	}
 
 	/**
 	 * @return
 	 */
 	protected JEEventScheduler getScheduler() {
 		return theUniqueEventScheduler;
 	}
 
 	/**
 	 * @param jeevent
 	 */
 	public abstract void event_handler(JEEvent jeevent);
 
 	/**
 	 * @param anEvent
 	 */
 	public void send(JEEvent anEvent) {
 		theUniqueEventScheduler.queue_event(anEvent);
 	}
 
 	/**
 	 * @return
 	 */
 	public Integer getHandlerId() {
 		return HandlerId;
 	}
 }
