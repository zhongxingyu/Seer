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
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.easymock.EasyMock;
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.tests.UITestHelper;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.core.marker.OutputMarker;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.listener.FocusEvent;
 import org.eclipse.riena.ui.ridgets.listener.IFocusListener;
 import org.eclipse.riena.ui.tests.base.PropertyChangeEventEquals;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Tests for the class {@link AbstractSwtRidget}.
  */
 public abstract class AbstractSWTRidgetTest extends TestCase {
 
 	private Shell shell;
 	private Control control;
 	private IRidget ridget;
 	private Text otherControl;
 	private PropertyChangeListener propertyChangeListenerMock;
 
 	@Override
 	protected void setUp() throws Exception {
 		Display display = Display.getDefault();
 
 		Realm realm = SWTObservables.getRealm(display);
 		assertNotNull(realm);
 		ReflectionUtils.invokeHidden(realm, "setDefault", realm);
 
 		shell = new Shell();
 		shell.setLayout(new RowLayout(SWT.VERTICAL));
 
 		control = createUIControl(shell);
 
 		ridget = createRidget();
 		ridget.setUIControl(control);
 		propertyChangeListenerMock = EasyMock.createMock(PropertyChangeListener.class);
 		ridget.addPropertyChangeListener(propertyChangeListenerMock);
 
 		otherControl = new Text(shell, SWT.SINGLE);
 		otherControl.setText("other focusable widget");
 
 		shell.setSize(130, 100);
 		shell.setLocation(0, 0);
 		shell.open();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		ridget = null;
 		control.dispose();
 		control = null;
 		otherControl.dispose();
 		otherControl = null;
 		shell.dispose();
 		shell = null;
 	}
 
 	// protected methods
 	// //////////////////
 
 	protected abstract Control createUIControl(final Composite parent);
 
 	protected abstract IRidget createRidget();
 
 	protected Control getUIControl() {
 		return control;
 	}
 
 	protected IRidget getRidget() {
 		return ridget;
 	}
 
 	protected final Shell getShell() {
 		return shell;
 	}
 
 	// easy mock helper methods
 	// /////////////////////////
 
 	protected final void verifyPropertyChangeEvents() {
 		EasyMock.verify(propertyChangeListenerMock);
 	}
 
 	protected final void expectNoPropertyChangeEvent() {
 		EasyMock.reset(propertyChangeListenerMock);
 		EasyMock.replay(propertyChangeListenerMock);
 	}
 
 	protected final void expectPropertyChangeEvents(PropertyChangeEvent... propertyChangeEvents) {
 		EasyMock.reset(propertyChangeListenerMock);
 		for (PropertyChangeEvent propertyChangeEvent : propertyChangeEvents) {
 			propertyChangeListenerMock.propertyChange(createArgumentMatcher(propertyChangeEvent));
 		}
 		EasyMock.replay(propertyChangeListenerMock);
 	}
 
 	protected final void expectPropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
 		expectPropertyChangeEvents(new PropertyChangeEvent(getRidget(), propertyName, oldValue, newValue));
 	}
 
 	// test methods
 	// /////////////
 
 	public void testIsVisible() {
 		shell.open();
 		ridget.setVisible(false);
 		assertFalse(ridget.isVisible());
 
 		ridget.setVisible(true);
 		assertTrue(ridget.isVisible());
 	}
 
 	public void testGetToolTip() {
 		ridget.setUIControl(null);
 
 		assertEquals(null, ridget.getToolTipText());
 
 		ridget.setToolTipText("foo");
 
 		assertEquals("foo", ridget.getToolTipText());
 
 		Control aControl = getUIControl();
 		aControl.setToolTipText(null);
 		ridget.setUIControl(aControl);
 
 		assertEquals("foo", ridget.getToolTipText());
 		assertEquals("foo", ((Control) ridget.getUIControl()).getToolTipText());
 	}
 
 	public void testGetFocusable() {
 		IRidget aRidget = getRidget();
 
 		assertTrue(aRidget.isFocusable());
 
 		aRidget.setFocusable(false);
 
 		assertFalse(aRidget.isFocusable());
 
 		aRidget.setFocusable(true);
 
 		assertTrue(aRidget.isFocusable());
 	}
 
 	public void testSetFocusable() {
 		IRidget aRidget = getRidget();
 		Control aControl = getUIControl();
 		otherControl.moveAbove(aControl);
 
 		aControl.setFocus();
 		if (aControl.isFocusControl()) { // skip if control cannot receive focus
 
 			aRidget.setFocusable(false);
 			otherControl.setFocus();
 
 			assertTrue(otherControl.isFocusControl());
 
 			UITestHelper.sendString(otherControl.getDisplay(), "\t");
 
 			assertFalse(aControl.isFocusControl());
 
 			aRidget.setFocusable(true);
 
 			otherControl.setFocus();
 			UITestHelper.sendString(otherControl.getDisplay(), "\t");
 
 			assertTrue(aControl.isFocusControl());
 		}
 	}
 
 	public void testRequestFocus() throws Exception {
 		control.setFocus();
 		if (control.isFocusControl()) { // skip if control cannot receive focus
 			assertTrue(otherControl.setFocus());
 
 			assertFalse(control.isFocusControl());
 			assertFalse(ridget.hasFocus());
 
 			final List<FocusEvent> focusGainedEvents = new ArrayList<FocusEvent>();
 			final List<FocusEvent> focusLostEvents = new ArrayList<FocusEvent>();
 			IFocusListener focusListener = new IFocusListener() {
 				public void focusGained(FocusEvent event) {
 					focusGainedEvents.add(event);
 				}
 
 				public void focusLost(FocusEvent event) {
 					focusLostEvents.add(event);
 				}
 			};
 			ridget.addFocusListener(focusListener);
 
 			ridget.requestFocus();
 
 			assertTrue(control.isFocusControl());
 			assertTrue(ridget.hasFocus());
 			assertEquals(1, focusGainedEvents.size());
 			assertEquals(ridget, focusGainedEvents.get(0).getNewFocusOwner());
 			assertEquals(0, focusLostEvents.size());
 
 			assertTrue(otherControl.setFocus());
 
 			assertFalse(control.isFocusControl());
 			assertFalse(ridget.hasFocus());
 			assertEquals(1, focusGainedEvents.size());
 			assertEquals(1, focusLostEvents.size());
 			assertEquals(ridget, focusLostEvents.get(0).getOldFocusOwner());
 
 			ridget.removeFocusListener(focusListener);
 
 			ridget.requestFocus();
 			assertTrue(otherControl.setFocus());
 
 			assertEquals(1, focusGainedEvents.size());
 			assertEquals(1, focusLostEvents.size());
 		}
 	}
 
 	public void testFiresMarkerProperty() {
 		if (!(getRidget() instanceof IMarkableRidget)) {
 			return;
 		}
 		IMarkableRidget ridget = (IMarkableRidget) getRidget();
 		IMarker marker = new MandatoryMarker();
		HashSet<IMarker> oneMarker = new HashSet<IMarker>();
		oneMarker.add(marker);
 
 		assertTrue(ridget.isEnabled());
 
		expectPropertyChangeEvent(IMarkableRidget.PROPERTY_MARKER, Collections.EMPTY_SET, oneMarker);
 		ridget.addMarker(marker);
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		ridget.addMarker(marker);
 		verifyPropertyChangeEvents();
 
		expectPropertyChangeEvent(IMarkableRidget.PROPERTY_MARKER, oneMarker, Collections.EMPTY_SET);
 		ridget.removeMarker(marker);
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		ridget.removeMarker(marker);
 		verifyPropertyChangeEvents();
 	}
 
 	public void testFiresDisabledPropertyUsingSetter() {
 		if (!(getRidget() instanceof IMarkableRidget)) {
 			return;
 		}
 		IMarkableRidget ridget = (IMarkableRidget) getRidget();
 		ridget.removePropertyChangeListener(propertyChangeListenerMock);
 		ridget.addPropertyChangeListener(IMarkableRidget.PROPERTY_ENABLED, propertyChangeListenerMock);
 
 		assertTrue(ridget.isEnabled());
 
 		expectNoPropertyChangeEvent();
 		ridget.setEnabled(true);
 		verifyPropertyChangeEvents();
 
 		expectPropertyChangeEvent(IMarkableRidget.PROPERTY_ENABLED, Boolean.TRUE, Boolean.FALSE);
 		ridget.setEnabled(false);
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		ridget.setEnabled(false);
 		verifyPropertyChangeEvents();
 
 		expectPropertyChangeEvent(IMarkableRidget.PROPERTY_ENABLED, Boolean.FALSE, Boolean.TRUE);
 		ridget.setEnabled(true);
 		verifyPropertyChangeEvents();
 	}
 
 	public void testFiresDisabledPropertyUsingAddRemove() {
 		if (!(getRidget() instanceof IMarkableRidget)) {
 			return;
 		}
 		IMarkableRidget ridget = (IMarkableRidget) getRidget();
 		IMarker marker = new DisabledMarker();
 		ridget.removePropertyChangeListener(propertyChangeListenerMock);
 		ridget.addPropertyChangeListener(IMarkableRidget.PROPERTY_ENABLED, propertyChangeListenerMock);
 
 		assertTrue(ridget.isEnabled());
 
 		expectPropertyChangeEvent(IMarkableRidget.PROPERTY_ENABLED, Boolean.TRUE, Boolean.FALSE);
 		ridget.addMarker(marker);
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		ridget.addMarker(marker);
 		verifyPropertyChangeEvents();
 
 		expectPropertyChangeEvent(IMarkableRidget.PROPERTY_ENABLED, Boolean.FALSE, Boolean.TRUE);
 		ridget.removeMarker(marker);
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		ridget.removeMarker(marker);
 		verifyPropertyChangeEvents();
 	}
 
 	/**
 	 * Check that disabling / enabling works when we don't have a bound control.
 	 */
 	public void testDisableWithoutUIControl() {
 		if (!(getRidget() instanceof IMarkableRidget)) {
 			return;
 		}
 		IMarkableRidget ridget = (IMarkableRidget) getRidget();
 		ridget.setUIControl(null);
 
 		assertTrue(ridget.isEnabled());
 
 		ridget.setEnabled(false);
 
 		assertFalse(ridget.isEnabled());
 
 		ridget.setEnabled(true);
 
 		assertTrue(ridget.isEnabled());
 	}
 
 	public void testFiresOutputPropertyUsingSetter() {
 		if (!(getRidget() instanceof IMarkableRidget)) {
 			return;
 		}
 		IMarkableRidget ridget = (IMarkableRidget) getRidget();
 		ridget.removePropertyChangeListener(propertyChangeListenerMock);
 		ridget.addPropertyChangeListener(IMarkableRidget.PROPERTY_OUTPUT_ONLY, propertyChangeListenerMock);
 
 		assertFalse(ridget.isOutputOnly());
 
 		expectNoPropertyChangeEvent();
 		ridget.setOutputOnly(false);
 		verifyPropertyChangeEvents();
 
 		expectPropertyChangeEvent(IMarkableRidget.PROPERTY_OUTPUT_ONLY, Boolean.FALSE, Boolean.TRUE);
 		ridget.setOutputOnly(true);
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		ridget.setOutputOnly(true);
 		verifyPropertyChangeEvents();
 
 		expectPropertyChangeEvent(IMarkableRidget.PROPERTY_OUTPUT_ONLY, Boolean.TRUE, Boolean.FALSE);
 		ridget.setOutputOnly(false);
 		verifyPropertyChangeEvents();
 	}
 
 	public void testFiresOutputPropertyUsingAddRemove() {
 		if (!(getRidget() instanceof IMarkableRidget)) {
 			return;
 		}
 		IMarkableRidget ridget = (IMarkableRidget) getRidget();
 		IMarker marker = new OutputMarker();
 		ridget.removePropertyChangeListener(propertyChangeListenerMock);
 		ridget.addPropertyChangeListener(IMarkableRidget.PROPERTY_OUTPUT_ONLY, propertyChangeListenerMock);
 
 		assertFalse(ridget.isOutputOnly());
 
 		expectPropertyChangeEvent(IMarkableRidget.PROPERTY_OUTPUT_ONLY, Boolean.FALSE, Boolean.TRUE);
 		ridget.addMarker(marker);
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		ridget.addMarker(marker);
 		verifyPropertyChangeEvents();
 
 		expectPropertyChangeEvent(IMarkableRidget.PROPERTY_OUTPUT_ONLY, Boolean.TRUE, Boolean.FALSE);
 		ridget.removeMarker(marker);
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		ridget.removeMarker(marker);
 		verifyPropertyChangeEvents();
 	}
 
 	// helping methods
 	// ////////////////
 
 	private PropertyChangeEvent createArgumentMatcher(PropertyChangeEvent propertyChangeEvent) {
 		return PropertyChangeEventEquals.eqPropertyChangeEvent(propertyChangeEvent);
 	}
 
 }
