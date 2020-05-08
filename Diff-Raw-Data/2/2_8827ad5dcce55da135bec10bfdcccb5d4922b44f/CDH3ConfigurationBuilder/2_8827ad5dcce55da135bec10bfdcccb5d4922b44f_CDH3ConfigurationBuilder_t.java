 package com.ngdata;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jclouds.compute.domain.NodeMetadata;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.ngdata.exception.JajcException;
 
 public class CDH3ConfigurationBuilder {
 	
 	private static StringBuilder sb;
 
 	public static String createConfiguration(Iterable<NodeMetadata> nodes, IConfig config) throws JajcException {
 		
 		sb = new StringBuilder();
 		sb.append("class cdh3::environment {");
 		
 		String jobtracker = Iterables.filter(nodes, new Predicate<NodeMetadata>() {
 			@Override
 			public boolean apply(NodeMetadata nm) {
				return nm.getTags().contains("cdh3::hadoop::jobtracker");
 			}
 		}).iterator().next().getHostname();
 		
 		StringBuilder namenode = new StringBuilder("hdfs://"); 
 		namenode.append(		
 				Iterables.filter(nodes, new Predicate<NodeMetadata>() {
 					@Override
 					public boolean apply(NodeMetadata nm) {
 						return nm.getTags().contains("cdh3::hadoop::namenode::service") 
 									|| nm.getTags().contains("cdh3::hadoop::namenode::postinstall")
 									|| nm.getTags().contains("cdh3::hadoop::namenode");
 					} 
 				}).iterator().next().getHostname());
 		namenode.append(":8020");
 		
 		//zookeeper quorum array maken
 		StringBuilder zk_quorum = new StringBuilder(" [ ");
 		Iterable<NodeMetadata> zknodes = Iterables.filter(nodes, new Predicate<NodeMetadata>() {
 			@Override
 			public boolean apply(NodeMetadata nm) {
 				return nm.getTags().contains("cdh3::zookeeper");
 				
 			}});
 		for (NodeMetadata zknode : zknodes) {
 			zk_quorum.append(" \"" + zknode.getHostname() + "\" ," );
 		}
 		zk_quorum.deleteCharAt(zk_quorum.length()-1);
 		zk_quorum.append(" ]");
 		
 		
 		sb.append("\n$namenode = \"").append(namenode).append("\"");
 		sb.append("\n$zk_quorum = ").append(zk_quorum);
 		
 		
 		Map<String,Map<String,String>> userConfig = config.getUserConfig();
 		if (userConfig == null)
 			userConfig = new HashMap<String, Map<String,String>>();
 		createNoOverwriteProperty(userConfig, "hdfs", new HashMap<String, String>());
 		createNoOverwriteProperty(userConfig, "core", new HashMap<String, String>());
 		createNoOverwriteProperty(userConfig, "mapred", new HashMap<String, String>());
 		createNoOverwriteProperty(userConfig, "zookeeper", new HashMap<String, String>());
 		createNoOverwriteProperty(userConfig, "hbase", new HashMap<String, String>());
 		
 		for (String s : userConfig.keySet() ) {
 			Map<String,String> properties = userConfig.get(s);
 			if ("hdfs".equals(s)){ 
 				createNoOverwriteProperty(properties, "dfs.name.dir", "/data/nn");
 				createNoOverwriteProperty(properties, "dfs.data.dir", "/data/dn");
 			}
 			else if ("mapred".equals(s)) {
 				createOrOverwriteProperty(properties, "mapred.job.tracker", jobtracker + ":9001");
 				createNoOverwriteProperty(properties, "mapred.local.dir", "/data/mapred/local");
 				createNoOverwriteProperty(properties, "mapred.system.dir", "/mapred/system");
 			}
 			else if ("core".equals(s)) {
 				createOrOverwriteProperty(properties, "fs.default.name", "${namenode}" );
 			}
 			else if ("zookeeper".equals(s)){
 				createNoOverwriteProperty(properties, "tickTime", "2000");
 				createNoOverwriteProperty(properties, "dataDir", "/var/zookeeper");
 				createNoOverwriteProperty(properties, "clientPort", "2181");
 				createNoOverwriteProperty(properties, "initLimit", "5");
 				createNoOverwriteProperty(properties, "syncLimit", "2");
 				createNoOverwriteProperty(properties, "servers", zk_quorum.toString());
 			}
 			else if ("hbase".equals(s)) {
 				createOrOverwriteProperty(properties, "hbase.cluster.distributed", "true");
 				createOrOverwriteProperty(properties, "hbase.rootdir", "${namenode}/hbase");
 				createOrOverwriteProperty(properties, "hbase.zookeeper.quorum", zk_quorum.toString());
 			}
 			createHash(s, properties);
 			
 		}
 		
 		sb.append("\n}");
 		return sb.toString();
 			
 	}
 
 	private static void createHash(String name,Map<String,String> properties) {
 		sb.append("\n\n#").append(name);
 		sb.append("\n$").append(name).append(" = {");
 
 		for (String key : properties.keySet()) {
 			String value = properties.get(key);
 			
 			//don't stringify arrays 
 			if (value.indexOf('[') == -1)
 				sb.append("\n  \"").append(key).append("\"  =>  \"").append(properties.get(key)).append("\" ,");
 			else
 				sb.append("\n  \"").append(key).append("\"  =>  ").append(properties.get(key)).append(" ,");
 		}
 		sb.deleteCharAt(sb.length() - 1);
 		sb.append("\n}");
 			
 	}
 	
 	//properties of which we know the value should overwrite user-defined settings
 	private static <T> void createOrOverwriteProperty(Map<String,T> properties, String name, T value) {
 		properties.put(name, value);
 	}
 	
 	//only use default if the user didn't set the property
 	private static <T> void createNoOverwriteProperty(Map<String,T> properties, String name, T value) {
 		if (properties.get(name) == null)
 			properties.put(name, value);
 			
 	}
 }
