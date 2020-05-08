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
    
    public void setExchangeRate(ExchangeRate exchangeRate)
    {
        // search whether exchange rate already exists
         
    }
    
     public ICalculatorFactory getCalculatorFactory()
     {
         return calculatorFactory;
     }
     
     private CalculatorModule()
     {
     }
 }
