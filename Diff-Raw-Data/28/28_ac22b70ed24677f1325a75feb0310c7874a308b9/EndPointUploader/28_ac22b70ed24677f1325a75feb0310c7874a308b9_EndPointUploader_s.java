 package de.uni_leipzig.simba.saim.gui.widget.form;
 
 import java.io.File;
import java.io.FileInputStream;
 import java.io.FileOutputStream;
import java.io.InputStream;
 import java.io.OutputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.vaadin.data.Item;
import com.vaadin.ui.*;
 import com.vaadin.ui.Upload.FailedEvent;
 import com.vaadin.ui.Upload.SucceededEvent;

 import de.uni_leipzig.simba.cache.HybridCache;
 import de.uni_leipzig.simba.io.KBInfo;
 import de.uni_leipzig.simba.query.FileQueryModule;
 import de.uni_leipzig.simba.saim.core.FileStore;
import de.uni_leipzig.simba.saim.util.FileUploadHelper;
 /**
  * @BUG: Writes an empty line at the end
  * @author Lyko
  *
  */
 public class EndPointUploader extends CustomComponent implements Upload.SucceededListener, 
 	Upload.FailedListener, Upload.Receiver{
 
 	VerticalLayout l = new VerticalLayout();
 	Panel root;
 	Select typeSelect;
 	
 	File file;
 	
 	public EndPointUploader() {
 		FileStore.setUp();
 		root = new Panel("Upload dumped endpoint to server");
 		
 		l.addComponent(root);
 	    setCompositionRoot(l);
 
 	    // Create the Upload component.
         final Upload upload = new Upload("Please upload dumped rdf graphs " +
         		"as files holding N-Trpiples to the server", this);
 
 	    // Use a custom button caption instead of plain "Upload".
 	    upload.setButtonCaption("Upload Now");
 	        
 	    // Listen for events regarding the success of upload.
 	    upload.addListener((Upload.SucceededListener) this);
 	    upload.addListener((Upload.FailedListener) this);
 
 	    root.addComponent(upload);
 	    
 	    typeSelect = new Select("Type");
 //		typeSelect.addItem("N-Triple");
 		typeSelect.addItem("N3");
 		typeSelect.addItem("Turtle");
 		
 		typeSelect.select("N3	");
 		typeSelect.setNullSelectionAllowed(false);
 	
 		root.addComponent(typeSelect);
 //	    root.addComponent(new Label("Click 'Browse' to "+
 //	    "select a file and then click 'Upload'."));
 	}
 	
 	
 	@Override
 	public OutputStream receiveUpload(String filename, String mimeType) {
 		String suffix = filename.substring(filename.lastIndexOf(".")+1);
 	    System.out.println(suffix);
 //		if(!suffix.equalsIgnoreCase("nt") && !suffix.equalsIgnoreCase("n3")
 //				&& !suffix.equalsIgnoreCase("ttl") && !suffix.equalsIgnoreCase("turtle")) {
 //			System.out.println("no supported format");
 //			getWindow().showNotification("No supported format");
 //	       	return null;
 //	    } else {
 	    	FileOutputStream fos = null; // Output stream to write to
 	        file = new File(FileStore.getPathToEPStore() + "/" + filename);
 	         
 	        System.out.println("Attempt to upload file "+filename);	     
 	        try {
 	            // Open the file for writing.
 	            fos = new FileOutputStream(file);
 	        } catch (final java.io.FileNotFoundException e) {
 	            // Error while opening the file. Not reported here.
 	            e.printStackTrace();
 	            return null;
 	        }
 	        return fos; // Return the output stream to write to
 //	    }
 		
 	}
 
 	@Override
 	public void uploadFailed(FailedEvent event) {
 		System.out.println("Upload failed:\n"+event.getReason().getMessage());
 		getWindow().showNotification("Sorry there was an error uploading the file.");		
 	}
 
 	@Override
 	public void uploadSucceeded(SucceededEvent event) {
 		root.removeAllComponents();
 
 		Label l = new Label();
 		l.setCaption("Successfull uploaded file "+file.getName()+" trying to parse data...");
 		root.addComponent(l);
 		//repair upload
 //		FileUploadHelper helper = new FileUploadHelper(file);
 //		helper.repairUpload();
 		/** @TODO test data*/
 		KBInfo info = new KBInfo();
 		info.endpoint = file.getAbsolutePath();
 		info.type = (String) typeSelect.getValue();
 		// The FileQueryModule catches all errors in the constructor
 		// so we have to test manually
 		try {
 			FileQueryModule fQModule = new FileQueryModule(info);
 			HybridCache hC = new HybridCache();
 			fQModule.fillCache(hC);
 			l.setCaption("File parsed correctly");
 			System.out.println("Successfully read data");
 //			getWindow().showNotification("Successfully read data");
 		} catch(Exception  e) {
 //			getWindow().showNotification("Sorry there was an error reading the file. Abborting...");
 			l.setCaption("Sorry there was an error reading the file. Abborting...");
 			e.printStackTrace();
 //			file.delete();
 		}
 		/** @TODO provide form for name and additional informations.*/
 		
 		/** @TODO Store prebuild KBInfo for endpoint,
 		 *  profide shortcut in predefined KBInfos set in the EndpointPanel
 		 */
 	}
 
 }
