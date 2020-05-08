 import javax.swing.*;
 import javax.swing.table.*;
 
 import java.awt.*;
 
 public class YearView extends BaseView {
 
     private static final long serialVersionUID = 1L;
 
     public YearView(CalendarModel model) {
         super(model);
     }
 
     public void setupCalendar() {
        //add(new JLabel("YEAR VIEW"));
        String[] names = { "January", "February", "March", "April",
            "May", "June", "July", "August", "September", "October",
            "November", "December" };
        for (int i = 0; i < 12; i++){
             TableModel temp = new YearDataModel(names[i]);
             JTable month = new JTable(temp);
             JScrollPane scrollPane = new JScrollPane(month);
             this.add(scrollPane, BorderLayout.CENTER);
         }
     }
 }
