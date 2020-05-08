 /*
  * Copyright 2006 Amazon Technologies, Inc. or its affiliates.
  * Amazon, Amazon.com and Carbonado are trademarks or registered trademarks
  * of Amazon Technologies, Inc. or its affiliates.  All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.amazon.carbonado.repo.replicated;
 
 import java.io.OutputStream;
 import java.io.Writer;
 
 import junit.framework.TestSuite;
 
 import com.amazon.carbonado.Repository;
 import com.amazon.carbonado.RepositoryBuilder;
 import com.amazon.carbonado.RepositoryException;
 import com.amazon.carbonado.Storage;
 import com.amazon.carbonado.TestUtilities;
 
 import com.amazon.carbonado.repo.replicated.ReplicatedRepository;
 
 import com.amazon.carbonado.lob.Blob;
 import com.amazon.carbonado.lob.Clob;
 import com.amazon.carbonado.lob.ByteArrayBlob;
 import com.amazon.carbonado.lob.StringClob;
 
 import com.amazon.carbonado.sequence.StoredSequence;
 
 import com.amazon.carbonado.stored.StorableWithLobs;
 
 /**
  *
  *
  * @author Brian S O'Neill
  */
 public class TestStorables extends com.amazon.carbonado.TestStorables {
     public static void main(String[] args) {
         junit.textui.TestRunner.run(suite());
     }
 
     public static TestSuite suite() {
         TestSuite suite = new TestSuite();
         suite.addTestSuite(TestStorables.class);
         return suite;
     }
 
     public TestStorables(String name) {
         super(name);
     }
 
     @Override
     protected Repository buildRepository(boolean isMaster) throws RepositoryException {
         RepositoryBuilder replica = TestUtilities.newTempRepositoryBuilder("rr-replica");
         RepositoryBuilder master = TestUtilities.newTempRepositoryBuilder("rr-writer");
         ReplicatedRepositoryBuilder builder = new ReplicatedRepositoryBuilder();
         builder.setMaster(isMaster);
         builder.setReplicaRepositoryBuilder(replica);
         builder.setMasterRepositoryBuilder(master);
         return builder.build();
     }
 
     public void testAuthoritative() throws Exception {
         // Make sure authoritative storable is not replicated.
 
         Storage<StoredSequence> storage = getRepository().storageFor(StoredSequence.class);
 
         StoredSequence seq = storage.prepare();
         seq.setName("foo");
         seq.setInitialValue(0);
         seq.setNextValue(1);
         seq.insert();
 
         Storage<StoredSequence> replica = ((ReplicatedRepository) getRepository())
             .getReplicaRepository().storageFor(StoredSequence.class);
 
         seq = replica.prepare();
         seq.setName("foo");
         assertFalse(seq.tryLoad());
 
         Storage<StoredSequence> master = ((ReplicatedRepository) getRepository())
             .getMasterRepository().storageFor(StoredSequence.class);
 
         seq = master.prepare();
         seq.setName("foo");
         assertTrue(seq.tryLoad());
    }
 
     public void testBlobReplication() throws Exception {
         Storage<StorableWithLobs> storage = getRepository().storageFor(StorableWithLobs.class);
 
         Storage<StorableWithLobs> replica = ((ReplicatedRepository) getRepository())
             .getReplicaRepository().storageFor(StorableWithLobs.class);
         Storage<StorableWithLobs> master = ((ReplicatedRepository) getRepository())
             .getMasterRepository().storageFor(StorableWithLobs.class);
 
         StorableWithLobs lobs = storage.prepare();
         lobs.setBlobValue(new ByteArrayBlob("hello".getBytes()));
         lobs.insert();
 
         StorableWithLobs maLobs = master.prepare();
         maLobs.setId(lobs.getId());
         maLobs.load();
         assertEquals("hello", maLobs.getBlobValue().asString());
 
         // Test update via stream.
 
         Blob blob = lobs.getBlobValue();
         OutputStream out = blob.openOutputStream();
         out.write("world!!!".getBytes());
         out.close();
 
         assertEquals("world!!!", blob.asString());
         assertEquals("world!!!", maLobs.getBlobValue().asString());
 
         // Test length change.
         blob.setLength(6);
 
         assertEquals(6, blob.getLength());
         assertEquals(6, maLobs.getBlobValue().getLength());
 
         assertEquals("world!", blob.asString());
         assertEquals("world!", maLobs.getBlobValue().asString());
     }
 
     public void testClobReplication() throws Exception {
         Storage<StorableWithLobs> storage = getRepository().storageFor(StorableWithLobs.class);
 
         Storage<StorableWithLobs> replica = ((ReplicatedRepository) getRepository())
             .getReplicaRepository().storageFor(StorableWithLobs.class);
         Storage<StorableWithLobs> master = ((ReplicatedRepository) getRepository())
             .getMasterRepository().storageFor(StorableWithLobs.class);
 
         StorableWithLobs lobs = storage.prepare();
         lobs.setClobValue(new StringClob("hello"));
         lobs.insert();
 
         StorableWithLobs maLobs = master.prepare();
         maLobs.setId(lobs.getId());
         maLobs.load();
         assertEquals("hello", maLobs.getClobValue().asString());
 
         // Test update via stream.
 
         Clob clob = lobs.getClobValue();
         Writer out = clob.openWriter();
         out.write("world!!!");
         out.close();
 
         assertEquals("world!!!", clob.asString());
         assertEquals("world!!!", maLobs.getClobValue().asString());
 
         // Test length change.
         clob.setLength(6);
 
         assertEquals(6, clob.getLength());
         assertEquals(6, maLobs.getClobValue().getLength());
 
         assertEquals("world!", clob.asString());
         assertEquals("world!", maLobs.getClobValue().asString());
     }
 }
