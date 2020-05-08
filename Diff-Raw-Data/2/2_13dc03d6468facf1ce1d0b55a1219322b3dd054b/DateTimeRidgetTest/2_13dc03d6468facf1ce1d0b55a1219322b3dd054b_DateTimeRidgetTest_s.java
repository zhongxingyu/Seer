 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import static org.eclipse.riena.ui.ridgets.IDateTimeRidget.*;
 
 import java.beans.PropertyChangeEvent;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DateTime;
 
 import org.eclipse.riena.beans.common.TypedBean;
 import org.eclipse.riena.core.marker.AbstractMarker;
 import org.eclipse.riena.internal.ui.swt.test.TestUtils;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.ui.core.marker.ValidationTime;
 import org.eclipse.riena.ui.ridgets.IDateTimeRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 
 /**
  * Tests for the class {@link DateTextRidget}.
  */
 public class DateTimeRidgetTest extends AbstractSWTRidgetTest {
 
 	@Override
 	protected IRidget createRidget() {
 		return new DateTimeRidget();
 	}
 
 	@Override
 	protected IDateTimeRidget getRidget() {
 		return (IDateTimeRidget) super.getRidget();
 	}
 
 	@Override
 	protected Control createWidget(final Composite parent) {
 		return new DateTime(getShell(), SWT.DATE | SWT.MEDIUM);
 	}
 
 	@Override
 	protected DateTime getWidget() {
 		return (DateTime) super.getWidget();
 	}
 
 	// test methods
 	///////////////
 
 	public void testRidgetMapping() {
 		final SwtControlRidgetMapper mapper = SwtControlRidgetMapper.getInstance();
 		assertSame(DateTimeRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	public void testSetDate() {
 		final IDateTimeRidget ridget = getRidget();
 		final DateTime control = getWidget();
		final TypedBean<Date> dateBean = new TypedBean<Date>(null);
 
 		expectNoPropertyChangeEvent();
 		ridget.bindToModel(dateBean, TypedBean.PROP_VALUE);
 		verifyPropertyChangeEvents();
 
 		assertEquals(null, ridget.getDate());
 		assertEquals(null, dateBean.getValue());
 
 		final Date date2001 = createDate(2001, 12, 2);
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "dateInternal", null, date2001),
 				new PropertyChangeEvent(ridget, IDateTimeRidget.PROPERTY_DATE, null, date2001));
 		ridget.setDate(date2001);
 		verifyPropertyChangeEvents();
 
 		assertEquals(date2001, ridget.getDate());
 		assertEquals(date2001, dateBean.getValue());
 		assertEquals("12/2/2001", getDate(control));
 
 		final Date date1800 = createDate(1800, 1, 1);
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "dateInternal", date2001, date1800),
 				new PropertyChangeEvent(ridget, IDateTimeRidget.PROPERTY_DATE, date2001, date1800));
 		ridget.setDate(date1800);
 		verifyPropertyChangeEvents();
 
 		assertEquals(date1800, ridget.getDate());
 		assertEquals(date1800, dateBean.getValue());
 		assertEquals("1/1/1800", getDate(control));
 	}
 
 	public void testSetDateNull() {
 		final IDateTimeRidget ridget = getRidget();
 		final DateTime control = getWidget();
 		final TypedBean<Date> dateBean = new TypedBean<Date>(new Date());
 		ridget.bindToModel(dateBean, TypedBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		ridget.setDate(null);
 
 		assertEquals(null, ridget.getDate());
 		assertEquals(asStringEmptyDate(), getDate(control));
 		assertEquals(null, dateBean.getValue());
 	}
 
 	public void testUpdateFromModel() {
 		final IDateTimeRidget ridget = getRidget();
 		final DateTime control = getWidget();
 		final Date date2000 = createDate(2000, 1, 1);
 		final Date date2001 = createDate(2001, 12, 2);
 		final TypedBean<Date> dateBean = new TypedBean<Date>(date2000);
 		ridget.bindToModel(dateBean, TypedBean.PROP_VALUE);
 
 		assertEquals(null, ridget.getDate());
 		assertEquals(asStringEmptyDate(), getDate(control));
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "dateInternal", null, date2000),
 				new PropertyChangeEvent(ridget, IDateTimeRidget.PROPERTY_DATE, null, date2000));
 		ridget.updateFromModel();
 		verifyPropertyChangeEvents();
 
 		assertEquals(date2000, ridget.getDate());
 		assertEquals("1/1/2000", getDate(control));
 
 		expectNoPropertyChangeEvent();
 		dateBean.setValue(date2001);
 		verifyPropertyChangeEvents();
 
 		assertEquals(date2000, ridget.getDate());
 		assertEquals("1/1/2000", getDate(control));
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "dateInternal", date2000, date2001),
 				new PropertyChangeEvent(ridget, IDateTimeRidget.PROPERTY_DATE, date2000, date2001));
 		ridget.updateFromModel();
 		verifyPropertyChangeEvents();
 
 		assertEquals(date2001, ridget.getDate());
 		assertEquals("12/2/2001", getDate(control));
 
 		expectPropertyChangeEvents(new PropertyChangeEvent(ridget, "dateInternal", date2001, null),
 				new PropertyChangeEvent(ridget, IDateTimeRidget.PROPERTY_DATE, date2001, null));
 		dateBean.setValue(null);
 		ridget.updateFromModel();
 		verifyPropertyChangeEvents();
 
 		assertEquals(null, ridget.getDate());
 		assertEquals(asStringEmptyDate(), getDate(control));
 	}
 
 	public void testWidgetModification() {
 		final PropertyChangeEvent[] events = new PropertyChangeEvent[6];
 		String input;
 		final String country = Locale.getDefault().getCountry();
 		if ("DE".equals(country)) {
 			events[0] = createPropertyChangeEvent("dateInternal", 2001, 12, 1, 2001, 12, 10);
 			events[1] = createPropertyChangeEvent(PROPERTY_DATE, 2001, 12, 1, 2001, 12, 10);
 			events[2] = createPropertyChangeEvent("dateInternal", 2001, 12, 10, 2001, 04, 10);
 			events[3] = createPropertyChangeEvent(PROPERTY_DATE, 2001, 12, 10, 2001, 04, 10);
 			events[4] = createPropertyChangeEvent("dateInternal", 2001, 04, 10, 2009, 04, 10);
 			events[5] = createPropertyChangeEvent(PROPERTY_DATE, 2001, 04, 10, 2009, 04, 10);
 			input = "10.04.2009";
 		} else if ("US".equals(country)) {
 			events[0] = createPropertyChangeEvent("dateInternal", 2001, 12, 1, 2001, 4, 1);
 			events[1] = createPropertyChangeEvent(PROPERTY_DATE, 2001, 12, 1, 2001, 4, 1);
 			events[2] = createPropertyChangeEvent("dateInternal", 2001, 4, 1, 2001, 4, 10);
 			events[3] = createPropertyChangeEvent(PROPERTY_DATE, 2001, 4, 1, 2001, 4, 10);
 			events[4] = createPropertyChangeEvent("dateInternal", 2001, 4, 10, 2009, 4, 10);
 			events[5] = createPropertyChangeEvent(PROPERTY_DATE, 2001, 4, 10, 2009, 4, 10);
 			input = "04/10/2009";
 		} else {
 			System.out.println("Skipping DateTimeRidgetTest#testWidgetModification()");
 			return;
 		}
 
 		final IDateTimeRidget ridget = getRidget();
 		final DateTime control = getWidget();
 		final Date date2001 = createDate(2001, 12, 1);
 		final TypedBean<Date> dateBean = new TypedBean<Date>(date2001);
 		ridget.bindToModel(dateBean, TypedBean.PROP_VALUE);
 		ridget.updateFromModel();
 
 		//		ridget.addPropertyChangeListener(new PropertyChangeListener() {
 		//			public void propertyChange(PropertyChangeEvent evt) {
 		//				System.out.println(String.format("%s: %s -> %s", evt.getPropertyName(), evt.getOldValue(), evt
 		//						.getNewValue()));
 		//			}
 		//		});
 
 		assertEquals(date2001, ridget.getDate());
 		assertEquals(date2001, dateBean.getValue());
 		expectPropertyChangeEvents(events);
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), input);
 		verifyPropertyChangeEvents();
 
 		assertEquals("4/10/2009", asString(ridget.getDate()));
 		assertEquals("4/10/2009", asString(dateBean.getValue()));
 	}
 
 	public void testMandatoryMarker() {
 		final IDateTimeRidget ridget = getRidget();
 		ridget.setMandatory(true);
 
 		assertTrue(ridget.isDisableMandatoryMarker());
 		TestUtils.assertMandatoryMarker(ridget, 1, true);
 	}
 
 	public void testControlEnablement() {
 		final IDateTimeRidget ridget = getRidget();
 		final DateTime control = getWidget();
 
 		assertTrue(control.isEnabled());
 
 		ridget.setEnabled(false);
 
 		assertFalse(control.isEnabled());
 
 		ridget.setEnabled(true);
 
 		assertTrue(control.isEnabled());
 
 		ridget.setOutputOnly(true);
 		ridget.setEnabled(false);
 		ridget.setEnabled(true);
 
 		assertFalse(control.isEnabled());
 
 		ridget.setEnabled(false);
 		ridget.setOutputOnly(false);
 
 		assertFalse(control.isEnabled());
 
 		ridget.setEnabled(true);
 
 		assertTrue(control.isEnabled());
 	}
 
 	public void testValidationUpdateFromModelOnUpdateRules() {
 		handleValidationUpdateFromModel(ValidationTime.ON_UPDATE_TO_MODEL);
 	}
 
 	public void testValidationUpdateFromModelOnEditRules() {
 		handleValidationUpdateFromModel(ValidationTime.ON_UI_CONTROL_EDIT);
 	}
 
 	public void testValidationSetDateOnUpdateRules() {
 		handleValidationSetDate(ValidationTime.ON_UPDATE_TO_MODEL);
 	}
 
 	public void testValidationSetDateOnEditRules() {
 		handleValidationSetDate(ValidationTime.ON_UI_CONTROL_EDIT);
 	}
 
 	public void testValidationWidgetModificationOnUpdateRules() {
 		final String country = Locale.getDefault().getCountry();
 		if ("DE".equals(country)) {
 			handleValidationWidgetModification(ValidationTime.ON_UPDATE_TO_MODEL, "10.04.2009");
 		} else if ("US".equals(country)) {
 			handleValidationWidgetModification(ValidationTime.ON_UPDATE_TO_MODEL, "04/10/2009");
 		} else {
 			System.out.println("Skipping DateTimeRidgetTest#testValidation_WidgetModification_onUpdateRules");
 		}
 	}
 
 	public void testValidationWidgetModificationOnEditRules() {
 		final String country = Locale.getDefault().getCountry();
 		if ("DE".equals(country)) {
 			handleValidationWidgetModification(ValidationTime.ON_UI_CONTROL_EDIT, "10.04.2009");
 		} else if ("US".equals(country)) {
 			handleValidationWidgetModification(ValidationTime.ON_UI_CONTROL_EDIT, "04/10/2009");
 		} else {
 			System.out.println("Skipping DateTimeRidgetTest#testValidation_WidgetModification_OnEditRules");
 		}
 	}
 
 	public void testRevalidate() {
 		final IDateTimeRidget ridget = getRidget();
 		final FTValidator validator = new FTValidator(new Date(99));
 		ridget.addValidationRule(validator, ValidationTime.ON_UPDATE_TO_MODEL);
 		ridget.setDate(new Date(99));
 
 		assertTrue(ridget.isErrorMarked());
 
 		ridget.removeValidationRule(validator);
 
 		assertFalse(ridget.isErrorMarked()); // since 1.2: remove updates immediately
 
 		ridget.revalidate();
 
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	// helping methods
 	//////////////////
 
 	private String asString(final Date date) {
 		String result = "null";
 		if (date != null) {
 			final Calendar cal = Calendar.getInstance();
 			cal.clear();
 			cal.setTime(date);
 			final int year = cal.get(Calendar.YEAR);
 			final int month = cal.get(Calendar.MONTH) + 1;
 			final int day = cal.get(Calendar.DAY_OF_MONTH);
 			result = String.format("%s/%s/%s", month, day, year);
 		}
 		return result;
 	}
 
 	private String asStringEmptyDate() {
 		return asString(new Date(0));
 	}
 
 	private Date createDate(final int year, final int month, final int day) {
 		final Calendar cal = Calendar.getInstance();
 		cal.clear();
 		cal.set(year, month - 1, day, 0, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		return cal.getTime();
 	}
 
 	private PropertyChangeEvent createPropertyChangeEvent(final String propertyName, final int oldYear,
 			final int oldMonth, final int oldDay, final int newYear, final int newMonth, final int newDay) {
 		final Date oldDate = createDate(oldYear, oldMonth, oldDay);
 		final Date newDate = createDate(newYear, newMonth, newDay);
 		return new PropertyChangeEvent(getRidget(), propertyName, oldDate, newDate);
 	}
 
 	private String getDate(final DateTime control) {
 		final int year = control.getYear();
 		final int month = control.getMonth() + 1;
 		final int day = control.getDay();
 		return String.format("%s/%s/%s", month, day, year);
 	}
 
 	private void handleValidationUpdateFromModel(final ValidationTime time) {
 		final IDateTimeRidget ridget = getRidget();
 		final FTValidator validator = new FTValidator(new Date(99));
 		ridget.addValidationRule(validator, time);
 		final TypedBean<Date> dateBean = new TypedBean<Date>(new Date(0));
 		ridget.bindToModel(dateBean, TypedBean.PROP_VALUE);
 
 		int count = validator.count;
 		ridget.updateFromModel();
 
 		assertTrue(count < validator.count);
 		assertFalse(ridget.isErrorMarked());
 
 		count = validator.count;
 		dateBean.setValue(new Date(99));
 		ridget.updateFromModel();
 
 		assertTrue(count < validator.count);
 		assertTrue(ridget.isErrorMarked());
 		assertEquals(new Date(99), ridget.getDate());
 
 		count = validator.count;
 		dateBean.setValue(new Date(0));
 		ridget.updateFromModel();
 
 		assertTrue(count < validator.count);
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	private void handleValidationSetDate(final ValidationTime time) {
 		final IDateTimeRidget ridget = getRidget();
 		final FTValidator validator = new FTValidator(new Date(99));
 		ridget.addValidationRule(validator, time);
 
 		assertEquals(0, validator.count);
 
 		ridget.setDate(new Date(0));
 
 		assertEquals(1, validator.count);
 		assertFalse(ridget.isErrorMarked());
 
 		ridget.setDate(new Date(99));
 
 		assertEquals(2, validator.count);
 		assertTrue(ridget.isErrorMarked());
 		assertEquals(new Date(99), ridget.getDate());
 
 		ridget.setDate(new Date(0));
 
 		assertEquals(3, validator.count);
 		assertFalse(ridget.isErrorMarked());
 	}
 
 	private void handleValidationWidgetModification(final ValidationTime time, final String input) {
 		final IDateTimeRidget ridget = getRidget();
 		final FTValidator validator = new FTValidator("4/10/2009");
 		final DateTime control = getWidget();
 		ridget.addValidationRule(validator, time);
 		final TypedBean<Date> dateBean = new TypedBean<Date>(new Date(0));
 		ridget.bindToModel(dateBean, TypedBean.PROP_VALUE);
 
 		final int count = validator.count;
 		assertFalse(ridget.isErrorMarked());
 
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), input);
 
 		assertTrue(count < validator.count);
 		assertTrue(ridget.isErrorMarked());
 	}
 
 	/**
 	 * As per Bug 323899
 	 */
 	public void testSetOutputOnly() {
 		final IDateTimeRidget ridget = (IDateTimeRidget) createRidget();
 		final DateTime control = (DateTime) createWidget(getShell());
 
 		ridget.setOutputOnly(true);
 
 		assertTrue(ridget.isOutputOnly());
 		assertTrue(control.isEnabled());
 
 		ridget.setUIControl(control);
 		ridget.addMarker(new AbstractMarker() {
 		});
 
 		assertTrue(ridget.isOutputOnly());
 		assertFalse(control.isEnabled());
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * IValidator with counter. Used for testing.
 	 */
 	private final class FTValidator implements IValidator {
 
 		private int count = 0;
 		private Date errorDate;
 		private String errorString;
 
 		FTValidator(final Date errorValue) {
 			this.errorDate = errorValue;
 		}
 
 		FTValidator(final String errorValue) {
 			this.errorString = errorValue;
 		}
 
 		public IStatus validate(final Object value) {
 			count++;
 			IStatus result = ValidationStatus.ok();
 			if (errorDate != null && errorDate.equals(value)) {
 				result = ValidationStatus.error("error");
 			}
 			if (errorString != null && errorString.equals(asString((Date) value))) {
 				result = ValidationStatus.error("error");
 			}
 			// System.out.println("validate: " + count + " " + value + " " + result);
 			return result;
 		}
 	}
 }
