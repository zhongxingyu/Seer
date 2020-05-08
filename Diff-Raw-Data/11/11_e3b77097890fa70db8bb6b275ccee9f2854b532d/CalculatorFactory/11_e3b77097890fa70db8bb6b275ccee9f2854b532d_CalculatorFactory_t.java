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
         
         try
         {
             for (Pair<Currency, Currency> currencyPair : currencyPairs)
             {
                 ExchangeRate firstExchangeRate = exchangeRates.getExchangeRate(currencyPair.getFirst(), currencyPair.getSecond());
                 ExchangeRate secondExchangeRate = exchangeRates.getExchangeRate(currencyPair.getSecond(), currencyPair.getFirst());
 
                 finalExchangeRates.add(firstExchangeRate);
                 finalExchangeRates.add(secondExchangeRate);
             }
         }
         catch (Exception e)
         {
             throw new ExchangeRateCalculatorException("Unable to create Calculator. Duplicate currency pairs specified.");
         }
         
         return new Calculator(finalExchangeRates);
     }
 
     public Calculator create(Calculator firstCalculator, Calculator secondCalculator) throws ExchangeRateCalculatorException
     {
         Set<ExchangeRate> exchangeRates = 
                 mergeExchangeRates(firstCalculator.getExchangeRates(), secondCalculator.getExchangeRates());
         
         return new Calculator(exchangeRates);
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
