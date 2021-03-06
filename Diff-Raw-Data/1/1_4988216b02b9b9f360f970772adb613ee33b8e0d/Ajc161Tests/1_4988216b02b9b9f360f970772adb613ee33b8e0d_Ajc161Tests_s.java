 /*******************************************************************************
  * Copyright (c) 2008 Contributors 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Andy Clement - initial API and implementation
  *******************************************************************************/
 package org.aspectj.systemtest.ajc161;
 
 import java.io.File;
 
 import junit.framework.Test;
 
 import org.aspectj.testing.XMLBasedAjcTestCase;
 
 public class Ajc161Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
 	
 	// AspectJ1.6.1
     public void testAroundAdviceOnFieldSet_pr229910() { runTest("around advice on field set"); }
     public void testPipelineCompilationGenericReturnType_pr226567() { runTest("pipeline compilation and generic return type"); }
 
     public static Test suite() {
       return XMLBasedAjcTestCase.loadSuite(Ajc161Tests.class);
     }
 
     protected File getSpecFile() {
       return new File("../tests/src/org/aspectj/systemtest/ajc161/ajc161.xml");
     }
   
 }
