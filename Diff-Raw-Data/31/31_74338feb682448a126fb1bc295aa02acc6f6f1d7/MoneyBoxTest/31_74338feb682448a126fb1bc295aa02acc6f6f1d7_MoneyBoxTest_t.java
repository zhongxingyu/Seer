 package ua.in.dmitry404.money;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Set;
 
 import static org.junit.Assert.*;
 
 /**
  * This class contains unit-tests for MoneyBox
  * 
  * @author Dmitriy Butakov
  */
 public class MoneyBoxTest {
     private MoneyBox moneyBox;
 
     @Before
     public void setUp() {
         moneyBox = new MoneyBox();
     }
 
     @Test
     public void testDepositMoney() {
         String currency = "USD";
         int value = 100;
         int quantity = 10;
 
         moneyBox.deposit(currency, value, quantity);
 
         assertTrue(moneyBox.exists(currency));
     }
 
     @Test
     public void testNotExistingMoney() {
         String notExistingCurrency = "USD";
 
         assertFalse(moneyBox.exists(notExistingCurrency));
     }
 
     @Test
     public void testGetExistingMoneyValues() {
         String currency1 = "UAH";
         String currency2 = "USD";
 
         int expectedSize = 2;
 
         moneyBox.deposit(currency1, 1, 1);
         moneyBox.deposit(currency2, 1, 1);
 
         Set<String> currencies = moneyBox.getValues();
 
         assertEquals(expectedSize, currencies.size());
         assertTrue(currencies.contains(currency1));
         assertTrue(currencies.contains(currency2));
     }
 
     @Test
     public void testWithdrawMoney() {
         String currency = "USD";
         int value = 100;
         int quantity = 10;
         int valueToWithdraw = value * quantity;
 
         int expectedWithdrawQuantity = 10;
 
         moneyBox.deposit(currency, value, quantity);
         NotesHolder notesHolder = moneyBox.withdraw(currency, valueToWithdraw);
 
         assertFalse(moneyBox.exists(currency));
         assertEquals(expectedWithdrawQuantity, notesHolder.getQuantity(value));
     }
 
     @Test
     public void testWithdrawMoneyWithMod() {
         String currency = "USD";
         int value = 100;
         int quantity = 10;
         int valueToWithdraw = value * quantity - value;
 
         int expectedWithdrawQuantity = 9;
 
         moneyBox.deposit(currency, value, quantity);
         NotesHolder notesHolder = moneyBox.withdraw(currency, valueToWithdraw);
 
         assertTrue(moneyBox.exists(currency));
         assertEquals(expectedWithdrawQuantity, notesHolder.getQuantity(value));
     }
 
     @Test
    public void testWithdrawMoneyWithRightOrder() {
        String currency = "USD";
        int value1 = 50;
        int value2 = 100;
        int quantity1 = 2;
        int quantity2 = 2;

        int valueToWithdraw = 100;

        int expectedValueQuantity = 1;

        moneyBox.deposit(currency, value1, quantity1);
        moneyBox.deposit(currency, value2, quantity2);
        NotesHolder notesHolder = moneyBox.withdraw(currency, valueToWithdraw);

        assertTrue(moneyBox.exists(currency));
        assertEquals(expectedValueQuantity, notesHolder.getQuantity(value2));
    }

    @Test
     public void testFailWithdrawMoney() {
         String currency = "USD";
         int value = 100;
         int quantity = 10;
 
         moneyBox.deposit(currency, value, quantity);
         NotesHolder notesHolder = moneyBox.withdraw(currency, (value * quantity) + 1);
 
         assertTrue(moneyBox.exists(currency));
         assertNull(notesHolder);
     }
 
     @Test
     public void testGetNotesHolderByCurrencyCode() {
         String currency = "USD";
         int value = 100;
         int quantity = 10;
         
         moneyBox.deposit(currency, value, quantity);
 
         NotesHolder notesHolder = moneyBox.getNotesHolder(currency);
 
         assertEquals(quantity, notesHolder.getQuantity(value));
     }
 }
