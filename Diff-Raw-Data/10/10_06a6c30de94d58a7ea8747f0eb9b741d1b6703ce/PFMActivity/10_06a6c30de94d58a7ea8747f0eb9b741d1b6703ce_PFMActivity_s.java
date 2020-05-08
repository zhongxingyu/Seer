 package me.horzwxy.app.pfm.android.activity;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import me.horzwxy.app.pfm.model.Request;
 import me.horzwxy.app.pfm.model.Response;
 import me.horzwxy.app.pfm.model.User;
 
 /**
  * Created by horz on 9/8/13.
  */
 public class PFMActivity extends Activity {
 
     protected ArrayList<Activity> createdActivities = new ArrayList<Activity>();
     protected User currentUser;
 
     protected abstract class PFMHttpAsyncTask extends AsyncTask< Request, Void, Response> {
         protected static final String HOST_NAME = "http://pfm.horzwxy.me";
 
         @Override
         protected Response doInBackground(Request... infos) {
             Response response = null;
             Request request = infos[0];
             try {
                 URL postUrl = new URL( HOST_NAME + request.getRequestType().getServletPattern() );
                 HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
                 connection.setDoOutput(true);
                 connection.setDoInput(true);
                 connection.setRequestMethod("POST");
                 connection.setUseCaches(false);
                 connection.setInstanceFollowRedirects(true);
                 connection.setRequestProperty("Content-Type",
                         "application/x-www-form-urlencoded");
                 connection.connect();
                 PrintWriter writer = new PrintWriter( connection.getOutputStream() );
                 String content = request.getPostContent();
                 writer.println(content);
                 writer.flush();
                 writer.close();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader( connection.getInputStream() ) );
                 String line  = reader.readLine();
                 System.out.println( line );
                 response = Response.parseResponse( line );
                 reader.close();
             } catch (MalformedURLException e) {
                 e.printStackTrace();
             } catch (ProtocolException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return response;
         }
 
         @Override
         protected abstract void onPostExecute(Response response);
     }
 }
