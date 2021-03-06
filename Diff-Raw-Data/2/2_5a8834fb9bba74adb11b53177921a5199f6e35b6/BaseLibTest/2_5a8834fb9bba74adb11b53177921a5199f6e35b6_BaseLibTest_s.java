 // $Header$
 
 // For j2meunit see http://j2meunit.sourceforge.net/
 import j2meunit.framework.Test;
 import j2meunit.framework.TestSuite;
 
 // Auxiliary files
 // BaseLibTestLoadfile.luc
 //   return 99
 // BaseLibTest.luc - contains functions that test each of base library
 // functions:
 //   function testprint()
 //     print()
 //     print(7, 'foo', {}, nil, function()end, true, false, -0.0)
 //   end
 //   function testtostring()
 //     return '7' == tostring(7),
 //         'foo' == tostring'foo',
 //         'nil' == tostring(nil),
 //         'true' == tostring(true),
 //         'false' == tostring(false)
 //   end
 //   function testtonumber()
 //     return 1 == tonumber'1',
 //         nil == tonumber'',
 //         nil == tonumber{},
 //         nil == tonumber(false),
 //         -2.5 == tonumber'-2.5'
 //   end
 //   function testtype()
 //     return type(nil) == 'nil',
 //         type(1) == 'number',
 //         type'nil' == 'string',
 //         type{} == 'table',
 //         type(function()end) == 'function',
 //         type(type==type) == 'boolean'
 //   end
 //   function testselect()
 //     return select(2, 6, 7, 8) == 7,
 //         select('#', 6, 7, 8) == 3
 //   end
 //   function testunpack()
 //     a,b,c = unpack{'foo', 'bar', 'baz'}
 //     return a == 'foo', b == 'bar', c == 'baz'
 //   end
 //   function testpairs()
 //     local t = {'alderan', 'deneb', 'vega'}
 //     local u = {}
 //     local x = 0
 //     for k,v in pairs(t) do
 //       u[v] = true
 //       x = x + k
 //     end
 //     return x==6, u.alderan, u.deneb, u.vega
 //   end
 //   function testnext()
 //     local t = {'alderan', 'deneb', 'vega'}
 //     local u = {}
 //     local x = 0
 //     for k,v in next, t, nil do
 //       u[v] = true
 //       x = x + k
 //     end
 //     return x==6, u.alderan, u.deneb, u.vega
 //   end
 //   function testipairs()
 //     local t = {'a', 'b', 'c', foo = 'bar' }
 //     local u = {}
 //     for k,v in ipairs(t) do
 //       u[k] = v
 //     end
 //     return u[1]=='a', u[2]=='b', u[3]=='c', u.foo==nil
 //   end
 //   function testrawequal()
 //     local eq = rawequal
 //     return eq(nil, nil),
 //         eq(1, 1),
 //         eq('foo', "foo"),
 //         eq(true, true),
 //         not eq(nil, false),
 //         not eq({}, {}),
 //         not eq(1, 2)
 //   end
 //   function testrawget()
 //     local t = {a='foo'}
 //     return rawget(t, 'a')=='foo', rawget(t, 'foo')==nil
 //   end
 //   function testrawset()
 //     local t = {}
 //     rawset(t, 'b', 'bar')
 //     return t.b=='bar', t.bar==nil
 //   end
 //   function testgetfenv()
 //     return type(getfenv(type))=='table'
 //   end
 //   function testsetfenv()
 //     x='global'
 //     local function f()return function()return x end end
 //     local f1 = f()
 //     local f2 = f()
 //     local f3 = f()
 //     local a,b,c = (f1()=='global'), (f2()=='global'), (f3()=='global')
 //     setfenv(f2, {x='first'})
 //     setfenv(f3, {x='second'})
 //     local d,e,f = (f1()=='global'), (f2()=='first'), (f3()=='second')
 //     return a,b,c,d,e,f
 //   end
 //   function testpcall()
 //     return pcall(function()return true end)
 //   end
 //   function testerror()
 //     local a,b = pcall(function()error('spong',0)end)
 //     return a==false, b=='spong'
 //   end
 //   function testmetatable()
 //     local t,m={},{}
 //     local r1 = getmetatable(t)==nil
 //     setmetatable(t, m)
 //     return r1, getmetatable(t)==m
 //   end
 //   function test__metatable()
 //     local t,f,m={},{},{}
 //     m.__metatable=f
 //     setmetatable(t, m)
 //     return (pcall(function()setmetatable(t, m)end))==false,
 //       getmetatable(t)==f
 //   end
 //   function test__tostring()
 //     local t,m={},{}
 //     m.__tostring = function()return'spong'end
 //     setmetatable(t, m)
 //     return tostring(t)=='spong'
 //   end
 //   function testcollectgarbage() -- very weak test
 //     collectgarbage'collect'
 //     return type(collectgarbage'count') == 'number'
 //   end
 //   function testassert()
 //     local a,b = pcall(function()assert(false)end)
 //     local c,d = pcall(function()return assert(1)end)
 //     return a==false, type(b)=='string', c==true, d==1
 //   end
 //   function testloadstring()
 //     local f = loadstring'return 99'
 //     return f()==99
 //   end
 //   testloadfilename='BaseLibTestLoadfile.luc'
 //   function testloadfile()
 //     local f = loadfile(testloadfilename)
 //     return f()==99
 //   end
 //   function loader(s) -- helper for testload
 //     return function()local x=s s=nil return x end
 //   end
 //   function testload()
 //     local f = load(loader'return 99')
 //     return f()==99
 //   end
 //   function testdofile()
 //     return dofile(testloadfilename)==99
 //   end
