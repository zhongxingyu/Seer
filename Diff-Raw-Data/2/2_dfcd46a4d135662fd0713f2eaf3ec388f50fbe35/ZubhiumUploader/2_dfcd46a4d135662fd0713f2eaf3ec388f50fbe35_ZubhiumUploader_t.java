 package zubhium;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.Credentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 /**
  * A zubhium uploader
  * @see official docs @ https://www.zubhium.com/docs/upload/
  */
 public class ZubhiumUploader {
 	
 	final String ApiHost = "www.zubhium.com";
 	final String ApiPath = "/api2/upload/";
 	
 	
     static class UploadRequest {
         String secretKey;
         String releaseNotes;
         File apk;
         Boolean replace;
         
         // Optional:
         String userGroups;
         String proguardMapping;
         
         // Proxy Settings:
         String proxyHost;
         String proxyUser;
         String proxyPass;
         int proxyPort;
     }
 
     public ZubhiumResponse upload(UploadRequest ur) throws IOException, org.json.simple.parser.ParseException {
 
         DefaultHttpClient httpClient = new DefaultHttpClient();
 
         // Configure the proxy if necessary
         if(ur.proxyHost!=null && !ur.proxyHost.isEmpty() && ur.proxyPort>0) {
             Credentials cred = null;
             if(ur.proxyUser!=null && !ur.proxyUser.isEmpty())
                 cred = new UsernamePasswordCredentials(ur.proxyUser, ur.proxyPass);
 
             httpClient.getCredentialsProvider().setCredentials(new AuthScope(ur.proxyHost, ur.proxyPort),cred);
             HttpHost proxy = new HttpHost(ur.proxyHost, ur.proxyPort);
             httpClient.getParams().setParameter( ConnRoutePNames.DEFAULT_PROXY, proxy);
         }
 
         HttpHost targetHost = new HttpHost(ApiHost, 443, "https");
         HttpPost httpPost = new HttpPost(ApiPath);
 
         MultipartEntity entity = new MultipartEntity();
         entity.addPart("secretkey", new StringBody(ur.secretKey));
         entity.addPart("releasenotes", new StringBody(ur.releaseNotes));
         entity.addPart("apk", new FileBody(ur.apk));
        entity.addPart("replace", new StringBody((ur.replace != null && ur.replace) ? "true" : "false"));
         
         if (ur.userGroups != null && ur.userGroups.length() > 0)
             entity.addPart("usergroups", new StringBody(ur.userGroups));
         
         if (ur.proguardMapping != null && ur.proguardMapping.length() > 0)
             entity.addPart("proguardmapping", new StringBody(ur.proguardMapping));
         
         httpPost.setEntity(entity);
 
         HttpResponse response = httpClient.execute(targetHost,httpPost);
         HttpEntity resEntity = response.getEntity();
 
         InputStream is = resEntity.getContent();
 
         // Improved error handling.
         int statusCode = response.getStatusLine().getStatusCode();
         if (statusCode != 200) {
             String responseBody = new Scanner(is).useDelimiter("\\A").next();
             throw new UploadException(statusCode, responseBody, response);
         }
 
         return parseZubhiumResponse(is);
     }
 
     //Parsing nested JSON to make life easier
     private static ZubhiumResponse parseZubhiumResponse(InputStream is) throws ParseException, IOException
     {
          JSONParser parser = new JSONParser();
         
     	 Map responseMap = (Map)parser.parse(new BufferedReader(new InputStreamReader(is)));
          
     	 if (responseMap == null)
     		 return null;
 
     	 ZubhiumResponse r = new ZubhiumResponse();
     	 
          // check if errors occured
          if (responseMap.containsKey("push.errors"))
          {
         	 r.PushErrrors = (Map)parser.parse(responseMap.get("push.errors").toString());
         	 
         	 if (r.PushErrrors.containsKey("fielderrors")){
         		 r.FieldErrrors = (Map)parser.parse(r.PushErrrors.get("fielderrors").toString());
         	 }
          }
          
          if (responseMap.containsKey("push.info"))
          	r.PushInfo = (Map)parser.parse(responseMap.get("push.info").toString());
          
          // append success param so we don't lose this information
          if (responseMap.containsKey("success"))
         	 r.Success = Boolean.parseBoolean(responseMap.get("success").toString());
          
          return r;
     }
 
     
     /**  Useful for quick testing as well */
     public static void main(String[] args) throws Exception {
     	JSONParser parser = new JSONParser();
     	String json = "{\"push.info\":{\"push.noUsersInvited\":4,\"push.directDownloadUrl\":\"http://zubhi.co/g/xxx/\",\"push.warnings\":[\"warning1\",\"warning2\"],\"push.versionName\":\"5.0\",\"push.socialUrl\":\"http://zubhi.co/d/xxxx/\",\"push.versionCode\":\"110\",\"push.proguardEnabled\":false,},\"success\":true}";
     	ZubhiumResponse response = parseZubhiumResponse(new ByteArrayInputStream(json.getBytes()));
     	
     	List<String> warnings = (List<String>)response.PushInfo.get("push.warnings");
     	
     	json = "{\"push.errors\": {\"fielderrors\": {\"apk\": [\"Please increment version code, push with version code 1 already exist\"]}, \"no_errors\": 1}, \"success\": false}";
     	response = parseZubhiumResponse(new ByteArrayInputStream(json.getBytes()));
     	
     	Iterator it = response.FieldErrrors.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry pairs = (Map.Entry)it.next();
 			System.out.println("Zubhium ERROR: " + pairs.getKey() + " = " + pairs.getValue());
 		}
     	
     }
 
     
     /**  Useful for testing */
     public static void mainCli(String[] args) throws Exception {
         
     	ZubhiumUploader uploader = new ZubhiumUploader();
         
         UploadRequest r = new UploadRequest();
         
         r.secretKey = args[0];
         r.releaseNotes = args[2];
         
         File file = new File(args[3]);
         r.apk = file;
         
         if (args.length > 4)
         	r.userGroups = args[4];
         
         if (args.length > 5)
         	r.proguardMapping = args[5];
         
         uploader.upload(r);
     }
 }
