 package ted.ui.editshowdialog;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 
 import ted.Lang;
 
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class DatePanel extends JPanel 
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5496935878161614311L;
 	private JComboBox jFromBreakDay;
 	private JComboBox jFromBreakMonth;
 	private JComboBox jFromBreakYear;
 	
 	private String[] months = {Lang.getString("TedEpisodeDialog.MonthJan"), Lang.getString("TedEpisodeDialog.MonthFeb"), Lang.getString("TedEpisodeDialog.MonthMar"), Lang.getString("TedEpisodeDialog.MonthApr"), Lang.getString("TedEpisodeDialog.MonthMay"), Lang.getString("TedEpisodeDialog.MonthJun"), Lang.getString("TedEpisodeDialog.MonthJul"), Lang.getString("TedEpisodeDialog.MonthAug"), Lang.getString("TedEpisodeDialog.MonthSep"), Lang.getString("TedEpisodeDialog.MonthOct"), Lang.getString("TedEpisodeDialog.MonthNov"), Lang.getString("TedEpisodeDialog.MonthDec")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
 	private String[] days  = initString(1, 31);
 	private String[] years;
 	
 	private int yearOffset;
 	
 	public DatePanel()
 	{
		
 		this.initGUI();
 	}
 
 	/**
 	 * Initialize a array with strings
 	 * @param low first value of the array
 	 * @param high last value of the array
 	 * @return A array filled with entries between low and high
 	 */
 	private String[] initString(int low, int high) 
 	{
 		String [] strings = new String [high-low+1];
 		
 		for (int i = 0; i+low <= high; i++)
 		{
 			strings[i] = ""+(low+i); //$NON-NLS-1$
 		}
 		
 		return strings;
 	}
 
 	private void initGUI()
 	{
 		try {
 			{
 				Calendar c = new GregorianCalendar();
 			
 				int year2 = c.get(Calendar.YEAR);
 				yearOffset = year2;
 				years = initString(year2, year2+5);
 				
 				//this.setPreferredSize(new java.awt.Dimension(363, 40));
 				{
 					ComboBoxModel jFromBreakDayModel = new DefaultComboBoxModel(
 							days);
 					jFromBreakDay = new JComboBox();
 					this.add(jFromBreakDay);
 					jFromBreakDay.setModel(jFromBreakDayModel);
 					jFromBreakDay.setBounds(223, 254, 63, 28);
 				}
 				{
 					ComboBoxModel jFromBreakMonthModel = new DefaultComboBoxModel(
 							months);
 					jFromBreakMonth = new JComboBox();
 					this.add(jFromBreakMonth);
 					jFromBreakMonth.setModel(jFromBreakMonthModel);
 					jFromBreakMonth.setBounds(294, 254, 63, 28);
 				}
 				{
 					ComboBoxModel jFromBreakYearModel = new DefaultComboBoxModel(
 							years);
 					jFromBreakYear = new JComboBox();
 					this.add(jFromBreakYear);
 					jFromBreakYear.setModel(jFromBreakYearModel);
 					jFromBreakYear.setBounds(365, 254, 63, 28);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Set the date to display in the date panel
 	 * @param date
 	 */
 	public void setDate(long date) 
 	{
 		Calendar c = new GregorianCalendar();
 		Calendar c2 = new GregorianCalendar();
 		c.setTimeInMillis(date);
 		
 		// update years in datepanel according to date to display
 		int year2 = c2.get(Calendar.YEAR);
		yearOffset = year2;
		years = initString(year2, year2+5);
 		ComboBoxModel jFromBreakYearModel = new DefaultComboBoxModel(
 				years);
 		jFromBreakYear.setModel(jFromBreakYearModel);
 		
 		jFromBreakDay.setSelectedIndex(c.get(Calendar.DAY_OF_MONTH)-1);
 		jFromBreakMonth.setSelectedIndex(c.get(Calendar.MONTH));
 		jFromBreakYear.setSelectedIndex(c.get(Calendar.YEAR) - yearOffset);
 		
 	}
 
 	/**
 	 * @return the selected day
 	 */
 	public int getDay() 
 	{
 		return this.jFromBreakDay.getSelectedIndex()+1;
 	}
 
 	/**
 	 * @return the selected month
 	 */
 	public int getMonth() 
 	{
 		return this.jFromBreakMonth.getSelectedIndex();
 	}
 
 	/**
 	 * @return the selected year
 	 */
 	public int getYear() 
 	{
 		return this.jFromBreakYear.getSelectedIndex() + yearOffset;
 	}
 
 	/**
 	 * @return Selected date in millis
 	 */
 	public long getDateInMillis() 
 	{
 		Calendar c = new GregorianCalendar();
 		int fday   = this.getDay();
 		int fmonth = this.getMonth();
 		int fyear  = this.getYear();
 		
 		c.set(fyear, fmonth, fday);
 		
 		return c.getTimeInMillis();
 	}
 
 	public void setDate(Calendar c) 
 	{
 		this.setDate(c.getTimeInMillis());
 		
 	}
 	
 	public void setEnabledContents(Boolean b)
 	{
 		this.jFromBreakDay.setEnabled(b);
 		this.jFromBreakMonth.setEnabled(b);
 		this.jFromBreakYear.setEnabled(b);
 	}
 
 }
