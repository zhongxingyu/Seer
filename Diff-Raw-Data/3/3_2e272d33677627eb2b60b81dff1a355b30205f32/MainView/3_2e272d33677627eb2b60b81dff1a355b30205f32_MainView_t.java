 package cl.votainteligente.inspector.client.views;
 
 import cl.votainteligente.inspector.client.presenters.MainPresenter;
 import cl.votainteligente.inspector.client.uihandlers.MainUiHandlers;
 
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class MainView extends ViewWithUiHandlers<MainUiHandlers> implements MainPresenter.MyView {
 	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);
 	interface MainViewUiBinder extends UiBinder<Widget, MainView> {}
 	private final Widget widget;
 
 	@UiField FlowPanel mainPanel;
 	private PopupPanel popup;
 
 	public MainView() {
 		widget = uiBinder.createAndBindUi(this);
 		popup = new PopupPanel();
 		popup.setGlassEnabled(true);
 		popup.setModal(true);
 		popup.setAutoHideOnHistoryEventsEnabled(true);
 
 		popup.addCloseHandler(new CloseHandler<PopupPanel>() {
 			@Override
 			public void onClose(CloseEvent<PopupPanel> event) {
 				getUiHandlers().clearPopupSlot();
 			}
 		});
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
 }
