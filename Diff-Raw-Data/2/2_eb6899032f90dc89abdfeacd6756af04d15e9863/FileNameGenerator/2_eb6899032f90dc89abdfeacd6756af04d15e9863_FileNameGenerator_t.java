 package com.washingtonpost.videocomments;
 
 import org.red5.logging.Red5LoggerFactory;
 import org.red5.server.api.IScope;
 import org.red5.server.api.stream.IStreamFilenameGenerator;
 import org.slf4j.Logger;
 
 import java.io.File;
 
 public class FileNameGenerator implements IStreamFilenameGenerator {
 
     private Logger log = Red5LoggerFactory.getLogger(FileNameGenerator.class);
 
     private String path;
 
     public void init() {
         File file = new File(path);
         if (!file.exists()) {
             if (!file.mkdirs()) {
                 throw new RuntimeException("Can't create directory for video:" + file.getAbsolutePath());
             }
         }
     }
 
     /**
      * Generate stream directory based on relative scope path. The base directory is
      * <code>streams</code>, e.g. a scope <code>/application/one/two/</code> will
      * generate a directory <code>/streams/one/two/</code> inside the application.
      *
      * @param scope Scope
      * @return Directory based on relative scope path
      */
     private String getStreamDirectory(IScope scope) {
         return path;
     }
 
     /**
      * {@inheritDoc}
      */
     public String generateFilename(IScope scope, String name, GenerationType type) {
         return generateFilename(scope, name, null, type);
     }
 
     /**
      * {@inheritDoc}
      */
     public String generateFilename(IScope scope, String name, String extension, GenerationType type) {
        String result = getStreamDirectory(scope) + File.separator + name;
         if (extension != null && !extension.equals("")) {
             result += extension;
         }
         return result;
     }
 
     /**
      * The default filenames are relative to the scope path, so always return <code>false</code>.
      *
      * @return always <code>false</code>
      */
     public boolean resolvesToAbsolutePath() {
         return true;
     }
 
     public void setPath(String path) {
         this.path = path;
     }
 }
