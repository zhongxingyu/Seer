 package com.wiredmind.booleanengine.core;
 
 import org.apache.commons.chain.Context;
 import org.apache.commons.chain.impl.ContextBase;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Craig Earley
  */
 public class ValidatorAlgebraScenarioTest {
 
     Context context = new ContextBase();
 
     public ValidatorAlgebraScenarioTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     @SuppressWarnings("unchecked")
     public void setUp() throws ParseException {
         context.put("MID", "53120251526");
         context.put("TerminalID", "00000001");
         context.put("Date", (new SimpleDateFormat("dd/MM/yyyy")).parse("31/12/2009"));
         context.put("award", "");
     }
 
     @Test
     @SuppressWarnings({"deprecation", "unchecked"})
     public void testComplexPrewardExample_SuccessfullAwardUpdateWhenTransactionQualifiesFirstTier() {
 
         context.put("Amount", 49.99);
 
         List<Rule> rules = new ArrayList<Rule>();
        rules.add((new ValidatorRule("A", null, null, "In", "MID 53120251526, 53110201523")));
         rules.add((new ValidatorRule("B", null, null, "EQ", "TerminalID 00000001")));
         rules.add((new ValidatorRule("C", null, null, "In", "Date 6/10/2009, 29/10/2009, 3/11/2009, 26/11/2009, 01/12/2009, 31/12/2009")));
         rules.add((new ValidatorRule("D", null, null, "GE", "Amount 20.00"))); // minimum spend
         rules.add((new ValidatorRule("E", null, null, "GE", "Amount 50.00"))); // next tier
         rules.add((new ValidatorRule("F", null, null, "Award", "5.00")));
         rules.add((new ValidatorRule("G", null, null, "PercentAward", "20")));
 
         ValidatorAlgebra algebra = new DefaultValidatorAlgebra("A and B and C and E ? G : A and B and C and D and F", rules);
         try {
             algebra.execute(context);
             assertTrue(true == algebra.isTruthValue());
             String award = (String) context.get("award");
             assertEquals("5.00", award);
         } catch (Exception ex) {
             fail(ex.getMessage());
         }
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testComplexPrewardExample_SuccessfullAwardUpdateWhenTransactionQualifiesSecondTier() {
 
         context.put("Amount", 50.00);
 
         List<Rule> rules = new ArrayList<Rule>();
        rules.add((new ValidatorRule("A", null, null, "In", "MID 53120251526, 53110201523")));
         rules.add((new ValidatorRule("B", null, null, "EQ", "TerminalID 00000001")));
         rules.add((new ValidatorRule("C", null, null, "In", "Date 6/10/2009, 29/10/2009, 3/11/2009, 26/11/2009, 01/12/2009, 31/12/2009")));
         rules.add((new ValidatorRule("D", null, null, "GE", "Amount 20.00"))); // minimum spend
         rules.add((new ValidatorRule("E", null, null, "GE", "Amount 50.00"))); // next tier
         rules.add((new ValidatorRule("F", null, null, "Award", "5.00")));
         rules.add((new ValidatorRule("G", null, null, "PercentAward", "20")));
 
         ValidatorAlgebra algebra = new DefaultValidatorAlgebra("A and B and C and E ? G : A and B and C and D and F", rules);
         try {
             algebra.execute(context);
             assertTrue(true == algebra.isTruthValue());
             String award = (String) context.get("award");
             assertEquals(new BigDecimal(50 * .2).toString(), award);
         } catch (Exception ex) {
             fail(ex.getMessage());
         }
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testComplexPrewardExample_UnsuccessfullAwardUpdateWhenTransactionDoesNotQualify() {
 
         context.put("Amount", 100.00);
 
         List<Rule> rules = new ArrayList<Rule>();
        rules.add((new ValidatorRule("A", null, null, "In", "MID 53120251526, 53110201523")));
 
         // The following TID restriction disqualifies the transaction (see setUp() in this Test class)
         rules.add((new ValidatorRule("B", null, null, "EQ", "TerminalID 9999")));
 
         // So execution of the evaluator will stop here and non of the following rules will be evaluated
 
         rules.add((new ValidatorRule("C", null, null, "In", "Date 6/10/2009, 29/10/2009, 3/11/2009, 26/11/2009, 01/12/2009, 31/12/2009")));
         rules.add((new ValidatorRule("D", null, null, "GE", "Amount 20.00"))); // minimum spend
         rules.add((new ValidatorRule("E", null, null, "GE", "Amount 50.00"))); // next tier
         rules.add((new ValidatorRule("F", null, null, "Award", "5.00")));
         rules.add((new ValidatorRule("G", null, null, "PercentAward", "20")));
 
         ValidatorAlgebra algebra = new DefaultValidatorAlgebra("A and B and C and E ? G : A and B and C and D and F", rules);
         try {
             algebra.execute(context);
             assertTrue(false == algebra.isTruthValue());
             String award = (String) context.get("award");
             assertEquals("", award);
         } catch (Exception ex) {
             fail(ex.getMessage());
         }
     }
 }
