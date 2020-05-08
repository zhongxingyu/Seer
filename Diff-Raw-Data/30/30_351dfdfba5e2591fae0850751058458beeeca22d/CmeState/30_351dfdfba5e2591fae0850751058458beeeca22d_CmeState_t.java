 package pkgCMEF;
 
 import java.util.HashMap;
 
 import javax.swing.JOptionPane;
 
 //====================================================================
 /**
  * CmeState
  * <P>
  * Purpose: This class defines the different states in which an experiment can
  * exist in.
  * 
  * @author Terry Meacham, Dr. Rick Coleman
  * @version 1.1 Date: February, 2009
  */
 // ===================================================================
 
 public class CmeState {
 	/** State for this state */
 	private int m_iState;
 	
 	private CmeState m_sPrevState;
 	
 	private CmeEventResponse[] m_erEvent = new CmeEventResponse[EVENT_MAX];
 
 	/** HashSet used to store the properties for each state */
 	private HashMap<String, Object> m_sProperties = new HashMap<String, Object>();
 
 	// -------- Defined Modes ----------
 	/** Display instructions mode */
 	public static final int STATE_INSTRUCTION = 1;
 	/** Display feedback mode */
 	public static final int STATE_FEEDBACK = 2;
 	/** Display prompt mode */
 	public static final int STATE_PROMPT = 3;
 	/** Learning mode */
 	public static final int STATE_STUDY = 4;
 	/** Testing mode */
 	public static final int STATE_TEST = 5;
 
 	// ---------- Events -------
 	/** Event for Clicking a Continue Button */
	public static final int EVENT_CLICK_PRIMARY = 0;
 	/** Number of events */
 	public static final int EVENT_MAX = 1;
 
 	// ---------------------------------------------------------
 	/** Default constructor */
 	// ---------------------------------------------------------
 	public CmeState() {
 	}
 
 	// ---------------------------------------------------------
 	/** Set the mode */
 	// ---------------------------------------------------------
 	public void setState(int m) {
 		m_iState = m;
 	}
 
 	// ---------------------------------------------------------
 	/** Set the mode */
 	// ---------------------------------------------------------
 	public int getState() {
 		return m_iState;
 	}
 
 	// ---------------------------------------------------------
 	/** Set the change by action */
 	// ---------------------------------------------------------
 	public void TriggerEvent(int event) {
 		if(event < 0 || event >= EVENT_MAX || m_erEvent[event] == null)
 			return;
 		m_erEvent[event].Respond();
 	}
 	
 	// ---------------------------------------------------------
 	/** Set event response */
 	// ---------------------------------------------------------
 	public void setEventResponse(int eventId, CmeEventResponse event) {
 			m_erEvent[eventId] = event;
 	}
 	
 	// ---------------------------------------------------------
 	/** Set a PropertyValue*/
 	// ---------------------------------------------------------
 	public void setProperty(String name, Object prop) {
 		m_sProperties.put(name, prop);
 	}
 	
 	// ---------------------------------------------------------
 	/** Set event response */
 	// ---------------------------------------------------------
 	public Object getProperty(String name) {
 		return m_sProperties.get(name);
 	}
 	
 	// ---------------------------------------------------------
 	/** Set a PropertyValue*/
 	// ---------------------------------------------------------
 	public void setPreviousState(CmeState state) {
 		m_sPrevState = state;
 	}
 	
 	// ---------------------------------------------------------
 	/** Set event response */
 	// ---------------------------------------------------------
 	public Object getPreviousState() {
 		return m_sPrevState;
 	}
 }
