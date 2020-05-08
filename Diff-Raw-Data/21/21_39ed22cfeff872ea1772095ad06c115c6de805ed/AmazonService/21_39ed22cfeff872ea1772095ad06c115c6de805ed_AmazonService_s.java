 package com.washingtonpost.videocomments.service;
 
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.model.*;
 import org.apache.commons.io.IOUtils;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class AmazonService {
 
     private String bucket;
 
     private AmazonS3 amazonClient;
 
     public void setAmazonClient(AmazonS3 client) {
         amazonClient = client;
     }
 
     public void setBucket(String bucket) {
         this.bucket = bucket;
     }
 
     public void upload(InputStream stream, String target, long contentLength) {
         List<PartETag> partETags = new ArrayList<PartETag>();
         InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucket, target);
         InitiateMultipartUploadResult initResponse = amazonClient.initiateMultipartUpload(initRequest);
         long partSize = 5*(1L<<20);// Set part size to 5 MB.
         try {
             long filePosition = 0;
             int partNumber = 1;
             while(filePosition < contentLength) {
                 partSize = Math.min(partSize, (contentLength - filePosition));
                 UploadPartRequest uploadRequest = new UploadPartRequest()
                         .withBucketName(bucket).withKey(target)
                         .withUploadId(initResponse.getUploadId()).withPartNumber(partNumber++)
                         .withFileOffset(filePosition)
                         .withInputStream(stream)
                         .withPartSize(partSize);
                 partETags.add(amazonClient.uploadPart(uploadRequest).getPartETag());
                 filePosition += partSize;
             }
             CompleteMultipartUploadRequest compRequest =
                     new CompleteMultipartUploadRequest(
                             bucket,
                             target,
                             initResponse.getUploadId(),
                             partETags
                     );
             amazonClient.completeMultipartUpload(compRequest);
         } catch (Exception e) {
             amazonClient.abortMultipartUpload(
                     new AbortMultipartUploadRequest(bucket, target, initResponse.getUploadId())
             );
         }
     }
 
     public void download(String keyName, OutputStream stream) {
         S3Object s3object = amazonClient.getObject(new GetObjectRequest(bucket, keyName));
         try {
             IOUtils.copy(s3object.getObjectContent(), stream);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
 
     public ObjectMetadata metadata(String keyName) {
         ObjectMetadata metadata = amazonClient.getObjectMetadata(new GetObjectMetadataRequest(bucket, keyName));
         return metadata;
     }
 
     public void deleteAll() {
     }
 
 
 
 }
