 package org.jpc.jpl;
 
 import org.jpc.engine.jpl.JplSwiDriver;
 import org.jpc.engine.prolog.driver.PrologEngineDriver;
 import org.jpc.engine.provider.SimpleEngineProvider;

import static org.jpc.engine.provider.PrologEngineProviderManager.setPrologEngineProvider;

 import org.junit.BeforeClass;
 
 public class JplSwiPrologEngineTestSuite extends JplPrologEngineTestSuite {
 	@BeforeClass
 	public static void setUp() {
 		PrologEngineDriver prologEngineConfiguration = new JplSwiDriver();
 		setPrologEngineProvider(new SimpleEngineProvider(prologEngineConfiguration.createPrologEngine()));
 	}
 }
