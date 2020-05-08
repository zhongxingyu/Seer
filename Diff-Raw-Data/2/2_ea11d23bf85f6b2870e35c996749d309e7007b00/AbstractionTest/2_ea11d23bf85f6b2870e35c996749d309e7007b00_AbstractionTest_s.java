 package languish.base;
 
 import static languish.base.Terms.*;
 import static languish.tools.testing.TestUtil.*;
 import junit.framework.TestCase;
 import languish.base.Term;
 import languish.tools.testing.LanguishTestCase;
 import languish.tools.testing.TestUtil;
 import languish.util.PrimitiveTree;
 
 public class AbstractionTest extends TestCase {
   public enum Tests implements LanguishTestCase {
     BASIC_APPLY( // 
         app(IDENT, primitive(FIVE)),
         "[APP [ABS [REF 1 NULL] NULL] [PRIMITIVE 5 NULL]]",
         primitive(FIVE),
         PrimitiveTree.of(FIVE)),

     ARGUMENT_CHOOSER_1( //
         app(app(TRUE, primitive(FOUR)), primitive(FIVE)),
         "[APP [APP [ABS [ABS [REF 2 NULL] NULL] NULL] "
             + "[PRIMITIVE 4 NULL]] [PRIMITIVE 5 NULL]]",
         app(abs(primitive(FOUR)), primitive(FIVE)),
         PrimitiveTree.of(FOUR)),
 
     ARGUMENT_CHOOSER_2( //
         app(app(Terms.FALSE, primitive(TestUtil.FOUR)), primitive(FIVE)),
         "[APP [APP [ABS [ABS [REF 1 NULL] NULL] NULL] [PRIMITIVE 4 NULL]] "
             + "[PRIMITIVE 5 NULL]]",
         app(abs(ref(1)), primitive(FIVE)),
         PrimitiveTree.of(FIVE)),
 
     NON_HALTER( //
         TestUtil.LOOP,
         "[APP [ABS [APP [REF 1 NULL] [REF 1 NULL]] NULL] "
             + "[ABS [APP [REF 1 NULL] [REF 1 NULL]] NULL]]",
         TestUtil.LOOP,
         null),
 
     IRRELEVANT_NON_HALTER( //
         app(app(Terms.TRUE, primitive(TestUtil.FOUR)), TestUtil.LOOP),
         "[APP [APP [ABS [ABS [REF 2 NULL] NULL] NULL] [PRIMITIVE 4 NULL]] "
             + "[APP [ABS [APP [REF 1 NULL] [REF 1 NULL]] NULL] "
             + "[ABS [APP [REF 1 NULL] [REF 1 NULL]] NULL]]]",
         app(abs(primitive(FOUR)), TestUtil.LOOP),
         PrimitiveTree.of(FOUR)),
 
     ;
 
     private final Term expression;
     private final String code;
     private final Term reducedOnce;
     private final PrimitiveTree reducedCompletely;
 
     private Tests(Term expression, String code, Term reducedOnce,
         PrimitiveTree reducedCompletely) {
       this.expression = expression;
       this.code = code;
       this.reducedOnce = reducedOnce;
       this.reducedCompletely = reducedCompletely;
     }
 
     public Term getExpression() {
       return expression;
     }
 
     public String getCode() {
       return code;
     }
 
     public Term getReducedOnce() {
       return reducedOnce;
     }
 
     public PrimitiveTree getReducedCompletely() {
       return reducedCompletely;
     }
   }
 
   public void test() {
     for (LanguishTestCase test : Tests.values()) {
       TestUtil.assertLanguishTestCase(test);
     }
   }
 }
 //
 
 //
 // public void testRelevantNonHalterFunction() {
 // Term exp =
 // TestUtil.reduceTupleOnce(Lambda.app(Lambda.app(SECOND_PICKER, Lambda
 // .primitive(FOUR)), LOOP));
 //
 // assertEquals(LOOP, TestUtil.reduceTupleOnce(exp));
 // }
 //
 // public void testNonHalter() {
 // assertEquals(LOOP, TestUtil.reduceTupleOnce(LOOP));
 // }
 // }
