 package controllers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.net.URL;
 import java.util.List;
 
 import org.apache.commons.mail.EmailException;
 
 import models.Emailer;
 import models.File_managment;
 import models.Users;
 import play.*;
 import play.data.Upload;
 import play.data.validation.Required;
 import play.mvc.*;
 
 public class Newsletter extends Controller {
 
 	/**
 	 * renders the index page
 	 */
     public static void index() {
     	String filesTable = buildTable();
     	render(filesTable);
     }
     
     /**
      * uploads the newsletter file and then lets the user know
      * @param file the newsletter to upload
      */
     public static void upload(@Required Upload file) {
     	//get the uploaded file parts
 		List<Upload> uploads  = (List<Upload>) request.current().args.get("__UPLOADS");
 		if(uploads != null){
     		//save file
 			String destinationPrefix = "public\\files\\newsletters\\";
 			File_managment.upload(uploads, destinationPrefix);
 			index(); //re-render index
     	} else {
     		index(); //if no upload found, send back to upload page
     	} 
     }
     
     /**
      * deletes selected newsletters and re-renders the index
      * @param newsletter String representing the newsletter files path
      */
     public static void delete(@Required String newsletter){
     	File_managment.delete(newsletter);
 		index();
     }
 
	//Email it too... (needs testing with Users email addresses, has worked with hard coded emails)
    //TODO - Fix this up once users are added
     public static void sendEmail(@Required String newsletter){
     	String fileDestination = newsletter;
 		File newFile = new File(fileDestination);
		for(int x = 1; x < Users.count(); x++){
 			Users user = Users.findById(x);
 			try {
 				Emailer.sendNewsletterTo(user.email, newFile);
 			} catch (EmailException e) {
 				e.printStackTrace();
 			}
 		}
     }
     
     //Returns a list of all files uploaded to the newsletter directory (
     private static String[][] getAvailableNewsletters(){
     	//Directory path here
     	String path = "public\\files\\newsletters\\";
     	 
     	File folder = new File(path);
     	File[] listOfFiles = folder.listFiles(); 
     	String[][] files = new String[listOfFiles.length+1][2];
     	
     	for (int i = 0; i < listOfFiles.length; i++){
     	   if (listOfFiles[i].isFile()){
     		   files[i][0] = listOfFiles[i].getName();
     		   String uri = listOfFiles[i].getPath();
     		   files[i][1] = uri.replaceAll("\\\\", "/");
     	   }
     	}
     	return files;
     }    
     
     //Returns a String that contains a html table that neatly presents the files available for use as newsletters
     private static String buildTable(){
     	String files[][] = getAvailableNewsletters();
 		String filesTable = "<div id='fileTable' name='fileTable'> \n<Table>\n";
 		for(int x = 0; x < files.length-1; x++ ){
 			filesTable += "<tr><td width=20px><center><input type='radio' name="+files[x][0]+" value="+files[x][1]+" onchange='newSelection(this);' /></center></td><td><a href='" + files[x][1] + "'> " + files[x][0] + "</a></td>\n";
 			filesTable += "</tr>\n";
 		}
 		filesTable += "</table>\n</div>\n";
 		return filesTable; 	
     }
 }
