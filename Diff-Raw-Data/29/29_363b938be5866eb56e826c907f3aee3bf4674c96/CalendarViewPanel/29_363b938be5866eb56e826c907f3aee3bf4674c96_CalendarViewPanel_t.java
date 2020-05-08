 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.ntnu.fp.gui;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Calendar;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  *
  * @author alxandr
  */
 public class CalendarViewPanel extends BaseCalendarView implements ActionListener
 {
 
     public static void main(String[] args)
     {
         JFrame frame = new JFrame();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         CalendarViewPanel calp = new CalendarViewPanel();
         frame.setContentPane(calp);
         frame.setVisible(true);
         frame.pack();
     }
     private CalendarPanel calendarPanel;
     private JLabel yearLabel;
     private JPanel paginationPanel;
     private JButton prevWeekButton;
     private JButton nextWeekButton;
     private JLabel weekLabel;
 
     public CalendarViewPanel()
     {
         super();
 
         setupGUI();
     }
 
     private void setupGUI()
     {
         calendarPanel = new CalendarPanel(this);
         GridBagConstraints s = new GridBagConstraints();
         s.anchor = GridBagConstraints.NORTHWEST;
         s.fill = GridBagConstraints.BOTH;
         calendarPanel.setPreferredSize(new Dimension(985, 445));
 
         add(calendarPanel, s);
 
         s.gridy = 2;
         s.anchor = GridBagConstraints.CENTER;
         s.fill = GridBagConstraints.NONE;
         yearLabel = new JLabel("" + calendarPanel.getWeek().get(Calendar.YEAR));
         add(yearLabel, s);
 
         paginationPanel = new JPanel();
         s.gridy = 1;
         //s.anchor = GridBagConstraints.NORTHWEST;
         //s.fill = GridBagConstraints.BOTH;
         paginationPanel.setPreferredSize(new Dimension(200, 40));
         add(paginationPanel, s);
 
         prevWeekButton = new JButton("<");
         paginationPanel.add(prevWeekButton);
         prevWeekButton.addActionListener(this);
 
         weekLabel = new JLabel("" + calendarPanel.getWeek().get(Calendar.WEEK_OF_YEAR));
         paginationPanel.add(weekLabel);
 
         nextWeekButton = new JButton(">");
         paginationPanel.add(nextWeekButton);
         nextWeekButton.addActionListener(this);
     }
 
     public void actionPerformed(ActionEvent e)
     {
         Object src = e.getSource();
         if (src == nextWeekButton)
         {
             Calendar cal = calendarPanel.getWeek();
             cal.add(Calendar.WEEK_OF_YEAR, 1);
             calendarPanel.setWeek(cal);
            int week = cal.get(Calendar.WEEK_OF_YEAR);
            String wk = "" + week;
            if (wk.length() == 1)
            {
                wk = "0" + wk;
            }
            weekLabel.setText("" + wk);
             yearLabel.setText("" + cal.get(Calendar.YEAR));
         }
         else if (src == prevWeekButton)
         {
             Calendar cal = calendarPanel.getWeek();
             cal.add(Calendar.WEEK_OF_YEAR, -1);
             calendarPanel.setWeek(cal);
            int week = cal.get(Calendar.WEEK_OF_YEAR);
            String wk = "" + week;
            if (wk.length() == 1)
            {
                wk = "0" + wk;
            }
            weekLabel.setText("" + wk);
             yearLabel.setText("" + cal.get(Calendar.YEAR));
         }
     }
 }
