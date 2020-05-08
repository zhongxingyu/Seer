 /*  mainTable.java - The JTable for the main-window
  *  Copyright (C) 1999 Fredrik Ehnbom
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.gjt.fredde.yamm.gui.main;
 
 import javax.swing.*;
 import javax.swing.table.*;
 import java.awt.event.*;
 import java.awt.*;
 import java.awt.dnd.*;
 import java.awt.datatransfer.StringSelection;
 import java.util.*;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.MalformedURLException;
 import org.gjt.fredde.yamm.YAMM;
 import org.gjt.fredde.yamm.YAMMWrite;
 import org.gjt.fredde.yamm.mail.Mailbox;
 import org.gjt.fredde.util.gui.MsgDialog;
 
 /**
  * The Table for listing the mails subject, date and sender.
  */
 public class mainTable extends JTable implements DragGestureListener,
                                                  DragSourceListener {
 
   /** If it should sort 1 to 10 or 10 to 1*/
   static protected boolean                firstSort    = true;
 
   /** Which column that was sorted */
   static protected int                    sortedCol    = 0;
 
   /** The list of mails */
   static protected Vector		  listOfMails = null;
 
   static protected YAMM                   frame = null;
 
   public JPopupMenu             popup = null;
 
   protected static DragSource drag = null;
 
   /**
    * Creates a new JTable
    * @param tm The TableModel to use
    * @param listOfMails the Maillist vector
    */
   public mainTable(YAMM frame, TableModel tm, Vector listOfMails) {
 
     drag = DragSource.getDefaultDragSource();
 
     drag.createDefaultDragGestureRecognizer(
       this, // drag component
       DnDConstants.ACTION_MOVE, // actions
       this); // drag gesture listener
 
 
     this.listOfMails = listOfMails;
     this.frame = frame;
     setModel(tm);
 
     TableColumn column = getColumnModel().getColumn(0);
     column.setIdentifier("num");
     column.setMinWidth(5);
     column.setMaxWidth(1024);
     column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("num.width", "20")));
 
     column = getColumnModel().getColumn(1);
     column.setIdentifier("subject");
     column.setMinWidth(5);
     column.setMaxWidth(1024);
     column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("subject.width", "200")));
 
     column = getColumnModel().getColumn(2);
     column.setIdentifier("from");
     column.setMinWidth(5);
     column.setMaxWidth(1024);
     column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("from.width", "200")));
 
     column = getColumnModel().getColumn(3);
     column.setIdentifier("date");
     column.setMinWidth(5);
     column.setMaxWidth(1024);
     column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("date.width", "125")));
 
 
     setRowHeight(12);
     setSelectionMode(2);
     setColumnSelectionAllowed(false);
     setShowHorizontalLines(false);
     setShowVerticalLines(false);
     setIntercellSpacing(new Dimension(0, 0));
     myRenderer rend = new myRenderer();
     rend.setFont(new Font("SansSerif", Font.PLAIN, 12));
     setDefaultRenderer(getColumnClass(0), rend /* new myRenderer() */ );
 
     TMListener(this);
 
     addMouseListener(mouseListener);
 
     registerKeyboardAction(keyListener, "Del",
                             KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), 0);
 
     popup = new JPopupMenu();
     popup.setInvoker(this);
 
     createPopup(popup);
   }
 
   public void save() {
     TableColumn c = getColumnModel().getColumn(0);
     YAMM.setProperty(c.getIdentifier().toString() + ".width", c.getWidth()+"");
     c = getColumnModel().getColumn(1);
     YAMM.setProperty(c.getIdentifier().toString() + ".width", c.getWidth()+"");
     c = getColumnModel().getColumn(2);
     YAMM.setProperty(c.getIdentifier().toString() + ".width", c.getWidth()+"");
     c = getColumnModel().getColumn(3);
     YAMM.setProperty(c.getIdentifier().toString() + ".width", c.getWidth()+"");
   }
 
   protected String getSelected() {
     String selected = "";
 
     int i = 0;
       
     while(i<4) {
       if(getColumnName(i).equals("#")) { break; }
       i++;     
     }    
 
     int[] mlist = getSelectedRows();
     int[] dragList = new int[mlist.length];
 
     for(int j = 0;j < mlist.length;j++) {
       dragList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
     }
 
     Arrays.sort(dragList);
     for(int j = 0; j < dragList.length; j++) {
       selected += dragList[j] + " ";
     }
 
     return selected;
   }
 
   public void dragGestureRecognized(DragGestureEvent e) {
     System.out.println("wanted to drag...");
     StringSelection text = new StringSelection(getSelected());
 
     try {
       drag.startDrag(e, DragSource.DefaultMoveDrop, // cursor
                   text, // transferable
                   this); // drag source listener
     } catch (InvalidDnDOperationException idoe) {
         System.err.println(idoe);
     }
   }
 
   public void dragDropEnd(DragSourceDropEvent e) { System.out.println("end"); }
   public void dragEnter(DragSourceDragEvent e) { System.out.println("enter"); }
   public void dragExit(DragSourceEvent e) { System.out.println("exit"); }
   public void dragOver(DragSourceDragEvent e) { System.out.println("over"); }
   public void dropActionChanged(DragSourceDragEvent e) { System.out.println("changed"); }
 
 
   /**
    * Sorts 1 -> 10
    */
   protected void SortFirst(int col) {
 
     if (col == 0) {
       for (int i = 0; i < listOfMails.size(); i++) {
         Object temp = null;
         for (int j = 0; j < listOfMails.size(); j++) {
           int one = Integer.parseInt(
               ((Vector)listOfMails.elementAt(i)).elementAt(col).toString());
 
           int two = Integer.parseInt(
               ((Vector)listOfMails.elementAt(j)).elementAt(col).toString());
 
           if (one < two) {
             temp = listOfMails.elementAt(j);
             listOfMails.setElementAt(listOfMails.elementAt(i), j);
             listOfMails.setElementAt(temp, i);
           }
         }
       }
     }
 
     else {
 
       for(int i = 0; i<listOfMails.size();i++) {
         Object temp = null;
         for (int j=0; j<listOfMails.size(); j++) {
           String s1 = 
               ((Vector)listOfMails.elementAt(i)).elementAt(col).toString();
           String s2 = 
               ((Vector)listOfMails.elementAt(j)).elementAt(col).toString();
 
           if (s1.toLowerCase().compareTo(s2.toLowerCase()) < 0 ) {
              temp = listOfMails.elementAt(j);
              listOfMails.setElementAt(listOfMails.elementAt(i), j);
              listOfMails.setElementAt(temp, i);
           }
         }
       }
     }
     updateUI();
   }
 
   /**
    * Sorts 10 -> 1
    */
   protected void SortLast(int col) {
      
     if (col == 0) {
       for (int i = 0; i < listOfMails.size(); i++) {
         Object temp = null;
         for (int j = 0; j < listOfMails.size(); j++) {
           int one = Integer.parseInt(
               ((Vector)listOfMails.elementAt(i)).elementAt(col).toString());
 
           int two = Integer.parseInt(
               ((Vector)listOfMails.elementAt(j)).elementAt(col).toString());
  
           if (one > two) {
             temp = listOfMails.elementAt(j);
             listOfMails.setElementAt(listOfMails.elementAt(i), j);
             listOfMails.setElementAt(temp, i);
           }
         }  
       }  
     }  
      
     else {
           
       for(int i = 0; i<listOfMails.size();i++) {
         Object temp = null;
         for (int j=0; j<listOfMails.size(); j++) {
           String s1 =
               ((Vector)listOfMails.elementAt(i)).elementAt(col).toString();
           String s2 =
               ((Vector)listOfMails.elementAt(j)).elementAt(col).toString();
 
           if (s1.toLowerCase().compareTo(s2.toLowerCase()) > 0 ) {
              temp = listOfMails.elementAt(j);
              listOfMails.setElementAt(listOfMails.elementAt(i), j);
              listOfMails.setElementAt(temp, i);
           }
         }  
       }
     }
     updateUI();
   }
 
   public void createPopup(JPopupMenu jpmenu) {
     Vector list = new Vector(), list2 = new Vector();          
     String boxHome = System.getProperty("user.home") + YAMM.sep +
                      ".yamm" + YAMM.sep + "boxes";
     
     fileList(list, new File(boxHome + YAMM.sep));
     fileList(list2, new File(boxHome + YAMM.sep));
     
     JMenuItem row, delete = new JMenuItem(YAMM.getString("button.delete")),
               reply = new JMenuItem(YAMM.getString("button.reply")); 
     delete.addActionListener(OtherMListener);
     reply.addActionListener(OtherMListener);
 
     jpmenu.add(reply);
     jpmenu.addSeparator();
     createPopCommand(jpmenu, YAMM.getString("edit.copy"), list, boxHome, KMListener);
     createPopCommand(jpmenu, YAMM.getString("edit.move"), list2, boxHome, FMListener);
     jpmenu.add(delete);
   }
 
     
   private void createPopCommand(JMenu menu, Vector flist, String base, ActionListener list) {
     String dname = base.substring(base.lastIndexOf(File.separator) + 1, base.length());
     if(dname.endsWith(".g")) dname = dname.substring(0, dname.length()-2);
     JMenu m = new JMenu(dname);
 
     for(int j=0;j < flist.size();j++) {
       String fpath = flist.elementAt(j).toString();
 
       if(fpath.indexOf(base) != -1) {
         String fname = fpath.substring(base.length() +1, fpath.length());
 
         if(fname.indexOf(File.separator) != -1) {
           menu.add(m); 
           String path = flist.elementAt(j).toString();
           path = path.substring(0, path.lastIndexOf(File.separator)); 
           createPopCommand(m, flist, path, list); 
           j--;
         }
         else {
           extMItem mitem = new extMItem(fname);
           mitem.setFullName(fpath);
           mitem.addActionListener(list);
           flist.remove(j);
           j--;
           m.add(mitem);
         }
       }
       else { 
         menu.add(m);
         j--;
         break;
       }
     }
     menu.add(m);
   }
 
   private void createPopCommand(JPopupMenu menu, String menuName, Vector flist, String base, ActionListener list) {
     JMenu m = new JMenu(menuName);                                                                            
                                   
     for(int j=0;j < flist.size();j++) {
       String fpath = flist.elementAt(j).toString();
                                               
       if(fpath.indexOf(base) != -1) {         
         String fname = fpath.substring(base.length() +1, fpath.length());
 
         if(fname.indexOf(File.separator) != -1) {
           menu.add(m);
           String path = flist.elementAt(j).toString();
           path = path.substring(0, path.lastIndexOf(File.separator));
           createPopCommand(m, flist, path, list);
           j--;
         }     
         else {
           extMItem mitem = new extMItem(fname);
           mitem.setFullName(fpath);
           mitem.addActionListener(list);
           flist.remove(j);
           j--;
           m.add(mitem);
         }
       }  
       else {
         menu.add(m);
         j--;
         break;
       }       
     }  
     menu.add(m);
   }
 
   private void fileList(Vector vect, File f) {
     if((f.toString()).equals(System.getProperty("user.home") + "/.yamm/boxes")) {
       String list[] = f.list();                                                  
       for(int i = 0; i < list.length; i++)
         fileList(vect, new File(f, list[i]));
     }                                     
     
     else if(f.isDirectory()) {
       String list[] = f.list();
       for(int i = 0; i < list.length; i++)
         fileList(vect, new File(f, list[i]));
     }
     else {
       String test = f.toString();
       test = test.substring(test.lastIndexOf(File.separator) + 1, test.length());
       if(!test.startsWith(".")) vect.add(f.toString()); 
     }
   }
  
 
 
   /**
    * Adds the sorting listener to the table header
    * @param table The JTable to add the sorting listener to
    */
   protected void TMListener(JTable table) {
     final JTable tableView = table;
     tableView.setColumnSelectionAllowed(false);
 
     MouseAdapter lmListener = new MouseAdapter() {
       public void mouseClicked(MouseEvent e) {
           TableColumnModel columnModel = tableView.getColumnModel();
           int viewColumn = columnModel.getColumnIndexAtX(e.getX());
           int column = tableView.convertColumnIndexToModel(viewColumn);
 
           if(column != -1) {
             if(column == sortedCol) {
               if(firstSort) {
                 SortFirst(column);
                 firstSort = false;
               }
               else {
                 SortLast(column);
                 firstSort = true;
               }
             }
             else {
               SortFirst(column);
               firstSort = false;
               sortedCol = column;
             }
           }
       }
     };
     JTableHeader th = tableView.getTableHeader();
     th.addMouseListener(lmListener);
   }
 
   /**
    * The renderer for the table
    */
   protected class myRenderer extends DefaultTableCellRenderer {
 
     public Component getTableCellRendererComponent(
       JTable table,
       Object value,
       boolean isSelected,
       boolean hasFocus,
       int row,
       int column) 
     {
       setValue(value);
       if(((Vector)frame.listOfMails.elementAt(row)).elementAt(4) != null) setForeground(Color.black);
       else setForeground(Color.blue);
       if(isSelected) setBackground(new Color(204, 204, 255));
       else setBackground(Color.white);
 
       return this;
     }
   }
 
 
 
   protected MouseListener mouseListener = new MouseAdapter() {
     public void mouseReleased(MouseEvent me) {
       if(me.isPopupTrigger()) popup.show(mainTable.this, me.getX(), me.getY());
       else if(getSelectedRow() != -1) get_mail();
 
  
       if(getSelectedRow() != -1 && !(getSelectedRow() >= frame.listOfMails.size())) { 
         if(((Vector)frame.listOfMails.elementAt(getSelectedRow())).elementAt(4) == null) {
           System.out.println("box: " + frame.selectedbox);
           Mailbox.setStatus(frame.selectedbox, getSelectedRow(), "Read");
           Mailbox.createList(frame.selectedbox, frame.listOfMails);
           System.out.println("box: " + frame.selectedbox);
         }
         ((JButton)frame.tbar.reply).setEnabled(true); 
         ((JButton)frame.tbar.print).setEnabled(true); 
         ((JButton)frame.tbar.forward).setEnabled(true);
       }
       else { 
         ((JButton)frame.tbar.reply).setEnabled(false); 
         ((JButton)frame.tbar.forward).setEnabled(false); 
         ((JButton)frame.tbar.print).setEnabled(false); 
       }
     }
     public void mousePressed(MouseEvent me) {
       if(me.isPopupTrigger()) popup.show(mainTable.this, me.getX(), me.getY());
     }
  
     void get_mail() {
       int i = 0;
  
       while(i<4) {
         if(getColumnName(i).equals("#")) { break; }
         i++;
       }
       int whatMail = Integer.parseInt(getValueAt(getSelectedRow(), i).toString());
 
       Mailbox.getMail(frame.selectedbox,whatMail);
       try {
         String boxName = frame.selectedbox.substring(frame.selectedbox.indexOf("boxes") + 6, frame.selectedbox.length()) + "/"; 
         frame.mailPage = new URL(frame.mailPageString + boxName + whatMail + ".html");
       }
       catch (MalformedURLException mue) { 
         Object[] args = {mue.toString()};
         new MsgDialog(frame, YAMM.getString("msg.error"), 
                              YAMM.getString("msg.exception", args)); 
       }
  
       try { frame.mail.setPage(frame.mailPage); }
       catch (IOException ioe) { 
         Object[] args = {ioe.toString()};
         new MsgDialog(frame, YAMM.getString("msg.error"), 
                              YAMM.getString("msg.exception", args)); 
       }
 
       frame.createAttachList();
       frame.myList.updateUI();
     }
   };
 
   protected ActionListener keyListener = new ActionListener() {
     public void actionPerformed(ActionEvent ae) {                 
       String text = ae.getActionCommand();
 
       int i = 0;                                              
                 
       while(i<4) {
         if(getColumnName(i).equals("#")) { break; }
         i++;
       }     
        
       if(text.equals("Del")) {
         frame.delUnNeededFiles();
         int[] mlist = getSelectedRows();
         int[] deleteList = new int[mlist.length];
 
         for(int j = 0;j < mlist.length;j++) {
           deleteList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
         }
          
         Arrays.sort(deleteList);
 
         Mailbox.deleteMail(frame.selectedbox, deleteList);
 
         Mailbox.createList(frame.selectedbox, listOfMails);
 
         updateUI();
         frame.attach = new Vector();
 
 
         Mailbox.getMail(frame.selectedbox, getSelectedRow());
 
         try {
           String boxName = frame.selectedbox.substring(frame.selectedbox.indexOf("boxes") + 6, frame.selectedbox.length()) + "/";
           frame.mailPage = new URL(frame.mailPageString + boxName + getSelectedRow()  + ".html");
         }
         catch (MalformedURLException mue) {
           Object[] args = {mue.toString()};
           new MsgDialog(frame, YAMM.getString("msg.error"),
                                YAMM.getString("msg.exception", args));
         }
 
         try { frame.mail.setPage(frame.mailPage); }
         catch (IOException ioe) {
           Object[] args = {ioe.toString()};
           new MsgDialog(frame, YAMM.getString("msg.error"),
                                YAMM.getString("msg.exception", args));
         }
       }
 
     }
   };
 
   protected ActionListener OtherMListener = new ActionListener() {
     public void actionPerformed(ActionEvent ae) {       
       String kommando = ((JMenuItem)ae.getSource()).getText();
                                                               
       int i = 0;
       
       while(i<4) {
         if(getColumnName(i).equals("#")) { break; }
         i++;     
       }    
 
       if(kommando.equals(YAMM.getString("button.delete"))) {
         frame.delUnNeededFiles();
         int[] mlist = getSelectedRows();
         int[] deleteList = new int[mlist.length];
 
         for(int j = 0;j < mlist.length;j++) {
           deleteList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
         }
 
         Arrays.sort(deleteList);
 
         Mailbox.deleteMail(frame.selectedbox, deleteList);
          
         Mailbox.createList(frame.selectedbox, listOfMails);
 
         updateUI();
         frame.attach = new Vector();
 
 
         Mailbox.getMail(frame.selectedbox, getSelectedRow());
 
         try { 
           String boxName = frame.selectedbox.substring(frame.selectedbox.indexOf("boxes") + 6, frame.selectedbox.length()) + "/"; 
           frame.mailPage = new URL(frame.mailPageString + boxName + getSelectedRow()  + ".html"); 
         }
         catch (MalformedURLException mue) { 
           Object[] args = {mue.toString()};
           new MsgDialog(frame, YAMM.getString("msg.error"), 
                                YAMM.getString("msg.exception", args)); 
         }
 
         try { frame.mail.setPage(frame.mailPage); }
         catch (IOException ioe) { 
           Object[] args = {ioe.toString()};
           new MsgDialog(frame, YAMM.getString("msg.error"), 
                                YAMM.getString("msg.exception", args)); }
       }
 
       else if(kommando.equals(YAMM.getString("button.reply"))) {
                  
         String[] mail = Mailbox.getMailForReplyHeaders(frame.selectedbox, 
                                 Integer.parseInt(getValueAt(getSelectedRow(), i).toString()));
  
         YAMMWrite yam = new YAMMWrite(mail[0], mail[1], mail[0] + " " + YAMM.getString("mail.wrote") + "\n");
         Mailbox.getMailForReply(frame.selectedbox, 
                                 Integer.parseInt(getValueAt(getSelectedRow(), i).toString()), 
                                 yam.myTextArea);
       }
     }
   };
 
   protected ActionListener KMListener = new ActionListener() {
     public void actionPerformed(ActionEvent ae) {
       String name = ((extMItem)ae.getSource()).getFullName();
                                                               
       int i = 0;                                              
                 
       while(i<4) {
         if(getColumnName(i).equals("#")) { break; }
         i++;
       }     
        
       int[] mlist = getSelectedRows();
       int[] copyList = new int[mlist.length];  
                                              
       for(int j = 0;j < mlist.length;j++) {  
         copyList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
       }                                                                             
        
       Mailbox.copyMail(frame.selectedbox, name, copyList);
     }
   };
 
   protected ActionListener FMListener = new ActionListener() {
     public void actionPerformed(ActionEvent ae) {
       String name = ((extMItem)ae.getSource()).getFullName();
 
       int i = 0;
 
       while(i<4) {
         if(getColumnName(i).equals("#")) { break; }
         i++;
       }
 
 
       int[] mlist = getSelectedRows();
       int[] moveList = new int[mlist.length];
 
       for(int j = 0;j < mlist.length;j++) {
         moveList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
       }
 
 
       Mailbox.moveMail(frame.selectedbox, name, moveList);
 
       Mailbox.createList(frame.selectedbox, frame.listOfMails);
 
       mainTable.this.updateUI();
 
       Mailbox.getMail(frame.selectedbox, getSelectedRow());
 
       try { 
         String boxName = frame.selectedbox.substring(frame.selectedbox.indexOf("boxes") + 6, frame.selectedbox.length()) + "/"; 
        frame.mailPage = new URL(frame.mailPageString + boxName + getSelectedRow() + "html"); 
       }
       catch (MalformedURLException mue) { 
         Object[] args = {mue.toString()};
         new MsgDialog(frame, YAMM.getString("msg.error"), 
                              YAMM.getString("msg.exception", args)); 
       }
 
       try { frame.mail.setPage(frame.mailPage); }
       catch (IOException ioe) { 
         Object[] args = {ioe.toString()};
         new MsgDialog(frame, YAMM.getString("msg.error"),
                              YAMM.getString("msg.exception", args)); 
       }    
     }
   };
 }
