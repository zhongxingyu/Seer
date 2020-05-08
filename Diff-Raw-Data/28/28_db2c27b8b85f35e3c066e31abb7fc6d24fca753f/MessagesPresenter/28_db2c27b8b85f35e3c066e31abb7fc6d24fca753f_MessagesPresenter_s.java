 package org.iplantc.de.client.sysmsgs.presenter;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.NewSystemMessagesEvent;
 import org.iplantc.de.client.sysmsgs.model.IdList;
 import org.iplantc.de.client.sysmsgs.model.Message;
 import org.iplantc.de.client.sysmsgs.model.MessageFactory;
 import org.iplantc.de.client.sysmsgs.model.MessageList;
 import org.iplantc.de.client.sysmsgs.services.Services;
 import org.iplantc.de.client.sysmsgs.view.MessagesView;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.sencha.gxt.data.shared.ListStore;
 
 /**
  * The system messages presenter.
  */
 public final class MessagesPresenter implements MessagesView.Presenter<Message> {
 
     interface MessageProperties extends MessagesView.MessageProperties<Message> {
     }
 
     private static final MessageProperties MSG_PROPS = GWT.create(MessageProperties.class);
     private static final Services services = GWT.create(Services.class);
     private static final MessagesView.Factory<Message> VIEW_FACTORY = GWT.create(MessagesView.Factory.class);
     
     private final MessagesView<Message> view;
 
     private String selectedMsgId;
     private HandlerRegistration updateHandlerReg;
 	
     /**
      * the constructor
      */
     public MessagesPresenter(final String startingSelectedMsgId) {
         view = VIEW_FACTORY.make(this, MSG_PROPS, new ActivationTimeRenderer());
         selectedMsgId = startingSelectedMsgId == "" ? null : startingSelectedMsgId;
         updateHandlerReg = null;
     }
 
     /**
      * @see MessageView.Presenter<T>#handleDismissMessage(T)
      */
     @Override
     public void handleDismissMessage(final Message message) {
         handleSelectMessage(message);
         view.verifyMessageDismissal(new Command() {
             @Override
             public void execute() {
                 dismissSelectedMessage();
             }
         });
     }
 
     /**
      * @see MessageView.Presenter<T>#handleSelectMessage(T)
      */
     @Override
     public void handleSelectMessage(final Message message) {
         selectedMsgId = message.getId();
         view.getSelectionModel().select(false, message);
         showBodyOf(message);
         showExpiryOf(message);
         markSeen(message);
     }
 
     /**
      * Returns the Id of the currently selected message.
      * 
      * @return the message Id or null if there is no currently selected message
      */
     public String getSelectedMessageId() {
         return selectedMsgId;
     }
 
     /**
      * Starts the presenter and attaches the view to the provided container. This also starts the
      * message caching.
      * 
      * @param container The container that will hold the view.
      */
 	public void go(final AcceptsOneWidget container) {
         if (container == null) {
             stop();
         } else {
             loadAllMessages();
             if (updateHandlerReg == null) {
                 updateHandlerReg = EventBus.getInstance().addHandler(NewSystemMessagesEvent.TYPE, new NewSystemMessagesEvent.Handler() {
                     @Override
                     public void onUpdate(final NewSystemMessagesEvent event) {
                         loadNewMessages();
                     }
                 });
             }
             container.setWidget(view);
             view.showLoading();
         }
 	}
 	
     /**
      * This should be called when the container holding the view has been closed. It stops the
      * message caching.
      */
     public void stop() {
         if (updateHandlerReg != null) {
             updateHandlerReg.removeHandler();
             updateHandlerReg = null;
         }
     }
 
     private void loadAllMessages() {
         services.getAllMessages(new AsyncCallback<MessageList>() {
             @Override
             public void onSuccess(final MessageList messages) {
                 markReceived(messages);
                 replaceMessages(messages);
             }
             @Override
             public void onFailure(final Throwable cause) {
                // TODO externalize string
                ErrorHandler.post("The system messages could not be loaded.", cause);
             }
         });
     }
 
     private void loadNewMessages() {
         services.getNewMessages(new AsyncCallback<MessageList>() {
             @Override
             public void onSuccess(final MessageList messages) {
                 markReceived(messages);
                 addMessages(messages);
             }
 
             @Override
             public void onFailure(Throwable caught) {}
         });
     }
 
     private void markReceived(final MessageList messages) {
         final ArrayList<String> ids = new ArrayList<String>();
         for (Message msg : messages.getList()) {
             ids.add(msg.getId());
         }
         final IdList idsDTO = MessageFactory.INSTANCE.makeIdList().as();
         idsDTO.setIds(ids);
         services.markReceived(idsDTO, new AsyncCallback<Void>() {
             @Override
             public void onSuccess(Void unused) {}
             @Override
             public void onFailure(final Throwable cause) {
                // TODO externalize string
                ErrorHandler.post("The system messages could not be marked as received.", cause);
             }
         });
     }
 
     private void markSeen(final Message message) {
         final IdList idsDTO = MessageFactory.INSTANCE.makeIdList().as();
         idsDTO.setIds(Arrays.asList(message.getId()));
         services.acknowledgeMessages(idsDTO, new AsyncCallback<Void>() {
             @Override
             public void onSuccess(Void unused) {
                 markLocalSeen(message);
             }
             @Override
             public void onFailure(final Throwable cause) {
                // TODO externalize string
                ErrorHandler.post("The system message could not be marked as seen.", cause);
             }
         });
     }
 
     private void dismissSelectedMessage() {
         if (!getSelectedMessage().isDismissible()) {
             return;
         }
         final IdList idsDTO = MessageFactory.INSTANCE.makeIdList().as();
         idsDTO.setIds(Arrays.asList(selectedMsgId));
        // TODO externalize message
        view.mask("dismissing message");
         services.hideMessages(idsDTO, new AsyncCallback<Void>() {
             @Override
             public void onSuccess(final Void unused) {
                 removeSelectedMessage();
                 view.unmask();
             }
             @Override
             public void onFailure(final Throwable cause) {
                 view.unmask();
                ErrorHandler.post("The system message could not be dismissed.", cause);
             }
         });
     }
 
     private void addMessages(final MessageList messages) {
         final ListStore<Message> store = view.getMessageStore();
         for (Message msg : messages.getList()) {
             if (store.findModel(msg) == null) {
                 store.add(msg);
             } else {
                 store.update(msg);
             }
         }
         store.applySort(false);
         updateView();
     }
 
     private void replaceMessages(final MessageList messages) {
         view.getMessageStore().replaceAll(messages.getList());
         updateView();
     }
 
     private void updateView() {
         final ListStore<Message> store = view.getMessageStore();
         if (store.size() <= 0) {
             showNoMessages();
         } else {
             final Message selMsg = selectedMsgId == null ? null : store.findModelWithKey(selectedMsgId);
             showMessageSelected(selMsg == null ? store.get(0) : selMsg);
         }
     }
 
     private void markLocalSeen(final Message message) {
         message.setSeen(true);
         view.getMessageStore().update(message);
     }
 
     private void removeSelectedMessage() {
         final Message selMsg = getSelectedMessage();
         final int idx = view.getMessageStore().indexOf(selMsg);
         view.getMessageStore().remove(selMsg);
         if (view.getMessageStore().size() <= 0) {
             showNoMessages();
         } else {
             final int newIdx = view.getMessageStore().size() <= idx ? idx - 1 : idx;
             showMessageSelected(view.getMessageStore().get(newIdx));
 		}
 	}
 	
     private void showNoMessages() {
         selectedMsgId = null;
         view.showNoMessages();
     }
 
     private void showMessageSelected(final Message msg) {
         selectedMsgId = msg.getId();
         view.getSelectionModel().select(msg, false);
         view.showMessages();
 	}
 
     private void showBodyOf(final Message message) {
         final SafeHtmlBuilder bodyBuilder = new SafeHtmlBuilder();
         bodyBuilder.appendHtmlConstant(message.getBody());
         view.setMessageBody(bodyBuilder.toSafeHtml());
     }
 
     private void showExpiryOf(final Message message) {
         final DateTimeFormat expiryFmt = DateTimeFormat.getFormat("dd MMMM yyyy");
         final String expiryStr = expiryFmt.format(message.getDeactivationTime());
         view.setExpiryMessage(I18N.DISPLAY.expirationMessage(expiryStr));
     }
 
     private Message getSelectedMessage() {
         return view.getMessageStore().findModelWithKey(selectedMsgId);
     }
 
 }
