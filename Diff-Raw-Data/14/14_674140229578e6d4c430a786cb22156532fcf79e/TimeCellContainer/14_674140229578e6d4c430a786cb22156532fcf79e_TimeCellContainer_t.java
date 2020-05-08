 package com.tuvistavie.meetup.event.model;
 
 import com.tuvistavie.meetup.event.activity.SelectTimeActivity;
 import com.tuvistavie.meetup.model.listener.OnUpdateListener;
 import com.tuvistavie.meetup.util.DateTimeUtil;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created by daniel on 9/6/13.
  */
 public class TimeCellContainer {
     private final int columnsNumber;
     private final int rowsNumber;
     private final int daysPerPage;
 
     private boolean[][] cells;
 
     private int currentPage;
 
     private OnUpdateListener onUpdateListener;
 
     public TimeCellContainer(int columnsNumber) {
         this(columnsNumber, SelectTimeActivity.CELLS_PER_DAY);
     }
 
     public TimeCellContainer(int columnsNumber, int rowsNumber) {
         this(columnsNumber, rowsNumber, SelectTimeActivity.DAYS_PER_PAGE);
     }
 
     public TimeCellContainer(int columnsNumber, int rowsNumber, int daysPerPage) {
         this.daysPerPage = daysPerPage;
         this.columnsNumber = columnsNumber;
         this.rowsNumber = rowsNumber;
         this.currentPage = 0;
         this.cells = new boolean[columnsNumber][rowsNumber];
     }
 
     public int getColumn(int position) {
        return (position % getDisplayedColumnsNumber()) + (currentPage * daysPerPage);
     }
 
     public int getRow(int position) {
         return position / getDisplayedColumnsNumber();
     }
 
     public int getDisplayedColumnsNumber() {
         return Math.min(daysPerPage, columnsNumber - (daysPerPage * currentPage));
     }
 
     public boolean getAt(int x, int y) {
         return cells[x][y];
     }
 
     public boolean getAt(int position) {
        int x = getColumn(position);
        int y = getRow(position);
        return getAt(x, y);
     }
 
     public void setAt(int x, int y, boolean b) {
         cells[x][y] = b;
         runOnUpdateListener();
     }
 
     public void setAt(int position, boolean b) {
         setAt(getColumn(position), getRow(position), b);
     }
 
     public int getCurrentPage() {
         return currentPage;
     }
 
     public int getDaysPerPage() {
         return daysPerPage;
     }
 
     public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
         this.onUpdateListener = onUpdateListener;
     }
 
     public int getColumnNumberAt(int index) {
         return index + (daysPerPage * currentPage);
     }
 
     public void setRange(int startPosition, int endPosition, boolean b) {
         int x1 = getColumn(startPosition), x2 = getColumn(endPosition);
         int y1 = getRow(startPosition), y2 = getRow(endPosition);
         int startX = Math.min(x1, x2), endX = Math.max(x1, x2);
         int startY = Math.min(y1, y2), endY = Math.max(y1, y2);
         for(int x = startX; x <= endX; x++) {
             for(int y = startY; y <= endY; y++) {
                 cells[x][y] = b;
             }
         }
         runOnUpdateListener();
     }
 
     private void runOnUpdateListener() {
         if(onUpdateListener != null) {
             onUpdateListener.onUpdate(null);
         }
     }
 
     public EventDateCollection toEventDateCollection(long[] datesArray) {
         EventDateCollection eventDateCollection = new EventDateCollection();
         for(int i = 0; i < datesArray.length; i++) {
             for(EventDate eventDate: datesForDay(datesArray[i], i)) {
                 eventDateCollection.getEntities().add(eventDate);
             }
         }
         return eventDateCollection;
     }
 
     private List<EventDate> datesForDay(long date, int dayIndex) {
         List<EventDate> dates = new ArrayList<EventDate>();
         for(int i = 0; i < cells[dayIndex].length; i++) {
             if(cells[dayIndex][i]) {
                 int j;
                 for(j = i + 1; j < cells[dayIndex].length && cells[dayIndex][j]; j++);
                 dates.add(getEventDate(date, i, j - 1));
                 i = j - 1;
             }
         }
         return dates;
     }
 
     private EventDate getEventDate(long date, int startIndex, int endIndex) {
         Date startDate = getDateFromIndex(date, startIndex);
         Date endDate = getDateFromIndex(date, endIndex);
         return new EventDate(startDate, endDate);
     }
 
     private Date getDateFromIndex(long date, int index) {
         Calendar c = Calendar.getInstance();
         c.setTime(new Date(date));
         c.set(Calendar.HOUR_OF_DAY, index * 24 / rowsNumber);
         c.set(Calendar.MINUTE, index % 2 == 0 ? 0 : 30);
         return c.getTime();
     }
 }
