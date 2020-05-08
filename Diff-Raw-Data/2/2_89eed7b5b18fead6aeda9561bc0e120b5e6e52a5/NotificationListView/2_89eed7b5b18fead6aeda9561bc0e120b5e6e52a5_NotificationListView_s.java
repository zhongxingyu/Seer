 /**
  *
  */
 package org.iplantc.de.client.notifications.views;
 
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.widgets.IPlantAnchor;
 import org.iplantc.de.client.DeResources;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.NotificationCountUpdateEvent;
 import org.iplantc.de.client.events.WindowShowRequestEvent;
 import org.iplantc.de.client.notifications.events.DeleteNotificationsUpdateEvent;
 import org.iplantc.de.client.notifications.events.DeleteNotificationsUpdateEventHandler;
 import org.iplantc.de.client.notifications.models.Notification;
 import org.iplantc.de.client.notifications.models.NotificationAutoBeanFactory;
 import org.iplantc.de.client.notifications.models.NotificationMessage;
 import org.iplantc.de.client.notifications.models.NotificationMessageProperties;
 import org.iplantc.de.client.notifications.models.payload.PayloadAnalysis;
 import org.iplantc.de.client.notifications.services.MessageServiceFacade;
 import org.iplantc.de.client.notifications.services.NotificationCallback;
 import org.iplantc.de.client.notifications.util.NotificationHelper;
 import org.iplantc.de.client.notifications.util.NotificationHelper.Category;
 import org.iplantc.de.client.utils.NotifyInfo;
 import org.iplantc.de.client.views.windows.configs.ConfigFactory;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.sencha.gxt.cell.core.client.SimpleSafeHtmlCell;
 import com.sencha.gxt.core.client.IdentityValueProvider;
 import com.sencha.gxt.core.client.XTemplates;
 import com.sencha.gxt.core.client.resources.CommonStyles;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.data.shared.SortDir;
 import com.sencha.gxt.data.shared.Store.StoreSortInfo;
 import com.sencha.gxt.widget.core.client.ListView;
 import com.sencha.gxt.widget.core.client.ListViewCustomAppearance;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
 
 /**
  * New notifications as list
  *
  * @author sriram
  *
  */
 public class NotificationListView implements IsWidget {
 
 	private ListView<NotificationMessage, NotificationMessage> view;
 	private ListStore<NotificationMessage> store;
 	private int total_unseen;
 	private final HorizontalPanel hyperlinkPanel;
 
 	public static final int NEW_NOTIFICATIONS_LIMIT = 10;
 
 	final Resources resources = GWT.create(Resources.class);
 	final DeResources deRes = GWT.create(DeResources.class);
 	final Style style = resources.css();
 	final Renderer r = GWT.create(Renderer.class);
 	private final NotificationAutoBeanFactory notificationFactory = GWT
 			.create(NotificationAutoBeanFactory.class);
 	private final EventBus eventBus;
 
 	interface Renderer extends XTemplates {
 		@XTemplate("<div class=\"{style.thumb}\"> {msg.message}</div>")
 		public SafeHtml renderItem(NotificationMessage msg, Style style);
 
 		@XTemplate("<div class=\"{style.thumb_highlight}\"> {msg.message}</div>")
 		public SafeHtml renderItemWithHighlight(NotificationMessage msg,
 				Style style);
 
 	}
 
 	interface Style extends CssResource {
 		String over();
 
 		String select();
 
 		String thumb();
 
 		String thumbWrap();
 
 		String thumb_highlight();
 	}
 
 	interface Resources extends ClientBundle {
 		@Source("NotificationListView.css")
 		Style css();
 	}
 
 	ModelKeyProvider<NotificationMessage> kp = new ModelKeyProvider<NotificationMessage>() {
 		@Override
 		public String getKey(NotificationMessage item) {
 			return item.getTimestamp() + "";
 		}
 	};
 
 	ListViewCustomAppearance<NotificationMessage> appearance = new ListViewCustomAppearance<NotificationMessage>(
 			"." + style.thumbWrap(), style.over(), style.select()) {
 
 		@Override
 		public void renderEnd(SafeHtmlBuilder builder) {
 			String markup = new StringBuilder("<div class=\"")
 					.append(CommonStyles.get().clear()).append("\"></div>")
 					.toString();
 			builder.appendHtmlConstant(markup);
 		}
 
 		@Override
 		public void renderItem(SafeHtmlBuilder builder, SafeHtml content) {
 			builder.appendHtmlConstant("<div class='" + style.thumbWrap()
					+ "' style='border: 1px solid white'>");
 			builder.append(content);
 			builder.appendHtmlConstant("</div>");
 		}
 
 	};
 	private HorizontalPanel emptyTextPnl;
 
 	public NotificationListView(EventBus eventBus) {
 		this.eventBus = eventBus;
 		resources.css().ensureInjected();
 		deRes.css().ensureInjected();
 		initListeners();
 		hyperlinkPanel = new HorizontalPanel();
 		hyperlinkPanel.setSpacing(2);
 		updateNotificationLink();
 	}
 
 	private void initListeners() {
 		EventBus.getInstance().addHandler(DeleteNotificationsUpdateEvent.TYPE,
 				new DeleteNotificationsUpdateEventHandler() {
 
 					@Override
 					public void onDelete(DeleteNotificationsUpdateEvent event) {
 						if (event.getMessages() != null) {
 							for (NotificationMessage deleted : event
 									.getMessages()) {
 								NotificationMessage nm = store
 										.findModel(deleted);
 								if (nm != null) {
 									store.remove(nm);
 								}
 							}
 						} else {
 							store.clear();
 						}
 
 						if (store.getAll().size() == 0) {
 							emptyTextPnl.setVisible(true);
 						}
 					}
 				});
 	}
 
 	public void highlightNewNotifications() {
 		// List<NotificationMessage> new_notifications = store.getAll();
 		// TODO: implement higlight
 	}
 
 	//
 	public void markAsSeen() {
 		java.util.List<NotificationMessage> new_notifications = store.getAll();
 		JSONArray arr = new JSONArray();
 		int i = 0;
 		for (NotificationMessage n : new_notifications) {
 			if (!n.isSeen()) {
 				arr.set(i++, new JSONString(n.getId().toString()));
 				n.setSeen(true);
 			}
 		}
 
 		if (arr.size() > 0) {
 			JSONObject obj = new JSONObject();
 			obj.put("uuids", arr);
 
 			org.iplantc.de.client.notifications.services.MessageServiceFacade facade = new org.iplantc.de.client.notifications.services.MessageServiceFacade();
 			facade.markAsSeen(obj, new AsyncCallback<String>() {
 
 				@Override
 				public void onSuccess(String result) {
 					JSONObject obj = JsonUtil.getObject(result);
 					int new_count = Integer.parseInt(JsonUtil.getString(obj,
 							"count"));
 					NotificationCountUpdateEvent event = new NotificationCountUpdateEvent(
 							new_count);
 					EventBus.getInstance().fireEvent(event);
 					view.refresh();
 				}
 
 				@Override
 				public void onFailure(Throwable caught) {
 					org.iplantc.core.uicommons.client.ErrorHandler.post(caught);
 				}
 			});
 		}
 
 	}
 
 	public void fetchUnseenNotifications() {
 		MessageServiceFacade facadeMessageService = new MessageServiceFacade();
 		facadeMessageService.getRecentMessages(new NotificationCallback() {
 
 			@Override
 			public void onSuccess(String result) {
 				super.onSuccess(result);
 				processMessages(this.getNotifications());
 			}
 		});
 	}
 
 	/**
 	 * Process notifications
 	 *
 	 * @param json
 	 *            string to be processed.
 	 */
 	// public void processMessages(final String json) {
 	public void processMessages(final List<Notification> notifications) {
 		// cache before removing
 		List<NotificationMessage> temp = store.getAll();
 		store.clear();
 		store.clearSortInfo();
 		boolean displayInfo = false;
 
 		for (Notification n : notifications) {
 			NotificationMessage nm = n.getMessage();
 			nm.setSeen(n.isSeen());
 			if (nm != null && !isExist(temp, nm)) {
 				store.add(nm);
 				displayNotificationPopup(nm);
 				displayInfo = true;
 			}
 		}
 		if (total_unseen > NEW_NOTIFICATIONS_LIMIT && displayInfo) {
 			NotifyInfo.display(I18N.DISPLAY.newNotificationsAlert());
 		}
 
 		if (store.getAll().size() > 0) {
 			emptyTextPnl.setVisible(false);
 		} else {
 			emptyTextPnl.setVisible(true);
 		}
 
 		NotificationMessageProperties props = GWT
 				.create(NotificationMessageProperties.class);
 		store.addSortInfo(new StoreSortInfo<NotificationMessage>(props
 				.timestamp(), SortDir.DESC));
 		highlightNewNotifications();
 	}
 
 	private void displayNotificationPopup(NotificationMessage n) {
 		if (!n.isSeen()) {
 			if (n.getCategory().equals(Category.DATA)) {
 				NotifyInfo.display(n.getMessage());
 			} else if (n.getCategory().equals(Category.ANALYSIS)) {
 				PayloadAnalysis analysisPayload = AutoBeanCodex.decode(
 						notificationFactory, PayloadAnalysis.class,
 						n.getContext()).as();
 
 				if ("Failed".equals(analysisPayload.getStatus())) { //$NON-NLS-1$
 					NotifyInfo.displayWarning(n.getMessage());
 				} else {
 					NotifyInfo.display(n.getMessage());
 				}
 			}
 		}
 	}
 
 	private boolean isExist(List<NotificationMessage> list,
 			NotificationMessage n) {
 		for (NotificationMessage noti : list) {
 			if (noti.getId().equals(n.getId())) {
 				return true;
 			}
 		}
 
 		return false;
 
 	}
 
 	public void setUnseenCount(int count) {
 		this.total_unseen = count;
 		updateNotificationLink();
 	}
 
 	public void updateNotificationLink() {
 		hyperlinkPanel.clear();
 		hyperlinkPanel.add(buildNotificationHyerlink());
 		if (total_unseen > 0) {
 			hyperlinkPanel.add(buildAckAllHyperlink());
 		}
 	}
 
 	private IPlantAnchor buildAckAllHyperlink() {
 		IPlantAnchor link = new IPlantAnchor(I18N.DISPLAY.markAllasSeen(), -1,
 				new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						MessageServiceFacade facade = new MessageServiceFacade();
 						facade.acknowledgeAll(new AsyncCallback<String>() {
 
 							@Override
 							public void onFailure(Throwable caught) {
 								ErrorHandler.post(caught);
 
 							}
 
 							@Override
 							public void onSuccess(String result) {
 							    IplantAnnouncer.getInstance().schedule(I18N.DISPLAY.markAllasSeenSuccess());
 							}
 						});
 
 					}
 				});
 
 		return link;
 
 	}
 
 	private IPlantAnchor buildNotificationHyerlink() {
 		String displayText;
 		if (total_unseen > 0) {
 			displayText = I18N.DISPLAY.newNotifications() + " (" + total_unseen
 					+ ")";
 		} else {
 			displayText = I18N.DISPLAY.allNotifications();
 		}
 
 		IPlantAnchor link = new IPlantAnchor(displayText, -1,
 				new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						if (total_unseen > 0) {
 							showNotificationWindow(NotificationHelper.Category.NEW);
 						} else {
 							showNotificationWindow(NotificationHelper.Category.ALL);
 						}
 
 					}
 				});
 		return link;
 	}
 
 	/** Makes the notification window visible and filters by a category */
 	private void showNotificationWindow(final Category category) {
 		eventBus.fireEvent(new WindowShowRequestEvent(ConfigFactory
 				.notifyWindowConfig(category)));
 	}
 
 	private HorizontalPanel getEmptyTextPanel() {
 		emptyTextPnl = new HorizontalPanel();
 		if (store != null && store.getAll().size() == 0) {
 			emptyTextPnl.add(new HTML("<span style='font-size:11px;'>"
 					+ I18N.DISPLAY.noNewNotifications() + "</span>"));
 		}
 		emptyTextPnl.setHeight("30px");
 		return emptyTextPnl;
 
 	}
 
 	@Override
 	public Widget asWidget() {
 		VerticalLayoutContainer container = new VerticalLayoutContainer();
 		container.setBorders(false);
 
 		store = new ListStore<NotificationMessage>(kp);
 		view = new ListView<NotificationMessage, NotificationMessage>(store,
 				new IdentityValueProvider<NotificationMessage>(), appearance);
 
 		view.getSelectionModel().addSelectionChangedHandler(
 				new SelectionChangedHandler<NotificationMessage>() {
 
 					@Override
 					public void onSelectionChanged(
 							SelectionChangedEvent<NotificationMessage> event) {
 						final NotificationMessage msg = event.getSelection()
 								.get(0);
 						if (msg != null) {
 							NotificationHelper.getInstance().view(msg);
 						}
 					}
 
 				});
 
 		view.setCell(new SimpleSafeHtmlCell<NotificationMessage>(
 				new AbstractSafeHtmlRenderer<NotificationMessage>() {
 
 					@Override
 					public SafeHtml render(NotificationMessage object) {
 						if (object.isSeen()) {
 							return r.renderItem(object, style);
 						} else {
 							return r.renderItemWithHighlight(object, style);
 						}
 
 					}
 				}));
 		view.setSize("250px", "220px");
 		view.setBorders(false);
 		container.add(getEmptyTextPanel());
 		container.add(view);
 		hyperlinkPanel.setHeight("30px");
 		container.add(hyperlinkPanel);
 		return container;
 	}
 }
