 package balle.strategy;
 
 import org.junit.Test;
 
 public class StrategyFactoryTest {
 
     /**
      * This is more an integration test than a unit test. Given available
      * designators, the strategy factory should be able to create all of them
      * and not raise UnknownDesignatorException
      * 
      * @throws UnknownDesignatorException
      */
     @Test
     public void testAvailableDesignators() throws UnknownDesignatorException {
         String[] availableDesignators = StrategyFactory.availableDesignators();
         for (String designator : availableDesignators) {
             // An actual test
            StrategyFactory.createClass(designator);
         }
     }
 
 }
