 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * Month view for the calendar
  * 
  * @author gordon
  * @author cmcl
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
         int offset = Date.getDayFromDate(new Date(1, 1,
                                                 currentDate.getYear()));
         monthView = new MonthDataModel(offset, 
                     days[currentDate.getMonth() - 1], dateMap);
         // Load currently known events for the date into
         // the table.
         monthView.setData(dateMap.get(currentDate));
         month = new JTable(monthView);
         month.getTableHeader().setReorderingAllowed(false);
         month.setRowHeight(100);
         month.setRowSelectionAllowed(false);
        month.setDefaultRenderer(TreeSet.class, new EventRenderer());
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
                 currentDate = currentDate.decrementMonth();
                 currentDate.setDay(1);
                 // Update offset for where the new month starts
                 int offset = Date.getDayFromDate(currentDate);
                 days = Date.getDaysInMonth(currentDate.getYear()); 
                 int numDays = days[currentDate.getMonth() - 1];
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
                 currentDate = currentDate.incrementMonth();
                 currentDate.setDay(1);
                 // Update offset to where this new month starts in the
                 // week.
                 int offset = Date.getDayFromDate(currentDate);
                 days = Date.getDaysInMonth(currentDate.getYear());
                 monthView.setOffset(offset);
                 monthView.setDays(days[currentDate.getMonth() - 1]);
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
