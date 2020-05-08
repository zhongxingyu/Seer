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
 package org.eclipse.riena.ui.ridgets.marker;
 
 import junit.framework.TestCase;
 
 import org.eclipse.riena.internal.ui.ridgets.swt.TextRidget;
 import org.eclipse.riena.tests.collect.UITestCase;
 import org.eclipse.riena.ui.core.marker.ErrorMessageMarker;
 import org.eclipse.riena.ui.core.marker.MessageMarker;
 import org.eclipse.riena.ui.ridgets.swt.DefaultRealm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
 
 /**
  * Tests for the TooltipMessageMarkerViewer.
  */
 @UITestCase
 public class TooltipMessageMarkerViewerTest extends TestCase {
 
 	private DefaultRealm realm;
 	private Shell shell;
 	private TooltipMessageMarkerViewer tooltipMessageMarkerViewer;
 	private TextRidget ridget;
 	private Text control;
 	private String errorMessage;
 	private ErrorMessageMarker errorMessageMarker;
 
 	/**
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		realm = new DefaultRealm();
 		shell = new Shell();
 		control = new Text(shell, SWT.BORDER);
 
 		ridget = new TextRidget();
 		ridget.setUIControl(control);
 
 		tooltipMessageMarkerViewer = new TooltipMessageMarkerViewer();
 		tooltipMessageMarkerViewer.addRidget(ridget);
 
 		errorMessage = "TestErrorMessage";
 		errorMessageMarker = new ErrorMessageMarker(errorMessage);
 	}
 
 	/**
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		shell.dispose();
 		shell = null;
 		realm.dispose();
 		realm = null;
 		super.tearDown();
 	}
 
 	public void testSetMessage() throws Exception {
 
 		assertNull(ridget.getToolTipText());
 
 		ridget.addMarker(errorMessageMarker);
 
 		assertEquals(errorMessage, ridget.getToolTipText());
 		assertEquals(errorMessage, control.getToolTipText());
 
 		ridget.removeMarker(errorMessageMarker);
 
 		assertNull(ridget.getToolTipText());
 		assertNull(control.getToolTipText());
 	}
 
 	public void testSetMessageExistingTooltip() throws Exception {
 
 		String existingTooltip = "TestExistingTooltip";
 
 		ridget.setToolTipText(existingTooltip);
 
 		ridget.addMarker(errorMessageMarker);
 
 		assertEquals(errorMessage, ridget.getToolTipText());
 		assertEquals(errorMessage, control.getToolTipText());
 
 		ridget.removeMarker(errorMessageMarker);
 
 		assertEquals(existingTooltip, ridget.getToolTipText());
 		assertEquals(existingTooltip, control.getToolTipText());
 	}
 
 	public void testAddRidgetWithMarker() throws Exception {
 
 		TextRidget anotherRidget = new TextRidget();
 		anotherRidget.addMarker(errorMessageMarker);
 
 		tooltipMessageMarkerViewer.addRidget(anotherRidget);
 
 		assertEquals(errorMessage, anotherRidget.getToolTipText());
 
 		tooltipMessageMarkerViewer.removeRidget(anotherRidget);
 
 		assertNull(anotherRidget.getToolTipText());
 	}
 
 	public void testAddAndRemoveMarkerType() throws Exception {
 
 		TextRidget anotherRidget = new TextRidget();
 		String anotherMessage = "TestAnotherMessage";
 		MessageMarker anotherMarker = new MessageMarker(anotherMessage);
 		anotherRidget.addMarker(anotherMarker);
 
 		tooltipMessageMarkerViewer.addRidget(anotherRidget);
 
 		assertNull(anotherRidget.getToolTipText());
 
 		tooltipMessageMarkerViewer.addMarkerType(MessageMarker.class);
 
 		assertEquals(anotherMessage, anotherRidget.getToolTipText());
 
 		tooltipMessageMarkerViewer.removeMarkerType(MessageMarker.class);
 
 		assertNull(anotherRidget.getToolTipText());
 	}
 
 	public void testVisible() throws Exception {
 
 		ridget.addMarker(errorMessageMarker);
 
 		tooltipMessageMarkerViewer.setVisible(false);
 
 		assertNull(ridget.getToolTipText());
 		assertNull(control.getToolTipText());
 
 		tooltipMessageMarkerViewer.setVisible(true);
 
 		assertEquals(errorMessage, ridget.getToolTipText());
 		assertEquals(errorMessage, control.getToolTipText());
 	}
 
 	public void testSetMessageTwoMarkers() throws Exception {
 
 		assertNull(ridget.getToolTipText());
 
 		ridget.addMarker(errorMessageMarker);
 		String secondMessage = errorMessage + 2;
 		MessageMarker secondMarker = new MessageMarker(secondMessage);
 		tooltipMessageMarkerViewer.addMarkerType(MessageMarker.class);
 		ridget.addMarker(secondMarker);
 
		assertEquals(errorMessage + "; " + secondMessage, ridget.getToolTipText());
 
 		ridget.removeMarker(errorMessageMarker);
 
 		assertEquals(secondMessage, ridget.getToolTipText());
 
 		ridget.removeMarker(secondMarker);
 
 		assertEquals(null, ridget.getToolTipText());
 		assertNull(ridget.getToolTipText());
 
 		ridget.addMarker(errorMessageMarker);
 		ridget.addMarker(secondMarker);
 		ridget.addMarker(new MessageMarker(errorMessage + 3));
 		ridget.addMarker(new MessageMarker(errorMessage + 4));
 		ridget.addMarker(new MessageMarker(errorMessage + 5));
 
		assertEquals(errorMessage + "; " + secondMessage + "; " + errorMessage + "3; " + errorMessage + "4; "
 				+ errorMessage + "5", ridget.getToolTipText());
 
 	}
 
 	/**
 	 * Ensure a ridget's tooltip is preserved when adding it to the
 	 * tooltipMessageMarkerViewer
 	 */
 	public void testBug258540() {
 		TextRidget aRidget = new TextRidget();
 		Text aControl = new Text(shell, SWT.BORDER);
 		aRidget.setUIControl(aControl);
 		aRidget.setToolTipText("tooltip");
 
 		assertEquals("tooltip", aRidget.getToolTipText());
 		assertEquals("tooltip", aControl.getToolTipText());
 
 		tooltipMessageMarkerViewer.addRidget(aRidget);
 
 		assertEquals("tooltip", aRidget.getToolTipText());
 		assertEquals("tooltip", aControl.getToolTipText());
 	}
 
 	/**
 	 * Ensure a ridget's tooltip is preserved, when calling
 	 * ridget.setToolTipText(...) while the {@link TooltipMessageMarkerViewer}
 	 * is showing an error message.
 	 */
 	public void testBug258540Snippet2() {
 		TextRidget aRidget = new TextRidget();
 		Text aControl = new Text(shell, SWT.BORDER);
 		aRidget.setUIControl(aControl);
 
 		tooltipMessageMarkerViewer.addRidget(aRidget);
 		aRidget.setToolTipText("tooltip");
 
 		assertEquals("tooltip", aRidget.getToolTipText());
 		assertEquals("tooltip", aControl.getToolTipText());
 
 		ErrorMessageMarker marker = new ErrorMessageMarker("errormessage");
 		aRidget.addMarker(marker);
 		aRidget.setToolTipText("tooltip2");
 
 		assertEquals("errormessage", aRidget.getToolTipText());
 		assertEquals("errormessage", aControl.getToolTipText());
 
 		aRidget.removeMarker(marker);
 
 		assertEquals("tooltip2", aRidget.getToolTipText());
 		assertEquals("tooltip2", aControl.getToolTipText());
 	}
 
 }
