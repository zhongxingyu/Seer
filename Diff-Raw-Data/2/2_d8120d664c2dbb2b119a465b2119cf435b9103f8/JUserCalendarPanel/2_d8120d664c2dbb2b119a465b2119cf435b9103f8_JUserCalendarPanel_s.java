 package com.hcalendar.ui.widgets.impl;
 
 /*
  * All rights reserved. Software written by Ian F. Darwin and others.
  * $Id: LICENSE,v 1.8 2004/02/09 03:33:38 ian Exp $
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * 
  * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
  * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
  * pioneering role in inventing and promulgating (and standardizing) the Java 
  * language and environment is gratefully acknowledged.
  * 
  * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
  * inventing predecessor languages C and C++ is also gratefully acknowledged.
  */
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import com.hcalendar.data.IDateEntity;
 import com.hcalendar.data.utils.DateHelper;
 import com.hcalendar.ui.ICalendarEventListener;
 import com.hcalendar.ui.widgets.ICalendarActionProvider;
 import com.hcalendar.ui.widgets.IWindowResultListener;
 
 /**
  * Calendar panel
  * */
 @SuppressWarnings("deprecation")
 public class JUserCalendarPanel extends JPanel implements
 		ICalendarActionProvider {
 
 	private static final long serialVersionUID = 1L;
 
 	List<ICalendarEventListener> registeredEventListeners = new ArrayList<ICalendarEventListener>();
 
 	List<Date> calendarFreeDays = new ArrayList<Date>();
 	List<Date> userHolidays = new ArrayList<Date>();
 	List<Date> userNotWorkingDays = new ArrayList<Date>();
 	List<Date> userWorkingDays = new ArrayList<Date>();
 
 	/** The currently-interesting year (not modulo 1900!) */
 	protected int yy;
 
 	/** Currently-interesting month and day */
 	protected int mm, dd;
 
 	/** The buttons to be displayed */
 	protected JButton labs[][];
 
 	/** The number of day squares to leave blank at the start of this month */
 	protected int leadGap = 0;
 
 	/** A Calendar object used throughout */
 	Calendar calendar = new GregorianCalendar();
 
 	/** Today's year */
 	protected final int thisYear = calendar.get(Calendar.YEAR);
 
 	/** Today's month */
 	protected final int thisMonth = calendar.get(Calendar.MONTH);
 
 	/** One of the buttons. We just keep its reference for getBackground(). */
 	private JButton b0;
 
 	/** The month choice */
 	private JComboBox monthChoice;
 
 	/** The year choice */
 	private JComboBox yearChoice;
 
 	private int activeDay = -1;
 
 	/**
 	 * Permite habilitar las acciones de botn derecho: Aadir festivo,
 	 * vacaciones, cambiar imputaciones...
 	 * */
 	private boolean enableRightClickActions;
 	private boolean allowYearChange;
 
 	/**
 	 * Construct a Cal, starting with today.
 	 */
 	public JUserCalendarPanel(ICalendarEventListener eventListener,
 			boolean enableRightClickActions, boolean allowYearChange) {
 		super();
 		this.allowYearChange = allowYearChange;
 		this.enableRightClickActions = enableRightClickActions;
 		addEventListener(eventListener);
 		setYYMMDD(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
 				calendar.get(Calendar.DAY_OF_MONTH));
 		buildGUI();
 		recompute();
 	}
 
 	/**
 	 * Construct a Cal, given the leading days and the total days
 	 * 
 	 * @exception IllegalArgumentException
 	 *                If year out of range
 	 */
 	public JUserCalendarPanel(int year, int month, int today,
 			ICalendarEventListener eventListener,
 			boolean enableRightClickActions, boolean allowYearChange) {
 		super();
 		this.allowYearChange = allowYearChange;
 		if (month == 0)
 			month = Calendar.MONTH;
 		if (today == 0)
 			today = Calendar.DAY_OF_MONTH;
 		this.enableRightClickActions = enableRightClickActions;
 		addEventListener(eventListener);
 		setYYMMDD(year, month, today);
 		buildGUI();
 		recompute();
 	}
 
 	private void setYYMMDD(int year, int month, int today) {
 		yy = year;
 		mm = month;
 		dd = today;
 	}
 
 	/** Build the GUI. Assumes that setYYMMDD has been called. */
 	private void buildGUI() {
 		getAccessibleContext().setAccessibleDescription(
 				"Calendar not accessible yet. Sorry!");
 		setBorder(BorderFactory.createEtchedBorder());
 
 		setLayout(new BorderLayout());
 
 		JPanel tp = new JPanel();
 		tp.add(monthChoice = new JComboBox());
 		for (int i = 0; i < DateHelper.months.length; i++)
 			monthChoice.addItem(DateHelper.months[i]);
 		monthChoice.setSelectedItem(DateHelper.months[mm]);
 		monthChoice.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				int i = monthChoice.getSelectedIndex();
 				if (i >= 0) {
 					mm = i;
 					// System.out.println("Month=" + mm);
 					recompute();
 				}
 			}
 		});
 		monthChoice.getAccessibleContext().setAccessibleName("Months");
 		monthChoice.getAccessibleContext().setAccessibleDescription(
 				"Selecciona un mes");
 
 		tp.add(yearChoice = new JComboBox());
 		// Allow to change year?
 		yearChoice.setEnabled(allowYearChange);
 		for (int i = yy - 5; i < yy + 5; i++)
 			yearChoice.addItem(Integer.toString(i));
 		yearChoice.setSelectedItem(Integer.toString(yy));
 		yearChoice.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				int i = yearChoice.getSelectedIndex();
 				if (i >= 0) {
 					yy = Integer.parseInt(yearChoice.getSelectedItem()
 							.toString());
 					// System.out.println("Year=" + yy);
 					recompute();
 				}
 			}
 		});
 		add(BorderLayout.CENTER, tp);
 
 		JPanel bp = new JPanel();
 		bp.setLayout(new GridLayout(7, 7));
 		labs = new JButton[6][7]; // first row is days
 
 		bp.add(b0 = new JButton("DO"));
 		bp.add(new JButton("LU"));
 		bp.add(new JButton("MA"));
 		bp.add(new JButton("MI"));
 		bp.add(new JButton("JU"));
 		bp.add(new JButton("VI"));
 		bp.add(new JButton("SA"));
 
 		ActionListener dateSetter = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String num = e.getActionCommand();
 
 				if (!num.equals("")) {
 					setDayActive(Integer.parseInt(num));
 					// Notify listeners
 					for (ICalendarEventListener l : registeredEventListeners) {
 						l.onDateChanged(new Date(
 								JUserCalendarPanel.this.yy - 1900,
 								JUserCalendarPanel.this.mm, dd),
 								isDaySelected(dd), JUserCalendarPanel.this);
 					}
 				}
 			}
 
 			private Boolean isDaySelected(int day) {
 				JButton b;
 				b = labs[(leadGap + day - 1) / 7][(leadGap + day - 1) % 7];
 				return b.getBackground().getRGB() != b0.getBackground()
 						.getRGB();
 			}
 		};
 
 		// Construct all the buttons, and add them.
 		for (int i = 0; i < 6; i++)
 			for (int j = 0; j < 7; j++) {
 				labs[i][j] = new JButton("");
 				if (enableRightClickActions) {
 					labs[i][j].addMouseListener(new MouseListener() {
 
 						@Override
 						public void mouseReleased(MouseEvent e) {
 							int buttonNumber = e.getButton();
 							if (e.isPopupTrigger()
 									&& MouseEvent.BUTTON3 == buttonNumber)
 								doPop(e);
 						}
 
 						@Override
 						public void mousePressed(MouseEvent e) {
 
 						}
 
 						@Override
 						public void mouseExited(MouseEvent e) {
 
 						}
 
 						@Override
 						public void mouseEntered(MouseEvent e) {
 
 						}
 
 						@Override
 						public void mouseClicked(MouseEvent e) {
 
 						}
 
 						private void doPop(MouseEvent e) {
 							JButton source = (JButton) e.getSource();
 							int day = Integer.parseInt(source.getText());
 							Date date = new Date(yy - 1900, mm, day);
 							//	Check if the date is a working day
 							boolean isWorkingDay = false;
 							if (!(JUserCalendarPanel.this.userHolidays.contains(date) || JUserCalendarPanel.this.userNotWorkingDays.contains(date)|| JUserCalendarPanel.this.calendarFreeDays.contains(date)))
 								isWorkingDay = true;
 							InputChangeOptionMenu menu = new InputChangeOptionMenu(
 									new IWindowResultListener() {
 
 										@Override
 										public void windowResult(
 												IDateEntity entity) {
 											for (ICalendarEventListener l : registeredEventListeners)
 												l.onDataInput(entity);
 										}
 									}, date, isWorkingDay);
 							menu.show(e.getComponent(), e.getX(), e.getY());
 							
 						}
 
 					});
 				}
 				bp.add(labs[i][j]);
 				labs[i][j].addActionListener(dateSetter);
 			}
 
 		add(BorderLayout.SOUTH, bp);
 	}
 
 	/** Compute which days to put where, in the Cal panel */
 	private void recompute() {
 		// System.out.println("Cal::recompute: " + yy + ":" + mm + ":" + dd);
 		if (mm < 0 || mm > 11)
 			throw new IllegalArgumentException("Month " + mm
 					+ " bad, must be 0-11");
 		// clearDayActive(dd);
 		clearAllDays();
 		calendar = new GregorianCalendar(yy, mm, dd);
 
 		// Compute how much to leave before the first.
 		// getDay() returns 0 for Sunday, which is just right.
 		leadGap = new GregorianCalendar(yy, mm, 1).get(Calendar.DAY_OF_WEEK) - 1;
 		// System.out.println("leadGap = " + leadGap);
 
 		int daysInMonth = DateHelper.daysOnMonth[mm];
 		if (DateHelper.isLeap(calendar.get(Calendar.YEAR)) && mm == 1)
 			// if (isLeap(calendar.get(Calendar.YEAR)) && mm > 1)
 			++daysInMonth;
 
 		// Blank out the labels before 1st day of month
 		for (int i = 0; i < leadGap; i++) {
 			labs[0][i].setText("");
 		}
 
 		// Fill in numbers for the day of month.
 		for (int i = 1; i <= daysInMonth; i++) {
 			JButton b = labs[(leadGap + i - 1) / 7][(leadGap + i - 1) % 7];
 			b.setText(Integer.toString(i));
 			// Mirar si tiene que estar pintado o no
 			if (isDayUserNotWorkingDay(yy, mm, i))
 				b.setBackground(Color.red);
 			if (isDayCalendarFreeDay(yy, mm, i))
 				b.setBackground(Color.green);
 			if (isDayUserHoliday(yy, mm, i))
 				b.setBackground(Color.blue);
 			if (isDayUserWorkingDay(yy, mm, i))
 				b.setBackground(b0.getBackground());
 		}
 
 		// 7 days/week * up to 6 rows
		for (int i = leadGap + 1 + daysInMonth; i < 6 * 7; i++) {
 			labs[(i) / 7][(i) % 7].setText("");
 		}
 
 		if (false) {
 			// No colorear ningn dia por defecto
 			// Shade current day, only if current month
 			if (thisYear == yy && mm == thisMonth)
 				setDayActive(dd, Color.red); // shade the box for today
 		}
 		// Say we need to be drawn on the screen
 		repaint();
 	}
 
 	/** Set the year, month, and day */
 	public void setDate(int yy, int mm, int dd) {
 		// System.out.println("Cal::setDate");
 		this.yy = yy;
 		this.mm = mm; // starts at 0, like Date
 		this.dd = dd;
 		yearChoice.setSelectedItem(Integer.toString(this.yy));
 		monthChoice.setSelectedItem(DateHelper.months[mm]);
 		recompute();
 	}
 
 	/**
 	 * Set just the day, on the current month
 	 * 
 	 * @param color
 	 */
 	public void setDayActive(int newDay, Color color) {
 		// Set the new one
 		if (newDay <= 0)
 			dd = new GregorianCalendar().get(Calendar.DAY_OF_MONTH);
 		else
 			dd = newDay;
 		if (!clearDayActive(newDay)) {
 			// Now shade the correct square
 			Component square = labs[(leadGap + newDay - 1) / 7][(leadGap
 					+ newDay - 1) % 7];
 			square.setBackground(color);
 			square.repaint();
 			// Anyadir a la lista
 			// addDayToList(newDay, true);
 		}
 		activeDay = newDay;
 	}
 
 	public void setDayActive(int newDay) {
 		// Set the new one
 		if (newDay <= 0)
 			dd = new GregorianCalendar().get(Calendar.DAY_OF_MONTH);
 		else
 			dd = newDay;
 		// clearDayActive(newDay);
 		activeDay = newDay;
 	}
 
 	public int getSelectedYear() {
 		return yy;
 	}
 
 	/**
 	 * Unset any previously highlighted day
 	 * 
 	 * @param newDay
 	 */
 	private boolean clearDayActive(int newDay) {
 		JButton b;
 
 		// No limpiamos el color si el dia seleccionado es distinto
 		b = labs[(leadGap + newDay - 1) / 7][(leadGap + newDay - 1) % 7];
 		if (b.getBackground().getRGB() == b0.getBackground().getRGB())
 			return false;
 		// First un-shade the previously-selected square, if any
 		if (activeDay > 0) {
 			// b = labs[(leadGap + activeDay - 1) / 7][(leadGap + activeDay - 1)
 			// % 7];
 			b.setBackground(b0.getBackground());
 			b.repaint();
 			activeDay = -1;
 			// Anyadir a la lista
 			// addDayToList(newDay, false);
 			return true;
 		}
 		return false;
 	}
 
 	private void clearAllDays() {
 		JButton b;
 
 		for (int i = 0; i < 6; i++)
 			for (int j = 0; j < 7; j++) {
 				b = labs[i][j];
 				b.setBackground(b0.getBackground());
 			}
 	}
 
 	public void clearAllUserSelections() {
 		this.calendarFreeDays.clear();
 		this.userHolidays.clear();
 		this.userNotWorkingDays.clear();
 		this.userWorkingDays.clear();
 	}
 
 	private void addEventListener(ICalendarEventListener l) {
 		if (l != null)
 			registeredEventListeners.add(l);
 	}
 
 	private void addCalendarFreeDayToList(Date date) {
 		calendarFreeDays.add(date);
 		recompute();
 	}
 
 	private void addUserHolidayToList(Date date) {
 		userHolidays.add(date);
 		recompute();
 	}
 
 	private void addUserNotWorkingDayToList(Date date) {
 		userNotWorkingDays.add(date);
 		recompute();
 	}
 
 	private void addUserWorkingDayToList(Date date) {
 		userWorkingDays.add(date);
 		recompute();
 	}
 
 	private void removeUserHolidayFromList(Date date) {
 		userHolidays.remove(date);
 		recompute();
 	}
 
 	private void removeUserWorkingDayFromList(Date date) {
 		userWorkingDays.remove(date);
 		recompute();
 	}
 
 	private void removeUserNotWorkingDayFromList(Date date) {
 		userNotWorkingDays.remove(date);
 		recompute();
 	}
 
 	private void removeCalendarFreeDayFromList(Date date) {
 		calendarFreeDays.remove(date);
 		recompute();
 	}
 
 	private boolean isDayUserNotWorkingDay(int year, int month, int day) {
 		Date date = new Date(year - 1900, month, day);
 		return userNotWorkingDays.contains(date);
 	}
 
 	private boolean isDayUserWorkingDay(int year, int month, int day) {
 		Date date = new Date(year - 1900, month, day);
 		return userWorkingDays.contains(date);
 	}
 
 	private boolean isDayUserHoliday(int year, int month, int day) {
 		Date date = new Date(year - 1900, month, day);
 		return userHolidays.contains(date);
 	}
 
 	private boolean isDayCalendarFreeDay(int year, int month, int day) {
 		Date date = new Date(year - 1900, month, day);
 		return calendarFreeDays.contains(date);
 	}
 
 	private boolean listContainstDay(int year, int month, int day,
 			LIST_TYPE listType) {
 		Date date = new Date(year - 1900, month, day);
 		switch (listType) {
 		case CALENDAR_FREEDAY:
 			return calendarFreeDays.contains(date);
 		case USER_HOLIDAYS:
 			return userHolidays.contains(date);
 		case USER_WORKINGDAY:
 			return userWorkingDays.contains(date);
 		}
 		return false;
 	}
 
 	// ICalendarActionProvider methods
 
 	@Override
 	public void addDayToList(Date date, LIST_TYPE type) {
 		switch (type) {
 		case CALENDAR_FREEDAY:
 			addCalendarFreeDayToList(date);
 			break;
 		case USER_HOLIDAYS:
 			addUserHolidayToList(date);
 			break;
 		case USER_NOT_WORKINGDAY:
 			addUserNotWorkingDayToList(date);
 			break;
 		case USER_WORKINGDAY:
 			addUserWorkingDayToList(date);
 			break;
 		}
 	}
 
 	@Override
 	public void removeDayFromList(Date date, LIST_TYPE type) {
 		switch (type) {
 		case CALENDAR_FREEDAY:
 			removeCalendarFreeDayFromList(date);
 			break;
 		case USER_HOLIDAYS:
 			removeUserHolidayFromList(date);
 			break;
 		case USER_NOT_WORKINGDAY:
 			removeUserNotWorkingDayFromList(date);
 			break;
 		case USER_WORKINGDAY:
 			removeUserWorkingDayFromList(date);
 			break;
 		}
 	}
 
 	public void dataInputOcurred(IDateEntity entity) {
 		entity.getDate();
 		switch (entity.getDateType()) {
 		case FREE_DAY:
 			addDayToList(entity.getDate(), LIST_TYPE.CALENDAR_FREEDAY);
 			removeDayFromList(entity.getDate(), LIST_TYPE.USER_HOLIDAYS);
 			removeDayFromList(entity.getDate(), LIST_TYPE.USER_WORKINGDAY);
 			break;
 		case HOLIDAYS:
 			addDayToList(entity.getDate(), LIST_TYPE.USER_HOLIDAYS);
 			removeDayFromList(entity.getDate(), LIST_TYPE.CALENDAR_FREEDAY);
 			removeDayFromList(entity.getDate(), LIST_TYPE.USER_WORKINGDAY);
 			break;
 		case WORK_DAY:
 			addDayToList(entity.getDate(), LIST_TYPE.USER_WORKINGDAY);
 			removeDayFromList(entity.getDate(), LIST_TYPE.USER_HOLIDAYS);
 			removeDayFromList(entity.getDate(), LIST_TYPE.CALENDAR_FREEDAY);
 			break;
 		}
 		recompute();
 	}
 
 	/** For testing, a main program */
 	public static void main(String[] av) {
 		JFrame f = new JFrame("Cal");
 		Container c = f.getContentPane();
 		c.setLayout(new FlowLayout());
 
 		// for this test driver, hardcode 1995/02/10.
 		c.add(new JUserCalendarPanel(1995, 2 - 1, 10, null, false, true));
 
 		// and beside it, the current month.
 		c.add(new JUserCalendarPanel(null, false, true));
 
 		f.pack();
 		f.setVisible(true);
 	}
 }
