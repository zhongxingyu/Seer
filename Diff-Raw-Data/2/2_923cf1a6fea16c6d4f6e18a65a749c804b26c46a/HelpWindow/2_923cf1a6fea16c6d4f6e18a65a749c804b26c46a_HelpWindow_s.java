 /******************************************************************************
  * This file is part of tf2-bot.                                              *
  *                                                                            *
  * tf2-bot is free software: you can redistribute it and/or modify            *
  * it under the terms of the GNU General Public License as published by       *
  * the Free Software Foundation, either version 3 of the License, or          *
  * (at your option) any later version.                                        *
  *                                                                            *
  * tf2-bot is distributed in the hope that it will be useful,                 *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
  * GNU General Public License for more details.                               *
  *                                                                            *
  * You should have received a copy of the GNU General Public License          *
  * along with tf2-bot.  If not, see <http://www.gnu.org/licenses/>.           *
  ******************************************************************************/
 
 package ui;
 
 import pojos.PointPlace;
 
 import javax.swing.*;
 import java.awt.*;
 
ublic class HelpWindow extends JFrame {
 
     private PointPlace pointPlace;
     private JPanel rootPanel = new JPanel();
     private JTextArea infoLabel = new JTextArea();
     private final int width = 300;
     private final int height = 200;
 
     public HelpWindow(PointPlace place) throws Exception {
         super();
         pointPlace = place;
         init();
         initPlace();
     }
 
     private void init(){
         add(rootPanel);
         setTitle("Help on " + pointPlace.toString());
         setSize(width, height);
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         Point curLoc = MouseInfo.getPointerInfo().getLocation();
         setLocation(curLoc.x-(width/2), curLoc.y-(height/2));
         setVisible(true);
     }
 
     private void initPlace(){
         infoLabel.setLineWrap(true);
         infoLabel.setEditable(false);
         rootPanel.add(infoLabel);
         switch(pointPlace){
             case smeltWeapons: {
                 infoLabel.setText("This is where the 'Smelt Weapons' \noption is.  Durr.");
             }
         }
     }
 }
