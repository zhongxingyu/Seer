 /* 
  * License: source-license.txt
  * If this code is used independently, copy the license here.
  */
 
 package wombat.gui.actions;
 
 import java.awt.Desktop;
 import java.awt.event.ActionEvent;
 import java.io.*;
 import java.net.URI;
 import java.net.URLEncoder;
 
 import javax.swing.*;
 
 import wombat.gui.icons.*;
 import wombat.util.files.*;
 
 /**
  * Open a web browser so that the user can upload the active document to Tiro.
 * TODO: Parameter the upload loaction.
  */
 public class Upload extends AbstractAction {
 	private static final long serialVersionUID = 5980425299281375927L;
 
 	/*
 	 * BASE_URL is the url when a file isn't known.
 	 * FILE_URL uses the filename to paramaterize the request.
 	 * 
 	 * TODO: Put this into a configuration file.
 	 */
	static final String BASE_URL = "https://www.cs.indiana.edu/cgi-pub/c211/spring12/tiro/tiro.cgi";
	static final String FILE_URL = "https://www.cs.indiana.edu/cgi-pub/c211/spring12/tiro/tiro.cgi?show_submissions=1&show_assignments=1&assignments={assignment_name}&show_group=1&assignments={assignment_name}";
 	
 	/**
 	 * Create the upload action.
 	 */
 	public Upload() {
 		super("Upload", IconManager.icon("Upload.png"));
 		putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 	}
 	
 	/**
 	 * Open the correct submission page in the user's web browser.
 	 * @param event Event parameters (ignored).
 	 * @see URLEncoder, Desktop,DocumentManager
 	 */
 	public void actionPerformed(ActionEvent event) {
 		File file = DocumentManager.getActiveFile();
 		
 		// Get the URL to fetch.
 		String url = null;
 		if (file == null) {
 			url = BASE_URL;
 		} else {
 			String name = file.getName();
 			int pos = name.lastIndexOf(".");
 			if (pos != -1) name = name.substring(0, pos);
 			
 			try {
 				url = FILE_URL.replace("{assignment_name}", URLEncoder.encode(name, "UTF-8"));
 			} catch (IOException e) {
 				url = BASE_URL;
 			}
 		}
 		
 		// Try to open it in the user's browser.
 		try {
 			Desktop.getDesktop().browse(URI.create(url));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
