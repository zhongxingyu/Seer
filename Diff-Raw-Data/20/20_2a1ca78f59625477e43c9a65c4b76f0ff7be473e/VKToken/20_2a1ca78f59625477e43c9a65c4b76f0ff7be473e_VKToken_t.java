 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package su.mctop.bot.vk_api;
 
 import java.awt.Desktop;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.ref.SoftReference;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author Enelar
  */
 public class VKToken {
    private static SoftReference<List<String>> default_tokens = null;
    private List<String> requested_tokens;
    private Integer appid;   
    private String token;
    private Integer user_id;
     
    public VKToken( Integer client_id, List<String> scope ) {
      requested_tokens = scope;
      appid = client_id;
      SetToken(null, "0");
    }
    
    public VKToken( Integer client_id ) {
      requested_tokens = new ArrayList<String>();
      appid = client_id;
      SetToken(null, "0");
    }
    
    private void SetToken( String _token, String _user ) {
        user_id = Integer.parseInt(_user);
        token = _token;
    }
    
   
    private List<String> DefaultTokens() {
       List<String> ret = null;
       if (default_tokens != null)
           ret = default_tokens.get();
        if (ret != null)
            return ret;
        ret = new ArrayList<String>();
        ret.add("wall");
        ret.add("groups");
        ret.add("offline");
        default_tokens = new SoftReference<List<String>>(ret);
        return ret;
    }
    
    public String Token() {
        GetToken();
        return token;
    }
    
    public Integer UserId() {
        GetToken();
        return user_id;
    }
    
    public String User() {
        GetToken();
        return UserId().toString();
    }
    
   private Boolean IsLoaded() {
       return token != null;
   }
   
    private void GetToken() {
       if (IsLoaded())
            return;
       
        String scope = GenScope();
        MakeAuth(scope, "https://oauth.vk.com/blank.html");
    }
    
    private String GenScope() {
         String res = "";
         Iterator<String> i = DefaultTokens().iterator();
         
         while (i.hasNext()) {
             String t = i.next();
             res += t + ",";
         }
 
         i = requested_tokens.iterator();
         while (i.hasNext()) {
             String t = i.next();
             res += t + ",";
         }
         return res;
    }
            
     private static final String auth_url = "https://oauth.vk.com/authorize";   
    
     private void MakeAuth( String scope, String redirect ) {
         String url = 
                 auth_url + 
                 "?client_id=" + appid.toString() +
                 "&scope=" + scope +
                 "&redirect_url=" + redirect + 
                 "&display=popup&response_type=token";
         try {
             String location = RecieveLocation(url);
             ExtractToken(location);
         } catch (URISyntaxException e) {
             System.out.print("URISyntaxException " + e.toString());
         } catch (IOException e) {
             System.out.print("IOException " + e.toString());
         }
             
     }
     
     private String RecieveLocation( String url ) throws URISyntaxException, IOException
     {
         if (Desktop.isDesktopSupported()) 
           Desktop.getDesktop().browse(new URI(url));
         else
           System.out.print("URL to token: " + url + "\n");
         System.out.print("Please intert redirect url here: ");
         InputStreamReader isr = new InputStreamReader(System.in);
         BufferedReader br = new BufferedReader(isr);
         return br.readLine();
     }
     
     private void ExtractToken( String url ) {
         String[] parts = url.split("#");
         String[] params = parts[1].split("&");
         
         Map<String, String> map = new HashMap<String, String>();  
         for (String param : params) {
             String[] t = param.split("=");
             map.put(t[0], t[1]);
         }
         SetToken(map.get("access_token"), map.get("user_id"));
     }    
 }
