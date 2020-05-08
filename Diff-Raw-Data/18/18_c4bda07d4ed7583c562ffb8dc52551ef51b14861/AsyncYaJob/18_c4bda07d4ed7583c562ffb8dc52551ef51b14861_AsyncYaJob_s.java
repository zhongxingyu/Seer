 package anywayanyday.pointsonmap;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.widget.ImageView;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 
 public class AsyncYaJob {
 
     private YaListener yaListener;
 
     public interface YaListener{
         public void onYaResponse(String[] request, String response);
         public void sendToast(String text);
         public Context getContext();
     }
 
     public AsyncYaJob(String [] request, YaListener yaListener) {
         this.yaListener = yaListener;
         AsyncPointRequest searchInMoscow = new AsyncPointRequest();
         searchInMoscow.isInMoscow = true;
         searchInMoscow.execute(request);
     }
 
 
 
     public AsyncYaJob(ImageView imageView, String url) {
         new DownloadImage(imageView).execute(url);
     }
 
     private class AsyncPointRequest extends AsyncTask<String, Void, String> {
 
         public static final String REQUEST_IN_MOSCOW = "http://geocode-maps.yandex.ru/1.x/?rspn=1&ll=37.609218,55.753559&spn=0.552069,0.400552&results=1&geocode=";
         public static final String REQUEST_IN_WORLD = "http://geocode-maps.yandex.ru/1.x/?ll=37.609218,55.753559&spn=0.552069,0.400552&results=1&geocode=";
         private String[] request;
         boolean isInMoscow;
         @Override
         protected String doInBackground(String... request)  {
             this.request = request;
             String address = request[0];
             address = address.replace(" ", "+");
             HttpGet httpRequest = isInMoscow ? new HttpGet(REQUEST_IN_MOSCOW + address) : new HttpGet(REQUEST_IN_WORLD + address);
             return getGeoData(httpRequest(httpRequest));
         }
 
         @Override
         protected void onPostExecute(String results)
         {
             if (results == null && isInMoscow){
                 yaListener.sendToast(yaListener.getContext().getResources().getString(R.id.start_adv_search));
                 AsyncPointRequest searchInMoscow = new AsyncPointRequest();
                 searchInMoscow.isInMoscow = false;
                 searchInMoscow.execute(request);
             }else{
             yaListener.onYaResponse(request, results);
             }
         }
     }
 
     private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
         ImageView bmImage;
 
         public DownloadImage(ImageView bmImage) {
             this.bmImage = bmImage;
         }
 
         protected Bitmap doInBackground(String... urls) {
             String urlDisplay = urls[0];
             Bitmap mIcon11 = null;
             try  {
                 InputStream in = new java.net.URL(urlDisplay).openStream();
                 mIcon11 = BitmapFactory.decodeStream(in);
                 in.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return mIcon11;
         }
 
         protected void onPostExecute(Bitmap result) {
             bmImage.setImageBitmap(result);
         }
     }
 
     static private String httpRequest(HttpGet request) {
         String data = null;
         try {
             HttpClient client = new DefaultHttpClient();
             HttpResponse response = client.execute(request);
             response.getStatusLine().getStatusCode();
 
             BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
             StringBuilder sb = new StringBuilder("");
             String line;
             while ((line = in.readLine()) != null) {
                 sb.append(line);
             }
             in.close();
             client.getConnectionManager().shutdown();
             data = sb.toString();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return data;
     }
 
     static private String getGeoData(String data) {
         try {
             XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
             XmlPullParser parser = factory.newPullParser();
             factory.setNamespaceAware(true);
             parser.setInput(new StringReader(data));
             return parseForGeoData(parser);
         } catch (XmlPullParserException | IOException e) {
             e.printStackTrace();
         }
         return null;
 
     }
 
     static private String parseForGeoData(XmlPullParser parser) throws XmlPullParserException, IOException {
         int eventType = parser.getEventType();
         while(eventType != XmlPullParser.END_DOCUMENT){
             if(eventType == XmlPullParser.START_TAG){
                 String name = parser.getName();
                 if ( name.equalsIgnoreCase("pos"))  {
                     parser.next();
                     return parser.getText();
                 }
             }
             eventType = parser.next();
         }
         return null;
     }
 
 }
