 package com.vaadin.incubator.ratingstars.gwt.client.ui;
 
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.ui.FocusWidget;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.UIDL;
 
 public class VRatingStars extends FocusWidget implements Paintable {
 
     /** Set the tagname used to statically resolve widget from UIDL. */
     public static final String TAGNAME = "ratingstars";
 
     /** Set the CSS class names to allow styling. */
     public static final String CLASSNAME = "v-" + TAGNAME;
     public static final String STAR_CLASSNAME = CLASSNAME + "-star";
     public static final String BAR_CLASSNAME = CLASSNAME + "-bar";
     public static final String WRAPPER_CLASSNAME = CLASSNAME + "-wrapper";
 
     private static final int ANIMATION_DURATION_IN_MS = 150;
 
     /** Component identifier in UIDL communications. */
     String uidlId;
 
     /** Reference to the server connection object. */
     ApplicationConnection client;
 
     private Element barDiv;
 
     private Element element;
 
     private int width;
 
     private int height;
 
     private boolean hasFocus;
 
     /** Values from the UIDL */
     private static final String ATTR_MAX_VALUE = "maxValue";
     private static final String ATTR_VALUE = "value";
     private static final String ATTR_ANIMATED = "animated";
     private static final String ATTR_IMMEDIATE = "immediate";
     private static final String ATTR_READONLY = "readonly";
     private static final String ATTR_DISABLED = "disabled";
     private int maxValue;
     private double value;
     private boolean animated;
     private boolean immediate;
     private boolean readonly;
     private boolean disabled;
 
     public VRatingStars() {
         setElement(Document.get().createDivElement());
         setStyleName(WRAPPER_CLASSNAME);
 
         element = Document.get().createDivElement();
         element.setClassName(CLASSNAME);
         getElement().appendChild(element);
     }
 
     public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
         // This call should be made first. Ensure correct implementation,
         // and let the containing layout manage caption, etc.
         if (client.updateComponent(this, uidl, true)) {
             return;
         }
 
         // Save reference to server connection object to be able to send
         // user interaction later
         this.client = client;
 
         // Save the UIDL identifier for the component
         uidlId = uidl.getId();
 
         // Collect the relevant values from UIDL
         maxValue = uidl.getIntAttribute(ATTR_MAX_VALUE);
         value = uidl.getDoubleAttribute(ATTR_VALUE);
         animated = uidl.getBooleanAttribute(ATTR_ANIMATED);
         immediate = uidl.getBooleanAttribute(ATTR_IMMEDIATE);
         disabled = uidl.getBooleanAttribute(ATTR_DISABLED);
         readonly = uidl.getBooleanAttribute(ATTR_READONLY);
 
         if (!disabled && !readonly) {
             sinkEvents(Event.ONCLICK);
             sinkEvents(Event.ONMOUSEOVER);
             sinkEvents(Event.ONMOUSEOUT);
             sinkEvents(Event.ONFOCUS);
             sinkEvents(Event.ONBLUR);
             sinkEvents(Event.ONKEYUP);
         }
 
         if (barDiv == null) {
             // DOM structure not yet constructed
             for (int i = 0; i < maxValue; i++) {
                 DivElement starDiv = createStarDiv(i + 1);
                 element.appendChild(starDiv);
                 width += starDiv.getClientWidth();
                 if (height < starDiv.getClientHeight()) {
                     height = starDiv.getClientHeight();
                 }
             }
             barDiv = createBarDiv(height);
             element.appendChild(barDiv);
         } else {
             setBarWidth(calcBarWidth(value));
         }
         element.getStyle().setPropertyPx("width", width);
         element.getStyle().setPropertyPx("height", height);
     }
 
     @Override
     public void onBrowserEvent(Event event) {
         if (uidlId == null || client == null) {
             return;
         }
 
         super.onBrowserEvent(event);
 
         Element target = Element.as(event.getEventTarget());
         switch (DOM.eventGetType(event)) {
         case Event.ONCLICK:
             // update value
             if (target.getClassName().equals(STAR_CLASSNAME)) {
                 int ratingValue = target.getPropertyInt("rating");
                 value = ratingValue;
                 client.updateVariable(uidlId, "value", ratingValue, immediate);
             }
             break;
         case Event.ONMOUSEOVER:
             // animate
             if (target.getClassName().equals(STAR_CLASSNAME)) {
                 setBarWidth(calcBarWidth(target.getPropertyInt("rating")));
             }
             break;
         case Event.ONMOUSEOUT:
             // animate
             setBarWidth(calcBarWidth(value));
             break;
         case Event.ONFOCUS:
             hasFocus = true;
             break;
         case Event.ONBLUR:
             hasFocus = false;
             break;
         case Event.ONKEYUP:
             handleKeyUp(event);
             break;
         }
     }
 
     public void handleKeyUp(Event event) {
         if (hasFocus) {
             if (event.getKeyCode() == KeyCodes.KEY_RIGHT) {
                 // TODO: increment currently focused star
             } else if (event.getKeyCode() == KeyCodes.KEY_LEFT) {
                 // TODO: decrement currently focused star
             } else if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                 // TODO: set value of currently focused star
             }
         }
     }
 
     /**
      * Creates the DivElement of the bar representing the current value.
      * 
      * @return the newly created DivElement representing the bar.
      * @see #setBarWidth(byte)
      */
     private DivElement createBarDiv(int height) {
         DivElement barDiv = Document.get().createDivElement();
         barDiv.setClassName(BAR_CLASSNAME);
         barDiv.getStyle().setProperty("width", calcBarWidth(value) + "%");
         barDiv.getStyle().setPropertyPx("height", height);
         return barDiv;
     }
 
     /**
      * Sets the width of the bar div instantly or via animated progress
      * depending on the value of the <code>animated</code> property.
      */
     private void setBarWidth(final byte widthPercentage) {
         if (barDiv == null) {
             return;
         }
 
         if (!animated) {
             barDiv.getStyle().setProperty("width", widthPercentage + "%");
         } else {
             String currentWidth = barDiv.getStyle().getProperty("width");
             final byte startPercentage = Byte.valueOf(currentWidth.substring(0,
                     currentWidth.length() - 1));
             Animation animation = new Animation() {
                 @Override
                 protected void onUpdate(double progress) {
                     byte newWidth = (byte) (startPercentage + (progress * (widthPercentage - startPercentage)));
                     barDiv.getStyle().setProperty("width", newWidth + "%");
                 }
             };
             animation.run(ANIMATION_DURATION_IN_MS);
         }
     }
 
     /**
      * Calculates the bar width for the given <code>forValue</code> as a
      * percentage of the <code>maxValue</code>. Returned value is from 0 to 100.
      * 
      * @return width percentage (0..100)
      */
     private byte calcBarWidth(double forValue) {
         return (byte) (forValue * 100 / maxValue);
     }
 
     /**
      * Creates a DivElement representing a single star. Given
      * <code>rating</code> value is set as an int property for the div.
      * 
      * @param rating
      *            rating value of this star.
      * @return a DivElement representing a single star.
      */
     private DivElement createStarDiv(int rating) {
         DivElement starDiv = Document.get().createDivElement();
         starDiv.setClassName(STAR_CLASSNAME);
         starDiv.setPropertyInt("rating", rating);
         return starDiv;
     }
 
 }
