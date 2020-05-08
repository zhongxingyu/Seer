 package org.vaadin.kim.textcustomlayout;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.vaadin.kim.textcustomlayout.TextCustomLayout.LocationClickEvent.LocationClickListener;
 
 import com.vaadin.event.LayoutEvents.LayoutClickNotifier;
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.terminal.gwt.client.EventId;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CustomLayout;
 
 /**
  * Server side component for the VTextCustomLayout widget.
  */
 @com.vaadin.ui.ClientWidget(org.vaadin.kim.textcustomlayout.client.ui.VTextCustomLayout.class)
 public class TextCustomLayout extends CustomLayout {
 
     /**
      * 
      */
     private static final long serialVersionUID = 3794686965290253910L;
 
     private static final String CLICK_EVENT = EventId.LAYOUT_CLICK;
 
     private final HashMap<String, String> textSlots = new HashMap<String, String>();
 
     private final Map<String, List<LocationClickListener>> locationListeners = new HashMap<String, List<LocationClickListener>>();
 
     /**
      * Constructs a custom layout with the template given in the stream.
      * 
      * @param templateStream
      *            Stream containing template data. Must be using UTF-8 encoding.
      *            To use a String as a template use for instance new
      *            ByteArrayInputStream("<template>".getBytes()).
      * @param streamLength
      *            Length of the templateStream
      * @throws IOException
      */
     public TextCustomLayout(InputStream templateStream) throws IOException {
         super(templateStream);
     }
 
     /**
      * Constructor for custom layout with given template name. Template file is
      * fetched from "<theme>/layout/<templateName>".
      */
     public TextCustomLayout(String template) {
         super(template);
     }
 
     public void addText(String text, String location) {
         Component c = getComponent(location);
         if (c != null) {
             removeComponent(c);
         }
 
         textSlots.put(location, text);
         requestRepaint();
     }
 
     public void removeText(String location) {
         textSlots.remove(location);
         requestRepaint();
     }
 
     @Override
     public void addComponent(Component c, String location) {
         super.addComponent(c, location);
         removeText(location);
     }
 
     /**
      * Gets a text by its location.
      * 
      * @param location
      *            the name of the location where the requested text resides.
      * @return the text in the given location or null if not found.
      */
     public String getText(String location) {
         return textSlots.get(location);
     }
 
     /**
      * Paints the content of this component.
      * 
      * @param target
      * @throws PaintException
      *             if the paint operation failed.
      */
     @Override
     public void paintContent(PaintTarget target) throws PaintException {
         super.paintContent(target);
 
         // Adds all items in all the locations
         for (String location : textSlots.keySet()) {
             final String text = textSlots.get(location);
             target.startTag("location");
             target.addAttribute("name", location);
             target.addAttribute("text", text);
             target.endTag("location");
         }
 
         for (String location : locationListeners.keySet()) {
             target.startTag("locationListener");
             target.addAttribute("name", location);
             target.endTag("locationListener");
         }
     }
 
     public void addListener(LocationClickListener listener, String location) {
         if (location == null || listener == null) {
             throw new IllegalArgumentException(
                     "Both listener and location must be set");
         }
 
         List<LocationClickListener> listeners;
         if (locationListeners.containsKey(location)) {
             listeners = locationListeners.get(location);
         } else {
             listeners = new ArrayList<TextCustomLayout.LocationClickEvent.LocationClickListener>();
             locationListeners.put(location, listeners);
         }
 
         listeners.add(listener);
         requestRepaint();
     }
 
     public void removeListener(LocationClickListener listener, String location) {
         if (location == null || listener == null) {
             throw new IllegalArgumentException(
                     "Both listener and location must be set");
         }
 
         List<LocationClickListener> listeners;
         if (locationListeners.containsKey(location)) {
             listeners = locationListeners.get(location);
         } else {
             return;
         }
 
         listeners.remove(listener);
         requestRepaint();
     }
 
     public void removeAllListeners(String location) {
         if (location == null) {
             throw new IllegalArgumentException("Location must be set");
         }
 
         locationListeners.remove(location);
         requestRepaint();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void changeVariables(Object source, Map<String, Object> variables) {
         if (variables.containsKey("location")) {
             String location = (String) variables.get("location");
             fireLocationClickEvent(location);
         } else if (this instanceof LayoutClickNotifier
                 && variables.containsKey(CLICK_EVENT)) {
             fireClick((Map<String, Object>) variables.get(CLICK_EVENT));
         }
     }
 
     private void fireLocationClickEvent(String location) {
         List<LocationClickListener> listeners = locationListeners.get(location);
         if (listeners != null) {
             LocationClickEvent event = new LocationClickEvent();
             event.setLocation(location);
             event.setSource(this);
             for (LocationClickListener listener : listeners) {
                 listener.locationClicked(event);
             }
         }
     }
 
     public static class LocationClickEvent implements Serializable {
 
         /**
          * 
          */
         private static final long serialVersionUID = -7062492274326680177L;
 
         private String location;
 
         private TextCustomLayout source;
 
         public String getLocation() {
             return location;
         }
 
         public void setLocation(String location) {
             this.location = location;
         }
 
         public TextCustomLayout getSource() {
             return source;
         }
 
         public void setSource(TextCustomLayout source) {
             this.source = source;
         }
 
         public static interface LocationClickListener {
             public void locationClicked(LocationClickEvent event);
         }
 
     }
 
 }
