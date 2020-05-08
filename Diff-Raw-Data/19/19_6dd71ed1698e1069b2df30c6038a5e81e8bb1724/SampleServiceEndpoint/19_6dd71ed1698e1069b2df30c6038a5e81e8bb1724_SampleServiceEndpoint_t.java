 package ru.forketyfork.mtomsoap.server;
 
 import org.springframework.ws.server.endpoint.annotation.Endpoint;
 import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
 import org.springframework.ws.server.endpoint.annotation.RequestPayload;
 import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
 import ru.forketyfork.mtomsoap.schema.SampleRequest;
 import ru.forketyfork.mtomsoap.schema.SampleResponse;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.UUID;
 
 /**
  * @author Serge Petunin
  * @created 22.06.13 1:15
  */
 @Endpoint
 public class SampleServiceEndpoint {
 
     /** The path to upload received files. */
     private String uploadPath;
 
     @PayloadRoot(namespace = "http://forketyfork.ru/mtomsoap/schema", localPart = "SampleRequest")
     @ResponsePayload
     public SampleResponse serve(@RequestPayload SampleRequest request) throws IOException {
 
         // randomly generating file name as a UUID
         String fileName = UUID.randomUUID().toString();
         File file = new File(uploadPath + File.separator + fileName);
 
         // writing attachment to file
         try(FileOutputStream fos = new FileOutputStream(file)) {
             request.getFile().writeTo(fos);
         }
 
         // constructing the response
         SampleResponse response = new SampleResponse();
         response.setText(String.format("Hi, just received a %d byte file from ya, saved with id = %s",
                 file.length(), fileName));
 
         return response;
     }
 
     public void setUploadPath(String uploadPath) {
         this.uploadPath = uploadPath;
     }
 
 }
