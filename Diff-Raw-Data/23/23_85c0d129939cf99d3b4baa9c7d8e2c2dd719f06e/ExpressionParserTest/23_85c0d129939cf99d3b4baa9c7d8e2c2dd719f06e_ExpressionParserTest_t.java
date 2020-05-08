 /*
  * Copyright 2013 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.parser;
 
 import static org.junit.Assert.*;
 
 import java.util.Calendar;
 
 import org.junit.Test;
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryParseException;
 import org.araqne.logdb.query.expr.Expression;
 
 public class ExpressionParserTest {
 	@Test
 	public void testSimple() {
 		Expression expr = ExpressionParser.parse("3+4*2/(1-5)");
 		Object v = expr.eval(null);
 		assertEquals(1.0, v);
 	}
 
 	@Test
 	public void testFuncExpr() {
 		Expression expr = ExpressionParser.parse("1 + abs(1-5*2)");
 		Object v = expr.eval(null);
 		assertEquals(10L, v);
 	}
 
 	@Test
 	public void testFuncMultiArgs() {
 		LogMap log = new LogMap();
 		log.put("test", 1);
 
 		Expression expr = ExpressionParser.parse("100 + min(3, 7, 2, 5, test) * 2");
 		Object v = expr.eval(log);
 		assertEquals(102L, v);
 	}
 
 	@Test
 	public void testNestedFuncExpr() {
 		Expression expr = ExpressionParser.parse("min(abs(1-9), 3, 10, 5)");
 		Object v = expr.eval(null);
 		assertEquals(3, v);
 	}
 
 	@Test
 	public void testNegation() {
 		Expression expr = ExpressionParser.parse("-abs(1-9) * 2");
 		Object v = expr.eval(null);
 		assertEquals(-16L, v);
 	}
 
 	@Test
 	public void testBrokenExpr() {
 		try {
 			ExpressionParser.parse("3+4*2/");
 			fail();
 		} catch (LogQueryParseException e) {
 			assertEquals("broken-expression", e.getType());
 		}
 	}
 
 	@Test
 	public void testGreaterThanEqual() {
 		Expression exp = ExpressionParser.parse("10 >= 3");
 		assertTrue((Boolean) exp.eval(null));
 
 		exp = ExpressionParser.parse("3 >= 3");
 		assertTrue((Boolean) exp.eval(null));
 	}
 
 	@Test
 	public void testGreaterThan() {
 		Expression exp = ExpressionParser.parse("10 > 3");
 		assertTrue((Boolean) exp.eval(null));
 
 		exp = ExpressionParser.parse("3 > 3");
 		assertFalse((Boolean) exp.eval(null));
 	}
 
 	@Test
 	public void testLesserThanEqual() {
 		Expression exp = ExpressionParser.parse("3 <= 5");
 		assertTrue((Boolean) exp.eval(null));
 
 		exp = ExpressionParser.parse("3 <= 3");
 		assertTrue((Boolean) exp.eval(null));
 	}
 
 	@Test
 	public void testLesserThan() {
 		Expression exp = ExpressionParser.parse("3 < 5");
 		assertTrue((Boolean) exp.eval(null));
 
 		exp = ExpressionParser.parse("3 < 3");
 		assertFalse((Boolean) exp.eval(null));
 	}
 
 	@Test
 	public void testBooleanArithmeticPrecendence() {
 		Expression exp = ExpressionParser.parse("1 == 3-2 or 2 == 2");
 		assertTrue((Boolean) exp.eval(null));
 	}
 
 	@Test
 	public void testEq() {
 		Expression exp = ExpressionParser.parse("1 == 0");
 		assertFalse((Boolean) exp.eval(null));
 	}
 
 	@Test
 	public void testAnd() {
 		Expression exp = ExpressionParser.parse("10 >= 3 and 1 == 0");
 		assertFalse((Boolean) exp.eval(null));
 	}
 
 	@Test
 	public void testAndOr() {
 		Expression exp = ExpressionParser.parse("10 >= 3 and (1 == 0 or 2 == 2)");
 		assertTrue((Boolean) exp.eval(null));
 	}
 
 	@Test
 	public void testIf() {
 		Expression exp = ExpressionParser.parse("if(field >= 10, 10, field)");
 
 		LogMap m1 = new LogMap();
 		m1.put("field", 15);
 		assertEquals(10, exp.eval(m1));
 
 		LogMap m2 = new LogMap();
 		m2.put("field", 3);
 		assertEquals(3, exp.eval(m2));
 	}
 
 	@Test
 	public void testCase() {
 		Expression exp = ExpressionParser.parse("case(field >= 10, 10, field < 10, field)");
 
 		LogMap m1 = new LogMap();
 		m1.put("field", 15);
 		assertEquals(10, exp.eval(m1));
 
 		LogMap m2 = new LogMap();
 		m2.put("field", 3);
 		assertEquals(3, exp.eval(m2));
 	}
 
 	@Test
 	public void testConcat() {
 		Expression exp = ExpressionParser.parse("concat(\"hello\", \"world\")");
 		assertEquals("helloworld", exp.eval(null));
 	}
 
 	@Test
 	public void testToDate() {
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.YEAR, 2013);
 		c.set(Calendar.MONTH, 1);
 		c.set(Calendar.DAY_OF_MONTH, 6);
 		c.set(Calendar.HOUR_OF_DAY, 11);
 		c.set(Calendar.MINUTE, 26);
 		c.set(Calendar.SECOND, 33);
 		c.set(Calendar.MILLISECOND, 0);
 
 		LogMap m = new LogMap();
 		m.put("date", "2013-02-06 11:26:33");
 
 		Expression exp = ExpressionParser.parse("date(date, \"yyyy-MM-dd HH:mm:ss\")");
 		Object v = exp.eval(m);
 		assertEquals(c.getTime(), v);
 	}
 
 	@Test
 	public void testSubstr() {
 		String s = "abcdefg";
 
 		LogMap m = new LogMap();
 		m.put("line", s);
 
 		Expression exp = ExpressionParser.parse("substr(line,0,7)");
 		assertEquals("abcdefg", exp.eval(m));
 
 		exp = ExpressionParser.parse("substr(line,0,0)");
 		assertEquals("", exp.eval(m));
 
 		exp = ExpressionParser.parse("substr(line,8,10)");
 		assertNull(exp.eval(m));
 
 		exp = ExpressionParser.parse("substr(line,3,6)");
 		assertEquals("def", exp.eval(m));
 	}
 
 	@Test
 	public void testMatch() {
 		String s = "210.119.122.32";
 		LogMap m = new LogMap();
 		m.put("line", s);
 
 		Expression exp = ExpressionParser.parse("match(line, \"210.*\")");
 		assertTrue((Boolean) exp.eval(m));
 
 		exp = ExpressionParser.parse("match(line, \"192.*\")");
 		assertFalse((Boolean) exp.eval(m));
 	}
 
 	@Test
 	public void testWildcard() {
 		Expression exp = ExpressionParser.parse("\"210.119.122.32\" == \"210*\"");
 		assertTrue((Boolean) exp.eval(null));
 
 		exp = ExpressionParser.parse("\"210.119.122.32\" == \"*32\"");
 		assertTrue((Boolean) exp.eval(null));
 
 		exp = ExpressionParser.parse("\"210.119.122.32\" == \"119*\"");
 		assertFalse((Boolean) exp.eval(null));
 
 		exp = ExpressionParser.parse("\"210.119.122.32\" == \"119\"");
 		assertFalse((Boolean) exp.eval(null));
 	}
 
 	@Test
 	public void testBooleanConstants() {
 		Expression exp1 = ExpressionParser.parse("field == true");
 		LogMap m = new LogMap();
 		m.put("field", true);
 		assertTrue((Boolean) exp1.eval(m));
 
 		m = new LogMap();
 		m.put("field", false);
 		assertFalse((Boolean) exp1.eval(m));
 
 		Expression exp2 = ExpressionParser.parse("field == false");
 		m = new LogMap();
 		m.put("field", false);
 		assertTrue((Boolean) exp2.eval(m));
 	}
 
 	@Test
 	public void testInIntegers() {
 		Expression expr = ExpressionParser.parse("in(field, 1, 2, 3)");
 
 		LogMap m = new LogMap();
 		m.put("field", 1);
 		assertTrue((Boolean) expr.eval(m));
 
 		m.put("field", 2);
 		assertTrue((Boolean) expr.eval(m));
 
 		m.put("field", 3);
 		assertTrue((Boolean) expr.eval(m));
 
 		m.put("field", 4);
 		assertFalse((Boolean) expr.eval(m));
 
 		m.put("field", null);
 		assertFalse((Boolean) expr.eval(m));
 	}
 
 	@Test
 	public void testInStrings() {
 		Expression expr = ExpressionParser.parse("in(field, \"a\", \"b\", \"c\")");
 
 		LogMap m = new LogMap();
 		m.put("field", "a");
 		assertTrue((Boolean) expr.eval(m));
 
 		m.put("field", "b");
 		assertTrue((Boolean) expr.eval(m));
 
 		m.put("field", "c");
 		assertTrue((Boolean) expr.eval(m));
 
 		m.put("field", "d");
 		assertFalse((Boolean) expr.eval(m));
 	}
 
 	@Test
 	public void testInStringWildcards() {
 		Expression expr = ExpressionParser.parse("in(field, \"*74.86.*\")");
 
 		LogMap m = new LogMap();
 		m.put("field", "ip = 74.86.1.2");
 		assertTrue((Boolean) expr.eval(m));
 
 		m.put("field", "ip = 75.81.1.2");
 		assertFalse((Boolean) expr.eval(m));
 	}
 
	@Test
	public void testBracket() {
		{
			Expression expr = ExpressionParser.parse("a == \"*[GameStart REP]*\"");
			LogMap m = new LogMap();
			m.put("a", "22:27:05.235(tid=4436)[Q=0:1:0:0]I[10.1.119.86-997014784-8439] [0 ms][GameStart REP]=126:200:3111 0073875:61.111.10.21:59930:2:1:0:101:qa161새롱 1:2:2718376:3:2000015:0");
			assertTrue((Boolean) expr.eval(m));
		}
		{
			Expression expr = ExpressionParser.parse("a == \"*[GameStart REP]*\"");
			LogMap m = new LogMap();
			m.put("a", "22:27:05.235(tid=4436)[Q=0:1:0:0]I[10.1.119.86-997014784-8439] [0 ms][GameStrt REP]=126:200:3111 0073875:61.111.10.21:59930:2:1:0:101:qa161새롱 1:2:2718376:3:2000015:0");
			assertFalse((Boolean) expr.eval(m));
		}
	}

 }
