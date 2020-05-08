 /*
  * This file is part of Disconnected.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * Disconnected is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Disconnected is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.disconnected.graphics.desktop;
 
 import com.quartercode.disconnected.graphics.component.GraphicsState;
 import com.quartercode.disconnected.sim.Simulation;
 import de.matthiasmann.twl.GUI;
 
 /**
  * The desktop state renders the desktop of the local player to the graphics manager.
  * 
  * @see DesktopWidget
  */
 public class DesktopState extends GraphicsState {
 
     private final Simulation simulation;
 
     private DesktopWidget    desktopWidget;
 
     /**
      * Creates a new desktop state and sets it up.
      * 
      * @param simulation The simulation whose local player's desktop is rendered.
      */
     public DesktopState(Simulation simulation) {
 
        super("/ui/desktop/desktop.xml");
         setTheme("");
 
         this.simulation = simulation;
     }
 
     /**
      * Returns the simulation whose local player's desktop is rendered.
      * 
      * @return The simulation whose local player's desktop is rendered.
      */
     public Simulation getSimulation() {
 
         return simulation;
     }
 
     @Override
     protected void layout() {
 
         desktopWidget.setSize(getParent().getWidth(), getParent().getHeight());
     }
 
     @Override
     protected void afterAddToGUI(GUI gui) {
 
         desktopWidget = new DesktopWidget(simulation.getLocalPlayer().getComputer().getOperatingSystem().getDesktop());
         add(desktopWidget);
     }
 
 }
