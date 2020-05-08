 package com.uwusoft.timesheet.util;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 
 public class WeekComposite {
 	private int currentWeekNum, lastWeekNum;
 	private Date startDate, endDate;
 	private Label startDateLabel, endDateLabel;
 	private Button leftButton, rightButton, currentWeekButton;
 	private PropertyChangeListener listener;
 	private Composite composite;
 	private Calendar current;
 	
 	public WeekComposite(PropertyChangeListener listener, int currentWeekNum, int lastWeekNum) {
 		this.listener = listener;
 		this.currentWeekNum = currentWeekNum;
 		this.lastWeekNum = lastWeekNum;
 	}
 
 	public Composite createComposite(final Composite composite) {
 		this.composite = composite;
         RowLayout layout = new RowLayout();
         layout.center = true;
         layout.wrap = false;
         composite.setLayout(layout);
         
         current = new GregorianCalendar();
         current.setTime(new Date());
     	
 		leftButton = new Button(composite, SWT.PUSH);
         leftButton.setText("<<");
         leftButton.setToolTipText("Previous week");
         leftButton.setEnabled(currentWeekNum > 1);
         
         startDateLabel = new Label(composite, SWT.NONE);
         
         currentWeekButton = new Button(composite, SWT.PUSH);
         currentWeekButton.setText(" - ");
         currentWeekButton.setToolTipText("Current week");
         currentWeekButton.setEnabled(currentWeekNum != current.get(Calendar.WEEK_OF_YEAR));
         
         endDateLabel = new Label(composite, SWT.NONE);
 
         rightButton = new Button(composite, SWT.PUSH);
         rightButton.setText(">>");
         rightButton.setToolTipText("Next week");
     	rightButton.setEnabled(currentWeekNum < lastWeekNum);
         
         leftButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
             	currentWeekNum = currentWeekNum - 1;
             	calculateStartEndDate(true);
             }
         });
         
     	currentWeekButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
             	currentWeekNum = current.get(Calendar.WEEK_OF_YEAR);
             	calculateStartEndDate(true);
             }
         });
 
         rightButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
             	currentWeekNum = currentWeekNum + 1;
             	calculateStartEndDate(true);
             }
         });
         
         calculateStartEndDate(false);
         return composite;		
 	}
 
 	private void calculateStartEndDate(boolean firePropertyChange) {
 		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.WEEK_OF_YEAR, currentWeekNum); // TODO if local=EN cal.set(Calendar.WEEK_OF_YEAR, currentWeekNum + 1);
     	cal.setFirstDayOfWeek(Calendar.MONDAY);
     	cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
     	startDate = DateUtils.truncate(cal.getTime(), Calendar.DATE);
     	cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
     	endDate = DateUtils.truncate(cal.getTime(), Calendar.DATE);
     	if (listener != null && firePropertyChange) listener.propertyChange(new PropertyChangeEvent(this, StorageService.PROPERTY_WEEK, null, currentWeekNum));
 		startDateLabel.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(startDate));
         endDateLabel.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(endDate));        
         leftButton.setEnabled(currentWeekNum > 1);
     	rightButton.setEnabled(currentWeekNum < lastWeekNum);
         currentWeekButton.setEnabled(currentWeekNum != current.get(Calendar.WEEK_OF_YEAR));
     	composite.pack();
 	}
     
 	public Date getStartDate() {
 		return startDate;
 	}
 
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	public int getWeekNum() {
 		return currentWeekNum;
 	}
 
 	public void setCurrentWeekNum(int currentWeekNum) {
 		this.currentWeekNum = currentWeekNum;
 		calculateStartEndDate(false);
 	}
 }
