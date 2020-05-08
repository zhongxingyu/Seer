 package kea.kme.pullpit.server.services;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import kea.kme.pullpit.client.objects.PullPitFile;
 import kea.kme.pullpit.client.services.FileService;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.Query;
 
 /**
  * RPC Service used to initialise file uploads.
  * 
  * @author Emil
  * 
  */
 public class FileServiceImpl extends RemoteServiceServlet implements
 		FileService {
 	private static final long serialVersionUID = 8694147314918269035L;
//	private static final Logger log = Logger.getLogger(FileServiceImpl.class.getName());
 	/**
 	 * The service that handles uploads.
 	 */
 	BlobstoreService blobstoreService = BlobstoreServiceFactory
 			.getBlobstoreService();
 	/**
 	 * The service that writes and reads file meta-data to the Java Datastore.
 	 */
 	Objectify ofy = ObjectifyService.begin();
 
 	// Registers the entity class.
 	static {
 		ObjectifyService.register(PullPitFile.class);
 	}
 
 	/**
 	 * Implementation of the method that retrieves a URL used in the file upload
 	 * form.
 	 * 
 	 * @see kea.kme.pullpit.client.services.FileService#getBlobStoreUploadUrl()
 	 */
 	@Override
 	public String getBlobStoreUploadUrl() throws Exception {
 		return blobstoreService.createUploadUrl("/upload");
 	}
 
 	/**
 	 * Uses {@link Objectify} to retrieve meta-data about a file.
 	 * 
 	 * @see kea.kme.pullpit.client.services.FileService#getFile(java.lang.String)
 	 */
 	@Override
 	public PullPitFile getFile(String id) {
 		long l = Long.parseLong(id);
 		PullPitFile file = ofy.get(PullPitFile.class, l);
 		return file;
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse res)
 			throws ServletException, IOException {
 
 		BlobKey blobKey = new BlobKey(req.getParameter("blob-key"));
 		blobstoreService.serve(blobKey, res);
 
 	}
 
 	public PullPitFile[] getFilesByParameter(String searchKey,
 			Integer searchValue) {
 		Query<PullPitFile> q = ofy.query(PullPitFile.class).filter(searchKey,
 				searchValue);
 		ArrayList<PullPitFile> resultList = new ArrayList<PullPitFile>();
 		for (PullPitFile result : q) {
 			resultList.add(result);
 		}
 		return resultList.toArray(new PullPitFile[resultList.size()]);
 	}
 
 }
