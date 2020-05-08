 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package test.org.modelcc.language.factory;
 
 import test.languages.optionaltest.OptionalTestLanguage2;
 import test.languages.inheritlanguage.Inherit1;
 import test.languages.inheritlanguage.Inherit2;
 import test.languages.inheritlanguage.Inherit3;
 import test.languages.inheritlanguage.Inherit4;
 import test.languages.inheritlanguage.Inherit5;
 import test.languages.keys.Keys11Lang;
 import test.languages.keys.Keys10Lang;
 import test.languages.keys.Keys9Lang;
 import test.languages.keys.Keys8Lang;
 import test.languages.keys.Keys7Lang;
 import test.languages.keys.Keys6Lang;
 import test.languages.keys.Keys5Lang;
 import test.languages.keys.Keys4Lang;
 import test.languages.keys.Keys3Lang;
 import test.languages.keys.Keys2Lang;
 
 import org.modelcc.language.factory.CompositeSymbolBuilder;
 import java.util.logging.Filter;
 import java.util.logging.LogRecord;
 import test.languages.keys.Keys1Lang;
 import test.languages.autorun.AutorunTests;
 import test.languages.optionaltest2.OptionalTest2Language;
 import test.languages.optionaltest.OptionalTestLanguage;
 import test.languages.positions.Position1;
 import test.languages.positions.Position10;
 import test.languages.positions.Position11;
 import test.languages.positions.Position12;
 import test.languages.positions.Position13;
 import test.languages.positions.Position14;
 import test.languages.positions.Position15;
 import test.languages.positions.Position16;
 import test.languages.positions.Position17;
 import test.languages.positions.Position18;
 import test.languages.positions.Position19;
 import test.languages.positions.Position2;
 import test.languages.positions.Position20;
 import test.languages.positions.Position3;
 import test.languages.positions.Position4;
 import test.languages.positions.Position5;
 import test.languages.positions.Position6;
 import test.languages.positions.Position7;
 import test.languages.positions.Position8;
 import test.languages.positions.Position9;
 import test.languages.positions.PositionFree1;
 import test.languages.positions.PositionRef1;
 import test.languages.testlanguage.Test7_2;
 import test.languages.testlanguage.Test7_1;
 import test.languages.testlanguage.Test7;
 import test.languages.composition3.CondSentence3;
 import test.languages.composition2.CondSentence2;
 import test.languages.composition3.Composition3;
 import test.languages.composition2.Composition2;
 import test.languages.composition.Composition1;
 import test.languages.arithmeticcalculator2.Expression2;
 import test.languages.arithmeticcalculator2.expressions.BinaryExpression;
 import test.languages.arithmeticcalculator2.expressions.ParenthesizedExpression;
 import test.languages.arithmeticcalculator2.expressions.literals.IntegerLiteral;
 import test.languages.worklanguage.Ini11;
 import test.languages.worklanguage.Ini10;
 import test.languages.worklanguage.Ini9;
 import test.languages.worklanguage.Ini8;
 import test.languages.worklanguage.Ini7;
 import test.languages.worklanguage.Ini61;
 import test.languages.worklanguage.Ini6;
 import test.languages.worklanguage.Ini5;
 import test.languages.worklanguage.Ini4;
 import test.languages.worklanguage.Ini3;
 import java.util.ArrayList;
 import test.languages.worklanguage.Ini2;
 import test.languages.worklanguage.Ini1;
 import test.languages.worklanguage.Ino;
 import test.languages.worklanguage.Ini;
 import test.languages.arithmeticcalculator.Expression;
 import org.modelcc.parser.CannotCreateParserException;
 import org.modelcc.parser.Parser;
 import org.modelcc.parser.ParserFactory;
 import org.modelcc.parser.ProbabilisticParser;
 import org.modelcc.parser.ProbabilisticParserFactory;
 import org.modelcc.probabilistic.ProbabilityValue;
 import org.modelcc.lexer.recognizer.regexp.RegExpPatternRecognizer;
 import org.modelcc.lexer.recognizer.PatternRecognizer;
 import org.modelcc.io.ModelReader;
 import org.modelcc.io.java.JavaModelReader;
 import org.modelcc.metamodel.Model;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author elezeta
  */
 public class LanguageSpecificationFactoryTest {
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     
     private Parser genParser(Class c) {
         ModelReader jmr = new JavaModelReader(c);
         Model m;
 		try {
 			m = jmr.read().clone();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			assertTrue(false);
 			return null;
 		}
         new HashSet<PatternRecognizer>();
         Set<PatternRecognizer> ignore = new HashSet<PatternRecognizer>();
         ignore.add(new RegExpPatternRecognizer("\\t"));
         ignore.add(new RegExpPatternRecognizer(" "));
         ignore.add(new RegExpPatternRecognizer("\n"));
         ignore.add(new RegExpPatternRecognizer("\r"));
         Parser<Expression2> parser;
         try {
 			parser = ParserFactory.create(m,ignore);
 		} catch (CannotCreateParserException e1) {
 			e1.printStackTrace();
 			assertTrue(false);
 			return null;
 		}
 		return parser;
     }
 
     private Parser genProbabilisticParser(Class c) {
         ModelReader jmr = new JavaModelReader(c);
         Model m;
 		try {
 			m = jmr.read();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			assertTrue(false);
 			return null;
 		}
         new HashSet<PatternRecognizer>();
         Set<PatternRecognizer> ignore = new HashSet<PatternRecognizer>();
         ignore.add(new RegExpPatternRecognizer("\\t"));
         ignore.add(new RegExpPatternRecognizer(" "));
         ignore.add(new RegExpPatternRecognizer("\n"));
         ignore.add(new RegExpPatternRecognizer("\r"));
         ProbabilisticParser<Expression2> parser;
         try {
 			parser = ProbabilisticParserFactory.create(m,ignore);
 		} catch (CannotCreateParserException e1) {
 			e1.printStackTrace();
 			assertTrue(false);
 			return null;
 		}
 		return parser;
     }
     private class CountFilter implements Filter {
 
         boolean show;
 
         private int count;
 
         public CountFilter(boolean show) {
             this.show = show;
         }
 
         @Override
 		public boolean isLoggable(LogRecord record) {
             if (record.getLevel() == Level.SEVERE) {
                 count++;
             }
             if (show) {
                 return true;
             }
             else
                 return false;
         }
 
         int getCount() { return count; }
 
     }
 
     public Collection<Object> testFull(String input,Class cl) {
 
         ModelReader jmr = new JavaModelReader(cl);
         Model m;
 		try {
 			m = jmr.read();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			assertTrue(false);
 			return null;
 		}
         new HashSet<PatternRecognizer>();
         Set<PatternRecognizer> ignore = new HashSet<PatternRecognizer>();
         ignore.add(new RegExpPatternRecognizer("\\t"));
         ignore.add(new RegExpPatternRecognizer(" "));
         ignore.add(new RegExpPatternRecognizer("\n"));
         ignore.add(new RegExpPatternRecognizer("\r"));
         Parser parser;
 		try {
 			parser = ParserFactory.create(m,ignore);
 		} catch (CannotCreateParserException e1) {
 			e1.printStackTrace();
 			assertTrue(false);
 			return null;
 		}
         try {
         	return parser.parseAll(input);
         } catch (Exception e) {
         	return new HashSet<Object>();
         }
     }
 
     void checkExpression(String str,double value) {
         Collection<Object> a = testFull(str,Expression.class);
         assertTrue(a.size()>=1);
         //System.out.println(str+"  = "+((Expression)a.iterator().next()).eval());
         assertTrue(((Expression)a.iterator().next()).eval()-0.1<value);
         assertTrue(((Expression)a.iterator().next()).eval()+0.1>value);
     }
 
     @Test
     public void ModelToLanguageSpecificationTest1() {
 
         checkExpression("3+5+5",13);
         checkExpression("3+5",8);
         checkExpression("3",3);
         checkExpression("3+(5+5)",13);
         checkExpression("3-5+6",4);
         checkExpression("3*5+5",20);
         checkExpression("3*(5+5)",30);
         checkExpression("3/5++(2*5)",10.6);
         checkExpression("3*2*5+-2",28);
         checkExpression("3+5*5",28);
         checkExpression("3+2/6/2",3.16);
         checkExpression("3*5*1-5+6*12+5",87);
     }
 
     @Test
     public void ModelToLanguageSpecificationTest1a() {
         assertEquals(0,testFull("3+5+5",Expression2.class).size());
         assertEquals(1,testFull("3+5",Expression2.class).size());
         assertEquals(1,testFull("3",Expression2.class).size());
         assertEquals(1,testFull("3+(5+5)",Expression2.class).size());
         assertEquals(1,testFull("3+5*5",Expression2.class).size());
         assertEquals(1,testFull("3*5+5",Expression2.class).size());
         assertEquals(1,testFull("3*(5+5)",Expression2.class).size());
         assertEquals(0,testFull("3+2/6/2",Expression2.class).size());
         assertEquals(0,testFull("3*5*1-5+6*12+5",Expression2.class).size());
         assertEquals(0,testFull("3*5+1*5+6*12+5",Expression2.class).size());
         assertEquals(1,testFull("(3*5+1*5)+(6*12+5)",Expression2.class).size());
         assertEquals(1,testFull("3/5++2*5",Expression2.class).size());
         assertEquals(1,testFull("(3/5++2*5)",Expression2.class).size());
         assertEquals(1,testFull("(3/5++2*5)+(3/5+2*5)",Expression2.class).size());
         assertEquals(0,testFull("3*2*5+-2",Expression2.class).size());
     }
 
     @Test
     public void ModelToLanguageSpecificationTest2() {
 
         Collection<Object> o;
         o = testFull("hello",Ini.class);
         assertEquals(1,o.size());
         Ini i = (Ini) o.iterator().next();
         assertEquals(Ino.class,i.a.getClass());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest3() {
 
         Collection<Object> o;
         o = testFull("hello",Ini1.class);
         assertEquals(1,o.size());
         Ini1 i = (Ini1) o.iterator().next();
         assertEquals(Ino[].class,i.a.getClass());
         assertEquals(1,i.a.length);
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest3a() {
 
         Collection<Object> o;
         o = testFull("hellohello",Ini1.class);
         assertEquals(1,o.size());
         Ini1 i = (Ini1) o.iterator().next();
         assertEquals(Ino[].class,i.a.getClass());
         assertEquals(2,i.a.length);
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest4() {
 
         Collection<Object> o;
         o = testFull("hello",Ini2.class);
         assertEquals(1,o.size());
         Ini2 i = (Ini2) o.iterator().next();
         assertEquals(ArrayList.class,i.a.getClass());
         assertEquals(1,i.a.size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest4a() {
 
         Collection<Object> o;
         o = testFull("hellohello",Ini1.class);
         assertEquals(1,o.size());
         Ini1 i = (Ini1) o.iterator().next();
         assertEquals(Ino[].class,i.a.getClass());
         assertEquals(2,i.a.length);
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest5() {
 
         Collection<Object> o;
         o = testFull("hello",Ini3.class);
         assertEquals(1,o.size());
         Ini3 i = (Ini3) o.iterator().next();
         assertEquals(ArrayList.class,i.a.getClass());
         assertEquals(1,i.a.size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest5a() {
 
         Collection<Object> o;
         o = testFull("hellohellohello",Ini3.class);
         assertEquals(1,o.size());
         Ini3 i = (Ini3) o.iterator().next();
         assertEquals(ArrayList.class,i.a.getClass());
         assertEquals(3,i.a.size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest6() {
 
         Collection<Object> o;
         o = testFull("hellohello",Ini4.class);
         assertEquals(1,o.size());
         Ini4 i = (Ini4) o.iterator().next();
         assertEquals(ArrayList.class,i.a.getClass());
         assertEquals(2,i.a.size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest7() {
 
         Collection<Object> o;
         o = testFull("hellohellohellohello",Ini5.class);
         assertEquals(1,o.size());
         Ini5 i = (Ini5) o.iterator().next();
         assertEquals(HashSet.class,i.a.getClass());
         assertEquals(4,i.a.size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest8() {
 
         Collection<Object> o;
         o = testFull("hellohellohellohello",Ini6.class);
         assertEquals(1,o.size());
         Ini6 i = (Ini6) o.iterator().next();
         assertEquals(HashSet.class,i.a.getClass());
         assertEquals(4,i.a.size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest9() {
 
         Collection<Object> o;
         o = testFull("hellohellohellohello",Ini61.class);
         assertEquals(1,o.size());
         Ini61 i = (Ini61) o.iterator().next();
         assertEquals(HashSet.class,i.a.getClass());
         assertEquals(1,i.a.size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest10() {
 
         Class c = Ini7.class;
         assertEquals(0,testFull("()",c).size());
         assertEquals(1,testFull("(-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+,-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+,-hello+,-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+,-hello+,-hello+,-hello+)",c).size());
 
     }
 
 
     @Test
     public void ModelToLanguageSpecificationTest11() {
 
         Class c = Ini8.class;
         assertEquals(1,testFull("()",c).size());
         assertEquals(1,testFull("(-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+,-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+,-hello+,-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+,-hello+,-hello+,-hello+)",c).size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest12() {
 
         Class c = Ini9.class;
         assertEquals(0,testFull("()",c).size());
         assertEquals(0,testFull("(-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+)",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+,-hello+)",c).size());
         assertEquals(0,testFull("(-hello+,-hello+,-hello+,-hello+,-hello+)",c).size());
         assertEquals(0,testFull("(-hello+,-hello+,-hello+,-hello+,-hello+,-hello+)",c).size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest13() {
 
         Class c = Ini10.class;
         assertEquals(0,testFull("()-hello+",c).size());
         assertEquals(0,testFull("(-hello+)-hello+",c).size());
         assertEquals(1,testFull("(-hello+,-hello+)-hello+",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+)-hello+",c).size());
         assertEquals(1,testFull("(-hello+,-hello+,-hello+,-hello+)-hello+",c).size());
         assertEquals(0,testFull("(-hello+,-hello+,-hello+,-hello+,-hello+)-hello+",c).size());
         assertEquals(0,testFull("(-hello+,-hello+,-hello+,-hello+,-hello+,-hello+)-hello+",c).size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest14() {
 
         Class c = Ini11.class;
         assertEquals(0,testFull("a()hellobcc",c).size());
         assertEquals(0,testFull("a(-hello+)hellobcc",c).size());
         assertEquals(1,testFull("a(-hello+,-hello+)hellobcc",c).size());
         assertEquals(1,testFull("a(-hello+,-hello+,-hello+)hellobcc",c).size());
         assertEquals(1,testFull("a(-hello+,-hello+,-hello+,-hello+)hellobcc",c).size());
         assertEquals(0,testFull("a(-hello+,-hello+,-hello+,-hello+,-hello+)hellobcc",c).size());
         assertEquals(0,testFull("a(-hello+,-hello+,-hello+,-hello+,-hello+,-hello+)hellobcc",c).size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest15() {
 
         Class c = Composition1.class;
         assertEquals(1,testFull("start s end",c).size());
         assertEquals(1,testFull("start if e s end",c).size());
         assertEquals(1,testFull("start if e s else s end",c).size());
         assertEquals(1,testFull("start if e  if e s else s else s end",c).size());
         assertEquals(2,testFull("start if e  if e s else s end",c).size());
 
     }
 
     @Test
     public void ModelToLanguageSpecificationTest16() {
 
         Class c = Composition2.class;
         Collection<Object> o;
         assertEquals(1,testFull("start s end",c).size());
         assertEquals(1,testFull("start if e s end",c).size());
         assertEquals(1,testFull("start if e s else s end",c).size());
         assertEquals(1,testFull("start if e  if e s else s else s end",c).size());
         assertEquals(1,testFull("start if e  if e s else s end",c).size());
 
         o = testFull("start if e  if e s else s end",c);
         Composition2 cc = (Composition2) o.iterator().next();
         assertNull(((CondSentence2)(cc.sent)).elsesentence);
         assertNotNull(((CondSentence2) ((CondSentence2)(cc.sent)).ifsentence).elsesentence);
     }
 
     @Test
     public void ModelToLanguageSpecificationTest17() {
 
         Class c = Composition3.class;
         Collection<Object> o;
         assertEquals(1,testFull("start s end",c).size());
         assertEquals(1,testFull("start if e s end",c).size());
         assertEquals(1,testFull("start if e s else s end",c).size());
         assertEquals(1,testFull("start if e  if e s else s else s end",c).size());
         assertEquals(1,testFull("start if e  if e s else s end",c).size());
 
         o = testFull("start if e  if e s else s end",c);
         Composition3 cc = (Composition3) o.iterator().next();
         assertNotNull(((CondSentence3)(cc.sent)).elsesentence);
         assertNull(((CondSentence3) ((CondSentence3)(cc.sent)).ifsentence).elsesentence);
     }
 
     @Test
     public void ModelToLanguageSpecificationTest18() {
 
         Class c = OptionalTestLanguage.class;
         Collection<Object> o;
         o = testFull("1",c);
         OptionalTestLanguage cc = (OptionalTestLanguage) o.iterator().next();
         assertNotNull(cc.getTest());
     }
     
     @Test
     public void ModelToLanguageSpecificationTest19() {
 
         Class c = OptionalTest2Language.class;
         Collection<Object> o;
         o = testFull("1",c);
         OptionalTest2Language cc = (OptionalTest2Language) o.iterator().next();
         assertNotNull(cc.getTest());
         assertNotNull(cc.getTest().getTest());
        assertNull(cc.getTest().getTest().getTest());
     }
 
     @Test
     public void AutorunTest1() {
         assertEquals(1,testFull("a",Test7.class).size());
         assertEquals(0,testFull("b",Test7.class).size());
         assertEquals(1,testFull("a",Test7_1.class).size());
         assertEquals(0,testFull("b",Test7_1.class).size());
         assertEquals(1,testFull("a",Test7_2.class).size());
         assertEquals(1,testFull("b",Test7_2.class).size());
     }
 
     @Test
     public void AutorunTest2() {
 
         Class c = AutorunTests.class;
         Collection<Object> o;
         o = testFull("a a a",c);
         assertEquals(1,o.size());
         AutorunTests cc = (AutorunTests) o.iterator().next();
         assertEquals(1,cc.a.count);
         assertEquals(1,cc.b.count);
         assertEquals(1,cc.c.count);
         assertEquals(1,cc.a.a.count);
         assertEquals(1,cc.b.a.count);
         assertEquals(1,cc.c.a.count);
     }
 
     
     @Test
     public void ModelToLanguageSpecificationReferencesWarningTest() {
 
         Class c = Keys1Lang.class;
         Collection<Object> o;
         CountFilter cf = new CountFilter(false);
         Logger.getLogger(CompositeSymbolBuilder.class.getName()).setFilter(cf);
         o = testFull("a1 a2 refs a",c);
         Keys1Lang cc = (Keys1Lang) o.iterator().next();
         assertEquals(cc.keys1[0],cc.refs[0]);
         assertEquals(cf.getCount(),1);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest1() {
 
         Class c = Keys1Lang.class;
         Collection<Object> o;
         o = testFull("a1 refs a",c);
         Keys1Lang cc = (Keys1Lang) o.iterator().next();
         assertEquals(cc.keys1[0],cc.refs[0]);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest2() {
 
         Class c = Keys1Lang.class;
         Collection<Object> o;
         o = testFull("a1 refs a a",c);
         Keys1Lang cc = (Keys1Lang) o.iterator().next();
         assertEquals(cc.keys1[0],cc.refs[0]);
         assertEquals(cc.keys1[0],cc.refs[1]);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest3() {
 
         Class c = Keys1Lang.class;
         Collection<Object> o;
         o = testFull("a1 b4 refs a a b",c);
         Keys1Lang cc = (Keys1Lang) o.iterator().next();
         assertEquals(cc.keys1[0],cc.refs[0]);
         assertEquals(cc.keys1[0],cc.refs[1]);
         assertEquals(cc.keys1[1],cc.refs[2]);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest4() {
         Class c = Keys2Lang.class;
         Collection<Object> o;
         o = testFull("a1 refs a a b",c);
         assertFalse(o.iterator().hasNext());
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest5() {
         Class c = Keys3Lang.class;
         Collection<Object> o;
         o = testFull("data a1",c);
         assertTrue(o.iterator().hasNext());
     }
 
     @Test
     public void ModelToLanguageSpecificationReferencesTest6() {
         Class c = Keys3Lang.class;
         Collection<Object> o;
         o = testFull("a data a1",c);
         assertTrue(o.iterator().hasNext());
         Keys3Lang cc = (Keys3Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.keys1[0],cc.refs[0]);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest7() {
         Class c = Keys4Lang.class;
         Collection<Object> o;
         o = testFull("a data a1",c);
         assertTrue(o.iterator().hasNext());
         Keys4Lang cc = (Keys4Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.keys1[0],cc.refs);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest8() {
         Class c = Keys4Lang.class;
         Collection<Object> o;
         o = testFull("b data a1 b2",c);
         assertTrue(o.iterator().hasNext());
         Keys4Lang cc = (Keys4Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.keys1[1],cc.refs);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest9() {
         Class c = Keys3Lang.class;
         Collection<Object> o;
         o = testFull("b a a data a1 b2",c);
         assertTrue(o.iterator().hasNext());
         Keys3Lang cc = (Keys3Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.keys1[1],cc.refs[0]);
         assertEquals(cc.keys1[0],cc.refs[1]);
         assertEquals(cc.keys1[0],cc.refs[2]);
     }
     
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest10() {
         Class c = Keys3Lang.class;
         Collection<Object> o;
         o = testFull("a a b data a1 b3",c);
         assertTrue(o.iterator().hasNext());
         Keys3Lang cc = (Keys3Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.keys1[0],cc.refs[0]);
         assertEquals(cc.keys1[0],cc.refs[1]);
         assertEquals(cc.keys1[1],cc.refs[2]);
 
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest11() {
         Class c = Keys3Lang.class;
         Collection<Object> o;
         o = testFull("a a data a1",c);
         assertTrue(o.iterator().hasNext());
         Keys3Lang cc = (Keys3Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.keys1[0],cc.refs[0]);
         assertEquals(cc.keys1[0],cc.refs[1]);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest12() {
         Class c = Keys5Lang.class;
         Collection<Object> o;
         o = testFull("startref b endref data a1 b2",c);
         assertTrue(o.iterator().hasNext());
         Keys5Lang cc = (Keys5Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.keys1[1],cc.refs);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest13() {
         Class c = Keys6Lang.class;
         Collection<Object> o;
         o = testFull("startref kbc endref data kac1 kbc2",c);
         assertTrue(o.iterator().hasNext());
         Keys6Lang cc = (Keys6Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.keys1[1],cc.refs);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest14() {
         Class c = Keys7Lang.class;
         Collection<Object> o;
         o = testFull("startref kbc endref data kac1 kbc2",c);
         assertTrue(o.iterator().hasNext());
         Keys7Lang cc = (Keys7Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.keys1[1],cc.refs[0]);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest15() {
         Class c = Keys7Lang.class;
         Collection<Object> o;
         o = testFull("startref kac kbc endref data kbc1 kac2",c);
         assertTrue(o.iterator().hasNext());
         Keys7Lang cc = (Keys7Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.keys1[1],cc.refs[0]);
         assertEquals(cc.keys1[0],cc.refs[1]);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest16() {
         Class c = Keys7Lang.class;
         Collection<Object> o;
         o = testFull("startref kac kbc kdc endref data kbc1 kac2",c);
         assertFalse(o.iterator().hasNext());
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest17() {
         Class c = Keys8Lang.class;
         Collection<Object> o;
         o = testFull("kbc1 kac2 refs kac",c);
         assertTrue(o.iterator().hasNext());
         Keys8Lang cc = (Keys8Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.keys1[1],cc.refs[0]);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest18() {
         Class c = Keys9Lang.class;
         Collection<Object> o;
         o = testFull("[b,a,c] data [a,c,b]:1 [b,a,c]:2",c);
         assertTrue(o.iterator().hasNext());
         Keys9Lang cc = (Keys9Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.data[1],cc.refs[0]);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest19() {
         Class c = Keys10Lang.class;
         Collection<Object> o;
         o = testFull("[c,b,a,c] data [d,a]:1 [b,c,a,c]:2",c);
         assertTrue(o.iterator().hasNext());
         Keys10Lang cc = (Keys10Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.data[1],cc.refs[0]);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest20() {
         Class c = Keys10Lang.class;
         Collection<Object> o;
         o = testFull("[b,a,c] data [d,a]:1 [b,c,a,c]:2",c);
         assertTrue(o.iterator().hasNext());
         Keys10Lang cc = (Keys10Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.data[1],cc.refs[0]);
     }
     
     @Test
     public void ModelToLanguageSpecificationReferencesTest21() {
         Class c = Keys11Lang.class;
         Collection<Object> o;
         o = testFull("d1 data cval3endval1 val2endval1d",c);
         assertTrue(o.iterator().hasNext());
         Keys11Lang cc = (Keys11Lang) o.iterator().next();
         assertEquals(1,o.size());
         assertEquals(cc.data[1],cc.refs[0]);
     }
     
     @Test
     public void FullOptionalTest() {
         Class c = OptionalTestLanguage2.class;
         Collection<Object> o;
         o = testFull("",c);
         assertTrue(o.iterator().hasNext());
         assertNotNull(o.iterator().next());
     }
     
     @Test
     public void PositionTest1() {
         assertEquals(1,testFull("BA",Position1.class).size());
         assertEquals(0,testFull("AB",Position1.class).size());
         Position1 o = (Position1)testFull("B12A22",Position1.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
 
     }
 
     @Test
     public void PositionTest2() {
         assertEquals(1,testFull("BA",Position2.class).size());
         assertEquals(0,testFull("AB",Position2.class).size());
         Position2 o = (Position2)testFull("B12A22",Position2.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
     }
 
     @Test
     public void PositionTest3() {
         assertEquals(1,testFull("BAC",Position3.class).size());
         assertEquals(0,testFull("ABC",Position3.class).size());
         Position3 o = (Position3)testFull("B12A22",Position3.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
         o = (Position3)testFull("B12A22C1",Position3.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
         assertEquals("C1",o.c.value);
         o = (Position3)testFull("B12A22",Position3.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
         assertNull(o.c);
     }
 
     @Test
     public void PositionTest4() {
         assertEquals(1,testFull("BAC",Position4.class).size());
         assertEquals(1,testFull("ABC",Position4.class).size());
         Position4 o = (Position4) testFull("B12A22",Position4.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
         o = (Position4)testFull("B12A22C1",Position4.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
         assertEquals("C1",o.c.value);
         o = (Position4)testFull("B12A22",Position4.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
         assertNull(o.c);
         o = (Position4) testFull("A22B12",Position4.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
         o = (Position4)testFull("A22B12C1",Position4.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
         assertEquals("C1",o.c.value);
         o = (Position4)testFull("A22B12",Position4.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
         assertNull(o.c);    }
     
     @Test
     public void PositionTest5() {
         assertEquals(1,testFull("ACBCCCC",Position5.class).size());
         assertEquals(1,testFull("ACCCCBC",Position5.class).size());
         assertEquals(1,testFull("ACCCCCB",Position5.class).size());
         assertEquals(1,testFull("ABCCCCC",Position5.class).size());
         assertEquals(0,testFull("BACCCCC",Position5.class).size());
         Position5 o = (Position5) testFull("A22C1B12C2C3",Position5.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
         assertEquals("C1",o.c[0].value);
         assertEquals("C2",o.c[1].value);
         assertEquals("C3",o.c[2].value);
     }
 
     @Test
     public void PositionTest6() {
         assertEquals(1,testFull("ACCCCBC",Position6.class).size());
         assertEquals(0,testFull("ACBCCCC",Position6.class).size());
         assertEquals(0,testFull("ACCCCCB",Position6.class).size());
         assertEquals(0,testFull("ABCCCCC",Position6.class).size());
         Position5 o = (Position5) testFull("A22C1B12C2",Position5.class).iterator().next();
         assertEquals("B12",o.b.value);
         assertEquals("A22",o.a.value);
         assertEquals("C1",o.c[0].value);
         assertEquals("C2",o.c[1].value);
         }
 
     @Test
     public void PositionTest7() {
         assertEquals(1,testFull("ACCCCBC",Position7.class).size());
         assertEquals(1,testFull("ACBCCCC",Position7.class).size());
         assertEquals(2,testFull("ACCCCCB",Position7.class).size());
         assertEquals(2,testFull("ABCCCCC",Position7.class).size());
         assertEquals(0,testFull("BACCCCC",Position7.class).size());
     }
 
     @Test
     public void PositionTest8() {
         assertEquals(1,testFull("ACCBC",Position8.class).size());
         assertEquals(1,testFull("ACCCCBC",Position8.class).size());
         assertEquals(0,testFull("ACCCCCB",Position8.class).size());
         assertEquals(0,testFull("ABC",Position8.class).size());
         assertEquals(0,testFull("ACBC",Position8.class).size());
     }    
 
     @Test
     public void PositionTest9() {
         assertEquals(1,testFull("ACxyCxyCxyCxyzBwC",Position9.class).size());
         assertEquals(0,testFull("ACxyzBwC",Position9.class).size());
         assertEquals(0,testFull("ACxyCxyCxyCxyCzBw",Position9.class).size());
         assertEquals(0,testFull("AzBwC",Position9.class).size());
     }    
 
     @Test
     public void PositionTest10() {
         assertEquals(1,testFull("ACxyCxyCxyCzBwxyC",Position10.class).size());
         assertEquals(0,testFull("ACxyzBwC",Position10.class).size());
         assertEquals(0,testFull("ACxyCxyCxyCxyCzBw",Position10.class).size());
         assertEquals(0,testFull("AzBwC",Position10.class).size());
     }    
 
     @Test
     public void PositionTest11() {
         assertEquals(1,testFull("ACxyCxyCxyCxyzBwxyC",Position11.class).size());
         assertEquals(0,testFull("ACxyzBwxyC",Position11.class).size());
         assertEquals(0,testFull("ACxyCxyCxyCxyCxyzBw",Position11.class).size());
         assertEquals(0,testFull("AzBwC",Position11.class).size());
     }    
 
     @Test
     public void PositionTest12() {
         assertEquals(1,testFull("ACxyCxyCxyCzBwC",Position12.class).size());
         assertEquals(0,testFull("ACzBwC",Position12.class).size());
         assertEquals(0,testFull("ACxyCxyCxyCxyCzBw",Position12.class).size());
         assertEquals(0,testFull("AzBwC",Position12.class).size());
     }    
 
     @Test
     public void PositionTest13() {
         assertEquals(1,testFull("ACxyCxyCxyCxyzBwC",Position13.class).size());
         assertEquals(1,testFull("ACxyCxyzBwCxyCxyC",Position13.class).size());
         assertEquals(1,testFull("ACxyzBwCxyCxyCxyC",Position13.class).size());
     }    
 
     @Test
     public void PositionTest14() {
         assertEquals(1,testFull("ACxyCxyCxyCzBwxyC",Position14.class).size());
         assertEquals(1,testFull("ACxyCzBwxyCxyCxyC",Position14.class).size());
         assertEquals(1,testFull("ACzBwxyCxyCxyCxyC",Position14.class).size());
     }    
 
     @Test
     public void PositionTest15() {
         assertEquals(1,testFull("ACxyCxyCxyCzBwC",Position15.class).size());
         assertEquals(1,testFull("ACxyCzBwCxyCxyC",Position15.class).size());
         assertEquals(1,testFull("ACzBwCxyCxyCxyC",Position15.class).size());
     }    
 
     @Test
     public void PositionTest16() {
         assertEquals(1,testFull("ACxyCxyCxyCxyzBwxyC",Position16.class).size());
         assertEquals(1,testFull("ACxyCxyzBwxyCxyCxyC",Position16.class).size());
         assertEquals(1,testFull("ACxyzBwxyCxyCxyCxyC",Position16.class).size());
     }    
 
     @Test
     public void PositionTest17() {
         assertEquals(1,testFull("ACxyCxyCxyCxyzBwC",Position17.class).size());
         assertEquals(1,testFull("ACxyCxyzBwCxyCxyC",Position17.class).size());
         assertEquals(1,testFull("ACxyzBwCxyCxyCxyC",Position17.class).size());
         assertEquals(2,testFull("AzBwCxyCxyCxyCxyC",Position17.class).size());
         assertEquals(2,testFull("ACxyCxyCxyCxyCzBw",Position17.class).size());
     }    
 
     @Test
     public void PositionTest18() {
         assertEquals(1,testFull("ACxyCxyCxyCzBwxyC",Position18.class).size());
         assertEquals(1,testFull("ACxyCzBwxyCxyCxyC",Position18.class).size());
         assertEquals(1,testFull("ACzBwxyCxyCxyCxyC",Position18.class).size());
         assertEquals(1,testFull("AzBwCxyCxyCxyCxyC",Position18.class).size());
         assertEquals(1,testFull("ACxyCxyCxyCxyCzBw",Position18.class).size());
         assertEquals(1,testFull("ACxyCxyCxyCxyCzBw",Position18.class).size());
         assertEquals(1,testFull("AzBwxyCxyCxyCxyCxyC",Position18.class).size());
     }    
 
     @Test
     public void PositionTest19() {
         assertEquals(1,testFull("ACxyCxyCxyCzBwC",Position19.class).size());
         assertEquals(1,testFull("ACxyCzBwCxyCxyC",Position19.class).size());
         assertEquals(1,testFull("ACzBwCxyCxyCxyC",Position19.class).size());
         assertEquals(2,testFull("AzBwCxyCxyCxyCxyC",Position19.class).size());
         assertEquals(2,testFull("ACxyCxyCxyCxyCzBw",Position19.class).size());
         assertEquals(0,testFull("AzBwxyCxyCxyCxyCxyC",Position19.class).size());
     }    
 
     @Test
     public void PositionTest20() {
         assertEquals(1,testFull("ACxyCxyCxyCxyzBwxyC",Position20.class).size());
         assertEquals(1,testFull("ACxyCxyzBwxyCxyCxyC",Position20.class).size());
         assertEquals(1,testFull("ACxyzBwxyCxyCxyCxyC",Position20.class).size());
         assertEquals(1,testFull("AzBwCxyCxyCxyCxyC",Position20.class).size());
         assertEquals(1,testFull("ACxyCxyCxyCxyCzBw",Position20.class).size());
     }    
     
     @Test
     public void PositionFreeTest1() {
         assertEquals(1,testFull("ACCBC",PositionFree1.class).size());
         assertEquals(1,testFull("ACCCCBC",PositionFree1.class).size());
         assertEquals(1,testFull("CCBCA",PositionFree1.class).size());
         assertEquals(1,testFull("CCCCBCA",PositionFree1.class).size());
     }    
 
     @Test
     public void PositionRefTest1() {
         assertEquals(1,testFull("ID1A1 ID2A2   ID2 ID2 aID1 ID1",PositionRef1.class).size());
         PositionRef1 o = (PositionRef1) testFull("ID1A1 ID2A2   ID2 ID2 aID1 ID1",PositionRef1.class).iterator().next();
         assertEquals("A1",o.objects[0].content.value);
         assertEquals("A2",o.objects[1].content.value);
         assertEquals(o.objects[1],o.reflist[0]);
         assertEquals(o.objects[1],o.reflist[1]);
         assertEquals(o.objects[0],o.reflist[2]);
         assertEquals(o.objects[0],o.ref);
 
     }    
 
     @Test
     public void InheritTest1() {
         assertEquals(1,testFull("pA",Inherit1.class).size());
         assertEquals(1,testFull("A",Inherit1.class).size());
 
     }    
 
     @Test
     public void InheritTest2() {
         assertEquals(2,testFull("A",Inherit2.class).size());
     }    
 
     @Test
     public void InheritTest3() {
         assertEquals(1,testFull("AB",Inherit3.class).size());
     }    
 
     @Test
     public void InheritTest4() {
         assertEquals(1,testFull("BA",Inherit4.class).size());
     }    
 
     @Test
     public void InheritTest5() {
         assertEquals(1,testFull("A",Inherit5.class).size());
     }    
     
     @Test
     public void startAndEndIndexTest() {
 
     	Parser<Expression2> parser = genParser(Expression2.class);
     	Expression2 exp2;
         try {
         	//                   0        10
         	//                   01234567890123456789
         	exp2 = parser.parse("(3/5++2*5)+(3/5+2*5)");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         
         BinaryExpression be = (BinaryExpression)exp2;
         assertEquals(0,parser.getParsingMetadata(be).get("startIndex"));
         assertEquals(19,parser.getParsingMetadata(be).get("endIndex"));
 
         assertEquals(0,parser.getParsingMetadata(be.e1).get("startIndex"));
         assertEquals(9,parser.getParsingMetadata(be.e1).get("endIndex"));
 
         assertEquals(10,parser.getParsingMetadata(be.op).get("startIndex"));
         assertEquals(10,parser.getParsingMetadata(be.op).get("endIndex"));
 
         assertEquals(11,parser.getParsingMetadata(be.e2).get("startIndex"));
         assertEquals(19,parser.getParsingMetadata(be.e2).get("endIndex"));
 
         ParenthesizedExpression pe = (ParenthesizedExpression)(be.e1);
 
         assertEquals(0,parser.getParsingMetadata(pe).get("startIndex"));
         assertEquals(9,parser.getParsingMetadata(pe).get("endIndex"));
 
         BinaryExpression be1 = (BinaryExpression)(pe.e);
         
         assertEquals(1,parser.getParsingMetadata(be1).get("startIndex"));
         assertEquals(8,parser.getParsingMetadata(be1).get("endIndex"));
 
         assertEquals(1,parser.getParsingMetadata(be1.e1).get("startIndex"));
         assertEquals(3,parser.getParsingMetadata(be1.e1).get("endIndex"));
 
         assertEquals(4,parser.getParsingMetadata(be1.op).get("startIndex"));
         assertEquals(4,parser.getParsingMetadata(be1.op).get("endIndex"));
 
         assertEquals(5,parser.getParsingMetadata(be1.e2).get("startIndex"));
         assertEquals(8,parser.getParsingMetadata(be1.e2).get("endIndex"));
 
         BinaryExpression be2 = (BinaryExpression)(be1.e1);
         
         assertEquals(1,parser.getParsingMetadata(be2).get("startIndex"));
         assertEquals(3,parser.getParsingMetadata(be2).get("endIndex"));
 
         assertEquals(1,parser.getParsingMetadata(be2.e1).get("startIndex"));
         assertEquals(1,parser.getParsingMetadata(be2.e1).get("endIndex"));
 
         IntegerLiteral le = (IntegerLiteral)(be2.e1);
         
         assertEquals(1,parser.getParsingMetadata(le).get("startIndex"));
         assertEquals(1,parser.getParsingMetadata(le).get("endIndex"));
 
         
     }
     
     
     @Test
     public void probabilityTest1() {
 
     	Parser<test.languages.probabilities.Test1> parser = genProbabilisticParser(test.languages.probabilities.Test1.class);
     	test.languages.probabilities.Test1 test1;
         try {
         	test1 = parser.parse("a");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.5d,((ProbabilityValue)parser.getParsingMetadata(test1.a).get("probability")).getNumericValue(),0.01d);
         assertEquals(0.08d,((ProbabilityValue)parser.getParsingMetadata(test1).get("probability")).getNumericValue(),0.001d);
                 
     }
 
     @Test
     public void probabilityTest2() {
 
     	Parser<test.languages.probabilities.Test2> parser = genProbabilisticParser(test.languages.probabilities.Test2.class);
     	Collection<test.languages.probabilities.Test2> test2;
         try {
         	test2 = parser.parseAll("a");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(2,test2.size());
         Iterator<test.languages.probabilities.Test2> ite = test2.iterator();
         assertEquals(0.0640d,((ProbabilityValue)parser.getParsingMetadata(ite.next()).get("probability")).getNumericValue(),0.001d);
         assertEquals(0.0040d,((ProbabilityValue)parser.getParsingMetadata(ite.next()).get("probability")).getNumericValue(),0.001d);
                 
     }
 
     @Test
     public void probabilityTest3() {
 
     	Parser<test.languages.probabilities.Test3> parser = genProbabilisticParser(test.languages.probabilities.Test3.class);
     	Collection<test.languages.probabilities.Test3> test3;
         try {
         	test3 = parser.parseAll("a");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(2,test3.size());
         Iterator<test.languages.probabilities.Test3> ite = test3.iterator();
         assertEquals(0.0640d,((ProbabilityValue)parser.getParsingMetadata(ite.next()).get("probability")).getNumericValue(),0.001d);
         assertEquals(0.0040d,((ProbabilityValue)parser.getParsingMetadata(ite.next()).get("probability")).getNumericValue(),0.001d);
                 
         try {
         	test3 = parser.parseAll("aa");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(1,test3.size());
         assertEquals(0.0080d,((ProbabilityValue)parser.getParsingMetadata(test3.iterator().next()).get("probability")).getNumericValue(),0.001d);
 
         try {
         	test3 = parser.parseAll("");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(1,test3.size());
         assertEquals(0.032d,((ProbabilityValue)parser.getParsingMetadata(test3.iterator().next()).get("probability")).getNumericValue(),0.001d);
     }
 
 
     @Test
     public void probabilityTest4() {
 
     	Parser<test.languages.probabilities.Test4> parser = genProbabilisticParser(test.languages.probabilities.Test4.class);
     	Collection<test.languages.probabilities.Test4> test4;
         try {
         	test4 = parser.parseAll("a");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(2,test4.size());
         Iterator<test.languages.probabilities.Test4> ite = test4.iterator();
         assertEquals(0.0640d,((ProbabilityValue)parser.getParsingMetadata(ite.next()).get("probability")).getNumericValue(),0.001d);
         assertEquals(0.0040d,((ProbabilityValue)parser.getParsingMetadata(ite.next()).get("probability")).getNumericValue(),0.001d);
                 
         try {
         	test4 = parser.parseAll("aa");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(1,test4.size());
         assertEquals(0.0080d,((ProbabilityValue)parser.getParsingMetadata(test4.iterator().next()).get("probability")).getNumericValue(),0.001d);
 
     }
 
     @Test
     public void probabilityTest5() {
 
     	Parser<test.languages.probabilities.Test5> parser = genProbabilisticParser(test.languages.probabilities.Test5.class);
     	test.languages.probabilities.Test5 test5;
         try {
         	test5 = parser.parse("");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.040d,((ProbabilityValue)parser.getParsingMetadata(test5).get("probability")).getNumericValue(),0.001d);
                 
         try {
         	test5 = parser.parse("a");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.020d,((ProbabilityValue)parser.getParsingMetadata(test5).get("probability")).getNumericValue(),0.001d);
         try {
         	test5 = parser.parse("aa");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.010d,((ProbabilityValue)parser.getParsingMetadata(test5).get("probability")).getNumericValue(),0.001d);
 
     }
 
     @Test
     public void probabilityTest6() {
 
     	Parser<test.languages.probabilities.Test6> parser = genProbabilisticParser(test.languages.probabilities.Test6.class);
     	test.languages.probabilities.Test6 test6;
         try {
         	test6 = parser.parse("");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.040d,((ProbabilityValue)parser.getParsingMetadata(test6).get("probability")).getNumericValue(),0.001d);
                 
         try {
         	test6 = parser.parse("a");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.020d,((ProbabilityValue)parser.getParsingMetadata(test6).get("probability")).getNumericValue(),0.001d);
         try {
         	test6 = parser.parse("aa");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.010d,((ProbabilityValue)parser.getParsingMetadata(test6).get("probability")).getNumericValue(),0.001d);
 
     }
 
 
     @Test
     public void probabilityTest7() {
 
     	Parser<test.languages.probabilities.Test7> parser = genProbabilisticParser(test.languages.probabilities.Test7.class);
     	test.languages.probabilities.Test7 test7;
         try {
         	test7 = parser.parse("");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.040d,((ProbabilityValue)parser.getParsingMetadata(test7).get("probability")).getNumericValue(),0.001d);
                 
         try {
         	test7 = parser.parse("a");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.020d,((ProbabilityValue)parser.getParsingMetadata(test7).get("probability")).getNumericValue(),0.001d);
         try {
         	test7 = parser.parse("aa");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.010d,((ProbabilityValue)parser.getParsingMetadata(test7).get("probability")).getNumericValue(),0.001d);
 
     }
 
 
     @Test
     public void probabilityTest8() {
 
     	Parser<test.languages.probabilities.Test8> parser = genProbabilisticParser(test.languages.probabilities.Test8.class);
     	test.languages.probabilities.Test8 test8;
         try {
         	test8 = parser.parse("");
         } catch (Exception e) {
         	e.printStackTrace();
 			assertTrue(false);
 			return;
         }
         assertEquals(0.040d,((ProbabilityValue)parser.getParsingMetadata(test8).get("probability")).getNumericValue(),0.001d);
                 
         try {
         	test8 = parser.parse("a");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.020d,((ProbabilityValue)parser.getParsingMetadata(test8).get("probability")).getNumericValue(),0.001d);
 
         try {
         	test8 = parser.parse("aa");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.010d,((ProbabilityValue)parser.getParsingMetadata(test8).get("probability")).getNumericValue(),0.001d);
 
         try {
         	test8 = parser.parse("b");
         } catch (Exception e) {
         	e.printStackTrace();
 			assertTrue(false);
 			return;
         }
         assertEquals(0.0040d,((ProbabilityValue)parser.getParsingMetadata(test8).get("probability")).getNumericValue(),0.001d);
                 
         try {
         	test8 = parser.parse("ab");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.0020d,((ProbabilityValue)parser.getParsingMetadata(test8).get("probability")).getNumericValue(),0.001d);
 
         try {
         	test8 = parser.parse("baa");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.0010d,((ProbabilityValue)parser.getParsingMetadata(test8).get("probability")).getNumericValue(),0.001d);
 
     }
 
     @Test
     public void probabilityTest9() {
 
     	Parser<test.languages.probabilities.Test9> parser = genProbabilisticParser(test.languages.probabilities.Test9.class);
     	test.languages.probabilities.Test9 test9;
         try {
         	test9 = parser.parse("");
         } catch (Exception e) {
         	e.printStackTrace();
 			assertTrue(false);
 			return;
         }
         assertEquals(0.036d,((ProbabilityValue)parser.getParsingMetadata(test9).get("probability")).getNumericValue(),0.001d);
                 
         try {
         	test9 = parser.parse("a");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.018d,((ProbabilityValue)parser.getParsingMetadata(test9).get("probability")).getNumericValue(),0.001d);
 
         try {
         	test9 = parser.parse("aa");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.009d,((ProbabilityValue)parser.getParsingMetadata(test9).get("probability")).getNumericValue(),0.001d);
 
         try {
         	test9 = parser.parse("b");
         } catch (Exception e) {
         	e.printStackTrace();
 			assertTrue(false);
 			return;
         }
         assertEquals(0.00040d,((ProbabilityValue)parser.getParsingMetadata(test9).get("probability")).getNumericValue(),0.0001d);
                 
         try {
         	test9 = parser.parse("ab");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.00020d,((ProbabilityValue)parser.getParsingMetadata(test9).get("probability")).getNumericValue(),0.0001d);
 
         try {
         	test9 = parser.parse("baa");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.00010d,((ProbabilityValue)parser.getParsingMetadata(test9).get("probability")).getNumericValue(),0.0001d);
 
     }
 
 
     @Test
     public void probabilityTest8b() {
 
     	Parser<test.languages.probabilities.Test8b> parser = genProbabilisticParser(test.languages.probabilities.Test8b.class);
     	test.languages.probabilities.Test8b test8b;
         try {
         	test8b = parser.parse("");
         } catch (Exception e) {
         	e.printStackTrace();
 			assertTrue(false);
 			return;
         }
         assertEquals(0.040d,((ProbabilityValue)parser.getParsingMetadata(test8b).get("probability")).getNumericValue(),0.001d);
                 
         try {
         	test8b = parser.parse("a");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.020d,((ProbabilityValue)parser.getParsingMetadata(test8b).get("probability")).getNumericValue(),0.001d);
 
         try {
         	test8b = parser.parse("aa");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.010d,((ProbabilityValue)parser.getParsingMetadata(test8b).get("probability")).getNumericValue(),0.001d);
    
         try {
         	test8b = parser.parse("ba");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.0020d,((ProbabilityValue)parser.getParsingMetadata(test8b).get("probability")).getNumericValue(),0.001d);
 
         try {
         	test8b = parser.parse("aba");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.0010d,((ProbabilityValue)parser.getParsingMetadata(test8b).get("probability")).getNumericValue(),0.001d);
 
     }
 
     @Test
     public void probabilityTest9b() {
 
     	Parser<test.languages.probabilities.Test9b> parser = genProbabilisticParser(test.languages.probabilities.Test9b.class);
     	test.languages.probabilities.Test9b test9b;
         try {
         	test9b = parser.parse("");
         } catch (Exception e) {
         	e.printStackTrace();
 			assertTrue(false);
 			return;
         }
         assertEquals(0.036d,((ProbabilityValue)parser.getParsingMetadata(test9b).get("probability")).getNumericValue(),0.001d);
                 
         try {
         	test9b = parser.parse("a");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.018d,((ProbabilityValue)parser.getParsingMetadata(test9b).get("probability")).getNumericValue(),0.001d);
 
         try {
         	test9b = parser.parse("aa");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.009d,((ProbabilityValue)parser.getParsingMetadata(test9b).get("probability")).getNumericValue(),0.001d);
 
         try {
         	test9b = parser.parse("ba");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.00020d,((ProbabilityValue)parser.getParsingMetadata(test9b).get("probability")).getNumericValue(),0.0001d);
 
         try {
         	test9b = parser.parse("aba");
         } catch (Exception e) {
 			assertTrue(false);
 			return;
         }
         assertEquals(0.00010d,((ProbabilityValue)parser.getParsingMetadata(test9b).get("probability")).getNumericValue(),0.0001d);
 
     }
 
 
 }
