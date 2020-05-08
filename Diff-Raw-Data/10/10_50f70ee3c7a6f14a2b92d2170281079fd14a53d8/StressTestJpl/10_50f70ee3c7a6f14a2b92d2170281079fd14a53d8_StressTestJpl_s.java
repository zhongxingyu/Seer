 package org.jpc.examples.metro.jpl;
 
import static junit.framework.Assert.assertTrue;
 
 import org.jpc.engine.jpl.JplYapDriver;
 import org.jpc.engine.profile.LogtalkEngineProfile;
 import org.jpc.engine.provider.SimpleEngineProvider;
 import org.jpc.examples.metro.MetroExample;
 import org.jpc.examples.metro.StressTest;

import static org.jpc.engine.provider.PrologEngineProviderManager.setPrologEngineProvider;

 import org.junit.BeforeClass;
 

 public class StressTestJpl extends StressTest {
 
 	@BeforeClass
     public static void oneTimeSetUp() {
 		setPrologEngineProvider(new SimpleEngineProvider(new LogtalkEngineProfile(new JplYapDriver()).createPrologEngine()));
 		assertTrue(MetroExample.loadAll());
     }
 
 }
