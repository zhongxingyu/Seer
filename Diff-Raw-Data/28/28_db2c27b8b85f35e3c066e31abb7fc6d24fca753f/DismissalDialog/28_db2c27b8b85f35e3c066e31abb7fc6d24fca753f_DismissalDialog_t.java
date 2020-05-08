 package org.iplantc.de.client.sysmsgs.view;
 
import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IPlantDialog;
 
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * This dialog verifies that the user really does want to dismiss a message.
  */
 final class DismissalDialog extends IPlantDialog {
 
     private final Command dismiss;
 
     /**
      * the constructor
      * 
      * @param dismiss the command to execute upon verification
      */
     DismissalDialog(final Command dismiss) {
         this.dismiss = dismiss;
        setWidget(new Label(I18N.DISPLAY.messageDismissQuery()));
     }
 
     /**
      * @see IPlantDialog#onOkButtonClicked()
      */
     @Override
     protected void onOkButtonClicked() {
         super.onOkButtonClicked();
         dismiss.execute();
     }
 
 }
