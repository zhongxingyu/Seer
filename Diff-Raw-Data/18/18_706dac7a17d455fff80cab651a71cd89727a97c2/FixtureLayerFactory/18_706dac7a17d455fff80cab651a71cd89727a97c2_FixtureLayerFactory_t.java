 package es.udc.cartolab.gvsig.fonsagua.epanet.utils;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.rules.TemporaryFolder;
 
 import com.hardcode.driverManager.DriverLoadException;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 
 import es.udc.cartolab.gvsig.epanet.network.LayerParser;
 import es.udc.cartolab.gvsig.epanet.structures.JunctionWrapper;
 import es.udc.cartolab.gvsig.epanet.structures.LinkWrapper;
 import es.udc.cartolab.gvsig.epanet.structures.NodeWrapper;
 import es.udc.cartolab.gvsig.epanet.structures.PipeWrapper;
 import es.udc.cartolab.gvsig.epanet.structures.PumpWrapper;
 import es.udc.cartolab.gvsig.epanet.structures.ReservoirWrapper;
 import es.udc.cartolab.gvsig.epanet.structures.TankWrapper;
 import es.udc.cartolab.gvsig.epanet.structures.ValveWrapper;
 import es.udc.cartolab.gvsig.shputils.SHPFactory;
 
 public class FixtureLayerFactory {
 
     private List<NodeWrapper> junctions;
     private List<NodeWrapper> reservoirs;
     private List<NodeWrapper> tanks;
     private List<LinkWrapper> pipes;
     private List<LinkWrapper> valves;
     private List<LinkWrapper> pumps;
     private TemporaryFolder temp;
     private LayerParser layerParser;
 
     public FixtureLayerFactory(TemporaryFolder temp, LayerParser layerParser) {
 	this.temp = temp;
 	this.layerParser = layerParser;
 
 	junctions = new ArrayList<NodeWrapper>();
 	reservoirs = new ArrayList<NodeWrapper>();
 	tanks = new ArrayList<NodeWrapper>();
 	pipes = new ArrayList<LinkWrapper>();
 	valves = new ArrayList<LinkWrapper>();
 	pumps = new ArrayList<LinkWrapper>();
 
     }
 
     public void getReservoirJunctionWithDemand() throws Exception {
 	IFeature junctionFeat = FixtureSHPFactory.createJunctionFeature(200,
 		8500, 50, 1);
 	storeJunction(junctionFeat, 50, 100, 1);
 
 	File file = temp.newFile("junctions.shp");
 	FixtureSHPFactory.createJunctionShp(file,
 		new IFeature[] { junctionFeat });
 
 	IFeature reservoirFeat = FixtureSHPFactory.createReservoirFeature(-800,
 		8500, 100);
 	storeReservoir(reservoirFeat, 0, 100, -1);
 	file = temp.newFile("reservoirs.shp");
 	FixtureSHPFactory.createReservoirShp(file,
 		new IFeature[] { reservoirFeat });
 
 	IFeature pipeFeat = FixtureSHPFactory.createPipeFeature(junctionFeat,
 		reservoirFeat, 300, 0.1);
 	storePipe(pipeFeat, 1, 0.01, 0.00138, 0.04);
 
 	file = temp.newFile("pipes.shp");
 	FixtureSHPFactory.createPipeShp(file, new IFeature[] { pipeFeat });
     }
 
     public void getReservoirTankJunctionWithDemand() throws Exception {
 
 	IFeature junctionFeat = FixtureSHPFactory.createJunctionFeature(200,
 		8500, 20, 1);
 	storeJunction(junctionFeat, 33.46, 53.46, 1);
 	File file = temp.newFile("junctions.shp");
 	FixtureSHPFactory.createJunctionShp(file,
 		new IFeature[] { junctionFeat });
 
 	IFeature reservoirFeat = FixtureSHPFactory.createReservoirFeature(
 		-1000, 8500, 100);
 	storeReservoir(reservoirFeat, 0, 100, -12.14);
 	file = temp.newFile("reservoirs.shp");
 	FixtureSHPFactory.createReservoirShp(file,
 		new IFeature[] { reservoirFeat });
 
 	IFeature tankFeat = FixtureSHPFactory.createTankFeature(0.0, 8500.0,
 		50, 5, 0, 10, 5);
 	storeTanks(tankFeat, 5, 55, 11.14);
 	file = temp.newFile("tanks.shp");
 	FixtureSHPFactory.createTankShp(file, new IFeature[] { tankFeat });
 
 	IFeature pipeFeat1 = FixtureSHPFactory.createPipeFeature(junctionFeat,
 		tankFeat, 50, 0.1);
 	storePipe(pipeFeat1, 1, 0.51, 7.79, 0.03);
 	IFeature pipeFeat2 = FixtureSHPFactory.createPipeFeature(reservoirFeat,
 		tankFeat, 90, 0.1);
 	storePipe(pipeFeat2, 12.14, 1.91, 45, 0.02);
 	file = temp.newFile("pipes.shp");
 	FixtureSHPFactory.createPipeShp(file, new IFeature[] { pipeFeat1,
 		pipeFeat2 });
     }
 
     public void getReservoirValveTankJunctionWithDemand() throws Exception {
 	IFeature n1 = FixtureSHPFactory.createReservoirFeature(0, 8500, 100);
 	storeReservoir(n1, 0, 100, -2);
 	File file = temp.newFile("reservoirs.shp");
 	FixtureSHPFactory.createReservoirShp(file, new IFeature[] { n1 });
 
 	double x = 100;
 	double y = 8500;
 	IFeature l4 = FixtureSHPFactory.createFCVFeature(x, y, 90, 90, 2);
 	storeValve(l4, 2, 0.31, 16.09, 0);
 	file = temp.newFile("valves.shp");
 	FixtureSHPFactory.createFCVShp(file, new IFeature[] { l4 });
 
 	IFeature n4 = FixtureSHPFactory
 		.createJunctionFeature(1200, 8500, 20, 1);
 	storeJunction(n4, 33.46, 53.46, 1);
 	file = temp.newFile("junctions.shp");
 	FixtureSHPFactory.createJunctionShp(file, new IFeature[] { n4 });
 
 	IFeature n5 = FixtureSHPFactory.createTankFeature(1000, 8500.0, 50, 5,
 		0, 10, 5);
 	storeTanks(n5, 5, 55, 1);
 	file = temp.newFile("tanks.shp");
 	FixtureSHPFactory.createTankShp(file, new IFeature[] { n5 });
 
 	IFeature l1 = FixtureSHPFactory.createPipeFeature(l4, n5, 50, 0.1);
 	storePipe(l1, 2, 1.02, 28.31, 0.03);
 	IFeature l2 = FixtureSHPFactory.createPipeFeature(n5, n4, 50, 0.1);
 	storePipe(l2, 1, 0.41, 7.70, 0.03);
 	IFeature l3 = FixtureSHPFactory.createPipeFeature(n1, l4, 50, 0.1);
 	storePipe(l3, 2, 1.02, 28.31, 0.03);
 
 	file = temp.newFile("pipes.shp");
 	FixtureSHPFactory.createPipeShp(file, new IFeature[] { l1, l2, l3 });
     }
 
     public void getValveOverReservoir() {
 
     }
 
     public void getPumpWithPower() throws Exception {
 	final double yCoord = 8500;
 	IFeature n1 = FixtureSHPFactory.createReservoirFeature(0, yCoord, 100);
 	storeReservoir(n1, 0, 100, -1.96);
 	File file = temp.newFile("reservoirs.shp");
 	FixtureSHPFactory.createReservoirShp(file, new IFeature[] { n1 });
 
 	double elevation = 90;
 	String type = "POWER";
	double value = 1.0;
 	IFeature p1 = FixtureSHPFactory.createPumpFeature(100, yCoord,
 		elevation, type, value);
 	storePump(p1, 1.96, 0, -52.15, 0);
 	file = temp.newFile("pumps.shp");
 	FixtureSHPFactory.createPumpShp(file, new IFeature[] { p1 });
 
 	IFeature n4 = FixtureSHPFactory.createJunctionFeature(1200, yCoord, 80,
 		1);
 	storeJunction(n4, 43.46, 123.46, 1);
 	file = temp.newFile("junctions.shp");
 	FixtureSHPFactory.createJunctionShp(file, new IFeature[] { n4 });
 
 	IFeature n5 = FixtureSHPFactory.createTankFeature(1000, yCoord, 120, 5,
 		0, 10, 5);
 	storeTanks(n5, 5, 125, 0.96);
 	file = temp.newFile("tanks.shp");
 	FixtureSHPFactory.createTankShp(file, new IFeature[] { n5 });
 
 	IFeature l1 = FixtureSHPFactory.createPipeFeature(p1, n5, 50, 0.1);
 	storePipe(l1, 1.96, 1, 27.15, 0.03);
 	IFeature l2 = FixtureSHPFactory.createPipeFeature(n5, n4, 50, 0.1);
 	storePipe(l2, 1, 0.51, 7.70, 0.03);
 	IFeature l3 = FixtureSHPFactory.createPipeFeature(n1, p1, 50, 0.1);
 	storePipe(l3, 1.96, 1, 27.15, 0.03);
 	file = temp.newFile("pipes.shp");
 	FixtureSHPFactory.createPipeShp(file, new IFeature[] { l1, l2, l3 });
     }
 
     private void storeJunction(IFeature feature, double pressure, double head,
 	    double demand) {
 	NodeWrapper node = new JunctionWrapper(feature);
 	node.setPressure(pressure);
 	node.setHead(head);
 	node.setDemand(demand);
 	junctions.add(node);
     }
 
     private void storeReservoir(IFeature feature, double pressure, double head,
 	    double demand) {
 	NodeWrapper node = new ReservoirWrapper(feature);
 	node.setPressure(pressure);
 	node.setHead(head);
 	node.setDemand(demand);
 	reservoirs.add(node);
     }
 
     private void storeTanks(IFeature feature, double pressure, double head,
 	    double demand) {
 	NodeWrapper node = new TankWrapper(feature);
 	node.setPressure(pressure);
 	node.setHead(head);
 	node.setDemand(demand);
 	tanks.add(node);
     }
 
     private void storePipe(IFeature feat, double flow, double velocity,
 	    double unitheadloss, double frictionfactor) {
 	LinkWrapper link = new PipeWrapper(null);
 	link.setFlow(flow);
 	link.setVelocity(velocity);
 	link.setUnitHeadLoss(unitheadloss);
 	link.setFrictionFactor(frictionfactor);
 	pipes.add(link);
     }
 
     private void storeValve(IFeature feat, double flow, double velocity,
 	    double unitheadloss, double frictionfactor) {
 	LinkWrapper link = new ValveWrapper(null);
 	link.setFlow(flow);
 	link.setVelocity(velocity);
 	link.setUnitHeadLoss(unitheadloss);
 	link.setFrictionFactor(frictionfactor);
 	valves.add(link);
     }
 
     private void storePump(IFeature feat, double flow, double velocity,
 	    double unitheadloss, double frictionfactor) {
 	LinkWrapper link = new PumpWrapper(null);
 	link.setFlow(flow);
 	link.setVelocity(velocity);
 	link.setUnitHeadLoss(unitheadloss);
 	link.setFrictionFactor(frictionfactor);
 	pumps.add(link);
     }
 
     public void parseSHPs() throws DriverLoadException, Exception {
 
 	File file = new File(temp.getRoot().getAbsoluteFile() + File.separator
 		+ "junctions.shp");
 	if (file.exists()) {
 	    FLyrVect junctions = SHPFactory.getFLyrVectFromSHP(file);
 	    layerParser.addJunctions(junctions);
 	}
 
 	file = new File(temp.getRoot().getAbsoluteFile() + File.separator
 		+ "reservoirs.shp");
 	if (file.exists()) {
 	    FLyrVect reservoirs = SHPFactory.getFLyrVectFromSHP(file);
 	    layerParser.addReservoirs(reservoirs);
 	}
 
 	file = new File(temp.getRoot().getAbsoluteFile() + File.separator
 		+ "tanks.shp");
 	if (file.exists()) {
 	    FLyrVect tanks = SHPFactory.getFLyrVectFromSHP(file);
 	    layerParser.addTanks(tanks);
 	}
 
 	file = new File(temp.getRoot().getAbsoluteFile() + File.separator
 		+ "valves.shp");
 	if (file.exists()) {
 	    FLyrVect valves = SHPFactory.getFLyrVectFromSHP(file);
 	    layerParser.addValves(valves);
 	}
 
 	file = new File(temp.getRoot().getAbsoluteFile() + File.separator
 		+ "pumps.shp");
 	if (file.exists()) {
 	    FLyrVect pumps = SHPFactory.getFLyrVectFromSHP(file);
 	    layerParser.addPumps(pumps);
 	}
 
 	file = new File(temp.getRoot().getAbsoluteFile() + File.separator
 		+ "pipes.shp");
 	if (file.exists()) {
 	    FLyrVect pipes = SHPFactory.getFLyrVectFromSHP(file);
 	    layerParser.addPipes(pipes);
 	}
 
     }
 
     public List<NodeWrapper> getJunctions() {
 	return junctions;
     }
 
     public List<NodeWrapper> getReservoirs() {
 	return reservoirs;
     }
 
     public List<NodeWrapper> getTanks() {
 	return tanks;
     }
 
     public List<LinkWrapper> getPipes() {
 	return pipes;
     }
 
     public List<LinkWrapper> getValves() {
 	return valves;
     }
 
     public List<LinkWrapper> getPumps() {
 	return pumps;
     }
 
 }
