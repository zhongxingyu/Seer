 package com.example.weatherdb;
 import com.example.weatherdb.R;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 // http://stackoverflow.com/questions/18123734/how-to-get-the-current-location-temperature-in-android
 // http://androidcodebits.wordpress.com/2013/07/20/integration-of-yahoo-weather-api-in-android/
 // http://www.vogella.com/articles/EGit/article.html
 
 // AsyncTask class in Andriod:
 // http://developer.android.com/reference/android/os/AsyncTask.html
 public class FetchForcast extends AsyncTask<String, Void, String> {
 	String temperature, date;
 	ProgressDialog dialog;
 	Bitmap icon = null;
 	
 	private View mView;
 	public FetchForcast (View mainView) {
 		mView = mainView;
 	}
 	@Override
     protected String doInBackground(String... zipcode) {
         
         String qResult = "OK";
 
         // Query yahoo weather by Zip code
         // http://stackoverflow.com/questions/2118289/undocumented-yahoo-weather-api
         HttpClient httpClient = new DefaultHttpClient();
         HttpContext localContext = new BasicHttpContext();
        //HttpGet httpGet = new HttpGet("http://weather.yahooapis.com/forecastrss?w=2295425&u=c&#8221;");
        HttpGet httpGet = new HttpGet("http://xml.weather.yahoo.com/forecastrss?p="+zipcode[0]);
         
         try {
         	// Initiate HTTP request
             HttpResponse response = httpClient.execute(httpGet, localContext);
             HttpEntity entity = response.getEntity();
             
             // Response OK. Convert the received XML file into a big string
             if (entity != null) {
                 InputStream inputStream = entity.getContent();
                 Reader httpStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedreader = new BufferedReader(httpStreamReader);
                 
                 // Put the xml file into a big string
                 StringBuilder stringBuilder = new StringBuilder();
                 String stringReadLine = null;
                 
                 // Read the input xml file line by line
                 while ((stringReadLine = bufferedreader.readLine()) != null) {
                     stringBuilder.append(stringReadLine + "\n");
                 }
                 qResult = stringBuilder.toString();
             }
 
         } catch (ClientProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
 			e.printStackTrace();
 		}
         
         // ---------- Parse the xml file -------------
         Document dest = null;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         //DocumentBuilder parser; // java xml parser
         try {
         	// You can only instantiate DocumentBuilder from the factory
         	DocumentBuilder parser = dbFactory.newDocumentBuilder();
             dest = parser.parse(new ByteArrayInputStream(qResult.getBytes()));
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
         //  read an XML file via DOM XML parser
         // http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
         
         // read temperature value
         Node temperatureNode = dest.getElementsByTagName("yweather:condition").item(0);
         temperature = temperatureNode.getAttributes().getNamedItem("temp").getNodeValue().toString();
         
         // read unit
         Node tempUnitNode = dest.getElementsByTagName("yweather:units").item(0);
         // Is it 'C or 'F?
         String unit = tempUnitNode.getAttributes().getNamedItem("temperature").getNodeValue().toString();
         temperature = temperature + unit;
 
         // read date
         Node dateNode = dest.getElementsByTagName("pubDate").item(0);
         date = dateNode.getTextContent();
 
         /*
         date = dateNode.getAttributes().getNamedItem("date").getNodeValue().toString();
         String desc = dest.getElementsByTagName("item").item(0)
             .getChildNodes().item(13).getTextContent().toString();
         StringTokenizer str = new StringTokenizer(desc, "<=>");
         System.out.println("Tokens: " + str.nextToken("=>"));
         
         String src = str.nextToken();
         System.out.println("src: " + src);
         String url1 = src.substring(1, src.length() - 2);
         Pattern TAG_REGEX = Pattern.compile("(.+?)<br />");
         Matcher matcher = TAG_REGEX.matcher(desc);
         
         //while (matcher.find()) {
         //    MainActivity.add(matcher.group(1));
         //}
          */
 
 
         /*
         Pattern links = Pattern.compile("(.+?)<BR/>");
         matcher = links.matcher(desc);
         
         while(matcher.find()){
             System.out.println("Match Links: "+ (matcher.group(1)));
             String link = matcher.group(1);
         }
         */
 
         /*
         InputStream in = null;
         try {
             int response = -1;
             URL url = new URL(url1);
             URLConnection conn = url.openConnection();
 
             if (!(conn instanceof HttpURLConnection))
                 throw new IOException("Not an HTTP connection");
             
             HttpURLConnection httpConn = (HttpURLConnection) conn;
             httpConn.setAllowUserInteraction(false);
             httpConn.setInstanceFollowRedirects(true);
             httpConn.setRequestMethod("GET");
             httpConn.connect();
 
             response = httpConn.getResponseCode();
             if (response == HttpURLConnection.HTTP_OK) {
                 System.out.println("**************");
                 in = httpConn.getInputStream();
             }
             icon = BitmapFactory.decodeStream(in);
             in.close();
         } catch (IOException e1) {
             e1.printStackTrace();
         }
         */
         return qResult;
     }
     
 
     protected void onPreExecute(){
         //dialog = new ProgressDialog(Weather.this);
         //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         //dialog.setMessage(“Loading…”);
         //dialog.setCancelable(false);
         //dialog.show();
     }
 
     /*
      * P401 of Busy Coder book
      * This method is called, from the UI thread, after doInBackground()
      * completes. It receives, as a parameter, the value returned by
      * doInBackground() (e.g., success or failure flag). 
      */
     @Override
     protected void onPostExecute(String result) {
     	System.out.println("POST EXECUTE");
     	
     	//if(dialog.isShowing())
     	//	dialog.dismiss();
     	TextView dateText = (TextView) mView.findViewById(R.id.dateText);
     	dateText.setText("Date: "+date);
     	
     	
     	TextView tempText = (TextView) mView.findViewById(R.id.tempText);
     	tempText.setText("Temperature: "+temperature);
     	
     	/*
     	ImageView image = (ImageView) mView.findViewById(R.id.icon);
     	image.setImageBitmap(icon);
     	*/
     }
 }
 
 
 
