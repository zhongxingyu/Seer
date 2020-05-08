 /*******************************************************************************
  * Copyright (c) 2007 by CRP Henri TUDOR - SANTEC LUXEMBOURG 
  * check http://www.santec.tudor.lu for more information
  *  
  * Contributor(s):
  * Johannes Hermen  johannes.hermen(at)tudor.lu                            
  * Martin Heinemann martin.heinemann(at)tudor.lu
  * Thorsten Roth thorsten.roth(at)tudor.lu  
  *  
  * This library is free software; you can redistribute it and/or modify it  
  * under the terms of the GNU Lesser General Public License (version 2.1)
  * as published by the Free Software Foundation.
  * 
  * This software is distributed in the hope that it will be useful, but     
  * WITHOUT ANY WARRANTY; without even the implied warranty of               
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        
  * Lesser General Public License for more details.                          
  * 
  * You should have received a copy of the GNU Lesser General Public         
  * License along with this library; if not, write to the Free Software      
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *******************************************************************************/
 package lu.tudor.santec.bizcal.views;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JToggleButton;
 
 import lu.tudor.santec.bizcal.AbstractCalendarView;
 import lu.tudor.santec.bizcal.CalendarIcons;
 import lu.tudor.santec.bizcal.EventModel;
 import lu.tudor.santec.bizcal.NamedCalendar;
 import lu.tudor.santec.bizcal.print.PrintUtilities;
 import lu.tudor.santec.i18n.Translatrix;
 
 import bizcal.common.DayViewConfig;
 import bizcal.common.Event;
 import bizcal.swing.CalendarListener;
 import bizcal.swing.CalendarView;
 import bizcal.swing.DayView;
 import bizcal.swing.DayView.Layout;
 import bizcal.util.Interval;
 
 /**
  * @author martin.heinemann@tudor.lu
  * 19.06.2008
  * 14:49:51
  *
  *
  * @version
  * <br>$Log: DayViewPanel.java,v $
  * <br>Revision 1.8  2011/03/04 12:45:35  thorstenroth
  * <br>1. Improvement of the mouse controls when event gets resize and move in the calendar.
  * <br>2. Bug Fix: The position of the current timeline is now correct and only shown ar the current day.
  * <br>3. Bug Fix: Because of the bug the view can not difference between Events form different calendars which have the same start and end time so sometimes by resize or move a event there are side effects when drawing the events.
  * <br>
  * <br>Revision 1.7  2011/02/22 14:59:32  thorstenroth
  * <br>1. Add a new layout for the day view. This layout split the day column into a number of lines which is equal to the number of calendars which are active. The events of one calendar are now shown in one line, one below the other.
  * <br>
  * <br>2. Add a new horizontal line to the day view to represent the current time.
  * <br>
  * <br>Revision 1.6  2011/02/11 07:22:07  thorstenroth
  * <br>Add a new view to the calendar the 'Three Day View' which shows three days per interval.
  * <br>
  * <br>Revision 1.5  2009/05/11 16:45:44  heine_
  * <br>added a listener for the zoom slider
  * <br>
  * <br>Revision 1.4  2008/08/12 12:47:28  heine_
  * <br>fixed some bugs and made code improvements
  * <br>
  * <br>Revision 1.3  2008/06/24 12:53:25  heine_
  * <br>*** empty log message ***
  * <br>
  *   
  */
 public class DayViewPanel extends AbstractCalendarView {
 
 	private static final long serialVersionUID = 1L;
 
 	private final static int STATE_FULL 		= 1;
 	private final static int STATE_MORNING 		= 2;
 	private final static int STATE_AFTERNOON 	= 3;
 
 	private int state = STATE_FULL;
 
 	private JToggleButton button;
 	
 	private EventModel dayModel;
 	
 	private DayView dayView;
 	
 	public static final String VIEW_NAME_DAY 		= "DAY_VIEW";
 	public static final String VIEW_NAME_WEEK 		= "WEEK_VIEW";
 	public static final String VIEW_NAME_THREE_DAY	= "DAY_THREE_VIEW";
 	
 	public String VIEW_NAME;
 	
 	private JButton switcherButton;
 
 	private ImageIcon fullDayIcon;
 
 	private ImageIcon morningDayIcon;
 
 	private ImageIcon afternoonDayIcon;
 
 	private DayViewConfig dayViewConfig;
 
 
 	/**
 	 * @param model
 	 */
 	public DayViewPanel(EventModel model) {
 		/* ================================================== */
 		this(model, new DayViewConfig(), Layout.DAY_COLUMN_NORMAL);
 		/* ================================================== */
 	}
 	
 	/**
 	 * @param model
 	 * @param config
 	 */
 	public DayViewPanel(EventModel model, DayViewConfig config)
 	{
 		this(model, config, Layout.DAY_COLUMN_NORMAL);
 	}
 	
 	public DayViewPanel(EventModel model, Integer layoutMode) {
 		/* ================================================== */
 		this(model, new DayViewConfig(), layoutMode);
 		/* ================================================== */
 	}
 
 	public DayViewPanel(EventModel model, DayViewConfig config, Integer layoutMode) {
 		/* ================================================== */
 
 		this.dayModel = model;
 		this.dayViewConfig = config;
 		
 		
 		this.setLayout(new BorderLayout());
 		
 		if (dayModel.getType() == EventModel.TYPE_DAY) {
 			/* ------------------------------------------------------- */
 			VIEW_NAME = VIEW_NAME_DAY;
 			this.button = new JToggleButton(
 					CalendarIcons.getMediumIcon(CalendarIcons.DAYVIEW));
 			this.button.setToolTipText(Translatrix.getTranslationString("bizcal.DAY_VIEW"));
 			model.setDate(new Date());
 			/* ------------------------------------------------------- */
 		}else if (dayModel.getType() == EventModel.TYPE_WEEK){
 			/* ------------------------------------------------------- */
 			VIEW_NAME = VIEW_NAME_WEEK;
 			this.button = new JToggleButton(
 					CalendarIcons.getMediumIcon(CalendarIcons.WEEKVIEW));
 			this.button.setToolTipText(Translatrix.getTranslationString("bizcal.WEEK_VIEW"));
 			
 			// set the weekday start/stop to the model
 			this.dayModel.setWeekdayStartEnd(dayViewConfig.getWeekStart(), dayViewConfig.getWeekEnd());
 			/* ------------------------------------------------------- */
 		}else if (dayModel.getType() == EventModel.TYPE_THREE_DAY){
 			/* ------------------------------------------------------- */
 			VIEW_NAME = VIEW_NAME_THREE_DAY;
 			this.button = new JToggleButton(
 				CalendarIcons.getMediumIcon(CalendarIcons.THREEDAYVIEW));
 			this.button.setToolTipText(Translatrix.getTranslationString("bizcal.THREE_DAY_VIEW"));
 		
 			// set the weekday start/stop to the model
 			Calendar c = new GregorianCalendar();
 			int start = c.get(Calendar.DAY_OF_WEEK);
 			c.add(Calendar.DAY_OF_WEEK, 2); // set the week + 2 day
 			int end = c.get(Calendar.DAY_OF_WEEK);
 			this.dayModel.setWeekdayStartEnd(start, end);
 			/* ------------------------------------------------------- */
 		}
 		try {
 			/* ------------------------------------------------------- */
 			
 			initDayViewSwitcherButton();
 
 			dayView = new DayView(this.dayViewConfig, this.switcherButton, layoutMode);
 			dayView.setModel(dayModel);
 			dayModel.addCalendarView(dayView);
 
 			dayView.refresh();
 //			dayView.refresh0();
 			this.add(dayView.getComponent());
 
 			updateSwitcherButton();
 			/* ------------------------------------------------------- */
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		/* ================================================== */
 	}
 
 	
 	/**
 	 * Updates the settings from the config file
 	 */
 	public void refresh() {
		/* ================================================== */
		if (VIEW_NAME_WEEK.equals(VIEW_NAME)) {
			/* ------------------------------------------------------- */
			// set the weekday start/stop to the model
			this.dayModel.setWeekdayStartEnd(dayViewConfig.getWeekStart(), dayViewConfig.getWeekEnd());
			/* ------------------------------------------------------- */
			//TODO damit in der drei tages ansicht die wochenende aus geblendet werden oder ???
		}
 		
 		try {
 //			dayView.refresh();
 			updateSwitcherButton();
 			refreshGrid();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
		/* ================================================== */
 	}
 	
 	
 	/**
 	 * 
 	 */
 	private void refreshGrid() {
 		/* ================================================== */
 		try {
 			this.dayView.resetHorizontalLines();
 			this.dayView.refresh();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		/* ================================================== */
 	}
 	
 	
 	/**
 	 *
 	 */
 	private void initDayViewSwitcherButton() {
 		/* ====================================================== */
 		this.switcherButton  = new JButton(CalendarIcons.getMediumIcon(CalendarIcons.DELETE));
 		this.switcherButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				/* ====================================================== */
 				switchState();
 				/* ------------------------------------------------------- */
 				// update buttons
 				updateSwitcherButton();
 				/* ====================================================== */
 			}
 
 		});
 
 
 		this.switcherButton.setBorderPainted(false);
 		this.switcherButton.setContentAreaFilled(false);
 		this.switcherButton.setFocusPainted(false);
 
 		this.fullDayIcon 	  = CalendarIcons.getMediumIcon(CalendarIcons.DAY_FULL);
 		this.morningDayIcon   = CalendarIcons.getMediumIcon(CalendarIcons.DAY_MORNING);
 		this.afternoonDayIcon = CalendarIcons.getMediumIcon(CalendarIcons.DAY_AFTERNOON);
 
 		/* ====================================================== */
 	}
 
 
 	/**
 	 * Update the button in the upper left corner to switch the day view
 	 */
 	private void updateSwitcherButton() {
 		/* ================================================== */
 		if (STATE_FULL == this.state) {
 			/* ------------------------------------------------------- */
 			this.switcherButton.setIcon(this.fullDayIcon);
 			this.switcherButton.setToolTipText(Translatrix.getTranslationString("bizcal.switcher.morning"));
 
 
 			this.dayViewConfig.setDayStartHour(this.dayViewConfig.getDefaultDayStartHour());
 			this.dayViewConfig.setDayEndHour(this.dayViewConfig.getDefaultDayEndHour());
 			/* ------------------------------------------------------- */
 		} else
 			if (STATE_MORNING == this.state) {
 				/* ------------------------------------------------------- */
 				this.switcherButton.setIcon(morningDayIcon);
 				this.switcherButton.setToolTipText(Translatrix.getTranslationString("bizcal.switcher.afternoon"));
 
 				this.dayViewConfig.setDayStartHour(this.dayViewConfig.getDefaultDayStartHour());
 				this.dayViewConfig.setDayEndHour(dayViewConfig.getDayBreak());
 				/* ------------------------------------------------------- */
 			}
 			else
 				if (STATE_AFTERNOON == this.state) {
 					/* ------------------------------------------------------- */
 					this.switcherButton.setIcon(afternoonDayIcon);
 					this.switcherButton.setToolTipText(Translatrix.getTranslationString("bizcal.switcher.full"));
 
 					this.dayViewConfig.setDayStartHour(dayViewConfig.getDayBreak());
 					this.dayViewConfig.setDayEndHour(this.dayViewConfig.getDefaultDayEndHour());
 					/* ------------------------------------------------------- */
 				}
 		try {
 			dayView.refresh();
 			refreshGrid();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		/* ================================================== */
 	}
 
 
 
 	public void addCalendarListener(CalendarListener listener) {
 		/* ================================================== */
 		dayView.addListener(listener);
 		/* ================================================== */
 	}
 
 
 	public JToggleButton getButton() {
 		return this.button;
 	}
 
 	public String getViewName() {
 		return VIEW_NAME;
 	}
 
 	public void dateChanged(Date date) {
 		dayModel.setDate(date);
 		try {
 			dayView.refresh();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	public void moved(Event event,
 					  Object orgCalId,
 					  Date orgDate,
 					  Object newCalId,
 					  Date newDate) throws Exception {
 		/* ====================================================== */
 		event.move(newDate);
 		dayModel.triggerUpdate();
 		/* ====================================================== */
 	}
 
 	public void resized(Event event, Object orgCalId, Date orgEndDate, Date newEndDate) throws Exception {
 		/* ====================================================== */
 		event.setEnd(newEndDate);
 		// notify all models that uses the same ObservableEventList
 		dayModel.triggerUpdate();
 		/* ====================================================== */
 	}
 
 	public void activeCalendarsChanged(Collection<NamedCalendar> calendars) {
 		/* ====================================================== */
 		// TODO activeCalendars
 		dayView.setActiveCalendars(calendars);
 		/* ====================================================== */
 	}
 
 	public void selectedCalendarChanged(NamedCalendar selectedCalendar) {
 		/* ====================================================== */
 		// TODO selectedCalendars
 		dayView.setSelectedCalendar(selectedCalendar);
 		//dayView.setSelectedCalendar(selectedCalendar);
 		/* ====================================================== */
 	}
 
 	@Override
 	public List<Event> getEvents() {
 		/* ================================================== */
 		try {
 			Interval interval = this.dayModel.getInterval();
 			/* ------------------------------------------------------- */
 			Date start 	= (Date) interval.getStart();
 			Date end 	= (Date) interval.getEnd();
 			/* ------------------------------------------------------- */
 			List<Event> evs = dayModel.getEvents(null);
 			List<Event> shownEvents = new ArrayList<Event>();
 			/* ------------------------------------------------------- */
 			if (evs != null)
 				for (Event e : evs) {
 					/* ------------------------------------------------------- */
 					if (e.getStart().after(start)
 							&& e.getStart().before(end))
 						shownEvents.add(e);
 					/* ------------------------------------------------------- */
 				}
 			return shownEvents;
 		} catch (Exception e) {
 			return null;
 		}
 		/* ================================================== */
 	}
 
 	public synchronized void setZoomFactor(int zoom) {
 		/* ================================================== */
 		DayView.PIXELS_PER_HOUR = zoom;
 		refresh();
 		/* ================================================== */
 	}
 
 
 	/**
 	 * Defines the order of the day view switching.
 	 * 1. Full
 	 * 2. Morning
 	 * 3. Afternoon
 	 *
 	 * @param state
 	 */
 	private void switchState() {
 		/* ================================================== */
 		if (STATE_FULL == state)
 			this.state = STATE_MORNING;
 		else
 			if (STATE_MORNING == state)
 				this.state = STATE_AFTERNOON;
 			else
 				if (STATE_AFTERNOON == state)
 					this.state = STATE_FULL;
 				else
 					this.state = STATE_AFTERNOON;
 		/* ================================================== */
 	}
 
 	@Override
 	public void print(boolean showPrinterDialog) {
 		int curZoom = DayView.PIXELS_PER_HOUR;
 		setZoomFactor(30);
 		if (dayModel.getType() == EventModel.TYPE_DAY)
 			PrintUtilities.printComponent(this, showPrinterDialog, false);
 		else
 			PrintUtilities.printComponent(this, showPrinterDialog, true);
 		setZoomFactor(curZoom);
 	}
 
 	@Override
 	public CalendarView getView() {
 		/* ====================================================== */
 		return this.dayView;
 		/* ====================================================== */
 	}
 }
