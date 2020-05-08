 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Widget;
 
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.util.Nop;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.ridgets.IBasicMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IClickableRidget;
 import org.eclipse.riena.ui.ridgets.IMenuItemRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.listener.ClickEvent;
 import org.eclipse.riena.ui.ridgets.listener.FocusEvent;
 import org.eclipse.riena.ui.ridgets.listener.IFocusListener;
 
 /**
  * Superclass to test a Ridget which is bound to an SWT-Widget.
  */
 public abstract class AbstractSWTRidgetTest extends AbstractRidgetTestCase {
 
 	// protected methods
 	// //////////////////
 
 	@Override
 	protected abstract Widget createWidget(final Composite parent);
 
 	@Override
 	protected Widget getWidget() {
 		return (Widget) super.getWidget();
 	}
 
 	// test methods
 	// /////////////
 
 	public void testGetToolTip() {
 		if (!isWidgetControl()) {
 			// only Control supports tool tips
 			return;
 		}
 
 		getRidget().setUIControl(null);
 
 		assertEquals("Fails for " + getRidget(), null, getRidget().getToolTipText());
 
 		getRidget().setToolTipText("foo");
 
 		assertEquals("Fails for " + getRidget(), "foo", getRidget().getToolTipText());
 
 		final Control aControl = (Control) getWidget();
 		aControl.setToolTipText(null);
 		getRidget().setUIControl(aControl);
 
 		assertEquals("Fails for " + getRidget(), "foo", getRidget().getToolTipText());
 		assertEquals("Fails for " + getRidget(), "foo", ((Control) getRidget().getUIControl()).getToolTipText());
 	}
 
 	public void testGetFocusable() {
 		if (!isWidgetControl()) {
 			// only Control supports focus
 			return;
 		}
 
 		final IRidget aRidget = getRidget();
 
 		assertTrue("Fails for " + aRidget, aRidget.isFocusable());
 
 		aRidget.setFocusable(false);
 
 		assertFalse("Fails for " + aRidget, aRidget.isFocusable());
 
 		aRidget.setFocusable(true);
 
 		assertTrue("Fails for " + aRidget, aRidget.isFocusable());
 	}
 
 	public void testSetFocusable() {
 		if (!isWidgetControl()) {
 			// only Control supports focus
 			return;
 		}
 
 		final IRidget aRidget = getRidget();
 		final Control aControl = (Control) getWidget();
 		getOtherControl().moveAbove(aControl);
 
 		aControl.setFocus();
 		if (aControl.isFocusControl()) { // skip if control cannot receive focus
 			aRidget.setFocusable(false);
 			getOtherControl().setFocus();
 
 			assertTrue("Fails for " + getOtherControl(), getOtherControl().isFocusControl());
 
 			UITestHelper.sendString(getOtherControl().getDisplay(), "\t");
 			assertFalse("Fails for " + aControl, aControl.isFocusControl());
 
 			aRidget.setFocusable(true);
 
 			getOtherControl().setFocus();
 			UITestHelper.sendString(getOtherControl().getDisplay(), "\t");
 
 			assertTrue("Fails for " + aControl, aControl.isFocusControl());
 		}
 	}
 
 	public void testRequestFocus() throws Exception {
 		if (!isWidgetControl()) {
 			// only Control supports focus
 			return;
 		}
 
 		final Control aControl = (Control) getWidget();
 		aControl.setFocus();
 		if (aControl.isFocusControl()) { // skip if control cannot receive focus
 			assertTrue("Fails for " + getOtherControl(), getOtherControl().setFocus());
 
 			assertFalse("Fails for " + aControl, aControl.isFocusControl());
 			assertFalse("Fails for " + getRidget(), getRidget().hasFocus());
 
 			final List<FocusEvent> focusGainedEvents = new ArrayList<FocusEvent>();
 			final List<FocusEvent> focusLostEvents = new ArrayList<FocusEvent>();
 			final IFocusListener focusListener = new IFocusListener() {
 				public void focusGained(final FocusEvent event) {
 					focusGainedEvents.add(event);
 				}
 
 				public void focusLost(final FocusEvent event) {
 					focusLostEvents.add(event);
 				}
 			};
 			getRidget().addFocusListener(focusListener);
 
 			getRidget().requestFocus();
 
 			assertTrue("Fails for " + aControl, aControl.isFocusControl());
 			assertTrue("Fails for " + getRidget(), getRidget().hasFocus());
 			assertEquals("Fails for " + getRidget(), 1, focusGainedEvents.size());
 			assertEquals("Fails for " + getRidget(), getRidget(), focusGainedEvents.get(0).getNewFocusOwner());
 			assertEquals("Fails for " + getRidget(), 0, focusLostEvents.size());
 
 			assertTrue("Fails for " + getOtherControl(), getOtherControl().setFocus());
 
 			assertFalse("Fails for " + aControl, aControl.isFocusControl());
 			assertFalse("Fails for " + getRidget(), getRidget().hasFocus());
 			assertEquals("Fails for " + getRidget(), 1, focusGainedEvents.size());
 			assertEquals("Fails for " + getRidget(), 1, focusLostEvents.size());
 			assertEquals("Fails for " + getRidget(), getRidget(), focusLostEvents.get(0).getOldFocusOwner());
 
 			getRidget().removeFocusListener(focusListener);
 
 			getRidget().requestFocus();
 			assertTrue("Fails for " + getOtherControl(), getOtherControl().setFocus());
 
 			assertEquals("Fails for " + getRidget(), 1, focusGainedEvents.size());
 			assertEquals("Fails for " + getRidget(), 1, focusLostEvents.size());
 		}
 	}
 
 	/**
 	 * Tests that a control becomes visible after toggling ridget.setVisible().
 	 */
 	public void testBug257484() {
 		final Widget theWidget = getWidget();
 		if (!(theWidget instanceof Control)) {
 			// skip if not a control - only controls can be hidden / visible
 			return;
 		}
 		final IRidget theRidget = getRidget();
 		final Control control = (Control) theWidget;
 
 		assertTrue("Fails for " + theRidget, theRidget.isVisible());
 		assertTrue("Fails for " + control, control.isVisible());
 
 		theRidget.setVisible(false);
 
 		assertFalse("Fails for " + theRidget, theRidget.isVisible());
 		assertFalse("Fails for " + control, control.isVisible());
 
 		theRidget.setVisible(true);
 
 		assertTrue("Fails for " + theRidget, theRidget.isVisible());
 		assertTrue("Fails for " + control, control.isVisible());
 
 		control.setVisible(false);
 		assertFalse("Fails for " + theRidget, theRidget.isVisible());
 
 		//unbind
 		theRidget.setUIControl(null);
 		// check saved state
 		assertFalse("Fails for " + theRidget, theRidget.isVisible());
 
 		theRidget.setUIControl(control);
 		control.setVisible(true);
 		assertTrue("Fails for " + theRidget, theRidget.isVisible());
 		theRidget.setUIControl(null);
 		assertTrue("Fails for " + theRidget, theRidget.isVisible());
 
 		theRidget.setUIControl(control);
 
 		// check implicit visibility
 		final Composite parent = control.getParent();
 		if (parent != null) {
 			parent.setVisible(false);
 			assertFalse("Fails for " + theRidget, theRidget.isVisible());
 			theRidget.setUIControl(null);
 			parent.setVisible(true);
 			assertFalse("Fails for " + theRidget, theRidget.isVisible());
 		}
 	}
 
 	/**
 	 * As per Bug 327496.
 	 * 
 	 * @see TextRidgetTest2#testToggleMandatoryMarkerWithMarkerHidingOn()
 	 * @see TextRidgetTest2#testToggleMarkerHidingWithMandatoryMarkerOn()
 	 */
 	@SuppressWarnings("unchecked")
 	public void testHideAndUnhideMarkers() {
 		if (!(getRidget() instanceof IBasicMarkableRidget)) {
 			return;
 		}
 		final IBasicMarkableRidget ridget = (IBasicMarkableRidget) getRidget();
 
 		assertEquals(0, ridget.getHiddenMarkerTypes().size());
 
 		ridget.hideMarkersOfType(ErrorMarker.class);
 
 		assertEquals(1, ridget.getHiddenMarkerTypes().size());
 
 		ridget.hideMarkersOfType(ErrorMarker.class);
 
 		Collection<Class<IMarker>> hiddenMarkers;
 
 		hiddenMarkers = ridget.getHiddenMarkerTypes();
 		assertEquals(1, hiddenMarkers.size());
 		assertTrue(hiddenMarkers.contains(ErrorMarker.class));
 
 		hiddenMarkers = ridget.hideMarkersOfType(MandatoryMarker.class);
 
 		assertEquals(2, ridget.getHiddenMarkerTypes().size());
 		assertTrue(hiddenMarkers.contains(ErrorMarker.class));
 		assertTrue(hiddenMarkers.contains(MandatoryMarker.class));
 
 		hiddenMarkers = ridget.showMarkersOfType(ErrorMarker.class);
 
 		assertEquals(1, ridget.getHiddenMarkerTypes().size());
 		assertTrue(hiddenMarkers.contains(MandatoryMarker.class));
 
 		ridget.showMarkersOfType(MandatoryMarker.class);
 
 		assertEquals(0, ridget.getHiddenMarkerTypes().size());
 	}
 
 	public void testAddClickListener() {
 
 		if (!(getRidget() instanceof IClickableRidget)) {
 			return;
 		}
 		final IClickableRidget ridget = (IClickableRidget) getRidget();
 
 		try {
 			ridget.addClickListener(null);
 			fail();
 		} catch (final RuntimeException npe) {
 			ok();
 		}
 
 		final FTClickListener listener1 = new FTClickListener();
 		ridget.addClickListener(listener1);
 
 		final FTClickListener listener2 = new FTClickListener();
 		ridget.addClickListener(listener2);
 		ridget.addClickListener(listener2);
 
 		Event mdEvent = new Event();
 		final Widget control = getWidget();
 		mdEvent.widget = control;
 		mdEvent.type = SWT.MouseDown;
 		mdEvent.button = 2;
 		control.notifyListeners(SWT.MouseDown, mdEvent);
 
 		assertEquals(0, listener1.getCount());
 		assertEquals(0, listener2.getCount());
 
 		mdEvent.type = SWT.MouseUp;
 		control.notifyListeners(SWT.MouseUp, mdEvent);
 
 		assertEquals(1, listener1.getCount());
 		assertEquals(1, listener2.getCount());
 
 		final ClickEvent event = listener2.getEvent();
 		assertEquals(ridget, event.getSource());
 		assertEquals(2, event.getButton());
 		assertEquals(-1, event.getColumnIndex());
 		assertNull(event.getRow());
 
 		ridget.removeClickListener(listener1);
 
 		mdEvent.type = SWT.MouseDown;
 		control.notifyListeners(SWT.MouseDown, mdEvent);
 		mdEvent.type = SWT.MouseUp;
 		control.notifyListeners(SWT.MouseUp, mdEvent);
 
 		assertEquals(1, listener1.getCount());
 
 		// left mouse button
 
 		mdEvent = new Event();
 		mdEvent.widget = control;
 		mdEvent.type = SWT.MouseDown;
 		mdEvent.button = 1;
 		control.notifyListeners(SWT.MouseDown, mdEvent);
 
 		assertEquals(2, listener2.getCount());
 
 		mdEvent.type = SWT.MouseUp;
 		control.notifyListeners(SWT.MouseUp, mdEvent);
 
 		assertEquals(3, listener2.getCount());
 
 		// different mouse buttons at down and up
 
 		mdEvent = new Event();
 		mdEvent.widget = control;
 		mdEvent.type = SWT.MouseDown;
 		mdEvent.button = 1;
 		control.notifyListeners(SWT.MouseDown, mdEvent);
 
 		assertEquals(3, listener2.getCount());
 
 		mdEvent.type = SWT.MouseUp;
 		mdEvent.button = 2;
 		control.notifyListeners(SWT.MouseUp, mdEvent);
 
 		assertEquals(3, listener2.getCount());
 
 		// Ridget is disabled
 
 		getRidget().setEnabled(false);
 
 		mdEvent = new Event();
 		mdEvent.widget = control;
 		mdEvent.type = SWT.MouseDown;
 		mdEvent.button = 1;
 		control.notifyListeners(SWT.MouseDown, mdEvent);
 
 		assertEquals(3, listener2.getCount());
 
 		mdEvent.type = SWT.MouseUp;
 		control.notifyListeners(SWT.MouseUp, mdEvent);
 
 		assertEquals(3, listener2.getCount());
 	}
 
 	public void testGetMenuItemCount() {
 		final IRidget ridget = getRidget();
 		final String menuItemWithoutIconText = "MenuItemWithoutIcon"; //$NON-NLS-1$
 		final String menuItemWithIconText = "MenuItemWithIcon"; //$NON-NLS-1$
 		final String iconName = "leftArrow"; //$NON-NLS-1$
 
 		assertEquals(0, ridget.getMenuItemCount());
 
 		ridget.addMenuItem(menuItemWithoutIconText);
 		assertEquals(1, ridget.getMenuItemCount());
 
 		ridget.addMenuItem(menuItemWithIconText, iconName);
 		assertEquals(2, ridget.getMenuItemCount());
 	}
 
 	public void testGetMenuItem() {
 		final IRidget ridget = getRidget();
 		final String menuItemWithoutIconText = "MenuItemWithoutIcon"; //$NON-NLS-1$
 		final String menuItemWithIconText = "MenuItemWithIcon"; //$NON-NLS-1$
 		final String iconName = "leftArrow"; //$NON-NLS-1$
 
 		final IMenuItemRidget menuItemWithoutIcon = ridget.addMenuItem(menuItemWithoutIconText);
 		assertEquals(menuItemWithoutIcon, ridget.getMenuItem(0));
 
 		final IMenuItemRidget menuItemWithIcon = ridget.addMenuItem(menuItemWithIconText, iconName);
 		assertEquals(menuItemWithIcon, ridget.getMenuItem(1));
 	}
 
 	public void testGetMenuItemEmptyContextMenu() {
 		try {
			final IRidget ridget = getRidget();
			final String menuItemWithoutIconText = "MenuItemWithoutIcon"; //$NON-NLS-1$
			final String menuItemWithIconText = "MenuItemWithIcon"; //$NON-NLS-1$
			final String iconName = "leftArrow"; //$NON-NLS-1$

			ridget.addMenuItem(menuItemWithoutIconText);
			ridget.addMenuItem(menuItemWithIconText, iconName);
			ridget.getMenuItem(2);
			fail("IllegalArgumentException expected"); //$NON-NLS-1$
		} catch (final IllegalArgumentException expected) {
			Nop.reason("IllegalArgumentException expected"); //$NON-NLS-1$
 		}
 	}
 
 	public void testGetMenuItemNotExistingItem() {
 		final IRidget ridget = getRidget();
 		try {
 			ridget.getMenuItem(0);
			fail("IllegalStateException expected"); //$NON-NLS-1$
		} catch (final IllegalStateException expected) {
			Nop.reason("IllegalStateException expected"); //$NON-NLS-1$
 		}
 	}
 
 	public void testAddMenuItem() {
 		final IRidget ridget = getRidget();
 		final String menuItemWithoutIconText = "MenuItemWithoutIcon"; //$NON-NLS-1$
 		final String menuItemWithIconText = "MenuItemWithIcon"; //$NON-NLS-1$
 		final String iconName = "leftArrow"; //$NON-NLS-1$
 		final Control control = (Control) getWidget();
 
 		final IMenuItemRidget menuItemWithoutIcon = ridget.addMenuItem(menuItemWithoutIconText);
 		assertEquals(1, ridget.getMenuItemCount());
 		assertEquals(menuItemWithoutIcon, ridget.getMenuItem(0));
 		assertEquals(1, control.getMenu().getItemCount());
 
 		final IMenuItemRidget menuItemWithIcon = ridget.addMenuItem(menuItemWithIconText, iconName);
 		assertEquals(2, ridget.getMenuItemCount());
 		assertEquals(menuItemWithIcon, ridget.getMenuItem(1));
 		assertEquals(2, control.getMenu().getItemCount());
 	}
 
 	public void testRemoveMenuItem() {
 		final IRidget ridget = getRidget();
 		final String menuItemWithoutIconText = "MenuItemWithoutIcon"; //$NON-NLS-1$
 		final String menuItemWithIconText = "MenuItemWithIcon"; //$NON-NLS-1$
 		final String iconName = "leftArrow"; //$NON-NLS-1$
 		final Control control = (Control) getWidget();
 
 		final IMenuItemRidget menuItemWithoutIcon = ridget.addMenuItem(menuItemWithoutIconText);
 		IMenuItemRidget menuItemWithIcon = ridget.addMenuItem(menuItemWithIconText, iconName);
 
 		assertEquals(2, ridget.getMenuItemCount());
 		assertEquals(2, control.getMenu().getItemCount());
 		ridget.removeMenuItem(menuItemWithIconText);
 		assertEquals(1, ridget.getMenuItemCount());
 		assertEquals(1, control.getMenu().getItemCount());
 
 		menuItemWithIcon = ridget.addMenuItem(menuItemWithIconText, iconName);
 		assertEquals(2, ridget.getMenuItemCount());
 		assertEquals(2, control.getMenu().getItemCount());
 
 		ridget.removeMenuItem(menuItemWithoutIcon);
 		assertEquals(1, ridget.getMenuItemCount());
 		assertEquals(1, control.getMenu().getItemCount());
 		ridget.removeMenuItem(menuItemWithIcon);
 		assertEquals(0, ridget.getMenuItemCount());
 		assertEquals(0, control.getMenu().getItemCount());
 	}
 
 }
