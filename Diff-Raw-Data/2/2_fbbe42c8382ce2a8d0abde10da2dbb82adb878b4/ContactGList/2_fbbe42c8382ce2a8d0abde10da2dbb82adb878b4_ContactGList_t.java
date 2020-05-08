 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.StatusLine;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.jivesoftware.resource.Default;
 import org.jivesoftware.resource.Res;
 import org.jivesoftware.resource.SparkRes;
 import org.jivesoftware.smack.*;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.packet.RosterPacket;
 import org.jivesoftware.smackx.LastActivityManager;
 import org.jivesoftware.smackx.packet.LastActivity;
 import org.jivesoftware.spark.ChatManager;
 import org.jivesoftware.spark.SparkManager;
 import org.jivesoftware.spark.Workspace;
 import org.jivesoftware.spark.component.InputDialog;
 import org.jivesoftware.spark.component.RolloverButton;
 import org.jivesoftware.spark.component.VerticalFlowLayout;
 import org.jivesoftware.spark.plugin.ContextMenuListener;
 import org.jivesoftware.spark.plugin.Plugin;
 import org.jivesoftware.spark.ui.FileDropListener;
 import org.jivesoftware.spark.ui.RosterDialog;
 import org.jivesoftware.spark.util.ModelUtil;
 import org.jivesoftware.spark.util.log.Log;
 import org.jivesoftware.sparkimpl.profile.VCardManager;
 import org.jivesoftware.sparkimpl.settings.local.LocalPreferences;
 import org.jivesoftware.sparkimpl.settings.local.SettingsManager;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: SaintKnight
  * Date: 8/15/13
  * Time: 2:43 PM
  * Creator:Huang Jie
  * To change this template use File | Settings | File Templates.
  */
 public class ContactGList extends JPanel implements ActionListener,
         ContactGGroupListener, Plugin, RosterListener, ConnectionListener {
     //分组请求地址，传递为JSON数组。格式如:[{ "groupName": "root", "parentGroup": null }]
     final String targetGroupURL = "http://localhost:8080/contactsweb/getcontactsggroup.jsp";
     final String targetContactsURL = "http://localhost:8080/contactsweb/getcontactsg.jsp";
 
     //参数设定为parentGroup为请求的父分组传参
     final String parameterURL = "parentGroup";
 
     private static final long serialVersionUID = 1L;
     private JPanel mainPanel = new JPanel();
     private JScrollPane contactListScrollPane;
 
     private List<ContactGGroup> groupList = new ArrayList<ContactGGroup>();
 
 
     // Create Menus
     private JMenuItem addContactMenu;
     private JMenuItem addContactGGroupMenu;
 
     private ContactGGroup unfiledGroup;
     private File propertiesFile;
 
 
     private LocalPreferences localPreferences;
 
 
     /**
      * Creates a new instance of ContactGList.
      */
     public ContactGList() {
         // Load Local Preferences
         localPreferences = SettingsManager.getLocalPreferences();
 
 //        unfiledGroup = new ContactGGroup("unfiledGroup");
 //        System.out.println("unfiledGroup Created");
 
         JToolBar toolbar = new JToolBar();
         toolbar.setFloatable(false);
         addContactMenu = new JMenuItem(Res.getString("menuitem.add.contact"), SparkRes.getImageIcon(SparkRes.USER1_ADD_16x16));
         addContactGGroupMenu = new JMenuItem(Res.getString("menuitem.add.contact.group"), SparkRes.getImageIcon(SparkRes.SMALL_ADD_IMAGE));
 
         addingGroupButton = new RolloverButton(SparkRes.getImageIcon(SparkRes.ADD_CONTACT_IMAGE));
         chatMenu = new JMenuItem(Res.getString("menuitem.start.a.chat"), SparkRes.getImageIcon(SparkRes.SMALL_MESSAGE_IMAGE));
         toolbar.add(addingGroupButton);
         addingGroupButton.addActionListener(this);
 
         addContactMenu.addActionListener(this);
         chatMenu.addActionListener(this);
         //remove和rename功能设定，去除于2013年8月29日10:21:46
 //        removeContactFromGroupMenu = new JMenuItem(Res.getString("menuitem.remove.from.group"), SparkRes.getImageIcon(SparkRes.SMALL_DELETE));
 //        renameMenu = new JMenuItem(Res.getString("menuitem.rename"), SparkRes.getImageIcon(SparkRes.DESKTOP_IMAGE));
 //        removeContactFromGroupMenu.addActionListener(this);
 //        renameMenu.addActionListener(this);
 
 
         setLayout(new BorderLayout());
 
         mainPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
         mainPanel.setBackground((Color) UIManager.get("ContactGItem.background"));
 
         contactListScrollPane = new JScrollPane(mainPanel);
         contactListScrollPane.setAutoscrolls(true);
 
         contactListScrollPane.setBorder(BorderFactory.createEmptyBorder());
         contactListScrollPane.getVerticalScrollBar().setBlockIncrement(200);
         contactListScrollPane.getVerticalScrollBar().setUnitIncrement(20);
 
         add(contactListScrollPane, BorderLayout.CENTER);
 
         //todo:属性文件部分，不确保好使，2013年8月27日14:33:22
         // Load Properties file
 //        props = new Properties();
 //        // Save to properties file.
 //        propertiesFile = new File(Spark.getSparkUserHome() + "/contactggroups.properties");
 //        try {
 //            props.load(new FileInputStream(propertiesFile));
 //        }
 //        catch (IOException e) {
 //            e.printStackTrace();
 //            // File does not exist.
 //        }
         //unfiledGroup测试分组,2013年9月2日10:04:35
 //        this.addContactGGroup(unfiledGroup);
 //        System.out.println("unfiledGroup Added");
 
 
     }
 
     private List<ContactGItem> contactGItems = new ArrayList<ContactGItem>();
 
     private void addContactGItemListToGroup(List<ContactGItem> contactGItems) {
         ListIterator<ContactGItem> contactGItemListIterator = contactGItems.listIterator();
         while (contactGItemListIterator.hasNext()) {
             ContactGItem next = contactGItemListIterator.next();
             addContactGItemToGroup(next);
         }
     }
 
     private void addContactGItemToGroup(ContactGItem contactGItem) {
         String groupName = contactGItem.getGroupName();
         if (null != groupName) {
             System.out.println("Now insert item " + contactGItem.getFullyQualifiedJID() + " to group " + contactGItem.getGroupName());
             ContactGGroup contactGGroup = getContactGGroup(groupName);
             if (null != contactGGroup) {
                 System.out.println("Already found " + contactGGroup.getGroupName() + " to insert.");
                 contactGGroup.addContactGItem(contactGItem);
             } else {
                 System.out.println("There hasn't some group named " + contactGGroup.getGroupName() + " to insert.");
             }
         } else {
             System.out.println("This " + contactGItem.getFullyQualifiedJID() + " hasn't a group name.");
         }
 
     }
 
     private void addContactGGroupToGroup(ContactGGroup child, ContactGGroup father) {
         father.addContactGGroup(child);
         child.addContactGGroupListener(this);
         fireContactGGroupAdded(child);
     }
 
     //gGroups，2013年9月2日10:12:59
     private void addContactGGroupFromList(List<ContactGGroup> gGroups) {
 //        for (int i = 0; i < gGroups.size(); i++) {
 //            ContactGGroup contactGGroup = gGroups.get(i);
 ////            System.out.println(contactGGroup.toString());
 //            //如果是根分组就直接添加在根目录下面，判断是是否存在重名
 //            if ("root".equals(contactGGroup.getParentGroup())) {
 //                if (null == getContactGGroup(contactGGroup.getGroupName())) {
 //                    this.addContactGGroup(contactGGroup);
 //                }
 //            } else {
 //                //如果不是跟分组，就查找的父亲分组，然后添加。不存在父亲分组，则创建父亲分组,在跟分组下面
 //                if (null != contactGGroup.getParentGroup()) {
 //                    this.addContactGGroupToGroup(contactGGroup, getContactGGroup(contactGGroup.getParentGroup()));
 //                } else {
 //                    this.addContactGGroup(new ContactGGroup(contactGGroup.getParentGroup()));
 //                }
 //            }
         //以上思路不好，重新编写，2013年9月2日10:16:07
         List<ContactGGroup> contactGGroups = new ArrayList<ContactGGroup>(gGroups);
         ListIterator<ContactGGroup> contactGGroupListIterator = contactGGroups.listIterator();
         while (contactGGroupListIterator.hasNext()) {
             ContactGGroup contactGGroup = contactGGroupListIterator.next();
             System.out.println("Start insert group:" + contactGGroup.getGroupName());
 //            System.out.println(contactGGroup.toString());
             //如果是根分组就直接添加在根目录下面，判断是是否存在重名
             if (null == contactGGroup.getParentGroup()) {
                 System.out.println("This group is First line:" + contactGGroup.getGroupName());
                 if (null == getContactGGroup(contactGGroup.getGroupName())) {
                     System.out.println("This group hasn't inserted before:" + contactGGroup.getGroupName());
                     this.addContactGGroup(contactGGroup);
                 }
 //                contactGGroups.remove(contactGGroup);
                 contactGGroupListIterator.remove();
             }
         }
         //不停递归，取得子分组，然后删除掉，直到所有分组全部取出，2013年9月2日10:22:01
         //用来确保所有分组正确取出，无论分组在grouplist中排序如何。
         //已处理2013年9月2日10:40:54:如何判断一个分组一定有父亲分组，否则这个循环不会跳出，BUG。
         ListIterator<ContactGGroup> listIterator = contactGGroups.listIterator();
         while (listIterator.hasNext()) {
             ContactGGroup noParent = null;
             while (listIterator.hasNext()) {
                 ContactGGroup contactGGroup = listIterator.next();
                 //查找的父亲分组，然后添加。
                 if (null != contactGGroup.getParentGroup() && null != getContactGGroup(contactGGroup.getParentGroup())) {
                     System.out.println("Insert " + contactGGroup.getGroupName() + " to " + contactGGroup.getParentGroup());
                     this.addContactGGroupToGroup(contactGGroup, getContactGGroup(contactGGroup.getParentGroup()));
 //                    contactGGroups.remove(contactGGroup);
                     listIterator.remove();
                 } else {
                     noParent = contactGGroup;
                 }
             }
 
             //做个处理，如果某一个分组的父分组确实不存在与已有分组和grouplist中。删除掉，防止无限循环。2013年9月2日10:29:16
             if (null != noParent && null != getContactGGroup(noParent.getParentGroup())) {
                 listIterator = contactGGroups.listIterator();
                 int flag = 0;
                 while (listIterator.hasNext()) {
                     ContactGGroup contactGGroup = listIterator.next();
                     if (contactGGroup.getGroupName().equals(noParent.getParentGroup())) {
                         flag = 1;
                         break;
                     }
                 }
                 if (flag == 0) {
                     System.out.println("This group hasn't a father:" + noParent.getGroupName());
                     //这里遍历已经结束，所以只能用list.remove，删除。2013年9月3日10:03:16
                     contactGGroups.remove(noParent);
 //                    listIterator.remove();
                 }
             }
         }
     }
 
     /**
      * Adds a new ContactGGroup to the ContactGList.
      *
      * @param group the group to add.
      */
     private void addContactGGroup(ContactGGroup group) {
         groupList.add(group);
         //分组排序功能，尚未完成，2013年8月27日14:27:08
 //        Collections.sort(groupList, GROUP_COMPARATOR);
         System.out.println("addContactGGroup");
         try {
             mainPanel.add(group, groupList.indexOf(group));
             System.out.println("addContactGGroup Done");
         } catch (Exception e) {
             Log.error(e);
             System.out.println("addContactGGroup Error");
         }
         group.addContactGGroupListener(this);
         fireContactGGroupAdded(group);
         // Check state
 //        String prop = props.getProperty(group.getGroupName());
 //        if (prop != null) {
 //            boolean isCollapsed = Boolean.valueOf(prop);
 //            group.setCollapsed(isCollapsed);
 //        }
     }
 
     private Properties props;
 
     /**
      * Returns a ContactGGroup based on its name.
      *
      * @param groupName the name of the ContactGGroup.
      * @return the ContactGGroup. If no ContactGGroup is found, null is returned.
      */
     public ContactGGroup getContactGGroup(String groupName) {
         ContactGGroup cGroup = null;
 
         for (ContactGGroup contactGGroup : groupList) {
             if (contactGGroup.getGroupName().equals(groupName)) {
                 cGroup = contactGGroup;
                 break;
             } else {
                 cGroup = getSubContactGGroup(contactGGroup, groupName);
                 if (cGroup != null) {
                     break;
                 }
             }
         }
 
         return cGroup;
     }
 
     /**
      * Returns the nested ContactGGroup of a given ContactGGroup with associated name.
      *
      * @param group     the parent ContactGGroup.
      * @param groupName the name of the nested group.
      * @return the nested ContactGGroup. If not found, null will be returned.
      */
     private ContactGGroup getSubContactGGroup(ContactGGroup group, String groupName) {
         //测试寻找字类是否好使，2013年8月28日14:49:42
         System.out.println("getSubContactGGroup:" + groupName);
 
         final Iterator<ContactGGroup> contactGGroups = group.getContactGGroups().iterator();
         ContactGGroup grp = null;
 
         while (contactGGroups.hasNext()) {
             ContactGGroup contactGGroup = contactGGroups.next();
             if (contactGGroup.getGroupName().equals(groupName)) {
                 grp = contactGGroup;
                 break;
             } else if (contactGGroup.getContactGGroups().size() > 0) {
                 grp = getSubContactGGroup(contactGGroup, groupName);
                 if (grp != null) {
                     break;
                 }
             }
 
         }
         return grp;
     }
 
     /**
      * Sorts ContactGGroups
      */
     public static final Comparator<ContactGGroup> GROUP_COMPARATOR = new Comparator<ContactGGroup>() {
         public int compare(ContactGGroup group1, ContactGGroup group2) {
 
 
             return group1.getGroupName().trim().toLowerCase().compareTo(group2.getGroupName().trim().toLowerCase());
         }
     };
 
     public JPanel getMainPanel() {
         return mainPanel;
     }
 
     private final RolloverButton addingGroupButton;
 
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == addingGroupButton) {
             new RosterDialog().showRosterDialog();
         } else if (e.getSource() == chatMenu) {
             if (activeItem != null) {
                 SparkManager.getChatManager().activateChat(activeItem.getJID(), activeItem.getDisplayName());
             }
         } else if (e.getSource() == addContactMenu) {
             RosterDialog rosterDialog = new RosterDialog();
             if (activeGroup != null) {
                 rosterDialog.setDefaultGroup(activeGroup.toContactGroup());
             }
             rosterDialog.showRosterDialog();
         }
         //删除Contact对象，去除此功能。2013年8月29日9:49:07
 //    else if (e.getSource() == removeContactFromGroupMenu) {
 //        if (activeItem != null) {
 //            removeContactFromGroup(activeItem);
 //        }
 //    }
         //去除rename功能设定，2013年8月29日10:21:09
 
 //        else if (e.getSource() == renameMenu) {
 //            if (activeItem == null) {
 //                return;
 //            }
 //
 //            String oldAlias = activeItem.getAlias();
 //            String newAlias = JOptionPane.showInputDialog(this, Res.getString("label.rename.to") + ":", oldAlias);
 //
 //            // if user pressed 'cancel', output will be null.
 //            // if user removed alias, output will be an empty String.
 //            if (newAlias != null) {
 //                if (!ModelUtil.hasLength(newAlias)) {
 //                    newAlias = null; // allows you to remove an alias.
 //                }
 //
 //                String address = activeItem.getJID();
 //                ContactGGroup contactGGroup = getContactGGroup(activeItem.getGroupName());
 //                ContactGItem contactGItem = contactGGroup.getContactGItemByDisplayName(activeItem.getDisplayName());
 //                contactGItem.setAlias(newAlias);
 //
 //                final Roster roster = SparkManager.getConnection().getRoster();
 //                RosterEntry entry = roster.getEntry(address);
 //                entry.setName(newAlias);
 //
 //
 //                final Iterator<ContactGGroup> contactGGroups = groupList.iterator();
 //                String user = StringUtils.parseBareAddress(address);
 //                while (contactGGroups.hasNext()) {
 //                    ContactGGroup cg = contactGGroups.next();
 //                    ContactGItem ci = cg.getContactGItemByJID(user);
 //                    if (ci != null) {
 //                        ci.setAlias(newAlias);
 //                    }
 //                }
 //            }
 //        }
     }
 
     public void connectionClosed() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void connectionClosedOnError(Exception e) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void reconnectingIn(int i) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void reconnectionSuccessful() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void reconnectionFailed(Exception e) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     //特殊分组，暂且去除,2013年8月28日13:54:22
 //    private ContactGGroup getUnfiledGroup() {
 //        if (unfiledGroup == null) {
 //            // Add Unfiled Group
 //            if(EventQueue.isDispatchThread()) {
 //                unfiledGroup = UIComponentRegistry.createContactGGroup(Res.getString("unfiled"));
 //                addContactGGroup(unfiledGroup);
 //            }
 //            else {
 //                try {
 //                    EventQueue.invokeAndWait(new Runnable(){
 //                        public void run() {
 //                            unfiledGroup = UIComponentRegistry.createContactGGroup(Res.getString("unfiled"));
 //                            addContactGGroup(unfiledGroup);
 //                        }
 //                    });
 //                }catch(Exception ex) {
 //                    ex.printStackTrace();
 //                }
 //            }
 //        }
 //        return unfiledGroup;
 //    }
     private void removeContactFromRoster(ContactGItem item) {
         Roster roster = SparkManager.getConnection().getRoster();
         RosterEntry entry = roster.getEntry(item.getJID());
         if (entry != null) {
             try {
                 roster.removeEntry(entry);
             } catch (XMPPException e) {
                 Log.warning("Unable to remove roster entry.", e);
             }
         }
     }
 
     public JComponent getGUI() {
         return this;
     }
 
     private JMenuItem chatMenu;
 //    private JMenuItem removeContactFromGroupMenu;
 //    private JMenuItem renameMenu;
 
     /**
      * Shows popup for right-clicking of ContactGItem.
      *
      * @param e         the MouseEvent
      * @param item      the ContactGItem
      * @param component the owning component
      */
     public void showPopup(Component component, MouseEvent e, final ContactGItem item) {
         if (item.getJID() == null) {
             return;
         }
 
         activeItem = item;
 
         final JPopupMenu popup = new JPopupMenu();
 
         // Add Start Chat Menu
         popup.add(chatMenu);
 
         // Add Send File Action
         Action sendAction = new AbstractAction() {
             private static final long serialVersionUID = -7519717310558205566L;
 
             public void actionPerformed(ActionEvent actionEvent) {
                 SparkManager.getTransferManager().sendFileTo(item.toContactItem());
             }
         };
 
         sendAction.putValue(Action.SMALL_ICON, SparkRes.getImageIcon(SparkRes.DOCUMENT_16x16));
         sendAction.putValue(Action.NAME, Res.getString("menuitem.send.a.file"));
 
         if (item.getPresence() != null) {
             popup.add(sendAction);
         }
 
         popup.addSeparator();
 
 
         String groupName = item.getGroupName();
         ContactGGroup contactGGroup = getContactGGroup(groupName);
 //去除removeContact，2013年8月29日10:19:47
 //        // Only show "Remove Contact From Group" if the user belongs to more than one group.
 //        if (!contactGGroup.isSharedGroup()) {
 //            Roster roster = SparkManager.getConnection().getRoster();
 //            RosterEntry entry = roster.getEntry(item.getJID());
 //            if (entry != null) {
 //                int groupCount = entry.getGroups().size();
 //
 //                //It should be possible to remove a user from the only group they're in
 //
 //                //which would put them into the unfiled group.
 //
 //                if (groupCount > 1) {
 //                    popup.add(removeContactFromGroupMenu);
 //                }
 //
 //            }
 //        }
 
 //        // Define remove entry action
 //        Action removeAction = new AbstractAction() {
 //            private static final long serialVersionUID = -2565914214685979320L;
 //
 //            public void actionPerformed(ActionEvent e) {
 //                removeContactFromRoster(item);
 //            }
 //        };
 //
 //        removeAction.putValue(Action.NAME, Res.getString("menuitem.remove.from.roster"));
 //        removeAction.putValue(Action.SMALL_ICON, SparkRes.getImageIcon(SparkRes.SMALL_CIRCLE_DELETE));
 //
 //        // Check if user is in shared group.
 //        boolean isInSharedGroup = false;
 //        for (ContactGGroup cGroup : new ArrayList<ContactGGroup>(getContactGGroups())) {
 //            if (cGroup.isSharedGroup()) {
 //                ContactGItem it = cGroup.getContactGItemByJID(item.getJID());
 //                if (it != null) {
 //                    isInSharedGroup = true;
 //                }
 //            }
 //        }
 //
 //
 //        if (!contactGGroup.isSharedGroup() && !isInSharedGroup) {
 //            popup.add(removeAction);
 //        }
 
 //        popup.add(renameMenu);
 
 
         Action viewProfile = new AbstractAction() {
             private static final long serialVersionUID = -2562731455090634805L;
 
             public void actionPerformed(ActionEvent e) {
                 VCardManager vcardSupport = SparkManager.getVCardManager();
                 String jid = item.getJID();
                 vcardSupport.viewProfile(jid, SparkManager.getWorkspace());
             }
         };
         viewProfile.putValue(Action.SMALL_ICON, SparkRes.getImageIcon(SparkRes.PROFILE_IMAGE_16x16));
         viewProfile.putValue(Action.NAME, Res.getString("menuitem.view.profile"));
 
         popup.add(viewProfile);
 
 
         popup.addSeparator();
 
         Action lastActivityAction = new AbstractAction() {
             private static final long serialVersionUID = -4884230635430933060L;
 
             public void actionPerformed(ActionEvent actionEvent) {
                 try {
                     String client = "";
                     if (item.getPresence().getType() != Presence.Type.unavailable) {
                         client = item.getPresence().getFrom();
                         if ((client != null) && (client.lastIndexOf("/") != -1)) {
                             client = client.substring(client.lastIndexOf("/"));
                         } else client = "/";
                     }
 
                     LastActivity activity = LastActivityManager.getLastActivity(SparkManager.getConnection(), item.getJID() + client);
                     long idleTime = (activity.getIdleTime() * 1000);
                     String time = ModelUtil.getTimeFromLong(idleTime);
                     JOptionPane.showMessageDialog(getGUI(), Res.getString("message.idle.for", time), Res.getString("title.last.activity"), JOptionPane.INFORMATION_MESSAGE);
                 } catch (Exception e1) {
                     JOptionPane.showMessageDialog(getGUI(), Res.getString("message.unable.to.retrieve.last.activity", item.getJID()), Res.getString("title.error"), JOptionPane.ERROR_MESSAGE);
                 }
 
             }
         };
 
         lastActivityAction.putValue(Action.NAME, Res.getString("menuitem.view.last.activity"));
         lastActivityAction.putValue(Action.SMALL_ICON, SparkRes.getImageIcon(SparkRes.SMALL_USER1_STOPWATCH));
 
         if (item.getPresence().isAway() || (item.getPresence().getType() == Presence.Type.unavailable) || (item.getPresence().getType() == null)) {
             popup.add(lastActivityAction);
         }
 
         Action subscribeAction = new AbstractAction() {
             private static final long serialVersionUID = -7754905015338902300L;
 
             public void actionPerformed(ActionEvent e) {
                 String jid = item.getJID();
                 Presence response = new Presence(Presence.Type.subscribe);
                 response.setTo(jid);
 
                 SparkManager.getConnection().sendPacket(response);
             }
         };
 
         subscribeAction.putValue(Action.SMALL_ICON, SparkRes.getImageIcon(SparkRes.SMALL_USER1_INFORMATION));
         subscribeAction.putValue(Action.NAME, Res.getString("menuitem.subscribe.to"));
 
         Roster roster = SparkManager.getConnection().getRoster();
         RosterEntry entry = roster.getEntry(item.getJID());
         if (entry != null && entry.getType() == RosterPacket.ItemType.from) {
             popup.add(subscribeAction);
         } else if (entry != null && entry.getType() != RosterPacket.ItemType.both && entry.getStatus() == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING) {
             popup.add(subscribeAction);
         }
 
         // Fire Context Menu Listener
         fireContextMenuListenerPopup(popup, item);
 
         ContactGGroup group = getContactGGroup(item.getGroupName());
         if (component == null) {
             popup.show(group.getList(), e.getX(), e.getY());
         } else {
             popup.show(component, e.getX(), e.getY());
             popup.requestFocus();
         }
     }
 
     public void showPopup(MouseEvent e, final Collection<ContactGItem> items) {
         ContactGGroup group = null;
         for (ContactGItem item : items) {
             group = getContactGGroup(item.getGroupName());
             break;
         }
 
 
         final JPopupMenu popup = new JPopupMenu();
         final JMenuItem sendMessagesMenu = new JMenuItem(Res.getString("menuitem.send.a.message"), SparkRes.getImageIcon(SparkRes.SMALL_MESSAGE_IMAGE));
 
 
         fireContextMenuListenerPopup(popup, items);
 
         popup.add(sendMessagesMenu);
 
         sendMessagesMenu.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 sendMessages(items);
             }
         });
 
         try {
             popup.show(group.getList(), e.getX(), e.getY());
         } catch (NullPointerException ee) {
             // Nothing we can do here
         }
     }
 
     private void sendMessages(Collection<ContactGItem> items) {
         StringBuffer buf = new StringBuffer();
         InputDialog dialog = new InputDialog();
         final String messageText = dialog.getInput(Res.getString("title.broadcast.message"), Res.getString("message.enter.broadcast.message"), SparkRes.getImageIcon(SparkRes.BLANK_IMAGE), SparkManager.getMainWindow());
         if (ModelUtil.hasLength(messageText)) {
 
             final Map<String, Message> broadcastMessages = new HashMap<String, Message>();
             for (ContactGItem item : items) {
                 final Message message = new Message();
                 message.setTo(item.getJID());
                 message.setProperty("broadcast", true);
                 message.setBody(messageText);
                 if (!broadcastMessages.containsKey(item.getJID())) {
                     buf.append(item.getDisplayName()).append("\n");
                     broadcastMessages.put(item.getJID(), message);
                 }
             }
 
             for (Message message : broadcastMessages.values()) {
                 SparkManager.getConnection().sendPacket(message);
             }
 
             JOptionPane.showMessageDialog(SparkManager.getMainWindow(), Res.getString("message.broadcasted.to", buf.toString()), Res.getString("title.notification"), JOptionPane.INFORMATION_MESSAGE);
         }
 
 
     }
 
     private final List<ContextMenuListener> contextListeners = new ArrayList<ContextMenuListener>();
 
     public void fireContextMenuListenerPopup(JPopupMenu popup, Object object) {
         for (ContextMenuListener listener : new ArrayList<ContextMenuListener>(contextListeners)) {
             listener.poppingUp(object, popup);
         }
     }
 
     public void showPopup(MouseEvent e, final ContactGItem item) {
         showPopup(null, e, item);
     }
 
 
     public void contactGGroupPopup(MouseEvent e, ContactGGroup group) {
         //本程序没有offlineGroup    ，故而删除
 //        // Do nothing with offline group
 //        if (group == offlineGroup || group == getUnfiledGroup()) {
 //            return;
 //        }
 
 
         final JPopupMenu popup = new JPopupMenu();
         if (!Default.getBoolean(Default.ADD_CONTACT_DISABLED)) {
             popup.add(addContactMenu);
         }
 
         if (!Default.getBoolean("ADD_CONTACT_GROUP_DISABLED")) {
             popup.add(addContactGGroupMenu);
         }
         popup.addSeparator();
 
         fireContextMenuListenerPopup(popup, group);
 
         JMenuItem delete = new JMenuItem(Res.getString("menuitem.delete"));
         JMenuItem rename = new JMenuItem(Res.getString("menuitem.rename"));
         JMenuItem expand = new JMenuItem(Res.getString("menuitem.expand.all.groups"));
         JMenuItem collapse = new JMenuItem(Res.getString("menuitem.collapse.all.groups"));
 
         if (!group.isSharedGroup()) {
             popup.addSeparator();
             popup.add(delete);
             popup.add(rename);
         }
 
         popup.addSeparator();
         popup.add(expand);
         popup.add(collapse);
         //下面代码是用来delete和rename的，不需要，故而删除。2013年8月29日10:06:14
 //        delete.addActionListener(new ActionListener() {
 //            public void actionPerformed(ActionEvent e) {
 //                int ok = JOptionPane.showConfirmDialog(group, Res.getString("message.delete.confirmation", group.getGroupName()), Res.getString("title.confirmation"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 //                if (ok == JOptionPane.YES_OPTION) {
 //                    String groupName = group.getGroupName();
 //                    Roster roster = SparkManager.getConnection().getRoster();
 //
 //                    RosterGroup rosterGroup = roster.getGroup(groupName);
 //                    if (rosterGroup != null) {
 //                        for (RosterEntry entry : rosterGroup.getEntries()) {
 //                            try {
 //                                rosterGroup.removeEntry(entry);
 //                            }
 //                            catch (XMPPException e1) {
 //                                Log.error("Error removing entry", e1);
 //                            }
 //                        }
 //                    }
 //
 //                    // Remove from UI
 //                    removeContactGGroup(group);
 //                    invalidate();
 //                    repaint();
 //                }
 //
 //            }
 //        });
 //
 //
 //        rename.addActionListener(new ActionListener() {
 //            public void actionPerformed(ActionEvent e) {
 //                String newName = JOptionPane.showInputDialog(group, Res.getString("label.rename.to") + ":", Res.getString("title.rename.roster.group"), JOptionPane.QUESTION_MESSAGE);
 //                if (!ModelUtil.hasLength(newName)) {
 //                    return;
 //                }
 //                String groupName = group.getGroupName();
 //                Roster roster = SparkManager.getConnection().getRoster();
 //
 //                RosterGroup rosterGroup = roster.getGroup(groupName);
 //                //Do not remove ContactGGroup if the name entered was the same
 //                if (rosterGroup != null && !groupName.equals(newName)) {
 //                    removeContactGGroup(group);
 //                    rosterGroup.setName(newName);
 //                    addContactGGroup(newName);
 //                    toggleGroupVisibility(newName, true);
 //                    getContactGGroup(newName).setCollapsed( group.isCollapsed());
 //                }
 //
 //            }
 //        });
         expand.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Collection<ContactGGroup> groups = getContactGGroups();
                 for (ContactGGroup group : groups) {
                     group.setCollapsed(false);
                 }
             }
         });
 
         collapse.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Collection<ContactGGroup> groups = getContactGGroups();
                 for (ContactGGroup group : groups) {
                     group.setCollapsed(true);
                 }
             }
         });
 
         // popup.add(inviteFirstAcceptor);
         popup.show(group, e.getX(), e.getY());
 
         activeGroup = group;
     }
 
     /**
      * For traversing of a nested group. Allows users to find the owning parent of a given contact group.
      *
      * @param groupName the name of the nested contact group.
      * @return the parent ContactGGroup. If no parent, null will be returned.
      */
     public ContactGGroup getParentGroup(String groupName) {
         // Check if there is even a parent group
         if (!groupName.contains("::")) {
             return null;
         }
 
         final ContactGGroup group = getContactGGroup(groupName);
         if (group == null) {
             return null;
         }
 
         // Otherwise, find parent
         int index = groupName.lastIndexOf("::");
         String parentGroupName = groupName.substring(0, index);
         return getContactGGroup(parentGroupName);
     }
 
     /**
      * Removes a ContactGGroup from the group model and ContactList.
      *
      * @param contactGGroup the ContactGGroup to remove.
      */
     private void removeContactGGroup(ContactGGroup contactGGroup) {
         contactGGroup.removeContactGGroupListener(this);
         groupList.remove(contactGGroup);
         mainPanel.remove(contactGGroup);
 
         ContactGGroup parent = getParentGroup(contactGGroup.getGroupName());
         if (parent != null) {
             parent.removeContactGGroup(contactGGroup);
         }
 
         contactListScrollPane.validate();
         mainPanel.invalidate();
         mainPanel.repaint();
 
         fireContactGGroupRemoved(contactGGroup);
     }
 
     private void addContactGListToWorkspace() {
         Workspace workspace = SparkManager.getWorkspace();
 //        workspace.getWorkspacePane().addTab("My Tab",null,new JButton("Hello"));
        workspace.getWorkspacePane().addTab("组织结构分组", null, this);
     }
 
     public void initialize() {
 
         System.out.println();
         System.out.println("Welcome To Spark");
         // Add Contact List
         addContactGListToWorkspace();
         List<ContactGGroup> gGroups = new ArrayList<ContactGGroup>();
 
         //锁定网址的条件下测试是否可以把分组读取出来。2013年9月2日10:47:58
         try {
             gGroups = getGroupListFromJson(targetGroupURL);
         } catch (IOException e) {
             e.printStackTrace();
         }
 //        System.out.println("groupList" + groupList.toString());
         addContactGGroupFromList(gGroups);
         //获取 contactGItems 从指定URL，2013年9月3日13:47:05 s
         try {
             contactGItems = getContactsListFromJson(targetContactsURL);
         } catch (IOException e) {
             e.printStackTrace();
         }
         //将contactGItems添加进入分组
         addContactGItemListToGroup(contactGItems);
 
         //测试分组，2013年9月2日10:02:52
 //        ContactGGroup contactGGroup1 = new ContactGGroup("Group 1");
 //        ContactGGroup contactGGroup2 = new ContactGGroup("Group 2");
 //        ContactGGroup contactGGroup3 = new ContactGGroup("Group 3");
 //        ContactGGroup contactGGroup4 = new ContactGGroup("Group 4");
 //        ContactGGroup contactGGroup41 = new ContactGGroup("Group 41");
 //        ContactGGroup contactGGroup42 = new ContactGGroup("Group 42");
 //        ContactGGroup contactGGroup43 = new ContactGGroup("Group 43");
 //        ContactGGroup contactGGroup431 = new ContactGGroup("Group 431");
 //        ContactGGroup contactGGroup432 = new ContactGGroup("Group 432");
 //        ContactGGroup contactGGroup4321 = new ContactGGroup("Group 4321");
 //        ContactGGroup contactGGroup4322 = new ContactGGroup("Group 4322");    //
 //        this.addContactGGroup(contactGGroup1);
 //        this.addContactGGroup(contactGGroup2);
 //        this.addContactGGroup(contactGGroup3);
 //        this.addContactGGroup(contactGGroup4);
 //        this.addContactGGroupToGroup(contactGGroup41, contactGGroup4);
 //        this.addContactGGroupToGroup(contactGGroup42, contactGGroup4);
 //        this.addContactGGroupToGroup(contactGGroup43, contactGGroup4);
 //        this.addContactGGroupToGroup(contactGGroup431, contactGGroup43);
 //        this.addContactGGroupToGroup(contactGGroup432, contactGGroup43);
 //        this.addContactGGroupToGroup(contactGGroup4321, contactGGroup432);
 //        this.addContactGGroupToGroup(contactGGroup4322, contactGGroup432);
 
         //尝试新方法，添加子分组，同时添加事件。
         //测试好使，2013年8月27日15:43:05，不要使用addContactGGroup添加子分组，用addContactGGroupToGroup
 //        contactGGroup4.addContactGGroup(contactGGroup41);
 //        contactGGroup4.addContactGGroup(contactGGroup42);
 //        contactGGroup4.addContactGGroup(contactGGroup43);
 //        contactGGroup43.addContactGGroup(contactGGroup431);
 //        contactGGroup43.addContactGGroup(contactGGroup432);
 //        contactGGroup432.addContactGGroup(contactGGroup4321);
 //        contactGGroup432.addContactGGroup(contactGGroup4322);
 
         //测试item，2013年9月2日10:02:37
 //        ContactGItem contactGItem = new ContactGItem("a", "a", "a@berserker");
 //        ContactGItem contactGItem1 = new ContactGItem("b", "b", "b@berserker");
 //        ContactGItem contactGItem2 = new ContactGItem("c", "c", "c@berserker");
 //        ContactGItem contactGItem3 = new ContactGItem("d", "d", "d@berserker");
 //        ContactGItem contactGItem4 = new ContactGItem("e", "e", "e@berserker");
 //        ContactGItem contactGItem5 = new ContactGItem("f", "f", "f@berserker");
 //        ContactGItem contactGItem6 = new ContactGItem("g", "g", "g@berserker");
 //        ContactGItem contactGItem7 = new ContactGItem("h", "h", "h@berserker");      //
 //        contactGGroup1.addContactGItem(contactGItem);
 //        contactGGroup2.addContactGItem(contactGItem1);
 //        contactGGroup3.addContactGItem(contactGItem2);
 //        contactGGroup4.addContactGItem(contactGItem3);
 //        contactGGroup41.addContactGItem(contactGItem4);
 //        contactGGroup42.addContactGItem(contactGItem5);
 //        contactGGroup43.addContactGItem(contactGItem6);
 //        contactGGroup431.addContactGItem(contactGItem7);
 
 
         this.setVisible(true);
 
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
             }
         });
 
     }
 
     // 最简单的HTTP客户端,用来演示通过GET或者POST方式访问某个页面
     public List<ContactGGroup> getGroupListFromJson(String url) throws IOException {
 
         HttpClient client = new HttpClient();
 
         //设置代理服务器地址和端口
         //client.getHostConfiguration().setProxy("proxy_host_addr",proxy_port);
 
         //使用GET方法，如果服务器需要通过HTTPS连接，那只需要将下面URL中的http换成https
         HttpMethod method = new GetMethod(url);
 
         //使用POST方法
         //HttpMethod method = new PostMethod(url);
 
         client.executeMethod(method);
 
         //打印服务器返回的状态
         StatusLine states = method.getStatusLine();
         System.out.println(states);
 
 
         //打印返回的信息
         String responseBodyAsString = method.getResponseBodyAsString();
         System.out.println(responseBodyAsString);
 
         //释放连接
         method.releaseConnection();
 
         /*
         测试gson用，2013年9月2日13:00:45
         List<ContactsGGroupBean> contactGGroupsTest = new ArrayList<ContactsGGroupBean>();
         contactGGroupsTest.add(new ContactsGGroupBean("a", "root"));
         contactGGroupsTest.add(new ContactsGGroupBean("b", "a"));
         //        System.out.println(new GsonBuilder().create().toJson(new ContactGGroup("c","root"))); //测试输出json是否报错，2013年9月2日14:23:52
         responseBodyAsString = new GsonBuilder().create().toJson(contactGGroupsTest, new TypeToken<List<ContactsGGroupBean>>() {
         }.getType());
         System.out.println(responseBodyAsString);
         转换json对象为List对象
         */
 
 
         Gson gson = new GsonBuilder().create();
         List<ContactsGGroupBean> contactGGroupBeans = gson.fromJson(responseBodyAsString, new TypeToken<List<ContactsGGroupBean>>() {
         }.getType());
         System.out.println(contactGGroupBeans);
 
 //       转换ContactsGGroupBean为ContactsGGroup
         List<ContactGGroup> contactGGroups = new ArrayList<ContactGGroup>();
         for (int i = 0; i < contactGGroupBeans.size(); i++) {
             ContactsGGroupBean contactsGGroupBean = contactGGroupBeans.get(i);
             contactGGroups.add(contactsGGroupBean.toContactGGroup());
 //            System.out.println(contactsGGroupBean.toString());
         }
 
         return contactGGroups;
     }
 
     public List<ContactGItem> getContactsListFromJson(String url) throws IOException {
 
         HttpClient client = new HttpClient();
 
         //设置代理服务器地址和端口
         //client.getHostConfiguration().setProxy("proxy_host_addr",proxy_port);
 
         //使用GET方法，如果服务器需要通过HTTPS连接，那只需要将下面URL中的http换成https
         HttpMethod method = new GetMethod(url);
 
         //使用POST方法
         //HttpMethod method = new PostMethod(url);
 
         client.executeMethod(method);
 
         //打印服务器返回的状态
         StatusLine states = method.getStatusLine();
         System.out.println(states);
 
 
         //打印返回的信息
         String responseBodyAsString = method.getResponseBodyAsString();
         System.out.println(responseBodyAsString);
 
         //释放连接
         method.releaseConnection();
 
         Gson gson = new GsonBuilder().create();
         List<ContactsGBean> contactGGroupBeans = gson.fromJson(responseBodyAsString, new TypeToken<List<ContactsGBean>>() {
         }.getType());
         System.out.println(contactGGroupBeans);
 
 //       转换ContactsGBean为ContactsGGroup
         List<ContactGItem> contactGItems = new ArrayList<ContactGItem>();
         for (int i = 0; i < contactGGroupBeans.size(); i++) {
             ContactsGBean contactsGBean = contactGGroupBeans.get(i);
             contactGItems.add(contactsGBean.toContactGItem());
 //            System.out.println(contactsGGroupBean.toString());
         }
 
         return contactGItems;
     }
 
     public void shutdown() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean canShutDown() {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void uninstall() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void entriesAdded(Collection collection) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void entriesUpdated(Collection collection) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void entriesDeleted(Collection collection) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
 
     public void presenceChanged(Presence presence) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private ContactGItem activeItem;
     private ContactGGroup activeGroup;
 
     public ContactGGroup getActiveGroup() {
         return activeGroup;
     }
 
     public void contactGItemDoubleClicked(ContactGItem item) {
         activeItem = item;
 
         System.out.println("Start contactGItemDoubleClicked");
 
         ChatManager chatManager = SparkManager.getChatManager();
         //todo:chatManager 里面没有好用的方法，尚未找到解决办法，2013年8月27日10:52:21
 //        boolean handled = chatManager.fireContactGItemDoubleClicked(item);
 
 //        if (!handled) {
         chatManager.activateChat(item.getJID(), item.getDisplayName());
 //        }
 
         clearSelectionList(item);
 
         fireContactGItemDoubleClicked(item);
     }
 
     public static KeyEvent activeKeyEvent;
 
     public void contactGItemClicked(ContactGItem item) {
         activeItem = item;
 
         if (activeKeyEvent == null || ((activeKeyEvent.getModifiers() & KeyEvent.CTRL_MASK) == 0)) {
             clearSelectionList(item);
         }
 
 
         fireContactGItemClicked(item);
         activeKeyEvent = null;
     }
 
     private void clearSelectionList(ContactGItem selectedItem) {
         // Check for null. In certain cases the event triggering the model might
         // not find the selected object.
         if (selectedItem == null) {
             return;
         }
 
         final ContactGGroup owner = getContactGGroup(selectedItem.getGroupName());
         for (ContactGGroup contactGGroup : new ArrayList<ContactGGroup>(groupList)) {
             if (owner != contactGGroup) {
                 contactGGroup.clearSelection(selectedItem);
             }
         }
     }
 
 
     public List<ContactGGroup> getContactGGroups() {
         final List<ContactGGroup> gList = new ArrayList<ContactGGroup>(groupList);
         Collections.sort(gList, GROUP_COMPARATOR);
         return gList;
     }
 
     private final List<FileDropListener> dndListeners = new ArrayList<FileDropListener>();
 
     public void addFileDropListener(FileDropListener listener) {
         dndListeners.add(listener);
     }
 
     public void removeFileDropListener(FileDropListener listener) {
         dndListeners.remove(listener);
     }
 
     public void fireFilesDropped(Collection<File> files, ContactGItem item) {
         for (FileDropListener fileDropListener : new ArrayList<FileDropListener>(dndListeners)) {
             fileDropListener.filesDropped(files, item);
         }
     }
 
     public void contactGItemAdded(ContactGItem item) {
         fireContactGItemAdded(item);
     }
 
     public void contactGItemRemoved(ContactGItem item) {
         fireContactGItemRemoved(item);
     }
 
     /*
         Adding ContactGListListener support.
     */
 
     private final List<ContactGListListener> contactListListeners = new ArrayList<ContactGListListener>();
 
     public void addContactGListListener(ContactGListListener listener) {
         contactListListeners.add(listener);
     }
 
     public void removeContactGListListener(ContactGListListener listener) {
         contactListListeners.remove(listener);
     }
 
     public void fireContactGItemAdded(ContactGItem item) {
         for (ContactGListListener contactListListener : new ArrayList<ContactGListListener>(contactListListeners)) {
             contactListListener.contactGItemAdded(item);
         }
     }
 
     public void fireContactGItemRemoved(ContactGItem item) {
         for (ContactGListListener contactListListener : new ArrayList<ContactGListListener>(contactListListeners)) {
             contactListListener.contactGItemRemoved(item);
         }
     }
 
     public void fireContactGGroupAdded(ContactGGroup group) {
         for (ContactGListListener contactListListener : new ArrayList<ContactGListListener>(contactListListeners)) {
             contactListListener.contactGGroupAdded(group);
         }
     }
 
     public void fireContactGGroupRemoved(ContactGGroup group) {
         for (ContactGListListener contactListListener : new ArrayList<ContactGListListener>(contactListListeners)) {
             contactListListener.contactGGroupRemoved(group);
         }
     }
 
     public void fireContactGItemClicked(ContactGItem contactGItem) {
         for (ContactGListListener contactListListener : new ArrayList<ContactGListListener>(contactListListeners)) {
             contactListListener.contactGItemClicked(contactGItem);
         }
     }
 
     public void fireContactGItemDoubleClicked(ContactGItem contactGItem) {
         for (ContactGListListener contactListListener : new ArrayList<ContactGListListener>(contactListListeners)) {
             contactListListener.contactGItemDoubleClicked(contactGItem);
         }
     }
 
     public Collection<ContactGItem> getSelectedUsers() {
         final List<ContactGItem> list = new ArrayList<ContactGItem>();
 
         for (ContactGGroup group : getContactGGroups()) {
             for (ContactGItem item : group.getSelectedContacts()) {
                 list.add(item);
             }
         }
         return list;
     }
 
     private class ContactsGBean {
         private String alias;
         private String nickname;
         private String fullyQualifiedJID;
         private String groupName;
 
         ContactsGBean(String alias, String nickname, String fullyQualifiedJID, String groupName) {
             this.alias = alias;
             this.nickname = nickname;
             this.fullyQualifiedJID = fullyQualifiedJID;
             this.groupName = groupName;
         }
 
         ContactsGBean(String alias, String nickname, String fullyQualifiedJID) {
             this.alias = alias;
             this.nickname = nickname;
             this.fullyQualifiedJID = fullyQualifiedJID;
         }
 
         void setAlias(String alias) {
             this.alias = alias;
         }
 
         void setNickname(String nickname) {
             this.nickname = nickname;
         }
 
         void setFullyQualifiedJID(String fullyQualifiedJID) {
             this.fullyQualifiedJID = fullyQualifiedJID;
         }
 
         void setGroupName(String groupName) {
             this.groupName = groupName;
         }
 
         String getAlias() {
             return alias;
         }
 
         String getNickname() {
             return nickname;
         }
 
         String getFullyQualifiedJID() {
             return fullyQualifiedJID;
         }
 
         String getGroupName() {
             return groupName;
         }
 
         public ContactGItem toContactGItem() {
             return new ContactGItem(this.alias, this.nickname, this.fullyQualifiedJID, this.getGroupName());
         }
     }
 
     private class ContactsGGroupBean {
         private String groupName;
         private String parentGroup;
 
         private ContactsGGroupBean(String groupName, String parentGroup) {
             this.groupName = groupName;
             this.parentGroup = parentGroup;
         }
 
         private String getGroupName() {
             return groupName;
         }
 
         private String getParentGroup() {
             return parentGroup;
         }
 
         private void setGroupName(String groupName) {
             this.groupName = groupName;
         }
 
         private void setParentGroup(String parentGroup) {
             this.parentGroup = parentGroup;
         }
 
         private ContactGGroup toContactGGroup() {
             return new ContactGGroup(this.getGroupName(), this.getParentGroup());
         }
     }
 }
