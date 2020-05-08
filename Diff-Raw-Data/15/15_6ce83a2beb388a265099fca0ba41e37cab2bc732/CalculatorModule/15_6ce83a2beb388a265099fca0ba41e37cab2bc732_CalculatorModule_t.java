 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package exchangeRate;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author user
  */
 public final class CalculatorModule {
     
     private ICalculatorFactory calculatorFactory = new CalculatorFactory();
     private List<ExchangeRate> exchangeRates = new ArrayList<ExchangeRate>();
     
     public static CalculatorModule create()
     {
         return new CalculatorModule();
     }
         
     public ICalculatorFactory getCalculatorFactory()
     {
         return calculatorFactory;
     }
     
    public void setExchangeRate(ExchangeRate exchangeRate)
    {
        // remove possibly pre-existing exchange rate
        exchangeRates.remove(exchangeRate);
        exchangeRates.add(exchangeRate);
    }
    
     private CalculatorModule()
     {
     }
 }
