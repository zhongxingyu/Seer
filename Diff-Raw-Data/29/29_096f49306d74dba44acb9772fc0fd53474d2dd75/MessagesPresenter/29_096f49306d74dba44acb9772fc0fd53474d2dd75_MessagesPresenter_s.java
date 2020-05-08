 package org.iplantc.de.client.sysmsgs.presenter;
 
 import java.util.Arrays;
 
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.NewSystemMessagesEvent;
 import org.iplantc.de.client.sysmsgs.model.IdList;
 import org.iplantc.de.client.sysmsgs.model.Message;
 import org.iplantc.de.client.sysmsgs.model.MessageFactory;
 import org.iplantc.de.client.sysmsgs.model.MessageList;
 import org.iplantc.de.client.sysmsgs.services.ServiceFacade;
 import org.iplantc.de.client.sysmsgs.view.MessagesView;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Window;
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
     private static final MessagesView.Factory<Message> VIEW_FACTORY = GWT.create(MessagesView.Factory.class);
     
     private final ServiceFacade services = new ServiceFacade();
     private final MessagesView<Message> view = VIEW_FACTORY.make(this, MSG_PROPS, new ActivationTimeRenderer());
 
     private HandlerRegistration updateHandlerReg = null;
     private boolean hasActiveSvcCall = false;
 	
     /**
      * @see MessageView.Presenter<T>#handleDismissMessage(T)
      */
     @Override
     public void handleDismissMessage(final Message message) {
         view.verifyMessageDismissal(new Command() {
             @Override
             public void execute() {
                 dismissMessage(message);
             }
         });
     }
 
     /**
      * @see MessageView.Presenter<T>#handleSelectMessage(T)
      */
     @Override
     public void handleSelectMessage(final Message message) {
         view.getSelectionModel().select(false, message);
         showBodyOf(message);
         showExpiryOf(message);
         markSeen(message);
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
                         handleNewMessages();
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
 
     private void handleNewMessages() {
         if (!hasActiveSvcCall) {
             loadNewMessages();
         }
     }
 
     private void loadAllMessages() {
         services.getAllMessages(new AsyncCallback<MessageList>() {
             @Override
             public void onFailure(final Throwable exn) {
                 // TODO handle failure
                 Window.alert(exn.getMessage());
                 noteStopSvcCall();
             }
 
             @Override
             public void onSuccess(final MessageList messages) {
                 noteStopSvcCall();
                 replaceMessages(messages);
             }
         });
         noteStartSvcCall(false);
     }
 
     private void loadNewMessages() {
         services.getNewMessages(new AsyncCallback<MessageList>() {
             @Override
             public void onFailure(final Throwable exn) {
                 // TODO handle failure
                 Window.alert(exn.getMessage());
                 noteStopSvcCall();
             }
             @Override
             public void onSuccess(final MessageList messages) {
                 noteStopSvcCall();
                 addMessages(messages);
             }
         });
         noteStartSvcCall(false);
     }
 
     private void markSeen(final Message message) {
         final IdList idsDTO = MessageFactory.INSTANCE.makeIdList().as();
         idsDTO.setIds(Arrays.asList(message.getId()));
         services.acknowledgeMessages(idsDTO, new AsyncCallback<Void>() {
             @Override
             public void onFailure(Throwable caught) {
                 // TODO Figure out how to handle this
                 Window.alert(caught.getMessage());
                 noteStopSvcCall();
             }
             @Override
             public void onSuccess(Void unused) {
                 noteStopSvcCall();
                 markLocalSeen(message);
             }
         });
         noteStartSvcCall(false);
     }
 
     private void dismissMessage(final Message message) {
         if (!message.isDismissible()) {
             return;
         }
 
         final IdList idsDTO = MessageFactory.INSTANCE.makeIdList().as();
         idsDTO.setIds(Arrays.asList(message.getId()));
         services.hideMessages(idsDTO, new AsyncCallback<Void>() {
             @Override
             public void onFailure(final Throwable caught) {
                 // TODO handle failure
                 Window.alert(caught.getMessage());
                 noteStopSvcCall();
             }
 
             @Override
             public void onSuccess(final Void unused) {
                 removeMessage(message);
                 noteStopSvcCall();
             }
         });
         noteStartSvcCall(true);
     }
 
     private void noteStartSvcCall(final boolean mask) {
         if (mask) {
             // TODO externalize message
             view.mask("dismissing message");
         }
         hasActiveSvcCall = true;
     }
 
     private void noteStopSvcCall() {
         hasActiveSvcCall = false;
         view.unmask();
     }
 
     private void addMessages(final MessageList messages) {
         final Message curSelect = view.getSelectionModel().getSelectedItem();
         final ListStore<Message> store = view.getMessageStore();
         for (Message msg : messages.getList()) {
             if (store.findModel(msg) == null) {
                 store.add(msg);
             } else {
                 store.update(msg);
             }
         }
         store.applySort(false);
         updateView(curSelect);
     }
 
     private void replaceMessages(final MessageList messages) {
         final Message curSelect = view.getSelectionModel().getSelectedItem();
         view.getMessageStore().replaceAll(messages.getList());
         updateView(curSelect);
     }
 
     private void updateView(final Message curSelect) {
         final ListStore<Message> store = view.getMessageStore();
         if (store.size() <= 0) {
             view.showNoMessages();
         } else {
             final Message newSelect = curSelect == null ? null : store.findModel(curSelect);
             if (newSelect == null) {
                 showMessageSelected(0);
             } else {
                 showMessageSelected(store.indexOf(newSelect));
             }
         }
     }
 
     private void markLocalSeen(final Message message) {
         message.setSeen(true);
         view.getMessageStore().update(message);
     }
 
     private void removeMessage(final Message message) {
         final int idx = view.getMessageStore().indexOf(message);
         view.getMessageStore().remove(message);
         if (view.getMessageStore().size() <= 0) {
             view.showNoMessages();
         } else {
             showMessageSelected(view.getMessageStore().size() <= idx ? idx - 1 : idx);
 		}
 	}
 	
 	private void showMessageSelected(final int index) {
         view.showMessages();
         view.getSelectionModel().select(index, false);
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
 
 }
