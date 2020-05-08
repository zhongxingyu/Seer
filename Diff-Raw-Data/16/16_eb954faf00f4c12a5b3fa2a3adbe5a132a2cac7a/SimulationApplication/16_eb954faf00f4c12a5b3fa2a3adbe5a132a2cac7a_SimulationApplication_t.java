 /*
  * Copyright (c) 2010, Soar Technology, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * 
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  * 
  * * Neither the name of Soar Technology, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without the specific prior written permission of Soar Technology, Inc.
  * 
  * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Created on May 22, 2007
  */
 package com.soartech.simjr.ui;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.net.MalformedURLException;
 
 import org.apache.log4j.Logger;
 
 import com.soartech.math.geotrans.Geodetic;
 import com.soartech.math.geotrans.Mgrs;
 import com.soartech.simjr.NullProgressMonitor;
 import com.soartech.simjr.ProgressMonitor;
 import com.soartech.simjr.SimJrProps;
 import com.soartech.simjr.SimulationException;
 import com.soartech.simjr.app.ApplicationState;
 import com.soartech.simjr.app.DefaultApplicationStateService;
 import com.soartech.simjr.scripting.ScriptRunner;
 import com.soartech.simjr.services.DefaultServiceManager;
 import com.soartech.simjr.sim.ScenarioLoader;
 import com.soartech.simjr.sim.SimpleTerrain;
 import com.soartech.simjr.sim.Simulation;
 import com.soartech.simjr.sim.SimulationListenerAdapter;
 import com.soartech.simjr.ui.actions.ActionManager;
 import com.soartech.simjr.util.FileTools;
 
 /**
  * @author ray
  */
 public class SimulationApplication extends DefaultServiceManager
 {
     private static final Logger logger = Logger.getLogger(SimulationApplication.class);
             
     public final WindowAdapter exitHandler = new WindowAdapter()
     {
         public void windowClosed(WindowEvent arg0)
         {
             try
             {
                 shutdownServices();
             }
             catch (SimulationException e)
             {
                 logger.error(e);
             }
         }
     };
     
     private DefaultApplicationStateService appState = new DefaultApplicationStateService();
     private ActionManager actionManager;
     
     public SimulationApplication()
     {
     }
 
     public void initialize(ProgressMonitor progress, String[] args)
     {
         progress = NullProgressMonitor.createIfNull(progress);
         
         addService(appState);
         addService(actionManager = findService(ActionManager.class));
         
         progress.subTask("Initializing scripting support ...");
         addService(new ScriptRunner(this));        
         
         createSimulation(progress);
                                 
         try
         {
             loadActions(progress);
             
             final SimulationMainFrame mainFrame = createMainFrame(progress);   
                         
             loadScenario(progress, args);
             
             completeInitialization(mainFrame);
         }
         catch (Throwable e)
         {
            logger.error("Application initialization failure: " + e.getMessage());
            logger.debug("stack trace", e);
             try
             {
                 shutdownServices();
             }
             catch (SimulationException e1)
             {
                 logger.error(e1);
             }
         }
         finally
         {
             appState.setState(ApplicationState.RUNNING);
         }
     }
 
     private void loadActions(ProgressMonitor progress) throws Exception
     {
         // Install default actions from javascript
         final ScriptRunner scriptRunner = findService(ScriptRunner.class);
         scriptRunner.runResource(progress, getClass().getClassLoader(), SimJrProps.get("simjr.actions.path", "/simjr.actions.js"));        
     }
     
     private void loadScenario(ProgressMonitor progress, String[] args)
             throws SimulationException, Exception
     {
         progress.subTask("Loading scenario ...");
         final ScriptRunner scriptRunner = findService(ScriptRunner.class);
         final ScenarioLoader loader = new ScenarioLoader(this);
         for(String arg : args)
         {
             final String ext = FileTools.getExtension(arg).toLowerCase();
             if(ext.equals("xml") || ext.equals("sjx"))
             {
                 loader.loadScenario(new File(arg), progress);
             }
             else
             {
                 scriptRunner.run(progress, new File(arg));
             }
         }
     }
 
     private void completeInitialization(SimulationMainFrame mainFrame)
     {
         actionManager.updateActions();
         mainFrame.toFront();
         mainFrame.requestFocus();
     }
 
     private void createSimulation(ProgressMonitor progress)
     {
         // Set up the simulation with a default terrain. It can be set later by a scenario script
         // For now, we're doing this here so that the global simulation variable can be set correctly
         // in simjr.common.js. TODO: get rid of the global variable or something.
         progress.subTask("Initializing simulation ...");
         Geodetic.Point origin = new Mgrs().toGeodetic(SimJrProps.get("simjr.simulation.defaultOriginMgrs", "11SMS6025093000"));
         SimpleTerrain terrain = new SimpleTerrain(origin);
         Simulation simulation = new Simulation(terrain);
         simulation.addListener(new SimListener());
         addService(simulation);
     }
 
     private SimulationMainFrame createMainFrame(ProgressMonitor progress)
     {
         progress.subTask("Initializing main window ...");
         SimulationMainFrame mainFrame = new SimulationMainFrame(this);
         mainFrame.setDefaultCloseOperation(SimulationMainFrame.DISPOSE_ON_CLOSE);
         mainFrame.addWindowListener(exitHandler);
         mainFrame.setLocationRelativeTo(null);
         mainFrame.setIconImage(SimulationImages.SIMJR_ICON.getImage());
         mainFrame.setVisible(true);
         return mainFrame;
     }
         
     /* (non-Javadoc)
      * @see com.soartech.simjr.ServiceManager#shutdownServices()
      */
     public void shutdownServices() throws SimulationException
     {
         super.shutdownServices();
         logger.info("Exiting");
         System.exit(0);
     }
     
     private class SimListener extends SimulationListenerAdapter
     {
         public void onPause()
         {   
             actionManager.updateActions();
         }
 
         public void onStart()
         {
             actionManager.updateActions();
         }
     }
  
     private static SplashScreen showSplashScreen()
     {
         SplashScreen splashScreen = null;
         if(SimJrProps.get("simjr.splash.enabled", true))
         {
             try
             {
                 final String splashImage = SimJrProps.get("simjr.splash.image", "/simjr/images/simjr-splash.png");
                 splashScreen = new SplashScreen(splashImage);
                 splashScreen.setAlwaysOnTop(SimJrProps.get("simjr.splash.alwaysOnTop", true));
                 splashScreen.splash();
             }
             catch (MalformedURLException e)
             {
                 logger.error("Failed to display splash screen: " + e.getMessage());
             }
         }
         return splashScreen;
     }
     
     private static void hideSplashScreen(SplashScreen splashScreen)
     {
         if(splashScreen != null)
         {
             splashScreen.hide();
         }
     }
     
     /**
      * @param args
      */
     public static void main(final String[] args)
     {
         main(new SimulationApplication(), args);
     }
 
     public static void main(final SimulationApplication app, final String[] args)
     {
         logger.info("Sim Jr started");
         logger.info("   simjr.home=" + SimJrProps.get(SimJrProps.HOME));
         logger.info("   current directory = " + System.getProperty("user.dir"));
         
         SplashScreen splashScreen = showSplashScreen();
         
         app.initialize(splashScreen, args);
         
         hideSplashScreen(splashScreen);
     }
 }
