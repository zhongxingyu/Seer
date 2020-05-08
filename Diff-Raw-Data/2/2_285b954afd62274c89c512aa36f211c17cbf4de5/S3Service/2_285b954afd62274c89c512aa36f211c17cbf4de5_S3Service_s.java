 /*
  * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
  * This is a java.net project, see https://jets3t.dev.java.net/
  * 
  * Copyright 2006 James Murty
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package org.jets3t.service;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import org.jets3t.service.acl.AccessControlList;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3BucketLoggingStatus;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.multithread.S3ServiceMulti;
 import org.jets3t.service.security.AWSCredentials;
 import org.jets3t.service.utils.RestUtils;
 import org.jets3t.service.utils.ServiceUtils;
 
 /**
  * A service that handles communication with S3, performing all available actions.
  * <p>
  * This class must be extended by implementation classes that perform the communication with S3 via
  * a particular interface, such as REST or SOAP. Implementations provided with jets3t include 
  * {@link org.jets3t.service.impl.rest.httpclient.RestS3Service} and 
  * {@link org.jets3t.service.impl.soap.axis.SoapS3Service}.
  * <p>
  * Implementations of <code>S3Service</code> must be thread-safe as they will probably be used by
  * the multi-threaded service class {@link S3ServiceMulti}. 
  * <p>
  * <p><b>Properties</b></p>
  * <p>The following properties, obtained through {@link Jets3tProperties}, are used by this class:</p>
  * <table>
  * <tr><th>Property</th><th>Description</th><th>Default</th></tr>
  * <tr><td>s3service.https-only</td>
  *   <td>If true, all communication with S3 will be via encrypted HTTPS connections, and will be
  *   much slower. If false, communications will be unencrypted and faster</td> 
  *   <td>false</td></tr>
  * </table>
  * 
  * 
  * @author James Murty
  */
 public abstract class S3Service {
 
     private AWSCredentials awsCredentials = null;
     private boolean isHttpsOnly = false;
 
     /**
      * Construct an <code>S3Service</code> identified by the given AWS Principal.
      * 
      * @param awsPrincipal
      * the S3 user credentials to use when communicating with S3, may be null in which case the
      * communication is done as an anonymous user.
      * @throws S3ServiceException
      */
     protected S3Service(AWSCredentials awsPrincipal) throws S3ServiceException {
         this.awsCredentials = awsPrincipal;
         isHttpsOnly = Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME)
             .getBoolProperty("s3service.https-only", false);        
     }
 
     /**
      * @return true if this service has <code>AWSCredentials</code> identifying an S3 user, false
      * if the service is acting as an anonymous user.
      */
     public boolean isAuthenticatedConnection() {
         return awsCredentials != null;
     }
     
     public boolean isHttpsOnly() {
         return isHttpsOnly;
     }
 
     /**
      * @return the AWS Credentials identifying the S3 user, may be null if the service is acting
      * anonymously.
      */
     public AWSCredentials getAWSCredentials() {
         return awsCredentials;
     }
     
     /**
      * Generates a signed URL string that will grant access to an S3 resource (bucket or object)
      * to whoever uses the URL up until the time specified.
      * 
      * @param bucketName
      * the name of the bucket to include in the URL, must be a valid bucket name.
      * @param objectKey
      * the name of the object to include in the URL, if null only the bucket name is used. 
      * @param awsCredentials
      * the credentials of someone with sufficient privileges to grant access to the bucket/object 
      * @param secondsSinceEpoch
      * the time after which URL's signature will no longer be valid. This time cannot be null.
      *  <b>Note:</b> This time is specified in seconds since the epoch, not milliseconds. 
      * @param isSecure
      * if true an HTTPS URL is created and data accessed using the generated URL will be encrypted
      * when accessed, otherwise a standard HTTP URL is created.
      * @return
      * a URL signed in such a way as to grant access to an S3 resource to whoever uses it.
      * @throws S3ServiceException
      */
     public static String createSignedUrl(String bucketName, String objectKey, AWSCredentials awsCredentials, 
         long secondsSinceEpoch, boolean isSecure) throws S3ServiceException
     {
        String fullKey = bucketName + (objectKey != null ? "/" + objectKey : "");
         fullKey += "?AWSAccessKeyId=" + awsCredentials.getAccessKey();
         fullKey += "&Expires=" + secondsSinceEpoch;
 
         String canonicalString = RestUtils.makeCanonicalString("GET", "/" + fullKey, null, String
             .valueOf(secondsSinceEpoch));
 
         String signedCanonical = ServiceUtils.signWithHmacSha1(awsCredentials.getSecretKey(),
             canonicalString);
         String encodedCanonical = RestUtils.encodeUrlString(signedCanonical);
         fullKey += "&Signature=" + encodedCanonical;
 
         if (isSecure) {
             return "https://" + Constants.REST_SERVER_DNS + "/" + fullKey;
         } else {
             return "http://" + Constants.REST_SERVER_DNS + "/" + fullKey;            
         }
     }
     
     public static String createSignedUrl(String bucketName, String objectKey, AWSCredentials awsCredentials, 
         Date expiryTime, boolean isSecure) throws S3ServiceException
     {
         long secondsSinceEpoch = expiryTime.getTime() / 1000;
         return createSignedUrl(bucketName, objectKey, awsCredentials, secondsSinceEpoch, isSecure);
     }
         
     /**
      * Generates a URL string that will return a Torrent file for an object in S3, 
      * which file can be downloaded and run in a BitTorrent client.  
      * 
      * @param bucketName
      * the name of the bucket containing the object.
      * @param objectKey
      * the name of the object. 
      * @return
      * a URL to a Torrent file representing the object.
      * @throws S3ServiceException
      */
     public static String createTorrentUrl(String bucketName, String objectKey) {
         return "http://" + Constants.REST_SERVER_DNS + "/" +
             bucketName + "/" + objectKey + "?torrent"; 
     }
     
     /////////////////////////////////////////////////////////////////////////////
     // Assertion methods used to sanity-check parameters provided to this service
     /////////////////////////////////////////////////////////////////////////////
     
     /**
      * Throws an exception if this service is anonymous (that is, it was created without
      * an <code>AWSCredentials</code> object representing an S3 user account.
      * @param action
      * the action being attempted which this assertion is applied, for debugging purposes.
      * @throws S3ServiceException
      */
     protected void assertAuthenticatedConnection(String action) throws S3ServiceException {
         if (!isAuthenticatedConnection()) {
             throw new S3ServiceException(
                 "The requested action cannot be performed with a non-authenticated S3 Service: "
                     + action);
         }
     }
 
     /**
      * Throws an exception if a bucket is null or contains a null/empty name.
      * @param bucket
      * @param action
      * the action being attempted which this assertion is applied, for debugging purposes.
      * @throws S3ServiceException
      */
     protected void assertValidBucket(S3Bucket bucket, String action) throws S3ServiceException {
         if (bucket == null || bucket.getName() == null || bucket.getName().length() == 0) {
             throw new S3ServiceException("The action " + action
                 + " cannot be performed with an invalid bucket: " + bucket);
         }
     }
 
     /**
      * Throws an exception if an object is null or contains a null/empty key.
      * @param object
      * @param action
      * the action being attempted which this assertion is applied, for debugging purposes.
      * @throws S3ServiceException
      */
     protected void assertValidObject(S3Object object, String action) throws S3ServiceException {
         if (object == null || object.getKey() == null || object.getKey().length() == 0) {
             throw new S3ServiceException("The action " + action
                 + " cannot be performed with an invalid object: " + object);
         }
     }    
     
     /////////////////////////////////////////////////
     // Methods below this point perform actions in S3
     /////////////////////////////////////////////////
 
     /**
      * Lists the objects in a bucket.
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucket
      * the bucket whose contents will be listed. 
      * This must be a valid S3Bucket object that is non-null and contains a name.
      * @return
      * the set of objects contained in a bucket.
      * @throws S3ServiceException
      */
     public S3Object[] listObjects(S3Bucket bucket) throws S3ServiceException {
         assertValidBucket(bucket, "listObjects");
         return listObjects(bucket, null, null, Constants.DEFAULT_OBJECT_LIST_CHUNK_SIZE);
     }
 
     /**
      * Lists the objects in a bucket matching a prefix.
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucket
      * the bucket whose contents will be listed. 
      * This must be a valid S3Bucket object that is non-null and contains a name.
      * @param prefix
      * only objects with a key that starts with this prefix will be listed
      * @param delimiter
      * only list objects with key names up to this delimiter, may be null.
      * <b>Note</b>: If a non-null delimiter is specified, the prefix must include enough text to
      * reach the first occurrence of the delimiter in the bucket's keys, or no results will be returned.
      * @return
      * the set of objects contained in a bucket whose keys start with the given prefix.
      * @throws S3ServiceException
      */
     public S3Object[] listObjects(S3Bucket bucket, String prefix, String delimiter) throws S3ServiceException {
         assertValidBucket(bucket, "listObjects");
         return listObjects(bucket, prefix, delimiter, Constants.DEFAULT_OBJECT_LIST_CHUNK_SIZE);
     }
 
     /**
      * Creates a bucket.
      * <p>
      * This method cannot be performed by anonymous services.
      * 
      * @param bucketName
      * the name of the bucket to create
      * @return
      * the created bucket object. <b>Note:</b> the object returned has minimal information about
      * the bucket that was created, including only the bucket's name.
      * @throws S3ServiceException
      */
     public S3Bucket createBucket(String bucketName) throws S3ServiceException {
         assertAuthenticatedConnection("createBucket");
         S3Bucket bucket = new S3Bucket();
         bucket.setName(bucketName);
         return createBucket(bucket);
     }
 
     /**
      * Returns an object representing the details and data of an item in S3, without applying any
      * preconditions.
      * <p>
      * This method can be performed by anonymous services.
      *  
      * @param bucket
      * the bucket containing the object.
      * This must be a valid S3Bucket object that is non-null and contains a name.
      * @param objectKey
      * the key identifying the object.
      * @return
      * the object with the given key in S3, including the object's data input stream.
      * @throws S3ServiceException
      */
     public S3Object getObject(S3Bucket bucket, String objectKey) throws S3ServiceException {
         assertValidBucket(bucket, "getObject");
         return getObject(bucket, objectKey, null, null, null, null, null, null);
     }
 
     /**
      * Returns an object representing the details of an item in S3 without the object's data, and
      * without applying any preconditions.
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucket
      * the bucket containing the object.
      * This must be a valid S3Bucket object that is non-null and contains a name.
      * @param objectKey
      * the key identifying the object.
      * @return
      * the object with the given key in S3, including only general details and metadata (not the data
      * input stream)
      * @throws S3ServiceException
      */
     public S3Object getObjectDetails(S3Bucket bucket, String objectKey) throws S3ServiceException {
         assertValidBucket(bucket, "getObjectDetails");
         return getObjectDetails(bucket, objectKey, null, null, null, null);
     }
 
 
     /**
      * Lists the buckets belonging to the service user. 
      * <p>
      * This method cannot be performed by anonymous services, and will fail with an exception
      * if the service is not authenticated.
      * 
      * @return
      * the list of buckets owned by the service user.
      * @throws S3ServiceException
      */
     public S3Bucket[] listAllBuckets() throws S3ServiceException {
         assertAuthenticatedConnection("List all buckets");
         return listAllBucketsImpl();
     }
 
     /**
      * Lists the objects in a bucket matching a prefix, chunking the results into batches of
      * a given size. 
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucket
      * the bucket whose contents will be listed. 
      * This must be a valid S3Bucket object that is non-null and contains a name.
      * @param prefix
      * only objects with a key that starts with this prefix will be listed
      * @param maxListingLength
      * the maximum number of objects to include in each result chunk
      * @return
      * the set of objects contained in a bucket whose keys start with the given prefix.
      * @throws S3ServiceException
      */
     public S3Object[] listObjects(S3Bucket bucket, String prefix, String delimiter, 
         long maxListingLength) throws S3ServiceException
     {
         assertValidBucket(bucket, "List objects in bucket");
         return listObjects(bucket.getName(), prefix, delimiter, maxListingLength);
     }
 
     /**
      * Lists the objects in a bucket matching a prefix, chunking the results into batches of
      * a given size. 
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucketName
      * the name of the the bucket whose contents will be listed. 
      * @param prefix
      * only objects with a key that starts with this prefix will be listed
      * @param maxListingLength
      * the maximum number of objects to include in each result chunk
      * @return
      * the set of objects contained in a bucket whose keys start with the given prefix.
      * @throws S3ServiceException
      */
     public S3Object[] listObjects(String bucketName, String prefix, String delimiter, 
         long maxListingLength) throws S3ServiceException
     {
         return listObjectsImpl(bucketName, prefix, delimiter, maxListingLength);
     }
 
     /**
      * Lists the objects in a bucket matching a prefix, chunking the results into batches of
      * a given size, and returning each chunk separately. It is the responsility of the caller 
      * to building a complete bucket object listing from .
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucketName
      * the name of the the bucket whose contents will be listed. 
      * @param prefix
      * only objects with a key that starts with this prefix will be listed
      * @param maxListingLength
      * the maximum number of objects to include in each result chunk
      * @param priorLastKey
      * the last object key received in a prior call to this method. The next chunk of objects
      * listed will start with the next object in the bucket <b>after</b> this key name.
      * This paramater may be null, in which case the listing will start at the beginning of the
      * bucket's object contents.
      * @return
      * the set of objects contained in a bucket whose keys start with the given prefix.
      * @throws S3ServiceException
      */
     public S3ObjectsChunk listObjectsChunked(String bucketName, String prefix, String delimiter, 
         long maxListingLength, String priorLastKey) throws S3ServiceException
     {
         return listObjectsChunkedImpl(bucketName, prefix, delimiter, maxListingLength, priorLastKey);
     }
 
     /**
      * Creates a bucket in S3 based on the provided bucket object.
      * <p>
      * This method cannot be performed by anonymous services.
      * 
      * @param bucket
      * an object representing the bucket to create which must be valid, and may contain ACL settings.
      * @return
      * the created bucket object, populated with all metadata made available by the creation operation. 
      * @throws S3ServiceException
      */
     public S3Bucket createBucket(S3Bucket bucket) throws S3ServiceException {
         assertAuthenticatedConnection("Create Bucket");
         assertValidBucket(bucket, "Create Bucket");
         return createBucketImpl(bucket.getName(), bucket.getAcl());
     }
 
     /**
      * Deletes an S3 bucket.
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucket
      * the bucket to delete.
      * @throws S3ServiceException
      */
     public void deleteBucket(S3Bucket bucket) throws S3ServiceException {
         assertValidBucket(bucket, "Delete bucket");
         deleteBucketImpl(bucket.getName());
     }
 
     /**
      * Deletes an S3 bucket.
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucketName
      * the name of the bucket to delete.
      * @throws S3ServiceException
      */
     public void deleteBucket(String bucketName) throws S3ServiceException {
         deleteBucketImpl(bucketName);
     }
 
     /**
      * Puts an object inside an existing bucket in S3, creating a new object or overwriting
      * an existing one with the same key.
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucketName
      * the name of the bucket inside which the object will be put.
      * @param object
      * the object containing all information that will be written to S3. At very least this object must
      * be valid. Beyond that it may contain: an input stream with the object's data content, metadata,
      * and access control settings.<p>
      * <b>Note:</b> It is very important to set the object's Content-Length to match the size of the 
      * data input stream when possible, as this can remove the need to read data into memory to
      * determine its size. 
      * 
      * @return
      * the object populated with any metadata information made available by S3. 
      * @throws S3ServiceException
      */
     public S3Object putObject(String bucketName, S3Object object) throws S3ServiceException {
         assertValidObject(object, "Create Object in bucket " + bucketName);        
         return putObjectImpl(bucketName, object);
     }
 
     /**
      * Puts an object inside an existing bucket in S3, creating a new object or overwriting
      * an existing one with the same key.
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucket
      * the bucket inside which the object will be put, which must be valid.
      * @param object
      * the object containing all information that will be written to S3. At very least this object must
      * be valid. Beyond that it may contain: an input stream with the object's data content, metadata,
      * and access control settings.<p>
      * <b>Note:</b> It is very important to set the object's Content-Length to match the size of the 
      * data input stream when possible, as this can remove the need to read data into memory to
      * determine its size. 
      * 
      * @return
      * the object populated with any metadata information made available by S3. 
      * @throws S3ServiceException
      */
     public S3Object putObject(S3Bucket bucket, S3Object object) throws S3ServiceException {
         assertValidBucket(bucket, "Create Object in bucket");
         return putObject(bucket.getName(), object);
     }
     
     /**
      * Deletes an object from a bucket in S3.
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucket
      * the bucket containing the object to be deleted.
      * @param objectKey
      * the key representing the object in S3.
      * @throws S3ServiceException
      */
     public void deleteObject(S3Bucket bucket, String objectKey) throws S3ServiceException {
         assertValidBucket(bucket, "deleteObject");
         deleteObject(bucket.getName(), objectKey);
     }
 
     /**
      * Deletes an object from a bucket in S3.
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucketName
      * the name of the bucket containing the object to be deleted.
      * @param objectKey
      * the key representing the object in S3.
      * @throws S3ServiceException
      */
     public void deleteObject(String bucketName, String objectKey) throws S3ServiceException {
         deleteObjectImpl(bucketName, objectKey);
     }
 
     /**
      * Returns an object representing the details of an item in S3 that meets any given preconditions.
      * The object is returned without the object's data.
      * <p>
      * An exception is thrown if any of the preconditions fail. 
      * Preconditions are only applied if they are non-null.
      * <p>
      * This method can be performed by anonymous services.
      * 
      * @param bucket
      * the bucket containing the object.
      * This must be a valid S3Bucket object that is non-null and contains a name.
      * @param objectKey
      * the key identifying the object.
      * @param ifModifiedSince
      * a precondition specifying a date after which the object must have been modified, ignored if null.
      * @param ifUnmodifiedSince
      * a precondition specifying a date after which the object must not have been modified, ignored if null.
      * @param ifMatchTags
      * a precondition specifying an MD5 hash the object must match, ignored if null.
      * @param ifNoneMatchTags
      * a precondition specifying an MD5 hash the object must not match, ignored if null.
      * @return
      * the object with the given key in S3, including only general details and metadata (not the data
      * input stream)
      * @throws S3ServiceException
      */
     public S3Object getObjectDetails(S3Bucket bucket, String objectKey,
         Calendar ifModifiedSince, Calendar ifUnmodifiedSince, String[] ifMatchTags,
         String[] ifNoneMatchTags) throws S3ServiceException
     {
         assertValidBucket(bucket, "Get Object Details");
         return getObjectDetailsImpl(bucket.getName(), objectKey, ifModifiedSince, ifUnmodifiedSince, 
             ifMatchTags, ifNoneMatchTags);
     }
 
     public S3Object getObjectDetails(String bucketName, String objectKey,
         Calendar ifModifiedSince, Calendar ifUnmodifiedSince, String[] ifMatchTags,
         String[] ifNoneMatchTags) throws S3ServiceException
     {
         return getObjectDetailsImpl(bucketName, objectKey, ifModifiedSince, ifUnmodifiedSince, 
             ifMatchTags, ifNoneMatchTags);
     }
 
     /**
      * Returns an object representing the details of an item in S3 that meets any given preconditions.
      * The object is returned with the object's data.
      * <p>
      * An exception is thrown if any of the preconditions fail. 
      * Preconditions are only applied if they are non-null.
      * <p>
      * This method can be performed by anonymous services.
      * <p>
      * <b>Implementation notes</b><p>
      * Implementations should use {@link #assertValidBucket} assertion.
      * 
      * @param bucket
      * the bucket containing the object.
      * This must be a valid S3Bucket object that is non-null and contains a name.
      * @param objectKey
      * the key identifying the object.
      * @param ifModifiedSince
      * a precondition specifying a date after which the object must have been modified, ignored if null.
      * @param ifUnmodifiedSince
      * a precondition specifying a date after which the object must not have been modified, ignored if null.
      * @param ifMatchTags
      * a precondition specifying an MD5 hash the object must match, ignored if null.
      * @param ifNoneMatchTags
      * a precondition specifying an MD5 hash the object must not match, ignored if null.
      * @param byteRangeStart
      * include only a portion of the object's data - starting at this point, ignored if null. 
      * @param byteRangeEnd
      * include only a portion of the object's data - ending at this point, ignored if null. 
      * @return
      * the object with the given key in S3, including only general details and metadata (not the data
      * input stream)
      * @throws S3ServiceException
      */
     public S3Object getObject(S3Bucket bucket, String objectKey, Calendar ifModifiedSince,
         Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags,
         Long byteRangeStart, Long byteRangeEnd) throws S3ServiceException 
     {
         assertValidBucket(bucket, "Get Object");
         return getObjectImpl(bucket.getName(), objectKey, ifModifiedSince, ifUnmodifiedSince, 
             ifMatchTags, ifNoneMatchTags, byteRangeStart, byteRangeEnd);
     }
 
     public S3Object getObject(String bucketName, String objectKey, Calendar ifModifiedSince,
         Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags,
         Long byteRangeStart, Long byteRangeEnd) throws S3ServiceException 
     {
         return getObjectImpl(bucketName, objectKey, ifModifiedSince, ifUnmodifiedSince, 
             ifMatchTags, ifNoneMatchTags, byteRangeStart, byteRangeEnd);
     }
 
     /**
      * Applies access control settings to an object. The ACL settings must be included
      * with the object.
      * 
      * @param bucket
      * the bucket containing the object to modify.
      * @param object
      * the object with ACL settings that will be applied.
      * @throws S3ServiceException
      */
     public void putObjectAcl(S3Bucket bucket, S3Object object) throws S3ServiceException {
         assertValidBucket(bucket, "Put Object Access Control List");
         assertValidObject(object, "Put Object Access Control List");
         putObjectAcl(bucket.getName(), object.getKey(), object.getAcl());
     }
 
     public void putObjectAcl(String bucketName, String objectKey, AccessControlList acl) 
         throws S3ServiceException 
     {
         if (acl == null) {
             throw new S3ServiceException("The object '" + objectKey +
                 "' does not include ACL information");
         }
         putObjectAclImpl(bucketName, objectKey, acl);
     }
 
     /**
      * Applies access control settings to a bucket. The ACL settings must be included
      * inside the bucket.
      * 
      * @param bucket
      * a bucket with ACL settings to apply.
      * @throws S3ServiceException
      */
     public void putBucketAcl(S3Bucket bucket) throws S3ServiceException {
         assertValidBucket(bucket, "Put Bucket Access Control List");
         putBucketAcl(bucket.getName(), bucket.getAcl());
     }
 
     public void putBucketAcl(String bucketName, AccessControlList acl) throws S3ServiceException {
         if (acl == null) {
             throw new S3ServiceException("The bucket '" + bucketName +
                 "' does not include ACL information");
         }
         putBucketAclImpl(bucketName, acl);
     }
 
     /**
      * Retrieves the access control settings of an object.
      * 
      * @param bucket
      * the bucket whose ACL settings will be retrieved (if objectKey is null) or the bucket containing the 
      * object whose ACL settings will be retrieved (if objectKey is non-null).
      * @param objectKey
      * if non-null, the key of the object whose ACL settings will be retrieved. Ignored if null.
      * @return
      * the ACL settings of the bucket or object.
      * @throws S3ServiceException
      */
     public AccessControlList getObjectAcl(S3Bucket bucket, String objectKey) throws S3ServiceException {
         assertValidBucket(bucket, "Get Object Access Control List");
         return getObjectAclImpl(bucket.getName(), objectKey);
     }
 
     public AccessControlList getObjectAcl(String bucketName, String objectKey) throws S3ServiceException {
         return getObjectAclImpl(bucketName, objectKey);
     }
 
     /**
      * Retrieves the access control settings of a bucket.
      * 
      * @param bucket
      * the bucket whose access control settings will be returned.
      * This must be a valid S3Bucket object that is non-null and contains a name.
      * @return
      * the ACL settings of the bucket.
      * @throws S3ServiceException
      */
     public AccessControlList getBucketAcl(S3Bucket bucket) throws S3ServiceException {
         assertValidBucket(bucket, "Get Bucket Access Control List");
         return getBucketAclImpl(bucket.getName());
     }
 
     public AccessControlList getBucketAcl(String bucketName) throws S3ServiceException {
         return getBucketAclImpl(bucketName);
     }
     
     /**
      * Retrieves the logging status settings of a bucket.
      * 
      * @param bucketName
      * the name of the bucket whose logging status settings will be returned.
      * @return
      * the Logging Status settings of the bucket.
      * @throws S3ServiceException
      */
     public S3BucketLoggingStatus getBucketLoggingStatus(String bucketName) throws S3ServiceException {
         return getBucketLoggingStatusImpl(bucketName);
     }
     
     /**
      * Applies logging settings to a bucket. 
      * 
      * @param bucketName
      * the name of the bucket the logging settings will apply to.
      * @param status
      * the logging status settings to apply to the bucket.
      * @throws S3ServiceException
      */
     public void setBucketLoggingStatus(String bucketName, S3BucketLoggingStatus status) 
         throws S3ServiceException
     {
         setBucketLoggingStatusImpl(bucketName, status);
     }
 
     // /////////////////////////////////////////////////////////////////////////////////
     // Abstract methods that must be implemented by interface-specific S3Service classes
     // /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Indicates whether a bucket exists and is accessible to a service user. 
      * <p>
      * This method can be performed by anonymous services.
      * <p>
      * <b>Implementation notes</b><p>
      * This method can be implemented by attempting to list the objects in a bucket. If the listing
      * is successful return true, if the listing failed for any reason return false. 
      * 
      * @return
      * true if the bucket exists and is accessible to the service user, false otherwise.
      * @throws S3ServiceException
      */
     public abstract boolean isBucketAccessible(String bucketName) throws S3ServiceException;
     
     public abstract S3BucketLoggingStatus getBucketLoggingStatusImpl(String bucketName) 
         throws S3ServiceException;
     
     public abstract void setBucketLoggingStatusImpl(String bucketName, S3BucketLoggingStatus status) 
         throws S3ServiceException;
     
     /**
      * 
      * @return
      * @throws S3ServiceException
      */
     protected abstract S3Bucket[] listAllBucketsImpl() throws S3ServiceException;
     
     /**
      * Lists objects in a bucket.
      * 
      * <b>Implementation notes</b><p>
      * The implementation of this method is expected to return <b>all</b> the objects
      * in a bucket, not a subset. This may require repeating the S3 list operation if the
      * first one doesn't include all the available objects, such as when the number of objects
      * is greater than <code>maxListingLength</code>.
      * <p>
      * 
      * @param bucketName
      * @param prefix
      * @param delimiter
      * @param maxListingLength
      * @return
      * @throws S3ServiceException
      */
     protected abstract S3Object[] listObjectsImpl(String bucketName, String prefix, 
         String delimiter, long maxListingLength) throws S3ServiceException;
 
     /**
      * Lists objects in a bucket up to the maximum listing length specified.
      * 
      * <b>Implementation notes</b><p>
      * The implementation of this method returns only as many objects as requested in the chunk
      * size. It is the responsibility of the caller to build a complete object listing from 
      * multiple chunks, should this be necessary.
      * <p>
      * 
      * @param bucketName
      * @param prefix
      * @param delimiter
      * @param maxListingLength
      * @param priorLastKey
      * @return
      * @throws S3ServiceException
      */
     protected abstract S3ObjectsChunk listObjectsChunkedImpl(String bucketName, String prefix, 
         String delimiter, long maxListingLength, String priorLastKey) throws S3ServiceException;
 
     /**
      * Creates a bucket.
      * 
      * <b>Implementation notes</b><p>
      * The implementing method must populate the bucket object's metadata with the results of the 
      * operation before returning the object. It must also apply any <code>AccessControlList</code> 
      * settings included with the bucket. 
      * 
      * @param bucketName
      * the name of the bucket to create.
      * @param acl
      * an access control object representing the initial acl values for the bucket. 
      * May be null, in which case the default permissions are applied.
      * @return
      * the created bucket object, populated with all metadata made available by the creation operation. 
      * @throws S3ServiceException
      */
     protected abstract S3Bucket createBucketImpl(String bucketName, AccessControlList acl) throws S3ServiceException;
 
     protected abstract void deleteBucketImpl(String bucketName) throws S3ServiceException;
 
     protected abstract S3Object putObjectImpl(String bucketName, S3Object object) throws S3ServiceException;
 
     protected abstract void deleteObjectImpl(String bucketName, String objectKey) throws S3ServiceException;
     
     protected abstract S3Object getObjectDetailsImpl(String bucketName, String objectKey,
         Calendar ifModifiedSince, Calendar ifUnmodifiedSince, String[] ifMatchTags,
         String[] ifNoneMatchTags) throws S3ServiceException;
 
     protected abstract S3Object getObjectImpl(String bucketName, String objectKey, Calendar ifModifiedSince,
         Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags,
         Long byteRangeStart, Long byteRangeEnd) throws S3ServiceException;
 
     protected abstract void putBucketAclImpl(String bucketName, AccessControlList acl) 
         throws S3ServiceException;
 
     protected abstract void putObjectAclImpl(String bucketName, String objectKey, AccessControlList acl) 
         throws S3ServiceException;
 
     protected abstract AccessControlList getObjectAclImpl(String bucketName, String objectKey)
         throws S3ServiceException;
 
     protected abstract AccessControlList getBucketAclImpl(String bucketName) throws S3ServiceException;
     
 }
