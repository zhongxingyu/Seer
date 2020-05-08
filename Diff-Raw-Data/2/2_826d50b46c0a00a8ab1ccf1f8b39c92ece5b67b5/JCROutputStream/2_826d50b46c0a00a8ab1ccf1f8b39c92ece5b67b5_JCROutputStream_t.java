 package org.wyona.yarep.impl.repo.jcr;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.apache.log4j.Category;
 
 import org.wyona.yarep.impl.repo.jcr.JCRNode;
 
 /**
  * OutputStream which sets some properties (lastModified, size) to the node 
  * when the stream is closed.
  * 
  * NOTE: Currently not used, because the Node implemenation uses the lastModified and size
  * of the content file.
  */
 public class JCROutputStream extends OutputStream {
 
     private static Category log = Category.getInstance(JCROutputStream.class);
 
     private java.io.ByteArrayOutputStream out;
     private JCRNode node;
 
     /**
      * 
      */
     public JCROutputStream(JCRNode node) {
         this.node = node;
         this.out = new java.io.ByteArrayOutputStream();
     }
     
     /**
      * 
      */
     public void write(int b) throws IOException {
         out.write(b);
     }
 
     public void write(byte[] b, int off, int len) throws IOException {
         out.write(b, off, len);
     }
     
     public void write(byte[] b) throws IOException {
         out.write(b);
     }
 
     /**
      * 
      */
     public void close() throws IOException {
         try {
             //node.getJCRNode().setProperty(JCRNode.BINARY_CONTENT_PROP_NAME, new java.io.ByteArrayInputStream(out.toByteArray()));
 
             //log.error("DEBUG: Last Modified: " + node.getJCRNode().getNode("jcr:content").getProperty("jcr:lastModified").getDate().getTimeInMillis());
             //log.error("DEBUG: Last Modified: " + node.getJCRResourceNode().getProperty("jcr:lastModified").getDate().getTimeInMillis());
 
             javax.jcr.Node resourceNode;
             if (node.getJCRNode().hasNode("jcr:content")) {
                 resourceNode = node.getJCRNode().getNode("jcr:content");
             } else {
                 resourceNode = node.getJCRNode().addNode("jcr:content", "nt:resource");
             }
             if (node.getMimeType() != null) {
                log.warn("DEBUG: Set mime type of jcr:data property: " + node.getMimeType());
                 if (node.getMimeType().equals("application/xml")) {
                     log.warn("Replace application/xml by text/xml");
                     resourceNode.setProperty("jcr:mimeType", "text/xml");
                 } else {
                     resourceNode.setProperty("jcr:mimeType", node.getMimeType());
                 }
             } else {
                 log.warn("No mime type set, hence use application/octet-stream");
                 resourceNode.setProperty("jcr:mimeType", "application/octet-stream");
             }
             resourceNode.setProperty("jcr:data", new java.io.ByteArrayInputStream(out.toByteArray()));
             resourceNode.setProperty("jcr:lastModified", new java.util.GregorianCalendar());
             out.close();
             node.getJCRSession().save();
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new IOException(e.getMessage());
         }
     }
 
 }
