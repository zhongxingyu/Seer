 /*
 * [The "New BSD" license]
 * Copyright (c) 2012 The Board of Trustees of The University of Alabama
 * All rights reserved.
 *
 * See LICENSE for details.
 */
 package edu.ua.eng.software.clonelink;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 
public class CloneResults
 {
    public CloneResults() {
        commits = new ArrayList();
        bugChanges = new HashMap();
        changes = new HashMap();
     }
 
     public List<Commit> getCommits() {
         return commits;
     }
 
     public int numBugChanges(String filePath) {
         return lookup(bugChanges, filePath);
     }
 
     public int numChanges(String filePath) {
         return lookup(changes, filePath);
     }
 
     public void add(Commit commit) {
         commits.add(commit);
 
         for(String filePath : commit.getFilesChanged()) {
             incMap(changes, filePath);
             if (commit.isBugFix()) {
                 incMap(bugChanges, filePath); 
             }
         }
     }
 
     protected int lookup(Map<String, Integer> map, String key) {
         return (map.containsKey(key)) ? map.get(key) : 0;
     }
 
     protected void incMap(Map<String, Integer> map, String key) {
        map.set(key, lookup(map, key) + 1);
     }
 
     private List<Commit> commits;
     private Map<String, Integer> bugChanges;
     private Map<String, Integer> changes;
 }
