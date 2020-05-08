 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 import homework.CreditCardNumberValidator;
 
 public class TestCreditCardNumberValidator {
 
     @Test
     public void testReturnsCorrectNumberOfDigits() {
         long size = CreditCardNumberValidator.getSize(1234);
         assertEquals(4, size);
     }
 
     @Test
     public void testReturnsCorrectPrefix() {
        assertEquals(12, CreditCardNumberValidator.getPrefix(1234567, 2));
        assertEquals(100, CreditCardNumberValidator.getPrefix(100, 5));
     }
 
     @Test
     public void testChecksValidPrefix() {
         boolean matched = CreditCardNumberValidator.prefixMatched(1234567, 1);
         assertEquals(matched, true);
 
         matched = CreditCardNumberValidator.prefixMatched(1234567, 9);
         assertEquals(matched, false);
     }
 
     @Test
     public void testReturnsSumOfOddPlaceNumbers() {
         assertEquals(41, CreditCardNumberValidator.sumOfOddPlace(4388576018410707L));
         assertEquals(33, CreditCardNumberValidator.sumOfOddPlace(371449635398431L));
         assertEquals(33, CreditCardNumberValidator.sumOfOddPlace(378282246310005L));
         assertEquals(5 , CreditCardNumberValidator.sumOfOddPlace(6011016011016011L));
         assertEquals(32, CreditCardNumberValidator.sumOfOddPlace(5454545454545454L));
         assertEquals(18, CreditCardNumberValidator.sumOfOddPlace(5111005111051128L));
     }
 
     @Test
     public void testGetDigitReturnsSumOfMoreThanSingleDigit() {
         int sum = CreditCardNumberValidator.getDigit(9);
         assertEquals(9, sum);
 
         sum = CreditCardNumberValidator.getDigit(16);
         assertEquals(7, sum);
 
         sum = CreditCardNumberValidator.getDigit(14);
         assertEquals(5, sum);
     }
 
     @Test
     public void testSumDoubleEvenPlaceNumbers() {
         assertEquals(37, CreditCardNumberValidator.sumOfDoubleEvenPlace(4388576018402626L));
         assertEquals(29, CreditCardNumberValidator.sumOfDoubleEvenPlace(4388576018410707L));
         assertEquals(47, CreditCardNumberValidator.sumOfDoubleEvenPlace(371449635398431L));
         assertEquals(27, CreditCardNumberValidator.sumOfDoubleEvenPlace(378282246310005L));
         assertEquals(15, CreditCardNumberValidator.sumOfDoubleEvenPlace(6011016011016011L));
         assertEquals(8,  CreditCardNumberValidator.sumOfDoubleEvenPlace(5454545454545454L));
         assertEquals(12, CreditCardNumberValidator.sumOfDoubleEvenPlace(5111005111051128L));
     }
 
     @Test
     public void testValidesCreditCardNumbers() {
         assertEquals(false, CreditCardNumberValidator.isValid(4388576018402626L));
         assertEquals(true , CreditCardNumberValidator.isValid(4388576018410707L));
         assertEquals(true , CreditCardNumberValidator.isValid(378282246310005L));
        assertEquals(true , CreditCardNumberValidator.isValid(371449635398431L));
        assertEquals(true , CreditCardNumberValidator.isValid(5111005111051128L));
        assertEquals(true , CreditCardNumberValidator.isValid(6011000995500000L));
     }
 }
