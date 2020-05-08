 package talk.client;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import talk.common.*;
 import java.util.*;
 import javax.swing.event.*;
 
 public class ContactsList extends JList {
   private ContactsPanel contactsPanel;
   ContactsList(DefaultListModel contactsListModel, ContactsPanel contactsPanel) {
     super(contactsListModel);
     this.contactsPanel = contactsPanel;
     //this.addListSelectionListener(new ConatacsListActionListener());
     this.addMouseListener(new ConatacsListMouseAdapter());
   }
 
   // public void actionPerformed(ActionEvent e) {
   //   Object o = e.getSource();
   //   System.out.println(o);
   // }
 
 
   // class ConatacsListActionListener implements ListSelectionListener {
 
   //   public void valueChanged(ListSelectionEvent e) {
   //     if(e.getValueIsAdjusting()) return;
   //     ContactsList cl = (ContactsList) e.getSource();
   //     System.out.println(cl.getSelectedIndex());
 
   //   }
   // }
 
   class ConatacsListMouseAdapter implements MouseListener {
 
     public void mouseClicked(MouseEvent e) {
       ContactsList cl = (ContactsList) e.getSource();
       if (e.getClickCount() == 2) {
         int index = cl.locationToIndex(e.getPoint());
        String us = (String)cl.getModel().getElementAt(index);
        System.out.println("Double clicked on Item " + index + ": us:" + us);
        String[] uname = us.split(":");
        contactsPanel.clientGUI.findOrCreateTab(uname[uname.length-1]);
       }
     }
 
     public void mousePressed(MouseEvent e) {
 
     }
 
     public void mouseReleased(MouseEvent e) {
 
     }
 
     public void mouseEntered(MouseEvent e) {
 
     }
 
     public void mouseExited(MouseEvent e) {
 
     }
 
   }
 
 }
 
 
 
  // final JList list = new JList(dataModel);
  // MouseListener mouseListener = new MouseAdapter() {
  //     public void mouseClicked(MouseEvent e) {
  //         if (e.getClickCount() == 2) {
  //             int index = list.locationToIndex(e.getPoint());
  //             System.out.println("Double clicked on Item " + index);
  //          }
  //     }
  // };
  // list.addMouseListener(mouseListener);
