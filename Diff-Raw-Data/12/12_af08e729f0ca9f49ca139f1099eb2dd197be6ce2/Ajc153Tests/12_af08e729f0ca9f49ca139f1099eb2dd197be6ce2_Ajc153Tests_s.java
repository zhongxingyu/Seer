 /*******************************************************************************
  * Copyright (c) 2006 IBM 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Andy Clement - initial API and implementation
  *******************************************************************************/
 package org.aspectj.systemtest.ajc153;
 
 import java.io.File;
 
 import junit.framework.Test;
 
 import org.aspectj.asm.AsmManager;
 import org.aspectj.asm.IHierarchy;
 import org.aspectj.asm.IProgramElement;
 import org.aspectj.testing.Utils;
 import org.aspectj.testing.XMLBasedAjcTestCase;
 import org.aspectj.weaver.bcel.Utility;
 
 public class Ajc153Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
 
   //public void testGenericsProblem_pr151978() { runTest("generics problem");}
//  public void testMissingLineNumbersInStacktraceAfter_pr145442() { runTest("missing line numbers in stacktrace after");}
//  public void testMissingLineNumbersInStacktraceAround_pr145442() { runTest("missing line numbers in stacktrace around");}
   // public void testArgnamesAndJavac_pr148381() { runTest("argNames and javac");}
   // public void testCFlowXMLAspectLTW_pr149096() { runTest("cflow xml concrete aspect"); }
   // public void testAmbiguousBinding_pr121805() { runTest("ambiguous binding");}
 //  public void testNoIllegalStateExceptionWithGenericInnerAspect_pr156058() { runTest("no IllegalStateException with generic inner aspect"); }
 //  public void testNegatedAnnotationMatchingProblem_pr153464() { runTest("negated annotation matching problem");}
   public void testGenericArrays_pr158624() { runTest("generics and arrays"); }
   public void testMissingLineNumbersInStacktraceBefore_pr145442() { runTest("missing line numbers in stacktrace before");}
   public void testMissingLineNumbersInStacktraceBefore_pr145442_Binary() { runTest("missing line numbers in stacktrace before - binary");}
   public void testAnnotationStylePointcutNPE_pr158412() { runTest("annotation style pointcut npe"); }
   public void testAnnotationStylePointcutNPE_pr158412_2() { runTest("annotation style pointcut npe - 2"); }
   public void testAnnotationsCallConstructors_pr158126() { runTest("annotations, call and constructors problem");}
   public void testIllegalStateExceptionGenerics_pr153845() { runTest("IllegalStateException at GenericSignatureParser.java"); }
   public void testNoIllegalStateExceptionFromAsmDelegate_pr153490_1() { runTest("no illegal state exception from AsmDelegate - 1");}
   public void testNoIllegalStateExceptionFromAsmDelegate_pr153490_2() { runTest("no illegal state exception from AsmDelegate - 2");}
   public void testNoIllegalStateExceptionFromAsmDelegate_pr153490_3() { runTest("no illegal state exception from AsmDelegate - 3");}
   public void testAnnotMethod_pr156962() { runTest("Test Annot Method");}
   public void testAnnotMethodHasMember_pr156962() { runTest("Test Annot Method Has Member"); }
   public void testMixingGenerics_pr152848()  { runTest("mixing generics"); }
   public void testIncorrectStaticinitializationWeaving_pr149560_1() { runTest("incorrect staticinitialization weaving - codestyle");}
   public void testIncorrectStaticinitializationWeaving_pr149560_2() { runTest("incorrect staticinitialization weaving - annstyle");}
   public void testIncorrectDeprecatedAnnotationProcessing_pr154332() { runTest("incorrect deprecated annotation processing");}
   public void testPipeliningProblemWithAnnotationsDecp_pr153380_1() { runTest("pipelining decps");}
   public void testUnwantedPointcutWarning_pr148219() { runTest("unwanted warning for pointcut");}
   public void testDecpAndCflowadderMungerClash_pr152631() { runTest("decp and cflowadder munger clash");}
   public void testGenericInheritanceDecp_pr150095() { runTest("generics, inheritance and decp");}
   public void testIllegalStateException_pr148737() { runTest("illegalstateexception for non generic type");}
   public void testAtajInheritance_pr149305_1()     { runTest("ataj inheritance - 1");}
   public void testAtajInheritance_pr149305_2()     { runTest("ataj inheritance - 2");}
   public void testAtajInheritance_pr149305_3()     { runTest("ataj inheritance - 3");}
   public void testVerificationFailureForAspectOf_pr148693() {
 	runTest("verification problem");   // build the code
 	Utils.verifyClass(ajc,"mypackage.MyAspect"); // verify it <<< BRAND NEW VERIFY UTILITY FOR EVERYONE TO TRY ;)
   }
   public void testIncorrectAnnotationValue_pr148537()          { runTest("incorrect annotation value");}
   public void testVerifyErrNoTypeCflowField_pr145693_1()       { runTest("verifyErrNoTypeCflowField"); }
   public void testVerifyErrInpathNoTypeCflowField_pr145693_2() { runTest("verifyErrInpathNoTypeCflowField"); }
   public void testCpathNoTypeCflowField_pr145693_3()           { runTest("cpathNoTypeCflowField"); }
   // public void testVisibilityProblem_pr149071()                 { runTest("visibility problem");}
   // public void testAdviceNotWovenAspectPath_pr147841()          { runTest("advice not woven on aspectpath");}
   public void testGenericSignatures_pr148409()                 { runTest("generic signature problem"); }
 //  public void testBrokenIfArgsCflowAtAj_pr145018() { runTest("ataj crashing with cflow, if and args");}
   public void testCantFindType_pr149322_01() {runTest("can't find type on interface call 1");}
   public void testCantFindType_pr149322_02() {runTest("can't find type on interface call 2");}
   public void testCantFindType_pr149322_03() {runTest("can't find type on interface call 3");}
   public void testParsingBytecodeLess_pr152871() { 
 	  Utility.testingParseCounter=0;
 	  runTest("parsing bytecode less"); 
 	  assertTrue("Should have called parse 5 times, not "+Utility.testingParseCounter+" times",Utility.testingParseCounter==5);
 	  // 5 means:   
 	  // (1)=registerAspect   
 	  // (2,3)=checkingIfShouldWeave,AcceptingResult for class
 	  // (4,5)=checkingIfShouldWeave,AcceptingResult for aspect
   }
   public void testMatchVolatileField_pr150671() {runTest("match volatile field");};
   public void testDuplicateJVMTIAgents_pr151938() {runTest("Duplicate JVMTI agents");};
   public void testLTWWorldWithAnnotationMatching_pr153572() { runTest("LTWWorld with annotation matching");}
   
   public void testReweavableAspectNotRegistered_pr129525 () {
 	  runTest("reweavableAspectNotRegistered error");
   }
   
   public void testNPEinConstructorSignatureImpl_pr155972 () {
 	  runTest("NPE in ConstructorSignatureImpl");
   }
   
   public void testNPEinFieldSignatureImpl_pr155972 () {
 	  runTest("NPE in FieldSignatureImpl");
   }
   
   public void testNPEinInitializerSignatureImpl_pr155972 () {
 	  runTest("NPE in InitializerSignatureImpl");
   }
   
   public void testLineNumberTableCorrectWithGenericsForEachAndContinue_pr155763() {
 	  runTest("ensure LineNumberTable correct with generics, for each and continue");
   }
   
   public void testDeclareSoftDoesntAllowUndeclaredExInAnonInnerClass_pr151772() {
 	  runTest("ensure declare soft doesn't allow undeclared exception in anonymous inner class");
   }
 
   public void testDeclareSoftDoesntAllowUndeclaredExInAnonInnerClass_pr151772_2() {
 	  runTest("ensure declare soft doesn't allow undeclared exception in anonymous inner class - 2");
   }
 
   public void testDeclareSoftAndInnerClasses_pr125981() {
 	  runTest("declare soft and inner classes");
   }
   
   public void testGetSourceSignature_pr148908() {
 	runTest("ensure getSourceSignature correct with static field");
 	IHierarchy top = AsmManager.getDefault().getHierarchy();
 	IProgramElement ipe = top.findElementForLabel(top.getRoot(),
 	IProgramElement.Kind.FIELD,"MY_COMPARATOR");
 	String expected = "static final Comparator MY_COMPARATOR = new Comparator() {\n" +
 					"  public int compare(Object o1, Object o2) {\n" +
 					"    return 0;\n" +
 					"  }\n" +
 					"};";
 			assertEquals("expected source signature to be " + expected + 
 					" but found " + ipe.getSourceSignature(), 
 					expected, ipe.getSourceSignature());
   }
 
   public void testNPEWithCustomAgent_pr158005() {
 	  runTest("NPE with custom agent");
   }
   
     /////////////////////////////////////////
   public static Test suite() {
     return XMLBasedAjcTestCase.loadSuite(Ajc153Tests.class);
   }
 
   protected File getSpecFile() {
     return new File("../tests/src/org/aspectj/systemtest/ajc153/ajc153.xml");
   }
 
   
 }
