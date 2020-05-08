 package net.scott.myfirstproject;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.Xml;
 import android.webkit.WebView;
 import android.widget.TextView;
 
 public class GetRiverLevelsActivity extends Activity{
 	TextView textView = null;
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         System.out.println( "Creating get river levels activity");
         //setContentView(R.layout.activity_get_river_levels);
         textView = new TextView(this);
         textView.setText("getting river data....");
         setContentView(textView);
         String stringUrl = "http://waterservices.usgs.gov/nwis/iv/?format=waterml,1.1&stateCd=wa&parameterCd=00060,00065&siteType=ST";
         new DownloadWebpageText().execute(stringUrl);
     }
 	
 //    public void getRiverDataFromNetwork(){
 //        // Gets the URL from the UI's text field.
 //        String stringUrl = "http://waterservices.usgs.gov/nwis/iv/?format=waterml,1.1&stateCd=wa&parameterCd=00060,00065&siteType=ST";
 //        ConnectivityManager connMgr = (ConnectivityManager) 
 //            getSystemService(Context.CONNECTIVITY_SERVICE);
 //        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 //        if (networkInfo != null && networkInfo.isConnected()) {
 //            new DownloadWebpageText().execute(stringUrl);
 //        } else {
 //        	
 //            textView.setText("No network connection available.");
 //        }
 //    }
     
    
 	
 	//////////////////////////////////////////
 	private class DownloadWebpageText extends AsyncTask<String, Void, String> {
         @Override
         protected  String doInBackground(String... urls) {              
             // params comes from the execute() call: params[0] is the url.
             try {
                 //return downloadUrl(urls[0]);
                 return loadXmlFromNetwork(urls[0]);
             } catch (IOException e) {
                 return "Unable to retrieve web page. URL may be invalid.";
             }catch (XmlPullParserException e) {
             return "Unable to parse XML.";
             }
         }
         // onPostExecute displays the results of the AsyncTask.
         @Override
         protected void onPostExecute(String xmlData) {
         	String result;
 //			try {
 //				//result = parseData(xmlData);
 //			} catch (XmlPullParserException e) {
 //				// TODO Auto-generated catch block
 //				result = e.getMessage();
 //			} catch (IOException e) {
 //				// TODO Auto-generated catch block
 //				result = e.getMessage();
 //			}
         	setContentView(R.layout.activity_get_river_levels);//setContentView(myWebView);
         	WebView myWebView = (WebView) findViewById(R.id.webview); //new WebView(this);
             //textView.setText("getting river data....");
            //setContentView(textView);
           
             // Displays the HTML string in the UI via a WebView
            // WebView myWebView = (WebView) findViewById(R.id.webview);
             myWebView.loadData(xmlData, "text/html", null);
             //textView.setText(xmlData);
        }
     }
     ////////////////////////////
     
     private String downloadUrl(String myurl) throws IOException {
         InputStream is = null;
         // Only display the first 500 characters of the retrieved
         // web page content.
         int len = 15000;
             
         try {
             URL url = new URL(myurl);
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setReadTimeout(10000 /* milliseconds */);
             conn.setConnectTimeout(15000 /* milliseconds */);
             conn.setRequestMethod("GET");
             conn.setDoInput(true);
             // Starts the query
             System.out.println("starting connection");
             conn.connect();
             int response = conn.getResponseCode();
             System.out.println( "The response is: " + response);
             Log.d("debug", "The response is: " + response);
             is = conn.getInputStream();
            // String contentAsString = ReadInputStream(is); //readIt(is, len);
             String contentAsString = readIt(is, len);
             System.out.println(contentAsString);
             return contentAsString;
             
         // Makes sure that the InputStream is closed after the app is
         // finished using it.
         } finally {
             if (is != null) {
                 is.close();
             } 
         }
     }
  public String parseData(String xmlData) throws XmlPullParserException, IOException
      {
      String result = "";
      String newLine = "" ;
 	 XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
          factory.setNamespaceAware(true);
          XmlPullParser xpp = factory.newPullParser();
          Boolean printText = false;
          xpp.setInput( new StringReader ( xmlData) );
          int eventType = xpp.getEventType();
          while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG &&  (xpp.getName().equals("siteName") || xpp.getName().equals("value"))) {
               //result += ("Start tag "+ xpp.getName());
               printText = true;
               if (xpp.getName().equals("value")){
             	  newLine = "\n"; 
               }
 //          } else if(eventType == XmlPullParser.END_TAG &&  xpp.getName().equals("siteName")) {
 //        	  result += ("End tag "+xpp.getName());
           } else if(eventType == XmlPullParser.TEXT && printText == true) {
         	  result += (xpp.getText()+newLine);
         	  printText = false;
         	  newLine =" ";
           }
           eventType = xpp.next();
          }
          return result;
         }
 
 	// Reads an InputStream and converts it to a String.
     public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
         Reader reader = null;
         reader = new InputStreamReader(stream, "UTF-8");        
         char[] buffer = new char[len];
         reader.read(buffer);
         return new String(buffer);
     }
     
     public static String ReadInputStream(InputStream in) throws IOException {
     	StringBuffer stream = new StringBuffer();
     	byte[] b = new byte[4096];
     	for (int n; (n = in.read(b)) != -1;) {
     	stream.append(new String(b, 0, n));
     	}
     	return stream.toString();
     	}
     
     public List parse(InputStream in) throws XmlPullParserException, IOException {
         try {
         	 XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
              factory.setNamespaceAware(true);
              XmlPullParser parser = factory.newPullParser();
            //XmlPullParser parser = Xml.newPullParser();
             parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
             parser.setInput(in, null);
             parser.nextTag();
             return readFeed(parser);
         } finally {
             in.close();
         }
     }
     
     private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
         List entries = new ArrayList();
 
         parser.require(XmlPullParser.START_TAG, null, "ns1:timeSeriesResponse");
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             // Starts by looking for the entry tag
             if (name.equals("ns1:timeSeries")) {
                 Entry entry = readEntry(parser);
                 if(entry != null) entries.add(entry);
             } else {
                 skip(parser);
             }
         }  
         return entries;
     }
     
     public static class Entry {
         public final String name;
         public final String flow;
 
         private Entry(String name, String flow) {
             this.name = name;
             this.flow = flow;
         }
     }
     private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
         parser.require(XmlPullParser.START_TAG, null, "ns1:timeSeries");
         String name = null;
         String flow = null;
         String riverName = null;
         Boolean ofInterest = false;
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
              name = parser.getName();
             if (name.equals("ns1:sourceInfo")) {
                 riverName = readName(parser);
             } else if (name.equals("ns1:variable")) {
                 ofInterest = readVariableDescription(parser);    
             } else if (name.equals("ns1:values")) {
                 flow = readFlow(parser);
             } else {
                 skip(parser);
             }
         }
         if (ofInterest == true) return new Entry(riverName, flow);
         return null;
     }
     private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
     	String riverName = "";
         parser.require(XmlPullParser.START_TAG, null, "ns1:sourceInfo");
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             // Starts by looking for the entry tag
             if (name.equals("ns1:siteName")) {
             	riverName = readText(parser);
             } else {
                 skip(parser);
             }
         }
         parser.require(XmlPullParser.END_TAG, null, "ns1:sourceInfo");
         return riverName;
     }
     
     private String readFlow(XmlPullParser parser) throws IOException, XmlPullParserException {
         String flow = "";
     	parser.require(XmlPullParser.START_TAG, null, "ns1:values");
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             // Starts by looking for the entry tag
             if (name.equals("ns1:value")) {
             	flow = readText(parser);
             } else {
                 skip(parser);
             }
         }  
         parser.require(XmlPullParser.END_TAG, null, "ns1:values");
         return flow;
     }
     
     private Boolean readVariableDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
     	String variableDescription = "";
     	Boolean ofInterest = false;
     	parser.require(XmlPullParser.START_TAG, null, "ns1:variable");
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             // Starts by looking for the entry tag
             if (name.equals("ns1:variableDescription")) {
             	variableDescription = readText(parser);
             	ofInterest = variableDescription.startsWith("Discharge");
             } else {
                 skip(parser);
             }
         }         
         return ofInterest;
     }
     
     private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
         String result = "";
         if (parser.next() == XmlPullParser.TEXT) {
             result = parser.getText();
             parser.nextTag();
         }
         return result;
     }
     
     private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
         if (parser.getEventType() != XmlPullParser.START_TAG) {
             throw new IllegalStateException();
         }
         int depth = 1;
         while (depth != 0) {
             switch (parser.next()) {
             case XmlPullParser.END_TAG:
                 depth--;
                 break;
             case XmlPullParser.START_TAG:
                 depth++;
                 break;
             }
         }
      }
     
     private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
         InputStream stream = null;
         // Instantiate the parser
         List<Entry> entries = null;
         String name = null;
         String flow = null;
         String summary = null;
 
             
         StringBuilder htmlString = new StringBuilder();
         
         
 
 
        htmlString.append("<table border='1'>  <tr> <th>River</th> <th>Flow in CFS</th>  </tr>");
 //        htmlString.append("<em>" + getResources().getString(R.string.updated) + " " + 
 //                formatter.format(rightNow.getTime()) + "</em>");
             
         try {
             stream = downloadXMLUrl(urlString);        
             entries = parse(stream);
         // Makes sure that the InputStream is closed after the app is
         // finished using it.
         } finally {
             if (stream != null) {
                 stream.close();
             } 
          }
         
 //      <tr>
 //      <td>row 1, cell 1</td>
 //      <td>row 1, cell 2</td>
 //      </tr>
         for (Entry entry : entries) {       
             htmlString.append("<tr><td>" + entry.name + "</td>" );
             htmlString.append("<td>" + entry.flow + "</td></tr>" );
         }
         htmlString.append("</table>");
         return htmlString.toString();
     }
     
     private InputStream downloadXMLUrl(String urlString) throws IOException {
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
