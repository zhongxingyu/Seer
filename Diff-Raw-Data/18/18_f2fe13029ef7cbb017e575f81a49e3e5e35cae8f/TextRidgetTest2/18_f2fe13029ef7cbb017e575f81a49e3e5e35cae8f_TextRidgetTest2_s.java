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
 import java.util.Collection;
 import java.util.Date;
 
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.tests.UITestHelper;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.IMessageMarker;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.core.marker.ValidationTime;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.databinding.DateToStringConverter;
 import org.eclipse.riena.ui.ridgets.databinding.StringToDateConverter;
 import org.eclipse.riena.ui.ridgets.marker.TooltipMessageMarkerViewer;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.DefaultSwtControlRidgetMapper;
 import org.eclipse.riena.ui.ridgets.util.beans.DateBean;
 import org.eclipse.riena.ui.ridgets.util.beans.StringBean;
 import org.eclipse.riena.ui.ridgets.validation.MaxLength;
 import org.eclipse.riena.ui.ridgets.validation.MinLength;
 import org.eclipse.riena.ui.ridgets.validation.ValidCharacters;
 import org.eclipse.riena.ui.ridgets.validation.ValidEmailAddress;
 import org.eclipse.riena.ui.ridgets.validation.ValidIntermediateDate;
 import org.eclipse.riena.ui.ridgets.validation.ValidationFailure;
 import org.eclipse.riena.ui.ridgets.validation.ValidationRuleStatus;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Tests for the class {@link TextRidget}.
  * 
  * @see TextRidgetTest
  */
 public class TextRidgetTest2 extends AbstractSWTRidgetTest {
 
 	private final static String TEXT_ONE = "TestText1";
 	private final static String TEXT_TWO = "TestText2";
 	private TestBean bean;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		bean = new TestBean();
 		Shell shell = getShell();
 		// need a second control, so that we can switch focus
 		new Text(shell, SWT.BORDER);
 		shell.layout();
 	}
 
 	@Override
 	protected IRidget createRidget() {
 		return new TextRidget();
 	}
 
 	@Override
 	protected Control createWidget(Composite parent) {
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
 		DefaultSwtControlRidgetMapper mapper = new DefaultSwtControlRidgetMapper();
 		assertSame(TextRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	public void testInitialValueFromModel() {
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
 		bean.setProperty(TEXT_TWO);
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		assertEquals("", control.getText());
 		assertEquals("", ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "", TEXT_TWO);
 
 		ridget.updateFromModel();
 
 		verifyPropertyChangeEvents();
 		assertEquals(TEXT_TWO, control.getText());
 		assertEquals(TEXT_TWO, ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 	}
 
 	public void testUpdateFromModel() {
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
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
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, TEXT_ONE, TEXT_TWO);
 
 		ridget.updateFromModel();
 
 		verifyPropertyChangeEvents();
 		assertEquals(TEXT_TWO, control.getText());
 		assertEquals(TEXT_TWO, ridget.getText());
 		assertEquals(TEXT_TWO, bean.getProperty());
 	}
 
 	public void testUpdateFromModelDirectWriting() {
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
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
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 		Display display = control.getDisplay();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		UITestHelper.sendString(display, "test");
 
 		assertEquals("test", control.getText());
 		assertEquals("", ridget.getText());
 		assertEquals(null, bean.getProperty());
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "", "test");
 
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
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "test", "test2");
 
 		UITestHelper.sendString(display, "\t");
 
 		verifyPropertyChangeEvents();
 		assertEquals("test2", control.getText());
 		assertEquals("test2", ridget.getText());
 		assertEquals("test2", bean.getProperty());
 	}
 
 	public void testUpdateFromControlUserInputDirectWriting() {
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.setDirectWriting(true);
 
 		Display display = control.getDisplay();
 		UITestHelper.sendString(display, "t");
 
 		assertEquals("t", control.getText());
 		assertEquals("t", ridget.getText());
 		assertEquals("t", bean.getProperty());
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "t", "te");
 
 		UITestHelper.sendString(display, "e");
 
 		verifyPropertyChangeEvents();
 		assertEquals("te", control.getText());
 		assertEquals("te", ridget.getText());
 		assertEquals("te", bean.getProperty());
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "te", "tes");
 
 		UITestHelper.sendString(display, "s");
 
 		verifyPropertyChangeEvents();
 		assertEquals("tes", control.getText());
 		assertEquals("tes", ridget.getText());
 		assertEquals("tes", bean.getProperty());
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "tes", "test");
 
 		UITestHelper.sendString(display, "t");
 
 		verifyPropertyChangeEvents();
 		assertEquals("test", control.getText());
 		assertEquals("test", ridget.getText());
 		assertEquals("test", bean.getProperty());
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "test", "tet");
 
 		UITestHelper.sendKeyAction(display, SWT.ARROW_LEFT);
 		UITestHelper.sendString(display, "\b");
 
 		verifyPropertyChangeEvents();
 		assertEquals("tet", control.getText());
 		assertEquals("tet", ridget.getText());
 		assertEquals("tet", bean.getProperty());
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "tet", "te");
 
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
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "te", "t");
 
 		UITestHelper.sendString(display, "\b");
 
 		verifyPropertyChangeEvents();
 		assertEquals("t", control.getText());
 		assertEquals("t", ridget.getText());
 		assertEquals("t", bean.getProperty());
 	}
 
 	public void testUpdateFromControlMethodCall() throws Exception {
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		((Text) ridget.getUIControl()).setText("test");
 
 		assertEquals("test", control.getText());
 		assertEquals("", ridget.getText());
 		assertEquals(null, bean.getProperty());
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "", "test");
 
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
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "test", "TEST2");
 
 		UITestHelper.sendString(control.getDisplay(), "\t");
 
 		verifyPropertyChangeEvents();
 		assertEquals("TEST2", control.getText());
 		assertEquals("TEST2", ridget.getText());
 		assertEquals("TEST2", bean.getProperty());
 	}
 
 	public void testUpdateFromControlMethodCallDirectWriting() {
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 
 		ridget.setDirectWriting(true);
 
 		((Text) ridget.getUIControl()).setText("t");
 
 		assertEquals("t", control.getText());
 		assertEquals("t", ridget.getText());
 		assertEquals("t", bean.getProperty());
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, ITextRidget.PROPERTY_TEXT, "t", "Test"));
 
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
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, "Test", "Test2");
 
 		UITestHelper.sendKeyAction(control.getDisplay(), SWT.END);
 		UITestHelper.sendString(control.getDisplay(), "2");
 
 		verifyPropertyChangeEvents();
 		assertEquals("Test2", control.getText());
 		assertEquals("Test2", ridget.getText());
 		assertEquals("Test2", bean.getProperty());
 	}
 
 	public void testUpdateFromRidget() throws Exception {
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
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
 
 		expectPropertyChangeEvent(ITextRidget.PROPERTY_TEXT, TEXT_TWO, TEXT_ONE);
 
 		ridget.setText(TEXT_ONE);
 
 		verifyPropertyChangeEvents();
 		assertEquals(TEXT_ONE, control.getText());
 		assertEquals(TEXT_ONE, ridget.getText());
 		assertEquals(TEXT_ONE, bean.getProperty());
 	}
 
 	public void testUpdateFromRidgetOnRebind() throws Exception {
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
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
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
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
 
 		Text text = new Text(getShell(), SWT.MULTI);
 		text.setText("foo");
 		text.setSelection(2, 2);
 
 		ridget.setUIControl(text);
 
 		assertEquals(TEXT_TWO, text.getText());
 		assertEquals(0, text.getCaretPosition());
 	}
 
 	public void testValidationOnUpdateToModel() throws Exception {
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
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
 
 	public void testValidationOnKeyPressedWithBlocking() throws Exception {
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
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
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
 		DateBean dateBean = new DateBean();
 		dateBean.setValue(new Date(0L));
 
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
 		ITextRidget ridget = getRidget();
 		StringBean model = new StringBean();
 		ridget.bindToModel(model, StringBean.PROP_VALUE);
 
 		// this is a blocking rule
 		IValidator onEditRule = new MaxLength(5);
 
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
 		ITextRidget ridget = getRidget();
 		StringBean model = new StringBean();
 		ridget.bindToModel(model, StringBean.PROP_VALUE);
 
 		IValidator onUpdateRule = new MinLength(10);
 
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
 		ITextRidget ridget = getRidget();
 		StringBean model = new StringBean();
 		ridget.bindToModel(model, StringBean.PROP_VALUE);
 
 		IValidator onEditRule = new MaxLength(5);
 
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
 		ITextRidget ridget = getRidget();
 		StringBean model = new StringBean();
 		ridget.bindToModel(model, StringBean.PROP_VALUE);
 
 		IValidator onUpdateRule = new MinLength(10);
 
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
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
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
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
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
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.addValidationRule(new EvenNumberOfCharacters(), ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.setDirectWriting(true);
 
 		ridget.addValidationMessage("TestTextTooShortMessage");
 
 		assertEquals(0, ridget.getMarkers().size());
 
 		UITestHelper.sendString(control.getDisplay(), "a");
 
 		assertEquals(2, ridget.getMarkers().size());
		assertEquals("TestTextTooShortMessage", getMessageMarker(ridget.getMarkers()).getMessage());
 
 		UITestHelper.sendString(control.getDisplay(), "b");
 
 		assertEquals(0, ridget.getMarkers().size());
 	}
 
 	public void testValidationMessageWithOnUpdateRule() throws Exception {
 		Text control = getWidget();
 		ITextRidget ridget = getRidget();
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.addValidationRule(new EvenNumberOfCharacters(), ValidationTime.ON_UPDATE_TO_MODEL);
 		ridget.setDirectWriting(true);
 
 		ridget.addValidationMessage("TestTextTooShortMessage");
 
 		assertEquals(0, ridget.getMarkers().size());
 
 		// \r triggers update
 		UITestHelper.sendString(control.getDisplay(), "a\r");
 
 		assertEquals(2, ridget.getMarkers().size());
 		assertEquals("TestTextTooShortMessage", getMessageMarker(ridget.getMarkers()).getMessage());
 
 		// \r triggers update
 		UITestHelper.sendString(control.getDisplay(), "b\r");
 
 		assertEquals(0, ridget.getMarkers().size());
 	}
 
 	public void testRevalidateOnEditRule() {
 		ITextRidget ridget = getRidget();
 		ValidCharacters numbersOnly = new ValidCharacters(ValidCharacters.VALID_NUMBERS);
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.setText("abc");
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.addValidationRule(numbersOnly, ValidationTime.ON_UI_CONTROL_EDIT);
 
 		assertFalse(ridget.isErrorMarked());
 
 		boolean isOk1 = ridget.revalidate();
 
 		assertFalse(isOk1);
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.removeValidationRule(numbersOnly);
 
 		assertTrue(ridget.isErrorMarked());
 
 		boolean isOk2 = ridget.revalidate();
 
 		assertTrue(isOk2);
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	public void testRevalidateOnUpdateRule() {
 		ITextRidget ridget = getRidget();
 		ValidCharacters numbersOnly = new ValidCharacters(ValidCharacters.VALID_NUMBERS);
 
 		ridget.bindToModel(bean, TestBean.PROPERTY);
 		ridget.setText("abc");
 
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.addValidationRule(numbersOnly, ValidationTime.ON_UPDATE_TO_MODEL);
 
 		assertFalse(ridget.isErrorMarked());
 
 		boolean isOk1 = ridget.revalidate();
 
 		assertFalse(isOk1);
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.removeValidationRule(numbersOnly);
 
 		assertTrue(ridget.isErrorMarked());
 
 		boolean isOk2 = ridget.revalidate();
 
 		assertTrue(isOk2);
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	public void testRevalidateDoesUpdate() {
 		ITextRidget ridget = getRidget();
 		Text control = getWidget();
 		EvenNumberOfCharacters evenChars = new EvenNumberOfCharacters();
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
 		ITextRidget ridget = getRidget();
 		ValidCharacters numbersOnly = new ValidCharacters(ValidCharacters.VALID_NUMBERS);
 
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
 		ITextRidget ridget = getRidget();
 		ValidCharacters numbersOnly = new ValidCharacters(ValidCharacters.VALID_NUMBERS);
 
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
 	 * Tests that the mandatory marker is enabled/disabled when calling {@code
 	 * setText(string)}.
 	 */
 	public void testDisableMandatoryMarkersSetText() {
 		ITextRidget ridget = getRidget();
 		MandatoryMarker mandatoryMarker = new MandatoryMarker();
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
 		ITextRidget ridget = getRidget();
 		MandatoryMarker mandatoryMarker = new MandatoryMarker();
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
 		ITextRidget ridget = getRidget();
 		Text control = getWidget();
 		MandatoryMarker mandatoryMarker = new MandatoryMarker();
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
 		ITextRidget ridget = getRidget();
 		Text control = getWidget();
 		MandatoryMarker mandatoryMarker = new MandatoryMarker();
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
 		ITextRidget ridget = getRidget();
 
 		ridget.setText("foo");
 
 		assertTrue(ridget.isDisableMandatoryMarker());
 
 		ridget.setText("");
 
 		assertFalse(ridget.isDisableMandatoryMarker());
 
 		ridget.setText("  ");
 
 		assertTrue(ridget.isDisableMandatoryMarker());
 	}
 
 	public void testControlNotEditableWithOutputMarker() {
 		ITextRidget ridget = getRidget();
 		Text control = getWidget();
 
 		assertTrue(control.getEditable());
 
 		ridget.setOutputOnly(true);
 
 		assertFalse(control.getEditable());
 
 		ridget.setOutputOnly(false);
 
 		assertTrue(control.getEditable());
 	}
 
 	public void testOutputMultipleSelectionCannotBeChangedFromUI() {
 		ITextRidget ridget = getRidget();
 		Text control = getWidget();
 
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
 
 	/**
 	 * Test that null is not allowed in {@code setText(string)}.
 	 */
 	public void testSetTextNull() {
 		ITextRidget ridget = getRidget();
 		try {
 			ridget.setText(null);
 			fail();
 		} catch (RuntimeException rex) {
 			// ok
 		}
 	}
 
 	public void testDisabledHasNoTextFromRidget() {
 		if (!MarkerSupport.HIDE_DISABLED_RIDGET_CONTENT) {
 			System.out.println("Skipping TextRidgetTest2.testDisabledHasNoTextFromRidget()");
 			return;
 		}
 
 		ITextRidget ridget = getRidget();
 		Text control = getWidget();
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
 		if (!MarkerSupport.HIDE_DISABLED_RIDGET_CONTENT) {
 			System.out.println("Skipping TextRidgetTest2.testDisabledHasNoTextFromModel()");
 			return;
 		}
 
 		ITextRidget ridget = getRidget();
 		Text control = getWidget();
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
 
 	public void testShowOneValidationRuleMessageInTooltip() {
 		ITextRidget ridget = getRidget();
 		TooltipMessageMarkerViewer viewer = new TooltipMessageMarkerViewer();
 		viewer.addRidget(ridget);
 		ridget.setToolTipText("original text");
 		AlwaysWrongValidator rule = new AlwaysWrongValidator();
 		ridget.addValidationRule(rule, ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.revalidate();
 
 		assertEquals("message1", ridget.getToolTipText());
 
 		ridget.revalidate();
 
 		assertEquals("message2", ridget.getToolTipText());
 
 		ridget.removeValidationRule(rule);
 		ridget.revalidate();
 
 		assertEquals("original text", ridget.getToolTipText());
 	}
 
 	public void testShowSeveralValidationRuleMessagesInTooltip() {
 		ITextRidget ridget = getRidget();
 		TooltipMessageMarkerViewer viewer = new TooltipMessageMarkerViewer();
 		viewer.addRidget(ridget);
 		ridget.setToolTipText("original text");
 		AlwaysWrongValidator rule1 = new AlwaysWrongValidator();
 		AlwaysWrongValidator rule2 = new AlwaysWrongValidator();
 		ridget.addValidationRule(rule1, ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.addValidationRule(rule2, ValidationTime.ON_UI_CONTROL_EDIT);
 		ridget.revalidate();
 
 		assertEquals("message1\nmessage1", ridget.getToolTipText());
 
 		ridget.removeValidationRule(rule1);
 		ridget.revalidate();
 
 		assertEquals("message2", ridget.getToolTipText());
 
 		ridget.removeValidationRule(rule2);
 		ridget.revalidate();
 
 		assertEquals("original text", ridget.getToolTipText());
 	}
 
 	// helping methods
 	// ////////////////
 
 	private IMessageMarker getMessageMarker(Collection<? extends IMarker> markers) {
 		for (IMarker marker : markers) {
 			if (marker instanceof IMessageMarker) {
 				return (IMessageMarker) marker;
 			}
 		}
 		return null;
 	}
 
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
 				return ValidationRuleStatus.error(false, "Odd number of characters.", this);
 			}
 			throw new ValidationFailure(getClass().getName() + " can only validate objects of type "
 					+ String.class.getName());
 		}
 
 	}
 
 	// helping classes
 	//////////////////
 
 	private static final class AlwaysWrongValidator implements IValidator {
 		private int i = 0;
 
 		public IStatus validate(Object value) {
 			i++;
 			return ValidationRuleStatus.error(false, "message" + i, this); //$NON-NLS-1$
 		}
 
 	}
 }
