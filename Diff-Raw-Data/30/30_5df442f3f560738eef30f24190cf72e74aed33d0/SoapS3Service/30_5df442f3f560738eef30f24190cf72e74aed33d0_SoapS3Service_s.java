 package org.jets3t.service.impl.soap.axis;
 
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
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
 import org.jets3t.service.impl.soap.axis._2006_03_01.CanonicalUser;
 import org.jets3t.service.impl.soap.axis._2006_03_01.GetObjectResult;
 import org.jets3t.service.impl.soap.axis._2006_03_01.Grant;
 import org.jets3t.service.impl.soap.axis._2006_03_01.Grantee;
 import org.jets3t.service.impl.soap.axis._2006_03_01.Group;
 import org.jets3t.service.impl.soap.axis._2006_03_01.ListAllMyBucketsEntry;
 import org.jets3t.service.impl.soap.axis._2006_03_01.ListAllMyBucketsList;
 import org.jets3t.service.impl.soap.axis._2006_03_01.ListAllMyBucketsResult;
 import org.jets3t.service.impl.soap.axis._2006_03_01.ListBucketResult;
 import org.jets3t.service.impl.soap.axis._2006_03_01.ListEntry;
 import org.jets3t.service.impl.soap.axis._2006_03_01.MetadataEntry;
 import org.jets3t.service.impl.soap.axis._2006_03_01.PutObjectResult;
 import org.jets3t.service.impl.soap.axis._2006_03_01.types.Permission;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.model.S3Owner;
 import org.jets3t.service.security.AWSCredentials;
 import org.jets3t.service.utils.Mimetypes;
 import org.jets3t.service.utils.ServiceUtils;
 
 import sun.misc.BASE64Encoder;
 
 public class SoapS3Service extends S3Service {
     private final Log log = LogFactory.getLog(SoapS3Service.class);
     private AmazonS3_ServiceLocator locator = null;
 
     public SoapS3Service(AWSCredentials awsCredentials) throws S3ServiceException {
         super(awsCredentials);
         
         locator = new AmazonS3_ServiceLocator();
         // Use an SSL connection, to further secure the signature. 
         locator.setAmazonS3EndpointAddress( locator.getAmazonS3Address().replaceAll( "http:", "https:" ) );
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
         BASE64Encoder encoder = new BASE64Encoder();
         return encoder.encode(mac.doFinal(canonicalString.getBytes()));
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
     
     private AccessControlList convertAccessControlTypes(AccessControlPolicy policy) 
         throws S3ServiceException 
     {
         AccessControlList acl = new AccessControlList();
         acl.setOwner(convertOwner(policy.getOwner()));
         
         Enumeration enumeration = policy.getAccessControlList().enumerateGrant();
         while (enumeration.hasMoreElements()) {
             Grant grant = (Grant) enumeration.nextElement();
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
             Permission permission = Permission.valueOf(jets3tGaP.getPermission().toString());
             grant.setPermission(permission);
             grants[index++] = grant;
         }
         return grants;
     }
 
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
     
     
     public S3Bucket[] listAllBuckets() throws S3ServiceException {
         assertAuthenticatedConnection("List all buckets");
         log.debug("Listing all buckets for AWS user: " + getAWSCredentials().getAccessKey());
         
         S3Bucket[] buckets = null;
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("ListAllMyBuckets", timestamp);
             ListAllMyBucketsResult result = s3SoapBinding.listAllMyBuckets(
                 getAWSAccessKey(), timestamp, signature);
 
             ListAllMyBucketsList list = result.getBuckets();            
             buckets = new S3Bucket[list.getBucketCount()];
             Enumeration enumeration = list.enumerateBucket();
             int index = 0;
             while (enumeration.hasMoreElements()) {
                 ListAllMyBucketsEntry entry = (ListAllMyBucketsEntry) enumeration.nextElement();
                 S3Bucket bucket = new S3Bucket();
                 bucket.setName(entry.getName());
                 bucket.setCreationDate(entry.getCreationDate());
                 buckets[index++] = bucket;
             }
         } catch (Exception e) {
             throw new S3ServiceException("Unable to List Buckets", e);
         }
         return buckets;
     }
 
     public boolean isBucketAvailable(String bucketName) throws S3ServiceException {
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
 
     public S3Object[] listObjects(S3Bucket bucket, String prefix, String delimiter, long maxListingLength)
         throws S3ServiceException
     {
         assertValidBucket(bucket, "List objects in bucket");
 
         String marker = null;
         ArrayList objects = new ArrayList();        
         boolean incompleteListing = true;            
 
         try {
             while (incompleteListing) {
                 AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
                 Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
                 String signature = makeSignature("ListBucket", timestamp);
                 ListBucketResult result = s3SoapBinding.listBucket(
                     bucket.getName(), prefix, marker, new Integer((int)maxListingLength), 
                     delimiter, getAWSAccessKey(), timestamp, signature, null);
                 
                 S3Object[] partialObjects = new S3Object[result.getContentsCount()];
                 ListEntry[] entries = result.getContents();
                 
                 log.debug("Found " + partialObjects.length + " objects in one batch");
                 for (int i = 0; i < entries.length; i++) {
                     ListEntry entry = entries[i];
                     S3Object object = new S3Object();
                     object.setKey(entry.getKey());
                     object.setLastModifiedDate(entry.getLastModified());
                     object.setETag(entry.getETag());
                     object.setContentLength(entry.getSize());
                     object.setStorageClass(entry.getStorageClass().toString());
                     object.setOwner(convertOwner(entry.getOwner()));
                     partialObjects[i] = object;
                     
                     // This shouldn't be necessary, but result.getMarker() doesn't work as expected.
                     marker = object.getKey();
                 }
                 
                 objects.addAll(Arrays.asList(partialObjects));
                 
                 incompleteListing = result.getIsTruncated();
                 if (incompleteListing) {
                     // Why doesn't result.getMarker() return the next marker?
                     // marker = result.getMarker();
                     log.debug("Yet to receive complete listing of bucket contents, "
                             + "querying for next batch of objects with marker: " + marker);
                 }
             }
         } catch (Exception e) {
             throw new S3ServiceException("Unable to List Objects in bucket: " + bucket.getName(), e);   
         }
         log.debug("Found " + objects.size() + " objects in total");
         return (S3Object[]) objects.toArray(new S3Object[] {});        
     }
 
     public S3Bucket createBucket(S3Bucket bucket) throws S3ServiceException {
         assertValidBucket(bucket, "Create bucket");        
         log.debug("Creating bucket with name: " + bucket.getName());
         
         Grant[] grants = null;
         if (bucket.getAcl() != null) {
             grants = convertACLtoGrants(bucket.getAcl());        
         }
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("CreateBucket", timestamp);
             s3SoapBinding.createBucket(
                 bucket.getName(), grants, getAWSAccessKey(), timestamp, signature);
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Create Bucket: " + bucket.getName(), e);   
         }
         return bucket;
     }
     
     public void deleteBucket(String bucketName) throws S3ServiceException {
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
 
     public S3Object putObject(S3Bucket bucket, S3Object object) throws S3ServiceException {
         assertValidBucket(bucket, "Create Object in bucket");
         assertValidObject(object, "Create Object in bucket " + bucket.getName());        
         log.debug("Creating Object with key " + object.getKey() + " in bucket " + bucket.getName());        
 
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
                    contentLength = object.getDataInputStream().available();
                     log.warn("S3Object Content-Length was set to 0 despite having a non-empty data"
                        + " input stream. The Content-length value has been reset to " + contentLength);
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
                 s3SoapBinding.putObject(bucket.getName(), object.getKey(), metadata, 
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
 
     public void deleteObject(S3Bucket bucket, String objectKey) throws S3ServiceException {
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             String signature = makeSignature("DeleteObject", timestamp);
             s3SoapBinding.deleteObject(bucket.getName(), objectKey, 
                 getAWSAccessKey(), timestamp, signature, null);
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Delete Object: " + objectKey, e);   
         } 
     }
 
     public S3Object getObjectDetails(S3Bucket bucket, String objectKey, Calendar ifModifiedSince, 
         Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags) 
         throws S3ServiceException
     {
         return getObjectImpl(false, bucket, objectKey, ifModifiedSince, ifUnmodifiedSince,
             ifMatchTags, ifNoneMatchTags, null, null);
     }
     
     public S3Object getObject(S3Bucket bucket, String objectKey, Calendar ifModifiedSince, 
         Calendar ifUnmodifiedSince, String[] ifMatchTags, String[] ifNoneMatchTags, 
         Long byteRangeStart, Long byteRangeEnd)
         throws S3ServiceException 
     {
         return getObjectImpl(true, bucket, objectKey, ifModifiedSince, ifUnmodifiedSince,
             ifMatchTags, ifNoneMatchTags, byteRangeStart, byteRangeEnd);
     }    
 
     private S3Object getObjectImpl(boolean withData, S3Bucket bucket, String objectKey, 
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
                     bucket.getName(), objectKey, true, true, false, byteRangeStart, byteRangeEnd,
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
                     bucket.getName(), objectKey, true, true, false,                
                     getAWSAccessKey(), timestamp, signature, null);                
             }
             
             S3Object object = new S3Object();
             object.setETag(result.getETag());
             object.setLastModifiedDate(result.getLastModified());
             object.setBucket(bucket);
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
 
     public void putAcl(S3Bucket bucket, S3Object object) throws S3ServiceException {
         assertValidBucket(bucket, "Set Access Control List");
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             Grant[] grants = null;
             if (object != null) {
                 if (object.getAcl() != null) {
                     grants = convertACLtoGrants(object.getAcl());
                 }
                 
                 String signature = makeSignature("SetObjectAccessControlPolicy", timestamp);
                 s3SoapBinding.setObjectAccessControlPolicy(bucket.getName(), object.getKey(), grants, 
                     getAWSAccessKey(), timestamp, signature, null);
             } else {
                 if (bucket.getAcl() != null) {
                     grants = convertACLtoGrants(bucket.getAcl());
                 }
     
                 String signature = makeSignature("SetBucketAccessControlPolicy", timestamp);
                 s3SoapBinding.setBucketAccessControlPolicy(bucket.getName(), grants, 
                     getAWSAccessKey(), timestamp, signature, null);
             }
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Put ACL", e);   
         }
     }
 
     public AccessControlList getAcl(S3Bucket bucket, String objectKey) throws S3ServiceException {
         assertValidBucket(bucket, "Get Access Control List");
         try {
             AmazonS3SoapBindingStub s3SoapBinding = getSoapBinding();
             Calendar timestamp = getTimeStamp( System.currentTimeMillis() );
             AccessControlPolicy result = null;
             if (objectKey != null)
             {
                 String signature = makeSignature("GetObjectAccessControlPolicy", timestamp);
                 result = s3SoapBinding.getObjectAccessControlPolicy(
                     bucket.getName(), objectKey, getAWSAccessKey(), 
                     timestamp, signature, null);
             }
             else
             {
                 String signature = makeSignature("GetBucketAccessControlPolicy", timestamp);
                 result = s3SoapBinding.getBucketAccessControlPolicy(bucket.getName(), 
                     getAWSAccessKey(), timestamp, signature, null);
             }
             return convertAccessControlTypes(result);
         } catch (Exception e) {
             throw new S3ServiceException("Unable to Get ACL", e);   
         }
     }
     
 }
