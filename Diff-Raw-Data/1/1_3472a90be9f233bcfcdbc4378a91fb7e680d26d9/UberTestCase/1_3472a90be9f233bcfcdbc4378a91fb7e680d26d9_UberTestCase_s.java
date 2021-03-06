 /**
  * to prevent a JVM startup-shutdown time per test, it should be more efficient to
  * collect the tests together into a suite.
  *
  * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
  * @version $Revision$
  */
 import junit.framework.*;
 public class UberTestCase extends TestCase {
     public static Test suite() {
         TestSuite suite = new TestSuite();
         suite.addTestSuite(AbstractClassAndInterfaceTest.class);
         suite.addTestSuite(AmbiguousInvocationTest.class);
         suite.addTestSuite(ArrayAutoboxingTest.class);        
         suite.addTestSuite(ArrayParamMethodTest.class);
         suite.addTestSuite(ArrayTest.class);
         suite.addTestSuite(AssertNumberTest.class);
         suite.addTestSuite(AssertTest.class);
         suite.addTestSuite(AssignmentInsideExpressionBug.class);
         suite.addTestSuite(AutoboxingOfComparisonsBug.class);
         suite.addTestSuite(BadScriptNameBug.class);
         suite.addTestSuite(Base64Test.class);
         suite.addTestSuite(BenchmarkBug.class);
         suite.addTestSuite(BigDecimalOperationTest.class);
         suite.addTestSuite(BindingTest.class);
         suite.addTestSuite(BitwiseOperationsTest.class);
         suite.addTestSuite(BlockAsClosureBug.class);
         suite.addTestSuite(BooleanBug.class);
         suite.addTestSuite(BooleanOperationTest.class);
         suite.addTestSuite(BreakContinueLabelTest.class); // todo: resolve its todo's
         // todo: add BreakContinueLabelWithClosureTest
         suite.addTestSuite(ByteIndexBug.class);
         suite.addTestSuite(Bytecode2Bug.class);
         suite.addTestSuite(Bytecode3Bug.class);
         suite.addTestSuite(Bytecode4Bug.class);
         suite.addTestSuite(Bytecode5Bug.class);
         suite.addTestSuite(Bytecode6Bug.class);
         suite.addTestSuite(Bytecode7Bug.class);
         suite.addTestSuite(BytecodeBug.class);
         suite.addTestSuite(CallInnerClassCtorTest.class);
         suite.addTestSuite(CallingClosuresWithClosuresBug.class);
         suite.addTestSuite(CastTest.class);
         suite.addTestSuite(CastWhenUsingClosuresBug.class);
         suite.addTestSuite(CategoryTest.class);
         suite.addTestSuite(ChainedAssignment.class);
         suite.addTestSuite(ChristofsPropertyBug.class);
         suite.addTestSuite(ClassExpressionTest.class);
         suite.addTestSuite(ClassInNamedParamsBug.class);
         suite.addTestSuite(ClassLoaderBug.class);
         suite.addTestSuite(ClassTest.class);
         suite.addTestSuite(ClosureAsParamTest.class);
         suite.addTestSuite(ClosureClassLoaderBug.class);
         suite.addTestSuite(ClosureCloneTest.class);
         suite.addTestSuite(ClosureComparatorTest.class);
         suite.addTestSuite(ClosureCurryTest.class);
         suite.addTestSuite(ClosureInClosureBug.class);
         suite.addTestSuite(ClosureInClosureTest.class);
 
         suite.addTestSuite(ClosureInStaticMethodTest.class);
 
         suite.addTestSuite(ClosureMethodCallTest.class);
         suite.addTestSuite(ClosureMethodTest.class);
         suite.addTestSuite(ClosureParameterPassingBug.class);
         suite.addTestSuite(ClosureReturnTest.class);
         suite.addTestSuite(ClosureReturnWithoutReturnStatementTest.class);
         suite.addTestSuite(ClosureSugarTest.class);
 
         suite.addTestSuite(ClosureTest.class);
         suite.addTestSuite(ClosureTypedVariableBug.class);
         suite.addTestSuite(ClosureUsingOuterVariablesTest.class);
         suite.addTestSuite(ClosureVariableBug.class);
         suite.addTestSuite(ClosureWithDefaultParamTest.class);
         suite.addTestSuite(groovy.CollectionTest.class);
         suite.addTestSuite(CompareToTest.class);
         suite.addTestSuite(CompilerErrorTest.class);
         suite.addTestSuite(ConstructorBug.class);
         suite.addTestSuite(CurlyBracketLayoutTest.class);
         suite.addTestSuite(DateTest.class);
         suite.addTestSuite(DefVariableBug.class);
         suite.addTestSuite(DefaultParamClosureTest.class);
         suite.addTestSuite(DefaultParamTest.class);
         suite.addTestSuite(DollarEscapingTest.class);
         suite.addTestSuite(DoubleOperationTest.class);
         suite.addTestSuite(DownUpStepTest.class);
 
         suite.addTestSuite(EscapedUnicodeTest.class);
         suite.addTestSuite(ExceptionInClosureTest.class);
 
         String osName = System.getProperty ( "os.name" ) ;
         if ( osName.equals ( "Linux" ) || osName.equals ( "SunOS" ) ) {
           suite.addTestSuite ( ExecuteTest_LinuxSolaris.class ) ;
         }
         else {
           System.err.println ( "XXXXXX  No execute testsfor this OS.  XXXXXX" ) ;
         }
 
         suite.addTestSuite(ExpandoPropertyTest.class);
         suite.addTestSuite(FilterLineTest.class);
 
         suite.addTestSuite(ForAndSqlBug.class);
         suite.addTestSuite(ForLoopBug.class);
         suite.addTestSuite(ForLoopTest.class);
         suite.addTestSuite(ForLoopWithLocalVariablesTest.class);
         suite.addTestSuite(FullyQualifiedClassBug.class);
         suite.addTestSuite(FullyQualifiedMethodReturnTypeBug.class);
         suite.addTestSuite(FullyQualifiedVariableTypeBug.class);
         suite.addTestSuite(GPathTest.class);
         suite.addTestSuite(GStringTest.class);
 
         suite.addTestSuite(GeneratorTest.class);
         suite.addTestSuite(GetterBug.class);
         suite.addTestSuite(GlobalPrintlnTest.class);
         suite.addTestSuite(Groovy239_Bug.class);
         suite.addTestSuite(Groovy249_Bug.class);
         suite.addTestSuite(Groovy252_Bug.class);
         suite.addTestSuite(Groovy389_Bug.class);
         suite.addTestSuite(Groovy513_Bug.class);
         suite.addTestSuite(groovy.bugs.Groovy662.class);
         suite.addTestSuite(GroovyInterceptableTest.class);
         suite.addTestSuite(GroovyClosureMethodsTest.class);
         suite.addTestSuite(GroovyMethodsTest.class);
         suite.addTestSuite(GuillaumesBug.class);
         suite.addTestSuite(GuillaumesMapBug.class);
         suite.addTestSuite(HeredocsTest.class);
         suite.addTestSuite(HomepageTest.class);
         suite.addTestSuite(IdentityClosureTest.class);
         suite.addTestSuite(IfElseCompactTest.class);
         suite.addTestSuite(IfElseTest.class);
         suite.addTestSuite(IfPropertyTest.class);
         suite.addTestSuite(IfTest.class);
         suite.addTestSuite(IfWithMethodCallTest.class);
         suite.addTestSuite(ImmutableModificationTest.class);
         suite.addTestSuite(ImportTest.class);
         suite.addTestSuite(InconsistentStackHeightBug.class);
         suite.addTestSuite(InstanceofTest.class);
         suite.addTestSuite(InvokeNormalMethodFromBuilder_Bug657.class);
         suite.addTestSuite(InvokeNormalMethodsFirstTest.class);
         suite.addTestSuite(IntegerOperationTest.class);
         suite.addTestSuite(IterateOverCustomTypeBug.class);
         suite.addTestSuite(ListIteratingTest.class);
         suite.addTestSuite(ListTest.class);
         suite.addTestSuite(LiteralTypesTest.class);
         suite.addTestSuite(LittleClosureTest.class);
         suite.addTestSuite(LocalFieldTest.class);
         suite.addTestSuite(LocalPropertyTest.class);
         suite.addTestSuite(LocalVariableTest.class);
         suite.addTestSuite(LogTest.class);
         suite.addTestSuite(LogicTest.class);
 
         suite.addTestSuite(LoopBreakTest.class);
         suite.addTestSuite(MapConstructionTest.class);
         suite.addTestSuite(MapPropertyTest.class);
         suite.addTestSuite(MapTest.class);
         suite.addTestSuite(MarkupAndMethodBug.class);
         suite.addTestSuite(MethodCallTest.class);
         suite.addTestSuite(MethodCallWithoutParensInStaticMethodBug.class);
         suite.addTestSuite(MethodCallWithoutParenthesisTest.class);
         suite.addTestSuite(MethodDispatchBug.class);
         suite.addTestSuite(MethodParameterAccessWithinClosureTest.class);
         suite.addTestSuite(MinMaxTest.class);
         suite.addTestSuite(MinusEqualsTest.class);
         suite.addTestSuite(ModuloTest.class);
         suite.addTestSuite(MorgansBug.class);
         suite.addTestSuite(groovy.MultiDimArraysTest.class); // adapt as soon as Multi Dim handling changes
         suite.addTestSuite(MultilineChainExpressionTest.class);
         suite.addTestSuite(MultilineStringTest.class);
         suite.addTestSuite(MultiplyDivideEqualsTest.class);
         suite.addTestSuite(NamedParameterTest.class);
         suite.addTestSuite(NavigationTest.class);
         suite.addTestSuite(NegateListsTest.class);
         suite.addTestSuite(NegationTests.class);
         suite.addTestSuite(NestedClosure2Bug.class);
         suite.addTestSuite(NestedClosureBug.class);
         suite.addTestSuite(NestedClosureBugTest.class);
         suite.addTestSuite(NewExpressionTest.class);
         suite.addTestSuite(NoPackageTest.class);
         suite.addTestSuite(NodeGPathTest.class);
         suite.addTestSuite(NullCompareBug.class);
         suite.addTestSuite(NullPropertyTest.class);
         suite.addTestSuite(NumberMathTest.class);
         suite.addTestSuite(NumberTest.class);
         suite.addTestSuite(OptionalReturnTest.class);
         suite.addTestSuite(OrderByTest.class);
         suite.addTestSuite(OverloadInvokeMethodBug.class);
         suite.addTestSuite(OverloadInvokeMethodTest.class);
         suite.addTestSuite(OverridePropertyGetterTest.class);
         suite.addTestSuite(PlusEqualsTest.class);
         suite.addTestSuite(PostfixTest.class);
         suite.addTestSuite(PowerOperationTest.class);
         suite.addTestSuite(PrefixTest.class);
 
         suite.addTestSuite(PrimitiveArraysTest.class);
         suite.addTestSuite(PrimitiveDefaultValueTest.class);
         suite.addTestSuite(PrimitiveTypeFieldTest.class);
         suite.addTestSuite(PrimitiveTypesTest.class);
 
         suite.addTestSuite(PrintTest.class);
         suite.addTestSuite(PrintlnWithNewBug.class);
         suite.addTestSuite(PrivateVariableAccessFromAnotherInstanceTest.class);
         suite.addTestSuite(ProcessTest.class);
         suite.addTestSuite(PropertyBug.class);
         suite.addTestSuite(PropertyTest.class);
         suite.addTestSuite(PropertyTest2.class);
         suite.addTestSuite(PropertyWithoutDotTest.class);
         suite.addTestSuite(RangeTest.class);
         suite.addTestSuite(ReadLineTest.class);
         suite.addTestSuite(RegExpGroupMatchTest.class);
         suite.addTestSuite(RegularExpressionsTest.class);
         suite.addTestSuite(ReturnTest.class);
         suite.addTestSuite(RodsBooleanBug.class);
         suite.addTestSuite(RodsBug.class);
 
         suite.addTestSuite(SafeNavigationTest.class);
         suite.addTestSuite(SpreadListOperatorTest.class);
         suite.addTestSuite(SpreadMapOperatorTest.class);
         suite.addTestSuite(SerializeTest.class);
         suite.addTestSuite(ShellTest.class);
         suite.addTestSuite(SingletonBugTest.class);
         suite.addTestSuite(SmallTreeTest.class);
         suite.addTestSuite(SocketTest.class);
         suite.addTestSuite(SortTest.class);
         suite.addTestSuite(SpreadDotTest.class);
         suite.addTestSuite(StaticClosurePropertyBug.class);
         suite.addTestSuite(StaticMarkupBug.class);
         suite.addTestSuite(StaticPrintlnTest.class);
         suite.addTestSuite(StaticThisTest.class);
         suite.addTestSuite(StringBufferTest.class);
         suite.addTestSuite(StringOperationTest.class);
         suite.addTestSuite(StringTest.class);
         suite.addTestSuite(SubscriptAndExpressionBug.class);
         suite.addTestSuite(SubscriptTest.class);
         suite.addTestSuite(SuperMethod2Bug.class);
         suite.addTestSuite(SuperMethodBug.class);
         suite.addTestSuite(SwitchTest.class);
         suite.addTestSuite(SwitchWithDifferentTypesTest.class);
         suite.addTestSuite(TedsClosureBug.class);
         suite.addTestSuite(TernaryOperatorTest.class);
         suite.addTestSuite(TextPropertyTest.class);
         suite.addTestSuite(ThrowTest.class);
         suite.addTestSuite(ToArrayBug.class);
         suite.addTestSuite(ToStringBug.class);
         suite.addTestSuite(TreeTest.class);
         suite.addTestSuite(TripleQuotedStringTest.class);
         suite.addTestSuite(TryCatch2Bug.class);
         suite.addTestSuite(TryCatchBug.class);
         suite.addTestSuite(TryCatchTest.class);
         suite.addTestSuite(TypesafeMethodTest.class);
         suite.addTestSuite(UnaryMinusTest.class);
         suite.addTestSuite(UnknownVariableBug.class);
         suite.addTestSuite(UnsafeNavigationTest.class);
         suite.addTestSuite(UseClosureInClosureBug.class);
         suite.addTestSuite(UseStaticInClosureBug.class);
         suite.addTestSuite(VarargsMethodTest.class);
         suite.addTestSuite(VariableScopingBug.class);
         suite.addTestSuite(VariblePrecedence.class);
         suite.addTestSuite(VerbatimGStringTest.class);
         suite.addTestSuite(VerboseTreeTest.class);
         suite.addTestSuite(WhileLoopTest.class);
         suite.addTestSuite(ZoharsBug.class);
         return suite;
     }
 
 //  The following classes appear in target/test-classes but do not extend junit.framework.TestCase
 //
 //        suite.addTestSuite(AnotherMockInputStream.class);
 //        suite.addTestSuite(Bean.class);
 //        suite.addTestSuite(Bean249.class);
 //        suite.addTestSuite(BooleanBean.class);
 //        suite.addTestSuite(CallAnotherScript.class);
 //        suite.addTestSuite(ClassWithScript.class);
 //        suite.addTestSuite(ComparableFoo.class);
 //        suite.addTestSuite(CreateData.class);
 //        suite.addTestSuite(Entry.class);
 //        suite.addTestSuite(EvalInScript.class);
 //        suite.addTestSuite(Feed.class);
 //        suite.addTestSuite(Foo.class);
 //        suite.addTestSuite(HelloWorld.class);
 //        suite.addTestSuite(HelloWorld2.class);
 //        suite.addTestSuite(Html2Wiki.class);
 //        suite.addTestSuite(IntegerCategory.class);
 //        suite.addTestSuite(Loop.class);
 //        suite.addTestSuite(Loop2.class);
 //        suite.addTestSuite(MapFromList.class);
 //        suite.addTestSuite(MarkupTestScript.class);
 //        suite.addTestSuite(MethodTestScript.class);
 //        suite.addTestSuite(MockInputStream.class);
 //        suite.addTestSuite(MockProcess.class);
 //        suite.addTestSuite(MockSocket.class);
 //        suite.addTestSuite(OverloadA.class);
 //        suite.addTestSuite(OverloadB.class);
 //        suite.addTestSuite(NavToWiki.class);
 //        suite.addTestSuite(Person.class);
 //        suite.addTestSuite(SampleMain.class);
 //        suite.addTestSuite(ScriptWithFunctions.class);
 //        suite.addTestSuite(ShowArgs.class);
 //        suite.addTestSuite(StringCategory.class);
 //        suite.addTestSuite(SuperBase.class);
 //        suite.addTestSuite(SuperDerived.class);
 //        suite.addTestSuite(TestBase.class);
 //        suite.addTestSuite(TestCaseBug.class);
 //        suite.addTestSuite(TestDerived.class);
 //        suite.addTestSuite(TinyAgent.class);
 //        suite.addTestSuite(UnitTestAsScript.class);
 //        suite.addTestSuite(UseClosureInScript.class);
 //        suite.addTestSuite(X.class);
 //        suite.addTestSuite(createLoop.class);
 }
