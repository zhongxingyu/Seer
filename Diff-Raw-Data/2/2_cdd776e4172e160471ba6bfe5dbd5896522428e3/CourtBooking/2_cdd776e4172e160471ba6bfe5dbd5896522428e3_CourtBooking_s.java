 package GUI;
 
 import BE.Reservation;
 import BLL.BookingManager;
 import BLL.MemberManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  *
  * @author Chris
  */
 public class CourtBooking extends javax.swing.JFrame
 {
     private static CourtBooking instance = null;
     private DefaultListModel courtModel = new DefaultListModel();
     private DefaultListModel model = new DefaultListModel();
     private DefaultComboBoxModel timeModel = new DefaultComboBoxModel();
     private int age;
 
     /**
      * Constructor for the courtbooking class
      */
     private CourtBooking()
     {
         timeModel.removeAllElements();
         initComponents();
         setLocationRelativeTo(null);
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         checkBirth();
         timeList();
         setTitle("Banebooking");
         thisMonth();
         thisDay();
 
         splMonth.addListSelectionListener(
                 new ListSelectionListener()
         {
             @Override
             public void valueChanged(ListSelectionEvent lse)
             {
                 if (!(lse.getValueIsAdjusting() || splMonth.isSelectionEmpty()))
                 {
                     dayList();
                 }
             }
         });
 
         splDate.addListSelectionListener(
                 new ListSelectionListener()
         {
             @Override
             public void valueChanged(ListSelectionEvent lse)
             {
                 checkCourts();
             }
         });
 
         cmbxTime.addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 checkCourts();
             }
         });
         thisTime();
     }
 
     /*
      * Finds the currently selected month, used for selecting the right daylist
      */
     private void thisMonth()
     {
         //4 is when the summer season starts, 9 is when it ends
         for (int i = 4; i <= 9; i++)
         {
             if (Calendar.getInstance().get(Calendar.MONTH) + 1 == i)
             {
                 splMonth.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH) - 3);
                 dayList();
             }
         }
     }
 
     /*
      * Finds the selected day and shows when a user can book
      */
     private void thisDay()
     {
         if (age < 18)
         {
             if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 16)
             {
                 splDate.setSelectedIndex(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
             }
             else
             {
                 splDate.setSelectedIndex(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1);
             }
         }
         if (age > 60)
         {
             if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 13)
             {
                 splDate.setSelectedIndex(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
             }
             else
             {
                 splDate.setSelectedIndex(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1);
             }
         }
         if (age >= 18 && age < 60)
         {
             if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 21)
             {
                 splDate.setSelectedIndex(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
             }
             else
             {
                 splDate.setSelectedIndex(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1);
             }
         }
         splDate.ensureIndexIsVisible(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
     }
 
     /*
      * Finds the currently selected time
      */
     private void thisTime()
     {
         if (age < 18)
         {
             if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 16 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 7)
             {
                 cmbxTime.setSelectedIndex(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - 6);
             }
             else
             {
                 cmbxTime.setSelectedIndex(0);
             }
         }
         if (age >= 60)
         {
             if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 13 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 7)
             {
                 cmbxTime.setSelectedIndex(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - 6);
             }
             else
             {
                 cmbxTime.setSelectedIndex(0);
             }
         }
         if (age >= 18 && age < 60)
         {
             if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 21 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 7)
             {
                 cmbxTime.setSelectedIndex(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - 6);
             }
             else
             {
                 cmbxTime.setSelectedIndex(0);
             }
         }
     }
 
     /**
      * Conversion of the court booking class to a singleton
      * @return An instance of the court booking class
      */
     public static CourtBooking getInstance()
     {
         if (instance == null)
         {
             instance = new CourtBooking();
         }
         return instance;
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents()
     {
 
         lblHeader = new javax.swing.JLabel();
         cmbxTime = new javax.swing.JComboBox();
         lblTime = new javax.swing.JLabel();
         btnAddBooking = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
         spMonth = new javax.swing.JScrollPane();
         splMonth = new javax.swing.JList();
         lblMonth = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         spDay = new javax.swing.JScrollPane();
         splDate = new javax.swing.JList();
         spCourt = new javax.swing.JScrollPane();
         splCourt = new javax.swing.JList();
         lblCourt = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setResizable(false);
 
         lblHeader.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblHeader.setText("Bookning af Baner");
 
         cmbxTime.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 cmbxTimeActionPerformed(evt);
             }
         });
 
         lblTime.setText("Tidsrum");
 
         btnAddBooking.setText("Tilføj booking");
         btnAddBooking.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 btnAddBookingActionPerformed(evt);
             }
         });
 
         btnCancel.setText("Annuller");
         btnCancel.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 btnCancelActionPerformed(evt);
             }
         });
 
         splMonth.setModel(new javax.swing.AbstractListModel()
         {
             String[] strings = { "4", "5", "6", "7", "8", "9" };
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         spMonth.setViewportView(splMonth);
 
         lblMonth.setText("Måned");
 
         jLabel1.setText("Dag");
 
         spDay.setViewportView(splDate);
 
         spCourt.setViewportView(splCourt);
 
         lblCourt.setText("Bane");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblMonth)
                     .addComponent(spMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(spDay, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1))
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(spCourt, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(lblCourt)
                         .addGap(0, 125, Short.MAX_VALUE)))
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(btnAddBooking)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnCancel))
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                         .addComponent(cmbxTime, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(lblTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addGap(20, 20, 20))
             .addComponent(lblHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {spDay, spMonth});
 
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblHeader)
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblTime)
                     .addComponent(lblMonth)
                     .addComponent(jLabel1)
                     .addComponent(lblCourt))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(cmbxTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(33, 33, 33)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(btnAddBooking)
                             .addComponent(btnCancel)))
                     .addComponent(spMonth)
                     .addComponent(spCourt)
                     .addComponent(spDay, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void cmbxTimeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmbxTimeActionPerformed
     {//GEN-HEADEREND:event_cmbxTimeActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_cmbxTimeActionPerformed
 
     private void btnCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnCancelActionPerformed
     {//GEN-HEADEREND:event_btnCancelActionPerformed
         dispose();
         MainMenu.getInstance().setVisible(true);
     }//GEN-LAST:event_btnCancelActionPerformed
 
     private void btnAddBookingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnAddBookingActionPerformed
     {//GEN-HEADEREND:event_btnAddBookingActionPerformed
         addBooking();
         MainMenu.getInstance().setVisible(true);
     }//GEN-LAST:event_btnAddBookingActionPerformed
 
     /*
      * Specifies the number of days in a given month 
      */
     private void dayList()
     {
         int month = new Scanner(splMonth.getSelectedValue().toString()).nextInt();
 
         if (month == 5 || month == 7 || month == 8)
         {
             for (int i = 1; i <= 31; i++)
             {
                 model.addElement(i);
             }
             splDate.setModel(model);
         }
 
         if (month == 2)
         {
             for (int i = 1; i <= 28; i++)
             {
                 model.addElement(i);
             }
             splDate.setModel(model);
         }
 
         if (month == 4 || month == 6 || month == 9)
         {
             for (int i = 1; i <= 30; i++)
             {
                 model.addElement(i);
             }
             splDate.setModel(model);
         }
     }
 
     /*
      * A list of when a user can book depending on the age of the user
      */
     private void timeList()
     {
 
         //7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21
         if (age < 18)
         {
             for (int i = 7; i <= 16; i++)
             {
                 timeModel.addElement(i);
             }
             cmbxTime.setModel(timeModel);
         }
 
         if (age >= 60)
         {
             for (int i = 7; i <= 13; i++)
             {
                 timeModel.addElement(i);
             }
             cmbxTime.setModel(timeModel);
         }
 
         if (age >= 18 && age < 60)
         {
             for (int i = 7; i <= 21; i++)
             {
                 timeModel.addElement(i);
             }
             cmbxTime.setModel(timeModel);
         }
     }
 
     /*
      * Adds a booking of a court in the database
      */
     private void addBooking()
     {
         if (splMonth.getSelectedValue() != null && splDate.getSelectedValue() != null && splCourt.getSelectedValue() != null)
         {
             Calendar booking = new GregorianCalendar();
 
             int year = Calendar.getInstance().get(Calendar.YEAR);
             int month = new Scanner(splMonth.getSelectedValue().toString()).nextInt() - 1;
             int date = new Scanner(splDate.getSelectedValue().toString()).nextInt();
             int time = Integer.parseInt(cmbxTime.getSelectedItem().toString());
 
             booking.set(year, month, date, time, 0, 0);
 
             try
             {
                 ArrayList ids = MemberManager.getInstance().getIds();
                 int memberId = MemberManager.getInstance().getLoggedIn();
 
                 if (JOptionPane.showConfirmDialog(null, "Den valgte dato: " + booking.getTime() + ". Vil du bestille denne tid?", "Reservation",
                         JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                         == JOptionPane.YES_OPTION)
                 {
                     String court = splCourt.getSelectedValue().toString();
                     int courtId = BookingManager.getInstance().getIdByName(court);
 
                     Reservation r = new Reservation(courtId, memberId, booking);
                     BookingManager.getInstance().reserveCourt(r);
 
                     booking.clear();
                 }
             }
             catch (Exception e)
             {
                 System.out.println("ERROR - " + e);
             }
         }
         else
         {
             JOptionPane.showMessageDialog(null, "Dato, bane eller medlem ikke valgt!", "Advarsel", JOptionPane.INFORMATION_MESSAGE);
         }
         courtModel.clear();
     }
 
     /*
      * Finds the age of a user
      */
     private void checkBirth()
     {
         try
         {
             int memberId = MemberManager.getInstance().getLoggedIn();
             Calendar mbday = MemberManager.getInstance().getMembersBDayByID(memberId);
             Date now = new Date();
             int nowMonth = now.getMonth() + 1;
             int nowYear = now.getYear() + 1900;
             int month = mbday.getTime().getMonth() + 1;
             age = nowYear - (mbday.getTime().getYear() + 1900);
 
             if (month > nowMonth)
             {
                 age--;
             }
             else if (month == nowMonth)
             {
                 int nowDay = now.getDate();
 
                 if (mbday.getTime().getDate() > nowDay)
                 {
                     age--;
                 }
             }
         }
         catch (SQLException e)
         {
             System.out.println("ERROR - " + e.getMessage());
         }
     }
 
     /*
      * Checks if a court is occupied
      */
     private void checkCourts()
     {
         if (splMonth.getSelectedValue() != null && splDate.getSelectedValue() != null)
         {
             courtModel.clear();
             Calendar booking = new GregorianCalendar();
 
             int year = Calendar.getInstance().get(Calendar.YEAR);
             int month = new Scanner(splMonth.getSelectedValue().toString()).nextInt() - 1;
             int date = new Scanner(splDate.getSelectedValue().toString()).nextInt();
             int time = Integer.parseInt(cmbxTime.getSelectedItem().toString());
             int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
             int currentDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
             int currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
             booking.set(year, month, date, time, 0, 0);
 
             if (month > currentMonth || (month == currentMonth && date > currentDate) || (month == currentMonth && date == currentDate && time > currentTime))
             {
 
                 try
                 {
                     ArrayList<Reservation> rs = BookingManager.getInstance().getReservations();
                     Reservation r = new Reservation(booking);
                     ArrayList<String> courtNames = BookingManager.getInstance().getCourtsName();
 
                     for (int i = 0; i < rs.size(); i++)
                     {
 
                         r.getReservationTime().clear(Calendar.MILLISECOND);
                         rs.get(i).getReservationTime().clear(Calendar.MILLISECOND);
                     }
 
                     if (!rs.contains(r))
                     {
                         for (String c : courtNames)
                         {
                             courtModel.addElement(c);
                         }
                     }
                     else
                     {
                         for (String c : courtNames)
                         {
                             courtModel.addElement(c);
                         }
 
                         for (int i = 0; i < rs.size(); i++)
                         {
                             if (r.getReservationTime().getTime().equals(rs.get(i).getReservationTime().getTime()))
                             {
                                 String courtName = BookingManager.getInstance().getNameById(rs.get(i).getCourtId());
                             }
                         }
                     }
                     splCourt.setModel(courtModel);
                 }
                 catch (Exception e)
                 {
                     System.out.println("ERROR - " + e.getMessage());
                 }
             }
             else
             {
                 JOptionPane.showMessageDialog(null, "Den valgte dato er ikke tilgængelig for booking.", "Advarsel", JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[])
     {
         /*
          * Set system look and feel
          */
         try
         {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (Exception e)
         {
             //Do nothing
         }
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable()
         {
             public void run()
             {
                 new CourtBooking().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAddBooking;
     private javax.swing.JButton btnCancel;
     private javax.swing.JComboBox cmbxTime;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel lblCourt;
     private javax.swing.JLabel lblHeader;
     private javax.swing.JLabel lblMonth;
     private javax.swing.JLabel lblTime;
     private javax.swing.JScrollPane spCourt;
     private javax.swing.JScrollPane spDay;
     private javax.swing.JScrollPane spMonth;
     private javax.swing.JList splCourt;
     private javax.swing.JList splDate;
     private javax.swing.JList splMonth;
     // End of variables declaration//GEN-END:variables
 }
