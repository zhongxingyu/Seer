 /**
  * Copyright (C) 2010-2013 Eugen Feller, INRIA <eugen.feller@inria.fr>
  *
  * This file is part of Snooze, a scalable, autonomic, and
  * energy-aware virtual machine (VM) management framework.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation, either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see <http://www.gnu.org/licenses>.
  */
 package org.inria.myriads.snoozeclient.handler;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.SwingUtilities;
 
 import org.inria.myriads.snoozeclient.configurator.api.ClientConfiguration;
 import org.inria.myriads.snoozeclient.configurator.general.GeneralSettings;
 import org.inria.myriads.snoozeclient.database.api.AttributeType;
 import org.inria.myriads.snoozeclient.database.api.ClientRepository;
 import org.inria.myriads.snoozeclient.discovery.VirtualMachineDiscovery;
 import org.inria.myriads.snoozeclient.exception.BootstrapUtilityException;
 import org.inria.myriads.snoozeclient.exception.CommandHandlerException;
 import org.inria.myriads.snoozeclient.parser.commands.ClientCommand;
 import org.inria.myriads.snoozeclient.parser.output.ParserOutput;
 import org.inria.myriads.snoozeclient.resourcecontrol.VirtualClusterControl;
 import org.inria.myriads.snoozeclient.statistics.results.SubmissionResults;
 import org.inria.myriads.snoozeclient.statistics.util.SubmissionResultsUtils;
 import org.inria.myriads.snoozeclient.systemtree.SystemTreeVisualizer;
 import org.inria.myriads.snoozeclient.systemtree.graph.SystemGraphGenerator;
 import org.inria.myriads.snoozeclient.util.BootstrapUtilis;
 import org.inria.myriads.snoozecommon.communication.NetworkAddress;
 import org.inria.myriads.snoozecommon.communication.groupmanager.GroupManagerDescription;
 import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerDescription;
 import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerList;
 import org.inria.myriads.snoozecommon.communication.rest.CommunicatorFactory;
 import org.inria.myriads.snoozecommon.communication.rest.api.GroupManagerAPI;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.VirtualMachineMetaData;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.discovery.VirtualMachineDiscoveryResponse;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.monitoring.NetworkDemand;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.monitoring.VirtualMachineMonitoringData;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.requests.MetaDataRequest;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.status.VirtualClusterErrorCode;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.status.VirtualMachineStatus;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.submission.VirtualClusterSubmissionRequest;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.submission.VirtualClusterSubmissionResponse;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.submission.VirtualMachineLocation;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.submission.VirtualMachineTemplate;
 import org.inria.myriads.snoozecommon.communication.virtualmachine.ResizeRequest;
 import org.inria.myriads.snoozecommon.globals.Globals;
 import org.inria.myriads.snoozecommon.guard.Guard;
 import org.inria.myriads.snoozecommon.parser.VirtualClusterParserFactory;
 import org.inria.myriads.snoozecommon.parser.api.VirtualClusterParser;
 import org.inria.myriads.snoozecommon.util.MonitoringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Command handling logic.
  * 
  * @author Eugen Feller
  */
 public final class CommandHandler 
 {        
     /** Define the logger. */
     private static final Logger log_ = LoggerFactory.getLogger(CommandHandler.class);
     
     /** Client configuration. */
     private ClientConfiguration clientConfiguration_;
     
     /** Hold the parser output. */
     private ParserOutput parserOutput_;
     
     /** Client repository. */
     private ClientRepository clientRepository_;
     
     /** Indicates the first virtual machine to be processed. */
     private boolean isFirst_ = true;
     
     /**
      * CommandHandler constructor.
      * 
      * @param clientConfiguration  The configuration
      * @param clientRepository     The client repository
      * @param parserOutput         The parser output
      * @throws Exception 
      */
     public CommandHandler(ClientConfiguration clientConfiguration,
                           ClientRepository clientRepository,
                           ParserOutput parserOutput) 
         throws Exception 
     {
         Guard.check(clientConfiguration, clientRepository, parserOutput);
         clientConfiguration_ = clientConfiguration;
         clientRepository_ = clientRepository;
         parserOutput_ = parserOutput;     
     }
             
     /**
      * Dispatches the client command.
      * 
      * @throws Exception 
      */
     public void dispatchCommand() 
         throws Exception
     {                    
         ClientCommand command = parserOutput_.getClientCommand();
         log_.debug(String.format("Dispatching client request: %s", command));
         
         GeneralSettings generalSettings = clientConfiguration_.getGeneralSettings();
         int numberOfMonitoringEntries = generalSettings.getNumberOfMonitoringEntries();
         SystemGraphGenerator graphGenerator = new SystemGraphGenerator(numberOfMonitoringEntries);
         List<NetworkAddress> bootstrapNodes = generalSettings.getBootstrapNodes();
         
         switch (command) 
         {                   
             case LIST :
                 processList();               
                 break;
                 
             case DEFINE :
                 processDefine();              
                 break;
                 
             case UNDEFINE :
                 processUndefine();                
                 break;
 
             case ADD :
                 processAdd();                
                 break;
                 
             case REMOVE :
                 processRemove();
                 break;
  
             case START : 
                 processStart();
                 return;
                 
             case INFO : 
                 processCollectiveCommand(command);
                 break;
                 
             case SUSPEND : 
                 processCollectiveCommand(command);              
                 break;
             
             case RESUME :
                 processCollectiveCommand(command);
                 break;
             
             case SHUTDOWN :
                 processCollectiveCommand(command);
                 break;
              
             case REBOOT :
                 processCollectiveCommand(command);
                 break;
                 
             case DESTROY :
                 processCollectiveCommand(command);
                 break;
                 
             case VISUALIZE :
                 processVisualizeCommand(clientConfiguration_, graphGenerator);
                 break;
                 
             case DUMP :
                 processDumpCommand(bootstrapNodes, graphGenerator, generalSettings.getDumpOutputFile());
                 break;
                 
             case RESIZE:
                 processResizeCommand(command);
                 break;
             case HOSTS:
                 processHosts();
                 break;
             default:
                 throw new CommandHandlerException(String.format("Unknown cluster command specified: %s", command));
         }
     }
     
 
     /**
      * 
      * Process the hosts list request.
      * @throws BootstrapUtilityException 
      * @throws CommandHandlerException 
      * 
      */
     private void processHosts() throws CommandHandlerException, BootstrapUtilityException 
     {
         // TODO Auto-generated method stub
         GroupManagerDescription groupLeader = getGroupLeaderDescription(); 
         
         NetworkAddress groupLeaderAddress = groupLeader.getListenSettings().getControlDataAddress();
         GroupManagerAPI groupLeaderCommunicator = CommunicatorFactory.newGroupManagerCommunicator(groupLeaderAddress); 
         
         LocalControllerList localControllers = groupLeaderCommunicator.getLocalControllerList();
         
         for (LocalControllerDescription localController : localControllers.getLocalControllers())
             displayLocalControllerDescription(localController);
     }
 
     /**
      * 
      * Process a resize command.
      * 
      * @param command           The client command
      * @throws Exception        Exception
      */
     private void processResizeCommand(ClientCommand command) throws Exception 
     {
         Guard.check(command);
         log_.debug(String.format("Processing collective command: %s", command));
 
         String virtualMachineName = parserOutput_.getVirtualMachineName();
         double vcpu = parserOutput_.getVcpu();
         double memory = parserOutput_.getMemory();
         double tx = parserOutput_.getNetworkCapacity().getTxBytes();
         double rx = parserOutput_.getNetworkCapacity().getRxBytes();
         
         if (virtualMachineName != null)
         {
             processVirtualMachineResize(virtualMachineName, vcpu, memory, tx, rx);
             return;
         } 
         
         String virtualClusterName = parserOutput_.getVirtualClusterName();
         if (virtualClusterName != null)
         {
             processVirtualClusterResize(virtualClusterName, vcpu, memory, tx, rx);
             return;
         }          
         
         throw new CommandHandlerException("You must at least specify a virtual machine or cluster name!");
     }
         
 
     /**
      * 
      * Process a resize for a cluster.
      * 
      * @param virtualClusterName        The virtual cluster 
      * @param vcpu                      The vcpu demand
      * @param memory                    The memory demand
      * @param tx                        The tx demand
      * @param rx                        The rx demand  
      * @throws Exception                Exception
      */
     private void processVirtualClusterResize(String virtualClusterName,
             double vcpu, double memory, double tx, double rx) throws Exception 
     {
         Guard.check(virtualClusterName, vcpu, memory, tx, rx);
         
         List<String> virtualMachineIds = clientRepository_.getVirtualMachineIds(virtualClusterName);        
         for (String virtualMachineId : virtualMachineIds)
         {               
             processVirtualMachineResize(virtualMachineId, vcpu, memory, tx, rx);    
         }       
     }
 
     /**
      * 
      * Process a resize for a single virtual machine.
      * 
      * @param virtualMachineName        The virtual machine 
      * @param vcpu                      The vcpu demand
      * @param memory                    The memory demand
      * @param tx                        The tx demand
      * @param rx                        The rx demand  
      */
     private void processVirtualMachineResize(String virtualMachineName,
             double vcpu, double memory, double tx, double rx) 
     {
         try
         {
             Guard.check(virtualMachineName, vcpu, memory, tx, rx);
             
             VirtualMachineMetaData metaData = clientRepository_.getVirtualMachineMetaData(virtualMachineName);
             if (metaData == null)
             {
                 log_.error(String.format("Virtual machine: %s meta data not found! Is it added?", virtualMachineName));
                 return;
             }
             VirtualMachineLocation location = metaData.getVirtualMachineLocation();
             
             ResizeRequest resizeRequest = new ResizeRequest();
             ArrayList<Double> requestedCapacity = new ArrayList<Double>(Arrays.asList(vcpu, memory, tx, rx));
             resizeRequest.setResizedCapacity(requestedCapacity);
             resizeRequest.setVirtualMachineLocation(location);
             
             log_.debug(String.format("Command: %s for virtual machine: %s on local controller %s", 
                                       "resize", virtualMachineName, location.getLocalControllerId()));
 
             NetworkAddress groupManagerAddress = metaData.getGroupManagerControlDataAddress();
             VirtualClusterControl virtualClusterControl = createVirtualClusterControl(location, groupManagerAddress);
             
             VirtualMachineMetaData newVirtualMachineMetaData = 
                     virtualClusterControl.resize(resizeRequest);
             
             if (newVirtualMachineMetaData != null)
             {
                 log_.info("Resize command successfull for virtual machine " + virtualMachineName);
             }
             else
             {
                 log_.info("Resize command failed for virtual machine " + virtualMachineName);
             }
 //FAKE resize here...  
 //            String newXmlDescription = parser.handleResizeRequest(virtualMachineTemplate, resizeRequest);
 //            //write the network demand in the client database...
 //            clientRepository_.updateNetworkCapacityDemand(virtualMachineName, new NetworkDemand(rx, tx));
 //            //write result back to the template
 //            String virtualMachineTemplatePath = clientRepository_.getVirtualMachineTemplate(virtualMachineName);
 //            BufferedWriter out = new BufferedWriter(new FileWriter(virtualMachineTemplatePath));
 //            out.write(newXmlDescription);
 //            out.close();
             
         }
         catch (Exception e)
         {
             log_.warn("Resize Command Failed");
         }
         
     }
 
     
 
     /**
      * Processes the visualize command.
      * 
      * @param clientConfiguration   The client configuration
      * @param graphGenerator        The graph generator
      * @throws Exception            The exception
      */
     private void processVisualizeCommand(final ClientConfiguration clientConfiguration,
                                          final SystemGraphGenerator graphGenerator)
         throws Exception
     {
         log_.debug(String.format("Proessing visualize command"));     
         SwingUtilities.invokeLater(new Runnable() 
         {
             public void run() 
             {
                 SystemTreeVisualizer visualizer = new SystemTreeVisualizer(clientConfiguration, graphGenerator);
                 visualizer.setVisible(true); 
             }
         });           
     }
     
     /**
      * Processes the dump command.
      * 
      * @param bootstrapNodes    The bootstrap nodes
      * @param graphGenerator    The graph generator
      * @param dumpOutputFile    The dump output file
      * @throws Exception        The exception
      */
     private void processDumpCommand(List<NetworkAddress> bootstrapNodes, 
                                     SystemGraphGenerator graphGenerator,
                                     String dumpOutputFile)
         throws Exception
     {
         GroupManagerDescription groupLeader = BootstrapUtilis.getGroupLeaderDescription(bootstrapNodes);
         //DumpUtil.writeGraph(graphGenerator.generateGraph(groupLeader), dumpOutputFile);
     }
     
     /**
      * Processes the remove command.
      * 
      * @throws Exception 
      */
     private void processRemove() 
         throws Exception 
     {        
         log_.debug("Processing remove");              
         boolean isRemoved = false;
         String virtualMachineName = parserOutput_.getVirtualMachineName();
         if (virtualMachineName == null)
         {
             throw new CommandHandlerException("You must specify a virtual machine name!");
         }
         
         String virtualClusterName = parserOutput_.getVirtualClusterName();
         if (virtualClusterName == null)
         {
             throw new CommandHandlerException("You must specify a virtual cluster name!");
         }
         
         log_.debug(String.format("Removing virtual machine: %s", virtualMachineName));
         isRemoved = clientRepository_.removeVirtualMachineDescription(virtualMachineName,  
                                                                       virtualClusterName);
         if (!isRemoved)
         {
             throw new CommandHandlerException("Failed to remove virtual machine!");
         }
         
         log_.info("Virtual machine has been removed!");
     }
 
     /** 
      * Processes the add command.
      * 
      * @throws Exception 
      */
     private void processAdd() 
         throws Exception 
     {
         log_.debug("Processing add command");
         
         boolean isAdded = false;
         String virtualMachineTemplate = parserOutput_.getVirtualMachineTemplate();
         if (virtualMachineTemplate == null)
         {
             throw new CommandHandlerException("Add command failed! You must specify a virtual machine template!");
         }
 
         VirtualMachineTemplate template = new VirtualMachineTemplate();
         template.setLibVirtTemplate(virtualMachineTemplate);
         template.setNetworkCapacityDemand(parserOutput_.getNetworkCapacity());
         template.setHostId(parserOutput_.getHostId());
         isAdded = clientRepository_.addVirtualMachineTemplate(template, parserOutput_.getVirtualClusterName());        
         if (!isAdded)
         {
             throw new CommandHandlerException("Add command failed! Does this virtual machine already exist?");
         }
         
         log_.info("Add command successfull!");
     }
 
     /** 
      * Processes the define command.
      * 
      * @throws Exception 
      */
     private void processDefine() 
         throws Exception 
     {
         log_.debug("Processing define");
         String virtualClusterName = parserOutput_.getVirtualClusterName();
         if (virtualClusterName == null)
         {
             throw new CommandHandlerException("You must specify a cluster name!");
         }
                  
         boolean isDefined = clientRepository_.defineVirtualCluster(virtualClusterName);       
         if (!isDefined)
         {
             throw new CommandHandlerException("Failed to define the cluster! Does it already exist?");
         }
         
         log_.info("Cluster has been defined!");
     }
 
     /** 
      * Processes the undefine command.
      * 
      * @throws Exception 
      */
     private void processUndefine() 
         throws Exception 
     {   
         log_.debug("Processing undefine");
         String virtualClusterName = parserOutput_.getVirtualClusterName();
         if (virtualClusterName == null)
         {
             throw new CommandHandlerException("You must specify a cluster name!");
         }
            
         boolean isUndefined = clientRepository_.undefineVirtualCluster(virtualClusterName);   
         if (!isUndefined)
         {
             throw new CommandHandlerException("Failed to undefine the cluster! Does it exist?");
         }
         
         log_.info("Cluster has been undefined!");
     }
  
     /**
      * Creates virtual cluster submission request.
      * 
      * @return              The virtual cluster submission request
      * @throws Exception 
      */
     private VirtualClusterSubmissionRequest createVirtualClusterSubmissionRequest()   
         throws Exception
     {
         VirtualClusterSubmissionRequest submissionRequest; 
         String virtualMachineName = parserOutput_.getVirtualMachineName();
         if (virtualMachineName != null)
         {
             log_.debug("Getting virtual cluster description for a single virtual machine");
             submissionRequest = clientRepository_.createVirtualClusterSubmissionRequest(virtualMachineName, 
                                                                                         AttributeType.vm);
         } else
         {
             log_.debug("Getting virtual cluster description for a set of virtual machines");
             String clusterName = parserOutput_.getVirtualClusterName();
             submissionRequest = clientRepository_.createVirtualClusterSubmissionRequest(clusterName, 
                                                                                         AttributeType.cluster);
         }
         return submissionRequest;        
     }
     
     /**
      * Starts the virtual cluster submission.
      *  
      * @param submissionRequest                 The virtual cluster submission request
      * @return                                  The virtual cluster submission response
      * @throws BootstrapUtilityException 
      * @throws CommandHandlerException 
      */
     private VirtualClusterSubmissionResponse 
         startVirtualClusterSubmission(VirtualClusterSubmissionRequest submissionRequest) 
         throws CommandHandlerException, BootstrapUtilityException
     {
         GroupManagerDescription groupLeader = getGroupLeaderDescription();  
         NetworkAddress groupLeaderAddress = groupLeader.getListenSettings().getControlDataAddress();   
         VirtualClusterControl virtualClusterControl = new VirtualClusterControl(clientConfiguration_);
         VirtualClusterSubmissionResponse response = virtualClusterControl.start(submissionRequest, 
                                                                                 groupLeaderAddress); 
         return response;
     }
     
     /**
      * Processes the cluster start command.
      * 
      * @throws Exception 
      */
     private void processStart() 
         throws Exception
     {
         log_.debug("Processing the start command");
         
         VirtualClusterSubmissionRequest submissionRequest = createVirtualClusterSubmissionRequest();            
         if (submissionRequest == null)
         {
             throw new CommandHandlerException("Unable to generate the virtual cluster description!");
         }
                            
         long startSystemTime = System.currentTimeMillis();
         VirtualClusterSubmissionResponse response = startVirtualClusterSubmission(submissionRequest);   
         if (response == null)
         {
             throw new CommandHandlerException("The cluster submission response is emtpy! Report this to the system " +
                                               "administrator!");
         }
         long finishSystemTime = System.currentTimeMillis();
         
         processVirtualClusterResponse(response); 
         if (clientConfiguration_.getStatisticsSettings().isEnabled())
         {
             log_.debug("Statistics mode is enabled! Starting the creation!");
             generateAndWriteSubmissionResults(response, 
                                               submissionRequest.getVirtualMachineTemplates().size(),
                                               startSystemTime, 
                                               finishSystemTime);           
         }
     }
         
     /**
      * Returns the group leader description.
      * 
      * @return                              The group leader description
      * @throws CommandHandlerException      The command handler exception
      * @throws BootstrapUtilityException       The global utility exception
      */
     private GroupManagerDescription getGroupLeaderDescription() 
         throws CommandHandlerException, BootstrapUtilityException 
     {
         List<NetworkAddress> bootstrapNodes = clientConfiguration_.getGeneralSettings().getBootstrapNodes();
         GroupManagerDescription groupLeaderDescription = BootstrapUtilis.getGroupLeaderDescription(bootstrapNodes); 
         return groupLeaderDescription;
     }
 
     /**
      * Generates and writes submission results.
      * 
      * @param virtualClusterResponse    The virtual cluster response
      * @param numberOfVirtualMachines   The number of virtual machines
      * @param startSystemTime           The start system time
      * @param finishSystemTime          The finish system time
      * @throws IOException              The IO exception
      */
     private void generateAndWriteSubmissionResults(VirtualClusterSubmissionResponse virtualClusterResponse,
                                                    int numberOfVirtualMachines,
                                                    long startSystemTime,
                                                    long finishSystemTime)
         throws IOException
     {
         Guard.check(virtualClusterResponse, startSystemTime, finishSystemTime);
         log_.debug("Generating and writing submission results");
         
         SubmissionResults submissionResults = 
             SubmissionResultsUtils.generateSubmissionResults(virtualClusterResponse,
                                                                numberOfVirtualMachines,
                                                                startSystemTime,
                                                                finishSystemTime);
         SubmissionResultsUtils.writeSubmissionResults(clientConfiguration_.getStatisticsSettings().getOutput(), 
                                                         submissionResults);            
     }
 
     /**
      * Generates information output.
      * 
      * @param virtualMachine     The virtual machine description
      * @return                   The output data
      */
     private ConsoleOutput generateInformationOutput(VirtualMachineMetaData virtualMachine)
     {
         Guard.check(virtualMachine);
         log_.debug("Generating information output");
         
         String cpuUtilization = Globals.DEFAULT_INITIALIZATION;
         String memoryUtilization = Globals.DEFAULT_INITIALIZATION;
         String networkUtilization = Globals.DEFAULT_INITIALIZATION;    
         String finalStatus = Globals.DEFAULT_INITIALIZATION;
         
         VirtualMachineStatus tmpStatus = virtualMachine.getStatus();
         if (tmpStatus.equals(VirtualMachineStatus.ERROR))
         { 
             finalStatus = tmpStatus + "(" + virtualMachine.getErrorCode() + ")";
         } else 
         {
             finalStatus = tmpStatus.toString();
         }
         
         Map<Long, VirtualMachineMonitoringData> usageHistory = virtualMachine.getUsedCapacity();
         if (usageHistory != null && !usageHistory.isEmpty())
         {
             VirtualMachineMonitoringData monitoring = 
                 MonitoringUtils.getLatestVirtualMachineMonitoringData(usageHistory);
             ArrayList<Double> usedCapacity = monitoring.getUsedCapacity();
             
             DecimalFormat format = new DecimalFormat("#.##"); 
             cpuUtilization = format.format(usedCapacity.get(Globals.CPU_UTILIZATION_INDEX));            
             memoryUtilization = format.format(usedCapacity.get(Globals.MEMORY_UTILIZATION_INDEX));
             
             String usedRxTraffic = format.format(usedCapacity.get(Globals.NETWORK_RX_UTILIZATION_INDEX));
             String usedTxTraffic = format.format(usedCapacity.get(Globals.NETWORK_TX_UTILIZATION_INDEX));
             networkUtilization = usedRxTraffic + "/" + usedTxTraffic;
         }
         
         ConsoleOutput output = new ConsoleOutput(cpuUtilization, 
                                                          memoryUtilization, 
                                                          networkUtilization,
                                                          finalStatus);        
         return output;
                 
     }
     
     /**
      * Print the virtual machine information.
      * 
      * @param virtualMachine  The virtual machine meta data
      */
     private void displayVirtualMachineInformation(VirtualMachineMetaData virtualMachine)
     {
         Guard.check(virtualMachine);
         log_.debug("Printing virtual machine information");
          
         String header = "%-25.25s \t %-15.15s \t %-15.15s \t %-15.15s \t %-15.15s \t %-15.15s \t %-15.15s \t %-10s";
         if (isFirst_)
         {
             log_.info(String.format(header, 
                                     "Name", "CPU usage", "Memory usage", "Rx/Tx usage", 
                                     "VM address", "GM address", "LC address", "Status"));    
             String divider = "------------------------------------------------------------------------------------" +
                              "------------------------------------------------------------------------------------" +
                              "------------------";
             log_.info(divider);
             isFirst_ = false;
         }
         
         ConsoleOutput output = generateInformationOutput(virtualMachine);      
         String virtualMachineAddress = virtualMachine.getIpAddress();
        String groupManagerAddress = virtualMachine.getGroupManagerControlDataAddress().getAddress();
         String localControllerAddress = virtualMachine.getVirtualMachineLocation().
                                             getLocalControllerControlDataAddress().getAddress();
         log_.info(String.format(header,
                                 virtualMachine.getVirtualMachineLocation().getVirtualMachineId(),
                                 output.getCpuUtilization(),
                                 output.getMemoryUtilization(),
                                 output.getNetworkUtilization(),
                                 virtualMachineAddress,
                                 groupManagerAddress,
                                 localControllerAddress,
                                 output.getFinalStatus()));
     }
     
     /**
      * 
      * Displays the local controller list
      * 
      * @param localControllers
      */
     private void displayLocalControllerDescription(LocalControllerDescription localController)
     {
         Guard.check(localController);
         log_.debug("Printing local controllers list");
         String header = "%-40.40s \t %-15.15s \t %-15.15s \t %-15.15s \t %-15.15s \t %-15.15s \t %-15.15s";
         if (isFirst_)
         {
             log_.info(String.format(header, 
                                     "UUID", "address:port", "VCPUs", "Memory", 
                                     "Tx", "Rx", "Status"));    
             String divider = "------------------------------------------------------------------------------------" +
                              "------------------------------------------------------------------------------------" +
                              "------------------";
             log_.info(divider);
             isFirst_ = false;
         }
               
         String localControllerId = localController.getId();
         String address = localController.getControlDataAddress().toString();
         Double vcpus = localController.getTotalCapacity().get(Globals.CPU_UTILIZATION_INDEX);
         Double memory = localController.getTotalCapacity().get(Globals.MEMORY_UTILIZATION_INDEX);
         Double tx = localController.getTotalCapacity().get(Globals.NETWORK_TX_UTILIZATION_INDEX);
         Double rx = localController.getTotalCapacity().get(Globals.NETWORK_RX_UTILIZATION_INDEX);
         String status = String.valueOf(localController.getStatus());
         log_.info(String.format(header,
                                 localControllerId,
                                 address,
                                 vcpus,
                                 memory,
                                 tx,
                                 rx,
                                 status
                                 ));
     }
     
     /**
      * Will print the virtual cluster response.
      * 
      * @param virtualClusterResponse     The virtual cluster response
      */
     private void displayVirtualClusterResponse(VirtualClusterSubmissionResponse virtualClusterResponse)
     {
         log_.debug("Printing the virtual cluster response");
                       
         List<VirtualMachineMetaData> metaData = virtualClusterResponse.getVirtualMachineMetaData();
         if (metaData == null)
         {
             log_.error("Virtual machine meta data is NULL! This should never happen!");
             return;
         }
                
         String header = "%-25.25s \t %-15.15s \t %-15.15s \t %-15.15s \t %-10s";
         String divider = "------------------------------------------------------------------------------------" +
                          "---------------------------";
         log_.info(String.format(header, "Name", "VM address", "GM address", "LC address", "Status"));
         log_.info(divider);
         
         for (VirtualMachineMetaData virtualMachine : metaData) 
         {
             String finalStatus;
             VirtualMachineStatus tmpStatus = virtualMachine.getStatus();
             if (tmpStatus.equals(VirtualMachineStatus.ERROR))
             { 
                 finalStatus = tmpStatus + "(" + virtualMachine.getErrorCode() + ")";
             } else 
             {
                 finalStatus = tmpStatus.toString();
             }
                       
             String virtualMachineAddress = virtualMachine.getIpAddress();
             if (virtualMachineAddress == null)
             {
                 virtualMachineAddress = Globals.DEFAULT_INITIALIZATION;
             }
             
             String groupManagerAddress = virtualMachine.getGroupManagerControlDataAddress().getAddress();
             if (groupManagerAddress == null)
             {
                 groupManagerAddress = Globals.DEFAULT_INITIALIZATION;
             }
                     
             String localControllerAddress = virtualMachine.getVirtualMachineLocation().
                                                   getLocalControllerControlDataAddress().getAddress();
             if (localControllerAddress == null)
             {
                 localControllerAddress = Globals.DEFAULT_INITIALIZATION;
             }
             
             String virtualMachineName = virtualMachine.getVirtualMachineLocation().getVirtualMachineId();
             log_.info(String.format(header, 
                                     virtualMachineName,
                                     virtualMachineAddress,
                                     groupManagerAddress,
                                     localControllerAddress,
                                     finalStatus));   
         }
     }
     
     /**
      * Processes the virtual cluster response.
      * 
      * @param virtualClusterResponse        The virtual cluster response
      * @throws Exception 
      */
     private void processVirtualClusterResponse(VirtualClusterSubmissionResponse virtualClusterResponse) 
         throws Exception
     {
         log_.debug("Processing the virtual cluster response");
                             
         VirtualClusterErrorCode virtualClusterError = virtualClusterResponse.getErrorCode();
         if (virtualClusterResponse.getErrorCode() != null)
         {
             throw new CommandHandlerException(String.format("The cluster response returned with error: %s", 
                                                             virtualClusterError));
         }
         
         displayVirtualClusterResponse(virtualClusterResponse);
         clientRepository_.addVirtualClusterResponse(virtualClusterResponse);
     }
     
     /**
      * Processes the list command.
      * 
      * @throws Exception    The exception
      */
     private void processList()
         throws Exception 
     {
         log_.debug("Processing cluster list request");
 
         String virtualClusterName = parserOutput_.getVirtualClusterName();
         if (virtualClusterName != null)
         {
             log_.debug(String.format("Printing content of cluster: %s", virtualClusterName));
             clientRepository_.printVirtualCluster(virtualClusterName);
             return;
         }
         
         log_.debug("Printing all clusters");
         clientRepository_.printVirtualClusters();
     }
     
     /**
      * Processes collective command (e.g., suspend).
      * 
      * @param command       The virtual cluster command
      * @throws Exception 
      */
     private void processCollectiveCommand(ClientCommand command) 
         throws Exception
     {
         Guard.check(command);
         log_.debug(String.format("Processing collective command: %s", command));
 
         String virtualMachineName = parserOutput_.getVirtualMachineName();
         if (virtualMachineName != null)
         {
             processVirtualMachineCommand(virtualMachineName, command);
             return;
         } 
         
         String virtualClusterName = parserOutput_.getVirtualClusterName();
         if (virtualClusterName != null)
         {
             processVirtualClusterCommand(virtualClusterName, command);
             return;
         }          
         
         throw new CommandHandlerException("You must atleast specify a virtual machine or cluster name!");
     }
     
     /**
      * Processes cluster command.
      * 
      * @param virtualClusterName        The virtual cluster name
      * @param command                   The virtual cluster command
      * @throws Exception 
      */
     private void processVirtualClusterCommand(String virtualClusterName, ClientCommand command) 
         throws Exception
     {        
         Guard.check(virtualClusterName, command);
         
         List<String> virtualMachineIds = clientRepository_.getVirtualMachineIds(virtualClusterName);        
         for (String virtualMachineId : virtualMachineIds)
         {               
             processVirtualMachineCommand(virtualMachineId, command);    
         }       
     }
         
     /**
      * Creates a meta data request.
      * 
      * @param location  The virtual machine location
      * @return          The meta data request
      */
     private MetaDataRequest createMetaDataRequest(VirtualMachineLocation location)
     {
         Guard.check(location);
         int numberOfMonitoringEntries = clientConfiguration_.getGeneralSettings().getNumberOfMonitoringEntries();
         MetaDataRequest request = new MetaDataRequest();
         request.setVirtualMachineLocation(location);
         request.setNumberOfMonitoringEntries(numberOfMonitoringEntries);
         return request;
     }
     
     /**
      * Processes virtual machine command.
      * 
      * @param virtualMachineId      The virtual machine identifier
      * @param command               The command
      * @throws Exception            The exception
      */
     private void processVirtualMachineCommand(String virtualMachineId, ClientCommand command)
         throws Exception 
     {
         Guard.check(virtualMachineId, command);
         VirtualMachineMetaData metaData = clientRepository_.getVirtualMachineMetaData(virtualMachineId);
         if (metaData == null)
         {
             log_.error(String.format("Virtual machine: %s meta data not found! Is it added?", virtualMachineId));
             return;
         }
               
         VirtualMachineLocation location = metaData.getVirtualMachineLocation();
         log_.debug(String.format("Command: %s for virtual machine: %s on local controller %s", 
                                   command, virtualMachineId, location.getLocalControllerId()));
 
         NetworkAddress groupManagerAddress = metaData.getGroupManagerControlDataAddress();
         VirtualClusterControl virtualClusterControl = createVirtualClusterControl(location, groupManagerAddress);
         executeVirtualMachineCommand(virtualClusterControl, command, location);   
     }
     
     /**
      * Executes the virtual machine command.
      * 
      * @param control        The virtual machine control
      * @param command        The virtual cluster command
      * @param location       The virtual machine location
      * @throws Exception     The exception
      */
     private void executeVirtualMachineCommand(VirtualClusterControl control,
                                               ClientCommand command, 
                                               VirtualMachineLocation location) 
         throws Exception
     {
         boolean isSuccessfull = false;
         log_.error(command.toString());
         switch (command)
         {
             case INFO :
                 VirtualMachineMetaData virtualMachine = null;
                 if (control == null)
                 {
                     virtualMachine = new VirtualMachineMetaData();
                     virtualMachine.setVirtualMachineLocation(location);
                     virtualMachine.setStatus(VirtualMachineStatus.OFFLINE);
                 } else
                 {
                     MetaDataRequest request = createMetaDataRequest(location);
                     virtualMachine = control.info(request);   
                 }
                 displayVirtualMachineInformation(virtualMachine);
                 break;
                 
             case SUSPEND:
                 if (control != null)
                 {
                     isSuccessfull = control.suspend(location);
                 }
                 break;
                 
             case RESUME:
                 if (control != null)
                 {
                     isSuccessfull = control.resume(location);
                 }
                 break;
                 
             case SHUTDOWN:
                 if (control != null)
                 {
                     isSuccessfull = control.shutdown(location);
                 }
                 break;
                 
             case REBOOT:
                 if (control != null)
                 {
                     isSuccessfull = control.reboot(location);
                 }
                 break;
                 
             case DESTROY:
                 if (control != null)
                 {
                     isSuccessfull = control.destroy(location);
                 }
                 break;
                 
                 
             default:
                 throw new CommandHandlerException(String.format("Unknown command specified: %s", command));
         }        
         
         String virtualMachineId = location.getVirtualMachineId();
         if (!command.equals(ClientCommand.INFO))
         {
             if (isSuccessfull)
             {
                 log_.info(String.format("Virtual machine %s %s executed successfully!", virtualMachineId, command));
             } else
             {
                 log_.info(String.format("Virtual machine %s %s failed!", virtualMachineId, command));
             }            
         }
     }
     
     
     /**
      * Discovers virtual machine.
      * 
      * @param virtualMachineId          The virtual machine identifier
      * @return                          The response
      * @throws BootstrapUtilityException 
      * @throws CommandHandlerException 
      */
     private VirtualMachineDiscoveryResponse discoverVirtualMachine(String virtualMachineId)
         throws CommandHandlerException, BootstrapUtilityException
     {
         GroupManagerDescription groupLeader = getGroupLeaderDescription();        
         NetworkAddress groupLeaderAddress = groupLeader.getListenSettings().getControlDataAddress();
         VirtualMachineDiscoveryResponse response = VirtualMachineDiscovery.discoverVirtualMachine(virtualMachineId, 
                                                                                                   groupLeaderAddress);
         return response;
     }
     
     /**
      * Updates virtual machine meta data.
      * 
      * @param location      The virtual machine location
      * @param response      The virtual machine discovery response
      * @throws Exception 
      */
     private void updateVirtualMachineMetaData(VirtualMachineLocation location, 
                                               VirtualMachineDiscoveryResponse response) 
         throws Exception
     {
         location.setLocalControllerId(response.getLocalControllerId());
         clientRepository_.updateVirtualMachineMetaData(location.getVirtualMachineId(), 
                                                        response.getLocalControllerId(),
                                                        response.getGroupManagerAddress()); 
     }
     
     /**
      * Creates virtual cluster control.
      * 
      * @param location              The virtual machine location
      * @param groupManagerAddress   The network address
      * @return                      The virtual cluster control object
      * @throws Exception            The exception
      */
     private VirtualClusterControl createVirtualClusterControl(VirtualMachineLocation location,
                                                               NetworkAddress groupManagerAddress) 
         throws Exception 
     {
         String virtualMachineId = location.getVirtualMachineId();
         log_.debug(String.format("Creating the virtual cluster control for virtual machine %s and local controller %s",
                                  virtualMachineId, location.getLocalControllerId()));
            
         if (location.getLocalControllerId() != null && 
             VirtualMachineDiscovery.hasVirtualMachine(location, groupManagerAddress))
         {          
             log_.debug("Virtual machine found on original group manager!");
             return new VirtualClusterControl(clientConfiguration_, groupManagerAddress);   
         }
         
         log_.debug("Virtual machine is not on the original group manager! Starting discovery!");
         VirtualMachineDiscoveryResponse response = discoverVirtualMachine(virtualMachineId);
         if (response == null)
         {
             log_.debug("Unable to discover the virtual machine! Not on any group manager?");
             return null;
         }
         
         log_.debug(String.format("Virtual machine found on local controller: %s", response.getLocalControllerId()));
         updateVirtualMachineMetaData(location, response);
         VirtualClusterControl control = new VirtualClusterControl(clientConfiguration_, 
                                                                   response.getGroupManagerAddress());
         return control;
     }
 }
