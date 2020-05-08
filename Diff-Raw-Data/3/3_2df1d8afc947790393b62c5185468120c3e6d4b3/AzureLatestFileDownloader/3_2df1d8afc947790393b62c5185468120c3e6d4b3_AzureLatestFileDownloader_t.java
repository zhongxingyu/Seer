 package org.atlasapi.feeds.lakeview.validation;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.security.InvalidKeyException;
 import java.util.Date;
 
 import org.joda.time.DateTime;
 
import com.google.common.base.Throwables;
 import com.microsoft.windowsazure.services.blob.client.CloudBlob;
 import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
 import com.microsoft.windowsazure.services.blob.client.CloudBlobDirectory;
 import com.microsoft.windowsazure.services.blob.client.ListBlobItem;
 import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
 import com.microsoft.windowsazure.services.core.storage.RetryLinearRetry;
 import com.microsoft.windowsazure.services.core.storage.StorageException;
 
 public class AzureLatestFileDownloader {
 
 	private String account;
 	private String key;
 	private String container;
 
 	public AzureLatestFileDownloader(String account, String key, String container) {
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
 
 	public AzureFileAndMetadata getLatestFile() {
 		
 	    try {
     		CloudBlobDirectory directory = getClient().getDirectoryReference(container);
     
     		ListBlobItem latestItem = null;
     		Date latestLastMod = null;
     		Iterable<ListBlobItem> blobs = directory.listBlobs();
     		for(ListBlobItem item : blobs) {
     		    Date thisLastMod = getClient().getBlockBlobReference(item.getUri().toString()).getProperties().getLastModified();
     		    if(latestLastMod == null || thisLastMod.after(latestLastMod)) {
     		        latestLastMod = thisLastMod;
     		        latestItem = item;
     		    }
     		}
     		CloudBlob blob = (CloudBlob) latestItem;
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             blob.download(os);
             return new AzureFileAndMetadata(os.toByteArray(), new DateTime(latestLastMod));
 	    }
 	    catch(StorageException e) {
 	        Throwables.propagate(e);
 	    }
 	    catch(IOException e) {
 	        Throwables.propagate(e);
 	    }
 	    catch(URISyntaxException e) {
 	        Throwables.propagate(e);
 	    } catch (InvalidKeyException e) {
             Throwables.propagate(e);
         }
 	    return null;
 		
 	}
 }
