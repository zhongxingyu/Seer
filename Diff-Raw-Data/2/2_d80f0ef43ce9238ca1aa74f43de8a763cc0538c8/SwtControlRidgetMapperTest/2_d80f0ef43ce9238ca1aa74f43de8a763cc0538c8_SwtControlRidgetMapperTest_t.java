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
 package org.eclipse.riena.navigation.ui.swt.binding;
 
 import java.beans.PropertyChangeListener;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.ui.ridgets.swt.ActionRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.LabelRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.ToggleButtonRidget;
 import org.eclipse.riena.tests.RienaTestCase;
 import org.eclipse.riena.tests.collect.UITestCase;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.listener.IFocusListener;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper.Mapping;
 import org.eclipse.riena.ui.ridgets.uibinding.IMappingCondition;
 
 /**
  * Tests of the class <code>SwtControlRidgetMapper</code>
  */
 @UITestCase
 public class SwtControlRidgetMapperTest extends RienaTestCase {
 
 	private SwtControlRidgetMapper mapper;
 	private Shell shell;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		mapper = SwtControlRidgetMapper.getInstance();
 		shell = new Shell();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 
 		// Create new instance of SwtControlRidgetMapper to start with initial mappings only (not additional mappings added in previous test cases)
		ReflectionUtils.setHidden(SwtControlRidgetMapper.class, "instance", ReflectionUtils.newInstanceHidden(
 				SwtControlRidgetMapper.class, new Object[0]));
 		mapper = null;
 		shell.dispose();
 		shell = null;
 
 		super.tearDown();
 	}
 
 	/**
 	 * Tests the method
 	 * <code>addMapping(Class<? extends Widget> , Class<? extends IRidget> )</code>
 	 * .
 	 * 
 	 * @throws Exception
 	 *             - handled by JUnit
 	 */
 	public void testAddMapping() throws Exception {
 
 		mapper.addMapping(MockComposite.class, MockRidget.class);
 
 		Class<? extends IRidget> ridget = mapper.getRidgetClass(MockComposite.class);
 		assertNotNull(ridget);
 		assertEquals(MockRidget.class.getName(), ridget.getName());
 
 	}
 
 	/**
 	 * Tests the method
 	 * <code>addMapping(Class<? extends Widget> , Class<? extends IRidget> , int )</code>
 	 * .
 	 * 
 	 * @throws Exception
 	 *             - handled by JUnit
 	 */
 	public void testAddMappingSwtStyle() throws Exception {
 
 		mapper.addMapping(MockComposite.class, MockRidget.class);
 		mapper.addMapping(MockComposite.class, MockRidget2.class, SWT.BORDER);
 
 		Class<? extends IRidget> ridget = mapper.getRidgetClass(MockComposite.class);
 		assertNotNull(ridget);
 		assertEquals(MockRidget.class.getName(), ridget.getName());
 
 		MockComposite widget = new MockComposite(shell, SWT.BORDER);
 		ridget = mapper.getRidgetClass(widget);
 		assertNotNull(ridget);
 		assertEquals(MockRidget2.class.getName(), ridget.getName());
 		widget.dispose();
 
 	}
 
 	/**
 	 * Tests the method
 	 * {@link SwtControlRidgetMapper#addMapping(Class, Class, IMappingCondition)}
 	 * .
 	 */
 	public void testAddMappingWithCondition() {
 
 		FTMappingCondition condition1 = new FTMappingCondition(false);
 		FTMappingCondition condition2 = new FTMappingCondition(false);
 
 		mapper.addMapping(MockComposite.class, MockRidget.class, condition1);
 		mapper.addMapping(MockComposite.class, MockRidget2.class, condition2);
 
 		MockComposite widget = new MockComposite(shell, SWT.NONE);
 		try {
 			condition1.setMatch(true);
 
 			Class<? extends IRidget> ridgetClass = mapper.getRidgetClass(widget);
 			assertNotNull(ridgetClass);
 			assertEquals(MockRidget.class.getName(), ridgetClass.getName());
 
 			condition1.setMatch(false);
 			condition2.setMatch(true);
 
 			ridgetClass = mapper.getRidgetClass(widget);
 			assertNotNull(ridgetClass);
 			assertEquals(MockRidget2.class.getName(), ridgetClass.getName());
 
 			condition2.setMatch(false);
 
 			try {
 				mapper.getRidgetClass(widget);
 				fail();
 			} catch (BindingException bex) {
 				ok();
 			}
 		} finally {
 			widget.dispose();
 		}
 	}
 
 	/**
 	 * Tests the method <code>getRidgetClass(Class<? extends Widget>)</code>.
 	 * 
 	 * @throws Exception
 	 *             - handled by JUnit
 	 */
 	public void testGetRidgetClass() throws Exception {
 
 		Class<? extends IRidget> ridget = mapper.getRidgetClass(Label.class);
 		assertNotNull(ridget);
 		assertEquals(LabelRidget.class.getName(), ridget.getName());
 
 		try {
 			mapper.getRidgetClass(MockComposite.class);
 			fail("BindingException expected");
 		} catch (BindingException e) {
 			ok("BindingException expected");
 		}
 
 	}
 
 	/**
 	 * Tests the method <code>getRidgetClass(Widget)</code>.
 	 * 
 	 * @throws Exception
 	 *             - handled by JUnit
 	 */
 	public void testGetRidgetClassWidget() throws Exception {
 
 		Button button = new Button(shell, SWT.DEFAULT);
 		Class<? extends IRidget> ridget = mapper.getRidgetClass(button);
 		assertNotNull(ridget);
 		assertEquals(ActionRidget.class.getName(), ridget.getName());
 
 		button = new Button(shell, SWT.FLAT);
 		ridget = mapper.getRidgetClass(button);
 		assertNotNull(ridget);
 		assertEquals(ActionRidget.class.getName(), ridget.getName());
 
 		button = new Button(shell, SWT.CHECK);
 		ridget = mapper.getRidgetClass(button);
 		assertNotNull(ridget);
 		assertEquals(ToggleButtonRidget.class.getName(), ridget.getName());
 
 	}
 
 	/**
 	 * Tests the method <code>isMatching(Class<? extends Widget>)</code>.
 	 * 
 	 * @throws Exception
 	 *             - handled by JUnit
 	 */
 	public void testIsMatching() throws Exception {
 
 		Mapping mapping = new Mapping(MockComposite.class, MockRidget.class);
 		assertTrue(mapping.isMatching(MockComposite.class));
 		assertFalse(mapping.isMatching(MockComposite2.class));
 
 		mapping = new Mapping(MockComposite.class, MockRidget.class, SWT.CHECK);
 		assertFalse(mapping.isMatching(MockComposite.class));
 		assertFalse(mapping.isMatching(MockComposite2.class));
 
 		mapping = new Mapping(MockComposite.class, MockRidget.class, new FTMappingCondition(true));
 		assertFalse(mapping.isMatching(MockComposite.class));
 		assertFalse(mapping.isMatching(MockComposite2.class));
 	}
 
 	/**
 	 * Tests the method <code>isMatching(Widget>)</code>.
 	 * 
 	 * @throws Exception
 	 *             - handled by JUnit
 	 */
 	public void testIsMatchingWidget() throws Exception {
 
 		Mapping mapping = new Mapping(MockComposite.class, MockRidget.class);
 		MockComposite comp = new MockComposite(shell, SWT.DEFAULT);
 		assertTrue(mapping.isMatching(comp));
 		comp.dispose();
 		MockComposite2 comp2 = new MockComposite2(shell, SWT.DEFAULT);
 		assertFalse(mapping.isMatching(comp2));
 		comp2.dispose();
 
 		mapping = new Mapping(MockComposite.class, MockRidget.class, SWT.ABORT);
 		comp = new MockComposite(shell, SWT.ALPHA);
 		assertFalse(mapping.isMatching(comp));
 		comp.dispose();
 		comp = new MockComposite(shell, SWT.ABORT);
 		assertTrue(mapping.isMatching(comp));
 		comp.dispose();
 		comp = new MockComposite(shell, SWT.ABORT | SWT.ALT);
 		assertTrue(mapping.isMatching(comp));
 		comp.dispose();
 
 		FTMappingCondition condition = new FTMappingCondition(true);
 		mapping = new Mapping(MockComposite.class, MockRidget.class, condition);
 		comp = new MockComposite(shell, SWT.DEFAULT);
 		try {
 			assertTrue(mapping.isMatching(comp));
 			condition.setMatch(false);
 			assertFalse(mapping.isMatching(comp));
 		} finally {
 			comp.dispose();
 		}
 
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Simple implementation of an IMappingCondition used for testing purposes.
 	 * USe the {@link #setMatch(boolean)} to change the behavior of a condition.
 	 */
 	private static final class FTMappingCondition implements IMappingCondition {
 
 		private boolean isMatch = true;
 
 		public FTMappingCondition(boolean isMatch) {
 			this.isMatch = isMatch;
 		}
 
 		void setMatch(boolean isMatch) {
 			this.isMatch = isMatch;
 		}
 
 		public boolean isMatch(Object widget) {
 			return isMatch;
 		}
 
 	}
 
 	/**
 	 * Mock extention of <code>Composite</code>.
 	 */
 	private static final class MockComposite extends Composite {
 
 		public MockComposite(Composite parent, int style) {
 			super(parent, style);
 		}
 
 	}
 
 	/**
 	 * Another mock extention of <code>Composite</code>.
 	 */
 	private static final class MockComposite2 extends Composite {
 
 		public MockComposite2(Composite parent, int style) {
 			super(parent, style);
 		}
 
 	}
 
 	/**
 	 * Mock implementation of ridget.
 	 */
 	private static final class MockRidget implements IRidget {
 
 		public Object getUIControl() {
 			return null;
 		}
 
 		public void setUIControl(Object uiControl) {
 		}
 
 		public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
 		}
 
 		public void addPropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener) {
 		}
 
 		public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
 		}
 
 		public void removePropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener) {
 		}
 
 		public boolean isVisible() {
 			return false;
 		}
 
 		public void setVisible(boolean visible) {
 		}
 
 		public boolean isEnabled() {
 			return false;
 		}
 
 		public void setEnabled(boolean enabled) {
 		}
 
 		public void addFocusListener(IFocusListener listener) {
 		}
 
 		public void removeFocusListener(IFocusListener listener) {
 		}
 
 		public void updateFromModel() {
 		}
 
 		public void requestFocus() {
 		}
 
 		public boolean hasFocus() {
 			return false;
 		}
 
 		public boolean isFocusable() {
 			return false;
 		}
 
 		public void setFocusable(boolean focusable) {
 		}
 
 		public String getToolTipText() {
 			return null;
 		}
 
 		public void setToolTipText(String toolTipText) {
 		}
 
 		public boolean isBlocked() {
 			return false;
 		}
 
 		public void setBlocked(boolean blocked) {
 		}
 
 		public String getID() {
 			return null;
 		}
 	}
 
 	/**
 	 * Another mock implementation of ridget.
 	 */
 	private static final class MockRidget2 implements IRidget {
 
 		public Object getUIControl() {
 			return null;
 		}
 
 		public void setUIControl(Object uiControl) {
 		}
 
 		public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
 		}
 
 		public void addPropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener) {
 		}
 
 		public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
 		}
 
 		public void removePropertyChangeListener(String propertyName, PropertyChangeListener propertyChangeListener) {
 		}
 
 		public boolean isVisible() {
 			return false;
 		}
 
 		public void setVisible(boolean visible) {
 		}
 
 		public boolean isEnabled() {
 			return false;
 		}
 
 		public void setEnabled(boolean enabled) {
 		}
 
 		public void addFocusListener(IFocusListener listener) {
 		}
 
 		public void removeFocusListener(IFocusListener listener) {
 		}
 
 		public void updateFromModel() {
 		}
 
 		public void requestFocus() {
 		}
 
 		public boolean hasFocus() {
 			return false;
 		}
 
 		public boolean isFocusable() {
 			return false;
 		}
 
 		public void setFocusable(boolean focusable) {
 		}
 
 		public String getToolTipText() {
 			return null;
 		}
 
 		public void setToolTipText(String toolTipText) {
 		}
 
 		public boolean isBlocked() {
 			return false;
 		}
 
 		public void setBlocked(boolean blocked) {
 		}
 
 		public String getID() {
 			return null;
 		}
 	}
 
 }
