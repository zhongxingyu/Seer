 package vinna.http;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import vinna.exception.VuntimeException;
 import vinna.util.MultivaluedHashMap;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.*;
 
 public class VinnaMultipartWrapper extends VinnaRequestWrapper implements MultipartRequest {
     private static Logger logger = LoggerFactory.getLogger(VinnaMultipartWrapper.class);
 
     private final HttpServletRequest request;
     private final MultivaluedHashMap<String, String> parameters;
     private final Map<String, FileItem> files;
 
     public VinnaMultipartWrapper(HttpServletRequest servletRequest, File temporaryDirectory) {
         super(servletRequest);
         this.request = servletRequest;
         this.parameters = new MultivaluedHashMap<>();
         this.files = new HashMap<>();
 
         try {
             DiskFileItemFactory factory = new DiskFileItemFactory();
             factory.setRepository(temporaryDirectory);
             //factory.setSizeThreshold(1234); // TODO
             ServletFileUpload upload = new ServletFileUpload(factory);
             //upload.setSizeMax(1234); // TODO
 
             List<FileItem> parts = upload.parseRequest(request);
             for (FileItem part : parts) {
                 if (part.isFormField()) {
                     // FIXME use request.getCharacterEncoding as encoding ?
                     parameters.add(part.getFieldName(), part.getString(request.getCharacterEncoding()));
                 } else {
                     files.put(part.getFieldName(), part);
                 }
             }
 
         } catch (FileUploadException e) {
             logger.error("Error while parsing a multipart request", e);
             throw new VuntimeException(e);
         } catch (UnsupportedEncodingException e) {
             // TODO
             e.printStackTrace();
         }
     }
 
     @Override
     public Collection<String> getPartsName() {
         return Collections.unmodifiableCollection(files.keySet());
     }
 
     @Override
     public InputStream getParts(String name) {
         try {
             return files.get(name).getInputStream();
         } catch (IOException e) {
            logger.error("Error while retrieving the multipart data", e);
             throw new VuntimeException(e);
         }
     }
 }
