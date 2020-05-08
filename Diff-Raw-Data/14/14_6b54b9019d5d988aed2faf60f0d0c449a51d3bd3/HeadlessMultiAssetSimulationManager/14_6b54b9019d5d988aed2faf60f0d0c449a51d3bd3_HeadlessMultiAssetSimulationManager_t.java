 /*
  * investovator, Stock Market Gaming Framework
  *     Copyright (C) 2013  investovator
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.investovator.agentsimulation.multiasset.simulation;
 
 import net.sourceforge.jabm.SimulationManager;
 import net.sourceforge.jabm.SpringSimulationController;
 import net.sourceforge.jabm.report.Report;
 import net.sourceforge.jabm.spring.BeanFactorySingleton;
 import net.sourceforge.jasa.market.MarketSimulation;
 import org.apache.log4j.Logger;
 import org.investovator.agentsimulation.exchange.Exchange;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * @author ishan
  * @version ${Revision}
  */
 public class HeadlessMultiAssetSimulationManager extends SimulationManager {
 
     public static final String SIMULATION_MANAGER_BEAN = "simulationManager";
 
     private static ArrayList<SpringSimulationController> simulationControllers;
 
     static Logger logger = Logger.getLogger(HeadlessMultiAssetSimulationManager.class);
 
     protected Thread simulationThread;
 
     private HeadlessMultiAssetSimulationManager headlessMultiAssetSimulationManager;
 
     public HeadlessMultiAssetSimulationManager() {
 //        SystemProperties systemProperties = SystemProperties
 //                .jabsConfiguration();
 //        propFile = systemProperties
 //                .getProperty(SystemProperties.PROPERTY_PROPFILE);
 //        varFile = systemProperties.getProperty(
 //                SystemProperties.PROPERTY_VARFILE);
 //        baseDirName = systemProperties.getProperty(
 //                SystemProperties.PROPERTY_BASE_DIR_NAME, "data");
 //        configOnly = Boolean.parseBoolean(systemProperties.getProperty(
 //                SystemProperties.PROPERTY_CONFIG_ONLY, "false"));
 //        generateSeeds = Boolean.parseBoolean(systemProperties.getProperty(
 //                SystemProperties.PROPERTY_SEEDS, "false"));
 //        seedMask = Integer.parseInt(systemProperties.getProperty(
 //                SystemProperties.PROPERTY_SEED_MASK, "-1"));
     }
 
     public HeadlessMultiAssetSimulationManager getHeadlessMultiAssetSimulationManager(){

        BeanFactorySingleton.initialiseFactory();


         return  (HeadlessMultiAssetSimulationManager) BeanFactorySingleton
                 .getBean(SIMULATION_MANAGER_BEAN);
     }
 
     @Override
     public void run() {
         this.headlessMultiAssetSimulationManager=getHeadlessMultiAssetSimulationManager();
         if (propFile != null) {
             runSingleExperiment(propFile);
         } else {
             if (varFile != null) {
                 setup(varFile, baseDirName, generateSeeds,
                         seedMask);
             } else {
                 if (configOnly) {
                     setup(baseDirName, generateSeeds,
                             seedMask);
                 } else {
                     runSingleExperiment();
                 }
             }
         }
     }
 
     public void runSingleExperiment() {
         launchSimulations();
         //TODO - what does this line do? (from MultiAssetSimManager)
         this.simulationThread = new Thread(this);
     }
 
     public void launchSimulations() {
         logger.info("Starting...");
         long start = System.currentTimeMillis();
 
         ArrayList<Thread> simulationThreads = new ArrayList<Thread>();
         for (SpringSimulationController controller : simulationControllers){
             Thread controllerThread = new Thread(controller);
             simulationThreads.add(controllerThread);
         }
 
         for (Thread simulationThread : simulationThreads){
             simulationThread.start();
         }
 
         for (Thread simulationThread : simulationThreads){
             try {
                 simulationThread.join();
             } catch (InterruptedException ignored) {
                 //TODO
             }
         }
 
         long finish = System.currentTimeMillis();
         long duration = finish - start;
         logger.info("all done.");
         logger.info("completed simulation(s) in " + duration + "ms.");
     }
 
     public void slow(int slowSleepInterval) {
         for (SpringSimulationController controller: simulationControllers) {
             controller.slow(slowSleepInterval);
         }
     }
 
     public void setSimulations(ArrayList<SpringSimulationController> securities) {
         System.out.println("called");
         simulationControllers = new ArrayList<SpringSimulationController>(securities);
     }
 
     public void terminate() {
         for (SpringSimulationController controller: simulationControllers) {
             controller.terminate();
         }
     }
 
     public void pause() {
         for (SpringSimulationController controller: simulationControllers) {
             controller.getSimulation().pause();
         }
     }
 
     public void resume(){
         for (SpringSimulationController controller: simulationControllers) {
             controller.getSimulation().resume();
         }
     }
 
     public HashMap <String, ArrayList<Report>> getReports() {
         HashMap <String, ArrayList<Report>> reports = new HashMap<String, ArrayList<Report>>();
         for (SpringSimulationController controller: simulationControllers) {
             reports.put(((MarketSimulation)controller.getSimulation()).getStockID(),
                     controller.getReports());
         }
         return reports;
     }
 
     public Exchange getExchange(){
         return (Exchange) BeanFactorySingleton.getBean("exchange");
     }
 
 
     public SpringSimulationController getController(String stockID){
         for (SpringSimulationController controller: simulationControllers) {
             if( ((MarketSimulation)controller.getSimulation()).getStockID().equals(stockID) ) return controller;
         }
         return null;
     }
 
 
     public static void main(String[] args) {
         HeadlessMultiAssetSimulationManager manager = new HeadlessMultiAssetSimulationManager();
         manager.run();
     }
 
 //    public void setup(String baseDirName, boolean generateSeeds,
 //                      int seedMask) {
 //
 //        logger.info("Creating experiment with no variable bindings...");
 //        logger.debug("baseDirName = " + baseDirName);
 //        BeanFactory beanFactory = BeanFactorySingleton.getBeanFactory();
 //        Map<String, String> emptyBindings = new HashMap<String, String>();
 //        SimulationExperiment experiment = new SimulationExperiment(beanFactory,
 //                baseDirName, 0, emptyBindings, generateSeeds, seedMask);
 //        experiment.createPropertyFile();
 //        logger.info("done.");
 //    }
 
 
 
 }
