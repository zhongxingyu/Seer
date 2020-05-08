 package ch9k.chat.gui;
 
 import ch9k.chat.Conversation;
 import ch9k.chat.event.CloseConversationEvent;
 import ch9k.chat.event.ConversationEventFilter;
 import ch9k.chat.event.ReleasePluginContainerEvent;
 import ch9k.chat.event.RequestPluginContainerEvent;
 import ch9k.chat.event.RequestedPluginContainerEvent;
 import ch9k.chat.gui.components.MessageEditor;
 import ch9k.core.gui.WindowMenu;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 
 /**
  * Shows a conversation
  * @author Pieter De Baets
  */
 public class ConversationWindow extends JFrame implements EventListener {
     /**
      * The shown conversation
      */
     private Conversation conversation;
 
     /**
      * Window is rendered as a split pane
      */
     private JSplitPane splitPane;
 
     /**
      * Pane with visual plugins.
      */
     private JTabbedPane pluginPane;
 
     /**
      * Show all messages in the conversation
      */
     private ConversationListView conversationView;
 
     /**
      * Inputbox for new chatmessages
      */
     private MessageEditor editor;
 
     /**
      * Construct a new ConversationWindow
      * Must be called from the Swing-thread
      * @param conversation
      */
     public ConversationWindow(Conversation conversation) {
         super(conversation.getContact().getUsername() + " @ " + conversation.getContact().getIp().getCanonicalHostName());
         this.conversation = conversation;
         setName(conversation.getContact().getUsername());
     }
 
     public void init() {
         EventFilter requestFilter = new ConversationEventFilter(
                 RequestPluginContainerEvent.class, conversation);
         EventPool.getAppPool().addListener(this, requestFilter);
         
         EventFilter releaseFilter = new ConversationEventFilter(
                 ReleasePluginContainerEvent.class, conversation);
         EventPool.getAppPool().addListener(this, releaseFilter);
         
         // listen for close-events
         addWindowListener(new WindowAdapter() {
             public void windowClosed(WindowEvent e) {
                 if(!conversation.isClosed()) {
                     EventPool.getAppPool().removeListener(
                             ConversationWindow.this);
                     EventPool.getAppPool().raiseNetworkEvent(
                             new CloseConversationEvent(conversation));
                 }
             }
         });
         
         initComponents();
 
         // Add a menu bar, containing a menu in which different 
         // plugins can be selected
         JMenuBar menuBar = new JMenuBar();
         menuBar.add(new PluginMenu(conversation));
         menuBar.add(new WindowMenu(this));
         setJMenuBar(menuBar);
 
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         setVisible(true);
     }
 
     private void initComponents() {
         pluginPane = new JTabbedPane();
         conversationView = new ConversationListView(conversation);
         editor = new MessageEditor(conversation);
 
         JPanel conversationPanel = new JPanel(new BorderLayout());
         conversationPanel.add(conversationView, BorderLayout.CENTER);
         conversationPanel.add(editor, BorderLayout.SOUTH);
         splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
                 pluginPane, conversationPanel);
         splitPane.setSize(new Dimension(900, 600));
 
         splitPane.setDividerLocation(0.55);
         splitPane.setResizeWeight(0.5);
         splitPane.setBorder(null);
         
         getContentPane().setLayout(new BorderLayout());
         getContentPane().add(splitPane, BorderLayout.CENTER);
         setSize(splitPane.getSize());
         setMinimumSize(new Dimension(600, 400));
 
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
     }
 
     @Override
     public void handleEvent(Event e) {
        if(e instanceof RequestPluginContainerEvent &&
                 !conversation.isClosed()) {
             RequestPluginContainerEvent event = (RequestPluginContainerEvent) e;
             JPanel newTab = new JPanel();
             pluginPane.add(event.getTitle(), newTab);
             EventPool.getAppPool().raiseEvent(new RequestedPluginContainerEvent(
                 conversation, newTab));
         }
 
         if(e instanceof ReleasePluginContainerEvent) {
             ReleasePluginContainerEvent event = (ReleasePluginContainerEvent)e;
             pluginPane.remove(event.getPluginContainer());
         }
 
         pluginPane.revalidate();
         pluginPane.repaint();
     }
 }
