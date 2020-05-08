 import javax.swing.*;
 import javax.swing.table.*;
 
 import java.awt.*;
 
 public class YearView extends BaseView {
 
     private static final long serialVersionUID = 1L;
 
     public YearView(CalendarModel model) {
         super(model);
     }
 
     public void setupCalendar() {
         GridLayout grid = new GridLayout(0, 4);
         JPanel panel = new JPanel();
         panel.setLayout(grid);
         String[] names = { "January", "February", "March", "April", "May",
                 "June", "July", "August", "September", "October", "November",
                 "December" };
         int[] days = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
         int offset = 0;
         for (int i = 0; i < 12; i++) {
             TableModel temp = new YearDataModel(names[i], offset, days[i]);
             offset = (offset + days[i]) % 7;
             JTable month = new JTable(temp);
             JScrollPane scrollPane = new JScrollPane(month);
             this.add(scrollPane, BorderLayout.CENTER);
             panel.add(scrollPane);
         }
         this.add(panel);
     }
 }
