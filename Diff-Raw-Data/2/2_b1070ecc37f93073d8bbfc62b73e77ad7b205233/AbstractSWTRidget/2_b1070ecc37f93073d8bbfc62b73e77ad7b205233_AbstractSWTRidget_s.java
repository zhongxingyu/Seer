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
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.riena.ui.ridgets.AbstractRidget;
 import org.eclipse.riena.ui.swt.utils.ImageUtil;
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
 public abstract class AbstractSWTRidget extends AbstractRidget {
 
 	private static Image missingImage;
 	private FocusListener focusManager = new FocusManager();
 	private Control uiControl;
 	private boolean visible;
 	private boolean focusable;
 	private String toolTip = null;
 	private boolean blocked;
 
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
 		visible = true;
 		focusable = true;
 	}
 
 	public void setUIControl(Object uiControl) {
 		checkUIControl(uiControl);
 		uninstallListeners();
 		unbindUIControl();
 		this.uiControl = (Control) uiControl;
 		updateVisible();
 		updateToolTip();
 		bindUIControl();
 		installListeners();
 	}
 
 	public Control getUIControl() {
 		return uiControl;
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
		return uiControl == null ? visible : uiControl.isVisible();
 	}
 
 	public void setVisible(boolean visible) {
 		if (this.visible != visible) {
 			this.visible = visible;
 			updateVisible();
 		}
 	}
 
 	public final void setToolTipText(String toolTipText) {
 		this.toolTip = toolTipText;
 		updateToolTip();
 	}
 
 	public final String getToolTipText() {
 		return toolTip;
 	}
 
 	public boolean isBlocked() {
 		return blocked;
 	}
 
 	public void setBlocked(boolean blocked) {
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
 
 	private void updateVisible() {
 		if (uiControl != null) {
 			uiControl.setVisible(visible);
 		}
 	}
 
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
 
 }