//   function testdoxpcall()
 //     local function anerror()return {}..{}end
 //     local function seven()return 7 end
 //     local a,b = xpcall(anerror, nil)
 //     local c,d = xpcall(anerror, seven)
 //     local e,f = xpcall(seven, anerror)
 //     return a == false, c == false, d == 7, e == true, f == 7
 //   end
 
 
 // :todo: test radix conversion for tonumber.
 // :todo: test unpack with non-default arguments.
 // :todo: test rawequal for things with metamethods.
 // :todo: test rawget for tables with metamethods.
 // :todo: test rawset for tables with metamethods.
 // :todo: (when string library is available) test the strings returned
 //     by error and assert.
 
 
 /**
  * J2MEUnit tests for Jili's BaseLib (base library).  DO NOT SUBCLASS.
  * public access granted only because j2meunit makes it necessary.
  */
 public class BaseLibTest extends JiliTestCase {
   /** void constructor, necessary for running using
    * <code>java j2meunit.textui.TestRunner BaseLibTest</code>
    */
   public BaseLibTest() { }
 
   /** Clones constructor from superclass.  */
   private BaseLibTest(String name) {
     super(name);
   }
 
   /**
    * Tests BaseLib.
    */
   public void testBaseLib() {
     System.out.println("BaseLibTest.testBaseLib()");
     Lua L = new Lua();
 
     BaseLib.open(L);
 
     // Test that each global name is defined as expected.
     String[] name = {
       "_VERSION",
       "_G", "ipairs", "pairs", "print", "rawequal", "rawget", "rawset",
       "select", "tonumber", "tostring", "type", "unpack"
     };
     for (int i=0; i<name.length; ++i) {
       Object o = L.getGlobal(name[i]);
       assertTrue(name[i] + " exists", !L.isNil(o));
     }
   }
 
   /**
    * Opens the base library into a fresh Lua state, calls a global
    * function, and returns the Lua state.
    * @param name  name of function to call.
    * @param n     number of results expected from function.
    */
   private Lua luaGlobal(String name, int n) {
     Lua L = new Lua();
     BaseLib.open(L);
     loadFile(L, "BaseLibTest");
     L.call(0, 0);
     System.out.println(name);
     L.push(L.getGlobal(name));
     L.call(0, n);
     return L;
   }
 
   /**
    * Calls a global lua function and checks that <var>n</var> results
    * are all true.
    */
   private void nTrue(String name, int n) {
     Lua L = luaGlobal(name, n);
     for (int i=1; i<=n; ++i) {
       assertTrue("Result " + i + " is true",
 	  L.valueOfBoolean(true).equals(L.value(i)));
     }
   }
 
   /**
    * Tests print.  Not much we can reasonably do here apart from call
    * it.  We can't automatically check that the output appears anywhere
    * or is correct.  This also tests tostring to some extent; print
    * calls tostring internally, so this tests that it can be called
    * without error, for example.
    */
   public void testPrint() {
     luaGlobal("testprint", 0);
   }
 
   public void testTostring() {
     nTrue("testtostring", 5);
   }
 
   public void testTonumber() {
     nTrue("testtonumber", 5);
   }
 
   public void testType() {
     nTrue("testtype", 6);
   }
 
   public void testSelect() {
     nTrue("testselect", 2);
   }
 
   public void testUnpack() {
     nTrue("testunpack", 1);
   }
 
   public void testPairs() {
     nTrue("testpairs", 4);
   }
 
   public void testNext() {
     nTrue("testnext", 4);
   }
 
   public void testIpairs() {
     nTrue("testipairs", 4);
   }
 
   public void testRawequal() {
     nTrue("testrawequal", 7);
   }
 
   public void testRawget() {
     nTrue("testrawget", 2);
   }
 
   public void testRawset() {
     nTrue("testrawset", 2);
   }
 
   public void testGetfenv() {
     nTrue("testgetfenv", 1);
   }
 
   public void testSetfenv() {
     nTrue("testsetfenv", 1);
   }
 
   public void testPcall() {
     nTrue("testpcall", 2);
   }
 
   public void testError() {
     nTrue("testerror", 2);
   }
 
   public void testMetatable() {
     nTrue("testmetatable", 2);
   }
 
   public void test__metatable() {
     nTrue("test__metatable", 2);
   }
 
   public void test__tostring() {
     nTrue("test__tostring", 1);
   }
 
   public void testCollectgarbage() {
     nTrue("testcollectgarbage", 1);
   }
 
   public void testAssert() {
     nTrue("testassert", 1);
   }
 
   public void testLoadstring() {
     nTrue("testloadstring", 1);
   }
 
   public void testLoadfile() {
     nTrue("testloadfile", 1);
   }
 
   public void testLoad() {
     nTrue("testload", 1);
   }
 
   public void testDofile() {
     nTrue("testdofile", 1);
   }
 
   /** Tests _VERSION */
   public void testVersion() {
     Lua L = new Lua();
     BaseLib.open(L);
 
     Object o = L.getGlobal("_VERSION");
     assertTrue("_VERSION exists", o != null);
     assertTrue("_VERSION is a string", L.isString(o));
   }
 
   public void testXpcall() {
     nTrue("testxpcall", 1);
   }
 
   public Test suite() {
     TestSuite suite = new TestSuite();
 
     suite.addTest(new BaseLibTest("testBaseLib") {
         public void runTest() { testBaseLib(); } });
     suite.addTest(new BaseLibTest("testPrint") {
         public void runTest() { testPrint(); } });
     suite.addTest(new BaseLibTest("testTostring") {
         public void runTest() { testTostring(); } });
     suite.addTest(new BaseLibTest("testTonumber") {
         public void runTest() { testTonumber(); } });
     suite.addTest(new BaseLibTest("testType") {
         public void runTest() { testType(); } });
     suite.addTest(new BaseLibTest("testSelect") {
         public void runTest() { testSelect(); } });
     suite.addTest(new BaseLibTest("testUnpack") {
         public void runTest() { testUnpack(); } });
     suite.addTest(new BaseLibTest("testPairs") {
         public void runTest() { testPairs(); } });
     suite.addTest(new BaseLibTest("testIpairs") {
         public void runTest() { testIpairs(); } });
     suite.addTest(new BaseLibTest("testRawequal") {
         public void runTest() { testRawequal(); } });
     suite.addTest(new BaseLibTest("testRawget") {
         public void runTest() { testRawget(); } });
     suite.addTest(new BaseLibTest("testRawset") {
         public void runTest() { testRawset(); } });
     suite.addTest(new BaseLibTest("testGetfenv") {
         public void runTest() { testGetfenv(); } });
     suite.addTest(new BaseLibTest("testSetfenv") {
         public void runTest() { testSetfenv(); } });
     suite.addTest(new BaseLibTest("testNext") {
         public void runTest() { testNext(); } });
     suite.addTest(new BaseLibTest("testPcall") {
         public void runTest() { testPcall(); } });
     suite.addTest(new BaseLibTest("testError") {
         public void runTest() { testError(); } });
     suite.addTest(new BaseLibTest("testMetatable") {
         public void runTest() { testMetatable(); } });
     suite.addTest(new BaseLibTest("test__metatable") {
         public void runTest() { test__metatable(); } });
     suite.addTest(new BaseLibTest("test__tostring") {
         public void runTest() { test__tostring(); } });
     suite.addTest(new BaseLibTest("testCollectgarbage") {
         public void runTest() { testCollectgarbage(); } });
     suite.addTest(new BaseLibTest("testAssert") {
         public void runTest() { testAssert(); } });
     suite.addTest(new BaseLibTest("testLoadstring") {
         public void runTest() { testLoadstring(); } });
     suite.addTest(new BaseLibTest("testLoadfile") {
         public void runTest() { testLoadfile(); } });
     suite.addTest(new BaseLibTest("testLoad") {
         public void runTest() { testLoad(); } });
     suite.addTest(new BaseLibTest("testDofile") {
         public void runTest() { testDofile(); } });
     suite.addTest(new BaseLibTest("testVersion") {
         public void runTest() { testVersion(); } });
     suite.addTest(new BaseLibTest("testXpcall") {
         public void runTest() { testXpcall(); } });
     return suite;
   }
 }
