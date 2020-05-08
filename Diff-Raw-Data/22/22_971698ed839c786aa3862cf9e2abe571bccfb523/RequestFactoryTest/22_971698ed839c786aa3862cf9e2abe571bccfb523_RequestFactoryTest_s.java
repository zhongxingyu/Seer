 package com.atlassian.sal.ctk.test;
 
 import org.springframework.stereotype.Component;
 
 import com.atlassian.sal.api.net.Request;
 import com.atlassian.sal.api.net.RequestFactory;
 import com.atlassian.sal.api.net.Response;
 import com.atlassian.sal.api.net.ResponseException;
 import com.atlassian.sal.api.net.ResponseHandler;
 import com.atlassian.sal.ctk.CtkTest;
 import com.atlassian.sal.ctk.CtkTestResults;
 
 @Component
 public class RequestFactoryTest implements CtkTest
 {
     private final RequestFactory<Request<?>> requestFactory;
     private boolean passed = false;
 
     public RequestFactoryTest(final RequestFactory<Request<?>> requestFactory)
 	{
 		this.requestFactory = requestFactory;
 	}
 
     public void execute(final CtkTestResults results) throws ResponseException
     {
         results.assertTrue("RequestFactory must be injectable", requestFactory != null);
 
         Request<?> request = requestFactory.createRequest(Request.MethodType.GET, "http://google.com");
 
         request.execute(new ResponseHandler()
         {
             public void handle(final Response response) throws ResponseException
             {
                 passed = response.getResponseBodyAsString().contains("Google");
             }
         });
         results.assertTrue("Should be able to call http://google.com and get result that contains 'google'", passed);
 
         request = requestFactory.createRequest(Request.MethodType.GET, "http://demo.jira.com");
         request.addSeraphAuthentication("admin", "admin");
 
         request.execute(new ResponseHandler()
         {
             public void handle(final Response response) throws ResponseException
             {
                 passed = response.getResponseBodyAsString().contains("Joe Administrator");
             }
         });
        results.assertTrue("Should be able to call http://demo.jira.com and log in using seraph authentication", passed);
     }
 }
