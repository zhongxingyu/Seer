 package org.multiverse.integrationtests;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.multiverse.annotations.TransactionalMethod;
 import org.multiverse.annotations.TransactionalObject;
 
 import static org.junit.Assert.fail;
 import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
 
 /**
  * Test to make sure that the equals/hash/toString of a transactional object is not used
  * inside transactions.
  * <p/>
  * It tests for different transaction lengths because multiverse uses different transaction
  * implementations based on the best performing transaction length. So we want to make sure
  * that all of them are tested.
  *
  * @author Peter Veentjer
  */
 public class ToStringEqualsAndHashNotUsedTest {
 
     @Before
     public void setUp() {
         clearThreadLocalTransaction();
     }
 
     @Test
     public void test_1() {
         test(1);
     }
 
     @Test
     public void test_10() {
         test(10);
     }
 
     @Test
     public void test_100() {
         test(100);
     }
 
     @Test
     public void test_1000() {
         test(1000);
     }
 
     @Test
     public void test_10000() {
         test(10000);
     }
 
     @Test
    public void test_100000() {
         test(100000);
     }
 
    //todo: somehow this test is extremely slow using the ibm jdk.
    //needs further investigation
    //@Test

    public void test_1000000() {
         test(1000000);
     }
 
     public void test(int count) {
         Ref[] refs = new Ref[count];
         for (int k = 0; k < count; k++) {
             refs[k] = new Ref(0);
         }
 
         inc(refs);
     }
 
     @TransactionalMethod
     private void inc(Ref[] refs) {
         for (Ref ref : refs) {
             ref.set(ref.get() + 10);
         }
     }
 
     @TransactionalObject
     class Ref {
         private int value;
 
         public Ref(int value) {
             this.value = value;
         }
 
         public int get() {
             return value;
         }
 
         public void set(int value) {
             this.value = value;
         }
 
         public String toString() {
             fail();
             return null;
         }
 
         public boolean equals(Object item) {
             fail();
             return false;
         }
 
         public int hashCode() {
             fail();
             return 0;
         }
     }
 }
