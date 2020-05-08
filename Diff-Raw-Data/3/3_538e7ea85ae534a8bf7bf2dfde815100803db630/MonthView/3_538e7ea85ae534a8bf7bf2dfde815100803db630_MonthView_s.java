 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.SortedSet;
 
 /**
  * Month view for the calendar
  * 
  * @author gordon
  * 
  */
 public class MonthView extends BaseView {
 
     private static final long serialVersionUID = 1L;
     private JTable month;
     private MonthDataModel monthView;
     private GridBagConstraints gbc;
     Integer[] days = Date.getDaysInMonth(currentDate.getYear());
 
     public MonthView(CalendarModel model, JLabel viewLabel) {
         super(model, viewLabel);
     }
 
     protected void setupCalendar() {
         setUpGBC();
         addPreviousButton();
         addTable();
         addNextButton();
         this.add(panel, BorderLayout.CENTER);
     }
 
     private void addTable() {
         // 1st of January 2011 was a Saturday
         int offset = 5;
         monthView = new MonthDataModel(offset, days[currentDate.getMonth() - 1]);
         // Load currently known events for the date into
         // the table.
         monthView.setData(dateMap.get(currentDate));
         month = new JTable(monthView);
         month.getTableHeader().setReorderingAllowed(false);
         month.setRowHeight(100);
         month.setRowSelectionAllowed(false);
         JScrollPane scrollPane = new JScrollPane(month);
         this.add(scrollPane, BorderLayout.CENTER);
         panel.add(scrollPane, gbc);
     }
 
     private void setUpGBC() {
         gbc = new GridBagConstraints();
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         gbc.fill = GridBagConstraints.BOTH;
     }
 
     private void addPreviousButton() {
         previousB.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 // Decrement the current date by one day
                 // until the month value changes. currentDate
                 // now refers to the last day of the previous
                 // month
                 int month = currentDate.getMonth();
                 currentDate.decrement();
                 while (month == currentDate.getMonth()) {
                     currentDate.decrement();
                 }
                 // Update offset for where the new month starts
                 int offset = monthView.getOffset();
                 int numDays = days[currentDate.getMonth() - 1];
                 offset = (offset - numDays) % 7;
                 if (offset < 0)
                     offset += 7;
                 numDays = days[currentDate.getMonth() - 1];
                 monthView.setOffset(offset);
                 monthView.setDays(numDays);
                 // Update table with data for the new month.
                 monthView.setData(dateMap.get(currentDate));
                 monthView.fireTableDataChanged();
                 viewLabel.setText(MonthView.this.toString());
             }
         });
         panel.add(previousB);
     }
 
     private void addNextButton() {
         nextB.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 // Increment current date until it ticks
                 // over to a new month. Current date then
                 // points to the 1st of the next month.
                 currentDate.increment();
                 while (currentDate.getDay() != 1) {
                     currentDate.increment();
                 }
                 int offset = monthView.getOffset();
                 int numDays = monthView.getDays();
                 // Update offset to work out where this
                 // new month starts in the week.
                 offset = (offset + numDays) % 7;
                 numDays = days[currentDate.getMonth() - 1];
                 monthView.setOffset(offset);
                 monthView.setDays(numDays);
                 // Update table with data for new month.
                 monthView.setData(dateMap.get(currentDate));
                 monthView.fireTableDataChanged();
                 viewLabel.setText(MonthView.this.toString());
             }
         });
         panel.add(nextB);
     }
 
     public String toString() {
         // Label for Month view is <Month Name> + <Year>
         return monthName(currentDate.getMonth()) + " "
                         + currentDate.getYear();
     }
 
     private String monthName(int i) {
         // Return the full name for requested month
         return Date.monthNames[i - 1];
     }
 
 }
