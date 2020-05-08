 /*
  * Model.java
  *
  * Created on January 14, 2001, 12:39 AM
  */
  
 package org.jbundle.util.biorhythm;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Date;
 import java.util.StringTokenizer;
 
 import javax.swing.JApplet;
 import javax.swing.JFrame;
 
 import org.jbundle.util.muffinmanager.MuffinManager;
 
 
 /** 
  * Biorhythm - This is main the biorhythm program.
  * Copyright (c) 2009 tourapp.com. All Rights Reserved.
  * Copy freely, but don't sell this program or remove this copyright notice.
  * @author  Don Corley
  * @version 1.0
  */
 public class Biorhythm extends JApplet implements Constants
 {
     private static final long serialVersionUID = 1L;
     
	zprotected String[] m_args = null;
 	
 	protected Controller m_controller = null;
 	
 	protected MuffinManager m_muffinManager = null;
 
 	/**
 	 * Creates new Model.
 	 */
 	public Biorhythm()
 	{
 		super();
 	}
 	/**
 	 * AppCloser quits the application when the user closes the window.
 	 */
 	public static class AppCloser extends WindowAdapter
 	{
 		JFrame m_frame = null;
 		Biorhythm m_biorhythm = null;
 		/**
 		 * Constructor.
 		 */
 		public AppCloser(JFrame frame, Biorhythm biorhythm)
 		{
 			super();
 			m_frame = frame;
 			m_biorhythm = biorhythm;
 		}
 		/**
 		 * Close the window.
 		 */
 		public void windowClosing(WindowEvent e)
 		{
 			m_biorhythm.stop();		// Simulate the applet calls
 			m_biorhythm.destroy();
 			m_frame.dispose();
 			System.exit(0);
 		}
 	}
 	/**
 	 * main entrypoint - starts the part when it is run as an application
 	 * @param args java.lang.String[]
 	 */
 	public static void main(String[] args)
 	{
 		JFrame frame;
 		Biorhythm biorhythm = new Biorhythm();
 		try {
 			frame = new JFrame("Biorhythm");
 			frame.addWindowListener(new AppCloser(frame, biorhythm));
 		} catch (java.lang.Throwable ivjExc) {
 			frame = null;
 			System.out.println(ivjExc.getMessage());
 			ivjExc.printStackTrace();
 		}
 		frame.getContentPane().add(BorderLayout.CENTER, biorhythm);
 		Dimension size = biorhythm.getSize();
 		if ((size == null) || ((size.getHeight() < 100) | (size.getWidth() < 100)))
 			size = new Dimension(640, 400);
 		frame.setSize(size);
 		biorhythm.m_args = args;
 
 		biorhythm.init();		// Simulate the applet calls
 //			frame.setTitle(m_resources.getString("Biorhythm"));
 		biorhythm.start();
 
 //xaBiotest.print();
 
 		frame.setVisible(true);
 
 //x		biorhythm.destroy();
 	}
 	/**
 	 * Initialize this applet.
 	 */
 	public void init()
 	{
         try {
             Class.forName("javax.jnlp.PersistenceService");   // Test if this exists
             MuffinManager muffinManager = new MuffinManager(this);
             this.setMuffinManager(muffinManager);
         } catch (Exception ex)  {
         }
 
         View view = new LegendView(null);
 		Model model = new Model(view);
 		m_controller = new LegendController(model, null, null, null);
 		view.setController(m_controller);
 		this.getContentPane().add(BorderLayout.NORTH, m_controller);
 		this.getContentPane().add(BorderLayout.CENTER, view);
 		this.getContentPane().setBackground(view.getElementColor(BACKGROUND));
 		this.addParams();
 	}
 	/**
 	 * Called just before exiting.
 	 */
 	public void destroy()
 	{
 		if (m_muffinManager != null)
 		{
 			Date dateBirthdate = m_controller.getBirthdate();
 			if (dateBirthdate != null)
 			{
 				long lTimeChange = new Date().getTime() - dateBirthdate.getTime();
 				if (lTimeChange > ONE_DAY_MS)
 				{		// Save the birthdate in a muffin if it isn't still set to today.
 					String strBirthdate = this.getBirthdate();
 					m_muffinManager.setMuffin(BIRTHDATE_PARAM, strBirthdate);
 				}
 			}
 		}
 		super.destroy();
 	}
 	/**
 	 * Handle the Applet init method.
 	 */
 	public void addParams()
 	{
 		String param = this.GetParameter(BIRTHDATE_PARAM);
 		if ((param == null) || (param.length() == 0))
 			if (m_muffinManager != null)
 				param = m_muffinManager.getMuffin(BIRTHDATE_PARAM);
 
 		if ((param != null) && (param.length() > 0))
 			this.setBirthdate(param);
 
 		param = this.GetParameter(STARTDATE_PARAM);
 		if ((param != null) && (param.length() > 0))
 			this.setStartDate(param);
 
 		param = this.GetParameter(ENDDATE_PARAM);
 		if ((param != null) && (param.length() > 0))
 			this.setEndDate(param);
 
 		param = this.GetParameter(LANGUAGE_PARAM);
 		if ((param != null) && (param.length() > 0))
 			this.setLanguage(param);
 	}
 	/**
 	 * This method was created by a SmartGuide.
 	 * @return java.lang.String
 	 * @param strParam java.lang.String
 	 */
 	public String GetParameter (String strParam )
 	{
 		if (m_args == null)
 		{
 			try	{
 				return this.getParameter(strParam);		// For applets, get the applet param
 			} catch (NullPointerException ex)	{
 				return null;	// Ignore this - possibly this is not an applet, but it was started from another standalone app
 			}
 		}
 		// For stand-alone apps, get the passed in param
 		for (int i = 0; i < m_args.length; i++)
 		{
 			StringTokenizer st = new StringTokenizer(m_args[i], TOKENS);
 			if (st.hasMoreTokens()) if (st.nextToken().equalsIgnoreCase(strParam))
 				if (st.hasMoreTokens())
 					return (st.nextToken());
 		}
 		return null;
 	}
 	/**
 	 * Method to handle events for the PropertyChangeListener interface.
 	 * @param evt java.beans.PropertyChangeEvent
 	 */
 	public void propertyChange(java.beans.PropertyChangeEvent evt)
 	{
 		// user code begin {1}
 		// user code end
 		if (evt.getPropertyName().equals(BIRTHDATE_PARAM)) {
 			this.setBirthdate((String)evt.getNewValue());
 		}
 		if (evt.getPropertyName().equals(STARTDATE_PARAM)) {
 			this.setStartDate((String)evt.getNewValue());
 		}
 		if (evt.getPropertyName().equals(ENDDATE_PARAM)) {
 			this.setEndDate((String)evt.getNewValue());
 		}
 		if (evt.getPropertyName().equals(LANGUAGE_PARAM)) {
 			this.setLanguage((String)evt.getNewValue());
 		}
 		// user code begin {2}
 		// user code end
 	}
 	/**
 	 * Set the birthdate.
 	 * @param strDate the birthdate stirng in standard form.
 	 */
 	public void setBirthdate(String strDate)
 	{
 		((LegendController)m_controller).setTextBirthdate(strDate);
 	}
 	/**
 	 * Get the birthdate.
 	 */
 	public String getBirthdate()
 	{
 		return LegendController.m_dtf.format(m_controller.getBirthdate());
 	}
 	/**
 	 * Get the start date.
 	 */
 	public String getStartDate()
 	{
 		return LegendController.m_df.format(m_controller.getStartDate());
 	}
 	/**
 	 * Set the start date.
 	 */
 	public void setStartDate(String strDate)
 	{
 		((LegendController)m_controller).setTextStartDate(strDate);
 	}
 	/**
 	 * Get the end date.
 	 */
 	public String getEndDate()
 	{
 		return LegendController.m_df.format(m_controller.getEndDate());
 	}
 	/**
 	 * Set the end date.
 	 */
 	public void setEndDate(String strDate)
 	{
 		((LegendController)m_controller).setTextEndDate(strDate);
 	}
 	/**
 	 * Get the language.
 	 */
 	public String getLanguage()
 	{
 		return "";
 	}
 	/**
 	 * Set the language.
 	 */
 	public void setLanguage(String strLanguage)
 	{
 		((LegendController)m_controller).setLanguage(strLanguage);
 	}
 	/**
 	 * Set the muffin manager.
 	 * @param muffinManager The muffin manager.
 	 */
 	public void setMuffinManager(MuffinManager muffinManager)
 	{
 		m_muffinManager = muffinManager;
 	}
 }
