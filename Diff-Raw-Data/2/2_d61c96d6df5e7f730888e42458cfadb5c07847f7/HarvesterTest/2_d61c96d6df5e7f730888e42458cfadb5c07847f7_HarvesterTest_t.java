 package lv.k2611a.domain.unitgoals;
 
 import lv.k2611a.domain.*;
 import lv.k2611a.network.req.StartConstruction;
 import lv.k2611a.service.game.UserActionService;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import lv.k2611a.service.game.GameServiceImpl;
 import lv.k2611a.util.Point;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:testContext.xml"})
 public class HarvesterTest {
 
     @Autowired
     private GameServiceImpl gameService;
 
     @Autowired
     private UserActionService userActionService;
 
     @Before
     public void setup() {
         gameService.setTickCount(0);
     }
 
     @Test
     public void twoHarvestersCannotReturnToSameRefinery() {
         Map map = new Map(64,64);
 
         // set the numbers big to no let the harvest unload faster
         Harvest.TICKS_FOR_FULL = 100000;
         ReturnToBase.TICKS_COLLECTING_UNLOADED_PER_TICK = 1;
 
         Building building = new Building();
         building.setType(BuildingType.REFINERY);
         building.setX(1);
         building.setY(1);
         building.setOwnerId(1);
         map.addBuilding(building);
 
         Unit harvester = new Unit();
         harvester.setUnitType(UnitType.HARVESTER);
         harvester.setTicksCollectingSpice(Harvest.TICKS_FOR_FULL);
         harvester.setGoal(new ReturnToBase());
         harvester.setOwnerId(1);
         harvester.setX(5);
         harvester.setY(5);
         map.addUnit(harvester);
 
         Unit harvester2 = new Unit();
         harvester2.setUnitType(UnitType.HARVESTER);
         harvester2.setTicksCollectingSpice(Harvest.TICKS_FOR_FULL);
         harvester2.setGoal(new ReturnToBase());
         harvester2.setOwnerId(1);
         harvester2.setX(6);
         harvester2.setY(5);
         map.addUnit(harvester2);
 
         gameService.setMap(map);
 
         // should be enough ticks to return to base
         for (int i = 0; i < 1000; i++) {
             gameService.tick();
         }
 
         assertFalse(harvester.getPoint().equals(harvester2.getPoint()));
     }
 
     @Test
     public void notFullyLoadedHarvesterReturnsToBaseIfNoSpiceLeft() {
         Map map = new Map(64,64, TileType.SAND);
 
         // set the numbers big to no let the harvest unload faster
         Harvest.TICKS_FOR_FULL = 100000;
         ReturnToBase.TICKS_COLLECTING_UNLOADED_PER_TICK = 1;
 
 
         Building building = new Building();
         building.setType(BuildingType.REFINERY);
         building.setX(1);
         building.setY(3);
         building.setOwnerId(1);
         map.addBuilding(building);
 
         Unit harvesterWaitingToUnloaded = new Unit();
         harvesterWaitingToUnloaded.setUnitType(UnitType.HARVESTER);
         harvesterWaitingToUnloaded.setTicksCollectingSpice(Harvest.TICKS_FOR_FULL / 2);
         harvesterWaitingToUnloaded.setGoal(new Harvest());
         harvesterWaitingToUnloaded.setOwnerId(1);
         harvesterWaitingToUnloaded.setX(1);
         harvesterWaitingToUnloaded.setY(1);
         map.addUnit(harvesterWaitingToUnloaded);
 
 
         gameService.setMap(map);
 
         // should be enough ticks to return to base
         for (int i = 0; i < 1000; i++) {
             gameService.tick();
         }
 
         Point entranceToRefinery = new Point(2,4);
         assertEquals(entranceToRefinery, harvesterWaitingToUnloaded.getPoint());
         assertFalse(entranceToRefinery.equals(new Point(1,1)));
     }
 
     @Test
     public void unloadedHarvesterLeavesTheBaseEvenIfNoSpiceLeftOnTheMap() {
 
         Map map = new Map(64,64, TileType.SAND);
 
         // set the numbers big to no let the harvest unload faster
         Harvest.TICKS_FOR_FULL = 100000;
         ReturnToBase.TICKS_COLLECTING_UNLOADED_PER_TICK = 1;
 
 
         Building building = new Building();
         building.setType(BuildingType.REFINERY);
         building.setX(1);
         building.setY(3);
         building.setOwnerId(1);
         map.addBuilding(building);
 
         Unit harvesterWaitingToUnloaded = new Unit();
         harvesterWaitingToUnloaded.setUnitType(UnitType.HARVESTER);
         harvesterWaitingToUnloaded.setTicksCollectingSpice(Harvest.TICKS_FOR_FULL);
         harvesterWaitingToUnloaded.setGoal(new ReturnToBase());
         harvesterWaitingToUnloaded.setOwnerId(1);
         harvesterWaitingToUnloaded.setX(1);
         harvesterWaitingToUnloaded.setY(1);
         map.addUnit(harvesterWaitingToUnloaded);
 
         Unit unloadedHarvester = new Unit();
         unloadedHarvester.setUnitType(UnitType.HARVESTER);
         unloadedHarvester.setTicksCollectingSpice(1);
         unloadedHarvester.setGoal(new ReturnToBase());
         unloadedHarvester.setOwnerId(1);
         unloadedHarvester.setX(2);
         unloadedHarvester.setY(4);
         map.addUnit(unloadedHarvester);
 
         gameService.setMap(map);
 
         // should be enough ticks to return to base
         for (int i = 0; i < 1000; i++) {
             gameService.tick();
         }
 
         Point entranceToRefinery = new Point(2,4);
         assertEquals(entranceToRefinery, harvesterWaitingToUnloaded.getPoint());
         assertFalse(entranceToRefinery.equals(unloadedHarvester.getPoint()));
     }
 
     @Test
     public void harvesterLooksForAnotherRefineryIfNearestIsBlocked() {
         Map map = new Map(64,64);
 
         // set the numbers big to no let the harvest unload faster
         Harvest.TICKS_FOR_FULL = 100000;
         ReturnToBase.TICKS_COLLECTING_UNLOADED_PER_TICK = 1;
 
 
         // first refinery is near the harvester but its entrance is blocked by the factory
         Building building = new Building();
         building.setType(BuildingType.REFINERY);
         building.setX(1);
         building.setY(3);
         building.setOwnerId(1);
         map.addBuilding(building);
 
         building = new Building();
         building.setType(BuildingType.FACTORY);
         building.setX(1);
         building.setY(5);
         building.setOwnerId(1);
         map.addBuilding(building);
 
         building = new Building();
         building.setType(BuildingType.REFINERY);
         building.setX(1);
         building.setY(10);
         building.setOwnerId(1);
         map.addBuilding(building);
 
         Unit harvester = new Unit();
         harvester.setUnitType(UnitType.HARVESTER);
         harvester.setTicksCollectingSpice(Harvest.TICKS_FOR_FULL);
         harvester.setGoal(new ReturnToBase());
         harvester.setOwnerId(1);
         harvester.setX(1);
         harvester.setY(1);
         map.addUnit(harvester);
 
         gameService.setMap(map);
 
         // should be enough ticks to return to base
         for (int i = 0; i < 1000; i++) {
             gameService.tick();
         }
 
         Point entranceToSecondRefinery = new Point(2, 11);
         assertEquals(entranceToSecondRefinery, harvester.getPoint());
     }
 
     @Test
     public void newlyBuiltHarvesterCollectsSpice() {
         Map map = new Map(32,32);
 
         // set the numbers big to no let the harvest unload faster
         Harvest.TICKS_FOR_FULL = 100000;
         ReturnToBase.TICKS_COLLECTING_UNLOADED_PER_TICK = 1;
 
         Building refinery = new Building();
         refinery.setType(BuildingType.REFINERY);
         refinery.setX(3);
         refinery.setY(3);
         refinery.setOwnerId(1);
         map.addBuilding(refinery);
 
         // Factory builds a harvester that should start collecting spice
         Building factory = new Building();
         factory.setType(BuildingType.FACTORY);
         factory.setX(6);
         factory.setY(3);
         factory.setOwnerId(1);
         map.addBuilding(factory);
 
         map.getTile(10,10).setTileType(TileType.SPICE);
         map.getTile(10,11).setTileType(TileType.SPICE);
         map.getTile(10,12).setTileType(TileType.SPICE);
         map.getTile(10,13).setTileType(TileType.SPICE);
         map.getTile(10,14).setTileType(TileType.SPICE);
         map.getTile(10,15).setTileType(TileType.SPICE);
 
         gameService.setMap(map);
 
         StartConstruction startConstruction = new StartConstruction();
         startConstruction.setBuilderId(factory.getId());
         startConstruction.setPlayerId(1);
         startConstruction.setEntityToBuildId(ConstructionOption.HARVESTER.getEntityToBuildIdOnJs());
         userActionService.registerAction(startConstruction);
 
         map.getPlayerById(1).setMoney(100000);
 
         assertEquals(0,map.getUnits().size());
         assertEquals(2,map.getBuildings().size());
         assertEquals(100000,map.getPlayerById(1).getMoney());
 
         // should be enough ticks to return to base
        for (int i = 0; i < 10000; i++) {
             gameService.tick();
         }
 
         assertEquals(1,map.getUnits().size());
         assertEquals(2,map.getBuildings().size());
         assertEquals(100000 - UnitType.HARVESTER.getCost() + Tile.TICKS_IN_SPICE_TILE*ReturnToBase.MONEY_PER_TICK * 6,map.getPlayerById(1).getMoney());
     }
 }
