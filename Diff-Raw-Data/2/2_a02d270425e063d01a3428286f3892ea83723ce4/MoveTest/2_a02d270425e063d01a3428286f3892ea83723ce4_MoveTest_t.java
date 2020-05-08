 package lv.k2611a.domain;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import lv.k2611a.domain.goals.Move;
 import lv.k2611a.service.GameServiceImpl;
 
 import static org.junit.Assert.assertEquals;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testContext.xml"})
 public class MoveTest {
 
     @Autowired
     private GameServiceImpl gameService;
 
     @Before
     public void setup() {
         gameService.setTickCount(0);
     }
 
     @Test
     public void unitSpeedWorksCorrectly() {
         Map map = new Map(2,1);
         Unit unit = new Unit();
         unit.setGoal(new Move(1,0));
         int ID = 1;
         unit.setId(ID);
         unit.setUnitType(UnitType.LAUNCHER);
         unit.setX(0);
         unit.setY(0);
         map.getUnits().add(unit);
 
         gameService.setMap(map);
 
         for (int i = 0; i < UnitType.LAUNCHER.getSpeed() -1; i++) {
             gameService.tick();
             unit = gameService.getMap().getUnit(ID);
             assertEquals(0, unit.getX());
             assertEquals(0, unit.getY());
         }
 
         // one last final tick
         gameService.tick();
         unit = gameService.getMap().getUnit(ID);
         assertEquals(1, unit.getX());
         assertEquals(0,unit.getY());
 
 
     }
 
     @Test
     public void mapShouldBePassableByUnitsOccupyingAllHorizontalLines() {
         int mapHeight = 12;
         int mapWidth = 12;
         Map map = new Map(mapWidth,mapHeight);
         for (int horizontalLineNumber = 0; horizontalLineNumber < mapHeight; horizontalLineNumber++) {
             Unit unit = new Unit();
             unit.setGoal(new Move(mapWidth - 1,horizontalLineNumber));
             unit.setId(horizontalLineNumber);
             unit.setUnitType(UnitType.BATTLE_TANK);
             unit.setX(0);
             unit.setY(horizontalLineNumber);
             map.getUnits().add(unit);
         }
 
         gameService.setMap(map);
         // The tick count to wait is set to minimum . No ticks should be wasted
         int tickCountRequired = (mapWidth-1) * UnitType.BATTLE_TANK.getSpeed();
         for (int i = 0; i < tickCountRequired -1; i++) {
             gameService.tick();
         }
 
         // no other ticks have been made
         assertEquals(tickCountRequired-1, gameService.getTickCount());
 
         // Test that units travelled the map horizontally
         for (int horizontalLineNumber = 0; horizontalLineNumber < mapHeight; horizontalLineNumber++) {
             Unit unit = gameService.getMap().getUnit(horizontalLineNumber);
             assertEquals(horizontalLineNumber, unit.getY());
             assertEquals(mapWidth-2, unit.getX());
         }
 
 
         // one last tick to pass the map
         gameService.tick();
 
         for (int horizontalLineNumber = 0; horizontalLineNumber < mapHeight; horizontalLineNumber++) {
             Unit unit = gameService.getMap().getUnit(horizontalLineNumber);
             assertEquals(horizontalLineNumber, unit.getY());
             assertEquals(mapWidth-1, unit.getX());
         }
     }
 
     @Test
     public void mapShouldBePassableByUnitsOccupyingAllVerticalLines() {
         int mapHeight = 12;
         int mapWidth = 12;
         Map map = new Map(mapWidth,mapHeight);
         for (int verticalLineNumber = 0; verticalLineNumber < mapHeight; verticalLineNumber++) {
             Unit unit = new Unit();
             unit.setGoal(new Move(verticalLineNumber, mapHeight-1));
             unit.setId(verticalLineNumber);
             unit.setUnitType(UnitType.BATTLE_TANK);
             unit.setX(verticalLineNumber);
             unit.setY(0);
             map.getUnits().add(unit);
         }
 
         gameService.setMap(map);
         // The tick count to wait is set to minimum . Unit should not yet reach the final cell
         int tickCountRequired = UnitType.BATTLE_TANK.getSpeed() * (mapHeight-1);
         for (int i = 0; i <  tickCountRequired -1; i++) {
             gameService.tick();
         }
 
         // no other ticks have been made
         assertEquals(tickCountRequired-1, gameService.getTickCount());
 
         // Test that units travelled the map horizontally, Unit should not yet reach the final cell
         for (int verticalLineNumber = 0; verticalLineNumber < mapHeight; verticalLineNumber++) {
             Unit unit = gameService.getMap().getUnit(verticalLineNumber);
             assertEquals(verticalLineNumber, unit.getX());
             assertEquals(mapHeight-2, unit.getY());
         }
 
         // one last tick to pass the map
         gameService.tick();
 
         for (int verticalLineNumber = 0; verticalLineNumber < mapHeight; verticalLineNumber++) {
             Unit unit = gameService.getMap().getUnit(verticalLineNumber);
             assertEquals(verticalLineNumber, unit.getX());
             assertEquals(mapHeight-1, unit.getY());
         }
     }
 }
