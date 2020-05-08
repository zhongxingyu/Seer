 package pl.pks.memgen.io;
 
 import static org.apache.commons.io.IOUtils.copy;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Arrays;
 import pl.pks.memgen.UploadConfiguration;
 import pl.pks.memgen.api.Figure;
 import pl.pks.memgen.db.FigureStorageService;
 import com.amazonaws.services.s3.model.ObjectMetadata;
 import com.google.common.io.LimitInputStream;
 import com.yammer.dropwizard.logging.Log;
 
 public class FigureUploader {
 
     private static final Log LOG = Log.forClass(FigureUploader.class);
 
     private final FigureStorageService storageService;
     private final UploadConfiguration configuration;
 
     public FigureUploader(FigureStorageService storageService, UploadConfiguration configuration) {
         this.storageService = storageService;
         this.configuration = configuration;
     }
 
     public Figure fromLink(String url) {
         try {
             HttpURLConnection urlConnection = doHEADRequest(url);
             String contentType = urlConnection.getContentType();
             checkContentType(contentType);
             checkContentLength(urlConnection);
 
             InputStream inputStream = doGETRequest(url).getInputStream();
 
             return persist(inputStream, contentType);
 
         } catch (IOException | IllegalArgumentException e) {
             LOG.error(e, "Could not download file {}", url);
             throw new ImageDownloadException(e);
         }
     }
 
     public Figure fromDisk(InputStream uploadedInputStream, String contentType) {
         try {
             checkContentType(contentType);
             return persist(uploadedInputStream, contentType);
         } catch (IllegalArgumentException | IOException e) {
             LOG.error(e, "Could not download file");
             throw new ImageDownloadException(e);
         }
     }
 
     private Figure persist(InputStream uploadedInputStream, String contentType) throws IOException {
         LimitInputStream limitInputStream = new LimitInputStream(uploadedInputStream, configuration.getMaxSize());
         ByteArrayOutputStream temp = new ByteArrayOutputStream();
         int length = copy(limitInputStream, temp);
        checkContentLength(length);
         ObjectMetadata objectMetadata = getObjectMetadata(length);
         return storageService.save(contentType, objectMetadata, new ByteArrayInputStream(temp.toByteArray()));
     }
 
     private void checkContentLength(HttpURLConnection urlConnection) throws IOException {
         long contentLength = urlConnection.getContentLengthLong();
         checkContentLength(contentLength);
     }
 
     private void checkContentLength(long contentLength) {
         if (contentLength <= 0 || contentLength > configuration.getMaxSize()) {
             throw new IllegalArgumentException(String.format("Invalid content length %s", contentLength));
         }
     }
 
     private void checkContentType(String contentType) {
         boolean valid = Arrays.asList("image/jpeg", "image/png").contains(contentType);
         if (!valid) {
             throw new IllegalArgumentException(String.format("Invalid content type %s", contentType));
         }
     }
 
     private HttpURLConnection doHEADRequest(String url) throws IOException {
         HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
         urlConnection.setRequestMethod("HEAD");
         urlConnection.connect();
         return urlConnection;
     }
 
     private HttpURLConnection doGETRequest(String url) throws IOException {
         HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
         urlConnection.connect();
         return urlConnection;
     }
 
     private ObjectMetadata getObjectMetadata(long contentLength) {
         ObjectMetadata objectMetadata = new ObjectMetadata();
         objectMetadata.setContentLength(contentLength);
         return objectMetadata;
     }
 
 }
