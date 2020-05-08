 package com.cjra.battleships;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertTrue;
 
 public class ShipAvailablityTests extends VerticalSelectionTests {
 
     @Test
     public void noCellsSelected(){
         deploymentController.resetGrid();
 
         assertEquals(ShipType.NONE, testView.offeredShip);
     }
 
     @Test
     public void oneCellSelected(){
         resetGrid();
         selectInitialCell(4,3);
 
         assertEquals(ShipType.NONE, testView.offeredShip);
     }
 
     @Test
     public void placePatrolBoatOffered(){
         selectTwoCells();
 
         assertEquals(ShipType.PATROL_BOAT, testView.offeredShip);
     }
 
     @Test
     public void placeDestroyerOffered(){
         selectThirdCellAtBottom();
 
         assertEquals(ShipType.DESTROYER, testView.offeredShip);
     }
 
     @Test
     public void placeBattleshipOffered(){
         selectFourthCellAtTop();
 
         assertEquals(ShipType.BATTLESHIP, testView.offeredShip);
     }
 
     @Test
     public void placeAircraftCarrierOffered(){
         selectFiveCells();
 
         assertEquals(ShipType.AIRCRAFT_CARRIER, testView.offeredShip);
     }
 
     @Test
     public void allShipsAvailableToPlace(){
 
         assertTrue(testView.availableShips.contains(ShipType.PATROL_BOAT));
         assertTrue(testView.availableShips.contains(ShipType.DESTROYER));
         assertTrue(testView.availableShips.contains(ShipType.BATTLESHIP));
         assertTrue(testView.availableShips.contains(ShipType.AIRCRAFT_CARRIER));
     }
 
     @Test
     public void placeDestroyer(){
         placeDestroyerOffered();
 
         deploymentController.placeShip(ShipType.DESTROYER);
 
         assertFalse(testView.availableShips.contains(ShipType.DESTROYER));
     }
 
     @Test
     public void cannotPlaceShipOnEmptySelection(){
         deploymentController.resetGrid();
         deploymentController.placeShip(ShipType.DESTROYER);
 
         assertTrue(testView.availableShips.contains(ShipType.DESTROYER));
     }
 
     @Test
     public void cannotPlaceShipOnASingleCellSelection(){
         deploymentController.resetGrid();
         deploymentController.selectCell(3,3);
 
         deploymentController.placeShip(ShipType.PATROL_BOAT);
 
         assertTrue(testView.availableShips.contains(ShipType.PATROL_BOAT));
     }
 }
