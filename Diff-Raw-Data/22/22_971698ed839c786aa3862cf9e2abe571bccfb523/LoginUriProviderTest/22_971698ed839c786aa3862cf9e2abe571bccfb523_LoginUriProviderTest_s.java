 package com.atlassian.sal.ctk.test;
 
 import com.atlassian.sal.api.message.I18nResolver;
 import com.atlassian.sal.api.message.Message;
 import com.atlassian.sal.api.auth.AuthenticationController;
 import com.atlassian.sal.api.auth.AuthenticationListener;
 import com.atlassian.sal.api.auth.LoginUriProvider;
 import com.atlassian.sal.ctk.CtkTest;
 import com.atlassian.sal.ctk.CtkTestResults;
 import org.springframework.stereotype.Component;
 
 import java.util.Locale;
 import java.util.Map;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.io.UnsupportedEncodingException;
 
 @Component
 public class LoginUriProviderTest implements CtkTest
 {
 	private final LoginUriProvider provider;
 
     public LoginUriProviderTest(LoginUriProvider provider)
     {
         this.provider = provider;
     }
 
 
     public void execute(final CtkTestResults results) throws URISyntaxException, UnsupportedEncodingException
     {
 		results.assertTrue("LoginUriProvider should be injectable", provider != null);
 
         String destUri = "http://server/dest.html?param=value";
         URI loginUri = provider.getLoginUri(new URI(destUri));
         results.assertTrue("Login URI should contain destination: " + loginUri.toString(), loginUri.toString().contains(URLEncoder.encode(destUri, "UTF-8")));
        System.out.println("encoded:"+URLEncoder.encode(destUri, "UTF-8"));
     }
 }
