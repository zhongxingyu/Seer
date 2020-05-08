 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Widget;

 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.ErrorMessageMarker;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.core.marker.OutputMarker;
 import org.eclipse.riena.ui.ridgets.AbstractMarkerSupport;
 import org.eclipse.riena.ui.ridgets.AbstractRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingPropertyLocator;
 import org.eclipse.riena.ui.swt.utils.ImageStore;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 
 /**
  * Ridget for an SWT widget.
  */
 public abstract class AbstractSWTWidgetRidget extends AbstractRidget implements IMarkableRidget {
 
 	private Widget uiControl;
 	private String toolTip = null;
 	private boolean blocked;
 	private ErrorMessageMarker errorMarker;
 	private DisabledMarker disabledMarker;
 	private MandatoryMarker mandatoryMarker;
 	private OutputMarker outputMarker;
 	private HiddenMarker hiddenMarker;
 	private AbstractMarkerSupport markerSupport;
 
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
 
 	/*
 	 * Do not override. Template Method Pattern: Subclasses may implement {@code
 	 * unbindUIControl()} and {@code bindUIControl}, if they need to manipulate
	 * the control when it is bound/unbound, for example to add/remove
 	 * listeners.
 	 */
 	public final void setUIControl(Object uiControl) {
 		checkUIControl(uiControl);
 		uninstallListeners();
 		unbindUIControl();
 		this.uiControl = (Widget) uiControl;
 		updateMarkers();
 		updateToolTip();
 		bindUIControl();
 		installListeners();
 	}
 
 	public Widget getUIControl() {
 		return uiControl;
 	}
 
 	public String getID() {
 
 		if (getUIControl() != null) {
 			IBindingPropertyLocator locator = SWTBindingPropertyLocator.getInstance();
 			return locator.locateBindingProperty(getUIControl());
 		}
 
 		return null;
 
 	}
 
 	public void requestFocus() {
 		// not supported
 	}
 
 	public boolean hasFocus() {
 		return false;
 	}
 
 	public boolean isFocusable() {
 		return false;
 	}
 
 	public void setFocusable(boolean focusable) {
 		// not supported
 	}
 
 	public final boolean isVisible() {
 		return (uiControl != null) && (getMarkersOfType(HiddenMarker.class).isEmpty());
 	}
 
 	public final void setVisible(boolean visible) {
 
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
 		String oldValue = this.toolTip;
 		this.toolTip = toolTipText;
 		updateToolTip();
 		firePropertyChange(PROPERTY_TOOLTIP, oldValue, this.toolTip);
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
 
 	// abstract methods - subclasses must implement
 	/////////////////////////////////////////////////////////
 
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
 	 * Returns true if mandatory markers should be disabled (i.e. if the ridget
 	 * has non empty input).
 	 */
 	abstract public boolean isDisableMandatoryMarker();
 
 	/**
 	 * Update the tooltip for the ridget's control.
 	 */
 	abstract protected void updateToolTip();
 
 	// helping methods
 	// ////////////////
 
 	/**
 	 * Adds listeners to the <tt>uiControl</tt> after it was bound to the
 	 * ridget.
 	 */
 	protected void installListeners() {
 	}
 
 	/**
 	 * Removes listeners from the <tt>uiControl</tt> when it is about to be
 	 * unbound from the ridget.
 	 */
 	protected void uninstallListeners() {
 	}
 
 	protected Image getManagedImage(String key) {
 		Image image = ImageStore.getInstance().getImage(key);
 		if (image == null) {
 			image = ImageStore.getInstance().getMissingImage();
 		}
 		return image;
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
 		if (oldValue == null || newValue == null) {
 			return true;
 		}
 		return !oldValue.equals(newValue);
 	}
 
 	public final boolean isErrorMarked() {
 		return !getMarkersOfType(ErrorMarker.class).isEmpty();
 	}
 
 	public final void setErrorMarked(boolean errorMarked) {
 		setErrorMarked(errorMarked, null);
 	}
 
 	protected final void setErrorMarked(boolean errorMarked, String message) {
 		if (!errorMarked) {
 			if (errorMarker != null) {
 				removeMarker(errorMarker);
 			}
 		} else {
 			if (errorMarker == null) {
 				errorMarker = new ErrorMessageMarker(message);
 			} else {
 				errorMarker.setMessage(message);
 			}
 			addMarker(errorMarker);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Adding the same marker twice has no effect.
 	 */
 	public synchronized final void addMarker(IMarker marker) {
 		if (markerSupport == null) {
 			markerSupport = createMarkerSupport();
 		}
 		if (marker instanceof MandatoryMarker) {
 			((MandatoryMarker) marker).setDisabled(isDisableMandatoryMarker());
 		}
 		markerSupport.addMarker(marker);
 	}
 
 	protected AbstractMarkerSupport createMarkerSupport() {
 		return new MarkerSupport(this, propertyChangeSupport);
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
 
 	public final synchronized void setEnabled(boolean enabled) {
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
 
 	public final void setOutputOnly(boolean outputOnly) {
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
