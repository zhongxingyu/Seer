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
 
 import java.beans.PropertyChangeEvent;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.eclipse.riena.core.marker.AbstractMarker;
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.ErrorMessageMarker;
 import org.eclipse.riena.ui.core.marker.IMarkerPropertyChangeEvent;
 import org.eclipse.riena.ui.core.marker.IMessageMarker;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
 
 /**
 * Tests of the class {@link ComboRidget}.
  */
 public class MarkableRidgetTest extends AbstractSWTRidgetTest {
 
 	@Override
 	protected Control createWidget(Composite parent) {
 		return new Text(parent, SWT.SINGLE);
 	}
 
 	@Override
 	protected IRidget createRidget() {
 		return new TextRidget();
 	}
 
 	@Override
 	protected IMarkableRidget getRidget() {
 		return (IMarkableRidget) super.getRidget();
 	}
 
 	// test methods
 	// /////////////
 
 	public void testSetEnabled() {
 		IMarkableRidget ridget = getRidget();
 
 		assertTrue(ridget.isEnabled());
 
 		ridget.setEnabled(false);
 
 		assertFalse(ridget.isEnabled());
 
 		ridget.setEnabled(true);
 
 		assertTrue(ridget.isEnabled());
 	}
 
 	public void testSetErrorMarked() {
 		IMarkableRidget ridget = getRidget();
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.setErrorMarked(true);
 
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.setErrorMarked(false);
 
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	public void testSetMandatory() {
 		IMarkableRidget ridget = getRidget();
 
 		assertFalse(ridget.isMandatory());
 
 		ridget.setMandatory(true);
 
 		assertTrue(ridget.isMandatory());
 
 		ridget.setMandatory(false);
 
 		assertFalse(ridget.isMandatory());
 	}
 
 	public void testIsDisableMandatoryMarker() {
 		IMarkableRidget ridget = getRidget();
 		ridget.setMandatory(true);
 
 		assertFalse(ridget.isDisableMandatoryMarker());
 		assertTrue(ridget.isMandatory());
 
 		((ITextRidget) ridget).setText("foo");
 
 		assertTrue(ridget.isDisableMandatoryMarker());
 		assertTrue(ridget.isMandatory());
 
 		((ITextRidget) ridget).setText("   ");
 
 		assertTrue(ridget.isDisableMandatoryMarker());
 		assertTrue(ridget.isMandatory());
 
 		((ITextRidget) ridget).setText("");
 
 		assertFalse(ridget.isDisableMandatoryMarker());
 		assertTrue(ridget.isMandatory());
 	}
 
 	public void testSetOutputOnly() {
 		IMarkableRidget ridget = getRidget();
 
 		assertFalse(ridget.isOutputOnly());
 
 		ridget.setOutputOnly(true);
 
 		assertTrue(ridget.isOutputOnly());
 
 		ridget.setOutputOnly(false);
 
 		assertFalse(ridget.isOutputOnly());
 	}
 
 	@SuppressWarnings("unchecked")
 	public void testAddMarker() throws Exception {
 		IMarkableRidget ridget = getRidget();
 		IMarker marker1 = new ErrorMarker();
 		IMarker marker2 = new MandatoryMarker();
 
 		expectNoPropertyChangeEvent();
 
 		ridget.addMarker(null);
 
 		verifyPropertyChangeEvents();
 		Collection<?> markers = ridget.getMarkers();
 		assertEquals(0, markers.size());
 
 		Collection newValue = new HashSet<IMarker>();
 		newValue.add(marker1);
 		expectPropertyChangeEvents(new ExpectedMarkerPropertyChangeEvent(ridget.getMarkers(), newValue, false));
 
 		ridget.addMarker(marker1);
 
 		verifyPropertyChangeEvents();
 		newValue = ridget.getMarkers();
 		markers = ridget.getMarkers();
 		assertEquals(1, markers.size());
 		assertEquals(true, markers.contains(marker1));
 
 		newValue.add(marker2);
 		expectPropertyChangeEvents(new ExpectedMarkerPropertyChangeEvent(ridget.getMarkers(), newValue, false));
 
 		ridget.addMarker(marker2);
 
 		verifyPropertyChangeEvents();
 		markers = ridget.getMarkers();
 		assertEquals(2, markers.size());
 		assertEquals(true, markers.contains(marker1));
 		assertEquals(true, markers.contains(marker2));
 
 		expectNoPropertyChangeEvent();
 
 		ridget.addMarker(marker2);
 
 		verifyPropertyChangeEvents();
 		markers = ridget.getMarkers();
 		assertEquals(2, markers.size());
 		assertEquals(true, markers.contains(marker1));
 		assertEquals(true, markers.contains(marker2));
 	}
 
 	public void testRemoveMarker() throws Exception {
 		IMarkableRidget ridget = getRidget();
 
 		IMarker marker1 = new ErrorMarker();
 		IMarker marker2 = new MandatoryMarker();
 		IMarker marker3 = new AbstractMarker() {
 			/**/
 		};
 		ridget.addMarker(marker1);
 		ridget.addMarker(marker2);
 
 		expectNoPropertyChangeEvent();
 
 		ridget.removeMarker(null);
 		ridget.removeMarker(marker3);
 
 		verifyPropertyChangeEvents();
 
 		Collection<?> newValue = new HashSet<IMarker>(ridget.getMarkers());
 		newValue.remove(marker1);
 		expectPropertyChangeEvents(new ExpectedMarkerPropertyChangeEvent(ridget.getMarkers(), newValue, false));
 
 		ridget.removeMarker(marker1);
 
 		verifyPropertyChangeEvents();
 		Collection<?> markers = ridget.getMarkers();
 		assertEquals(1, markers.size());
 		assertEquals(true, markers.contains(marker2));
 
 		expectNoPropertyChangeEvent();
 
 		marker1.setAttribute("TestAttribute", "TestValue");
 
 		verifyPropertyChangeEvents();
 	}
 
 	public void testRemoveAllMarkers() throws Exception {
 		IMarkableRidget ridget = getRidget();
 		IMarker marker1 = new ErrorMarker();
 		IMarker marker2 = new MandatoryMarker();
 		ridget.addMarker(marker1);
 		ridget.addMarker(marker2);
 
 		Collection<IMarker> newValue = new HashSet<IMarker>(ridget.getMarkers());
 		newValue.clear();
 		expectPropertyChangeEvents(new ExpectedMarkerPropertyChangeEvent(ridget.getMarkers(), newValue, false));
 
 		ridget.removeAllMarkers();
 
 		verifyPropertyChangeEvents();
 		Collection<?> markers = ridget.getMarkers();
 		assertEquals(0, markers.size());
 
 		expectNoPropertyChangeEvent();
 
 		ridget.removeAllMarkers();
 		marker1.setAttribute("TestAttribute", "TestValue");
 
 		verifyPropertyChangeEvents();
 		markers = ridget.getMarkers();
 		assertEquals(0, markers.size());
 	}
 
 	public void testGetMarkers() throws Exception {
 		IMarkableRidget ridget = getRidget();
 		IMarker marker = new ErrorMarker();
 
 		Collection<?> markers = ridget.getMarkers();
 		assertNotNull(markers);
 
 		ridget.addMarker(marker);
 		Collection<?> markers2 = ridget.getMarkers();
 
 		assertEquals(0, markers.size());
 		assertEquals(1, markers2.size());
 	}
 
 	public void testGetMarkersOfType() throws Exception {
 		IMarkableRidget ridget = getRidget();
 		ErrorMarker errorMarker = new ErrorMarker();
 		MandatoryMarker mandatoryMarker = new MandatoryMarker();
 
 		Collection<? extends IMarker> markers = ridget.getMarkersOfType(null);
 		assertNotNull(markers);
 		assertEquals(0, markers.size());
 
 		markers = ridget.getMarkersOfType(ErrorMarker.class);
 		assertEquals(0, markers.size());
 		markers = ridget.getMarkersOfType(MandatoryMarker.class);
 		assertEquals(0, markers.size());
 
 		ridget.addMarker(errorMarker);
 		ridget.addMarker(mandatoryMarker);
 
 		markers = ridget.getMarkersOfType(ErrorMarker.class);
 		Iterator<?> iterator = markers.iterator();
 		assertEquals(true, iterator.hasNext());
 		assertSame(errorMarker, iterator.next());
 		assertEquals(false, iterator.hasNext());
 
 		markers = ridget.getMarkersOfType(MandatoryMarker.class);
 		iterator = markers.iterator();
 		assertEquals(true, iterator.hasNext());
 		assertSame(mandatoryMarker, iterator.next());
 		assertEquals(false, iterator.hasNext());
 
 		markers = ridget.getMarkersOfType(null);
 		assertNotNull(markers);
 		assertEquals(0, markers.size());
 	}
 
 	public void testFireMarkerAttributeEvents() throws Exception {
 		IMarkableRidget ridget = getRidget();
 		ErrorMessageMarker errorMessageMarker = new ErrorMessageMarker("Message");
 		ridget.addMarker(errorMessageMarker);
 
 		expectPropertyChangeEvents(new ExpectedMarkerPropertyChangeEvent(null, ridget.getMarkers(), true));
 
 		errorMessageMarker.setAttribute(IMessageMarker.MESSAGE, "NewMessage");
 
 		verifyPropertyChangeEvents();
 
 		Collection<IMarker> newValue = new HashSet<IMarker>(ridget.getMarkers());
 		newValue.remove(errorMessageMarker);
 		expectPropertyChangeEvents(new ExpectedMarkerPropertyChangeEvent(ridget.getMarkers(), newValue, false));
 
 		ridget.removeMarker(errorMessageMarker);
 
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 
 		errorMessageMarker.setAttribute(IMessageMarker.MESSAGE, "YetAnotherMessage");
 
 		verifyPropertyChangeEvents();
 	}
 
 	// helping methods
 	// ////////////////
 
 	private class ExpectedMarkerPropertyChangeEvent extends PropertyChangeEvent implements IMarkerPropertyChangeEvent {
 		private static final long serialVersionUID = 4711L;
 
 		private boolean attributeRelated;
 
 		public ExpectedMarkerPropertyChangeEvent(Object oldValue, Object newValue, boolean attributeRelated) {
 			super(getRidget(), IMarkableRidget.PROPERTY_MARKER, oldValue, newValue);
 			this.attributeRelated = attributeRelated;
 		}
 
 		public boolean isAttributeRelated() {
 			return attributeRelated;
 		}
 	}
 }
