 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 
 package org.eclipse.vjet.dsf.jst.validation.vjo.syntax.global.vjoType;
 
 
 
 
 import java.util.List;
 
 import org.eclipse.vjet.dsf.jsgen.shared.ids.MethodProbIds;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoSemanticProblem;
 import org.eclipse.vjet.dsf.jst.validation.vjo.VjoValidationBaseTester;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 
 
 
 
 /**
  * Test for vjo global method.
  * 
  * @author <a href="mailto:liama@ebay.com">liama</a>
  * @since JDK 1.5
  */
 //@Category( { P1, FAST, UNIT })
 //@ModuleInfo(value="DsfPrebuild",subModuleId="JsToJava")

 public class VjoType extends VjoValidationBaseTester {
 
     @Before
     public void setUp() {
         expectProblems.clear();
         expectProblems.add(createNewProblem(
                 MethodProbIds.WrongNumberOfArguments, 26, 0));
         expectProblems.add(createNewProblem(MethodProbIds.ParameterMismatch,
                 27, 0));
        expectProblems.add(createNewProblem(null,
                16, 0));
        expectProblems.add(createNewProblem(null,
                47, 0));
     }
 
     @Test
     //@Category( { P1, FAST, UNIT })
     //@Description("Test Vjo type's methods")
    @Ignore("need to determine how to validate this since it has syntax errors")
     public void testIfstatement1() {
         List<VjoSemanticProblem> problems = getVjoSemanticProblem(
                 "syntax.global.vjoType/", "VjoType.js", this.getClass());
         assertProblemEquals(expectProblems, problems);
     }
 }
