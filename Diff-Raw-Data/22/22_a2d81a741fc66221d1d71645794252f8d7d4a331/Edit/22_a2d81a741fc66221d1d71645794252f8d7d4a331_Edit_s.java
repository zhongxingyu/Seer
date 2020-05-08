 package org.vikenpedia.fellesprosjekt.client.ui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 import org.vikenpedia.fellesprosjekt.shared.models.Meeting;
 import org.vikenpedia.fellesprosjekt.shared.models.MeetingParticipant;
 
 /**
  * Edit Meeting window
  * 
  * @author CVi
  * 
  */
 public class Edit extends TblModel implements ActionListener {
 
     /**
      * Listener for Edit window.
      * 
      * @author CVi
      * 
      */
     public interface EditListener {
         /**
          * Save action
          * 
          * @param m
          *            edited meeting instance
          */
         public void save(Meeting m);
 
         /**
          * Delete action
          * 
          * @param m
          *            meeting instance (probably unedited because it won't
          *            matter anyway)
          */
         public void delete(Meeting m);
 
         /**
          * Book room action (initialize room booking interface)
          * 
          * @param m
          *            Meeting instance (probably unedited because it won't
          *            matter anyway)
          */
         public void book(Meeting m);
 
         /**
          * Invite action (initialize invite more users interface)
          * 
          * @param m
          *            Meeting instance (probably unedited because it won't
          *            matter anyway)
          */
         public void invite(Meeting m);
     }
 
     public JFrame frame;
     private JTextField textName;
     private JTextField textBegin;
     private JTextField textEnd;
     private JTextField textPlace;
     private JTextField textAlarm;
     private JTextField textDate;
     private JTable table;
     private JButton btnSave;
     private JButton btnDelete;
     private ArrayList<MeetingParticipant> participants;
     private JTextField textRoom;
     private JButton btnInvite;
     private JButton btnBook;
     private EditListener el;
     private Meeting m;
 
     /**
      * Create the application.
      * 
      * @param el
      *            EditListener to trigger to.
      * @param m
      *            Meeting to edit
      */
     public Edit(EditListener el, Meeting m) {
         super();
         this.el = el;
         this.m = m;
         participants = new ArrayList<MeetingParticipant>();
         initialize();
     }
 
     /**
      * Initialize the contents of the frame.
      */
     private void initialize() {
         frame = new JFrame();
         frame.setBounds(100, 100, 450, 324);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         SpringLayout springLayout = new SpringLayout();
         frame.getContentPane().setLayout(springLayout);
 
         JPanel left = new JPanel();
         springLayout.putConstraint(SpringLayout.NORTH, left, 0, SpringLayout.NORTH,
                 frame.getContentPane());
         springLayout.putConstraint(SpringLayout.WEST, left, 0, SpringLayout.WEST,
                 frame.getContentPane());
         springLayout.putConstraint(SpringLayout.SOUTH, left, 0, SpringLayout.SOUTH,
                 frame.getContentPane());
         springLayout.putConstraint(SpringLayout.EAST, left, 205, SpringLayout.WEST,
                 frame.getContentPane());
         frame.getContentPane().add(left);
 
         JPanel right = new JPanel();
         springLayout.putConstraint(SpringLayout.NORTH, right, 0, SpringLayout.NORTH,
                 frame.getContentPane());
         springLayout.putConstraint(SpringLayout.WEST, right, 0, SpringLayout.EAST, left);
         springLayout.putConstraint(SpringLayout.SOUTH, right, 0, SpringLayout.SOUTH,
                 frame.getContentPane());
         springLayout.putConstraint(SpringLayout.EAST, right, 0, SpringLayout.EAST,
                 frame.getContentPane());
         frame.getContentPane().add(right);
         SpringLayout sl_left = new SpringLayout();
         left.setLayout(sl_left);
 
         JLabel lblEdit = new JLabel("Endre avtale");
         sl_left.putConstraint(SpringLayout.NORTH, lblEdit, 10, SpringLayout.NORTH, left);
         sl_left.putConstraint(SpringLayout.WEST, lblEdit, 10, SpringLayout.WEST, left);
         left.add(lblEdit);
 
         JLabel lblName = new JLabel("Navn:");
         sl_left.putConstraint(SpringLayout.WEST, lblName, 10, SpringLayout.WEST, left);
         left.add(lblName);
 
         textName = new JTextField();
         textName.setEnabled(false);
         sl_left.putConstraint(SpringLayout.NORTH, lblName, 4, SpringLayout.NORTH, textName);
         sl_left.putConstraint(SpringLayout.NORTH, textName, 24, SpringLayout.NORTH, left);
         sl_left.putConstraint(SpringLayout.WEST, textName, 65, SpringLayout.WEST, left);
         left.add(textName);
         textName.setColumns(10);
 
         JLabel lblDate = new JLabel("Dato:");
         sl_left.putConstraint(SpringLayout.WEST, lblDate, 10, SpringLayout.WEST, left);
         left.add(lblDate);
 
         textDate = new JTextField();
         textDate.setEnabled(false);
         sl_left.putConstraint(SpringLayout.WEST, textDate, 0, SpringLayout.WEST, textName);
         sl_left.putConstraint(SpringLayout.NORTH, lblDate, 4, SpringLayout.NORTH, textDate);
         sl_left.putConstraint(SpringLayout.NORTH, textDate, 5, SpringLayout.SOUTH, textName);
         left.add(textDate);
         textDate.setColumns(10);
 
         JLabel lblAlarm = new JLabel("Alarm:");
         sl_left.putConstraint(SpringLayout.WEST, lblAlarm, 10, SpringLayout.WEST, left);
         left.add(lblAlarm);
 
         textAlarm = new JTextField();
         sl_left.putConstraint(SpringLayout.NORTH, textAlarm, 5, SpringLayout.SOUTH, textDate);
         sl_left.putConstraint(SpringLayout.WEST, textAlarm, 0, SpringLayout.WEST, textDate);
         sl_left.putConstraint(SpringLayout.NORTH, lblAlarm, 4, SpringLayout.NORTH, textAlarm);
         left.add(textAlarm);
         textAlarm.setColumns(10);
 
         JLabel lblBegin = new JLabel("Start:");
         sl_left.putConstraint(SpringLayout.WEST, lblBegin, 10, SpringLayout.WEST, left);
         left.add(lblBegin);
 
         textBegin = new JTextField();
         textBegin.setEnabled(false);
         sl_left.putConstraint(SpringLayout.NORTH, lblBegin, 4, SpringLayout.NORTH, textBegin);
         sl_left.putConstraint(SpringLayout.NORTH, textBegin, 5, SpringLayout.SOUTH, textAlarm);
         sl_left.putConstraint(SpringLayout.WEST, textBegin, 0, SpringLayout.WEST, textAlarm);
         left.add(textBegin);
         textBegin.setColumns(10);
 
         JLabel lblEnd = new JLabel("Slutt:");
         sl_left.putConstraint(SpringLayout.WEST, lblEnd, 10, SpringLayout.WEST, left);
         left.add(lblEnd);
 
         textEnd = new JTextField();
         textEnd.setEnabled(false);
         sl_left.putConstraint(SpringLayout.NORTH, lblEnd, 4, SpringLayout.NORTH, textEnd);
         sl_left.putConstraint(SpringLayout.NORTH, textEnd, 5, SpringLayout.SOUTH, textBegin);
         sl_left.putConstraint(SpringLayout.WEST, textEnd, 0, SpringLayout.WEST, textBegin);
         left.add(textEnd);
         textEnd.setColumns(10);
 
         JLabel lblPlace = new JLabel("Sted:");
         sl_left.putConstraint(SpringLayout.WEST, lblPlace, 10, SpringLayout.WEST, left);
         left.add(lblPlace);
 
         textPlace = new JTextField();
         textPlace.setEnabled(false);
         sl_left.putConstraint(SpringLayout.NORTH, lblPlace, 4, SpringLayout.NORTH, textPlace);
         sl_left.putConstraint(SpringLayout.NORTH, textPlace, 5, SpringLayout.SOUTH, textEnd);
         sl_left.putConstraint(SpringLayout.WEST, textPlace, 0, SpringLayout.WEST, textEnd);
         left.add(textPlace);
         textPlace.setColumns(10);
 
         JLabel lblRoom = new JLabel("Rom:");
         sl_left.putConstraint(SpringLayout.WEST, lblRoom, 10, SpringLayout.WEST, left);
         left.add(lblRoom);
 
         textRoom = new JTextField();
         textRoom.setEditable(false);
         sl_left.putConstraint(SpringLayout.NORTH, lblRoom, 4, SpringLayout.NORTH, textRoom);
         sl_left.putConstraint(SpringLayout.NORTH, textRoom, 10, SpringLayout.SOUTH, textPlace);
         sl_left.putConstraint(SpringLayout.WEST, textRoom, 0, SpringLayout.WEST, textName);
         left.add(textRoom);
         textRoom.setColumns(10);
         SpringLayout sl_right = new SpringLayout();
         right.setLayout(sl_right);
 
         btnSave = new JButton("Lagre");
         sl_left.putConstraint(SpringLayout.WEST, btnSave, 10, SpringLayout.WEST, left);
         sl_left.putConstraint(SpringLayout.SOUTH, btnSave, -10, SpringLayout.SOUTH, left);
         left.add(btnSave);
 
         btnDelete = new JButton("Slett");
         sl_left.putConstraint(SpringLayout.SOUTH, btnDelete, -10, SpringLayout.SOUTH, left);
         sl_left.putConstraint(SpringLayout.EAST, btnDelete, -10, SpringLayout.EAST, left);
         left.add(btnDelete);
 
         JLabel lblSvarstatus = new JLabel("Svarstatus");
         sl_right.putConstraint(SpringLayout.NORTH, lblSvarstatus, 5, SpringLayout.NORTH, right);
         sl_right.putConstraint(SpringLayout.WEST, lblSvarstatus, 12, SpringLayout.WEST, right);
         lblSvarstatus.setBounds(271, 6, 76, 16);
         right.add(lblSvarstatus);
 
         table = new JTable(this);
         table.setRowSelectionAllowed(false);
         table.setBounds(271, 28, 161, 185);
 
         JScrollPane scrollPane = new JScrollPane(table);
         sl_right.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH,
                 lblSvarstatus);
         sl_right.putConstraint(SpringLayout.WEST, scrollPane, 5, SpringLayout.WEST, right);
         sl_right.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, right);
         right.add(scrollPane);
 
         btnInvite = new JButton("Inviter andre");
         btnInvite.setEnabled(false);
         sl_right.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.NORTH, btnInvite);
         sl_right.putConstraint(SpringLayout.SOUTH, btnInvite, 0, SpringLayout.SOUTH, right);
         sl_right.putConstraint(SpringLayout.EAST, btnInvite, 0, SpringLayout.EAST, right);
         right.add(btnInvite);
 
         btnBook = new JButton("Book rom");
         btnBook.setEnabled(false);
         sl_right.putConstraint(SpringLayout.WEST, btnBook, 0, SpringLayout.WEST, right);
         sl_right.putConstraint(SpringLayout.SOUTH, btnBook, 0, SpringLayout.SOUTH, right);
         right.add(btnBook);
         fill();
        isChairman();
     }
 
     /**
      * If the user is the Chairman, we need to re-enable the disabled fields and
      * buttons.
      */
     private void isChairman() {
         textName.setEnabled(true);
         textBegin.setEnabled(true);
         textEnd.setEnabled(true);
         textPlace.setEnabled(true);
         textDate.setEnabled(true);
         btnInvite.setEnabled(true);
         btnBook.setEnabled(true);
     }
 
     /**
      * Fill inn data to fields.
      */
     private void fill() {
         SimpleDateFormat sdDay = new SimpleDateFormat("yyyy-MM-dd");
         SimpleDateFormat sdTime = new SimpleDateFormat("HH:mm");
         textName.setText(m.getDescription());
         textBegin.setText(sdTime.format(m.getStartTime()));
         textEnd.setText(sdTime.format(m.getEndTime()));
         textPlace.setText(m.getPlace());
         textDate.setText(sdDay.format(m.getStartTime()));
     }
 
     /**
      * Extract the data from the fields.
      */
     private void reFill() {
         SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm");
         try {
             m.setStartTime(new Timestamp(sd.parse(textDate.getText() + " " + textBegin.getText())
                     .getTime()));
             m.setEndTime(new Timestamp(sd.parse(textDate.getText() + " " + textEnd.getText())
                     .getTime()));
             m.setDescription(textName.getText());
             m.setPlace(textPlace.getText());
         } catch (ParseException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public int getColumnCount() {
         return 2;
     }
 
     @Override
     public String getColumnName(int arg0) {
         String r = "";
         switch (arg0) {
         case 0:
             r = "Navn";
             break;
         case 1:
             r = "Status";
             break;
         }
         return r;
     }
 
     @Override
     public int getRowCount() {
         return participants.size();
     }
 
     @Override
     public Object getValueAt(int arg0, int arg1) {
         MeetingParticipant p;
         try {
             p = participants.get(arg0);
         } catch (IndexOutOfBoundsException n) {
             return null;
         }
         switch (arg1) {
         case 0:
             return p.getUserId();
         case 1:
             return p.getReplyStatus();
         }
         return null;
     }
 
     @Override
     public void actionPerformed(ActionEvent arg0) {
         if (arg0.getSource() == this.btnSave) {
             reFill();
             el.save(m);
         } else if (arg0.getSource() == this.btnDelete) {
             el.delete(m);
         } else if (arg0.getSource() == this.btnInvite) {
             el.invite(m);
         } else if (arg0.getSource() == this.btnBook) {
             el.book(m);
         }
     }
 
     /**
      * Adds a participant to the list
      * 
      * @param m
      *            MeetingParticipant to add
      */
     public void addParticipant(MeetingParticipant m) {
         participants.add(m);
     }
 
     /**
      * Sets the list to the provided Collection of participants
      * 
      * @param ms
      *            Collection of MeetingParticipants
      */
     public void addParticipants(Collection<MeetingParticipant> ms) {
         participants = new ArrayList<MeetingParticipant>(ms);
     }
 
     /**
      * Removes all the participants from the list
      */
     public void clearParticipants() {
         participants = new ArrayList<MeetingParticipant>();
     }
 }
