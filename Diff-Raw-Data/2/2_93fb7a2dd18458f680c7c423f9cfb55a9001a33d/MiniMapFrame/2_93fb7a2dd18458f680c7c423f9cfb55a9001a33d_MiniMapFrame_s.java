 /**********************************************************************/
 /* Copyright 2013 KRV                                                 */
 /*                                                                    */
 /* Licensed under the Apache License, Version 2.0 (the "License");    */
 /* you may not use this file except in compliance with the License.   */
 /* You may obtain a copy of the License at                            */
 /*                                                                    */
 /*  http://www.apache.org/licenses/LICENSE-2.0                        */
 /*                                                                    */
 /* Unless required by applicable law or agreed to in writing,         */
 /* software distributed under the License is distributed on an        */
 /* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,       */
 /* either express or implied.                                         */
 /* See the License for the specific language governing permissions    */
 /* and limitations under the License.                                 */
 /**********************************************************************/
 package ui.graphical;
 
 // Graphical Libraries (AWT)
 import java.awt.*;
 
 // Graphical Libraries (Swing)
 import javax.swing.*;
 
 // Libraries
 import arena.Map;
 import parameters.*;
 import players.Player;
 
 // Import links
 import static parameters.Game.*;
 
 /**
 * <b>Graphical - MiniMapFrame</b><br>
  * Provides a small screen with the
  * user's view of all terrains.
  * 
  * @author Karina Suemi
  * @author Renato Cordeiro Ferreira
  * @author Vinicius Silva
  */
 public class MiniMapFrame extends JFrame
 {
     // View data model
     private Map map;
     private Player player;
     
     // Internal structures
     protected Panel miniMap;
     
     // Internal variables
     private boolean visible = true;
     
     /**
      * Default constructor.<br>
      * @param gui    Graphical set in which the 
      *               Panel is set
      * @param map    Map of the arena
      * @param player Owner of the User Interface
      */
     MiniMapFrame(Graphical gui, Map map, Player player)
     {
         // Setting MiniMapFrame attributes
         this.map    = map;
         this.player = player;
         
         //* ARENA SCREEN *********************************************//
             int RADIUS = 5;
             int x0 = 20;
             int y0 = 20;
             int MAP_WIDTH  = 2*x0 + (int)(RADIUS * MAP_SIZE * Math.sqrt(3) * 0.875);
             int MAP_HEIGHT = 2*y0 + (int)(RADIUS * 3 * MAP_SIZE/2 * 1.05);
                 
             this.miniMap = new Panel(
                 gui, map, player, RADIUS, 0, 0, MAP_WIDTH, MAP_HEIGHT 
             );
             this.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
             this.miniMap.hide(true); // No scenarios/items
             this.pack();
         
         //* VISIBILITY ***********************************************//
             this.add(this.miniMap);
             
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() { setVisible(visible); }
             });
     }
     
     /**
      * Auxiliar method to repaint frame.
      */
     void paint() { this.miniMap.repaint(); }
         
     /**
      * Auxiliar method to toggle the
      * Mini Map exhibition.
      */
     void toggle() 
     {
         this.visible = !this.visible;
         setVisible(this.visible);
     }
 }
