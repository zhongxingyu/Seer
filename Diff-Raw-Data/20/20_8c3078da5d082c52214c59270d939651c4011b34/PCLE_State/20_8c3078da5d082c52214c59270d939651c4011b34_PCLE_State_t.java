 package pkgPriceCLE;
 
 import javax.swing.JOptionPane;
 
 //====================================================================
 /** PCLE_State
  *  <P>Purpose: This class defines the different states in which
  *  an experiment can exist in.
  *  @author Dr. Rick Coleman
  *  @version 1.0
  *  Date: February, 2009
  */
 //===================================================================
 
 public class PCLE_State
 {
 	/** State for this state */
 	private int m_iState;
 
 	//----- Vars for show instructions mode -----
 	private String m_sInsFileName;
 	
 	//----- Vars for show condtion validation -----
 	private String m_sValidConditions;
	private boolean m_bRequireCondition;
 	
 	//----- Vars for all modes -----
 	private int m_iChangeStateBy;
 	
 	//-------- Defined Modes ----------
 	/** Display instructions mode */
 	public static final int SHOW_INSTRUCTIONS = 1;
 	/** Rate ease of learning mode */
 	public static final int RATE_EASE_OF_LEARNING = 2;
 	/** Estimate number to remember mode */
 	public static final int ESTIMATE_NUMBER_TO_REMEMBER_PRE = 3;
 	/** Estimate number to remember mode */
 	public static final int ESTIMATE_NUMBER_TO_REMEMBER_POST = 4;
 	/** Estimate number correct */
 	public static final int ESTIMATE_NUMBER_CORRECT = 5;
 	/** Learning mode */
 	public static final int LEARNING = 6;
 	/** Testing mode */
 	public static final int TESTING = 7;
 	/** Display instructions/point notification mode */
 	public static final int SHOW_POINT_NOTIFICATION = 8;
 
 	//---------- Change mode indicator -------
 	/** Click Continue button to change mode */
 	public static final int CLICK_CONTINUE = 1;
 	
 	//---------------------------------------------------------
 	/** Default constructor */
 	//---------------------------------------------------------
 	public PCLE_State()
 	{
		m_bRequireCondition = false;
 	}
 	
 	//---------------------------------------------------------
 	/** Set the mode */
 	//---------------------------------------------------------
 	public void setState(int m)
 	{
 		m_iState = m;
 	}
 
 	//---------------------------------------------------------
 	/** Set the mode */
 	//---------------------------------------------------------
 	public int getState()
 	{
 		return m_iState;
 	}
 
 	//---------------------------------------------------------
 	/** Set the change by action */
 	//---------------------------------------------------------
 	public void setChangeStateAction(int c)
 	{
 		m_iChangeStateBy = c;
 	}
 
 	//---------------------------------------------------------
 	/** Set the mode */
 	//---------------------------------------------------------
 	public int getChangeStateAction()
 	{
 		return m_iChangeStateBy;
 	}
 	
 	//---------------------------------------------------------
 	/** Set the instruction file name */
 	//---------------------------------------------------------
 	public void setInstructionFile(String s)
 	{
 		m_sInsFileName = s;
 	}
 
 	//---------------------------------------------------------
 	/** Set the mode */
 	//---------------------------------------------------------
 	public String getInstructionFile()
 	{
 		return m_sInsFileName;
 	}
 
 	//---------------------------------------------------------
 	/** Set the mode */
 	//---------------------------------------------------------
 	public void setValidConditions(String conditions) {
		if (conditions.length() > 0) {
			m_bRequireCondition = true;
		}
 		m_sValidConditions = conditions;
 	}
 
 	//---------------------------------------------------------
 	/** validate the condition we are currently in */
 	//---------------------------------------------------------
 	public boolean validCondition(String condition) {
		if (!m_bRequireCondition)
			return true;
 		return m_sValidConditions.contains(condition);
 	}
 
 }
