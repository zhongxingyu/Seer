 /**
 *   TestCARules.java
 *
 *   Copyright (c) 2013 Vladimir Brigant
 *   This software is distributed under the terms of the GNU General Public License.
 */
 
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 import static org.junit.Assert.assertEquals;
 
 import cassp.ca.*;
 import cassp.data.*;
 import cassp.config.*;
 import cassp.ca.rules.*;
 
 
 
 public class TestCARules extends TestCase {
 
     public static Test suite() {
         return new TestSuite(TestCARules.class);
     }
 
     public static void main(String[] args) {
         TestRunner.run(suite());
     }
 
     private CASimpleRule simpleRule;
     private CAConformRule conformRule;
     private CellularAutomaton ca;
     private Data data;
     private SimConfig config;
     private DataItem di;
 
 
     public void setUp() {
         this.data = new Data();
         this.config = new SimConfig();
         this.di = new DataItem();
     }
 
     public void tearDown() {
         this.simpleRule = null;
         this.data = null;
         this.config = null;
         this.di = null;
         this.config = null;
         this.ca = null;
     }
 
     public void testSimpleRuleNeigh1(){
         this.config.setNeigh(1);
 
         di.setAaSeq("CRM");
         data.add(di);
         this.data.loadChouFasman("./src/cassp/tests/test.cf");
         this.ca = new CellularAutomaton(di, this.config);
 
         this.simpleRule = new CASimpleRule(1);
         this.simpleRule.setSteps(1);
 
         double[] weights = new double[3];
         weights[0] = 1; weights[1] = 1; weights[2] = 1;
 
         this.simpleRule.setWeights(weights);
         String seq = this.ca.run(this.simpleRule, this.data);
 
         assertEquals(seq, "CEC");
         assertEquals(93.333, this.ca.getCell(1).getHelixProps(), 0.1);
         assertEquals(156.666, this.ca.getCell(1).getSheetProps(), 0.1);
         assertEquals(116.666, this.ca.getCell(1).getCoilProps(), 0.1);
     }
 
     public void testConformRuleNeigh1(){
         this.config.setNeigh(1);
 
         di.setAaSeq("CRM");
         data.add(di);
         this.data.loadChouFasman("./src/cassp/tests/test.cf");
         this.data.loadConformCoeffs("./src/cassp/tests/test.cc");
         this.ca = new CellularAutomaton(di, this.config);
 
         this.conformRule = new CAConformRule(1);
         this.conformRule.setSteps(1);
 
         double[] weights = new double[3];
         weights[0] = 1; weights[1] = 1; weights[2] = 1;
 
         this.conformRule.setWeights(weights);
         this.conformRule.setAlpha(0.01);
         this.conformRule.setBeta(1);
         this.conformRule.setGamma(1);
 
 
         String seq = this.ca.run(this.conformRule, this.data);
        assertEquals("CCC", seq);
 
        assertEquals(2.245, this.ca.getCell(0).getHelixProps(), 0.1);
        assertEquals(3.97186, this.ca.getCell(0).getSheetProps(), 0.1);
        assertEquals(4.09224, this.ca.getCell(0).getCoilProps(), 0.1);
 
        assertEquals(3.0546, this.ca.getCell(1).getHelixProps(), 0.1);
        assertEquals(3.0866, this.ca.getCell(1).getSheetProps(), 0.1);
        assertEquals(3.1728, this.ca.getCell(1).getCoilProps(), 0.1);
 
        assertEquals(2.8989, this.ca.getCell(2).getHelixProps(), 0.1);
        assertEquals(2.655, this.ca.getCell(2).getSheetProps(), 0.1);
        assertEquals(3.4326, this.ca.getCell(2).getCoilProps(), 0.1);
     }
 }
