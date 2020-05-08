 /** es.upm.dit.gsi.shanks
  11/01/2012
 
  */
 package es.upm.dit.gsi.shanks.model;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Properties;
 import java.util.logging.LogManager;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import es.upm.dit.gsi.shanks.model.element.device.test.MyDevice;
 import es.upm.dit.gsi.shanks.model.element.link.test.MyLink;
 import es.upm.dit.gsi.shanks.model.scenario.ComplexScenario;
 import es.upm.dit.gsi.shanks.model.scenario.Scenario;
 import es.upm.dit.gsi.shanks.model.scenario.test.FinalComplexScenario;
 import es.upm.dit.gsi.shanks.model.scenario.test.MyComplexScenario;
 import es.upm.dit.gsi.shanks.model.scenario.test.MyHyperComplexScenario;
 import es.upm.dit.gsi.shanks.model.scenario.test.MyMegaComplexScenario;
 import es.upm.dit.gsi.shanks.model.scenario.test.MyScenario;
 import es.upm.dit.gsi.shanks.model.scenario.test.MySuperComplexScenario;
 import es.upm.dit.gsi.shanks.model.test.MyShanksSimulation;
 import es.upm.dit.gsi.shanks.model.test.MyShanksSimulation2DGUI;
 import es.upm.dit.gsi.shanks.model.test.MyShanksSimulation3DGUI;
 
 
 /**
  * @author a.carrera
  *
  */
 public class ShanksSimulationTest {
 
 
     /**
      * @throws Exception
      */
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         LogManager lm = LogManager.getLogManager();
         File configFile = new File("src/test/resources/logging.properties");
         lm.readConfiguration(new FileInputStream(configFile));
 
     }
 
     /**
      * @throws Exception
      */
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
     }
 
     /**
      * @throws Exception
      */
     @Before
     public void setUp() throws Exception {
     }
     
     /**
      * @throws Exception
      */
     @After
     public void tearDown() throws Exception {
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationWithoutGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.NO_GUI);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "0");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyScenario.class, "MyScenario",
                     MyScenario.SUNNY, scenarioProperties, configProperties);
             sim.start();
             do
                 if (!sim.schedule.step(sim))
                     break;
             while (sim.schedule.getSteps() < 2001);
             sim.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithoutGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.NO_GUI);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "1");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyScenario.class, "MyScenario",
                     MyScenario.SUNNY, scenarioProperties, configProperties);
             sim.start();
             do
                 if (!sim.schedule.step(sim))
                     break;
             while (sim.schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             sim.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
     }
 
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithoutGUICountingResolvedFailures() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.NO_GUI);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "1");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyScenario.class, "MyScenario",
                     MyScenario.SUNNY, scenarioProperties, configProperties);
             sim.start();
             do
                 if (!sim.schedule.step(sim))
                     break;
             while (sim.schedule.getSteps() < 5001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             sim.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationWith2DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_2D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "0");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyScenario.class, "MyScenario",
                     MyScenario.SUNNY, scenarioProperties, configProperties);
             MyShanksSimulation2DGUI gui = new MyShanksSimulation2DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWith2DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_2D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "1");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyScenario.class, "MyScenario",
                     MyScenario.SUNNY, scenarioProperties, configProperties);
             MyShanksSimulation2DGUI gui = new MyShanksSimulation2DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             gui.finish();
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationWith3DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_3D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "0");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyScenario.class, "MyScenario",
                     MyScenario.SUNNY, scenarioProperties, configProperties);
             MyShanksSimulation3DGUI gui = new MyShanksSimulation3DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWith3DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_3D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "1");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyScenario.class, "MyScenario",
                     MyScenario.SUNNY, scenarioProperties, configProperties);
             MyShanksSimulation3DGUI gui = new MyShanksSimulation3DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void MyShanksSimulationResolvingProblemsWithComplexScenario2DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_2D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "1");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyComplexScenario.class,
                     "MyComplexScenario", MyComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation2DGUI gui = new MyShanksSimulation2DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithComplexScenario2DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_2D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "2");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyComplexScenario.class,
                     "MyComplexScenario", MyComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation2DGUI gui = new MyShanksSimulation2DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithComplexScenarioCheckingElementsNoGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.NO_GUI);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "1");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyComplexScenario.class,
                     "MyComplexScenario", MyComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             ComplexScenario cs = (ComplexScenario) sim.getScenario();
             Scenario s1 = cs.getScenario("Scenario1");
             Assert.assertNotNull(s1);
             Assert.assertNotNull(s1.getNetworkElement("D1"));
             Assert.assertNotNull(s1.getNetworkElement("L3"));
             Assert.assertEquals(MyDevice.class, s1.getNetworkElement("D1")
                     .getClass());
             Assert.assertEquals(MyLink.class, s1.getNetworkElement("L3")
                     .getClass());
 
             Scenario s2 = cs.getScenario("Scenario1");
             Assert.assertNotNull(s2);
             Assert.assertNotNull(s2.getNetworkElement("D1"));
             Assert.assertNotNull(s2.getNetworkElement("L3"));
             Assert.assertEquals(MyDevice.class, s2.getNetworkElement("D1")
                     .getClass());
             Assert.assertEquals(MyLink.class, s2.getNetworkElement("L3")
                     .getClass());
             sim.start();
             do
                 if (!sim.schedule.step(sim))
                     break;
             while (sim.schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             sim.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithComplexScenarioCheckingElements2DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_2D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "1");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyComplexScenario.class,
                     "MyComplexScenario", MyComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation2DGUI gui = new MyShanksSimulation2DGUI(sim);
             ComplexScenario cs = (ComplexScenario) sim.getScenario();
             Scenario s1 = cs.getScenario("Scenario1");
             Assert.assertNotNull(s1);
             Assert.assertNotNull(s1.getNetworkElement("D1"));
             Assert.assertNotNull(s1.getNetworkElement("L3"));
             Assert.assertEquals(MyDevice.class, s1.getNetworkElement("D1")
                     .getClass());
             Assert.assertEquals(MyLink.class, s1.getNetworkElement("L3")
                     .getClass());
 
             Scenario s2 = cs.getScenario("Scenario1");
             Assert.assertNotNull(s2);
             Assert.assertNotNull(s2.getNetworkElement("D1"));
             Assert.assertNotNull(s2.getNetworkElement("L3"));
             Assert.assertEquals(MyDevice.class, s2.getNetworkElement("D1")
                     .getClass());
             Assert.assertEquals(MyLink.class, s2.getNetworkElement("L3")
                     .getClass());
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithComplexScenarioCheckingElements3DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_3D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "1");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyComplexScenario.class,
                     "MyComplexScenario", MyComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation3DGUI gui = new MyShanksSimulation3DGUI(sim);
             ComplexScenario cs = (ComplexScenario) sim.getScenario();
             Scenario s1 = cs.getScenario("Scenario1");
             Assert.assertNotNull(s1);
             Assert.assertNotNull(s1.getNetworkElement("D1"));
             Assert.assertNotNull(s1.getNetworkElement("L3"));
             Assert.assertEquals(MyDevice.class, s1.getNetworkElement("D1")
                     .getClass());
             Assert.assertEquals(MyLink.class, s1.getNetworkElement("L3")
                     .getClass());
 
             Scenario s2 = cs.getScenario("Scenario1");
             Assert.assertNotNull(s2);
             Assert.assertNotNull(s2.getNetworkElement("D1"));
             Assert.assertNotNull(s2.getNetworkElement("L3"));
             Assert.assertEquals(MyDevice.class, s2.getNetworkElement("D1")
                     .getClass());
             Assert.assertEquals(MyLink.class, s2.getNetworkElement("L3")
                     .getClass());
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void MyShanksSimulationResolvingProblemsWithComplexScenario3DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_3D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "1");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyComplexScenario.class,
                     "MyComplexScenario", MyComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation3DGUI gui = new MyShanksSimulation3DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithComplexScenario3DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_3D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "2");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyComplexScenario.class,
                     "MyComplexScenario", MyComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation3DGUI gui = new MyShanksSimulation3DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithSuperComplexScenario2DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_2D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "2");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MySuperComplexScenario.class,
                     "MySuperComplexScenario", MySuperComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation2DGUI gui = new MyShanksSimulation2DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithSuperComplexScenario3DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_3D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "2");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MySuperComplexScenario.class,
                     "MySuperComplexScenario", MySuperComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation3DGUI gui = new MyShanksSimulation3DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithMegaComplexScenario2DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_2D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "2");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyMegaComplexScenario.class,
                     "MyMegaComplexScenario", MyMegaComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation2DGUI gui = new MyShanksSimulation2DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithMegaComplexScenario3DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_3D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "2");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyMegaComplexScenario.class,
                     "MyMegaComplexScenario", MyMegaComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation3DGUI gui = new MyShanksSimulation3DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             gui.finish();
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
        Assert.assertFalse(catched);

     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithHyperComplexScenario2DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_2D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "2");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyHyperComplexScenario.class,
                     "MyHyperComplexScenario", MyHyperComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation2DGUI gui = new MyShanksSimulation2DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithHyperComplexScenario3DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_3D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "2");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), MyHyperComplexScenario.class,
                     "MyHyperComplexScenario", MyHyperComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation3DGUI gui = new MyShanksSimulation3DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 2001);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithFinalComplexScenario2DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_2D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "2");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), FinalComplexScenario.class,
                     "FinalComplexScenario", MyHyperComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation2DGUI gui = new MyShanksSimulation2DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 100);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
     /**
      * 
      */
     @Test
     public void ShanksSimulationResolvingProblemsWithFinalComplexScenario3DGUI() {
         boolean catched = false;
         try {
             Properties scenarioProperties = new Properties();
             scenarioProperties.put(MyScenario.CLOUDY_PROB, "50.0");
             scenarioProperties.put(Scenario.SIMULATION_GUI,
                     Scenario.SIMULATION_3D);
 
             Properties configProperties = new Properties();
             configProperties.put(MyShanksSimulation.CONFIGURATION, "2");
             MyShanksSimulation sim = new MyShanksSimulation(
                     System.currentTimeMillis(), FinalComplexScenario.class,
                     "FinalComplexScenario", MyHyperComplexScenario.SUNNY,
                     scenarioProperties, configProperties);
             MyShanksSimulation3DGUI gui = new MyShanksSimulation3DGUI(sim);
             gui.start();
             do
                 if (!gui.getSimulation().schedule.step(sim))
                     break;
             while (gui.getSimulation().schedule.getSteps() < 100);
             Assert.assertTrue(sim.getNumOfResolvedFailures()>0);
             gui.finish();
         } catch (Exception e) {
             catched = true;
             e.printStackTrace();
         }
         Assert.assertFalse(catched);
 
     }
 
 }
