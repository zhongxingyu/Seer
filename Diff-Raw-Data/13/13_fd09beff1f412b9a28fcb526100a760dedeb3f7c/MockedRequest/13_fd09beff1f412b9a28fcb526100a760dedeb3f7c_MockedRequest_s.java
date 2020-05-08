 package vinna.request;
 
 import java.util.*;
 
 public class MockedRequest implements Request {
 
     private final String verb;
     private final String path;
     private final Map<String, Collection<String>> params;
     private final Map<String, Collection<String>> headers;
 
     private MockedRequest(String verb, String path, Map<String, Collection<String>> params, Map<String, Collection<String>> headers) {
         this.verb = verb;
         this.path = path;
         this.params = params;
         this.headers = headers;
     }
 
     @Override
     public String getVerb() {
         return verb;
     }
 
     @Override
     public String getPath() {
         return path;
     }
 
     @Override
     public String getParam(String name) {
        return params.get(name).iterator().next();
     }
 
     @Override
     public Collection<String> getParams(String name) {
         if (params.get(name) == null) {
             return Collections.emptyList();
         }
         return Collections.unmodifiableCollection(params.get(name));
     }
 
     @Override
     public Map<String, Collection<String>> getParams() {
         return Collections.unmodifiableMap(params);
     }
 
     @Override
     public String getHeader(String name) {
        return headers.get(name).iterator().next();
     }
 
     @Override
     public Collection<String> getHeaders(String name) {
         if (headers.get(name) == null) {
             return Collections.emptyList();
         }
         return Collections.unmodifiableCollection(headers.get(name));
     }
 
     @Override
     public Map<String, Collection<String>> getHeaders() {
         return Collections.unmodifiableMap(headers);
     }
 
     public static Builder get(String path) {
         return new Builder("GET", path);
     }
 
     public static Builder post(String path) {
         return new Builder("POST", path);
     }
 
     public static class Builder {
         private final String verb;
         private final String path;
         private final Map<String, Collection<String>> params = new HashMap<>();
         private final Map<String, Collection<String>> headers = new HashMap<>();
 
         private Builder(String verb, String path) {
             this.verb = verb;
             this.path = path;
         }
 
         public Builder param(String name, String value) {
             params.put(name, Collections.singleton(value));
             return this;
         }
 
         public Builder param(String name, String... values) {
             params.put(name, Arrays.asList(values));
             return this;
         }
 
         public Builder header(String name, String value) {
             headers.put(name, Collections.singleton(value));
             return this;
         }
 
         public Builder header(String name, String... values) {
             headers.put(name, Arrays.asList(values));
             return this;
         }
 
         public MockedRequest build() {
             return new MockedRequest(verb, path, params, headers);
         }
     }
 }
