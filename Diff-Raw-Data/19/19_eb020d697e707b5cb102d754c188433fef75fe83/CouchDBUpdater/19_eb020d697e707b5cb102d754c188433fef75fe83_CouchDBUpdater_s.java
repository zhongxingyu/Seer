 package org.jcouchdb.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.jcouchdb.document.DesignDocument;
 
 public class CouchDBUpdater extends AbstractCouchDBUpdater
 {
     private File designDocumentDir;
     
     public void setDesignDocumentDir(File designDocumentDir)
     {
         Assert.isTrue(designDocumentDir.exists(), "designDocumentDir must exist");
         Assert.isTrue(designDocumentDir.isDirectory(), "designDocumentDir must actually be a directory");
         this.designDocumentDir = designDocumentDir;
     }
 
     @Override
     protected List<DesignDocument> readDesignDocuments() throws IOException
     {
         Assert.notNull(designDocumentDir, "designDocumentDir can't be null");
 
         Collection<File> files = FileUtils.listFiles(designDocumentDir, new String[]{ "js"}, true);
 
         Map<String,DesignDocument> designDocuments = new HashMap<String, DesignDocument>();
 
         String designDocumentDirPath = designDocumentDir.getPath();
 
         for (File file : files)
         {
             String path = file.getPath();
             Assert.isTrue(path.startsWith(designDocumentDirPath), "not in dir");
 
             path = path.substring(designDocumentDirPath.length());
 
             boolean isMapFunction = path.endsWith(MAP_SUFFIX);
             boolean isReduceFunction = path.endsWith(REDUCE_SUFFIX);
             if (isMapFunction || isReduceFunction)
             {
                 String content = FileUtils.readFileToString(file);
 
                 if (content == null || content.trim().length() == 0 )
                 {
                     continue;
                 }
 
                createViewFor(path, content, designDocuments);
             }
         }
 
         return new ArrayList<DesignDocument>(designDocuments.values());
     }
 }
