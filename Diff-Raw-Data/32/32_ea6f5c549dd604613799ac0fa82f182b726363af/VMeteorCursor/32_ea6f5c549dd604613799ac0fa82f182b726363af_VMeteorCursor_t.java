 package com.github.wolfie.meteorcursor.client.ui;
 
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Event.NativePreviewEvent;
 import com.google.gwt.user.client.Event.NativePreviewHandler;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.BrowserInfo;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.UIDL;
 
 public class VMeteorCursor extends Widget implements Paintable,
     NativePreviewHandler {
   
   private static class Particle extends Widget {
     public Particle(final int x, final int y, final double speed) {
       setElement(DOM.createDiv());
       final Element e = getElement();
       e.setClassName(PARTICLE_CLASSNAME);
       
       final Style style = e.getStyle();
       style.setTop(y + 10, Style.Unit.PX);
       style.setLeft(x + 10, Style.Unit.PX);
       style.setBackgroundColor(getBackground((speed - 10) * 10));
       
       new Animation() {
         private double deltaTop = -2;
         private double deltaLeft = -2;
         
         @Override
         protected void onUpdate(final double progress) {
           if (deltaTop == -2 && deltaLeft == -2) {
             deltaTop = Math.random() * 2 - 1;
             deltaLeft = Math.random() * 2 - 1;
           }
           
           if (progress < 1) {
             style.setTop(y + 10
                 + (PARTICLE_TRAVEL_LENGTH * progress * deltaTop) + GRAVITY
                 * progress * progress, Style.Unit.PX);
             style.setLeft(x + 10
                 + (PARTICLE_TRAVEL_LENGTH * progress * deltaLeft),
                 Style.Unit.PX);
             
             final double size = PARTICLE_SIZE - (PARTICLE_SIZE * progress);
             style.setHeight(size, Style.Unit.PX);
             style.setWidth(size, Style.Unit.PX);
             
           } else {
             removeFromParent();
           }
         }
         
         @Override
         protected double interpolate(final double progress) {
           // ease-out
           return 1.5 * progress - 0.5 * Math.pow(progress, 3);
         }
       }.run(PARTICLE_LIFETIME_MILLIS);
     }
     
     private String getBackground(final double speed) {
       final int colorInt = Math.min(Double.valueOf(speed).intValue(), 255);
       String hexString = Integer.toHexString(colorInt);
       hexString = hexString.length() < 2 ? "0" + hexString : hexString;
       return "#" + hexString + "0000";
     }
   }
   
   /** Set the CSS class name to allow styling. */
   public static final String CLASSNAME = "v-meteorcursor";
   public static final String PARTICLE_CLASSNAME = CLASSNAME + "-particle";
   
   private static final double THRESHOLD = 10;
   
   private static final int PARTICLE_LIFETIME_MILLIS = 1000;
   private static final double PARTICLE_TRAVEL_LENGTH = 100;
   private static final int PARTICLE_SIZE = 10;
  private static final int GRAVITY = 75;
   
   /** The client side widget identifier */
   protected String paintableId;
   
   /** Reference to the server connection object. */
   ApplicationConnection client;
   
   private int previousMouseY = -1;
   private int previousMouseX = -1;
   
   /**
    * The constructor should first call super() to initialize the component and
    * then handle any initialization relevant to Vaadin.
    */
   public VMeteorCursor() {
     Event.addNativePreviewHandler(this);
     
     // we need a div so that Vaadin doesn't throw a fit.
     setElement(Document.get().createDivElement());
     if (BrowserInfo.get().isIE6()) {
       getElement().getStyle().setProperty("overflow", "hidden");
       getElement().getStyle().setProperty("height", "0");
     }
   }
   
   /**
    * Called whenever an update is received from the server
    */
   public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {
     if (client.updateComponent(this, uidl, true)) {
       return;
     }
     
     this.client = client;
     paintableId = uidl.getId();
   }
   
   public void onPreviewNativeEvent(final NativePreviewEvent event) {
     final String type = event.getNativeEvent().getType();
     
     if ("mousemove".equals(type)) {
       final int mouseX = event.getNativeEvent().getClientX();
       final int mouseY = event.getNativeEvent().getClientY();
       
       final double speed = getDistanceTravelled(mouseX, mouseY, previousMouseX,
           previousMouseY);
       
       previousMouseX = mouseX;
       previousMouseY = mouseY;
       
       if (speed > THRESHOLD) {
         RootPanel.get().add(new Particle(mouseX, mouseY, speed));
       }
     }
   }
   
   private double getDistanceTravelled(final int x, final int y,
       final int previousX, final int previousY) {
     return Math.sqrt(Math.pow(x - previousX, 2) + Math.pow(y - previousY, 2));
   }
 }
