 package com.pk.cwierkacz.controller;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URISyntaxException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.junit.Ignore;
 
 @Ignore
 public class HttpTest
 {
     public static void main( String[] args ) throws URISyntaxException, ClientProtocolException, IOException {
         HttpClient client = new DefaultHttpClient();
 
         CookieStore cookieStore = new BasicCookieStore();
         HttpContext localContext = new BasicHttpContext();
         localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
 
         HttpContext context = new BasicHttpContext();
 
         HttpPost[] requests = new HttpPost[] {new HttpPost("http://localhost:8080/cwierkacz-0.0.1-SNAPSHOT/welcom.do?action=ADDACCOUNT&username=test&password=test"),
                                               new HttpPost("http://localhost:8080/cwierkacz-0.0.1-SNAPSHOT/welcom.do?action=SIGNIN&username=test&password=test"),
                                               new HttpPost("http://localhost:8080/cwierkacz-0.0.1-SNAPSHOT/welcom.do?action=SIGNOUT")};
 
         for ( HttpPost request : requests ) {
             HttpResponse response = client.execute(request, context);
 
             BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 
             StringBuilder res = new StringBuilder();
             String line = "";
             while ( ( line = rd.readLine() ) != null ) {
                 res.append(line);
             }
             System.out.println(res);
         }
     }
 }
