 package com.github.wolfie.popupextension.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Event.NativePreviewEvent;
 import com.google.gwt.user.client.Event.NativePreviewHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.client.ui.VOverlay;
 import com.vaadin.shared.ui.AlignmentInfo;
 
 public class PopupExtensionWidget extends VOverlay {
 
 	private static final String STYLE_NAME = "h-popupextension";
 
 	public interface VisibilityChangeListener {
		public void becameVisible(boolean visible, PopupExtensionWidget widget, boolean serverSideChange);
 	}
 
 	static final Map<String, PopupExtensionWidget> INSTANCES = new HashMap<String, PopupExtensionWidget>();
 
 	private AlignmentInfo anchor;
 	private AlignmentInfo direction;
 
 	private HandlerRegistration clickEventHandler;
 
 	private final List<VisibilityChangeListener> visibilityChangeListeners = new ArrayList<PopupExtensionWidget.VisibilityChangeListener>();
 
 	private final Timer delayedResizer = new Timer() {
 		@Override
 		public void run() {
 			repositionMaybe();
 		};
 	};
 
 	private int xOffset;
 
 	private int yOffset;
 
 	public PopupExtensionWidget() {
 		setPopupStyleName(null);
 		setShadowEnabled(false);
 		Window.addResizeHandler(new ResizeHandler() {
 			@Override
 			public void onResize(final ResizeEvent event) {
 				delayedResizer.schedule(50);
 			}
 		});
 	}
 
 	public void setAnchor(final AlignmentInfo anchor) {
 		if (this.anchor != anchor) {
 			this.anchor = anchor;
 			repositionMaybe();
 		}
 	}
 
 	public void setDirection(final AlignmentInfo direction) {
 		if (this.direction != direction) {
 			this.direction = direction;
 			repositionMaybe();
 		}
 	}
 
 	private void repositionMaybe() {
 		if (!isShowing() || anchor == null || direction == null) {
 			return;
 		}
 		reposition(getOffsetWidth(), getOffsetHeight());
 	}
 
	public void setOpen(final boolean open, boolean serverSideChange) {
 		if ((isShowing() && open) || (!isShowing() && !open)) {
 			return;
 		}
 
 		if (open) {
 			setPopupPositionAndShow(new PositionCallback() {
 				@Override
 				public void setPosition(final int offsetWidth,
 						final int offsetHeight) {
 					reposition(offsetWidth, offsetHeight);
 				}
 			});
 
 			repositionMaybe();
 		} else {
 			hide();
 		}
 
 		for (final VisibilityChangeListener listener : visibilityChangeListeners) {
			listener.becameVisible(open, this, serverSideChange);
 		}
 	}
 
 	public void addVisibilityChangeListener(
 			final VisibilityChangeListener listener) {
 		visibilityChangeListeners.add(listener);
 	}
 
 	private void reposition(final int offsetWidth, final int offsetHeight) {
 		final Widget w = getOwner();
 
 		int left = w.getAbsoluteLeft();
 		int top = w.getAbsoluteTop();
 		final int height = w.getOffsetHeight();
 		final int width = w.getOffsetWidth();
 
 		if (anchor.isRight()) {
 			left += width;
 		} else if (anchor.isHorizontalCenter()) {
 			left += width / 2;
 		}
 
 		if (anchor.isBottom()) {
 			top += height;
 		} else if (anchor.isVerticalCenter()) {
 			top += height / 2;
 		}
 
 		if (direction.isLeft()) {
 			left -= offsetWidth;
 		} else if (direction.isHorizontalCenter()) {
 			left -= offsetWidth / 2;
 		}
 
 		if (direction.isTop()) {
 			top -= offsetHeight;
 		} else if (direction.isVerticalCenter()) {
 			top -= offsetHeight / 2;
 		}
 
 		top += yOffset;
 		left += xOffset;
 
 		setPopupPosition(left, top);
 	}
 
 	public void closeOnOutsideMouseClick(final boolean closeOnOutsideMouseClick) {
 		if (closeOnOutsideMouseClick) {
 			if (clickEventHandler != null) {
 				return;
 			}
 
 			clickEventHandler = Event
 					.addNativePreviewHandler(new NativePreviewHandler() {
 						@Override
 						public void onPreviewNativeEvent(
 								final NativePreviewEvent event) {
 							if (event.getTypeInt() == Event.ONCLICK) {
 								final boolean thisWasClicked = DOM
 										.isOrHasChild(getElement(),
 												(Element) Element.as(event
 														.getNativeEvent()
 														.getEventTarget()));
 
 								if (!thisWasClicked) {
									setOpen(false, false);
 								}
 							}
 						}
 					});
 		} else {
 			if (clickEventHandler == null) {
 				return;
 			}
 
 			clickEventHandler.removeHandler();
 			clickEventHandler = null;
 		}
 	}
 
 	public void setOffset(final int x, final int y) {
 		boolean changed = false;
 		if (this.xOffset != x) {
 			this.xOffset = x;
 			changed = true;
 		}
 
 		if (this.yOffset != y) {
 			this.yOffset = y;
 			changed = true;
 		}
 
 		if (changed) {
 			repositionMaybe();
 		}
 	}
 
 	/**
 	 * Set the CSS style name for the popup element. <code>null</code> will
 	 * erase the stylename.
 	 */
 	public void setPopupStyleName(final String styleName) {
 		setStyleName(STYLE_NAME);
 
 		if (styleName != null) {
 			addStyleName(STYLE_NAME + "-" + styleName);
 			addStyleName(styleName);
 		}
 	}
 }
