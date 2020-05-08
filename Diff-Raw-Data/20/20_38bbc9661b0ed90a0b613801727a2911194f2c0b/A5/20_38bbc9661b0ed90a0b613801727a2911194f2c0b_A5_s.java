 package cs444.acceptance;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 
 public class A5 {
     @Test
     public void testCompileProgramsNoStdLib() throws IOException, InterruptedException{
         List<String> ignoreList = new LinkedList<String>(
                 Arrays.asList(
                         "NullCheck"
                         )
                 );
 
         TestHelper.assertReturnCodeForFiles("JoosPrograms/A5/NoStdLibPrograms/", 0, true, false,
                 true, ignoreList, new AsmAndLinkCallback());
     }
 
     //@Ignore("not ready")
     @Test
     public void testMarmosetPrograms() throws IOException, InterruptedException{
 
 
         List<String> failingList = new LinkedList<String>(Arrays.asList(
                                                                        "J1e_divisionbyzero",
 "J1_1_Instanceof_OfAdditiveExpression",
 "J1_A_ArrayBaseInAssignment",
 "J1_A_ArrayStoreLoad",
 "J1_A_BooleanArray_External",
 "J1_A_ConcatInSimpleInvoke",
 "J1_A_ConcatInStaticInvoke",
 "J1_A_String_ByteShortCharInt",
 "J1_Hello",
 "J1_StringCast",
 "J1_WildConcat",
 "J1_charadd",
 "J1_callbeforereturn",
 "J1_concatInMethods",
 "J1_concat_in_binop",
 "J1_constructorbodycast",
 "J1_implicitstringconcatenation",
 "J1_intstringadd",
 "J1_nestedcast",
 "J1_sideeffect7",
 "J1_sideeffects_array",
 "J1_sideeffects_array2",
 "J1_sideeffects_array3",
 "J1_sideeffects_array4",
 "J1_sideeffects_obj2",
 "J1_sideeffects_obj",
 "J1_sideeffects_obj3",
 "J1_sim_xor",
 "J1_stringadd",
 "J1_stringconcat",
 "J1_typecheck_array",
 "J1_typecheck_expstm"
 ));
 
         Map<String, String> env = System.getenv();
         String allTest = env.get("all_tests");
         if (allTest != null && allTest.equals("true")){
             TestHelper.assertReturnCodeForFiles("JoosPrograms/MarmosetPrograms/a5/", 0, true, true,
                     true, Collections.<String>emptyList(), new AsmAndLinkCallback());
         } else{
             TestHelper.assertReturnCodeForFiles("JoosPrograms/MarmosetPrograms/a5/", 0, true, true,
                     true, failingList, new AsmAndLinkCallback());
         }
     }
 }
 
