 package org.nuxeo.ecm.platform.categorization.categorizer.tfidf;
 
 import java.io.IOException;
 
 import org.nuxeo.ecm.platform.categorization.service.Categorizer;
 import org.nuxeo.ecm.platform.categorization.service.CategorizerFactory;
 
 public class TfIdfCategorizerFactory implements CategorizerFactory {
 
     public Categorizer loadInstance(String modelFile, boolean readonly)
             throws IOException {
         try {
             TfIdfCategorizer categorizer = TfIdfCategorizer.load(modelFile);
             if (readonly) {
                 categorizer.disableUpdate();
             }
             return categorizer;
         } catch (ClassNotFoundException e) {
            throw new IOException(e);
         }
     }
 
 }
