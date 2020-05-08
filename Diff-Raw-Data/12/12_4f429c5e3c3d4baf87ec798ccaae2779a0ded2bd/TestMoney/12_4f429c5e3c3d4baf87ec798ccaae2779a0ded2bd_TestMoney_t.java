 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class TestMoney
 {
     public void testMultiplication()
     {
         Money five = Money.dollar(5);
         assertEquals(Money.dollar(10), five.times(2));
         assertEquals(Money.dollar(15), five.times(3));
     }
     
     public void testFrancMultiplication()
     {
        Money five = Money.franc(5);
         assertEquals(Money.franc(10), five.times(2));
         assertEquals(Money.franc(15), five.times(3));
     }
 
     public void testEquality()
     {
         assertTrue(Money.dollar(5).equals(Money.dollar(5)));
         assertFalse(Money.dollar(5).equals(Money.dollar(6)));
         assertTrue(Money.franc(5).equals(Money.franc(5)));
         assertFalse(Money.franc(5).equals(Money.franc(6)));
         assertFalse(Money.franc(5).equals(Money.dollar(5)));
     }
 
     public static void main(String[] args)
     {
        TestMoney testMoney = new TestMoney();
         
         // Tests.
        testMoney.testMultiplication();
        testMoney.testEquality();
        testMoney.testFrancMultiplication();
     }
 }
 
