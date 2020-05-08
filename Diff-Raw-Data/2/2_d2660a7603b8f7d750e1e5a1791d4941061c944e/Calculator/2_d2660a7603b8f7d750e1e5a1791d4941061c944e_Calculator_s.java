 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package exchangeRate;
 
 /**
  *
  * @author ronald
  */
 public class Calculator {
     private ExchangeRate firstExchangeRate;
     private ExchangeRate secondExchangeRate;
     
     // constructor has default visibility
     Calculator(ExchangeRate firstExchangeRate, ExchangeRate secondExchangeRate) {
         assert(firstExchangeRate.getFirstCurrency().equals(secondExchangeRate.getSecondCurrency()));
         assert(firstExchangeRate.getSecondCurrency().equals(secondExchangeRate.getFirstCurrency()));
         this.firstExchangeRate = firstExchangeRate;
         this.secondExchangeRate = secondExchangeRate;
     }
     
    public CurrencyValue convert(CurrencyValue from, Currency to) throws CalculatorException {
         boolean directionEqualToExchangeRate = getExchangeDirection(to);
         verifyCurrencyFrom(directionEqualToExchangeRate, from);
         
         // we're set, lets convert!
         double newValue;
         if (directionEqualToExchangeRate) {
             newValue = from.getValue() / firstExchangeRate.getExchangeRate();
         }
         else {
             newValue = from.getValue() / secondExchangeRate.getExchangeRate();
         }
         
         return new CurrencyValue(to, newValue);
     }
 
     /**
      * Verifies that the 'from' currency is a valid currency.
      * @param directionEqualToExchangeRate
      * @param from
      * @throws CalculatorException 
      */
     private void verifyCurrencyFrom(boolean directionEqualToExchangeRate, CurrencyValue from) throws CalculatorException {
         if (directionEqualToExchangeRate)
         {
             if (! firstExchangeRate.getFirstCurrency().equals(from)) {
                 throw new CalculatorException("the 'from'-currency is not valid");
             }
         }
         else {
             if (! secondExchangeRate.getFirstCurrency().equals(from)) {
                 throw new CalculatorException("the 'from'-currency is not valid");
             }
         }
     }
 
     /**
      * Find out which exchangeRate member matches the current exchange.
      * @param to
      * @return true if the direction is equal to firstExchangeRate,
      * false if is equal to secondExchangeRate
      * @throws CalculatorException if to does not match any exchangeRate member
      */
     private boolean getExchangeDirection(Currency to) throws CalculatorException {
         if (firstExchangeRate.getSecondCurrency().equals(to))
         {
             return true;
         }
         else if (secondExchangeRate.getSecondCurrency().equals(to)) {
             return false;
         }
         else {
             throw new CalculatorException("the 'to'-currency is not valid");
         }
     }
 }
