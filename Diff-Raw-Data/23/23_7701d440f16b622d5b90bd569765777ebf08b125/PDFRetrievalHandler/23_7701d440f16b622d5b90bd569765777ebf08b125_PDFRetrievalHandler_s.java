 /**
  * 
  */
 package fr.esiea.sd.greenrobot.website.handler;
 
 import java.io.File;
 import java.net.URL;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FileUtils;
 
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.ListeningExecutorService;
 
 /**
  * @author nic0w
  *
  */
 public class PDFRetrievalHandler {
 
 	private final ServletFileUpload fileUploadManager;
 	
 	/**
 	 * 
 	 */
 	public PDFRetrievalHandler() {
 		this.fileUploadManager = new ServletFileUpload(new DiskFileItemFactory());
 	}
 
 	public ListenableFuture<File> retrieveFile(final HttpServletRequest request, ListeningExecutorService executor) {
 		
 		return executor.submit(new Callable<File>() {
 			@Override
 			public File call() throws Exception {
 				List<FileItem> formItems = fileUploadManager.parseRequest(request);
 				
 				if(formItems.size()>0) {
 				
 					FileItem firstItem = formItems.get(0); //We are only interested in the first item.
 					
 					File tempFile = File.createTempFile("greenrobot", null);
 					
 					if(firstItem.isFormField()) { //Then this is an URL to a PDF File we have to download.
 					
 						String fieldName = firstItem.getFieldName();
 						
 						if(!fieldName.contentEquals("url"))
 							throw new IllegalArgumentException("The field 'url' was not found in the form !");
 						
 						String fieldValue = firstItem.getString();
 						
 						URL fileURL = new URL(fieldValue);
 						
 						//We are force to have Commons IO as a dependency because of PDFBox, so let's use it !
 						FileUtils.copyURLToFile(fileURL, tempFile, 5000, 5000);						
 					}
 					else { //The file is uploaded.
 						
 						FileUtils.copyInputStreamToFile(firstItem.getInputStream(), tempFile);	
 					}
 					
 					return tempFile;
 				}
 				else
 					throw new IllegalArgumentException("The form submitted is empty !");
 			}
 		});
 	}
 
 }
