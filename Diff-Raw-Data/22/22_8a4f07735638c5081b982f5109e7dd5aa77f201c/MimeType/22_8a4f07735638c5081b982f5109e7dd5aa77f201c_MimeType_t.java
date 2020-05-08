 package com.bics.jira.mail.model.mail;
 
 import javax.mail.MessagingException;
 import javax.mail.Part;
 import javax.mail.internet.ContentType;
 import java.util.Arrays;
 
 /**
  * JavaDoc here
  *
  * @author Victor Polischuk
  * @since 10.02.13 14:17
  */
 public enum MimeType {
     MULTIPART(new ContentType("multipart", "*", null)),
    MESSAGE(new ContentType("message", "rfc822", null)),
     HTML(new ContentType("text", "html", null)),
     TEXT(new ContentType("text", "plain", null)),
     OTHER(null);
 
     private final ContentType contentType;
 
     private MimeType(ContentType contentType) {
         this.contentType = contentType;
     }
 
     public static MimeType valueOf(Part part) throws MessagingException {
         ContentType partType = new ContentType(part.getContentType());
         MimeType[] values = values();
 
         for (MimeType mimeType : Arrays.copyOf(values, values.length - 1)) {
             if (partType.match(mimeType.contentType)) {
                 return mimeType;
             }
         }
 
         return OTHER;
     }
 }
