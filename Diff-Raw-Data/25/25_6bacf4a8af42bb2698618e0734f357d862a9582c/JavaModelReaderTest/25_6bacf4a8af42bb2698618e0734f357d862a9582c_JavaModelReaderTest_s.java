 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package test.org.modelcc.io.java;
 
 
 
 import test.languages.warnings.OptionalPart;
 import test.languages.warnings.ReferenceNotID2;
 import test.languages.warnings.ReferenceNotID;
 import test.languages.warnings.ReferenceNotIModel;
 import test.languages.warnings.IDNotIModel;
 import test.languages.warnings.OptionalID;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import test.languages.warnings.OptionalMult;
 import test.languages.warnings.OptionalMain;
 import test.languages.warnings.AbstNoSubClasses1;
 import test.languages.warnings.AbstNoSubClasses;
 
 import org.modelcc.io.java.ClassDoesNotExtendIModelException;
 import org.modelcc.io.java.JavaModelReader;
 import java.util.logging.Filter;
 import java.util.logging.LogRecord;
 import test.languages.wronglanguages.*;
 import test.languages.patternrecognizertest.*;
 import org.modelcc.metamodel.*;
 import org.modelcc.parser.CannotCreateParserException;
 import org.modelcc.parser.Parser;
 import org.modelcc.parser.ParserException;
 import org.modelcc.parser.ParserFactory;
 import org.modelcc.probabilistic.NumericProbabilityEvaluator;
 
 import test.languages.testlanguage.*;
 import org.modelcc.AssociativityType;
 import org.modelcc.Position;
 
 import test.languages.arithmeticcalculator.*;
 import test.languages.arithmeticcalculator.unaryoperators.*;
 import test.languages.arithmeticcalculator.binaryoperators.*;
 import test.languages.nestedinheritsexample.*;
 import test.languages.arithmeticcalculator.expressions.*;
 import test.languages.arithmeticcalculator.expressions.literals.*;
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
 public class JavaModelReaderTest {
 
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
 
     boolean checkPrec(Model m,Class c1,Class c2) {
         if (m.getPrecedences().get(m.getClassToElement().get(c1)) != null)
             if (m.getPrecedences().get(m.getClassToElement().get(c1)).contains(m.getClassToElement().get(c2)))
                 return true;
         return false;
             
     }
 
     private Model modelGen(Class cl) {
         JavaModelReader jmr = new JavaModelReader(cl);
         Model m = null;
 
         try {
             m = jmr.read();
         } catch (Exception ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return null;
         }
         return m;
     }
 
     private List<String> modelWarnings(Class cl) {
         JavaModelReader jmr = new JavaModelReader(cl);
         try {
             jmr.read();
         } catch (Exception ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return jmr.getWarnings();
         }
         return jmr.getWarnings();
     }
 
     @Test
     public void runTimeFindSubClassesCheck1() {
        JavaModelReader jmr = new JavaModelReader(this.getClass());
         Method m;
         try {
             Class[] args = new Class[2];
             args[0] = String.class;
             args[1] = Class.class;
            m = jmr.getClass().getDeclaredMethod("runTimeFindSubclasses", args);
         } catch (NoSuchMethodException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         } catch (SecurityException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         }
 
         m.setAccessible(true);
         
         Set<Class> classes;
         try {
            classes = (Set<Class>) m.invoke(jmr,Expression.class.getPackage().getName(),Expression.class);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         } catch (InvocationTargetException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         }
 
         assertTrue(classes.contains(UnaryExpression.class));
         assertTrue(classes.contains(BinaryExpression.class));
         assertTrue(classes.contains(ParenthesizedExpression.class));
         assertTrue(classes.contains(IntegerLiteral.class));
         assertTrue(classes.contains(RealLiteral.class));
         assertTrue(classes.contains(LiteralExpression.class));
         assertEquals(6,classes.size());
 
 
     }
 
     @Test
     public void runTimeFindSubClassesCheck2() {
        JavaModelReader jmr = new JavaModelReader(this.getClass());
         Method m;
         try {
             Class[] args = new Class[2];
             args[0] = String.class;
             args[1] = Class.class;
            m = jmr.getClass().getDeclaredMethod("runTimeFindSubclasses", args);
         } catch (NoSuchMethodException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         } catch (SecurityException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         }
 
         m.setAccessible(true);
 
         Set<Class> classes;
         try {
            classes = (Set<Class>) m.invoke(jmr,LiteralExpression.class.getPackage().getName(),LiteralExpression.class);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         } catch (InvocationTargetException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         }
 
         /*
         for (Iterator<Class> ite = classes.iterator();ite.hasNext();) {
             Class cl = ite.next();
             System.out.println(cl.getCanonicalName());
         }*/
 
         assertTrue(classes.contains(IntegerLiteral.class));
         assertTrue(classes.contains(RealLiteral.class));
         assertEquals(2,classes.size());
 
 
     }
 
     @Test
     public void runTimeFindSubClassesCheck3() {
        JavaModelReader jmr = new JavaModelReader(this.getClass());
         Method m;
         try {
             Class[] args = new Class[2];
             args[0] = String.class;
             args[1] = Class.class;
            m = jmr.getClass().getDeclaredMethod("runTimeFindSubclasses", args);
         } catch (NoSuchMethodException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         } catch (SecurityException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         }
 
         m.setAccessible(true);
 
         Set<Class> classes;
         try {
            classes = (Set<Class>) m.invoke(jmr,NestedInherits.A.class.getPackage().getName(),NestedInherits.A.class);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         } catch (InvocationTargetException ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
             return;
         }
 
         /*
         for (Iterator<Class> ite = classes.iterator();ite.hasNext();) {
             Class cl = ite.next();
             System.out.println(cl.getCanonicalName());
         }*/
 
         assertTrue(classes.contains(NestedInherits.A.B.class));
         assertTrue(classes.contains(NestedInherits.C.class));
         assertEquals(2,classes.size());
 
     }
 
 
     @Test
     public void modelReadTest1() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(Expression.class);
         if (m == null)
             return;
         if (c.getCount()>0) {
             assertFalse(true);
             return;
         }
 
         assertEquals(15,m.getElements().size());
         ComplexModelElement ce;
         BasicModelElement be;
         ChoiceModelElement se;
 
         ElementMember sc;
         be = (BasicModelElement) m.getClassToElement().get(AdditionOperator.class);
         assertTrue(m.getElements().contains(be));
         assertEquals(AdditionOperator.class,be.getElementClass());
         assertEquals(AssociativityType.LEFT_TO_RIGHT,be.getAssociativity());
         assertEquals(0,be.getPrefix().size());
         assertEquals(0,be.getSuffix().size());
         assertEquals(0,be.getSeparator().size());
         assertNotNull(be.getPattern());
         assertNull(be.getValueField());
         assertNull(be.getSetupMethod());
         assertEquals(NumericProbabilityEvaluator.class,be.getProbabilityEvaluator().getClass());
         assertEquals(0.2d,((NumericProbabilityEvaluator)be.getProbabilityEvaluator()).evaluate(null).getNumericValue(),0.01d);
 
         be = (BasicModelElement) m.getClassToElement().get(DivisionOperator.class);
         assertTrue(m.getElements().contains(be));
         assertEquals(DivisionOperator.class,be.getElementClass());
         assertEquals(AssociativityType.LEFT_TO_RIGHT,be.getAssociativity());
         assertEquals(0,be.getPrefix().size());
         assertEquals(0,be.getSuffix().size());
         assertEquals(0,be.getSeparator().size());
         assertNotNull(be.getPattern());
         assertNull(be.getValueField());
         assertNull(be.getSetupMethod());
         assertEquals(FixedProbabilityEvaluator.class,be.getProbabilityEvaluator().getClass());
         assertEquals(0.1d,((FixedProbabilityEvaluator)be.getProbabilityEvaluator()).evaluate(null).getNumericValue(),0.01d);
 
         be = (BasicModelElement) m.getClassToElement().get(MultiplicationOperator.class);
         assertTrue(m.getElements().contains(be));
         assertEquals(MultiplicationOperator.class,be.getElementClass());
         assertEquals(AssociativityType.LEFT_TO_RIGHT,be.getAssociativity());
         assertEquals(0,be.getPrefix().size());
         assertEquals(0,be.getSuffix().size());
         assertEquals(0,be.getSeparator().size());
         assertNotNull(be.getPattern());
         assertNull(be.getValueField());
         assertNull(be.getSetupMethod());
         assertEquals(NumericProbabilityEvaluator.class,be.getProbabilityEvaluator().getClass());
         assertEquals(1d,((NumericProbabilityEvaluator)be.getProbabilityEvaluator()).evaluate(null).getNumericValue(),0.01d);
 
         be = (BasicModelElement) m.getClassToElement().get(SubstractionOperator.class);
         assertTrue(m.getElements().contains(be));
         assertEquals(SubstractionOperator.class,be.getElementClass());
         assertEquals(AssociativityType.LEFT_TO_RIGHT,be.getAssociativity());
         assertEquals(0,be.getPrefix().size());
         assertEquals(0,be.getSuffix().size());
         assertEquals(0,be.getSeparator().size());
         assertNotNull(be.getPattern());
         assertNull(be.getValueField());
         assertNull(be.getSetupMethod());
         assertEquals(NumericProbabilityEvaluator.class,be.getProbabilityEvaluator().getClass());
         assertEquals(0d,((NumericProbabilityEvaluator)be.getProbabilityEvaluator()).evaluate(null).getNumericValue(),0.01d);
 
         be = (BasicModelElement) m.getClassToElement().get(IntegerLiteral.class);
         assertTrue(m.getElements().contains(be));
         assertEquals(IntegerLiteral.class,be.getElementClass());
         assertEquals(AssociativityType.UNDEFINED,be.getAssociativity());
         assertEquals(0,be.getPrefix().size());
         assertEquals(0,be.getSuffix().size());
         assertEquals(0,be.getSeparator().size());
         assertNotNull(be.getPattern());
         assertNotNull(be.getValueField());
         assertNull(be.getSetupMethod());
 
         be = (BasicModelElement) m.getClassToElement().get(RealLiteral.class);
         assertTrue(m.getElements().contains(be));
         assertEquals(RealLiteral.class,be.getElementClass());
         assertEquals(AssociativityType.UNDEFINED,be.getAssociativity());
         assertEquals(0,be.getPrefix().size());
         assertEquals(0,be.getSuffix().size());
         assertEquals(0,be.getSeparator().size());
         assertNotNull(be.getPattern());
         assertNotNull(be.getValueField());
         assertNull(be.getSetupMethod());
 
         ce = (ComplexModelElement) m.getClassToElement().get(BinaryExpression.class);
         assertTrue(m.getElements().contains(ce));
         assertEquals(BinaryExpression.class,ce.getElementClass());
         assertEquals(3,ce.getContents().size());
         assertEquals(Position.AFTER,ce.getPositions().get(ce.getContents().get(2)).getPosition()[0]);
         assertEquals(ce.getContents().get(1),ce.getPositions().get(ce.getContents().get(2)).getMember());
         sc = ce.getContents().get(0);
         assertEquals("e1",sc.getField());
         assertEquals(false,sc.isOptional());
         assertEquals(false,sc.isId());
         assertNull(sc.getPrefix());
         assertNull(sc.getSuffix());
         assertNull(sc.getSeparator());
         assertEquals(NumericProbabilityEvaluator.class,sc.getProbabilityEvaluator().getClass());
         assertEquals(0.5d,((NumericProbabilityEvaluator)sc.getProbabilityEvaluator()).evaluate(null).getNumericValue(),0.01d);
 
         sc = ce.getContents().get(1);
         assertEquals("op",sc.getField());
         assertEquals(false,sc.isOptional());
         assertEquals(false,sc.isId());
         assertNull(sc.getPrefix());
         assertNull(sc.getSuffix());
         assertNull(sc.getSeparator());
 
         sc = ce.getContents().get(2);
         assertEquals("e2",sc.getField());
         assertEquals(false,sc.isOptional());
         assertEquals(false,sc.isId());
         assertNull(sc.getPrefix());
         assertNull(sc.getSuffix());
         assertNull(sc.getSeparator());
         assertEquals(false,ce.isFreeOrder());
         assertEquals(AssociativityType.UNDEFINED,ce.getAssociativity());
         assertEquals(CompositionType.UNDEFINED,ce.getComposition());
         assertEquals(0,ce.getPrefix().size());
         assertEquals(0,ce.getSuffix().size());
         assertEquals(0,ce.getSeparator().size());
         assertNull(ce.getSetupMethod());
         assertEquals(NumericProbabilityEvaluator.class,sc.getProbabilityEvaluator().getClass());
         assertEquals(0.8d,((NumericProbabilityEvaluator)sc.getProbabilityEvaluator()).evaluate(null).getNumericValue(),0.01d);
 
         se = (ChoiceModelElement) m.getClassToElement().get(LiteralExpression.class);
         assertTrue(m.getElements().contains(se));
         assertEquals(LiteralExpression.class,se.getElementClass());
         assertEquals(AssociativityType.UNDEFINED,se.getAssociativity());
         assertEquals(0,se.getPrefix().size());
         assertEquals(0,se.getSuffix().size());
         assertEquals(0,se.getSeparator().size());
         assertNull(se.getSetupMethod());
         assertTrue(m.getSubelements().get(m.getClassToElement().get(LiteralExpression.class)).contains(m.getClassToElement().get(IntegerLiteral.class)));
         assertTrue(m.getSubelements().get(m.getClassToElement().get(LiteralExpression.class)).contains(m.getClassToElement().get(RealLiteral.class)));
         assertTrue(m.getSuperelements().get(m.getClassToElement().get(IntegerLiteral.class)).equals(m.getClassToElement().get(LiteralExpression.class)));
         assertTrue(m.getSuperelements().get(m.getClassToElement().get(RealLiteral.class)).equals(m.getClassToElement().get(LiteralExpression.class)));
 
         ce = (ComplexModelElement) m.getClassToElement().get(ParenthesizedExpression.class);
         assertTrue(m.getElements().contains(ce));
         assertEquals(ParenthesizedExpression.class,ce.getElementClass());
         assertEquals(1,ce.getContents().size());
         sc = ce.getContents().get(0);
         assertEquals("e",sc.getField());
         assertEquals(false,sc.isOptional());
         assertEquals(false,sc.isId());
         assertNull(sc.getPrefix());
         assertNull(sc.getSuffix());
         assertNull(sc.getSeparator());
 
         assertEquals(false,ce.isFreeOrder());
         assertEquals(AssociativityType.UNDEFINED,ce.getAssociativity());
         assertEquals(CompositionType.UNDEFINED,ce.getComposition());
         assertEquals(1,ce.getPrefix().size());
         assertEquals("\\(",((RegExpPatternRecognizer)ce.getPrefix().get(0)).getRegExp());
         assertEquals(1,ce.getSuffix().size());
         assertEquals("\\)",((RegExpPatternRecognizer)ce.getSuffix().get(0)).getRegExp());
         assertEquals(0,ce.getSeparator().size());
         assertNull(ce.getSetupMethod());
 
         ce = (ComplexModelElement) m.getClassToElement().get(UnaryExpression.class);
         assertTrue(m.getElements().contains(ce));
         assertEquals(UnaryExpression.class,ce.getElementClass());
         assertEquals(2,ce.getContents().size());
         sc = ce.getContents().get(0);
         assertEquals("op",sc.getField());
         assertEquals(false,sc.isOptional());
         assertEquals(false,sc.isId());
         assertNull(sc.getPrefix());
         assertNull(sc.getSuffix());
         assertNull(sc.getSeparator());
 
         sc = ce.getContents().get(1);
         assertEquals("e",sc.getField());
         assertEquals(false,sc.isOptional());
         assertEquals(false,sc.isId());
         assertNull(sc.getPrefix());
         assertNull(sc.getSuffix());
         assertNull(sc.getSeparator());
 
         assertEquals(false,ce.isFreeOrder());
         assertEquals(AssociativityType.UNDEFINED,ce.getAssociativity());
         assertEquals(CompositionType.UNDEFINED,ce.getComposition());
         assertEquals(0,ce.getPrefix().size());
         assertEquals(0,ce.getSuffix().size());
         assertEquals(0,ce.getSeparator().size());
         assertNull(ce.getSetupMethod());
 
         be = (BasicModelElement) m.getClassToElement().get(MinusOperator.class);
         assertTrue(m.getElements().contains(be));
         assertEquals(MinusOperator.class,be.getElementClass());
         assertEquals(AssociativityType.UNDEFINED,be.getAssociativity());
         assertEquals(0,be.getPrefix().size());
         assertEquals(0,be.getSuffix().size());
         assertEquals(0,be.getSeparator().size());
         assertNotNull(be.getPattern());
         assertNull(be.getValueField());
         assertNull(be.getSetupMethod());
 
         be = (BasicModelElement) m.getClassToElement().get(PlusOperator.class);
         assertTrue(m.getElements().contains(be));
         assertEquals(PlusOperator.class,be.getElementClass());
         assertEquals(AssociativityType.UNDEFINED,be.getAssociativity());
         assertEquals(0,be.getPrefix().size());
         assertEquals(0,be.getSuffix().size());
         assertEquals(0,be.getSeparator().size());
         assertNotNull(be.getPattern());
         assertNull(be.getValueField());
         assertNull(be.getSetupMethod());
 
         se = (ChoiceModelElement) m.getClassToElement().get(BinaryOperator.class);
         assertTrue(m.getElements().contains(se));
         assertEquals(BinaryOperator.class,se.getElementClass());
         assertEquals(AssociativityType.LEFT_TO_RIGHT,se.getAssociativity());
         assertEquals(0,se.getPrefix().size());
         assertEquals(0,se.getSuffix().size());
         assertEquals(0,se.getSeparator().size());
         assertNull(se.getSetupMethod());
         assertTrue(m.getSubelements().get(m.getClassToElement().get(BinaryOperator.class)).contains(m.getClassToElement().get(AdditionOperator.class)));
         assertTrue(m.getSubelements().get(m.getClassToElement().get(BinaryOperator.class)).contains(m.getClassToElement().get(SubstractionOperator.class)));
         assertTrue(m.getSubelements().get(m.getClassToElement().get(BinaryOperator.class)).contains(m.getClassToElement().get(MultiplicationOperator.class)));
         assertTrue(m.getSubelements().get(m.getClassToElement().get(BinaryOperator.class)).contains(m.getClassToElement().get(DivisionOperator.class)));
         assertTrue(m.getSuperelements().get(m.getClassToElement().get(AdditionOperator.class)).equals(m.getClassToElement().get(BinaryOperator.class)));
         assertTrue(m.getSuperelements().get(m.getClassToElement().get(SubstractionOperator.class)).equals(m.getClassToElement().get(BinaryOperator.class)));
         assertTrue(m.getSuperelements().get(m.getClassToElement().get(DivisionOperator.class)).equals(m.getClassToElement().get(BinaryOperator.class)));
         assertTrue(m.getSuperelements().get(m.getClassToElement().get(MultiplicationOperator.class)).equals(m.getClassToElement().get(BinaryOperator.class)));
 
         se = (ChoiceModelElement) m.getClassToElement().get(Expression.class);
         assertTrue(m.getElements().contains(se));
         assertEquals(Expression.class,se.getElementClass());
         assertEquals(AssociativityType.UNDEFINED,se.getAssociativity());
         assertEquals(0,se.getPrefix().size());
         assertEquals(0,se.getSuffix().size());
         assertEquals(0,se.getSeparator().size());
         assertNull(se.getSetupMethod());
         assertTrue(m.getSubelements().get(m.getClassToElement().get(Expression.class)).contains(m.getClassToElement().get(UnaryExpression.class)));
         assertTrue(m.getSubelements().get(m.getClassToElement().get(Expression.class)).contains(m.getClassToElement().get(BinaryExpression.class)));
         assertTrue(m.getSubelements().get(m.getClassToElement().get(Expression.class)).contains(m.getClassToElement().get(ParenthesizedExpression.class)));
         assertTrue(m.getSuperelements().get(m.getClassToElement().get(UnaryExpression.class)).equals(m.getClassToElement().get(Expression.class)));
         assertTrue(m.getSuperelements().get(m.getClassToElement().get(BinaryExpression.class)).equals(m.getClassToElement().get(Expression.class)));
         assertTrue(m.getSuperelements().get(m.getClassToElement().get(ParenthesizedExpression.class)).equals(m.getClassToElement().get(Expression.class)));
 
         se = (ChoiceModelElement) m.getClassToElement().get(UnaryOperator.class);
         assertTrue(m.getElements().contains(se));
         assertEquals(UnaryOperator.class,se.getElementClass());
         assertEquals(AssociativityType.UNDEFINED,se.getAssociativity());
         assertEquals(0,se.getPrefix().size());
         assertEquals(0,se.getSuffix().size());
         assertEquals(0,se.getSeparator().size());
         assertNull(se.getSetupMethod());
         assertTrue(m.getSubelements().get(m.getClassToElement().get(UnaryOperator.class)).contains(m.getClassToElement().get(MinusOperator.class)));
         assertTrue(m.getSubelements().get(m.getClassToElement().get(UnaryOperator.class)).contains(m.getClassToElement().get(PlusOperator.class)));
         assertTrue(m.getSuperelements().get(m.getClassToElement().get(MinusOperator.class)).equals(m.getClassToElement().get(UnaryOperator.class)));
         assertTrue(m.getSuperelements().get(m.getClassToElement().get(PlusOperator.class)).equals(m.getClassToElement().get(UnaryOperator.class)));
 
     //        Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Maximum value has to be 0 or higher.", new Object[]{field.getName(), elementClass.getCanonicalName()});
     }
 
 
     @Test
     public void modelReadTest2() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(true);
         lg.setFilter(c);
         Model m = modelGen(Main.class);
         if (m == null)
             return;
         if (c.getCount()>0) {
             assertFalse(true);
             return;
         }
 
         assertEquals(7,m.getElements().size());
         ComplexModelElement ce;
         BasicModelElement be;
         ChoiceModelElement se;
 
         ElementMember sc;
         ElementMember scid;
         MultipleElementMember mc;
         be = (BasicModelElement) m.getClassToElement().get(Test1.class);
         assertTrue(m.getElements().contains(be));
         assertEquals(Test1.class,be.getElementClass());
         assertEquals(AssociativityType.NON_ASSOCIATIVE,be.getAssociativity());
         assertEquals(2,be.getPrefix().size());
             assertEquals("a1",((RegExpPatternRecognizer)be.getPrefix().get(0)).getRegExp());
             assertEquals("b1",((RegExpPatternRecognizer)be.getPrefix().get(1)).getRegExp());
         assertEquals(2,be.getSuffix().size());
             assertEquals("c1",((RegExpPatternRecognizer)be.getSuffix().get(0)).getRegExp());
             assertEquals("d1",((RegExpPatternRecognizer)be.getSuffix().get(1)).getRegExp());
         assertEquals(2,be.getSeparator().size());
             assertEquals("e",((RegExpPatternRecognizer)be.getSeparator().get(0)).getRegExp());
             assertEquals("a1",((RegExpPatternRecognizer)be.getSeparator().get(1)).getRegExp());
             assertEquals(((RegExpPatternRecognizer)be.getPrefix().get(0)).getRegExp(),((RegExpPatternRecognizer)be.getSeparator().get(1)).getRegExp());
         assertNotNull(be.getPattern());
         assertEquals("a", be.getValueField());
         assertNull(be.getSetupMethod());
 
 
         ce = (ComplexModelElement) m.getClassToElement().get(Main.class);
         assertTrue(m.getElements().contains(ce));
         assertEquals(Main.class,ce.getElementClass());
         assertEquals(1,ce.getIds().size());
         
         assertEquals(5,ce.getContents().size());
         mc = (MultipleElementMember) ce.getContents().get(0);
         assertEquals("tests1",mc.getField());
         assertEquals(CollectionType.LANGARRAY,mc.getCollection());
         assertEquals(0,mc.getMinimumMultiplicity());
         assertEquals(1000,mc.getMaximumMultiplicity());
         assertEquals(true,mc.isOptional());
         assertEquals(false,mc.isId());
         assertEquals(false,mc.isReference());
         assertEquals(2,mc.getPrefix().size());
             assertEquals("a",((RegExpPatternRecognizer)mc.getPrefix().get(0)).getRegExp());
             assertEquals("b",((RegExpPatternRecognizer)mc.getPrefix().get(1)).getRegExp());
         assertEquals(2,mc.getSuffix().size());
             assertEquals("c",((RegExpPatternRecognizer)mc.getSuffix().get(0)).getRegExp());
             assertEquals("d",((RegExpPatternRecognizer)mc.getSuffix().get(1)).getRegExp());
         assertEquals(2,mc.getSeparator().size());
             assertEquals("e",((RegExpPatternRecognizer)mc.getSeparator().get(0)).getRegExp());
             assertEquals("a",((RegExpPatternRecognizer)mc.getSeparator().get(1)).getRegExp());
             assertEquals(((RegExpPatternRecognizer)be.getSeparator().get(0)).getRegExp(),((RegExpPatternRecognizer)mc.getSeparator().get(0)).getRegExp());
 
         mc = (MultipleElementMember) ce.getContents().get(1);
         assertEquals("tests2",mc.getField());
         assertEquals(CollectionType.LIST,mc.getCollection());
         assertEquals(1,mc.getMinimumMultiplicity());
         assertEquals(-1,mc.getMaximumMultiplicity());
         assertEquals(false,mc.isOptional());
         assertEquals(false,mc.isId());
         assertEquals(false,mc.isReference());
         assertNull(mc.getPrefix());
         assertNull(mc.getSuffix());
         assertNull(mc.getSeparator());
 
         mc = (MultipleElementMember) ce.getContents().get(2);
         assertEquals("tests3",mc.getField());
         assertEquals(CollectionType.LIST,mc.getCollection());
         assertEquals(-1,mc.getMinimumMultiplicity());
         assertEquals(10,mc.getMaximumMultiplicity());
         assertEquals(false,mc.isOptional());
         assertEquals(false,mc.isId());
         assertEquals(true,mc.isReference());
         assertNull(mc.getPrefix());
         assertNull(mc.getSuffix());
         assertNull(mc.getSeparator());
 
         mc = (MultipleElementMember) ce.getContents().get(3);
         assertEquals("tests4",mc.getField());
         assertEquals(CollectionType.SET,mc.getCollection());
         assertEquals(-1,mc.getMinimumMultiplicity());
         assertEquals(-1,mc.getMaximumMultiplicity());
         assertEquals(false,mc.isOptional());
         assertEquals(false,mc.isId());
         assertEquals(false,mc.isReference());
         assertNull(mc.getPrefix());
         assertNull(mc.getSuffix());
         assertNull(mc.getSeparator());
 
         sc = ce.getContents().get(4);
         scid = ce.getIds().get(0);
         assertEquals(scid,sc);
         assertEquals("tests5",sc.getField());
         assertEquals(false,sc.isOptional());
         assertEquals(true,sc.isId());
         assertEquals(true,sc.isReference());
         assertNull(sc.getPrefix());
         assertNull(sc.getSuffix());
         assertNull(sc.getSeparator());
 
         assertEquals(false,ce.isFreeOrder());
         assertEquals(AssociativityType.UNDEFINED,ce.getAssociativity());
         assertEquals(CompositionType.UNDEFINED,ce.getComposition());
         assertEquals(0,ce.getPrefix().size());
         assertEquals(0,ce.getSuffix().size());
         assertEquals(0,ce.getSeparator().size());
         assertEquals(1,ce.getConstraintMethods().size());
         assertEquals("run",ce.getConstraintMethods().get(0));
         assertEquals("setup",ce.getSetupMethod());
 
         se = (ChoiceModelElement) m.getClassToElement().get(Test2.class);
         assertTrue(m.getElements().contains(se));
         assertEquals(Test2.class,se.getElementClass());
         assertEquals(AssociativityType.LEFT_TO_RIGHT,se.getAssociativity());
         assertEquals(2,se.getPrefix().size());
             assertEquals("a",((RegExpPatternRecognizer)se.getPrefix().get(0)).getRegExp());
             assertEquals("b",((RegExpPatternRecognizer)se.getPrefix().get(1)).getRegExp());
         assertEquals(2,se.getSuffix().size());
             assertEquals("c",((RegExpPatternRecognizer)se.getSuffix().get(0)).getRegExp());
             assertEquals("d",((RegExpPatternRecognizer)se.getSuffix().get(1)).getRegExp());
         assertEquals(1,se.getSeparator().size());
             assertEquals("e",((RegExpPatternRecognizer)se.getSeparator().get(0)).getRegExp());
         assertEquals(1,se.getConstraintMethods().size());
         assertEquals("run",se.getConstraintMethods().get(0));
 
         ce = (ComplexModelElement) m.getClassToElement().get(Test3.class);
         assertTrue(m.getElements().contains(ce));
         assertEquals(Test3.class,ce.getElementClass());
         assertEquals(true,ce.isFreeOrder());
         assertEquals(AssociativityType.LEFT_TO_RIGHT,ce.getAssociativity());
         assertEquals(CompositionType.EAGER,ce.getComposition());
         assertEquals(2,se.getPrefix().size());
             assertEquals("a",((RegExpPatternRecognizer)se.getPrefix().get(0)).getRegExp());
             assertEquals("b",((RegExpPatternRecognizer)se.getPrefix().get(1)).getRegExp());
         assertEquals(2,se.getSuffix().size());
             assertEquals("c",((RegExpPatternRecognizer)se.getSuffix().get(0)).getRegExp());
             assertEquals("d",((RegExpPatternRecognizer)se.getSuffix().get(1)).getRegExp());
         assertEquals(1,se.getSeparator().size());
             assertEquals("e",((RegExpPatternRecognizer)se.getSeparator().get(0)).getRegExp());
         assertEquals(1,se.getConstraintMethods().size());
         assertEquals("run",se.getConstraintMethods().get(0));
 
         se = (ChoiceModelElement) m.getClassToElement().get(Test4.class);
         assertTrue(m.getElements().contains(se));
         assertEquals(Test4.class,se.getElementClass());
         assertEquals(AssociativityType.RIGHT_TO_LEFT,se.getAssociativity());
         assertEquals(0,se.getPrefix().size());
         assertEquals(2,se.getSuffix().size());
             assertEquals("d",((RegExpPatternRecognizer)se.getSuffix().get(0)).getRegExp());
             assertEquals("c",((RegExpPatternRecognizer)se.getSuffix().get(1)).getRegExp());
         assertEquals(0,se.getSeparator().size());
         assertEquals(1,se.getConstraintMethods().size());
         assertTrue(se.getConstraintMethods().get(0).contains("run2"));
 
         ce = (ComplexModelElement) m.getClassToElement().get(Test5.class);
         assertTrue(m.getElements().contains(ce));
         assertEquals(Test5.class,ce.getElementClass());
         assertEquals(true,ce.isFreeOrder());
         assertEquals(AssociativityType.LEFT_TO_RIGHT,ce.getAssociativity());
         assertEquals(CompositionType.EAGER,ce.getComposition());
         assertEquals(0,ce.getPrefix().size());
         assertEquals(0,ce.getSuffix().size());
         assertEquals(1,ce.getSeparator().size());
             assertEquals("e",((RegExpPatternRecognizer)ce.getSeparator().get(0)).getRegExp());
         assertEquals(0,ce.getConstraintMethods().size());
 
 
         ce = (ComplexModelElement) m.getClassToElement().get(Test6.class);
         assertTrue(m.getElements().contains(ce));
         assertEquals(Test6.class,ce.getElementClass());
         assertEquals(1,ce.getContents().size());
 
         sc =  ce.getContents().get(0);
         assertEquals("two",sc.getField());
         assertEquals(false,sc.isOptional());
         assertEquals(false,sc.isId());
         assertEquals(false,sc.isReference());
         assertNull(sc.getPrefix());
         assertNull(sc.getSuffix());
         assertNull(sc.getSeparator());
 
         assertEquals(false,ce.isFreeOrder());
         assertEquals(AssociativityType.RIGHT_TO_LEFT,ce.getAssociativity());
         assertEquals(CompositionType.LAZY,ce.getComposition());
         assertEquals(0,ce.getPrefix().size());
         assertEquals(0,ce.getSuffix().size());
         assertEquals(0,ce.getSeparator().size());
         assertEquals(0,ce.getConstraintMethods().size());
 
         assertFalse(checkPrec(m,Main.class,Test1.class));
         assertFalse(checkPrec(m,Main.class,Test2.class));
         assertFalse(checkPrec(m,Main.class,Test3.class));
         assertFalse(checkPrec(m,Main.class,Test4.class));
         assertFalse(checkPrec(m,Main.class,Test5.class));
         assertFalse(checkPrec(m,Main.class,Test6.class));
 
         assertFalse(checkPrec(m,Test1.class,Main.class));
         assertFalse(checkPrec(m,Test1.class,Test2.class));
         assertFalse(checkPrec(m,Test1.class,Test3.class));
         assertFalse(checkPrec(m,Test1.class,Test4.class));
         assertFalse(checkPrec(m,Test1.class,Test5.class));
         assertFalse(checkPrec(m,Test1.class,Test6.class));
 
         assertFalse(checkPrec(m,Test2.class,Main.class));
         assertFalse(checkPrec(m,Test2.class,Test1.class));
         assertFalse(checkPrec(m,Test2.class,Test3.class));
         assertFalse(checkPrec(m,Test2.class,Test4.class));
         assertTrue(checkPrec(m,Test2.class,Test5.class));
         assertFalse(checkPrec(m,Test2.class,Test6.class));
 
         assertFalse(checkPrec(m,Test3.class,Main.class));
         assertFalse(checkPrec(m,Test3.class,Test1.class));
         assertFalse(checkPrec(m,Test3.class,Test2.class));
         assertFalse(checkPrec(m,Test3.class,Test4.class));
         assertTrue(checkPrec(m,Test3.class,Test5.class));
         assertFalse(checkPrec(m,Test3.class,Test6.class));
 
         assertFalse(checkPrec(m,Test4.class,Main.class));
         assertFalse(checkPrec(m,Test4.class,Test1.class));
         assertTrue(checkPrec(m,Test4.class,Test2.class));
         assertTrue(checkPrec(m,Test4.class,Test3.class));
         assertTrue(checkPrec(m,Test4.class,Test5.class));
         assertTrue(checkPrec(m,Test4.class,Test6.class));
 
         assertFalse(checkPrec(m,Test5.class,Main.class));
         assertFalse(checkPrec(m,Test5.class,Test1.class));
         assertFalse(checkPrec(m,Test5.class,Test2.class));
         assertFalse(checkPrec(m,Test5.class,Test3.class));
         assertFalse(checkPrec(m,Test5.class,Test4.class));
         assertFalse(checkPrec(m,Test5.class,Test6.class));
 
         assertFalse(checkPrec(m,Test6.class,Main.class));
         assertFalse(checkPrec(m,Test6.class,Test1.class));
         assertTrue(checkPrec(m,Test6.class,Test2.class));
         assertTrue(checkPrec(m,Test6.class,Test3.class));
         assertFalse(checkPrec(m,Test6.class,Test4.class));
         assertTrue(checkPrec(m,Test6.class,Test5.class));
 
    }
 
 
     @Test
     public void patternRecognizerTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(true);
         lg.setFilter(c);
         Model m = modelGen(BasicClass.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         if (c.getCount()>0) {
             assertFalse(true);
             return;
         }
         assertEquals(PatternRec.class,((BasicModelElement)m.getClassToElement().get(BasicClass.class)).getPattern().getClass());
         assertEquals("hi",((BasicModelElement)m.getClassToElement().get(BasicClass.class)).getPattern().getArg());
     }
     @Test
     public void modelWrongTest01() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass01.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(2,c.getCount());
         BasicModelElement be;
         be = (BasicModelElement) m.getClassToElement().get(WrongClass01.class);
         assertNull(be.getValueField());
     }
 
     @Test
     public void modelWrongTest02() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass02.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(3,c.getCount());
         BasicModelElement be;
         be = (BasicModelElement) m.getClassToElement().get(WrongClass02.class);
         assertNull(be.getSetupMethod());
     }
 
     @Test
     public void modelWrongTest02b() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass02b.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         BasicModelElement be;
         be = (BasicModelElement) m.getClassToElement().get(WrongClass02b.class);
         assertEquals(1,be.getConstraintMethods().size());
     }
     @Test
     public void modelWrongTest03() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass03.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
 
     }
 
     @Test
     public void modelWrongTest04() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass04.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         BasicModelElement be;
         be = (BasicModelElement) m.getClassToElement().get(WrongClass04.class);
         assertNull(m.getPrecedences().get(be));
     }
 
     @Test
     public void modelWrongTest05() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass05.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass05.class).getClass());
     }
 
     @Test
     public void modelWrongTest06() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass06.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass06.class).getClass());
     }
 
     @Test
     public void modelWrongTest07() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass07.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass07.class).getClass());
     }
 
     @Test
     public void modelWrongTest08() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass08.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass08.class).getClass());
     }
 
     @Test
     public void modelWrongTest09() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass09.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(-1,((MultipleElementMember)((ComplexModelElement)m.getClassToElement().get(WrongClass09.class)).getContents().get(0)).getMinimumMultiplicity());
     }
 
     @Test
     public void modelWrongTest10() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass10.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass10.class).getClass());
         assertEquals(-1,((MultipleElementMember)((ComplexModelElement)m.getClassToElement().get(WrongClass10.class)).getContents().get(0)).getMaximumMultiplicity());
     }
 
     @Test
     public void modelWrongTest11() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass11.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass11.class).getClass());
         assertEquals(3,((MultipleElementMember)((ComplexModelElement)m.getClassToElement().get(WrongClass11.class)).getContents().get(0)).getMinimumMultiplicity());
         assertEquals(-1,((MultipleElementMember)((ComplexModelElement)m.getClassToElement().get(WrongClass11.class)).getContents().get(0)).getMaximumMultiplicity());
     }
 
     @Test
     public void modelWrongTest12() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass12.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass12.class).getClass());
     }
 
     @Test
     public void modelCorrectTest13() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(CorrectClass13.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
     	//removed
         assertEquals(0,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(CorrectClass13.class).getClass());
     }
 
     @Test
     public void modelWrongTest14() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass14.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass14.class).getClass());
         assertNull(m.getClassToElement().get(WrongClass14.class).getSetupMethod());
     }
 
     @Test
     public void modelWrongTest15() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass15.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(2,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass15.class).getClass());
         assertNull(m.getClassToElement().get(WrongClass15.class).getSetupMethod());
     }
 
     @Test
     public void modelWrongTest16() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass16.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass16.class).getClass());
         assertNull(m.getClassToElement().get(WrongClass16.class).getSetupMethod());
     }
 
     @Test
     public void modelWrongTest17() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass17.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass17.class).getClass());
         assertNull(m.getPrecedences().get(m.getClassToElement().get(WrongClass17.class)));
         assertNull(m.getPrecedences().get(m.getClassToElement().get(WrongClass17A.class)));
     }
 
     @Test
     public void modelWrongTest18() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass18.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass18.class).getClass());
         assertNull(m.getPrecedences().get(m.getClassToElement().get(WrongClass18.class)));
         assertNull(m.getPrecedences().get(m.getClassToElement().get(WrongClass18A.class)));
     }
 
     @Test
     public void modelWrongTest19() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass19.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
         assertEquals(ComplexModelElement.class,m.getClassToElement().get(WrongClass19.class).getClass());
     }
 
     @Test
     public void modelWrongTest20() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass20.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongTest21() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass21.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongTest24() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass24.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongTest25() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass25.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongTest26() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass26.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongTest27() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass27.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongTest28() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass28.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongTest29() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass29.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongTest30() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass30.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongTest31() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass31.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongPatternTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClass23.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongEmptyTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(test.languages.emptymatchers.multiple.StartPoint.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(2,c.getCount());
     }
     
     @Test
     public void NotIModelTest() {
         JavaModelReader jmr = new JavaModelReader(WrongClass22.class);
         try {
             jmr.read();
         } catch (Exception ex) {
             assertEquals(ClassDoesNotExtendIModelException.class,ex.getClass());
             assertFalse(false);
             return;
         }
         assertFalse(true);
     }
 
     @Test
     public void modelWrongPositionTest1() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPosition1.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongPositionTest2() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPosition2.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongPositionTest3() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPosition3.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongPositionTest4() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPosition4.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
 
     @Test
     public void modelWrongPositionTest5() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPosition5.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
 
     @Test
     public void modelWrongPositionTest6() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPosition6.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(2,c.getCount());
     }
 
     @Test
     public void modelWrongPositionTest7() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPosition7.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void modelWrongPositionClashTest1() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPositionClash1.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(2,c.getCount());
     }
 
     @Test
     public void modelWrongPositionClashTest2() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPositionClash2.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(2,c.getCount());
     }
     @Test
     public void modelWrongPositionClashTest3() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPositionClash3.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(2,c.getCount());
     }
     @Test
     public void modelWrongPositionClashTest4() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPositionClash4.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(2,c.getCount());
     }
     @Test
     public void modelWrongPositionClashTest5() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(WrongClassPositionClash5.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(2,c.getCount());
     }
     
     @Test
     public void AbstractTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(AbstNoSubClasses.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void AbstractTest2() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(AbstNoSubClasses1.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
     
     @Test
     public void OptionalIDTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(OptionalID.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
     
     @Test
     public void IDNotIModelTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(IDNotIModel.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
     
     @Test
     public void ReferenceNotIModelTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(ReferenceNotIModel.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void EmptyPrefixTest() throws ParserException {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(test.languages.emptymatchers.prefix.StartPoint.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         Set<PatternRecognizer> se = new HashSet<PatternRecognizer>();
         se.add(new RegExpPatternRecognizer(" "));
         Parser<test.languages.emptymatchers.prefix.StartPoint> parser;
 		try {
 			parser = ParserFactory.create(m,se);
 		} catch (CannotCreateParserException e) {
             assertFalse(true);
             return;
 		}
 		try {
 			parser.parseAll("ab");
 			assertTrue(false);
 		} catch (ParserException e) {
 			
 		}
     }
 
     @Test
     public void EmptyPrefixTest2() throws ParserException {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(test.languages.emptymatchers.prefix2.StartPoint.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         Set<PatternRecognizer> se = new HashSet<PatternRecognizer>();
         se.add(new RegExpPatternRecognizer(" "));
         Parser<test.languages.emptymatchers.prefix2.StartPoint> parser;
 		try {
 			parser = ParserFactory.create(m,se);
 		} catch (CannotCreateParserException e) {
             assertFalse(true);
             return;
 		}
         Collection<test.languages.emptymatchers.prefix2.StartPoint> result = parser.parseAll("acb");
         assertEquals(1,result.size());
     }
 
 /*
     @Test
     public void EmptyMultipleTest() throws ParserException {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(test.languages.emptymatchers.multiple.StartPoint.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         Set<PatternRecognizer> se = new HashSet<PatternRecognizer>();
         se.add(new RegExpPatternRecognizer(" "));
         Parser<test.languages.emptymatchers.multiple.StartPoint> parser;
 		try {
 			parser = ParserFactory.create(m,se);
 		} catch (CannotCreateParserException e) {
             assertFalse(true);
             return;
 		}
 		try {
 			assertEquals(2,parser.parseAll("").size());
 			Collection<test.languages.emptymatchers.multiple.StartPoint> res = parser.parseAll("");
 			Iterator<test.languages.emptymatchers.multiple.StartPoint> ite = res.iterator();
 			test.languages.emptymatchers.multiple.StartPoint r0 = ite.next();
 			test.languages.emptymatchers.multiple.StartPoint r1 = ite.next();
 			assertNotNull(r0.content);
 			assertNotNull(r1.content);
 			System.out.println(r0.content.getClass());
 			System.out.println(r1.content.getClass());
 			
 		} catch (ParserException e) {
 			assertTrue(false);
 			
 		}
     }
 */
     @Test
     public void PrefixLoopTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(test.languages.emptymatchers.prefixloop.StartPoint.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
 
     @Test
     public void PrefixLoopTest2() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(test.languages.emptymatchers.prefixloop2.StartPoint.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
     
     @Test
     public void ReferenceNotIDTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(ReferenceNotID.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
     
     @Test
     public void ReferenceNotID2Test() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(ReferenceNotID2.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
     
 
     @Test
     public void AllOptionalTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(OptionalMain.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
     
     @Test
     public void MultOptionalTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         Model m = modelGen(OptionalMult.class);
         if (m == null) {
             assertFalse(true);
             return;
         }
         assertEquals(1,c.getCount());
     }
             
     @Test
     public void WarningExportHandlerTest() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         List<String> warnings = modelWarnings(OptionalMult.class);
         
         assertEquals(1,warnings.size());
     }    
     
     @Test
     public void WarningExportHandlerTest2() {
         Logger lg = Logger.getLogger(JavaModelReader.class.getName());
         CountFilter c = new CountFilter(false);
         lg.setFilter(c);
         List<String> warnings = modelWarnings(OptionalMult.class);
         List<String> warnings2 = modelWarnings(OptionalMult.class);
 
         
         assertEquals(1,warnings.size());
         assertEquals(1,warnings2.size());
     }
     
 
     @Test
     public void WarningExportHandlerTest3() {
         JavaModelReader jmr = new JavaModelReader(OptionalMult.class);
         try {
             jmr.read();
             jmr.read();
         } catch (Exception ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse(true);
         }
         assertEquals(1,jmr.getWarnings().size());
     }
     
     @Test
     public void SerializationTest() {
         JavaModelReader jmr = new JavaModelReader(OptionalMult.class);
         Model m = null;
 
         try {
             m = jmr.read();
             assertNotNull(Serialization.testSerialize(m));
         } catch (Exception ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Test
     public void CloningTest() {
         JavaModelReader jmr = new JavaModelReader(OptionalMult.class);
         Model m = null;
 
         try {
             m = jmr.read();
         } catch (Exception ex) {
             Logger.getLogger(JavaModelReaderTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         Model m2 = m.clone();
         assertNotSame(m.getClassToElement().get(OptionalMult.class),m2.getClassToElement().get(OptionalMult.class));
         assertNotSame(m.getClassToElement().get(OptionalPart.class),m2.getClassToElement().get(OptionalPart.class));
     }
  
 }
