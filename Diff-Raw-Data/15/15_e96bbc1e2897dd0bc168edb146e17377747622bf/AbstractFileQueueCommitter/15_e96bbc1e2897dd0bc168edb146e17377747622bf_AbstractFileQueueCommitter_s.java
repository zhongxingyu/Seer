 /* Copyright 2010-2013 Norconex Inc.
  * 
  * This file is part of Norconex Committer.
  * 
  * Norconex Committer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Norconex Committer is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of 
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Norconex Committer. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.norconex.committer;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 
 import com.norconex.committer.impl.FileSystemCommitter;
 import com.norconex.commons.lang.io.FileUtil;
 import com.norconex.commons.lang.io.IFileVisitor;
 import com.norconex.commons.lang.map.Properties;
 
 //TODO Maybe offer pluggable implementations for where to queue (FS, DB, etc)?
 
 
 /**
  * Queues documents on filesystem, leaving only the committing of additions 
  * and deletions to implement.  Subclasses can optionally implement
  * {@link #prepareCommitAddition(IAddOperation)} and 
  * {@link #prepareCommitDeletion(IDeleteOperation)} to manipulate the 
  * data supplied with the operations before committing takes place.
  * <p/>
  * To also control how many documents are sent on each call to 
  * a remote repository, consider extending {@link AbstractBatchCommitter}.
  * 
  * @author Pascal Essiembre
  * @since 1.1.0
  */
 @SuppressWarnings("nls")
 public abstract class AbstractFileQueueCommitter extends AbstractCommitter {
 
     private static final long serialVersionUID = -5775959203678116077L;
 
     /** Default directory where to queue files. */
     public static final String DEFAULT_QUEUE_DIR = "./committer-queue";
 
     private static final FileFilter NON_META_FILTER = new FileFilter() {
         @Override
         public boolean accept(File pathname) {
             return !pathname.getName().endsWith(".meta");
         }
     };
 
     private final FileSystemCommitter queue = new FileSystemCommitter();
 
     /**
      * Constructor.
      */
     public AbstractFileQueueCommitter() {
         super();
         queue.setDirectory(DEFAULT_QUEUE_DIR);
     }
     /**
      * Constructor.
      * @param batchSize batch size
      */
     public AbstractFileQueueCommitter(int batchSize) {
         super(batchSize);
         queue.setDirectory(DEFAULT_QUEUE_DIR);
     }
 
     /**
      * Gets the directory where queued files are stored.
      * @return directory
      */
     public String getQueueDir() {
         return queue.getDirectory();
     }
     /**
      * Sets the directory where queued files are stored.
      * @param queueDir directory
      */
     public void setQueueDir(String queueDir) {
         this.queue.setDirectory(queueDir);
     }
     
     @Override
     protected void queueAddittion(
             String reference, File document, Properties metadata) {
         queue.queueAdd(reference, document, metadata);
     }
 
     @Override
     protected void queueRemoval(
             String ref, File document, Properties metadata) {
         queue.queueRemove(ref, document, metadata);
     }
 
     @Override
     public void commit() {
 
         // --- Additions ---
         FileUtil.visitAllFiles(queue.getAddDir(), new IFileVisitor() {
             @Override
             public void visit(File file) {
                 try {
                     IAddOperation op = new FileAddOperation(file);
                     prepareCommitAddition(op);
                     commitAddition(op);
                 } catch (IOException e) {
                     throw new CommitterException(
                             "Cannot create document for file: " + file, e);
                 }
             }
         }, NON_META_FILTER);
 
         // --- Deletions ---
         FileUtil.visitAllFiles(queue.getRemoveDir(), new IFileVisitor() {
             @Override
             public void visit(File file) {
                 try {
                     IDeleteOperation op = new FileDeleteOperation(file);
                     prepareCommitDeletion(op);
                     commitDeletion(op);
                 } catch (IOException e) {
                     throw new CommitterException(
                             "Cannot read reference from : " + file, e);
                 }
             }
         });
 
         commitComplete();
     }
 
     /**
      * <p>
      * Allow subclasses to commit a file to be added.
      * </p>
      * <p>
      * The subclass has the responsibility of deleting the file once the content
      * is permanently stored by invoking 
      * {@link QueuedAddedDocument#deleteFromQueue()}. 
      * The subclass may decide to further batch those documents before
      * storing them if more efficient this way.
      * </p>
      *
      * @param operation the document operation to perform
      * @throws IOException
      */
     protected abstract void commitAddition(IAddOperation operation)
             throws IOException;
 
     /**
      * <p>
      * Allow subclasses to commit a file to be deleted.
      * </p>
      * <p>
      * The subclass has the responsibility of deleting the file once the content
      * is permanently stored by invoking 
      * {@link QueuedDeletedDocument#deleteFromQueue()}. The subclass may 
      * decide to further batch those deletions before storing them if more
      * efficient that way.
      * </p>
      *
      * @param operation the document operation to perform
      * @throws IOException
      */
     protected abstract void commitDeletion(IDeleteOperation operation)
             throws IOException;
 
     /**
      * Allow subclasses to operate upon the end of the commit operation.
      * 
      * For example, if the subclass decided to batch documents to commit, it may
      * decide to store all remaining documents on that event.
      * 
      */
     protected abstract void commitComplete();
 
     /**
      * Optionally performs actions on a document to be added before
      * actually committing it.  Default implementation does nothing.
      * @param operation addition to be performed
      * @throws IOException
      */
     protected void prepareCommitAddition(IAddOperation operation) 
             throws IOException {
         // Do nothing by default
     }
     /**
      * Optionally performs operations on a document to be deleted before
      * actually committing it.  Default implementation does nothing.
      * @param operation deletion to be performed
      * @throws IOException
      */
     protected void prepareCommitDeletion(IDeleteOperation operation)
         throws IOException {
         // Do nothing by default
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof AbstractFileQueueCommitter)) {
             return false;
         }
         AbstractFileQueueCommitter other = (AbstractFileQueueCommitter) obj;
         EqualsBuilder equalsBuilder = new EqualsBuilder();
         equalsBuilder.appendSuper(true);
         equalsBuilder.append(queue, other.queue);
         return equalsBuilder.isEquals();
     }
     
     @Override
     public int hashCode() {
         HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
         hashCodeBuilder.appendSuper(super.hashCode());
         hashCodeBuilder.append(queue);
         return hashCodeBuilder.toHashCode();
     }
     
     @Override
     public String toString() {
         ToStringBuilder builder = new ToStringBuilder(this);
         builder.appendSuper(super.toString());
         builder.append("queue", queue);
         return builder.toString();
     }
 }
