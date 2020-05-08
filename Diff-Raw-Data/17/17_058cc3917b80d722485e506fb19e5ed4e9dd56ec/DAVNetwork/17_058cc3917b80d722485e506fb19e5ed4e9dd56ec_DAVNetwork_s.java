 package edu.sjsu.cs.davsync;
 
 import android.os.Environment;
 import android.util.Log;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.File;
 import java.io.InputStream;
 import java.lang.IllegalArgumentException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.FileRequestEntity;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.jackrabbit.webdav.MultiStatus;
 import org.apache.jackrabbit.webdav.MultiStatusResponse;
 import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
 import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
 import org.apache.jackrabbit.webdav.property.PropEntry;
 import org.apache.jackrabbit.webdav.DavException;
 
 public class DAVNetwork {
 	File path;
 	private String url;
 	private Credentials creds;
 	HttpClient client;
 	
 	public DAVNetwork(Profile profile) {
 		url = "https://" + profile.getHostname() + profile.getResource();
 		creds = new UsernamePasswordCredentials(profile.getUsername(), profile.getPassword());
 		File sdcard = Environment.getExternalStorageDirectory();
 		path = new File( sdcard.getAbsolutePath() + profile.getResource() );
 		path.getParentFile().mkdirs();
 		client = new HttpClient();
 		client.getState().setCredentials(AuthScope.ANY, creds);
 		client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
 	}
 
 	public boolean testRemote() {
 		try {
 			PropFindMethod pfm = new PropFindMethod(url);
             int ret = client.executeMethod(pfm);
 
             if( (ret == HttpStatus.SC_MULTI_STATUS && pfm.succeeded()) || ret == HttpStatus.SC_NOT_FOUND ) {
             	// not found (404) isn't an error in this sense since it only means
             	// that the remote file doesn't exist - BUT, this means we can log in
             	pfm.releaseConnection();
             	return true;
             } else {
             	pfm.releaseConnection();
             	return false;
             }
 		} catch( IOException ioe ) {
 			// also handles HttpException from the client
 			return false;
 		} catch( IllegalArgumentException iae ) {
 			// from client, in case something is wrong with 'url'
 			return false;
 		}
 	}
 	
 	public boolean sync() throws HttpException, IOException, IllegalArgumentException, DavException {
 		boolean has_remote = false, has_local = false;
 		
 		// first check if the file exists
         PropFindMethod pfm = new PropFindMethod(url);
         int ret = client.executeMethod(pfm);
         if( ret == HttpStatus.SC_MULTI_STATUS && pfm.succeeded() )
         	has_remote = true;
         pfm.releaseConnection();
         if( path.exists() )
         	has_local = true;
         
         if( has_local == true && has_remote == false ) {
         	Log.d("DAVNetwork::sync", "Uploading local file");
         	return upload();
         } else if( has_local == false && has_remote == true ) {
         	Log.d("DAVNetwork::sync", "Downloading remote file");
         	return download();
         } else if( has_local == false && has_remote == false ) {
         	// FIXME: create a new KDB file
         	Log.d("DAVNetwork::sync", "New KDB file creation unimplemented");
         	return false;
         } else {
         	MultiStatusResponse[] msr = pfm.getResponseBodyAsMultiStatus().getResponses();
         	if( msr.length != 1 ) {
         		// FIXME: how do we handle this?
         		Log.d("DAVNetwork::sync", "Got " + msr.length + " MultiStatusResponse objects");
         		return false;
         	}
         	
         	String dateString = null;
         	Iterator<? extends PropEntry> iter = msr[0].getProperties(HttpStatus.SC_OK).getContent().iterator();
         	while( iter.hasNext() ) {
         		DefaultDavProperty tmp = (DefaultDavProperty)iter.next();
         		if( tmp.getName().toString().equals("{DAV:}getlastmodified") ) {
         			dateString = tmp.getValue().toString();
         			break;
         		}
         	}
 
         	if( dateString == null ) {
         		Log.d("DAVNetwork::sync", "Didn't get a last modified string");
         		return false;
         	}
 
        	// FIXME: this doesn't work right now
         	Date date_remote;
         	try {
        		SimpleDateFormat df = new SimpleDateFormat();
        		date_remote = df.parse(dateString);
         	} catch( ParseException pe ) {
         		Log.d("DAVNetwork::sync", "Unable to parse remote timestamp: " + dateString);
         		return false;
         	}
         	
         	Date date_local = new Date(path.lastModified());
         	
         	int comparator = date_local.compareTo(date_remote);
         	if( comparator < 0 ) {
         		Log.d("DAVNetwork::sync", "Final decision: download");
         		return download();
         	} else if( comparator > 0 ) {
         		Log.d("DAVNetwork::sync", "Final decision: upload");
         		return upload();
         	} else {
         		// the files are already synced, we do nothing
         		Log.d("DAVNetwork::sync", "Final decision: the files are equal");
         		return true;
         	}
         }
 	}
 	
 	private boolean upload() {
 		int ret = -1;
 		final String TAG = "DAVNetwork::upload";
         
         PutMethod pm = new PutMethod(url);
 		pm.setRequestEntity(new FileRequestEntity(path, "binary/octet-stream"));
 		try {
 			ret = client.executeMethod(pm);
 		} catch (HttpException he) {
 			Log.w(TAG, "Caught HttpException: " + he.getMessage());
 		} catch (IOException ioe) {
 			Log.w(TAG, "Caught IOException: " + ioe.getMessage());
 		} finally {
 			pm.releaseConnection();
 		}
 		
 		if ( ret != HttpStatus.SC_NO_CONTENT ) {
 			Log.d(TAG, "Failed to execute Put method: " + ret);
 			return false;
 		} else {
 			Log.d(TAG, "Put method successfully completed");
 			return true;
 		}
 	}
 	
 	private boolean download() {
 		int ret = -1;
 		final String TAG = "DAVNetwork::upload";
 		boolean fail = false;
 		GetMethod gm = new GetMethod(url);
 		gm.setFollowRedirects(true);
 		try {
 			ret = client.executeMethod(gm);
 			// http://www.eboga.org/java/open-source/httpclient-demo.html
 			InputStream input = gm.getResponseBodyAsStream();
 			FileOutputStream output = new FileOutputStream(path, false);
 			int count = -1;
 			byte[] buffer = new byte[8192];
 			while( (count = input.read(buffer)) != -1 ) {
 				output.write(buffer, 0, count);
 			}
 			output.flush();
 			output.close();
 		}catch (HttpException he) {
 			Log.w(TAG, "Caught HttpException: " + he.getMessage());
 			fail = true;
 		} catch (IOException ioe) {
 			Log.w(TAG, "Caught IOException: " + ioe.getMessage());
 			fail = true;
 		} finally {
 			gm.releaseConnection();
 		}
 		
 		if ( ret != HttpStatus.SC_OK || fail == true ) {
 			Log.d(TAG, "Failed to execute Get method: " + ret);
 			return false;
 		} else {
 			Log.d(TAG, "Get method successfully completed");
 			return true;
 		}
 	}
 }
