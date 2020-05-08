 package ch9k.plugins.carousel;
 
 import ch9k.chat.Conversation;
 import ch9k.core.settings.Settings;
 import ch9k.plugins.AbstractPlugin;
 import ch9k.plugins.AbstractPluginInstance;
 import ch9k.plugins.Plugin;
 import javax.swing.JPanel;
 
 import javax.swing.JFrame;
 import java.net.InetAddress;
 import ch9k.chat.Contact;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventPool;
 import ch9k.chat.event.RequestedPluginContainerEvent;
 import ch9k.plugins.flickr.FlickrImageProvider;
 
 /**
  * Plugin for a standard image carousel.
  */
 public class CarouselPlugin extends AbstractPlugin {
     @Override
     protected AbstractPluginInstance createPluginInstance(
             Plugin plugin, Conversation conversation, Settings settings) {
         return new Carousel(plugin, conversation, settings);
     }
 
     @Override
     protected JPanel createPreferencePane(Settings settings) {
         return new CarouselPreferencePane(settings);
     }
 
     @Override
     protected Settings createDefaultSettings() {
         Settings settings = new Settings();
         settings.setInt(CarouselPreferencePane.MAX_IMAGES, 10);
         return settings;
     }
 
     public static void main(String[] args) throws Exception {
         JFrame frame = new JFrame();
         Contact contact = new Contact("JPanneel",
                 InetAddress.getByName("google.be"));
         Conversation conversation = new Conversation(contact, true);
         CarouselPlugin plugin = new CarouselPlugin();
         plugin.enablePlugin(conversation, plugin.createDefaultSettings());
 
         //plugin.onReceivePanel(frame.getContentPane());
         Event event = new RequestedPluginContainerEvent(conversation,
                 frame.getContentPane());
         EventPool.getAppPool().raiseEvent(event);
 
         frame.pack();
         frame.setTitle("Carousel test.");
         frame.setVisible(true);
 
         FlickrImageProvider flickr = new FlickrImageProvider(null, 
                 conversation, new Settings());
         flickr.sendNewImageEvent("jailbait");
     }
 }
