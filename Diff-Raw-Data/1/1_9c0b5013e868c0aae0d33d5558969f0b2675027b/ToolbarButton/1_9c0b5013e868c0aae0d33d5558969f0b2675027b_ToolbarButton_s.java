 package org.pentaho.pac.client.common.ui;
 
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.PushButton;
 
 /**
  * Manages a PushButton in the common toolbar {@link Toolbar}. 
  * 
  * Note, it's not a subclass of PushButton as the PushButton api does not allow the 
  * changing of the image after instantiation. It is not a decorator because the GWT
  * PushButton does not implement an interface. If these limitations change, please 
  * change this class.
  *  
  * @author nbaker
  *
  */
 public class ToolbarButton {
   private PushButton button = new PushButton();
   private boolean enabled = true;
   private boolean visible = true;
   private String text;
   private ClickListener clickListener;
   private Image image;
   
   /**
    * Constructs a toolbar button with an image and a label
    * 
    * @param img GWT Image object 
    * @param label String containing an option label
    */
   public ToolbarButton(Image img, String label){
     this(img);
     button.setText(label);
   }
   
   /**
    * Constructs a toolbar button with an image, currently hardcoded to 16x16
    * 
    * @param img GWT Image object 
    */
   public ToolbarButton(Image img){
     button = new PushButton(img);
     button.setStylePrimaryName("common-toolbar-button");
   }
 
   
   /**
    * Gets the enabled status of the button.
    * 
    * @return boolean flag
    */
   public boolean isEnabled() {
     return enabled;  
   }
 
   /**
    * Sets the enabled status of the button.
    * 
    * @param enabled boolean flag
    */
   public void setEnabled(boolean enabled) {
     this.enabled = enabled;
     button.setEnabled(enabled);
   }
 
   /**
    * Gets the visibility of the button]
    * 
    * @return boolean flag
    */
   public boolean isVisible() {
     return visible;
   }
 
   /**
    * Sets the visibility of the button
    * 
    * @param visible boolean flag
    */
   public void setVisible(boolean visible) {
     this.visible = visible;
     button.setVisible(visible);
   }
   
   /**
    * Returns the managed PushButton object
    * 
    * @return PushButton concreate object
    */
   public PushButton getPushButton(){
     return button;
   }
 
   /**
    * Returns the image displayed on this button.
    * 
    * @return GWT Image
    */
   public Image getImage() {
     return image;
   }
 
   /**
    * Returns the optional text to be displayed on this button.
    * 
    * @return String
    */
   public String getText() {
     return text;
   }
 
   /**
    * Sets the optional text to be displayed on this button.
    * 
    * @param text String to be displayed
    */
   public void setText(String text) {
     this.text = text;
     button.setText(text);
   }
 
   /**
    * Returns the click listener attached to the button instance.
    * 
    * @return ClickListener
    */
   public ClickListener getClickListener() {
     return clickListener;
   }
 
   /**
    * Sets the ClickListener on the button. If a ClickListener was previously
    * added to the button, it will be removed (let Nick know if you don't like
    * this behavior).
    * 
    * @param clickListener
    */
   public void setClickListener(ClickListener clickListener) {
     if(this.clickListener != null){
       button.removeClickListener(this.clickListener);
     }
     this.clickListener = clickListener;
     button.addClickListener(this.clickListener);
     
   }
   
   
   
 }
