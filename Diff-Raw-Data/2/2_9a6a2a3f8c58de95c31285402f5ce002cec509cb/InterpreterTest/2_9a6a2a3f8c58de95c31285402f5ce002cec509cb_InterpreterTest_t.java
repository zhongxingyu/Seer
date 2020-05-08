 /*
  * Created on 05.jul.2005
  *
  * Copyright (c) 2004, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the IBM Common Public License, v1.0
  */
 package org.spoofax.interp.test;
 
 import java.io.IOException;
 
 import junit.framework.TestCase;
 
 import org.spoofax.interp.FatalError;
 import org.spoofax.interp.Interpreter;
 
 import aterm.ATerm;
 import aterm.pure.ATermImpl;
 
 public class InterpreterTest extends TestCase {
 
     Interpreter itp;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         itp = new Interpreter();
     }
     
     public void testBuildInt() {
         interpTest("build_int", "1", "5");
     }
 
     public void testBuildString() {
         interpTest("build_string", "1", "\"a\"");
     }
 
     public void testBuildTuple() {
         interpTest("build_tuple", itp.makeTerm("1"), itp.makeTuple("[2, 3]"));
     }
 
     public void testBuildList1() {
         interpTest("build_list_1", itp.makeTerm("1"), itp.makeList("[]"));
     }
 
     public void testBuildList2() {
         interpTest("build_list_2", itp.makeTerm("1"), itp.makeList("[1,2,3]"));
     }
 
     public void testMatchInt1() {
         interpTest("match_int_1", "2", "2");
     }
 
     public void testMatchString1() {
         interpTest("match_string_1", "\"abc\"", "\"abc\"");
     }
 
     public void testMatchInt2() {
         interpTestFail("match_int_2", "3");
     }
 
     public void testMatchString2() {
         interpTestFail("match_string_2", "\"abc\"");
     }
 
     public void testMatchTuple1() {
         interpTest("match_tuple_1", itp.makeTuple("[1,2]"), itp.makeTerm("1"));
     }
 
     public void testMatchTuple2() {
         interpTest("match_tuple_2", itp.makeTuple("[1,2]"), itp.makeTerm("2"));
     }
 
     public void testMatchTuple3() {
         interpTest("match_tuple_3", itp.makeTuple("[2,2]"), itp.makeTerm("2"));
     }
 
     public void testMatchTuple4() {
         interpTestFail("match_tuple_4", itp.makeTuple("[2,3]"));
     }
 
     public void testMatchList1() {
         interpTest("match_list_1", "Cons(1, Cons(2, Nil))", "1");
     }
 
     public void testMatchList2() {
         interpTest("match_list_2", "Cons(1, Cons(2, Nil))", "2");
     }
 
     public void testMatchList3() {
         interpTest("match_list_3", "Cons(2, Cons(2, Nil))", "2");
     }
 
     public void testMatchList4() {
         interpTestFail("match_list_4", "Cons(2, Cons(3, Nil))");
     }
 
     public void testMatchAndBuild1() {
         interpTest("match_and_build_1", "1", "1");
     }
 
     public void testMatchAndBuild2() {
         interpTestFail("match_and_build_2", "1");
     }
 
     public void interpTestFail(String test, ATerm input) {
         assertFalse(runInterp(test, input));
     }
 
     public void interpTestFail(String test, String input) {
         ATerm t = itp.makeTerm(input);
         interpTestFail(test, t);
     }
 
     public void interpTest(String test, String input, String output) {
         ATerm i = itp.makeTerm(input);
         ATerm o = itp.makeTerm(output);
         interpTest(test, i, o);
     }
 
     public void interpTest(String test, ATerm input, ATerm output) {
         assertTrue(runInterp(test, input));
         ATermImpl x = (ATermImpl) output;
         ATermImpl y = (ATermImpl) itp.getCurrent();
         assertTrue(x.getFactory() == y.getFactory());
         System.out.println("Want : " + x + " / " + x.getType() + " / " + x.getClass()
                 + " / " + x.getChildCount());
         System.out.println("Got  : " + y + " / " + y.getType() + " / " + y.getClass()
                            + " / " + y.getChildCount());
         System.out.println(itp.getCurrent().match(output));
         assertTrue(itp.getCurrent().match(output) != null);
     }
 
     private boolean runInterp(String test, ATerm input) {
         itp.reset();
 
         try {
         itp.load("/home/karltk/source/oss/spoofax/spoofax/core/tests/data/"
                 + test + ".rtree");
 
         itp.setCurrent(input);
         System.out.println("Input : " + input);
         return itp.eval(itp.makeTerm("CallT(SVar(\"main_0_0\"), [], [])"));
         } catch(FatalError e) {
             e.printStackTrace();
             return false;
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
 
     }
 
     public void testScopeMatchInterrupt1() {
         interpTest("match_interrupted_by_scope_1", "2", "2");
     }
 
     public void testScopeMatchInterrupt2() {
         interpTest("match_interrupted_by_scope_2", "2", "2");
     }
 
     public void testScopeMatchInterrupt3() {
         interpTest("match_interrupted_by_scope_3", "3", "3");
     }
 
     public void testScopeMatchInterrupt4() {
         interpTest("match_interrupted_by_scope_4", "4", "4");
     }
 
     public static void main(String[] args) {
         junit.textui.TestRunner.run(InterpreterTest.class);
     }
 
     public void testChoiceUnbinding() {
         interpTest("unbinding_in_lchoice", "2", "3");
     }
 
     public void testChoiceDoNotUnbinding() {
         interpTest("do_not_unbinding_lhs_of_lchoice_if_it_succeeds", itp
                 .makeTuple("[]"), itp.makeTerm("1"));
     }
 
     public void testLeftChoiceGuard() {
         interpTest("guarded_modifies_current_term", itp.makeTuple("[]"), itp
                 .makeTerm("3"));
     }
 
     public void testCongInt1() {
         interpTest("cong_int_1", "2", "2");
     }
 
     public void testCongInt2() {
         interpTestFail("cong_int_2", "3");
     }
 
     public void testCongString1() {
         interpTest("cong_string_1", "\"foo\"", "\"foo\"");
     }
 
     public void testCongString2() {
         interpTestFail("cong_string_2", "\"foo\"");
     }
 
     public void testCongTuple1() {
         interpTest("cong_tuple_1", itp.makeTuple("[3, 4]"), itp
                 .makeTuple("[3, 4]"));
     }
 
     public void testCongTuple2() {
         interpTest("cong_tuple_2", itp.makeTuple("[3, 4]"), itp
                 .makeTuple("[4, 5]"));
     }
 
     public void testCongTuple3() {
         interpTestFail("cong_tuple_3", itp.makeTuple("[3, 4]"));
     }
 
     public void testCongTuple4() {
         interpTest("cong_tuple_4", itp.makeTuple("[3, 4]"), itp
                 .makeTuple("[3,5]"));
     }
 
     public void testCongTuple5() {
         interpTestFail("cong_tuple_5", itp.makeTuple("[3, 5]"));
     }
 
     public void testCongList1() {
         interpTest("cong_list_1", itp.makeList("[]"), itp.makeList("[]"));
     }
 
     public void testCongList2() {
         interpTest("cong_list_2", itp.makeList("[1]"), itp.makeList("[2]"));
     }
 
     public void testCongList3() {
         interpTest("cong_list_3", itp.makeList("[1,2]"), itp.makeList("[1,3]"));
     }
 
     public void testCongList4() {
         interpTest("cong_list_4", itp.makeList("[1]"), itp.makeList("[1]"));
     }
 
     public void testCongList5() {
         interpTest("cong_list_5", itp.makeList("[1,2]"), itp.makeList("[1]"));
     }
 
     public void testCongList6() {
         interpTestFail("cong_list_6", itp.makeList("[1,2]"));
     }
 
     public void testCongList7() {
         interpTestFail("cong_list_7", itp.makeList("[1,2]"));
     }
 
     public void testCongList8() {
         interpTestFail("cong_list_8", itp.makeList("[1,2]"));
     }
 
     public void testCongList9() {
         interpTest("cong_list_9", itp.makeList("[1,2]"), itp.makeList("[2]"));
     }
 
     public void testCongList10() {
         interpTest("cong_list_10", itp.makeList("[1,2]"), itp.makeTerm("2"));
     }
 
     public void testCongList11() {
         interpTestFail("cong_list_11", itp.makeList("[1]"));
     }
 
     public void testTermArg1() {
         interpTest("id_term-arg_1", itp.makeTuple("[]"), itp.makeTerm("3"));
     }
 
     public void testSDef1() {
         interpTest("foo_sdef_1", itp.makeTuple("[]"), itp.makeTerm("3"));
     }
 
     public void testSDef2() {
         interpTest("foo_sdef_2", itp.makeTuple("[]"), itp.makeTerm("3"));
     }
 
     public void testSDef3() {
         interpTest("foo_sdef_3", itp.makeTuple("[]"), itp.makeTerm("3"));
     }
 
     public void testRDef1() {
         interpTest("foo_rdef_1", itp.makeTerm("1"), itp.makeTerm("2"));
     }
 
     public void testRDef2() {
         interpTest("foo_rdef_2", itp.makeTuple("[]"), itp.makeTuple("[2, 1]"));
     }
 
     public void testRDef3() {
         interpTest("foo_rdef_3", itp.makeTuple("[]"), itp.makeTerm("2"));
     }
 
     public void testRDef4() {
         interpTestFail("foo_rdef_4", itp.makeTuple("[]"));
     }
 
     public void testOverloading1() {
         interpTest("overloading_1", itp.makeTuple("[]"), itp.makeTerm("1"));
     }
 
     public void testOverloading2() {
         interpTest("overloading_2", itp.makeTuple("[]"), itp.makeTerm("2"));
     }
 
     public void testOverloading3() {
         interpTest("overloading_3", itp.makeTuple("[]"), itp.makeTerm("1"));
     }
 
     public void testOverloading4() {
         interpTest("overloading_4", itp.makeTuple("[]"), itp.makeTerm("2"));
     }
 
     public void testIncInt() {
         interpTest("inc_int", itp.makeTerm("1"), itp.makeTerm("2"));
     }
 
     public void testAddInt1() {
         interpTest("add_int_1", itp.makeTuple("[1,2]"), itp.makeTerm("3"));
     }
 
     public void testAddInt2() {
         interpTest("add_int_2", itp.makeTuple("[]"), itp.makeTerm("3"));
     }
 
     public void testGtInt1() {
         interpTest("gt_int_1", itp.makeTuple("[]"), itp.makeTuple("[2, 1]"));
     }
 
     public void testGtInt2() {
         interpTestFail("gt_int_2", itp.makeTuple("[]"));
     }
 
     public void testMulInt() {
         interpTest("mul_int", itp.makeTuple("[2,3]"), itp.makeTerm("6"));
     }
 
     public void testSwapTuple() {
         interpTest("swap_tuple", itp.makeTuple("[1,2]"), itp.makeTuple("[2,1]"));
     }
 
     public void testIntToString() {
         interpTest("int-to-string", itp.makeTerm("14"), itp.makeTerm("\"14\""));
     }
 
     public void testExplodeString() {
         interpTest("explode-string", itp.makeTerm("\"ab\""), itp.makeList("[97,98]"));
     }
 
     public void testFstTuple() {
         interpTest("Fst_tuple", itp.makeTuple("[1,2]"), itp.makeTerm("1"));
     }
 
     public void testSndTuple() {
         interpTest("Snd_tuple", itp.makeTuple("[1,2]"), itp.makeTerm("2"));
     }
 
     public void testSumOfIntList1() {
         interpTest("sum_of_int_list", itp.makeList("[1,2,3]"), itp.makeTerm("6"));
     }
 
     public void testSumOfIntList2() {
         interpTest("sum_of_int_list", itp.makeList("[1,1,1,1,1,1,1,1,1,1]"), itp.makeTerm("10"));
     }
 
     public void testIncIntList2() {
         interpTest("inc_int_list_2", itp.makeList("[1,2,3]"), itp.makeList("[2,3,4]"));
     }
 
     public void testFetchElem1() {
         interpTest("fetch_elem_1", itp.makeList("[1,2,3]"), itp.makeTerm("2"));
     }
 
     public void testFetch1() {
         interpTest("fetch_1", itp.makeList("[1,2,3]"), itp.makeTerm("2"));
     }
 
     public void testFetch2() {
        interpTestFail("fetch_2", itp.makeList("[1,2,3]"));
     }
 
     public void testConc() {
         interpTest("conc", itp.makeTuple(itp.makeList("[1,2,3]"), itp.makeList("[3,4,5]")), 
                    itp.makeList("[1,2,3,3,4,5]"));
     }
 
     public void testConcat() {
         interpTest("concat", itp.makeList(itp.makeList("[1,2,3]"), itp.makeList("[3,4,5]")), 
                    itp.makeList("[1,2,3,3,4,5]"));
     }
 
     public void testUnion() {
         interpTest("union", itp.makeTuple(itp.makeList("[1,2,3]"), itp.makeList("[3,4,5]")), 
                    itp.makeList("[1,2,3,4,5]"));
     }
 
     public void testTermSize() {
         interpTest("term-size", itp.makeTerm("2"), itp.makeTerm("1"));
     }
 
     public void testCollectOm1() {
         interpTest("collect-om_1", itp.makeList(itp.makeTerm("1"), itp.makeTuple("[2,3]"), itp.makeTerm("3")),
                    itp.makeList("[1,2,3]"));
     }
     
     public void testTopdownTry() {
         interpTest("topdown_try", 
                    itp.makeTuple(
                                  itp.makeTerm("1"),
                                  itp.makeTerm("2"),
                                  itp.makeTuple("[3,4]")),
                                  itp.makeTuple(itp.makeTerm("1"),
                                                itp.makeTerm("2"),
                                                itp.makeTuple("[4,4]")));
     }
     
     public void testWrapSplit1() {
         interpTest("wrap_split_1", itp.makeTerm("2"), itp.makeTuple("[2,2]"));
     }
 
     public void testWrapSplit2() {
         interpTest("wrap_split_2", itp.makeTerm("2"), itp.makeTuple("[2,3]"));
     }
 
     public void testWrapSplit3() {
         interpTest("wrap_split_3", itp.makeTerm("2"), itp.makeTuple("[3,3]"));
     }
 
     public void testWrapSplit4() {
         interpTest("wrap_split_4", itp.makeTerm("2"), itp.makeList("[2,2]"));
     }
 
     public void testProject1() {
         interpTest("project_1", itp.makeTuple("[2,3]"), itp.makeTerm("2"));
     }
 
     public void testProject2() {
         interpTest("project_2", itp.makeTuple("[2,3]"), itp.makeTerm("3"));
     }
     
     public void testTest1() {
         interpTest("test_1", itp.makeTerm("2"), itp.makeTerm("2"));
     }
 
     public void testTest2() {
         interpTestFail("test_2", itp.makeTerm("3"));
     }
 
     public void testTest3() {
         interpTestFail("test_3", itp.makeTuple("[]")); //, itp.makeTerm("3"));
     }
 
     public void testAs1() {
         interpTest("as_1", itp.makeTuple("[1,2]"), itp.makeTuple(itp.makeTuple("[1,2]"), itp.makeTuple("[1,2]")));
     }
 
     public void testAs2() {
         interpTest("as_2", itp.makeTuple("[1,2]"), itp.makeTuple("[1,1]"));
     }
 
     public void testDynruleCounter1() {
         interpTest("dynrule_counter_1", itp.makeTuple("[]"), itp.makeTerm("2"));
     }
 
     public void testClosure1() {
         interpTest("closure_test_1", itp.makeTuple("[]"), itp.makeTerm("1"));
     }
 
     public void testClosure2() {
         interpTest("closure_test_2", itp.makeTuple("[]"), itp.makeTerm("1"));
     }
 
     public void testLet1() {
         interpTest("let_test_1", itp.makeTerm("1"), itp.makeTerm("2"));
     }
     public void testLet2() {
         interpTest("let_test_2", itp.makeTerm("1"), itp.makeTerm("2"));
     }
 
     public void testClosure2b() {
         interpTest("closure_test_2b", 
                    itp.makeList("[5, 5, 5, 5, 5, 5, 5, 5, 5, 5]"), 
                    itp.makeList("[3, 3, 3, 3, 3, 3, 3, 3, 3, 3]"));
     }
 
     public void testClosure2c() {
         interpTest("closure_test_2c", 
                    itp.makeList("[5, 5, 5, 5, 5, 5, 5, 5, 5, 5]"), 
                    itp.makeList("[3, 3, 3, 3, 3, 3, 3, 3, 3, 3]"));
     }
 
     public void testClosure2f() {
         interpTest("closure_test_2f", makeRecTuple(10, "5"), makeRecTuple(10, "3"));
     }
 
     private ATerm makeRecTuple(int i, String t) {
         if(i == 0)
             return itp.makeTuple("[]");
         return itp.makeTuple(itp.makeTerm(t), makeRecTuple(i-1, t)); 
     }
 
     public void testClosure4() {
         interpTest("closure_test_4", itp.makeTerm("1"), itp.makeTerm("3"));
     }
 
     public void testClosure5a() {
         interpTest("closure_test_5a", itp.makeList("[1,2,3]"), itp.makeList("[1,1,1]"));
     }
 
     public void testClosure5b() {
         interpTest("closure_test_5b", itp.makeList("[1,2,3]"), itp.makeList("[1,1,1]"));
     }
 
     public void testClosure6() {
         interpTest("closure_test_6", itp.makeTuple("[]"), itp.makeTerm("1"));
     }
 
     public void testClosure7() {
         interpTest("closure_test_7", 
                    itp.makeList("[5, 5, 5, 5, 5, 5, 5, 5, 5, 5]"), 
                    itp.makeList("[4, 6, 4, 6, 4, 6, 4, 6, 4, 6]"));
     }
 
     public void testClosure8() {
         interpTest("closure_test_8", 
                    itp.makeList("[5, 5, 5, 5, 5, 5, 5, 5, 5, 5]"), 
                    itp.makeList("[4, 6, 4, 6, 4, 6, 4, 6, 4, 6]"));
     }
 
     public void testClosure9() {
         interpTest("closure_test_9", 
                    itp.makeList("[5, 5, 5, 5, 5, 5, 5, 5, 5, 5]"), 
                    itp.makeList("[4, 6, 4, 6, 4, 6, 4, 6, 4, 6]"));
     }
 
    public void testClosure10() {
         interpTest("closure_test_10", 
                    itp.makeList("[5, 5, 5, 5, 5, 5, 5, 5, 5, 5]"), 
                    itp.makeList("[4, 6, 4, 6, 4, 6, 4, 6, 4, 6]"));
     }
 
    public void testClosure11() {
        interpTest("closure_test_11", 
                   itp.makeList("[5, 5, 5, 5, 5, 5, 5, 5, 5, 5]"), 
                   itp.makeList("[4, 6, 4, 6, 4, 6, 4, 6, 4, 6]"));
    }
 
    public void testClosure12() {
        interpTest("closure_test_12", 
                   itp.makeList("[5, 5, 5, 5, 5, 5, 5, 5, 5, 5]"), 
                   itp.makeList("[4, 6, 4, 6, 4, 6, 4, 6, 4, 6]"));
    }
 
    public void testClosure13() {
        interpTest("closure_test_13", 
                   itp.makeTerm("1"), 
                   itp.makeTerm("4"));
    }
    
    public void testGuarded1() {
        interpTest("guarded_1", itp.makeTuple("[]"), itp.makeTerm("1"));
    }
 
    public void testGuarded2() {
        interpTest("guarded_2", itp.makeTuple("[]"), itp.makeTerm("2"));
    }
 
    public void testGuarded3() {
        interpTest("guarded_3", itp.makeTuple("[]"), itp.makeTerm("3"));
    }
 
    public void testGuarded4() {
        interpTest("guarded_4", itp.makeTerm("5"), itp.makeTerm("5"));
    }
 
    public void testGuarded5() {
        interpTest("guarded_5", itp.makeTuple("[]"), itp.makeTerm("1"));
    }
 
    public void testGuarded6() {
        interpTest("guarded_6", itp.makeTuple("[]"), itp.makeTerm("1"));
    }
 
    public void testGuarded7() {
        interpTest("guarded_7", itp.makeTuple("[]"), itp.makeTerm("2"));
    }
 
 }
 
 /*
  
 */
