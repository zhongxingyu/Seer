 package org.sakaiproject.nakamura.lite.storage.jdbc;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.lite.content.StreamedContentHelper;
 import org.sakaiproject.nakamura.lite.storage.RowHasher;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Maps;
 
 public class FileStreamContentHelper implements StreamedContentHelper {
 
     private static final String DEFAULT_FILE_STORE = "store";
     private static final String CONFIG_STOREBASE = "store-base-dir";
     private static final Logger LOGGER = LoggerFactory.getLogger(FileStreamContentHelper.class);
     private static final String STORE_LOCATION = "bodyLocation";
     private String fileStore;
     private RowHasher rowHasher;
 
     public FileStreamContentHelper(RowHasher rowHasher, Map<String,Object> properties) {
         fileStore = StorageClientUtils.getSetting(properties.get(CONFIG_STOREBASE), DEFAULT_FILE_STORE);
         this.rowHasher = rowHasher;
     }
     
     @Override
     public Map<String, Object> writeBody(String keySpace, String columnFamily, String contentId,
             String contentBlockId, Map<String, Object> content, InputStream in) throws IOException {
         String path = getPath(keySpace, columnFamily, contentBlockId);
         File file = new File(fileStore+"/"+path);
        File parentFile = file.getParentFile();
         if ( !parentFile.exists() ) {
             if ( !parentFile.mkdirs() ) {
                 throw new IOException("Unable to create directory "+parentFile.getAbsolutePath());
             }
         }
         FileOutputStream out = new FileOutputStream(file);
         long length = IOUtils.copyLarge(in, out);
         out.close();
         LOGGER.info("Wrote {} bytes to {} as body of {}:{}:{} ",new Object[] {length,path, keySpace, columnFamily, contentBlockId});
         Map<String, Object> metadata = Maps.newHashMap();
         metadata.put(Content.LENGTH_FIELD, StorageClientUtils.toStore(length));
         metadata.put(Content.BLOCKID_FIELD, StorageClientUtils.toStore(contentBlockId));
         metadata.put(STORE_LOCATION, StorageClientUtils.toStore(path));
         return metadata;
     }
 
     private String getPath(String keySpace, String columnFamily, String contentBlockId) {
         Calendar c = new GregorianCalendar();
         c.setTimeInMillis(System.currentTimeMillis());
         int year = c.get(Calendar.YEAR);
         int month = c.get(Calendar.MONTH);
         String rowHash = rowHasher.rowHash(keySpace, columnFamily, contentBlockId);
         return year+"/"+month+"/"+rowHash.substring(0,2)+"/"+rowHash.substring(2,4)+"/"+rowHash.substring(4,6)+"/"+rowHash;
     }
 
     @Override
     public InputStream readBody(String keySpace, String columnFamily, String contentBlockId, Map<String, Object> content) throws IOException {
         String path = StorageClientUtils.toString(content.get(STORE_LOCATION));
         LOGGER.info("Reading from {} as body of {}:{}:{} ",new Object[] {path, keySpace, columnFamily, contentBlockId});
         File file = new File(fileStore+"/"+path);
         return new FileInputStream(file);
     }
 
 }
