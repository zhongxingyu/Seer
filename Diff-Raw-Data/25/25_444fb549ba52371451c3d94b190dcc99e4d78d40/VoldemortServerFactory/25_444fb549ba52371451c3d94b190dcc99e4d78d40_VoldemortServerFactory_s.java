 package com.page5of4.codon.persistence.voldemort.extender;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Pattern;
 
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedServiceFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import voldemort.serialization.SerializerDefinition;
 
 import com.page5of4.codon.persistence.voldemort.extender.Dictionaries.ExtractedMap;
 import com.page5of4.nagini.VoldemortCluster;
 import com.page5of4.nagini.VoldemortClusterBuilder;
 
 public class VoldemortServerFactory implements ManagedServiceFactory {
    private static final Logger logger = LoggerFactory.getLogger(VoldemortServerFactory.class);
    private final Map<String, VoldemortCluster> clusters = new ConcurrentHashMap<String, VoldemortCluster>();
    private final StoreClientFactory storeClientFactory;
 
    public Map<String, VoldemortCluster> getClusters() {
       return Collections.unmodifiableMap(clusters);
    }
 
    public VoldemortServerFactory(StoreClientFactory storeClientFactory) {
       this.storeClientFactory = storeClientFactory;
    }
 
    @Override
    public String getName() {
       return "com.page5of4.codon.persistence.voldemort.server.Config";
    }
 
    @Override
    public void updated(String pid, Dictionary p) throws ConfigurationException {
       deleted(pid);
 
       logger.info("Configuring '{}'", pid);
 
       VoldemortClusterBuilder builder = VoldemortClusterBuilder.make().numberOfNodes(2);
       Pattern storePropertyPattern = Pattern.compile("store.([^\\.]+).(\\S+)");
      Map<Object, Object> config = Dictionaries.asMap(p);
       Collection<ExtractedMap> storeConfigs = Dictionaries.extractPrefixedMaps(p, storePropertyPattern);
       for(ExtractedMap storeConfig : storeConfigs) {
          String keySerializer = (String)storeConfig.getMap().get("serializer.keys");
          String valueSerializer = (String)storeConfig.getMap().get("serializer.keys");
          String valueSchema = (String)storeConfig.getMap().get("schema");
          builder.withStore(storeConfig.getKey(), new SerializerDefinition(keySerializer), new SerializerDefinition(valueSerializer, valueSchema));
          logger.info("Store: {}", storeConfig.getKey());
       }
 
       VoldemortCluster cluster = builder.start();
       clusters.put(pid, cluster);
 
       p.put("bootstrap.url", cluster.getBootstrapUrl());
       storeClientFactory.updated(pid, p);
    }
 
    @Override
    public void deleted(String pid) {
       VoldemortCluster cluster = clusters.remove(pid);
       if(cluster != null) {
          logger.info("Stopping {} {}", pid, cluster);
          cluster.stop();
          storeClientFactory.deleted(pid);
       }
    }
 }
