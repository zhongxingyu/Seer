 package uk.ac.ox.oucs.search.producer;
 
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.search.api.StoredDigestContentProducer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.List;
 
 /**
  * @author Colin Hebert
  */
 public class BinaryContentHostingContentProducer extends ContentHostingContentProducer implements BinaryEntityContentProducer, StoredDigestContentProducer {
     private static final Logger logger = LoggerFactory.getLogger(BinaryContentHostingContentProducer.class);
     private static final byte[] EMPTY_DOCUMENT = new byte[0];
     private List<String> supportedContentTypes;
     private long documentMaximumSize = Long.MAX_VALUE;
 
     @Override
     public boolean isContentFromReader(String reference) {
         return false;
     }
 
     @Override
     public Reader getContentReader(String reference) {
         return null;
     }
 
     @Override
     public String getContent(String reference) {
         return null;
     }
 
     protected boolean isResourceTypeSupported(String contentType) {
         return supportedContentTypes.contains(contentType);
     }
 
     @Override
     public InputStream getContentStream(String ref) {
         ContentResource contentResource;
         try {
             contentResource = contentHostingService.getResource(getId(ref));
 
             if (contentResource.getContentLength() > documentMaximumSize) {
                logger.info("The document '" + ref + "' was bigger (" + contentResource.getContentLength() + "B)" +
                        "than the maximum expected size (" + documentMaximumSize + "B), its content won't be handled");
                 return new ByteArrayInputStream(EMPTY_DOCUMENT);
             } else {
                 return contentResource.streamContent();
             }
         } catch (Exception ex) {
             throw new RuntimeException("Failed to obtain content from " + ref, ex);
         }
     }
 
     public void setSupportedContentTypes(List<String> supportedContentTypes) {
         this.supportedContentTypes = supportedContentTypes;
     }
 
     public void setDocumentMaximumSize(long documentMaximumSize) {
         this.documentMaximumSize = documentMaximumSize;
     }
 }
