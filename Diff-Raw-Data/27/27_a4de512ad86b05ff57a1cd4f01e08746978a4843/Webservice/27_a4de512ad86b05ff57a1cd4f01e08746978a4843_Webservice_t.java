 package se.softwerk.coffee;
 
 import android.util.Log;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 /**
  * Created by sam on 10/9/13.
  */
 public class Webservice {
     private String url = "http://dev.softwerk.se:81/api";
 
     public int getSession(String user, String pass){
         String currentProgressStr = getWebservice(url+"/?user="+user+"&pass="+pass+"&command=getSession").trim();
         Log.i("Session", url+"/?user="+user+"&pass="+pass+"&command=getSession");
         int currentProgressInt = Integer.parseInt(currentProgressStr);
         return currentProgressInt;
     }
 
     public String getAutoswitchStatus(String user, String pass){
         return getWebservice(url+"/?user="+user+"&pass="+pass+"&command=autoswitchStatus");
     }
 
    public void toggleAutoswitch(String user, String pass){
        getWebservice(url+"/?user="+user+"&pass="+pass+"&command=toggleautoswitch");
    }
    public void untoggleAutoswitch(String user, String pass){
        getWebservice(url+"/?user="+user+"&pass="+pass+"&command=untoggleautoswitch");
     }
 
     public void clearSession(String user, String pass){
         getWebservice(url+"/?user="+user+"&pass="+pass+"&command=turnOff");
     }
 
     public String getTime(String user, String pass){
         return getWebservice(url+"/?user="+user+"&pass="+pass+"&command=getTime");
     }
 
     public String toggleCoffeepowder(String user, String pass){
         return getWebservice(url+"/?user="+user+"&pass="+pass+"&command=toggleCoffeepowder");
     }
 
     public String getCoffeepowder(String user, String pass){
         return getWebservice(url+"/?user="+user+"&pass="+pass+"&command=getCoffeepowderstatus");
     }
 
     public void saveSession(String user, String pass, long progress){
         getWebservice(url+"/?user="+user+"&pass="+pass+"&command=saveSession&percent="+progress);
     }
 
     public void toggleCoffee(String user, String pass, String toggle){
         if(toggle.equals("off")){
             getWebservice(url+"/?user="+user+"&pass="+pass+"&command=turnOff");
         } else if(toggle.equals("on")){
             getWebservice(url+"/?user="+user+"&pass="+pass+"&"+"&command=turnOn");
         }
     }
 
     public String getWebservice(String url){
         String output = "";
         try {
             DefaultHttpClient httpClient = new DefaultHttpClient();
             HttpGet httpGet = new HttpGet(url);
             HttpResponse httpResponse = httpClient.execute(httpGet);
             HttpEntity httpEntity = httpResponse.getEntity();
             output = EntityUtils.toString(httpEntity);
             Log.i("output", output);
         } catch (Exception e){
             Log.i("Error...", "something went wrong o.O"+e);
         }
         return output;
     }
 }
