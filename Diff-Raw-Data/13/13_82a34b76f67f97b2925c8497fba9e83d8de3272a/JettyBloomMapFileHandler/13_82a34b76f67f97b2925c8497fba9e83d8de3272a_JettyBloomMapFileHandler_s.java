 package net.joshdevins.hadoop.utils.io.http;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Set;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.joshdevins.hadoop.utils.Pair;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BloomMapFile;
 import org.apache.hadoop.io.BloomMapFileReader;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.IOUtils;
 import org.apache.hadoop.io.Text;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.util.log.Log;
 
 import com.google.common.collect.MapEvictionListener;
 import com.google.common.collect.MapMaker;
 
 /**
  * A {@link Handler} for embedded Jetty to serve files out of {@link BloomMapFile}s. This currently assumes no
  * sub-driectories will ever be accessed since it splits the request URL into two parts: {dataset path}/{file name}.
  * This implementation is pretty simple in that it will just iterate over all the bloom filters for that dataset
  * and test for the file. Not efficient, but simple.
  * 
  * <h2>Why?</h2>
  * <p>
  * The basic premise is as follows. Serving small files (like images) out of HDFS is pretty much a no-go given the
  * amount of overhead involved in just storing and managing the little files. We first encountered this when building an
  * in-house map-tile server to serve tiles built in Hadoop. To work around this problem, we store a whole bunch of files
  * in multiple {@link BloomMapFile}s (generally a {@link BloomMapFile} per reducer). This server will then do lookups in
  * the backing {@link BloomMapFile}. The file that is returned from the server will have a MIME type based on the file
  * extension in the {@link BloomMapFile}'s key.
  * </p>
  * 
  * <h2>Caching</h2>
  * <p>
  * Internally this relies on a couple of caching mechanisms. First we store all of the {@link BloomMapFile} readers in a
  * cache on first access to a dataset. They are expunged from the cache on demand through a "DELETE" HTTP request on the
  * dataset URL or after 24 hours of not being accessed. Within the readers themselves there are two levels of access.
  * The first is the bloom filter and the second is the index into the {@link BloomMapFile}. Any complete misses on a
  * dataset will also be cached alongside the readers so as to avoid checking all the readers again for a known
  * non-existent key/value.
  * </p>
  * 
  * TODO: Add refreshing readers based on modification times of underlying {@link BloomMapFile}s.
  * TODO: Add logging.
  * 
  * @author Josh Devins
  */
 public class JettyBloomMapFileHandler extends AbstractJettyHdfsFileHandler {
 
     private static class DataSet {
 
         private final Set<BloomMapFileReader> readers;
 
         private final Set<String> notFoundFiles;
 
         public DataSet(final Set<BloomMapFileReader> readers) {
             this.readers = readers;
             notFoundFiles = new HashSet<String>();
         }
 
         public boolean addNotFoundFile(final String filename) {
             return notFoundFiles.add(filename);
         }
 
         /**
          * Closes readers, general cleanup.
          */
         public void cleanup() {
 
             for (BloomMapFileReader reader : readers) {
                 IOUtils.closeStream(reader);
             }
 
             readers.clear();
             notFoundFiles.clear();
         }
 
         public Set<BloomMapFileReader> getReaders() {
             return readers;
         }
 
         public boolean isKnownNotFoundFile(final String filename) {
             return notFoundFiles.contains(filename);
         }
     }
 
     private final ConcurrentMap<String, DataSet> datasetMap;
 
     public JettyBloomMapFileHandler(final String rootPathInFileSystem) throws IOException {
         super(rootPathInFileSystem);
 
         MapEvictionListener<String, DataSet> mapEvictionListener = new MapEvictionListener<String, DataSet>() {
 
             @Override
             public void onEviction(final String key, final DataSet value) {
                value.cleanup();
             }
         };
 
        // build an entry expiring (based on access) ConcurrentHashMap with soft referenced values
        // this will enable garbage collection to sweep away values at will
         // this will also do some pre-emptive cleaning if a dataset has not been used recently
        datasetMap = new MapMaker().softValues().expireAfterAccess(1, TimeUnit.DAYS)
                .evictionListener(mapEvictionListener).makeMap();
     }
 
     @Override
     protected void doStop() throws Exception {
         super.doStop();
 
         // close any open readers
         Collection<DataSet> datasets = datasetMap.values();
         for (DataSet dataset : datasets) {
             dataset.cleanup();
         }
     }
 
     @Override
     protected void handleWithExceptionTranslation(final String target, final Request baseRequest,
             final HttpServletRequest request, final HttpServletResponse response) {
 
         // check HTTP method
         String httpMethod = request.getMethod().toUpperCase(Locale.UK);
 
         if ("DELETE".equals(httpMethod)) {
             handleDelete(target, baseRequest, request, response);
 
         } else if ("GET".equals(httpMethod)) {
             handleGet(target, baseRequest, request, response);
 
         } else {
             throw new HttpErrorException(HttpServletResponse.SC_NOT_ACCEPTABLE, "HTTP method not supported: "
                     + request.getMethod());
         }
 
         ((Request) request).setHandled(true);
     }
 
     Pair<String, String> splitTargetIntoDatasetAndFilename(final String target) {
 
         // break the request URI into two parts: dataset path, filename in map file
         int splitAt = StringUtils.lastIndexOf(target, '/');
 
         // ensure split is possible
         if (splitAt < 1 || splitAt == target.length() - 1) {
             return null;
         }
 
         String dataset = target.substring(0, splitAt);
         String filename = target.substring(splitAt + 1);
 
         return new Pair<String, String>(dataset, filename);
     }
 
     private Set<BloomMapFileReader> getReadersForDataset(final String dataset) {
 
         Set<BloomMapFileReader> readers = new HashSet<BloomMapFileReader>();
 
         // verify dataset
         Path datasetPath = new Path(getRootPathInFileSystem() + dataset);
         try {
             if (!getFileSystem().exists(datasetPath) || !getFileSystem().getFileStatus(datasetPath).isDir()) {
 
                 throw new HttpErrorException(HttpServletResponse.SC_NOT_FOUND, "Dataset directory does not exist: "
                         + dataset);
             }
 
         } catch (IOException ioe) {
             throw new HttpErrorException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     "Could not open dataset directory: " + dataset, ioe);
         }
 
         // get files in dir
         FileStatus[] files;
         try {
             files = getFileSystem().listStatus(datasetPath);
 
         } catch (IOException ioe) {
             throw new HttpErrorException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     "Could not list map files in dataset: " + dataset, ioe);
         }
 
         for (FileStatus fileStatus : files) {
 
             // skip any files that are not BloomMapFile directories
             if (!isBloomMapFile(fileStatus)) {
 
                 Log.warn("Skipping non-BloomMapFile found in dataset: dataset=" + dataset + "file="
                         + fileStatus.getPath().getName(), this);
                 continue;
             }
 
             Path path = fileStatus.getPath();
             try {
                 readers.add(new BloomMapFileReader(getFileSystem(), path.toString(), getConfiguration()));
 
             } catch (IOException ioe) {
                 throw new HttpErrorException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                         "Could not create reader for map file: " + path.toString(), ioe);
             }
         }
 
         return readers;
     }
 
     private void handleDelete(final String target, final Request baseRequest, final HttpServletRequest request,
             final HttpServletResponse response) {
 
         // full target is the dataset
         if (!datasetMap.containsKey(target)) {
             throw new HttpErrorException(HttpServletResponse.SC_NOT_FOUND, "Dataset not found: " + target);
         }
 
         // remove and cleanup
         datasetMap.remove(target).cleanup();
 
         response.setStatus(HttpServletResponse.SC_OK);
     }
 
     private void handleGet(final String target, final Request baseRequest, final HttpServletRequest request,
             final HttpServletResponse response) {
 
         // split the target URL into two parts
         Pair<String, String> splitTarget = splitTargetIntoDatasetAndFilename(target);
         if (splitTarget == null) {
             throw new HttpErrorException(HttpServletResponse.SC_NOT_ACCEPTABLE,
                     "Error splitting target into dataset and filename: " + target);
         }
 
         String datasetName = splitTarget.getA();
         String filename = splitTarget.getB();
         String datasetFilenameDebugString = "dataset=" + datasetName + " filename=" + filename;
 
         // get the readers for this dataset
         DataSet dataset = datasetMap.get(datasetName);
 
         // need to get the readers
         if (dataset == null) {
             dataset = new DataSet(getReadersForDataset(datasetName));
 
             // only need to set it if it still doesn't exist (race conditions, needs fixing?)
             datasetMap.putIfAbsent(datasetName, dataset);
 
         } else {
 
             // check immediately for a known miss
             if (dataset.isKnownNotFoundFile(filename)) {
                 throw new HttpErrorException(HttpServletResponse.SC_NOT_FOUND,
                         "File was not found in any backing mapfile (cached 404): " + datasetFilenameDebugString);
             }
         }
 
         // have the readers, find the file
         BytesWritable value = new BytesWritable();
         Text key = new Text(filename);
         boolean found = false;
 
         for (BloomMapFileReader reader : dataset.getReaders()) {
 
             // try to get from the mapfile, internally this hits the bloom filter first
             try {
                 if (reader.get(key, value) != null) {
                     found = true;
                     break;
                 }
             } catch (IOException ioe) {
                 throw new HttpErrorException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                         "Error looking for filename key in mapfile reader: " + datasetFilenameDebugString, ioe);
             }
         }
 
         // not found? need this variable since value is already non-null
         if (!found) {
             dataset.addNotFoundFile(filename);
             throw new HttpErrorException(HttpServletResponse.SC_NOT_FOUND,
                     "File was not found in any backing mapfile: " + datasetFilenameDebugString);
         }
 
         // get the bytes out of the value, trimmed padding
         byte[] bytes = new byte[value.getLength()];
         System.arraycopy(value.getBytes(), 0, bytes, 0, value.getLength());
 
         // send response with hopefully right content/mime type
         response.setContentType(getMimeType(filename));
         response.setStatus(HttpServletResponse.SC_OK);
         try {
             response.getOutputStream().write(bytes);
             response.getOutputStream().flush();
 
         } catch (IOException ioe) {
             throw new HttpErrorException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     "Error writing file bytes to output stream", ioe);
         }
     }
 
     /**
      * Determine if a directory is actually a {@link BloomMapFile}. This is based on the existence of exactly three
      * files named: bloom, index, data
      */
     private boolean isBloomMapFile(final FileStatus fileStatus) {
 
         if (!fileStatus.isDir()) {
             return false;
         }
 
         Path path = fileStatus.getPath();
         try {
             FileStatus[] files = getFileSystem().listStatus(path);
             Set<String> fileNames = new HashSet<String>(files.length);
 
             for (FileStatus file : files) {
 
                 if (file.isDir()) {
                     return false;
                 }
 
                 fileNames.add(file.getPath().getName());
             }
 
             return fileNames.size() == 3 && fileNames.contains("bloom") && fileNames.contains("index")
                     && fileNames.contains("data");
 
         } catch (IOException ioe) {
             throw new HttpErrorException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     "Error listing files in subdirectory of dataset", ioe);
         }
     }
 }
