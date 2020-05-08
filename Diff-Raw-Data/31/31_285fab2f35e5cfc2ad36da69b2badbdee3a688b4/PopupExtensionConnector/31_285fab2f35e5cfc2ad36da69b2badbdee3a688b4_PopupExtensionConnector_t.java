 package com.github.wolfie.popupextension.client;
 
 import com.github.wolfie.popupextension.PopupExtension;
 import com.github.wolfie.popupextension.client.PopupExtensionWidget.VisibilityChangeListener;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.client.ComponentConnector;
 import com.vaadin.client.ServerConnector;
 import com.vaadin.client.communication.StateChangeEvent;
 import com.vaadin.client.extensions.AbstractExtensionConnector;
 import com.vaadin.shared.ui.AlignmentInfo;
 import com.vaadin.shared.ui.Connect;
 
 @Connect(PopupExtension.class)
 public class PopupExtensionConnector extends AbstractExtensionConnector
 implements VisibilityChangeListener {
 	private static final long serialVersionUID = -6833923031674458681L;
 
 	private Widget extendedWidget;
 	private final PopupExtensionWidget popup = new PopupExtensionWidget();
 
 	private PopupExtensionServerRpc rpc;
 
 	@Override
 	protected void extend(final ServerConnector target) {
 		extendedWidget = ((ComponentConnector) target).getWidget();
 		popup.setOwner(extendedWidget);
 	}
 
 	@Override
 	protected void init() {
 		super.init();
 		rpc = getRpcProxy(PopupExtensionServerRpc.class);
 		popup.addVisibilityChangeListener(this);
 	}
 
 	@Override
 	public void onUnregister() {
 		super.onUnregister();
 		PopupExtensionWidget.INSTANCES.remove(getState().id);
 	}
 
 	@Override
 	public void onStateChanged(final StateChangeEvent e) {
 		if (e.hasPropertyChanged("id")) {
 			PopupExtensionWidget.INSTANCES.put(getState().id, popup);
 
 			/*
 			 * This ensures that if the PopupExtension is persistent, and is
 			 * removed and reattached over time, the popup contents are
 			 * re-injected every time.
 			 */
 			final PopupExtensionDataTransferComponentConnector data = PopupExtensionDataTransferComponentConnector.INSTANCES
 					.get(getState().id);
 			if (data != null) {
 				data.reinject(popup);
 			}
 		}
 
 		if (e.hasPropertyChanged("anchor")) {
 			popup.setAnchor(new AlignmentInfo(getState().anchor));
 		}
 
 		if (e.hasPropertyChanged("direction")) {
 			popup.setDirection(new AlignmentInfo(getState().direction));
 		}
 
 		if (e.hasPropertyChanged("open")) {
			popup.setOpen(getState().open, true);
 		}
 
 		if (e.hasPropertyChanged("closeOnOutsideMouseClick")) {
 			popup.closeOnOutsideMouseClick(getState().closeOnOutsideMouseClick);
 		}
 
 		if (e.hasPropertyChanged("popupStyleName")) {
 			popup.setPopupStyleName(getState().popupStyleName);
 		}
 
 		popup.setOffset(getState().xOffset, getState().yOffset);
 	}
 
 	@Override
 	public PopupExtensionState getState() {
 		return (PopupExtensionState) super.getState();
 	}
 
 	@Override
 	public void becameVisible(final boolean visible,
                              final PopupExtensionWidget widget,
                              boolean serverSideChange) {
        if (!serverSideChange) {
            rpc.setOpen(visible);
        }
 	}
 }
