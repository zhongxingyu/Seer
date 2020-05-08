 package ru.shutoff.caralarm;
 
 import android.os.AsyncTask;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.ByteArrayOutputStream;
 
 public abstract class HttpTask extends AsyncTask<String, Void, JSONObject> {
 
     abstract void result(JSONObject res) throws JSONException;
 
     abstract void error();
 
     void background(JSONObject res) throws JSONException {
     }
 
     int pause = 0;
     String error_text;
 
     @Override
     protected JSONObject doInBackground(String... params) {
         HttpClient httpclient = new DefaultHttpClient();
         String url = params[0];
         for (int i = 1; i < params.length; i++) {
             url = url.replace("$" + i, params[i]);
         }
         try {
             if (pause > 0)
                 Thread.sleep(pause);
             error_text = null;
             HttpResponse response = httpclient.execute(new HttpGet(url));
             StatusLine statusLine = response.getStatusLine();
             int status = statusLine.getStatusCode();
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             response.getEntity().writeTo(out);
             out.close();
             String res = out.toString();
             JSONObject result = new JSONObject(res);
             if (status != HttpStatus.SC_OK) {
                 error_text = result.getString("error");
                 return null;
             }
             background(result);
             return result;
         } catch (Exception ex) {
            ex.printStackTrace();
             // ignore
         }
         return null;
     }
 
     @Override
     protected void onPostExecute(JSONObject res) {
         if (res != null) {
             try {
                 result(res);
                 return;
             } catch (Exception ex) {
                 // ignore
                ex.printStackTrace();
                 State.appendLog("onPost");
                 State.print(ex);
             }
         }
         error();
     }
 
 }
