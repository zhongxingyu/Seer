 package ca.ualberta.cmput301project;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
import android.util.Log;
 
 
 public class ExternalTaskManager
 {
     private String baseURL = "http://crowdsourcer.softwareprocess.es/F12/CMPUT301F12T02/";
     StringBuilder builder = new StringBuilder();
     public ExternalTaskManager(){
         
     }
     private void internetFetch(HttpGet httpGet){
         try {
 
             HttpClient client = new DefaultHttpClient();
             HttpResponse response = client.execute(httpGet);
             StatusLine statusLine = response.getStatusLine();
             int statusCode = statusLine.getStatusCode();
             if (statusCode == 200) {
               HttpEntity entity = response.getEntity();
               InputStream content = entity.getContent();
               BufferedReader reader = new BufferedReader(new InputStreamReader(content));
               String line;
               while ((line = reader.readLine()) != null) {
                 builder.append(line);
               }
            } else {
              Log.e(Activity_placeholder.class.toString(), "Failed to download file");
             }
           } catch (ClientProtocolException e) {
             e.printStackTrace();
           } catch (IOException e) {
             e.printStackTrace();
           }
     }
     public String readAllTasks() {
         HttpGet httpGet = new HttpGet(baseURL+"?action=list");
         internetFetch(httpGet);
         return builder.toString();
     }
     
     public String readTask(String id){
         HttpGet httpGet = new HttpGet(baseURL+"?action=get&id="+id);
         internetFetch(httpGet);
         return builder.toString();
     }
     public String removeTask(String id){
         HttpGet httpGet = new HttpGet(baseURL+"?action=remove&id="+id);
         internetFetch(httpGet);
         return builder.toString();
     }
     public void addTask(Task task){
         String reqPhoto, reqAudio;
         if(task.getReqPhoto()){
             reqPhoto = "true";
         }
         else {reqPhoto = "false";}
         if(task.getReqAudio()){
             reqAudio = "true";
         }
         else {reqAudio = "false";}
         JSONObject object = new JSONObject();
         try {
             object.put("description", task.getDescription());
             object.put("reqPhoto", reqPhoto);
             object.put("reqAudio", reqAudio);
             object.put("timestamp", task.gettimestamp());
           } catch (JSONException e) {
             e.printStackTrace();
           }
         HttpGet httpGet = new HttpGet(baseURL+"?action=post&summary=taskrequest&content="+object.toString()+"&description=sampledescription");
         internetFetch(httpGet);
     }
     public void updateTask(Task task, String id){
        JSONObject object;
         try
         {
             object = new JSONObject(readTask(id));
         } catch (JSONException e1)
         {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
        JSONObject oldContent;
         try
         {
             oldContent = new JSONObject(object.getString("content"));
         } catch (JSONException e1)
         {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         try {
             oldContent.put("updatesummary", task.getResDescription());
             oldContent.put("photofile", task.getResPhotoName());
             oldContent.put("audiofile", task.getResAudioName());
         } catch (JSONException e) {
             e.printStackTrace();
         }
         HttpGet httpGet = new HttpGet(baseURL+"?action=update&id="+id+"&summary=complete&content="+oldContent.toString()+"&description=completetask");
         internetFetch(httpGet);
     }
 }
