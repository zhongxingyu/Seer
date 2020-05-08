 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2008-2011 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.cpd;
 
 import org.sonar.api.cpd.IndexBlock;
 import org.sonar.api.database.DatabaseSession;
 import org.sonar.duplications.block.Block;
 import org.sonar.duplications.index.CloneIndex;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class DBCloneIndex implements CloneIndex {
 
   private final DatabaseSession session;
 
   public DBCloneIndex(DatabaseSession session) {
     this.session = session;
   }
 
   public Collection<String> getAllUniqueResourceId() {
     String hql = "SELECT d FROM IndexBlock d WHERE index_in_file = 0 GROUP BY resource_id";
     List<IndexBlock> list = session.createQuery(hql).getResultList();
     List<String> resources = new ArrayList<String>();
     for (IndexBlock indexBlock : list) {
       resources.add(indexBlock.getResourceId());
     }
     return resources;
   }
 
   public boolean containsResourceId(String resourceId) {
     String hql = "SELECT d FROM IndexBlock d WHERE resource_id=:resource_id";
     hql += " AND index_in_file = 0";
     List<IndexBlock> list = session.createQuery(hql)
         .setParameter("resource_id", resourceId)
         .getResultList();
     return !list.isEmpty();
   }
 
   public Collection<Block> getByResourceId(String resourceId) {
     String hql = "SELECT d FROM IndexBlock d WHERE resource_id=:resource_id";
    hql += " ORDER_BY index_in_file ASC";
     List<IndexBlock> list = session.createQuery(hql)
         .setParameter("resource_id", resourceId)
         .getResultList();
     List<Block> blocks = new ArrayList<Block>(list.size());
     for (IndexBlock indexBlock : list) {
       Block block = new Block(indexBlock.getResourceId(),
           indexBlock.getBlockHash(), indexBlock.getIndexInFile(),
           indexBlock.getStartLine(), indexBlock.getEndLine());
       blocks.add(block);
     }
     return blocks;
   }
 
   public Collection<Block> getBySequenceHash(String blockHash) {
     String hql = "SELECT d FROM IndexBlock d WHERE block_hash=:block_hash";
     List<IndexBlock> list = session.createQuery(hql)
         .setParameter("block_hash", blockHash)
         .getResultList();
     List<Block> blocks = new ArrayList<Block>(list.size());
     for (IndexBlock indexBlock : list) {
       Block block = new Block(indexBlock.getResourceId(),
           indexBlock.getBlockHash(), indexBlock.getIndexInFile(),
           indexBlock.getStartLine(), indexBlock.getEndLine());
       blocks.add(block);
     }
     return blocks;
   }
 
   public void insert(Block block) {
     IndexBlock indexBlock = new IndexBlock(block.getResourceId(),
         block.getBlockHash(), block.getIndexInFile(),
         block.getFirstLineNumber(), block.getLastLineNumber());
     session.save(indexBlock);
     session.commit();
   }
 
   public void remove(String resourceId) {
     String hql = "DELETE FROM IndexBlock WHERE resource_id=:resource_id";
     session.createQuery(hql)
         .setParameter("resource_id", resourceId)
         .executeUpdate();
     session.commit();
   }
 
   public void remove(Block block) {
     String hql = "DELETE FROM IndexBlock WHERE block_hash=:block_hash";
     hql += " AND resource_id=:resource_id AND index_in_file=:index_in_file";
     session.createQuery(hql)
         .setParameter("block_hash", block.getBlockHash())
         .setParameter("resource_id", block.getResourceId())
         .setParameter("index_in_file", block.getIndexInFile())
         .executeUpdate();
     session.commit();
   }
 
   public void removeAll() {
     session.createQuery("DELETE FROM IndexBlock")
         .executeUpdate();
     session.commit();
   }
 
   public int size() {
     return session.createQuery("SELECT d FROM IndexBlock d")
         .getResultList().size();
   }
 }
