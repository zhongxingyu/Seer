 package org.arz.miniscript.tests;
 
 import static org.junit.Assert.*;
 import junit.framework.Assert;
 
 import org.arz.MiniScriptInjectorProvider;
 import org.arz.interpreter.Closure;
 import org.arz.interpreter.MiniScriptEvaluator;
 import org.arz.interpreter.MiniScriptStatementExecutor;
 import org.arz.interpreter.MyEnvironment;
 import org.arz.miniScript.Model;
 import org.arz.miniScript.Program;
 import org.arz.miniScript.SymbolReference;
 import org.eclipse.xtext.junit.util.ParseHelper;
 import org.eclipse.xtext.junit4.InjectWith;
 import org.eclipse.xtext.junit4.XtextRunner;
 import org.eclipse.xtext.scoping.IScopeProvider;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.inject.Inject;
 
 import de.itemis.interpreter.ExecutionContext;
 import de.itemis.interpreter.InterpreterException;
 import de.itemis.interpreter.MessageList;
 import de.itemis.interpreter.logging.LogEntry;
 import de.itemis.xtext.typesystem.ITypesystem;
 
 @RunWith(XtextRunner.class)
 @InjectWith(MiniScriptInjectorProvider.class)
 public class InterpreterTests {
 
 	@Inject
 	private ParseHelper<Program> parserHelper;
 	@Inject
 	private IScopeProvider scopeProvider;
 
 	private MyEnvironment runModel(Model m, ITypesystem ts)
 			throws InterpreterException {
 		ExecutionContext ctx = new ExecutionContext(ts);
 		LogEntry log = LogEntry.root("running model " + m.toString());
 		LogEntry.setMostRecentRoot(log);
 		new MiniScriptEvaluator(ctx);
 		new MiniScriptStatementExecutor(ctx).execute(m, log);
 		return (MyEnvironment) ctx.environment.get("myEnv");
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testBasicAssignment() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("a=1;");
 		Program p;
 		try {
 			p = parserHelper.parse(builder.toString());
 			MyEnvironment m = runModel(p, null);
 			Object result = m.get("a");
 			Assert.assertNotNull(result);
 			Assert.assertTrue(result instanceof Integer);
 			Integer intResult = (Integer) result;
 			Assert.assertEquals(1, intResult.intValue());
 		} catch (Exception e) {
 			Assert.fail();
 		}
 	}
 	
 	@Test
 	public void testAddition() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("a=1+1;");
 		Program p;
 		try {
 			p = parserHelper.parse(builder.toString());
 			MyEnvironment m = runModel(p, null);
 			Object result = m.get("a");
 			Assert.assertNotNull(result);
 			Assert.assertTrue(result instanceof Integer);
 			Integer intResult = (Integer) result;
 			Assert.assertEquals(2, intResult.intValue());
 		} catch (Exception e) {
 			Assert.fail();
 		}
 	}
 	@Test
 	public void testSubtraction() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("a=1-1;");
 		Program p;
 		try {
 			p = parserHelper.parse(builder.toString());
 			MyEnvironment m = runModel(p, null);
 			Object result = m.get("a");
 			Assert.assertNotNull(result);
 			Assert.assertTrue(result instanceof Integer);
 			Integer intResult = (Integer) result;
 			Assert.assertEquals(0, intResult.intValue());
 		} catch (Exception e) {
 			Assert.fail();
 		}
 	}
 	@Test
 	public void testMultiplication() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("a=3*5;");
 		Program p;
 		try {
 			p = parserHelper.parse(builder.toString());
 			MyEnvironment m = runModel(p, null);
 			Object result = m.get("a");
 			Assert.assertNotNull(result);
 			Assert.assertTrue(result instanceof Integer);
 			Integer intResult = (Integer) result;
 			Assert.assertEquals(15, intResult.intValue());
 		} catch (Exception e) {
 			Assert.fail();
 		}
 	}
 
 	@Test
 	public void testDivision() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("a=15 / 3;");
 		Program p;
 		try {
 			p = parserHelper.parse(builder.toString());
 			MyEnvironment m = runModel(p, null);
 			Object result = m.get("a");
 			Assert.assertNotNull(result);
 			Assert.assertTrue(result instanceof Integer);
 			Integer intResult = (Integer) result;
 			Assert.assertEquals(5, intResult.intValue());
 		} catch (Exception e) {
 			Assert.fail();
 		}
 	}
 
 	
 	@Test
 	public void testFunctionAssignment() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("a=func() 1;");
 		Program p;
 		try {
 			p = parserHelper.parse(builder.toString());
 			MyEnvironment m = runModel(p, null);
 			Object result = m.get("a");
 			Assert.assertNotNull(result);
 			Assert.assertTrue(result instanceof Closure);
 		} catch (Exception e) {
 			Assert.fail();
 		}
 	}
 	
 	@Test
 	public void testInlineApply() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("a=(func(x) x + 1)(5);");
 		Program p;
 		try {
 			p = parserHelper.parse(builder.toString());
 			MyEnvironment m = runModel(p, null);
 			Object result = m.get("a");
 			Assert.assertNotNull(result);
			Assert.assertNotNull(result);
 			Assert.assertTrue(result instanceof Integer);
 			Integer intResult = (Integer) result;
 			Assert.assertEquals(6, intResult.intValue());
 		} catch (Exception e) {
 			Assert.fail();
 		}
 	}
 	
 	@Test
 	public void testClosureApply() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("foo=func(x){func(y){y*x;};}; goo=foo(2); a=goo(2)+goo(3);");
 		Program p;
 		try {
 			p = parserHelper.parse(builder.toString());
 			MyEnvironment m = runModel(p, null);
 			Object result = m.get("a");
 			Assert.assertNotNull(result);
			Assert.assertNotNull(result);
 			Assert.assertTrue(result instanceof Integer);
 			Integer intResult = (Integer) result;
 			Assert.assertEquals(10, intResult.intValue());
 		} catch (Exception e) {
 			Assert.fail();
 		}
 	}
 	
 
 }
