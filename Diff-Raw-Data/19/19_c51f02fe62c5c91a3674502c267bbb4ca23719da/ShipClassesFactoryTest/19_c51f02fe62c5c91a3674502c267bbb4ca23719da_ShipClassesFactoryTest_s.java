 package kop.ships;
 
 import kop.ships.blueprint.ContainerShipBlueprint;
 import kop.ships.blueprint.ShipBlueprint;
 import kop.ships.engine.Engine;
 import org.testng.annotations.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ola
  * Date: 4/9/11
  * Time: 3:03 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ShipClassesFactoryTest {
	@Test
 	public void testSaveShipClasses() throws Exception {
 		ShipClassesFactory factory = new ShipClassesFactory();
 		ArrayList<ShipClass> list = new ArrayList<ShipClass>();
 		ShipBlueprint shipSpec = new ContainerShipBlueprint();
 
 		Engine engine = new Engine();
 		engine.setBsfc(178);
 		engine.setManufacturer("Deutz");
 		engine.setModelName("TBD 645 L9");
 		engine.setkW(3825);
 
 		shipSpec.addEngine(engine);
 
 		ShipClass shipClass = new ShipClass(shipSpec, 10.0, "Feeder");
 
 
 		list.add(shipClass);
 		factory.saveShipClasses("shipclasses.xml", list);
 	}
	@Test
 	public void testCreateShipClasses() throws Exception {
 		ShipClassesFactory factory = new ShipClassesFactory();
 		List<ShipClass> list =  factory.createShipClasses("shipclasses.xml");
 	}
 }
