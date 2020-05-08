 package net;
 
 import models.Entry;
 
 import android.util.Log;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 
 public class JavaConnection implements Connection {
 	
 	private static final String TAG = "JavaConnection";
 	private static final String HOST = "http://127.0.0.1:3000/";
 
 	protected JavaConnection() {}
 
 	@Override
 	public void add(String title, String content) {
 		// TODO Auto-generated method stub
		connect();
 	}
 
 	@Override
 	public Entry[] getContent() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void pushRating(int contentID) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public int getAverage(int contentID) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int[] getRatings(int contentID) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	/**
 	 * Makes an HTTP GET request to the target (HOST + url)
 	 * @param url
 	 * @return the HTTP response
 	 * @throws IOException 
 	 */
 	private String get(String url) throws IOException {
 		URL u = null;
 		HttpURLConnection conn = null;
 		BufferedReader r = null;
 
 		try {
 			u = new URL(HOST + url);
 			conn = (HttpURLConnection) u.openConnection();
 			InputStream in = new BufferedInputStream(conn.getInputStream());
 			
 			// Builds string from input stream.
 			StringBuilder sb = new StringBuilder();
 			r = new BufferedReader(new InputStreamReader(in));
 			while (r.ready())
 				sb.append(r.readLine());
 			return sb.toString();
 		} finally {
 			if (r != null)
 				r.close();
 			if (conn != null)
 				conn.disconnect();
 		}
 	}
 	
 	/**
 	 * Makes an HTTP POST request to the target (HOST + url)
 	 * @param url
 	 * @param post the content of the POST
 	 * @return
 	 * @throws IOException 
 	 */
 	private boolean post(String url, String post) throws IOException {
 		URL u = null;
 		HttpURLConnection conn = null;
 		BufferedReader r = null;
 
 		try {
 			u = new URL(HOST + url);
 			conn = (HttpURLConnection) u.openConnection();
 			conn.setDoOutput(true);
 			conn.setRequestMethod("POST");
 			conn.setRequestProperty("Content-Type", "application/json");
 			conn.setRequestProperty("Accept", "application/json");
 			conn.setChunkedStreamingMode(0);
 			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
 			wr.write(post);
 			conn.connect();
 			
 			int rc = conn.getResponseCode();
 			return rc < 300;
 			
 		} finally {
 			if (r != null)
 				r.close();
 			if (conn != null)
 				conn.disconnect();
 		}
 	}
 	
 }
