 package org.multiverse.stms.alpha.instrumentation.fieldaccess;
 
 import org.junit.After;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.multiverse.annotations.TransactionalObject;
 import org.multiverse.stms.alpha.AlphaStm;
 import org.multiverse.stms.alpha.AlphaTransactionalObject;
 import org.multiverse.transactional.primitives.TransactionalInteger;
 
 import java.util.concurrent.atomic.AtomicLong;
 
 import static org.junit.Assert.*;
 import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
 
 /**
  * @author Peter Veentjer
  */
 public class TransactionalObject_ConstructorTest {
 
     private AlphaStm stm;
 
     @Before
     public void setUp() {
         stm = (AlphaStm) getGlobalStmInstance();
     }
 
     @After
     public void tearDown() {
         //assertNoInstrumentationProblems();
     }
 
     @Test
    @Ignore
     public void testConstructorWithReadFirst() {
         ConstructorWithReadFirst object = new ConstructorWithReadFirst();
         assertEquals(0, object.getField());
     }
 
     @TransactionalObject
     public class ConstructorWithReadFirst {
 
         private int field;
 
         public ConstructorWithReadFirst() {
             this.field = field * 2;
         }
 
         public int getField() {
             return field;
         }
     }
 
     @Test
     public void testNoArgConstructor() {
         NoArgConstructor o = new NoArgConstructor();
         assertEquals(10, o.getValue());
     }
 
     @TransactionalObject
     static class NoArgConstructor {
 
         private int value;
 
         public NoArgConstructor() {
             value = 10;
         }
 
         public int getValue() {
             return value;
         }
     }
 
     @Test
     public void testEmptyConstructor() {
         long version = stm.getVersion();
 
         testEmptyConstructor o = new testEmptyConstructor();
 
         assertEquals(version, stm.getVersion());
     }
 
     @TransactionalObject
     static class testEmptyConstructor {
 
         private int value;
 
         public testEmptyConstructor() {
         }
     }
 
     @Test
     public void testNoConstructor() {
         long version = stm.getVersion();
 
         NoConstructor noConstructor = new NoConstructor();
 
         assertEquals(version, stm.getVersion());
     }
 
     @TransactionalObject
     static class NoConstructor {
 
         private int value;
     }
 
     @Test
     public void testOneArgConstructor() {
         long version = stm.getVersion();
 
         OneArgConstructor o = new OneArgConstructor(10);
 
         assertEquals(version, stm.getVersion());
         assertEquals(10, o.getA());
     }
 
     @TransactionalObject
     static class OneArgConstructor {
 
         private int a;
 
         public OneArgConstructor(int a) {
             this.a = a;
         }
 
         public int getA() {
             return a;
         }
     }
 
     @Test
     public void testTwoArgConstructor() {
         long version = stm.getVersion();
 
         TwoArgConstructor o = new TwoArgConstructor(20, 8);
 
         assertEquals(version, stm.getVersion());
         assertEquals(20, o.getA());
         assertEquals(8, o.getB());
     }
 
     @TransactionalObject
     static class TwoArgConstructor {
 
         private int a;
         private int b;
 
         public TwoArgConstructor(int a, int b) {
             this.a = a;
             this.b = b;
         }
 
         public int getA() {
             return a;
         }
 
         public int getB() {
             return b;
         }
     }
 
     @Test
     public void testFiveArgConstructor() {
         long version = stm.getVersion();
 
         FiveArgConstructor fiveArgConstructor = new FiveArgConstructor(10, 40, 2, 9, -1);
 
         assertEquals(version, stm.getVersion());
         assertEquals(10, fiveArgConstructor.getA());
         assertEquals(40, fiveArgConstructor.getB());
         assertEquals(2, fiveArgConstructor.getC());
         assertEquals(9, fiveArgConstructor.getD());
         assertEquals(-1, fiveArgConstructor.getE());
     }
 
     @TransactionalObject
     static class FiveArgConstructor {
 
         private int a;
         private int b;
         private int c;
         private int d;
         private int e;
 
         public FiveArgConstructor(int a, int b, int c, int d, int e) {
             this.a = a;
             this.b = b;
             this.c = c;
             this.d = d;
             this.e = e;
         }
 
         public int getA() {
             return a;
         }
 
         public int getB() {
             return b;
         }
 
         public int getC() {
             return c;
         }
 
         public int getD() {
             return d;
         }
 
         public int getE() {
             return e;
         }
     }
 
     @Test
     public void testVarArgsConstructor() {
         long version = stm.getVersion();
 
         VarArgConstructor varArgConstructor = new VarArgConstructor(1, 2, 3, 4);
 
         assertEquals(version, stm.getVersion());
         assertArrayEquals(new int[]{1, 2, 3, 4}, varArgConstructor.getArgs());
     }
 
     @TransactionalObject
     static class VarArgConstructor {
 
         private int[] args;
 
         VarArgConstructor(int... args) {
             this.args = args;
         }
 
         public int[] getArgs() {
             return args;
         }
     }
 
     @Test
     public void testArrayConstructor() {
         long version = stm.getVersion();
 
         int[] args = new int[]{1, 2, 3, 4};
         ArrayConstructor o = new ArrayConstructor(args);
 
         assertEquals(version, stm.getVersion());
         assertSame(args, o.getArgs());
     }
 
     @TransactionalObject
     static class ArrayConstructor {
 
         private int[] args;
 
         ArrayConstructor(int[] args) {
             this.args = args;
         }
 
         public int[] getArgs() {
             return args;
         }
     }
 
     @Test
     public void testTransactionalObjectConstructor() {
         TransactionalInteger intValue = new TransactionalInteger(10);
 
         long version = stm.getVersion();
 
         TransactionalObjectConstructor o = new TransactionalObjectConstructor(intValue);
 
         assertEquals(version, stm.getVersion());
         assertSame(intValue, o.getIntValue());
     }
 
     @TransactionalObject
     static class TransactionalObjectConstructor {
 
         TransactionalInteger intValue;
 
         TransactionalObjectConstructor(TransactionalInteger intValue) {
             this.intValue = intValue;
         }
 
         public TransactionalInteger getIntValue() {
             return intValue;
         }
     }
 
     @Test
     public void testNormalRefConstructor() {
         String value = "foo";
 
         long version = stm.getVersion();
 
         NormalRefConstructor o = new NormalRefConstructor(value);
 
         assertEquals(version, stm.getVersion());
         assertSame(value, o.getValue());
     }
 
     @TransactionalObject
     static class NormalRefConstructor {
 
         String value;
 
         NormalRefConstructor(String intValue) {
             this.value = intValue;
         }
 
         public String getValue() {
             return value;
         }
     }
 
     @Test
     public void testCreateTxObjectInConstructor() {
         long startVersion = stm.getVersion();
 
         CreateTxObjectInConstructor o = new CreateTxObjectInConstructor(10);
 
         assertEquals(startVersion, stm.getVersion());
         assertNotNull(o.getValue());
     }
 
     @TransactionalObject
     static class CreateTxObjectInConstructor {
 
         private TransactionalInteger value;
 
         CreateTxObjectInConstructor(int i) {
             this.value = new TransactionalInteger(i);
         }
 
         public TransactionalInteger getValue() {
             return value;
         }
     }
 
     @Test
     public void testExceptionIsThrownInsideConstructor() {
         long version = stm.getVersion();
 
         try {
             new ExceptionIsThrownInsideConstructor();
             fail();
         } catch (MyException ex) {
 
         }
 
         assertEquals(version, stm.getVersion());
     }
 
     @TransactionalObject
     static class ExceptionIsThrownInsideConstructor {
 
         private int field;
 
         ExceptionIsThrownInsideConstructor() {
             throw new MyException();
         }
     }
 
     static class MyException extends RuntimeException {
 
     }
 
     @Test
     public void constructorWithExplicitSuperCallingConstructor() {
         long version = stm.getVersion();
 
         TxObjectWithNoArgSuperCallingConstructor o = new TxObjectWithNoArgSuperCallingConstructor();
 
         assertEquals(version, stm.getVersion());
     }
 
     @TransactionalObject
     static class TxObjectWithNoArgSuperCallingConstructor {
 
         private int value;
 
         TxObjectWithNoArgSuperCallingConstructor() {
             super();
         }
     }
 
     @Test
     public void constructorWithSuperCallingConstructor() {
         ConstructorThatCallsSuper object = new ConstructorThatCallsSuper(10);
         assertEquals(10, object.value);
     }
 
     static class Super {
 
         int value;
 
         Super(int value) {
             this.value = value;
         }
     }
 
     @TransactionalObject
     static class ConstructorThatCallsSuper extends Super {
 
         public ConstructorThatCallsSuper(int value) {
             super(value);
         }
     }
 
 
     @Test
     public void constructorThatCallsThis() {
         ThisCallingConstructor c = new ThisCallingConstructor();
         assertEquals(25, c.getValue());
     }
 
     @TransactionalObject
     static class ThisCallingConstructor {
 
         int value;
 
         public ThisCallingConstructor() {
             this(25);
         }
 
         public ThisCallingConstructor(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
 
         public void setValue(int value) {
             this.value = value;
         }
     }
 
     //test inner classes
 
     // ============================== finals ==========================
 
     @Test
     public void finalFieldOnTxObject() {
         long version = stm.getVersion();
 
         TxObjectWithFinalField o = new TxObjectWithFinalField(10);
         assertFalse(o instanceof AlphaTransactionalObject);
         assertEquals(version, stm.getVersion());
         assertEquals(10, o.value);
     }
 
     @TransactionalObject
     static class TxObjectWithFinalField {
 
         final int value;
 
         TxObjectWithFinalField(int value) {
             this.value = value;
         }
     }
 
     @Test
     public void finalFieldIsAtomicObject() {
         TransactionalInteger value = new TransactionalInteger(100);
 
         long version = stm.getVersion();
 
         TxObjectWithOtherTxObjectAsFinalField o = new TxObjectWithOtherTxObjectAsFinalField(value);
         assertFalse(o instanceof AlphaTransactionalObject);
         assertEquals(version, stm.getVersion());
         assertEquals(value, o.value);
     }
 
     @TransactionalObject
     static class TxObjectWithOtherTxObjectAsFinalField {
 
         final TransactionalInteger value;
 
         TxObjectWithOtherTxObjectAsFinalField(TransactionalInteger value) {
             this.value = value;
         }
     }
 
     @Test
     public void finalFieldIsTxObjectAndCreatedInConstructor() {
         long version = stm.getVersion();
 
         TxObjectWithFinalRefThatIsCreatedInsideConstructor o = new TxObjectWithFinalRefThatIsCreatedInsideConstructor();
         assertFalse(o instanceof AlphaTransactionalObject);
         assertEquals(10, o.value.get());
         assertEquals(version, stm.getVersion());
     }
 
     @TransactionalObject
     static class TxObjectWithFinalRefThatIsCreatedInsideConstructor {
 
         final TransactionalInteger value;
 
         public TxObjectWithFinalRefThatIsCreatedInsideConstructor() {
             value = new TransactionalInteger(10);
         }
     }
 
     @Test
     public void testStaticInitializer() {
         ObjectWithStaticInitializer o = new ObjectWithStaticInitializer(10);
 
         assertEquals(100, ObjectWithStaticInitializer.staticValue);
         assertEquals(10, o.getValue());
     }
 
     @Test
     public void testInstanceInitializer() {
         constructorInitCounter.set(0);
 
         InstanceInitializer foo = new InstanceInitializer();
 
         //if the executors has executed correct, the counter should be 1.
         assertEquals(1, constructorInitCounter.get());
         //and the value should be the oldvalue+1
         assertEquals(1, foo.getInitCounter());
 
         //if the constructor code is executed again, the initCounter is increased
         //and this is not what we want. The reset counter should not change anymore
         assertEquals(1, constructorInitCounter.get());
     }
 
     private static final AtomicLong constructorInitCounter = new AtomicLong();
 
     @TransactionalObject
     public static class InstanceInitializer {
 
         long initCounter = constructorInitCounter.incrementAndGet();
 
         public InstanceInitializer() {
         }
 
         public long getInitCounter() {
             return initCounter;
         }
     }
 
 }
