     package LinkFuture.Core.WebClient;
 
 
 import LinkFuture.Core.Debugger;
 import LinkFuture.Init.Config;
 import LinkFuture.Init.Extensions.StringExtension;
 import LinkFuture.Init.ObjectExtend.NameValuePair;
 
 import javax.net.ssl.*;
 import java.io.*;
 import java.net.*;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 
 /**
  * User: Cyokin Zhang
  * Date: 9/27/13
  * Time: 7:32 PM
  */
 public class WebClient {
     //allow https connection without install certification
     static {
         AllowHttpsConnection();
         AppendAuthentication();
         //http://www.hccp.org/java-net-cookie-how-to.html
 //        CookieManager cm = new CookieManager();
 //        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
 //        CookieHandler.setDefault(cm);
     }
     private static void AppendAuthentication(){
         Authenticator.setDefault(new GroupAuthenticator());
     }
     private static void AllowHttpsConnection(){
         try
         {
             TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                 public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                     return null;
                 }
                 public void checkClientTrusted(X509Certificate[] certs, String authType) {
                 }
                 public void checkServerTrusted(X509Certificate[] certs, String authType) {
                 }
             } };
             SSLContext sc = SSLContext.getInstance("SSL");
             sc.init(null, trustAllCerts, new java.security.SecureRandom());
             HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
             // Create all-trusting host name verifier
             HostnameVerifier allHostsValid = new HostnameVerifier() {
                 public boolean verify(String hostname, SSLSession session) {
                     return true;
                 }
             };
             // Install the all-trusting host verifier
             HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
         }
         catch (Exception ex)
         {
             Debugger.traceln(ex);
         }
     }
 
     HttpURLConnection con = null;
     public volatile boolean isConnected = false;
     private DataOutputStream outputStream = null;
     private Thread interruptURLThread = null;
     private WebRequestInfo requestMeta;
     private static final String boundary =  "7ddd51836076e";
     private static final String twoHyphens = "--";
     private static final String crlf = "\r\n";
     private static final String fieldSeperated = twoHyphens + boundary + crlf;
     private WebClient(WebRequestInfo requestMeta){
          this.requestMeta = requestMeta;
     }
     private WebClientResultInfo Send(){
         WebClientResultInfo result = new WebClientResultInfo();
         try {
             this.connect(requestMeta.RequestURL);
             result.code = con.getResponseCode();
             result.errorMessage = con.getResponseMessage();
             //redirection
             if(this.requestMeta.AutoRedirect)
             {
                 while (result.code == HttpURLConnection.HTTP_MOVED_TEMP
                         || result.code == HttpURLConnection.HTTP_MOVED_PERM
                         || result.code == HttpURLConnection.HTTP_SEE_OTHER )
                 {
                     String newUrl = con.getHeaderField("Location");
                     this.close();
                     Debugger.traceln("Redirect to %s",newUrl);
                     this.connect(new URL(newUrl));
                     result.code = con.getResponseCode();
                     result.errorMessage = con.getResponseMessage();
                 }
             }
             InputStream replyStream = null;
             //  200 <= code < 300
             if(result.code>=HttpURLConnection.HTTP_OK && result.code < HttpURLConnection.HTTP_MULT_CHOICE)
             {
                  replyStream = con.getInputStream();
             }
             else
             {
                 replyStream = con.getErrorStream();
             }
             final InputStreamReader inputStreamReader;
             final String contentEncodingField = con.getContentEncoding();
             //read header
             result.HeaderFields = new HashMap<>();
             for (Map.Entry<String, List<String>> header : con.getHeaderFields().entrySet()) {
                 result.HeaderFields.put(header.getKey(),header.getValue().toString());
             }
            if(replyStream!=null && replyStream.available() > 0)
             {
                 String charset = getCharset();
                 if (contentEncodingField != null && contentEncodingField.equalsIgnoreCase("gzip")) {
                     final GZIPInputStream gzipInputStream =  new GZIPInputStream(replyStream);
                     inputStreamReader = new InputStreamReader(gzipInputStream,charset);
                 } else {
                     inputStreamReader = new InputStreamReader(replyStream,charset);
                 }
                 result.response = read(inputStreamReader);
             }
             else
             {
                 result.response = Config.Empty;
             }
             result.success =  (result.code>=HttpURLConnection.HTTP_OK && result.code < HttpURLConnection.HTTP_MULT_CHOICE);
         }
         catch (Exception e) {
             if(result.code==0)
             {
                 result.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
             }
             if(isConnected)
             {
                 result.errorMessage = e.toString();
             }
             else
             {
                 result.errorMessage = "java.net.SocketTimeoutException: Timed out";
             }
             if(StringExtension.IsNullOrEmpty(result.errorMessage)){
                 String error = Debugger.getDetail(e);
                 result.errorMessage = error.substring(0,error.indexOf(Config.NewLine));
             }
         }
         finally {
            this.close();
         }
         return result;
     }
     private String getCharset(){
         String contentType = con.getContentType();
         String[] values = contentType.split(";"); //The values.length must be equal to 2...
         String charset = null;
         for (String value : values) {
             value = value.trim();
             if (value.toLowerCase().startsWith("charset=")) {
                 charset = value.split("=", 2)[1];
             }
         }
        return StringExtension.IsNullOrEmpty(charset)?Config.DefaultEncoding:charset;
     }
     private void connect(URL url) throws IOException {
         isConnected = true;
         con = (HttpURLConnection) url.openConnection();
         if(requestMeta.ReadTimeout>0)
         {
             con.setReadTimeout(requestMeta.ReadTimeout);
         }
         if(requestMeta.ConnectionTimeout>0)
         {
             con.setConnectTimeout(requestMeta.ConnectionTimeout);
         }
         if(!StringExtension.IsNullOrEmpty(requestMeta.UserName) || !StringExtension.IsNullOrEmpty(requestMeta.Password))
         {
             GroupAuthenticator.register(requestMeta.UserName,requestMeta.Password);
         }
         //append header
         if(this.requestMeta.RequestHeadList!=null)
         {
             for (Map.Entry<String,Object> item:requestMeta.RequestHeadList.entrySet())
             {
                 con.setRequestProperty(item.getKey(), item.getValue().toString());
             }
         }
         if(requestMeta.ReadTimeout >0 || requestMeta.ConnectionTimeout>0)
         {
             //use thread to force disconnect, as for somehow default timeout not working on unix.
             interruptURLThread = new Thread(new WebClientInterruptThread(this));
             interruptURLThread.start();
         }
         writeBytes();
     }
     private void close(){
         if(interruptURLThread!=null)
         {
             interruptURLThread.interrupt();
             interruptURLThread = null;
         }
         if(outputStream !=null)
         {
             try {
                 outputStream.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             outputStream = null;
         }
         if(con!=null)
         {
             isConnected = false;
             con.disconnect();
             con = null;
         }
     }
 
     private String buildFormPostString() throws UnsupportedEncodingException {
         if(requestMeta.PostStringList.size() > 0)
         {
             StringBuilder sb = new StringBuilder();
             for (Map.Entry<String,Object> item:requestMeta.PostStringList.entrySet())
             {
                 sb.append(fieldSeperated);
                 sb.append("Content-Disposition: form-data; name=\"" + item.getKey() + "\"");
                 sb.append(crlf);
                 sb.append(crlf);
                 sb.append(item.getValue());
                 sb.append(crlf);
             }
             return sb.toString();
         }
         return null;
     }
     private String buildUrlEncodingPostString() throws UnsupportedEncodingException {
         if(requestMeta.PostStringList.size() > 0)
         {
             StringBuilder sb = new StringBuilder();
             int i=0;
             for (Map.Entry<String,Object> item:requestMeta.PostStringList.entrySet())
             {
                 sb.append(String.format("%s=%s", item.getKey(), URLEncoder.encode(item.getValue().toString(), Config.DefaultEncoding)));
                 i++;
                 if(i<requestMeta.PostStringList.size())
                 {
                     sb.append("&");
                 }
             }
             return sb.toString();
         }
         return null;
     }
     private void writeBytes() throws IOException {
         if(requestMeta.RequestMethod==HttpMethod.Post)
         {
             //URLEncoder.encode
             con.setRequestMethod("POST");
             con.setUseCaches(false);
             con.setRequestProperty("Cache-Control", "no-cache");
             boolean hasPostData = requestMeta.PostStringList!=null && requestMeta.PostStringList.size()>0;
             boolean hasPostFile =  requestMeta.PostFileList!=null && requestMeta.PostFileList.size()>0;
             //do we have data to post?
             if(hasPostData || hasPostFile)
             {
                 //open write
                 con.setDoOutput(true);
                 con.setDoInput(true);
                 //we only have string to post, then use application/x-www-form-urlencoded
                 //check http://en.wikipedia.org/wiki/POST_(HTTP)
                 if(hasPostData && !hasPostFile && requestMeta.EncType==FormContentTypes.UrlEncoded)
                 {
                     con.setRequestProperty("Content-Type",FormContentTypes.UrlEncoded.toString());
                     byte[] postString = buildUrlEncodingPostString().getBytes(Config.DefaultEncoding);
                     con.setRequestProperty("Content-Length",String.valueOf(postString.length));
                     outputStream = new DataOutputStream(con.getOutputStream());
                     outputStream.write(postString);
                 }
                 else
                 {
                     //looks we got file to post, we need use multipart/form-data
                     //check http://www.w3.org/TR/html401/interact/forms.html
                     con.setRequestProperty("Content-Type",FormContentTypes.FormData.toString().concat(";boundary="+ boundary));
                     outputStream = new DataOutputStream(con.getOutputStream());
                     if(hasPostData)
                     {
                         outputStream.writeBytes(buildFormPostString());
                     }
                     for (Map.Entry<String,WebRequestFileInfo> item:requestMeta.PostFileList.entrySet())
                     {
                         WebRequestFileInfo fileInfo = item.getValue();
                         outputStream.writeBytes(fieldSeperated);
                         outputStream.writeBytes("Content-Disposition:form-data;name=\"" + fileInfo.Name + "\";filename=\"" + fileInfo.FileName + "\"" + crlf);
                         if(StringExtension.IsNullOrEmpty(fileInfo.ContentType))
                         {
                             outputStream.writeBytes("Content-Type: application/octet-stream");
                         }
                         else
                         {
                             outputStream.writeBytes("Content-Type: " + fileInfo.ContentType);
                         }
                         outputStream.writeBytes(crlf);
                         outputStream.writeBytes(crlf);
                         //write stream
                         int nRead;
                         byte[] data = new byte[1024];
                         while ((nRead = fileInfo.FileStream.read(data, 0, data.length)) != -1) {
                             outputStream.write(data,0,nRead);
                         }
                         outputStream.writeBytes(crlf);
                     }
                     outputStream.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                 }
                 outputStream.flush();
             }
             else
             {
                 throw new IllegalArgumentException("Must append data to post");
             }
             //con.setRequestProperty("Content-Language", "en-US");
             Debugger.traceln("POST data to %s",requestMeta.RequestURL);
         }
         else{
             con.setRequestMethod("GET");
             con.setUseCaches(requestMeta.UseCaches);
             Debugger.traceln("GET data from %s", requestMeta.RequestURL);
         }
     }
     private String read(InputStreamReader inputStreamReader) throws IOException {
         final BufferedReader reader =  new BufferedReader(inputStreamReader);
         String inputLine;
         StringBuilder response = new StringBuilder();
         while ((inputLine = reader.readLine()) != null) {
             response.append(inputLine + Config.NewLine);
         }
         reader.close();
         return response.toString();
     }
 
     //region  SendRequest
     public static WebClientResultInfo sendRequest(WebRequestInfo requestMeta){
         WebClient client = new WebClient(requestMeta);
         return client.Send();
     }
 
     /**
      *   Send Request
      * @param requestMeta
      * @param retryTimes must >=1
      * @return
      */
     public static WebClientResultInfo sendRequest(WebRequestInfo requestMeta,int retryTimes)
     {
         //at least once
         if(retryTimes<1) retryTimes=1;
         WebClientResultInfo result = null;
         for (int i=0;i<retryTimes;i++)
         {
             result = sendRequest(requestMeta);
             if(result.success)
             {
                 return result;
             }
         }
         return result;
     }
     public static WebClientResultInfo sendRequest(URL url,HttpMethod method, ArrayList<NameValuePair> postData) throws UnsupportedEncodingException {
         WebRequestInfo requestMeta = new WebRequestInfo();
         requestMeta.RequestURL = url;
         requestMeta.RequestMethod = method;
         if(requestMeta.RequestMethod==HttpMethod.Post && postData !=null)
         {
             for (NameValuePair item:postData)
             {
                 requestMeta.addPostData(item.id,item.value);
             }
         }
         return sendRequest(requestMeta);
     }
     public static WebClientResultInfo sendRequest(String url,HttpMethod method, ArrayList<NameValuePair> postData) throws UnsupportedEncodingException, MalformedURLException {
         return sendRequest(new URL(url),method,postData);
     }
     //endregion
 
     //region Post
     public static WebClientResultInfo post(URL url, ArrayList<NameValuePair> postData) throws UnsupportedEncodingException {
         return sendRequest(url,HttpMethod.Post,postData);
     }
     public static WebClientResultInfo post(String url, ArrayList<NameValuePair> postData) throws UnsupportedEncodingException, MalformedURLException {
         return sendRequest(new URL(url),HttpMethod.Post,postData);
     }
     //endregion
 
     //region Get
     public static WebClientResultInfo get(String url) throws MalformedURLException {
         return get(new URL(url));
     }
     public static WebClientResultInfo get(URL url)  {
         WebRequestInfo requestMeta = new WebRequestInfo();
         requestMeta.RequestURL = url;
         requestMeta.RequestMethod = HttpMethod.Get;
         return sendRequest(requestMeta);
     }
     //endregion
 }
