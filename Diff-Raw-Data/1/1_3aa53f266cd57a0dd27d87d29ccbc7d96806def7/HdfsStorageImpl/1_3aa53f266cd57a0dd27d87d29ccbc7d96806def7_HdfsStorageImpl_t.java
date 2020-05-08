 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package zkndb.storage;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.yarn.conf.YarnConfiguration;
 import zkndb.benchmark.BenchmarkUtils;
 import zkndb.metrics.Metric;
 import zkndb.metrics.ThroughputMetricImpl;
 
 /**
  *
  * @author 4knahs
  */
 public class HdfsStorageImpl extends Storage {
 
     private static final String HDFS_URI = "hdfsstorageimpl.hdfsuri";
     private static final String HDFS_URI_DEFAULT = "hdfs://localhost:9000/";
     private static final String HDFS_ROOT_DIR_NAME = "hdfsstorageimpl.rootdirname";
     private static final String HDFS_ROOT_DIR_DEFAULT = "zkndb";
    
     private String _hdfsUri;
     private String _rootDirName;
     private FileSystem fs = null;
     private Path _fsWorkingPath;
     private Path _fsRootDirPath;
     private long _uuid;
     private int _blockSize = 53;
     private byte[] _block = new byte[_blockSize];
 
     @Override
     public void init() {
         _sharedData = BenchmarkUtils.sharedData;
         _requestRate = BenchmarkUtils.requestRate;
 
         System.out.println("Storage " + _id + " establishing connection");
 
         InputStream inStream = null;
         FileInputStream fileInStream = null;
 
         try {
             fileInStream = new FileInputStream("zkndb-hdfsstorageimpl.properties");
             inStream = fileInStream;
         } catch (FileNotFoundException ex) {
             //if the file not found, load from the default properties file 
             //inside jar file
             inStream = ClassLoader.getSystemResourceAsStream(
                     "zkndb/storage/zkndb-hdfsstorageimpl.properties");
         }
 
         Properties props = new Properties();
 
         try {
             props.load(inStream);
 
             _hdfsUri = props.getProperty(HdfsStorageImpl.HDFS_URI, 
                     HdfsStorageImpl.HDFS_URI_DEFAULT);
             _rootDirName = props.getProperty(HdfsStorageImpl.HDFS_ROOT_DIR_NAME, 
                     HdfsStorageImpl.HDFS_ROOT_DIR_DEFAULT);
 
         } catch (IOException ex) {
             Logger.getLogger(HdfsStorageImpl.class.getName()).log(Level.SEVERE, null, ex);
             System.out.println("Program can't continue because initialization "
                     + "from properties file fails :(");
             return;
         }
 
         Configuration conf = new YarnConfiguration();
         conf.set(YarnConfiguration.FS_RM_STATE_STORE_URI, _hdfsUri);
 
         _fsWorkingPath = new Path(_hdfsUri);
         _fsRootDirPath = new Path(_fsWorkingPath, _rootDirName);
         
         try {
             // create filesystem
             fs = _fsWorkingPath.getFileSystem(conf);
             fs.mkdirs(_fsRootDirPath);
         } catch (IOException ex) {
             System.err.println("IO Exception: " + ex.getLocalizedMessage());
         }
 
     }
 
     @Override
     public void write() {
         Metric metric = _sharedData.get(_id);
         long new_uuid = _uuid;
 
         //synchronized (metric) {
             ((ThroughputMetricImpl) metric).incrementRequests();
             try {
                 //do write to datastore
                 new_uuid = UUID.randomUUID().getLeastSignificantBits();
                 Path nodeCreatePath = getNodePath(String.valueOf(new_uuid));
 
                 try {
                     writeFile(nodeCreatePath, _block);
                     _uuid = new_uuid;
                 } catch (Exception e) {
                     throw e;
                 }
 
 
                 ((ThroughputMetricImpl) metric).incrementAcks();
             } catch (Exception e) {
                 //Sent request but it could not be served.
                 //Should catch only specific exception.
                 System.err.println("IO Exception: " + e.getLocalizedMessage());
             }
         //}
     }
 
     @Override
     public void read() {
         ((ThroughputMetricImpl) _sharedData.get(_id)).incrementRequests();
         try {
             //Do read to datastore
             Path nodeCreatePath = getNodePath(String.valueOf(_uuid));
             readFile(nodeCreatePath, _blockSize);
             ((ThroughputMetricImpl) _sharedData.get(_id)).incrementAcks();
         } catch (Exception e) {
             //Sent request but it could not be served.
             //Should catch only specific exception.
         }
     }
 
     private void deleteFile(Path deletePath) throws Exception {
         if (!fs.delete(deletePath, true)) {
             throw new Exception("Failed to delete " + deletePath);
         }
     }
 
     private byte[] readFile(Path inputPath, long len) throws Exception {
         FSDataInputStream fsIn = fs.open(inputPath);
         // state data will not be that "long"
         byte[] data = new byte[(int) len];
         fsIn.readFully(data);
        fsIn.close();
         return data;
     }
 
     private void writeFile(Path outputPath, byte[] data) throws Exception {
         FSDataOutputStream fsOut = fs.create(outputPath, false);
         fsOut.write(data);
         fsOut.flush();
         fsOut.close();
     }
 
     private Path getNodePath(String nodeName) {
         return new Path(_fsRootDirPath, nodeName);
     }
 }
