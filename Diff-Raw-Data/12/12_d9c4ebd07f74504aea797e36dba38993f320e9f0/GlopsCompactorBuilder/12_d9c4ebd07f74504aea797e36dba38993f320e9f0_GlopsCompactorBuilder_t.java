 package com.moviepilot.sheldon.compactor.producer.glops;
 
 import com.moviepilot.sheldon.compactor.Compactor;
 import com.moviepilot.sheldon.compactor.CompactorBuilder;
 import com.moviepilot.sheldon.compactor.config.Config;
 import com.moviepilot.sheldon.compactor.event.EdgeEvent;
 import com.moviepilot.sheldon.compactor.event.NodeEvent;
 import com.moviepilot.sheldon.compactor.producer.PropertyContainerEventProducer;
 import gnu.trove.map.TObjectLongMap;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 
 /**
  * @author stefanp
  * @since 08.08.12
  */
 public class GlopsCompactorBuilder implements CompactorBuilder {
     private final EmbeddedGraphDatabase sourceDb;
 
     public GlopsCompactorBuilder(final EmbeddedGraphDatabase sourceDb) {
         this.sourceDb = sourceDb;
     }
 
     public Compactor build(final Config config) {
 
         // Map that tells the compactor how often to print progress information
         final TObjectLongMap<String> modMap = config.getModMap();
 
         final long dotNodes = config.getDotNodes();
         modMap.put("node_read", dotNodes);
         modMap.put("node_write", dotNodes);
        modMap.put("node_write_index", dotNodes);
         modMap.put("node_create", dotNodes);
         modMap.put("node_update", dotNodes);
         modMap.put("node_delete", dotNodes);
 
         final long dotEdges = config.getDotEdges();
         modMap.put("edge_read", dotEdges);
         modMap.put("edge_write", dotEdges);
        modMap.put("edge_write_index", dotEdges);
         modMap.put("edge_create", dotEdges);
         modMap.put("edge_update", dotEdges);
         modMap.put("edge_delete", dotEdges);
 
         modMap.put("clean_ok", config.getDotOk());
 
         // Iterators for input
         final PropertyContainerEventProducer<NodeEvent> nodeProducer = new GlopsNodeEventProducer(config, sourceDb);
         final PropertyContainerEventProducer<EdgeEvent> edgeProducer = new GlopsEdgeEventProducer(config, sourceDb);
 
         return new Compactor(config, nodeProducer, edgeProducer);
     }
 }
