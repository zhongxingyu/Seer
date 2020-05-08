 package hash.basetests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import hash.lang.Context;
 import hash.lang.Function;
 import hash.runtime.Factory;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public abstract class StatementTest {
 
 	protected Context context;
 
 	protected abstract Object evaluate(String expression);
 
 	@Before
 	public void setup() {
 		context = Factory.createContext();
 	}
 
 	@Test
 	public void functionStatement() {
 		evaluate("function\n f1\n  (\nn){return n}");
 		assertEquals("name", evaluate("f1('name')"));
 		assertEquals(2147483648l, evaluate("f1(1<<31)"));
 	}
 
 	@Test
 	public void importJavaClasses() {
 		evaluate("import java.lang.Integer; import java.lang.StringBuilder");
 		assertEquals(10, evaluate("new Integer('10')"));
 		evaluate("sb = new StringBuilder('Testing')");
 		evaluate("sb.append('\\nTesting2')");
 		assertEquals("Testing\nTesting2", evaluate("sb.toString()"));
 	}
 
 	@Test
 	public void classStatement() {
 		evaluate("class \n\nAccount {balance:0,add:(amount){this.balance+=1.0*amount},"
 				+ "remove:(amount){this.balance-=amount}}");
 		evaluate("class BonusAccount extends Account{balance:1,"
 				+ "add:(amount){this.balance+=1.05*amount}}");
 		evaluate("acc = new Account();acc.add(100)");
 		evaluate("bonusAcc = new BonusAccount();bonusAcc.add(100)");
 		assertEquals(100f, evaluate("acc.balance"));
 		assertEquals(106f, evaluate("bonusAcc.balance"));
 	}
 
 	@Test
 	public void tryCatchFinally() {
 		String message = "TryCatchFinally";
 		final RuntimeException ex = new RuntimeException(message);
 		context.put("throwingAction", new Function() {
 			public Object invoke(Object... args) {
 				throw ex;
 			}
 		});
 		evaluate("try {\n  throwingAction()\n} catch(ex) {\n  x = ex\n"
 				+ "y=ex.getMessage()} finally {\n  z = 'abc'\n}");
 		assertTrue(ex == context.get("x"));
 		assertEquals(message, context.get("y"));
 		assertEquals("abc", context.get("z"));
 	}
 
 	@Test
 	public void tryFinally() {
 		String message = "TryCatchFinally";
 		final RuntimeException ex = new RuntimeException(message);
 		context.put("throwingAction", new Function() {
 			public Object invoke(Object... args) {
 				throw ex;
 			}
 		});
 		try {
 			evaluate("try {throwingAction()}  \n\nfinally \n"
 					+ "{\ny = 'finally'\n}");
 			fail("Should have thrown");
 		} catch (Exception e) {
 			assertEquals("finally", context.get("y"));
 		}
 	}
 
 	@Test
 	public void tryCatchRightExceptionType() {
 		String message = "TryCatchFinally";
 		final UnsupportedOperationException ex = new UnsupportedOperationException(
 				message);
 		context.put("throwingAction", new Function() {
 			public Object invoke(Object... args) {
 				throw ex;
 			}
 		});
 		evaluate("import java.lang.UnsupportedOperationException");
 
 		evaluate("try {throwingAction()} catch(UnsupportedOperationException e){a=1}"
 				+ "catch(e){b=2}");
 		assertEquals(1, context.get("a"));
 		assertFalse(context.containsKey("b"));
 	}
 
 	@Test
 	public void tryCatchWrongExceptionType() {
 		String message = "TryCatchFinally";
 		final UnsupportedOperationException ex = new UnsupportedOperationException(
 				message);
 		context.put("throwingAction", new Function() {
 			public Object invoke(Object... args) {
 				throw ex;
 			}
 		});
 		evaluate("import java.lang.IllegalArgumentException");
 		evaluate("try {throwingAction()} catch(IllegalArgumentException e){a=1}"
 				+ "catch(e){b=2}");
 		assertFalse(context.containsKey("a"));
 		assertEquals(2, context.get("b"));
 	}
 
 	@Test
 	public void throwing() {
 		try {
 			evaluate("throw 'SomeString'");
 			fail("should have thrown");
 		} catch (RuntimeException e) {
 			assertEquals("SomeString", e.getMessage());
 		}
 	}
 
 	@Test
 	public void elseif() {
 		evaluate("f=(x){if(x>10)return x else if(x>5) return x+5 else return x+10}");
 		assertEquals(11, evaluate("f(11)"));
 		assertEquals(15, evaluate("f(10)"));
 		assertEquals(14, evaluate("f(9)"));
 		assertEquals(15, evaluate("f(5)"));
 	}
 
 	@Test
 	public void elseifBlock() {
 		evaluate("f=(x){if(x>10){return x}\n else if(x>5) {return x+5}\n"
 				+ " else {return x+10}}");
 		assertEquals(11, evaluate("f(11)"));
 		assertEquals(15, evaluate("f(10)"));
 		assertEquals(14, evaluate("f(9)"));
 		assertEquals(15, evaluate("f(5)"));
 	}
 
 	@Test
 	public void ifBlock() {
 		evaluate("f=(n){if(n==1){return 1} return n*f(n-1);}");
 		assertEquals(120, evaluate("f(5)"));
 		assertEquals(720, evaluate("f(6)"));
 	}
 
 	@Test
 	public void forStmt() {
 		evaluate("z=0 for (i=0;i<10;i++) z+=i");
 		assertEquals(45, context.get("z"));
 	}
 
 	@Test
 	public void nestedFor() {
 		evaluate("z=0 for(i=0;i<10;i++)for(j=0;j<10;j++)for(k=0;k<10;k++)"
 				+ "z+=i");
 		assertEquals(4500, context.get("z"));
 	}
 
 	@Test
 	public void whileStmt() {
 		evaluate("i=0 while (i<100) i++");
 		assertEquals(100, context.get("i"));
 	}
 
 	@Test
 	public void whileBlock() {
 		evaluate("i=100 while (i<100){y=i; y*=2}");
 		assertEquals(100, context.get("i"));
 		assertFalse(context.containsKey("y"));
 	}
 
 	@Test
 	public void nestedWhiles() {
 		evaluate("n=i=0 while (i<10){j=0 while(j<10)"
 				+ " {k=0 while(k<10){k++;n++}j++}i++}");
 		assertEquals(1000, context.get("n"));
 	}
 
 	@Test
 	public void doWhileStmt() {
 		evaluate("i=0; do \ni++ while (i<100)");
 		assertEquals(100, context.get("i"));
 	}
 
 	@Test
 	public void doWhileBlock() {
 		evaluate("i=100\n do {\ny=i;\n y*=2}\n\n while (i<100)");
 		assertEquals(100, context.get("i"));
 		assertEquals(200, context.get("y"));
 	}
 
 	@Test
 	public void foreachStmt() {
 		evaluate("s=0 for (i in [1,2,3,4]) s+=i");
 		assertEquals(10, context.get("s"));
 	}
 
 	@Test
 	public void nestedLoops() {
 		evaluate("i=0 do while(i<10000) do for(j=0;j<10001;j++){i++} while(i<10000)"
 				+ "  while (i<10000)");
 		assertEquals(10001, context.get("i"));
 	}
 
 	@Test
 	public void loopBreak() {
 		evaluate("i=0 while(i<10) if(i>5)break else i++");
 		assertEquals(6, context.get("i"));
 		evaluate("i=0 do if(i==0)break else i++ while(i<10)");
 		assertEquals(0, context.get("i"));
 		evaluate("l=[1,2,3,4];l2=[]for (i in l) if(i==3)break else l2.add(i)");
 		assertEquals("[1, 2]", context.get("l2").toString());
 		evaluate("for(i=10;i>0;i--) if(i==5)break");
 		assertEquals(5, context.get("i"));
 	}
 
 	@Test
 	public void loopContinue() {
 		evaluate("l=[];i=0 while(i<10) {i++ if(i%2==0)continue; l.add(i)}");
 		assertEquals("[1, 3, 5, 7, 9]", context.get("l").toString());
 		evaluate("j=i=0 do {i++ if(i%3!=0)continue;j++}while(i<10)");
 		assertEquals(3, context.get("j"));
 		evaluate("l=[1,2,3,4];l2=[]for (i in l){ if(i<3)continue; l2.add(i)}");
 		assertEquals("[3, 4]", context.get("l2").toString());
 		evaluate("for(i=0;i<=10;i++) {if(i<10)continue;i+=10}");
 		assertEquals(21, context.get("i"));		
 	}
 }
