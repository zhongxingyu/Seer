 package org.vika.routing;
 
 import jade.core.Profile;
 import jade.core.ProfileImpl;
 import jade.core.Runtime;
 import jade.tools.sniffer.Agent;
 import jade.util.ExtendedProperties;
 import jade.util.leap.ArrayList;
 import jade.util.leap.Properties;
 import jade.wrapper.AgentContainer;
 import jade.wrapper.AgentController;
 import jade.wrapper.ControllerException;
 import jade.wrapper.StaleProxyException;
 import org.vika.routing.network.Network;
 import org.vika.routing.network.Node;
 import org.vika.routing.network.Parser;
 import org.vika.routing.network.jade.NodeAgent;
 import org.vika.routing.network.jade.TrafficAgent;
 import org.vika.routing.routing.DeikstraRoutingManager;
 import org.vika.routing.routing.NeuroRoutingManager;
 import org.vika.routing.routing.RoutingManager;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * @author oleg
  */
 public class Main {
 
     public static void main(String[] args) throws IOException, ControllerException, InterruptedException {
         final Options options = Options.readOptions(args);
         final String inputFileName = options.getInputFile();
         if (inputFileName == null){
             System.err.println("Input file not specified");
             Options.printUsage();
             System.exit(0);
         }
         final File inputFile = new File(inputFileName);
         if (!inputFile.exists() || inputFile.isDirectory()){
             System.err.println("Wrong input file: " + inputFile);
             Options.printUsage();
             System.exit(0);
         }
         final String outputFileName = options.getOutputFile();
         if (outputFileName == null){
             System.err.println("Output file not specified");
             Options.printUsage();
             System.exit(0);
         }
        final File outputFile = new File(outputFileName);
        if (!outputFile.exists() || outputFile.isDirectory()){
            System.err.println("Wrong output file: " + outputFile);
            Options.printUsage();
            System.exit(0);
        }
 
         // Create empty profile
         final Properties props = new ExtendedProperties();
         props.setProperty(Profile.LOCAL_SERVICE_MANAGER, "true");
         props.setProperty(Profile.LOCAL_HOST, "127.0.0.1");
         final Profile p = new ProfileImpl(props);
         // Start a new JADE runtime system
         final Runtime runtime = Runtime.instance();
         final AgentContainer container = runtime.createMainContainer(p);
 
         // Now we have successfully launched Agents platform
         final Network network = Parser.parse(inputFile);
         final Node[] nodes = network.nodes;
         final NodeAgent[] nodeAgents = new NodeAgent[nodes.length];
 
         final LoadManager loadManager = new LoadManager();
 
         // Time manager
        final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
         final TimeLogManager timeManager = new TimeLogManager(writer, options.getTimeLimit(), options.getTimeQuant());
 
         // Initiate traffic agent
         final TrafficManager trafficManager = new TrafficManager();
 
         // Create JADE backend
         // Register all the agents according to the network, and create argument string for the snifferAgent
         final ArrayList nodeAgentsList = new ArrayList(nodeAgents.length);
         final StringBuilder builder = new StringBuilder();
         for (int i = 0; i < nodes.length; i++) {
             final NodeAgent nodeAgent = new NodeAgent(i, nodeAgents, loadManager, timeManager);
             nodeAgents[i] = nodeAgent;
             final String name = "agent" + i;
             if  (builder.length() > 0){
                 builder.append(";");
             }
             builder.append(name);
             nodeAgentsList.add(new Agent(name));
             container.acceptNewAgent(name, nodeAgent).start();
         }
 
         // SnifferAgent creating
         if (options.showUi()) {
             final AgentController sniffer =
                    container.createNewAgent("sniffer", "jade.tools.sniffer.Sniffer", new Object[]{builder.toString()});
             sniffer.start();
         }
         try {
             for (int i=0;i<options.getExperimentCount();i++){
                 timeManager.printToWriter("Starting experiment #" + i);
                 emulate(options, container, network, nodes, nodeAgents, loadManager, timeManager, trafficManager);
             }
             timeManager.printAllStatistics();
 
         } finally {
             writer.close();
         }
         // Nasty hack to shut down
         System.exit(0);
     }
 
     private static void emulate(final Options options,
                                 final AgentContainer container,
                                 final Network network,
                                 final Node[] nodes,
                                 final NodeAgent[] nodeAgents,
                                 final LoadManager loadManager,
                                 final TimeLogManager timeManager,
                                 final TrafficManager trafficManager) throws StaleProxyException, InterruptedException {
         // Generate random system load and traffic
         final LoadManager.Load load = LoadManager.generate(options.getTimeLimit(), network.edges);
         final List<TrafficManager.TrafficEvent> traffic = TrafficManager.generate(nodes.length, options.getMessagesCount(), options.getTimeLimit());
         trafficManager.setTraffic(traffic);
         loadManager.setLoad(load);
 
         // Create neuro routing manager
         final RoutingManager neuroRoutingManager = new NeuroRoutingManager(network, loadManager, timeManager, options.getMessagesCount());
         NodeAgent.routingManager = neuroRoutingManager;
         timeManager.resetStatistics(options.getMessagesCount());
         // Start traffic agent
         container.acceptNewAgent("NeuroTrafficAgent", new TrafficAgent(nodeAgents, trafficManager, timeManager)).start();
 
         // Spin lock while all the messages are not processed
         while (!neuroRoutingManager.areAllMessagesReceived()){
             System.out.println("Waiting for routing finished.\n" +
                     "Messages left: " + neuroRoutingManager.leftMessages());
             Thread.sleep(1000);
         }
         timeManager.log("Routing successfully finished");
         timeManager.printStatistics();
         timeManager.saveNeuroStatistics();
 
         // Create OSPF routing manager
         final RoutingManager deikstraRoutingManager = new DeikstraRoutingManager(network, loadManager, timeManager, options.getMessagesCount());
         NodeAgent.routingManager = deikstraRoutingManager;
         timeManager.resetStatistics(options.getMessagesCount());
         // Start traffic agent
         trafficManager.reset();
         container.acceptNewAgent("DeikstraTrafficAgent", new TrafficAgent(nodeAgents, trafficManager, timeManager)).start();
         // Spin lock while all the messages are not processed
         while (!deikstraRoutingManager.areAllMessagesReceived()){
             System.out.println("Waiting for routing finished.\n" +
                     "Messages left: " + deikstraRoutingManager.leftMessages());
 			Thread.sleep(1000);
         }
         timeManager.log("Routing successfully finished");
         timeManager.printStatistics();
         timeManager.saveDeikstraStatistics();
     }
 }
