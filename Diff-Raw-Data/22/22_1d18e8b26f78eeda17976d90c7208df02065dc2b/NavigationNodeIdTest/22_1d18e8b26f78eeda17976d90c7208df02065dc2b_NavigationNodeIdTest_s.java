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
 package org.eclipse.riena.navigation;
 
 import junit.framework.TestCase;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 
 /**
  * Tests of the class {@link NavigationNodeId}.
  */
 @NonUITestCase
 public class NavigationNodeIdTest extends TestCase {
 
 	/**
 	 * Tests the <i>private</i> method {@code checkId(String)}.
 	 */
 	public void testCheckId() {
 
 		NavigationNodeId nodeId = new NavigationNodeId("1", "2");
 		boolean result = ReflectionUtils.invokeHidden(nodeId, "checkId", "");
 		assertTrue(result);
 		result = ReflectionUtils.invokeHidden(nodeId, "checkId", "4711");
 		assertTrue(result);
 		result = ReflectionUtils.invokeHidden(nodeId, "checkId", "4711+0815");
 		assertTrue(result);
 		result = ReflectionUtils.invokeHidden(nodeId, "checkId", "4711/0815");
 		assertFalse(result);
 		result = ReflectionUtils.invokeHidden(nodeId, "checkId", "4711?0815");
 		assertFalse(result);
 		result = ReflectionUtils.invokeHidden(nodeId, "checkId", "4711*0815");
 		assertFalse(result);
 
 	}
 
 	/**
 	 * Tests the method {@code equals(Object)}.
 	 */
 	public void testEquals() {
 
 		NavigationNodeId nodeId = new NavigationNodeId("1", "2");
 		assertFalse(nodeId.equals(null));
 		assertFalse(nodeId.equals(new Object()));
 
 		NavigationNodeId nodeId2 = new NavigationNodeId("1", "2");
 		assertTrue(nodeId.equals(nodeId2));
 		assertTrue(nodeId.equals(nodeId));
 
 		nodeId = new NavigationNodeId("1");
 		nodeId2 = new NavigationNodeId("1");
 		assertTrue(nodeId.equals(nodeId2));
 
 		nodeId2 = new NavigationNodeId("2");
 		assertFalse(nodeId.equals(nodeId2));
 
 		nodeId2 = new NavigationNodeId("1", "2");
 		assertFalse(nodeId.equals(nodeId2));
 
 	}
 
 }
