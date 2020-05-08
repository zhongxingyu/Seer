 package org.duraspace.fcrepo.cloudsync.service.backend;
 
 import org.duraspace.fcrepo.cloudsync.api.ObjectInfo;
 import org.duraspace.fcrepo.cloudsync.api.ObjectStore;
 import org.duraspace.fcrepo.cloudsync.service.util.JSON;
 import org.duraspace.fcrepo.cloudsync.service.util.StringUtil;
 import com.github.cwilper.fcrepo.dto.core.ControlGroup;
 import com.github.cwilper.fcrepo.dto.core.Datastream;
 import com.github.cwilper.fcrepo.dto.core.DatastreamVersion;
 import com.github.cwilper.fcrepo.dto.core.FedoraObject;
 import com.github.cwilper.fcrepo.dto.foxml.FOXMLReader;
 import com.github.cwilper.fcrepo.dto.foxml.FOXMLWriter;
 import com.github.cwilper.ttff.Filter;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class FilesystemConnector extends StoreConnector {
     private static final Logger logger =
             LoggerFactory.getLogger(FilesystemConnector.class);
 
     private final File baseDir;
 
     public FilesystemConnector(ObjectStore store) {
         Map<String, String> map = JSON.getMap(JSON.parse(store.getData()));
         String path = StringUtil.validate("path", map.get("path"));
         baseDir = new File(path);
         if (!baseDir.isDirectory() && !baseDir.mkdir()) {
             throw new IllegalArgumentException("Cannot create base directory: "
                     + baseDir);
         }
     }
 
     @Override
     public void listObjects(ObjectQuery query, ObjectListHandler handler) {
         String type = query.getType();
         if (type.equals("pidPattern")) {
             listObjects(baseDir, new PIDPatternFilter(query.getPidPattern()), handler);
         } else if (type.equals("pidList")) {
             listObjects(query.getPidList().iterator(), handler);
         } else {
             throw new UnsupportedOperationException("Filesystem store does not support " + query.getQueryType() + " queries.");
         }
     }
     
     private void listObjects(File dir, Filter<String> filter, ObjectListHandler handler) {
         for (File child : dir.listFiles()) {
             if (child.isDirectory()) {
                 listObjects(child, filter, handler);
             } else {
                 String[] parts = decode(child.getName());
                 String pid = parts[0];
                 try {
                     if (parts[1] == null && filter.accept(pid) != null) {
                         ObjectInfo info = new ObjectInfo();
                         info.setPid(pid);
                         handler.handleObject(info);
                     }
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }
         }
     }
 
     @Override
     protected boolean hasObject(String pid) {
         return getFile(pid, null, null).exists();
     }
 
     @Override
     public FedoraObject getObject(String pid) {
         InputStream foxml = getStream(pid, null, null);
         if (foxml == null) return null;
         FOXMLReader reader = new FOXMLReader();
         try {
             return reader.readObject(foxml);
         } catch (IOException e) {
             throw new RuntimeException(e);
         } finally {
             reader.close();
         }
     }
 
     @Override
     public boolean putObject(FedoraObject o,
                              StoreConnector source,
                              boolean overwrite,
                              boolean copyExternal,
                              boolean copyRedirect) {
         boolean existed = hasObject(o.pid());
         if (existed) {
             if (!overwrite) {
                 return existed;
             }
         }
         FOXMLWriter writer = new FOXMLWriter();
         File foxmlFile = null;
         OutputStream out = null;
         try {
             // convert E/R datastreams to managed, if needed
             for (Datastream ds: o.datastreams().values()) {
                 ControlGroup c = ds.controlGroup();
                 if ((c.equals(ControlGroup.EXTERNAL) && copyExternal)
                         || (c.equals(ControlGroup.REDIRECT) && copyRedirect)) {
                     ds.controlGroup(ControlGroup.MANAGED);
                 }
             }
             // write foxml
             foxmlFile = getFile(o.pid(), null, null);
            foxmlFile.getParentFile().mkdirs();
             out = new FileOutputStream(foxmlFile);
             writer.writeObject(o, out);
             out.close();
             writer.close();
             // upload managed datastream content and foxml to DuraCloud
             putDatastreams(o, source);
             return existed;
         } catch (IOException e) {
             IOUtils.closeQuietly(out);
             throw new RuntimeException(e);
         }
     }
 
     private void putDatastreams(FedoraObject o,
                                 StoreConnector source) throws IOException {
         boolean success = false;
         Set<File> files = new HashSet<File>();
         try {
             for (Datastream ds: o.datastreams().values()) {
                 if (ds.controlGroup().equals(ControlGroup.MANAGED)) {
                     putVersions(o, ds, source, files);
                 }
             }
             success = true;
         } finally {
             if (!success) {
                 logger.info("Cleaning up after failure to put " +
                         "datastream(s) for {}", o.pid());
                 for (File file : files) {
                     if (!file.delete()) {
                         logger.warn("Unable to delete {} after failed put",
                                 file.getPath());
                     }
                 }
             }
         }
     }
 
     private void putVersions(FedoraObject o,
                              Datastream ds,
                              StoreConnector source,
                              Set<File> files) throws IOException {
         for (DatastreamVersion dsv: ds.versions()) {
             InputStream in = source.getContent(o,  ds, dsv);
             File file = getFile(o.pid(), ds.id(), dsv.id());
             OutputStream out = new FileOutputStream(file);
             try {
                 IOUtils.copyLarge(in,  out);
             } finally {
                 IOUtils.closeQuietly(in);
                 IOUtils.closeQuietly(out);
             }
             files.add(file);
         }
     }
 
     @Override
     public InputStream getContent(FedoraObject o, Datastream ds, DatastreamVersion dsv) {
         return getStream(o.pid(), ds.id(), dsv.id());
     }
 
     @Override
     public void close() {
         // no-op
     }
 
     // returns null if file doesn't exist
     // dsId and dsVersionId may be null
     private InputStream getStream(String pid, String dsId, String dsVersionId) {
         File file = getFile(pid, dsId, dsVersionId);
         try {
             return new FileInputStream(file);
         } catch (FileNotFoundException e) {
             return null;
         }
     }
 
     // dsId and dsVersionId may be null
     private File getFile(String pid, String dsId, String dsVersionId) {
         File dir = new File(baseDir, getPath(pid));
         String filename = encode(pid, dsId, dsVersionId);
         return new File(dir, filename);
     }
 
     // gets a path like 'ff/ff' from first 4 hex chars of pid md5 digest
     static String getPath(String pid) {
         String hex = DigestUtils.md5Hex(pid);
         return "" + hex.charAt(0) + hex.charAt(1) +
                 '/' + hex.charAt(2) + hex.charAt(3);
     }
 
     // gets a filename safe for use with modern filesystems
     // dsId and dsVersionId may be null
     static String encode(String pid, String dsId, String dsVersionId) {
         if (dsId == null) return encodePart(pid);
         return encodePart(pid) + '+' + encodePart(dsId) + '+' +
                 encodePart(dsVersionId);
     }
 
     // reverses the encoding
     static String[] decode(String filename) {
         int i = filename.indexOf("+");
         if (i == -1) return new String[] { decodePart(filename), null, null };
         int j = filename.lastIndexOf("+");
         return new String[] { decodePart(filename.substring(0, i)),
                 decodePart(filename.substring(i + 1, j)),
                 decodePart(filename.substring(j + 1)) };
     }
     
     private static String encodePart(String string) {
         // encode char-by-char because we only want to borrow
         // URLEncoder.encode's behavior for some characters
         StringBuilder out = new StringBuilder();
         for (int i = 0; i < string.length(); i++) {
             char c = string.charAt(i);
             if (c >= 'a' && c <= 'z') {
                 out.append(c);
             } else if (c >= '0' && c <= '9') {
                 out.append(c);
             } else if (c >= 'A' && c <= 'Z') {
                 out.append(c);
             } else if (c == '-' || c == '=' || c == '(' || c == ')'
                     || c == '[' || c == ']' || c == ';') {
                 out.append(c);
             } else if (c == ':') {
                 out.append("%3A");
             } else if (c == ' ') {
                 out.append("%20");
             } else if (c == '+') {
                 out.append("%2B");
             } else if (c == '_') {
                 out.append("%5F");
             } else if (c == '*') {
                 out.append("%2A");
             } else if (c == '.') {
                 if (i == string.length() - 1) {
                     out.append("%2E");
                 } else {
                     out.append(".");
                 }
             } else {
                 try {
                     out.append(URLEncoder.encode("" + c, "UTF-8"));
                 } catch (UnsupportedEncodingException wontHappen) {
                     throw new RuntimeException(wontHappen);
                 }
             }
         }
         return out.toString();
     }
 
     private static String decodePart(String string) {
         if (string.endsWith("%2E")) {
             string = string.substring(0, string.length() - 3) + ".";
         }
         try {
             return URLDecoder.decode(string, "UTF-8");
         } catch (UnsupportedEncodingException wontHappen) {
             throw new RuntimeException(wontHappen);
         }
     }
 
 }
