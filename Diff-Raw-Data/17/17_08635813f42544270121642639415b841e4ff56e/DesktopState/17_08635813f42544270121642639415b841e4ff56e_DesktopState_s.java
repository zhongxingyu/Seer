 
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
 
         super("/ui/desktop.xml");
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
