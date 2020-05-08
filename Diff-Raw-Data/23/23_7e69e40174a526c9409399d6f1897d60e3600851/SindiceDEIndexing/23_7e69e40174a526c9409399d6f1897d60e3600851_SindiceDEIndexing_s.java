 /**
  * Copyright 2011, Campinas Stephane
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 /**
  * @project trec-entity-tool
  * @author Campinas Stephane [ 3 Jun 2011 ]
  * @link stephane.campinas@deri.org
  */
 package org.sindice.siren.index;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.lucene.store.Directory;
 
 /**
  * Index the Document-Entity format of the Sindice-2011 dataset.
  * An entity consists in the triples in docIdxx/entityIDyy/*.nt
  */
 public class SindiceDEIndexing extends Indexing {
   
   /* the current entity */
   private final Entity entity = new Entity();
   
   /**
    * @param inputDir
    * @param indexDir
    * @throws IOException
    */
   public SindiceDEIndexing(File inputDir, Directory dir) throws IOException {
     super(inputDir, dir);
   }
 
   @Override
   protected String getPattern() {
     return "DE-[0-9]+\\.tar\\.gz";
   }
 
   @Override
   public Entity next() {
     entity.clear();
     try {
       // metadata
       Utils.getFile(reader, tarEntry.getSize(), entity.sbMetadata);
       // outgoing-triples.nt
       if (!hasNext()) {
         logger.info("Error while Trying to get the outgoing-triples.nt from {}, entry name: {}",
           input[inputPos].getAbsolutePath(), tarEntry.getName());
         throw new IllegalStateException("entry file missing");
       }
       Utils.getFile(reader, tarEntry.getSize(), entity.sb);
       // incoming-triples.nt
       if (!hasNext()) {
         logger.info("Error while Trying to get the incoming-triples.nt from {}, entry name: {}",
           input[inputPos].getAbsolutePath(), tarEntry.getName());
         throw new IllegalStateException("entry file missing");
       }
       Utils.getFile(reader, tarEntry.getSize(), entity.sb);
     } catch (IOException e) {
      logger.info("Error while Trying to get the incoming-triples.nt from {}, entry name: {}",
         input[inputPos].getAbsolutePath(), tarEntry.getName());
     }
    // Strip outgoing triples from rdf:type statements
    Utils.sortAndFlattenNTriples(entity.sb, entity.outTuples, entity.type, true);
    Utils.sortAndFlattenNTriples(entity.sb, entity.inTuples, null, false);
    final int newLine = entity.sbMetadata.indexOf("\n");
    entity.context = entity.sbMetadata.substring(0, newLine);
    entity.subject = entity.sbMetadata.substring(newLine + 1);
     return entity;
   }
   
 }
