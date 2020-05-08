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
 package org.sonar.duplications.benchmark;
 
import java.io.File;
import java.util.List;
import java.util.Map;

 import org.sonar.duplications.CloneFinder;
 import org.sonar.duplications.block.FileBlockGroup;
 import org.sonar.duplications.index.CloneGroup;
 import org.sonar.duplications.index.MemoryCloneIndex;
 import org.sonar.duplications.java.JavaCloneFinder;
 
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
 
 public class NewCpdBenchmark extends Benchmark {
 
   private final List<File> files;
   private final int blockSize;
 
   public NewCpdBenchmark(List<File> files, int blockSize) {
     this.files = files;
     this.blockSize = blockSize;
   }
 
   @Override
   public void runRound() throws Exception {
     singleRun(files, blockSize);
   }
 
   public static List<CloneGroup> singleRun(List<File> files, int blockSize) {
     MemoryCloneIndex mci = new MemoryCloneIndex();
     CloneFinder cf = JavaCloneFinder.build(mci, blockSize);
    Map<File, FileBlockGroup> blocksByFile = Maps.newHashMap();
     for (File file : files) {
       FileBlockGroup fileBlockGroup = cf.tokenize(file);
      blocksByFile.put(file, fileBlockGroup);
       cf.register(fileBlockGroup);
    }
    List<CloneGroup> clones = Lists.newArrayList();
    for (File file : files) {
      clones.addAll(cf.findClones(blocksByFile.get(file)));
     }
     return clones;
   }
 
 }
