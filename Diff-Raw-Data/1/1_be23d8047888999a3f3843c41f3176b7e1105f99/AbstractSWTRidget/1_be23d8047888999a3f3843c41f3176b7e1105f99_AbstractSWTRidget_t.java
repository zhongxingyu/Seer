 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.core.marker.OutputMarker;
 import org.eclipse.riena.ui.ridgets.AbstractRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingPropertyLocator;
 import org.eclipse.riena.ui.swt.utils.ImageUtil;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * Ridget for an SWT control.
  */
 public abstract class AbstractSWTRidget extends AbstractRidget implements IMarkableRidget {
 
 	private static Image missingImage;
 	private FocusListener focusManager = new FocusManager();
 	private Control uiControl;
 	private boolean focusable;
 	private String toolTip = null;
 	private boolean blocked;
 	private ErrorMarker errorMarker;
 	private DisabledMarker disabledMarker;
 	private MandatoryMarker mandatoryMarker;
 	private OutputMarker outputMarker;
 	private HiddenMarker hiddenMarker;
 	private MarkerSupport markerSupport;
 
 	/**
 	 * Checks that the given uiControl is assignable to the the given type.
 	 * 
 	 * @param uiControl
 	 *            a uiControl, may be null
 	 * @param type
 	 *            a class instance (non-null)
 	 * @throws BindingException
 	 *             if the uiControl is not of the given type
 	 */
 	public static void assertType(Object uiControl, Class<?> type) {
 		if ((uiControl != null) && !(type.isAssignableFrom(uiControl.getClass()))) {
 			String expectedClassName = type.getSimpleName();
 			String controlClassName = uiControl.getClass().getSimpleName();
 			throw new BindingException("uiControl of  must be a " + expectedClassName + " but was a " //$NON-NLS-1$ //$NON-NLS-2$
 					+ controlClassName);
 		}
 	}
 
 	public AbstractSWTRidget() {
 		focusable = true;
 	}
 
 	public void setUIControl(Object uiControl) {
 		checkUIControl(uiControl);
 		uninstallListeners();
 		unbindUIControl();
 		this.uiControl = (Control) uiControl;
		updateMarkers();
 		updateToolTip();
 		bindUIControl();
 		installListeners();
 	}
 
 	public Control getUIControl() {
 		return uiControl;
 	}
 
 	public String getID() {
 
 		if (getUIControl() != null) {
 			IBindingPropertyLocator locator = SWTBindingPropertyLocator.getInstance();
 			return locator.locateBindingProperty(getUIControl());
 		}
 
 		return null;
 
 	}
 
 	public final void requestFocus() {
 		if (isFocusable()) {
 			if (getUIControl() != null) {
 				Control control = getUIControl();
 				control.setFocus();
 			}
 		}
 	}
 
 	public final boolean hasFocus() {
 		if (getUIControl() != null) {
 			Control control = getUIControl();
 			return control.isFocusControl();
 		}
 		return false;
 	}
 
 	public final boolean isFocusable() {
 		return focusable;
 	}
 
 	public final void setFocusable(boolean focusable) {
 		if (this.focusable != focusable) {
 			this.focusable = focusable;
 		}
 	}
 
 	public final boolean isVisible() {
 		return (uiControl != null) && (getMarkersOfType(HiddenMarker.class).isEmpty());
 	}
 
 	public void setVisible(boolean visible) {
 
 		if (hiddenMarker == null) {
 			hiddenMarker = new HiddenMarker();
 		}
 
 		if (visible) {
 			removeMarker(hiddenMarker);
 		} else {
 			addMarker(hiddenMarker);
 		}
 
 	}
 
 	public final void setToolTipText(String toolTipText) {
 		this.toolTip = toolTipText;
 		updateToolTip();
 	}
 
 	public final String getToolTipText() {
 		return toolTip;
 	}
 
 	public final boolean isBlocked() {
 		return blocked;
 	}
 
 	public final void setBlocked(boolean blocked) {
 		this.blocked = blocked;
 	}
 
 	/**
 	 * <p>
 	 * Performs checks on the control about to be bound by this ridget.
 	 * </p>
 	 * <p>
 	 * Implementors must make sure the given <tt>uiControl</tt> has the expected
 	 * type.
 	 * </p>
 	 * 
 	 * @param uiControl
 	 *            a {@link Widget} instance or null
 	 * @throws BindingException
 	 *             if the <tt>uiControl</tt> fails the check
 	 */
 	abstract protected void checkUIControl(Object uiControl);
 
 	/**
 	 * <p>
 	 * Bind the current <tt>uiControl</tt> to the ridget.
 	 * </p>
 	 * <p>
 	 * Implementors must call {@link #getUIControl()} to obtain the current
 	 * control. If the control is non-null they must do whatever necessary to
 	 * bind it to the ridget.
 	 * </p>
 	 */
 	abstract protected void bindUIControl();
 
 	/**
 	 * <p>
 	 * Unbind the current <tt>uiControl</tt> from the ridget.
 	 * </p>
 	 * <p>
 	 * Implementors ensure they dispose the control-to-ridget binding and
 	 * dispose any data structures that are not necessary in an unbound state.
 	 * </p>
 	 */
 	abstract protected void unbindUIControl();
 
 	/**
 	 * Adds listeners to the <tt>uiControl</tt> after it was bound to the
 	 * ridget.
 	 */
 	protected void installListeners() {
 		if (uiControl != null) {
 			uiControl.addFocusListener(focusManager);
 		}
 	}
 
 	/**
 	 * Removes listeners from the <tt>uiControl</tt> when it is about to be
 	 * unbound from the ridget.
 	 */
 	protected void uninstallListeners() {
 		if (uiControl != null) {
 			uiControl.removeFocusListener(focusManager);
 		}
 	}
 
 	// helping methods
 	// ////////////////
 
 	private void updateToolTip() {
 		if (uiControl != null) {
 			uiControl.setToolTipText(toolTip);
 		}
 	}
 
 	protected Image getManagedImage(String key) {
 		Image image = ImageUtil.getImage(key);
 		if (image == null) {
 			image = getMissingImage();
 		}
 		return image;
 	}
 
 	public synchronized Image getMissingImage() {
 		if (missingImage == null) {
 			missingImage = ImageDescriptor.getMissingImageDescriptor().createImage();
 		}
 		return missingImage;
 	}
 
 	/**
 	 * Compares the two given values.
 	 * 
 	 * @param oldValue
 	 *            - old value
 	 * @param newValue
 	 *            - new value
 	 * @return true, if value has changed; otherwise false
 	 */
 	protected boolean hasChanged(Object oldValue, Object newValue) {
 		if (oldValue == null && newValue == null) {
 			return false;
 		}
 		return (oldValue == null && newValue != null) || (oldValue != null && newValue == null)
 				|| !oldValue.equals(newValue);
 	}
 
 	/**
 	 * Focus listener that also prevents the widget corresponding to this ridget
 	 * from getting the UI focus when the ridget is not focusable.
 	 * 
 	 * @see AbstractSWTRidget#setFocusable(boolean).
 	 */
 	private final class FocusManager extends FocusAdapter {
 
 		@Override
 		public void focusGained(FocusEvent e) {
 			if (focusable) {
 				fireFocusGained(new org.eclipse.riena.ui.ridgets.listener.FocusEvent(null, AbstractSWTRidget.this));
 			} else {
 				Control control = (Control) e.widget;
 				Composite parent = control.getParent();
 				Control[] tabList = parent.getTabList();
 				int i = findNextElement(control, tabList);
 				if (i != -1) {
 					Control nextFocusControl = tabList[i];
 					nextFocusControl.setFocus();
 				} else { // no suitable control found, try one level up
 					Composite pParent = parent.getParent();
 					if (pParent != null) {
 						tabList = pParent.getTabList();
 						i = findNextElement(parent, tabList);
 						if (i != -1) {
 							Control nextFocusControl = tabList[i];
 							nextFocusControl.setFocus();
 						}
 					}
 				}
 			}
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 			if (focusable) {
 				fireFocusLost(new org.eclipse.riena.ui.ridgets.listener.FocusEvent(AbstractSWTRidget.this, null));
 			}
 		}
 
 		private int findNextElement(Control control, Control[] controls) {
 			int myIndex = -1;
 			// find index for control
 			for (int i = 0; myIndex == -1 && i < controls.length; i++) {
 				if (controls[i] == control) {
 					myIndex = i;
 				}
 			}
 			// find next possible control
 			int result = -1;
 			for (int i = myIndex + 1; result == -1 && i < controls.length; i++) {
 				Control candidate = controls[i];
 				if (candidate.isEnabled() && candidate.isVisible()) {
 					result = i;
 				}
 			}
 			// find previous possible control
 			for (int i = 0; result == -1 && i < myIndex; i++) {
 				Control candidate = controls[i];
 				if (candidate.isEnabled() && candidate.isVisible()) {
 					result = i;
 				}
 			}
 			return result;
 		}
 	};
 
 	abstract public boolean isDisableMandatoryMarker();
 
 	public final boolean isErrorMarked() {
 		return !getMarkersOfType(ErrorMarker.class).isEmpty();
 	}
 
 	public final void setErrorMarked(boolean errorMarked) {
 		if (!errorMarked) {
 			if (errorMarker != null) {
 				removeMarker(errorMarker);
 			}
 		} else {
 			if (errorMarker == null) {
 				errorMarker = new ErrorMarker();
 			}
 			addMarker(errorMarker);
 		}
 	}
 
 	public synchronized final void addMarker(IMarker marker) {
 		if (markerSupport == null) {
 			markerSupport = new MarkerSupport(this, propertyChangeSupport);
 		}
 		if (marker instanceof MandatoryMarker) {
 			((MandatoryMarker) marker).setDisabled(isDisableMandatoryMarker());
 		}
 		markerSupport.addMarker(marker);
 	}
 
 	public final Collection<IMarker> getMarkers() {
 		if (markerSupport != null) {
 			return markerSupport.getMarkers();
 		}
 		return Collections.emptySet();
 	}
 
 	public final <T extends IMarker> Collection<T> getMarkersOfType(Class<T> type) {
 		if (markerSupport != null) {
 			return markerSupport.getMarkersOfType(type);
 		}
 		return Collections.emptySet();
 	}
 
 	public final void removeAllMarkers() {
 		if (markerSupport != null) {
 			markerSupport.removeAllMarkers();
 		}
 	}
 
 	public final void removeMarker(IMarker marker) {
 		if (markerSupport != null) {
 			markerSupport.removeMarker(marker);
 		}
 	}
 
 	public final boolean isEnabled() {
 		return getMarkersOfType(DisabledMarker.class).isEmpty();
 	}
 
 	public synchronized void setEnabled(boolean enabled) {
 		if (enabled) {
 			if (disabledMarker != null) {
 				removeMarker(disabledMarker);
 			}
 		} else {
 			if (disabledMarker == null) {
 				disabledMarker = new DisabledMarker();
 			}
 			addMarker(disabledMarker);
 		}
 	}
 
 	public final boolean isOutputOnly() {
 		return !getMarkersOfType(OutputMarker.class).isEmpty();
 	}
 
 	public void setOutputOnly(boolean outputOnly) {
 		if (!outputOnly) {
 			if (outputMarker != null) {
 				removeMarker(outputMarker);
 			}
 		} else {
 			if (outputMarker == null) {
 				outputMarker = new OutputMarker();
 			}
 			addMarker(outputMarker);
 		}
 	}
 
 	public final boolean isMandatory() {
 		return !getMarkersOfType(MandatoryMarker.class).isEmpty();
 	}
 
 	public final void setMandatory(boolean mandatory) {
 		if (!mandatory) {
 			if (mandatoryMarker != null) {
 				removeMarker(mandatoryMarker);
 			}
 		} else {
 			if (mandatoryMarker == null) {
 				mandatoryMarker = new MandatoryMarker();
 			}
 			addMarker(mandatoryMarker);
 		}
 	}
 
 	// protected methods
 	// //////////////////
 
 	/**
 	 * Iterates over the MandatoryMarker instances held by this ridget changing
 	 * their disabled state to given value.
 	 * 
 	 * @param disable
 	 *            the new disabled state
 	 */
 	protected final void disableMandatoryMarkers(boolean disable) {
 		for (IMarker marker : getMarkersOfType(MandatoryMarker.class)) {
 			MandatoryMarker mMarker = (MandatoryMarker) marker;
 			mMarker.setDisabled(disable);
 		}
 	}
 
 	// helping methods
 	// ////////////////
 
 	private void updateMarkers() {
 		if (markerSupport != null) {
 			markerSupport.updateMarkers();
 		}
 	}
 
 }
