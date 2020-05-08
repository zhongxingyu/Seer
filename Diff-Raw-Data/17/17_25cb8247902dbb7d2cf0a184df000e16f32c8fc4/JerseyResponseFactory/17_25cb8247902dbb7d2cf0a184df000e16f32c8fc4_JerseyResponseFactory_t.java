 package com.tjh.swivel.controller;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
import vanderbilt.util.Sets;
 
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import java.io.IOException;
import java.util.Set;
 
 public class JerseyResponseFactory {
    //REDTAG:TJH - another place where proxy behavior and HTTP headers needs to be more fully
    //researched and implemented
    public static final Set<String> EXCLUDED_HEADERS = Sets.asConstantSet("Transfer-Encoding");

     public Response createResponse(HttpResponse response) throws IOException {
         StatusLine statusLine = response.getStatusLine();
         Response.ResponseBuilder builder =
                 Response.status(statusLine.getStatusCode());
         HttpEntity entity = response.getEntity();
         if (entity != null) {
             builder.entity(entity.getContent());
         }
 
         for (Header header : response.getAllHeaders()) {
            if (!EXCLUDED_HEADERS.contains(header.getName())) {
                builder.header(header.getName(), header.getValue());
                if (header.getName().equals(HttpHeaders.CONTENT_TYPE)) {
                    builder.type(header.getValue());
                }
             }
         }
 
         return builder.build();
     }
 }
