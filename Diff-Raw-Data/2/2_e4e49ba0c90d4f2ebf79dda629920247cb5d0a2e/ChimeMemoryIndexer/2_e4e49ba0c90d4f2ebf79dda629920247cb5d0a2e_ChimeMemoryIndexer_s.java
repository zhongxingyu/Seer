 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.chime.extension;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.store.RAMDirectory;
 
 import com.paxxis.chime.client.common.DataInstance;
 import com.paxxis.chime.client.common.User;
 import com.paxxis.chime.client.common.extension.MemoryIndexer;
 import com.paxxis.chime.database.DatabaseConnection;
 import com.paxxis.chime.database.DatabaseConnectionPool;
 import com.paxxis.chime.indexing.ChimeAnalyzer;
 import com.paxxis.chime.indexing.IndexerBase;
 
 /**
  *
  * @author Robert Englander
  */
 public class ChimeMemoryIndexer implements MemoryIndexer {
 
     private RAMDirectory ramDirectory;
     private DatabaseConnectionPool dbPool = null;
 
     public ChimeMemoryIndexer() {
         try {
             ramDirectory = new RAMDirectory();
             ChimeAnalyzer analyzer = new ChimeAnalyzer();
             IndexWriter writer = new IndexWriter(ramDirectory, analyzer);
             writer.optimize();
             writer.close();
         } catch (CorruptIndexException ex) {
             Logger.getLogger(ChimeMemoryIndexer.class.getName()).log(Level.SEVERE, null, ex);
         } catch (LockObtainFailedException ex) {
             Logger.getLogger(ChimeMemoryIndexer.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(ChimeMemoryIndexer.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void setConnectionPool(DatabaseConnectionPool pool) {
         dbPool = pool;
     }
 
     public RAMDirectory getRamDirectory() {
         return ramDirectory;
     }
     
     public void close() {
         ramDirectory.close();
     }
 
     public void removeDataInstance(DataInstance instance) throws Exception {
         try {
             ChimeAnalyzer analyzer = new ChimeAnalyzer();
 
             IndexWriter writer = new IndexWriter(ramDirectory, analyzer);
            Term term = new Term("id", instance.getId().getValue());
 
             writer.deleteDocuments(term);
 
             writer.optimize();
             writer.close();
         } catch (Exception e) {
             Logger.getLogger(ChimeMemoryIndexer.class.getName()).log(Level.SEVERE, null, e);
         }
     }
 
     public void addDataInstance(DataInstance instance) throws Exception {
         DatabaseConnection dbconn = dbPool.borrowInstance(this);
         try {
             addDataInstance(instance, dbconn);
         } catch (Exception e) {
             Logger.getLogger(ChimeMemoryIndexer.class.getName()).log(Level.SEVERE, null, e);
         }
 
         dbPool.returnInstance(dbconn, this);
     }
 
     private void addDataInstance(DataInstance instance, DatabaseConnection database) throws Exception {
 
         User indexingUser = new User();
         indexingUser.setId(User.SYSTEM);
         indexingUser.setIndexing(true);
 
         Document doc = IndexerBase.createDocument(instance, indexingUser, database);
 
         try {
             ChimeAnalyzer analyzer = new ChimeAnalyzer();
 
             IndexWriter writer = new IndexWriter(ramDirectory, analyzer);
             Term term = new Term("instanceid", instance.getId().getValue());
 
             writer.deleteDocuments(term);
             writer.addDocument(doc);
 
             writer.optimize();
             writer.close();
         } catch (Exception e) {
             Logger.getLogger(ChimeMemoryIndexer.class.getName()).log(Level.SEVERE, null, e);
         }
     }
 
 }
