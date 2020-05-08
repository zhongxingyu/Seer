 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
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
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.BrowserFunction;
 import org.eclipse.swt.browser.LocationListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 import org.eclipse.riena.beans.common.StringBean;
 import org.eclipse.riena.core.test.collect.UITestCase;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.ui.ridgets.IBrowserRidget;
 import org.eclipse.riena.ui.ridgets.IBrowserRidget.IBrowserRidgetFunction;
 import org.eclipse.riena.ui.ridgets.listener.ILocationListener;
 import org.eclipse.riena.ui.ridgets.listener.LocationEvent;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 
 /**
  * Tests for {@link BrowserRidget}
  */
 @UITestCase
 public class BrowserRidgetTest extends AbstractSWTRidgetTest {
 
 	@Override
 	protected Browser createWidget(final Composite parent) {
 		return new Browser(parent, SWT.NONE);
 	}
 
 	@Override
 	protected IBrowserRidget createRidget() {
 		return new BrowserRidget();
 	}
 
 	@Override
 	protected Browser getWidget() {
 		return (Browser) super.getWidget();
 	}
 
 	@Override
 	protected IBrowserRidget getRidget() {
 		return (IBrowserRidget) super.getRidget();
 	}
 
 	public void testRidgetMapping() {
 		final SwtControlRidgetMapper mapper = SwtControlRidgetMapper.getInstance();
 		assertSame(BrowserRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	@Override
 	public void testSetFocusable() {
 		// skipping super.testSetFocusable() because of Bug 84532
 		ok();
 	}
 
 	@Override
 	public void testRequestFocus() {
 		// skipping testRequestFocus() because of Bug 84532
 		ok();
 	}
 
 	public void testMapScriptFunctionRidget() {
 		final IBrowserRidget r = new BrowserRidget();
 		final Map<String, BrowserFunction> functions = ReflectionUtils.getHidden(r, "browserFunctions"); //$NON-NLS-1$
 
 		assertNull(r.getUIControl());
 		assertTrue(functions.isEmpty());
 
 		final String functionName = "f1"; //$NON-NLS-1$
 		r.mapScriptFunction(functionName, new IBrowserRidgetFunction() {
 			public Object execute(final Object[] jsParams) {
 				return null;
 			}
 		});
 		assertTrue(functions.isEmpty());
 
 		// bind
 		r.setUIControl(new Browser(getShell(), SWT.NONE));
 		assertEquals(1, functions.size());
 		assertEquals(functionName, functions.get(functionName).getName());
 
 		// unbind
 		r.setUIControl(null);
 		assertTrue(functions.isEmpty());
 
 		// bind again, function should be created again
 		r.setUIControl(new Browser(getShell(), SWT.NONE));
 		assertEquals(1, functions.size());
 		assertEquals(functionName, functions.get(functionName).getName());
 
 		// unmap function while ridget is bound to the ui control
 		r.unmapScriptFunction(functionName);
 		assertTrue(functions.isEmpty());
 
 		// map function while ridget is bound to the ui control
 		r.mapScriptFunction(functionName, new IBrowserRidgetFunction() {
 			public Object execute(final Object[] jsParams) {
 				return null;
 			}
 		});
 		assertEquals(1, functions.size());
 		assertEquals(functionName, functions.get(functionName).getName());
 	}
 
 	public void testBindToModel() {
 		final IBrowserRidget ridget = getRidget();
 		final String url1 = "http://www.redview.org";
 		final String url2 = "http://www.eclipse.org";
 
 		final StringBean bean = new StringBean(url1);
 		ridget.bindToModel(bean, StringBean.PROP_VALUE);
 
 		assertNull(ridget.getUrl());
 
 		ridget.updateFromModel();
 
 		assertEquals(url1, bean.getValue());
 		assertEquals(url1, ridget.getUrl());
 
 		bean.setValue(url2);
 
 		assertEquals(url2, bean.getValue());
 		assertEquals(url1, ridget.getUrl());
 
 		ridget.updateFromModel();
 
 		assertEquals(url2, bean.getValue());
 		assertEquals(url2, ridget.getUrl());
 
 		ridget.setUrl(url1);
 
 		assertEquals(url1, bean.getValue());
 		assertEquals(url1, ridget.getUrl());
 	}
 
 	public void testSetText() {
 		final IBrowserRidget ridget = getRidget();
 		final String text = "<html><body><h1>Riena</h1></body></html>";
 
 		ridget.setText(text);
 
 		assertEquals(text, ridget.getText());
 
 		ridget.setText("");
 
 		assertEquals("", ridget.getText());
 
 		ridget.setText(null);
 
 		assertEquals(null, ridget.getText());
 	}
 
	public void testSetTextClearsUrl() {
 		final IBrowserRidget ridget = getRidget();
 		final String text = "<html><body><p>riena</p></body></html>";
 
 		ridget.setUrl("http://eclipse.org");
 		ridget.setText(text);
 
 		assertEquals(null, ridget.getUrl());
 		assertEquals(text, ridget.getText());
 	}
 
 	public void testSetTextOnOutputOnly() {
 		final IBrowserRidget ridget = getRidget();
 		final Browser control = getWidget();
 
 		assertNull(ridget.getText());
 
 		// allow ridget.setText() on output only
 		ridget.setOutputOnly(true);
 		final String text = "<hmtl><body><h2>Riena</h2></body></html>";
 		ridget.setText(text);
 		UITestHelper.readAndDispatch(control);
 
 		assertEquals(text, ridget.getText());
 		// browser may add line breaks - just check if 'Riena' is in the output
 		assertTrue("control.text:" + control.getText(), control.getText().contains("Riena"));
 	}
 
 	public void testSetUrl() {
 		final IBrowserRidget ridget = getRidget();
 
 		ridget.setUrl("http://www.redview.org");
 
 		assertEquals("http://www.redview.org", ridget.getUrl());
 		// control.getUrl() is not reliable because of timing + network acccess
 		// so I'm not testing that...
 
 		ridget.setUrl("b o g u s");
 
 		assertEquals("b o g u s", ridget.getUrl());
 
 		ridget.setUrl("");
 
 		assertEquals("", ridget.getUrl());
 
 		ridget.setUrl(null);
 
 		assertEquals(null, ridget.getUrl());
 
 		ridget.setUrl("about:blank");
 
 		assertEquals("about:blank", ridget.getUrl());
 	}
 
 	public void testSetUrlClearsText() {
 		final IBrowserRidget ridget = getRidget();
 
 		ridget.setText("riena");
 		ridget.setUrl("http://eclipse.org");
 
 		assertEquals("http://eclipse.org", ridget.getUrl());
 		assertEquals(null, ridget.getText());
 	}
 
 	public void testSetUrlOnOutputOnly() {
 		final IBrowserRidget ridget = getRidget();
 
 		assertNull(ridget.getUrl());
 
 		// allow ridget.setUrl() on output only
 		ridget.setOutputOnly(true);
 		ridget.setUrl("http://www.redview.org");
 		// ridget.addPropertyChangeListener(new PropertyChangeListener() {
 		//	public void propertyChange(PropertyChangeEvent evt) {
 		//		System.out.println(evt.getPropertyName() + " " + evt.getNewValue());
 		//	}
 		// });
 
 		assertEquals("http://www.redview.org", ridget.getUrl());
 
 		// disallow widget.setUrl() on output only - can't test this because
 		// widget.getUrl() is not reliable because of timing + network access
 	}
 
	public void testSettersFireUrlEvents() {
 		final IBrowserRidget ridget = getRidget();
 		final String newValue = "http://www.redview.org";
 		final String oldValue = ridget.getUrl();
 
 		assertFalse(newValue.equals(ridget.getUrl()));
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "urlInternal", oldValue, newValue),
 				new PropertyChangeEvent(ridget, IBrowserRidget.PROPERTY_URL, oldValue, newValue));
 		ridget.setUrl(newValue);
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		ridget.setUrl(newValue);
 		verifyPropertyChangeEvents();
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "urlInternal", newValue, null),
 				new PropertyChangeEvent(ridget, IBrowserRidget.PROPERTY_URL, newValue, null));
 		ridget.setText("<html><body><h1>h1</h1></body></html>");
 		verifyPropertyChangeEvents();
 
 		expectNoPropertyChangeEvent();
 		ridget.setText("<html><body><h1>h1</h1></body></html>");
 		verifyPropertyChangeEvents();
 	}
 
 	public void testApplyTextOnRebind() throws Exception {
 		final IBrowserRidget ridget = getRidget();
 		final Browser control1 = getWidget();
 
 		final String text = "<html><body><h1>Riena</h1></body></html>";
 		ridget.setText(text);
 		UITestHelper.readAndDispatch(control1);
 
 		retry(new Runnable() {
 			public void run() {
 				// browser may add line breaks - just check if 'Riena' is in the output
 				assertTrue("control1.text:" + control1.getText(), control1.getText().contains("Riena"));
 			}
 		}, control1, 3);
 
 		final Browser control2 = createWidget(getShell());
 		ridget.setUIControl(control2);
 		UITestHelper.readAndDispatch(control2);
 
 		retry(new Runnable() {
 			public void run() {
 				assertTrue("control2.text:" + control2.getText(), control2.getText().contains("Riena"));
 			}
 		}, control2, 3);
 	}
 
 	/**
 	 * As per Bug 338602
 	 */
 	public void testAddLocationListener() {
 		final IBrowserRidget ridget = getRidget();
 		final Browser control = getWidget();
 		final LocationListener intLocLi = ReflectionUtils.getHidden(ridget, "internalLocationListener");
 		intLocLi.changing(createLocationEvent(control, ""));
 
 		final FTLocationChangingListener changingListener = new FTLocationChangingListener();
 		ridget.addLocationListener(changingListener);
 		ridget.addLocationListener(changingListener);
 		final FTLocationChangedListener changedListener = new FTLocationChangedListener();
 		ridget.addLocationListener(changedListener);
 		ridget.addLocationListener(changedListener);
 		intLocLi.changing(createLocationEvent(control, "http://www.eclipse.org"));
 
 		assertEquals(1, changingListener.getCount());
 		assertEquals("http://www.eclipse.org", changingListener.getLocation());
 		assertEquals(0, changedListener.getCount());
 
 		intLocLi.changed(createLocationEvent(control, "http://www.eclipse.org"));
 
 		assertEquals(1, changingListener.getCount());
 		assertEquals(1, changedListener.getCount());
 		assertEquals("http://www.eclipse.org", changedListener.getLocation());
 
 		ridget.removeLocationListener(changingListener);
 		intLocLi.changing(createLocationEvent(control, "http://www.eclipse.org/riena"));
 
 		assertEquals(1, changingListener.getCount());
 		assertEquals(1, changedListener.getCount());
 
 		ridget.removeLocationListener(changedListener);
 		intLocLi.changed(createLocationEvent(control, "http://www.eclipse.org/riena"));
 
 		assertEquals(1, changingListener.getCount());
 		assertEquals(1, changedListener.getCount());
 	}
 
 	/**
 	 * As per Bug 338602
 	 */
	public void testSettersDoNotNotifyLocationListeners() {
 		final IBrowserRidget ridget = getRidget();
 		final Browser control = getWidget();
 		final LocationListener intLocLi = ReflectionUtils.getHidden(ridget, "internalLocationListener");
 		intLocLi.changing(createLocationEvent(control, ""));
 
 		final FTLocationChangingListener listener = new FTLocationChangingListener();
 		ridget.addLocationListener(listener);
 
 		assertEquals(0, listener.getCount());
 
 		ridget.setUrl("http://www.eclipse.org");
 
 		assertEquals(0, listener.getCount());
 
 		ridget.setText("<html><body><h1>Hello</h1></body></html>");
 
 		assertEquals(0, listener.getCount());
 	}
 
 	/**
 	 * As per Bug 338602
 	 */
 	public void testBlockFromLocationListener() {
 		final IBrowserRidget ridget = getRidget();
 		final Browser control = getWidget();
 		final LocationListener intLocLi = ReflectionUtils.getHidden(ridget, "internalLocationListener");
 		intLocLi.changing(createLocationEvent(control, ""));
 
 		final FTLocationChangingListener blockListener = new FTLocationChangingListener() {
 			@Override
 			public boolean locationChanging(final LocationEvent event) {
 				super.locationChanging(event);
 				return false;
 			}
 		};
 		ridget.addLocationListener(blockListener);
 		final FTLocationChangingListener listener = new FTLocationChangingListener();
 		ridget.addLocationListener(listener);
 
 		org.eclipse.swt.browser.LocationEvent event = createLocationEvent(control, "http://www.eclipse.org");
 		intLocLi.changing(event);
 
 		assertEquals(1, blockListener.getCount());
 		assertEquals(1, listener.getCount());
 		assertFalse(listener.isAllowed());
 		assertFalse(event.doit);
 
 		ridget.removeLocationListener(blockListener);
 		event = createLocationEvent(control, "http://www.eclipse.org");
 		intLocLi.changing(event);
 
 		assertEquals(1, blockListener.getCount());
 		assertEquals(2, listener.getCount());
 		assertTrue(listener.isAllowed());
 		assertTrue(event.doit);
 	}
 
 	public void testBlockWhenOutputOnly() {
 		final IBrowserRidget ridget = getRidget();
 		final Browser control = getWidget();
 		final LocationListener intLocLi = ReflectionUtils.getHidden(ridget, "internalLocationListener");
 		intLocLi.changing(createLocationEvent(control, ""));
 
 		final FTLocationChangingListener listener = new FTLocationChangingListener();
 		ridget.addLocationListener(listener);
 
 		ridget.setOutputOnly(true);
 		org.eclipse.swt.browser.LocationEvent event = createLocationEvent(control, "http://www.eclipse.org");
 		intLocLi.changing(event);
 
 		assertFalse(event.doit);
 		assertEquals(0, listener.getCount());
 
 		ridget.setOutputOnly(false);
 		event = createLocationEvent(control, "http://www.eclipse.org");
 		intLocLi.changing(event);
 
 		assertTrue(event.doit);
 		assertEquals(1, listener.getCount());
 	}
 
 	// helping methods
 	//////////////////
 
 	private org.eclipse.swt.browser.LocationEvent createLocationEvent(final Browser control, final String location) {
 		final org.eclipse.swt.browser.LocationEvent event = new org.eclipse.swt.browser.LocationEvent(control);
 		event.location = location;
 		event.top = true;
 		event.doit = true;
 		return event;
 	}
 
 	/**
 	 * Execute the 'closure' op up to {@code tries}-times and process the ui
 	 * queue between tries.
 	 */
 	private void retry(final Runnable op, final Control withControl, int tries) throws Exception {
 		while (tries > 0) {
 			try {
 				op.run();
 				tries = 0;
 			} catch (final Exception exc) {
 				tries--;
 				if (tries > 0) {
 					Thread.sleep(500);
 					UITestHelper.readAndDispatch(withControl);
 				} else {
 					throw exc;
 				}
 			} catch (final Error error) {
 				tries--;
 				if (tries > 0) {
 					Thread.sleep(500);
 					UITestHelper.readAndDispatch(withControl);
 				} else {
 					throw error;
 				}
 			}
 		}
 	}
 
 	// helping classes
 	//////////////////
 
 	private static class FTLocationChangingListener implements ILocationListener {
 
 		private int countChanging;
 		private LocationEvent event;
 
 		public boolean isAllowed() {
 			return event.isAllowed();
 		}
 
 		public int getCount() {
 			return countChanging;
 		}
 
 		public String getLocation() {
 			return event.getLocation();
 		}
 
 		public boolean locationChanging(final LocationEvent event) {
 			countChanging++;
 			this.event = event;
 			return true;
 		}
 
 		public void locationChanged(final LocationEvent event) {
 			// unused
 		}
 	}
 
 	private static class FTLocationChangedListener implements ILocationListener {
 
 		private int countChanged;
 		private LocationEvent event;
 
 		public int getCount() {
 			return countChanged;
 		}
 
 		public String getLocation() {
 			return event.getLocation();
 		}
 
 		public boolean locationChanging(final LocationEvent event) {
 			// unused
 			return true;
 		}
 
 		public void locationChanged(final LocationEvent event) {
 			countChanged++;
 			this.event = event;
 		}
 	}
 
 }
