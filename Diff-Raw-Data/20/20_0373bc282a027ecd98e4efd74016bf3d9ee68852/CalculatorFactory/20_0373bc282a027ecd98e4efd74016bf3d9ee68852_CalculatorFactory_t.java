 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package exchangeRate;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  *
  * @author ronald
  */
 class CalculatorFactory implements ICalculatorFactory
 {
     private ExchangeRates exchangeRates;
     
     public Calculator create(Currency firstCurrency, Currency secondCurrency) throws ExchangeRateCalculatorException
     {
         // get exchange rates from list
         ExchangeRate firstExchangeRate = exchangeRates.getExchangeRate(firstCurrency, secondCurrency);
         ExchangeRate secondExchangeRate = exchangeRates.getExchangeRate(secondCurrency, firstCurrency);
         
         Set<ExchangeRate> exchangeRates = new HashSet<ExchangeRate>();
         exchangeRates.add(firstExchangeRate);
         exchangeRates.add(secondExchangeRate);
         
         return new Calculator(exchangeRates);
     }
     
     public Calculator create(Collection<Pair<Currency, Currency>> currencyPairs) throws ExchangeRateCalculatorException
     {
         Set<ExchangeRate> finalExchangeRates = new HashSet<ExchangeRate>();
         
         for (Pair<Currency, Currency> currencyPair : currencyPairs)
         {
             ExchangeRate firstExchangeRate = exchangeRates.getExchangeRate(currencyPair.getFirst(), currencyPair.getSecond());
             ExchangeRate secondExchangeRate = exchangeRates.getExchangeRate(currencyPair.getSecond(), currencyPair.getFirst());
 
             boolean firstRateAdded = finalExchangeRates.add(firstExchangeRate);
             boolean secondRateAdded = finalExchangeRates.add(secondExchangeRate);
             if (!firstRateAdded || !secondRateAdded)
             {
                 throw new MergeCalculatorException("Unable to create Calculator. Duplicate currency pairs specified.");
             }
         }
         
         return new Calculator(finalExchangeRates);
     }
     
     private Calculator mergeTwoCalculators(Calculator calc1, Calculator calc2) throws ExchangeRateCalculatorException{
         assert(calc1 != null);
         assert(calc2 != null);
         
         Calculator result;
         if (calc1.getRatesProvider() == null){
             if (calc2.getRatesProvider() == null){
                 //both offline
                 Set<ExchangeRate> exchangeRates = 
                 mergeExchangeRates(calc1.getExchangeRates(), calc2.getExchangeRates());
         
                 result =  new Calculator(exchangeRates);
             }else{
                 //one offline two online
                result = calc2.copy();
             }
         }else{
             if (calc2.getRatesProvider() == null){
                 //one oneline two offline
                result = calc1.copy();               
             }else{
                 //both online
                result = calc1.copy();                
             }
         }
         return result;
     }
 
     public Calculator create(Calculator firstCalculator, Calculator secondCalculator) throws ExchangeRateCalculatorException
     {
         return mergeTwoCalculators(firstCalculator, secondCalculator);
     }
     
     CalculatorFactory(ExchangeRates exchangeRates)
     {
         assert(exchangeRates != null);
         this.exchangeRates = exchangeRates;
     }
     
     private CalculatorFactory()
     {
     }
 
     private Set<ExchangeRate> mergeExchangeRates(Set<ExchangeRate> first, Set<ExchangeRate> second) throws ExchangeRateCalculatorException
     {
         assert(first != null && second != null);
         assert(first.size() != 0 &&  second.size() !=0);
         
         
         Set<ExchangeRate> mergedRates = new HashSet<ExchangeRate>();
         mergedRates.addAll(first);
         mergedRates.addAll(second);
         
         if (second.size() + first.size() != mergedRates.size()){
             throw new MergeCalculatorException("Calculators cannot contain the same exchangeRates");
         }
             
         return mergedRates;
     }    
 }
