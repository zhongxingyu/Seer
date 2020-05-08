 package com.google.appengine.demos.sticky.server;
 
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.jdo.Transaction;
 
 import org.apache.commons.fileupload.FileItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.appengine.api.datastore.Blob;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.appengine.demos.sticky.server.Store.Author;
 import com.google.appengine.demos.sticky.server.Store.Note;
 
 import gwtupload.server.exceptions.UploadActionException;
 import gwtupload.server.gae.AppEngineUploadAction;
 
 @SuppressWarnings("serial")
 public class ImageUploadServlet extends AppEngineUploadAction {
 	private static Logger log = LoggerFactory.getLogger(ImageUploadServlet.class);
 	
 	private final Store store = Store.getInstance();
 	private static String NOTE_KEY = "noteKey";
 	
 	@Override
 	public String executeAction(HttpServletRequest request,
 			List<FileItem> sessionFiles) throws UploadActionException {
 		log.debug("executeAction called");
 		
 		String response = "";
 		String contentType = "";
 		String noteKey = "";
 		byte[] bytes = null;
 		
 		if(sessionFiles != null) {
 			for(FileItem fItem : sessionFiles) {				
 				if(fItem.isFormField()==false) {					
 					log.debug("File Uploaded: " + fItem.getName() + 
 							  ", Content-Type: " + fItem.getContentType());
 					
 					contentType = fItem.getContentType();
					bytes = fItem.get();
 					
 				} else if(fItem.getFieldName().equals(NOTE_KEY)){
 					try {
 						noteKey = fItem.getString("UTF-8").trim();
 						log.debug("Note Key: " + noteKey);
 					} catch (UnsupportedEncodingException ex) {
 						log.error(ex.getMessage(), ex);
 						throw new UploadActionException("Error decoding Note-Key!");
 					}
 				}
 			}
 			
 			if(!persistNoteImage(contentType, bytes, noteKey)) {
 				throw new UploadActionException("Error persisting image!");
 			}
 			
 		}
 		
 		removeSessionFileItems(request);
 		return (response.length()==0) ? null : response;
 	}
 	
 	private boolean persistNoteImage(String contentType, byte[] bytes, String noteKey) {
 		Store.Api api = store.getApi();
 		
 		User user = UserServiceFactory.getUserService().getCurrentUser();
 		//NoteImage image = new NoteImage(bytes, contentType, user.getEmail(), new Date(), new Date());
 		
 		try {
 			Author me = api.getOrCreateNewAuthor(user);
 			
 			Transaction tx = api.begin();
 			Note note = api.getNote(KeyFactory.stringToKey(noteKey));
 			if(!note.getAuthorEmail().equalsIgnoreCase(me.getEmail())) {
                 return false;
             }
 			
 			note.setImageData(new Blob(bytes));
 			note.setContentType(contentType);
 			api.saveNote(note);
 			tx.commit();
 			
 			log.debug("Persisted image with for note: " + note.getKey());
 						
 		} catch(Exception ex) {
 			log.error(ex.getMessage(), ex);
 			return false;
 		} finally {
 			api.close();
 		}
 		return true;
 	}
 
 }
