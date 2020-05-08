 package billsplit.engine;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 @RunWith(Suite.class)
@SuiteClasses({ ItemTest.class, TransactionTest.class, /*AccountTest.class,*/ EventTest.class, ParticipantTest.class /* ,  PaymentTest.class */})
 public class EngineTests {
 
 }
