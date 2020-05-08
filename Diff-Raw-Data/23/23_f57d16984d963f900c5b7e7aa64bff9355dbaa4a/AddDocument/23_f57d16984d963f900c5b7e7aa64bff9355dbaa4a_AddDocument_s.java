 package info.matthewwardrop.scholarley.feeder.arxiv;
 
 import info.matthewwardrop.scholarley.feeder.arxiv.FeedParser.ArxivDocument;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 
 public class AddDocument extends Activity {
 	
 	@Override
 	protected void onCreate(android.os.Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Intent intent = this.getIntent();
 		String pdf_uri = intent.getDataString();
 		
 		Uri pdfuri = Uri.parse(pdf_uri);
 		String arxiv_id = pdfuri.getLastPathSegment().replace(".pdf", "");
 		
 		String arxiv_uri = "http://export.arxiv.org/api/query?id_list=" + arxiv_id;
 		new DownloadXmlTask().execute(arxiv_uri);
 	};
 	
 	private void sendToScholarley(ArxivDocument document) {
 		
 		final Intent intent = new Intent("info.matthewwardrop.scholarley.RECEIVE_FEED");
 		intent.addCategory("android.intent.category.DEFAULT");
 		intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
 		intent.setClassName("info.matthewwardrop.scholarley","info.matthewwardrop.scholarley.activity.AddDocument");
 		
 		intent.putExtra("feeder:name", "Scholarley arXiv Feeder");
 		intent.putExtra("feeder:attachment", document.attachment);
 		intent.putExtra("feeder:uri", this.getIntent().getDataString());
 		intent.putExtra("document:title", document.title);
 		intent.putExtra("document:arxiv", document.arxiv_id);
 		intent.putExtra("document:abstract", document.doc_abstract);
 		intent.putExtra("document:url", document.link);
 		intent.putExtra("document:type", "Journal Article");
 		intent.putExtra("document:authors", getJSONAuthors(document.authors));
 		
 		startActivity(intent);
 	}
 	
 	private String getJSONAuthors(List<String> authors) {
 		JSONArray JSONauthors = new JSONArray();
 		for (String author : authors) {
 			if (author != null) {
 				String[] authorParts = author.trim().split(" ");
 				JSONObject JSONauthor = new JSONObject();
 				String forename = "";
 				for (int i = 0; i <authorParts.length - 1; i++) {
 					forename += " " + authorParts[i];
 				}
 				try {
 					JSONauthor.put("forename", forename.trim());
 					JSONauthor.put("surname",authorParts[authorParts.length -1]);
 					JSONauthors.put(JSONauthor);
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 		}
 		return JSONauthors.toString();
 	}
 	
 	// Implementation of AsyncTask used to download XML feed from arxiv.org .
 	private class DownloadXmlTask extends AsyncTask<String, Void, List<ArxivDocument>> {
 	    @Override
 	    protected List<ArxivDocument> doInBackground(String... urls) {
 	        try {
 	            return loadXmlFromNetwork(urls[0]);
 	        } catch (IOException e) {
 	        	e.printStackTrace();
 	        	return null;
 	            //return getResources().getString(R.string.connection_error);
 	        } catch (XmlPullParserException e) {
 	        	e.printStackTrace();
 	        	return null;
 	           //return getResources().getString(R.string.xml_error);
 	        }
 	    }
 
 	    @Override
 	    protected void onPostExecute(List<ArxivDocument> arxivDocuments) {  
 	    	// Send documents to Scholarley
 	    	for (ArxivDocument doc : arxivDocuments) {
 	    		sendToScholarley(doc);
 	    	}
 	    	
 	    	AddDocument.this.finish();
 	    }
 	}
 	
 	// Uploads XML from stackoverflow.com, parses it, and combines it with
 	// HTML markup. Returns HTML string.
 	private List<ArxivDocument> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
 	    InputStream stream = null;
 	    // Instantiate the parser
 	    FeedParser arxivXmlParser = new FeedParser();
 	    List<ArxivDocument> entries = null;
 	    try {
 	        stream = downloadUrl(urlString);
 	        entries = arxivXmlParser.parse(stream);
 	    // Makes sure that the InputStream is closed after the app is
 	    // finished using it.
 	    } finally {
 	        if (stream != null) {
 	            stream.close();
 	        } 
 	     }
 	    
 	    return entries;
 	}
 
 	// Given a string representation of a URL, sets up a connection and gets
 	// an input stream.
 	private InputStream downloadUrl(String urlString) throws IOException {
 	    URL url = new URL(urlString);
 	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 	    conn.setReadTimeout(10000 /* milliseconds */);
 	    conn.setConnectTimeout(15000 /* milliseconds */);
 	    conn.setRequestMethod("GET");
 	    conn.setDoInput(true);
 	    // Starts the query
 	    conn.connect();
 	    return conn.getInputStream();
 	}
 }
