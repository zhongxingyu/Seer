 package com.github.wolfie.popupextension;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import com.github.wolfie.popupextension.client.PopupExtensionServerRpc;
 import com.github.wolfie.popupextension.client.PopupExtensionState;
 import com.vaadin.server.AbstractClientConnector;
 import com.vaadin.server.AbstractExtension;
 import com.vaadin.ui.AbstractOrderedLayout;
 import com.vaadin.ui.AbstractSingleComponentContainer;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.UI;
 
 public class PopupExtension extends AbstractExtension {
 
 	public interface PopupVisibilityListener {
 		void visibilityChanged(boolean isOpened);
 	}
 
 	public interface PopupExtensionManualBundle {
 		PopupExtension getPopupExtension();
 
 		PopupExtensionDataTransferComponent getDataTransferComponent();
 	}
 
 	private static final long serialVersionUID = -5944700694390672500L;
 	private static final Alignment DEFAULLT_ANCHOR = Alignment.MIDDLE_CENTER;
 	private static final Alignment DEFAULT_DIRECTION = Alignment.MIDDLE_CENTER;
 
 	private Alignment anchor;
 	private Alignment direction;
 	private Component content;
 	private PopupExtensionDataTransferComponent dataTransferComponent;
 	private final List<PopupVisibilityListener> listeners = new ArrayList<PopupExtension.PopupVisibilityListener>();
 	private boolean dataTranserComponentIsMagicallyAdded;
 
 	private PopupExtension() {
 		setAnchor(DEFAULLT_ANCHOR);
 		setDirection(DEFAULT_DIRECTION);
 		getState().id = UUID.randomUUID().toString();
 
 		registerRpc(new PopupExtensionServerRpc() {
 			private static final long serialVersionUID = 741086893127824221L;
 
 			@Override
 			public void setOpen(final boolean open) {
				if (open != getState().open && getState().closeOnOutsideMouseClick) {
 					getState().open = open;
 					fireVisibilityListeners();
 				}
 			}
 		});
 	}
 
 	/**
 	 * <p>
 	 * Attach and align the PopupExtension relative to the given component
 	 * </p>
 	 * 
 	 * <p>
 	 * <i>Note:</i> Due to a workaround for Vaadin, PopupExtension needs to place
 	 * a data transfer component in your {@link UI UI's} content. Therefore, that
 	 * content needs to be an instance of {@link ComponentContainer}. If that's
 	 * not suitable, you can use {@link #extendWithManualBundle(Component)} to
 	 * manually place the data transfer component.
 	 * </p>
 	 * 
 	 * @param c
 	 * @return
 	 * @see #extendWithManualBundle(Component)
 	 */
 	public static PopupExtension extend(final Component c) {
 		final PopupExtension popup = new PopupExtension();
 		popup.dataTranserComponentIsMagicallyAdded = true;
 		popup.extend((AbstractClientConnector) c);
 
 		final Component content = UI.getCurrent().getContent();
 		if (!(content instanceof ComponentContainer)) {
 			throw new UnsupportedOperationException(
 					"UI.getCurrent().getContent() doesn't "
 							+ "return a ComponentContainer (Currently: "
 							+ content.getClass().getSimpleName()
 							+ "). PopupExtension requires a ComponentContainer "
 							+ "as the UI's content to work properly.");
 		} else {
 			final ComponentContainer ccContent = (ComponentContainer) content;
 			popup.dataTransferComponent = new PopupExtensionDataTransferComponent(
 					popup.getState().id);
 			ccContent.addComponent(popup.dataTransferComponent);
 
 			if (ccContent instanceof AbstractOrderedLayout) {
 				final AbstractOrderedLayout aol = (AbstractOrderedLayout) ccContent;
 				aol.setExpandRatio(popup.dataTransferComponent, 0.0f);
 			}
 		}
 
 		return popup;
 	}
 
 	/**
 	 * <p>
 	 * Attach and align the PopupExtension relative to the given component
 	 * </p>
 	 * 
 	 * <p>
 	 * Add the returned {@link PopupExtensionDataTransferComponent} somewhere in
 	 * your component hierarchy in such a way that it's present at all times
 	 * whenever you need the {@link PopupExtension}.
 	 * </p>
 	 * 
 	 * @param c
 	 * @return an object containing the paired {@link PopupExtension} and
 	 *         {@link PopupExtensionDataTransferComponent}.
 	 */
 	public static PopupExtensionManualBundle extendWithManualBundle(
 			final Component c) {
 		final PopupExtension popup = new PopupExtension();
 		popup.extend((AbstractClientConnector) c);
 		final PopupExtensionDataTransferComponent dataTransferComponent = new PopupExtensionDataTransferComponent(
 				popup.getState().id);
 		popup.dataTransferComponent = dataTransferComponent;
 
 		return new PopupExtensionManualBundle() {
 			@Override
 			public PopupExtension getPopupExtension() {
 				return popup;
 			}
 
 			@Override
 			public PopupExtensionDataTransferComponent getDataTransferComponent() {
 				return dataTransferComponent;
 			}
 		};
 	}
 
 	@Override
 	public void detach() {
 		if (dataTranserComponentIsMagicallyAdded) {
 			AbstractSingleComponentContainer.removeFromParent(dataTransferComponent);
 		}
 		super.detach();
 	}
 
 	public void toggle() {
 		getState().open = !getState().open;
 		fireVisibilityListeners();
 	}
 
 	public void open() {
 		getState().open = true;
 		fireVisibilityListeners();
 	}
 
 	public void close() {
 		getState().open = false;
 		fireVisibilityListeners();
 	}
 
 	public void closeOnOutsideMouseClick(final boolean enable) {
 		getState().closeOnOutsideMouseClick = enable;
 	}
 
 	/**
 	 * @return <code>true</code> iff this instance has been configured to close on
 	 *         outside mouse click.
 	 * @see PopupExtension#closeOnOutsideMouseClick(boolean)
 	 */
 	public boolean closeOnOutsideMouseClick() {
 		return getState().closeOnOutsideMouseClick;
 	}
 
 	/**
 	 * Set the content of the popup.
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if <code>content</code> is <code>null</code>.
 	 */
 	public void setContent(final Component content)
 			throws IllegalArgumentException {
 		if (content == null) {
 			throw new IllegalArgumentException(
 					"Null not a valid argument. Use removeContent() instead");
 		}
 
 		if (this.content != content) {
 			this.content = content;
 			dataTransferComponent.setContent(content);
 		}
 	}
 
 	/**
 	 * @throws IllegalArgumentException
 	 *             if <code>anchor</code> is <code>null</code>.
 	 * 
 	 */
 	public void setAnchor(final Alignment anchor)
 			throws IllegalArgumentException {
 		if (anchor == null) {
 			throw new IllegalArgumentException("null not a valid anchor");
 		}
 
 		if (this.anchor != anchor) {
 			this.anchor = anchor;
 			getState().anchor = anchor.getBitMask();
 		}
 	}
 
 	public Alignment getAnchor() {
 		return anchor;
 	}
 
 	/**
 	 * @throws IllegalArgumentException
 	 *             if <code>direction</code> is <code>null</code>.
 	 */
 	public void setDirection(final Alignment direction) {
 		if (direction == null) {
 			throw new IllegalArgumentException("null not a valid direction");
 		}
 
 		if (this.direction != direction) {
 			this.direction = direction;
 			getState().direction = direction.getBitMask();
 		}
 	}
 
 	public Alignment getDirection() {
 		return direction;
 	}
 
 	public Component getContent() {
 		return content;
 	}
 
 	public boolean isOpen() {
 		return getState().open;
 	}
 
 	public void setOffset(final int x, final int y) {
 		getState().xOffset = x;
 		getState().yOffset = y;
 	}
 
 	@Override
 	protected PopupExtensionState getState() {
 		return (PopupExtensionState) super.getState();
 	}
 
 	/**
 	 * @param listener
 	 *          a non-<code>null</code> listener.
 	 * @throws IllegalArgumentException
 	 *           if <code>listener</code> is <code>null</code>.
 	 */
 	public void addPopupVisibilityListener(
 			final PopupVisibilityListener listener) {
 		if (listener != null) {
 			listeners.add(listener);
 		} else {
 			throw new IllegalArgumentException("Listener can't be null");
 		}
 	}
 
 	/**
 	 * @return <code>true</code> iff the given <code>listener</code> was found and
 	 *         removed from this.
 	 */
 	public boolean removePopupVisibilityListener(
 			final PopupVisibilityListener listener) {
 		return listeners.remove(listener);
 	}
 
 	private void fireVisibilityListeners() {
 		for (final PopupVisibilityListener listener : listeners) {
 			listener.visibilityChanged(getState().open);
 		}
 	}
 
 	/**
 	 * Set the CSS style name for the popup. A <code>null</code> argument, or an
 	 * empty string, or a string that is empty when trimmed, will remove the
 	 * style name.
 	 */
 	public void setPopupStyleName(final String popupStyleName) {
 		if (!(popupStyleName != null && popupStyleName.trim().isEmpty())) {
 			getState().popupStyleName = popupStyleName;
 		} else {
 			getState().popupStyleName = null;
 		}
 	}
 
 	/** Get the current CSS style name for the popup. */
 	public String getPopupStyleName() {
 		return getState().popupStyleName;
 	}
 }
