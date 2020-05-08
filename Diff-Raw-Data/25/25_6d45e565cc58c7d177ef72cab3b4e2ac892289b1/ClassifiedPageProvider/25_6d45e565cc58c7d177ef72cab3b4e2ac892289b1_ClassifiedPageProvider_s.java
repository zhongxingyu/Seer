 package org.nuxeo.ecm.platform.classification;
 
 import org.nuxeo.ecm.classification.api.adapter.Classification;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.platform.query.api.AbstractPageProvider;
 
 import java.util.List;
 
 /**
  * Page Provider using for classified documents module inside the
  * currentDocument property. Do not handle paging right now.
  * 
  * @since 5.7
  */
 public class ClassifiedPageProvider extends AbstractPageProvider<DocumentModel> {
     @Override
     public List<DocumentModel> getCurrentPage() {
         try {
             Classification classification = getClassification();
             if (classification != null) {
                 return classification.getClassifiedDocuments();
             }
         } catch (ClientException e) {
             log.info(e, e);
         }
         return null;
     }
 
     protected Classification getClassification() {
         DocumentModel currentDocument = getCurrentDocument();
         return currentDocument.getAdapter(Classification.class);
     }
 
     protected DocumentModel getCurrentDocument() {
        return (DocumentModel) getProperties().get("currentDocument");
     }
 }
