 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
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
 
 import org.easymock.EasyMock;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.internal.core.test.collect.UITestCase;
 import org.eclipse.riena.internal.ui.ridgets.swt.TextRidget;
 import org.eclipse.riena.ui.core.marker.ErrorMessageMarker;
 import org.eclipse.riena.ui.core.marker.MessageMarker;
 import org.eclipse.riena.ui.ridgets.IStatuslineRidget;
 import org.eclipse.riena.ui.ridgets.swt.DefaultRealm;
 
 /**
  * Tests for the {@code StatuslineMessageMarkerViewer}.
  */
 @UITestCase
 public class StatuslineMessageMarkerViewerTest extends TestCase {
 
 	private static final String EMPTY_STATUSLINE_MESSAGE = "TestEmptyStatusline";
 
 	private DefaultRealm realm;
 	private Shell shell;
 	private StatuslineMessageMarkerViewer statuslineMessageMarkerViewer;
 	private IStatuslineRidget statuslineRidget;
 	private Text text1;
 	private Text text2;
 	private TextRidget ridget1;
 	private TextRidget ridget2;
 
 	/**
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		realm = new DefaultRealm();
 		shell = new Shell();
 		shell.setLayout(new RowLayout(SWT.VERTICAL));
 
 		text1 = new Text(shell, SWT.SINGLE);
 		text2 = new Text(shell, SWT.SINGLE);
 		ridget1 = new TextRidget();
 		ridget2 = new TextRidget();
 		ridget1.setUIControl(text1);
 		ridget2.setUIControl(text2);
 
 		statuslineRidget = EasyMock.createMock(IStatuslineRidget.class);
 
 		statuslineMessageMarkerViewer = new StatuslineMessageMarkerViewer(statuslineRidget);
 		statuslineMessageMarkerViewer.addRidget(ridget1);
 		statuslineMessageMarkerViewer.addRidget(ridget2);
 
 		shell.setSize(100, 100);
 		shell.setLocation(0, 0);
 		shell.open();
 		text1.setFocus();
 	}
 
 	/**
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
		EasyMock.reset(statuslineRidget);
		statuslineRidget = null;
 		ridget1 = null;
 		ridget2 = null;
 		text1.dispose();
 		text1 = null;
 		text2.dispose();
 		text2 = null;
 		shell.dispose();
 		shell = null;
 		realm.dispose();
 		realm = null;
 		super.tearDown();
 	}
 
 	public void testHandleFocusEvents() throws Exception {
 
 		final String testErrorMessage = "Test Error in Adapter 1";
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(EMPTY_STATUSLINE_MESSAGE);
 		statuslineRidget.error(testErrorMessage);
 		EasyMock.replay(statuslineRidget);
 
 		ridget1.addMarker(new ErrorMessageMarker(testErrorMessage));
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(testErrorMessage);
 		statuslineRidget.setMessage(EMPTY_STATUSLINE_MESSAGE);
 		statuslineRidget.clear();
 		EasyMock.replay(statuslineRidget);
 
 		text2.setFocus();
 
 		EasyMock.verify(statuslineRidget);
 	}
 
 	public void testHandleFocusEventsAndModifiedMessage() throws Exception {
 
 		final String testErrorMessage = "Test Error in Adapter 1";
 		final String testMessageBySomebodyElse = "Some message by somebody else";
 		final ErrorMessageMarker errorMessageMarker1 = new ErrorMessageMarker(testErrorMessage);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(EMPTY_STATUSLINE_MESSAGE);
 		statuslineRidget.error(testErrorMessage);
 		EasyMock.replay(statuslineRidget);
 
 		ridget1.addMarker(errorMessageMarker1);
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		// at this point somebody else changes the status line
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(testMessageBySomebodyElse);
 		EasyMock.replay(statuslineRidget);
 
 		text2.setFocus();
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(testMessageBySomebodyElse);
 		statuslineRidget.error(testErrorMessage);
 		EasyMock.replay(statuslineRidget);
 
 		text1.setFocus();
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(testErrorMessage);
 		statuslineRidget.setMessage(testMessageBySomebodyElse);
 		statuslineRidget.clear();
 		EasyMock.replay(statuslineRidget);
 
 		ridget1.removeMarker(errorMessageMarker1);
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		final String anotherTestErrorMessage = "Another Test Error in Adapter 1";
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(testMessageBySomebodyElse);
 		statuslineRidget.error(anotherTestErrorMessage);
 		EasyMock.replay(statuslineRidget);
 
 		ridget1.addMarker(new ErrorMessageMarker(anotherTestErrorMessage));
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(anotherTestErrorMessage);
 		statuslineRidget.setMessage(testMessageBySomebodyElse);
 		statuslineRidget.clear();
 		EasyMock.replay(statuslineRidget);
 
 		text2.setFocus();
 
 		EasyMock.verify(statuslineRidget);
 	}
 
 	public void testRemoveRidget() throws Exception {
 
 		final String testErrorMessage = "Test Error in Adapter 1";
 		final ErrorMessageMarker errorMessageMarker1 = new ErrorMessageMarker(testErrorMessage);
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(EMPTY_STATUSLINE_MESSAGE);
 		statuslineRidget.error(testErrorMessage);
 		EasyMock.replay(statuslineRidget);
 		ridget1.addMarker(errorMessageMarker1);
 		text1.setFocus();
 
 		EasyMock.reset(statuslineRidget);
 		EasyMock.replay(statuslineRidget);
 
 		statuslineMessageMarkerViewer.removeRidget(ridget2);
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(testErrorMessage);
 		statuslineRidget.setMessage(EMPTY_STATUSLINE_MESSAGE);
 		statuslineRidget.clear();
 		EasyMock.replay(statuslineRidget);
 
 		statuslineMessageMarkerViewer.removeRidget(ridget1);
 
 		EasyMock.verify(statuslineRidget);
 	}
 
 	public void testAddAndRemoveMarkerType() throws Exception {
 
 		EasyMock.replay(statuslineRidget);
 
 		final String messageDifferentType = "TestDifferentMarkerType";
 		ridget2.addMarker(new MessageMarker(messageDifferentType));
 
 		statuslineMessageMarkerViewer.addMarkerType(MessageMarker.class);
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(EMPTY_STATUSLINE_MESSAGE);
 		statuslineRidget.clear();
 		statuslineRidget.setMessage(messageDifferentType);
 		EasyMock.replay(statuslineRidget);
 
 		text2.setFocus();
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(messageDifferentType);
 		statuslineRidget.setMessage(EMPTY_STATUSLINE_MESSAGE);
 		statuslineRidget.clear();
 		EasyMock.replay(statuslineRidget);
 
 		statuslineMessageMarkerViewer.removeMarkerType(MessageMarker.class);
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(EMPTY_STATUSLINE_MESSAGE);
 		statuslineRidget.clear();
 		statuslineRidget.setMessage(messageDifferentType);
 		EasyMock.replay(statuslineRidget);
 
 		statuslineMessageMarkerViewer.addMarkerType(MessageMarker.class);
 
 		EasyMock.verify(statuslineRidget);
 	}
 
 	public void testSetVisible() throws Exception {
 
 		final String testErrorMessage = "Test Error in Adapter 1";
 
 		statuslineMessageMarkerViewer.setVisible(false);
 		ridget1.addMarker(new ErrorMessageMarker(testErrorMessage));
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(EMPTY_STATUSLINE_MESSAGE);
 		statuslineRidget.error(testErrorMessage);
 		EasyMock.replay(statuslineRidget);
 
 		statuslineMessageMarkerViewer.setVisible(true);
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(testErrorMessage);
 		statuslineRidget.clear();
 		statuslineRidget.setMessage(EMPTY_STATUSLINE_MESSAGE);
 		EasyMock.replay(statuslineRidget);
 
 		statuslineMessageMarkerViewer.setVisible(false);
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		EasyMock.replay(statuslineRidget);
 
 		text2.setFocus();
 		text1.setFocus();
 
 		EasyMock.verify(statuslineRidget);
 	}
 
 	public void testTwoMarkers() throws Exception {
 
 		final String testErrorMessage1 = "Test Error 1 in Adapter 1";
 		final ErrorMessageMarker marker1 = new ErrorMessageMarker(testErrorMessage1);
 		statuslineMessageMarkerViewer.addMarkerType(MessageMarker.class);
 		final String testErrorMessage2 = "Test Error 2 in Adapter 1";
 		final MessageMarker marker2 = new MessageMarker(testErrorMessage2);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(EMPTY_STATUSLINE_MESSAGE);
 		statuslineRidget.error(testErrorMessage1);
 		EasyMock.replay(statuslineRidget);
 
 		ridget1.addMarker(marker1);
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		statuslineRidget.error(testErrorMessage1 + " " + testErrorMessage2);
 		EasyMock.replay(statuslineRidget);
 
 		ridget1.addMarker(marker2);
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		statuslineRidget.clear();
 		statuslineRidget.setMessage(testErrorMessage2);
 		EasyMock.replay(statuslineRidget);
 
 		ridget1.removeMarker(marker1);
 
 		EasyMock.verify(statuslineRidget);
 		EasyMock.reset(statuslineRidget);
 
 		EasyMock.expect(statuslineRidget.getMessage()).andReturn(testErrorMessage2);
 		statuslineRidget.setMessage(EMPTY_STATUSLINE_MESSAGE);
 		statuslineRidget.clear();
 		EasyMock.replay(statuslineRidget);
 
 		ridget1.removeMarker(marker2);
 
 		EasyMock.verify(statuslineRidget);
 	}
 
 }
