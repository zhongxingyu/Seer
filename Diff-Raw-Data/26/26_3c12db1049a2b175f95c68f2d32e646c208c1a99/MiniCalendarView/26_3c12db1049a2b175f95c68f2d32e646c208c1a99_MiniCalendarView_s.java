 package no.ntnu.fp.gui;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 
 import no.ntnu.fp.ActiveWeekListener;
 import no.ntnu.fp.Client;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 
 public class MiniCalendarView extends JPanel implements ActiveWeekListener{
     private JPanel monthCalendar;
     private JLabel heading;
     private GregorianCalendar calendar;
     private JButton previousMonth, nextMonth;
 
     public MiniCalendarView(int year, int month) {
         setLayout(new BorderLayout());
         setPreferredSize(GUI_CONSTANTS.miniCalendarViewDimension);
         setUp();
         Client.addActiveWeekListener(this);
     }
     
     private void setUp() {
     	calendar = new GregorianCalendar();
     	previousMonth = new JButton("<");
     	previousMonth.setPreferredSize(GUI_CONSTANTS.miniCalendarNextDimension);
     	nextMonth = new JButton(">");
     	nextMonth.setPreferredSize(GUI_CONSTANTS.miniCalendarNextDimension);
     	
         heading = new JLabel("", JLabel.LEFT);
         heading.setFont(GUI_CONSTANTS.bigFont);
         add(previousMonth,BorderLayout.WEST);
         add(heading, BorderLayout.NORTH);
         add(nextMonth,BorderLayout.EAST);
         refreshCalendar();
         add(monthCalendar, BorderLayout.CENTER);
         
         previousMonth.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addActiveMonth(-1);
 			}
 		});
         
         nextMonth.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addActiveMonth(1);
 			}
 		});
         
         
     }
     
     public void setActiveWeek(int year, int month, int date){
     	calendar.set(GregorianCalendar.YEAR, year);
     	calendar.set(GregorianCalendar.MONTH, month);
     	calendar.set(GregorianCalendar.DATE, date);
     	monthChanged();
     }
     
     private void addActiveMonth(int month){
     	calendar.add(GregorianCalendar.MONTH,month);
     	monthChanged();
     }
     
     private void monthChanged(){
     	refreshCalendar();
     	heading.setText(calendar.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG,Locale.ENGLISH)+" "+calendar.get(GregorianCalendar.YEAR));
     	
     	System.out.println(Client.dateToString(calendar));
     }
     
     private void refreshCalendar() {
     	if(monthCalendar != null){
     	monthCalendar.removeAll();
     	remove(monthCalendar);
     	}
         monthCalendar= new JPanel();
         add(monthCalendar);
         monthCalendar.setLayout(new GridLayout(5,7));
         int numberOfDays = calendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
         GregorianCalendar g = (GregorianCalendar) calendar.clone();
         g.set(GregorianCalendar.DATE,1);
         int weekStartDelay;
         /* Here we assume the week starts on a monday. TODO: support for different locales */
         switch(g.get(GregorianCalendar.DAY_OF_WEEK)){
         case GregorianCalendar.MONDAY:
         	weekStartDelay = 1;
         	break;
         case GregorianCalendar.TUESDAY:
         	weekStartDelay = 2;
         	break;
         case GregorianCalendar.WEDNESDAY:
         	weekStartDelay = 3;
         	break;
         case GregorianCalendar.THURSDAY:
         	weekStartDelay = 4;
         	break;
         case GregorianCalendar.FRIDAY:
         	weekStartDelay = 5;
         	break;
         case GregorianCalendar.SATURDAY:
         	weekStartDelay = 6;
         	break;
         case GregorianCalendar.SUNDAY:
         	weekStartDelay = 7;
         	break;
         default:
         	weekStartDelay = 0;
         	break;
         }
         
         for(int i=0;i<weekStartDelay;i++){
         	monthCalendar.add(new JLabel("")); //adding empty JLabels for padding
          									   //because I have no idea how to
         									   //actually use Swing :3
         }
         
         for (int i = 1; i <= numberOfDays; i++) {
         	
             JLabel day = new JLabel(""+i);
             monthCalendar.add(day);
             day.addMouseListener( mouseListenerGenerator(calendar.get(GregorianCalendar.YEAR), calendar.get(GregorianCalendar.MONTH), i));
         }
         monthCalendar.add(new JLabel(""+new GregorianCalendar().get(GregorianCalendar.SECOND)));
         System.out.println("this is the month "+calendar.get(GregorianCalendar.MONTH));
         System.out.println("added lots of events");
         monthCalendar.repaint();
 
     }
     
     private MouseListener mouseListenerGenerator(final int _year, final int _month, final int _date){
     	return new MouseListener() {
 			
     		int year=_year;
     		int month=_month;
     		int date=_date;
 			@Override
 			public void mouseReleased(MouseEvent e) {
 			}
 			
 			@Override
 			public void mousePressed(MouseEvent e) {
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 
 			}
 			
 			@Override
 			public void mouseEntered(MouseEvent e) {
 			}
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				System.out.println("mouse event triggered, "+_year+"-"+_month+"-"+_date);
 				Client.setActiveWeek(year, month, date);
 			}
 		};
     	
     }
 }
