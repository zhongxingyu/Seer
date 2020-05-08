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
 package org.sonar.duplications;
 
 import org.sonar.duplications.algorithm.CloneReporterAlgorithm;
 import org.sonar.duplications.block.Block;
 import org.sonar.duplications.block.BlockChunker;
 import org.sonar.duplications.block.FileBlockGroup;
 import org.sonar.duplications.index.CloneGroup;
 import org.sonar.duplications.index.CloneIndex;
 import org.sonar.duplications.statement.Statement;
 import org.sonar.duplications.statement.StatementChunker;
 import org.sonar.duplications.token.TokenChunker;
 import org.sonar.duplications.token.TokenQueue;
 
 import java.io.File;
 import java.util.List;
 
 public final class CloneFinder {
 
   private TokenChunker tokenChunker;
   private StatementChunker stmtChunker;
   private BlockChunker blockChunker;
   private CloneIndex cloneIndex;
 
   private CloneReporterAlgorithm cloneReporter;
 
   private CloneFinder(Builder builder) {
     this.tokenChunker = builder.tokenChunker;
     this.stmtChunker = builder.stmtChunker;
     this.blockChunker = builder.blockChunker;
     this.cloneIndex = builder.cloneIndex;
     cloneReporter = builder.cloneReporter;
   }
 
   public static Builder build() {
     return new Builder();
   }
 
   public static final class Builder {
 
     private TokenChunker tokenChunker;
     private StatementChunker stmtChunker;
     private BlockChunker blockChunker;
     private CloneIndex cloneIndex;
     private CloneReporterAlgorithm cloneReporter;
 
     public Builder setTokenChunker(TokenChunker tokenChunker) {
       this.tokenChunker = tokenChunker;
       return this;
     }
 
     public Builder setStatementChunker(StatementChunker stmtChunker) {
       this.stmtChunker = stmtChunker;
       return this;
     }
 
     public Builder setBlockChunker(BlockChunker blockChunker) {
       this.blockChunker = blockChunker;
       return this;
     }
 
     public Builder setCloneIndex(CloneIndex cloneIndex) {
       this.cloneIndex = cloneIndex;
       return this;
     }
 
     public Builder setCloneReporter(CloneReporterAlgorithm cloneReporter) {
       this.cloneReporter = cloneReporter;
       return this;
     }
 
     public CloneFinder build() {
       return new CloneFinder(this);
     }
 
   }
 
   public void register(FileBlockGroup fileBlockGroup) {
     for (Block block : fileBlockGroup.getBlockList()) {
       cloneIndex.insert(block);
     }
   }
 
   public void register(File sourceFile) {
     FileBlockGroup blockGroup = tokenize(sourceFile);
     register(blockGroup);
   }
 
   public FileBlockGroup tokenize(File sourceFile) {
     List<Block> blocks;
     String absolutePath = sourceFile.getAbsolutePath();
     try {
       TokenQueue tokenQueue = tokenChunker.chunk(sourceFile);
       List<Statement> statements = stmtChunker.chunk(tokenQueue);
       blocks = blockChunker.chunk(absolutePath, statements);
     } catch (Exception e) {
       throw new DuplicationsException("Exception during registering file: " + absolutePath, e);
     }
    return FileBlockGroup.create(absolutePath, blocks);
   }
 
   public void printCloneReporterStatistics() {
     cloneReporter.printStatistics();
   }
 
   public List<CloneGroup> findClones(FileBlockGroup fileBlockGroup) {
     //build on the fly
     if (!cloneIndex.containsResourceId(fileBlockGroup.getResourceId())) {
       register(fileBlockGroup);
     }
 
     return cloneReporter.reportClones(fileBlockGroup);
   }
 
 }
