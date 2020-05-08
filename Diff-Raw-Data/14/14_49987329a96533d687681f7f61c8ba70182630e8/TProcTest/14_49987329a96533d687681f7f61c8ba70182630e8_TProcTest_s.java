 package org.shnell.universalCalculator.tests;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.shnell.universalCalculator.numbers.TComplex;
 import org.shnell.universalCalculator.numbers.TFraction;
 import org.shnell.universalCalculator.numbers.TPNumber;
 import org.shnell.universalCalculator.TProc;
 
 import static org.junit.Assert.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pavel
  * Date: 06.11.13
  * Time: 19:06
  * To change this template use File | Settings | File Templates.
  */
 public class TProcTest {
   TProc proc;
   @Before
   public void setUp() throws Exception {
     proc = new TProc();
   }
 
   @Test
   public void testComplexPNum() throws Exception {
     proc.setlOpRes(new TPNumber(2.0, 10, 1));
     assertEquals(new TPNumber(2.0, 10, 1), proc.getlOpRes());
 
     proc.setOperation(TProc.TOprtn.Add);
     proc.setrOp(new TPNumber(3.0, 10, 1));
     assertEquals(new TPNumber(3.0, 10, 1), proc.getrOp());
 
     proc.runOperation();
     assertEquals(new TPNumber(5.0, 10, 1), proc.getlOpRes());
 
     proc.setOperation(TProc.TOprtn.Mul);
     proc.setrOp(new TPNumber(4.0, 10, 1));
     assertEquals(new TPNumber(4.0, 10, 1), proc.getrOp());
 
     proc.runFunc(TProc.TFunc.Sqr);
     assertEquals(new TPNumber(16.0, 10, 1), proc.getrOp());
 
     proc.runOperation();
     assertEquals("80.0", ""+proc.getlOpRes());
 
     proc.reset();
    assertEquals(new TPNumber(), proc.getlOpRes());
    assertEquals(new TPNumber(), proc.getrOp());
   }
 
   @Test
   public void testComplexFrac() throws Exception {
     proc.setlOpRes(new TFraction(2L));
     assertEquals(new TFraction(2L), proc.getlOpRes());
 
     proc.setOperation(TProc.TOprtn.Add);
     proc.setrOp(new TFraction(3L));
     assertEquals(new TFraction(3L), proc.getrOp());
 
     proc.runOperation();
     assertEquals(new TFraction(5L), proc.getlOpRes());
 
     proc.setOperation(TProc.TOprtn.Mul);
     proc.setrOp(new TFraction(4L));
     assertEquals(new TFraction(4L), proc.getrOp());
 
     proc.runFunc(TProc.TFunc.Sqr);
     assertEquals(new TFraction(16L), proc.getrOp());
 
     proc.runOperation();
     assertEquals("80/1", ""+proc.getlOpRes());
 
     proc.reset();
    assertEquals(new TFraction(), proc.getlOpRes());
    assertEquals(new TFraction(), proc.getrOp());
   }
 
   @Test
   public void testComplexComplex() throws Exception {
     proc.setlOpRes(new TComplex(2.0));
     assertEquals(new TComplex(2.0), proc.getlOpRes());
 
     proc.setOperation(TProc.TOprtn.Add);
     proc.setrOp(new TComplex(3.0));
     assertEquals(new TComplex(3.0), proc.getrOp());
 
     proc.runOperation();
     assertEquals(new TComplex(5.0), proc.getlOpRes());
 
     proc.setOperation(TProc.TOprtn.Mul);
     proc.setrOp(new TComplex(4.0));
     assertEquals(new TComplex(4.0), proc.getrOp());
 
     proc.runFunc(TProc.TFunc.Sqr);
     assertEquals(new TComplex(16.0), proc.getrOp());
 
     proc.runOperation();
     assertEquals("80.0,i*0.0", ""+proc.getlOpRes());
 
     proc.reset();
    assertEquals(new TComplex(), proc.getlOpRes());
    assertEquals(new TComplex(), proc.getrOp());
   }
 }
