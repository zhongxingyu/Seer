package test;
 
 import com.google.api.services.drive.Drive;
 import com.google.api.services.drive.model.*;
 
 import gx.realtime.DriveComm;
 import gx.realtime.GSocket;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 public class PlaygroundTest {
 	
 	private static GSocket gSocket;
 	private static Drive service;
 
 	public static void main(String[] args) {
 		//create file on which content of files will be based
 		gSocket = GSocket.getInstance();
 		service = gSocket.getService();
 		
 		File playgroundFile = getPlaygroundFile();
 		if(playgroundFile == null){
 			playgroundFile = DriveComm.createFile(service, "A sample dev playground file", 
 					"This is a sample file for devving with the playground API", "application/vnd.google-apps.drive-sdk");
 		}
 		
 		printFile(playgroundFile);
 	}
 	
 	/**
 	 * Retrieve the first playgroundFile (of mimeType application/vnd.google-apps.drive-sdk) that can be found in
 	 * the Google Drive. If no playground file was found, this function returns null.
 	 * @return The first found playgroundFile. Null iff no playground file was found.
 	 */
 	private static File getPlaygroundFile(){
 		FileList files = null;
 		try {
 			files = service.files().list().execute();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	    
 	    int i = 0;
 	    File playgroundFile = null;
 	    while(files != null && playgroundFile == null && i < files.getItems().size()){
 	    	File file = files.getItems().get(i);
 	    	
 	    	if(file.getMimeType().equals("application/vnd.google-apps.drive-sdk")){
 	    		playgroundFile = file;
 	    		System.out.println("PlaygroundFile Found: " + playgroundFile.getId() + " / " + playgroundFile.getTitle());
 	    	}
     	
     	i++;
 	    }
 	    return playgroundFile;
 	}
 	
 	/**
 	 * This function is a function for testing purposes to print the properties and contents of a file.
 	 * @param file The file of which the properties and contents have to be printed.
 	 */
 	private static void printFile(File file){
 		InputStream contents = DriveComm.getFileContents(service, file);
 		System.out.println("File id: " + file.getId());
 		System.out.println("File title and description: " + file.getTitle() + " - " + file.getDescription());
 		
 		System.out.println("File contents: ");
 		if(contents != null){
 			BufferedReader in = new BufferedReader(new InputStreamReader(contents));
 			String line = null;
 			try {
 				while((line = in.readLine()) != null) {
 				  System.out.println(line);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else {
 			System.out.println(contents);
 		}
 	}
 	
 }
