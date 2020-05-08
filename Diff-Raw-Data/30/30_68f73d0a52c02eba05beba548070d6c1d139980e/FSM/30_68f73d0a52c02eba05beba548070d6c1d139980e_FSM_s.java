 /*
  * Claudia Project
  * http://claudia.morfeo-project.org
  *
  * (C) Copyright 2010 Telefonica Investigacion y Desarrollo
  * S.A.Unipersonal (Telefonica I+D)
  *
  * See CREDITS file for info about members and contributors.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the Affero GNU General Public License (AGPL) as
  * published by the Free Software Foundation; either version 3 of the License,
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the Affero GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * If you want to use this software an plan to distribute a
  * proprietary application in any way, and you are not licensing and
  * distributing your source code under AGPL, you probably need to
  * purchase a commercial license of the product. Please contact
  * claudia-support@lists.morfeo-project.org for more information.
  */
 package com.telefonica.claudia.slm.lifecyclemanager;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.dmtf.schemas.ovf.envelope._1.VirtualSystemType;
 import org.json.JSONException;
 
 import com.telefonica.claudia.configmanager.lb.LoadBalancerConfigurator;
 import com.telefonica.claudia.slm.common.DbManager;
 import com.telefonica.claudia.slm.common.SMConfiguration;
 import com.telefonica.claudia.slm.deployment.Customer;
 import com.telefonica.claudia.slm.deployment.Rule;
 import com.telefonica.claudia.slm.deployment.ServiceApplication;
 import com.telefonica.claudia.slm.deployment.ServiceKPI;
 import com.telefonica.claudia.slm.deployment.VEE;
 import com.telefonica.claudia.slm.deployment.VEEReplica;
 import com.telefonica.claudia.slm.deployment.hwItems.CPU;
 import com.telefonica.claudia.slm.deployment.hwItems.Disk;
 import com.telefonica.claudia.slm.deployment.hwItems.DiskConf;
 import com.telefonica.claudia.slm.deployment.hwItems.NIC;
 import com.telefonica.claudia.slm.deployment.hwItems.NICConf;
 import com.telefonica.claudia.slm.deployment.hwItems.Network;
 import com.telefonica.claudia.slm.eventsBus.events.SMControlEvent;
 import com.telefonica.claudia.slm.maniParser.ManiParserException;
 import com.telefonica.claudia.slm.maniParser.Parser;
 import com.telefonica.claudia.slm.monitoring.MonitoringRestBusConnector;
 import com.telefonica.claudia.slm.monitoring.WasupHierarchy;
 import com.telefonica.claudia.slm.naming.FQN;
 import com.telefonica.claudia.slm.naming.ReservoirDirectory;
 import com.telefonica.claudia.slm.rulesEngine.RulesEngine;
 import com.telefonica.claudia.slm.serviceconfiganalyzer.ServiceConfigurationAnalyzer;
 import com.telefonica.claudia.slm.vmiHandler.TCloudClient;
 import com.telefonica.claudia.slm.vmiHandler.VEEReplicaAllocationResult;
 import com.telefonica.claudia.slm.vmiHandler.VEEReplicaUpdateResult;
 import com.telefonica.claudia.slm.vmiHandler.VMIHandler;
 import com.telefonica.claudia.slm.vmiHandler.VMIHandler.SubmissionModelType;
 import com.telefonica.claudia.slm.vmiHandler.exceptions.AccessDeniedException;
 import com.telefonica.claudia.slm.vmiHandler.exceptions.CommunicationErrorException;
 import com.telefonica.claudia.slm.vmiHandler.exceptions.NotEnoughResourcesException;
 import com.telefonica.claudia.slm.vmiHandler.exceptions.VEEReplicaDescriptionMalformedException;
 
 /**
  * The FSM (Finite State Machine) manages the lifecycle of a single service. FSM
  * is a runnable class, that is started by the LCC.
  *
  * The service lifecycle, represented by the run() method, consists of an
  * event-consuming loop. Each service has a matching event queue in the LCC,
  * where all the control events are enqueued. In each step of the lifecycle, a
  * new event is pulled out of the queue and treated in the getNextState()
  * method. This method will transition the FSM to the appropiate state after any
  * completed action.
  *
  * When a service is created, first of all the init() method is called (INIT
  * state). In this method, all the needed clients are created and the OVF
  * descriptor is parsed so all the information about the service is translated
  * into a class hierarchy of classes in the deployment package. Once it is
  * parsed, the Service transitions to the SPECIFIED state where the service will
  * be really deployed.
  */
 public class FSM extends Thread implements Serializable {
 
     private static final long serialVersionUID = -8902978852760714189L;
 
     private static Logger logger = Logger.getLogger("Service");
 
     // Internal state information
     // --------------------------------------------------------------------------------------------
     @SuppressWarnings("unused")
     private long monitFreq = 1000;
 
     /**
      * This flag should be set for this situations that causa a non-recoverable
      * error. It leads the machine automatically to the ERROR state.
      */
     private boolean abort = false;
 
     /**
      * This message will be printed in the abort message of the FSM to be able
      * to recognize the problem that lead the FSM to abort its execution.
      */
     private String abortMessage = null;
 
     /**
      * Determines whether the Finite state machine should continue its execution
      * or not.
      */
     private boolean finishExecution = false;
 
     /**
      * Whenever an elasticity rule is fired (and so a creation or removement of
      * a replica), the timestamps of the beginning and ending of this action are
      * stored in this interval (the beginning in the first member of the array,
      * the ending in the last one) for each vee. If an event is recieved to
      * create or deploy a replica, the timestamp of the event is checked against
      * the interval for this veee, so to avoid creating more than one replica
      * due to the same load increment (this situation will happen whenever the
      * monitoring period is greater than the time needed to make one of those
      * actions).
      */
     private Map<VEE, long[]> lastElasticityInterval = new HashMap<VEE, long[]>();
 
     /**
      * Margin under and over the elasticity interval, to prevent events out of
      * order to fire actions supposed to be prevented by the elasticity interval
      * (the interval is calculated with the timestamp of the event firing the
      * action), and to let the deployed or removed replica affect the real load
      * of the service before reevaluating the need of doing some action.
      */
     private final static long ELASTICITY_MARGIN = 30 * 1000;
 
     // State-related information
     // --------------------------------------------------------------------------------------------
     private int currentState, nextState;;
     public static final int numStates = 6;
 
     public static final int NONE = 0;
     public static final int INIT = 1;
     public static final int RUNNING = 2;
     public static final int ERROR = 3;
     public static final int RECONFIGURED = 4;
     public static final int FINISHED = 5;
 
     /**
      * Determines the polling frequency the FSM will use to check the event
      * buffer.
      */
     private static final long POLLING_PERIOD = 1000;
 
     // Service related information
     // --------------------------------------------------------------------------------------------
     /**
      * Service application the FSM manages.
      */
     private ServiceApplication sap;
 
     /**
      * URL of the OVF Descriptor used to deploy the service.
      */
     private String xmlFile = "";
 
     // VEE Related information
     // -------------------------------------------------------------------------------------------
     /**
      * Array containing the vees. Is exactly the same information we have in
      * vees.
      *
      * TODO: Is this synchronized with the hashset?
      */
     private VEE[] veeArray;
 
     /**
      * Set with the currently deployed replicas.
      */
     private Set<VEEReplica> veesRollBack = new HashSet<VEEReplica>();
 
     /**
      * Set of nics used to deploy the service. It will be used to properly
      * undeploy them once the service has finished.
      */
     Set<Network> nicsToDeploy;
 
     // Internal references to other blocks of the SM
     // --------------------------------------------------------------------------------------------
     /**
      * Reference to the Lifecycle Controller that created the FSM (also the one
      * who have the evente queue).
      */
     private LifecycleController lcc;
 
     private RulesEngine rle;
 
     /**
      * OVF Parser. The parser is used for two kinds of actions: it is used first
      * time the service is deployed to create the class hierarchy of the service
      * components; and its also used to generate the OVF environment for each
      * replica once it is deployed (whether it is the first deployement or a
      * elasticity action).
      */
     private Parser parser;
 
     /**
      * VMI client used to comunicate with the underlying layer in order to
      * deploy replicas and other resources.
      */
     private VMIHandler vmi = null;
 
     // Deployment information
     // --------------------------------------------------------------------------------------------
     private static final String repositoryDir = "./repository";
     private static final String customImagesDir = repositoryDir
     + SMConfiguration.getInstance().getImagesServerPath();
     private static final String protocol = "http://";
 
     /**
      * Monitoring client for the service. It holds the hierarchy of measurement
      * types for this service and also acts as the client for client
      * interactions with Wasup (whenever a measurement reaches de SM it is
      * redirected to Wasup for data mining).
      */
     private WasupHierarchy wasupClient;
 
     private boolean setSwap = false;
 
     /**
      * Property map defining the customization info for the vees.
      *
      * TODO: it seems strange to have the same customization information for all
      * the types of vees. TODO: this seems more appropiate as a local attribute
      * in the getNextState method.
      */
     private HashMap<String, String> replicaCustomInfo;
 
     private LoadBalancerConfigurator lbConfigurator;
 
     /**
      * Determines the kind of events permitted in each of the different FSM
      * states. The key of the map represents the state and the value is a list
      * of the accepted events.
      */
     private static Map<Integer, List<Integer>> stateEventMatrix = new HashMap<Integer, List<Integer>>();
 
     static {
         stateEventMatrix.put(NONE, new ArrayList<Integer>());
         stateEventMatrix.put(INIT, new ArrayList<Integer>());
         stateEventMatrix.get(INIT).add(SMControlEvent.START_SERVICE);
 
         stateEventMatrix.put(RUNNING, new ArrayList<Integer>());
         stateEventMatrix.get(RUNNING).add(
                 SMControlEvent.ELASTICITY_RULE_VIOLATION);
         stateEventMatrix.get(RUNNING).add(SMControlEvent.NEW_RULE_ADDED);
         stateEventMatrix.get(RUNNING).add(SMControlEvent.RULE_CHANGED);
         stateEventMatrix.get(RUNNING).add(SMControlEvent.UNDEPLOY_SERVICE);
         stateEventMatrix.get(RUNNING).add(SMControlEvent.GET_MONITORING_DATA);
 
         stateEventMatrix.put(ERROR, new ArrayList<Integer>());
         stateEventMatrix.put(RECONFIGURED, new ArrayList<Integer>());
         stateEventMatrix.put(FINISHED, new ArrayList<Integer>());
     }
 
     public FSM(LifecycleController lcc, String endPoint) {
         this.lcc = lcc;
         currentState = NONE;
         nextState = currentState;
     }
 
     public int getCurrentState() {
         return this.currentState;
     }
 
     public String getStateText() {
         switch (currentState) {
         case NONE:
             return "NONE";
         case INIT:
             return "INIT";
         case RUNNING:
             return "RUNNING";
         case ERROR:
             return "ERROR";
         case RECONFIGURED:
             return "RECONFIGURED";
         case FINISHED:
             return "FINISHED";
         default:
             return "UNKNOWN";
         }
     }
 
     public void loadManifest(String mani) {
         this.xmlFile = mani;
     }
 
     public void loadRuleEngine(RulesEngine rulesEngine) {
         rle = rulesEngine;
     }
 
     /**
      * Method for the LCC to set the monitoring frequency upon SP request.
      */
     protected void setMonFreq(long freq) {
         monitFreq = freq;
     }
 
     @SuppressWarnings("unchecked")
     private void registerInDirectory() {
 
         VEE vee;
         Rule rule;
         ServiceKPI kpi;
 
         logger.info("Service Application: " + sap.getSerAppName());
 
         Set<VEE> veeVector = sap.getVEEs();
         Set<Rule> ruleVector = sap.getServiceRules();
         Set<ServiceKPI> kpiVector = sap.getServiceKPIs();
 
         // Register ServiceApp
         ReservoirDirectory.getInstance().registerObject(sap.getFQN(), sap);
 
         // add VEEs to Service
         Iterator veeIterator = veeVector.iterator();
         if (!veeIterator.hasNext())
             logger.info("No VEEs defined");
         while (veeIterator.hasNext()) {
             vee = (VEE) veeIterator.next();
             logger.info("VEE " + vee.getVEEName());
             ReservoirDirectory.getInstance().registerObject(vee.getFQN(), vee);
         }
 
         // add KPIs
         Iterator kpiIterator = kpiVector.iterator();
         if (!kpiIterator.hasNext())
             logger.info("No KPIs defined");
         while (kpiIterator.hasNext()) {
             kpi = (ServiceKPI) kpiIterator.next();
             logger.info("KPI " + kpi.getKPIName());
             ReservoirDirectory.getInstance().registerObject(kpi.getFQN(), kpi);
         }
 
         // add Rules
         Iterator ruleIterator = ruleVector.iterator();
         if (!ruleIterator.hasNext())
             logger.info("No Rules defined");
         while (ruleIterator.hasNext()) {
             rule = (Rule) ruleIterator.next();
             logger.info("Rule " + rule.getName());
             rule.configure((ServiceKPI) ReservoirDirectory.getInstance()
                     .getObject(new FQN(rule.getKPIName())));
             ReservoirDirectory.getInstance()
             .registerObject(rule.getFQN(), rule);
         }
     }
 
     /**
      * Initializes all the structures needed by the FSM to begin the service
      * lifecycle.
      *
      * @param serviceName
      *            Name of the service to be created.
      */
     @SuppressWarnings("unchecked")
     public void init(String serviceName, String customerName) {
 
         logger.info("Initiating FSM for service defined in file " + xmlFile
                 + " , customer " + customerName);
         Customer client;
 
         // Check if customer already present in directory.
         FQN customerFQN = new Customer(customerName).getFQN();
         if (ReservoirDirectory.getInstance().isNameRegistered(customerFQN)) {
             logger.info("Customer already present: " + customerFQN.toString());
             client = (Customer) ReservoirDirectory.getInstance().getObject(
                     customerFQN);
         } else {
             logger.info("New customer: " + customerFQN.toString()
                     + ", registering in directory");
             client = new Customer(customerName);
             ReservoirDirectory.getInstance().registerObject(client.getFQN(),
                     client);
             DbManager.getDbManager().save(client);
         }
 
         try {
             parser = new Parser(xmlFile, client, serviceName);
             parser.parse();
         } catch (ManiParserException ex) {
             logger.error(
                     "ManiParserException when trying to parse manifest file "
                     + xmlFile, ex);
             throw new Error(
                     "ManiParserException when trying to parse manifest file "
                     + xmlFile, ex);
         }
         logger.info("Manifest file successfully parsed");
 
         sap = parser.getServiceApplication();
         client.registerService(sap);
 
         registerInDirectory();
 
         // inform its lcc that the service FSM has been created
         logger
         .info("Reporting to Lifecycle Controler the FSM has been created for Service Application");
         lcc.registerFSM(sap.getFQN(), this);
 
         this.lcc.addCustomer(client);
 
         rle.configure(this);
         rle.claudiaRules2Drools();
 
         boolean callSCA = false;
         if (callSCA) {
             try {
                 ServiceConfigurationAnalyzer sca = new ServiceConfigurationAnalyzer();
                 logger.info("Calling Service Config Analyzer, client "
                         + client.getCustomerName());
                 sap = sca.refineService(sap);
             } catch (Exception ex) {
                 logger.error(
                         "Exception caught when calling to Service Config Analyzer, message: "
                         + ex.getMessage(), ex);
             }
 
         } else
             logger.info("Skipping SCA call...");
 
         URL oneURL = null;
         try {
             oneURL = new URL("http", SMConfiguration.getInstance()
                     .getVEEMHost(),
                     SMConfiguration.getInstance().getVEEMPort(), "/");
             logger.info("VEEM listening on URL " + oneURL);
         } catch (MalformedURLException ex) {
 
         }
 
         vmi = new TCloudClient(oneURL.toString());
 
         // Initialize the WASUP client.
         if (SMConfiguration.getInstance().isWasupActive()) {
             try {
                 wasupClient = WasupHierarchy.getWasupHierarchy();
             } catch (Exception e) {
                 logger.error("Error login to the WASUP: " + e.getMessage());
             } catch (Throwable e) {
                 logger.error("Unknow error connecting to WASUP: "
                         + e.getMessage(), e);
             }
         }
 
         // Initialize the lastElasticityInterval Map
         for (VEE veeItr : sap.getVEEs())
             lastElasticityInterval.put(veeItr, new long[2]);
 
         if (SMConfiguration.getInstance().isACDActive()) {
             try {
                 vmi.sendACD(sap);
             } catch (Throwable t) {
                 logger
                 .error("The ACD could not be sent due to the following exception: "
                         + t.getMessage());
             }
         }
 
         DbManager.getDbManager().save(sap);
 
         currentState = INIT;
         nextState = currentState;
     }
 
     public void loadService(ServiceApplication sap) {
 
         logger.info("Loading service into the FSM: " + sap.getSerAppName());
 
         this.sap = sap;
 
         try {
             parser = new Parser(sap.getXmlFile(), sap.getCustomer(), sap
                     .getSerAppName());
         } catch (ManiParserException ex) {
             logger.error(
                     "ManiParserException when trying to parse manifest file "
                     + xmlFile, ex);
             throw new Error(
                     "ManiParserException when trying to parse manifest file "
                     + xmlFile, ex);
         }
 
         parser.setServiceApplication(sap);
 
         registerInDirectory();
 
         logger
         .info("Reporting to Lifecycle Controler the FSM has been created for Service Application");
         lcc.registerFSM(sap.getFQN(), this);
 
         rle.configure(this);
         rle.claudiaRules2Drools();
 
         URL oneURL = null;
         try {
             oneURL = new URL("http", SMConfiguration.getInstance()
                     .getVEEMHost(),
                     SMConfiguration.getInstance().getVEEMPort(),
                     SMConfiguration.getInstance().getVEEMPath());
             logger.info("VEEM listening on URL " + oneURL);
         } catch (MalformedURLException ex) {
 
         }
 
         vmi = new TCloudClient(oneURL.toString());
 
         // Initialize the WASUP client.
         if (SMConfiguration.getInstance().isWasupActive()) {
             try {
                 wasupClient = WasupHierarchy.getWasupHierarchy();
             } catch (Exception e) {
                 logger.error("Error login to the WASUP: " + e.getMessage());
             } catch (Throwable e) {
                 logger.error("Unknow error connecting to WASUP: "
                         + e.getMessage(), e);
             }
         }
 
         // Initialize the lastElasticityInterval Map
         for (VEE veeItr : sap.getVEEs())
             lastElasticityInterval.put(veeItr, new long[2]);
 
         // Recreate the array with the NICS to be undeployed
         nicsToDeploy = sap.getNetworks();
 
         veesRollBack.addAll(DbManager.getDbManager().getList(VEEReplica.class));
 
         rle.run();
         currentState = RUNNING;
         nextState = currentState;
     }
 
     /**
      * Main process of the FSM. The main loop consists of two actions: consuming
      * an event from the Lifecycle, and transitioning to the next state, using
      * the information on that event.
      *
      * Two conditions finish the execution of the FSM: the abort flag, that is
      * set if the abort() method is called during the SPECIFIED state, makes the
      * service finish with an error log; the finishExecution is set when the
      * Service is done (it has received the undeploy message).
      *
      */
     public void run() {
         while (!finishExecution) {
             currentState = nextState;
             try {
                 // wait for the arrival of the order to deploy the service
                 Thread.sleep(POLLING_PERIOD);
                 this.interrupt();
 
                 if (abort) {
                     logger
                     .error("Service lifecycle execution aborted for service ["
                             + sap.getFQN()
                             + "], due to the following reason: "
                             + abortMessage);
                     break;
                 }
             } catch (InterruptedException e) {
                 try {
                     // ask whether is there any message
                     nextState = getNextState(lcc.deliver(sap.getFQN()),
                             currentState);
                 } catch (InterruptedException ex) {
                     logger.error("InterruptedException caught ", ex);
                 } catch (MalformedURLException ex) {
                     logger.error("MalformedURLException caught", ex);
                 }
             }
         }
 
         // Remove the FSM from the Lifecycle controller
         lcc.deregisterFSM(this.getServiceFQN());
 
         if (finishExecution)
             logger.info("Service lifecycle finished cleanly");
     }
 
     public FQN getServiceFQN() {
         return (sap.getFQN());
     }
 
     public ServiceApplication getServiceApplication() {
         return this.sap;
     }
 
     /**
      * Used to check whether an action should be discarded due to the elasticity
      * interval. Used to prevent the FSM to execute out of date actions.
      *
      * @param vee
      *            VEE that is being the target of the action. There is a
      *            different elasticity interval for each vee.
      *
      * @param time
      *            Time when the event was generated.
      *
      * @return <i>true</i> if the action can proceed. <i>false</i> if the action
      *         should be discarded.
      *
      */
     private boolean checkElasticityInterval(VEE vee, long time) {
 
         long[] interval = lastElasticityInterval.get(vee);
 
         if (interval[0] == 0 || interval[1] == 0)
             return true;
 
         if (time < interval[0] || time > interval[1])
             return true;
 
         return false;
     }
 
     private boolean startService() {
 
         // in this case we use an Array so as to sort it properly
         int g = 0;
         veeArray = new VEE[sap.getVEEs().size()];
         for (Iterator<VEE> veeIt = sap.getVEEs().iterator(); veeIt.hasNext();) {
             veeArray[g] = veeIt.next();
             g++;
         }
 
         // rearrange the array according to the order field in the VEE
         // object
         // VEE implements Comparable
         Arrays.sort(veeArray);
 
         // First of all, check the needed networks have been created.
         if (!checkAndDeployNetworks()) {
             abort("Network creation error");
             return false;
         }
 
         // for each VEE, get the required replicas
         for (int j = 0; j < veeArray.length; j++) {
 
             logger.info("Deployment step " + (j + 1) + ", VEE: "
                     + veeArray[j].getVEEName() + " with "
                     + veeArray[j].getInitReplicas() + " replicas");
 
             // get the required number of replicas.
             if (createReplica(veeArray[j], veeArray[j].getInitReplicas()) == null)
                 abort("Replicas could not be created for vee ["
                         + veeArray[j].getFQN() + "]");
         }
 
         rle.run();
         logger.info("DONE with ALL replicas and ALL VEEs ");
 
         // Once all the VEES are working, it's time to replicate its structure
         // in
         // WASUP
         if (SMConfiguration.getInstance().isWasupActive()) {
             try {
                 wasupClient.createWasupHierarchy(getServiceApplication());
             } catch (IOException e) {
                 logger
                 .error("Error creating the WASUP monitorization hierarchy: "
                         + e.getMessage());
             } catch (JSONException e) {
                 logger.error("Error parsing the response of a WASUP request: "
                         + e.getMessage());
             } catch (Throwable e) {
                 logger.error("Unknow error connecting to WASUP: "
                         + e.getMessage(), e);
             }
         }
 
         return true;
     }
 
     public boolean elasticityCreateReplica(String veeType, int replicaNumber,
             long initialTime) {
 
         VEE vee2Replicate = (VEE) ReservoirDirectory.getInstance().getObject(
                 new FQN(veeType));
 
         if (!checkElasticityInterval(vee2Replicate, initialTime)) {
             logger.warn("Action discarded due to the Elasticity Interval.");
             return false;
         }
 
         // Save the beginning of the interval
         lastElasticityInterval.get(vee2Replicate)[0] = initialTime
         - ELASTICITY_MARGIN;
 
         // check that the current number of replicas does not
         // exceed the maximum
         int currentReplicas = vee2Replicate.getCurrentReplicas();
         if (currentReplicas < vee2Replicate.getMaxReplicas()) {
             if (createReplica(vee2Replicate, replicaNumber) == null)
                 logger.error("Replica could not be created.");
             else
                 logger.info("Replica created successfully");
 
         } else {
             logger
             .warn("Replica was not created, for the VEE already have the maximum number of replicas");
         }
 
         // Save the ending part of the interval
         lastElasticityInterval.get(vee2Replicate)[1] = System
         .currentTimeMillis()
         + ELASTICITY_MARGIN;
 
         return true;
     }
 
     private boolean elasticityRemoveReplica(String veeType, long initialTime) {
 
         VEE vee2Plicate = (VEE) ReservoirDirectory.getInstance().getObject(
                 new FQN(veeType));
 
         if (!checkElasticityInterval(vee2Plicate, initialTime)) {
             logger.warn("Action discarded due to the Elasticity Interval.");
             return false;
         }
 
         // Save the beginning of the interval
         lastElasticityInterval.get(vee2Plicate)[0] = initialTime
         - ELASTICITY_MARGIN;
 
         // check that the current number of replicas is not going to be
         // under the minimum
         if (vee2Plicate.getCurrentReplicas() > vee2Plicate.getMinReplicas()) {
             Set<VEEReplica> replicas = vee2Plicate.getVEEReplicas();
             // assume all the replicas can equally be removed
 
             int maxreplica = 0;
             Set<VEEReplica> plicateSet = new HashSet<VEEReplica>();
             VEEReplica removeCandidate = null;
             for (Iterator<VEEReplica> removeIt = replicas.iterator(); removeIt
             .hasNext();) {
                 VEEReplica plicate = (VEEReplica) removeIt.next();
                 if ((plicate.getId()) > maxreplica) {
                     maxreplica = plicate.getId();
                     removeCandidate = plicate;
                 }
 
             }
             plicateSet.add(removeCandidate);
 
             // invoke the appropriate VMI methods
             if (shutdown(plicateSet)) {
 
                 // Remove it from wasup
                 if (SMConfiguration.getInstance().isWasupActive()) {
                     try {
                         wasupClient.removeNode(removeCandidate);
                     } catch (IOException e) {
                         logger
                         .error("The replica could not be removed from WASUP: "
                                 + e.getMessage());
                     } catch (Throwable e) {
                         logger.error(
                                 "Unknow error connecting to WASUP: "
                                 + e.getMessage(), e);
                     }
                 }
 
                 // Parche para borrar la réplica del reservoir directory
 
                 FQN fqn = removeCandidate.getFQN();
                 ReservoirDirectory.getInstance().removeMatchingNames(fqn);
 
                 // update the data model by removing a new
                 // replica and increasing the number of
                 // current replicas
                 //vee2Plicate.getVEEReplicas().remove(plicate);
                 vee2Plicate.unregisterVEEReplica(removeCandidate);
                 vee2Plicate.removeCurrentReplicas();
                 veesRollBack.remove(removeCandidate);
 
                 if (vee2Plicate.isVEEReplicaRegistered(removeCandidate)){
                     logger.error("The replIca HAS NOT BEEN REMOVED");
                 }
                 logger.info("" +"AFTER SCALING DOWN THERE ARE " + vee2Plicate.getCurrentReplicas());
 
 
                 return true;
             } else {
                 logger.error("FAILURE WHILE REMOVING THE REPLICA!!!!!!");
 
 
             }
         }
 
         // Save the ending part of the interval
         lastElasticityInterval.get(vee2Plicate)[1] = System.currentTimeMillis()
         + ELASTICITY_MARGIN;
 
         return false;
     }
 
     private int dispatchEvent(SMControlEvent cntrlEvent)
     throws MalformedURLException {
 
         int retorno;
 
         // Check there is really an event.
         if (cntrlEvent == null)
             return currentState;
 
         // Check the event is acceptable in the actual state
         boolean accepted = false;
 
         for (Integer eventId : stateEventMatrix.get(currentState))
             if (cntrlEvent.getControlEventType() == eventId)
                 accepted = true;
 
         if (!accepted) {
             logger.error("Event discarded: not acceptable in this state.");
             return currentState;
         }
 
         if (!cntrlEvent.getServiceFQN().equals(sap.getFQN().toString())) {
             logger
             .error("¡The FQNs of service and event doesn't match.\nService FQN: "
                     + sap.getFQN().toString()
                     + "\nEvent FQN: "
                     + cntrlEvent.getServiceFQN());
         }
 
         logger.info("\n\n* Dispatching event of type: "
                 + cntrlEvent.getControlEventType());
 
         switch (cntrlEvent.getControlEventType()) {
         case SMControlEvent.ELASTICITY_RULE_VIOLATION:
 
             logger
             .info("\n* FSM Processing Control Event: ELASTICITY_RULE_VIOLATION\n");
 
             // this method is triggered by the RuleEngine on the
             // LifecycleController. Get rule action and execute it
             String action = cntrlEvent.getSuggestedAction();
             logger.info(SMControlEvent.ELASTICITY_RULE_VIOLATION + ", action: "
                     + action);
 
             // check the action grammar and perform the needed VMI
             // actions
             if (action.contains("createReplica")) {
 
                 // get the action parameters
                 int initIndex = action.indexOf("(");
                 int endIndex = action.lastIndexOf(")");
                 String veeType = action.substring(initIndex + 1, endIndex);
 
                 String[] parameters = veeType.split(",");
 
                 if (parameters.length < 2) {
                     elasticityCreateReplica(parameters[0], 1, cntrlEvent
                             .getInitialTime());
                 } else {
                     elasticityCreateReplica(parameters[0], Integer
                             .parseInt(parameters[1]), cntrlEvent
                             .getInitialTime());
                 }
 
                 retorno = FSM.RUNNING;
 
             } else if (action.contains("removeReplica")) {
                 // get the action parameters
                 int initIndex = action.indexOf("(");
                 int endIndex = action.lastIndexOf(")");
                 String veeType = action.substring(initIndex + 1, endIndex);
 
                 elasticityRemoveReplica(veeType, cntrlEvent.getInitialTime());
 
                 retorno = FSM.RUNNING;
             } else {
                 // or return an error if they weren't
                 retorno = FSM.ERROR;
             }
 
             break;
 
         case SMControlEvent.NEW_RULE_ADDED:
             logger.info("\n* FSM Processing Control Event: NEW_RULE_ADDED\n");
             rle.addRule(cntrlEvent.getSuggestedAction());
             retorno = FSM.RECONFIGURED;
             break;
 
         case SMControlEvent.RULE_CHANGED:
             logger.info("\n* FSM Processing Control Event: RULE_CHANGED\n");
             retorno = FSM.RECONFIGURED;
             break;
 
         case SMControlEvent.UNDEPLOY_SERVICE:
             logger.info("\n* FSM Processing Control Event: UNDEPLOY_SERVICE\n");
             // issue the appropriate VMI Events to undeploy the service,
             shutdown(veesRollBack);
             removeNetworks();
             sap.getCustomer().unregisterService(sap);
             DbManager.getDbManager().save(sap.getCustomer());
 
             FQN fqnService = sap.getFQN();
             DbManager.getDbManager().remove(sap);
             DbManager.getDbManager().remove(fqnService);
             retorno = FSM.FINISHED;
             break;
 
         case SMControlEvent.START_SERVICE:
             if (startService())
                 retorno = FSM.RUNNING;
             else
                 retorno = FSM.ERROR;
             break;
 
         default:
             retorno = FSM.ERROR;
             break;
         }
 
         return retorno;
     }
 
     private void removeNetworks() {
 
         try {
             vmi.deleteNetwork(nicsToDeploy);
         } catch (AccessDeniedException e) {
             logger.error("Access denied accessing the VMI: " + e.getMessage());
         } catch (CommunicationErrorException e) {
             logger
             .error("Couldn't connect to the VMI to undeploy the networks: "
                     + e.getMessage());
         }
     }
 
     /**
      * Analyzes the event in the queue to determine the next state in the FSM
      *
      * @param cntrlEvent
      * @param currentState
      */
     private int getNextState(SMControlEvent cntrlEvent, int currentState)
     throws InterruptedException, MalformedURLException {
 
         int retorno = currentState;
 
         switch (currentState) {
         case INIT:
             logger.info("\n\n\n\n* Processing State: INIT");
             retorno = dispatchEvent(cntrlEvent);
             break;
 
         case RUNNING:
             if (!(cntrlEvent == null)) {
                 retorno = dispatchEvent(cntrlEvent);
             }
 
             break;
 
         case RECONFIGURED:
             logger.info("\n\n\n\n* Processing State: RECONFIGURED");
             retorno = FSM.RUNNING;
             break;
 
         case FINISHED:
             try {
                 finishExecution = true;
             } catch (Throwable ex) {
 
             }
             break;
 
         case ERROR:
             logger.info("\n\n\n\n* Processing State: ERROR");
             shutdown(veesRollBack);
             retorno = FSM.FINISHED;
             break;
         }
 
         if (abort)
             retorno = FSM.ERROR;
 
         return (retorno);
     }
 
     /**
      * Read the SA configuration to find out the FQN of the required networks.
      * Calculate its required size, deploy them and save a reference in the FSM
      * network repository.
      *
      * The size is calculated after the sum of the maximum number of replicas
      * for each VEE using the network.
      *
      * @throws CommunicationErrorException
      * @throws AccessDeniedException
      *
      */
     private boolean checkAndDeployNetworks() {
 
         logger.info("Checking and deploying networks");
 
         nicsToDeploy = new HashSet<Network>();
 
         // Calculate network sizes
         Map<Network, Integer> networkMaximumSizes = new HashMap<Network, Integer>();
 
         for (VEE vee : sap.getVEEs()) {
 
             HashMap<String, Integer> ipsNeeded = parser
             .getNumberOfIpsPerNetwork(vee.getFQN().contexts()[vee
                                                               .getFQN().contexts().length - 1]);
 
             for (NICConf nic : vee.getNICsConf()) {
 
                 Integer numberOfIps = ipsNeeded.get(nic.getNetwork().getName());
 
                 if (numberOfIps == null || numberOfIps == 0)
                     numberOfIps = 1;
 
                 logger.info(numberOfIps + "(" + vee.getMaxReplicas()
                         * numberOfIps + ")" + " IPs required for vee "
                         + vee.getVEEName() + " on network "
                         + nic.getNetwork().getName());
 
                 if (networkMaximumSizes.containsKey(nic.getNetwork())) {
                     networkMaximumSizes.put(nic.getNetwork(),
                             networkMaximumSizes.get(nic.getNetwork())
                             + vee.getMaxReplicas() * numberOfIps);
                 } else {
                     networkMaximumSizes.put(nic.getNetwork(), vee
                             .getMaxReplicas());
                 }
             }
         }
 
         for (Network net : networkMaximumSizes.keySet()) {
 
             // Update the network size, based on the minumum number of bits
             // needed to represent the size
             net.setSize(networkMaximumSizes.get(net));
 
             if (net.getSize() > 1)
                 net.setSize(net.getSize() + 2);
 
             logger.info("Asking for a "
                     + ((net.getPrivateNet()) ? "private" : "public")
                     + " network with " + net.getSize() + " IPs");
 
             String[] address = lcc.getNetworkAddress(net.getSize(), !net
                     .getPrivateNet());
             if (address == null) {
                 logger
                 .error("The lifecycle manager was unable to give enough IPs. It's impossible to deploy all the needed networks. Aborting.");
                 return false;
             }
 
             net.setNetworkAddresses(address);
             nicsToDeploy.add(net);
         }
 
         try {
             vmi.allocateNetwork(nicsToDeploy);
             return true;
 
         } catch (AccessDeniedException e) {
             logger.error("Access denied when allocating a new network: "
                     + e.getMessage() + ". Aborting.");
         } catch (CommunicationErrorException e) {
             logger.error("Communication error when allocating a new network: "
                     + e.getMessage() + ". Aborting.");
         } catch (NotEnoughResourcesException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         logger.info("Networks for Service Application "
                 + sap.getFQN().toString() + " checked and deployed");
 
         return false;
     }
 
     /**
      * Method to shutdown a set of running replicas
      *
      * @param rollBack
      *
      * @return
      */
     private boolean shutdown(Set<VEEReplica> rollBack) {
 
         Map<VEEReplica, VEEReplicaUpdateResult> updateResults = null;
 
         try {
             updateResults = vmi.shutdownReplica(rollBack);
 
         } catch (CommunicationErrorException ex) {
             logger.error("Comunication error waiting for Replica shutdown: "
                     + ex.getMessage());
             return false;
         } catch (AccessDeniedException e) {
             logger.error("Access error waiting for Replica shutdown: "
                     + e.getMessage());
             return false;
         }
 
         Iterator<VEEReplica> rollIt = rollBack.iterator();
         while (rollIt.hasNext()) {
             VEEReplica veeReplica = rollIt.next();
 
             logger.info("Shutting down replic: " + veeReplica.getFQN());
 
             // Shutdown the replica
             VEEReplicaUpdateResult updateResult = updateResults.get(veeReplica);
 
             if (!updateResult.isSuccess()) {
                 logger.error("Update failed: " + updateResult.getMessage());
                 return false;
             }
 
             // Release the ip addres leased to the replica.
             for (NIC nic : veeReplica.getNICs())
                 for (String ipAddress : nic.getIPAddresses())
                     lcc.releaseHostAddress(ipAddress, nic.getNICConf()
                             .getNetwork().getNetworkAddresses()[0]);
 
             // Once the replica is finished, it should be removed from the
             // storage system.
             // But first, it has to be unlinked from the VEE
             veeReplica.getVEE().unregisterVEEReplica(veeReplica);
             DbManager.getDbManager().save(veeReplica.getVEE());
 
             FQN fqnReplica = veeReplica.getFQN();
             DbManager.getDbManager().remove(veeReplica);
             DbManager.getDbManager().remove(fqnReplica);
         }
         return true;
     }
 
     /**
      * Method to deploy a replica into the underlying infrastructure
      *
      * @param veeReplica
      * @return
      */
     private boolean deployReplica(Set<VEEReplica> veeReplicasConfs) {
 
         Map<VEEReplica, VEEReplicaAllocationResult> allocResults = null;
         try {
             allocResults = vmi.allocateVEEReplicas(veeReplicasConfs,
                     SubmissionModelType.BESTEFFORT);
         } catch (NotEnoughResourcesException ex) {
             abort("NotEnoughResourcesException exception caught "
                     + ex.getMessage());
             return false;
         } catch (AccessDeniedException ex) {
             abort("AccessDeniedException exception caught " + ex.getMessage());
             return false;
         } catch (VEEReplicaDescriptionMalformedException ex) {
             abort("VEEReplicaDescriptionMalformedException exception caught "
                     + ex.getMessage());
             return false;
         } catch (CommunicationErrorException ex) {
             abort("CommunicationErrorException exception caught "
                     + ex.getMessage());
             return false;
         }
 
         for (VEEReplica veeReplica: veeReplicasConfs) {
 			logger.info("**** Reviewing replica " + veeReplica.getFQN());
 			VEEReplicaAllocationResult allocResult = allocResults.get(veeReplica);
 			if (!allocResult.isSuccess()) {
 				abort("VIM reports error: "+ allocResult.getMessage());
 				return false;
 			}
 
 			// If replica is loadbalanced, balancer should be notified
 
 			VEE balancerVEE = veeReplica.getVEE().getBalancedBy();
 
 			if (balancerVEE!=null){
 				logger.info("Adding replica to LB");
 				addReplicaToLoadBalancer(veeReplica, balancerVEE);
 			}
  else {
 				logger.info(veeReplica.getFQN() + " is not balanced");
 			}
 
 		}
 
 		// The allocation was successful and the replica is UP and Running
 		// update the rollback
 		veesRollBack.addAll(veeReplicasConfs);
 		return true;
 	
     }
 
 
     /**
      * @param veeReplica
      * @param balancerVEE
      */
    private void addReplicaToLoadBalancer(VEEReplica veeReplica, VEE balancerVEE) {
             
                     addRegularReplicaToBalancer(veeReplica, balancerVEE);
             
     }
 
 
     /**
      * Creates a customization image for the VEEReplica passed as a parameter,
      * containing only one file, called ovf-env.xml, with all the customization
      * data needed to initialize the Replica.
      *
      * Returns an URL to access the replica.
      *
      * @param veeReplica
      * @param createISOImage
      * @return
      */
     private String createCustomizationFile(VEEReplica veeReplica) {
 
         String customizationDirName = customImagesDir + "/"
         + veeReplica.getFQN().toString();
         File customizationDir = new File(customizationDirName);
         customizationDir.mkdirs();
 
         String customizationFileName = customizationDirName + "/ovf-env.xml";
         File customizationFile = new File(customizationFileName);
         logger.info("Creating customization file in "
                 + customizationFile.getPath());
 
         String customizationDirURLPath = SMConfiguration.getInstance()
         .getImagesServerPath()
         + "/" + veeReplica.getFQN().toString();
 
         PrintWriter out = null;
         try {
             out = new PrintWriter(new FileWriter(customizationFile));
         } catch (IOException ex) {
             java.util.logging.Logger.getLogger(FSM.class.getName()).log(
                     java.util.logging.Level.SEVERE, null, ex);
             return ex.getMessage();
         }
         out.write(veeReplica.getOVFEnvironment() + "\n");
         out.close();
 
         String urlToCustomFile = protocol
         + SMConfiguration.getInstance().getImagesServerHost() + ":"
         + SMConfiguration.getInstance().getImagesServerPort()
         + customizationDirURLPath + "/ovf-env.xml";
 
         logger.info("URL to customization file: " + urlToCustomFile);
 
         return urlToCustomFile;
     }
 
     /**
      * Returns the number of replicas for the indicated VEE FQN, if the FQN
      * belongs to this service.
      *
      * @param veeFqn
      *            FQN of the VEE whose replica number want to be retrieved.
      *
      * @return The actual number of replicas if the VEE is part of the current
      *         service or 0 otherwise.
      */
     public int getAmount(String veeFqn) {
         VEE vee = (VEE) ReservoirDirectory.getInstance().getObject(
                 new FQN(veeFqn));
         if (vee != null) {
             logger.info("VEE " + vee + " current replicas: "
                     + vee.getCurrentReplicas());
             return vee.getCurrentReplicas();
         }
         logger.error("Not VEE registered with FQN " + veeFqn);
         return 0;
     }
 
     /**
      * Abort the current action if the service was deployed successfully or
      * abort the whole execution of the service if the abort signal is generated
      * during the SPECIFIED state.
      *
      * @param reason
      *            Textual reason to send the abort signal. It will end up in the
      *            logs.
      */
     private void abort(String reason) {
         if (currentState == INIT) {
             logger.error("Aborting the FSM due to the following reason: "
                     + reason);
             abort = true;
             this.abortMessage = reason;
         } else {
             logger.error("Action discarded due to the following reason: "
                     + reason);
         }
     }
 
     @SuppressWarnings("unchecked")
     private Set<VEEReplica> createReplica(VEE originalVEE, int replicaNumber) {
 
         /*
          * Set<VEEReplica> replicas = new HashSet<VEEReplica>();
          *
          * for (int q=0; q < replicaNumber;q++) {
          *
          * VEEReplica veeReplica = new VEEReplica(originalVEE);
          *
          * replicaCustomInfo = new HashMap<String, String>();
          *
          * logger.info("Deploying replica: "+ veeReplica.getFQN());
          * veeReplica.registerHwElementsInResDir();
          *
          * ReservoirDirectory.getInstance().registerObject(veeReplica.getFQN(),
          * veeReplica);
          *
          * MonitoringRestBusConnector restBusConnector =
          * MonitoringRestBusConnector.getInstance(); List<String> restEndPoints
          * = restBusConnector.getRestEndPoints();
          *
          * if ((restEndPoints != null) && (restEndPoints.size() > 0)) {
          * logger.info("KPIMonitorEndPoint customization value: " +
          * restBusConnector.getRestEndPoints().get(0));
          * replicaCustomInfo.put("KPIMonitorEndPoint",
          * restBusConnector.getRestEndPoints().get(0)); }
          *
          * replicaCustomInfo.put("KPIQualifier", sap.getFQN().toString() +
          * ".kpis"); replicaCustomInfo.put("ReplicaName",
          * veeReplica.getFQN().toString()); replicaCustomInfo.put("VEEid",
          * String .valueOf(veeReplica.getId()));
          * replicaCustomInfo.put("ReplicaName", veeReplica.getFQN().toString());
          *
          * // call parser to complete the hashTable and update the //
          * CustomizationInformation String in the VEEReplica // object
          * logger.info("Customizing replica " + veeReplica.getId() + " del VEE "
          * + originalVEE.getVEEName()); if (replicaCustomInfo.isEmpty())
          * logger.info("Empty custom information"); else { Iterator keyIter =
          * replicaCustomInfo.keySet().iterator();
          *
          * while (keyIter.hasNext()) { String key = (String) keyIter.next();
          * logger.info("Key: " + key + "; Value: "+ replicaCustomInfo.get(key));
          * } }
          *
          * // Get the networks for the VM Environment HashMap<String,String>
          * masks = new HashMap<String,String>();
          * HashMap<String,ArrayList<String>> ips = new
          * HashMap<String,ArrayList<String>>(); HashMap<String,String> dnss =
          * new HashMap<String, String> (); HashMap<String,String> gateways = new
          * HashMap<String, String> ();
          *
          * HashMap<String, Integer> ipsNeeded =
          * parser.getNumberOfIpsPerNetwork(originalVEE.getVEEName());
          *
          * for (Network net: sap.getNetworks()) { masks.put(net.getName(),
          * net.getNetworkAddresses()[1]); dnss.put(net.getName(),
          * net.getNetworkAddresses()[2]); gateways.put(net.getName(),
          * net.getNetworkAddresses()[3]); }
          *
          * for (NIC nic: veeReplica.getNICs()) { String networkName =
          * nic.getNICConf().getNetwork().getName();
          *
          * if (ips.get(networkName)==null) ips.put(networkName, new
          * ArrayList<String>());
          *
          * Integer iterations = ipsNeeded.get(networkName);
          *
          * if (iterations == null || iterations == 0) iterations = 1;
          *
          * for (int i=0; i < iterations; i++) { String replicaIP =
          * lcc.getHostAddress
          * (nic.getNICConf().getNetwork().getNetworkAddresses()[0]);
          *
          * if (replicaIP==null) { abort("Lack of ip addresses"); return null; }
          *
          * // Put the IP in the entrypoint map for this network. Map<String,
          * String> entryPoint = nic.getNICConf().getNetwork().getEntryPoints();
          *
          * if (entryPoint.containsKey(originalVEE.getVEEName())) { String
          * ipList= entryPoint.get(originalVEE.getVEEName());
          * entryPoint.put(originalVEE.getVEEName(), ipList + " " + replicaIP); }
          * else { entryPoint.put(originalVEE.getVEEName(), replicaIP); }
          *
          * nic.addIPAddress(replicaIP);
          *
          * ips.get(networkName).add(replicaIP); } }
          */
         if (this.abort) {
             this.logger
             .error("Unable to deploy replicas. Service already aborted");
             return null;
         }
 
         Set<VEEReplica> replicas = new HashSet<VEEReplica>();
 
         for (int q = 0; q < replicaNumber; q++) {
 
             VEEReplica veeReplica = new VEEReplica(originalVEE);
 
             replicaCustomInfo = new HashMap<String, String>();
 
             logger.info("Deploying replica: " + veeReplica.getFQN());
             veeReplica.registerHwElementsInResDir();
 
             ReservoirDirectory.getInstance().registerObject(
                     veeReplica.getFQN(), veeReplica);
 
             ReservoirDirectory.getInstance().registerObject(
                     veeReplica.getMemory().getFQN(), veeReplica.getMemory());
 
             logger.info("PONG Memory FQN: " + veeReplica.getMemory());
 
             Iterator iter = veeReplica.getDisks().iterator();
 
             while (iter.hasNext()) {
                 Disk disc = (Disk) iter.next();
                 ReservoirDirectory.getInstance().registerObject(disc.getFQN(),
                         disc);
                 logger.info("PONG DISK FQN: " + disc.getFQN());
             }
 
             iter = veeReplica.getCPUs().iterator();
 
             while (iter.hasNext()) {
                 CPU cpu = (CPU) iter.next();
                 ReservoirDirectory.getInstance().registerObject(cpu.getFQN(),
                         cpu);
                 logger.info("PONG CPU FQN: " + cpu.getFQN());
             }
 
             iter = veeReplica.getNICs().iterator();
 
             while (iter.hasNext()) {
                 NIC nic = (NIC) iter.next();
                 ReservoirDirectory.getInstance().registerObject(nic.getFQN(),
                         nic);
                 logger.info("PONG NIC FQN: " + nic.getFQN());
             }
 
             MonitoringRestBusConnector restBusConnector = MonitoringRestBusConnector
             .getInstance();
             List<String> restEndPoints = restBusConnector.getRestEndPoints();
 
             if ((restEndPoints != null) && (restEndPoints.size() > 0)) {
                 logger.info("KPIMonitorEndPoint customization value: "
                         + restBusConnector.getRestEndPoints().get(0));
                 replicaCustomInfo.put("KPIMonitorEndPoint", restBusConnector
                         .getRestEndPoints().get(0));
             }
 
             replicaCustomInfo.put("KPIQualifier", sap.getFQN().toString()
                     + ".kpis");
             replicaCustomInfo
             .put("ReplicaName", veeReplica.getFQN().toString());
             replicaCustomInfo.put("VEEid", String.valueOf(veeReplica.getId()));
             replicaCustomInfo
             .put("ReplicaName", veeReplica.getFQN().toString());
 
             // call parser to complete the hashTable and update the
             // CustomizationInformation String in the VEEReplica
             // object
             logger.info("Customizing replica " + veeReplica.getId()
                     + " del VEE " + originalVEE.getVEEName());
             if (replicaCustomInfo.isEmpty())
                 logger.info("Empty custom information");
             else {
                 Iterator keyIter = replicaCustomInfo.keySet().iterator();
 
                 while (keyIter.hasNext()) {
                     String key = (String) keyIter.next();
                     logger.info("Key: " + key + "; Value: "
                             + replicaCustomInfo.get(key));
                 }
             }
 
             // Get the networks for the VM Environment
             HashMap<String, String> masks = new HashMap<String, String>();
             HashMap<String, ArrayList<String>> ips = new HashMap<String, ArrayList<String>>();
             HashMap<String, String> dnss = new HashMap<String, String>();
             HashMap<String, String> gateways = new HashMap<String, String>();
 
             HashMap<String, Integer> ipsNeeded = parser
             .getNumberOfIpsPerNetwork(originalVEE.getVEEName());
 
             for (Network net : sap.getNetworks()) {
                 masks.put(net.getName(), net.getNetworkAddresses()[1]);
                 dnss.put(net.getName(), net.getNetworkAddresses()[2]);
                 gateways.put(net.getName(), net.getNetworkAddresses()[3]);
             }
 
             for (NIC nic : veeReplica.getNICs()) {
                 String networkName = nic.getNICConf().getNetwork().getName();
 
                 if (ips.get(networkName) == null)
                     ips.put(networkName, new ArrayList<String>());
 
                 Integer iterations = ipsNeeded.get(networkName);
 
                 if (iterations == null || iterations == 0)
                     iterations = 1;
 
                 String staticIpProp = parser.getStaticIpProperty(originalVEE.getVEEName());
                 
                 for (int i = 0; i < iterations; i++) {
 
                 	 logger.info("PONG static ip property: "+ staticIpProp);
                 	
                     String replicaIP = lcc.getHostAddress(nic.getNICConf()
                             .getNetwork().getNetworkAddresses()[0],staticIpProp);
                     staticIpProp=null;
                     if (replicaIP == null) {
                         abort("Lack of ip addresses");
                         return null;
                     }
                     
                     
 
                     // Put the IP in the entrypoint map for this network.
                     Map<String, String> entryPoint = nic.getNICConf()
                     .getNetwork().getEntryPoints();
 
                     if (entryPoint.containsKey(originalVEE.getVEEName())) {
                         String ipList = entryPoint
                         .get(originalVEE.getVEEName());
                         entryPoint.put(originalVEE.getVEEName(), ipList + " "
                                 + replicaIP);
                     } else {
                         entryPoint.put(originalVEE.getVEEName(), replicaIP);
                     }
 
                     nic.addIPAddress(replicaIP);
 
                     ips.get(networkName).add(replicaIP);
                 }
             }
 
             HashMap<String, HashMap<String, String>> entryPoints = new HashMap<String, HashMap<String, String>>();
 
             for (Network net : sap.getNetworks())
                 entryPoints.put(net.getName(), net.getEntryPoints());
 
             try {
                 veeReplica.setOvfRepresentation(parser
                         .inEnvolopeMacroReplacement(originalVEE
                                 .getOvfRepresentation(), veeReplica.getId(),
                                 SMConfiguration.getInstance().getDomainName(),
                                 sap.getFQN().toString(), SMConfiguration
                                 .getInstance().getMonitoringAddress(),
                                 ips, masks, dnss, gateways, entryPoints));
             } catch (IOException e) {
                 abort("Unable to create environment file: " + e.getMessage());
             } catch (IllegalArgumentException iae) {
                 abort("Some data could not be calculated for the OVF environment: "
                         + iae.getMessage());
             }
 
             // XXXXXXXXXXXXXXXXXXXXXXXXXX
             // to be added to OVF: How is a swap disk defined?
             // XXXXXXXXXXXXXXXXXXXXXXXXXX
             if (setSwap) {
                 DiskConf swap = new DiskConf(256, null, null);
                 swap.setType("swap");
                 veeReplica.getVEE().addDiskConf(swap);
             } else
                 logger.info("Skipping swap disk");
 
             // generate additional image
             @SuppressWarnings("unused")
             String pathToCustomizationFile = createCustomizationFile(veeReplica);
 
             replicas.add(veeReplica);
         }
 
         // deploy the Replica with the newly generated
         logger.info("Deploying replicas...");
         if (deployReplica(replicas)) {
 
             for (VEEReplica veeReplica : replicas) {
                 originalVEE.registerVEEReplica(veeReplica);
                 originalVEE.addCurrentReplicas();
 
                 try {
                     DbManager.getDbManager().save(veeReplica);
                 } catch (Throwable e) {
                     abort("Error updating the model in the DB: "
                             + e.getMessage());
                 }
             }
         } else
             return null;
 
         return replicas;
     }
 
     private void addRegularReplicaToBalancer(VEEReplica veeReplica,
             VEE balancerVEE) {
         logger.info("Adding regular replica to load balancer");
         logger.info("Configuring loadbalancer for "
                + veeReplica.getVEE().getFQN());
 
         String replicaIP = veeReplica.getNICs().iterator().next()
         .getIPAddresses().iterator().next();
 
         Set<VEEReplica> balancerReplicas = balancerVEE.getVEEReplicas();
         
          for (VEEReplica balancer : balancerReplicas) { Set<NIC> nics =
          balancer.getNICs(); for (NIC nic : nics) { if
          (!nic.getNICConf().getNetwork().getPrivateNet()) { List<String>
          addresses = nic.getIPAddresses(); for (String ip : addresses) {
         	 if (balancerVEE.getLbManagementPort()==0)
         	 {
         		 logger.error("No management port for the balancer"); 
         		 return;
         	 }
          this.lbConfigurator.addNode(ip, balancerVEE.getLbManagementPort(),
         veeReplica.getFQN() .toString(), replicaIP); } } } }
          
     }
 
 }
