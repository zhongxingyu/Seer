 package de.flower.rmt.ui.feedback;
 
 import de.flower.common.ui.feedback.AlertMessage;
 import de.flower.common.ui.feedback.AlertMessagePanel;
 import de.flower.rmt.service.IApplicationService;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 /**
  * @author flowerrrr
  */
 public class MessageOfTheDayMessage extends AlertMessage {
 
     @SpringBean
     private IApplicationService applicationService;
 
     public MessageOfTheDayMessage() {
         super(null, null);
         setMessageModel(getMOTDModel());
     }
 
     @Override
     public boolean onClick(final AlertMessagePanel alertMessagePanel) {
         throw new UnsupportedOperationException("This message does not display a clickable button!");
     }
 
     @Override
     public boolean isVisible(final AlertMessagePanel alertMessagePanel) {
         return !StringUtils.isBlank(getMessageModel().getObject());
     }
 
     IModel<String> getMOTDModel() {
         return Model.of(applicationService.getMessageOfTheDay());
     }
 
     /**
      * Motd might contain html markup.
      * @return
      */
     @Override
     public boolean getEscapeModelStrings() {
         return false;
     }
 }
