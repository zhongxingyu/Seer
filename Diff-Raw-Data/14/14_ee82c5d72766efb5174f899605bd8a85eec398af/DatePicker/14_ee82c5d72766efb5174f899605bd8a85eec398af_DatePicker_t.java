 /**
  * This class reports behaviors of what happens in the small calendar.
  */
 
 package gui.main;
 import java.awt.Color;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.swing.JPanel;
 
import java.util.Date;
 
 public class DatePicker extends JPanel {
 	
 	private TinyDayBlock selectedBlock;
 	private MonthView prev, curr, next;
 	private MainWindow mainWindow;
 	private Date currentDate;
 	
 	public DatePicker(MainWindow window) {
 		this.mainWindow = window;
 		setMaximumSize(new Dimension(200, 500));
 		GregorianCalendar cal = new GregorianCalendar();
 		cal.setTimeInMillis(System.currentTimeMillis());
 		
 		currentDate = new Date(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.YEAR));
 		
 		//prev = new MonthView(this, currentDate, MonthView.PREVIOUS_MONTH);
 		curr = new MonthView(this, currentDate, MonthView.CURRENT_MONTH);
 		//next = new MonthView(this, currentDate, MonthView.NEXT_MONTH);
 		
 		//reportFocusGained(selectedBlock);
 		curr.selectDay(selectedBlock.getDate().getDay());
 		
 		setLayout(new GridLayout(0,1));
 		//add(prev);
 		add(curr);
 		//add(next);
 	}
 	
 	public Date getDate() {
 		return currentDate;
 	}
 	
 	public void registerCurrentDay(TinyDayBlock b) {
 		selectedBlock = b;
 	}
 	
 	public void setDate(Date d) {
 		//prev = new MonthView(this, d, MonthView.PREVIOUS_MONTH);
 		curr = new MonthView(this, d, MonthView.CURRENT_MONTH);
 		//next = new MonthView(this, d, MonthView.NEXT_MONTH);
 		
 		removeAll();
 		//add(prev);
 		add(curr);
 		//add(next);
 		
 		repaint();
 		validate();
 		
 		
 		//curr.selectDay(selectedBlock.getDate().getDay());
 		mainWindow.setDate(d);		
 	}
 	
 	public void nextMonth() {
 		int day = curr.getDate().getDay();
 		int month = curr.getDate().getMonth();
 		int year = curr.getDate().getYear();
 		if (month == 12) {
 			month = 1;
 			year++;
 		} else
 			month++;
 		Date newDate = new Date(month, day, year);
 		setDate(newDate);
 		//selectedBlock.setDate(newDate);
 		//reportFocusGained(selectedBlock);		
 		TinyDayBlock b = new TinyDayBlock(this, newDate, Color.LIGHT_GRAY, false);
 		curr.deselectDay(selectedBlock.getDate().getDay());
 		curr.selectDay(day);
 		selectedBlock = b;
 		//curr.selectDay(day);
 		//curr.selectDay(selectedBlock.getDate().getDay());
 	}
 	
 	public void prevMonth() {
 		int day = curr.getDate().getDay();
 		int month = curr.getDate().getMonth();
 		int year = curr.getDate().getYear();
 		if (month == 1) {
 			month = 12;
 			year--;
 		} else
 			month--;
 		Date newDate = new Date(month, day, year);
 		setDate(newDate);
 		//selectedBlock.setDate(newDate);
 		//reportFocusGained(selectedBlock);		
 		TinyDayBlock b = new TinyDayBlock(this, newDate, Color.LIGHT_GRAY, false);
 		curr.deselectDay(selectedBlock.getDate().getDay());
 		curr.selectDay(day);
 		selectedBlock = b;
 
 		//curr.selectDay(day);
 		//curr.selectDay(selectedBlock.getDate().getDay());
 	}
 	
 	/**
 	 * This method determines the behavior of clicking on a day in the month view.
 	 */
 	public void reportFocusGained(TinyDayBlock b) {
 		if (b.getDate().getMonth() == selectedBlock.getDate().getMonth()) {
 			if (mainWindow.inMonthView()) mainWindow.switchView();
 			curr.deselectDay(selectedBlock.getDate().getDay());
 			curr.selectDay(b.getDate().getDay());
 			selectedBlock = b;
 			mainWindow.setDate(b.getDate());
 		} else {
 			if (mainWindow.inMonthView()) mainWindow.switchView();
 			setDate(b.getDate());
 			curr.selectDay(b.getDate().getDay());
 			selectedBlock = b;
 		}
 	}
 }
