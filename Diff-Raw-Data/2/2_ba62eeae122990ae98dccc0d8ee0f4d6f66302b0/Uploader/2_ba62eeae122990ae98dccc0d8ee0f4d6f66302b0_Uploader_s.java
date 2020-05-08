 package ee.mattijagula.mikker.upload;
 
 import ee.mattijagula.mikker.Configuration;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Uploader {
     private Configuration ctx;
 
     public Uploader(Configuration ctx) {
         this.ctx = ctx;
     }
 
     public void upload(byte content[], ProgressListener progressListener) throws UploadFailedException {
         try {
             doUpload(content, progressListener);
         } catch (IOException e) {
             throw new UploadFailedException("Upload failed with exception: " + e.getMessage(), e);
         }
     }
 
     private void doUpload(final byte content[], ProgressListener progressListener)
             throws IOException, UploadFailedException {
         HttpClient client = new HttpClient();
 
         PostMethod post = new PostMethod(ctx.getUploadUrl());
 
         post.addRequestHeader("User-Agent", ctx.getUserAgent());
 
         if (!"".equals(ctx.getCookies())) {
             post.addRequestHeader("Cookie", ctx.getCookies());
         }
 
         PartSource partSource = new PartSource() {
             public long getLength() {
                 return content.length;
             }
             public String getFileName() {
                 return ctx.getUploadFilename();
             }
             public InputStream createInputStream() throws IOException {
                 return new ByteArrayInputStream(content);
             }
         };
 
         Part[] partArray = composeParts(partSource);
 
         MultipartRequestEntity requestEntity = new MultipartRequestEntity(partArray, post.getParams());
         post.setRequestEntity(new ProgressReportingRequestEntity(requestEntity, progressListener));
 
         int status = client.executeMethod(post);
        if (status >= 200 && status <= 400) {
             progressListener.finished();
             System.out.println("Upload complete, response: " + status + " - " + post.getResponseBodyAsString());
         } else {
             throw new UploadFailedException("Upload failed with code " + status
                     + " : " + HttpStatus.getStatusText(status));
         }
     }
 
     private Part[] composeParts(PartSource partSource) {
         List<Part> parts = new ArrayList<Part>();
         
         parts.add(new FilePart(ctx.getFileFieldName(), partSource, ctx.getUploadMimeType(), "utf-8"));
         for (KeyValuePairParser.Pair pair : ctx.getAdditionalPostParameters()) {
             parts.add(new StringPart(pair.key, pair.value));
         }
 
         return parts.toArray(new Part[parts.size()]);
     }
 }
