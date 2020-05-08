 package com.qut.spc.task;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.os.AsyncTask;
 import android.util.Xml;
 
 /**
  * Implement class should override onPostExecute to know when it's done
  * 
  * @author QuocViet
  * @see http://developer.android.com/training/basics/network-ops/xml.html
  */
 public abstract class XmlRequestTask extends AsyncTask<String, Void, XmlPullParser> {
	private final int READ_TIMEOUT = 30000; /* milliseconds */
	private final int CONNECT_TIMEOUT = 60000; /* milliseconds */
 
 	private String method = "GET";
 
 	public XmlRequestTask() {
 		
 	}
 	
 	@Override
 	protected XmlPullParser doInBackground(String... params) {
 		InputStream stream;
 		try {
 			stream = getDownloadStream(params[0]);
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			return null;
 		}
 		try {
 			return parseXml(stream);
 		} catch (XmlPullParserException ex) {
 			ex.printStackTrace();
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	protected abstract void onXmlTag(XmlPullParser parser, int eventType);
 
 	private XmlPullParser parseXml(InputStream stream)
 			throws XmlPullParserException, IOException {
 		XmlPullParser parser = Xml.newPullParser();
 
 		parser.setInput(stream, "UTF_8");
 		int type = parser.getEventType();
 		while (type != XmlPullParser.END_DOCUMENT) {
 			onXmlTag(parser, type);
 			type = parser.next();
 		}
 		return parser;
 	}
 	
 	// Given a string representation of a URL, sets up a connection and gets
 	// an input stream.
 	private InputStream getDownloadStream(String url) throws IOException {
 		URL u = new URL(url);
 		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
 
 		conn.setReadTimeout(READ_TIMEOUT);
 		conn.setConnectTimeout(CONNECT_TIMEOUT);
 		conn.setRequestMethod(method);
 		conn.setDoInput(true);
 
 		// Starts the query
 		conn.connect();
 		return conn.getInputStream();
 	}
 }
