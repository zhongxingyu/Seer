 /**
  * The contents of this file are subject to the license and copyright
  * detailed in the LICENSE and NOTICE files at the root of the source
  * tree and available online at
  *
  * http://www.dspace.org/license/
  */
 package org.dspace.ctask.replicate.store;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.duracloud.client.ContentStore;
 import org.duracloud.client.ContentStoreManager;
 import org.duracloud.client.ContentStoreManagerImpl;
 import org.duracloud.common.model.Credential;
 import org.duracloud.domain.Content;
 import org.duracloud.error.ContentStoreException;
 import org.duracloud.error.NotFoundException;
 
 import org.dspace.core.ConfigurationManager;
 import org.dspace.ctask.replicate.ObjectStore;
 import org.dspace.curate.Utils;
 
 /**
  * DuraCloudReplicaStore invokes the DuraCloud RESTful web service API,
  * (using a java client library) rather than using the rsync tool.
  *
  * @author richardrodgers
  */
 public class DuraCloudObjectStore implements ObjectStore
 {
     // DuraCloud store
     private ContentStore dcStore = null;
     
     public DuraCloudObjectStore()
     {
     }
 
     @Override
     public void init() throws IOException
     {
         // locate & login to Duracloud store
         ContentStoreManager storeManager =
             new ContentStoreManagerImpl(localProperty("host"),
                                         localProperty("port"),
                                         localProperty("context"));
         Credential credential = 
             new Credential(localProperty("username"), localProperty("password"));
         storeManager.login(credential);
         try
         {
             //Get the primary content store (e.g. Amazon)   
             dcStore = storeManager.getPrimaryContentStore();
         }
         catch (ContentStoreException csE)
         {      
             throw new IOException("Unable to connect to the DuraCloud Primary Content Store. Please check the DuraCloud connection/authentication settings in your 'duracloud.cfg' file.", csE);
         }
     }
 
     @Override
     public long fetchObject(String group, String id, File file) throws IOException
     {
         long size = 0L;
         try
         {
              // DEBUG REMOVE
             long start = System.currentTimeMillis();
             Content content = dcStore.getContent(group, id);
             // DEBUG REMOVE
             long elapsed = System.currentTimeMillis() - start;
             //System.out.println("DC fetch content: " + elapsed);
             size = Long.valueOf(content.getProperties().get(ContentStore.CONTENT_SIZE));
             FileOutputStream out = new FileOutputStream(file);
             // DEBUG remove
             start = System.currentTimeMillis();
             InputStream in = content.getStream();
             Utils.copy(in, out);
             in.close();
             out.close();
              // DEBUG REMOVE
             elapsed = System.currentTimeMillis() - start;
             //System.out.println("DC fetch download: " + elapsed);
         }
         catch (NotFoundException nfE)
         {
             // no object - no-op
         }
         catch (ContentStoreException csE)
         {
             throw new IOException(csE);
         }
         return size;
     }
     
     @Override
     public boolean objectExists(String group, String id) throws IOException
     {
         try
         {
             return dcStore.getContentProperties(group, id) != null;
         }
         catch (NotFoundException nfE)
         {
             return false;
         }
         catch (ContentStoreException csE)
         {
             throw new IOException(csE);
         }
     }
 
     @Override
     public long removeObject(String group, String id) throws IOException
     {
         // get metadata before blowing away
         long size = 0L;
         try
         {
             Map<String, String> attrs = dcStore.getContentProperties(group, id);
             size = Long.valueOf(attrs.get(ContentStore.CONTENT_SIZE));
             dcStore.deleteContent(group, id);
         }
         catch (NotFoundException nfE)
         {
             // no replica - no-op
         }
         catch (ContentStoreException csE)
         {
             throw new IOException(csE);
         }
         return size;
     }
 
     @Override
     public long transferObject(String group, File file) throws IOException
     {
         long size = 0L;
         String chkSum = Utils.checksum(file, "MD5");
         // make sure this is a different file from what replica store has
         // to avoid network I/O tax
         try
         {
             Map<String, String> attrs = dcStore.getContentProperties(group, file.getName());
             if (! chkSum.equals(attrs.get(ContentStore.CONTENT_CHECKSUM)))
             {
                 size = uploadReplica(group, file, chkSum);
             }
         }
         catch (NotFoundException nfE)
         {
             // no extant replica - proceed
             size = uploadReplica(group, file, chkSum);
         }
         catch (ContentStoreException csE)
         {
             throw new IOException(csE);
         }
         // delete staging file
         file.delete();
         return size;
     }
 
    private long uploadReplica(String group, File zipFile, String chkSum) throws IOException
     {
         try
         {
            //@TODO: This shouldn't have hardcoded MIME Type. Unfortunately, DuraCloud 
            // currently (as of 1.3) doesn't properly determine MIME Type. In future it should.
            dcStore.addContent(group, zipFile.getName(),
                               new FileInputStream(zipFile), zipFile.length(),
                               "application/zip", chkSum,
                                new HashMap<String, String>());
         
            return zipFile.length();
         }
         catch (ContentStoreException csE)
         {
             throw new IOException(csE);
         }
     }
 
     @Override
     public String objectAttribute(String group, String id, String attrName) throws IOException
     {
         try
         {
             Map<String, String> attrs = dcStore.getContentProperties(group, id);
             
             if ("checksum".equals(attrName))
             {
                 return attrs.get(ContentStore.CONTENT_CHECKSUM);
             }
             else if ("sizebytes".equals(attrName))
             {
                 return attrs.get(ContentStore.CONTENT_SIZE);
             }
             else if ("modified".equals(attrName))
             {
                 return attrs.get(ContentStore.CONTENT_MODIFIED);
             }
             return null;
         }
         catch (NotFoundException nfE)
         {
             return null;
         }
         catch (ContentStoreException csE)
         {
             throw new IOException(csE);
         }
     }
     
     private static String localProperty(String name)
     {
         return ConfigurationManager.getProperty("duracloud", name);
     }
 }
