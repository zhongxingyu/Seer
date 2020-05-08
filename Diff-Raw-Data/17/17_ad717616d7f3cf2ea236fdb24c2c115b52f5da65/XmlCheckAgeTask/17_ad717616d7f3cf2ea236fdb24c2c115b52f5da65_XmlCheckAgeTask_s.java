 package edu.grinnell.sandb.xmlpull;
  
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
 import android.os.AsyncTask;
  
 public class XmlCheckAgeTask extends AsyncTask<String, Integer, Long> {
 	
 	public static interface CheckAgeListener {
 		public void receive(Long result);
 	}
 	
 	private final CheckAgeListener mListener;
 	
 	public XmlCheckAgeTask(CheckAgeListener l) {
 		super();
 		this.mListener = l;
 	}
 	
 	@Override
 	protected Long doInBackground(String... args) {
 		return lastModified(args[0]);
 	}
  
 	@Override
 	protected void onPostExecute(Long result) {
 		mListener.receive(result);
 	}
 
 	// GET THE LAST MODIFIED TIME
 	public static long lastModified(String url) {
 		HttpURLConnection.setFollowRedirects(false);
 	    HttpURLConnection con;
 	    long date = 0;
 	    try {
 	    	con = (HttpURLConnection) new URL(url).openConnection();
 	    	date = con.getLastModified();
 	    	if (date == 0)
	    		System.out.println("No last-modified information.");
 			else
				System.out.println("Last-Modified: " + new Date(date));
 	    } catch (MalformedURLException e) {
 	    	e.printStackTrace();
 	    } catch (IOException e) {
 	    	e.printStackTrace();
 	    }
 	    return date;
 	}
 }
