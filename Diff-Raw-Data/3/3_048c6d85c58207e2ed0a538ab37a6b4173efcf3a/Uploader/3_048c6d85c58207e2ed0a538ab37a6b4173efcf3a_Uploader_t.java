 import java.util.*;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.FileOutputStream;
 
 import java.applet.*;
 import java.awt.*;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import netscape.javascript.*;
 
 public class Uploader extends Applet {
     
     public native String getWoWPath();
     
     private Label message;
     private JSObject window;
     
     public void init() {
 
 	try { window = JSObject.getWindow(this); } catch (JSException jse) { jse.printStackTrace(); }
 	
 	String account_name = getParameter("wowid");
 	String wow_install_path = "";
 	String accounts_dir = "WTF" + File.separator + "Account";
 	
 	if( load_native_extensions( getServerURL() ) ) {
 	    wow_install_path = osSpecificWoWDir();
 	    //System.err.println( wow_install_path );
 	}
 	
 	message = new Label();
 	setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
 	add(message);
 	
 	try {
 	    if( wow_install_path.length() != 0 ) {
 		
 		notifyDOM("UPLOAD_PREPARE");
 		
 		String upload_file_path;
 		
 		if ( account_name != null && account_name.length() != 0 ) {
 		    
 		    upload_file_path = wow_install_path + File.separator +
 			"WTF" + File.separator + "Account" + File.separator +
 			account_name + File.separator + "SavedVariables" +
 			File.separator + "GnomishYellowPages.lua"; // "zugslist.lua";
 		    
 		    if( new File( upload_file_path ).exists() ) {
 			postContentToServer( upload_file_path );
 		    } else {
 			message.setText( "Zugslist addon file was not found!" );
 		    }
 		    
 		} else {
 		    ArrayList<String> gyp_files_to_upload = findTradeLinksFiles(
 			wow_install_path + File.separator + accounts_dir
 		    );
 		    ArrayList<String> poss_files_to_upload = findPossessionsFiles(
 			wow_install_path + File.separator + accounts_dir
 		    );
 
 		    notifyDOM("UPLOAD_START");
 		    
 		    postContentToServer( gyp_files_to_upload, poss_files_to_upload );
 		    
 		    notifyDOM("UPLOAD_END");
 		}
 		
 		notifyDOM("DOWNLOAD_PREPARE");
 		
 		ArrayList<String> all_accounts = getAccountDirs(
 		    wow_install_path + File.separator + accounts_dir
 		);
 		downloadZugslistFeed( getServerURL(), all_accounts );
 		
 		notifyDOM("DOWNLOAD_END");
 		
 	    } else {
 		notifyDOM("WOW_NOT_FOUND");
 		message.setText( "World of warcraft installtion dir not found!" );
 	    }
 	} catch(java.io.IOException ioe) {
 	    notifyDOM("NETWORK_ERROR");
 	    ioe.printStackTrace();
 	}
     }
     
     private static boolean load_native_extensions(String doc_base_url) {
 	if ( System.getProperty("os.name").toLowerCase().matches("windows.*") ) {
 	    if( !load_win32_dll() ) {
 		download_win32_dll(doc_base_url);
 		if( !load_win32_dll() ) {
 		    return false;
 		}
 	    }
 	}
 	return true;
     }
     
     private static void download_win32_dll(String doc_base_url) {
 	HttpClient http_client = new DefaultHttpClient();
 	//System.err.println( doc_base_url + "libs/zugslist_jni.dll" );
 	HttpGet get_request = new HttpGet( doc_base_url + "libs/zugslist_jni.dll" );
 	try {
 	    HttpResponse payload = http_client.execute(get_request);
 	    InputStream in = payload.getEntity().getContent();
 	    byte[] b = new byte[1024];
 	    int len;
 	    OutputStream out = new FileOutputStream(
                 System.getProperty("java.io.tmpdir") + File.separator + "zugslist_jni.dll"
             );
 	    while ((len = in.read(b)) != -1) {
 		out.write(b, 0, len);
 	    }
 	    in.close();
 	    out.close();
 	} catch(java.io.IOException ioe){
 	    ioe.printStackTrace();
 	} finally{
 	    http_client.getConnectionManager().shutdown();
 	}
     }
 
     private static boolean load_win32_dll() throws java.lang.UnsatisfiedLinkError {
 	boolean loaded_sccessfully = true;
 	try {
 	    //System.err.println( System.getProperty("java.io.tmpdir") + File.separator + "zugslist_jni.dll" );
 	    System.load( System.getProperty("java.io.tmpdir") + File.separator + "zugslist_jni.dll" );
 	} catch(java.lang.UnsatisfiedLinkError ule) {
 	    loaded_sccessfully = false;
 	}
 	return loaded_sccessfully;
     }
     
     private String osSpecificWoWDir() {
 	String os_name = System.getProperty("os.name");
 	String wow_install_path = "";
 	if ( os_name.toLowerCase().matches("windows.*") ) {
 	    wow_install_path = getWoWPath();
 	} else if( os_name.toLowerCase().matches("mac.*") ) {
 	    wow_install_path = "/Applications/World of Warcraft";
 	} else if ( os_name.toLowerCase().matches("linux") ) {
 	    wow_install_path = System.getProperty("user.home") +
 		"/.wine/drive_c/Program Files/World of Warcraft";
 	}
 	return wow_install_path;
     }
 
     private ArrayList<String> findPossessionsFiles( String dir ) {
 	return findFiles(dir, "gnomishyellowpages.lua");
     }
     
     private ArrayList<String> findTradeLinksFiles( String dir ) {
 	return findFiles(dir, "gnomishyellowpages.lua");
     }
     
     private ArrayList<String> findFiles( String dir, String file_name ) {
 	File path = new File( dir );
 	ArrayList<String> files_to_upload = new ArrayList<String>();
 	if( path.exists() ) {
 	    String[] listing = path.list();
 	    for(String item : listing) {
 		File item_file = new File( dir + File.separator + item );
 		if( item_file.isFile() == true && item.toLowerCase().matches(file_name) ) {
 		    files_to_upload.add( dir + File.separator + item );
 		} else if( item_file.isDirectory() == true ) {
 		    files_to_upload.addAll(
 			files_to_upload.size(),
 			findFiles( dir + File.separator + item, file_name )
 		    );
 		}
 	    }
 	} else {
 	    return files_to_upload;
 	}
 	return files_to_upload;
     }
 
     private static ArrayList<String> getAccountDirs(String dir) {
 	ArrayList<String> account_dirs = new ArrayList<String>();
 	File path = new File( dir );
 	if( path.exists() ) {
 		String[] listing = path.list();
 		for(String item : listing) {
 		    File item_file = new File(dir + File.separator + item);
 		    if( item_file.isDirectory() == true ) {
 			account_dirs.add( dir + File.separator + item );
 		    }
 		}
 	}
 	return account_dirs;
     }
     
     private boolean postContentToServer(String name) throws java.io.IOException {
 	HttpClient httpclient = new DefaultHttpClient();
 	HttpPost httppost = new HttpPost( getServerURL() + "zugslist/upload" );
         FileBody payload = new FileBody(new File(name));
 	MultipartEntity reqEntity = new MultipartEntity();
 	
 	reqEntity.addPart("payload", payload);
 	httppost.setEntity(reqEntity);
 	
 	HttpResponse response = httpclient.execute(httppost);
         HttpEntity resEntity = response.getEntity();
 	
 	message.setText( "TradeLinks file uploaded to server!" );
 	
 	return true;
     }
     
     private boolean postContentToServer(
 	ArrayList<String> gyp_files_to_upload, ArrayList<String> poss_files_to_upload
     ) throws java.io.IOException {
 	HttpClient httpclient = new DefaultHttpClient();
 	HttpPost httppost = new HttpPost( getServerURL() + "zugslist/upload" );
 	MultipartEntity reqEntity = new MultipartEntity();
 	
 	if( gyp_files_to_upload.size() == 0 && poss_files_to_upload.size() == 0 ) {
 	    message.setText( "No files founds for uploading!" );
 	    return false;
 	}
 	
 	int file_count = 1;
 	for(String file_to_upload : gyp_files_to_upload) {
 	    FileBody payload = new FileBody(new File( file_to_upload ));
 	    reqEntity.addPart("payload_gyp_"+file_count, payload);
 	    file_count++;
 	}
 
 	file_count = 1;
 	for(String file_to_upload : poss_files_to_upload) {
 	    FileBody payload = new FileBody(new File( file_to_upload ));
 	    reqEntity.addPart("payload_poss_"+file_count, payload);
 	    file_count++;
 	}
 	
 	httppost.setEntity(reqEntity);
 	HttpResponse response = httpclient.execute(httppost);
         HttpEntity resEntity = response.getEntity();
 	
 	message.setText( "All files uploaded to server!" );
 	
 	return true;
     }
     
     private void downloadZugslistFeed(String doc_base_url, ArrayList<String> accounts) {
 
 	HttpClient http_client = new DefaultHttpClient();
 	HttpGet get_request = new HttpGet( doc_base_url + "zugslist/zugsfeed" );
 	try {
 	    HttpResponse payload = http_client.execute(get_request);
 	    InputStream in = payload.getEntity().getContent();
 	    byte[] b = new byte[1024];
 	    int len;
 	    // create tmp zugslist.lua with current timestamp
 	    OutputStream out = new FileOutputStream(
                 System.getProperty("java.io.tmpdir") + File.separator + "Zugslist.lua"
             );
 	    while ((len = in.read(b)) != -1) {
 		out.write(b, 0, len);
 	    }
 	    in.close();
 	    out.close();
 
 	    // copy zugslist.lua from tmp to every user account
 	    for(String account : accounts) {
 		File saved_var_dir = new File( account + File.separator + "SavedVariables" );
 		if( saved_var_dir.exists() && saved_var_dir.isDirectory() ) {
 		    File zugslist_path = new File(
 			account + File.separator + "SavedVariables" + File.separator + "Zugslist.lua"
 		    );
 		    try {
 			copy(
 			     System.getProperty("java.io.tmpdir") + File.separator + "Zugslist.lua",
 			     account + File.separator + "SavedVariables" + File.separator + "Zugslist.lua"
 			);
 		    } catch(java.io.IOException ioe) {
 			ioe.printStackTrace();
 		    }
 		}
 	    }
 	} catch(java.io.IOException ioe){
 	    notifyDOM("ZUGSLIST_DOWNLOAD_ERROR");
 	    ioe.printStackTrace();
 	} finally{
 	    http_client.getConnectionManager().shutdown();
 	}
     }
 
     private void copy(String fromFileName, String toFileName)
 	throws java.io.IOException {
 	File fromFile = new File(fromFileName);
 	File toFile = new File(toFileName);
 	
 	if (!fromFile.exists())
 	    throw new java.io.IOException("FileCopy: " + "no such source file: " + fromFileName);
 	if (!fromFile.isFile())
 	    throw new java.io.IOException("FileCopy: " + "can't copy directory: " + fromFileName);
 	if (!fromFile.canRead())
 	    throw new java.io.IOException("FileCopy: " + "source file is unreadable: " + fromFileName);
 	
 	FileInputStream from = null;
 	FileOutputStream to = null;
 	try {
 	    from = new FileInputStream(fromFile);
 	    to = new FileOutputStream(toFile);
 	    byte[] buffer = new byte[4096];
 	    int bytesRead;
 	    
 	    while ((bytesRead = from.read(buffer)) != -1)
 		to.write(buffer, 0, bytesRead); // write
 	} finally {
 	    if (from != null)
 		try {
 		    from.close();
 		} catch (java.io.IOException e) {
 		    ;
 		}
 	    if (to != null)
 		try {
 		    to.close();
 		} catch (java.io.IOException e) {
 		    ;
 		}
 	}
     }
     
     private String getServerURL() {
 	String url = getDocumentBase().toString();
 	url = url.replaceFirst("/\\w+\\.\\w+$", "");
 	return url + "/";
     }
 
     private void notifyDOM(String msg) {
 	try {
	    window.eval("applet_message_callback('" + msg + "')");
 	} catch (JSException jse) {
 	    jse.printStackTrace();
 	}
     }
 }
