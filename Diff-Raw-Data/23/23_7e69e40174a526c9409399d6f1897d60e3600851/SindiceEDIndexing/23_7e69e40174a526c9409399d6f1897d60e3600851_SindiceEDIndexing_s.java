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
  * Index the Entity-Document format of the Sindice-2011 dataset.
  * An entity consists in the triples in entityIDyy/docID{*}/{*}.nt
  */
 public class SindiceEDIndexing extends Indexing {
 
   /* the current entity */
   private final Entity entity = new Entity();
   private final long MAX_ENTITY_SIZE = 64L*1048576L; // 64MB
   
   /**
    * @param inputDir
    * @param indexDir
    * @throws IOException
    */
   public SindiceEDIndexing(File inputDir, Directory dir) throws IOException {
     super(inputDir, dir);
   }
 
   @Override
   protected String getPattern() {
    return "ED-[0-9]+.tar.gz";
   }
 
   @Override
   public Entity next() {
     final String entityID = tarEntry.getName().substring(0, tarEntry.getName().indexOf('/') + 1);
     long entityByteSize = 0;
     
     entity.clear();
     try {
       do {
         /*
          * metadata
          */
         if (entity.sbMetadata.length() == 0) {
           Utils.getFile(reader, tarEntry.getSize(), entity.sbMetadata);
           final int newLine = entity.sbMetadata.indexOf("\n");
           entity.context = entity.sbMetadata.substring(0, newLine);
           entity.subject = entity.sbMetadata.substring(newLine + 1);          
         } else // the metadata has already been read.
           reader.skip(tarEntry.getSize());
         /*
          * outgoing-triples.nt
          */
         if (!hasNext()) {
           logger.info("Error while Trying to get the outgoing-triples.nt from {}, entry name: {}",
             input[inputPos].getAbsolutePath(), tarEntry.getName());
           throw new IllegalStateException("entry file missing");
         }
         entityByteSize += tarEntry.getSize();
         Utils.getFile(reader, tarEntry.getSize(), entity.sb);
         // Strip outgoing triples from rdf:type statements
         Utils.sortAndFlattenNTriples(entity.sb, entity.outTuples, entity.type, true);
         /*
          * incoming-triples.nt
          */
         if (!hasNext()) {
           logger.info("Error while Trying to get the incoming-triples.nt from {}, entry name: {}",
             input[inputPos].getAbsolutePath(), tarEntry.getName());
           throw new IllegalStateException("entry file missing");
         }
         entityByteSize += tarEntry.getSize();
         if (entityByteSize > MAX_ENTITY_SIZE) {
           // Too big entity: just keep outgoing-triples, as they are the most informative ones.
           reader.skip(tarEntry.getSize());
           entity.inTuples.clear();
         } else {
           Utils.getFile(reader, tarEntry.getSize(), entity.sb);
           Utils.sortAndFlattenNTriples(entity.sb, entity.inTuples, null, false);
         }
       } while (hasNext(entityID)); // while documents describe the same entity
     } catch (IOException e) {
       logger.info("Couldn't read a compressed file from {}, entry name: ",
         input[inputPos].getAbsolutePath(), tarEntry.getName());
     }
     return entity;
   }
 
 }
