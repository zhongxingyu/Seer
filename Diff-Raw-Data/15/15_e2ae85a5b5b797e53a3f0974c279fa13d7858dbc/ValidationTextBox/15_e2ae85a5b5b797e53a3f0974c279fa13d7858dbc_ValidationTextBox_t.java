 package org.pentaho.gwt.widgets.client.text;
 
 import org.pentaho.gwt.widgets.client.utils.ElementUtils;
 import org.pentaho.gwt.widgets.client.utils.Rectangle;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class ValidationTextBox extends HorizontalPanel implements IValidationTextBox{
 
   private TextBox textBox;
   
   private PopupPanel popupPanel;
   
   private String validationMessage;
   
   private SimplePanel imagePanel;
   
   private Image image;
   
   private ValidationTextBoxKeyUpHandlerCollection handlers;
   
   private ValidationTextBoxListenerCollection listeners;
   
   private static final int DEFAULT_OFFSET = 9;
   
   public ValidationTextBox() {
     textBox = new TextBox();
     textBox.addKeyUpHandler(new KeyUpHandler() {
       
       @Override
       public void onKeyUp(KeyUpEvent event) {
        performValidation(true);
         fireOnKeyUp(event);
       }
     });
 
     imagePanel = new SimplePanel() {
 
       @Override
       public void onBrowserEvent(Event event) {
         super.onBrowserEvent(event);
         switch (DOM.eventGetType(event)) {
           case Event.ONMOUSEOVER: {
             if(!validate()) {
               showMessagePopup();
             }
             break;
           }
 
           case Event.ONMOUSEOUT: {
             hideMessagePopup();
             break;
           }
         }
       }
     };
     imagePanel.setStylePrimaryName("validation-textbox-image-panel"); //$NON-NLS-1$
     imagePanel.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
     this.add(textBox);
     textBox.setWidth("100%");
     this.setCellWidth(textBox, "100%");
     this.setStylePrimaryName("custom-text-box"); //$NON-NLS-1$
     SimplePanel hSpacer = new SimplePanel();
     hSpacer.setWidth("10px");
     this.add(hSpacer);
     image = new Image(GWT.getModuleBaseURL() + "images/spacer.gif"); //$NON-NLS-1$
     image.setStylePrimaryName("validation-textbox-image");
     imagePanel.add(image);
     imagePanel.addStyleDependentName("invalid");
     this.add(imagePanel);
   }
   
   public void addKeyUpHandler(KeyUpHandler handler) {
     if (handlers == null) {
       handlers = new ValidationTextBoxKeyUpHandlerCollection();
     }
     handlers.add(handler);
   }
 
   public void removeKeyUpHandler(KeyUpHandler handler) {
     if (handlers != null) {
       handlers.remove(handler);
     }
   }
 
   /**
    * Fire all current {@link KeyUpHandler}.
    */
   void fireOnKeyUp(KeyUpEvent event) {
 
     if (handlers != null) {
       handlers.fireOnKeyUp(event);
     }
   }
 
   public void addValidatableTextBoxListener(IValidationTextBoxListener listener) {
     if (listeners == null) {
       listeners = new ValidationTextBoxListenerCollection();
     }
     listeners.add(listener);
   }
 
   public void removeValidatableTextBoxListener(IValidationTextBoxListener listener) {
     if (listeners != null) {
       listeners.remove(listener);
     }
   }
 
   /**
    * Fire all current {@link IValidationTextBoxListener}.
    */
   void fireOnSuccess() {
     if (listeners != null) {
       listeners.fireOnSuccess(this);
     }
   }
 
   /**
    * Fire all current {@link IValidationTextBoxListener}.
    */
   void fireOnFailure() {
     if (listeners != null) {
       listeners.fireOnFailure(this);
     }
   }
   
  private void performValidation(boolean showPopUp) {
     if(!validate()) {
       fireOnFailure();
       imagePanel.removeStyleDependentName("valid");
       imagePanel.addStyleDependentName("invalid");
      if(showPopUp) {
    	  showMessagePopup();
      }
     } else {
       fireOnSuccess();
       imagePanel.removeStyleDependentName("invalid");
       imagePanel.addStyleDependentName("valid"); 
       hideMessagePopup();
     }
   }
   
   @Override
   public boolean validate() {
     // TODO Auto-generated method stub
     return false;
   }
   
   public String getValidationMessage() {
     return validationMessage;
   }
   public void setValidationMessage(String validationMessage) {
     this.validationMessage = validationMessage;
   }
   public TextBox getManagedObject() {
     return textBox;
   }
   
   public String getValue() {
     return textBox.getValue();
   }
   
   public void setValue(String value) {
     textBox.setValue(value);
    performValidation(false);      
   }
   
   public String getText() {
     return textBox.getText();
   }
   
   private void showMessagePopup() {
     Label validationMessageLabel = new Label();
     validationMessageLabel.setStyleName("validation-textbox-message-label"); //$NON-NLS-1$
     validationMessageLabel.setText(getValidationMessage());
     VerticalPanel messagePanel = new VerticalPanel();
     messagePanel.add(validationMessageLabel);
     HorizontalPanel bottomPanel = new HorizontalPanel();
     SimplePanel hSpacer = new SimplePanel();
     hSpacer.setStylePrimaryName("validation-textbox-left-image-buffer"); //$NON-NLS-1$
     bottomPanel.add(hSpacer);
     SimplePanel tailImagePanel = new SimplePanel();
     image = new Image(GWT.getModuleBaseURL() + "images/spacer.gif"); //$NON-NLS-1$
     image.setStylePrimaryName("validation-textbox-tail-image"); //$NON-NLS-1$
     tailImagePanel.add(image);
     bottomPanel.add(tailImagePanel);
     messagePanel.add(bottomPanel);
     popupPanel = new PopupPanel(true, false);
     popupPanel.setWidget(messagePanel);
     popupPanel.setPopupPositionAndShow(new PositionCallback() {
       public void setPosition(int offsetWidth, int offsetHeight) {
         int absLeft = -1;
         int absTop = -1;
         absLeft = textBox.getAbsoluteLeft();
         absTop = textBox.getAbsoluteTop();
         Rectangle popupSize = ElementUtils.getSize(popupPanel.getElement());
         popupPanel.setPopupPosition(absLeft, absTop - popupSize.height >= 0 ? absTop - popupSize.height: absTop);
       }
     });      
     popupPanel.show(); 
   }
   
   private void hideMessagePopup() {
     if(popupPanel != null) {
       popupPanel.hide();      
     }
   }
   
 }
