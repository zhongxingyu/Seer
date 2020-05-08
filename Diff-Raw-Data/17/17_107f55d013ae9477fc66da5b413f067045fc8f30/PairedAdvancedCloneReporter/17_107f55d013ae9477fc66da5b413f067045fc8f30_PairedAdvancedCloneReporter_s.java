 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2008-2011 SonarSource
  * Written (W) 2011 Andrew Tereskin
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
 package org.sonar.duplications.algorithm;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import org.sonar.duplications.block.Block;
 import org.sonar.duplications.block.FileBlockGroup;
 import org.sonar.duplications.index.CloneGroup;
 import org.sonar.duplications.index.CloneIndex;
 import org.sonar.duplications.index.ClonePair;
 import org.sonar.duplications.index.ClonePart;
 
 import java.util.*;
 
 public class PairedAdvancedCloneReporter implements CloneReporterAlgorithm {
 
   private static final ClonePairFilter INTERVAL_FILTER = new IntervalTreeClonePairFilter();
  private static final ClonePairFilter SIMPLE_FILTER = new BruteforceClonePairFilter();
 
   public static final String ALGORITHM_KEY = "algorithm";
   public static final String INIT_KEY = "init";
   public static final String DUPLIACATES_KEY = "duplicates";
   public static final String FILTER_KEY = "filter";
   public static final String GROUPS_KEY = "groups";
 
   public static final Comparator<ClonePair> CLONEPAIR_COMPARATOR = new Comparator<ClonePair>() {
     public int compare(ClonePair o1, ClonePair o2) {
       return o1.getOriginPart().getUnitStart() - o2.getOriginPart().getUnitStart();
     }
   };
 
   private final CloneIndex cloneIndex;
   private final StatsCollector statsCollector;
 
   public PairedAdvancedCloneReporter(CloneIndex cloneIndex) {
     this.cloneIndex = cloneIndex;
     statsCollector = new StatsCollector("PairedAdvanced");
   }
 
   public void printStatistics() {
     statsCollector.printAllStatistics();
   }
 
   public List<CloneGroup> reportClones(FileBlockGroup fileBlockGroup) {
     statsCollector.startTime(INIT_KEY);
     ArrayList<ClonePair> clones = Lists.newArrayList();
 
     List<Block> resourceBlocks = fileBlockGroup.getBlockList();
 
     ArrayList<List<Block>> sameHashBlockGroups = Lists.newArrayList();
 
     for (Block block : fileBlockGroup.getBlockList()) {
       List<Block> sameHashBlockGroup = Lists.newArrayList();
       Collection<Block> foundBlocks = cloneIndex.getBySequenceHash(block.getBlockHash());
       statsCollector.addNumber("total found blocks", foundBlocks.size());
       for (Block shBlock : foundBlocks) {
         if (!shBlock.getResourceId().equals(block.getResourceId()) ||
             shBlock.getIndexInFile() > block.getIndexInFile()) {
           sameHashBlockGroup.add(shBlock);
         }
       }
       sameHashBlockGroups.add(sameHashBlockGroup);
     }
 
     //an empty list is needed a the end to report clone at the end of file
     sameHashBlockGroups.add(new ArrayList<Block>());
     Map<CloneKey, ClonePair> prevActiveMap = Maps.newTreeMap();
     statsCollector.stopTime(INIT_KEY);
 
     statsCollector.startTime(ALGORITHM_KEY);
     Iterator<Block> blockIterator = resourceBlocks.iterator();
     for (List<Block> blockGroup : sameHashBlockGroups) {
       Map<CloneKey, ClonePair> nextActiveMap = Maps.newTreeMap();
 
       Block origBlock = null;
       if (blockIterator.hasNext()) {
         origBlock = blockIterator.next();
       }
 
       for (Block block : blockGroup) {
         processBlock(prevActiveMap, nextActiveMap, origBlock, block);
       }
       clones.addAll(prevActiveMap.values());
 
       prevActiveMap = nextActiveMap;
     }
     statsCollector.stopTime(ALGORITHM_KEY);
 
     statsCollector.addNumber("clones", clones.size());
 
     statsCollector.startTime(DUPLIACATES_KEY);
     List<ClonePair> filtered = removeDuplicates(clones);
     statsCollector.stopTime(DUPLIACATES_KEY);
 
     statsCollector.addNumber("removed duplicates", clones.size() - filtered.size());
 
     int sizeBefore = filtered.size();
     statsCollector.startTime(FILTER_KEY);
     filtered = INTERVAL_FILTER.filter(filtered);
     statsCollector.stopTime(FILTER_KEY);
 
     statsCollector.addNumber("removed covered", sizeBefore - filtered.size());
 
     statsCollector.startTime(GROUPS_KEY);
     List<CloneGroup> groups = groupClonePairs(filtered);
     statsCollector.stopTime(GROUPS_KEY);
 
     statsCollector.addNumber("total clone groups", groups.size());
     return groups;
   }
 
   private List<ClonePair> removeDuplicates(List<ClonePair> clones) {
     HashSet<ClonePair> set = Sets.newHashSet(clones);
     return Lists.newArrayList(set);
   }
 
   /**
    * processes curren block - checks if current block continues one of block sequences
    * or creates new block sequence. sequences (<tt>TempClone</tt>) are put to
    * <tt>nextActiveMap</tt>
    *
    * @param prevActiveMap, map with active block sequences from previous cycle iteration
    * @param nextActiveMap, map with active block sequences after current cycle iteration
    * @param origBlock,     block of original file
    * @param anotherBlock,  one of blocks with same hash as <tt>origBlock</tt>
    */
   private void processBlock(Map<CloneKey, ClonePair> prevActiveMap,
                             Map<CloneKey, ClonePair> nextActiveMap,
                             Block origBlock, Block anotherBlock) {
     ClonePart origPart = new ClonePart(origBlock);
     ClonePart anotherPart = new ClonePart(anotherBlock);
     int cloneLength = 0;
 
     CloneKey curKey = new CloneKey(anotherBlock.getResourceId(), anotherBlock.getIndexInFile());
     if (prevActiveMap.containsKey(curKey)) {
       ClonePair prevClonePair = prevActiveMap.get(curKey);
 
       ClonePart prevOrigPart = prevClonePair.getOriginPart();
       ClonePart prevAnotherPart = prevClonePair.getAnotherPart();
 
       origPart.setLineStart(prevOrigPart.getLineStart());
       origPart.setUnitStart(prevOrigPart.getUnitStart());
 
       anotherPart.setLineStart(prevAnotherPart.getLineStart());
       anotherPart.setUnitStart(prevAnotherPart.getUnitStart());
 
       cloneLength = prevClonePair.getCloneLength();
 
       prevActiveMap.remove(curKey);
     }
 
     ClonePair tempClone = new ClonePair(origPart, anotherPart, cloneLength + 1);
 
     CloneKey nextKey = new CloneKey(anotherBlock.getResourceId(), anotherBlock.getIndexInFile() + 1);
     nextActiveMap.put(nextKey, tempClone);
   }
 
 
   /**
    * @param clones, array of TempClone sorted by getOriginPart().getUnitStart()
    * @return list of reported clones
    */
   private List<CloneGroup> groupClonePairs(List<ClonePair> clones) {
     List<CloneGroup> res = Lists.newArrayList();
     //sort elements of prevActiveMap by getOriginPart.getUnitStart()
     Collections.sort(clones, CLONEPAIR_COMPARATOR);
 
     CloneGroup curClone = null;
     int prevUnitStart = -1;
     int prevLength = -1;
     for (ClonePair clonePair : clones) {
       int curUnitStart = clonePair.getOriginPart().getUnitStart();
       int curLength = clonePair.getCloneLength();
       //if current sequence matches with different sequence in original file
       if (curUnitStart != prevUnitStart || prevLength != curLength) {
         prevLength = curLength;
 
         curClone = new CloneGroup(clonePair.getCloneLength());
         curClone.setOriginPart(clonePair.getOriginPart());
         curClone.addPart(clonePair.getOriginPart());
         curClone.addPart(clonePair.getAnotherPart());
 
         res.add(curClone);
       } else {
         curClone.addPart(clonePair.getAnotherPart());
       }
       prevUnitStart = curUnitStart;
     }
     return res;
   }
 
 
 }
