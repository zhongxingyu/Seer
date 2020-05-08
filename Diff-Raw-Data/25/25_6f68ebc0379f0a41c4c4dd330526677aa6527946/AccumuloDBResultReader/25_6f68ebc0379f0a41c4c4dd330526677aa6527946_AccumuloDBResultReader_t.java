 package cellmate.accumulo.reader;
 
 
 import cellmate.cell.CellGroup;
 import cellmate.reader.CellTransformer;
 import cellmate.reader.DBResultReader;
 import cellmate.reader.ReadParameters;
 import cellmate.reader.BasicCellGroupingDBResultReader;
 import com.google.common.annotations.VisibleForTesting;
 import org.apache.accumulo.core.client.*;
 import org.apache.accumulo.core.data.Key;
 import org.apache.accumulo.core.data.Range;
 import org.apache.accumulo.core.data.Value;
 import org.apache.accumulo.core.security.Authorizations;
 import org.apache.hadoop.io.Text;
 import org.apache.log4j.Logger;
 
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.regex.Pattern;
 
 /**
  * User: bfemiano
  * Date: 9/4/12
  * Time: 11:21 PM
  */
 public class AccumuloDBResultReader<C>
         implements DBResultReader<Map.Entry<Key,Value>,C> {
 
 
     private Instance instance;
     private String instanceName;
     private String zookeepers;
     private static final Logger log = Logger.getLogger(AccumuloDBResultReader.class);
     private static final Pattern colon = Pattern.compile("[:]");
     private DBResultReader<Map.Entry<Key,Value>, C> baseReader;
 
 
     @VisibleForTesting
     public AccumuloDBResultReader(Instance instance){
         this.instance = instance;
         baseReader = new BasicCellGroupingDBResultReader<Map.Entry<Key, Value>, C>();
     }
 
     public AccumuloDBResultReader(AccumuloReadParameters parameters){
         try {
             String instanceName = parameters.getInstanceName();
             String zookeepers = parameters.getZookeepers();
             this.instance = new ZooKeeperInstance(instanceName, zookeepers);
             this.baseReader = new BasicCellGroupingDBResultReader<Map.Entry<Key, Value>, C>();
         } catch (NoSuchElementException e){
             throw new IllegalArgumentException("missing zookeepers and/or instance id");
         }
     }
 
     public AccumuloDBResultReader(DBResultReader<Map.Entry<Key,Value>, C> baseReader, AccumuloReadParameters parameters) {
         this(parameters);
         this.baseReader = baseReader;
     }
 
     public AccumuloDBResultReader(DBResultReader<Map.Entry<Key,Value>, C> baseReader, String instanceName, String zookeepers){
         this.baseReader = baseReader;
         instance = new ZooKeeperInstance(instanceName, zookeepers);
     }
 
     public AccumuloDBResultReader(String instanceName, String zookeepers){
         instance = new ZooKeeperInstance(instanceName, zookeepers);
         baseReader = new BasicCellGroupingDBResultReader<Map.Entry<Key, Value>, C>();
     }
 
 
 
    private Authorizations getAuthsFromConnector(Connector connector) {
         try {
             return connector.securityOperations().getUserAuthorizations(connector.whoami());
         } catch (AccumuloException e) {
             throw new RuntimeException("General Accumulo error getting auths for current user: " + connector.whoami(),e);
         } catch (AccumuloSecurityException e) {
             throw new RuntimeException("Security error getting auths for current user: " + connector.whoami(),e);
         }
     }
 
     public List<CellGroup<C>> read(ReadParameters params, CellTransformer<Map.Entry<Key, Value>, C> transformer) {
         if(!(params instanceof AccumuloReadParameters)){
             throw new IllegalArgumentException("ReadParameter implementation must be " +
                     AccumuloReadParameters.class.getName() + " to use this reader class " +
                     " instead found " + params.getClass().getName());
         }
         AccumuloReadParameters parameters =  (AccumuloReadParameters)params;
         Connector connector = getConnectorFromParameters(parameters);
        Authorizations auths = getAuthsFromConnector(connector);
         try {
             Scanner scan = connector.createScanner(parameters.getTableName(), auths);
             scan = addRange(scan, parameters);
             scan = addColFamsAndQuals(scan, parameters);
             scan = setBatchSize(scan, parameters);
             scan = attachIterators(scan, parameters);
             return read(scan, parameters, transformer);
         } catch (NoSuchElementException e){
             throw new IllegalArgumentException("Missing table name in parameters");
         } catch (TableNotFoundException e) {
             throw new IllegalArgumentException("Table not found during read: " + parameters.getTableName(),e);
         }
     }
 
     private Scanner attachIterators(Scanner scan, AccumuloReadParameters parameters) {
        for(IteratorSetting iterator : parameters.getIterators())
             scan.addScanIterator(iterator);
         return scan;
     }
 
     public List<CellGroup<C>> read(Iterable<Map.Entry<Key, Value>> dbItems,
                                    ReadParameters parameters,
                                    CellTransformer<Map.Entry<Key, Value>, C> transformer) {
         return baseReader.read(dbItems, parameters, transformer);
     }
 
     private Scanner addColFamsAndQuals(Scanner scan, AccumuloReadParameters parameters) {
         String[] colfamsAndQuals = parameters.getColumns();
         for(String pair : colfamsAndQuals) {
             String[] colFamAndQual = colon.split(pair);
             if(colFamAndQual.length == 1){
                 scan.fetchColumnFamily(new Text(colFamAndQual[0]));
             } else if(colFamAndQual.length == 2){
                 scan.fetchColumn(new Text(colFamAndQual[0]), new Text(colFamAndQual[1]));
             } else {
                 throw new IllegalArgumentException("malformed colfam entry: " + pair);
             }
         }
         if(colfamsAndQuals.length == 0 && log.isDebugEnabled())
             log.debug("no columns specified in parameters");
         return scan;
     }
 
     private Scanner setBatchSize(Scanner scan, AccumuloReadParameters parameters) {
         try {
             int batchSize = parameters.getBatchSize();
             scan.setBatchSize(batchSize);
         } catch (NoSuchElementException e){
             if(log.isInfoEnabled())
                 log.info(" Using default batch size.");
         }
         return scan;
     }
 
 
     private Scanner addRange(Scanner scan, AccumuloReadParameters parameters) {
         String startKey;
         String endKey;
         try {
             startKey = parameters.getStartKey();
         } catch (NoSuchElementException e){
             startKey = null;
         }
 
         try {
             endKey = parameters.getEndKey();
         } catch (NoSuchElementException e){
             endKey = null;
         }
 
         scan.setRange(new Range(startKey, endKey));
         return scan;
     }
 
 
    private Connector getConnectorFromParameters(AccumuloReadParameters parameters) {
         Connector connector;
         try {
             String user = parameters.getUser();
             String pass = parameters.getPassword();
             connector = instance.getConnector(user, pass);
         } catch (NoSuchElementException e){
             throw new IllegalArgumentException("missing user/pass");
         } catch (AccumuloSecurityException e) {
             throw new RuntimeException("Security error trying to establish populate connector",e);
         } catch (AccumuloException e) {
             throw new RuntimeException("General Accumulo error while setting up populate connector",e);
         }
         return connector;
     }
 }
