 package swp_compiler_ss13.fuc.backend;
 
 import org.junit.Test;
 import swp_compiler_ss13.common.backend.BackendException;
 import java.io.*;
 import static org.junit.Assert.assertEquals;
 
 import static swp_compiler_ss13.common.backend.Quadruple.*;
 
 /**
  * Tests for LLVMBackend: Arrays
  */
 public class LLVMBackendArrayTest extends TestBase {
 	/* test test */
 	@Test public void arrays__declare_array__long() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#70", EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		String mainFunctionCode = "" +
 				"  %x = alloca [70 x i64]\n" +
 				"  ret i64 0\n";
 		expectMain("", mainFunctionCode, generateCodeAsString(tac));
 	}
 	@Test public void arrays__declare_array__double() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#25", EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		String mainFunctionCode = "" +
 				"  %x = alloca [25 x double]\n" +
 				"  ret i64 0\n";
 		expectMain("", mainFunctionCode, generateCodeAsString(tac));
 	}
 	@Test public void arrays__declare_array__double_and_long() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#25", EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#35", EmptyArgument, "y"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		String mainFunctionCode = "" +
 				"  %x = alloca [25 x double]\n" +
 				"  %y = alloca [35 x i64]\n" +
 				"  ret i64 0\n";
 		expectMain("", mainFunctionCode, generateCodeAsString(tac));
 	}
 	@Test public void arrays__declare_array__double_prim_long() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#25", EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, "z"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#35", EmptyArgument, "y"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		String mainFunctionCode = "  %x = alloca [25 x double]\n"+
 		           "  %z = alloca double\n"+
 		           "  store double 0.0, double* %z\n"+
 		           "  %y = alloca [35 x i64]\n"+
 		           "  ret i64 0\n"+
 		           "";
 		expectMain("", mainFunctionCode, generateCodeAsString(tac));
 	}
 
 	@Test public void arrays__declare_array__3d_long() throws IOException, BackendException {
 	   	tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#70", EmptyArgument, "x"));
 	   	tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#600", EmptyArgument, null));
 	   	tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#2", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		String mainFunctionCode = "" +
 				"  %x = alloca [70 x [600 x [2 x i64]]]\n" +
 				"  ret i64 0\n";
 		expectMain("", mainFunctionCode, generateCodeAsString(tac));
  	}
 
 	@Test public void arrays__get__double() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_DOUBLE, "a", "#12", "x"));
 		expectMain("",
 				   "  %x = alloca double\n"+
 		           "  store double 0.0, double* %x\n"+
 		           "  %a = alloca [20 x double]\n"+
 				   "  call void @aoob1(i64 12, i64 20)\n"+
		           "  %a.0 = getelementptr [20 x double]* %a, i64 0, i64 12\n"+
 		           "  %x.0 = load double* %a.0\n"+
 		           "  store double %x.0, double* %x\n"+
 		           "  ret i64 0\n"+
 		           "",
 				   generateCodeAsString(tac));
     }
 
 	// careful: variables given to ARRAY_SET_DOUBLE get loaded (cdl'ed) AFTER the out of bounds check!
 	@Test public void arrays__set__double() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE,"#66.5", EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_SET_DOUBLE, "a", "#12", "x"));
 		expectMain("",
 				   "  %x = alloca double\n"+
 		           "  store double 66.5, double* %x\n"+
 		           "  %a = alloca [20 x double]\n"+
 				   "  call void @aoob1(i64 12, i64 20)\n"+
 		           "  %x.0 = load double* %x\n"+
		           "  %a.0 = getelementptr [20 x double]* %a, i64 0, i64 12\n"+
 		           "  store double %x.0, double* %a.0\n"+
 		           "  ret i64 0\n"+
 		           "",
 				   generateCodeAsString(tac));
     }
 
 	@Test public void arrays__set__const_double() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_SET_DOUBLE, "a", "#12", "#88.6"));
 		expectMain("",
 				   "  %a = alloca [20 x double]\n"+
 				   "  call void @aoob1(i64 12, i64 20)\n"+
		           "  %a.0 = getelementptr [20 x double]* %a, i64 0, i64 12\n"+
 		           "  store double 88.6, double* %a.0\n"+
 		           "  ret i64 0\n"+
 		           "",
 				   generateCodeAsString(tac));
     }
 
 	@Test public void arrays__set__const_long() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_SET_LONG, "a", "#12", "#17"));
         expectMain("",
 				   "  %a = alloca [20 x i64]\n"+
 				   "  call void @aoob1(i64 12, i64 20)\n"+
		           "  %a.0 = getelementptr [20 x i64]* %a, i64 0, i64 12\n"+
 		           "  store i64 17, i64* %a.0\n"+
 		           "  ret i64 0\n"+
 		           "",
 				   generateCodeAsString(tac));
     }
 
 	@Test public void arrays__declare_reference() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, EmptyArgument, EmptyArgument, "r"));
 		expectMain("", "  ret i64 0"+"\n",
 				   generateCodeAsString(tac));
 	}
 
 	@Test public void arrays__get_reference__2d_double() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#30", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, EmptyArgument, EmptyArgument, "r"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "a", "#15", "r"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, "v"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_DOUBLE, "r", "#25", "v"));
 		expectMain("",
 				   "  %a = alloca [20 x [30 x double]]\n"+
 		           "  %v = alloca double\n"+
 		           "  store double 0.0, double* %v\n"+
 				   "  call void @aoob1(i64 15, i64 20)\n"+
 				   "  call void @aoob1(i64 25, i64 30)\n"+
		           "  %a.0 = getelementptr [20 x [30 x double]]* %a, i64 0, i64 15, i64 25\n"+
 		           "  %v.0 = load double* %a.0\n"+
 		           "  store double %v.0, double* %v\n"+
 		           "  ret i64 0\n"+
 		           "",
 		           generateCodeAsString(tac));
     }
 
 	@Test public void arrays__use_reference_two_times() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#30", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, EmptyArgument, EmptyArgument, "r"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "a", "#15", "r"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "a", "#16", "r"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, "v"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_DOUBLE, "r", "#25", "v"));
 		expectMain("",
 				   "  %a = alloca [20 x [30 x double]]\n"+
 		           "  %v = alloca double\n"+
 		           "  store double 0.0, double* %v\n"+
 				   "  call void @aoob1(i64 16, i64 20)\n"+
 				   "  call void @aoob1(i64 25, i64 30)\n"+
		           "  %a.0 = getelementptr [20 x [30 x double]]* %a, i64 0, i64 16, i64 25\n"+
 		           "  %v.0 = load double* %a.0\n"+
 		           "  store double %v.0, double* %v\n"+
 		           "  ret i64 0\n"+
 		           "",
 				   generateCodeAsString(tac));
     }
 	@Test public void arrays__reference_self_resolve() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, EmptyArgument, EmptyArgument, "r"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#30", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#40", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "a", "#15", "r"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "r", "#20", "r"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, "v"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_DOUBLE, "r", "#25", "v"));
 		expectMain("",
 				   "  %a = alloca [20 x [30 x [40 x i64]]]\n"+
 		           "  %v = alloca i64\n"+
 		           "  store i64 0, i64* %v\n"+
 				   "  call void @aoob1(i64 15, i64 20)\n"+
 				   "  call void @aoob1(i64 20, i64 30)\n"+
 				   "  call void @aoob1(i64 25, i64 40)\n"+
		           "  %a.0 = getelementptr [20 x [30 x [40 x i64]]]* %a, i64 0, i64 15, i64 20, i64 25\n"+
 		           "  %v.0 = load double* %a.0\n"+
 		           "  store double %v.0, double* %v\n"+
 		           "  ret i64 0\n"+
 		           "",
 				   generateCodeAsString(tac));
     }
 	@Test public void arrays__reference_self_resolve_set_long() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, EmptyArgument, EmptyArgument, "r"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#30", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#40", EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "a", "#15", "r"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "r", "#20", "r"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_SET_DOUBLE, "r", "#25", "#999"));
 		expectMain("",
 				   "  %a = alloca [20 x [30 x [40 x i64]]]\n"+
 				   "  call void @aoob1(i64 15, i64 20)\n"+
 				   "  call void @aoob1(i64 20, i64 30)\n"+
 				   "  call void @aoob1(i64 25, i64 40)\n"+
		           "  %a.0 = getelementptr [20 x [30 x [40 x i64]]]* %a, i64 0, i64 15, i64 20, i64 25\n"+
 		           "  store double 999, double* %a.0\n"+
 		           "  ret i64 0\n"+
 		           "",
 				   generateCodeAsString(tac));
     }
 
 	@Test public void arrays__strings() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#4", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_STRING, EmptyArgument, EmptyArgument, null));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_STRING, EmptyArgument, EmptyArgument, "x"));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_SET_STRING, "a", "#2", "#\"Hello, World!\""));
 		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_STRING, "a", "#2", "x"));
 		tac.add(new QuadrupleImpl(Operator.PRINT_STRING, "x", EmptyArgument, EmptyArgument));
 		expectMain(// string constants
 				   "@.string_0 = constant [1 x i8][i8 0]\n"+
 				   "@.string_1 = constant [14 x i8][i8 72, i8 101, i8 108, i8 108, i8 111, i8 44, i8 32, i8 87, i8 111, i8 114, i8 108, i8 100, i8 33, i8 0]\n",
 		           // main
 				   "  %a = alloca [4 x i8*]\n"+
 		           "  %x = alloca i8*\n"+
 		           "  %x.0 = getelementptr [1 x i8]* @.string_0, i64 0, i64 0\n"+
 		           "  store i8* %x.0, i8** %x\n"+
 				   "  call void @aoob1(i64 2, i64 4)\n"+
 		           "  %.tmp.0 = getelementptr [14 x i8]* @.string_1, i64 0, i64 0\n"+
		           "  %a.0 = getelementptr [4 x i8*]* %a, i64 0, i64 2\n"+
 		           "  store i8* %.tmp.0, i8** %a.0\n"+
 				   "  call void @aoob1(i64 2, i64 4)\n"+
		           "  %a.2 = getelementptr [4 x i8*]* %a, i64 0, i64 2\n"+
 		           "  %x.1 = load i8** %a.2\n"+
 		           "  store i8* %x.1, i8** %x\n"+
 		           "  %x.2 = load i8** %x\n"+
 		           "  call i32 (i8*, ...)* @printf(i8* %x.2)\n"+
 		           "  ret i64 0\n"+
 		           "",
 		generateCodeAsString(tac));
 				   
 /*		expectMain("",
 				   "  %a = alloca [4 x i8*]\n"+
 		           "  %x = alloca i8*\n"+
 		           "  %.string_0 = alloca [1 x i8]\n"+
 		           "  store [1 x i8] [i8 0], [1 x i8]* %.string_0\n"+
 		           "  %x.0 = getelementptr [1 x i8]* %.string_0, i64 0, i64 0\n"+
 		           "  store i8* %x.0, i8** %x\n"+
 				   "  call void @aoob1(i64 2, i64 4)\n"+
 		           "  %.string_1 = alloca [14 x i8]\n"+
 		           "  store [14 x i8] [i8 72, i8 101, i8 108, i8 108, i8 111, i8 44, i8 32, i8 87, i8 111, i8 114, i8 108, i8 100, i8 33, i8 0], [14 x i8]* %.string_1\n"+
 		           "  %.tmp.0 = getelementptr [14 x i8]* %.string_1, i64 0, i64 0\n"+
 		           "  %a.0 = getelementptr [4 x i8*]* %a, i64 0, i64 2\n"+
 		           "  store i8* %.tmp.0, i8** %a.0\n"+
 				   "  call void @aoob1(i64 2, i64 4)\n"+
 		           "  %a.2 = getelementptr [4 x i8*]* %a, i64 0, i64 2\n"+
 		           "  %x.1 = load i8** %a.2\n"+
 		           "  store i8* %x.1, i8** %x\n"+
 		           "  %x.2 = load i8** %x\n"+
 		           "  call i32 (i8*, ...)* @printf(i8* %x.2)\n"+
 		           "  ret i64 0\n",
 		           generateCodeAsString(tac));*/
     }
 
 // completely broken, either im wrong about the struct interface the struct interface is wrong
 /*	@Test public void arrays__of_structs() throws IOException, BackendException {
 		tac.add(new QuadrupleImpl(Operator.DECLARE_ARRAY, "#20", EmptyArgument, "a"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_STRUCT, "#2", EmptyArgument, "ss"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, "s1"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, "s2"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, EmptyArgument, EmptyArgument, "r"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, "l"));
 //		tac.add(new QuadrupleImpl(Operator.ARRAY_GET_REFERENCE, "a", "#5", "r"));
 //		tac.add(new QuadrupleImpl(Operator.STRUCT_GET_LONG, "r",".s2","l"));
 
 		tac.add(new QuadrupleImpl(Operator.DECLARE_STRUCT, "#2", EmptyArgument, "s"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_DOUBLE, EmptyArgument, EmptyArgument, "s1"));
 		tac.add(new QuadrupleImpl(Operator.DECLARE_LONG, EmptyArgument, EmptyArgument, "s2"));
 		tac.add(new QuadrupleImpl(Operator.STRUCT_GET_LONG, "s","s.s2","l"));
 		System.out.println(generateCodeAsString(tac));
 		assertEquals(true,true);
 		}*/
 }
