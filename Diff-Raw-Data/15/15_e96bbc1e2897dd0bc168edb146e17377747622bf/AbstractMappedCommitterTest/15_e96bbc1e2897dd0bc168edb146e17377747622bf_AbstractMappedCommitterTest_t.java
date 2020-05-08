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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.commons.configuration.XMLConfiguration;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 import com.norconex.commons.lang.map.Properties;
 
 /**
  * @author Pascal Dimassimo
  * @author Pascal Essiembre
  */
@SuppressWarnings({"nls","javadoc"})
 public class AbstractMappedCommitterTest {
 
 
 
     @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
     private StubCommitter committer;
     private boolean committed;
 
 //    private List<QueuedAddedDocument> listCommitAdd = 
 //            new ArrayList<QueuedAddedDocument>();
 
     private Properties metadata = new Properties();
 
     private String defaultReference = "1";
 
     /**
      * Sets up a committer for testing.
      * @throws IOException problem setting up committer
      */
     @Before
     public void setup() throws IOException {
         committer = new StubCommitter();
         File queue = tempFolder.newFolder("queue");
         committer.setQueueDir(queue.toString());
 
         committed = false;
 //        listCommitAdd.clear();
         metadata.clear();
         metadata.addString(
                 ICommitter.DEFAULT_DOCUMENT_REFERENCE, defaultReference);
     }
 
     /**
      * Test no commit if not enough document
      * @throws IOException could not create temporary file 
      */
     @Test
     public void testNoCommit() throws IOException {
         // Default batch size is 1000, so no commit should occur
         committer.queueAdd(defaultReference, tempFolder.newFile(), metadata);
         assertFalse(committed);
     }
 
     /**
      * Test commit if there is enough document.
      * @throws IOException could not create temporary file 
      */
     @Test
     public void testCommit() throws IOException {
         committer.setQueueSize(1);
         committer.queueAdd(defaultReference, tempFolder.newFile(), metadata);
         assertTrue(committed);
     }
 
     /**
      * Test setting the source and target IDs.
      * @throws IOException could not create temporary file
      */
     @Test
     public void testSetSourceAndTargetId() throws IOException {
 
         // Set a different source and target id
         String customSourceId = "mysourceid";
         committer.setIdSourceField(customSourceId);
         String customTargetId = "mytargetid";
         committer.setIdTargetField(customTargetId);
 
         // Store the source id value in metadata
         metadata.addString(customSourceId, defaultReference);
 
         // Add a doc (it should trigger a commit because batch size is 1)
         committer.setQueueSize(1);
         committer.queueAdd(defaultReference, tempFolder.newFile(), metadata);
 
         // Get the map generated
         assertEquals(1, committer.getCommitBatch().size());
         IAddOperation op = (IAddOperation) committer.getCommitBatch().get(0);
         Properties docMeta = op.getMetadata();
         
         // Check that customTargetId was used
         assertEquals(defaultReference, docMeta.getString(customTargetId));
 
         // Check that customSourceId was removed (default behavior)
         assertFalse(defaultReference, docMeta.containsKey(customSourceId));
     }
 
     /**
      * Test keeping source id field.
      * @throws IOException could not create temporary file 
      */
     @Test
     public void testKeepSourceId() throws IOException {
 
         committer.setKeepIdSourceField(true);
 
         // Add a doc (it should trigger a commit because batch size is 1)
         committer.setQueueSize(1);
         committer.queueAdd(defaultReference, tempFolder.newFile(), metadata);
//        committer.commit();
 
         // Get the map generated
         assertEquals(1, committer.getCommitBatch().size());
         IAddOperation op = (IAddOperation) committer.getCommitBatch().get(0);
 
         // Check that the source id is still there
         assertTrue(op.getMetadata().containsKey(
                 ICommitter.DEFAULT_DOCUMENT_REFERENCE));
     }
     
     class StubCommitter extends AbstractMappedCommitter {
 
         private static final long serialVersionUID = 5395010993071444611L;
 
         private List<ICommitOperation> commitBatch;
         
         
         @Override
         protected void commitBatch(List<ICommitOperation> batch) {
             commitBatch = batch;
         }
         
         /**
          * @return the operationCount
          */
         public List<ICommitOperation> getCommitBatch() {
             return commitBatch;
         }
 
 //        @Override
 //        protected void commitAddedDocument(QueuedAddedDocument document) 
 //                throws IOException {
 //            listCommitAdd.add(document);
 //        }
 //
 //        @Override
 //        protected void commitDeletedDocument(QueuedDeletedDocument document) 
 //                throws IOException {
 //            //TODO implement me
 //        }
 
         @Override
         protected void commitComplete() {
            super.commitComplete();
             committed = true;
         }
 
         @Override
         protected void saveToXML(XMLStreamWriter writer)
                 throws XMLStreamException {
             // no saving
         }
 
         @Override
         protected void loadFromXml(XMLConfiguration xml) {
             // no loading
         }
     }
 }
