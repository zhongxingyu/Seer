 /*
     Copyright 2011 Anton S. Kraievoy akraievoy@gmail.com
 
     This file is part of org.akraievoy:couch.
 
     org.akraievoy:couch is free software: you can redistribute it and/or modify
     it under the terms of the GNU Lesser General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     org.akraievoy:couch is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public License
     along with org.akraievoy:couch. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.akraievoy.couch;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.MapMaker;
 import com.google.common.io.*;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.*;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.TimeUnit;
 
 /**
  * CouchDB entity persistence.
  *
  * Please note that for optimal performance you have to override
  * default CouchDB configuration.
  *
  * <pre><code>[httpd]
  * socket_options = [{nodelay, true}]</code></pre>
  *
  * http://stackoverflow.com/questions/8992122/couchdb-mochiweb-negative-effect-of-persistent-connections
  */
 @SuppressWarnings("UnusedDeclaration")
 public class CouchDao {
     private final ObjectMapper mapper = new ObjectMapper();
     {
         mapper.getSerializationConfig().enable(SerializationConfig.Feature.INDENT_OUTPUT);
     }
 
     protected String couchUrl = "http://localhost:5984/";
     public void setCouchUrl(String couchUrl) {
         if (!couchUrl.endsWith("/")) {
             throw new IllegalArgumentException("couchUrl '" + couchUrl + "' must end with '/'");
         }
         this.couchUrl = couchUrl;
     }
 
     protected String dbName = "elw-data";
     public void setDbName(String dbName) { this.dbName = dbName; }
 
     protected String username = "supercow";
     public void setPassword(String password) { this.password = password; }
 
     protected String password = "typical";
     public void setUsername(String username) { this.username = username; }
 
     protected int concurrencyLevel = 3;
     public void setConcurrencyLevel(int concurrencyLevel) {
         this.concurrencyLevel = concurrencyLevel;
     }
 
     protected long cacheValidityMinutes = 1;
     public void setCacheValidityMinutes(long cacheValidityMinutes) {
         this.cacheValidityMinutes = cacheValidityMinutes;
     }
 
     private ConcurrentMap<Squab.Path, List<Squab.Path>> cachePaths;
     private ConcurrentMap<Squab.Path, List<List<String>>> cacheAxes;
     private ConcurrentMap<Squab.Path, List<? extends Squab>> cacheSquabs;
     private ConcurrentMap<Squab.Path, SortedMap<Long, ? extends Squab.Stamped>> cacheStamped;
 
     private static final SortedMap<Long, ? extends Squab.Stamped> EMPTY_MAP =
             Collections.unmodifiableSortedMap(new TreeMap<Long, Squab.Stamped>());
 
     @Deprecated
     public CouchDao() {
         this(true);
     }
 
     public CouchDao(boolean autoStart) {
         if (autoStart) {
             start();
         }
     }
 
     public void start() {
         final MapMaker caches =
             new MapMaker()
                 .concurrencyLevel(concurrencyLevel)
                 .expireAfterWrite(cacheValidityMinutes, TimeUnit.MINUTES);
 
         cachePaths = caches.makeMap();
         cacheAxes = caches.makeMap();
         cacheSquabs = caches.makeMap();
         cacheStamped = caches.makeMap();
     }
 
     protected void invalidate(
             final Squab.Path path
     ) {
         invalidate_(path, cacheAxes.keySet());
         invalidate_(path, cachePaths.keySet());
         invalidate_(path, cacheSquabs.keySet());
         invalidate_(path, cacheStamped.keySet());
     }
 
     protected void invalidate_(
             final Squab.Path path,
             final Set<Squab.Path> paths
     ) {
         for (
                 Iterator<Squab.Path> iterator = paths.iterator();
                 iterator.hasNext();
         ) {
             if (iterator.next().intersects(path)) {
                 iterator.remove();
             }
         }
     }
 
     //  Squab
     public static class UpdateStatus {
         public final String rev;
         public final Long stamp;
 
         public UpdateStatus(String rev, Long stamp) {
             this.rev = rev;
             this.stamp = stamp;
         }
     }
 
     public UpdateStatus createOrUpdate(
             final Squab squab
     ) {
         return createOrUpdate(squab, true);
     }
     
     public UpdateStatus createOrUpdate(
             final Squab squab,
             final boolean updateStamp
     ) {
         try {
             Long stamp = null;
             if (squab instanceof Squab.Stamped) {
                 final Squab.Stamped stamped = (Squab.Stamped) squab;
                 if (!updateStamp && stamped.getStamp() != null) {
                     stamp = stamped.getStamp();
                 } else {
                     stamp = stamped.updateStamp();
                 }
             }
             final String couchRev = couchPut(
                     squab.getCouchPath().id(),
                     squab
             );
             return new UpdateStatus(couchRev, stamp);
         } finally {
             invalidate(squab.getCouchPath());
         }
     }
 
     @Deprecated
     public Long update(
             final Squab squab
     ) {
         return update(squab, true);
     }
 
     @Deprecated
     public Long update(
             final Squab squab,
             final boolean updateStamp
     ) {
         Long stamp = null;
         if (squab instanceof Squab.Stamped) {
             final Squab.Stamped stamped = (Squab.Stamped) squab;
             if (!updateStamp && stamped.getStamp() != null) {
                 stamp = stamped.getStamp();
             } else {
                 stamp = stamped.updateStamp();
             }
         }
         couchPut(
                 squab.getCouchPath().id(),
                 squab
         );
         //  LATER we should do these operations in finally block
         invalidate(squab.getCouchPath());
         return stamp;
     }
 
     /**
      * Send attachments out of band of the main document update/create request
      *     http://wiki.apache.org/couchdb/HTTP_Document_API#Standalone_Attachments
      * More efficient than encoding byte stream within document JSON.
      *
      * @param squab to attach the stream to (only id/rev fields are used)
      * @param fileName attachment fileName
      * @param contentType content type of the attachment
      * @param streamBytes containing the attachment bytes
      *
      * @return new revision of the main document
      */
     public String attachStream(
             final Squab squab,
             final String fileName,
             final String contentType,
             final InputSupplier<InputStream> streamBytes
     ) {
         final Squab.Path couchPath = squab.getCouchPath();
 
         try {
             final String newCouchRev = couchFilePut(
                     couchPath.id(),
                     squab.getCouchRev(),
                     fileName,
                     contentType,
                     streamBytes
             );
 
             return newCouchRev;
         } finally {
             invalidate(couchPath);
         }
     }
 
     public <S extends Squab> S findOne(
             final Class<S> squabClass, 
             final String... path
     ) {
         final List<S> all = findAll(squabClass, path);
         if (all.isEmpty()) {
             throw new IllegalStateException(
                     "no records: " + squabClass.getSimpleName() + " '" + Arrays.toString(path) + "'"
             );
         }
         return all.get(0);
     }
 
     protected <S extends Squab> S findOne(
             final Class<S> squabClass, 
             final Squab.Path fullPath
     ) {
         final List<S> all = findAll(squabClass, fullPath);
         if (all.isEmpty()) {
             throw new IllegalStateException(
                     "no records: '" + fullPath + "'"
             );
         }
         return all.get(0);
     }
 
     public InputSupplier<InputStream> file(
             final Squab squab,
             final String fileName
     ) {
         return couchFileGet(squab.getCouchPath(), fileName);
     }
 
     public byte[] fileBytes(
             final Squab squab,
             final String fileName
     ) throws IOException {
         return ByteStreams.toByteArray(
                 couchFileGet(squab.getCouchPath(), fileName)
         );
     }
 
     public List<String> fileLines(
             final Squab squab,
             final String fileName
     ) throws IOException {
         return CharStreams.readLines(CharStreams.newReaderSupplier(
                 couchFileGet(squab.getCouchPath(), fileName),
                 Charsets.UTF_8
         ));
     }
 
     public String fileText(
             final Squab squab,
             final String fileName
     ) throws IOException {
         return CharStreams.toString(CharStreams.newReaderSupplier(
                 couchFileGet(squab.getCouchPath(), fileName),
                 Charsets.UTF_8
         ));
     }
 
     public <S extends Squab> S findSome(
             final Class<S> squabClass,
             final String... path
     ) {
         final List<S> all = findAll(squabClass, path);
         return all.isEmpty() ? null : all.get(0);
     }
 
     public <S extends Squab> List<S> findAll(
             final Class<S> squabClass,
             final String... path
     ) {
         final Squab.Path fullPath = new Squab.Path(squabClass, path);
         return findAll(squabClass, fullPath);
     }
 
     protected <S extends Squab> List<S> findAll(
             final Class<S> squabClass,
             final Squab.Path fullPath
     ) {
         final List<? extends Squab> allCached = cacheSquabs.get(fullPath);
         if (allCached != null) {
             //noinspection unchecked
             return (List<S>) allCached;
         }
 
         final List<Squab.Path> allPaths = findAllPaths(fullPath);
         final ArrayList<S> all = new ArrayList<S>();
         for (Squab.Path aPath : allPaths) {
             all.add(couchGet(aPath, squabClass));
         }
 
         final List<S> allRO = Collections.unmodifiableList(all);
         cacheSquabs.put(fullPath, allRO);
 
         return allRO;
     }
 
     public <S extends Squab> List<List<String>> axes(
             final Class<S> squabClass, 
             final String... path
     ) {
         return axes(new Squab.Path(squabClass, path));
     }
 
     /**
      * Computes possible criteria positions, enumerating undefined ones.
      *
      * @param fullPath criteria to inspect
      * @return all possible values for each criteria path position
      */
     public List<List<String>> axes(
             final Squab.Path fullPath
     ) {
         final List<List<String>> cachedAxes = cacheAxes.get(fullPath);
         if (cachedAxes != null) {
             return cachedAxes;
         }
 
         final List<Squab.Path> paths = findAllPaths(fullPath);
 
         final SortedMap<Integer, TreeSet<String>> resultMap =
                 new TreeMap<Integer, TreeSet<String>>();
         for (Squab.Path matchPath : paths) {
             for (int i = 0; i < matchPath.len(); i++) {
                 final TreeSet<String> axis = resultMap.get(i);
                 if (axis == null) {
                     TreeSet<String> newAxis = new TreeSet<String>();
                     newAxis.add(matchPath.elem(i));
                     resultMap.put(i, newAxis);
                 } else {
                     axis.add(matchPath.elem(i));
                 }
             }
         }
 
         final List<List<String>> axes = new ArrayList<List<String>>(resultMap.size());
         for (int i = 0; i < resultMap.size(); i++) {
             axes.add(Collections.unmodifiableList(
                     new ArrayList<String>(resultMap.get(i))
             ));
         }
 
         final List<List<String>> axesRO = Collections.unmodifiableList(axes);
         cacheAxes.put(fullPath, axesRO);
 
         return axesRO;
     }
 
     public <S extends Squab> List<Squab.Path> findAllPaths(
             final Class<S> squabClass,
             final String... path
     ) {
         return findAllPaths(new Squab.Path(squabClass, path));
     }
 
     public List<Squab.Path> findAllPaths(
             final Squab.Path fullPath
     ) {
         final List<Squab.Path> cachedIds = cachePaths.get(fullPath);
         if (cachedIds != null) {
             return cachedIds;
         }
 
         final Squab.RespViewList viewList = couchList(fullPath);
         final List<Squab.Path> paths =
                 new ArrayList<Squab.Path>(viewList.getRows().size());
         for (Squab.RespViewList.Row row : viewList.getRows()) {
             final Squab.Path rowPath = Squab.Path.fromId(row.getId());
             if (rowPath.len() < fullPath.len()) {
                 continue;
             }
             boolean afterWild = false;
             boolean matches = true;
             for (int i = 0; i < fullPath.len(); i++) {
                 //  path may have some wilds in the middle, so
                 //      we have to filter even after couch range-query
                 afterWild |= i > 0 && fullPath.elem(i - 1) == null;
                 if (afterWild
                         && i < fullPath.len()
                         && fullPath.elem(i) != null
                         && !fullPath.elem(i).equals(rowPath.elem(i))
                 ) {
                     matches = false;
                     break;
                 }
             }
 
             if (matches) {
                 paths.add(rowPath);
             }
         }
 
         final List<Squab.Path> pathsRO = Collections.unmodifiableList(paths);
         cachePaths.put(fullPath, pathsRO);
 
         return pathsRO;
     }
 
     //  Squab.Stamped
     public <S extends Squab.Stamped> S findLast(
             final Class<S> squabClass,
             final String... path
     ) {
         final Squab.Path fullPath = new Squab.Path(squabClass, path);
         final List<Squab.Path> allPaths = findAllPaths(fullPath);
 
         Squab.Path lastPath = null;
         long lastStamp = 0;
         for (Squab.Path somePath : allPaths) {
             final long someStamp = Squab.Stamped.parse(somePath.getLast());
             if (lastPath == null || someStamp >= lastStamp) {
                 lastPath = somePath;
                 lastStamp = someStamp;
             }
         }
 
        return lastPath == null ? null : findOne(squabClass, fullPath);
     }
 
     public <S extends Squab.Stamped> S findByStamp(
             final long stamp,
             final Class<S> squabClass,
             final String... path
     ) {
         final Squab.Path fullPath = new Squab.Path(squabClass, path);
         final List<Squab.Path> allPaths = findAllPaths(fullPath);
 
         Squab.Path stampPath = null;
         for (Squab.Path somePath : allPaths) {
             final long someStamp = Squab.Stamped.parse(somePath.getLast());
             if (someStamp == stamp) {
                 stampPath = somePath;
                 break;
             }
         }
 
         return stampPath == null ? null : findOne(squabClass, fullPath);
     }
 
     public <S extends Squab.Stamped> SortedMap<Long, S> findAllStamped(
             final Long sinceTime,
             final Long untilTime,
             final Class<S> squabClass,
             final String... path
     ) {
         final SortedMap<Long, S> stampToSquab =
                 findAllStamped(squabClass, path);
         if (sinceTime != null && untilTime != null) {
             return stampToSquab.subMap(sinceTime, untilTime);
         }
         if (sinceTime != null) {
             return stampToSquab.tailMap(sinceTime);
         }
         if (untilTime != null) {
             return stampToSquab.headMap(untilTime);
         }
         return stampToSquab;
     }
 
     public <S extends Squab.Stamped> SortedMap<Long, S> findAllStamped(
             final Class<S> squabClass,
             final String... path
     ) {
         final Squab.Path fullPath = new Squab.Path(squabClass, path);
         final SortedMap<Long, ? extends Squab.Stamped> stampedCached =
                 cacheStamped.get(fullPath);
         if (stampedCached != null) {
             //noinspection unchecked
             return (SortedMap<Long, S>) stampedCached;
         }
 
         final List<S> squabs = findAll(squabClass, path);
         final SortedMap<Long, S> timeToSquab;
         if (squabs.isEmpty()) {
             //noinspection unchecked
             timeToSquab = (SortedMap<Long, S>) EMPTY_MAP;
         } else {
             timeToSquab = byStamp(squabs);
         }
 
         final SortedMap<Long, S> timeToSquabRO =
                 Collections.unmodifiableSortedMap(timeToSquab);
         cacheStamped.put(fullPath, timeToSquabRO);
 
         return timeToSquabRO;
     }
 
     protected static <S extends Squab.Stamped> SortedMap<Long, S> byStamp(
             final Collection<S> squabs
     ) {
         final SortedMap<Long, S> result = new TreeMap<Long, S>();
         for (S squab : squabs) {
             result.put(squab.getStamp(), squab);
         }
         return result;
     }
 
     //  Couch HTTP
     protected Squab.RespViewList couchList(Squab.Path path) {
         HttpURLConnection connection = null;
         try {
             URL url = new URL(couchUrl +
                     dbName + "/" + "_all_docs/?" +
                     "startkey=\"" + url(path.rangeMin()) + "\"&" +
                     "endkey=\"" + url(path.rangeMax()) + "\""
             );
             connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod("GET");
             connection.setUseCaches(false);
             connection.setDoInput(true);
             connection.setDoOutput(false);
 
             final InputStream is = connection.getInputStream();
             final Squab.RespViewList list =
                     mapper.readValue(is, Squab.RespViewList.class);
             is.close();
 
             return list;
         } catch (IOException e) {
             throw new IllegalStateException("list failed", e);
         } finally {
             if (connection != null) {
                 connection.disconnect();
             }
         }
     }
 
     private static String url(final String urlElem) {
         try {
             return URLEncoder.encode(urlElem, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new IllegalStateException("UTF-8 not supported?!");
         }
     }
 
     protected <S extends Squab> S couchGet(
             final Squab.Path path,
             final Class<S> squabClass
     ) {
         HttpURLConnection connection = null;
         try {
             URL url = new URL(couchUrl + dbName + "/" + url(path.id()));
             connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod("GET");
             connection.setUseCaches(true);
             connection.setDoInput(true);
             connection.setDoOutput(false);
 
             final BufferedReader reader =
                     new BufferedReader(new InputStreamReader(
                             connection.getInputStream(), Charsets.UTF_8
                     ));
             final S squab = mapper.readValue(reader, squabClass);
             reader.close();
 
             return squab;
         } catch (IOException e) {
             throw new IllegalStateException("GET failed", e);
         } finally {
             if (connection != null) {
                 connection.disconnect();
             }
         }
     }
 
     public InputSupplier<InputStream> couchFileGet(
             final Squab.Path path,
             final String fileName
     ) {
         try {
             final URL url = new URL(
                     couchUrl + dbName + "/" + url(path.id()) + "/" + url(fileName)
             );
 
             return new InputSupplier<InputStream>() {
                 public InputStream getInput() throws IOException {
                     final HttpURLConnection connection =
                             (HttpURLConnection) url.openConnection();
                     connection.setRequestMethod("GET");
                     connection.setUseCaches(true);
                     connection.setDoInput(true);
                     connection.setDoOutput(false);
 
                     return new FilterInputStream(connection.getInputStream()) {
                         @Override
                         public void close() throws IOException {
                             connection.disconnect();
                             super.close();
                         }
                     };
                 }
             };
         } catch (IOException e) {
             throw new IllegalStateException("failed to construct URL", e);
         }
     }
 
     protected String couchPut(
             final String couchId,
             final Squab squab
     ) {
         HttpURLConnection connection = null;
 
         try {
             final String squabJSON = mapper.writeValueAsString(squab);
 
             URL url = new URL(couchUrl + dbName + "/" + url(couchId));
             connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod("PUT");
             connection.setRequestProperty(
                     "Content-Type",
                     "application/json"
             );
             connection.setRequestProperty(
                     "Content-Length",
                     Integer.toString(squabJSON.getBytes().length)
             );
             storeAuth(connection);
             connection.setUseCaches(false);
             connection.setDoInput(true);
             connection.setDoOutput(true);
 
             BufferedOutputStream bos = new BufferedOutputStream(
                     connection.getOutputStream()
             );
             bos.write(squabJSON.getBytes(Charsets.UTF_8));
             bos.flush();
             bos.close();
 
             return expectCouchOkResponse(connection);
         } catch (IOException e) {
             throw new IllegalStateException("PUT failed", e);
         } finally {
             if (connection != null) {
                 connection.disconnect();
             }
         }
     }
 
     /**
      * Send attachments out of band of the main document update
      *     http://wiki.apache.org/couchdb/HTTP_Document_API#Standalone_Attachments
      *
      * @param couchId id of the main document
      * @param couchRev revision of the main document
      * @param fileName attachment fileName
      * @param contentType content type of the attachment
      * @param dataStream containing the attachment bytes
      * @return new revision of the main document
      */
     protected String couchFilePut(
             final String couchId,
             final String couchRev,
             final String fileName,
             final String contentType,
             final InputSupplier<InputStream> dataStream
     ) {
         HttpURLConnection connection = null;
         try {
             //Create connection
             URL url = new URL(
                     couchUrl + dbName + "/"
                             + url(couchId) + "/" + url(fileName)
                             + "?rev=" + url(couchRev)
             );
             connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod("PUT");
             connection.setRequestProperty(
                     "Content-Type",
                     contentType
             );
             storeAuth(connection);
 
             connection.setUseCaches(false);
             connection.setDoInput(true);
             connection.setDoOutput(true);
 
             ByteStreams.copy(dataStream, connOutput(connection));
 
             return expectCouchOkResponse(connection);
         } catch (IOException e) {
             throw new IllegalStateException("PUT failed", e);
         } finally {
             if (connection != null) {
                 connection.disconnect();
             }
         }
     }
 
     protected String expectCouchOkResponse(
             final HttpURLConnection connection
     ) throws IOException {
         InputStream is = null;
         try {
             is = connection.getInputStream();
             final Squab.RespUpdate update =
                     mapper.readValue(is, Squab.RespUpdate.class);
             if (!update.isOk()) {
                 throw new IllegalStateException("PUT failed:" + update);
             }
             return update.getRev();
         } finally {
             Closeables.closeQuietly(is);
         }
     }
 
     protected static OutputSupplier<OutputStream> connOutput(
             final HttpURLConnection connection
     ) {
         return new OutputSupplier<OutputStream>() {
             public OutputStream getOutput() throws IOException {
                 return new BufferedOutputStream(
                         connection.getOutputStream()
                 );
             }
         };
     }
 
     protected void storeAuth(
             final HttpURLConnection connection
     ) throws IOException {
         //  use Base64 codec bundled with Jackson: bytes -> "base64"
         final String base64Str = mapper.writeValueAsString(
                 (username + ":" + password).getBytes()
         );
         connection.addRequestProperty(
                 "Authorization",
                 //  drop first and last chars as those are JSON quotas
                 "Basic " + base64Str.substring(1, base64Str.length() - 1)
         );
     }
 }
