 // $Header$
 
 // For j2meunit see http://j2meunit.sourceforge.net/
 import j2meunit.framework.Test;
 import j2meunit.framework.TestCase;
 import j2meunit.framework.TestSuite;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 
 // The VMTest uses ancillary files:
 // Mostly these are compiled versions of one line Lua scripts compiled
 // in the following manner:
//   luac -o VMTestLoadbool.luc - << 'EOF'
 //   return x==nil
 //   EOF
 //
 // In the following list, the Lua script follows the filename.
 //
 // VMTestLoadbool.luc - a binary chunk containing OP_LOADBOOL.
 //   return x==nil
 // VMTestLoadnil.luc - a binary chunk containing OP_LOADNIL.
 //   local a,b,c; a,b,c="foo","bar","baz"; a,b,c=7; return a,b,c
 // VMTestAdd.luc - a binary chunk containing OP_ADD.
 //   local a,b,c=3,7,8;return a+b+c
 // VMTestSub.luc - a binary chunk containing OP_SUB.
 //   local a,b,c = 18, 3, 5;return a - (b - c)
 // VMTestConcat.luc - a binary chunk containing OP_CONCAT.
 //   local a,b,c="foo","bar","baz";return a..b..c
 // VMTestSettable.luc - a binary chunk containing OP_SETTABLE.
 //   return {a=1, b=2, c=3}
 // VMTestGettable.luc - a binary chunk containing OP_GETTABLE.
 //   local a={a=2,b=3,c=23};return a.a+a.b+a.c
 // VMTestSetlist.luc - a binary chunk containing OP_SETLIST.
 //   local a = {13, 18, 1};return a[1]+a[2]+a[3]
 // VMTestCall.luc - a binary chunk containing OP_CALL.
 //   return f(7)+1
 // VMTestClosure.luc - a binary chunk containing OP_CLOSURE.
 //   return function(x)return "foo"..x end 
 
 
 
 /**
  * J2MEUnit tests for Jili's VM execution.  DO NOT SUBCLASS.  public
  * access granted only because j2meunit makes it necessary.
  * This test does not run in CLDC 1.1.
  */
 public class VMTest extends TestCase {
   /** void constructor, necessary for running using
    * <code>java j2meunit.textui.TestRunner VMTest</code>
    */
   public VMTest() { }
 
   /** Clones constructor from superclass.  */
   private VMTest(String name) {
     super(name);
   }
 
   /**
    * @param L         Lua state in which to load file.
    * @param filename  filename without '.luc' extension.
    */
   private LuaFunction loadFile(Lua L, String filename) {
     filename += ".luc";
     System.out.println(filename);
     LuaFunction f = null;
     try {
       f = L.load(new FileInputStream(filename), filename);
     } catch (Exception e) {
       e.printStackTrace();
     }
     return f;
   }
 
   /**
    * Tests execution of the OP_LOADBOOL opcode.  This opcode is
    * generated when the results of a boolean expression are used for its
    * value (as opposed to inside an "if").  Our test is "return x==nil".
    * This generates both the skip and non-skip forms.
    */
   public void testVMLoadbool() {
     Lua L = new Lua();
     LuaFunction f;
     f = loadFile(L, "VMTestLoadbool");
     L.push(f);
     L.call(0, 1);
     Object res = L.value(1);
     Boolean b = (Boolean)res;
     assertTrue("Result is true", b.booleanValue());
     L.rawset(L.getGlobals(), "x", "foo");
     L.push(f);
     L.call(0, 1);
     res = L.value(-1);
     b = (Boolean)res;
     assertTrue("Result is false", b.booleanValue() == false);
   }
 
   /**
    * Tests execution of OP_LOADNIL opcode.  This opcode is generated for
    * assignment sequences like "a,b,c=7".
    */
   public void testVMLoadnil() {
     Lua L = new Lua();
     LuaFunction f;
     f = loadFile(L, "VMTestLoadnil");
     L.push(f);
     L.call(0, 3);
     Object first = L.value(1);
     Double d = (Double)first;
     assertTrue("First result is 7", d.doubleValue() == 7);
     assertTrue("Second result is nil", L.value(2) == L.NIL);
     assertTrue("Third result is nil", L.value(3) == L.NIL);
   }
 
   /** Tests execution of OP_ADD opcode. */
   public void testVMAdd() {
     Lua L = new Lua();
     LuaFunction f;
     f = loadFile(L, "VMTestAdd");
     L.push(f);
     L.call(0, 1);
     Object res = L.value(1);
     Double d = (Double)res;
     assertTrue("Result is 18", d.doubleValue() == 18);
   }
 
   /** Tests execution of OP_SUB opcode. */
   public void testVMSub() {
     Lua L = new Lua();
     LuaFunction f;
     f = loadFile(L, "VMTestSub");
     L.push(f);
     L.call(0, 1);
     Object res = L.value(1);
     Double d = (Double)res;
     assertTrue("Result is 20", d.doubleValue() == 20);
   }
 
   /** Tests execution of OP_CONCAT opcode. */
   public void testVMConcat() {
     Lua L = new Lua();
     LuaFunction f;
     f = loadFile(L, "VMTestConcat");
     L.push(f);
     L.call(0, 1);
     Object res = L.value(1);
     String s = (String)res;
     assertTrue("Result is foobarbaz", s.equals("foobarbaz"));
   }
 
   /** Tests execution of OP_SETTABLE opcode. */
   public void testVMSettable() {
     Lua L = new Lua();
     LuaFunction f;
     f = loadFile(L, "VMTestSettable");
     L.push(f);
     L.call(0, 1);
     Object t = L.value(1);
     Double d;
     d = (Double)L.rawget(t, "a");
     assertTrue("t.a == 1", d.doubleValue() == 1);
     d = (Double)L.rawget(t, "b");
     assertTrue("t.b == 2", d.doubleValue() == 2);
     d = (Double)L.rawget(t, "c");
     assertTrue("t.c == 3", d.doubleValue() == 3);
     assertTrue("t.d == nil", L.isNil(L.rawget(t, "d")));
   }
 
   /** Tests execution of OP_GETTABLE opcode. */
   public void testVMGettable() {
     Lua L = new Lua();
     LuaFunction f;
     f = loadFile(L, "VMTestGettable");
     L.push(f);
     L.call(0, 1);
     Double d = (Double)L.value(1);
     assertTrue("Result is 28", d.doubleValue() == 28);
   }
 
   /**
    * Tests execution of OP_SETLIST opcode.
    * :todo: There are special cases in SETLIST that are currently untested:
    * When field B is 0 (all elements up to TOS are set);
    * when field C is 0 (starting index is loaded from next opcode).
    * The former is presumably generated when a list initialiser has at
    * least 25,600 (512*50) elements (!).  The latter is generated when an
    * expression like "{...}" is used.
    */
   public void testVMSetlist() {
     Lua L = new Lua();
     LuaFunction f;
     f = loadFile(L, "VMTestSetlist");
     L.push(f);
     L.call(0, 1);
     Double d = (Double)L.value(1);
     assertTrue("Result is 32", d.doubleValue() == 32);
   }
 
   /** Tests execution of OP_CALL opcode. */
   public void testVMCall() {
     Lua L = new Lua();
     // Create a Lua Java function and install it in the global 'f'.
     class Mine extends LuaJavaCallback {
       int luaFunction(Lua L) {
         double d = L.toNumber(L.value(1));
         L.pushNumber(d*3);
         return 1;
       }
     }
     L.rawset(L.getGlobals(), "f", new Mine());
     LuaFunction f;
     f = loadFile(L, "VMTestCall");
     L.push(f);
     L.call(0, 1);
     assertTrue("Result is 22", L.toNumber(L.value(1)) == 22);
   }
 
   /** Tests execution of OP_CLOSURE opcode. */
   public void testVMClosure() {
     Lua L = new Lua();
     LuaFunction f;
     f = loadFile(L, "VMTestClosure");
     L.push(f);
     L.call(0, 1);
     Object o = L.value(1);
     L.push("bar");
     L.call(1, 1);
     assertTrue("Result is foobar", "foobar".equals(L.value(-1)));
   }
 
   public Test suite() {
     TestSuite suite = new TestSuite();
 
     suite.addTest(new VMTest("testVMLoadbool") {
         public void runTest() { testVMLoadbool(); } });
     suite.addTest(new VMTest("testVMLoadnil") {
         public void runTest() { testVMLoadnil(); } });
     suite.addTest(new VMTest("testVMAdd") {
         public void runTest() { testVMAdd(); } });
     suite.addTest(new VMTest("testVMSub") {
         public void runTest() { testVMSub(); } });
     suite.addTest(new VMTest("testVMConcat") {
         public void runTest() { testVMConcat(); } });
     suite.addTest(new VMTest("testVMSettable") {
         public void runTest() { testVMSettable(); } });
     suite.addTest(new VMTest("testVMGettable") {
         public void runTest() { testVMGettable(); } });
     suite.addTest(new VMTest("testVMSetlist") {
         public void runTest() { testVMSetlist(); } });
     suite.addTest(new VMTest("testVMCall") {
         public void runTest() { testVMCall(); } });
     suite.addTest(new VMTest("testVMClosure") {
         public void runTest() { testVMClosure(); } });
     return suite;
   }
 }
