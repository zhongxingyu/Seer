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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
import org.eclipse.riena.tests.collect.NonUITestCase;
 import org.eclipse.riena.ui.core.marker.IMessageMarker;
 import org.eclipse.riena.ui.core.marker.MessageMarker;
 import org.eclipse.riena.ui.ridgets.IBasicMarkableRidget;
 
 /**
  * Tests of the class {@link AbstractMessageMarkerViewer}.
  */
@NonUITestCase
 public class AbstractMessageMarkerViewerTest extends TestCase {
 
 	private AbstractMessageMarkerViewer viewer;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		viewer = new MyMessageMarkerViewer();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		viewer = null;
 	}
 
 	/**
 	 * Tests the method {@code constructMessage}.
 	 */
 	public void testConstructMessage() {
 
 		List<IMessageMarker> messageMarkers = new ArrayList<IMessageMarker>();
 
 		String message = viewer.constructMessage(messageMarkers);
 		assertEquals("", message);
 
 		messageMarkers.add(new MessageMarker("one"));
 		message = viewer.constructMessage(messageMarkers);
 		assertEquals("one", message);
 
 		messageMarkers.add(new MessageMarker("  Two  "));
 		message = viewer.constructMessage(messageMarkers);
 		assertEquals("one*  Two", message);
 
 		messageMarkers.add(new MessageMarker("THREE"));
 		message = viewer.constructMessage(messageMarkers);
 		assertEquals("one*  Two  *THREE", message);
 
 		messageMarkers.add(new MessageMarker("  "));
 		message = viewer.constructMessage(messageMarkers);
 		assertEquals("one*  Two  *THREE*", message);
 
 	}
 
 	private class MyMessageMarkerViewer extends AbstractMessageMarkerViewer {
 
 		@Override
 		String getMessageSeparator() {
 			return "*";
 		}
 
 		@Override
 		protected void hideMessages(IBasicMarkableRidget ridget) {
 			// not implemented yet for these test cases
 		}
 
 		@Override
 		protected void showMessages(IBasicMarkableRidget ridget) {
 			// not implemented yet for these test cases
 		}
 
 	}
 
 }
