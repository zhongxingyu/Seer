 package org.jets3t.service.impl.soap.axis;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.activation.DataHandler;
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import javax.xml.rpc.ServiceException;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.axis.attachments.AttachmentPart;
 import org.apache.axis.attachments.SourceDataSource;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jets3t.service.Constants;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.acl.AccessControlList;
 import org.jets3t.service.acl.CanonicalGrantee;
 import org.jets3t.service.acl.EmailAddressGrantee;
 import org.jets3t.service.acl.GrantAndPermission;
 import org.jets3t.service.acl.GranteeInterface;
 import org.jets3t.service.acl.GroupGrantee;
 import org.jets3t.service.impl.soap.axis._2006_03_01.AccessControlPolicy;
 import org.jets3t.service.impl.soap.axis._2006_03_01.AmazonCustomerByEmail;
 import org.jets3t.service.impl.soap.axis._2006_03_01.AmazonS3SoapBindingStub;
 import org.jets3t.service.impl.soap.axis._2006_03_01.AmazonS3_ServiceLocator;
 import org.jets3t.service.impl.soap.axis._2006_03_01.BucketLoggingStatus;
 import org.jets3t.service.impl.soap.axis._2006_03_01.CanonicalUser;
 import org.jets3t.service.impl.soap.axis._2006_03_01.GetObjectResult;
 import org.jets3t.service.impl.soap.axis._2006_03_01.Grant;
 import org.jets3t.service.impl.soap.axis._2006_03_01.Grantee;
 import org.jets3t.service.impl.soap.axis._2006_03_01.Group;
 import org.jets3t.service.impl.soap.axis._2006_03_01.ListAllMyBucketsEntry;
 import org.jets3t.service.impl.soap.axis._2006_03_01.ListAllMyBucketsResult;
 import org.jets3t.service.impl.soap.axis._2006_03_01.ListBucketResult;
 import org.jets3t.service.impl.soap.axis._2006_03_01.ListEntry;
 import org.jets3t.service.impl.soap.axis._2006_03_01.LoggingSettings;
 import org.jets3t.service.impl.soap.axis._2006_03_01.MetadataEntry;
 import org.jets3t.service.impl.soap.axis._2006_03_01.Permission;
 import org.jets3t.service.impl.soap.axis._2006_03_01.PutObjectResult;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3BucketLoggingStatus;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.model.S3Owner;
 import org.jets3t.service.security.AWSCredentials;
 import org.jets3t.service.utils.Mimetypes;
 import org.jets3t.service.utils.ServiceUtils;
 
 /**
  * SOAP implementation of an {@link S3Service} based on the 
  * <a href="http://ws.apache.org/axis/">Apache Axis 1.4</a> library.  
  * <p>
  * <b>Note</b> This SOAP implementation does <b>not</b> support IO streaming uploads to S3. Any 
  * documents uploaded by this implementation must fit inside memory allocated to the Java program
  * running this class if OutOfMemory errors are to be avoided. 
  * 
  * @author James Murty
  */
 public class SoapS3Service extends S3Service {
     private final Log log = LogFactory.getLog(SoapS3Service.class);
     private AmazonS3_ServiceLocator locator = null;
 
     /**
      * Constructs the SOAP service implementation and, based on the value of {@link S3Service#isHttpsOnly}
      * sets the SOAP endpoint to use HTTP or HTTPS protocols.
      * 
      * @param awsCredentials
      * @throws S3ServiceException
      */
     public SoapS3Service(AWSCredentials awsCredentials) throws S3ServiceException {
         super(awsCredentials);
         
         locator = new AmazonS3_ServiceLocator();
         if (super.isHttpsOnly()) {
             // Use an SSL connection, to further secure the signature. 
             log.debug("SOAP service will use HTTPS for all communication");
             locator.setAmazonS3EndpointAddress( locator.getAmazonS3Address().replaceAll( "http:", "https:" ) );
         }
         // Ensure we can get the stub.
         getSoapBinding();
     }
     
     private AmazonS3SoapBindingStub getSoapBinding() throws S3ServiceException {
         try {
             return (AmazonS3SoapBindingStub) locator.getAmazonS3();
         } catch (ServiceException e) {
             throw new S3ServiceException("Unable to initialise SOAP binding", e);
         }
     }
     
     private String getAWSAccessKey() {
         if (getAWSCredentials() == null) {
             return null;
         } else {
             return getAWSCredentials().getAccessKey();
         }
     }
     
     /**
      * Creates a HMAC/SHA1 signature for the given method and time.
      * 
      * @param method
      * @param timestamp
      * @return
      * @throws ParseException
      */
     private String makeSignature(String method, Calendar timestamp) throws ParseException {
         if (getAWSCredentials() == null) {
             return null;
         }
         String canonicalString = Constants.SOAP_SERVICE_NAME + method 
             + convertDateToString(timestamp.getTimeInMillis());
 
         // The following HMAC/SHA1 code for the signature is taken from the
         // AWS Platform's implementation of RFC2104
         // (amazon.webservices.common.Signature)
         //
         // Acquire an HMAC/SHA1 from the raw key bytes.
         SecretKeySpec signingKey = new SecretKeySpec(
             getAWSCredentials().getSecretKey().getBytes(), Constants.HMAC_SHA1_ALGORITHM);
 
         // Acquire the MAC instance and initialize with the signing key; the exceptions
         // are unlikely.
         Mac mac = null;
         try {
             mac = Mac.getInstance(Constants.HMAC_SHA1_ALGORITHM);
             mac.init(signingKey);
         } catch ( NoSuchAlgorithmException nsae ) {
             throw new ParseException( nsae.getMessage(), 0 );
         } catch ( InvalidKeyException ike ) {
             throw new ParseException( ike.getMessage(), 0 );
         }
 
         // Compute the HMAC on the digest, and set it.
         byte[] b64 = Base64.encodeBase64(mac.doFinal(canonicalString.getBytes())); 
         return new String(b64);
     }
 
     private Calendar getTimeStamp( long timestamp ) throws ParseException {
         if (getAWSCredentials() == null) {
             return null;
         }
         Calendar ts = new GregorianCalendar();
         Date date = ServiceUtils.parseIso8601Date(convertDateToString(timestamp));
         ts.setTime(date);
         return ts;
     }
 
     private String convertDateToString(long time) {
         return ServiceUtils.formatIso8601Date(new Date(time));
     }
     
     private S3Owner convertOwner(CanonicalUser user) {
         S3Owner owner = new S3Owner(user.getID(), user.getDisplayName());
         return owner;
     }
     
     /**
      * Converts a SOAP object AccessControlPolicy to a jets3t AccessControlList.
      * 
      * @param policy
      * @return
      * @throws S3ServiceException
      */
     private AccessControlList convertAccessControlTypes(AccessControlPolicy policy) 
         throws S3ServiceException 
     {
         AccessControlList acl = new AccessControlList();
         acl.setOwner(convertOwner(policy.getOwner()));
         
         Grant[] grants = policy.getAccessControlList();
         for (int i = 0; i < grants.length; i++) {
             Grant grant = (Grant) grants[i];
             org.jets3t.service.acl.Permission permission =
                 org.jets3t.service.acl.Permission.parsePermission(grant.getPermission().toString());            
             
             Grantee grantee = grant.getGrantee();
             if (grantee instanceof Group) {
                 GroupGrantee jets3tGrantee = new GroupGrantee();
                 jets3tGrantee.setIdentifier(((Group)grantee).getURI());                
                 acl.grantPermission(jets3tGrantee, permission);                
             } else if (grantee instanceof CanonicalUser) {
                 CanonicalUser canonicalUser = (CanonicalUser) grantee;
                 CanonicalGrantee jets3tGrantee = new CanonicalGrantee();
                 jets3tGrantee.setIdentifier(canonicalUser.getID());
                 jets3tGrantee.setDisplayname(canonicalUser.getDisplayName());
                 acl.grantPermission(jets3tGrantee, permission);                
             } else if (grantee instanceof AmazonCustomerByEmail) {
                 AmazonCustomerByEmail customerByEmail = (AmazonCustomerByEmail) grantee;
                 EmailAddressGrantee jets3tGrantee = new EmailAddressGrantee();
                 jets3tGrantee.setIdentifier(customerByEmail.getEmailAddress());
                 acl.grantPermission(jets3tGrantee, permission);                
             } else {
                 throw new S3ServiceException("Unrecognised grantee type: " + grantee.getClass());
             }
         }
         return acl;
     }
     
     /**
      * Converts a jets3t AccessControlList object to an array of SOAP Grant objects.
      * 
      * @param acl
      * @return
      * @throws S3ServiceException
      */
     private Grant[] convertACLtoGrants(AccessControlList acl) throws S3ServiceException {
         if (acl == null) {
             return null;
         }
         if (acl.isCannedRestACL()) {
             throw new S3ServiceException("Cannot use canned REST ACLs with SOAP service");        
         }        
         
         Grant[] grants = new Grant[acl.getGrants().size()];
             
         Iterator grantIter = acl.getGrants().iterator();
         int index = 0;
         while (grantIter.hasNext()) {
             GrantAndPermission jets3tGaP = (GrantAndPermission) grantIter.next();
             GranteeInterface jets3tGrantee = jets3tGaP.getGrantee();
             Grant grant = new Grant();
             
             if (jets3tGrantee instanceof GroupGrantee) {
                 GroupGrantee groupGrantee = (GroupGrantee) jets3tGrantee;
                 Group group = new Group();
                 group.setURI(groupGrantee.getIdentifier());
                 grant.setGrantee(group);
             } else if (jets3tGrantee instanceof CanonicalGrantee) {
                 CanonicalGrantee canonicalGrantee = (CanonicalGrantee) jets3tGrantee;
                 CanonicalUser canonicalUser = new CanonicalUser();
                 canonicalUser.setID(canonicalGrantee.getIdentifier());
                 canonicalUser.setDisplayName(canonicalGrantee.getDisplayName());
                 grant.setGrantee(canonicalUser);
             } else if (jets3tGrantee instanceof EmailAddressGrantee) {
                 EmailAddressGrantee emailGrantee = (EmailAddressGrantee) jets3tGrantee;
                 AmazonCustomerByEmail customerByEmail = new AmazonCustomerByEmail();
                 customerByEmail.setEmailAddress(emailGrantee.getIdentifier());
                 grant.setGrantee(customerByEmail);
             } else {
                 throw new S3ServiceException("Unrecognised jets3t grantee type: " 
                     + jets3tGrantee.getClass());
             }
             Permission permission = Permission.fromString(jets3tGaP.getPermission().toString());
             grant.setPermission(permission);
             grants[index++] = grant;
         }
         return grants;
     }
 
     /**
      * Converts metadata information from a standard map to SOAP objects.
      * 
      * @param metadataMap
      * @return
      */
     private MetadataEntry[] convertMetadata(Map metadataMap) {
         MetadataEntry[] metadata = new MetadataEntry[metadataMap.size()];
         Iterator keyIter = metadataMap.keySet().iterator();
         int index = 0;
         while (keyIter.hasNext()) {
             Object metadataName = keyIter.next();
             Object metadataValue = metadataMap.get(metadataName);
             log.debug("Setting metadata: " + metadataName + "=" + metadataValue);
             MetadataEntry entry = new MetadataEntry();
             entry.setName(metadataName.toString());
             entry.setValue(metadataValue.toString());
             metadata[index++] = entry;
         }
         return metadata;
     }
     
     ////////////////////////////////////////////////////////////////
     // Methods below this point implement S3Service abstract methods
     ////////////////////////////////////////////////////////////////    
     
     public S3Bucket[] listAllBucketsImpl() throws S3ServiceException {
         log.debug("Listing all buckets for AWS user: " + getAWSCredentials().getAccessKey());
         
         S3Bucket[] buckets = null;
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("ListAllMyBuckets", timestamp);
             ListAllMyBucketsResult result = s3SoapBinding.listAllMyBuckets(
                 getAWSAccessKey(), timestamp, signature);
 
             ListAllMyBucketsEntry[] entries = result.getBuckets();            
             buckets = new S3Bucket[entries.length];
             int index = 0;
             for (int i = 0; i < entries.length; i++) {
                 ListAllMyBucketsEntry entry = (ListAllMyBucketsEntry) entries[i];
                 S3Bucket bucket = new S3Bucket();
                 bucket.setName(entry.getName());
                 bucket.setCreationDate(entry.getCreationDate().getTime());
                 buckets[index++] = bucket;
             }
         } catch (Exception e) {
             throw new S3ServiceException("Unable to List Buckets", e);
         }
         return buckets;
     }
 
     public boolean isBucketAccessible(String bucketName) throws S3ServiceException {
         log.debug("Checking existence of bucket: " + bucketName);
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("ListBucket", timestamp);            
             
             s3SoapBinding.listBucket(
                 bucketName, null, null, new Integer(0), 
                 null, getAWSAccessKey(), timestamp, signature, null);
             
             // If we get this far, the bucket exists.
             return true;
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Get Bucket: " + bucketName, e);   
         }
     }
 
     public S3Object[] listObjectsImpl(String bucketName, String prefix, String delimiter, long maxListingLength)
         throws S3ServiceException
     {
         String marker = null;
         ArrayList objects = new ArrayList();        
         boolean incompleteListing = true;            
 
         try {
             while (incompleteListing) {
                 AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
                 Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
                 String signature = makeSignature("ListBucket", timestamp);
                 ListBucketResult result = s3SoapBinding.listBucket(
                     bucketName, prefix, marker, new Integer((int)maxListingLength), 
                     delimiter, getAWSAccessKey(), timestamp, signature, null);
                 
                 ListEntry[] entries = result.getContents();
                S3Object[] partialObjects = new S3Object[entries.length];
                 
                 log.debug("Found " + partialObjects.length + " objects in one batch");
                for (int i = 0; i < entries.length; i++) {
                     ListEntry entry = entries[i];
                     S3Object object = new S3Object();
                     object.setKey(entry.getKey());
                     object.setLastModifiedDate(entry.getLastModified().getTime());
                     object.setETag(entry.getETag());
                     object.setContentLength(entry.getSize());
                     object.setStorageClass(entry.getStorageClass().toString());
                     object.setOwner(convertOwner(entry.getOwner()));
                     partialObjects[i] = object;
                     
                     // This shouldn't be necessary, but result.getMarker() doesn't work as expected.
                     marker = object.getKey();
                 }
                 
                 objects.addAll(Arrays.asList(partialObjects));
                 
                 incompleteListing = result.isIsTruncated();
                 if (incompleteListing) {
                     // Why doesn't result.getMarker() return the next marker?
                     // marker = result.getMarker();
                     log.debug("Yet to receive complete listing of bucket contents, "
                             + "querying for next batch of objects with marker: " + marker);
                 }
             }
         } catch (Exception e) {
             throw new S3ServiceException("Unable to List Objects in bucket: " + bucketName, e);   
         }
         log.debug("Found " + objects.size() + " objects in total");
         return (S3Object[]) objects.toArray(new S3Object[] {});        
     }
 
     public S3Bucket createBucketImpl(String bucketName, AccessControlList acl) throws S3ServiceException {
         Grant[] grants = null;
         if (acl != null) {
             grants = convertACLtoGrants(acl);        
         }
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("CreateBucket", timestamp);
             s3SoapBinding.createBucket(
                 bucketName, grants, getAWSAccessKey(), timestamp, signature);
             
             S3Bucket bucket = new S3Bucket(bucketName);
             bucket.setAcl(acl);
             return bucket;            
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Create Bucket: " + bucketName, e);   
         }
     }
     
     public void deleteBucketImpl(String bucketName) throws S3ServiceException {
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("DeleteBucket", timestamp);
             s3SoapBinding.deleteBucket(
                 bucketName, getAWSAccessKey(), timestamp, signature, null);
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Delete Bucket: " + bucketName, e);   
         }            
     }
 
     public S3Object putObjectImpl(String bucketName, S3Object object) throws S3ServiceException {
         log.debug("Creating Object with key " + object.getKey() + " in bucket " + bucketName);        
 
         Grant[] grants = null;
         if (object.getAcl() != null) {
             grants = convertACLtoGrants(object.getAcl());
         }
         MetadataEntry[] metadata = convertMetadata(object.getMetadata());
         
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             long contentLength = object.getContentLength();
             String contentType = object.getContentType();
             if (contentType == null) {
                 // Set default content type.
                 contentType = Mimetypes.MIMETYPE_OCTET_STREAM;
             }
             
             if (object.getDataInputStream() != null) {                
                 log.debug("Uploading data input stream for S3Object: " + object.getKey());
                 
                 if (contentLength == 0 && object.getDataInputStream().available() > 0) {
                     
                     log.warn("S3Object " + object.getKey() 
                         + " - Content-Length was set to 0 despite having a non-empty data"
                         + " input stream. The Content-length will be determined in memory.");
                     
                     // Read all data into memory to determine it's length.
                     BufferedInputStream bis = new BufferedInputStream(object.getDataInputStream());
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     BufferedOutputStream bos = new BufferedOutputStream(baos);
                     byte[] buffer = new byte[8192];
                     int read = -1;
                     while ((read = bis.read(buffer)) != -1) {
                         bos.write(buffer, 0, read);
                     }
                     bis.close();
                     bos.close();
 
                     contentLength = baos.size();
                     object.setDataInputStream(new ByteArrayInputStream(baos.toByteArray()));
                     
                     log.debug("Content-Length value has been reset to " + contentLength);
                 }                
                 
                 DataHandler dataHandler = new DataHandler(
                     new SourceDataSource(
                         null, contentType, new StreamSource(object.getDataInputStream())));           
                 s3SoapBinding.addAttachment(dataHandler);                
             } else {
                 DataHandler dataHandler = new DataHandler(
                     new SourceDataSource(
                         null, contentType, new StreamSource()));
                 s3SoapBinding.addAttachment(dataHandler);                
             }
             
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("PutObject", timestamp);
             PutObjectResult result = 
                 s3SoapBinding.putObject(bucketName, object.getKey(), metadata, 
                     contentLength, grants, null, getAWSAccessKey(), 
                     timestamp, signature, null);
             
             object.setETag(result.getETag());
             object.setContentLength(contentLength);
             object.setContentType(contentType);
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Create Object: " + object.getKey(), e);   
         }
         return object;
     }
 
     public void deleteObjectImpl(String bucketName, String objectKey) throws S3ServiceException {
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("DeleteObject", timestamp);
             s3SoapBinding.deleteObject(bucketName, objectKey, 
                 getAWSAccessKey(), timestamp, signature, null);
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Delete Object: " + objectKey, e);   
         } 
     }
 
     public S3Object getObjectDetailsImpl(String bucketName, String objectKey, Calendar ifModifiedSince, 
         Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags) 
         throws S3ServiceException
     {
         return getObjectImpl(false, bucketName, objectKey, ifModifiedSince, ifUnmodifiedSince,
             ifMatchTags, ifNoneMatchTags, null, null);
     }
     
     public S3Object getObjectImpl(String bucketName, String objectKey, Calendar ifModifiedSince, 
         Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags, 
         Long byteRangeStart, Long byteRangeEnd)
         throws S3ServiceException 
     {
         return getObjectImpl(true, bucketName, objectKey, ifModifiedSince, ifUnmodifiedSince,
             ifMatchTags, ifNoneMatchTags, byteRangeStart, byteRangeEnd);
     }    
 
     private S3Object getObjectImpl(boolean withData, String bucketName, String objectKey, 
         Calendar ifModifiedSince, Calendar ifUnmodifiedSince, String[] ifMatchTags, 
         String[] ifNoneMatchTags, Long byteRangeStart, Long byteRangeEnd)
         throws S3ServiceException
     {
         boolean useExtendedGet = 
             ifModifiedSince != null || ifUnmodifiedSince != null
             || ifMatchTags != null || ifNoneMatchTags != null 
             || byteRangeStart != null || byteRangeEnd != null;
         
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             GetObjectResult result = null;
             
             if (useExtendedGet) {
                 log.debug("Using Extended GET to apply constraints: "
                     + "ifModifiedSince=" + (ifModifiedSince != null? ifModifiedSince.getTime().toString() : "null")
                     + ", ifUnmodifiedSince=" + (ifUnmodifiedSince != null? ifUnmodifiedSince.getTime().toString() : "null")
                     + ", ifMatchTags=" + (ifMatchTags != null? Arrays.asList(ifMatchTags).toString() : "null") 
                     + ", ifNoneMatchTags=" + (ifNoneMatchTags != null? Arrays.asList(ifNoneMatchTags).toString() : "null")
                     + ", byteRangeStart=" + byteRangeStart + ", byteRangeEnd=" + byteRangeEnd);
                 
                 String signature = makeSignature("GetObjectExtended", timestamp);
                 result = s3SoapBinding.getObjectExtended(
                     bucketName, objectKey, true, true, false, byteRangeStart, byteRangeEnd,
                     ifModifiedSince, ifUnmodifiedSince, ifMatchTags, ifNoneMatchTags,
                     Boolean.FALSE, getAWSAccessKey(), timestamp, signature, null);
                 
                 // Throw an exception if the preconditions failed.
                 int expectedStatusCode = 200;
                 if (byteRangeStart != null || byteRangeEnd != null) {
                     // Partial data responses have a status code of 206. 
                     expectedStatusCode = 206;
                 }
                 if (result.getStatus().getCode() != expectedStatusCode) {
                     throw new S3ServiceException("Precondition failed when getting object "
                         + objectKey + ": " + result.getStatus().getDescription());
                 }
             } else {
                 log.debug("Using standard GET (no constraints to apply)");
                 String signature = makeSignature("GetObject", timestamp);
                 result = s3SoapBinding.getObject(
                     bucketName, objectKey, true, true, false,                
                     getAWSAccessKey(), timestamp, signature, null);                
             }
             
             S3Object object = new S3Object();
             object.setETag(result.getETag());
             object.setLastModifiedDate(result.getLastModified().getTime());
             object.setBucketName(bucketName);
             object.setKey(objectKey);
             
             // Get data details from the SOAP attachment.
             Object[] attachments = s3SoapBinding.getAttachments();
             log.debug("SOAP attachment count for " + object.getKey() + ": " + attachments.length);
             for (int i = 0; i < attachments.length; i++) {
                 if (i > 0) {
                     throw new S3ServiceException(
                         "Received multiple SOAP attachment parts, this shouldn't happen");
                 }
                 AttachmentPart part = (AttachmentPart) attachments[i];
                 
                 object.setContentType(part.getContentType());
                 object.setContentLength(part.getSize());
                 if (withData) {
                     object.setDataInputStream(part.getDataHandler().getInputStream());
                 } else {
                     part.getDataHandler().getInputStream().close();
                     part.clearContent();
                 }
             }
             
             // Populate object's metadata details.
             MetadataEntry[] metadata = result.getMetadata();
             for (int i = 0; i < metadata.length; i++) {
                 MetadataEntry entry = metadata[i];
                 object.addMetadata(entry.getName(), entry.getValue());
             }
             object.setMetadataComplete(true);
             
             return object;
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Get Object: " + objectKey, e);   
         } 
     }
 
     public void putObjectAclImpl(String bucketName, String objectKey, AccessControlList acl) 
         throws S3ServiceException 
     {
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             Grant[] grants = convertACLtoGrants(acl);
                 
             String signature = makeSignature("SetObjectAccessControlPolicy", timestamp);
             s3SoapBinding.setObjectAccessControlPolicy(bucketName, objectKey, grants, 
                 getAWSAccessKey(), timestamp, signature, null);
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Put Object ACL", e);   
         }
     }
 
     public void putBucketAclImpl(String bucketName, AccessControlList acl) 
         throws S3ServiceException 
     {
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             Grant[] grants = convertACLtoGrants(acl);
                 
             String signature = makeSignature("SetBucketAccessControlPolicy", timestamp);
             s3SoapBinding.setBucketAccessControlPolicy(bucketName, grants, 
                 getAWSAccessKey(), timestamp, signature, null);
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Put Bucket ACL", e);   
         }        
     }
 
     public AccessControlList getObjectAclImpl(String bucketName, String objectKey) throws S3ServiceException {
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("GetObjectAccessControlPolicy", timestamp);
             AccessControlPolicy result = s3SoapBinding.getObjectAccessControlPolicy(
                 bucketName, objectKey, getAWSAccessKey(), 
                 timestamp, signature, null);
             return convertAccessControlTypes(result);
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Get ACL", e);   
         }
     }
 
     public AccessControlList getBucketAclImpl(String bucketName) throws S3ServiceException {
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("GetBucketAccessControlPolicy", timestamp);
             AccessControlPolicy result = s3SoapBinding.getBucketAccessControlPolicy(bucketName, 
                 getAWSAccessKey(), timestamp, signature, null);
             return convertAccessControlTypes(result);
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Get ACL", e);   
         }
     }
 
     public S3BucketLoggingStatus getBucketLoggingStatusImpl(String bucketName) throws S3ServiceException {
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("GetBucketLoggingStatus", timestamp);
             
             BucketLoggingStatus loggingStatus = s3SoapBinding.getBucketLoggingStatus(                
                 bucketName, getAWSAccessKey(), timestamp, signature, null);            
             LoggingSettings loggingSettings = loggingStatus.getLoggingEnabled();
             if (loggingSettings != null) {
                 return new S3BucketLoggingStatus(loggingSettings.getTargetBucket(), loggingSettings.getTargetPrefix());                
             } else {
                 return new S3BucketLoggingStatus();
             }
         } catch (Exception e) {
            throw new S3ServiceException("Unable to Get ACL", e);   
         }        
     }
 
     public void setBucketLoggingStatusImpl(String bucketName, S3BucketLoggingStatus status) throws S3ServiceException {
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("SetBucketLoggingStatus", timestamp);
             
             LoggingSettings loggingSettings = null;
             if (status.isLoggingEnabled()) {
                 loggingSettings = new LoggingSettings(status.getTargetBucketName(), status.getLogfilePrefix());                
             }
             
             s3SoapBinding.setBucketLoggingStatus(
                 bucketName, getAWSAccessKey(), timestamp, signature, null, 
                 new BucketLoggingStatus(loggingSettings)); 
         } catch (Exception e) {
            throw new S3ServiceException("Unable to Get ACL", e);   
         }        
     }
 
 }
