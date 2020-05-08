 package com.asksven.ledeffects.data;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 
 import com.asksven.ledeffects.manager.EffectsFassade;
 import com.asksven.ledeffects.manager.EffectsState;
 
 public class Preferences
 {
 	public static final String PREFS_NAME = "LedEffectsPrefs";
 	
 	/** Prefs handler */
 	SharedPreferences m_mySettings;
 	
 	/** shall service be started automatically upon boot */
 	private boolean m_bAutostart;
 	
 	private boolean m_bNotifyRing;
 	private boolean m_bNotifyCharge;
 	private boolean m_bNotifySMS;
 	private boolean m_bNotifyMail;
 	private boolean m_bNotifyIM;
 	private boolean m_bApplySleep;
 	
 	private int m_iEffectRing;
 	private int m_iEffectCharge;
 	private int m_iEffectSMS;
 	private int m_iEffectMail;
 	private int m_iEffectIM;
 	private int m_iEffectSleep;
 
 
 
 	/** Timer frequency in seconds*/
 	private int m_iPollInterval;
 
 	
 	/**
 	 * @return the autostart
 	 */
 	public boolean getAutostart()
 	{
 		return m_bAutostart;
 	}
 
 	/**
 	 * @param autostart the autostart to set
 	 */
 	public void setAutostart(boolean autostart)
 	{
 		m_bAutostart = autostart;
 	}
 
 	public boolean getNotifyRing()
 	{
 		return m_bNotifyRing;
 	}
 
 	public void setNotifyRing(boolean bNotify)
 	{
 		m_bNotifyRing = bNotify;
 	}
 
 	public boolean getNotifyCharge()
 	{
 		return m_bNotifyCharge;
 	}
 
 	public void setNotifyCharge(boolean bNotify)
 	{
 		m_bNotifyCharge = bNotify;
 	}
 	public boolean getNotifySMS()
 	{
 		return m_bNotifySMS;
 	}
 
 	public void setNotifySMS(boolean bNotify)
 	{
 		m_bNotifySMS = bNotify;
 	}
 
 	public boolean getNotifyMail()
 	{
 		return m_bNotifyMail;
 	}
 
 	public void setNotifyMail(boolean bNotify)
 	{
 		m_bNotifyMail = bNotify;
 	}
 
 	public boolean getNotifyIM()
 	{
 		return m_bNotifyIM;
 	}
 
 	public void setNotifyIM(boolean bNotify)
 	{
 		m_bNotifyIM = bNotify;
 	}
 	
 	public boolean getApplySleep()
 	{
 		return m_bApplySleep;
 	}
 
 	public void setApplySleep(boolean bApply)
 	{
 		m_bApplySleep = bApply;
 	}
 
 	public int getEffectRing()
 	{
 		return m_iEffectRing;
 	}
 
 	public void setEffectRing(int iPos)
 	{
 		m_iEffectRing = iPos;
 	}
 
 	public int getEffectCharge()
 	{
 		return m_iEffectCharge;
 	}
 
 	public void setEffectCharge(int iPos)
 	{
 		m_iEffectCharge = iPos;
 	}
 
 	public int getEffectSMS()
 	{
 		return m_iEffectSMS;
 	}
 
 	public void setEffectSMS(int iPos)
 	{
 		m_iEffectSMS = iPos;
 	}
 
 	public int getEffectMail()
 	{
 		return m_iEffectMail;
 	}
 
 	public void setEffectMail(int iPos)
 	{
 		m_iEffectMail = iPos;
 	}
 
 	public int getEffectIM()
 	{
 		return m_iEffectIM;
 	}
 
 	public void setEffectIM(int iPos)
 	{
 		m_iEffectIM = iPos;
 	}
 
 	public int getEffectSleep()
 	{
 		return m_iEffectSleep;
 	}
 
 	public void setEffectSleep(int iPos)
 	{
 		m_iEffectSleep = iPos;
 	}
 	
 	public Preferences(Activity myActivity)
 	{
 		m_mySettings = myActivity.getSharedPreferences(PREFS_NAME, 0);
 		this.init();
 	}
 	
 	public Preferences(Context myContext)
 	{
 		m_mySettings = myContext.getSharedPreferences(PREFS_NAME, 0);
 		this.init();
 	}
 
 	public Preferences(SharedPreferences myPrefs)
 	{
 		m_mySettings = myPrefs;
 		this.init();
 	}
 	
 	/** initialize value holder */
 	private void init()
 	{
 	    int iPollInterval 	= m_mySettings.getInt("pollInterval", 30);
 	    boolean bAutostart 	= m_mySettings.getBoolean("autostart", false);
 	    
 	    setPollInterval(iPollInterval);
 	    setAutostart(bAutostart);
 	    
 	    setNotifyRing(m_mySettings.getBoolean("notifyRing", false));
 	    setNotifyCharge(m_mySettings.getBoolean("notifyCharge", false));
 	    setNotifySMS(m_mySettings.getBoolean("notifySMS", false));
 	    setNotifyMail(m_mySettings.getBoolean("notifyMail", false));
 	    setNotifyIM(m_mySettings.getBoolean("notifyIM", false));
 	    setApplySleep(m_mySettings.getBoolean("applySleep", false));
 	    
 	    setEffectRing(m_mySettings.getInt("effectRing", 0));	    
 	    setEffectCharge(m_mySettings.getInt("effectCharge", 0));
 	    setEffectSMS(m_mySettings.getInt("effectSMS", 0));
 	    setEffectMail(m_mySettings.getInt("effectMail", 0));
 	    setEffectIM(m_mySettings.getInt("effectIM", 0));
 	    setEffectSleep(m_mySettings.getInt("effectSleep", 3));
 	}
 	
 	public void save()
 	{
 		
 		SharedPreferences.Editor editor = m_mySettings.edit();
 		
 	    editor.putInt("pollInterval", getPollInterval());
 	    editor.putBoolean("autostart", getAutostart());
 	    
 	    editor.putBoolean("notifyRing", getNotifyRing());
 	    editor.putBoolean("notifyCharge", getNotifyCharge());
 	    editor.putBoolean("notifySMS", getNotifySMS());
 	    editor.putBoolean("notifyMail", getNotifyMail());
 	    editor.putBoolean("notifyIM", getNotifyIM());
 	    editor.putBoolean("applySleep", getApplySleep());
 	    
 	    editor.putInt("effectRing", getEffectRing());
 	    editor.putInt("effectCharge", getEffectCharge());
 	    editor.putInt("effectSMS", getEffectSMS());
 	    editor.putInt("effectMail", getEffectMail());
 	    editor.putInt("effectIM", getEffectIM());
 	    editor.putInt("effectSleep", getEffectSleep());
 	    
 	    // write to kernel if sleep is to be applied
 	    if (getApplySleep())
 	    {
 	    	EffectsFassade.getInstance().writeSleepEffect(getEffectSleep());
 	    }
	    editor.commit();
 	}
 	
	
 	/**
 	 * @return the pollInterval
 	 */
 	public int getPollInterval() {
 		return m_iPollInterval;
 	}
 	/**
 	 * @param pollInterval the pollInterval to set
 	 */
 	public void setPollInterval(int pollInterval) {
 		m_iPollInterval = pollInterval;
 	}
 
 	public Effect getEffectForState(int iState)
 	{
 		switch (iState)
 		{
 			case EffectsState.STATE_NONE :
 				return new Effect(0, false, "", false);
 
 			case EffectsState.STATE_CHARGING :
 				return new Effect(m_iEffectCharge, false, "Charging battery", getNotifyCharge());
 
 			case EffectsState.STATE_RINGING :
 				return new Effect(m_iEffectRing, false, "Incoming call", getNotifyRing());
 
 			case EffectsState.NOTIFY_SMS :
 				return new Effect(m_iEffectSMS, true, "Incoming SMS", getNotifySMS());
 
 			case EffectsState.NOTIFY_IM :
 				return new Effect(m_iEffectIM, true, "Incoming IM", getNotifyIM());
 
 			case EffectsState.NOTIFY_MAIL :
 				return new Effect(m_iEffectMail, true, "Incoming mail", getNotifyMail());
 				
 			case EffectsState.STATE_SLEEPING :
 				return new Effect(m_iEffectSleep, false, "Sleeping", false);
 
 			default :
 				return new Effect(0, false, "", false);		
 		}
 	}
 }
