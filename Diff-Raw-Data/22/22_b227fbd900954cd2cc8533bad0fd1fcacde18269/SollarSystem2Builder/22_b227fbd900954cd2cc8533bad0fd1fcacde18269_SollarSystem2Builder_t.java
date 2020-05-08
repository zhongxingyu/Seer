 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 package viewerfx;
 
 import com.newdawn.controllers.InitialisationController;
 import com.newdawn.model.ships.Ship;
 import com.newdawn.model.ships.Squadron;
 import com.newdawn.model.ships.orders.MoveToSpaceObjectOrder;
 import com.newdawn.model.ships.orders.Order;
 import com.newdawn.model.system.Satellite;
 import com.newdawn.model.system.Planet;
 import com.newdawn.model.system.Star;
 import com.newdawn.model.system.StellarSystem;
 
 /**
  *
  * @author Pierrick Puimean-Chieze
  */
 public class SollarSystem2Builder {
 
     public static StellarSystem getIt(InitialisationController initialisationController) {
         StellarSystem solarSystem = new StellarSystem();
         solarSystem.setName("Sol System 2");
        Star sun = initialisationController.addStarToSystem(solarSystem, "Sun", Star.SpectralClass.A, 1391900);
        
 
 
 
         Planet mercury = initialisationController.addNewPlanetToStar(sun, null, Planet.PlanetaryClass.TERRESTRIAL, 57950000, 4866);
 //        mercury.setOrbitalPeriod(7600530L);
         mercury.setOrbitalPeriod(10368000L);
         mercury.setName("Mercury");
 
         Planet venus = initialisationController.addNewPlanetToStar(sun, null, Planet.PlanetaryClass.TERRESTRIAL, 108110000, 12106);
         venus.setName("Venus");
 
         Planet Earth = initialisationController.addNewPlanetToStar(sun, null, Planet.PlanetaryClass.TERRESTRIAL, 149570000, 12742);
         Earth.setName("Earth");
 
 
         Satellite moon = initialisationController.addNewSatelliteToPlanet(Earth, null, 384000, 4860);
         moon.setName("Moon");
         Planet Mars = initialisationController.addNewPlanetToStar(sun, null, Planet.PlanetaryClass.TERRESTRIAL, 227840000, 6760);
         Mars.setName("Mars");
 
         Planet Jupiter = initialisationController.addNewPlanetToStar(sun, null, Planet.PlanetaryClass.TERRESTRIAL, 778140000, 142984);
         Jupiter.setName("Jupiter");
 
 
         Planet Saturn = initialisationController.addNewPlanetToStar(sun, null, Planet.PlanetaryClass.TERRESTRIAL, 1_427_000_000, 116438);
         Saturn.setName("Saturn");
 
 
         Planet Uranus = initialisationController.addNewPlanetToStar(sun, null, Planet.PlanetaryClass.TERRESTRIAL, 2_870_300_000L, 46940);
         Uranus.setName("Uranus");
 
         Planet Neptune = initialisationController.addNewPlanetToStar(sun, null, Planet.PlanetaryClass.TERRESTRIAL, 4499900000L, 45432);
         Neptune.setName("Neptune");
 
         Planet Pluto = initialisationController.addNewPlanetToStar(sun, null, Planet.PlanetaryClass.TERRESTRIAL, 5913000000L, 2274);
         Pluto.setName("Pluto");
 
         Squadron testTG = new Squadron();
         testTG.setName("testTG 2");
         testTG.setSpeed(2000 * 1000);
 
 
         Ship testShip = new Ship();
         testShip.setMaxSpeed(4000 * 1000);
         testShip.setTaskGroup(testTG);
         testShip.setName("saassa");
         testTG.getShips().add(testShip);
         testTG.setPositionX(57950000);
         testTG.setPositionY(50000000);
 
         Order testMoveOrder = new MoveToSpaceObjectOrder(venus, testTG);
         testTG.setCurrentOrder(testMoveOrder);
         testMoveOrder.applyOrder();
         solarSystem.getSquadrons().add(testTG);
         testTG.setStellarSystem(solarSystem);
         return solarSystem;
     }
 }
