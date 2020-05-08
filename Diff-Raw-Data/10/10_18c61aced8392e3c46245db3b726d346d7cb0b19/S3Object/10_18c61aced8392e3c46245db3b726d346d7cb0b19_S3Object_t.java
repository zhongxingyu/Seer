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
 package org.jets3t.service.model;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 
 import org.jets3t.service.Constants;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.acl.AccessControlList;
 import org.jets3t.service.utils.Mimetypes;
 import org.jets3t.service.utils.ServiceUtils;
 
 /**
  * An S3 object.
  * 
  * @author James Murty
  */
 public class S3Object extends BaseS3Object {
     /*
      * Listing of the metadata names that are required in S3 objects, or are used frequently
      * in jets3t applications.  
      */
 	public static final String METADATA_HEADER_LAST_MODIFIED_DATE = "Last-Modified";
 	public static final String METADATA_HEADER_DATE = "Date";
 	public static final String METADATA_HEADER_OWNER = "Owner";	
 	public static final String METADATA_HEADER_ETAG = "ETag";	
     public static final String METADATA_HEADER_HASH_MD5 = "md5-hash";   
     public static final String METADATA_HEADER_CONTENT_MD5 = "Content-MD5";   
 	public static final String METADATA_HEADER_CONTENT_LENGTH = "Content-Length";	
 	public static final String METADATA_HEADER_CONTENT_TYPE = "Content-Type";	
     public static final String METADATA_HEADER_CONTENT_ENCODING = "Content-Encoding";   
 	public static final String METADATA_HEADER_STORAGE_CLASS = "Storage-Class";	
     public static final String METADATA_HEADER_CONTENT_DISPOSITION = "Content-Disposition";   
     public static final String METADATA_HEADER_CONTENT_LANGUAGE = "Content-Language";   
 	
 	private String key = null;
 	private String bucketName = null;
 	private InputStream dataInputStream = null;
 	private AccessControlList acl = null;
     private boolean isMetadataComplete = false;
     
     /**
      * Store references to files when the object's data comes from a file, to allow for lazy
      * opening of the file's input streams.
      */
     private File dataInputFile = null;
     
     
     /**
      * Create an object representing a file. The object is initialised with the file's name
      * as its key, the file's content as its data, a content type based on the file's extension
      * (see {@link Mimetypes}), and a content length matching the file's size.
      * 
      * @param bucket
      * the bucket the object belongs to, or will be placed in.
      * @param file
      * the file the object will represent. This file must exist and be readable.
      * 
      * @throws FileNotFoundException
      */
     public S3Object(S3Bucket bucket, File file) throws FileNotFoundException {
         this(bucket, file.getName());
         setContentLength(file.length());
         setContentType(Mimetypes.getMimetype(file));
         if (!file.canRead()) {
             throw new FileNotFoundException("Cannot read from file: " + file.getAbsolutePath());
         }
         setDataInputFile(file);
     }
     
     /**
      * Create an object representing text data. The object is initialized with the given
      * key, the given string as its data, a content type of <code>text/plain</code>, and a 
      * content length matching the string's length.
      * 
      * @param bucket
      * the bucket the object belongs to, or will be placed in.
      * @param key
      * the key name for the object.
      * @param dataString
      * the text data the object will contain. This string cannot be null.
      */
     public S3Object(S3Bucket bucket, String key, String dataString) {
         this(bucket, key);
         ByteArrayInputStream bais = new ByteArrayInputStream(dataString.getBytes());
         setDataInputStream(bais);
         setContentLength(bais.available());
         setContentType("text/plain");
     }
 
     /**
      * Create an object without any associated data, and no associated bucket.
      * 
      * @param key
      * the key name for the object.
      */
     public S3Object(String key) {
         this.key = key;
     }
 
     /**
      * Create an object without any associated data.
      * 
      * @param bucket
      * the bucket the object belongs to, or will be placed in.
      * @param key
      * the key name for the object.
      */
     public S3Object(S3Bucket bucket, String key) {
         if (bucket != null) {
             this.bucketName = bucket.getName();
         }
         this.key = key;
     }
 	
     public String toString() {
 		return "S3Object [key=" + getKey() + ",bucket=" + (bucketName == null ? "<Unknown>" : bucketName)  
 			+ ",lastModified=" + getLastModifiedDate() + ", dataInputStream=" + dataInputStream 
 			+ "] Metadata=" + getMetadataMap();
 	}
 	
     /**
      * @return
      * an input stream containing this object's data, or null if there is no data associated
      * with the object.
      * 
      * @throws S3ServiceException 
      */
 	public InputStream getDataInputStream() throws S3ServiceException {
        if (dataInputStream == null && dataInputFile != null) {
             try {
                dataInputStream = new FileInputStream(dataInputFile);                
             } catch (FileNotFoundException e) {
                 throw new S3ServiceException("Cannot open file input stream", e); 
             }
         }
        return dataInputStream;
 	}
 
     /**
      * Sets an input stream containing the data content to associate with this object.
      * <p>
      * <b>Note</b>: If the data content comes from a file, use the alternate method
      * {@link setDataInputFile} which allows object's to lazily open files and avoid any
      * Operating System limits on the number of files that may be opened simultaneously. 
      * <p>
      * This method will set the object's file data reference to null.
      * 
      * @param dataInputStream
      * an input stream containing the object's data.
      */
 	public void setDataInputStream(InputStream dataInputStream) {
         this.dataInputFile = null;
 		this.dataInputStream = dataInputStream;
 	}	
 
     /**
      * Sets the file containing the data content to associate with this object. This file will
      * be automatically opened as an input stream only when absolutely necessary, that is when
      * {@ getDataInputStream} is called.
      * <p>
      * This method will set the object's input stream data reference to null.
      * 
      * @param dataInputFile
      * a file containing the object's data.
      */
     public void setDataInputFile(File dataInputFile) {
         this.dataInputStream = null;
         this.dataInputFile = dataInputFile;
     }
 
     /**
      * Closes the object's data input stream if it exists.
      * 
      * @throws IOException
      */
     public void closeDataInputStream() throws IOException {
         if (this.dataInputStream != null) {
             this.dataInputStream.close();
         }
     }
     
     /**
      * @return
      * the ETag value of the object as returned by S3 when an object is created. The ETag values
      * does not include quote (") characters. This value will be null if the object's ETag value
      * is not known, such as when an object has not yet been uploaded to S3.
      */
 	public String getETag() {
 		String etag = (String) getMetadata(METADATA_HEADER_ETAG);
 		if (etag != null) {
 			if (etag.startsWith("\"") && etag.endsWith("\"")) {
 				return etag.substring(1, etag.length() -1);
 			}
 		}
 		return etag;
 	}
 	
     /**
      * Set the ETag value of the object.
      * 
      * @param etag
      * the ETag value as provided by S3.
      */
     public void setETag(String etag) {
         addMetadata(METADATA_HEADER_ETAG, etag);
     }
     
     /**
      * @return
      * the hex-encoded MD5 hash of an object's data contents as stored in the jets3t-specific metadata
      * item <code>md5-hash</code>, or null if the hash value is not available.
      */
     public String getMd5HashAsHex() {
         return (String) getMetadata(METADATA_HEADER_HASH_MD5);
     }
 	
     /**
      * @return
      * the Base64-encoded MD5 hash of an object's data contents as stored in the metadata
      * item <code>Content-MD5</code>, or as derived from an <code>ETag</code> or 
      * <code>md5-hash</code> hex-encoded version of the hash. Returns null if the hash value is not 
      * available.
      */
     public String getMd5HashAsBase64() {
         String md5HashBase64 = (String) getMetadata(METADATA_HEADER_CONTENT_MD5);
         if (md5HashBase64 == null) {
             // Try converting the object's ETag (a hex-encoded md5 hash).
             if (getETag() != null) {
                 return ServiceUtils.toBase64(ServiceUtils.fromHex(getETag()));
             }
             // Try converting the object's md5-hash (another hex-encoded md5 hash).
             if (getMd5HashAsHex() != null) {
                 return ServiceUtils.toBase64(ServiceUtils.fromHex(getMd5HashAsHex()));
             }
         }
         return md5HashBase64;
     }
     
     /**
      * Set the MD5 hash value of this object's data.
      * The hash value is stored as metadata under <code>Content-MD5</code> (Base64-encoded)
      * and the jets3t-specific <code>md5-hash</code> (Hex-encoded).
      * 
      * @param hash
      * the MD5 hash value of the object's data.
      */
     public void setMd5Hash(byte[] md5Hash) {
         addMetadata(METADATA_HEADER_HASH_MD5, ServiceUtils.toHex(md5Hash));
         addMetadata(METADATA_HEADER_CONTENT_MD5, ServiceUtils.toBase64(md5Hash));
     }
 
     /**
      * @return
      * the last modified date of this object, as provided by S3. If the last modified date is not
      * available (eg if the object has only just been created) the object's creation date is 
      * returned instead. If both last modified and creation dates are unavailable, null is returned.
      */
     public Date getLastModifiedDate() {
 		Date lastModifiedDate = (Date) getMetadata(METADATA_HEADER_LAST_MODIFIED_DATE);
 		if (lastModifiedDate == null) {
 			// Perhaps this object has just been created, in which case we can use the Date metadata.
 			lastModifiedDate = (Date) getMetadata(METADATA_HEADER_DATE);
 		}
 		return lastModifiedDate;
 	}
 	
     /**
      * Set this object's last modified date.
      * 
      * @param lastModifiedDate
      */
 	public void setLastModifiedDate(Date lastModifiedDate) {
         addMetadata(METADATA_HEADER_LAST_MODIFIED_DATE, lastModifiedDate);
 	}
 	
     /**
      * @return
      * this object's owner, or null if the owner is not available.
      */
 	public S3Owner getOwner() {
 		return (S3Owner) getMetadata(METADATA_HEADER_OWNER);
 	}
 
 	public void setOwner(S3Owner owner) {
         addMetadata(METADATA_HEADER_OWNER, owner);
 	}	
 
     /**
      * @return
      * the content length, or size, of this object's data. If the content length is unknown 0 is returned.
      */
 	public long getContentLength() {
 		Object contentLength = getMetadata(METADATA_HEADER_CONTENT_LENGTH);
 		if (contentLength == null) {
 			return 0;
 		} else {
 			return Long.parseLong(contentLength.toString());
 		}
 	}
 	
 	public void setContentLength(long size) {
         addMetadata(METADATA_HEADER_CONTENT_LENGTH, String.valueOf(size));
 	}
 
 	public String getStorageClass() {
 		return (String) getMetadata(METADATA_HEADER_STORAGE_CLASS);
 	}
 
 	public void setStorageClass(String storageClass) {
         addMetadata(METADATA_HEADER_STORAGE_CLASS, storageClass);
 	}
 
 	public String getContentType() {
 		return (String) getMetadata(METADATA_HEADER_CONTENT_TYPE);
 	}
 
 	public void setContentType(String contentType) {
         addMetadata(METADATA_HEADER_CONTENT_TYPE, contentType);
 	}
 	
     public String getContentLanguage() {
         return (String) getMetadata(METADATA_HEADER_CONTENT_LANGUAGE);
     }
 
     public void setContentLanguage(String contentLanguage) {
         addMetadata(METADATA_HEADER_CONTENT_LANGUAGE, contentLanguage);
     }
 
     public String getContentDisposition() {
         return (String) getMetadata(METADATA_HEADER_CONTENT_DISPOSITION);
     }
 
     public void setContentDisposition(String contentDisposition) {
         addMetadata(METADATA_HEADER_CONTENT_DISPOSITION, contentDisposition);
     }
 
     public String getContentEncoding() {
         return (String) getMetadata(METADATA_HEADER_CONTENT_ENCODING);
     }
 
     public void setContentEncoding(String contentEncoding) {
         addMetadata(METADATA_HEADER_CONTENT_ENCODING, contentEncoding);
     }
 
     public String getBucketName() {
 		return bucketName;
 	}
 
 	public void setBucketName(String bucketName) {
 		this.bucketName = bucketName;
 	}
 
 	public AccessControlList getAcl() {
 		return acl;
 	}
 
     /**
      * Sets the object's ACL. If a pre-canned REST ACL is used, the plain-text representation
      * of the canned ACL is also added as a metadata header <code>x-amz-acl</code>. 
      * 
      * @param acl
      */
 	public void setAcl(AccessControlList acl) {
         this.acl = acl;
         
         if (acl != null) {
             if (AccessControlList.REST_CANNED_PRIVATE.equals(acl)) {                
                 addMetadata(Constants.REST_HEADER_PREFIX + "acl", "private");
             } else if (AccessControlList.REST_CANNED_PUBLIC_READ.equals(acl)) { 
                 addMetadata(Constants.REST_HEADER_PREFIX + "acl", "public-read");
             } else if (AccessControlList.REST_CANNED_PUBLIC_READ_WRITE.equals(acl)) { 
                 addMetadata(Constants.REST_HEADER_PREFIX + "acl", "public-read-write");
             } else if (AccessControlList.REST_CANNED_AUTHENTICATED_READ.equals(acl)) {
                 addMetadata(Constants.REST_HEADER_PREFIX + "acl", "authenticated-read");
             } else {
                 // Non-REST canned ACLs are not added as headers...
             }
         }
 	}
 
 	public String getKey() {
 		return key;
 	}
 
 	public void setKey(String key) {
 		this.key = key;
 	}
 	
     /**
      * @return
      * true if the object's metadata is considered complete, such as when the object's metadata
      * has been retrieved from S3 by a HEAD request. If this value is not true, the metadata
      * information in this object should not be considered authoritive.
      */
     public boolean isMetadataComplete() {
         return isMetadataComplete;
     }
 
     /**
      * S3 Object metadata is only complete when it is populated with all values following
      * a HEAD or GET request.
      * 
      * @param isMetadataComplete
      */
     public void setMetadataComplete(boolean isMetadataComplete) {
         this.isMetadataComplete = isMetadataComplete;
     }
 }
