 package au.net.netstorm.boost.test.automock;
 
 import au.net.netstorm.boost.test.cases.StrategyTestCase;
 
 public abstract class InteractionTestCase extends StrategyTestCase implements UsesMocks {
    public MockExpectations expect;
 }
