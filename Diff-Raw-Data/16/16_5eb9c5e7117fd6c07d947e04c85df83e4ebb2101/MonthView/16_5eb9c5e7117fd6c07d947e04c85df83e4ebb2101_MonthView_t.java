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
     JPanel panel;
     JTable month;
     MonthDataModel monthView;
     JButton nextB, previousB;
 
     public MonthView(CalendarModel model) {
         super(model);
     }
 
     protected void setupCalendar() {
         panel = new JPanel(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         gbc.fill = GridBagConstraints.BOTH;
         addPreviousButton();
        monthView = new MonthDataModel(0, 31);
         month = new JTable(monthView);
         month.getTableHeader().setReorderingAllowed(false);
         month.setRowHeight(100);
         month.setRowSelectionAllowed(false);
         JScrollPane scrollPane = new JScrollPane(month);
         this.add(scrollPane, BorderLayout.CENTER);
         panel.add(scrollPane, gbc);
         addNextButton();
         this.add(panel, BorderLayout.CENTER);
 
     }
 
     private void addPreviousButton() {
         previousB = new JButton("Previous");
         previousB.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 int offset = monthView.getOffset();
                 int days = monthView.getDays();
                 offset = (offset - days) % 7;
                 if (offset < 0)
                     offset += 7;
                 System.out.println(offset);
                 monthView.setOffset(offset);
                 monthView.fireTableDataChanged();
             }
         });
         panel.add(previousB);
     }
 
     private void addNextButton() {
         nextB = new JButton("Next");
         nextB.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 int offset = monthView.getOffset();
                 int days = monthView.getDays();
                 offset = (offset + days) % 7;
                 monthView.setOffset(offset);
                 monthView.fireTableDataChanged();
             }
         });
         panel.add(nextB);
     }
 
 }
