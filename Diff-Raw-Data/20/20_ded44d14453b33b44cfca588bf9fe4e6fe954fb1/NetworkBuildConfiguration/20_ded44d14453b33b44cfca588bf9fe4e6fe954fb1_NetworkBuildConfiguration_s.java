 package sk.sochuliak.barabasi.network;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class NetworkBuildConfiguration {
 	
 	public static final int DEGREE_DRIVEN = 0;
 	public static final int CLUSTER_DRIVEN = 1;
 	public static final int RANDOM_DRIVEN = 2;
 	
	private Network network = null;
 	
 	private String name = "";
 	
 	private int numberOfNodes = 0;
 	
 	private int firstEdgeConnecting = 0;
 	
 	private List<EdgeConnectingMethodRowConfig> edgeConnectingMethodRowConfigs = null;
 	
	public static NetworkBuildConfiguration getInstance() {
		return new NetworkBuildConfiguration();
 	}
 	
 	public static NetworkBuildConfiguration createDefaultConfig(int method, String name, int numberOfNodes, int numberOfEdges) {
 		NetworkBuildConfiguration config = new NetworkBuildConfiguration();
 		config.setName(name);
 		config.setNetwork(new MapNetwork(name));
 		config.setNumberOfNodes(numberOfNodes);
 		config.setFirstEdgeConnecting(method);
 		
 		EdgeConnectingMethodRowConfig rowConfig = new EdgeConnectingMethodRowConfig();
 		if (method == EdgeConnectingMethodRowConfig.DRIVEN_DEGREE) {
 			rowConfig.setConnectingMethod(EdgeConnectingMethodRowConfig.DRIVEN_DEGREE);
 			rowConfig.setRange(EdgeConnectingMethodRowConfig.RANGE_ALL);
 			rowConfig.setNumberOfEdges(numberOfEdges-1);
 		} else {
 			rowConfig.setConnectingMethod(EdgeConnectingMethodRowConfig.DRIVEN_RANDOM);
 			rowConfig.setRange(EdgeConnectingMethodRowConfig.RANGE_NEIGHBOR);
 			rowConfig.setNumberOfEdges(numberOfEdges-1);
 		}
 		
 		List<EdgeConnectingMethodRowConfig> rowsConfig = new ArrayList<EdgeConnectingMethodRowConfig>();
 		rowsConfig.add(rowConfig);
 		
 		config.setEdgeConnectingMethodRowConfigs(rowsConfig);
 		return config;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public NetworkBuildConfiguration setName(String name) {
 		this.name = name;
 		return this;
 	}
 
 	public int getNumberOfNodes() {
 		return numberOfNodes;
 	}
 
 	public NetworkBuildConfiguration setNumberOfNodes(int numberOfNodes) {
 		this.numberOfNodes = numberOfNodes;
 		return this;
 	}
 
 	public int getFirstEdgeConnecting() {
 		return firstEdgeConnecting;
 	}
 
 	public NetworkBuildConfiguration setFirstEdgeConnecting(int firstEdgeConnecting) {
 		this.firstEdgeConnecting = firstEdgeConnecting;
 		return this;
 	}
 
 	public List<EdgeConnectingMethodRowConfig> getEdgeConnectingMethodRowConfigs() {
 		return edgeConnectingMethodRowConfigs;
 	}
 
 	public NetworkBuildConfiguration setEdgeConnectingMethodRowConfigs(
 			List<EdgeConnectingMethodRowConfig> edgeConnectingMethodRowConfigs) {
 		this.edgeConnectingMethodRowConfigs = edgeConnectingMethodRowConfigs;
 		return this;
 	}
 
 	public Network getNetwork() {
 		return network;
 	}
 
 	public NetworkBuildConfiguration setNetwork(Network network) {
 		this.network = network;
 		return this;
 	}
 
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		sb.append(this.getClass().getName()).append("\n");
 		sb.append("\t").append("Name: ").append(this.getName()).append("\n");
 		sb.append("\t").append("Number of nodes: ").append(this.getNumberOfNodes()).append("\n");
 		sb.append("\t").append("First edge connecting: ").append(this.getFirstEdgeConnecting()).append("\n");
 		sb.append("\t").append("Other edges connecting").append("\n");
 		for (EdgeConnectingMethodRowConfig rowConfig : this.getEdgeConnectingMethodRowConfigs()) {
 			sb.append("\t\t").append(rowConfig).append("\n");
 		}
 		return sb.toString();
 	}
 }
