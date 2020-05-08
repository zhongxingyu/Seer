 package cs444.acceptance;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class A5 {
 
     @Test
     public void testCompileProgramsNoStdLib() throws IOException, InterruptedException{
         TestHelper.assertReturnCodeForFiles("JoosPrograms/A5/NoStdLibPrograms/", 0, true, false,
                 true, Collections.<String>emptyList(), new AsmAndLinkCallback());
     }
 
     // @Ignore("not ready")
     @Test
     public void testMarmosetPrograms() throws IOException, InterruptedException{
         List<String> ignoreList = new LinkedList<String>(Arrays.asList(
                 "J1_01",
                 "J1_1_Instanceof_InLazyExp",
                 "J1_1_Instanceof_OfAdditiveExpression",
                 "J1_1_Instanceof_OfCastExpression",
                 "J1_6_Assignable_Object_ObjectArray",
                 "J1_ArrayCreateAndIndex",
                 "J1_6_AssignmentInArrayLength",
                 "J1_A_AddressNotEqual",
                 "J1_A_ArrayBaseInAssignment",
                 "J1_A_ArrayStoreLoad",
                 "J1_A_AssignmentInLazyOr",
                 "J1_A_BooleanArray_External",
                "J1_A_CloneOnInterface",
                 "J1_A_CloneWithArgs",
                 "J1_A_Complement_SideEffect",
                 "J1_A_ConcatInSimpleInvoke",
                 "J1_A_ConcatInStaticInvoke",
                 "J1_A_FieldInitialization_Before",
                 "J1_A_FieldInitialization_NonConstant_Before",
                 "J1_A_LazyEagerAndOr",
                 "J1_A_LazyEval",
                 "J1_arrayAccess",
                 "J1_arrayinstanceof1",
                 "J1_arrayinstanceof2",
                 "J1_array",
                 "J1_backwardRef",
                 "J1_callbeforereturn",
                 "J1_concat_in_binop",
                 "J1_concatInMethods",
                 "J1_constructorbodycast",
                 "J1_divdiv",
                 "J1e_A_CastNewExp",
                 "J1e_A_CastToArray",
                 "J1e_A_CastToString",
                 "J1_fieldinit_forward_ref2",
                 "J1_fieldinit_forward_ref",
                 "J1_fieldinit",
                 "J1_Hello",
                 "J1_implicitstringconcatenation",
                 "J1_instanceof_array2",
                 "J1_instanceof_array",
                 "J1_intstringadd",
                 "J1_NamedTypeArray",
                 "J1_nestedcast",
                 "J1_sideeffect1",
                 "J1_sideeffect2",
                 "J1_sideeffect3",
                 "J1_sideeffect4",
                 "J1_sideeffect5",
                 "J1_sideeffect6",
                 "J1_sideeffect7",
                 "J1_sideeffect8",
                 "J1_sideeffects_array2",
                 "J1_sideeffects_array3",
                 "J1_sideeffects_array4",
                 "J1_sideeffects_array",
                 "J1_sideeffects_obj",
                 "J1_sideeffects_obj2",
                 "J1_sideeffects_obj3",
                 "J1_sim_and",
                 "J1_sim_or",
                 "J1_SimpleTypeArray",
                 "J1_sim_xor",
                 "J1_SmallInt",
                 "J1_StaticField_AccessFromClass",
                 "J1_staticMethodInvocation",
                 "J1_stringadd",
                 "J1_StringCast",
                 "J1_stringconcat",
                 "J1_toomuchinc",
                 "J1_typecheck_array",
                 "J1_typecheck_expstm",
                 "J1_typecheck_plus",
                 "J1_WildConcat",
                 "J2_A_FieldInitialization_Static_Before",
                 "J2_A_FieldInitialization_Static_NonConstant_Before",
                 "J2_fieldinit_forward_ref",
                 "J1_A_String_ByteShortCharInt",
                 "J1_charadd",
                 "J1_random_arithmetic_var"));
 
         List<String> failingList = new LinkedList<String>(Arrays.asList("J1e_A_CastNewExp",
 "J1e_A_CastToArray",
 "J1e_A_CastToString",
 "J1_01",
 "J1_1_Instanceof_InLazyExp",
 "J1_1_Instanceof_OfAdditiveExpression",
 "J1_1_Instanceof_OfCastExpression",
 "J1_6_Assignable_Object_ObjectArray",
 "J1_6_AssignmentInArrayLength",
 "J1_array",
 "J1_arrayAccess",
 "J1_ArrayCreateAndIndex",
 "J1_arrayinstanceof1",
 "J1_A_AddressNotEqual",
 "J1_A_ArrayBaseInAssignment",
 "J1_A_ArrayStoreLoad",
 "J1_A_BooleanArray_External",
//"J1_A_CloneOnInterface",
 "J1_A_ConcatInSimpleInvoke",
 "J1_A_ConcatInStaticInvoke",
 "J1_A_String_ByteShortCharInt",
 "J1_callbeforereturn",
 "J1_charadd",
 "J1_concatInMethods",
 "J1_concat_in_binop",
 "J1_constructorbodycast",
 "J1_divdiv",
 "J1_Hello",
 "J1_implicitstringconcatenation",
 "J1_intstringadd",
 "J1_nestedcast",
 "J1_random_arithmetic_var",
 "J1_sideeffect1",
 "J1_sideeffect6",
 "J1_sideeffect7",
 "J1_sideeffects_array",
 "J1_sideeffects_array2",
 "J1_sideeffects_array3",
 "J1_sideeffects_array4",
 "J1_sideeffects_obj",
 "J1_sideeffects_obj3",
 "J1_sim_xor",
 "J1_stringadd",
 "J1_StringCast",
 "J1_stringconcat",
 "J1_typecheck_array",
 "J1_typecheck_expstm",
 "J1_WildConcat"
 ));
 
         Map<String, String> env = System.getenv();
         String allTest = env.get("all_tests");
         if (allTest == null){
             TestHelper.assertReturnCodeForFiles("JoosPrograms/MarmosetPrograms/a5/", 0, true, true,
                     true, ignoreList, new AsmAndLinkCallback());
         } else if (allTest.equals("true")){
             TestHelper.assertReturnCodeForFiles("JoosPrograms/MarmosetPrograms/a5/", 0, true, true,
                     true, Collections.<String>emptyList(), new AsmAndLinkCallback());
         } else{
             TestHelper.assertReturnCodeForFiles("JoosPrograms/MarmosetPrograms/a5/", 0, true, true,
                     true, failingList, new AsmAndLinkCallback());
         }
     }
 }
 
