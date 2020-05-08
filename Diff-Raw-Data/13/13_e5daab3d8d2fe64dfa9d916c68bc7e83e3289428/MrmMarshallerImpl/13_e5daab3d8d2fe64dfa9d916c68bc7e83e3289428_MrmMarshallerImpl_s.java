 package org.jfrog.wharf.ivy.marshall;
 
 
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonParser;
 import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * @author Tomer Cohen
  */
 public class MrmMarshallerImpl implements MrmMarshaller {
 
     public ModuleRevisionMetadata getModuleRevisionMetadata(File file) {
         if (file.exists()) {
             InputStream inputStream = null;
             try {
                 inputStream = new FileInputStream(file);
                 JsonParser jsonParser = JacksonFactory.createJsonParser(inputStream);
                 return jsonParser.readValueAs(ModuleRevisionMetadata.class);
             } catch (IOException e) {
                // TODO: Log error in ivy wrong MD file.
                 // Delete the file (send exception if delete impossible) and returns null
                 file.delete();
             } finally {
                 if (inputStream != null) {
                     try {
                         inputStream.close();
                     } catch (IOException e) {
                         // ignore
                     }
                 }
             }
         }
         return null;
     }
 
     public void save(ModuleRevisionMetadata mrm, File file) {
         OutputStream stream = null;
         try {
             File dir = file.getParentFile();
             if (!dir.exists()) {
                 dir.mkdirs();
             }
             stream = new FileOutputStream(file);
             JsonGenerator generator = JacksonFactory.createJsonGenerator(stream);
             generator.writeObject(mrm);
         } catch (IOException e) {
             throw new RuntimeException(e);
         } finally {
             if (stream != null) {
                 try {
                     stream.close();
                 } catch (IOException e) {
                     // ignore
                 }
             }
         }
     }
 }
