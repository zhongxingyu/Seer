 package de.flower.common.ui.feedback;
 
 import org.apache.wicket.model.IModel;
 
 import java.io.Serializable;
 
 /**
  * @author flowerrrr
  */
 public abstract class AlertMessage implements Serializable {
 
     private IModel<String> messageModel;
 
     private IModel<String> labelModel;
 
     public AlertMessage(final IModel<String> messageModel, IModel<String> labelModel) {
         this.messageModel = messageModel;
         this.labelModel = labelModel;
     }
 
     protected abstract boolean onClick(final AlertMessagePanel alertMessagePanel);
 
     public IModel<String> getMessageModel() {
         return messageModel;
     }
 
     public IModel<String> getLabelModel() {
         return labelModel;
     }
 
     protected boolean isVisible(final AlertMessagePanel alertMessagePanel) {
         return true && isVisible();
     }
 
     protected boolean isVisible() {
         return true;
     }

    /**
     * Returns string identifying this message. Id is used to mark messages as closed
     * for the current session. By default returns messageModel.getObject().
     * Subclasses can override if messages with same messageModel need to be handled separately.
     *
     * @return
     */
    public String getId() {
        return messageModel.getObject();
    }
 }
