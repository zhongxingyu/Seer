 /*
 * [The "New BSD" license]
 * Copyright (c) 2012 The Board of Trustees of The University of Alabama
 * All rights reserved.
 *
 * See LICENSE for details.
 */
 package edu.ua.eng.software.clonelink;
 
 /**
 * @author      Nicholas A. Kraft <nkraft@cs.ua.edu>
 * @author      Colin C. Hemphill <colin@hemphill.us>
 * @author      Paige A. Rodeghero <parodeghero@bsu.edu>
 * @author      Blake Bassett <rbbassett@crimson.ua.edu>
 * @author      Casey Ferris <cmferris1@crimson.ua.edu>
 */
 public class CloneLink {
 
 	public static void main (String [] args) throws Exception {
             
         //Git Repo testing
         System.out.println("Setting up Git Repo.");
         Repo git = new GitRepo();
         git.walk();
 
         //SVN Repo testing
         System.out.println("Setting up SVN Repo.");
         Repo svn = new SVNRepo();
         svn.walk();
 
         CommitData gitCD = git.getCommitData();
         CommitData svnCD = svn.getCommitData();
 
         Commit svnTest = svnCD.getCommits().get(10);
         Commit gitTest = gitCD.getCommits().get(10);
 
        for(String file : gitTest.getFilesChanged()) {
             System.out.println(file);
             System.out.printf("Git:: changes: %d, bugFixes: %d\n", gitCD.numChanges(file), gitCD.numBugChanges(file));
             System.out.printf("SVN:: changes: %d, bugFixes: %d\n", svnCD.numChanges(file), svnCD.numBugChanges(file));
         }
 	}
 }
