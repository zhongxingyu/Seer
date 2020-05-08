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
 package org.sonar.duplications.index;
 
 import org.sonar.duplications.block.Block;
 import org.sonar.duplications.interval.Interval;
 import org.sonar.duplications.interval.IntervalTree;
 
 import java.util.*;
 
 public class CloneReporter {
 
   private final static class Key implements Comparable<Key> {
     private String resourceId;
     private int unitNum;
 
     private Key(String resourceId, int unitNum) {
       this.resourceId = resourceId;
       this.unitNum = unitNum;
     }
 
     public int compareTo(Key o) {
       if (this.resourceId.equals(o.resourceId)) {
         return this.unitNum - o.unitNum;
       }
       return this.resourceId.compareTo(o.resourceId);
     }
 
     @Override
     public boolean equals(Object object) {
       if (object instanceof Key) {
         Key other = (Key) object;
 
         if (other.resourceId.equals(resourceId) && other.unitNum == unitNum) {
           return true;
         }
       }
       return false;
     }
 
     @Override
     public int hashCode() {
       int h = 0;
       h = 31 * h + resourceId.hashCode();
       h = 31 * h + unitNum;
       return h;
     }
   }
 
   private final static class TempClone {
     private ClonePart origPart;
     private ClonePart anotherPart;
     private int cloneLength;
 
     private TempClone(ClonePart origPart, ClonePart anotherPart, int cloneLength) {
       this.origPart = origPart;
       this.anotherPart = anotherPart;
       this.cloneLength = cloneLength;
     }
 
     public ClonePart getOrigPart() {
       return origPart;
     }
 
     public ClonePart getAnotherPart() {
       return anotherPart;
     }
 
     public int getCloneLength() {
       return cloneLength;
     }
   }
 
   private final static class PartWrapper {
     public Clone clone;
     public ClonePart part;
 
     private PartWrapper(Clone clone, ClonePart part) {
       this.clone = clone;
       this.part = part;
     }
 
     public Clone getClone() {
       return clone;
     }
 
     public ClonePart getPart() {
       return part;
     }
   }
 
   private static class TempCloneComparator implements Comparator<TempClone> {
 
     public int compare(TempClone o1, TempClone o2) {
       return o1.getOrigPart().getUnitStart() - o2.getOrigPart().getUnitStart();
     }
   }
 
   public static List<Clone> reportClones(List<Block> candidateBlocks, CloneIndex index) {
     ArrayList<Clone> clones = new ArrayList<Clone>();
 
     ArrayList<Block> resourceBlocks = new ArrayList<Block>(candidateBlocks);
 
     ArrayList<List<Block>> sameHashBlockGroups = new ArrayList<List<Block>>();
 
     for (Block block : candidateBlocks) {
       List<Block> sameHashBlockGroup = new ArrayList<Block>();
       for (Block shBlock : index.getBySequenceHash(block.getBlockHash())) {
         if (!shBlock.getResourceId().equals(block.getResourceId()) ||
             shBlock.getIndexInFile() > block.getIndexInFile()) {
           sameHashBlockGroup.add(shBlock);
         }
       }
       sameHashBlockGroups.add(sameHashBlockGroup);
     }
 
     //an empty list is needed a the end to report clone at the end of file
     sameHashBlockGroups.add(new ArrayList<Block>());
 
    Map<Key, TempClone> prevActiveMap = new TreeMap<Key, TempClone>();
 
     for (int i = 0; i < sameHashBlockGroups.size(); i++) {
      Map<Key, TempClone> nextActiveMap = new TreeMap<Key, TempClone>();
 
       for (Block block : sameHashBlockGroups.get(i)) {
         Block origBlock = resourceBlocks.get(i);
         processBlock(prevActiveMap, nextActiveMap, origBlock, block);
       }
 
       //sort elements of prevActiveMap by getOrigPart.getUnitStart()
       ArrayList<TempClone> sortedArr = new ArrayList<TempClone>(prevActiveMap.values());
 
       Collections.sort(sortedArr, new TempCloneComparator());
 
       clones.addAll(reportClones(sortedArr));
 
       prevActiveMap = nextActiveMap;
     }
 
     return removeDuplicates(clones);
   }
 
   /**
    * O(n^2) filter for clones fully covered by another clones
    *
    * @param clones original list of clones
    * @return filtered list of clones
    */
   private static List<Clone> filterCovered(List<Clone> clones) {
     List<Clone> filtered = new ArrayList<Clone>();
     for (int i = 0; i < clones.size(); i++) {
       Clone first = clones.get(i);
       boolean covered = false;
       for (int j = 0; j < clones.size(); j++) {
         if (i == j)
           continue;
 
         Clone second = clones.get(j);
         covered |= first.containsIn(second);
         if (covered)
           break;
       }
       if (!covered)
         filtered.add(first);
     }
     return filtered;
   }
 
   /**
    * O(n log n) filter using interval tree for clones fully covered by another clones
    *
    * @param clones original list of clones
    * @return filtered list of clones
    */
   private static List<Clone> filterCoveredIntervalTree(List<Clone> clones) {
     List<Clone> filtered = new ArrayList<Clone>();
     HashMap<String, IntervalTree> trees = new HashMap<String, IntervalTree>();
 
     //populate interval tree structure
    for (Clone clone : clones) {
       for (ClonePart part : clone.getCloneParts()) {
         PartWrapper partWrap = new PartWrapper(clone, part);
         IntervalTree tree = trees.get(part.getResourceId());
         if (tree == null) {
           tree = new IntervalTree();
           trees.put(part.getResourceId(), tree);
         }
         int unitStart = part.getUnitStart();
         int unitEnd = part.getUnitStart() + clone.getCloneUnitLength() - 1;
 
         tree.addInterval(new Interval(unitStart, unitEnd, partWrap));
       }
     }
 
    for (Clone clone : clones) {
       ClonePart originPart = clone.getOriginPart();
       IntervalTree tree = trees.get(originPart.getResourceId());
 
       int unitStart = originPart.getUnitStart();
       int unitEnd = originPart.getUnitStart() + clone.getCloneUnitLength() - 1;
       List<Interval> intervals = tree.getCoveringIntervals(unitStart, unitEnd);
 
       boolean covered = false;
       for (Interval<PartWrapper> interval : intervals) {
         Clone foundClone = interval.getData().getClone();
         if (foundClone.equals(clone))
           continue;
 
         covered |= clone.containsIn(foundClone);
         if (covered)
           break;
       }
 
       if (!covered)
         filtered.add(clone);
     }
     return filtered;
   }
 
   private static List<Clone> removeDuplicates(List<Clone> clones) {
     HashSet<Clone> set = new HashSet<Clone>(clones);
     List<Clone> result = new ArrayList<Clone>(set);
 
     return filterCoveredIntervalTree(result);
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
  private static void processBlock(Map<Key, TempClone> prevActiveMap,
                                   Map<Key, TempClone> nextActiveMap,
                                    Block origBlock, Block anotherBlock) {
     ClonePart origPart = new ClonePart(origBlock);
     ClonePart anotherPart = new ClonePart(anotherBlock);
     int cloneLength = 0;
 
     Key curKey = new Key(anotherBlock.getResourceId(), anotherBlock.getIndexInFile());
     if (prevActiveMap.containsKey(curKey)) {
       TempClone prevTmp = prevActiveMap.get(curKey);
 
       ClonePart prevOrigPart = prevTmp.getOrigPart();
       origPart.setLineStart(prevOrigPart.getLineStart());
       origPart.setUnitStart(prevOrigPart.getUnitStart());
 
       ClonePart prevAnotherPart = prevTmp.getAnotherPart();
       anotherPart.setLineStart(prevAnotherPart.getLineStart());
       anotherPart.setUnitStart(prevAnotherPart.getUnitStart());
 
       cloneLength = prevTmp.getCloneLength();
 
       prevActiveMap.remove(curKey);
     }
 
     TempClone tempClone = new TempClone(origPart, anotherPart, cloneLength + 1);
 
     Key nextKey = new Key(anotherBlock.getResourceId(), anotherBlock.getIndexInFile() + 1);
     nextActiveMap.put(nextKey, tempClone);
   }
 
 
   /**
    * @param sortedArr, array of TempClone sorted by getOrigPart().getUnitStart()
    * @return list of reported clones
    */
   private static List<Clone> reportClones(List<TempClone> sortedArr) {
     List<Clone> res = new ArrayList<Clone>();
     Clone curClone = null;
     int prevUnitStart = -1;
     for (int j = 0; j < sortedArr.size(); j++) {
       TempClone tempClone = sortedArr.get(j);
       int curUnitStart = tempClone.getOrigPart().getUnitStart();
       //if current sequence matches with different sequence in original file
       if (curUnitStart != prevUnitStart) {
         curClone = new Clone(tempClone.getCloneLength());
         curClone.setOriginPart(tempClone.getOrigPart());
         curClone.addPart(tempClone.getOrigPart());
         curClone.addPart(tempClone.getAnotherPart());
         res.add(curClone);
       } else {
         curClone.addPart(tempClone.getAnotherPart());
       }
       prevUnitStart = curUnitStart;
     }
     return res;
   }
 
 }
