 /* 
 
   The GNU General Public License (GPL)
  
   Version 2, June 1991
 
   Copyright (C) 1989, 1991 Free Software Foundation, Inc.
   59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   Everyone is permitted to copy and distribute verbatim copies of this license
   document, but changing it is not allowed.
   
   Copyright (c) 2012, John Oliver <johno@insightfullogic.com>, Martijn Verburg <martijn.verburg@gmail.com> All rights reserved.
   
   DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  
   This code is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License version 2 only, as
   published by the Free Software Foundation.  Oracle designates this
   particular file as subject to the "Classpath" exception as provided
   by Oracle in the LICENSE file that accompanied this code.
  
   This code is distributed in the hope that it will be useful, but WITHOUT
   ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
   FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
   version 2 for more details (a copy is included in the LICENSE file that
   accompanied this code).
  
   You should have received a copy of the GNU General Public License version
   2 along with this work; if not, write to the Free Software Foundation,
   Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  
  */
 
 package org.ljc.adoptojdk;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The entry point for this patch review utility. Merges patch 
  * files into groupings that match project groups (and/or more 
  * fine grained levels) as requested by the OpenJDK project 
  */
 public class MergePatches {
 
     private final static int DEFAULT_MERGE_PACKAGE_RANGE = 3;
 
     /** 
      * This can be tuned to say how willing the program is to merging patches 
      * from different packages, higher = patches further apart in the
      * package tree will be merged.
      * 
      * TODO Issue #9 This should not be a public variable, this is an issue with 
      * how DirectoryTreeNode uses this value
      */
     public static int mergePackageRange;
     
     private static Path globalRoot;
     
     public static void main(String[] args) throws IOException {
         sanityCheckArguments(args);
         processArguments(args);
         generateShellScript();
     }
 
     private static void generateShellScript() throws IOException {
         PatchVisitor visitor = new PatchVisitor(globalRoot);
         Files.walkFileTree(globalRoot, visitor);
 
         List<List<DirectoryTreeNode>> out = new ArrayList<>();
         final DirectoryTreeNode rootNode = visitor.getRootNode();
 
         // Retrieve groups of diffs until the tree is empty
         while (rootNode.getSize() > 1) {
            List<DirectoryTreeNode> bag = rootNode.getBagsUsingPreOrderTraversal();
 
             int bagSize = findListSize(bag.size());
 
             while (bag.size() > 0) {
                 final int min = Math.min(bag.size(), bagSize);
                 final List<DirectoryTreeNode> subList = bag.subList(0, min);
                 List<DirectoryTreeNode> newBag = new ArrayList<>(subList);
                 bag.removeAll(newBag);
                 out.add(newBag);
             }
         }
 
         printBags(out);
     }
 
     private static void printBags(List<List<DirectoryTreeNode>> out) {
 
         List<String> usedNames = new ArrayList<>();
         for (List<DirectoryTreeNode> bag : out) {
 
             Path lowestCommonAnc = null;
             
             // Find common parent
             for (DirectoryTreeNode file : bag) {
                 if (lowestCommonAnc == null) {
                     lowestCommonAnc = file.location;
                 }
                 while (!file.location.startsWith(lowestCommonAnc)) {
                     lowestCommonAnc = lowestCommonAnc.getParent();
                 }
             }
 
             String patchName = globalRoot.relativize(lowestCommonAnc).toString().replace("/", "_") + ".patch";
 
             int i = 1;
             while (usedNames.contains(patchName)) {
                 patchName = globalRoot.relativize(lowestCommonAnc).toString().replace("/", "_") + "_" + i + ".patch";
                 i++;
             }
             usedNames.add(patchName);
 
             System.out.println("cat \\");
             for (DirectoryTreeNode file : bag) {
                 System.out.println(globalRoot.relativize(file.location) + " \\");
             }
             System.out.println(" > diffs/" + patchName + "\n\n");
         }
 
     }
 
     private static int findListSize(int n) {
         if (n < 10) {
             return n;
         }
 
         int min = 5;
         for (int i = 5; i < 11; i++) {
             if (n % i == 0) {
                 return i;
             }
 
             if (n % min < n % i) {
                 min = i;
             }
         }
         return min;
     }
 
     private static void sanityCheckArguments(String[] args) {
         if (args == null || args.length < 1) {
             System.out.println("Please pass in the mandatory argument. That is, "
                     + "the root of the path that contains the patches you want merged. "
                     + "This is typically $ADOPT_OPENJDK/reviewed.");
             System.exit(-1);
         }
         else if (args.length > 2) {
             System.out.println("Please pass in a max of two arguments.");
             System.exit(-1);
         }
     }
 
     private static void processArguments(String[] args) {
         globalRoot = Paths.get(args[0]);
         if (args.length == 2) {
             mergePackageRange = Integer.parseInt(args[1]);
         } else {
             mergePackageRange = DEFAULT_MERGE_PACKAGE_RANGE;
         }
         
     }
 }
