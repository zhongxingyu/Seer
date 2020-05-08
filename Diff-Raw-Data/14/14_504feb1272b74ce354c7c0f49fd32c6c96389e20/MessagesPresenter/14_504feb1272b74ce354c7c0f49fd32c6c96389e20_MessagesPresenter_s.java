 package org.iplantc.de.client.sysmsgs.presenter;
 
 import java.util.Date;
 import java.util.List;
 
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.sysmsgs.cache.SystemMessageCache;
 import org.iplantc.de.client.sysmsgs.events.DismissMessageEvent;
 import org.iplantc.de.client.sysmsgs.events.MessagesUpdatedEvent;
 import org.iplantc.de.client.sysmsgs.model.Message;
 import org.iplantc.de.client.sysmsgs.view.MessageSummaryCell;
 import org.iplantc.de.client.sysmsgs.view.MessagesView;
 import org.iplantc.de.client.sysmsgs.view.SummaryListAppearance;
 
 import com.google.gwt.core.client.Callback;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.sencha.gxt.core.client.IdentityValueProvider;
 import com.sencha.gxt.core.client.Style;
 import com.sencha.gxt.core.client.ValueProvider;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.data.shared.PropertyAccess;
 import com.sencha.gxt.data.shared.SortDir;
 import com.sencha.gxt.data.shared.Store.StoreSortInfo;
 import com.sencha.gxt.data.shared.loader.ListLoadResult;
 import com.sencha.gxt.widget.core.client.ListView;
 import com.sencha.gxt.widget.core.client.ListViewSelectionModel;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
 
 /**
  * The system messages presenter.
  */
 public final class MessagesPresenter {
 
     interface MessageProperties extends PropertyAccess<Message> {
         ModelKeyProvider<Message> id();
 
         ValueProvider<Message, Date> activationTime();
 
         ValueProvider<Message, Boolean> dismissible();
     }
 
     private static final MessageProperties MSG_PROPS = GWT.create(MessageProperties.class);
 
     private final MessagesView<Message> view;
     private final ListStore<Message> messageStore;
     private final ListViewSelectionModel<Message> messageSelectionModel;
 	
     /**
      * the constructor
      */
     public MessagesPresenter() {
         view = GWT.create(MessagesView.class);
         messageStore = new ListStore<Message>(MSG_PROPS.id());
         messageSelectionModel = new SelectionModel();
 		initStore();
 		initSelectionModel();
         initMessagesView();
 	}
 	
 	private void initStore() {
         final StoreSortInfo<Message> sortInfo = new StoreSortInfo<Message>(MSG_PROPS.activationTime(), SortDir.DESC);
         messageStore.addSortInfo(sortInfo);
 		updateStoreAsync();
 		EventBus.getInstance().addHandler(MessagesUpdatedEvent.TYPE, 
 				new MessagesUpdatedEvent.Handler() {
 					@Override
 					public void onUpdate(final MessagesUpdatedEvent event) {
 						updateStoreAsync();
 					}});
 		}
 	
 	private void initSelectionModel() {
         messageSelectionModel.setSelectionMode(Style.SelectionMode.SINGLE);
         messageSelectionModel.addSelectionChangedHandler(
 				new SelectionChangedHandler<Message>() {
 					@Override
 					public void onSelectionChanged(final SelectionChangedEvent<Message> event) 
 							{
 						final List<Message> selection = event.getSelection();
 						if (!selection.isEmpty()) {
 							selectMessage(selection.get(0));
 						}}});
 	}
 		
     private void initMessagesView() {
         final IdentityValueProvider<Message> msgProv = new IdentityValueProvider<Message>();
        final SummaryListAppearance sumAppearance = new SummaryListAppearance();
         final ListView<Message, Message> sumView = new ListView<Message, Message>(messageStore, msgProv, sumAppearance);
         final MessageSummaryCell sumCell = new MessageSummaryCell();
         sumCell.addHandler(new DismissMessageEvent.Handler() {
             @Override
             public void handleDismiss(final DismissMessageEvent event) {
                 handleDismissMessageEvent(event.getMessageId());
             }
         }, DismissMessageEvent.TYPE);
         sumView.setCell(sumCell);
         sumView.setSelectionModel(messageSelectionModel);
         view.init(sumView);
     }
 
     private void handleDismissMessageEvent(final String messageId) {
 		// TODO mask view
         final Message msg = messageStore.findModelWithKey(messageId);
         SystemMessageCache.instance().dismissMessage(msg, new Callback<Void, Throwable>() {
 			@Override
 			public void onFailure(final Throwable reason) {
 				// FIXME handle failure
 				Window.alert(reason.getMessage());
 			}
 			@Override
 			public void onSuccess(Void unused) {
                 removeMessage(msg);
 				// TODO unmask  view
 			}});
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
             SystemMessageCache.instance().startSyncing();
             container.setWidget(view);
             view.showLoading();
         }
 	}
 	
     /**
      * This should be called when the container holding the view has been closed. It stops the
      * message caching.
      */
     public void stop() {
         SystemMessageCache.instance().stopSyncing();
     }
 
 	private void selectMessage(final Message msg) {
         messageSelectionModel.select(false, msg);
 		final SafeHtmlBuilder bodyBuilder = new SafeHtmlBuilder();
 		bodyBuilder.appendHtmlConstant(msg.getBody());
         view.setMessageBody(bodyBuilder.toSafeHtml());
         final DateTimeFormat expiryFmt = DateTimeFormat.getFormat("dd MMMM yyyy");
         final String expiryStr = expiryFmt.format(msg.getDeactivationTime());
         view.setExpiryMessage(I18N.DISPLAY.expirationMessage(expiryStr));
 	}
 
 	private void updateStoreAsync() {
 		SystemMessageCache.instance().load(null, 
 				new Callback<ListLoadResult<Message>, Throwable>() {
 					@Override
 					public void onFailure(final Throwable reason) {
 						// TODO implement
 						Window.alert("Failed to retrieve messages");
 					}
 					@Override
 					public void onSuccess(final ListLoadResult<Message> result) {
 						updateStore(result.getData());
 					}});
 	}
 	
 	private void updateStore(final List<Message> updatedMessages) {
         final Message curSelect = messageSelectionModel.getSelectedItem();
         messageStore.replaceAll(updatedMessages);
         if (curSelect != null && messageStore.findModel(curSelect) == null) {
             messageStore.add(curSelect);
 		}
         if (messageStore.size() > 0) {
 			if (curSelect != null) {
                 showMessageSelected(messageStore.indexOf(curSelect));
 			} else {
 				showMessageSelected(0);
 			}
 		} else {
             view.showNoMessages();
 		}
 	}
 
 	private void removeMessage(final Message message) {
         messageStore.remove(message);
         if (messageStore.size() <= 0) {
             view.showNoMessages();
 		}
 	}
 	
 	private void showMessageSelected(final int index) {
         view.showMessages();
         messageSelectionModel.deselectAll();
         messageSelectionModel.select(index, false);
 	}
 
 }
