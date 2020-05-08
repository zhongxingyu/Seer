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
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.ui.ridgets.ILabelRidget;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.DefaultSwtControlRidgetMapper;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 /**
  * Tests for the {@link LabelRidget}.
  */
 public class LabelRidgetTest extends AbstractSWTRidgetTest {
 
 	private final static String PLUGIN_ID = "org.eclipse.riena.tests:";
 	private final static String ICON_ECLIPSE = PLUGIN_ID + "/icons/eclipse.gif";
 
 	private final static String LABEL = "testlabel";
 	private final static String LABEL2 = "testlabel2";
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		getRidget().setText(LABEL);
 	}
 
 	@Override
 	protected ILabelRidget createRidget() {
 		return new LabelRidget();
 	}
 
 	@Override
 	protected Label createWidget(Composite parent) {
 		return new Label(parent, SWT.NONE);
 	}
 
 	@Override
 	protected ILabelRidget getRidget() {
 		return (ILabelRidget) super.getRidget();
 	}
 
 	@Override
 	protected Label getWidget() {
 		return (Label) super.getWidget();
 	}
 
 	public void testRidgetMapping() {
 		DefaultSwtControlRidgetMapper mapper = new DefaultSwtControlRidgetMapper();
 		assertSame(LabelRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	/**
 	 * Test method get/setIcon().
 	 */
 	public final void testSetIcon() {
 
 		ILabelRidget ridget = getRidget();
 		Label control = (Label) ridget.getUIControl();
 
 		ridget.setIcon(ICON_ECLIPSE);
 
 		assertEquals(ICON_ECLIPSE, ridget.getIcon());
 		assertNotNull(control.getImage());
 
 		ridget.setIcon(null);
 
 		assertNull(ridget.getIcon());
 		assertNull(control.getImage());
 
 		Label label = createWidget(getShell());
 		Image labelImage = label.getDisplay().getSystemImage(SWT.ICON_INFORMATION);
 		label.setImage(labelImage);
 		ILabelRidget labelRidget = createRidget();
 		// binding doesn't remove image of label, because the icon of the ridget is null and the method #setIcon wasn't called yet.
 		labelRidget.setUIControl(label);
 		assertSame(labelImage, label.getImage());
 
 		labelRidget.setIcon(null);
 		assertNull(labelRidget.getIcon());
 		assertNull(label.getImage());
 
 		labelRidget.setIcon(ICON_ECLIPSE);
 		assertEquals(ICON_ECLIPSE, labelRidget.getIcon());
 		assertNotNull(label.getImage());
 		assertNotSame(labelImage, label.getImage());
 
 		label = createWidget(getShell());
 		label.setImage(labelImage);
 		labelRidget = createRidget();
 		labelRidget.setIcon(ICON_ECLIPSE);
 		// binding replaces image of label, because the icon of the ridget is not null.
 		labelRidget.setUIControl(label);
 		assertNotNull(label.getImage());
 		assertNotSame(labelImage, label.getImage());
 
 	}
 
 	/**
 	 * Test method get/setText.
 	 */
 	public final void testSetText() throws Exception {
 		ILabelRidget ridget = getRidget();
 		Label control = (Label) ridget.getUIControl();
 
 		ridget.setText(LABEL2);
 
 		assertEquals(LABEL2, ridget.getText());
 		assertEquals(LABEL2, control.getText());
 
 		ridget.setText("");
 
 		assertEquals("", ridget.getText());
 		assertEquals("", control.getText());
 
 		try {
 			ridget.setText(null);
 			fail();
 		} catch (IllegalArgumentException iae) {
 			ok();
 		}
 	}
 
 	/**
 	 * Test method updateFromModel().
 	 */
 	public void testUpdateFromModel() throws Exception {
 		ILabelRidget ridget = getRidget();
 
 		TestBean bean = new TestBean();
 		bean.setProperty("NewLabel");
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		assertEquals("NewLabel", bean.getProperty());
 		assertEquals(LABEL, ridget.getText());
 		assertEquals(LABEL, ((Label) ridget.getUIControl()).getText());
 
 		ridget.updateFromModel();
 
 		assertEquals("NewLabel", bean.getProperty());
 		assertEquals("NewLabel", ridget.getText());
 		assertEquals("NewLabel", ((Label) ridget.getUIControl()).getText());
 	}
 
 	/**
 	 * For LabelRidgets, ensure that updates are made to the model from the
 	 * ridget.
 	 */
 	public void testUpdateFromRidget() {
 		ILabelRidget ridget = getRidget();
 		Label control = (Label) ridget.getUIControl();
 
 		TestBean bean = new TestBean();
 		bean.setProperty("NewLabel");
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.updateFromModel();
 
 		assertEquals("NewLabel", bean.getProperty());
 		assertEquals("NewLabel", ridget.getText());
 		assertEquals("NewLabel", control.getText());
 
 		ridget.setText(LABEL);
 
 		assertEquals(LABEL, bean.getProperty());
 		assertEquals(LABEL, ridget.getText());
 		assertEquals(LABEL, control.getText());
 	}
 
 	/**
 	 * For LabelRidgets, ensure that <b>no</b> updates are made to the model
 	 * from the control.
 	 */
 	public void testUpdateFromControl() {
 		ILabelRidget ridget = getRidget();
 		Label control = (Label) ridget.getUIControl();
 
 		TestBean bean = new TestBean();
 		bean.setProperty("NewLabel");
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.updateFromModel();
 
 		assertEquals("NewLabel", bean.getProperty());
 		assertEquals("NewLabel", ridget.getText());
 		assertEquals("NewLabel", control.getText());
 
 		control.setText(LABEL);
 
 		assertEquals("NewLabel", bean.getProperty());
 		assertEquals("NewLabel", ridget.getText());
 		assertEquals(LABEL, control.getText());
 	}
 
 	public void testUpdateFromRidgetOnRebind() throws Exception {
 		ILabelRidget ridget = getRidget();
 		Label control = (Label) ridget.getUIControl();
 
 		TestBean bean = new TestBean();
 		bean.setProperty("NewLabel");
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.updateFromModel();
 
 		assertEquals("NewLabel", ridget.getText());
 		assertEquals("NewLabel", control.getText());
 
 		ridget.setUIControl(null);
 
 		assertEquals("NewLabel", ridget.getText());
 		assertEquals("NewLabel", control.getText());
 
 		ridget.setText(LABEL);
 
 		assertEquals(LABEL, ridget.getText());
 		assertEquals("NewLabel", control.getText());
 
 		control = new Label(getShell(), SWT.NONE);
 		ridget.setUIControl(control);
 
 		assertEquals(LABEL, ridget.getText());
 		assertEquals(LABEL, control.getText());
 
 	}
 
 	/**
 	 * Test for problem report #536.
 	 */
 	public void testDontReadValueInConstructor() throws Exception {
 		ILabelRidget ridget = getRidget();
 
 		IObservableValue observableValue = new AbstractObservableValue() {
 			@Override
 			protected Object doGetValue() {
 				return "TestText";
 			}
 
 			public Object getValueType() {
 				return String.class;
 			}
 		};
 		ridget.bindToModel(observableValue);
 
 		assertEquals(LABEL, ridget.getText());
 
 		ridget.updateFromModel();
 
 		assertEquals("TestText", ridget.getText());
 	}
 
 	/**
 	 * Test method get/setIconLocation().
 	 */
 	public void testSetIconLocation() throws Exception {
 		ILabelRidget ridget = getRidget();
 		Label control = (Label) ridget.getUIControl();
 
 		assertNull(ridget.getIconLocation());
 		assertNull(control.getImage());
 
 		URL url = new URL("http://www.compeople.de/assets/compeople-logo.gif");
 		ridget.setIconLocation(url);
 
 		assertEquals(url, ridget.getIconLocation());
 		assertNotNull(control.getImage());
 
 		ridget.setIconLocation(null);
 
 		assertNull(ridget.getIconLocation());
 		assertNull(control.getImage());
 	}
 
 	/**
	 * Test deprecated method getRidgetObservable(). When this method is removed
	 * the test may be removed too.
 	 */
	@SuppressWarnings("deprecation")
	public void testGetRidgetObservable() throws Exception {
 		ILabelRidget ridget = getRidget();
 		IObservableValue ridgetObservable = ridget.getRidgetObservable();
 
 		assertNotNull(ridgetObservable);
 
 		ridget.setText("huhu");
 
 		assertEquals("huhu", ridgetObservable.getValue());
 
 		TestBean bean = new TestBean();
 		bean.setProperty("NewLabel");
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		assertEquals("huhu", ridgetObservable.getValue());
 
 		ridget.updateFromModel();
 
 		assertEquals("NewLabel", ridgetObservable.getValue());
 	}
 
 	/**
	 * Test method getValueType(). When the deprecated method
	 * getRidgetObservable() is removed this test may be removed too.
 	 */
	@SuppressWarnings("deprecation")
 	public void testGetValueType() throws Exception {
 		ILabelRidget ridget = getRidget();
 		assertEquals(String.class, ridget.getRidgetObservable().getValueType());
 	}
 
 	/**
 	 * Test method setUIControl().
 	 */
 	public void testSetUIControl() throws Exception {
 		ILabelRidget ridget = getRidget();
 		Label control = getWidget();
 
 		assertEquals(LABEL, ridget.getText());
 		assertEquals(LABEL, control.getText());
 
 		ridget.setUIControl(null);
 
 		assertEquals(LABEL, ridget.getText());
 		assertEquals(LABEL, control.getText());
 
 		ridget.setText(LABEL2);
 		ridget.setUIControl(control);
 
 		assertEquals(LABEL2, ridget.getText());
 		assertEquals(LABEL2, control.getText());
 	}
 
 	/**
 	 * Tests the method {@code initText}
 	 */
 	public void testInitText() {
 
 		ILabelRidget ridget = getRidget();
 		Label control = (Label) ridget.getUIControl();
 
 		ReflectionUtils.setHidden(ridget, "textAlreadyInitialized", false);
 		ReflectionUtils.setHidden(ridget, "text", null);
 		control.setText("Hello!");
 
 		ReflectionUtils.invokeHidden(ridget, "initText", new Object[] {});
 		assertEquals("Hello!", ridget.getText());
 		assertEquals("Hello!", control.getText());
 		assertTrue((Boolean) ReflectionUtils.getHidden(ridget, "textAlreadyInitialized"));
 
 		control.setText("World");
 		ReflectionUtils.invokeHidden(ridget, "initText", new Object[] {});
 		assertEquals("Hello!", ridget.getText());
 		assertEquals("World", control.getText());
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code hasChanged.}
 	 * 
 	 * @throws MalformedURLException
 	 */
 	public void testHasChanged() throws MalformedURLException {
 
 		ILabelRidget ridget = getRidget();
 
 		URL url1 = new URL("file:/a");
 		URL url2 = new URL("file:/b");
 		boolean ret = ReflectionUtils.invokeHidden(ridget, "hasChanged", url1, url2);
 		assertTrue(ret);
 		ret = ReflectionUtils.invokeHidden(ridget, "hasChanged", url1, url1);
 		assertFalse(ret);
 		ret = ReflectionUtils.invokeHidden(ridget, "hasChanged", url1, (URL) null);
 		assertTrue(ret);
 		ret = ReflectionUtils.invokeHidden(ridget, "hasChanged", (URL) null, url2);
 		assertTrue(ret);
 		ret = ReflectionUtils.invokeHidden(ridget, "hasChanged", (URL) null, (URL) null);
 		assertFalse(ret);
 
 	}
 
 }
