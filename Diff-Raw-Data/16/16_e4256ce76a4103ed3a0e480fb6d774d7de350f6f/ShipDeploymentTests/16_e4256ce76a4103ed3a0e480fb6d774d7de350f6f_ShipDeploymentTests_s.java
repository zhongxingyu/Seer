 package com.cjra.battleships;
 
 import org.junit.Test;
 
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 
 public class ShipDeploymentTests extends VerticalSelectionTests {
 
     @Test
     public void noShipsPlaced(){
 
         assertEquals(0, testView.deployedShips.length);
     }
 
     @Test
     public void placePatrolBoat(){
         deploymentController.placeShip(ShipType.PATROL_BOAT);
 
         assertEquals(CellType.SHIP, testView.latestGrid[4][4]);
         assertEquals(CellType.SHIP, testView.latestGrid[4][5]);
         assertOtherCellsEmpty(expectedSelections.toArray(new Position[0]));
     }
 
     @Test
     public void deployPatrolBoat(){
         deploymentController.placeShip(ShipType.PATROL_BOAT);
 
         assertEquals(ShipType.PATROL_BOAT, testView.deployedShips[0].getType());
     }
 
     @Test
     public void deployPatrolBoatToCorrectLocation(){
         deploymentController.placeShip(ShipType.PATROL_BOAT);
 
         List<Position> positions = testView.deployedShips[0].getPositions();
 
         assertEquals(2, positions.size());
         assertTrue(positions.get(0).matches(4,4));
         assertTrue(positions.get(1).matches(4, 5));
     }
 
     @Test
     public void deployDestroyer(){
         deploymentController.selectCell(4,6);
         deploymentController.placeShip(ShipType.DESTROYER);
 
         assertEquals(ShipType.DESTROYER, testView.deployedShips[0].getType());
     }
 
     @Test
     public void deployDestroyerToCorrectLocation(){
         deploymentController.selectCell(4,6);
         deploymentController.placeShip(ShipType.DESTROYER);
 
         List<Position> positions = testView.deployedShips[0].getPositions();
 
         assertEquals(3, positions.size());
         assertTrue(positions.get(0).matches(4, 4));
         assertTrue(positions.get(1).matches(4,5));
         assertTrue(positions.get(2).matches(4,6));
     }
 
     @Test
     public void deployPatrolBoatAndDestroyer(){
         deploymentController.placeShip(ShipType.PATROL_BOAT);
 
         deploymentController.selectCell(5,4);
         deploymentController.selectCell(5,5);
         deploymentController.selectCell(5,6);
         deploymentController.placeShip(ShipType.DESTROYER);
 
         assertEquals(2, testView.deployedShips.length);
     }
 
     @Test
     public void clearDeployment(){
         deployPatrolBoatAndDestroyer();
 
         deploymentController.resetGrid();
 
         assertEquals(0, testView.deployedShips.length);
         assertEquals(0, testView.selection.size());
         assertEquals(4, testView.availableShips.size());
     }
 
     @Test
     public void deployingAllShipsOffersGame(){
         deploymentController.resetGrid();
         deploymentController.selectCell(0,0);
         deploymentController.selectCell(0,1);
         deploymentController.placeShip(ShipType.PATROL_BOAT);
 
         deploymentController.selectCell(1,0);
         deploymentController.selectCell(1,1);
         deploymentController.selectCell(1,2);
         deploymentController.placeShip(ShipType.DESTROYER);
 
         deploymentController.selectCell(3,0);
         deploymentController.selectCell(3,1);
         deploymentController.selectCell(3,2);
         deploymentController.selectCell(3,3);
         deploymentController.placeShip(ShipType.BATTLESHIP);
 
         deploymentController.selectCell(4,0);
         deploymentController.selectCell(4,1);
         deploymentController.selectCell(4,2);
         deploymentController.selectCell(4,3);
         deploymentController.selectCell(4,4);
         deploymentController.placeShip(ShipType.AIRCRAFT_CARRIER);
 
         assertEquals(4, testView.deployedShips.length);
         assertTrue(testView.canStartGame);
     }
 }
