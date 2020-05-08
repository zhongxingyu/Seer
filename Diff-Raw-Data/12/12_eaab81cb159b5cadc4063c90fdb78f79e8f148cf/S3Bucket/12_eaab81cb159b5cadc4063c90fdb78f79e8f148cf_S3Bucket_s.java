 package com.github.vmorev.amazon;
 
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.Bucket;
 import com.amazonaws.services.s3.model.ObjectListing;
 import com.amazonaws.services.s3.model.ObjectMetadata;
 import com.amazonaws.services.s3.model.S3ObjectSummary;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 /**
  * User: Valentin_Morev
  * Date: 14.02.13
  */
 public class S3Bucket extends AmazonService {
     private static AmazonS3 s3;
     private String name;
 
     public static AmazonS3 getS3() {
         if (s3 == null)
             s3 = new AmazonS3Client(getCredentials());
         return s3;
     }
 
     public static List<Bucket> listBuckets() {
         return getS3().listBuckets();
     }
 
     public S3Bucket(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void createBucket() {
         if (!isBucketExist())
             getS3().createBucket(name);
     }
 
     public void deleteBucket() {
         try {
             listObjectSummaries(new ListFunc<S3ObjectSummary>() {
                 public void process(S3ObjectSummary summary) {
                     getS3().deleteObject(name, summary.getKey());
                 }
             });
         } catch (Exception e) {
             //let's try to delete bucket
         }
         getS3().deleteBucket(name);
     }
 
     public <T> T getObject(String key, Class<T> clazz) {
         T obj = null;
         try {
             obj = new ObjectMapper().readValue(getS3().getObject(name, key).getObjectContent(), clazz);
         } catch (Exception e) {
            e.printStackTrace();
             //do nothing and return null
         }
         return obj;
     }
 
     public void saveObject(String key, Object obj) throws IOException {
         InputStream inStream = new ByteArrayInputStream(new ObjectMapper().writeValueAsBytes(obj));
         ObjectMetadata metadata = new ObjectMetadata();
         metadata.setContentLength(inStream.available());
         getS3().putObject(name, key, inStream, metadata);
     }
 
     public <T> void listObjects( final Class<T> clazz, final ListFunc<T> func) throws Exception {
         listObjectSummaries(new ListFunc<S3ObjectSummary>() {
             public void process(S3ObjectSummary summary) throws Exception {
                 func.process(getObject(summary.getKey(), clazz));
             }
         });
     }
 
     public void listObjectSummaries(ListFunc<S3ObjectSummary> func) throws Exception {
         ObjectListing objectListing = getS3().listObjects(name);
         do {
             for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                 func.process(objectSummary);
             }
             objectListing.setMarker(objectListing.getNextMarker());
         } while (objectListing.isTruncated());
     }
 
     protected boolean isBucketExist() {
         for (Bucket bucket : listBuckets())
             if (bucket.getName().equals(name) && bucket.getOwner() != null)
                 return true;
         return false;
     }
 }
