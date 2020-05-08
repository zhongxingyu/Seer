 package es.udc.cartolab.gvsig.fonsagua.epanet;
 
import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.addition.epanet.network.structures.Curve.CurveType;
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 public class NetworkBuilderIntegrationTest {
 
     private static EpanetWrapper epanet;
 
     private static BaseformWrapper baseform;
 
     private NetworkBuilder nb;
 
     @Rule
     public TemporaryFolder temp = new TemporaryFolder();
 
     @BeforeClass
     public static void setUpBeforeClass() {
 	epanet = new EpanetWrapper(TestProperties.epanetPath);
 	baseform = new BaseformWrapper("binaries/BaseformEpaNetLib-1.0.jar");
     }
 
     @Before
     public void setUp() throws Exception {
 	nb = new NetworkBuilder();
     }
 
     @Test
     public void reservoir_junctionWithDemand() throws Exception {
 	getReservoirJunctionWithDemand();
 	executeTest("reservoir_junction-with-demand");
     }
 
     // TODO: check inp
     @Test
    public void reservoir_tank_juctionWithDemand() throws Exception {
 	getReservoirTankJuctionWithDemand();
 	executeTest("reservoir_tank_junction-with-demand");
     }
 
     // TODO: check inp
     @Test
    public void reservoir__valve_tank_juctionWithDemand() throws Exception {
 	getReservoirValveTankJuctionWithDemand();
 	executeTest("reservoir_valve_tank_junctionWithDemand");
     }
 
     // TODO: check inp
     @Test
     public void pumpWithPower() throws Exception {
 	getPumpWithPower();
 	executeTest("pumpWithPower");
     }
 
     // TODO: check inp
     @Test
     public void pumpWithCurve() throws Exception {
 	getPumpWithCurve();
 	executeTest("pumpWithCurve");
     }
 
     private void executeTest(String patternName) throws Exception {
 
 	File inp = temp.newFile("foo.inp");
 	nb.createInpFile(inp);
 
 	// Epanet
 	String actualRPT[] = epanet.execute(inp.getAbsolutePath());
 	final File expectedRPT = new File("fixtures/" + patternName + ".rpt");
 	assertTrue(ComparatorUtils.rptComparator(expectedRPT, new File(
 		actualRPT[0])));
 
 	// Baseform
 	String actualRPT2[] = baseform.execute(inp.getAbsolutePath());
	assertFalse(FileUtils.contentEquals(new File(actualRPT2[0]), new File(
		"fixtures/" + patternName + ".inp.nodes.out")));
	assertFalse(FileUtils.contentEquals(new File(actualRPT2[1]), new File(
 		"fixtures/" + patternName + ".inp.links.out")));
     }
 
     private void getReservoirJunctionWithDemand() {
 	nb.getNode("2", 200.00, 8500.00, 50, 1);
 	nb.getReservoir("1", -800.00, 8500.00, 100);
 	nb.getPipe("1", "1", "2", 1000, 300, 0.1);
     }
 
     private void getReservoirTankJuctionWithDemand() {
 	nb.getNode("2", 207.01, 8428.87, 20, 1);
 	nb.getReservoir("1", -1321.66, 8460.72, 100);
 	nb.getTank("3", -557.32, 8704.88, 50, 5, 0, 10, 5);
 	nb.getPipe("1", "2", "3", 200, 50, 0.1);
 	nb.getPipe("2", "1", "3", 1000, 90, 0.1);
     }
 
     private void getReservoirValveTankJuctionWithDemand() {
 	nb.getNode("2", -1406.58, 7728.24, 90, 0);
 	nb.getNode("3", -1119.96, 7728.24, 90, 0);
 	nb.getNode("4", 4145.44, 7664.54, 20, 1);
 	nb.getReservoir("1", -1884.29, 7738.85, 100);
 	nb.getTank("5", 1204.88, 8046.71, 50, 5, 0, 10, 5);
 	nb.getPipe("1", "3", "5", 900, 50, 0.1);
 	nb.getPipe("2", "5", "4", 200, 50, 0.1);
 	nb.getPipe("3", "1", "2", 100, 50, 0.1);
 	nb.getFlowControlValve("4", "2", "3", 90, 2);
     }
 
     private void getPumpWithPower() {
 	nb.getNode("2", -1406.58, 7728.24, 90, 0);
 	nb.getNode("3", -1119.96, 7728.24, 90, 0);
 	nb.getNode("4", 4145.448, 7664.54, 80, 1);
 	nb.getReservoir("1", -1884.29, 7738.85, 100);
 	nb.getTank("5", 1204.88, 8046.71, 120, 5, 0, 10, 5);
 	nb.getPipe("1", "3", "5", 900, 50, 0.1);
 	nb.getPipe("2", "5", "4", 200, 50, 0.1);
 	nb.getPipe("3", "1", "2", 100, 50, 0.1);
 	nb.getPumpWithPower("4", "2", "3", 1);
     }
 
     private void getPumpWithCurve() {
 	nb.getNode("2", -1406.58, 7728.24, 90, 0);
 	nb.getNode("3", -1119.96, 7728.24, 90, 0);
 	nb.getNode("4", 4145.448, 7664.54, 80, 1);
 	nb.getReservoir("1", -1884.29, 7738.85, 100);
 	nb.getTank("5", 1204.88, 8046.71, 120, 5, 0, 10, 5);
 	nb.getPipe("1", "3", "5", 900, 50, 0.1);
 	nb.getPipe("2", "5", "4", 200, 50, 0.1);
 	nb.getPipe("3", "1", "2", 100, 50, 0.1);
 	nb.getHeadCurve("1", CurveType.H_CURVE, 2.71, 60);
 	nb.getPumpWithCurve("4", "2", "3", "1");
     }
 
 }
