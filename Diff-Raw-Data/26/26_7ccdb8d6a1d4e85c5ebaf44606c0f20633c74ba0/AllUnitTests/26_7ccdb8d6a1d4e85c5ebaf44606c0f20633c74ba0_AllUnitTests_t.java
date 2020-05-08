 package n3phele;
 
 import n3phele.process.AccountResourceTest;
 import n3phele.process.CloudProcessManagerTest;
 import n3phele.process.CloudProcessTest;
 import n3phele.service.actions.CreateVMActionTest;
 import n3phele.service.actions.NShellActionTest;
 import n3phele.service.nShell.ExpressionTest;
 import n3phele.service.nShell.ParserTest;
 import n3phele.workloads.CloudProcessWorkloadsTest;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 @RunWith(Suite.class)
 @SuiteClasses({CloudProcessTest.class, ExpressionTest.class, ParserTest.class, CreateVMActionTest.class, NShellActionTest.class, CloudProcessManagerTest.class, 
	AccountResourceTest.class, CloudProcessWorkloadsTest.class })
 public class AllUnitTests {
 
 }
