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
 
 import static org.eclipse.riena.internal.ui.swt.utils.TestUtils.*;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.eclipse.core.databinding.conversion.Converter;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.beans.common.DateBean;
 import org.eclipse.riena.beans.common.StringBean;
 import org.eclipse.riena.beans.common.TestBean;
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.internal.ui.swt.utils.TestUtils;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.ErrorMessageMarker;
 import org.eclipse.riena.ui.core.marker.IMessageMarker;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.core.marker.ValidationTime;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.databinding.DateToStringConverter;
 import org.eclipse.riena.ui.ridgets.databinding.StringToDateConverter;
 import org.eclipse.riena.ui.ridgets.databinding.StringToLowerCaseConverter;
 import org.eclipse.riena.ui.ridgets.databinding.StringToUpperCaseConverter;
 import org.eclipse.riena.ui.ridgets.marker.TooltipMessageMarkerViewer;
 import org.eclipse.riena.ui.ridgets.marker.ValidationMessageMarker;
 import org.eclipse.riena.ui.ridgets.swt.MarkerSupport;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 import org.eclipse.riena.ui.ridgets.validation.MaxLength;
 import org.eclipse.riena.ui.ridgets.validation.MinLength;
 import org.eclipse.riena.ui.ridgets.validation.ValidCharacters;
 import org.eclipse.riena.ui.ridgets.validation.ValidDecimal;
 import org.eclipse.riena.ui.ridgets.validation.ValidEmailAddress;
 import org.eclipse.riena.ui.ridgets.validation.ValidIntermediateDate;
 import org.eclipse.riena.ui.ridgets.validation.ValidationFailure;
 import org.eclipse.riena.ui.ridgets.validation.ValidationRuleStatus;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Tests for the class {@link TextRidget}.
  * <p>
  * <i>No extensions are planed for this test class.</i>
  * 
  * @see TextRidgetTest
  */
 public final class TextRidgetTest2 extends AbstractSWTRidgetTest {
 
 	private final static String TEXT_ONE = "TestText1";
 	private final static String TEXT_TWO = "TestText2";
 	private TestBean bean;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		bean = new TestBean();
 		final Shell shell = getShell();
 		shell.layout();
 	}
 
 	@Override
 	protected IRidget createRidget() {
 		return new TextRidget();
 	}
 
 	@Override
 	protected Control createWidget(final Composite parent) {
 		return new Text(getShell(), SWT.BORDER);
 	}
 
 	@Override
 	protected ITextRidget getRidget() {
 		return (ITextRidget) super.getRidget();
 	}
 
 	@Override
 	protected Text getWidget() {
 		return (Text) super.getWidget();
 	}
 
 	public void testRidgetMapping() {
 		final SwtControlRidgetMapper mapper = SwtControlRidgetMapper.getInstance();
 		assertSame(TextRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	public void testInitialValueFromModel() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		bean.setProperty(TEXT_TWO);
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		assertEquals("", control.getText());
 		assertEquals("", ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "", TEXT_TWO),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "", TEXT_TWO));
 
 		ridget.updateFromModel();
 
 		verifyPropertyChangeEvents();
 		assertEquals(TEXT_TWO, control.getText());
 		assertEquals(TEXT_TWO, ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 	}
 
 	public void testUpdateFromModel() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		assertEquals("", control.getText());
 		assertEquals("", ridget.getText());
 		assertEquals(null, bean.getProperty());
 
 		bean.setProperty(TEXT_ONE);
 		ridget.updateFromModel();
 
 		assertEquals(TEXT_ONE, control.getText());
 		assertEquals(TEXT_ONE, ridget.getText());
 		assertEquals(TEXT_ONE, bean.getProperty());
 
 		expectNoPropertyChangeEvent();
 
 		bean.setProperty(TEXT_TWO);
 
 		verifyPropertyChangeEvents();
 		assertEquals(TEXT_ONE, control.getText());
 		assertEquals(TEXT_ONE, ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", TEXT_ONE, TEXT_TWO),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, TEXT_ONE, TEXT_TWO));
 
 		ridget.updateFromModel();
 
 		verifyPropertyChangeEvents();
 		assertEquals(TEXT_TWO, control.getText());
 		assertEquals(TEXT_TWO, ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 	}
 
 	public void testUpdateFromModelDirectWriting() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.setDirectWriting(true);
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		bean.setProperty(TEXT_ONE);
 		ridget.updateFromModel();
 
 		assertEquals(TEXT_ONE, control.getText());
 		assertEquals(TEXT_ONE, ridget.getText());
 		assertEquals(TEXT_ONE, bean.getProperty());
 
 		bean.setProperty(TEXT_TWO);
 		ridget.updateFromModel();
 
 		assertEquals(TEXT_TWO, control.getText());
 		assertEquals(TEXT_TWO, ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 	}
 
 	public void testUpdateFromControlUserInput() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 		final Display display = control.getDisplay();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		UITestHelper.sendString(display, "test");
 
 		assertEquals("test", control.getText());
 		assertEquals("", ridget.getText());
 		assertEquals(null, bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "", "test"),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "", "test"));
 
 		UITestHelper.sendString(display, "\r");
 		UITestHelper.readAndDispatch(control);
 
 		verifyPropertyChangeEvents();
 		assertEquals("test", control.getText());
 		assertEquals("test", ridget.getText());
 		assertEquals("test", bean.getProperty());
 
 		expectNoPropertyChangeEvent();
 
 		UITestHelper.sendString(display, "2");
 
 		verifyPropertyChangeEvents();
 		assertEquals("test2", control.getText());
 		assertEquals("test", ridget.getText());
 		assertEquals("test", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "test", "test2"),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "test", "test2"));
 
 		UITestHelper.sendString(display, "\t");
 
 		verifyPropertyChangeEvents();
 		assertEquals("test2", control.getText());
 		assertEquals("test2", ridget.getText());
 		assertEquals("test2", bean.getProperty());
 	}
 
 	public void testUpdateFromControlUserInputDirectWriting() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.setDirectWriting(true);
 
 		final Display display = control.getDisplay();
 		UITestHelper.sendString(display, "t");
 
 		assertEquals("t", control.getText());
 		assertEquals("t", ridget.getText());
 		assertEquals("t", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "t", "te"), new PropertyChangeEvent(
 				ridget, ITextRidget.PROPERTY_TEXT, "t", "te"));
 
 		UITestHelper.sendString(display, "e");
 
 		verifyPropertyChangeEvents();
 		assertEquals("te", control.getText());
 		assertEquals("te", ridget.getText());
 		assertEquals("te", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "te", "tes"),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "te", "tes"));
 
 		UITestHelper.sendString(display, "s");
 
 		verifyPropertyChangeEvents();
 		assertEquals("tes", control.getText());
 		assertEquals("tes", ridget.getText());
 		assertEquals("tes", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "tes", "test"),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "tes", "test"));
 
 		UITestHelper.sendString(display, "t");
 
 		verifyPropertyChangeEvents();
 		assertEquals("test", control.getText());
 		assertEquals("test", ridget.getText());
 		assertEquals("test", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "test", "tet"),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "test", "tet"));
 
 		UITestHelper.sendKeyAction(display, SWT.ARROW_LEFT);
 		UITestHelper.sendString(display, "\b");
 
 		verifyPropertyChangeEvents();
 		assertEquals("tet", control.getText());
 		assertEquals("tet", ridget.getText());
 		assertEquals("tet", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "tet", "te"),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "tet", "te"));
 
 		UITestHelper.sendString(display, String.valueOf(SWT.DEL));
 
 		verifyPropertyChangeEvents();
 		assertEquals("te", control.getText());
 		assertEquals("te", ridget.getText());
 		assertEquals("te", bean.getProperty());
 
 		expectNoPropertyChangeEvent();
 
 		bean.setProperty("Test");
 
 		verifyPropertyChangeEvents();
 		assertEquals("te", control.getText());
 		assertEquals("te", ridget.getText());
 		assertEquals("Test", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "te", "t"), new PropertyChangeEvent(
 				ridget, ITextRidget.PROPERTY_TEXT, "te", "t"));
 
 		UITestHelper.sendString(display, "\b");
 
 		verifyPropertyChangeEvents();
 		assertEquals("t", control.getText());
 		assertEquals("t", ridget.getText());
 		assertEquals("t", bean.getProperty());
 	}
 
 	public void testUpdateFromControlMethodCall() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		((Text) ridget.getUIControl()).setText("test");
 
 		assertEquals("test", control.getText());
 		assertEquals("", ridget.getText());
 		assertEquals(null, bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "", "test"),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "", "test"));
 
 		UITestHelper.sendString(control.getDisplay(), "\r");
 
 		verifyPropertyChangeEvents();
 		assertEquals("test", control.getText());
 		assertEquals("test", ridget.getText());
 		assertEquals("test", bean.getProperty());
 
 		expectNoPropertyChangeEvent();
 
 		((Text) ridget.getUIControl()).setText("TEST2");
 
 		verifyPropertyChangeEvents();
 		assertEquals("TEST2", control.getText());
 		assertEquals("test", ridget.getText());
 		assertEquals("test", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "test", "TEST2"),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "test", "TEST2"));
 
 		UITestHelper.sendString(control.getDisplay(), "\t");
 
 		verifyPropertyChangeEvents();
 		assertEquals("TEST2", control.getText());
 		assertEquals("TEST2", ridget.getText());
 		assertEquals("TEST2", bean.getProperty());
 	}
 
 	public void testUpdateFromControlMethodCallDirectWriting() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		ridget.setDirectWriting(true);
 
 		((Text) ridget.getUIControl()).setText("t");
 
 		assertEquals("t", control.getText());
 		assertEquals("t", ridget.getText());
 		assertEquals("t", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "t", "Test"),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "t", "Test"));
 
 		((Text) ridget.getUIControl()).setText("Test");
 
 		verifyPropertyChangeEvents();
 		assertEquals("Test", control.getText());
 		assertEquals("Test", ridget.getText());
 		assertEquals("Test", bean.getProperty());
 
 		expectNoPropertyChangeEvent();
 
 		bean.setProperty("Toast");
 
 		verifyPropertyChangeEvents();
 		assertEquals("Test", control.getText());
 		assertEquals("Test", ridget.getText());
 		assertEquals("Toast", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", "Test", "Test2"),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "Test", "Test2"));
 
 		UITestHelper.sendKeyAction(control.getDisplay(), SWT.END);
 		UITestHelper.sendString(control.getDisplay(), "2");
 
 		verifyPropertyChangeEvents();
 		assertEquals("Test2", control.getText());
 		assertEquals("Test2", ridget.getText());
 		assertEquals("Test2", bean.getProperty());
 	}
 
 	public void testUpdateFromRidget() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		ridget.setText(TEXT_TWO);
 
 		assertEquals(TEXT_TWO, control.getText());
 		assertEquals(TEXT_TWO, ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 
 		expectNoPropertyChangeEvent();
 
 		control.selectAll();
 		UITestHelper.sendString(control.getDisplay(), "12");
 		bean.setProperty("Bean34");
 
 		verifyPropertyChangeEvents();
 		assertEquals("12", control.getText());
 		assertEquals(TEXT_TWO, ridget.getText());
 		assertEquals("Bean34", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "textInternal", TEXT_TWO, TEXT_ONE),
 				new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, TEXT_TWO, TEXT_ONE));
 
 		ridget.setText(TEXT_ONE);
 
 		verifyPropertyChangeEvents();
 		assertEquals(TEXT_ONE, control.getText());
 		assertEquals(TEXT_ONE, ridget.getText());
 		assertEquals(TEXT_ONE, bean.getProperty());
 	}
 
 	public void testUpdateFromRidgetOnRebind() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		bean.setProperty(TEXT_ONE);
 		ridget.updateFromModel();
 
 		assertEquals(TEXT_ONE, control.getText());
 		assertEquals(TEXT_ONE, ridget.getText());
 		assertEquals(TEXT_ONE, bean.getProperty());
 
 		// unbind, e.g. when the view is used by another controller
 		ridget.setUIControl(null);
 
 		control.selectAll();
 		UITestHelper.sendString(control.getDisplay(), "xy");
 
 		assertEquals("xy", control.getText());
 		assertEquals(TEXT_ONE, ridget.getText());
 		assertEquals(TEXT_ONE, bean.getProperty());
 
 		// rebind
 		ridget.setUIControl(control);
 
 		assertEquals(TEXT_ONE, control.getText());
 		assertEquals(TEXT_ONE, ridget.getText());
 		assertEquals(TEXT_ONE, bean.getProperty());
 
 		// unbind again
 		ridget.setUIControl(null);
 
 		bean.setProperty(TEXT_TWO);
 		ridget.updateFromModel();
 
 		assertEquals(TEXT_ONE, control.getText());
 		assertEquals(TEXT_TWO, ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 
 		// rebind
 		ridget.setUIControl(control);
 
 		assertEquals(TEXT_TWO, control.getText());
 		assertEquals(TEXT_TWO, ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 	}
 
 	public void testCaretPositionAfterBind() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		bean.setProperty(TEXT_ONE);
 		ridget.updateFromModel();
 
 		assertEquals(TEXT_ONE, control.getText());
 		assertEquals(0, control.getCaretPosition());
 
 		UITestHelper.sendKeyAction(control.getDisplay(), SWT.RIGHT);
 		UITestHelper.sendKeyAction(control.getDisplay(), SWT.RIGHT);
 		bean.setProperty(TEXT_TWO);
 		ridget.updateFromModel();
 
 		assertEquals(0, control.getCaretPosition());
 
 		final Text text = new Text(getShell(), SWT.MULTI);
 		text.setText("foo");
 		text.setSelection(2, 2);
 
 		ridget.setUIControl(text);
 
 		assertEquals(TEXT_TWO, text.getText());
 		assertEquals(0, text.getCaretPosition());
 	}
 
 	public void testValidationOnUpdateToModel() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		ridget.addValidationRule(new MinLength(3), ValidationTime.ON_UPDATE_TO_MODEL);
 
 		bean.setProperty(TEXT_ONE);
 		ridget.updateFromModel();
 
 		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals(TEXT_ONE, ridget.getText());
 
 		control.selectAll();
 		UITestHelper.sendString(control.getDisplay(), "xy\t");
 
 		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals("xy", ridget.getText());
 
 		control.setFocus();
 		UITestHelper.sendKeyAction(control.getDisplay(), SWT.END);
 		UITestHelper.sendString(control.getDisplay(), "z");
 
 		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 
 		UITestHelper.sendString(control.getDisplay(), "\r");
 
 		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals("xyz", ridget.getText());
 	}
 
 	public void testValidationOnUpdateToModelWithValidChars() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		ridget.addValidationRule(new ValidCharacters("xy"), ValidationTime.ON_UPDATE_TO_MODEL);
 
 		bean.setProperty("xyxxy");
 		ridget.updateFromModel();
 
 		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals("xyxxy", ridget.getText());
 
 		control.selectAll();
 		UITestHelper.sendKeyAction(control.getDisplay(), SWT.END);
 		UITestHelper.sendString(control.getDisplay(), "h\t");
 
 		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals("xyxxyh", ridget.getText());
 
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), "x\t");
 		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals("x", ridget.getText());
 	}
 
 	public void testValidationOnUpdateToModelWithMaxLength() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		ridget.addValidationRule(new MaxLength(5), ValidationTime.ON_UPDATE_TO_MODEL);
 
 		bean.setProperty("12345");
 		ridget.updateFromModel();
 
 		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals("12345", ridget.getText());
 
 		control.selectAll();
 		UITestHelper.sendKeyAction(control.getDisplay(), SWT.END);
 		UITestHelper.sendString(control.getDisplay(), "6\t");
 
 		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals("123456", ridget.getText());
 
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), "1\t");
 		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals("1", ridget.getText());
 	}
 
 	public void testValidationOnUpdateToModelWithValidDecimal() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		ridget.addValidationRule(new ValidDecimal(), ValidationTime.ON_UPDATE_TO_MODEL);
 
 		bean.setProperty(localize("20,5"));
 		ridget.updateFromModel();
 
 		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals(localize("20,5"), ridget.getText());
 
 		control.selectAll();
 		UITestHelper.sendString(control.getDisplay(), "s\t");
 
 		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals("s", ridget.getText());
 
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), localize("3,1\t"));
 
 		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 		assertEquals(localize("3,1"), ridget.getText());
 	}
 
 	public void testValidationOnKeyPressedWithBlocking() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		ridget.addValidationRule(new ValidCharacters(ValidCharacters.VALID_NUMBERS), ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.setDirectWriting(true);
 
 		UITestHelper.sendString(control.getDisplay(), "12");
 
 		assertEquals("12", control.getText());
 		assertEquals("12", ridget.getText());
 		assertEquals("12", bean.getProperty());
 
 		UITestHelper.sendString(control.getDisplay(), "d");
 
 		assertEquals("12", control.getText());
 		assertEquals("12", ridget.getText());
 		assertEquals("12", bean.getProperty());
 	}
 
 	public void testValidationOnKeyPressedWithoutBlocking() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		final DateBean dateBean = new DateBean();
 		dateBean.setValue(localize(0L));
 
 		ridget.addValidationRule(new ValidIntermediateDate("dd.MM.yyyy"), ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.setUIControlToModelConverter(new StringToDateConverter("dd.MM.yyyy"));
 		ridget.setModelToUIControlConverter(new DateToStringConverter("dd.MM.yyyy"));
 
 		ridget.setUIControl(control);
 		ridget.bindToModel(dateBean, DateBean.DATE_PROPERTY);
 		ridget.updateFromModel();
 
 		assertEquals("01.01.1970", control.getText());
 		assertEquals("01.01.1970", ridget.getText());
 		assertTrue(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 
 		UITestHelper.sendKeyAction(control.getDisplay(), SWT.DEL);
 		UITestHelper.sendString(control.getDisplay(), "4");
 		ridget.addMarker(new ErrorMarker());
 
 		assertEquals("41.01.1970", control.getText());
 		assertEquals("01.01.1970", ridget.getText());
 		assertFalse(ridget.getMarkersOfType(ErrorMarker.class).isEmpty());
 	}
 
 	public void testValidationOnSetTextWithOnEditRule() {
 		final ITextRidget ridget = getRidget();
 		final StringBean model = new StringBean();
 		ridget.bindToModel(model, StringBean.PROP_VALUE);
 
 		// this is a blocking rule
 		final IValidator onEditRule = new MaxLength(5);
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.setText("tiny");
 		ridget.addValidationRule(onEditRule, ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.setText("too long");
 
 		assertTrue(ridget.isErrorMarked());
 		assertEquals("too long", ridget.getText());
 		assertEquals("too long", getWidget().getText());
 		assertEquals("tiny", model.getValue());
 
 		ridget.setText("short");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("short", ridget.getText());
 		assertEquals("short", getWidget().getText());
 		assertEquals("short", model.getValue());
 	}
 
 	public void testValidationOnSetTextWithOnUpdateRule() {
 		final ITextRidget ridget = getRidget();
 		final StringBean model = new StringBean();
 		ridget.bindToModel(model, StringBean.PROP_VALUE);
 
 		final IValidator onUpdateRule = new MinLength(10);
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.setText("this is long enough");
 		ridget.addValidationRule(onUpdateRule, ValidationTime.ON_UPDATE_TO_MODEL);
 		ridget.setText("tiny");
 
 		assertTrue(ridget.isErrorMarked());
 		assertEquals("tiny", ridget.getText());
 		assertEquals("tiny", getWidget().getText());
 		assertEquals("this is long enough", model.getValue());
 
 		ridget.setText("this is not too short");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("this is not too short", ridget.getText());
 		assertEquals("this is not too short", getWidget().getText());
 		assertEquals("this is not too short", model.getValue());
 	}
 
 	public void testValidationOnUpdateFromModelWithOnEditRule() {
 		final ITextRidget ridget = getRidget();
 		final StringBean model = new StringBean();
 		ridget.bindToModel(model, StringBean.PROP_VALUE);
 
 		final IValidator onEditRule = new MaxLength(5);
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.setText("tiny");
 		ridget.addValidationRule(onEditRule, ValidationTime.ON_UI_CONTROL_EDIT);
 		model.setValue("too long");
 
 		ridget.updateFromModel();
 
 		assertTrue(ridget.isErrorMarked());
 		assertEquals("too long", ridget.getText());
 		assertEquals("too long", getWidget().getText());
 		assertEquals("too long", model.getValue());
 
 		model.setValue("short");
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("short", ridget.getText());
 		assertEquals("short", getWidget().getText());
 		assertEquals("short", model.getValue());
 	}
 
 	public void testValidationOnUpdateFromModelWithOnUpdateRule() {
 		final ITextRidget ridget = getRidget();
 		final StringBean model = new StringBean();
 		ridget.bindToModel(model, StringBean.PROP_VALUE);
 
 		final IValidator onUpdateRule = new MinLength(10);
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.setText("something long");
 		ridget.addValidationRule(onUpdateRule, ValidationTime.ON_UPDATE_TO_MODEL);
 		model.setValue("tiny");
 
 		ridget.updateFromModel();
 
 		assertTrue(ridget.isErrorMarked());
 		assertEquals("tiny", ridget.getText());
 		assertEquals("tiny", getWidget().getText());
 		assertEquals("tiny", model.getValue());
 
 		model.setValue("this is not too short");
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("this is not too short", ridget.getText());
 		assertEquals("this is not too short", getWidget().getText());
 		assertEquals("this is not too short", model.getValue());
 	}
 
 	public void testUpdateFromRidgetWithValidationOnEditRule() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.addValidationRule(new ValidEmailAddress(), ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		assertFalse(ridget.isErrorMarked());
 		assertFalse(ridget.isDirectWriting());
 
 		// a little workaround because UITestHelper cannot send '@'
 		control.setText("a@");
 		control.setSelection(3, 3);
 		// \t triggers update
 		UITestHelper.sendString(control.getDisplay(), "b.com\t");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("a@b.com", ridget.getText());
 		assertEquals("a@b.com", bean.getProperty());
 
 		control.setFocus();
 		control.selectAll();
 		// \t triggers update
 		UITestHelper.sendString(control.getDisplay(), "invalid\t");
 
 		assertTrue(ridget.isErrorMarked());
 		// ValidEmailAddress is non-blocking, so we expected 'invalid' in ridget
 		assertEquals("invalid", ridget.getText());
 		assertEquals("a@b.com", bean.getProperty());
 
 		// a little workaround because UITestHelper cannot send '@'
 		control.setText("c@");
 		control.setFocus();
 		control.setSelection(3, 3);
 		// \t triggers update
 		UITestHelper.sendString(control.getDisplay(), "d.com\t");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("c@d.com", ridget.getText());
 		assertEquals("c@d.com", bean.getProperty());
 	}
 
 	public void testUpdateFromRidgetWithValidationOnUpdateRule() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.addValidationRule(new ValidEmailAddress(), ValidationTime.ON_UPDATE_TO_MODEL);
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		assertFalse(ridget.isErrorMarked());
 		assertFalse(ridget.isDirectWriting());
 
 		// a little workaround because UITestHelper cannot send '@'
 		control.setText("a@");
 		control.setSelection(3, 3);
 		// \t triggers update
 		UITestHelper.sendString(control.getDisplay(), "b.com\t");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("a@b.com", ridget.getText());
 		assertEquals("a@b.com", bean.getProperty());
 
 		control.setFocus();
 		control.selectAll();
 		// \t triggers update
 		UITestHelper.sendString(control.getDisplay(), "invalid\t");
 
 		assertTrue(ridget.isErrorMarked());
 		assertEquals("invalid", ridget.getText());
 		assertEquals("a@b.com", bean.getProperty());
 
 		// a little workaround because UITestHelper cannot send '@'
 		control.setText("c@");
 		control.setFocus();
 		control.setSelection(3, 3);
 		// \t triggers update
 		UITestHelper.sendString(control.getDisplay(), "d.com\t");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("c@d.com", ridget.getText());
 		assertEquals("c@d.com", bean.getProperty());
 	}
 
 	public void testValidationMessageWithOnEditRule() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.addValidationRule(new EvenNumberOfCharacters(), ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.setDirectWriting(true);
 
 		ridget.addValidationMessage("TestTextTooShortMessage");
 
 		assertEquals(0, ridget.getMarkers().size());
 
 		UITestHelper.sendString(control.getDisplay(), "a");
 
 		assertEquals(2, ridget.getMarkers().size());
 		final Iterator<? extends IMarker> iterator = ridget.getMarkers().iterator();
 		while (iterator.hasNext()) {
 			final IMarker next = iterator.next();
 			assertTrue(next instanceof IMessageMarker);
 			final IMessageMarker marker = (IMessageMarker) next;
 			assertTrue(marker.getMessage().equals("TestTextTooShortMessage")
 					|| marker.getMessage().equals("Odd number of characters."));
 		}
 
 		UITestHelper.sendString(control.getDisplay(), "b");
 
 		assertEquals(0, ridget.getMarkers().size());
 	}
 
 	public void testValidationMessageWithOnUpdateRule() throws Exception {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.addValidationRule(new EvenNumberOfCharacters(), ValidationTime.ON_UPDATE_TO_MODEL);
 		ridget.setDirectWriting(true);
 
 		ridget.addValidationMessage("TestTextTooShortMessage");
 
 		assertEquals(0, ridget.getMarkers().size());
 
 		// \r triggers update
 		UITestHelper.sendString(control.getDisplay(), "a\r");
 
 		assertEquals(2, ridget.getMarkers().size());
 		assertMessage(ridget, ValidationMessageMarker.class, "TestTextTooShortMessage");
 
 		// \r triggers update
 		UITestHelper.sendString(control.getDisplay(), "b\r");
 
 		assertEquals(0, ridget.getMarkers().size());
 	}
 
 	/**
 	 * As per Bug 279665 comment #3
 	 */
 	public void testValidationMessageWithOnUpdateAndOnEditRules() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		final IValidator ruleMin3 = new MinLength(3);
 		final IValidator ruleMin5 = new MinLength(5);
 		ridget.addValidationRule(ruleMin3, ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.addValidationRule(ruleMin5, ValidationTime.ON_UPDATE_TO_MODEL);
 		ridget.addValidationMessage("need 3", ruleMin3);
 		ridget.addValidationMessage("need 5", ruleMin5);
 
 		bean.setProperty("a");
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.updateFromModel();
 
 		assertMessage(ridget, ValidationMessageMarker.class, "need 3");
 		assertMessage(ridget, ValidationMessageMarker.class, "need 5");
 		assertMessageCount(ridget, ValidationMessageMarker.class, 2);
 
 		control.setSelection(1, 1);
 		UITestHelper.sendString(control.getDisplay(), "b");
 
 		assertMessage(ridget, ValidationMessageMarker.class, "need 3");
 		assertMessage(ridget, ValidationMessageMarker.class, "need 5");
 		assertMessageCount(ridget, ValidationMessageMarker.class, 2);
 
 		UITestHelper.sendString(control.getDisplay(), "c");
 
 		assertMessage(ridget, ValidationMessageMarker.class, "need 5");
 		assertMessageCount(ridget, ValidationMessageMarker.class, 1);
 
 		UITestHelper.sendString(control.getDisplay(), "de");
 
 		assertMessage(ridget, ValidationMessageMarker.class, "need 5");
 		assertMessageCount(ridget, ValidationMessageMarker.class, 1);
 
 		UITestHelper.sendString(control.getDisplay(), "\r");
 
 		assertMessageCount(ridget, ValidationMessageMarker.class, 0);
 	}
 
 	public void testErrorMessageWithOnUpdateAndOnEditRules() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 
 		final IValidator ruleMin3 = new MinLength(3);
 		final IValidator ruleMin5 = new MinLength(5);
 		ridget.addValidationRule(ruleMin3, ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.addValidationRule(ruleMin5, ValidationTime.ON_UPDATE_TO_MODEL);
 
 		bean.setProperty("a");
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.updateFromModel();
 
 		assertMessage(ridget, ErrorMessageMarker.class, "'a' is less than 3 characters long.");
 		assertMessage(ridget, ErrorMessageMarker.class, "'a' is less than 5 characters long.");
 		assertMessageCount(ridget, ErrorMessageMarker.class, 2);
 
 		control.setSelection(1, 1);
 		UITestHelper.sendString(control.getDisplay(), "b");
 
 		assertMessage(ridget, ErrorMessageMarker.class, "'ab' is less than 3 characters long.");
 		assertMessage(ridget, ErrorMessageMarker.class, "'a' is less than 5 characters long.");
 		assertMessageCount(ridget, ErrorMessageMarker.class, 2);
 
 		UITestHelper.sendString(control.getDisplay(), "c");
 
 		assertMessage(ridget, ErrorMessageMarker.class, "'a' is less than 5 characters long.");
 		assertMessageCount(ridget, ErrorMessageMarker.class, 1);
 
 		UITestHelper.sendString(control.getDisplay(), "de");
 
 		assertMessage(ridget, ErrorMessageMarker.class, "'a' is less than 5 characters long.");
 		assertMessageCount(ridget, ErrorMessageMarker.class, 1);
 
 		UITestHelper.sendString(control.getDisplay(), "\r");
 
 		assertMessageCount(ridget, ErrorMessageMarker.class, 0);
 	}
 
 	public void testRevalidateOnEditRule() {
 		final ITextRidget ridget = getRidget();
 		final ValidCharacters numbersOnly = new ValidCharacters(ValidCharacters.VALID_NUMBERS);
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.setText("abc");
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.addValidationRule(numbersOnly, ValidationTime.ON_UI_CONTROL_EDIT);
 
 		assertFalse(ridget.isErrorMarked());
 
 		final boolean isOk1 = ridget.revalidate();
 
 		assertFalse(isOk1);
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.removeValidationRule(numbersOnly);
 
 		assertFalse(ridget.isErrorMarked()); // since 1.2: remove updates immediately
 
 		final boolean isOk2 = ridget.revalidate();
 
 		assertTrue(isOk2);
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	public void testRevalidateOnUpdateRule() {
 		final ITextRidget ridget = getRidget();
 		final ValidCharacters numbersOnly = new ValidCharacters(ValidCharacters.VALID_NUMBERS);
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.setText("abc");
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.addValidationRule(numbersOnly, ValidationTime.ON_UPDATE_TO_MODEL);
 
 		assertFalse(ridget.isErrorMarked());
 
 		final boolean isOk1 = ridget.revalidate();
 
 		assertFalse(isOk1);
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.removeValidationRule(numbersOnly);
 
 		assertFalse(ridget.isErrorMarked()); // since 1.2: remove updates immediately
 
 		final boolean isOk2 = ridget.revalidate();
 
 		assertTrue(isOk2);
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	public void testRevalidateDoesUpdate() {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 		final EvenNumberOfCharacters evenChars = new EvenNumberOfCharacters();
 		ridget.addValidationRule(evenChars, ValidationTime.ON_UI_CONTROL_EDIT);
 
 		bean.setProperty("ab");
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 
 		control.setFocus();
 		control.selectAll();
 		UITestHelper.sendString(control.getDisplay(), "abc\t");
 		assertEquals("abc", control.getText());
 		// non-blocking rule, expect 'abc'
 		assertEquals("abc", ridget.getText());
 		assertEquals("ab", bean.getProperty());
 
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.removeValidationRule(evenChars);
 		ridget.revalidate();
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("abc", control.getText());
 		assertEquals("abc", ridget.getText());
 		assertEquals("abc", bean.getProperty());
 	}
 
 	public void testReValidationOnSetText() {
 		final ITextRidget ridget = getRidget();
 		final ValidCharacters numbersOnly = new ValidCharacters(ValidCharacters.VALID_NUMBERS);
 
 		ridget.setText("123");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("123", ridget.getText());
 
 		ridget.addValidationRule(numbersOnly, ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.setText("abc");
 
 		assertTrue(ridget.isErrorMarked());
 		assertEquals("abc", ridget.getText());
 
 		ridget.removeValidationRule(numbersOnly);
 		ridget.setText("abc");
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("abc", ridget.getText());
 	}
 
 	public void testReValidationOnUpdateFromModel() {
 		final ITextRidget ridget = getRidget();
 		final ValidCharacters numbersOnly = new ValidCharacters(ValidCharacters.VALID_NUMBERS);
 
 		bean.setProperty("123");
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("123", ridget.getText());
 
 		ridget.addValidationRule(numbersOnly, ValidationTime.ON_UI_CONTROL_EDIT);
 
 		bean.setProperty("abc");
 		ridget.updateFromModel();
 
 		assertTrue(ridget.isErrorMarked());
 		assertEquals("abc", bean.getProperty());
 		assertEquals("abc", ridget.getText());
 
 		ridget.removeValidationRule(numbersOnly);
 
 		ridget.updateFromModel();
 
 		assertFalse(ridget.isErrorMarked());
 		assertEquals("abc", bean.getProperty());
 		assertEquals("abc", ridget.getText());
 	}
 
 	/**
 	 * Tests that the mandatory marker is enabled/disabled when calling
 	 * {@code setText(string)}.
 	 */
 	public void testDisableMandatoryMarkersSetText() {
 		final ITextRidget ridget = getRidget();
 		final MandatoryMarker mandatoryMarker = new MandatoryMarker();
 		ridget.addMarker(mandatoryMarker);
 
 		ridget.setText("");
 
 		assertFalse(mandatoryMarker.isDisabled());
 
 		ridget.setText("foo");
 
 		assertTrue(mandatoryMarker.isDisabled());
 
 		ridget.setText("");
 
 		assertFalse(mandatoryMarker.isDisabled());
 	}
 
 	/**
 	 * Tests that the manddatory marker is enabled/disabled when updated from
 	 * the model.
 	 */
 	public void testDisableMandatoryMarkersUpdateFromModel() {
 		final ITextRidget ridget = getRidget();
 		final MandatoryMarker mandatoryMarker = new MandatoryMarker();
 		ridget.addMarker(mandatoryMarker);
 
 		ridget.setText("foo");
 
 		assertTrue(ridget.isDisableMandatoryMarker());
 
 		bean.setProperty("");
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.updateFromModel();
 
 		assertFalse(mandatoryMarker.isDisabled());
 
 		bean.setProperty("baz");
 		ridget.updateFromModel();
 
 		assertTrue(mandatoryMarker.isDisabled());
 	}
 
 	/**
 	 * Tests that the mandatory marker is enabled/disabled when typing.
 	 */
 	public void testDisableMandatoryMarkers() {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 		final MandatoryMarker mandatoryMarker = new MandatoryMarker();
 		ridget.addMarker(mandatoryMarker);
 
 		assertFalse(ridget.isDirectWriting());
 
 		ridget.setText("foo");
 
 		assertTrue(mandatoryMarker.isDisabled());
 
 		control.selectAll();
 		control.setFocus();
 		// delete all in control
 		UITestHelper.sendString(control.getDisplay(), "\b");
 
 		assertFalse(mandatoryMarker.isDisabled());
 
 		// type 'x'
 		UITestHelper.sendString(control.getDisplay(), "x");
 
 		assertTrue(mandatoryMarker.isDisabled());
 
 		// tab out
 		UITestHelper.sendString(control.getDisplay(), "\t");
 
 		assertTrue(mandatoryMarker.isDisabled());
 
 		// delete
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), "\b");
 
 		assertFalse(mandatoryMarker.isDisabled());
 	}
 
 	/**
 	 * Tests that the mandatory marker is enabled/disabled when typing in
 	 * "direct writing" mode.
 	 */
 	public void testDisableMandatoryMarkersDirectWriting() {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 		final MandatoryMarker mandatoryMarker = new MandatoryMarker();
 		ridget.addMarker(mandatoryMarker);
 		ridget.setDirectWriting(true);
 
 		assertTrue(ridget.isDirectWriting());
 
 		ridget.setText("foo");
 
 		assertTrue(mandatoryMarker.isDisabled());
 
 		control.selectAll();
 		control.setFocus();
 		// delete all in control
 		UITestHelper.sendString(control.getDisplay(), "\b");
 
 		assertFalse(mandatoryMarker.isDisabled());
 
 		// type 'x'
 		UITestHelper.sendString(control.getDisplay(), "x");
 
 		assertTrue(mandatoryMarker.isDisabled());
 
 		// delete
 		UITestHelper.sendString(control.getDisplay(), "\b");
 
 		assertFalse(mandatoryMarker.isDisabled());
 	}
 
 	/**
 	 * Tests that the isDisabledMandatoryMarker true when we have text
 	 */
 	public void testIsDisableMandatoryMarker() {
 		final ITextRidget ridget = getRidget();
 
 		ridget.setText("foo");
 
 		assertTrue(ridget.isDisableMandatoryMarker());
 
 		ridget.setText("");
 
 		assertFalse(ridget.isDisableMandatoryMarker());
 
 		ridget.setText("  ");
 
 		assertTrue(ridget.isDisableMandatoryMarker());
 	}
 
 	public void testControlNotEditableWithOutputMarker() {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 
 		assertTrue(control.getEditable());
 
 		ridget.setOutputOnly(true);
 
 		assertFalse(control.getEditable());
 
 		ridget.setOutputOnly(false);
 
 		assertTrue(control.getEditable());
 	}
 
 	public void testOutputMultipleSelectionCannotBeChangedFromUI() {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 
 		ridget.setText("foo");
 
 		assertEquals("foo", control.getText());
 		assertEquals("foo", ridget.getText());
 
 		ridget.setOutputOnly(true);
 		control.selectAll();
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), "bar\t");
 
 		assertEquals("foo", control.getText());
 		assertEquals("foo", ridget.getText());
 
 		ridget.setOutputOnly(false);
 		control.selectAll();
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), "bar\t");
 
 		assertEquals("bar", control.getText());
 		assertEquals("bar", ridget.getText());
 	}
 
 	public void testDisabledHasNoTextFromRidget() {
 		if (!MarkerSupport.isHideDisabledRidgetContent()) {
 			System.out.println("Skipping TextRidgetTest2.testDisabledHasNoTextFromRidget()");
 			return;
 		}
 
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		ridget.setText("foo");
 
 		assertEquals("foo", control.getText());
 		assertEquals("foo", ridget.getText());
 		assertEquals("foo", bean.getProperty());
 
 		ridget.setEnabled(false);
 
 		assertEquals("", control.getText());
 		assertEquals("foo", ridget.getText());
 		assertEquals("foo", bean.getProperty());
 
 		ridget.setText("bar");
 
 		assertEquals("", control.getText());
 		assertEquals("bar", ridget.getText());
 		assertEquals("bar", bean.getProperty());
 
 		ridget.setEnabled(true);
 
 		assertEquals("bar", control.getText());
 		assertEquals("bar", ridget.getText());
 		assertEquals("bar", bean.getProperty());
 	}
 
 	public void testDisabledHasNoTextFromModel() {
 		if (!MarkerSupport.isHideDisabledRidgetContent()) {
 			System.out.println("Skipping TextRidgetTest2.testDisabledHasNoTextFromModel()");
 			return;
 		}
 
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 		bean.setProperty(TEXT_TWO);
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		ridget.updateFromModel();
 
 		assertEquals(TEXT_TWO, control.getText());
 		assertEquals(TEXT_TWO, ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 
 		ridget.setEnabled(false);
 
 		assertEquals("", control.getText());
 		assertEquals(TEXT_TWO, ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 
 		bean.setProperty(TEXT_ONE);
 		ridget.updateFromModel();
 
 		assertEquals("", control.getText());
 		assertEquals(TEXT_ONE, ridget.getText());
 		assertEquals(TEXT_ONE, bean.getProperty());
 
 		ridget.setEnabled(true);
 
 		assertEquals(TEXT_ONE, control.getText());
 		assertEquals(TEXT_ONE, ridget.getText());
 		assertEquals(TEXT_ONE, bean.getProperty());
 	}
 
 	/**
 	 * Bug 281891.
 	 * 
 	 * @see http://bugs.eclipse.org/281891
 	 */
 	public void testDisabledControlHasNoText() {
 		if (!MarkerSupport.isHideDisabledRidgetContent()) {
 			System.out.println("Skipping TextRidgetTest2.testDisabledHasNoText()");
 			return;
 		}
 
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 
 		ridget.setUIControl(null);
 		ridget.setText("text");
 		ridget.setEnabled(false);
 		ridget.setUIControl(control);
 
 		assertEquals("", control.getText());
 
 		ridget.setEnabled(true);
 
 		assertEquals("text", control.getText());
 	}
 
 	public void testShowOneValidationRuleMessageInTooltip() {
 		final ITextRidget ridget = getRidget();
 		final TooltipMessageMarkerViewer viewer = new TooltipMessageMarkerViewer();
 		viewer.addRidget(ridget);
 		ridget.setToolTipText("original text");
 		final AlwaysWrongValidator rule = new AlwaysWrongValidator("ruleA_");
 		ridget.addValidationRule(rule, ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.revalidate();
 
 		assertEquals("ruleA_1", ridget.getToolTipText());
 
 		ridget.revalidate();
 
 		assertEquals("ruleA_2", ridget.getToolTipText());
 
 		ridget.removeValidationRule(rule);
 		ridget.revalidate();
 
 		assertEquals("original text", ridget.getToolTipText());
 	}
 
 	public void testShowSeveralValidationRuleMessagesInTooltip() {
 		final ITextRidget ridget = getRidget();
 		final TooltipMessageMarkerViewer viewer = new TooltipMessageMarkerViewer();
 		viewer.addRidget(ridget);
 		ridget.setToolTipText("original text");
 		final AlwaysWrongValidator rule1 = new AlwaysWrongValidator("ruleA_");
 		final AlwaysWrongValidator rule2 = new AlwaysWrongValidator("ruleB_");
 		ridget.addValidationRule(rule1, ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.addValidationRule(rule2, ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.revalidate();
 
 		assertEquals("ruleA_1\nruleB_1", ridget.getToolTipText());
 
 		ridget.removeValidationRule(rule1);
 		ridget.revalidate();
 
 		assertEquals("ruleB_2", ridget.getToolTipText());
 
 		ridget.removeValidationRule(rule2);
 		ridget.revalidate();
 
 		assertEquals("original text", ridget.getToolTipText());
 	}
 
 	public void testModelUpdatedBeforeListenerNotificationOnSetText() {
 		final ITextRidget ridget = getRidget();
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		final FTPropertyChangeListener listener1 = new FTPropertyChangeListener(bean, "newValue");
 		ridget.addPropertyChangeListener(listener1);
 		final FTPropertyChangeListener listener2 = new FTPropertyChangeListener(bean, "newValue");
 		ridget.addPropertyChangeListener(ITextRidget.PROPERTY_TEXT, listener2);
 
 		ridget.setText("newValue");
 
 		assertEquals("newValue", getWidget().getText());
 		assertEquals("newValue", ridget.getText());
 		assertEquals("newValue", bean.getProperty());
 		assertEquals(1, bean.getSetCount());
 		assertEquals(1, listener1.count);
 		assertEquals(1, listener2.count);
 	}
 
 	public void testModelUpdatedBeforeListenerNotificationOnTyping() {
 		final ITextRidget ridget = getRidget();
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		final FTPropertyChangeListener listener1 = new FTPropertyChangeListener(bean, "abc");
 		ridget.addPropertyChangeListener(listener1);
 		final FTPropertyChangeListener listener2 = new FTPropertyChangeListener(bean, "abc");
 		ridget.addPropertyChangeListener(ITextRidget.PROPERTY_TEXT, listener2);
 		ridget.setDirectWriting(false);
 
 		final Text control = getWidget();
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), "abc\r");
 
 		assertEquals("abc", control.getText());
 		assertEquals("abc", ridget.getText());
 		assertEquals("abc", bean.getProperty());
 		assertEquals(1, bean.getSetCount());
 		assertEquals(1, listener1.count);
 		assertEquals(1, listener2.count);
 	}
 
 	public void testMandatoryMarker() {
 		final ITextRidget ridget = getRidget();
 		ridget.setMandatory(true);
 
 		ridget.setText("abc");
 
 		assertMandatoryMarker(ridget, 1, true);
 
 		ridget.setText(null);
 
 		assertMandatoryMarker(ridget, 1, false);
 
 		ridget.setMandatory(false);
 
 		assertMandatoryMarker(ridget, 0, false);
 	}
 
 	public void testInputToUIControlConverter() {
 		final Text control = getWidget();
 		final ITextRidget ridget = getRidget();
 		final Display display = control.getDisplay();
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		ridget.setInputToUIControlConverter(new StringToUpperCaseConverter());
 		control.setFocus();
 		UITestHelper.sendString(display, "teSt2\r");
 
 		assertEquals("TEST2", control.getText());
 		assertEquals("TEST2", ridget.getText());
 		assertEquals("TEST2", bean.getProperty());
 
 		ridget.setInputToUIControlConverter(new StringToLowerCaseConverter());
 		control.selectAll();
 		UITestHelper.sendString(display, "TEsT2\r");
 
 		assertEquals("test2", control.getText());
 		assertEquals("test2", ridget.getText());
 		assertEquals("test2", bean.getProperty());
 
 		ridget.setInputToUIControlConverter(null);
 		control.selectAll();
 		UITestHelper.sendString(display, "ABcd\r");
 
 		assertEquals("ABcd", control.getText());
 		assertEquals("ABcd", ridget.getText());
 		assertEquals("ABcd", bean.getProperty());
 	}
 
 	public void testInputToUIControlConverterWrongType() {
 		final ITextRidget ridget = getRidget();
 
 		try {
 			ridget.setInputToUIControlConverter(new Converter(Integer.class, String.class) {
 				public Object convert(final Object fromObject) {
 					return "1";
 				}
 			});
 			fail();
 		} catch (final RuntimeException rex) {
 			ok("expected - must have a String.class from-type");
 		}
 
 		try {
 			ridget.setInputToUIControlConverter(new Converter(String.class, Integer.class) {
 				public Object convert(final Object fromObject) {
 					return Integer.valueOf(1);
 				}
 			});
 			fail();
 		} catch (final RuntimeException rex) {
 			ok("expected - must have a String.class to-type");
 		}
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code isSubModuleViewComposite(Control)}
 	 * .
 	 */
 	public void testIsSubModuleViewComposite() {
 		final Text text = new Text(getShell(), SWT.BORDER);
 
 		boolean ret = ReflectionUtils.invokeHidden(getRidget(), "isSubModuleViewComposite", text);
 		assertFalse(ret);
 
 		text.setData("isSubModuleViewComposite", Boolean.TRUE);
 		ret = ReflectionUtils.invokeHidden(getRidget(), "isSubModuleViewComposite", text);
 		assertFalse(ret);
 		SwtUtilities.dispose(text);
 
 		final Composite comp = new Composite(getShell(), SWT.NONE);
 
 		ret = ReflectionUtils.invokeHidden(getRidget(), "isSubModuleViewComposite", comp);
 		assertFalse(ret);
 
 		comp.setData("isSubModuleViewComposite", Boolean.TRUE);
 		ret = ReflectionUtils.invokeHidden(getRidget(), "isSubModuleViewComposite", comp);
 		assertTrue(ret);
 		SwtUtilities.dispose(comp);
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code isChildOfSubModuleView(Control)} .
 	 */
 	public void testIsChildOfSubModuleView() {
 		Text text = new Text(getShell(), SWT.BORDER);
 		boolean ret = ReflectionUtils.invokeHidden(getRidget(), "isChildOfSubModuleView", text);
 		assertFalse(ret);
 		SwtUtilities.dispose(text);
 
 		final Composite comp = new Composite(getShell(), SWT.NONE);
 		text = new Text(comp, SWT.BORDER);
 		ret = ReflectionUtils.invokeHidden(getRidget(), "isChildOfSubModuleView", text);
 		assertFalse(ret);
 
 		comp.setData("isSubModuleViewComposite", Boolean.TRUE);
 		ret = ReflectionUtils.invokeHidden(getRidget(), "isChildOfSubModuleView", text);
 		assertTrue(ret);
 		SwtUtilities.dispose(text);
 		SwtUtilities.dispose(comp);
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code isControlVisible(Control)} .
 	 */
 	public void testIsControlVisible() {
 		Text text = new Text(getShell(), SWT.BORDER);
 		boolean ret = ReflectionUtils.invokeHidden(getRidget(), "isControlVisible", text);
 		assertTrue(ret);
 		getShell().setVisible(false);
 		ret = ReflectionUtils.invokeHidden(getRidget(), "isControlVisible", text);
 		assertFalse(ret);
 		SwtUtilities.dispose(text);
 		getShell().setVisible(true);
 
 		final Composite compTop = new Composite(getShell(), SWT.NONE);
 		final Composite compChild = new Composite(compTop, SWT.NONE);
 		text = new Text(compChild, SWT.BORDER);
 		ret = ReflectionUtils.invokeHidden(getRidget(), "isControlVisible", text);
 		assertTrue(ret);
 
 		getShell().setVisible(false);
 		ret = ReflectionUtils.invokeHidden(getRidget(), "isControlVisible", text);
 		assertFalse(ret);
 
 		compTop.setData("isSubModuleViewComposite", Boolean.TRUE);
 		ret = ReflectionUtils.invokeHidden(getRidget(), "isControlVisible", text);
 		assertTrue(ret);
 
 		compTop.setVisible(false);
 		ret = ReflectionUtils.invokeHidden(getRidget(), "isControlVisible", text);
 		assertTrue(ret);
 
 		compTop.setVisible(true);
 		compChild.setVisible(false);
 		ret = ReflectionUtils.invokeHidden(getRidget(), "isControlVisible", text);
 		assertFalse(ret);
 
 		getShell().setVisible(true);
 		SwtUtilities.dispose(text);
 		SwtUtilities.dispose(compChild);
 		SwtUtilities.dispose(compTop);
 	}
 
 	/**
 	 * As per Bug 317028.
 	 */
 	public void testTogglingEnabledWithDirectWritingPreservesContent() {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 
 		ridget.setDirectWriting(true);
 
 		assertTrue(ridget.isDirectWriting());
 
 		bean.setProperty("abcd");
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.updateFromModel();
 
 		assertEquals("abcd", control.getText());
 		assertEquals("abcd", ridget.getText());
 		assertEquals("abcd", bean.getProperty());
 
 		ridget.setEnabled(false);
 
 		assertEquals("", control.getText());
 		assertEquals("abcd", ridget.getText());
 		assertEquals("abcd", bean.getProperty());
 
 		ridget.setEnabled(true);
 
 		assertEquals("abcd", control.getText());
 		assertEquals("abcd", ridget.getText());
 		assertEquals("abcd", bean.getProperty());
 	}
 
 	/**
 	 * As per Bug 327496.
 	 */
 	@SuppressWarnings("unchecked")
 	public void testToggleMarkerHidingWithMandatoryMarkerOn() {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 
 		final Color mandatoryMarkerBg = new Color(control.getDisplay(), 255, 255, 175);
 		final Color whiteBg = control.getDisplay().getSystemColor(SWT.COLOR_WHITE);
 
 		try {
 			ridget.setMandatory(true);
 			ridget.setText("");
 
 			assertEquals(mandatoryMarkerBg, control.getBackground());
 
 			ridget.hideMarkersOfType(MandatoryMarker.class);
 
 			assertEquals(whiteBg, control.getBackground());
 
 			ridget.showMarkersOfType(MandatoryMarker.class);
 
 			assertEquals(mandatoryMarkerBg, control.getBackground());
 		} finally {
 			mandatoryMarkerBg.dispose();
 		}
 	}
 
 	/**
 	 * As per Bug 327496.
 	 */
 	@SuppressWarnings("unchecked")
 	public void testToggleMandatoryMarkerWithMarkerHidingOn() {
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 
 		final Color whiteBg = control.getDisplay().getSystemColor(SWT.COLOR_WHITE);
 
 		ridget.setText("");
 		ridget.hideMarkersOfType(MandatoryMarker.class);
 
 		assertEquals(whiteBg, control.getBackground());
 
 		ridget.setMandatory(true);
 
 		assertEquals(whiteBg, control.getBackground());
 
 		ridget.setMandatory(false);
 
 		assertEquals(whiteBg, control.getBackground());
 
 		ridget.showMarkersOfType(MandatoryMarker.class);
 
 		assertEquals(whiteBg, control.getBackground());
 	}
 
 	public void testUpdateTextValue() {
 
 		final ITextRidget ridget = getRidget();
 		final Text control = getWidget();
 		control.setText("one");
 		ReflectionUtils.invokeHidden(ridget, "updateTextValue");
 		assertEquals("one", ridget.getText());
 
 		ridget.setOutputOnly(true);
 		control.setText("two");
 		ReflectionUtils.invokeHidden(ridget, "updateTextValue");
 		assertEquals("one", ridget.getText());
 
 		ridget.setOutputOnly(false);
 		control.setText("three");
 		ReflectionUtils.invokeHidden(ridget, "updateTextValue");
 		assertEquals("three", ridget.getText());
 
 	}
 
 	/**
 	 * Tests if all nested properties of a <b>pojo</b> will be observed by the
 	 * JFace data binding.
 	 * <p>
 	 * Note: All involved value holder must be beans. With pojos a rebind to the
 	 * model will be necessary.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testBindToModelPojoWithNestedProperties() throws Exception {
 
 		final ITextRidget ridget = getRidget();
 		ridget.setText("");
 
 		final TextPojoHolder modelHolder = new TextPojoHolder();
 		final TextPojo model = new TextPojo();
 		modelHolder.setPojo(model);
 		ridget.bindToModel(modelHolder, "pojo.text2");
 
 		assertEquals("", ridget.getText());
 
 		ridget.updateFromModel();
 
 		assertEquals(modelHolder.getPojo().getText2(), ridget.getText());
 
 		final TextPojo model2 = new TextPojo();
 		model2.setText2("three");
 		modelHolder.setPojo(model2);
 		ridget.updateFromModel();
 
 		assertEquals(modelHolder.getPojo().getText2(), ridget.getText());
 
 	}
 
 	/**
 	 * Tests if all nested properties of a <b>bean</b> will be observed by the
 	 * JFace data binding.
 	 * <p>
 	 * Note: All involved value holder must be beans. With pojos a rebind to the
 	 * model will be necessary.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testBindToModelBeanWithNestedProperties() throws Exception {
 
 		final ITextRidget ridget = getRidget();
 		ridget.setText("");
 
 		final TextBeanHolder modelHolder = new TextBeanHolder();
 		final TextBean model = new TextBean();
 		modelHolder.setBean(model);
 		ridget.bindToModel(modelHolder, "bean.text2");
 
 		assertEquals("", ridget.getText());
 
 		ridget.updateFromModel();
 
 		assertEquals(modelHolder.getBean().getText2(), ridget.getText());
 
 		final TextBean model2 = new TextBean();
 		model2.setText2("three");
 		modelHolder.setBean(model2);
 		ridget.updateFromModel();
 
 		assertEquals(modelHolder.getBean().getText2(), ridget.getText());
 
 	}
 
 	// helping methods
 	//////////////////
 
 	@SuppressWarnings("deprecation")
 	private Date localize(final long msSinceEpochUtc) {
 		final Date localDate = new Date(msSinceEpochUtc);
 		return new Date(localDate.getTime() + (60 * 1000 * localDate.getTimezoneOffset()));
 	}
 
 	private String localize(final String number) {
 		return TestUtils.getLocalizedNumber(number);
 	}
 
 	// helping classes
 	//////////////////
 
 	private static class EvenNumberOfCharacters implements IValidator {
 		public IStatus validate(final Object value) {
 			if (value == null) {
 				return ValidationRuleStatus.ok();
 			}
 			if (value instanceof String) {
 				final String string = (String) value;
 				if (string.length() % 2 == 0) {
 					return ValidationRuleStatus.ok();
 				}
 				return ValidationRuleStatus.error(false, "Odd number of characters.");
 			}
 			throw new ValidationFailure(getClass().getName() + " can only validate objects of type "
 					+ String.class.getName());
 		}
 	}
 
 	private static final class AlwaysWrongValidator implements IValidator {
 		private final String message;
 		private int invokeCount = 0;
 
 		AlwaysWrongValidator(final String message) {
 			this.message = message;
 		}
 
 		public IStatus validate(final Object value) {
 			invokeCount++;
 			return ValidationRuleStatus.error(false, message + String.valueOf(invokeCount));
 		}
 	}
 
 	private static final class FTPropertyChangeListener implements PropertyChangeListener {
 		private final TestBean bean;
 		private final String value;
 
 		private int count;
 
 		FTPropertyChangeListener(final TestBean bean, final String value) {
 			this.bean = bean;
 			this.value = value;
 		}
 
 		public void propertyChange(final PropertyChangeEvent evt) {
 			if (ITextRidget.PROPERTY_TEXT.equals(evt.getPropertyName())) {
 				count++;
 				assertEquals(1, count);
 				assertEquals(value, evt.getNewValue());
 				/*
 				 * tests that the model is already updated with the new value,
 				 * when this listener is notified
 				 */
 				assertEquals(value, bean.getProperty());
 			}
 		}
 	}
 
 	private static class TextPojoHolder {
 
 		private TextPojo pojo;
 
 		public TextPojo getPojo() {
 			return pojo;
 		}
 
 		public void setPojo(final TextPojo pojo) {
 			this.pojo = pojo;
 		}
 
 	}
 
 	private static class TextBeanHolder {
 
 		private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
 
 		private TextBean bean;
 
 		public TextBean getBean() {
 			return bean;
 		}
 
 		public void setBean(final TextBean bean) {
 			propertyChangeSupport.firePropertyChange("bean", this.bean, this.bean = bean);
 		}
 
 		public void removePropertyChangeListener(final PropertyChangeListener listener) {
 			propertyChangeSupport.removePropertyChangeListener(listener);
 		}
 
 		public void addPropertyChangeListener(final PropertyChangeListener listener) {
 			propertyChangeSupport.addPropertyChangeListener(listener);
 		}
 	}
 
 	/**
 	 * An object holding two strings.
 	 */
 	private static class TextPojo {
 
 		private String text1;
 		private String text2;
 
 		public TextPojo() {
 			text1 = "one";
 			text2 = "two";
 		}
 
 		public String getText1() {
 			return text1;
 		}
 
 		@SuppressWarnings("unused")
 		public void setText1(final String text1) {
 			this.text1 = text1;
 		}
 
 		public String getText2() {
 			return text2;
 		}
 
 		public void setText2(final String text2) {
 			this.text2 = text2;
 		}
 
 	}
 
 	/**
 	 * A bean holding two strings.
 	 */
 	private static class TextBean {
 
 		private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
 
 		private String text1;
 		private String text2;
 
 		public TextBean() {
 			text1 = "one";
 			text2 = "two";
 		}
 
 		public String getText1() {
 			return text1;
 		}
 
 		@SuppressWarnings("unused")
 		public void setText1(final String text1) {
 			propertyChangeSupport.firePropertyChange("text1", this.text1, this.text1 = text1);
 		}
 
 		public String getText2() {
 			return text2;
 		}
 
 		public void setText2(final String text2) {
 			propertyChangeSupport.firePropertyChange("text2", this.text2, this.text2 = text2);
 		}
 
 		public void removePropertyChangeListener(final PropertyChangeListener listener) {
 			propertyChangeSupport.removePropertyChangeListener(listener);
 		}
 
 		public void addPropertyChangeListener(final PropertyChangeListener listener) {
 			propertyChangeSupport.addPropertyChangeListener(listener);
 		}
 
 	}
 
 }
