 package org.atlasapi.feeds.upload.azure;
 
 import java.io.ByteArrayInputStream;
 import java.net.URISyntaxException;
 import java.security.InvalidKeyException;
 
 import org.atlasapi.feeds.upload.FileUpload;
 import org.atlasapi.feeds.upload.FileUploader;
 
 import com.microsoft.windowsazure.services.blob.client.BlobContainerPermissions;
 import com.microsoft.windowsazure.services.blob.client.BlobContainerPublicAccessType;
 import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
 import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
 import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
 import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
 import com.microsoft.windowsazure.services.core.storage.RetryLinearRetry;
 import com.microsoft.windowsazure.services.core.storage.StorageException;
 
 public class AzureFileUploader implements FileUploader {
 		
 	private static final String CONTENT_TYPE_BINARY = "Binary";
 	
 	private String account;
 	private String key;
 	private String container;
 	
 	public AzureFileUploader(String account, String key, String container) {
 		this.container = container;
 		this.account = account;
 		this.key = key;
 	}
 	
 	protected CloudBlobClient getClient() throws InvalidKeyException, URISyntaxException {
         CloudStorageAccount cloudAccount = CloudStorageAccount.parse(
                 String.format("DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s", account, key));
         CloudBlobClient client = cloudAccount.createCloudBlobClient();
         client.setRetryPolicyFactory(new RetryLinearRetry(300, 3));
                 
         return client;
 	}
 
 	@Override
 	public FileUploaderResult upload(FileUpload upload) throws Exception {
 		
         String contentType = upload.getContentType() != null ? upload.getContentType().toString() : CONTENT_TYPE_BINARY;
         
         CloudBlobContainer blobContainer = createOrGetBlobContainer();
         CloudBlockBlob blockBlobRef = blobContainer.getBlockBlobReference(upload.getFilename());
             
         ByteArrayInputStream inputStream = new ByteArrayInputStream(upload.getFileData());      
         blockBlobRef.getProperties().setContentType(contentType);
         blockBlobRef.upload(inputStream, upload.getFileData().length);
         blockBlobRef.uploadProperties();
 	}
 	
	   private CloudBlobContainer createOrGetBlobContainer() throws StorageException, URISyntaxException, InvalidKeyException {
	        CloudBlobClient client = getClient();
	        CloudBlobContainer containerRef = client.getContainerReference(container);
	        
	        if(containerRef.createIfNotExist() == true) {
	            BlobContainerPermissions containerPermissions;
	            containerPermissions = new BlobContainerPermissions();
	            containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
	            containerRef.uploadPermissions(containerPermissions);
	        }
	        return containerRef;
 	    }
 }
