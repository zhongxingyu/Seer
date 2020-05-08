 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package test.org.modelcc.io.java;
 
 
 
 import test.languages.warnings.ReferenceNotID2;
 import test.languages.warnings.ReferenceNotID;
 import test.languages.warnings.ReferenceNotIModel;
 import test.languages.warnings.CollectionID;
 import test.languages.warnings.IDNotIModel;
 import test.languages.warnings.OptionalID;
 
 import java.util.HashSet;
 import java.util.List;
 import test.languages.warnings.OptionalMult;
 import test.languages.warnings.OptionalMain;
 import test.languages.warnings.AbstNoSubClasses1;
 import test.languages.warnings.AbstNoSubClasses;
 
 import org.modelcc.io.ModelReader;
 import org.modelcc.io.java.ClassDoesNotExtendIModel;
 import org.modelcc.io.java.JavaModelReader;
 import java.util.logging.Filter;
 import java.util.logging.LogRecord;
 import test.languages.wronglanguages.*;
 import test.languages.patternrecognizertest.*;
 import org.modelcc.metamodel.*;
 import org.modelcc.parser.Parser;
 import org.modelcc.parser.ParserFactory;
 
 import test.languages.testlanguage.*;
 import org.modelcc.AssociativityType;
 import test.languages.arithmeticcalculator.*;
 import test.languages.arithmeticcalculator.unaryoperators.*;
 import test.languages.arithmeticcalculator.binaryoperators.*;
 import test.languages.nestedinheritsexample.*;
 import test.languages.arithmeticcalculator.expressions.*;
 import test.languages.arithmeticcalculator.expressions.literals.*;
 import test.org.modelcc.ModelCC2_03Test;
 import test.org.modelcc.io.Serialization;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Set;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.modelcc.lexer.recognizer.PatternRecognizer;
 import org.modelcc.lexer.recognizer.regexp.RegExpPatternRecognizer;
 import org.modelcc.CompositionType;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author elezeta
  */
 public class DefaultElementTest {
 
     @Test
     public void DefaultElementTest1() {
 
         try {
 
             ModelReader jmr = new JavaModelReader(test.languages.emptymatchers.basics.StartPoint.class);
 
             Model m = jmr.read();
 
             Set<PatternRecognizer> se = new HashSet<PatternRecognizer>();
             se.add(new RegExpPatternRecognizer(" "));
             
             Parser<test.languages.emptymatchers.basics.StartPoint> parser = ParserFactory.create(m,se);
 
             test.languages.emptymatchers.basics.StartPoint result = parser.parse("ab");
             
             if (result == null)
                 assertFalse(true);
 
             assertNotNull(result);
             assertNotNull(result.content);
             assertNotNull(((test.languages.emptymatchers.basics.ContentBasics)result.content).val);
             assertEquals("",((test.languages.emptymatchers.basics.ContentBasics)result.content).val);
 
 
         } catch (Exception ex) {
             Logger.getLogger(ModelCC2_03Test.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         }
 
     }
     
 
     @Test
     public void DefaultElementTest2() {
 
         try {
 
             ModelReader jmr = new JavaModelReader(test.languages.emptymatchers.choices.StartPoint.class);
 
             Model m = jmr.read();
 
             Set<PatternRecognizer> se = new HashSet<PatternRecognizer>();
             se.add(new RegExpPatternRecognizer(" "));
             
             Parser<test.languages.emptymatchers.choices.StartPoint> parser = ParserFactory.create(m,se);
 
             test.languages.emptymatchers.choices.StartPoint result = parser.parse("ab");
             
             if (result == null)
                 assertFalse(true);
 
             assertNotNull(result);
             assertNotNull(result.content);
             assertNotNull(((test.languages.emptymatchers.choices.ContentChoices2)result.content).val);
             assertEquals("",((test.languages.emptymatchers.choices.ContentChoices2)result.content).val);
 
 
         } catch (Exception ex) {
             Logger.getLogger(ModelCC2_03Test.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         }
 
     }
 
     @Test
     public void DefaultElementTest3() {
 
         try {
 
             ModelReader jmr = new JavaModelReader(test.languages.emptymatchers.complex.StartPoint.class);
 
             Model m = jmr.read();
 
             Set<PatternRecognizer> se = new HashSet<PatternRecognizer>();
             se.add(new RegExpPatternRecognizer(" "));
             
             Parser<test.languages.emptymatchers.complex.StartPoint> parser = ParserFactory.create(m,se);
 
             test.languages.emptymatchers.complex.StartPoint result = parser.parse("ab");
             
             if (result == null)
                 assertFalse(true);
 
             assertNotNull(result);
             assertNotNull(result.content);
            assertNotNull(((test.languages.emptymatchers.complex.ContentComplex)result.content).something);
            assertNull(((test.languages.emptymatchers.complex.ContentComplex)result.content).empty);
 
 
         } catch (Exception ex) {
             Logger.getLogger(ModelCC2_03Test.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         }
 
     }
 }
