 package hackathon.boxme;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.DropboxAPI.Entry;
 import com.dropbox.client2.exception.DropboxException;
 import com.dropbox.client2.exception.DropboxServerException;
 import com.dropbox.client2.session.AccessTokenPair;
 import com.dropbox.client2.session.AppKeyPair;
 import com.dropbox.client2.session.Session;
 import com.dropbox.client2.session.WebAuthSession;
 
 public class DropboxStorageProvider implements StorageProvider {
 
 	private AppKeyPair appKeyPair = new AppKeyPair(
 			"u4d52lnoqpoqztk", "58xjxsb08ybg584");
 
 	private AccessTokenPair processCredentials(String credentials) {
 		String[] credSplit = credentials.split(" ");
 		AccessTokenPair access = new AccessTokenPair(credSplit[0], credSplit[1]);
 		return access;
 	}
 
 	@Override
 	public void putFile(String fileKey, FileCopyStream stream, String receiverCredentials) {
 		AccessTokenPair token = processCredentials(receiverCredentials);
 		AppKeyPair pair = this.appKeyPair;
 		System.out.println("putFile");
 		new Thread(new PutFileRequest(fileKey, stream, token, pair)).start();
 	}
 
 	@Override
 	public FileCopyStream getFile(String fileKey, String senderCredentials) {
 		AccessTokenPair token = processCredentials(senderCredentials);
 		WebAuthSession session = new WebAuthSession(appKeyPair,
 				Session.AccessType.DROPBOX, token);
 		System.out.println("getfile");
 		DropboxAPI<?> client = new DropboxAPI<WebAuthSession>(session);
 		try {
 			FileCopyStream stream = new FileCopyStream();
 			stream.setInputStream(client.getFileStream(fileKey, null));
 			stream.setSize(client.metadata(fileKey, 1, null, false, null).bytes);
 			return stream;
 		} catch (DropboxException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public boolean copyFile(String fileKey, String senderCredentials,
 			String receiverCredentials) {
 		return copyFile(fileKey, senderCredentials, fileKey, receiverCredentials);
 	}
 
 	private boolean copyFile(String senderFileKey, String senderCredentials,
 			String receiverFileKey, String receiverCredentials) {
 		try {
 			AccessTokenPair sender = processCredentials(senderCredentials);
 	        // Connect to the <source> UID and create a copy-ref.
 	        WebAuthSession sourceSession = new WebAuthSession(this.appKeyPair, Session.AccessType.DROPBOX, sender);
 	        DropboxAPI<?> sourceClient = new DropboxAPI<WebAuthSession>(sourceSession);
 	        DropboxAPI.CreatedCopyRef cr = sourceClient.createCopyRef(senderFileKey);
 	
         	AccessTokenPair receiver = processCredentials(receiverCredentials);
 	        WebAuthSession targetSession = new WebAuthSession(this.appKeyPair, Session.AccessType.DROPBOX, receiver);
 	        DropboxAPI<?> targetClient = new DropboxAPI<WebAuthSession>(targetSession);
 	        targetClient.addFromCopyRef(cr.copyRef, receiverFileKey);
 	        return true;
 		} catch (DropboxException e) {
 			e.printStackTrace();
 			if (e instanceof DropboxServerException) {
 				DropboxServerException e1 = (DropboxServerException)e;
 				if (e1.toString().endsWith("already exists.)")) {
 					int lastDot = receiverFileKey.lastIndexOf('.');
 					String newReceiver = receiverFileKey + "(copy)";
 					if (lastDot >= 0) {
 						newReceiver = receiverFileKey.substring(0, lastDot)
 								+ "(copy)" + receiverFileKey.substring(lastDot);
 					}
 					return copyFile(senderFileKey, senderCredentials, newReceiver, receiverCredentials);
 				}
 			}
 			return false;
 		}
 	}
 
 	@Override
 	public List<String> listAllFiles(String credentials) {
 		List<String> files = new LinkedList<String>();
 		AccessTokenPair creds = processCredentials(credentials);
         // Connect to the <source> UID and create a copy-ref.
         WebAuthSession session = new WebAuthSession(this.appKeyPair, Session.AccessType.DROPBOX, creds);
         DropboxAPI<?> client = new DropboxAPI<WebAuthSession>(session);
         addFilesToList(client, "", files);
         return files;
 	}
 
 	private void addFilesToList(DropboxAPI<?> client, String parentDir, List<String> files) {
 		Entry parent = null;
 		try {
 			parent = client.metadata(parentDir, 25000, null, true, null);
 		} catch (DropboxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (parent != null && parent.contents != null) {
 			for (Entry child : parent.contents) {
 				System.out.println(child.path + " " + child.isDir);
 				if (child.isDir) {
 					addFilesToList(client, child.path, files);
 				} else {
 					files.add(child.parentPath() + "/" + child.fileName());
 				}
 			}
 		}
 	}
 	
 	@Override
 	public DirectoryListing getFilesUnderPath(String path, String credentials) {
 		DirectoryListing dl = new DirectoryListing();
 		List<String> dirs = new LinkedList<String>();
 		List<String> files = new LinkedList<String>();
 		AccessTokenPair token = processCredentials(credentials);
         WebAuthSession session = new WebAuthSession(this.appKeyPair, Session.AccessType.DROPBOX, token);
         DropboxAPI<?> client = new DropboxAPI<WebAuthSession>(session);
 		try {
 			Entry parent = client.metadata(path, 25000, null, true, null);
 			for (Entry child : parent.contents) {
 				if (child.isDir) {
 					dirs.add(child.path);
 				} else {
 					files.add(child.path);
 				}
 			}
 			dl.setDirectories(dirs);
 			dl.setFiles(files);
 		} catch (DropboxException e) {
 			e.printStackTrace();
 		}
 		return dl;
 	}
 }
