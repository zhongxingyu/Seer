 package exchangeRate;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.math.RoundingMode;
 import junit.framework.TestCase;
 
 /** Finish the Calculator API, and then write bodies of methods inside
   * of this class to match the given tasks. To fulfill your task, use the
   * API define in the <code> exchangeRate</code> package.
   * Do not you reflection, or other “hacks” as your code
   * shall run without any runtime permissions.
   */
 public class Task1Test extends TestCase {
     
      private static CalculatorModule calculatorModule;
     
     
      public Task1Test(String testName) throws ExchangeRateCalculatorException {
          super(testName);
          
          calculatorModule = CalculatorModule.create();
         calculatorModule.setExchangeRate(new ExchangeRate(BigDecimal.ONE.divide(new BigDecimal(17), 20, RoundingMode.HALF_EVEN), new Currency("USD"), new Currency("CZK")));
          calculatorModule.setExchangeRate(new ExchangeRate(new BigDecimal(17), new Currency("CZK"), new Currency("USD")));
          calculatorModule.setExchangeRate(new ExchangeRate(new BigDecimal(100).divide(new BigDecimal(80)), new Currency("SKK"), new Currency("CZK")));
          calculatorModule.setExchangeRate(new ExchangeRate(new BigDecimal(80).divide(new BigDecimal(100)), new Currency("CZK"), new Currency("SKK")));
      }
 
      @Override
      protected void setUp() throws Exception {
      }
 
      @Override
      protected void tearDown() throws Exception {
      }
 
      //
      // Imagine that there are three parts of the whole system:
      // 1. there is someone who knows the current exchange rate
      // 2. there is someone who wants to calculate the exchange
      // 3. party 1 provides party 2 with an object that realizes the API.
      // Please design such API
      //
 
      /** Create a calculator that understands two currencies, CZK and
       *  USD. Make 1 USD == 17 CZK. This is a method provided for #1 group -
       *  e.g. those that know the exchange rate. They somehow need to create
       *  the objects from the API and tell them the exchange rate. The API itself
       *  knows nothing about any rates, before the createCZKtoUSD method is called.
       *
       * Creation of the calculator shall not require subclassing of any class
       * or interface on the client side.
       *
       * @return prepared calculator ready for converting USD to CZK and CZK to USD
       */
      public static Calculator createCZKtoUSD() throws ExchangeRateCalculatorException {
          return calculatorModule.getCalculatorFactory().create(new Currency("CZK"), new Currency("USD"));
      }
 
      /** Create calculator that understands two currencies, CZK and
       *  SKK. Make 100 SKK == 80 CZK. Again this is method for the #1 group -
       *  it knows the exchange rate, and needs to use the API to create objects
       *  with the exchange rate. Anyone shall be ready to call this method without
       *  any other method being called previously. The API itself shall know
       *  nothing about any rates, before this method is called.
       *
       * Creation of the calculator shall not require subclassing of any class
       * or interface on the client side.
       * 
       * @return prepared calculator ready for converting SKK to CZK and CZK to SKK
       */
      public static Calculator createSKKtoCZK() throws ExchangeRateCalculatorException {
          return calculatorModule.getCalculatorFactory().create(new Currency("SKK"), new Currency("CZK"));
      }
 
      //
      // now the methods for group #2 follow:
      // this group knows nothing about exchange rates, but knows how to use
      // the API to calculate exchanges. It somehow (by calling a method provided by 
      // group #1) gets an object from the API and uses it to do the rate caluclations.
      //
      
      /** Use the calculator from <code>createCZKtoUSD</code> method and do few calculations
       * with it.
       */
      public void testExchangeCZKUSD() throws Exception {
          Currency usd = new Currency("USD");
          Currency czk = new Currency("CZK");
          
          Calculator c = createCZKtoUSD();
          // convert $5 to CZK using c:
          // assertEquals("Result is 85 CZK");
          CurrencyValue result = c.convert(new CurrencyValue(usd, new BigDecimal(5)), czk);
          assertEquals(result.getValue(), new BigDecimal (85));
          assertEquals(result.getCurrency(), czk);
 
          // convert $8 to CZK
          // assertEquals("Result is 136 CZK");
          result = c.convert(new CurrencyValue(usd, new BigDecimal(8)), czk);
          assertEquals(result.getValue(), new BigDecimal(136));
          assertEquals(result.getCurrency(), czk);
 
          // convert 1003CZK to USD
          // assertEquals("Result is 59 USD");
          result = c.convert(new CurrencyValue(czk, new BigDecimal(1003)), usd);
          assertEquals(result.getValue(), new BigDecimal(59));
          assertEquals(result.getCurrency(), usd);
      }
 
      /** Use the calculator from <code>createSKKtoCZK</code> method and do a few calculations
       * with it.
       */
      public void testExchangeSKKCZK() throws Exception {
          Currency skk = new Currency("SKK");
          Currency czk = new Currency("CZK");        
          
          Calculator c = createSKKtoCZK();
          // convert 16CZK using c:
          // assertEquals("Result is 20 SKK");
          CurrencyValue result = c.convert(new CurrencyValue(czk, new BigDecimal(16)), skk);
          assertTrue(result.getValue().equals(new BigDecimal(20)));
          assertEquals(result.getCurrency(), skk);
 
          // convert 500SKK to CZK
          // assertEquals("Result is 400 CZK");
          result = c.convert(new CurrencyValue(skk, new BigDecimal(500)), czk);
          assertEquals(result.getValue(), new BigDecimal(400));
          assertEquals(result.getCurrency(), czk);
      }
 
      /** Verify that the CZK to USD calculator knows nothing about SKK.
       */
      public void testCannotConvertToSKKwithCZKUSDCalculator() throws Exception {
          Calculator c = createCZKtoUSD();
          try{
              // convert $5 to SKK, the API shall say this is not possible
              c.convert(new CurrencyValue(new Currency("USD"), new BigDecimal(5)), new Currency("SKK"));
              fail("should not be able to convert 500 SKK to CZK with this calculator");
 
              // convert 500 SKK to CZK, the API shall say this is not possible
              c.convert(new CurrencyValue(new Currency("SKK"), new BigDecimal(500)), new Currency("CZK"));
 
              fail("should not be able to convert 500 SKK to CZK with this calculator");
          }catch(Exception e){
              
          }
      }
 
      /** Verify that the CZK to SKK calculator knows nothing about USD.
       */
      public void testCannotConvertToUSDwithCZKSKKCalculator() throws Exception {
          Calculator c = createSKKtoCZK();
 
          Currency usd = new Currency("USD");
          Currency skk = new Currency("SKK");
          Currency czk = new Currency("CZK");
          
          // convert $5 to SKK, the API shall say this is not possible
          try {
              CurrencyValue result = c.convert(new CurrencyValue(usd, new BigDecimal(5)), skk);
              fail("converting USD using SKK/CZK calculator succeeded");
          }
          catch (ExchangeRateCalculatorException ex) {
              // everything's fine
          }
 
          // convert 500 CZK to USD, the API shall say this is not possible
          try {
              CurrencyValue result = c.convert(new CurrencyValue(czk, new BigDecimal(500)), usd);
              fail("converting USD using SKK/CZK calculator succeeded");
          }
          catch (ExchangeRateCalculatorException ex) {
              // everything's fine
          }
      }
 }
