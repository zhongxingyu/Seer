 /* JEEvent - Decompiled by JODE
  * Visit http://jode.sourceforge.net/
  */
 package ddg.kernel;
 
 import java.util.Vector;
 
 /**
  * TODO make doc
  * 
  * @author thiago - thiago@lsd.ufcg.edu.br
  */
 public class JEEvent {
 
 	private static int eventId = 0;
 
 	private final int myEventId;
 	private final String name;
 
 	private Integer theTargetHandlerId;
 	private JETime theScheduledTime;
 	private Vector theParameterList;
 
 	/**
 	 * @param aName
 	 * @param aHandler
 	 * @param aScheduledTime
 	 */
 	public JEEvent(String aName, JEEventHandler aHandler, JETime aScheduledTime) {
 
 		myEventId = eventId++;
 
		name = aName;
 		theTargetHandlerId = aHandler.getHandlerId();
 		theScheduledTime = aScheduledTime;
 		theParameterList = new Vector();
 	}
 
 	/**
 	 * @return
 	 */
 	public Integer getTheTargetHandlerId() {
 		return theTargetHandlerId;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return
 	 */
 	public JETime getTheScheduledTime() {
 		return theScheduledTime;
 	}
 
 	/**
 	 * @return
 	 */
 	public Vector getTheParameterList() {
 		return new Vector(theParameterList);
 	}
 
 	/**
 	 * @return
 	 */
 	public int getEventId() {
 		return myEventId;
 	}
 
 	/**
 	 * @param theTargetHandlerId
 	 */
 	public void setTheTargetHandlerId(Integer theTargetHandlerId) {
 		this.theTargetHandlerId = theTargetHandlerId;
 	}
 }
