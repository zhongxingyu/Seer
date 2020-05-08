 package org.fourdnest.androidclient.comm;
 
 import android.util.Log;
 
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.HttpResponse;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.cookie.DateUtils;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.NameValuePair;
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.Nest;
 import org.fourdnest.androidclient.Tag;
 
 import java.io.*;
 import java.net.URI;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 public class FourDNestProtocol implements Protocol {
 	private static final String TAG = "FourDNestProtocol";
 	private static final String EGG_UPLOAD_PATH = "fourdnest/api/v1/egg/upload/";
 	private static final int HTTP_STATUSCODE_CREATED = 201;
 	private Nest nest;
 
 	public FourDNestProtocol() {
 	    this.nest = null;
 	}
 
     /**
      * Parses egg's content and sends it in multipart mime format with HTTP
      * post.
      * 
      * @return HTTP status code and egg URI on server if creation successful
      **/
     public String sendEgg(Egg egg) {
         String concatedMd5 = "";
         HttpClient client = new DefaultHttpClient();
         HttpPost post = new HttpPost(this.nest.getBaseURI() + EGG_UPLOAD_PATH);
 
         // Create list of NameValuePairs
         List<NameValuePair> pairs = new ArrayList<NameValuePair>();
         pairs.add(new BasicNameValuePair("caption", egg.getCaption()));
         concatedMd5 += md5FromString(egg.getCaption());
         // FIXME: Check for null file path.
         if (egg.getLocalFileURI() != null) {
             pairs.add(new BasicNameValuePair("file", egg.getLocalFileURI()
                     .getPath()));
             concatedMd5 += md5FromFile(egg.getLocalFileURI().getPath());
         }
         
         
         // FIXME: Add tags later
         String multipartMd5String = md5FromString(concatedMd5);
         
         int status = 0;
         try {
             post.setEntity(this.createEntity(pairs));
             Date date = new Date();
             
             /*
              * StringToSign = HTTP-Verb + ‘\n’ +
              * base64(Content-MD5) + ‘\n’ +
              * base64(x-4dnest-multipartMD5) + ‘\n’ +
              * Content-Type + ‘\n’ +
              * Date + ‘\n’ +
              * RequestURI
              * 
              * Should be in utf-8 automatically.
              */
             String stringToSign = post.getMethod() + "\n" +
             "" + "\n" +                                     //Content-MD5 empty for now
             multipartMd5String + "\n" +
             ""  + "\n" +                                       //Content-type empty for now
             DateUtils.formatDate(date) + "\n" +
             post.getURI().getPath() + "\n";
             
             String secretKey = "secret";
             String userName = "testuser";
             
            post.setHeader("x-4dnest-multipartMD5", multipartMd5String);
            
             String signature = computeSignature(stringToSign, secretKey);
             String authHeader = userName + ":" + signature;
             post.setHeader("Authorization", authHeader);
             Log.d("sign", authHeader);
             
             post.setHeader("Date", DateUtils.formatDate(date));
             HttpResponse response = client.execute(post);
             status = response.getStatusLine().getStatusCode();
             if (status == HTTP_STATUSCODE_CREATED) {
                 return status + " "
                         + response.getHeaders("Location")[0].getValue();
             }
         } catch (ClientProtocolException e) {
             Log.e(TAG, "ClientProtocolException, egg not sent "
                     + e.getMessage());
             return "0";
         } catch (IOException e) {
             Log.e(TAG, "IOException, egg not sent " + e.getMessage());
             return "0";
         }
         return String.valueOf(status);
     }
 
 
     /**
      * Creates the MultipartEntity from name-value -pair list
      * 
      * @throws UnsupportedEncodingException
      */
     private MultipartEntity createEntity(List<NameValuePair> pairs)
             throws UnsupportedEncodingException {
         MultipartEntity entity = new MultipartEntity(HttpMultipartMode.STRICT);
 
         for (int i = 0; i < pairs.size(); i++) {
             File file = new File(pairs.get(i).getValue());
             if (pairs.get(i).getName().equalsIgnoreCase("file")) {
                 entity.addPart(pairs.get(i).getName(), new FileBody(file));
             } else {
                 entity.addPart(pairs.get(i).getName(), new StringBody(pairs
                         .get(i).getValue()));
             }
         }
 
         return entity;
     }
 
     public ArrayList<Tag> topTags(int count) {
         // TODO Auto-generated method stub
         return null;
     }
 
 
 	public int getProtocolId() {
 		return ProtocolFactory.PROTOCOL_4DNEST;
 	}
 	
     public String getTest() throws Exception {
         HttpClient client = new DefaultHttpClient();
         HttpGet request = new HttpGet();
         request.setURI(new URI("http://hs.fi/index.html"));
         HttpResponse response = client.execute(request);
         BufferedReader in = new BufferedReader(new InputStreamReader(response
                 .getEntity().getContent()));
         StringBuffer sb = new StringBuffer("");
         String line = "";
         String NL = System.getProperty("line.separator");
         while ((line = in.readLine()) != null) {
             sb.append(line + NL);
         }
         in.close();
         String page = sb.toString();
 
         return page;
     }
 
     public void setNest(Nest nest) {
         this.nest = nest;
 
     }
     /**
      * Hashes the string in MD5 and returns a base64 encoded string
      * of the hash
      * 
      * @param String to be hashed
      * 
      * @return Base64 encoded md5 hash
      **/
     private String md5FromString(String s) {
         String result = "";
         if (s != null) {
             try {
                 byte[] bytes = DigestUtils.md5(s.getBytes("UTF-8"));
                 result = new String(Base64.encodeBase64(bytes));
     
             } catch (UnsupportedEncodingException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             
         }
         return result;
     }
     /**
      * Hashes the file with the given path in MD5 and returns a base64 encoded string
      * of the hash
      * 
      * @param Path of the file
      * 
      * @return Base64 encoded md5 hash
      **/
     private String md5FromFile(String path) {
         String result = "";
         try {
             FileInputStream fis = new FileInputStream( new File(path));
             byte[] bytes = DigestUtils.md5(fis);
             result = new String(Base64.encodeBase64(bytes));
             
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return result;
         
     }
     
     private String computeSignature(String stringToSign, String secretKey) {
         String result = "";
         byte[] keyBytes = secretKey.getBytes();
         SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
         
         Mac mac;
         try {
             mac = Mac.getInstance("HmacSHA1");
             mac.init(signingKey);
             byte[] bytes = mac.doFinal(stringToSign.getBytes());
             result = new String(Base64.encodeBase64(bytes));
             
         } catch (NoSuchAlgorithmException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InvalidKeyException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return result;
     }
     
 }
