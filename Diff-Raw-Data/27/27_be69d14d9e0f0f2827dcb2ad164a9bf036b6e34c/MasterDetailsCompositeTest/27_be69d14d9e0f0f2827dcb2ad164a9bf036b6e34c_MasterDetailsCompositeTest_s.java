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
 package org.eclipse.riena.ui.swt;
 
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.core.test.collect.UITestCase;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.ui.common.IComplexComponent;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Tests of the class {@link MasterDetailsComposite}
  */
 @UITestCase
 public class MasterDetailsCompositeTest extends TestCase {
 
 	private Shell shell;
 	private MasterDetailsComposite masterDetails;
 
 	@Override
 	protected void setUp() throws Exception {
 		shell = new Shell();
 		masterDetails = new MasterDetailsComposite(shell, SWT.NONE);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		SwtUtilities.dispose(shell);
 	}
 
 	public void testGetUIControlsWithBindingProperty() {
 		final Composite parent = masterDetails.getDetails();
 		final Text txtFirstName = UIControlsFactory.createText(parent, SWT.BORDER, "txtFirstName");
 		final Text txtLastName = UIControlsFactory.createText(parent, SWT.BORDER, "txtLastName");
 		final Text txtWithoutID = UIControlsFactory.createText(parent, SWT.BORDER);
 
 		final List<Object> controls = masterDetails.getUIControls();
 
 		assertEquals(6, controls.size());
 		assertTrue(controls.contains(txtFirstName));
 		assertTrue(controls.contains(txtLastName));
 		assertFalse(controls.contains(txtWithoutID));
 
 		final List<Object> controls2 = masterDetails.getUIControls();
 		assertEquals(6, controls2.size());
 	}
 
 	public void testGetUIControlsWithBindingPropertyFromComposite() {
 		final Composite parent = new Composite(masterDetails.getDetails(), SWT.NONE);
 		final Text txtFirstName = UIControlsFactory.createText(parent, SWT.BORDER, "txtFirstName");
 		final Text txtLastName = UIControlsFactory.createText(parent, SWT.BORDER, "txtLastName");
 		final Text txtWithoutID = UIControlsFactory.createText(parent, SWT.BORDER);
 
 		final List<Object> controls = masterDetails.getUIControls();
 
 		assertTrue(controls.contains(txtFirstName));
 		assertTrue(controls.contains(txtLastName));
 		assertFalse(controls.contains(txtWithoutID));
 	}
 
 	/**
 	 * As per bug 297524.
 	 */
 	public void testGetUIControlsThatAreComposites() {
 		final Composite parent = masterDetails.getDetails();
 		final Combo combo = UIControlsFactory.createCombo(parent, "combo");
 		final CCombo ccombo = UIControlsFactory.createCCombo(parent, "ccombo");
 		final DateTime datetime = UIControlsFactory.createDate(parent, SWT.NONE, "datetime");
 
 		List<Object> controls = masterDetails.getUIControls();
 
 		assertTrue(controls.contains(combo));
 		assertTrue(controls.contains(ccombo));
 		assertTrue(controls.contains(datetime));
 
 		final Composite composite = new Composite(parent, SWT.NONE);
 		final Combo combo2 = UIControlsFactory.createCombo(composite, "combo2");
 
 		controls = masterDetails.getUIControls();
 
 		assertFalse(controls.contains(composite));
 		assertTrue(controls.contains(combo2));
 	}
 
 	public void testSkipIComplexComponent() {
 		final MasterDetailsComposite widget = new MasterDetailsComposite(shell, SWT.NONE) {
 			@Override
 			protected void createDetails(final Composite details) {
 				final IComplexComponent complex = new TestComplexComponent(details, SWT.NONE);
 				addUIControl(complex, "complex");
 			}
 		};
 		final List<Object> controls = widget.getUIControls();
 
 		System.out.println(Arrays.toString(controls.toArray()));
 
 		// should contain the complex control, but not it's children 
 		// even if they have an id
 		assertEquals(5, controls.size());
 		assertTrue(controls.get(0) instanceof Table);
 		assertTrue(controls.get(1) instanceof Button);
 		assertTrue(controls.get(2) instanceof Button);
 		assertTrue(controls.get(3) instanceof Button);
 		assertTrue(controls.get(4) instanceof TestComplexComponent);
 	}
 
 	public void testSetMargins() {
 		final MasterDetailsComposite widget = new MasterDetailsComposite(shell, SWT.NONE);
 
		GridLayout layout = (GridLayout) widget.getLayout();
 
 		assertEquals(0, layout.marginHeight);
 		assertEquals(0, layout.marginWidth);
 
 		widget.setMargins(4, 5);
		layout = (GridLayout) widget.getLayout();
 
 		assertEquals(4, layout.marginHeight);
 		assertEquals(5, layout.marginWidth);
 	}
 
 	public void testGetMargins() {
 		final MasterDetailsComposite widget = new MasterDetailsComposite(shell, SWT.NONE);
 
 		assertEquals(0, widget.getMargins().x);
 		assertEquals(0, widget.getMargins().y);
 
 		widget.setMargins(4, 5);
 
 		assertEquals(4, widget.getMargins().x);
 		assertEquals(5, widget.getMargins().y);
 	}
 
 	public void testGetSetSpacing() {
 		final MasterDetailsComposite widget = new MasterDetailsComposite(shell, SWT.NONE);
 
 		assertEquals(0, widget.getSpacing().x);
 		assertEquals(5, widget.getSpacing().y);
 
 		widget.setSpacing(3, 4);
 
 		assertEquals(3, widget.getSpacing().x);
 		assertEquals(4, widget.getSpacing().y);
 	}
 
 	public void testCheckButton() {
 		final MasterDetailsComposite widget = new MasterDetailsComposite(shell, SWT.NONE);
 
 		final Button button = new Button(shell, SWT.PUSH);
 		final ImageButton imageButton = new ImageButton(shell, SWT.NONE);
 
 		ReflectionUtils.invokeHidden(widget, "checkButton", button, true);
 		ReflectionUtils.invokeHidden(widget, "checkButton", button, false);
 		ReflectionUtils.invokeHidden(widget, "checkButton", imageButton, true);
 		ReflectionUtils.invokeHidden(widget, "checkButton", imageButton, false);
 
 		try {
 			ReflectionUtils.invokeHidden(widget, "checkButton", new Object(), true);
 			fail();
 		} catch (final RuntimeException rex) {
 			// ok
 		}
 	}
 
 	// helping classes
 	//////////////////
 
 	private static class TestComplexComponent extends Composite implements IComplexComponent {
 		private final Label label;
 
 		public TestComplexComponent(final Composite parent, final int style) {
 			super(parent, style);
 			label = UIControlsFactory.createLabel(this, "txt", "label");
 		}
 
 		public List<Object> getUIControls() {
 			return Arrays.asList(new Object[] { label });
 		}
 	}
 }
