 /*******************************************************************************
  * Copyright (c) 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.tomcat.core.tests.internal;
 
 import org.eclipse.jst.server.tomcat.core.internal.TomcatServerBehaviour;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 public class UtilTestCase extends TestCase {
 	public static Test suite() {
 		return new TestSuite(UtilTestCase.class, "UtilTestCase");
 	}
 	
 	public void testArgMerge() {
 		assertEquals("", TomcatServerBehaviour.mergeArguments("", new String[] { }));
 	}
 	
 	public void testArgMerge2() {
 		assertEquals("a=b", TomcatServerBehaviour.mergeArguments("", new String[] { "a=b"}));
 	}
 	
 	public void testArgMerge3() {
 		assertEquals("a=b c=d", TomcatServerBehaviour.mergeArguments("", new String[] { "a=b", "c=d" }));
 	}
 	
 	public void testArgMerge4() {
 		assertEquals("a=b c=d", TomcatServerBehaviour.mergeArguments("a=b", new String[] { "c=d" }));
 	}
 	
 	public void testArgMerge5() {
 		assertEquals("a=c", TomcatServerBehaviour.mergeArguments("a=b", new String[] { "a=c" }));
 	}
 	
 	public void testArgMerge6() {
 		assertEquals("a b=2 d e=3", TomcatServerBehaviour.mergeArguments("a b=c d", new String[] { "b=2", "e=3" }));
 	}
 	
 	public void testArgMerge7() {
 		assertEquals("a bb=c d b=2 e=3", TomcatServerBehaviour.mergeArguments("a bb=c d", new String[] { "b=2", "e=3" }));
 	}
 	
 	public void testArgMerge8() {
 		assertEquals("a", TomcatServerBehaviour.mergeArguments("a", new String[] { }));
 	}
 	
 	public void testArgMerge9() {
 		assertEquals("a b", TomcatServerBehaviour.mergeArguments("a", new String[] { "b" }));
 	}
 	
 	public void testArgMerge10() {
 		assertEquals("a b c d", TomcatServerBehaviour.mergeArguments("a b ", new String[] { "c", "d" }));
 	}
 }
