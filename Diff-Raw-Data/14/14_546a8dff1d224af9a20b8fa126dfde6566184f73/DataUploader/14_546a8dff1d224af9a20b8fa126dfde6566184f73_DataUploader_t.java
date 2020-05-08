 package uk.ac.ebi.esd.uploder;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 
 public class DataUploader
 {
  public static void main( String[] args )
  {
   try
   {
    File wDir = new File( args[0] );
    
    System.out.println("Working directory: "+args[0]);
    
    DefaultHttpClient httpclient = new DefaultHttpClient();
 
    HttpPost httpost = new HttpPost("http://127.0.0.1:8888/Login");
 
    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    nvps.add(new BasicNameValuePair("username", "test"));
    nvps.add(new BasicNameValuePair("password", "test"));
 
    httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
 
    ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
    String response = httpclient.execute(httpost, responseHandler);
 
    System.out.println("Login responce: " + response);
 
    if(!"OK".equals(response.trim()))
     return;
 
    HttpPost httppost = new HttpPost("http://127.0.0.1:8888/upload");
 
    for( String fn : wDir.list() )
    {
     if( ! fn.endsWith(".age.txt") )
      continue;
 
     System.out.println("Loading: "+fn);
     
     FileBody bin = new FileBody(new File(wDir,fn));
     
     MultipartEntity reqEntity = new MultipartEntity();
     reqEntity.addPart("file1", bin);
     reqEntity.addPart("Command", new StringBody("Submission"));
     
     httppost.setEntity(reqEntity);
     
     response = httpclient.execute(httppost, responseHandler);
     
     if( "OK".equals(response.trim()) )
      System.out.println(fn+": OK");
     else
     {
      System.out.println(fn+": ERROR");
      
      FileWriter fout = new FileWriter(new File(wDir,fn+".log"));
      fout.write(response);
      fout.close();
      
 //     break;
     }
     
//    httppost.abort();
    
//    System.out.print(">");
//    System.in.read();
 //    if( fn.endsWith("11826.age.txt") )
 //     return;
    }
    
   }
   catch(Exception e)
   {
    // TODO Auto-generated catch block
    e.printStackTrace();
   }
  }
 }
