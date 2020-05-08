 package ch9k.chat.gui;
 
 import ch9k.chat.Conversation;
 import ch9k.chat.event.ConversationEventFilter;
 import ch9k.core.ChatApplication;
 import ch9k.core.I18n;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import ch9k.plugins.PluginManager;
 import ch9k.plugins.event.PluginChangeEvent;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  * Menu to enable and disable plugins for a specific conversation.
  * @author Jasper Van der Jeugt
  */
 public class PluginMenu extends JMenu
         implements ChangeListener, EventListener {
     /**
      * The releveant conversation for this menu.
      */
     private Conversation conversation;
 
     /**
      * Keep the buttons in a map.
      */
     private Map<String, JCheckBoxMenuItem> itemMap;
 
     /**
      * The plugin manager.
      */
     private PluginManager manager;
 
     /**
      * Constructor.
      * @param conversation Conversation this menu adheres to.
      */
     public PluginMenu(Conversation conversation) {
         super(I18n.get("ch9k.chat", "plugins"));
         this.conversation = conversation;
         itemMap = new HashMap<String, JCheckBoxMenuItem>();
 
         manager = ChatApplication.getInstance().getPluginManager();
 
         /* Add buttons for the available plugins. */
         buildMenu();
 
         /* Listen for changes in the manager's state. */
         manager.addChangeListener(this);
 
         /* Listen for changes in a plugin's state. */
         EventFilter filter = new ConversationEventFilter(
                 PluginChangeEvent.class, conversation);
         EventPool.getAppPool().addListener(this, filter);
     }
 
     /**
      * Build the actual menu. Calling this method again is equivalent to a
      * "refresh".
      */
     public void buildMenu() {
         /* Remove everything. */
         removeAll();
         itemMap.clear();
 
         /* Add a button for every available plugin. */
         Map<String, String> map = manager.getPrettyNames();
         for(String prettyName: map.keySet()) {
             /* Declare the string object final for use in our listener. */
             final String plugin = map.get(prettyName);
 
             JCheckBoxMenuItem item = new JCheckBoxMenuItem(prettyName);
             add(item);
             itemMap.put(plugin, item);
 
             /* Add a simple listener. */
             item.addActionListener( new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     pluginChanged(plugin);
                 }
             });
 
             /* Only the user who initiated the conversation has the power to
              * enable and disable plugins. */
             item.setEnabled(conversation.isInitiatedByMe());
         }
     }
 
     /**
      * Enable/disable a plugin. Called when a checkbox is clicked.
      * @param plugin Name of the plugin.
      */
     private void pluginChanged(String plugin) {
         JCheckBoxMenuItem item = itemMap.get(plugin);
 
         /* Enable the plugin. */
         if(manager.isEnabled(plugin, conversation)) {
             manager.disablePlugin(plugin, conversation);
         /* Disable the plugin. */
         } else {
             manager.enablePlugin(plugin, conversation);
         }
     }
 
     @Override
     public void stateChanged(ChangeEvent event) {
         /* If the manager sends an event, this means the installed plugins list
          * has changed. We thus want to rebuild it. */
         if(manager == event.getSource()) {
             buildMenu();
         }
     }
 
     @Override
     public void handleEvent(Event e) {
         PluginChangeEvent event = (PluginChangeEvent) e;
         JCheckBoxMenuItem item = itemMap.get(event.getPlugin());
 
         /* Not found in the map. We just ignore this. */
         if(item == null) return;
 
         /* Adapt our view. */
         if(item.getState() != event.isPluginEnabled()) {
             item.setState(event.isPluginEnabled());
         }
     }
 }
