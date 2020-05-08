 import java.util.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import java.applet.*;
 import java.awt.*;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public class Uploader extends Applet {
     
     static {
 	if ( System.getProperty("os.name").toLowerCase().matches("windows.*") ) {
 	    System.load("c:\\code\\zugslist_jni.dll");
	    System.err.println("DLL Loaded.");
 	}
     }
     public native String getWoWPath();
     private Label message;
     
     public void init() {
 	try {
 	    String wow_install_path = osSpecificWoWDir();
 	    String account_name = getParameter("wowid");
 	    String accounts_dir = "WTF" + File.separator + "Account";
 	    
 	    message = new Label();
 	    
 	    setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
 	    add(message);
 	    
 	    if( wow_install_path.length() != 0 ) {
 		
 		String upload_file_path;
 		
 		if ( account_name != null && account_name.length() != 0 ) {
 		    
 		    upload_file_path = wow_install_path + File.separator +
 			"WTF" + File.separator + "Account" + File.separator +
 			account_name + File.separator + "SavedVariables" +
 			File.separator + "Blizzard_CombatLog.lua"; // "zugslist.lua";
 
 		    if( new File( upload_file_path ).exists() ) {
 			postContentToServer( upload_file_path );
 		    } else {
 			message.setText( "Zugslist addon file was not found!" );
			System.err.println( upload_file_path );
 		    }
 		} else {
 		    ArrayList<String> files_to_upload = findTradeLinksFiles(
 			wow_install_path + File.separator + accounts_dir
 		    );
 		    postContentToServer( files_to_upload );
 		}
 	    } else {
 		message.setText( "World of warcraft installtion dir not found!" );
 	    }
 	} catch(java.io.IOException ioe) {
 	    ioe.printStackTrace();
 	}
     }
     
     private String osSpecificWoWDir() {
 	String os_name = System.getProperty("os.name");
 	String wow_install_path = "";
	System.err.println( wow_install_path + " : " + os_name );
 	if ( os_name.toLowerCase().matches("windows.*") ) {
 	    wow_install_path = getWoWPath();
	    System.err.println( wow_install_path + " : " + os_name );
 	} else if( os_name.toLowerCase().matches("mac.*") ) {
 	    wow_install_path = "/Applications/World of Warcraft";
 	} else if ( os_name.toLowerCase().matches("linux") ) {
 	    wow_install_path = System.getProperty("user.home") +
 		"/.wine/drive_c/Program Files/World of Warcraft";
 	}
 	return wow_install_path;
     }
     
     private ArrayList<String> findTradeLinksFiles( String dir ) {
 	File path = new File( dir );
 	ArrayList<String> files_to_upload = new ArrayList<String>();
 	if( path.exists() ) {
 	    String[] listing = path.list();
 	    for(String item : listing) {
 		File item_file = new File( dir + File.separator + item );
 		//if( item_file.isFile() == true && item.toLowerCase().matches("zugslist.lua") ) {
		if( item_file.isFile() == true && item.toLowerCase().matches("Blizzard_CombatLog.lua") ) {
 		    files_to_upload.add( dir + File.separator + item );
 		} else if( item_file.isDirectory() == true ) {
 		    files_to_upload.addAll(
 			files_to_upload.size(),
 			findTradeLinksFiles( dir + File.separator + item )
 		    );
 		}
 	    }
 	} else {
 	    return files_to_upload;
 	}
 	return files_to_upload;
     }
     
     private boolean postContentToServer(String name) throws java.io.IOException {
 	HttpClient httpclient = new DefaultHttpClient();
 	HttpPost httppost = new HttpPost("http://10.0.0.40:3000/uploader/upload");
         FileBody payload = new FileBody(new File(name));
 	MultipartEntity reqEntity = new MultipartEntity();
 	
 	reqEntity.addPart("payload", payload);
 	httppost.setEntity(reqEntity);
 	
 	HttpResponse response = httpclient.execute(httppost);
         HttpEntity resEntity = response.getEntity();
 	
 	message.setText( "TradeLinks file uploaded to server!" );
 	
 	return true;
     }
 
     private boolean postContentToServer(ArrayList<String> files_to_upload) throws java.io.IOException {
 	HttpClient httpclient = new DefaultHttpClient();
 	HttpPost httppost = new HttpPost("http://10.0.0.40:3000/uploader/upload");
 	MultipartEntity reqEntity = new MultipartEntity();
 	
 	if( files_to_upload.size() == 0 ) {
 	    message.setText( "No files for upload found!" );
 	    return false;
 	}
 
 	int file_count = 1;
 	for(String file_to_upload : files_to_upload) {
 	    FileBody payload = new FileBody(new File( file_to_upload ));
 	    reqEntity.addPart("payload_"+file_count, payload);
 	    file_count++;
 	}
 	
 	httppost.setEntity(reqEntity);
 	HttpResponse response = httpclient.execute(httppost);
         HttpEntity resEntity = response.getEntity();
 	
 	message.setText( "TradeLinks file uploaded to server!" );
 	
 	return true;
     }
 }
