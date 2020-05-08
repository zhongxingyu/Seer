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
 
 package com.jostrobin.battleships.view.panels;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.swing.*;
 
 import com.jostrobin.battleships.data.Ship;
 import com.jostrobin.battleships.view.components.CellComponent;
 import com.jostrobin.battleships.view.listeners.SelectionListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author rowyss
  *         Date: 05.11.11 Time: 18:52
  */
 public class ShipsPanel extends JPanel implements ActionListener
 {
     private JButton rotate;
     private JButton rotateRightButton;
     private int y;
     private List<SelectionListener<Ship>> selectionListeners = new ArrayList<SelectionListener<Ship>>();
     private Map<Ship, JPanel> shipPanels = new HashMap<Ship, JPanel>();
     private static final Logger LOG = LoggerFactory.getLogger(ShipsPanel.class);
 
     public ShipsPanel()
     {
         initUi();
     }
 
     private void initUi()
     {
         setLayout(new GridBagLayout());
         rotate = new JButton("Rotate left");
         GridBagConstraints leftButtonConstraints = new GridBagConstraints();
         leftButtonConstraints.gridy = y++;
         leftButtonConstraints.anchor = GridBagConstraints.ABOVE_BASELINE;
         add(rotate, leftButtonConstraints);
     }
 
     public void updateShips(List<Ship> ships)
     {
         removeAllShips();
 
         MouseListener mouseListener = new MouseListener();
         for (Ship ship : ships)
         {
             if (ship.isPlaced())
             {
                 continue;
             }
             JPanel shipPanel = new JPanel();
             shipPanel.setLayout(new GridLayout(1, ship.getSize()));
             shipPanel.setPreferredSize(new Dimension(ship.getSize() * CellComponent.CELL_SIZE, CellComponent.CELL_SIZE));
             for (int i = 0; i < ship.getSize(); i++)
             {
                 CellComponent cell = new CellComponent(1, i);
 //                cell.setHighlight(false);
                 cell.setShip(ship);
                 cell.addMouseListener(mouseListener);
                 ship.addCell(cell);
                 shipPanel.add(cell);
             }
             GridBagConstraints shipConstraints = new GridBagConstraints();
             shipConstraints.gridy = y++;
             shipConstraints.anchor = GridBagConstraints.BASELINE;
             shipConstraints.insets = new Insets(2, 2, 2, 2);
             shipPanels.put(ship, shipPanel);
             add(shipPanel, shipConstraints);
         }
        revalidate();
     }
 
     private void removeAllShips()
     {
 
         for (JPanel shipPanel : shipPanels.values())
         {
             remove(shipPanel);
             y--;
         }
         shipPanels.clear();
     }
 
     public void addSelectionListener(SelectionListener<Ship> selectionListener)
     {
         selectionListeners.add(selectionListener);
     }
 
     @Override
     public void actionPerformed(ActionEvent actionEvent)
     {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private class MouseListener extends MouseAdapter
     {
         @Override
         public void mouseClicked(MouseEvent mouseEvent)
         {
             if (mouseEvent.getSource() instanceof CellComponent)
             {
                 CellComponent cell = (CellComponent) mouseEvent.getSource();
                 cell.setSelected(true);
                 Ship ship = cell.getShip();
 
                 LOG.debug("Ship with size {} was selected", ship.getSize());
                 for (SelectionListener<Ship> selectionListener : selectionListeners)
                 {
                     selectionListener.selected(ship);
                 }
             }
         }
 
     }
 }
