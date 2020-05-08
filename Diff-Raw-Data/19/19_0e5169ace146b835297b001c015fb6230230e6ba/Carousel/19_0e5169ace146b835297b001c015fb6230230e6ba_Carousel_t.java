 package ch9k.plugins.carousel;
 
 import ch9k.chat.Contact;
 import ch9k.chat.Conversation;
 import ch9k.chat.event.ConversationEventFilter;
 import ch9k.chat.event.RequestPluginContainerEvent;
 import ch9k.chat.event.RequestedPluginContainerEvent;
 import ch9k.chat.event.ReleasePluginContainerEvent;
 import ch9k.core.settings.Settings;
 import ch9k.core.I18n;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.plugins.event.RemotePluginSettingsChangeEvent;
 import ch9k.eventpool.EventPool;
 import ch9k.plugins.AbstractPluginInstance;
 import ch9k.plugins.Plugin;
 import ch9k.plugins.flickr.FlickrImageProviderPlugin;
 import java.awt.GridLayout;
 import java.awt.Container;
 import java.net.InetAddress;
 import javax.swing.JFrame;
 
 /**
  * Plugin for a standard image carousel.
  */
 public class Carousel extends AbstractPluginInstance {
     /**
      * The container that we get from the system.
      */
     private Container container;
 
     /**
      * The main view for this plugin.
      */
     private CarouselPanel panel;
 
     /**
      * Selection model for this plugin.
      */
     public CarouselImageModel model;
 
     /**
      * Constructor.
      * @param plugin Corresponding plugin.
      * @param conversation Conversation to display carousel for.
      * @param settings Local plugin instance settings.
      */
     public Carousel(Plugin plugin,
             Conversation conversation, Settings settings) {
         super(plugin, conversation, settings);
         /* We will asynchronously receive a container later. */
         this.container = null;
     }
 
     @Override
     public void enablePluginInstance() {
         /* First, register this plugin as listener so it can receive a container
          * later. */
         EventFilter filter = new ConversationEventFilter(
                 RequestedPluginContainerEvent.class, getConversation());
         EventPool.getAppPool().addListener(this, filter);
 
         /* Asyncrhonously request a panel for this plugin. */
         Event event = new RequestPluginContainerEvent(getConversation(),
                 I18n.get("ch9k.plugins.carousel", "carousel"));
         EventPool.getAppPool().raiseEvent(event);
     }
 
     @Override
     public void disablePluginInstance() {
         /* Disable the plugin. */
         EventPool.getAppPool().removeListener(this);
         panel.disablePlugin();
 
         /* Release the container request a panel for this plugin. */
        if(container != null) {
            Event event = new ReleasePluginContainerEvent(
                    getConversation(), container);
            EventPool.getAppPool().raiseEvent(event);
            container.removeAll();
            container = null;
        }
     }
 
     @Override
     public void handleEvent(Event e) {
         super.handleEvent(e);
         if(e instanceof RequestedPluginContainerEvent) {
             RequestedPluginContainerEvent event =
                     (RequestedPluginContainerEvent) e;
 
             /* We only need one panel. */
             if(container != null) return;
 
             /* Okay, we have a panel now, start using it. */
             container = event.getPluginContainer();
 
             /* Clear the container and set a new layout. */
             container.removeAll();
             container.setLayout(new GridLayout(1, 1));
 
             /* Add our carousel to it. */
             model = new CarouselImageModel(getConversation());
             panel = new CarouselPanel(getSettings(), model);
             container.add(panel);
 
             /* Redraw the container. */
             container.validate();
         }
     }
 }
