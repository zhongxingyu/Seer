 package org.codehaus.xfire.attachments;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PushbackInputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.mail.Header;
 import javax.mail.MessagingException;
 import javax.mail.internet.InternetHeaders;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.util.CachedOutputStream;
 
 public class StreamedAttachments implements Attachments
 {
     public static final String ATTACHMENT_DIRECTORY = "attachment-directory";
     public static final String ATTACHMENT_MEMORY_THRESHOLD = "attachment-memory-threshold";
     
     private boolean soapMessageRead = false;
     private PushbackInputStream stream;
     private String boundary;
     private int threshold = 1024*100;
     private File tempDirectory;
     private Map parts = new HashMap();
     private Attachment soapMessage;
     private String contentType;
     private List cache = new ArrayList();
     private MessageContext context;
     
     public StreamedAttachments(InputStream is, String contentType) throws IOException
     {
         this(null, is, contentType);
     }
     
     public StreamedAttachments(MessageContext context, InputStream is, String contentType) throws IOException
     {
         int i = contentType.indexOf("boundary=\"");
         int end;
         int len;
         if (i == -1)
         {
             i = contentType.indexOf("boundary=");
             end = contentType.indexOf(";", i+9);
             if (end == -1) end = contentType.length();
             len = 9;
         }
         else
         {
             end = contentType.indexOf("\"", i+10);
             len = 10;
         }
         
         if (i == -1 || end == -1)
             throw new IOException("Invalid content type: missing boundary! " + contentType);
         
         this.boundary = "--" + contentType.substring(i + len, end);
 
         this.stream = new PushbackInputStream(is, boundary.length());
         this.contentType = contentType;
         
         if (!readTillFirstBoundary(stream, boundary.getBytes()))
             throw new IOException("Couldn't find MIME boundary: " + boundary);
         
         this.context = context;
     }
 
     public void addPart(Attachment part)
     {
         throw new UnsupportedOperationException();
     }
 
     public String getContentType()
     {
         return contentType;
     }
 
     public Attachment getPart(String id)
     {
         if (!parts.containsKey(id)) readTo(id);
         
         return (Attachment) parts.get(id);
     }
 
     public Iterator getParts()
     {
         ensureAllPartsRead();
 
         return parts.values().iterator();
     }
 
     public String getSoapContentType()
     {
         ensureSoapAttachmentIsRead();
         
         return soapMessage.getHeader("Content-Type");
     }
 
     public Attachment getSoapMessage()
     {
         ensureSoapAttachmentIsRead();
         
         return soapMessage;
     }
 
     private void ensureSoapAttachmentIsRead()
     {
         if (soapMessageRead) return;
         
         try
         {
             soapMessage = readNextAttachment();
             soapMessageRead = true;
         }
         catch (IOException e)
         {
             throw new XFireRuntimeException("Could not read message!", e);
         }
     }
 
     private void ensureAllPartsRead()
     {
         readTo(null);
     }
 
     private void readTo(String id)
     {
         ensureSoapAttachmentIsRead();
         
         try 
         {
             for (Attachment a = readNextAttachment(); a != null; a = readNextAttachment())
             {
                 parts.put(a.getId(), a);
                 
                 if (a != null && id != null && a.getId().equals(id)) return;
             }
         }
         catch (IOException e)
         {
             throw new XFireRuntimeException("Could not read message!", e);
         }
     }
 
     
     private Attachment readNextAttachment()
         throws IOException
     {
         int v = stream.read();
         if (v == -1) return null;
         stream.unread(v);
         
         try
         {
             InternetHeaders headers = new InternetHeaders(stream);
             
             MimeBodyPartInputStream partStream = new MimeBodyPartInputStream(stream, boundary.getBytes());
             final CachedOutputStream cos = new CachedOutputStream(threshold, tempDirectory);
             
             copy(partStream, cos);
 
             final String ct = headers.getHeader("Content-Type", null);
             
             cache.add(cos);
             
             DataSource source = new AttachmentDataSource(ct, cos);
             
             DataHandler dh = new DataHandler(source);
             String id = headers.getHeader("Content-ID", null);
             if (id != null && id.startsWith("<"))
             {
                 id = id.substring(1, id.length() - 1);
             }
             
             SimpleAttachment att = new SimpleAttachment(id, dh);
 
             for (Enumeration e = headers.getAllHeaders(); e.hasMoreElements();)
             {
                 Header header = (Header) e.nextElement();
                 att.setHeader(header.getName(), header.getValue());
             }
             
             return att;
         }
         catch (MessagingException e)
         {
             throw new IOException("Couldn't read headers.");
         }
     }
 
     public void setSoapContentType(String soapMimeType)
     {
         throw new UnsupportedOperationException();
     }
 
     public void setSoapMessage(Attachment soapMessage)
     {
         throw new UnsupportedOperationException();
     }
 
     public int size()
     {
         ensureAllPartsRead();
         
         return parts.size();
     }
 
     public void write(OutputStream out)
         throws IOException
     {
         throw new UnsupportedOperationException();
     }
 
     public void dispose()
     {
         for (Iterator itr = cache.iterator(); itr.hasNext(); )
         {
             CachedOutputStream cos = (CachedOutputStream) itr.next();
             
             cos.dispose();
         }
     }
 
     /**
      * The directory where attachments will be written to if they exceed
      * the Threshold.
      * @return
      */
     public File getTempDirectory()
     {
         File td = null;
         
         if (context != null)
             td = (File) context.getContextualProperty(ATTACHMENT_DIRECTORY);
         
         if (td == null)
             td = tempDirectory;
         
         return td;
     }
 
     public void setTempDirectory(File tempDirectory)
     {
         this.tempDirectory = tempDirectory;
     }
 
     /**
      * Get the threshold in bytes. The threshold is the size an attachment
      * needs to reach before it is written to a temporary directory.
      * @return
      */
     public int getThreshold()
     {
         if (context != null)
         {
            Integer t = (Integer) context.getContextualProperty(ATTACHMENT_DIRECTORY);
             if (t != null) return t.intValue();
         }
         
         return threshold;
     }
 
     public void setThreshold(int threshold)
     {
         this.threshold = threshold;
     }
 
     protected static void copy(InputStream input, OutputStream output) throws IOException
     {
         try
         {
             final byte[] buffer = new byte[8096];
             int n = input.read(buffer);
             while (n > 0)
             {
                 output.write(buffer, 0, n);
                 n = input.read(buffer);
             }
         }
         finally
         {
             input.close();
             output.close();
         }
     }
     
     /**
      * Move the read pointer to the begining of the first part
      * read till the end of first boundary
      *
      * @param pushbackInStream
      * @param boundary
      * @throws MessagingException
      */
     private boolean readTillFirstBoundary(PushbackInputStream pushbackInStream, byte[] boundary) 
         throws IOException {
 
         // work around a bug in PushBackInputStream where the buffer isn't initialized
         // and available always returns 0.
         int value = pushbackInStream.read();
         pushbackInStream.unread(value);
         
         while (value != -1) {
             value = pushbackInStream.read();
             if ((byte) value == boundary[0]) {
                 int boundaryIndex = 0;
                 while (value != -1 && (boundaryIndex < boundary.length)
                         && ((byte) value == boundary[boundaryIndex])) {
                     value = pushbackInStream.read();
                     if (value == -1)
                         throw new IOException("Unexpected End of Stream while searching for first Mime Boundary");
                     boundaryIndex++;
                 }
                 if (boundaryIndex == boundary.length) { // boundary found
                     pushbackInStream.read();
                     return true;
                 }
             }
         }
 
         return false;
     }
     
     private class MimeBodyPartInputStream extends InputStream {
         PushbackInputStream inStream;
         boolean boundaryFound = false;
         byte[] boundary;
         
         public MimeBodyPartInputStream(PushbackInputStream inStream,
                                        byte[] boundary) {
             super();
             this.inStream = inStream;
             this.boundary = boundary;
         }
 
         public int read()
             throws IOException
         {
             if (boundaryFound)
             {
                 return -1;
             }
 
             // read the next value from stream
             int value = inStream.read();
 
             // Used to add back a new line if a boundary is not found.
             boolean encounteredNewLine = false;
             // A problem occured because all the mime parts tends to have a /r/n
             // at the end. Making it hard to transform them to correct
             // DataSources.
             // This logic introduced to handle it
             // TODO look more in to this && for a better way to do this
             if (value == 13)
             {
                 value = inStream.read();
                 if (value != 10)
                 {
                     inStream.unread(value);
                     return 13;
                 }
                 else
                 {
                     value = inStream.read();
                     if ((byte) value != boundary[0])
                     {
                         inStream.unread(value);
                         inStream.unread(10);
                         return 13;
                     }
                     else
                     {
                         encounteredNewLine = true;
                     }
                 }
             }
             else if ((byte) value != boundary[0])
             {
                 return value;
             }
             // read value is the first byte of the boundary. Start matching the
             // next characters to find a boundary
             int boundaryIndex = 0;
             while ((boundaryIndex < boundary.length) && ((byte) value == boundary[boundaryIndex]))
             {
                 value = inStream.read();
                 boundaryIndex++;
             }
             if (boundaryIndex == boundary.length)
             { // boundary found
                 boundaryFound = true;
                 // read the end of line character
                 if (inStream.read() == 45 && value == 45)
                 {
                     // Last mime boundary should have a succeeding "--"
                     // as we are on it, read the terminating CRLF
                     int value1 = inStream.read();
                     int value2 = inStream.read();
                 }
                 return -1;
             }
             // Boundary not found. Restoring bytes skipped.
 
             // write first skipped byte, push back the rest
             if (value != -1)
             { // Stream might have ended
                 inStream.unread(value);
             }
 
             inStream.unread(boundary, 1, boundaryIndex - 1);
 
             if (encounteredNewLine)
             {
                 // Put back 45
                 inStream.unread(boundary[0]);
                 // Put back 10
                 inStream.unread(10);
                 // Return the first character we read.
                 return 13;
             }
             else
             {
                 // No new line encountered, return as usual.
                 return boundary[0];
             }
         }
     }
 
 }
