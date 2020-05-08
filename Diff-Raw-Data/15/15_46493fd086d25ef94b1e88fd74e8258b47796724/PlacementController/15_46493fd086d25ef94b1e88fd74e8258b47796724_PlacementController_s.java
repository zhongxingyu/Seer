 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jostrobin.battleships.controller;
 
 import com.jostrobin.battleships.ApplicationController;
 import com.jostrobin.battleships.common.data.Cell;
 import com.jostrobin.battleships.common.data.Orientation;
 import com.jostrobin.battleships.common.data.Ship;
 import com.jostrobin.battleships.common.data.enums.CellType;
 import com.jostrobin.battleships.common.network.Command;
 import com.jostrobin.battleships.common.network.NetworkListener;
 import com.jostrobin.battleships.model.PlacementModel;
 import com.jostrobin.battleships.view.listeners.EventListener;
 import com.jostrobin.battleships.view.listeners.SelectionListener;
 import com.jostrobin.battleships.view.panels.PlacementPanel;
 import org.springframework.beans.factory.InitializingBean;
 
 /**
  * @author rowyss
  *         Date: 09.11.11 Time: 16:44
  */
 public class PlacementController implements InitializingBean, NetworkListener
 {
     private PlacementPanel placementPanel;
     private PlacementModel model;
     private ApplicationController applicationController;
 
     @Override
     public void afterPropertiesSet() throws Exception
     {
         applicationController.addNetworkListener(this);
 
         placementPanel.addCellSelectionListener(new CellSelectionListener());
         placementPanel.addShipSelectionListener(new ShipSelectionListener());
         placementPanel.addRotationListener(new RotationListener());
         placementPanel.addReadyListener(new ReadyListener());
     }
 
     private void rotateShip()
     {
         Ship ship = findSelectedShip();
         if (ship != null)
         {
             Orientation orientation = switchOrientation(ship.getOrientation());
             if (ship.isPlaced())
             {
                 if (canBePlaced(ship, ship.getPositionX(), ship.getPositionY(), orientation))
                 {
                     ship.setOrientation(orientation);
                     placeShip(ship, ship.getPositionX(), ship.getPositionY());
                 }
             }
             else
             {
                 ship.setOrientation(orientation);
                 ship.setSelected(true);
                 placementPanel.updateShips();
             }
         }
     }
 
 
     private void deselectOtherShips(Ship ship)
     {
         ship.setSelected(true);
         for (Ship otherShip : model.getShips())
         {
             if (otherShip != ship)
             {
                 otherShip.setSelected(false);
             }
         }
     }
 
     public void selected(Cell cell)
     {
         if (cell.getShip() != null)
         {
             Ship ship = cell.getShip();
             if (ship.isSelected())
             {
                 ship.setSelected(false);
             }
             else
             {
                 cell.getShip().setSelected(true);
                 deselectOtherShips(ship);
             }
         }
         else
         {
             Ship ship = findSelectedShip();
             if (ship != null && placeShip(ship, cell.getBoardX(), cell.getBoardY()))
             {
                 placementPanel.updateShips();
                 checkAllShipsPlaced();
             }
         }
     }
 
     private void checkAllShipsPlaced()
     {
         boolean ready = true;
         for (Ship ship : model.getShips())
         {
             ready &= ship.isPlaced();
         }
         placementPanel.enableReadyButton(ready);
     }
 
     public void selected(Ship ship)
     {
         if (ship.isSelected())
         {
             ship.setSelected(false);
         }
         else
         {
             deselectOtherShips(ship);
         }
     }
 
 
     private boolean placeShip(Ship ship, int x, int y)
     {
         boolean placed = false;
         if (canBePlaced(ship, x, y, ship.getOrientation()))
         {
             ship.setPosition(x, y);
             ship.clearCells();
             for (int i = 0; i < ship.getSize(); i++)
             {
                 ship.addCell(placementPanel.findCellAt(x, y));
                 if (ship.getOrientation() == Orientation.HORIZONTAL)
                 {
                     x++;
                 }
                 else
                 {
                     y++;
                 }
             }
             ship.setSelected(true);
             ship.setPlaced(true);
             placed = true;
         }
         return placed;
     }
 
     private boolean canBePlaced(Ship ship, int x, int y, Orientation orientation)
     {
         boolean ok = true;
         for (int i = 0; i < ship.getSize(); i++)
         {
             Cell cell = placementPanel.findCellAt(x, y);
             ok &= (cell != null && (cell.getType().equals(CellType.WATER) || ship.getCells().contains(cell)));
             if (orientation == Orientation.HORIZONTAL)
             {
                 x++;
             }
             else
             {
                 y++;
             }
         }
         return ok;
     }
 
     private Ship findSelectedShip()
     {
         for (Ship ship : model.getShips())
         {
             if (ship.isSelected())
             {
                 return ship;
             }
         }
         return null;
     }
 
     private Orientation switchOrientation(Orientation orientation)
     {
         if (Orientation.HORIZONTAL.equals(orientation))
         {
             return Orientation.VERTICAL;
         }
         else
         {
             return Orientation.HORIZONTAL;
         }
     }
 
     public PlacementModel getModel()
     {
         return model;
     }
 
     public void setModel(PlacementModel model)
     {
         this.model = model;
     }
 
     public PlacementPanel getPlacementPanel()
     {
         return placementPanel;
     }
 
     public void setPlacementPanel(PlacementPanel placementPanel)
     {
         this.placementPanel = placementPanel;
     }
 
     public ApplicationController getApplicationController()
     {
         return applicationController;
     }
 
     public void setApplicationController(ApplicationController applicationController)
     {
         this.applicationController = applicationController;
     }
 
     private class ShipSelectionListener implements SelectionListener<Ship>
     {
 
         @Override
         public void selected(Ship ship)
         {
             PlacementController.this.selected(ship);
         }
     }
 
     private class CellSelectionListener implements SelectionListener<Cell>
     {
 
         @Override
         public void selected(Cell cell)
         {
             PlacementController.this.selected(cell);
         }
     }
 
 
     private class RotationListener implements EventListener<Object>
     {
 
         @Override
         public void actionPerformed(Object value)
         {
             rotateShip();
         }
     }
 
     private class ReadyListener implements EventListener<Object>
     {
 
         @Override
         public void actionPerformed(Object value)
         {
             applicationController.shipsPlaced(model.getShips());
         }
     }
 
     @Override
     public void notify(Command command)
     {
         switch (command.getCommand())
         {
             case Command.START_GAME:
                applicationController.showGameView(command.getStartingPlayer());
                 break;
         }
     }
 }
