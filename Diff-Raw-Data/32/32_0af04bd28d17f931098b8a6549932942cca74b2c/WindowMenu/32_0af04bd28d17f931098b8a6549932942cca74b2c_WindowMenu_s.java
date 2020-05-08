 
 package ch9k.core.gui;
 
 import ch9k.chat.Conversation;
 import ch9k.chat.ConversationManager;
 import ch9k.chat.event.NewConversationEvent;
 import ch9k.core.ChatApplication;
 import ch9k.core.I18n;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import java.awt.Component;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 /**
  * Menu to switch between different windows
  * @author Pieter De Baets
  */
 public class WindowMenu extends JMenu implements WindowListener, EventListener {
     private Map<Window,ShowWindowItem> menuMap;
     private ConversationManager conversationManager;
     private JFrame ownerWindow;
 
     public WindowMenu(JFrame ownerWindow) {
         super(I18n.get("ch9k.core", "window"));
 
         menuMap = new HashMap<Window,ShowWindowItem>();
         conversationManager = ChatApplication.getInstance().getConversationManager();
         this.ownerWindow = ownerWindow;
 
         addWindow("ChatHammer 9000", ChatApplication.getInstance().getWindow());
         for(Conversation conversation : conversationManager) {
             addWindow(conversation.getContact().getUsername(),
                     conversation.getWindow());
         }
 
        EventPool.getAppPool().addListener(this,
                 new EventFilter(NewConversationEvent.class));
     }
 
     private void addWindow(String title, JFrame window) {
         ShowWindowItem menuItem = new ShowWindowItem(title, window);
         menuMap.put(window, menuItem);
         window.addWindowListener(this);
         add(menuItem);
     }
 
     @Override
     public void windowClosed(WindowEvent e) {
         ShowWindowItem menuItem = menuMap.get(e.getWindow());
 
        for(Component c : getComponents()) {
             JMenuItem component = (JMenuItem)c;
             if(menuItem.equals(component)) {
                 remove(component);
             }
         }
     }
 
     @Override
     public void handleEvent(Event event) {
        NewConversationEvent conversationEvent = (NewConversationEvent)event;
        Conversation conversation = conversationEvent.getConversation();

        if(conversation == null || conversation.getContact() == null ||
                menuMap.containsKey(conversation.getWindow())) {
            return;
         }

        addWindow(conversation.getContact().getUsername(), conversation.getWindow());
     }
 
     public void windowClosing(WindowEvent e) {}
     public void windowIconified(WindowEvent e) {}
     public void windowDeiconified(WindowEvent e) {}
     public void windowActivated(WindowEvent e) {}
     public void windowDeactivated(WindowEvent e) {}
     public void windowOpened(WindowEvent e) {}
 
     private class ShowWindowItem extends JCheckBoxMenuItem implements ActionListener {
         private JFrame window;
 
         public ShowWindowItem(String title, JFrame window) {
             super(title);
             this.window = window;
 
             setSelected(ownerWindow.equals(window));
             addActionListener(this);
         }
 
         public void actionPerformed(ActionEvent e) {
             window.toFront();
             setSelected(window.equals(ownerWindow));
         }
     }
 }
