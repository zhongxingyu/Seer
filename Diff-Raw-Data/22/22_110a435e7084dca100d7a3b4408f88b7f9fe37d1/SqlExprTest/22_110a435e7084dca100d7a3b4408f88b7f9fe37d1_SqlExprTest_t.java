 package com.tonbeller.wcf.param;
 
 import java.math.BigDecimal;
 
 import junit.framework.TestCase;
 
 public class SqlExprTest extends TestCase {
   SqlGenerator sg;
   
   public void testSqlAndExpr() {
     SqlAndExpr and = new SqlAndExpr();
     eqlExprWithOperandsTest(and, "(C1 = 'c1' AND C2 = 'c2')");
   }
 
   public void testSqlOrExpr() {
     SqlOrExpr and = new SqlOrExpr();
     eqlExprWithOperandsTest(and, "(C1 = 'c1' OR C2 = 'c2')");
   }
   
   public void testAndOrExpr() {
     SqlEqualExpr ex = new SqlEqualExpr();
     ex.setSqlValue(new Integer(1));
     SqlAndExpr and = new SqlAndExpr();
     and.getOperands().add(ex);
     SqlOrExpr or = new SqlOrExpr();
     or.getOperands().add(ex);
     or.getOperands().add(ex);
     and.getOperands().add(or);
     assertEquals("(C = 1 AND (C = 1 OR C = 1))", sg.generate(and));
   }
   
   public void testSqlEqualExpr() {
     SqlEqualExpr ex = new SqlEqualExpr();
     ex.setSqlValue("ab");
     assertEquals("C = 'ab'", sg.generate(ex));
 
     ex.setSqlValue(null);
     assertEquals("C IS NULL", sg.generate(ex));
   }
 
   public void testSqlInExpr() {
     SqlInExpr ex = new SqlInExpr();
     
     // empty value
     ex.setSqlValues(new Object[]{});
     assertEquals("1=0", sg.generate(ex));
     
     // 1 value
     ex.setSqlValues(new Object[]{new Integer(1)});
     assertEquals("C = 1", sg.generate(ex));
     ex.setSqlValues(new Object[]{null});
     assertEquals("C IS NULL", sg.generate(ex));
     
     // 2 values
     ex.setSqlValues(new String[]{"a","b"});
     assertEquals("C IN ('a', 'b')", sg.generate(ex));
     ex.setSqlValues(new String[]{"a", null});
     assertEquals("(C = 'a' OR C IS NULL)", sg.generate(ex));
     ex.setSqlValues(new String[]{null, "a"});
     assertEquals("(C = 'a' OR C IS NULL)", sg.generate(ex));
 
     // 3 or more values
     ex.setSqlValues(new String[]{"a","b","c"});
     assertEquals("C IN ('a', 'b', 'c')", sg.generate(ex));
     ex.setSqlValues(new String[]{"a", null, "b"});
     assertEquals("(C IN ('a', 'b') OR C IS NULL)", sg.generate(ex));
     
     // 3 or more number
    ex.setSqlValues(new Object[]{new Double(12),new Integer(0), new Double(Double.NaN), new BigDecimal(1234.0)});
     assertEquals("C IN (12.0, 0, NaN, 1234)", sg.generate(ex));
   }
   
   public void testDummyExpr() {
     SqlDummyExpr ex = new SqlDummyExpr();
     assertEquals("1 = 1", sg.generate(ex));
   }
   
   public void testBetweenExpr() {
     SqlBetweenExpr ex = new SqlBetweenExpr();
     ex.setFirst(new Integer(12));
     ex.setLast(new Integer(22));
     assertEquals("C BETWEEN 12 AND 22", sg.generate(ex));
   }
 
   private void eqlExprWithOperandsTest(SqlExprWithOperands expr, String expected) {
     SqlEqualExpr opd1 = new SqlEqualExpr();
     opd1.setColumnId("C1");
     opd1.setSqlValue("c1");
     expr.addOperand(opd1);
     SqlEqualExpr opd2 = new SqlEqualExpr();
     opd2.setColumnId("C2");
     opd2.setSqlValue("c2");
     expr.addOperand(opd2);
     SqlGenerator sg = new SqlGenerator();
     assertEquals(expected, sg.generate(expr));
   }
   
   protected void setUp() throws Exception {
     sg = new SqlGenerator("C");
   }
 }
