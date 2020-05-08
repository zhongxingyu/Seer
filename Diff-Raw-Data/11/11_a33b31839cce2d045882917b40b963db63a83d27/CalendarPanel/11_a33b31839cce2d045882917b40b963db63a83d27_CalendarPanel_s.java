 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GUI;
 
 import BackEnd.EventSystem.CalendarEvent;
 import BackEnd.EventSystem.SubEvent;
 import BackEnd.EventSystem.TimeSchedule;
 import BackEnd.ManagerSystem.MainManager;
 import BackEnd.ManagerSystem.PrivilegeInsufficientException;
 import EMS_Database.DoesNotExistException;
 import GUI.Dialog.EditSubEventDialog;
 import GUI.Dialog.NewSubEventDialog;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.MouseAdapter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.TableCellRenderer;
 import java.awt.event.MouseEvent;
 import java.text.DateFormatSymbols;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  *
  * @author Karina
  */
 public class CalendarPanel extends javax.swing.JPanel {
 
     private Calendar tempCalendar = Calendar.getInstance(); // gets the current day and time
     private int month = Calendar.getInstance().get(Calendar.MONTH);
     private int year = Calendar.getInstance().get(Calendar.YEAR);
     private MainManager manager;
     private JList detailsList = new JList();
     private SubEvent selectedSubEvent;
     JScrollPane listScroller = new JScrollPane(detailsList);
     int selectedRow;
     int selectedColumn;
     
     /**
      * Creates new form CalendarPanel
      */
     public CalendarPanel() {
         initComponents();
         detailsPanel.setLayout(new BorderLayout());
         detailsPanel.add(listScroller, BorderLayout.CENTER);
         manager = MainManager.getInstance();
         selectedSubEvent = null;
         calendarTable.getTableHeader().setReorderingAllowed(false);
         for (int i = 0; i < 7; i++) {
             calendarTable.setRowHeight(i, 64);
             calendarTable.getColumnModel().getColumn(i).setCellRenderer(new TextTableRenderer());
         }
 
         calendarTable.setCellSelectionEnabled(true);
         ListSelectionModel cellSelectionModel = calendarTable.getSelectionModel();
         cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
         calendarTable.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() == 1) {
                     String selectedData = null;
 
                     JTable target = (JTable) e.getSource();
 
                     selectedRow = target.getSelectedRow();
                     selectedColumn = target.getSelectedColumn();
 
                     CalendarEvent selectedCellValue = (CalendarEvent) target.getValueAt(selectedRow, selectedColumn);
                     manager.getSubEventManager().setSelectedSubEvent(selectedCellValue.getSubEventList().get(0));
                     updateDetailsList(selectedCellValue);
                 }
             }
         });
     
     
 
         populateCalendar();
         lastYearButton.setEnabled(false);
     }
     
      public void hideEventButtons() {
         addEventButton.setVisible(false);
         removeEventButton.setVisible(false);
         editSubEventButton.setVisible(false);
     } 
     
     public void updateDetailsList(CalendarEvent ce)
     {
         if (!ce.getSubEventList().isEmpty())
             manager.getSubEventManager().setSelectedSubEvent(ce.getSubEventList().get(0));
         Object[] tempDetailsList = new Object[ce.getSubEventList().size()];
         for (int i = 0; i < tempDetailsList.length; i++)
         {
             tempDetailsList[i] = ce.getSubEventList().get(i);
         }
         
         detailsList.setListData(tempDetailsList);
         
         detailsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         detailsList.setLayoutOrientation(JList.VERTICAL_WRAP);
         detailsList.setVisibleRowCount(-1);
         detailsList.addListSelectionListener(new DetailsListSelectionListener());
         detailsList.setCellRenderer(new listCellRenderer());
     }
     
     public void populateCalendar() {
         tempCalendar.set(year, month, 1); // sets the current month to its first day
         tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
         tempCalendar.set(Calendar.MINUTE, 0);
         tempCalendar.set(Calendar.SECOND, 0);
         tempCalendar.set(Calendar.MILLISECOND, 0);
 
         // tempCalendar.set(Calendar.MONTH, Calendar.MARCH); // set the month to the one you want, first param will stay the same, second is the month
 
         int maxWeeksInMonth = tempCalendar.getActualMaximum(tempCalendar.WEEK_OF_MONTH); // gets the max number of weeks in a month
         int maxDaysInWeek = tempCalendar.getActualMaximum(tempCalendar.DAY_OF_WEEK); // gets the max number of days in a week, which should be 7 regardless
 
         int firstDayInMonth = tempCalendar.get(tempCalendar.DAY_OF_WEEK); // gets the ordinal value of where the day falls in the week
         int maxDaysInMonth = tempCalendar.getActualMaximum(tempCalendar.DAY_OF_MONTH); // gets the max number of days in a month
 
         for (int weekOfMonth = 0, calendarSlot = 0, day = 1; weekOfMonth < 6; weekOfMonth++) { // weekOfMonth is also the row number
             for (int dayOfWeek = 0; dayOfWeek < maxDaysInWeek; dayOfWeek++) { // dayOfWeek is also the column number
 
                 if ((weekOfMonth == 0 && dayOfWeek + 1 >= firstDayInMonth) || // if in week 0, check to make sure you add only if at the proper point in the week
                         (weekOfMonth > 0 && day <= maxDaysInMonth)){ // or if week is not 0, make sure you don't go over the max number of days
 
                     /**
                      * This is the algorithm for determining whether a
                      * scheduleItem falls on a certain day. It goes through all
                      * the elements of say, a sub event list. If you want it to
                      * check just the main event, then you don't need the for
                      * loop.
                      */
                     ArrayList<SubEvent> events = new ArrayList<SubEvent>();
                      int tempMillis; // temporary value used in for loop that saves a time in milliseconds
                      int tempMonth;
                      int tempYear;
                      for (int i = 0; i < manager.getEventManager().getSelectedEvent().getSubEventList().size(); i++){
                       tempMillis = manager.getEventManager().getSelectedEvent().getSubEventList().get(i).getTimeSchedule().getStartDateTimeCalendar().get(Calendar.DAY_OF_MONTH); // set the date for the current element
                       tempMonth = manager.getEventManager().getSelectedEvent().getSubEventList().get(i).getTimeSchedule().getStartDateTimeCalendar().get(Calendar.MONTH);
                       tempYear = manager.getEventManager().getSelectedEvent().getSubEventList().get(i).getTimeSchedule().getStartDateTimeCalendar().get(Calendar.YEAR);
                       // if you want to check end time, use getEndTime() instead of getStartTime
                       if (tempMillis == day && tempMonth == tempCalendar.get(tempCalendar.MONTH) && tempYear == tempCalendar.get(tempCalendar.YEAR)){
                             events.add(manager.getEventManager().getSelectedEvent().getSubEventList().get(i));
                         tempCalendar.set(Calendar.DAY_OF_MONTH, tempCalendar.get(tempCalendar.DAY_OF_MONTH - 1)); // reset the day back to original
                       }
                     }
                      if (events.isEmpty())
                      {
                          events.add(new SubEvent());
                      }
                      
                      
                      CalendarEvent cEvent = new CalendarEvent(day, events);
                     tempCalendar.set(Calendar.DAY_OF_MONTH, tempCalendar.get(tempCalendar.DAY_OF_MONTH) + 1);
                     //String dayString = "" + day;
                     day++;
                     calendarTable.setValueAt(cEvent, weekOfMonth, dayOfWeek);
 
                 } else {
                     ArrayList<SubEvent> tempList = new ArrayList<SubEvent>();
                     tempList.add(new SubEvent());
                     calendarTable.setValueAt(new CalendarEvent(-1, tempList), weekOfMonth, dayOfWeek);
                 }
                 calendarSlot++;
             }
         }
         monthLabel.setText("" + getMonthString(month));
         yearLabel.setText("" + year);
     }
     
     public void populateSubEvents()
     {
         ArrayList<SubEvent> subEventList = manager.getEventManager().getSelectedEvent().getSubEventList();
        subEventList.add(new SubEvent(12, "This is an event"));
     }
     
     public String getMonthString(int month) {
     return new DateFormatSymbols().getMonths()[month];
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         calendarTableScrollPane = new javax.swing.JScrollPane();
         calendarTable = new javax.swing.JTable(){
             public boolean isCellEditable(int rowIndex, int collIndex) {
                 return false;
             }
         };
         detailsPanel = new javax.swing.JPanel();
         addEventButton = new javax.swing.JButton();
         monthLabel = new javax.swing.JLabel();
         lastMonthButton = new javax.swing.JButton();
         nextMonthButton = new javax.swing.JButton();
         jLabel2 = new javax.swing.JLabel();
         yearLabel = new javax.swing.JLabel();
         removeEventButton = new javax.swing.JButton();
         editSubEventButton = new javax.swing.JButton();
         nextYearButton = new javax.swing.JButton();
         lastYearButton = new javax.swing.JButton();
 
         setBackground(new java.awt.Color(128, 128, 128));
         setMaximumSize(new java.awt.Dimension(760, 520));
         setMinimumSize(new java.awt.Dimension(760, 520));
         setPreferredSize(new java.awt.Dimension(760, 520));
 
         calendarTableScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         calendarTableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
 
         calendarTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         calendarTable.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
         calendarTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null}
             },
             new String [] {
                 "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
         });
         calendarTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
         calendarTable.setFillsViewportHeight(true);
         calendarTable.setMaximumSize(new java.awt.Dimension(448, 384));
         calendarTable.setMinimumSize(new java.awt.Dimension(448, 384));
         calendarTable.setPreferredSize(new java.awt.Dimension(448, 384));
         calendarTableScrollPane.setViewportView(calendarTable);
         calendarTable.getColumnModel().getColumn(0).setMinWidth(48);
         calendarTable.getColumnModel().getColumn(1).setMinWidth(48);
         calendarTable.getColumnModel().getColumn(2).setMinWidth(48);
         calendarTable.getColumnModel().getColumn(3).setMinWidth(48);
         calendarTable.getColumnModel().getColumn(4).setMinWidth(48);
         calendarTable.getColumnModel().getColumn(5).setMinWidth(48);
         calendarTable.getColumnModel().getColumn(6).setMinWidth(48);
         calendarTable.getColumnModel().getColumn(6).setPreferredWidth(48);
         calendarTable.getColumnModel().getColumn(6).setMaxWidth(48);
 
         detailsPanel.setBackground(new java.awt.Color(255, 255, 255));
         detailsPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
         detailsPanel.setMaximumSize(new java.awt.Dimension(278, 384));
         detailsPanel.setMinimumSize(new java.awt.Dimension(278, 384));
         detailsPanel.setPreferredSize(new java.awt.Dimension(278, 384));
 
         javax.swing.GroupLayout detailsPanelLayout = new javax.swing.GroupLayout(detailsPanel);
         detailsPanel.setLayout(detailsPanelLayout);
         detailsPanelLayout.setHorizontalGroup(
             detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         detailsPanelLayout.setVerticalGroup(
             detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 380, Short.MAX_VALUE)
         );
 
         addEventButton.setText("Add Event");
         addEventButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addEventButtonActionPerformed(evt);
             }
         });
 
         monthLabel.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
         monthLabel.setForeground(new java.awt.Color(255, 255, 255));
         monthLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         monthLabel.setText("Month");
         monthLabel.setMaximumSize(new java.awt.Dimension(200, 50));
         monthLabel.setMinimumSize(new java.awt.Dimension(200, 50));
         monthLabel.setPreferredSize(new java.awt.Dimension(200, 50));
 
         lastMonthButton.setText("<");
         lastMonthButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 lastMonthButtonActionPerformed(evt);
             }
         });
 
         nextMonthButton.setText(">");
         nextMonthButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 nextMonthButtonActionPerformed(evt);
             }
         });
 
         jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
         jLabel2.setForeground(new java.awt.Color(255, 255, 255));
         jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel2.setText("Sub Event Details");
 
         yearLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         yearLabel.setForeground(new java.awt.Color(255, 255, 255));
         yearLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         yearLabel.setText("year");
         yearLabel.setMaximumSize(new java.awt.Dimension(100, 25));
         yearLabel.setMinimumSize(new java.awt.Dimension(100, 25));
         yearLabel.setPreferredSize(new java.awt.Dimension(100, 25));
 
         removeEventButton.setText("Remove Event");
         removeEventButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 removeEventButtonActionPerformed(evt);
             }
         });
 
         editSubEventButton.setText("Edit Event");
         editSubEventButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editSubEventButtonActionPerformed(evt);
             }
         });
 
         nextYearButton.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
         nextYearButton.setText(">>");
         nextYearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 nextYearButtonActionPerformed(evt);
             }
         });
 
         lastYearButton.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
         lastYearButton.setText("<<");
         lastYearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 lastYearButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(143, 143, 143)
                 .addComponent(lastYearButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(yearLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(7, 7, 7)
                 .addComponent(nextYearButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 203, Short.MAX_VALUE)
                 .addComponent(jLabel2)
                 .addGap(76, 76, 76))
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(calendarTableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(detailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(addEventButton)
                         .addGap(6, 6, 6)
                         .addComponent(removeEventButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(editSubEventButton)))
                 .addContainerGap())
             .addGroup(layout.createSequentialGroup()
                 .addGap(98, 98, 98)
                 .addComponent(lastMonthButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(monthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(nextMonthButton)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGap(16, 16, 16)
                         .addComponent(nextMonthButton))
                     .addComponent(monthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(layout.createSequentialGroup()
                         .addGap(15, 15, 15)
                         .addComponent(lastMonthButton)))
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(nextYearButton)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(lastYearButton)
                                 .addComponent(yearLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(11, 11, 11)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(calendarTableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                             .addComponent(detailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(editSubEventButton)
                                 .addComponent(removeEventButton))
                             .addComponent(addEventButton)))
                     .addGroup(layout.createSequentialGroup()
                         .addGap(2, 2, 2)
                         .addComponent(jLabel2)))
                 .addGap(9, 9, 9))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void lastMonthButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastMonthButtonActionPerformed
         if (month > 0)
             month--;
         else {
             year --;
             month = 11;
         }
         if(month == 0 && year == 2013)
         {
             lastMonthButton.setEnabled(false);
         }
         populateCalendar();
     }//GEN-LAST:event_lastMonthButtonActionPerformed
 
     private void nextMonthButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextMonthButtonActionPerformed
         if (month < 11)
             month++;
         else {
             year ++;
             month = 0;
         }
         lastMonthButton.setEnabled(true);
         populateCalendar();
     }//GEN-LAST:event_nextMonthButtonActionPerformed
 
     private void addEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEventButtonActionPerformed
         // TODO add your handling code here:
         NewSubEventDialog nsed;
         if (selectedSubEvent != null)
         {
             nsed = new NewSubEventDialog((JFrame)SwingUtilities.windowForComponent(this), selectedSubEvent, true);
         }
         else
         {
             SubEvent se = new SubEvent();
             TimeSchedule ts = new TimeSchedule();
             if(calendarTable.getSelectedRowCount() == 0 && calendarTable.getSelectedColumnCount() == 0)
             {
                 se.setTimeSchedule(ts);
             }
             else
             {
                 CalendarEvent ce = (CalendarEvent)calendarTable.getModel().getValueAt(calendarTable.getSelectedRow(), calendarTable.getSelectedColumn());
                 ts.setStartDateTime(year, month+1, ce.getDay(), 0, 0);
                 ts.setEndDateTime(year, month+1,ce.getDay() , 0, 0);
                 se.setTimeSchedule(ts);
             }
             nsed = new NewSubEventDialog((JFrame)SwingUtilities.windowForComponent(this), se, true);
         }
         nsed.setVisible(true);
         if(nsed.getConfirm())
         {
             try
             {
                 manager.getEventManager().createSubEvent(nsed.createEvent());
                 populateCalendar();
                 updateDetailsList((CalendarEvent) calendarTable.getValueAt(selectedRow, selectedColumn));
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }
         }
     }//GEN-LAST:event_addEventButtonActionPerformed
 
     private void removeEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeEventButtonActionPerformed
         if (selectedSubEvent != null) {
            int choice = JOptionPane.showConfirmDialog(null, "Do you want to remove " + selectedSubEvent.getDescription() + "?");
             if (choice == JOptionPane.YES_OPTION) {
                 try {
                     manager.getEventManager().deleteSubEvent(selectedSubEvent);
                 } catch (PrivilegeInsufficientException ex) {
                     Logger.getLogger(UserManagementPanel.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (DoesNotExistException ex) {
                     Logger.getLogger(UserManagementPanel.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 populateCalendar();
                 updateDetailsList((CalendarEvent) calendarTable.getValueAt(selectedRow, selectedColumn));
             }
         } else {
            JOptionPane.showMessageDialog(null, "Please select a Sub Event to remove first");
         }
     }//GEN-LAST:event_removeEventButtonActionPerformed
 
     private void editSubEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSubEventButtonActionPerformed
         if (selectedSubEvent != null) {
             EditSubEventDialog esed = new EditSubEventDialog((JFrame) SwingUtilities.windowForComponent(this), selectedSubEvent, true);
             esed.setVisible(true);
             if (esed.getConfirm()) {
                 try {
                     populateCalendar();
                     updateDetailsList((CalendarEvent) calendarTable.getValueAt(selectedRow, selectedColumn));
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
         else
            JOptionPane.showMessageDialog(null, "Please select a Sub Event to edit first");
     }//GEN-LAST:event_editSubEventButtonActionPerformed
 
     private void lastYearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastYearButtonActionPerformed
 
         
         year --;
         populateCalendar();
         if(year == 2013)
         {
             lastYearButton.setEnabled(false);
         }
     }//GEN-LAST:event_lastYearButtonActionPerformed
 
     private void nextYearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextYearButtonActionPerformed
         year ++;
         populateCalendar();
         if(year > 2013)
         {
             lastYearButton.setEnabled(true);
         }
     }//GEN-LAST:event_nextYearButtonActionPerformed
 
    public void hideSubEventButtons() {
         addEventButton.setVisible(false);
         removeEventButton.setVisible(false);
         editSubEventButton.setVisible(false);
     } 
     
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton addEventButton;
     private javax.swing.JTable calendarTable;
     private javax.swing.JScrollPane calendarTableScrollPane;
     private javax.swing.JPanel detailsPanel;
     private javax.swing.JButton editSubEventButton;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JButton lastMonthButton;
     private javax.swing.JButton lastYearButton;
     private javax.swing.JLabel monthLabel;
     private javax.swing.JButton nextMonthButton;
     private javax.swing.JButton nextYearButton;
     private javax.swing.JButton removeEventButton;
     private javax.swing.JLabel yearLabel;
     // End of variables declaration//GEN-END:variables
 
 
 private class TextTableRenderer extends JTextArea implements TableCellRenderer {
 public TextTableRenderer() {
 setOpaque(true);
 setLineWrap(true);
 setWrapStyleWord(true);
 }
 
     public Component getTableCellRendererComponent(JTable table,
             Object value, boolean isSelected, boolean hasFocus, int row,
             int column) {
 
         if (isSelected) {
             setForeground(Color.BLACK);
             setBackground(table.getSelectionBackground());
         } else {
             if (value.toString().split("\n").length != 1) {
                 setBackground(Color.GREEN);
             } else if (value.toString().equals("")) {
                 setBackground(Color.LIGHT_GRAY);
             } else {
                 setForeground(Color.BLACK);
                 setBackground(table.getBackground());
             }
         }
 
             setText((value == null)
                     ? ""
                     : value.toString());
             return this;
         }
     }
 
 class DetailsListSelectionListener implements ListSelectionListener {
     public void valueChanged(ListSelectionEvent e) {
         selectedSubEvent = (SubEvent)detailsList.getSelectedValue();
     }
     }
 
     class listCellRenderer extends DefaultListCellRenderer {
 
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
             SubEvent v = (SubEvent)value;
       String text = "<html><p>" + v.getTitle() + "</p></html>";
         return super.getListCellRendererComponent(detailsList, text, index, isSelected, cellHasFocus);
         }
     }
 }
