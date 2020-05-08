 package ru.jcorp.m2parse.test;
 
 import junit.framework.TestCase;
 import ru.jcorp.m2parse.analyze.AnalyzeCursor;
 import ru.jcorp.m2parse.analyze.AnalyzeResult;
 import ru.jcorp.m2parse.analyze.Modula2Analyzer;
 import ru.jcorp.m2parse.analyze.RPNExecutor;
 import ru.jcorp.m2parse.exceptions.AnalyzeException;
 
 /**
  * White tests for Modula-2 analyzer
  *
  * @author artamonov
  */
 public class AnalyzerTest extends TestCase {
 
     private Modula2Analyzer analyzer = new Modula2Analyzer();
 
     public void testNumber() throws AnalyzeException {
         String expr = "\t \r \n  -1234  \r \n \t\t";
         analyzer.matchConst(new AnalyzeCursor(expr, 0), new String[]{" "});
     }
 
     public void testIndex() throws AnalyzeException {
         String expr = "\t \r \n  123 + 13 MOD 2 DIV 7  \r \n \t\t]";
         AnalyzeResult result = analyzer.matchIndex(new AnalyzeCursor(expr, 0));
         assertEquals(4, result.getConstants().size());
     }
 
     public void testIndexList() throws AnalyzeException {
         String expr = "\t \r \n  123 + 13 MOD 2 DIV 7, 23, a23]";
         AnalyzeResult result = analyzer.matchIndexList(new AnalyzeCursor(expr, 0));
         assertEquals(5, result.getConstants().size());
         assertEquals(1, result.getVariables().size());
     }
 
     public void testIdentity() throws AnalyzeException {
        String expr = "\t \r \n  _anabc3  \r \n \t\t";
         analyzer.matchIdentity(new AnalyzeCursor(expr, 0), new String[]{" "});
     }
 
     public void testSimpleExpr() throws AnalyzeException {
         String expression = "\t \r \n  FOR i[7, 14 MOD 3]:=0*23 TO 10 BY 2 DO  \r \n \t\t";
         AnalyzeResult result = analyzer.analyze(expression);
 
         assertEquals(result.getConstants().size(), 7);
         assertEquals(result.getVariables().size(), 1);
     }
 
     public void testComplexExpr() throws AnalyzeException {
         String expression = "for i[7, 14 mod 3 * apx] := 0*23 DIV 12 TO 10 MOD 7 DIV 6 BY 2 + 12 do  \r \n \t\t";
         AnalyzeResult result = analyzer.analyze(expression);
 
         assertEquals(9, result.getConstants().size());
         assertEquals(2, result.getVariables().size());
     }
 
     public void testSimpleCalc() throws AnalyzeException {
         String expression = "12 * 7 + 2 * 2 - 3";
         RPNExecutor notationExecutor = new RPNExecutor();
         int result = notationExecutor.calculate(expression);
 
         assertEquals(85, result);
     }
 
     public void testComplexCalc() throws AnalyzeException {
         String expression = "12 DIV 7 MOD 4 + 2 * 2 - 3 DIV 2";
         RPNExecutor notationExecutor = new RPNExecutor();
         int result = notationExecutor.calculate(expression);
 
         assertEquals(4, result);
     }
 
     public void testWhiteSample() throws AnalyzeException {
         String expression = "FOR  par[1,yy+23 MOD 7 DIV 2-1*3,kkk]:=ijk+aa[1,h-2]  TO  kkk*24 DIV 3\n" +
                 "         BY aa[3,4]-3  DO";
         AnalyzeResult result = analyzer.analyze(expression);
 
         assertEquals(7, result.getConstants().size());
         assertEquals(6, result.getVariables().size());
     }
 
     public void testWhiteSampleSecond() throws AnalyzeException {
            String expression = "FOR  ijk:=1444-7 DIV 12 * 3  TO  12345  BY  23-1*5 DIV 2 DO";
            AnalyzeResult result = analyzer.analyze(expression);
 
            assertEquals(9, result.getConstants().size());
            assertEquals(1, result.getVariables().size());
        }
 }
