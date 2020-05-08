 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package exchangeRate;
 
 
 /**
  * Root component into the Calculator API.
  * Use this class to get access to the calculator factory, as well as the
  * exchange rates.
  * @author Freek
  */
 public final class CalculatorModule {
     
     private ICalculatorFactory calculatorFactory;
     private ExchangeRates exchangeRates;
     
     public static CalculatorModule create()
     {
         return new CalculatorModule();
     }
         
     public ICalculatorFactory getCalculatorFactory()
     {
        assert(calculatorFactory != null);
        assert(exchangeRates != null);
         return calculatorFactory;
     }
     
     public void setExchangeRate(ExchangeRate exchangeRate)
     {
         exchangeRates.setExchangeRate(exchangeRate);
     }
     
     private CalculatorModule()
     {
          exchangeRates = new ExchangeRates();
          calculatorFactory = new CalculatorFactory(exchangeRates);
     }
 }
