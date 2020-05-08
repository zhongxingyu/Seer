 package org.kalibro.desktop;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PowerMockIgnore;
 import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 /**
  * Application should exit after click on menu Kalibro->Exit.
  * 
  * @author Carlos Morais
  */
 @RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.*")
 @PrepareOnlyThisForTest(KalibroMenu.class)
 public class ExitFromMenu extends KalibroDesktopTestCase {
 
 	@Test(timeout = ACCEPTANCE_TIMEOUT)
 	public void shouldExitFromMenu() {
 		PowerMockito.mockStatic(System.class);
 		startKalibroFrame();
 
 		fixture.menuItem("exit").click();
 		PowerMockito.verifyStatic();
 		System.exit(0);
 	}
 }
