 package com.moviepilot.sheldon.compactor.main;
 
 import com.beust.jcommander.IStringConverter;
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.ParameterException;
 import com.moviepilot.sheldon.compactor.config.Config;
 import com.moviepilot.sheldon.compactor.config.Defaults;
 import com.moviepilot.sheldon.compactor.handler.*;
 import com.moviepilot.sheldon.compactor.producer.glops.GlopsCompactorBuilder;
 import com.moviepilot.sheldon.compactor.util.Progressor;
import com.sun.servicetag.SystemEnvironment;
 import gnu.trove.map.TObjectLongMap;
 import gnu.trove.map.hash.THashMap;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.neo4j.tooling.GlobalGraphOperations;
 import org.neo4j.unsafe.batchinsert.BatchInserter;
 import org.neo4j.unsafe.batchinsert.BatchInserters;
 import org.neo4j.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
 import org.yaml.snakeyaml.Yaml;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import static com.moviepilot.sheldon.compactor.handler.PropertyContainerEventHandler.*;
 import static com.moviepilot.sheldon.compactor.handler.PropertyContainerEventHandler.Kind.*;
 
 /**
  * Plain runner for the compactor
  *
  * @author stefanp
  * @since 03.08.12
  */
 public final class Main {
 
     public enum Mode {
         // plain good old neo4j global iterators
         GLOPS;
 
         /* Ideas:
 
          RAW    - Use store file directly
          ONLINE - Use online backup
          PAR    - Use max id to chunk id space for parallel processing
 
          */
 
         public abstract static class Runner {
             public final Mode mode;
             protected final Config config;
 
             protected Runner(final Mode mode, final Config config) {
                 this.mode   = mode;
                 this.config = config;
             }
 
             public abstract int run();
         }
     }
 
     @Parameter(names = "--mode", description = "Operation mode (supported: GLOPS)",
             converter = ModeConverter.class)
     private Mode mode = Mode.GLOPS;
 
     @Parameter(names = "--source", description = "Source store directory (must exist)", required = true,
             converter = FileConverter.class)
     private File sourceStoreDir = null;
 
     @Parameter(names = "--target", description = "Target store directory (must not exist)", required = true,
             converter = FileConverter.class)
     private File targetStoreDir = null;
 
     @Parameter(names = "--config", description = "Defaults .properties or .yaml file path",
             converter = PropsConverter.class)
     private Map<String, String> props = new THashMap<String, String>(1);
 
 
     @Parameter(names = "--node-indexer", description = "Node indexer class name",
             converter = NodeIndexerConverter.class)
     private NodeIndexer optNodeIndexer = null;
 
     @Parameter(names = "--node-handler", description = "Node handler class name",
             converter = NodeEventHandlerConverter.class)
     private NodeEventHandler optNodeEventHandler = null;
 
     @Parameter(names = "--edge-handler", description = "Edge handler class name",
             converter = EdgeEventHandlerConverter.class)
     private EdgeEventHandler optEdgeEventHandler = null;
 
     @Parameter(names = "--edge-indexer", description = "Edge indexer class name",
             converter = EdgeIndexerConverter.class)
     private EdgeIndexer optEdgeIndexer = null;
 
     @Parameter(names = "--ring-size", hidden = true,
             description = "log2 of size of disruptor ring ")
     private int ringSize = Defaults.RING_SIZE;
 
     @Parameter(names = "--index-flush-min-count", hidden = true,
             description = "log2 of minimal number of event before index flush")
     private int indexFlushMinInterval = Defaults.INDEX_FLUSH_MIN_INTERVAL;
 
     @Parameter(names = "--index-flush-max-count", hidden = true,
             description = "log2 of maximal number of event before index flush")
     private int indexFlushMaxInterval = Defaults.INDEX_FLUSH_MAX_INTERVAL;
 
     @Parameter(names = "--num-index-entries", hidden = true)
     private int numIndexEntries = Defaults.DEFAULT_NUM_INDEX_ENTRIES;
 
     @Parameter(names = "--num-index-props", hidden = true)
     private int numIndexProps = Defaults.DEFAULT_NUM_INDEX_PROPS;
 
     @Parameter(names = "--num-props", hidden = true)
     private int numProps = Defaults.DEFAULT_NUM_PROPS;
 
     @Parameter(names = "--dot-nodes", hidden = true)
     private int dotNodes = Defaults.DOT_NODES;
 
     @Parameter(names = "--dot-edges", hidden = true)
     private int dotEdges = Defaults.DOT_EDGES;
 
     @Parameter(names = "--dot-ok", hidden = true)
     private int dotOk = Defaults.DOT_OK;
 
     @SuppressWarnings("FieldCanBeLocal")
     @Parameter(names = "--help", description = "Print usage information and exit", help = true)
     private boolean help = false;
 
     public static void main(final String[] args) {
         if (args.length == 0) {
             System.err.println("Run with --help to get a list of required and supported parameters");
             System.exit(1);
         }
         final Main main             = new Main();
         final JCommander jCommander = new JCommander(main, args);
 
         if (main.help) {
             final StringBuilder builder = new StringBuilder();
             jCommander.usage(builder);
             System.err.print(builder.toString());
             System.exit(2);
         }
 
         System.exit(main.run());
     }
 
     public int run() {
         verifyState();
 
         // setup targetStoreDir database *and* lucene index
         final BatchInserter targetDb                = BatchInserters.inserter(targetStoreDir.getAbsolutePath(), props);
         final LuceneBatchInserterIndexProvider prov = new LuceneBatchInserterIndexProvider(targetDb);
         final Config config                         = makeCompactorConfig(targetDb, prov);
 
         try {
             // build compactor according to mode
             switch(mode) {
                 case GLOPS: return new GlopsModeRunner(config).run();
                 default: throw new IllegalArgumentException("Unknown mode");
             }
         }
         finally {
             // shutdown both: index *and* db
             prov.shutdown();
             targetDb.shutdown();
         }
     }
 
     private void verifyState() {
         // TODO Replace with proper JCommander validators
 
         if (!sourceStoreDir.exists())
             throw new IllegalArgumentException("Source Database does not exist " + sourceStoreDir);
 
         if (targetStoreDir.exists())
             throw new IllegalArgumentException("Target Directory already exists " + targetStoreDir);
 
         assert(indexFlushMinInterval >= 0);
         assert(indexFlushMinInterval <= indexFlushMaxInterval);
         assert(indexFlushMaxInterval <= ringSize);
 
         // batch inserter wont come up if this is true
         props.put("allow_store_upgrade", "false");
     }
 
     private Config makeCompactorConfig(final BatchInserter targetDb,
                                        final LuceneBatchInserterIndexProvider indexProv) {
         final TObjectLongMap<String> modMap = Progressor.makeCountMap();
 
         return new Config() {
             public int getRingSize() {
                 return 1 << ringSize;
             }
 
             public int getIndexFlushMinInterval() {
                 return 1 << indexFlushMinInterval;
             }
 
             public int getIndexFlushMaxInterval() {
                 return 1 << indexFlushMaxInterval;
             }
 
             public BatchInserter getTargetDatabase() {
                 return targetDb;
             }
 
             public LuceneBatchInserterIndexProvider getTargetIndexProvider() {
                 return indexProv;
             }
 
             public TObjectLongMap<String> getModMap() {
                 return modMap;
             }
 
             public NodeEventHandler getOptNodeHandler() {
                 return optNodeEventHandler;
             }
 
             public EdgeEventHandler getOptEdgeHandler() {
                 return optEdgeEventHandler;
             }
 
             public NodeIndexer getOptNodeIndexer() {
                 return optNodeIndexer;
             }
 
             public EdgeIndexer getOptEdgeIndexer() {
                 return optEdgeIndexer;
             }
 
             public int getNumExtraNodeThreads() {
                 return (optNodeEventHandler == null ? 0 : 1) + (optNodeIndexer == null ? 0 : 2);
             }
 
             public int getNumExtraEdgeThreads() {
                 return (optEdgeEventHandler == null ? 0 : 1) + (optEdgeIndexer == null ? 0 : 2);
             }
 
             public int getNumIndexEntries() {
                 return numIndexEntries;
             }
 
             public int getNumIndexProps() {
                 return numIndexProps;
             }
 
             public int getNumProps() {
                 return numProps;
             }
 
             public long getDotNodes() {
                 return dotNodes;
             }
 
             public long getDotEdges() {
                 return dotEdges;
             }
 
             public long getDotOk() {
                 return dotOk;
             }
 
             public long getDotKind(final Kind kind) {
                 switch (kind) {
                     case NODE: return getDotNodes();
                     case EDGE: return getDotEdges();
                     default: return getDotOk();
                 }
             }
         };
     }
 
     final public class GlopsModeRunner extends Mode.Runner {
 
         public GlopsModeRunner(final Config config) {
             super(Mode.GLOPS, config);
         }
 
         public int run() {
             // Load sourceStoreDir database
             final EmbeddedGraphDatabase sourceDb = new EmbeddedGraphDatabase(sourceStoreDir.getAbsolutePath(), props);
             try {
                 // Build separate compactors to re-load the target db between stages
                 warmUp(sourceDb, Kind.NODE);
                 new GlopsCompactorBuilder(sourceDb).build(config).copy(Kind.NODE);
                 warmUp(sourceDb, Kind.EDGE);
                 new GlopsCompactorBuilder(sourceDb).build(config).copy(Kind.EDGE);
             }
             finally {
                 sourceDb.shutdown();
             }
             return 0;
         }
 
         @SuppressWarnings("WhileLoopReplaceableByForEach")
         private void warmUp(final EmbeddedGraphDatabase sourceDb, final Kind kind) {
             final Iterable<? extends PropertyContainer> iterable;
 
             switch (kind) {
                 case NODE: iterable = GlobalGraphOperations.at(sourceDb).getAllNodes(); break;
                 case EDGE: iterable = GlobalGraphOperations.at(sourceDb).getAllRelationships(); break;
                 default:
                     throw new IllegalArgumentException("Unsupported kind");
             }
 
             final Iterator<? extends PropertyContainer> iterator = iterable.iterator();
             while (iterator.hasNext()) {
                 try {
                     iterator.next();
                 }
                 catch (RuntimeException e) {
                     System.err.println("Encountered broken record during warmup: " + e);
                 }
             }
         }
     }
 
     final public static class ModeConverter implements IStringConverter<Mode> {
 
         public Mode convert(String value) {
             return Mode.valueOf(value.trim().toUpperCase());
         }
     }
 
     final public static class FileConverter implements IStringConverter<File> {
         public File convert(final String value) {
             return new File(value);
         }
     }
 
     final public static class PropsConverter implements IStringConverter<Map<String, String>> {
 
         @SuppressWarnings("unchecked")
         public Map<String, String> convert(final String value) {
             try {
                 final FileReader fileReader = new FileReader(value);
                 if (value.endsWith(".yaml") || value.endsWith(".yml")) {
                     return toStringStringMap(new Yaml().loadAs(fileReader, Map.class));
 
                 }
                 if (value.endsWith(".properties") || value.endsWith(".props")) {
                     final Properties props = new Properties();
                     props.load(fileReader);
                     return toStringStringMap(props);
                 }
                 throw new IllegalArgumentException("Unknown file action");
             }
             catch (IOException e) {
                 throw new ParameterException(e);
             }
 
         }
 
         private Map<String, String> toStringStringMap(Map<Object, Object> aMap) {
             final Map<String, String> result = new HashMap<String, String>();
             for (final Map.Entry<Object, Object> entry : aMap.entrySet()) {
                 final String key = entry.getKey().toString();
                 final String val = entry.getValue().toString();
                 result.put(key, val);
                 System.out.println(key + " = " + val);
             }
             return result;
         }
     }
 
     private static class PropertyContainerEventHandlerConverter<H extends PropertyContainerEventHandler<?>>
             implements IStringConverter<H> {
 
         private final Class<H> eventHandlerClazz;
 
         PropertyContainerEventHandlerConverter(Class<H> eventHandlerClazz) {
             this.eventHandlerClazz = eventHandlerClazz;
         }
 
         @Override
         public H convert(String arg) {
             try {
                 if (arg.length() == 0)
                     throw new IllegalArgumentException();
                 final String clazzName = arg.startsWith(".") ? ("com.moviepilot.sheldon.compactor.custom" + arg) : arg;
                 final Class<?> clazz   = Class.forName(clazzName);
                 if (eventHandlerClazz.isAssignableFrom(clazz)) {
                     return eventHandlerClazz.cast(clazz.newInstance());
                 }
                 throw new IllegalArgumentException(arg + " is not instantiable as a  "
                         + eventHandlerClazz.getCanonicalName());
             } catch (ClassNotFoundException e) {
                 throw new ParameterException(e);
             } catch (InstantiationException e) {
                 throw new ParameterException(e);
             } catch (IllegalAccessException e) {
                 throw new ParameterException(e);
             }
         }
     }
 
     final public static class NodeEventHandlerConverter
             extends PropertyContainerEventHandlerConverter<NodeEventHandler> {
 
         public NodeEventHandlerConverter() {
             super(NodeEventHandler.class);
         }
     }
 
     final public static class EdgeEventHandlerConverter
             extends PropertyContainerEventHandlerConverter<EdgeEventHandler> {
 
         public EdgeEventHandlerConverter() {
             super(EdgeEventHandler.class);
         }
     }
 
     final public static class NodeIndexerConverter
             extends PropertyContainerEventHandlerConverter<NodeIndexer> {
 
         public NodeIndexerConverter() {
             super(NodeIndexer.class);
         }
     }
 
     final public static class EdgeIndexerConverter
             extends PropertyContainerEventHandlerConverter<EdgeIndexer> {
 
         public EdgeIndexerConverter() {
             super(EdgeIndexer.class);
         }
     }
 }
