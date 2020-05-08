 package com.tjh.swivel.model;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.hamcrest.Matcher;
 import org.hamcrest.StringDescription;
 
 import java.net.URI;
 
 public abstract class AbstractStubRequestHandler implements StubRequestHandler {
     private static Logger logger = Logger.getLogger(AbstractStubRequestHandler.class);
     protected final Matcher<HttpUriRequest> matcher;
 
     public AbstractStubRequestHandler(Matcher<HttpUriRequest> matcher) {this.matcher = matcher;}
 
     @Override
     public boolean matches(HttpUriRequest request) {
         boolean result = matcher.matches(request);
         if (Level.DEBUG.equals(logger.getEffectiveLevel())) {
             String matchDescription = "matches!";
             if (!result) {
                 StringDescription mismatchDescription = new StringDescription();
                 matcher.describeMismatch(request, mismatchDescription);
                matchDescription = mismatchDescription.toString();
             }
 
             logger.debug("Stub match result: " + matchDescription);
         }
         return result;
     }
 
     @Override
     public int getId() { return System.identityHashCode(this); }
 
     public abstract HttpResponse handle(HttpUriRequest request, URI matchedURI, HttpClient client);
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder("AbstractStubRequestHandler{");
         StringDescription stringDescription = new StringDescription();
         matcher.describeTo(stringDescription);
         sb.append("matcher=").append(stringDescription.toString());
         sb.append(", id=").append(getId());
         sb.append('}');
         return sb.toString();
     }
 }
