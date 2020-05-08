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
 package org.xwiki.store.attachments.datanucleus.internal;
 
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiAttachmentContent;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 import javax.jdo.PersistenceManager;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.model.reference.AttachmentReference;
 import org.xwiki.model.reference.EntityReferenceSerializer;
 import org.xwiki.store.attachments.newstore.internal.AttachmentContentStore;
 import org.xwiki.store.TransactionRunnable;
 import org.xwiki.store.blob.BlobStore;
 
 import java.io.IOException;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 
 /**
  * A means of storing the content of an attachment in a datanucleus based store.
  *
  * @version $Id$
  * @since TODO
  */
 @Component
 @Named("datanucleus")
 @Singleton
 public class DataNucleusAttachmentContentStore implements AttachmentContentStore<PersistenceManager>
 {
     /** The underlying blob store. */
     @Inject
     @Named("datanucleus")
     private BlobStore<PersistenceManager> blobStore;
 
     /** A serializer for attachment references. */
     @Inject
     private EntityReferenceSerializer<String> referenceSerializer;
 
     @Override
     public TransactionRunnable<PersistenceManager> getAttachmentContentSaveRunnable(
             final XWikiAttachmentContent content)
     {
         final String id = idForAttach(content.getAttachment());
         return this.blobStore.getSaveRunnable(id, content.getContentInputStream());
     }
 
     @Override
     public TransactionRunnable<PersistenceManager> getAttachmentContentLoadRunnable(final XWikiAttachment attachment)
     {
         final PipedInputStream pis = new PipedInputStream();
         final PipedOutputStream pos = new PipedOutputStream();
         final Exception[] exception = new Exception[1];
         final Thread thread = new Thread(new Runnable() {
             @Override
             public void run()
             {
                 try {
                     attachment.setContent(pis);
                 } catch (Exception e) {
                     exception[0] = e;
                 }
             }
         });
         final TransactionRunnable tr = (new TransactionRunnable() {
             @Override
             protected void onRun() throws IOException
             {
                 pos.connect(pis);
                 thread.start();
             }
             @Override
            protected void onComplete() throws InterruptedException
             {
                 if (thread.isAlive()) {
                     thread.interrupt();
                    thread.join(1000);
                    if (thread.isAlive()) { throw new RuntimeException("Failed to stop pipe thread."); }
                 }
                 if (exception[0] != null) {
                     throw new RuntimeException("Exception in pipe thread", exception[0]);
                 }
             }
         });
         final TransactionRunnable loadTr = this.blobStore.getLoadRunnable(this.idForAttach(attachment), pos);
         loadTr.runIn(tr);
         return tr;
     }
 
     @Override
     public TransactionRunnable<PersistenceManager> getAttachmentContentDeleteRunnable(final XWikiAttachment attachment)
     {
         return this.blobStore.getDeleteRunnable(this.idForAttach(attachment));
     }
 
     /**
      * Get a serialized attachment reference for the attachment.
      *
      * @param attach the attachment to get a serialized reference for.
      * @return the serialized reference for the given attachment.
      */
     private String idForAttach(final XWikiAttachment attach)
     {
         final AttachmentReference ar =
                 new AttachmentReference(attach.getFilename(), attach.getDoc().getDocumentReference());
         return this.referenceSerializer.serialize(ar);
     }
 }
