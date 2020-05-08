 package org.jtheque.views.impl.windows;
 
 /*
  * This file is part of JTheque.
  *
  * JTheque is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License.
  *
  * JTheque is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with JTheque.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import org.jtheque.messages.IMessageService;
 import org.jtheque.ui.utils.builders.I18nPanelBuilder;
 import org.jtheque.ui.utils.builders.PanelBuilder;
 import org.jtheque.ui.utils.windows.dialogs.SwingFilthyBuildedDialogView;
 import org.jtheque.utils.ui.GridBagUtils;
 import org.jtheque.views.able.windows.IMessageView;
 import org.jtheque.views.impl.actions.messages.DisplayNextMessageAction;
 import org.jtheque.views.impl.actions.messages.DisplayPreviousMessageAction;
 import org.jtheque.views.impl.models.IMessageModel;
 import org.jtheque.views.impl.models.MessageModel;
 
 import javax.swing.JLabel;
 import javax.swing.JTextArea;
 
 /**
  * A view to display the messages of the module and the application. 
  *
  * @author Baptiste Wicht
  */
 public final class MessageView extends SwingFilthyBuildedDialogView<IMessageModel> implements IMessageView {
     private JLabel dateLabel;
     private JLabel sourceLabel;
     private JLabel titleLabel;
     private JTextArea messageArea;
 
     @Override
     protected void initView(){
         setModel(new MessageModel(getService(IMessageService.class)));
         setTitleKey("messages.view.title");
         setResizable(false);
     }
 
     @Override
     protected void buildView(I18nPanelBuilder builder){
         addLabels(builder);
         addFields(builder);
 
         builder.addButtonBar(builder.gbcSet(0, 4, GridBagUtils.HORIZONTAL, GridBagUtils.LINE_END, 2, 1),
                 getCloseAction("messages.actions.close"), new DisplayNextMessageAction(), new DisplayPreviousMessageAction());
     }
 
     /**
      * Add the labels to the view.
      *
      * @param builder The builder to add the labels to.
      */
     private static void addLabels(I18nPanelBuilder builder) {
         builder.addI18nLabel("messages.view.data.date", PanelBuilder.BOLD, builder.gbcSet(0, 0, GridBagUtils.NONE, GridBagUtils.LINE_END));
         builder.addI18nLabel("messages.view.data.source", PanelBuilder.BOLD, builder.gbcSet(0, 1, GridBagUtils.NONE, GridBagUtils.LINE_END));
         builder.addI18nLabel("messages.view.data.title", PanelBuilder.BOLD, builder.gbcSet(0, 2, GridBagUtils.NONE, GridBagUtils.LINE_END));
     }
 
     /**
      * Add the fields to the view.
      *
      * @param builder The builder to add the fields to.
      */
     private void addFields(PanelBuilder builder) {
         dateLabel = builder.addLabel(getModel().isDefaultMessage() ? "" : getModel().getCurrentMessage().getDate().getStrDate(), builder.gbcSet(1, 0));
         sourceLabel = builder.addLabel(getModel().isDefaultMessage() ? "" : getModel().getCurrentMessage().getSource(), builder.gbcSet(1, 1));
         titleLabel = builder.addLabel(getModel().isDefaultMessage() ? "" : getModel().getCurrentMessage().getTitle(), builder.gbcSet(1, 2));
         
         messageArea = builder.addScrolledTextArea(getModel().isDefaultMessage() ? "" : getModel().getCurrentMessage().getMessage(),
                 builder.gbcSet(0, 3, GridBagUtils.BOTH, GridBagUtils.LINE_START, 2, 1));
         messageArea.setRows(8);
         messageArea.setEnabled(false);
     }
 
     @Override
     public void next() {
         setMessage(getModel().getNextMessage());
     }
 
     @Override
     public void previous() {
         setMessage(getModel().getPreviousMessage());
     }
 
     /**
      * Set the message to display.
      *
      * @param message The message to display.
      */
     private void setMessage(Message message) {
         if (getModel().isDefaultMessage()) {
             dateLabel.setText(message.getDate().getStrDate());
             sourceLabel.setText(message.getSource());
             titleLabel.setText(message.getTitle());
             messageArea.setText(message.getMessage());
         }
     }
 }
