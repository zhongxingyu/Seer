 /*
  * LobbyWindow.java
  *
  * Created on Apr 5, 2009, 12:47:25 PM
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import javax.swing.*;
 import java.util.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import shoddybattleclient.AdminPanel.ChannelLookup;
 import shoddybattleclient.network.ServerLink;
 import shoddybattleclient.network.ServerLink.BanElement;
 import shoddybattleclient.network.ServerLink.ChallengeMediator;
 import shoddybattleclient.network.ServerLink.RuleSet;
 import shoddybattleclient.utils.*;
 import shoddybattleclient.utils.ClauseList.Clause;
 import shoddybattleclient.utils.CloseableTabbedPane.TabCloseListener;
 
 /**
  *
  * @author ben
  */
 public class LobbyWindow extends javax.swing.JFrame implements TabCloseListener, ChannelLookup {
 
     public static class Channel {
         //User flags
         public static final int PROTECTED = 1; // +a
         public static final int OP = 2;        // +o
         public static final int VOICE = 4;     // +v
         public static final int MUTE = 8;      // +b
         public static final int IDLE = 16;     // inactive
         public static final int BUSY = 32;     // ("ignoring challenges")
         //Channel flags
         public static final int MUTED = 1;    // +m muted room
         public static final int INVITE = 2;  // +i invite only
 
         public static final String[] MODES =
                 { "a", "o", "v", "b"};
         public static final String[] CHANNEL_MODES = {"m", "i"};
 
         public static final int TYPE_ORDINARY = 0;
         public static final int TYPE_BATTLE = 1;
 
         
 
         public static final SimpleDateFormat DATE_FORMATTER =
                 new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
 
         private int m_id;
         private int m_type;
         private String m_name;
         private String m_topic;
         private int m_flags;
         private ChatPane m_chat;
         // TOOD: Do not assume the background is white.
         public static ColourMap COLOUR_MAP = new ColourMap(Color.WHITE);
         private UserListModel m_users = new UserListModel();
 
         public void setChatPane(ChatPane c) {
             m_chat = c;
         }
         public ChatPane getChatPane() {
             return m_chat;
         }
         public UserListModel getModel() {
             return m_users;
         }
         public static int getLevel(int flags) {
             if ((flags & PROTECTED) != 0)
                 return 3;
             if ((flags & OP) != 0)
                 return 2;
             if ((flags & VOICE) != 0)
                 return 1;
             if ((flags & MUTE) != 0)
                 return -1;
             return 0;
         }
         public Channel(int id, int type, String name, String topic, int flags) {
             m_id = id;
             m_type = type;
             m_name = name;
             m_topic = topic;
             m_flags = flags;
         }
         public void addUser(String name, int flags) {
             m_users.addUser(new User(name, flags));
         }
         public void removeUser(String name) {
             m_users.removeUser(name);
         }
         private String getModeString(int oldflags, int newflags, boolean channel) {
             String[] modeNames = channel ? CHANNEL_MODES : MODES;
             int diff = oldflags ^ newflags;
             if (diff == 0)
                 return null;
             StringBuilder modes = new StringBuilder();
             String last = null;
             for (int i = 0; i < modeNames.length; ++i) {
                 int bit = 1 << i;
                 if ((diff & bit) != 0) {
                     String prefix = ((oldflags & bit) != 0) ? "-" : "+";
                     if (!prefix.equals(last)) {
                         modes.append(prefix);
                     }
                     last = prefix;
                     modes.append(modeNames[i]);
                 }
             }
             return modes.toString();
         }
         //sets the flags for this channel
         public void setFlags(String setter, int flags) {
             String modes = getModeString(m_flags, flags, true);
             if (modes == null) return;
             m_flags = flags;
             String msg = Text.getText(26, 0, new String[] {setter, modes, ""});
             msg = "<font class='mode'>" + msg + "</font>";
             m_chat.getLobby().showChannelMessage(this, null, msg, false);
         }
         public void updateUser(String setter, String name, int flags) {
             User user = getUser(name);
             int old = user.getFlags();
             String modes = getModeString(old, flags, false);
             if (modes == null) return;
             String msg;
             if (setter.length() > 0) {
                 msg = Text.getText(26, 0, new String[] {
                     setter, modes, name });
             } else {
                 msg = Text.getText(26, 1, new String[] {
                     modes, name });
             }
             msg = "<font class='mode'>" + msg + "</font>";
             m_chat.getLobby().showChannelMessage(this, null,
                     msg, false);
             m_users.setLevel(user, flags);
         }
 
         public boolean isMuted() {            
             if ((m_flags & MUTED) != 0) { return true; }
             return false;
         }
         public boolean isInviteOnly() {
             if ((m_flags & INVITE) != 0) { return true; }
             return false;
         }
 
         public void informBan(String mod, String user, int date) {
             if (date == 0) {
                 //kick
                 String msg = Text.getText(26, 2, new String[] {user, mod});
                 m_chat.addMessage(null, "<b class='kick'>" + msg + "</b>", false);
             } else if (date == -1) {
                 //unban
                 String msg = Text.getText(26, 4, new String[] {user});
                 m_chat.addMessage(null, "<b class='unban'>" + msg + "</b>", false);
             } else if (date > 0) {
                 String dateStr = Text.formatDateDifference(date);
                 String msg = Text.getText(26, 3, new String[] {user, mod, dateStr});
                 m_chat.addMessage(null, "<b class='ban'>" + msg + "</b>", false);
             }
         }
 
         public static String getUserHtml(User user, Color color) {
             return user.getHtml(color);
         }
 
         public static String getUserHtml(User user) {
             return getUserHtml(user, COLOUR_MAP.getColour(user.getName()));
         }
         
         public String getTopic() {
             return m_topic;
         }
         public void setTopic(String topic) {
             m_topic = topic;
         }
         public String getName() {
             return m_name;
         }
         int getId() {
             return m_id;
         }
         public User getUser(String name) {
             for (int i = 0; i < m_users.getSize(); i++) {
                 User u = (User)m_users.getElementAt(i);
                 if (u.m_name.equalsIgnoreCase(name)) {
                     return u;
                 }
             }
             return null;
         }
         public int getType() {
             return m_type;
         }
     }
 
     public static class UserCellRenderer extends JLabel implements ListCellRenderer {
         private static final ImageIcon[] STATUS_ICONS = new ImageIcon[3];
         static {
             STATUS_ICONS[0] = getModeIcon("voice.png");
             STATUS_ICONS[1] = getModeIcon("operator.png");
             STATUS_ICONS[2] = getModeIcon("protected.png");
         }
         public static ImageIcon getModeIcon(String file) {
             return new ImageIcon(Toolkit.getDefaultToolkit()
                     .createImage(GameVisualisation.class.getResource(
                     "resources/modes/" + file)));
         }
         
         public Component getListCellRendererComponent(JList list,
                 Object value, int index, boolean isSelected,
                 boolean cellHasFocus) {
             User user = (User)value;
             int level = user.getLevel();
             if (level > 0) {
                 setIcon(STATUS_ICONS[level - 1]);
             } else {
                 setIcon(null);
             }
             if (isSelected) {
                 setText("<html>" + Channel.getUserHtml(
                         user, list.getSelectionForeground()) + "</html>");
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
             } else {
                 setText("<html>" + Channel.getUserHtml(user) + "</html>");
                 setBackground(list.getBackground());
                 setForeground(list.getForeground());
             }
             setEnabled(list.isEnabled());
             setFont(list.getFont());
             setOpaque(true);
             return this;
         }
 
     }
 
     /**
      * Algorithm for assigning to each string a colour that contrasts well with
      * the background
      */
     public static class ColourMap {
         private static final int BRIGHTNESS_DELTA = 40000;//100000;
         private static final int COLOUR_DELTA = 500;
 
         private static final Random m_random = new Random(2718281);
         private static final Color[] m_colours = new Color[255];
         private static int getBrightness(Color c) {
             return c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114;
         }
         private static int getDifference(Color c1, Color c2) {
             int r1 = c1.getRed();
             int g1 = c1.getGreen();
             int b1 = c1.getBlue();
             int r2 = c2.getRed();
             int g2 = c2.getGreen();
             int b2 = c2.getBlue();
             return Math.max(r1, r2) - Math.min(r1, r2)
                     + Math.max(g1, g2) - Math.min(g1, g2)
                     + Math.max(b1, b2) - Math.min(b1, b2);
         }
         private static Color getColour() {
             int r = m_random.nextInt(256);
             int g = m_random.nextInt(256);
             int b = m_random.nextInt(256);
             return new Color(r, g, b);
         }
         private static Color getVisibleColour(Color background) {
             while (true) {
                 Color c = getColour();
                 int b = getBrightness(c);
                 int delta = Math.abs(b - getBrightness(background));
                 if ((delta > BRIGHTNESS_DELTA)
                         && (getDifference(c, background) > COLOUR_DELTA))
                     return c;
             }
         }
         public ColourMap(Color background) {
             Set<Color> set = new HashSet<Color>();
             for (int i = 0; i < m_colours.length; ++i) {
                 Color c = getVisibleColour(background);
                 while (set.contains(c)) {
                     c = getVisibleColour(background);
                 }
                 set.add(c);
             }
             set.toArray(m_colours);
         }
         public Color getColour(Object key) {
             return m_colours[Math.abs(key.hashCode() % m_colours.length)];
         }
     }
 
     public static class User implements Comparable {
         private String m_name;
         private int m_status = 0;
         private int m_flags, m_level;
         private Map<Integer, String> m_battles = new HashMap<Integer, String>();
 
         public User(String name, int flags) {
             m_name = name;
             m_flags = flags;
             m_level = Channel.getLevel(flags);
         }
         public int getFlags() {
             return m_flags;
         }
         public void setLevel(int flags) {
             m_flags = flags;
             m_level = Channel.getLevel(flags);
         }
         public int getLevel() {
             return m_level;
         }
         public void setStatus(int status) {
             m_status = status;
         }
         public String getName() {
             return m_name;
         }
         public boolean hasMute() {
             return ((m_flags & Channel.MUTE) != 0);
         }
         @Override
         public int compareTo(Object o2) {
             User u2 = ((User)o2);
             if (m_level > u2.m_level)
                 return -1;
             if (m_level < u2.m_level)
                 return 1;
             if (m_status < u2.m_status)
                 return -1;
             if (m_status > u2.m_status)
                 return 1;
             String s2 = u2.m_name;
             return m_name.compareToIgnoreCase(s2);
         }
         public String getPrefix() {
             String[] prefixes = { "", "+", "@", "&" };
             return prefixes[m_level];
         }
         public void addBattle(int id, String opponent) {
             m_battles.put(id, opponent);
         }
         public void removeBattle(int id) {
             m_battles.remove(id);
         }
         @Override
         public boolean equals(Object o2) {
             if(o2 == null)
                 return false;
             if(!this.getClass().equals(o2.getClass()))
                 return false;
             return ((User)o2).m_name.equalsIgnoreCase(m_name);
         }
         @Override
         public int hashCode() {
             return m_name.toUpperCase().hashCode();
         }
         @Override
         public String toString() {
             return m_name;
         }
         public String getHtml(Color c) {
             if (m_level != -1) {
                 int r = c.getRed();
                 int g = c.getGreen();
                 int b = c.getBlue();
                 String colour = "rgb(" + r + "," + g + "," + b + ")";
                 String style = (m_battles.size() > 0) ? "font-style: italic;" : "";
                 return "<font class='name' style='color: "
                     + colour + style + "'>" + m_name + "</font>";
             }
             return "<font class='muted'>"
                     + m_name + "</font>";
         }
     }
 
     private class PopupListener extends MouseAdapter {
         int m_level = 0;
         public void setLevel(int level) { m_level = level; }
         public void mouseReleased(MouseEvent e) {
             triggerPopup(e);
         }
         public void mousePressed(MouseEvent e) {
             triggerPopup(e);
         }
         private void triggerPopup(MouseEvent e) {
             if (e.isPopupTrigger()) {
                 //If the source is an HTMLPane then it is the chat
                 if (e.getSource().getClass().getSimpleName().equals("HTMLPane")) {
                     new ChatPopupMenu(LobbyWindow.this, m_level)
                             .show(e.getComponent(), e.getX(), e.getY());
                 }
                 else {
                     int idx = listUsers.locationToIndex(e.getPoint());
                     if (idx < 0) return;
                     listUsers.setSelectedIndex(idx);
                     User u = (User)((UserListModel)listUsers.getModel()).getElementAt(idx);
                     new UserPopupMenu(LobbyWindow.this, u, m_level)
                                     .show(e.getComponent(), e.getX(), e.getY());
                 }
             }
         }
     };
 
     private String m_name;
     private ServerLink m_link;
     private Map<Integer, Channel> m_channels = new HashMap<Integer, Channel>();
     private Map<String, UserPanel>  m_userPanels = new HashMap<String, UserPanel>();
     final private BattlePanel m_battlePanel;
     final private FindPanel m_findPanel;
     final private AdminPanel m_adminPanel;
     //The most recent channel that was focused
     private Channel m_recentChannel = null;
     final private PopupListener m_popupListener = new PopupListener();
 
     public BattlePanel getBattlePanel() {
         return m_battlePanel;
     }
 
     public Channel getChannel(String name) {
         for (Channel i : m_channels.values()) {
             if (name.equals(i.getName())) {
                 return i;
             }
         }
         return null;
     }
 
     public void addChannel(Channel channel) {
         m_channels.put(channel.m_id, channel);
         
         ChatPane c = new ChatPane(channel, this, m_name);
         channel.setChatPane(c);
 
         if (channel.getType() == Channel.TYPE_BATTLE) {
             return;
         }
         String name = channel.getName();
         c.addMessage(null, "<b>The topic for #"
                 + name + " is: "
                 + channel.getTopic()
                 + "</b>", false);
         tabChats.add("#" + name, c);
         listUsers.setModel(channel.getModel());
     }
     
     public Channel getChannel(int id) {
         return m_channels.get(id);
     }
 
     public void handleJoinPart(int id, String user, boolean join) {
         Channel channel = m_channels.get(id);
         if (channel != null) {
             if (join) {
                 channel.addUser(user, 0);
             } else {
                 channel.removeUser(user);
             }
         }
         if (channel.getType() == Channel.TYPE_BATTLE) {
             BattleWindow wnd = m_link.getBattle(channel.getId());
             if (wnd != null) wnd.refreshUsers();
         }
     }
 
     public FindPanel getFindPanel() {
         return m_findPanel;
     }
 
     /** Creates new form LobbyWindow */
     public LobbyWindow(ServerLink link, String userName) {
         initComponents();
         m_link = link;
         m_link.setLobbyWindow(this);
         m_battlePanel = new BattlePanel(m_link);
         m_findPanel = new FindPanel(m_link);
         m_adminPanel = new AdminPanel(m_link);
         m_name = userName;
 
         listUsers.setCellRenderer(new UserCellRenderer());
 
         tabChats.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 if (tabChats.getSelectedIndex() == -1) {
                     listUsers.setModel(new UserListModel());
                     return;
                 }
                 ((CloseableTabbedPane)tabChats).setFlashingAt(tabChats.getSelectedIndex(), false);
                 Component comp = tabChats.getSelectedComponent();
                 if (comp instanceof ChatPane) {
                     ChatPane c = (ChatPane)comp;
                     Channel channel = c.getChannel();
                     c.getChat().addMouseListener(m_popupListener);
                     m_recentChannel = channel;
                     listUsers.setModel(channel.getModel());
                     User u = channel.getUser(m_name);
                     if (u != null) {
                         m_popupListener.setLevel(u.getLevel());
                     }
                 } else if (comp instanceof BattlePanel) {
                     m_link.requestChannelList();
                 }
             }
         });
         
         ((CloseableTabbedPane)tabChats).addTabCloseListener(this);
 
         setTitle("Shoddy Battle - " + userName);
 
         mnuPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
               Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         mnuQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
               Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         mnuLeaveServer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
               Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 
         listUsers.addMouseListener(m_popupListener);
     }
 
     public ServerLink getLink() {
         return m_link;
     }
 
     public static void viewWebPage(URL page) {
         try {
             throw new Exception();
         } catch (Exception e) {
             System.out.println(page);
         }
     }
 
     /**
      * Takes some letters and returns a list of matching usernames
      */
     public List<String> autocompleteUser(String str) {
         str = str.toLowerCase();
         UserListModel model = (UserListModel)listUsers.getModel();
         List<String> list = model.getNames();
         List<String> ret = new ArrayList<String>();
         for (String name : list) {
             if (name.toLowerCase().startsWith(str)) {
                 ret.add(name);
             }
         }
         return ret;
     }
 
     public void addChallenge(String name, boolean incoming, int gen, int n,
             int teamLength, int metagame, RuleSet rules) {
         if (Preference.ignoring(name)) {
             m_link.resolveChallenge(name, false, null);
             return;
         }
         UserPanel panel = openUserPanel(name, gen, n, teamLength, metagame, rules);
         CloseableTabbedPane closeable = (CloseableTabbedPane)tabChats;
         int idx = closeable.indexOfComponent(panel);
         closeable.setFlashingAt(idx, true);
     }
 
     public ChallengeMediator getChallengeMediator(String name) {
         UserPanel panel = m_userPanels.get(name);
         if (panel != null) {
             return panel.getMediator();
         } else {
             return null;
         }
     }
 
     public void cancelChallenge(String name) {
 
     }
 
     public ChatPane getChat() {
         return (ChatPane)tabChats.getSelectedComponent();
     }
 
     public String getUserName() {
         return m_name;
     }
 
     private void updateBattleUsers(int id) {
         BattleWindow wnd = m_link.getBattle(id);
         if (wnd != null) wnd.refreshUsers();
     }
 
     public void handleUpdateStatus(int id, String setter, String user, int flags) {
         Channel channel = m_channels.get(id);
         if (channel != null) {
             if ("".equals(user)) {
                 channel.setFlags(setter, flags);
                 return;
             }
             channel.updateUser(setter, user, flags);
             if (user.equalsIgnoreCase(m_name)) {
                 m_popupListener.setLevel(channel.getUser(m_name).getLevel());
             }
             if (channel.getType() == Channel.TYPE_BATTLE) {
                 updateBattleUsers(channel.getId());
             }
         }
     }
 
     public void showChannelMessage(Channel channel,
             String user, String message, boolean encode) {
         if (channel.getType() == Channel.TYPE_ORDINARY) {
             channel.getChatPane().addMessage(user, message, encode);
         } else {
             // battle chat message
             BattleWindow wnd = m_link.getBattle(channel.getId());
             if (wnd != null) wnd.addMessage(user, message, encode);
         }
     }
 
     public void handleChannelMessage(int id, String user, String message) {
         if (Preference.ignoring(user) && !user.equals(m_name)) return;
         Channel channel = m_channels.get(id);
         if (channel != null) {
             User u = channel.getUser(user);
            String userString = user;
            if (u != null) {
                String prefix = u.getPrefix();
                userString = prefix + Channel.getUserHtml(u);
            }
             showChannelMessage(channel,
                    userString, message, true);
         }
     }
 
     public void closeTab(int index) {
         tabChats.removeTabAt(index);
     }
 
     public UserPanel openUserPanel(String user, boolean incoming, int generation, 
             int n, int teamLength, int metagame, RuleSet rules) {
         UserPanel panel = m_userPanels.get(user);
         if (panel == null) {
             int index = tabChats.getTabCount();
             panel = new UserPanel(user, m_link, index);
             tabChats.add(user, panel);
             m_userPanels.put(user, panel);
         }
         if (incoming) {
             panel.setIncoming();
             panel.setOptions(n, teamLength, generation, metagame, rules);
         } else {
             tabChats.setSelectedComponent(panel);
         }
         return panel;
     }
 
     public boolean isUserPanelSelected(String user) {
         UserPanel panel = m_userPanels.get(user);
         Component selected = tabChats.getSelectedComponent();
         if (panel == selected)
             return true;
         return false;
     }
     
     public void removeUserPanel(String user) {
         UserPanel panel = m_userPanels.get(user);
         this.closeTab(tabChats.indexOfComponent(panel));
         m_userPanels.remove(user);
     }
 
     public void openUserPanel(String user) {
         openUserPanel(user, false, 0, 0, 0, -1, null);
     }
 
     public UserPanel openUserPanel(String user, int generation, int n,
                 int teamLength, int metagame, RuleSet rules) {
         return openUserPanel(user, true, generation, n, teamLength, metagame, rules);
     }
 
     @Override
     public void tabClosed(Component c) {
         if (c instanceof UserPanel) {
             UserPanel panel = (UserPanel)c;
             m_userPanels.remove(panel.getOpponent());
         }
     }
 
     public void handleBanMessage(int id, String mod, String user, int date) {
         if (id == -1) {
             for (Channel c : m_channels.values()) {
                 if (c.getType() == Channel.TYPE_ORDINARY) {
                     c.informBan(mod, user, date);
                 }
             }
         } else {
             Channel c = m_channels.get(id);
             if (c != null) c.informBan(mod, user, date);
         }
     }
 
     public void informBadLookup() {
         m_adminPanel.setErrorText("No such user");
     }
 
     public void showLookupResults(String user, String ip, List<String> aliases,
                                                 List<BanElement> bans) {
         m_adminPanel.updateDetails(user, ip, aliases, bans);
         tabChats.add("Admin", m_adminPanel);
         tabChats.setSelectedComponent(m_adminPanel);
     }
 
     public String getChannelName(int id) {
         Channel c = m_channels.get(id);
         if (c == null) {
             return null;
         } else {
             return c.getName();
         }
     }
 
     public int getActiveChannel() {
         if (m_recentChannel == null) return -1;
         return m_recentChannel.getId();
     }
 
     public void informInvalidTeam(String user, int teamSize, int[] clauses) {
         boolean[] invalidTraits = new boolean[5];
         StringBuilder clauseList = new StringBuilder();
         List<Clause> cl = m_link.getClauseList();
         for (int i = 0; i < clauses.length; i++) {
             if (clauses[i] < 0) {
                 int problem = (-clauses[i]-1) / teamSize;
                 invalidTraits[problem] = true;
             } else {
                 clauseList.append(" -");
                 clauseList.append(cl.get(clauses[i]).name);
                 clauseList.append("\n");
             }
         }
 
         StringBuilder sb = new StringBuilder();
         sb.append("The supplied team does not conform to:\n");
         if (invalidTraits[0]) sb.append(" -Legal Pokemon\n");
         if (invalidTraits[1]) sb.append(" -Legal Moves\n");
         if (invalidTraits[2]) sb.append(" -Legal Move Combinations\n");
         if (invalidTraits[3]) sb.append(" -Legal Items\n");
         if (invalidTraits[4]) sb.append(" -Legal Stats\n");
         sb.append(clauseList.toString());
 
         if (!"".equals(user)) {
             UserPanel panel = m_userPanels.get(user);
             if (panel != null) {
                 panel.unsetTeam();
             }
         } else {
             m_findPanel.unsetTeam();
         }
 
         JOptionPane.showMessageDialog(this, sb.toString());
     }
 
     public void addImportantMessage(String msg) {
         msg = Text.addClass(msg, "important");
         for (Component c : tabChats.getComponents()) {
             if (c instanceof ChatPane) {
                 ((ChatPane)c).addMessage(null, msg, false);
             }
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         jMenu2 = new javax.swing.JMenu();
         btnChallenge = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         listUsers = new javax.swing.JList();
         tabChats = new CloseableTabbedPane();
         jMenuBar2 = new javax.swing.JMenuBar();
         jMenu3 = new javax.swing.JMenu();
         mnuJoinMain = new javax.swing.JMenuItem();
         mnuBattleList = new javax.swing.JMenuItem();
         mnuFind = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JSeparator();
         mnuPreferences = new javax.swing.JMenuItem();
         mnuLeaveServer = new javax.swing.JMenuItem();
         mnuQuit = new javax.swing.JMenuItem();
         jMenu4 = new javax.swing.JMenu();
         chkAway = new javax.swing.JCheckBoxMenuItem();
         chkIgnoreChallenge = new javax.swing.JCheckBoxMenuItem();
         jSeparator1 = new javax.swing.JSeparator();
         mnuPersonalMessage = new javax.swing.JMenuItem();
 
         jMenu1.setText("File");
         jMenuBar1.add(jMenu1);
 
         jMenu2.setText("Edit");
         jMenuBar1.add(jMenu2);
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
         setLocationByPlatform(true);
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         btnChallenge.setText("Challenge");
         btnChallenge.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnChallengeActionPerformed(evt);
             }
         });
 
         listUsers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane1.setViewportView(listUsers);
 
         jMenu3.setText("File");
 
         mnuJoinMain.setText("Join #main");
         mnuJoinMain.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuJoinMainActionPerformed(evt);
             }
         });
         jMenu3.add(mnuJoinMain);
 
         mnuBattleList.setText("Battle List");
         mnuBattleList.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuBattleListActionPerformed(evt);
             }
         });
         jMenu3.add(mnuBattleList);
 
         mnuFind.setText("Find");
         mnuFind.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuFindActionPerformed(evt);
             }
         });
         jMenu3.add(mnuFind);
         jMenu3.add(jSeparator2);
 
         mnuPreferences.setText("Preferences");
         mnuPreferences.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuPreferencesActionPerformed(evt);
             }
         });
         jMenu3.add(mnuPreferences);
 
         mnuLeaveServer.setText("Leave Server");
         mnuLeaveServer.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuLeaveServerActionPerformed(evt);
             }
         });
         jMenu3.add(mnuLeaveServer);
 
         mnuQuit.setText("Quit");
         mnuQuit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuQuitActionPerformed(evt);
             }
         });
         jMenu3.add(mnuQuit);
 
         jMenuBar2.add(jMenu3);
 
         jMenu4.setText("Status");
 
         chkAway.setText("Away");
         jMenu4.add(chkAway);
 
         chkIgnoreChallenge.setText("Ignoring Challenges");
         jMenu4.add(chkIgnoreChallenge);
         jMenu4.add(jSeparator1);
 
         mnuPersonalMessage.setText("Personal Message");
         mnuPersonalMessage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuPersonalMessageActionPerformed(evt);
             }
         });
         jMenu4.add(mnuPersonalMessage);
 
         jMenuBar2.add(jMenu4);
 
         setJMenuBar(jMenuBar2);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(tabChats, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(btnChallenge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(layout.createSequentialGroup()
                         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                         .add(9, 9, 9)
                         .add(btnChallenge))
                     .add(org.jdesktop.layout.GroupLayout.LEADING, tabChats, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         if (confirmLeave()) {
             this.dispose();
             m_link.close();
             new WelcomeWindow().setVisible(true);
         }
     }//GEN-LAST:event_formWindowClosing
 
     private void btnChallengeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChallengeActionPerformed
         User user = (User)listUsers.getSelectedValue();
         if (user == null) return;
         String opponent = user.getName();
         if (opponent.equals(m_name) && false) {
             // todo: internationalisation
             JOptionPane.showMessageDialog(this, "You cannot challenge yourself.");
         } else {
             openUserPanel(opponent);
         }
 }//GEN-LAST:event_btnChallengeActionPerformed
 
     private void mnuPersonalMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPersonalMessageActionPerformed
         new PersonalMessageDialog(m_link).setVisible(true);
     }//GEN-LAST:event_mnuPersonalMessageActionPerformed
 
     private void mnuQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuQuitActionPerformed
         if (confirmLeave()) {
             System.exit(0);
         }
     }//GEN-LAST:event_mnuQuitActionPerformed
 
     private void mnuLeaveServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLeaveServerActionPerformed
         formWindowClosing(null);
     }//GEN-LAST:event_mnuLeaveServerActionPerformed
 
     private void mnuJoinMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuJoinMainActionPerformed
         m_link.joinChannel("main");
     }//GEN-LAST:event_mnuJoinMainActionPerformed
 
     private void mnuPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPreferencesActionPerformed
         new PreferencePane().setVisible(true);
     }//GEN-LAST:event_mnuPreferencesActionPerformed
 
     private void mnuBattleListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuBattleListActionPerformed
         tabChats.add("Battles", m_battlePanel);
         tabChats.setSelectedComponent(m_battlePanel);
         m_link.requestChannelList();
     }//GEN-LAST:event_mnuBattleListActionPerformed
 
     private void mnuFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuFindActionPerformed
         tabChats.add("Find", m_findPanel);
         tabChats.setSelectedComponent(m_findPanel);
     }//GEN-LAST:event_mnuFindActionPerformed
 
     // prompts the user to confirm that they wish to leave the server
     private boolean confirmLeave() {
         if (!m_link.isAlive()) return true;
         int response = JOptionPane.showConfirmDialog(this, "Are you sure you " +
                 "want to leave this server?", "Disconnecting...", JOptionPane.YES_NO_OPTION);
         return (response == JOptionPane.YES_OPTION);
     }
 
     /**
     * @param args the command line arguments
     */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 LobbyWindow lw = new LobbyWindow(null, "Ben");
                 lw.setVisible(true);
             }
         });
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnChallenge;
     private javax.swing.JCheckBoxMenuItem chkAway;
     private javax.swing.JCheckBoxMenuItem chkIgnoreChallenge;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenu jMenu3;
     private javax.swing.JMenu jMenu4;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuBar jMenuBar2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JList listUsers;
     private javax.swing.JMenuItem mnuBattleList;
     private javax.swing.JMenuItem mnuFind;
     private javax.swing.JMenuItem mnuJoinMain;
     private javax.swing.JMenuItem mnuLeaveServer;
     private javax.swing.JMenuItem mnuPersonalMessage;
     private javax.swing.JMenuItem mnuPreferences;
     private javax.swing.JMenuItem mnuQuit;
     private javax.swing.JTabbedPane tabChats;
     // End of variables declaration//GEN-END:variables
 
 }
