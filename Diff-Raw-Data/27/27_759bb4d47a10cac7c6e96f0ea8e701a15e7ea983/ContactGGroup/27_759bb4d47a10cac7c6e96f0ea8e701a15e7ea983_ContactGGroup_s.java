 import org.jivesoftware.resource.Res;
 import org.jivesoftware.spark.component.VerticalFlowLayout;
 import org.jivesoftware.spark.component.panes.CollapsiblePane;
 import org.jivesoftware.spark.component.renderer.JPanelRenderer;
 import org.jivesoftware.spark.ui.ContactGroup;
 import org.jivesoftware.spark.util.GraphicUtils;
 import org.jivesoftware.spark.util.log.Log;
 import org.jivesoftware.sparkimpl.settings.local.LocalPreferences;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionAdapter;
 import java.util.*;
 import java.util.List;
 import java.util.Timer;
 
 /**
  * Created with IntelliJ IDEA.
  * User: SaintKnight
  * Date: 13-8-20
  * Time: 下午4:02
  * Creator:Huang Jie
  * To change this template use File | Settings | File Templates.
  */
 public class ContactGGroup extends CollapsiblePane implements MouseListener {
     private static final long serialVersionUID = 1L;
 
     private List<ContactGItem> contactGItems = new ArrayList<ContactGItem>();
     private List<ContactGGroup> contactGGroups = new ArrayList<ContactGGroup>();
     private List<ContactGGroupListener> listeners = new ArrayList<ContactGGroupListener>();
     private List<ContactGItem> offlineGContacts = new ArrayList<ContactGItem>();
 
     // Used to display no contacts in list.
 
     private String groupName;
     private String parentGroup;
     private DefaultListModel model;
     private JPanel listPanel;
 
     public ContactGGroup(String groupName, String parentGroup) {
        this.groupName = groupName;
         this.parentGroup = parentGroup;
     }
 
     public void setGroupName(String groupName) {
         this.groupName = groupName;
     }
 
     public void setParentGroup(String parentGroup) {
         this.parentGroup = parentGroup;
     }
 
     public String getParentGroup() {
         return parentGroup;
     }
 
     public ContactGGroup(String groupName) {
 //        super(groupName);
         // Initialize Model and UI
         model = new DefaultListModel();
         contactGItemList = new JList(model);
 
         setTitle(getGroupTitle(groupName));
 
         // Use JPanel Renderer
         contactGItemList.setCellRenderer(new JPanelRenderer());
 
         this.groupName = groupName;
 
         listPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
 
 
         listPanel.add(contactGItemList, listPanel);
 
         //设置分组是不可以拖拽的
         contactGItemList.setDragEnabled(false);
 //        contactGItemList.setTransferHandler(new ContactGGroupTransferHandler());
 
 
         this.setContentPane(listPanel);
 
         // Items should have selection listener
         contactGItemList.addMouseListener(this);
 
 
         // Add Popup Window
         addPopupWindow();
 
     }
 
     /**
      * Add a <code>ContactGGroupListener</code>.
      *
      * @param listener the ContactGGroupListener.
      */
     public void addContactGGroupListener(ContactGGroupListener listener) {
         listeners.add(listener);
     }
 
     private Timer timer = new Timer();
 
     /**
      * Adds an internal popup listesner.
      */
     private void addPopupWindow() {
         contactGItemList.addMouseListener(new MouseAdapter() {
             //todo:以下有bug，尚未解决，所以注释掉 2013年8月27日13:56:15
             public void mouseEntered(MouseEvent mouseEvent) {
 //                canShowPopup = true;
 //                timerTask = new DisplayWindowTask(mouseEvent);
 //                timer.schedule(timerTask, 500, 1000);
             }
 
             public void mouseExited(MouseEvent mouseEvent) {
 //                canShowPopup = false;
 //                UIComponentRegistry.getContactInfoWindow().dispose();
             }
         });
 
 
         contactGItemList.addMouseMotionListener(motionListener);
     }
 
     private final ListMotionListener motionListener = new ListMotionListener();
     private boolean mouseDragged = false;
     private DisplayWindowTask timerTask = null;
 
     private class DisplayWindowTask extends TimerTask {
         private MouseEvent event;
         private boolean newPopupShown = false;
 
         public DisplayWindowTask(MouseEvent e) {
             event = e;
         }
 
         @Override
         public void run() {
             if (canShowPopup) {
                 if (!newPopupShown && !mouseDragged) {
                     displayWindow(event);
                     newPopupShown = true;
                 }
             }
         }
 
         public void setEvent(MouseEvent event) {
             this.event = event;
         }
 
         public void setNewPopupShown(boolean popupChanged) {
             this.newPopupShown = popupChanged;
         }
 
         public boolean isNewPopupShown() {
             return newPopupShown;
         }
     }
 
     private boolean canShowPopup;
 
     private class ListMotionListener extends MouseMotionAdapter {
 
         @Override
         public void mouseMoved(MouseEvent e) {
             if (!canShowPopup) {
                 return;
             }
 
             if (e == null) {
                 return;
             }
             timerTask.setEvent(e);
             if (needToChangePopup(e) && timerTask.isNewPopupShown()) {
 
                 //todo:鼠标拖动时间，尚未完成，2013年8月27日15:20:46
 //                UIComponentRegistry.getContactInfoWindow().dispose();
                 timerTask.setNewPopupShown(false);
             }
             mouseDragged = false;
         }
 
         @Override
         public void mouseDragged(MouseEvent e) {
             //todo:鼠标拖动时间，尚未完成，2013年8月27日15:20:46
 //            if (timerTask.isNewPopupShown()) {
 
 
 //                UIComponentRegistry.getContactInfoWindow().dispose();
 //            }
 //            mouseDragged = true;
         }
     }
 
     private LocalPreferences preferences;
 
     /**
      * Displays the <code>ContactInfoWindow</code>.
      *
      * @param e the mouseEvent that triggered this event.
      */
     private void displayWindow(MouseEvent e) {
         if (preferences.areVCardsVisible()) {
             //原始代码无法复用，对象不同
 //            UIComponentRegistry.getContactInfoWindow().display(this, e);
         }
     }
 
     private boolean needToChangePopup(MouseEvent e) {
 //        ContactInfoWindow contact = UIComponentRegistry.getContactInfoWindow();
 //        int loc = getList().locationToIndex(e.getPoint());
 //        ContactGItem item = (ContactGItem)getList().getModel().getElementAt(loc);
 //        boolean isTrue =item == null || contact == null || contact.getContactGItem() == null ? true : !contact.getContactGItem().getJID().equals(item.getJID());
         return false;
     }
 
     /**
      * Returns the containing <code>JList</code> of the ContactGGroup.
      *
      * @return the JList.
      */
     public JList getList() {
         return contactGItemList;
     }
 
     /**
      * Returns the "pretty" title of the ContactGGroup.
      *
      * @param title the title.
      * @return the new title.
      */
     public String getGroupTitle(String title) {
         int lastIndex = title.lastIndexOf("::");
         if (lastIndex != -1) {
             title = title.substring(lastIndex + 2);
         }
 
         return title;
     }
 
     /**
      * Returns the name of the ContactGGroup.
      *
      * @return the name of the ContactGGroup.
      */
     public String getGroupName() {
         return groupName;
     }
 
     public Collection<ContactGGroup> getContactGGroups() {
         return contactGGroups;
     }
 
 
     /**
      * Adds a sub group to this Contact group.
      *
      * @param contactGGroup that should be the new subgroup
      */
     public void addContactGGroup(ContactGGroup contactGGroup) {
         final JPanel panel = new JPanel(new GridBagLayout());
         panel.add(contactGGroup, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 15, 0, 0), 0, 0));
         panel.setBackground(Color.white);
         contactGGroup.setSubPane(true);
 
         // contactGroup.setStyle(CollapsiblePane.TREE_STYLE);
         contactGGroups.add(contactGGroup);
         Collections.sort(contactGGroups, ContactGList.GROUP_COMPARATOR);
         listPanel.add(panel, contactGGroups.indexOf(contactGGroup));
     }
 
 
     /**
      * Removes a <code>ContactGGroupListener</code>.
      *
      * @param listener the ContactGGroupListener.
      */
     public void removeContactGGroupListener(ContactGGroupListener listener) {
         listeners.remove(listener);
     }
 
 
     /**
      * Removes a child ContactGGroup.
      *
      * @param contactGGroup the contact group to remove.
      */
     public void removeContactGGroup(ContactGGroup contactGGroup) {
         Component[] comps = listPanel.getComponents();
         for (Component comp : comps) {
             if (comp instanceof JPanel) {
                 JPanel panel = (JPanel) comp;
                 ContactGGroup group = (ContactGGroup) panel.getComponent(0);
                 if (group == contactGGroup) {
                     listPanel.remove(panel);
                     break;
                 }
             }
         }
         contactGGroups.remove(contactGGroup);
     }
 
     public void setPanelBackground(Color color) {
         Component[] comps = listPanel.getComponents();
         for (Component comp : comps) {
             if (comp instanceof JPanel) {
                 JPanel panel = (JPanel) comp;
                 panel.setBackground(color);
             }
         }
 
     }
 
     /**
      * Returns a ContactGGroup based on it's name.
      *
      * @param groupName the name of the group.
      * @return the ContactGGroup.
      */
     public ContactGGroup getContactGGroup(String groupName) {
         for (ContactGGroup group : new ArrayList<ContactGGroup>(contactGGroups)) {
             if (group.getGroupName().equals(groupName)) {
                 return group;
             }
         }
 
         return null;
     }
 
     /**
      * Adds a <code>ContactGItem</code> to the ContactGGroup.
      *
      * @param item the ContactGItem.
      */
     public void addContactGItem(ContactGItem item) {
 
 
         item.setGroupName(getGroupName());
         contactGItems.add(item);
 
         List<ContactGItem> tempItems = getContactGItems();
 
 
         Collections.sort(tempItems, itemComparator);
 
 
         int index = tempItems.indexOf(item);
 
 
         Object[] objs = contactGItemList.getSelectedValues();
 
         model.insertElementAt(item, index);
 
         int[] intList = new int[objs.length];
         for (int i = 0; i < objs.length; i++) {
             ContactGItem contact = (ContactGItem) objs[i];
             intList[i] = model.indexOf(contact);
         }
 
         if (intList.length > 0) {
             contactGItemList.setSelectedIndices(intList);
         }
 
         fireContactGItemAdded(item);
     }
 
     /**
      * Removes a <code>ContactGItem</code>.
      *
      * @param item the ContactGItem to remove.
      */
     public void removeContactGItem(ContactGItem item) {
         contactGItems.remove(item);
         if (contactGItems.isEmpty()) {
             removeContactGGroup(this);
         }
 
         model.removeElement(item);
         updateTitle();
 
         fireContactGItemRemoved(item);
     }
 
     private void updateTitle() {
         int count = 0;
         List<ContactGItem> list = new ArrayList<ContactGItem>(getContactGItems());
         int size = list.size();
         for (int i = 0; i < size; i++) {
             ContactGItem it = list.get(i);
             if (it.isAvailable()) {
                 count++;
             }
         }
         setTitle(getGroupTitle(groupName) + " (" + count + " " + Res.getString("online") + ")");
     }
 
     /**
      * Returns all <code>ContactGItem</cod>s in the ContactGGroup.
      *
      * @return all ContactGItems.
      */
     public List<ContactGItem> getContactGItems() {
         final List<ContactGItem> list = new ArrayList<ContactGItem>(contactGItems);
         Collections.sort(list, itemComparator);
         return list;
     }
 
     /**
      * Sorts ContactGItems.
      */
     final protected Comparator<ContactGItem> itemComparator = new Comparator<ContactGItem>() {
         public int compare(ContactGItem item1, ContactGItem item2) {
             return item1.getDisplayName().toLowerCase().compareTo(item2.getDisplayName().toLowerCase());
         }
     };
 
 
     /**
      * Returns a <code>ContactGItem</code> by the users bare bareJID.
      *
      * @param bareJID the bareJID of the user.
      * @return the ContactGItem.
      */
     public ContactGItem getContactGItemByJID(String bareJID) {
         for (ContactGItem item : new ArrayList<ContactGItem>(contactGItems)) {
             if (item != null && item.getJID().equals(bareJID)) {
                 return item;
             }
         }
         return null;
     }
 
     /**
      * Returns a <code>ContactGItem</code> by the displayed name the user has been assigned.
      *
      * @param displayName the displayed name of the user.
      * @return the ContactGItem.
      */
     public ContactGItem getContactGItemByDisplayName(String displayName) {
         for (ContactGItem item : new ArrayList<ContactGItem>(contactGItems)) {
             if (item.getDisplayName().equals(displayName)) {
                 return item;
             }
         }
         return null;
     }
 
     public void mouseClicked(MouseEvent e) {
 
         Object o = contactGItemList.getSelectedValue();
         if (!(o instanceof ContactGItem)) {
             return;
         }
 
         // Iterator through rest
         ContactGItem item = (ContactGItem) o;
 
         if (e.getClickCount() == 2) {
             System.out.println("fireContactGItemDoubleClicked");
             //双击事件，测试版本
             fireContactGItemDoubleClicked(item);
         } else if (e.getClickCount() == 1) {
             System.out.println("fireContactGItemClicked");
 
             //单击事件，测试版本
             fireContactGItemClicked(item);
         }
     }
 
     public void mousePressed(MouseEvent e) {
         checkPopup(e);
     }
 
     public void mouseReleased(MouseEvent e) {
         checkPopup(e);
     }
 
     public void mouseEntered(MouseEvent e) {
         int loc = contactGItemList.locationToIndex(e.getPoint());
 
         Object o = model.getElementAt(loc);
         if (!(o instanceof ContactGItem)) {
             return;
         }
 
         contactGItemList.setCursor(GraphicUtils.HAND_CURSOR);
     }
 
     public void mouseExited(MouseEvent e) {
         Object o;
         try {
             int loc = contactGItemList.locationToIndex(e.getPoint());
             if (loc == -1) {
                 return;
             }
 
             o = model.getElementAt(loc);
             if (!(o instanceof ContactGItem)) {
 //                UIComponentRegistry.getContactInfoWindow().dispose();
                 return;
             }
         } catch (Exception e1) {
             Log.error(e1);
             return;
         }
         contactGItemList.setCursor(GraphicUtils.DEFAULT_CURSOR);
     }
 
     private void checkPopup(MouseEvent e) {
         //右键菜单事件，尚不完善 2013年8月28日13:22:08
         if (e.isPopupTrigger()) {
 
             System.out.println("PopupTrigger!");
             // Otherwise, handle single selection
             int index = contactGItemList.locationToIndex(e.getPoint());
             if (index != -1) {
                 int[] indexes = contactGItemList.getSelectedIndices();
                 boolean selected = false;
                 for (int o : indexes) {
                     if (index == o) {
                         selected = true;
                     }
                 }
 
                 if (!selected) {
                     contactGItemList.setSelectedIndex(index);
                     fireContactGItemClicked((ContactGItem) contactGItemList.getSelectedValue());
                 }
             }
 
             firePopupEvent(e, (ContactGItem) contactGItemList.getSelectedValue());
 
             //todo:传入选中的SelectedItems，目前尚未完成，2013年8月28日13:39:10
 //            final Collection<ContactGItem> selectedItems = SparkManager.getChatManager().getSelectedContactGItems();
 //            if (selectedItems.size() > 1) {
 //                firePopupEvent(e, selectedItems);
 //            }
 //            else if (selectedItems.size() == 1) {
 //                final ContactGItem contactGItem = selectedItems.iterator().next();
 //                firePopupEvent(e, contactGItem);
 //            }
         }
     }
 
     /**
      * Returns all Selected Contacts within the ContactGroup.
      *
      * @return all selected ContactGItems.
      */
     public List<ContactGItem> getSelectedContacts() {
         final List<ContactGItem> items = new ArrayList<ContactGItem>();
         Object[] selections = contactGItemList.getSelectedValues();
         final int no = selections != null ? selections.length : 0;
         for (int i = 0; i < no; i++) {
             try {
                 ContactGItem item = (ContactGItem) selections[i];
                 items.add(item);
             } catch (NullPointerException e) {
                 //  Evaluate if we should do something here.
             }
         }
         return items;
     }
 
     private void fireContactGItemClicked(ContactGItem item) {
         for (ContactGGroupListener contactGroupListener : new ArrayList<ContactGGroupListener>(listeners)) {
             contactGroupListener.contactGItemClicked(item);
         }
     }
 
     private void fireContactGItemDoubleClicked(ContactGItem item) {
         for (ContactGGroupListener contactGroupListener : new ArrayList<ContactGGroupListener>(listeners)) {
             contactGroupListener.contactGItemDoubleClicked(item);
         }
     }
 
 
     private void firePopupEvent(MouseEvent e, ContactGItem item) {
         for (ContactGGroupListener contactGroupListener : new ArrayList<ContactGGroupListener>(listeners)) {
             contactGroupListener.showPopup(e, item);
         }
     }
 
     private void firePopupEvent(MouseEvent e, Collection<ContactGItem> items) {
         for (ContactGGroupListener contactGroupListener : new ArrayList<ContactGGroupListener>(listeners)) {
             contactGroupListener.showPopup(e, items);
         }
     }
 
     private void fireContactGGroupPopupEvent(MouseEvent e) {
         for (ContactGGroupListener contactGroupListener : new ArrayList<ContactGGroupListener>(listeners)) {
             contactGroupListener.contactGGroupPopup(e, this);
         }
     }
 
     private void fireContactGItemAdded(ContactGItem item) {
         for (ContactGGroupListener contactGroupListener : new ArrayList<ContactGGroupListener>(listeners)) {
             contactGroupListener.contactGItemAdded(item);
         }
     }
 
     private void fireContactGItemRemoved(ContactGItem item) {
         for (ContactGGroupListener contactGroupListener : new ArrayList<ContactGGroupListener>(listeners)) {
             contactGroupListener.contactGItemRemoved(item);
         }
     }
 
     private JList contactGItemList;
 
     public void clearSelection(ContactGItem selectedItem) {
         contactGItemList.clearSelection();
         if (contactGGroups != null) {
             final ContactGGroup owner = getContactGGroup(selectedItem.getGroupName());
             for (ContactGGroup contactGGroup : new ArrayList<ContactGGroup>(contactGGroups)) {
                 if (owner != contactGGroup) {
                     contactGGroup.clearSelection(selectedItem);
                 }
             }
         }
     }
 
     /**
      * Returns true if ContactGroup is a Shared Group.
      *
      * @return true if Shared Group.
      */
     private boolean sharedGroup;
 
     public boolean isSharedGroup() {
         return sharedGroup;
     }
 
     /**
      * Returns true if ContactGroup is a Shared Group.
      *
      * @return true if Shared Group.
      */
     public boolean getUnfiledGroup() {
         return sharedGroup;
     }
 
     public ContactGroup toContactGroup() {
         return new ContactGroup(this.groupName);
     }
 
 }
