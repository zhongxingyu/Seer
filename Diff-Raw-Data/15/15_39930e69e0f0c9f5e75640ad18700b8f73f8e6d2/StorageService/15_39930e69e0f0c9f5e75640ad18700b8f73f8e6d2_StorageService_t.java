 package me.justshare.service;
 
 import com.xerox.amazonws.sdb.SDBException;
 import me.justshare.domain.SharedItem;
 import me.justshare.storage.AmazonSimpleDB;
 import me.justshare.storage.S3;
 import me.justshare.storage.Utils;
 import org.jets3t.service.S3ServiceException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 /**
  * User: filippo@diotalevi.com
  * Date: Nov 17, 2010
  */
 public class StorageService {
 
     private AmazonSimpleDB simpleDb = new AmazonSimpleDB();
     private S3 s3 = new S3();
 
     private Cache cache = Cache.getInstance();
 
     public void createSpace(String space, String password) {
         try {
             simpleDb.createSpace(space, password);
         } catch (SDBException e) {
             throw new StorageException("Cannot create space", e);
         }
     }
 
 
     public boolean existSpace(String space) {
         try {
             return simpleDb.existSpace(space);
         } catch (SDBException e) {
             throw new StorageException("Cannot lookup space", e);
         }
     }
 
     public boolean isPasswordCorrect(String space, String password) {
         try {
             return simpleDb.isPasswordCorrect(space,password);
         } catch (SDBException e) {
             throw new StorageException("Cannot check password of space", e);
         }
     }
 
 
     public List<String> getSpaces() {
         try {
             return simpleDb.listSpaces();
         } catch (SDBException e) {
             throw new StorageException("Cannot list spaces", e);
         }
     }
 
 
     public void store(String space, InputStream fileInputStream, String fileName, String description) {
         try {
 
             if ("image/jpeg".equals(Utils.guessContentType(fileName)))
                 fileInputStream = Utils.scaleImageIfNecessary(fileInputStream, 600);
 
            String s3ObjectKey = null;
            if (fileInputStream != null)
               s3ObjectKey = s3.storeStream(fileName,fileInputStream, space);

             simpleDb.addSharedItem(space, s3ObjectKey, Utils.guessContentType(fileName),
                     description);
 
             cache.invalidateItemsFor(space);
             
         } catch (S3ServiceException e) {
             throw new StorageException("Cannot store in S3", e);
         } catch (SDBException e) {
             throw new StorageException("Cannot store in SimpleDb", e);
         } catch (IOException e) {
             throw new StorageException("Cannot resize image", e);    
         }
     }
 
 
     public List<SharedItem> listSharedItems(String space, int page) {
         try {
             if (cache.hasItemsFor(space, page))
                 return cache.getItemsFor(space, page);
             else
             {
                 List<SharedItem> sharedItems = simpleDb.getSharedItems(space, page);
                 cache.storeItems(space, page, sharedItems);
                 return sharedItems;
             }
 
         } catch (SDBException e) {
             throw new StorageException("Cannot retrieve shared items", e);
         }
     }
     
 }
