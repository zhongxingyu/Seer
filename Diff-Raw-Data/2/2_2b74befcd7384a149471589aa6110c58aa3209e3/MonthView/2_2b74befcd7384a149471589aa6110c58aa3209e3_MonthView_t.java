 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
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
        int offset = 5; // 1st of January 2011 was a Saturday
         monthView = new MonthDataModel(offset, days[currentDate.getMonth() - 1]);
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
                 int month = currentDate.getMonth();
                 currentDate.decrement();
                 while (month == currentDate.getMonth()) {
                     currentDate.decrement();
                 }
                 int offset = monthView.getOffset();
                 int numDays = days[currentDate.getMonth() - 1];
                 offset = (offset - numDays) % 7;
                 if (offset < 0)
                     offset += 7;
                 numDays = days[currentDate.getMonth() - 1];
                 monthView.setOffset(offset);
                 monthView.setDays(numDays);
                 monthView.fireTableDataChanged();
                 viewLabel.setText(MonthView.this.toString());
             }
         });
         panel.add(previousB);
     }
 
     private void addNextButton() {
         nextB.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 currentDate.increment();
                 while (currentDate.getDay() != 1) {
                     currentDate.increment();
                 }
                 int offset = monthView.getOffset();
                 int numDays = monthView.getDays();
                 offset = (offset + numDays) % 7;
                 numDays = days[currentDate.getMonth() - 1];
                 monthView.setOffset(offset);
                 monthView.setDays(numDays);
                 monthView.fireTableDataChanged();
                 viewLabel.setText(MonthView.this.toString());
             }
         });
         panel.add(nextB);
     }
     
     public String toString() {
         return monthName(currentDate.getMonth()) + " " + currentDate.getYear();
     }
     
     private String monthName(int i) {
         return Date.monthNames[i-1];
     }
 
 }
