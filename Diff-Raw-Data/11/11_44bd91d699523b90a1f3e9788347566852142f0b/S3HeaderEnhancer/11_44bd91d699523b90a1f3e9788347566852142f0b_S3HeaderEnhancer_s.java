 /*
  * Copyright 2013, Moritz Siuts
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.scoyo.tools.s3cacheenhancer;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.model.AccessControlList;
 import com.amazonaws.services.s3.model.CopyObjectRequest;
 import com.amazonaws.services.s3.model.CopyObjectResult;
 import com.amazonaws.services.s3.model.ObjectListing;
 import com.amazonaws.services.s3.model.ObjectMetadata;
 import com.amazonaws.services.s3.model.S3ObjectSummary;
 
 public class S3HeaderEnhancer {
 
     private final AmazonS3 s3;
     private final String bucketName;
     private int maxAge = 94608000;
 
     public S3HeaderEnhancer(AmazonS3 s3, String bucketName) {
         this.bucketName = bucketName;
         this.s3 = s3;
     }
 
     public void createCacheHeaders() throws AmazonClientException {
         String maxAgeHeader = "max-age=" + maxAge;
         ObjectListing listing = s3.listObjects(bucketName);
         setHeaders(listing, maxAgeHeader);
         while (listing.isTruncated()) {
             listing = s3.listNextBatchOfObjects(listing);
             setHeaders(listing, maxAgeHeader);
         }
     }
 
     private void setHeaders(ObjectListing listing, String maxAgeHeader) {
 
         for (S3ObjectSummary summary : listing.getObjectSummaries()) {
             String bucket = summary.getBucketName();
             String key = summary.getKey();
 

            ObjectMetadata metadata = s3.getObjectMetadata(bucket, key);
             if (!maxAgeHeader.equals(metadata.getCacheControl())) {
                  metadata.setCacheControl(maxAgeHeader);
             }
 
             AccessControlList acl = s3.getObjectAcl(summary.getBucketName(), summary.getKey());
 
             CopyObjectRequest copyReq = new CopyObjectRequest(bucket, key, bucket, key)
                     .withAccessControlList(acl)
                     .withNewObjectMetadata(metadata);
 
             CopyObjectResult result = s3.copyObject(copyReq);
 
             if (result != null) {
                 System.out.println("Updated " + key);
             } else {
                 System.out.println("Could not update " + key);
             }
         }
     }
 
     /**
      * The the maxAge value for the Cache-Control-Header.
      * Defaults to {@code 94608000}.
      * @param maxAge a positive int, see above
      */
     public void setMaxAge(int maxAge) {
         if (maxAge < 0) {
             throw new IllegalArgumentException("maxAge must be positive.");
         }
         this.maxAge = maxAge;
     }
 
 
 }
