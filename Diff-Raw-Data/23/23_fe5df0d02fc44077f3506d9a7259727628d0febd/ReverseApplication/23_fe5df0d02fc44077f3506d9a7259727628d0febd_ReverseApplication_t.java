 /*
  This file is part of NerdzApi-java.
 
     NerdzApi-java is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     NerdzApi-java is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with NerdzApi-java.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) 2013 Marco Cilloni <marco.cilloni@yahoo.com>
 */
 
 package eu.nerdz.api.impl.reverse;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import eu.nerdz.api.Application;
 import eu.nerdz.api.HttpException;
 
 
 /**
  * Created by marco on 7/16/13.
  */
 
 class ReverseApplication implements Application {
 
     protected static String NERDZ_DOMAIN_NAME = "http://beta.nerdz.eu";
 
     private String userName;
     private String password;
     protected DefaultHttpClient httpClient;
     protected String token;
 
     protected ReverseApplication(String user, String password) throws IOException, HttpException {
 
         this.userName = user;
         this.password = password;
         this.httpClient = new DefaultHttpClient();
 
         //fetch token.
         {
             String body = this.get();
             int start = body.indexOf("<input type=\"hidden\" value=\"") + 28;
             this.token = body.substring(start, start + 32);
         }
 
         Map<String,String> form = new HashMap<String, String>(4);
         form.put("setcookie", "on");
         form.put("username", user);
         form.put("password", password);
         form.put("tok", this.token);
 
        String responseBody = this.post("/pages/profile/login.json.php", form, null, true);
        if( responseBody.contains("error")){
            throw new HttpException(401, responseBody);
        }
     }
 
     @Override
     public String getUsername() {
         return this.userName;
     }
 
     @Override
     public int getUserID() {
 
        for(Cookie cookie : this.httpClient.getCookieStore().getCookies())
            if (cookie.getName().equals("nerdz_id"))
                return Integer.parseInt(cookie.getValue());
 
         return -1;
     }
 
     protected String get() throws IOException, HttpException {
         return this.get("");
     }
 
     protected String get(String url) throws IOException, HttpException {
         return this.get(url, false);
     }
 
 
     protected String get(String url, boolean consume) throws IOException, HttpException {
 
         HttpGet get = new HttpGet(ReverseApplication.NERDZ_DOMAIN_NAME + url);
         ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
         HttpResponse response = this.httpClient.execute(get);
         StatusLine statusLine = response.getStatusLine();
 
         int code = statusLine.getStatusCode();
         if (code != 200 )
             throw new HttpException(code, statusLine.getReasonPhrase());
 
         String body = responseHandler.handleResponse(response);
 
         if (consume) {
             HttpEntity entity = response.getEntity();
             if (entity != null)
                 EntityUtils.consume(entity);
         }
 
         return body.trim();
     }
 
     protected String post(String url, Map<String,String> form) throws IOException, HttpException {
         return this.post(url, form, null, false);
     }
 
     protected String post(String url, Map<String,String> form, String referer) throws IOException, HttpException {
         return this.post(url, form, referer, false);
     }
 
 
     protected String post(String url, Map<String,String> form, String referer, boolean consume) throws IOException, HttpException {
 
         HttpPost post = new HttpPost(ReverseApplication.NERDZ_DOMAIN_NAME + url);
 
         if (referer != null)
             post.addHeader("Referer", referer);
 
         List<NameValuePair> formEntries = new ArrayList<NameValuePair>(form.size());
         for (Map.Entry<String,String> entry : form.entrySet())
             formEntries.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
         post.setEntity(new UrlEncodedFormEntity(formEntries, Charset.forName("UTF-8")));
 
         HttpResponse response = this.httpClient.execute(post);
         StatusLine statusLine = response.getStatusLine();
 
         int code = statusLine.getStatusCode();
         if (code != 200 )
             throw new HttpException(code, statusLine.getReasonPhrase());
 
         ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
         String body = responseHandler.handleResponse(response);
 
        if (consume) {
             HttpEntity entity = response.getEntity();
             if (entity != null)
                 EntityUtils.consume(entity);
         }
 
 
         return body.trim();
     }
 
 }
 
 
