 package org.bahmni.webclients;
 
 import org.apache.http.Header;
 import org.apache.http.HttpMessage;
 import org.apache.http.message.BasicHeader;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class HttpHeaders extends HashMap<String, String> {
 
     public void addTo(HttpMessage httpMessage) {
        httpMessage.setHeaders(headerArray());
    }

    private Header[] headerArray() {
        ArrayList<Header> headers = new ArrayList<>();
         for (Map.Entry<String, String> entry : this.entrySet()) {
            headers.add(new BasicHeader(entry.getKey(), entry.getValue()));
         }
        return headers.toArray(new Header[headers.size()]);
     }
 }
