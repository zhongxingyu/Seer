 package com.tjh.swivel.config.model;
 
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import vanderbilt.util.Maps;
 
 import java.net.URI;
 import java.util.Map;
 
 import static vanderbilt.util.Validators.notNull;
 
 public class When {
     public static final String METHOD_KEY = "method";
     public static final String SCRIPT_KEY = "script";
     public static final String CONTENT_KEY = "content";
     public static final String CONTENT_TYPE_KEY = "contentType";
    public static final String REMOTE_ADDRESS_KEY = "remoteAddress";
     private final HttpMethod method;
     private String content;
     private String contentType;
     private String remoteAddress;
     private String script;
     private URI uri;
 
     public When(HttpMethod method, URI uri) {
         this.method = notNull("method", method);
         this.uri = notNull("uri", uri);
     }
 
     public JSONObject toJSON() {
         try {
             JSONObject jsonObject = new JSONObject(Maps.asMap(METHOD_KEY, method.getMethodName()));
 
             if (script != null) {
                 jsonObject.put(SCRIPT_KEY, script);
             } else {
                 Map<String, Object> optionalValues = Maps.<String, Object>asMap(
                         CONTENT_KEY, content,
                         CONTENT_TYPE_KEY, contentType,
                         REMOTE_ADDRESS_KEY, remoteAddress);
                 for (Map.Entry<String, Object> entry : optionalValues.entrySet()) {
                     jsonObject.putOpt(entry.getKey(), entry.getValue());
                 }
             }
             return jsonObject;
         } catch (JSONException e) {
             throw new RuntimeException(e);
         }
     }
 
     //<editor-fold desc="builder">
     public When withContent(String content) {
         setContent(content);
         return this;
     }
 
     public When as(String contentType) {
         setContentType(contentType);
         return this;
     }
 
     public When from(String remoteAddress) {
         setRemoteAddress(remoteAddress);
         return this;
     }
 
     //YELLOWTAG:TJH - Contemplating either 'helpfully' replacing '\\' with '\\\\', or
     //at least issuing a warning. Modifying data on a setter is full of problems,
     //but so is the nasty double-interpreted strings that will be coming through here.
     public When matches(String script) {
         setScript(script);
         return this;
     }
     //</editor-fold>
 
 
     //<editor-fold desc="Object">
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof When)) return false;
 
         When when = (When) o;
 
         return method == when.method
                 && uri.equals(when.uri)
                 && !(content != null ? !content.equals(when.content) : when.content != null)
                 && !(contentType != null ? !contentType.equals(when.contentType) : when.contentType != null)
                 && !(remoteAddress != null ? !remoteAddress.equals(when.remoteAddress) : when.remoteAddress != null)
                 && !(script != null ? !script.equals(when.script) : when.script != null);
     }
 
     @Override
     public int hashCode() {
         int result = method.hashCode();
         result = 31 * result + uri.hashCode();
         result = 31 * result + (content != null ? content.hashCode() : 0);
         result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
         result = 31 * result + (remoteAddress != null ? remoteAddress.hashCode() : 0);
         result = 31 * result + (script != null ? script.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder("When{");
         sb.append("method=").append(method);
         sb.append(", content='").append(content).append('\'');
         sb.append(", contentType='").append(contentType).append('\'');
         sb.append(", remoteAddress='").append(remoteAddress).append('\'');
         sb.append(", script='").append(script).append('\'');
         sb.append(", uri=").append(uri);
         sb.append('}');
         return sb.toString();
     }
 
     //</editor-fold>
 
     //<editor-fold desc="bean">
     public void setContent(String content) {
         if (!method.isAcceptsData()) {
             throw new IllegalStateException("HTTP '" + method.getMethodName() + "' does not accept data");
         }
         this.content = content;
     }
 
     public String getContent() { return content; }
 
     public HttpMethod getMethod() { return method; }
 
     public String getContentType() { return contentType; }
 
     public void setContentType(String contentType) { this.contentType = contentType; }
 
     public String getRemoteAddress() { return remoteAddress; }
 
     public void setRemoteAddress(String remoteAddress) { this.remoteAddress = remoteAddress; }
 
     public String getScript() { return script; }
 
     public void setScript(String script) { this.script = script; }
 
     public URI getUri() { return uri; }
     //</editor-fold>
 }
