 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import com.community.xanadu.components.table.BeanReaderJTable;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Contact;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Group;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import javax.swing.*;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableRowSorter;
 
 /**
  * Panel with contacts
  *
  * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
  */
 public class ContactsPanel extends JPanel implements KeyListener {
 
   static final long serialVersionUID = 0;
   private JPopupMenu contextMenu = new JPopupMenu();
   /**
    * Handle to the MainWindow.
    */
   private MainWindow mainWindowHandle;
   /**
    * Database instance.
    */
   private static final Database db = Database.getInstance();
   /**
    * Contacts table bean reader.
    */
   private static final BeanReaderJTable<Contact> table = new BeanReaderJTable<Contact>(new String[]{"FullName", "AllEmails", "AllPhones"}, new String[]{"Name", "Emails", "Phones"});
   /**
    * Contacts table sorter.
    */
   private static final TableRowSorter<BeanReaderJTable.GenericTableModel> sorter = new TableRowSorter<BeanReaderJTable.GenericTableModel>(table.getModel());
 
   /**
    * Constructor
    */
   public ContactsPanel(MainWindow handle, ListSelectionListener listSelectionListener) {
     this.mainWindowHandle = handle;
 
     setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 //    JLabel label = new JLabel("Contacts");
 //    label.setAlignmentX(CENTER_ALIGNMENT);
 //    add(label);
 
     fillTable(false, false);
     this.mainWindowHandle.handleContactActionsVisibility();
 
     table.getSelectionModel().addListSelectionListener(listSelectionListener);
     table.setRowSorter(sorter);
     table.setDefaultRenderer(Object.class, new TableRowColorRenderer());
     table.addKeyListener(this);
     JScrollPane scrollPane = new JScrollPane(table);
     filter("");
     add(scrollPane);
     this.initContextMenu();
   }
 
   /**
    * Fill table with data from list
    */
   public static void fillTable(boolean rememberSelection, boolean newContact) {
     final RowFilter filter = sorter.getRowFilter();	//Warnings!
 
     int[] selRows = null;
 
     if (rememberSelection) {
       selRows = table.getSelectedRows();
     }
 
    if (newContact) {
      GroupsPanel.selectRootGroup();
    }

     Group[] groups = GroupsPanel.getSelectedGroups();
     List<Group> selectedGroups = Arrays.asList(groups);
     final List<Contact> contacts = db.getAllContactsFromGroups(selectedGroups);
 
     sorter.setRowFilter(null);
     table.clear();
     table.addRow(contacts);
     sorter.setRowFilter(filter);
 
     if (rememberSelection) {
       for (int row : selRows) {
         table.setRowSelectionInterval(row, row);
       }
     }
 
     if (newContact) {
       int last = contacts.size() - 1;
       table.setRowSelectionInterval(last, last);
     }
   }
 
   /**
    * Filter contacts
    *
    * @param f String to filter
    */
   void filter(String f) {
     sorter.setRowFilter(RowFilter.regexFilter("(?i)" + f));
   }
 
   /**
    * Get selected contacts
    */
   static Contact getSelectedContact() {
     return table.getSelectedObject();
   }
 
   /**
    * Get all selected contacts.
    *
    * @return
    */
   static Contact[] getSelectedContacts() {
     Object[] objects = table.getSelectedObjects();
     Contact[] contacts = new Contact[objects.length];
     for (int i = 0; i < objects.length; i++) {
       contacts[i] = (Contact) objects[i];
     }
     return contacts;
   }
 
   /**
    * Method for creating contacts string for msg box
    *
    * @param contacts array of contacts
    * @return string with contacts full names
    */
   private static String getContactsString(Contact[] contacts) {
     if (contacts.length == 1) {
       return contacts[0].getFullName();
     }
 
     StringBuilder sb = new StringBuilder();
     sb.append("[");
     int length = contacts.length;
     for (int i = 0; i < length; i++) {
       sb.append(contacts[i].getFullName());
       if (i != length - 1) {
         sb.append(", ");
       }
     }
 
     sb.append("]");
 
     return sb.toString();
   }
 
   /**
    * Method for removing contacts
    */
   static boolean removeContacts() {
     final Contact[] contacts = getSelectedContacts();
     final String msg = getContactsString(contacts);
 
     final int delete = JOptionPane.showConfirmDialog(
             null,
             "Delete contact(s) " + msg + "?",
             "Delete contact",
             JOptionPane.YES_NO_OPTION,
             JOptionPane.QUESTION_MESSAGE,
             new ImageIcon(ContactsPanel.class.getResource("/res/minus.png"), "-"));
 
     if (delete == 0) {
       List<Contact> contactToRemove = new ArrayList<Contact>();
       contactToRemove.addAll(Arrays.asList(contacts));
       db.removeContacts(contactToRemove);
       ContactsPanel.fillTable(false, false);
       GroupsPanel.fillList();
       return true;
     }
 
     return false;
   }
 
   /**
    * Initialize context menu.
    */
   private void initContextMenu() {
     this.contextMenu.add(this.mainWindowHandle.actions.actionNewContact);
     this.contextMenu.add(this.mainWindowHandle.actions.actionEditContact);
     this.contextMenu.add(this.mainWindowHandle.actions.actionDeleteContact);
     this.contextMenu.addSeparator();
     this.contextMenu.add(this.mainWindowHandle.actions.actionManageContactGroups);
     this.contextMenu.addSeparator();
     this.contextMenu.add(this.mainWindowHandle.actions.actionImport);
     this.contextMenu.add(this.mainWindowHandle.actions.actionExport);
 
     MouseListener popupListener = new PopupListener();
     table.addMouseListener(popupListener);
   }
 
   /**
    * Handle delete button pressed and shortcuts.
    */
   @Override
   public void keyPressed(KeyEvent e) {
 
     switch (e.getKeyCode()) {
       case KeyEvent.VK_DELETE:
         if (this.mainWindowHandle.actions.actionDeleteContact.isEnabled()) {
           if (removeContacts()) {
             this.mainWindowHandle.getDetailPanel().hideDetail();
           }
         }
         break;
     }
 
     if (e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK) {
       switch (e.getKeyCode()) {
         case KeyEvent.VK_E:
           if (this.mainWindowHandle.actions.actionEditContact.isEnabled()) {
             new ContactWindow(ContactsPanel.getSelectedContact());
           }
           break;
         case KeyEvent.VK_M:
           if (this.mainWindowHandle.actions.actionManageContactGroups.isEnabled()) {
             new GroupsMembershipWindow(ContactsPanel.getSelectedContact());
           }
           break;
       }
     }
   }
 
   /**
    * Only for complete interface implementation - NOT USED.
    */
   @Override
   public void keyTyped(KeyEvent e) {
   }
 
   /**
    * Only for complete interface implementation - NOT USED.
    */
   @Override
   public void keyReleased(KeyEvent e) {
   }
 
   /**
    * Class for handling contact double click and popup menu.
    */
   private class PopupListener extends MouseAdapter {
 
     @Override
     public void mouseClicked(MouseEvent e) {
       if (e.getClickCount() == 2) {
         new ContactWindow(getSelectedContact());
       }
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
       showPopup(e);
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
       showPopup(e);
     }
 
     private void showPopup(MouseEvent e) {
       if (e.isPopupTrigger()) {
         contextMenu.show(e.getComponent(), e.getX(), e.getY());
       }
     }
   }
 }
