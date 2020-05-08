 package com.fawna.dumbo;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.entity.StringEntity;
 import org.json.JSONObject;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import java.util.ArrayList;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 import android.util.Log;
 
 
 public class ShowPickerActivity extends Activity {
 
   public static ArrayList<MovieStub> movies = null;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.show_picker);
 
 
 
     final EditText minuteBox = (EditText) findViewById(R.id.set_time);
     minuteBox.setInputType(InputType.TYPE_CLASS_NUMBER);
 
     LinearLayout ll = (LinearLayout)findViewById(R.id.movie_layout);
 
     LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 
     if(movies != null) {
       for (MovieStub mv : movies) { 
         
         Button newBut = new Button(this);
         
         newBut.setText(mv.name);
         final int id = mv.id;
         
         newBut.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
             int minutes = getMinutes();
             
             prepareCardActivity(minutes * 60, id); 
 
           }
         });
 
         ll.addView(newBut,lp);
       }
     }
     else {
       /*
       Button lotr = (Button) findViewById(R.id.load_lotr);
       lotr.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
           int minutes = getMinutes();
           //TODO WILL IMPLEMENT ME FOR OPENING LOTR
         }
       });
       Button himym = (Button) findViewById(R.id.load_himym);
       himym.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
           int minutes = getMinutes();
           // TODO WILL IMPLEMENT ME FOR OPENING HIMYM
         }
       });*/
     }
   }
 
   public void prepareCardActivity(final int seconds, final int id) { 
     Thread getOne = new Thread() {
     @Override
     public void run() { 
       try { 
 
         String urlstr = "http://www.willhughes.ca/echo/get";      
         HttpClient client = new DefaultHttpClient();
         HttpPost post = new HttpPost(urlstr);
 
         JSONObject requestJson = new JSONObject();
 
         requestJson.put("id", id);
         StringEntity se = new StringEntity(requestJson.toString());
         post.setEntity(se);
         post.setHeader("Accept", "application/json");
         post.setHeader("Content-type", "application/json");
         // get response
         HttpResponse response = client.execute(post);                
         // Get hold of the response entity
         HttpEntity entity = response.getEntity();
         // If the response does not enclose an entity, there is no need
         // to worry about connection release
 
         String result = "";
         if (entity != null) 
         {
             // A Simple JSON Response Read
             InputStream instream = entity.getContent();
             result= convertStreamToString(instream);
             // now you have the string representation of the HTML request
             instream.close();
         }
         Log.d("Fingerprinter", "Result: " + result);
         JSONObject jobj = new JSONObject(result);
 
         if (jobj.getBoolean("success")) {
           JSONObject match = jobj.getJSONObject("match");
           final MovieInfo mov = new MovieInfo(match);
           mov.time = seconds;
           CardsFragment.movieInfo = mov;
           runOnUiThread(new Runnable() 
           {   
             public void run() 
             {
               didFindMatchForCode();
             }
           });
 
         }
       }
       catch (Exception e) { 
         Log.d("Fingerprinter", "Exception: " + e);
         throw new RuntimeException(e);
       }
     }
     };
     getOne.start();
     
 
   }
 
   // TODO Will use me
   public void didFindMatchForCode() {
     Intent intent = new Intent(this, MovieViewActivity.class);
     startActivity(intent);
   }
 
   public int getMinutes() {
     final EditText minuteBox = (EditText) findViewById(R.id.set_time);
     minuteBox.setInputType(InputType.TYPE_CLASS_NUMBER);
     Log.d("Fingerprinter", minuteBox.getText().toString());
    try {
     return Integer.parseInt(minuteBox.getText().toString());
    } catch (NumberFormatException ex) {
      return 0;
    }
   }
 
   private static String convertStreamToString(InputStream is) 
   {
       /*
        * To convert the InputStream to String we use the BufferedReader.readLine()
        * method. We iterate until the BufferedReader return null which means
        * there's no more data to read. Each line will appended to a StringBuilder
        * and returned as String.
        */
       BufferedReader reader = new BufferedReader(new InputStreamReader(is));
       StringBuilder sb = new StringBuilder();
 
       String line = null;
       try {
           while ((line = reader.readLine()) != null) {
               sb.append(line + "\n");
           }
       } catch (IOException e) {
           e.printStackTrace();
       } finally {
           try {
               is.close();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
       return sb.toString();
   }
 
 }
