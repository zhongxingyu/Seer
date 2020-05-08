 package cl.votainteligente.inspector.client.views;
 
 import cl.votainteligente.inspector.client.presenters.MainPresenter;
 import cl.votainteligente.inspector.client.uihandlers.MainUiHandlers;
 import cl.votainteligente.inspector.shared.NotificationEventParams;
 import cl.votainteligente.inspector.shared.NotificationEventType;
 
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.*;
 
 public class MainView extends ViewWithUiHandlers<MainUiHandlers> implements MainPresenter.MyView {
 	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);
 	interface MainViewUiBinder extends UiBinder<Widget, MainView> {}
 	private final Widget widget;
 
 	@UiField FlowPanel mainPanel;
 	@UiField FlowPanel notificationPanel;
 	private PopupPanel popup;
 	private PopupPanel loading;
 
 	public MainView() {
 		widget = uiBinder.createAndBindUi(this);
 		popup = new PopupPanel();
 		popup.setGlassEnabled(true);
 		popup.setModal(false);
 		popup.setAutoHideOnHistoryEventsEnabled(true);
 
 		popup.addCloseHandler(new CloseHandler<PopupPanel>() {
 			@Override
 			public void onClose(CloseEvent<PopupPanel> event) {
 				getUiHandlers().clearPopupSlot();
 			}
 		});
 
 		loading = new PopupPanel();
 		loading.setGlassEnabled(true);
		loading.setModal(false);
 		loading.setAutoHideOnHistoryEventsEnabled(true);
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 
 	@Override
 	public void setInSlot(Object slot, Widget content) {
 		if (MainPresenter.SLOT_MAIN_CONTENT.equals(slot)) {
 			popup.hide();
 			mainPanel.clear();
 
 			if (content != null) {
 				mainPanel.add(content);
 			}
 		} else if (MainPresenter.SLOT_POPUP_CONTENT.equals(slot)) {
 			popup.clear();
 
 			if (content != null) {
 				popup.add(content);
 				popup.center();
 			}
 		} else {
 			super.setInSlot(slot, content);
 		}
 	}
 
 	@Override
 	public void setNotificationMessage(final NotificationEventParams params) {
 		final FlowPanel notification = new FlowPanel();
 		Label notificationClose = new Label();
 		Label notificationLabel = new Label(params.getMessage());
 		notification.setStyleName(params.getType().getType());
 		notificationClose.addStyleName("closeNotice");
 		notificationLabel.addStyleName("fRightNotice");
 		notification.setVisible(true);
 
 		Timer notificationTimer = new Timer() {
 			@Override
 			public void run() {
 				notification.setVisible(false);
 			}
 		};
 
 		if (params.getDuration() > 0) {
 			notificationTimer.schedule(params.getDuration());
 		}
 
 		notificationClose.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				notificationPanel.remove(notification);
 			}
 		});
 
 		notification.add(notificationClose);
 		notification.add(notificationLabel);
 
 		if (params.getType().equals(NotificationEventType.SUCCESS)) {
 			clearNotifications();
 		}
 
 		notificationPanel.insert(notification, 0);
 
 		if (notificationPanel.getWidgetCount() > 3) {
 			for (Integer index = 3; index < notificationPanel.getWidgetCount(); index++) {
 				notificationPanel.remove(index);
 			}
 		}
 	}
 
 	@Override
 	public void clearNotifications() {
 		notificationPanel.clear();
 	}
 
 	@Override
 	public void showLoading() {
		popup.clear();
		popup.add(new Image("images/loading.gif"));
		popup.center();
 	}
 
 	@Override
 	public void hideLoading() {
		popup.hide();
 	}
 }
