 /**
  * $$\\ToureNPlaner\\$$
  */
 package server;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 
 import algorithms.AlgorithmFactory;
 import algorithms.DummyFactory;
 import algorithms.GraphAlgorithmFactory;
 import computecore.AlgorithmRegistry;
 import computecore.ComputeCore;
 
 /**
  * ToureNPlaner Event Based Server
  * 
  * @author Niklas Schnelle
  * @version 0.1 Prototype
  * 
  *  Initially based on: 
  * 	http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/package-summary.html
  */
 public class HttpServer {
 	
 	
 	public static Map<String, Object> getServerInfo(AlgorithmRegistry reg){
 		Map<String, Object> info = new HashMap<String,Object>(4);
 		info.put("version", new Float(0.1));
 		info.put("servertype", "public");
 		info.put("port", new Integer(8080));
 		// Enumerate Algorithms
		Enumeration<AlgorithmFactory> algs = reg.getAlgorithms();
		AlgorithmFactory alg;
 		Map<String, Object> algInfo;
 		List<Map<String, Object>> algList= new ArrayList<Map<String,Object>>();
		while(algs.hasMoreElements()){
			alg = algs.nextElement();
 			algInfo = new HashMap<String, Object>(5);
 			algInfo.put("version", alg.getVersion());
 			algInfo.put("name", alg.getAlgName());
 			algInfo.put("urlsuffix", alg.getURLSuffix());
 			if(alg instanceof GraphAlgorithmFactory){
 				algInfo.put("pointconstraints", ((GraphAlgorithmFactory)alg).getPointConstraints());
 				algInfo.put("constraints", ((GraphAlgorithmFactory)alg).getConstraints());
 			}
 			algList.add(algInfo);
 		}
 		info.put("algorithms", algList);
 		
 		return info;
 	}
 	
     public static void main(String[] args) {
         // Configure the server.
         ServerBootstrap bootstrap = new ServerBootstrap(
                 new NioServerSocketChannelFactory( // Change to Oio* if you want OIO
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool()));
 
         ServerBootstrap infoBootstrap = new ServerBootstrap(
                 new NioServerSocketChannelFactory( // Change to Oio* if you want OIO
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool()));
         
         // Register Algorithms
         AlgorithmRegistry reg = new AlgorithmRegistry();
         reg.registerAlgorithm(new DummyFactory());
         
         // Create our ComputeCore that manages all ComputeThreads
         ComputeCore comCore = new ComputeCore(reg, 16, 32);
         
         // Create ServerInfo object
         Map<String, Object> serverInfo = getServerInfo(reg);
         
         // Set up the event pipeline factory.
         bootstrap.setPipelineFactory(new ServerPipelineFactory(comCore, false, serverInfo));
         infoBootstrap.setPipelineFactory(new ServerInfoOnlyPipelineFactory(reg));
         
         // Bind and start to accept incoming connections.
         bootstrap.bind(new InetSocketAddress(8081));
         infoBootstrap.bind(new InetSocketAddress(8080));
     }
 }
