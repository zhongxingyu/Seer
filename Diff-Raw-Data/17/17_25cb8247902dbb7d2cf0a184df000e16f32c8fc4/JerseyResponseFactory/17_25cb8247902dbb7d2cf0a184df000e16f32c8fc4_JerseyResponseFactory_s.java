 package com.tjh.swivel.controller;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import java.io.IOException;
 
 public class JerseyResponseFactory {
     public Response createResponse(HttpResponse response) throws IOException {
         StatusLine statusLine = response.getStatusLine();
         Response.ResponseBuilder builder =
                 Response.status(statusLine.getStatusCode());
         HttpEntity entity = response.getEntity();
         if (entity != null) {
             builder.entity(entity.getContent());
         }
 
         for (Header header : response.getAllHeaders()) {
            builder.header(header.getName(), header.getValue());
            if (header.getName().equals(HttpHeaders.CONTENT_TYPE)) {
                builder.type(header.getValue());
             }
         }
 
         return builder.build();
     }
 }
