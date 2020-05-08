 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.photo.utilities;
 
import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 
 /**
  * Used to add data as a new XWikiAttachment to an XWikiDocument.
  */
 public class AddAttachmentToDoc {
  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      AddAttachmentToDoc.class);
   
   /**
    * Converts the given ByteArrayOutputStream to an XWikiAttachment and adds
    * it to the specified XWikiDocument.
    * 
    * @param doc XWikiDocument to attach the attachment to.
    * @param data The attachment data.
    * @param filename The name for the attachment.
    * @param context XWikiContext. Needed to get the author and to save the
    *           attachment.
    * @return The attachment containing the given data.
    * @throws XWikiException
    */
   public XWikiAttachment addAtachment(XWikiDocument doc, ByteArrayOutputStream data, 
       String filename, XWikiContext context) throws XWikiException{
     return addAtachment(doc, data.toByteArray(), filename, context);
   }
   
   /**
    * Converts the given byte array to an XWikiAttachment and adds
    * it to the specified XWikiDocument.
    * 
    * @param doc XWikiDocument to attach the attachment to.
    * @param data The attachment data.
    * @param filename The name for the attachment.
    * @param context XWikiContext. Needed to get the author and to save the
    *           attachment.
    * @return The attachment containing the given data.
    * @throws XWikiException
    */
   public XWikiAttachment addAtachment(XWikiDocument doc, byte[] data, String filename, 
       XWikiContext context) throws XWikiException{
     XWikiDocument olddoc = (XWikiDocument) doc.clone();
     XWikiAttachment attachment = olddoc.getAttachment(filename);
 
     if (attachment==null) {
         attachment = new XWikiAttachment();
         olddoc.getAttachmentList().add(attachment);
     }
    LOGGER.info("filename='" + filename + "' contentsize='" + data.length + "'");
    ByteArrayInputStream dataStream = null;
    try {
      dataStream = new ByteArrayInputStream(data);
      attachment.setContent(dataStream);
    } catch (IOException ioe) {
      LOGGER.error("Error setting Attachment content", ioe);
    } finally {
      if(dataStream != null) {
        try {
          dataStream.close();
        } catch (IOException ioe) {
          LOGGER.error("Exception cloasing stream.", ioe);
        }
      }
    }
     attachment.setFilename(filename);
     attachment.setAuthor(context.getUser());
 
     attachment.setDoc(olddoc);
     
     olddoc.setAuthor(context.getUser());
     if (olddoc.isNew()) {
         olddoc.setCreator(context.getUser());
     }
     
     olddoc.saveAttachmentContent(attachment, context);
         
     return attachment;
   }
 }
