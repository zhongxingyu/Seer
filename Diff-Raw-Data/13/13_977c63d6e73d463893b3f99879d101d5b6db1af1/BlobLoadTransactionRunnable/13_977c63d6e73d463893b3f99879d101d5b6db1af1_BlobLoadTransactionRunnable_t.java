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
 package org.xwiki.store.blob.datanucleus.internal;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.jdo.annotations.Index;
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import org.apache.commons.io.IOUtils;
 import org.xwiki.store.TransactionRunnable;
 
 class BlobLoadTransactionRunnable extends TransactionRunnable<PersistenceManager>
 {
     private final String blobId;
     private final OutputStream writeTo;
 
     public BlobLoadTransactionRunnable(final String blobId, final OutputStream writeTo)
     {
         this.blobId = blobId;
         this.writeTo = writeTo;
     }
 
     @Override
     protected void onRun() throws IOException
     {
         final PersistenceManager pm = this.getContext();
         final Blob b = pm.getObjectById(Blob.class, this.blobId);
         long version = b.getActiveVersion();
         try {
             for (int i = 0;; i++) {
                 final Object chunkId = BlobChunk.makeId(this.blobId, version, i);
                 final BlobChunk bc = pm.getObjectById(BlobChunk.class, chunkId);
                 IOUtils.write(bc.getContent(), this.writeTo);
                 pm.evict(bc);
             }
         } catch (JDOObjectNotFoundException e) {
             // No more chunks to get.
         }
        writeTo.close();
     }
 }
