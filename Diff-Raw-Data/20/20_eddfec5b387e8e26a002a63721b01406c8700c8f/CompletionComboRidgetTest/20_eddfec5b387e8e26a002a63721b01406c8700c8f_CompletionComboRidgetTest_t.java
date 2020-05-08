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
 
 import java.util.List;
 
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.beans.common.StringManager;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.ui.swt.test.UITestHelper;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.listener.ISelectionListener;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.SwtControlRidgetMapper;
 import org.eclipse.riena.ui.swt.CompletionCombo;
 import org.eclipse.riena.ui.swt.CompletionCombo.AutoCompletionMode;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Tests of the class {@link ComboRidget}.
  */
 public class CompletionComboRidgetTest extends AbstractComboRidgetTest {
 
 	@Override
 	protected Control createWidget(final Composite parent) {
 		return UIControlsFactory.createCompletionCombo(parent);
 	}
 
 	@Override
 	protected Control createWidget(final Composite parent, final int style) {
 		return UIControlsFactory.createCompletionCombo(parent, style);
 	}
 
 	@Override
 	protected IRidget createRidget() {
 		return new CompletionComboRidget();
 	}
 
 	// testing methods
 	//////////////////
 
 	public void testRidgetMapping() {
 		final SwtControlRidgetMapper mapper = SwtControlRidgetMapper.getInstance();
 		assertTrue(getWidget() instanceof CompletionCombo);
 		assertSame(CompletionComboRidget.class, mapper.getRidgetClass(getWidget()));
 	}
 
 	public void testMandatoryChangesTextBackgroundOnly() {
 		final IComboRidget ridget = getRidget();
 		final CompletionCombo control = (CompletionCombo) getWidget();
 		final Color bg = control.getBackground();
 		final Color mandatory = new Color(null, 255, 255, 175);
 
 		try {
 			assertEquals(bg, control.getBackground());
 			assertEquals(bg, control.getTextBackground());
 			assertEquals(bg, control.getListBackground());
 
 			ridget.setMandatory(true);
 
 			assertEquals(bg, control.getBackground());
 			assertEquals(mandatory, control.getTextBackground());
 			assertEquals(bg, control.getListBackground());
 
 			ridget.setMandatory(false);
 
 			assertEquals(bg, control.getBackground());
 			assertEquals(bg, control.getTextBackground());
 			assertEquals(bg, control.getListBackground());
 		} finally {
 			mandatory.dispose();
 		}
 	}
 
 	/**
 	 * As per Bug 337926
 	 */
 	public void testSelectionEventOnKeyboardEntryWithACMNoMismatch() {
 		final IComboRidget ridget = getRidget();
 		final CompletionCombo control = (CompletionCombo) getWidget();
 		control.setAutoCompletionMode(AutoCompletionMode.NO_MISSMATCH);
 		final StringManager aManager = new StringManager("A1", "A2", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		final FTSelectionListener listener = new FTSelectionListener();
 		control.addSelectionListener(listener);
 		ridget.addSelectionListener(listener);
 
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), "A");
 
 		listener.assertSelection(1, "", "A1");
 
 		UITestHelper.sendString(control.getDisplay(), "2");
 
 		listener.assertSelection(2, "A1", "A2");
 
 		UITestHelper.sendString(control.getDisplay(), "x");
 
 		listener.assertSelection(2, "A1", "A2");
 
 		UITestHelper.sendString(control.getDisplay(), "\b");
 
 		listener.assertSelection(3, "A2", "A1");
 
 		UITestHelper.sendString(control.getDisplay(), "\b");
 
 		listener.assertSelection(4, "A1", "");
 
 		UITestHelper.sendString(control.getDisplay(), "\b");
 
 		listener.assertSelection(4, "A1", "");
 
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 
 		listener.assertSelection(5, "", "A1");
 	}
 
 	/**
 	 * As per Bug 337926
 	 */
 	public void testSelectionEventOnKeyboardEntryWithACMAllowMismatch() {
 		final IComboRidget ridget = getRidget();
 		final CompletionCombo control = (CompletionCombo) getWidget();
 		control.setAutoCompletionMode(AutoCompletionMode.ALLOW_MISSMATCH);
 		final StringManager aManager = new StringManager("A1", "A2", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		final FTSelectionListener listener = new FTSelectionListener();
 		control.addSelectionListener(listener);
 		ridget.addSelectionListener(listener);
 
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), "A");
 
 		listener.assertSelection(1, "", "A1");
 
 		UITestHelper.sendString(control.getDisplay(), "2");
 
 		listener.assertSelection(2, "A1", "A2");
 
 		UITestHelper.sendString(control.getDisplay(), "b");
 
 		listener.assertSelection(3, "A2", "");
 
 		UITestHelper.sendString(control.getDisplay(), "c");
 
 		listener.assertSelection(3, "A2", "");
 
 		UITestHelper.sendString(control.getDisplay(), "\b\b");
 
 		listener.assertSelection(4, "", "A2");
 
 		UITestHelper.sendString(control.getDisplay(), "\b");
 
 		listener.assertSelection(5, "A2", "A1");
 
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 
		listener.assertSelection(6, "A1", "A2");
 	}
 
 	/**
 	 * As per Bug 337926
 	 */
 	public void testSelectionEventOnKeyboardEntryWithACMFirstLetterMatch() {
 		final IComboRidget ridget = getRidget();
 		final CompletionCombo control = (CompletionCombo) getWidget();
 		control.setAutoCompletionMode(AutoCompletionMode.FIRST_LETTER_MATCH);
 		final StringManager aManager = new StringManager("A1", "A2", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		final FTSelectionListener listener = new FTSelectionListener();
 		control.addSelectionListener(listener);
 		ridget.addSelectionListener(listener);
 
 		control.setFocus();
 		UITestHelper.sendString(control.getDisplay(), "a");
 
 		listener.assertSelection(1, "", "A1");
 
 		UITestHelper.sendString(control.getDisplay(), "a");
 
 		listener.assertSelection(2, "A1", "A2");
 
 		UITestHelper.sendString(control.getDisplay(), "b");
 
 		listener.assertSelection(3, "A2", "B");
 
 		UITestHelper.sendString(control.getDisplay(), "Q");
 
 		listener.assertSelection(3, "A2", "B");
 
 		UITestHelper.sendString(control.getDisplay(), "\b");
 
		listener.assertSelection(3, "A2", "B");
 
 		UITestHelper.sendString(control.getDisplay(), "\b");
 
		listener.assertSelection(3, "A2", "B");
 
 		UITestHelper.sendKeyAction(control.getDisplay(), UITestHelper.KC_ARROW_DOWN);
 
		listener.assertSelection(4, "B", "C");
 	}
 
 	/**
 	 * As per Bug 337926
 	 */
 	public void testSelectionEventOnCut() {
 		final IComboRidget ridget = getRidget();
 		final CompletionCombo control = (CompletionCombo) getWidget();
 		final Text textControl = (Text) ReflectionUtils.invokeHidden(control, "getTextControl", (Object[]) null);
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		control.setAutoCompletionMode(AutoCompletionMode.ALLOW_MISSMATCH);
 		control.setText("Alpha");
 		final FTSelectionListener listener = new FTSelectionListener();
 		control.addSelectionListener(listener);
 		ridget.addSelectionListener(listener);
 
 		control.setSelection(new Point(1, 5));
 		ReflectionUtils.invokeHidden(control, "handleCut", (Object[]) null);
 
 		listener.assertSelection(1, "", "A");
 
 		textControl.selectAll();
 		ReflectionUtils.invokeHidden(control, "handleCut", (Object[]) null);
 
 		listener.assertSelection(2, "A", "");
 	}
 
 	/**
 	 * As per Bug 337926
 	 */
 	public void testSelectionEventOnPaste() {
 		final IComboRidget ridget = getRidget();
 		final CompletionCombo control = (CompletionCombo) getWidget();
 		final Text textControl = (Text) ReflectionUtils.invokeHidden(control, "getTextControl", (Object[]) null);
 		final StringManager aManager = new StringManager("A", "B", "C", "D", "E");
 		ridget.bindToModel(aManager, "items", String.class, null, aManager, "selectedItem");
 		ridget.updateFromModel();
 
 		final FTSelectionListener listener = new FTSelectionListener();
 		control.addSelectionListener(listener);
 		ridget.addSelectionListener(listener);
 
 		final Clipboard clip = new Clipboard(control.getDisplay());
 		final TextTransfer textTransfer = TextTransfer.getInstance();
 		clip.setContents(new Object[] { "A" }, new Transfer[] { textTransfer });
 		textControl.selectAll();
 		ReflectionUtils.invokeHidden(control, "handlePaste", (Object[]) null);
 
 		listener.assertSelection(1, "", "A");
 
 		clip.setContents(new Object[] { "Not_in_list" }, new Transfer[] { textTransfer });
 		textControl.selectAll();
 		ReflectionUtils.invokeHidden(control, "handlePaste", (Object[]) null);
 
 		listener.assertSelection(1, "", "A");
 	}
 
 	// helping classes
 	//////////////////
 
 	private static final class FTSelectionListener extends SelectionAdapter implements ISelectionListener {
 
 		private int widgetEventCount;
 		private int ridgetEventCount;
 		private org.eclipse.riena.ui.ridgets.listener.SelectionEvent ridgetEvent;
 
 		@Override
 		public void widgetSelected(final SelectionEvent event) {
 			widgetEventCount++;
 			// System.out.println(widgetEventCount + " - " + event);
 		}
 
 		public void ridgetSelected(final org.eclipse.riena.ui.ridgets.listener.SelectionEvent event) {
 			ridgetEvent = event;
 			ridgetEventCount++;
 			// System.out.println(ridgetEventCount + " - " + event);
 		}
 
 		void assertSelection(final int expectedCount, final String oldSelection, final String newSelection) {
 			assertEquals("widget event count mismatch", expectedCount, widgetEventCount);
 			assertEquals("ridget event count mismatch", expectedCount, ridgetEventCount);
 			assertEquals("old selection value mismatch", oldSelection, getOldSelection());
 			assertEquals("new selection value mismatch", newSelection, getNewSelection());
 		}
 
 		// helping methods
 		//////////////////
 
 		private String getOldSelection() {
 			final List<Object> list = ridgetEvent.getOldSelection();
 			return (list.size() > 0) ? list.get(0).toString() : "";
 		}
 
 		private String getNewSelection() {
 			final List<Object> list = ridgetEvent.getNewSelection();
 			return (list.size() > 0) ? list.get(0).toString() : "";
 		}
 	}
 }
