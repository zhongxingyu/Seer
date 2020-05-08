 package de.tuberlin.dima.presslufthammer.data.ondisk;
 
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
 
 public class OnDiskDataStore implements DataStore {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private File rootDirectory;
     private Map<TabletKey, OnDiskTablet> tablets;
 
     public OnDiskDataStore(File rootDirectory) {
         this.rootDirectory = rootDirectory;
         tablets = Maps.newHashMap();
         log.info("Opening OnDiskDataStore from " + rootDirectory + ".");
     }
 
     public static OnDiskDataStore openDataStore(File directory)
             throws IOException {
        if (!directory.exists() && directory.isDirectory()) {
             throw new IOException(
                     "Directory given in openDataStore does not exist or is not a directory.");
         }
         OnDiskDataStore store = new OnDiskDataStore(directory);
         store.openTablets();
 
         return store;
     }
 
     public static void removeDataStore(File directory) throws IOException {
         if (directory.exists()) {
             FileUtils.deleteDirectory(directory);
         }
     }
 
     public static OnDiskDataStore createDataStore(File directory)
             throws IOException {
         if (directory.exists()) {
             throw new RuntimeException("Directory for tablet already exists: "
                     + directory);
         }
         OnDiskDataStore store = new OnDiskDataStore(directory);
         directory.mkdirs();
 
         return store;
     }
 
     private void openTablets() throws IOException {
         log.debug("Reading tablets from {}.", rootDirectory);
         for (File tabletDir : rootDirectory.listFiles()) {
             if (!tabletDir.isDirectory()) {
                 continue;
             }
             String dirname = tabletDir.getName();
             String[] splitted = dirname.split("\\.");
             int partitionNum = Integer.parseInt(splitted[1]);
             OnDiskTablet tablet = OnDiskTablet.openTablet(tabletDir);
             tablets.put(new TabletKey(tablet.getSchema().getName(), partitionNum), tablet);
             log.debug("Opened tablet " + tablet.getSchema().getName() + ":"
                     + partitionNum);
         }
     }
 
     public void flush() throws IOException {
         for (OnDiskTablet tablet : tablets.values()) {
             log.info("Flushing data of tablet " + tablet.getSchema().getName()
                     + ".");
             tablet.flush();
         }
     }
 
     @Override
     public boolean hasTablet(String tableName, int partition) {
         TabletKey key = new TabletKey(tableName, partition);
         return tablets.containsKey(key);
     }
 
     @Override
     public Tablet createOrGetTablet(SchemaNode schema, int partition) throws IOException {
         TabletKey key = new TabletKey(schema.getName(), partition);
         if (tablets.containsKey(key)) {
             return tablets.get(key);
         } else {
             File tablePath = new File(rootDirectory, schema.getName() + "."
                     + partition);
             log.info("Creating new tablet " + schema.getName() + ":" + partition);
             OnDiskTablet newTablet = OnDiskTablet.createTablet(schema,
                     tablePath);
             tablets.put(key, newTablet);
             return newTablet;
         }
     }
 
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
 
     @Override
     public int hashCode() {
         return Objects.hashCode(tableName, partition);
     }
 }
