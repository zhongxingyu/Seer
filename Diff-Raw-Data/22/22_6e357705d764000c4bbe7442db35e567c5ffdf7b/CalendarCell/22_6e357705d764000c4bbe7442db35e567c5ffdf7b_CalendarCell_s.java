 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class CalendarCell extends JPanel implements MouseListener {
     
     private static final Color NOT_IN_MONTH_COLOR = new Color(220, 220, 220);
     private static final Color TODAY_COLOR = new Color(220, 240, 240);
     private static final int DATE_SIZE = 20;
     
     private static JPopupMenu popup;
     private static EventLabel selectedEventLabel;
     
     // This static initialization block is executed exactly once (at the start of the program)
     static {
         popup = new JPopupMenu();
        final JMenuItem editEvent = new JMenuItem("Edit Event");
         popup.add(editEvent);
         final JMenuItem deleteEvent = new JMenuItem("Delete Event");
         popup.add(deleteEvent);
         ActionListener listener = new ActionListener(){
             @Override
             public void actionPerformed(ActionEvent e) {
                 RecurringEvent parent = selectedEventLabel.event.getParent();
                 Event event = (parent != null) ? parent : selectedEventLabel.event;
                 if (e.getSource() == editEvent){
                     SchedulerMain.application.showEditEventPage(event);
                 } else if (e.getSource() == deleteEvent){
                     SchedulerMain.application.showDeletePopup(event);
                 }
             }
         };
         editEvent.addActionListener(listener);
         deleteEvent.addActionListener(listener);
     }
     
     private ArrayList<EventLabel> list = new ArrayList<EventLabel>(0);
     private JLabel dayNumberLabel;
     
     public CalendarCell(Calendar calendar, boolean isInMonth, boolean isToday){
         // Calendar calendar will be modified outside of this class, so do NOT 
         // store a reference to the object
         if (isInMonth){
             setBackground(isToday ? TODAY_COLOR : Color.WHITE);
         } else {
             setBackground(NOT_IN_MONTH_COLOR);
         }
         setBorder(BorderFactory.createLineBorder(Color.BLACK));
         setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
         dayNumberLabel = new JLabel(""+calendar.get(Calendar.DATE));
         dayNumberLabel.setFont(new Font(dayNumberLabel.getFont().getFontName(), Font.BOLD, DATE_SIZE));
         dayNumberLabel.setHorizontalTextPosition(SwingConstants.LEFT);
         dayNumberLabel.setVerticalTextPosition(SwingConstants.TOP);
         add(dayNumberLabel);
     }
     
     public void addEvent(OneTimeEvent event){
         JLabel label = new JLabel(event.getName());
         add(label);
         label.addMouseListener(this);
         Iterator<EventLabel> it = list.iterator();
         while (it.hasNext()){
             EventLabel el = it.next();
             if (el.event.getEndDate().after(event.getStartDate())){
                 el.label.setForeground(Color.RED);
                 label.setForeground(Color.RED);
             }
         }
         list.add(new EventLabel(event, label));
     }
     
     public void addWeatherIcon(ImageIcon icon){
         dayNumberLabel.setIcon(icon);
     }
     
     @Override
     public void mouseClicked(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1){
             Iterator<EventLabel> it = list.iterator();
             while (it.hasNext()){
                 EventLabel el = it.next();
                 if (e.getSource() == el.label){
                     RecurringEvent parent = el.event.getParent();
                     if (parent == null){
                         SchedulerMain.application.showViewEventPage(el.event, null);
                     } else {
                         SchedulerMain.application.showViewEventPage(parent, el.event);
                     }
                     break;
                 }
             }
         }
     }
     
     @Override
     public void mousePressed(MouseEvent e) {
         checkForTriggerEvent(e);
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         checkForTriggerEvent(e);
     }
     
     public void checkForTriggerEvent(MouseEvent e){
         if (e.isPopupTrigger()){
             Iterator<EventLabel> it = list.iterator();
             while (it.hasNext()){
                 EventLabel el = it.next();
                 if (e.getSource() == el.label){
                     selectedEventLabel = el;
                     break;
                 }
             }
             popup.show(e.getComponent(), e.getX(), e.getY());
         }
     }
     
     @Override
     public void mouseEntered(MouseEvent e) {
         setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
     }
     @Override
     public void mouseExited(MouseEvent e) {
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
     }
 }
