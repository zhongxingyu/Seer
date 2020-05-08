 /**
  * 
  */
 package de.saumya.gwt.translation.common.client.widget;
 
 import java.sql.Timestamp;
 
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 
 import de.saumya.gwt.session.client.model.User;
 import de.saumya.gwt.translation.common.client.GetTextController;
 
 public class ResourceHeaderPanel extends FlowPanel {
 
     private final GetTextController getText;
 
     private final Label             keyLabel;
     private final Label             keyValue;
     private final Label             modifiedAtLabel;
     private final Label             modifiedAtValue;
     private final Label             byLabel;
     private final Label             modifiedByLabel;
     private final Label             modifiedByValue;
 
     public ResourceHeaderPanel(final GetTextController getText) {
         setStyleName("header-panel");
         this.getText = getText;
         this.keyLabel = label("key");
         this.keyValue = label();
         this.modifiedAtLabel = label("modified at");
         this.modifiedAtValue = label();
         this.byLabel = label("by");
         this.modifiedByLabel = label("modified by");
         this.modifiedByValue = label();
     }
 
     public void reset(final String keyValue, final Timestamp updatedAt,
             final User updatedBy) {
         if (keyValue != null) {
             this.keyValue.setText("\u00a0" + keyValue + "\u00a0");
         }
        this.keyValue.setVisible(keyValue != null);
        this.keyLabel.setVisible(keyValue != null);
        this.modifiedByLabel.setVisible(false);
        this.byLabel.setVisible(false);
         if (updatedAt != null) {
             this.modifiedAtValue.setText("\u00a0" + updatedAt.toString()
                     + "\u00a0");
             if (updatedBy != null) {
                 this.byLabel.setVisible(true);
                 this.modifiedByValue.setText("\u00a0" + updatedBy.display());
             }
         }
         else if (updatedBy != null) {
             this.modifiedByLabel.setVisible(true);
             this.modifiedByValue.setText(updatedBy.display());
         }
        this.modifiedByValue.setVisible(updatedBy != null);
        this.modifiedAtLabel.setVisible(updatedAt != null);
        this.modifiedAtValue.setVisible(updatedAt != null);

         setVisible(true);
     }
 
     private Label label() {
         return label(null);
     }
 
     private Label label(final String labelValue) {
         final Label label = labelValue == null
                 ? new Label()
                 : new TranslatableLabel(labelValue, this.getText);
         label.setVisible(false);
         add(label);
         return label;
     }
 }
