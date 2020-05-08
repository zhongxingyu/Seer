 package lv.k2611a.network.req;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import lv.k2611a.domain.Building;
 import lv.k2611a.domain.BuildingType;
 import lv.k2611a.domain.ConstructionOption;
 import lv.k2611a.domain.Map;
 import lv.k2611a.domain.TileType;
 import lv.k2611a.domain.Unit;
 import lv.k2611a.domain.UnitType;
 import lv.k2611a.service.game.GameServiceImpl;
 import lv.k2611a.service.game.UserActionService;
 import lv.k2611a.util.Point;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:testContext.xml"})
 public class StartConstructionTest {
 
 
     @Autowired
     private GameServiceImpl gameService;
 
     @Autowired
     private UserActionService userActionService;
 
     @Before
     public void setup() {
         gameService.setTickCount(0);
         // all building should cost 0
         for (BuildingType buildingType : BuildingType.values()) {
             buildingType.setCostPerTick(0);
         }
     }
 
     @Test
     public void onlyConstructionYardCanBuild() {
         Map map = new Map(64,64, TileType.ROCK);
         gameService.setMap(map);
 
         Building constructionYard = new Building();
         constructionYard.setOwnerId(1);
         constructionYard.setType(BuildingType.POWERPLANT);
         constructionYard.setX(1);
         constructionYard.setY(1);
         constructionYard.setHp(100);
         int conYardId =  map.addBuilding(constructionYard);
 
         StartConstruction startConstruction = new StartConstruction();
         startConstruction.setBuilderId(conYardId);
         startConstruction.setEntityToBuildId(BuildingType.POWERPLANT.getIdOnJS());
         startConstruction.setPlayerId(1);
         userActionService.registerAction(startConstruction);
 
         for (int i = 0; i < BuildingType.POWERPLANT.getTicksToBuild(); i++) {
             gameService.tick();
         }
 
         assertEquals("building should not be built", 1, map.getBuildings().size());
         assertEquals("building should not be built", false, map.getBuilding(conYardId).isAwaitingClick());
 
     }
 
     @Test
     public void unitConstructionWithMoneyScenario() {
         Map map = new Map(64,64, TileType.ROCK);
         gameService.setMap(map);
 
         Building factory = new Building();
         factory.setOwnerId(1);
         factory.setType(BuildingType.FACTORY);
         factory.setX(1);
         factory.setY(1);
         int factoryId = map.addBuilding(factory);
 
         // for electricity to be enough
         Building powerplant = new Building();
         powerplant.setOwnerId(1);
         powerplant.setType(BuildingType.POWERPLANT);
         powerplant.setX(5);
         powerplant.setY(5);
         map.addBuilding(powerplant);
 
 
         StartConstruction startConstruction = new StartConstruction();
         startConstruction.setBuilderId(factoryId);
         startConstruction.setPlayerId(1);
         startConstruction.setEntityToBuildId(ConstructionOption.TANK.getEntityToBuildIdOnJs());
 
         map.getPlayerById(1).setMoney(UnitType.BATTLE_TANK.getTicksToBuild() * UnitType.BATTLE_TANK.getCostPerTick());
 
         userActionService.registerAction(startConstruction);
 
         for (int i = 0; i < UnitType.BATTLE_TANK.getTicksToBuild()-1; i++) {
             gameService.tick();
         }
 
         assertEquals(0, map.getUnits().size());
 
         gameService.tick();
 
         // one last final tick
         assertEquals(1, map.getUnits().size());
         assertEquals(0, map.getPlayerById(1).getMoney());
     }
 
     @Test
     public void unitConstructionWhenBlocked() {
         Map map = new Map(64,64, TileType.ROCK);
         gameService.setMap(map);
 
         Building factory = new Building();
         factory.setOwnerId(1);
         factory.setType(BuildingType.FACTORY);
         factory.setX(0);
         factory.setY(0);
         int factoryId = map.addBuilding(factory);
 
         // turrets are blocking all possible exits from factory
         placeTurret(3,0,map,1);
         placeTurret(3,1,map,1);
         placeTurret(0,2,map,1);
         placeTurret(1,2,map,1);
         placeTurret(2,2,map,1);
         int lastTurretId = placeTurret(3,2,map,1);
 
         // for electricity to be enough
         Building powerplant = new Building();
         powerplant.setOwnerId(1);
         powerplant.setType(BuildingType.POWERPLANT);
         powerplant.setX(5);
         powerplant.setY(5);
         map.addBuilding(powerplant);
 
 
         StartConstruction startConstruction = new StartConstruction();
         startConstruction.setBuilderId(factoryId);
         startConstruction.setPlayerId(1);
         startConstruction.setEntityToBuildId(ConstructionOption.TANK.getEntityToBuildIdOnJs());
 
         map.getPlayerById(1).setMoney(UnitType.BATTLE_TANK.getTicksToBuild() * UnitType.BATTLE_TANK.getCostPerTick() + 100);
 
         userActionService.registerAction(startConstruction);
 
         for (int i = 0; i < UnitType.BATTLE_TANK.getTicksToBuild(); i++) {
             gameService.tick();
         }
 
         // unit is not built
         assertEquals(0, map.getUnits().size());
        assertEquals(100, map.getPlayerById(1).getMoney());
 
         // nothing happens even after another tick
         gameService.tick();
         assertEquals(0, map.getUnits().size());
        assertEquals(100 , map.getPlayerById(1).getMoney());
 
         assertNotNull(map.getBuilding(factoryId).getCurrentGoal());
 
         // remove blocking turret
         Point lastTurrentPosition = map.getBuilding(lastTurretId).getPoint();
         map.removeBuilding(lastTurretId);
 
         // unit shoudl be built now, because turret is no longer blocking the exit
         gameService.tick();
         assertEquals(1, map.getUnits().size());
         assertEquals(100, map.getPlayerById(1).getMoney());
         assertEquals(map.getUnits().get(0).getPoint(), lastTurrentPosition);
     }
 
     @Test
     public void twoUnitsConstructedSimultaneouslyWithOneLocationForBoth() {
         // second one of units should not be placed on the same location
 
         Map map = new Map(64,64, TileType.ROCK);
         gameService.setMap(map);
 
         Building factory = new Building();
         factory.setOwnerId(1);
         factory.setType(BuildingType.FACTORY);
         factory.setX(0);
         factory.setY(0);
         int factoryId = map.addBuilding(factory);
 
         Building factory2 = new Building();
         factory2.setOwnerId(1);
         factory2.setType(BuildingType.FACTORY);
         factory2.setX(3);
         factory2.setY(0);
         int factoryId2 = map.addBuilding(factory2);
 
 
         // turrets are blocking all possible exits from factories, except one
         placeTurret(6,0,map,1);
         placeTurret(6,1,map,1);
         placeTurret(6,2,map,1);
 
         placeTurret(0,2,map,1);
         placeTurret(1,2,map,1);
         placeTurret(2,2,map,1);
         placeTurret(4,2,map,1);
         placeTurret(5,2,map,1);
 
         // point 3,2 is shared between factories
 
         // for electricity to be enough
         Building powerplant = new Building();
         powerplant.setOwnerId(1);
         powerplant.setType(BuildingType.POWERPLANT);
         powerplant.setX(5);
         powerplant.setY(5);
         map.addBuilding(powerplant);
 
 
         StartConstruction startConstruction = new StartConstruction();
         startConstruction.setBuilderId(factoryId);
         startConstruction.setPlayerId(1);
         startConstruction.setEntityToBuildId(ConstructionOption.TANK.getEntityToBuildIdOnJs());
 
         StartConstruction startConstruction2 = new StartConstruction();
         startConstruction2.setBuilderId(factoryId2);
         startConstruction2.setPlayerId(1);
         startConstruction2.setEntityToBuildId(ConstructionOption.TANK.getEntityToBuildIdOnJs());
 
         userActionService.registerAction(startConstruction);
         userActionService.registerAction(startConstruction2);
 
         map.getPlayerById(1).setMoney(UnitType.BATTLE_TANK.getTicksToBuild() * UnitType.BATTLE_TANK.getCostPerTick() * 2);
 
         for (int i = 0; i < UnitType.BATTLE_TANK.getTicksToBuild(); i++) {
             gameService.tick();
         }
 
         // unit is not built
         assertEquals(1, map.getUnits().size());
 
 
     }
 
 
     @Test
     public void nonExistingConstructionYardDoesntCauseError() {
         Map map = new Map(64,64, TileType.ROCK);
         gameService.setMap(map);
 
         StartConstruction startConstruction = new StartConstruction();
         startConstruction.setBuilderId(1);
         startConstruction.setEntityToBuildId(BuildingType.POWERPLANT.getIdOnJS());
         userActionService.registerAction(startConstruction);
 
         gameService.tick();
         gameService.tick();
     }
 
     @Test
     public void constructionWithMoneyScenario() {
         Map map = new Map(64,64, TileType.ROCK);
         gameService.setMap(map);
 
         Building constructionYard = new Building();
         constructionYard.setOwnerId(1);
         constructionYard.setType(BuildingType.CONSTRUCTIONYARD);
         constructionYard.setX(1);
         constructionYard.setY(1);
         constructionYard.setHp(100);
         int conYardId =  map.addBuilding(constructionYard);
 
         BuildingType.POWERPLANT.setCostPerTick(10);
 
         map.getPlayerById(1).setMoney(BuildingType.POWERPLANT.getTicksToBuild() * BuildingType.POWERPLANT.getCostPerTick() - 1);
 
         StartConstruction startConstruction = new StartConstruction();
         startConstruction.setBuilderId(conYardId);
         startConstruction.setEntityToBuildId(BuildingType.POWERPLANT.getIdOnJS());
         startConstruction.setPlayerId(1);
         userActionService.registerAction(startConstruction);
 
 
         for (int i = 0; i < BuildingType.POWERPLANT.getTicksToBuild(); i++) {
             gameService.tick();
             assertEquals("building should not be build without money", false, map.getBuilding(conYardId).isAwaitingClick());
         }
 
         // one last final tick
         map.getPlayerById(1).setMoney(map.getPlayerById(1).getMoney() + 1);
         gameService.tick();
 
         assertEquals("building should be built", true, map.getBuilding(conYardId).isAwaitingClick());
         assertEquals("tick count should reset", 0, map.getBuilding(conYardId).getTicksAccumulated());
         assertEquals("building goal should be empty", null, map.getBuilding(conYardId).getCurrentGoal());
         assertEquals(0,map.getPlayerById(1).getMoney());
     }
 
     @Test
     public void constructionCancelledOnLastTickMoneyReturnedScenario() {
         Map map = new Map(64,64, TileType.ROCK);
         gameService.setMap(map);
 
         Building constructionYard = new Building();
         constructionYard.setOwnerId(1);
         constructionYard.setType(BuildingType.CONSTRUCTIONYARD);
         constructionYard.setX(1);
         constructionYard.setY(1);
         constructionYard.setHp(100);
         int conYardId = map.addBuilding(constructionYard);
 
         BuildingType.POWERPLANT.setCostPerTick(10);
 
         int moneyRequiredToBuyPowerPlant = BuildingType.POWERPLANT.getTicksToBuild() * BuildingType.POWERPLANT.getCostPerTick();
         map.getPlayerById(1).setMoney(moneyRequiredToBuyPowerPlant);
 
         StartConstruction startConstruction = new StartConstruction();
         startConstruction.setBuilderId(conYardId);
         startConstruction.setEntityToBuildId(BuildingType.POWERPLANT.getIdOnJS());
         startConstruction.setPlayerId(1);
         userActionService.registerAction(startConstruction);
 
 
         for (int i = 0; i < BuildingType.POWERPLANT.getTicksToBuild()-1; i++) {
             gameService.tick();
             assertEquals("building should not be build", false, map.getBuilding(conYardId).isAwaitingClick());
         }
 
         // cancel construction
         CancelConstruction cancelConstruction = new CancelConstruction();
         cancelConstruction.setBuilderId(conYardId);
         cancelConstruction.setPlayerId(1);
         userActionService.registerAction(cancelConstruction);
         // one last final tick
         gameService.tick();
 
         assertEquals("building should not be build", false, map.getBuilding(conYardId).isAwaitingClick());
         assertEquals("ticks should reset", 0, map.getBuilding(conYardId).getTicksAccumulated());
         assertEquals(moneyRequiredToBuyPowerPlant, map.getPlayerById(1).getMoney());
 
         // event after more ticks building should not be built and money should not be subtracted
         for (int i = 0; i < 10; i++) {
             gameService.tick();
             assertEquals("building should not be build", false, map.getBuilding(conYardId).isAwaitingClick());
             assertEquals(moneyRequiredToBuyPowerPlant, map.getPlayerById(1).getMoney());
         }
     }
 
     @Test
     public void constructionCancelledWhenBuildingReadyTickMoneyReturnedScenario() {
         Map map = new Map(64,64, TileType.ROCK);
         gameService.setMap(map);
 
         Building constructionYard = new Building();
         constructionYard.setOwnerId(1);
         constructionYard.setType(BuildingType.CONSTRUCTIONYARD);
         constructionYard.setX(1);
         constructionYard.setY(1);
         constructionYard.setHp(100);
         int conYardId = map.addBuilding(constructionYard);
 
         BuildingType.POWERPLANT.setCostPerTick(10);
 
         int moneyRequiredToBuyPowerPlant = BuildingType.POWERPLANT.getTicksToBuild() * BuildingType.POWERPLANT.getCostPerTick();
         map.getPlayerById(1).setMoney(moneyRequiredToBuyPowerPlant);
 
         StartConstruction startConstruction = new StartConstruction();
         startConstruction.setBuilderId(conYardId);
         startConstruction.setEntityToBuildId(BuildingType.POWERPLANT.getIdOnJS());
         startConstruction.setPlayerId(1);
         userActionService.registerAction(startConstruction);
 
 
         for (int i = 0; i < BuildingType.POWERPLANT.getTicksToBuild(); i++) {
             gameService.tick();
         }
 
         assertEquals("building should be built", true, map.getBuilding(conYardId).isAwaitingClick());
 
         // cancel construction
         CancelConstruction cancelConstruction = new CancelConstruction();
         cancelConstruction.setBuilderId(conYardId);
         cancelConstruction.setPlayerId(1);
         userActionService.registerAction(cancelConstruction);
         // one last final tick
         gameService.tick();
 
         assertEquals("building should not be build", false, map.getBuilding(conYardId).isAwaitingClick());
         assertEquals("ticks should reset", 0, map.getBuilding(conYardId).getTicksAccumulated());
         assertEquals(moneyRequiredToBuyPowerPlant, map.getPlayerById(1).getMoney());
 
         // event after more ticks building should not be built and money should not be subtracted
         for (int i = 0; i < 10; i++) {
             gameService.tick();
             assertEquals("building should not be build", false, map.getBuilding(conYardId).isAwaitingClick());
             assertEquals(moneyRequiredToBuyPowerPlant, map.getPlayerById(1).getMoney());
         }
     }
 
     @Test
     public void constructionScenario() {
         Map map = new Map(64,64, TileType.ROCK);
         gameService.setMap(map);
 
         Building constructionYard = new Building();
         constructionYard.setOwnerId(1);
         constructionYard.setType(BuildingType.CONSTRUCTIONYARD);
         constructionYard.setX(1);
         constructionYard.setY(1);
         constructionYard.setHp(100);
         int conYardId = map.addBuilding(constructionYard);
 
         Building anotherConstructionYard = new Building();
         anotherConstructionYard.setOwnerId(1);
         anotherConstructionYard.setType(BuildingType.CONSTRUCTIONYARD);
         anotherConstructionYard.setX(20);
         anotherConstructionYard.setY(3);
         anotherConstructionYard.setHp(100);
         int anotherConYardId = map.addBuilding(anotherConstructionYard);
 
         StartConstruction startConstruction = new StartConstruction();
         startConstruction.setBuilderId(conYardId);
         startConstruction.setEntityToBuildId(BuildingType.POWERPLANT.getIdOnJS());
         startConstruction.setPlayerId(1);
         userActionService.registerAction(startConstruction);
 
         for (int i = 0; i < BuildingType.POWERPLANT.getTicksToBuild()-1; i++) {
             gameService.tick();
             assertEquals("building should not be build faster", 2, map.getBuildings().size());
             assertEquals("building should not be build faster", false, map.getBuilding(conYardId).isAwaitingClick());
         }
 
         // one last final tick
         gameService.tick();
 
         assertEquals("building should be built", true, map.getBuilding(conYardId).isAwaitingClick());
         assertEquals("tick count should reset", 0, map.getBuilding(conYardId).getTicksAccumulated());
         assertEquals("building goal should be empty", null, map.getBuilding(conYardId).getCurrentGoal());
 
         placeTurret(14, 14, map, 1);
 
         PlaceBuilding placeBuilding = new PlaceBuilding();
         placeBuilding.setBuilderId(anotherConYardId);
         placeBuilding.setX(15);
         placeBuilding.setY(15);
         placeBuilding.setPlayerId(1);
         userActionService.registerAction(placeBuilding);
         gameService.tick();
 
         assertEquals("building should not be built from another construction yard", 0, map.getBuildingsByType(BuildingType.POWERPLANT).size());
         assertEquals("con yard should not be empty", true, map.getBuilding(conYardId).isAwaitingClick());
 
         placeBuilding = new PlaceBuilding();
         placeBuilding.setBuilderId(conYardId);
         placeBuilding.setX(2);
         placeBuilding.setY(2);
         placeBuilding.setPlayerId(1);
         userActionService.registerAction(placeBuilding);
         gameService.tick();
 
         assertEquals("building should not be built on nonempty location", 0, map.getBuildingsByType(BuildingType.POWERPLANT).size());
         assertEquals("con yard should not be empty", true, map.getBuilding(conYardId).isAwaitingClick());
 
         Unit unit = new Unit();
         unit.setX(16);
         unit.setY(16);
         unit.setUnitType(UnitType.BATTLE_TANK);
         map.addUnit(unit);
 
         placeTurret(14, 14, map, 1);
         placeBuilding = new PlaceBuilding();
         placeBuilding.setBuilderId(conYardId);
         placeBuilding.setX(15);
         placeBuilding.setY(15);
         placeBuilding.setPlayerId(1);
         userActionService.registerAction(placeBuilding);
         gameService.tick();
 
         assertEquals("building should not be built on a unit", 0, map.getBuildingsByType(BuildingType.POWERPLANT).size());
         assertEquals("con yard should not be empty", true, map.getBuilding(conYardId).isAwaitingClick());
 
         placeTurret(62, 62, map, 1);
 
         placeBuilding = new PlaceBuilding();
         placeBuilding.setBuilderId(conYardId);
         placeBuilding.setX(63);
         placeBuilding.setY(63);
         placeBuilding.setPlayerId(1);
         userActionService.registerAction(placeBuilding);
         gameService.tick();
 
         assertEquals("building should not be built on the edge of the map", 0, map.getBuildingsByType(BuildingType.POWERPLANT).size());
         assertEquals("con yard should not be empty", true, map.getBuilding(conYardId).isAwaitingClick());
 
         placeTurret(57, 57, map, 1);
 
         placeBuilding = new PlaceBuilding();
         placeBuilding.setBuilderId(conYardId);
         placeBuilding.setX(58);
         placeBuilding.setY(58);
         placeBuilding.setPlayerId(1);
         map.getTile(59,59).setTileType(TileType.SAND);
         userActionService.registerAction(placeBuilding);
         gameService.tick();
 
         assertEquals("building should not be built on the sand", 0, map.getBuildingsByType(BuildingType.POWERPLANT).size());
         assertEquals("con yard should not be empty", true, map.getBuilding(conYardId).isAwaitingClick());
 
 
         // there is turret nearby, but from wrong owner
         placeTurret(7, 7, map, 2);
 
         placeBuilding = new PlaceBuilding();
         placeBuilding.setBuilderId(conYardId);
         placeBuilding.setX(5);
         placeBuilding.setY(5);
         placeBuilding.setPlayerId(1);
         userActionService.registerAction(placeBuilding);
         gameService.tick();
 
         assertEquals("building should not be built without building nearby", 0, map.getBuildingsByType(BuildingType.POWERPLANT).size());
         assertEquals("con yard should not be empty", true, map.getBuilding(conYardId).isAwaitingClick());
 
 
         placeTurret(4, 4, map, 1);
 
         placeBuilding = new PlaceBuilding();
         placeBuilding.setBuilderId(conYardId);
         placeBuilding.setX(5);
         placeBuilding.setY(5);
         placeBuilding.setPlayerId(1);
         userActionService.registerAction(placeBuilding);
         gameService.tick();
 
         assertEquals("power plant is built", 1, map.getBuildingsByType(BuildingType.POWERPLANT).size());
         assertEquals("power plant hp is ok", BuildingType.POWERPLANT.getHp(), map.getBuildingsByType(BuildingType.POWERPLANT).get(0).getHp());
         assertEquals("con yard should be empty", false, map.getBuilding(conYardId).isAwaitingClick());
 
     }
 
     private int placeTurret(int x, int y, Map map, int playerId) {
         Building building = new Building();
         building.setOwnerId(playerId);
         building.setType(BuildingType.TURRET);
         building.setX(x);
         building.setY(y);
         return map.addBuilding(building);
     }
 
 
 }
