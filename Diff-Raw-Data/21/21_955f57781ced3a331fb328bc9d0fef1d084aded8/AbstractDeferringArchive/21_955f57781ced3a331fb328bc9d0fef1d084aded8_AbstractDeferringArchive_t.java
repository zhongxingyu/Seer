 package dk.statsbiblioteket.metadatarepository.xmltapes.common;
 
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abr
  * Date: 6/20/13
  * Time: 12:17 PM
  * To change this template use File | Settings | File Templates.
  */
 public abstract  class AbstractDeferringArchive<T extends Archive> implements Archive{
 
    public static final String TEMP_PREFIX = "temp";
     private static final Logger log = LoggerFactory.getLogger(AbstractDeferringArchive.class);
 
     public static final String UTF_8 = "UTF-8";
     private T delegate;
     private  File deferredDir;
 
     public final LockPool lockPool;
 
 
 
 
     public AbstractDeferringArchive() {
         lockPool = new LockPoolImpl();
     }
 
 
 
     @Override
     public InputStream getInputStream(URI id) throws FileNotFoundException, IOException {
 
 
         File cacheFile = getDeferredFile(id);
         try {
             return new FileInputStream(cacheFile);
         } catch (FileNotFoundException e){
             return delegate.getInputStream(id);
         }
     }
 
     public File getDeferredFile(URI id) {
         try {
             return new File(deferredDir,
                     URLEncoder.encode(id.toString(), UTF_8));
         } catch (UnsupportedEncodingException e) {
             throw new Error(e);
         }
     }
 
     protected File getDeferredFileDeleted(URI id) {
         try {
             return new File(deferredDir,
                     URLEncoder.encode(id.toString()+ "#"+TapeUtils.DELETED, UTF_8));
         } catch (UnsupportedEncodingException e) {
             throw new Error(e);
         }
 
     }
 
 
     protected URI getIDfromFile(File cacheFile) {
 
         try {
            String name = cacheFile.getName();
            name = URLDecoder.decode(name,"UTF-8");
            name = name.replaceAll(Pattern.quote("#"+TapeUtils.DELETED)+"$","");
             return new URI(URLDecoder.decode(name, UTF_8));
         } catch (URISyntaxException e) {
             return null;
         } catch (UnsupportedEncodingException e) {
             throw new Error(e);
         }
     }
 
 
 
 
 
     @Override
     public boolean exist(URI id) throws IOException {
 
         File cacheFile = getDeferredFile(id);
         try {
             lockPool.lockForWriting();
             if (cacheFile.exists()){
                 return true;
             }
         } finally {
             lockPool.unlockForWriting();
         }
         return  delegate.exist(id);
     }
 
     @Override
     public long getSize(URI id) throws FileNotFoundException, IOException {
 
         File cacheFile = getDeferredFile(id);
         try {
             lockPool.lockForWriting();
             long size = cacheFile.length();
             if (cacheFile.exists()){
                 return size;
             }
         } finally {
             lockPool.unlockForWriting();
         }
         return delegate.getSize(id);
 
 
     }
 
 
     /**
      * Get the files in the cache.
      * @return
      */
     public List<File> getCacheFiles() {
         //Ensure that noone is able to lock files until we have locked all.
         List<File> cacheFiles;
 
         lockPool.lockForWriting();
         try {
             cacheFiles = new ArrayList<File>(FileUtils.listFiles(getDeferredDir(), null, false));
             // Sort them in last modified order
             Collections.sort(cacheFiles, new Comparator<File>() {
                 @Override
                 public int compare(File o1, File o2) {
                     long x = o1.lastModified();
                     long y = o2.lastModified();
                     return Long.valueOf(x).compareTo(y);
                 }
             });
             return cacheFiles;
         } finally {
             lockPool.unlockForWriting();
         }
     }
 
     public Collection<URI> getCacheIDs(String filterPrefix) {
         //Get the cached files
         List<File> cacheFiles = getCacheFiles();
         ArrayList<URI> result = new ArrayList<URI>();
         for (File cacheFile : cacheFiles) {
             URI id = getIDfromFile(cacheFile);
             if (id == null){
                 continue;
             }
 
             if (filterPrefix != null && id.toString().startsWith(filterPrefix)){
                 result.add(id);
             }
         }
         return result;
     }
 
 
 
     @Override
     public Iterator<URI> listIds(String filterPrefix) {
         log.debug("Calling listIDs with argument {}",filterPrefix);
         return delegate.listIds(filterPrefix);
     }
 
     @Override
     public void init() throws IOException {
         delegate.init();
     }
 
     public File getDeferredDir() {
         return deferredDir;
     }
 
 
     public T getDelegate() {
         return delegate;
     }
 
     public void setDelegate(T delegate) {
         this.delegate = delegate;
     }
 
     public void setDeferredDir(File deferredDir) {
         try {
             this.deferredDir = deferredDir.getCanonicalFile();
             this.deferredDir.mkdirs();
         } catch (IOException e) {
             this.deferredDir = deferredDir;
         }
     }
 
 
     public void close() throws IOException{
         getDelegate().close();
     }
 
    protected File getTempFile(URI id, File temp_dir) throws IOException {
        File tempfile = File.createTempFile(URLEncoder.encode(id.toString(), UTF_8), TEMP_PREFIX, temp_dir);
        tempfile.deleteOnExit();
        return tempfile;
    }


 
 }
 
 
