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
 
 package com.quartercode.disconnected;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 import com.quartercode.disconnected.graphics.GraphicsManager;
 import com.quartercode.disconnected.graphics.desktop.DesktopState;
 import com.quartercode.disconnected.profile.ProfileManager;
 import com.quartercode.disconnected.sim.Simulation;
 import com.quartercode.disconnected.sim.comp.hardware.CPU;
 import com.quartercode.disconnected.sim.comp.hardware.HardDrive;
 import com.quartercode.disconnected.sim.comp.hardware.Mainboard;
 import com.quartercode.disconnected.sim.comp.hardware.NetworkInterface;
 import com.quartercode.disconnected.sim.comp.hardware.RAM;
 import com.quartercode.disconnected.sim.comp.program.ExploitProgram;
 import com.quartercode.disconnected.sim.comp.program.SystemViewerProgram;
 import com.quartercode.disconnected.sim.member.ai.PlayerController;
 import com.quartercode.disconnected.sim.member.ai.UserController;
 import com.quartercode.disconnected.sim.member.interest.DestroyInterest;
 import com.quartercode.disconnected.sim.run.TickAction;
 import com.quartercode.disconnected.sim.run.TickSimulator;
 import com.quartercode.disconnected.sim.run.TickTimer;
 import com.quartercode.disconnected.sim.run.Ticker;
 import com.quartercode.disconnected.sim.run.util.SimulationGenerator;
 import com.quartercode.disconnected.util.LogExceptionHandler;
 
 /**
  * The main class which initalizes the whole game.
  */
 public class Main {
 
     private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
 
     /**
      * The main method which initalizes the whole game.
      * 
      * @param args The command line arguments.
      */
     public static void main(String[] args) {
 
         // Logging configuration
         try {
             LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/config/logging.properties"));
         }
         catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Can't load logging configuration", e);
             return;
         }
 
         // Default exception handler if the vm throws an exception to the entry point of thread (e.g. main() or run())
         Thread.setDefaultUncaughtExceptionHandler(new LogExceptionHandler());
 
         // Print information about the software
         LOGGER.info("Version " + Disconnected.getVersion());
 
        // Initalize registry
         Disconnected.setRegistry(new Registry());
         fillRegistry();
 
         // Initalize profile manager and load stored profiles (TODO: Add code for loading).
         Disconnected.setProfileManager(new ProfileManager());
 
         // Initalize graphics manager and start it
         Disconnected.setGraphicsManager(new GraphicsManager());
         Disconnected.getGraphicsManager().setRunning(true);
 
         // Initalize ticker
         List<TickAction> tickActions = new ArrayList<TickAction>();
         tickActions.add(new TickTimer());
         tickActions.add(new TickSimulator());
         Disconnected.setTicker(new Ticker(tickActions.toArray(new TickAction[tickActions.size()])));
 
         // DEBUG: Generate and set new simulation
         Simulation simulation = SimulationGenerator.generateSimulation(10, 2);
         Disconnected.setSimulation(simulation);
         // DEBUG: Start "game" with current simulation
         Disconnected.getTicker().setRunning(true);
         Disconnected.getGraphicsManager().setState(new DesktopState(simulation));
     }
 
     /**
      * Fills the active registry with the default values which are needed for running vanilla disconnected.
      */
     public static void fillRegistry() {
 
         // Hardware
         Disconnected.getRegistry().registerClass(Mainboard.class);
         Disconnected.getRegistry().registerClass(CPU.class);
         Disconnected.getRegistry().registerClass(RAM.class);
         Disconnected.getRegistry().registerClass(HardDrive.class);
         Disconnected.getRegistry().registerClass(NetworkInterface.class);
 
         // Programs
         Disconnected.getRegistry().registerClass(SystemViewerProgram.class);
         Disconnected.getRegistry().registerClass(ExploitProgram.class);
 
         // AI Controllers
         Disconnected.getRegistry().registerClass(PlayerController.class);
         Disconnected.getRegistry().registerClass(UserController.class);
 
         // Interests
         Disconnected.getRegistry().registerClass(DestroyInterest.class);
     }
 
     private Main() {
 
     }
 
 }
