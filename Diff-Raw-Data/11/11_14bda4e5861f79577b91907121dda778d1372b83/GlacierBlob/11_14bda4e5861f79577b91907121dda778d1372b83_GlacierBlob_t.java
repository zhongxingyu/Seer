 package org.fcrepo.akubra.glacier;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.util.Map;
 
 import org.akubraproject.Blob;
 import org.akubraproject.DuplicateBlobException;
 import org.akubraproject.MissingBlobException;
 import org.akubraproject.impl.AbstractBlob;
 import org.akubraproject.impl.StreamManager;
 
 import com.amazonaws.services.glacier.AmazonGlacierClient;
 import com.amazonaws.services.glacier.model.DeleteArchiveRequest;
 
 public class GlacierBlob extends AbstractBlob {
 
 	private URI blobId;
 	private StreamManager manager;
 	private GlacierBlobStoreConnection connection;
 	private AmazonGlacierClient client;
 
 	public GlacierBlob(GlacierBlobStoreConnection connection, URI blobId, StreamManager manager) {
 		super(connection, blobId);
 		
 		this.connection = connection;
 		this.client = connection.getGlacierClient();
 		this.blobId = blobId;
 		this.manager = manager;
 	}
 	
 	public void delete() throws IOException {
 		client.deleteArchive(new DeleteArchiveRequest()
 		.withVaultName(getVault())
 		.withArchiveId(getArchiveId()));
 		
 		connection.getGlacierInventoryManager().remove(blobId);
 	}
 
 	public boolean exists() throws IOException {
		return getArchiveId() != null;
 	}
 
 	public long getSize() throws IOException, MissingBlobException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public Blob moveTo(URI arg0, Map<String, String> arg1)
 			throws DuplicateBlobException, IOException, MissingBlobException,
 			NullPointerException, IllegalArgumentException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public InputStream openInputStream() throws IOException,
 			MissingBlobException {
 		ensureOpen();
 
 	    if (!exists())
 	      throw new MissingBlobException(getId());
 	    
 		InputStream gis = new GlacierInputStream(client, getVault(), getArchiveId());
 		
 	    return manager.manageInputStream(getConnection(), gis);
 	}
 
 	public OutputStream openOutputStream(long expectedSize, boolean overwrite)
 			throws IOException, DuplicateBlobException {
 		ensureOpen();
 
 	    if (!overwrite && exists())
 	      throw new DuplicateBlobException(getId());
 	    
 		OutputStream os = new GlacierMultipartBufferedOutputStream(connection, getVault(), blobId);
 	    return manager.manageOutputStream(getConnection(), os);
 	}
 	
 	private String getVault() {
 		return connection.getVault();
 	}
 	
 	private String getArchiveId() {
 		GlacierInventoryManager im = connection.getGlacierInventoryManager();
 		
		GlacierInventoryObject obj = im.get(blobId);
		
		if(obj != null) {
			return obj.getArchiveId();
		} else {
			return null;
		}
 	}
 
 }
