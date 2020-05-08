 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.it.render;
 
 import junit.framework.Test;
 
 import org.seasar.teeda.unit.web.TeedaWebTestCase;
 import org.seasar.teeda.unit.web.TeedaWebTester;
 
 /**
  * @author shot
  */
 public class ErrorTest extends TeedaWebTestCase {
 
 	public static Test suite() throws Exception {
 		return setUpTest(ErrorTest.class);
 	}
 
 	public void testErrorOnClickButton() throws Exception {
 		// ## Arrange ##
 		TeedaWebTester tester = new TeedaWebTester();
 
 		// ## Act ##
 		tester.beginAt(getBaseUrl(), "view/error/error.html");
 		tester.dumpHtml();
 
 		tester.submitById("doHoge");
 
 		// ## Assert ##
 		assertTrue(tester.getCurrentUri().endsWith("errorResult.html"));
 	}
 
	public void testErrorOnInitialize() throws Exception {
 		// ## Arrange ##
 		TeedaWebTester tester = new TeedaWebTester();
 
 		// ## Act ##
 		tester.beginAt(getBaseUrl(), "view/error/errorOnInitialize.html");
 		tester.dumpHtml();
 
 		// ## Assert ##
 		assertTrue(tester.getCurrentUri().indexOf("errorResult.html") >= 0);
		tester.assertTextPresent("ErrorPage throws exception.");
 	}
 
 	public void testErrorOnPrerender() throws Exception {
 		// ## Arrange ##
 		TeedaWebTester tester = new TeedaWebTester();
 
 		// ## Act ##
 		tester.beginAt(getBaseUrl(), "view/error/errorOnPrerender.html");
 		tester.dumpHtml();
 
 		// ## Assert ##
 		assertTrue(tester.getCurrentUri().indexOf("errorResult.html") >= 0);
		tester.assertTextPresent("ErrorPage throws exception.");
 	}
 
 }
