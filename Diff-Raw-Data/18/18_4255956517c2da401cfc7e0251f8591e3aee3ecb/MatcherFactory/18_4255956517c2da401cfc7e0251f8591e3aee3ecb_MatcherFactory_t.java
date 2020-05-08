 package com.tjh.swivel.model;
 
 import org.apache.http.client.methods.HttpUriRequest;
 import org.hamcrest.Matcher;
 
 import javax.script.ScriptException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static com.tjh.swivel.model.matchers.ContentMatcher.hasContent;
 import static com.tjh.swivel.model.matchers.ContentTypeMatcher.hasContentType;
 import static com.tjh.swivel.model.matchers.QueryStringMatcher.hasQueryString;
 import static com.tjh.swivel.model.matchers.RequestHeaderMatcher.hasHeader;
 import static com.tjh.swivel.model.matchers.RequestMethodMatcher.hasMethod;
 import static com.tjh.swivel.model.matchers.ScriptMatcher.scriptMatches;
 import static org.hamcrest.CoreMatchers.allOf;
 import static org.hamcrest.CoreMatchers.containsString;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.hasItem;
 
 public class MatcherFactory {
     public static final String METHOD_KEY = "method";
    public static final String REMOTE_ADDR_KEY = "remoteAddress";
     public static final String CONTENT_TYPE_KEY = "contentType";
     public static final String CONTENT_KEY = "content";
     public static final String SCRIPT_KEY = "script";
     public static final int STATIC_MATCHER_COUNT = 2;
     public static final int OPTIONAL_MATCHER_COUNT = 5;
 
     //TODO:TJH - I have a feeling
     //some of the more complicated XML post matching stubs are going to be brittle
     //consider options for managing that - significant whitespace flag? (or more simply, xml/json)
     @SuppressWarnings("unchecked")
     public WhenMatcher buildMatcher(URI localURI, Map<String, String> when) {
 
         List<Matcher<HttpUriRequest>> matchers = new ArrayList<Matcher<HttpUriRequest>>(STATIC_MATCHER_COUNT);
         matchers.add(hasMethod(equalTo(when.get(METHOD_KEY))));
 
         matchers.add(buildOptionalMatcher(localURI, when));
         return new WhenMatcher(allOf(matchers.toArray(new Matcher[matchers.size()])), when);
     }
 
     @SuppressWarnings("unchecked")
     protected Matcher<HttpUriRequest> buildOptionalMatcher(URI localURI, Map<String, String> stubDescription) {
         try {
             List<Matcher<HttpUriRequest>> result =
                     new ArrayList<Matcher<HttpUriRequest>>(OPTIONAL_MATCHER_COUNT);
             String queryString = localURI.getQuery();
             if (queryString != null) {
                 result.add(hasQueryString(equalTo(queryString)));
             }
             if (stubDescription.containsKey(REMOTE_ADDR_KEY)) {
                 String remoteAddr = stubDescription.get(REMOTE_ADDR_KEY);
                 result.add(hasHeader("X-Forwarded-For", hasItem(containsString(remoteAddr))));
             }
             if (stubDescription.containsKey(CONTENT_TYPE_KEY)) {
                 result.add(hasContentType(containsString(stubDescription.get(CONTENT_TYPE_KEY))));
             }
             if (stubDescription.containsKey(CONTENT_KEY)) {
                 result.add(hasContent(equalTo(stubDescription.get(CONTENT_KEY))));
             }
             if (stubDescription.containsKey(SCRIPT_KEY)) {
                 result.add(scriptMatches(stubDescription.get(SCRIPT_KEY)));
             }
             return allOf(result.toArray(new Matcher[result.size()]));
         } catch (ScriptException e) {
             throw new RuntimeException(e);
         }
     }
 }
