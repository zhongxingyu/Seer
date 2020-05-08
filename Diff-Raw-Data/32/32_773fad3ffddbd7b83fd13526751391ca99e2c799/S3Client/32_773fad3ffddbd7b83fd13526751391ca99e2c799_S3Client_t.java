 package com.jsync;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.*;
 import org.apache.http.client.utils.URIBuilder;
 
 import java.io.*;
 import java.net.URI;
 import java.net.URISyntaxException;
import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.net.URLEncoder;
 
 /**
  * User: sanders
  * Date: 1/12/13
  */
 public class S3Client {
 
     private AmazonS3Client client;
     private int threadsOpen;
     private List<S3ObjectSummary> objects;
     private URI base;
     private String bucket;
     private boolean useReducedRedundancy;
     private boolean makePublic;
     private String fileEncoding;
 
 
     public S3Client(URI base, AmazonS3Client client, boolean useReducedRedundancy, boolean makePublic) {
         this.bucket = base.getHost();
         this.base = base;
         this.client = client;
         this.threadsOpen = 0;
         this.useReducedRedundancy = useReducedRedundancy;
         this.makePublic = makePublic;
         this.fileEncoding = System.getProperty("file.encoding");
     }
 
     public S3Client(URI base, AWSCredentials creds, boolean useReducedRedundancy, boolean makePublic) {
         this(base, new AmazonS3Client(creds), useReducedRedundancy, makePublic);
     }
 
     private void incrementThreadCount() {
         threadsOpen += 1;
     }
     private  void decrementThreadCount() {
         threadsOpen -= 1;
     }
     private int getThreadsOpen() {
         return threadsOpen;
     }
 
     private List<S3ObjectSummary> getObjectList() {
         if (this.objects == null) {
             this.objects = new ArrayList<S3ObjectSummary>();
             ObjectListing listing = client.listObjects(getBucket(), this.base.getPath().substring(1));
             while (true) {
                 for (S3ObjectSummary obj : listing.getObjectSummaries()) {
                     this.objects.add(obj);
                 }
                 if (listing.isTruncated()) {
                     listing = client.listNextBatchOfObjects(listing);
                 } else {
                     break;
                 }
             }
         }
 
         return this.objects;
     }
 
     public List<S3Resource> getChildren(String path, int point) throws URISyntaxException {
 
         String bucket = base.getHost();
         URI uri = null;
         try {
             uri = new URI(URLEncoder.encode(path, this.fileEncoding));
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
 
         S3ObjectSummary theFile = null;
 
         List<S3Resource> files = new ArrayList<S3Resource>();
         List<S3Resource> subdirs = new ArrayList<S3Resource>();
         List<String> visitedSubdirs = new ArrayList<String>();
 
         int c = point;
 
        String basepath = null;
        try {
            basepath = URLDecoder.decode(uri.getPath(),this.fileEncoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

         for (S3ObjectSummary object : getObjectList().subList(point, getObjectList().size())) {
 
             String key = "/" + object.getKey();
            String repl = key.replaceFirst("^" + Pattern.quote(basepath) + "/", "");
            Boolean matches = key.matches("^" + Pattern.quote(basepath) + "/.*$");
 
            if (key.equals(basepath)) {
                 theFile = object;
             }
 
             // is subdir
            if (repl.contains("/") && !key.equals(basepath) && matches) {
                 String name = repl.substring(0, repl.indexOf("/", 1));
                 if (!visitedSubdirs.contains(name)) {
                     URIBuilder builder = new URIBuilder();
                     builder.setScheme("s3");
                     builder.setHost(bucket);
                    builder.setPath(basepath + "/" + name);
                     subdirs.add(this.getResource(builder.build().toString(), c));
                     visitedSubdirs.add(name);
                 }
             }
 
             // is file
            else if (!repl.contains("/") && !key.equals(basepath) && matches) {
                 URIBuilder builder = new URIBuilder();
                 builder.setScheme("s3");
                 builder.setHost(bucket);
                 builder.setPath(key);
                 files.add(new S3Resource(object,  builder.build(), false, this, c));
             } else if ((files.size() != 0 || subdirs.size() != 0) || theFile != null){
                 break;
             }
 
             c += 1;
         }
 
 
         for (S3Resource dir : subdirs) {
             files.add(dir);
         }
 
         return files;
     }
 
 
     public S3Resource getResource(String path) throws URISyntaxException {
         return this.getResource(path, 0);
     }
 
     public S3Resource getResource(String path, int point) throws URISyntaxException {
 
         URI uri;
         uri = new URI(path);
 
         int numberOfChildren = 0;
 
         S3ObjectSummary theFile = null;
 
         int c = point;
         int off = point;
 
         for (S3ObjectSummary object : getObjectList().subList(point, getObjectList().size())) {
             String key = "/" + object.getKey();
             String repl = key.replaceFirst("^" + Pattern.quote(uri.getPath()) + "/", "");
 
             Boolean matches = key.matches("^" + Pattern.quote(uri.getPath()) + "/.*$");
 
             if (key.equals(uri.getPath())) {
                 theFile = object;
                 off = c;
             }
 
             // is subdir
             if (repl.contains("/") && !key.equals(uri.getPath()) && matches) {
                 numberOfChildren += 1;
             }
 
             // is file
             else if (!repl.contains("/") && !key.equals(uri.getPath()) && matches) {
                 numberOfChildren += 1;
             } else if (theFile != null){
                 break;
             }
 
             c += 1;
         }
 
         return new S3Resource(theFile, uri, numberOfChildren > 0, this, off);
 
     }
 
     public OutputStream getWriter(final URI path, final ObjectMetadata metadata) {
         final PipedInputStream in = new PipedInputStream();
         final PipedOutputStream out;
         try {
             out = new PipedOutputStream(in);
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
         incrementThreadCount();
         new Thread(
                 new Runnable(){
                     public void run(){
                         try {
                             PutObjectRequest request = new PutObjectRequest(getBucket(), path.getPath().substring(1), in, metadata);
                             if (useReducedRedundancy) {
                                 request.setStorageClass(StorageClass.ReducedRedundancy.toString());
                             }
                             if (makePublic) {
                                 request = request.withCannedAcl(CannedAccessControlList.PublicRead);
                             }
                             client.putObject(request);
                         } catch (AmazonClientException e) {
                             e.printStackTrace();
                         }
                         try {
                             in.close();
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                         Thread.yield();
                         decrementThreadCount();
                     }
                 }
         ).start();
         return out;
     }
 
     public Boolean cleanupWrites () {
         while (getThreadsOpen() >= 1) {
             try {
                 Thread.sleep(500);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
         return true;
     }
 
     public S3Object getObject(S3Resource resource) {
 
         return client.getObject(this.getBucket(), resource.getKey());
 
     }
 
     public void deleteObject(S3Resource resource) {
 
         client.deleteObject(this.getBucket(), resource.getKey());
 
     }
 
     public ObjectMetadata getObjectMetadata(S3Resource resource) {
 
         return client.getObjectMetadata(this.getBucket(), resource.getKey());
     }
 
     public void copyObject(S3Resource in, S3Resource out) {
         client.copyObject(this.getBucket(), in.getKey(), out.getBucket(), out.getKey());
     }
 
     public String getBucket() {
         return bucket;
     }
 }
