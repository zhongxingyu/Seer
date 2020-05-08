 package com.tjh.swivel.model;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.FileEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.message.BasicHttpResponse;
 import org.apache.http.message.BasicStatusLine;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.util.Map;
 
 public class ResponseFactory {
     private static Logger LOGGER = Logger.getLogger(ResponseFactory.class);
 
     public static final String STATUS_CODE_KEY = "statusCode";
     public static final String CONTENT_KEY = "content";
     public static final String CONTENT_TYPE_KEY = "contentType";
     public static final String FILE_NAME_KEY = "fileName";
 
     public HttpResponse createResponse(int code, String stringEntity, String contentType) {
         BasicHttpResponse result = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, code, null));
         if (stringEntity != null) {
             StringEntity entity = new StringEntity(stringEntity, ContentType.create(contentType));
             result.setEntity(entity);
         }
         return result;
     }
 
     //TODO:TJH - content disposition should be defined on the stub
     public HttpResponse createResponse(Map<String, Object> then, File responseFile) {
         HttpResponse result = createResponse(getStatusCode(then));
        FileEntity fileEntity = new FileEntity(responseFile, ContentType.create((String) then.get(CONTENT_KEY)));
         result.setEntity(fileEntity);
         result.setHeader("Content-Disposition", "attachment; filename=\"" + then.get(FILE_NAME_KEY) + "\"");
         return result;
     }
 
     public HttpResponse createResponse(Map<String, Object> map) {
         return createResponse(getStatusCode(map),
                 (String) map.get(CONTENT_KEY),
                 (String) map.get(CONTENT_TYPE_KEY));
     }
 
     public HttpResponse createResponse(int code) { return createResponse(code, null, null); }
 
     private int getStatusCode(Map<String, Object> map) {return ((Number) map.get(STATUS_CODE_KEY)).intValue();}
 }
