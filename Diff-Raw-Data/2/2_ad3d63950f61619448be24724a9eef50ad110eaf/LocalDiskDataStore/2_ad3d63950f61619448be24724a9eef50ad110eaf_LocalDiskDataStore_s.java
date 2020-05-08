 package de.tuberlin.dima.presslufthammer.data.columnar.local;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Maps;
 
 import de.tuberlin.dima.presslufthammer.data.SchemaNode;
 import de.tuberlin.dima.presslufthammer.data.columnar.DataStore;
 import de.tuberlin.dima.presslufthammer.data.columnar.Tablet;
 
 /**
  * {@link DataStore} implementation that stores the tablets (tablet data) in
  * files on a local disk. Objects of this class cannot be directly instantiated
  * and one should use the {@code openDataStore} and {@code createDataStore}
  * methods.
  * 
  * @author Aljoscha Krettek
  * 
  */
 public class LocalDiskDataStore implements DataStore {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private File rootDirectory;
     private Map<TabletKey, LocalDiskTablet> tablets;
 
     /**
      * Constructs the data store and sets the storage directory. Only used
      * internally.
      */
     private LocalDiskDataStore(File rootDirectory) {
         this.rootDirectory = rootDirectory;
         tablets = Maps.newHashMap();
        log.info("Opening OnDiskDataStore from " + rootDirectory + ".");
     }
 
     /**
      * Returns an {@link LocalDiskDataStore} that uses the given directory for
      * storing/loading tablets.
      */
     public static LocalDiskDataStore openDataStore(File directory)
             throws IOException {
         if (!directory.exists() || !directory.isDirectory()) {
             throw new IOException(
                     "Directory given in openDataStore does not exist or is not a directory.");
         }
         LocalDiskDataStore store = new LocalDiskDataStore(directory);
         store.openTablets();
 
         return store;
     }
 
     /**
      * Creates a new empty {@link LocalDiskDataStore} in the given directory and
      * returns it.
      */
     public static LocalDiskDataStore createDataStore(File directory)
             throws IOException {
         if (directory.exists()) {
             throw new RuntimeException("Directory for tablet already exists: "
                     + directory);
         }
         LocalDiskDataStore store = new LocalDiskDataStore(directory);
         directory.mkdirs();
 
         return store;
     }
 
     /**
      * Deletes the specified data store, ie. deletes the directory.
      */
     public static void removeDataStore(File directory) throws IOException {
         if (directory.exists()) {
             FileUtils.deleteDirectory(directory);
         }
     }
 
     /**
      * Internal method that scans the datastore directory and fills the internal
      * map of tablets from the tablet directories.
      */
     private void openTablets() throws IOException {
         log.debug("Reading tablets from {}.", rootDirectory);
         for (File tabletDir : rootDirectory.listFiles()) {
             if (!tabletDir.isDirectory()) {
                 continue;
             }
             String dirname = tabletDir.getName();
             String[] splitted = dirname.split("\\.");
             int partitionNum = Integer.parseInt(splitted[1]);
             LocalDiskTablet tablet = LocalDiskTablet.openTablet(tabletDir);
             tablets.put(new TabletKey(tablet.getSchema().getName(),
                     partitionNum), tablet);
             log.debug("Opened tablet " + tablet.getSchema().getName() + ":"
                     + partitionNum);
         }
     }
 
     /**
      * Calls {@code flush} on all the managed tablets.
      */
     public void flush() throws IOException {
         for (TabletKey key : tablets.keySet()) {
             LocalDiskTablet tablet = tablets.get(key);
             log.info("Flushing data of tablet " + tablet.getSchema().getName()
                     + "." + key.getPartition());
             tablet.flush();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean hasTablet(String tableName, int partition) {
         TabletKey key = new TabletKey(tableName, partition);
         return tablets.containsKey(key);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Tablet createOrGetTablet(SchemaNode schema, int partition)
             throws IOException {
         TabletKey key = new TabletKey(schema.getName(), partition);
         if (tablets.containsKey(key)) {
             return tablets.get(key);
         } else {
             File tablePath = new File(rootDirectory, schema.getName() + "."
                     + partition);
             log.info("Creating new tablet " + schema.getName() + ":"
                     + partition);
             LocalDiskTablet newTablet = LocalDiskTablet.createTablet(schema,
                     tablePath);
             tablets.put(key, newTablet);
             return newTablet;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Tablet getTablet(String tableName, int partition) throws IOException {
         TabletKey key = new TabletKey(tableName, partition);
         if (tablets.containsKey(key)) {
             return tablets.get(key);
         } else {
             return null;
         }
     }
 }
 
 /**
  * The key that is used in the internal map of tablets in
  * {@link LocalDiskDataStore}.
  * 
  * @author Aljoscha Krettek
  * 
  */
 class TabletKey {
     private String tableName;
     private int partition;
 
     public TabletKey(String tableName, int partition) {
         this.tableName = tableName;
         this.partition = partition;
     }
 
     @Override
     public boolean equals(Object other) {
         if (!(other instanceof TabletKey)) {
             return false;
         }
         TabletKey oKey = (TabletKey) other;
         if (!tableName.equals(oKey.tableName)) {
             return false;
         }
         if (!(partition == oKey.partition)) {
             return false;
         }
         return true;
     }
 
     public String getTableName() {
         return tableName;
     }
 
     public int getPartition() {
         return partition;
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(tableName, partition);
     }
 }
