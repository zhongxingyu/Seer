 /*
     StatCvs - CVS statistics generation 
     Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
     http://statcvs.sf.net/
     
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package net.sf.statcvs.model;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * Represents a CVS Repository and provides access to the {@link CvsFile}s,
  * {@link Directory}s, {@link CvsRevision}s and {@link Author}s recorded
  * in the repository's history. 
  * 
  * TODO: Rename class to Repository, getCurrentLOC to getCurrentLines, getAuthors to getLogins
  * TODO: Change getCommits to SortedSet
  * 
  * @author Manuel Schulze
  * @author Tammo van Lessen
  * @author Richard Cyganiak <richard@cyganiak.de>
  * @version $Id$
  */
 public class CvsContent {
 	private final SortedSet files = new TreeSet();
 	private final SortedSet authors = new TreeSet();
 	private final SortedSet revisions = new TreeSet();
 	private Directory root = null;
 	private Date firstDate = null;
 	private Date lastDate = null;
 	private List commits;
 	private SortedSet symbolicNames = new TreeSet();
 
 	/**
 	 * Adds one file to the repository.
 	 * @param file the file
 	 */
 	public void addFile(CvsFile file) {
 		files.add(file);
 		Iterator it = file.getRevisions().iterator();
 		while (it.hasNext()) {
 			CvsRevision revision = (CvsRevision) it.next();
 			revisions.add(revision);
 			if (revision.getAuthor() != null) {
 				authors.add(revision.getAuthor());
 			}
 			adjustStartAndEndDate(revision.getDate());
             
 		}
 		if (root == null) {
 			initRoot();
 		}
 	}
 
 	/**
 	 * Sets the list of commits. <em>This method exists only because
 	 * of stupid design. This method may only be called by stupid
 	 * designers.</em>
 	 * TODO: Fix this ugly hack!
 	 * @param commits the list of commits
 	 */
 	public void setCommits(List commits) {
 		this.commits = commits;
 	}
 
 	/**
 	 * Returns a <tt>List</tt> of all {@link Commit}s.
 	 * 
 	 * @return all commits
 	 */
 	public List getCommits() {
 		return commits;
 	}
 
 	/**
 	 * Returns the latest {@link java.util.Date} when there
 	 * were changes on the repository.
 	 * 
 	 * @return The latest Date
 	 */
 	public Date getLastDate() {
 		return lastDate;
 	}
 
 	/**
 	 * Returns the first {@link java.util.Date} when there
 	 * were changes on the repository.
 	 * 
 	 * @return The first Date
 	 */
 	public Date getFirstDate() {
 		return firstDate;
 	}
 
 	/**
 	 * returns the current line count of the repository
 	 * @return the current line count of the repository
 	 */
 	public int getCurrentLOC() {
 		int result = 0;
 		Iterator it = files.iterator();
 		while (it.hasNext()) {
 			CvsFile file = (CvsFile) it.next();
 			result += file.getCurrentLinesOfCode();
 		}
 		return result;
 	}
 
 	/**
 	 * Returns a list of all {@link CvsFile}s, ordered by full name
 	 * @return a list of all {@link CvsFile}s
 	 */
 	public SortedSet getFiles() {
 		return files;
 	}
 
 	/**
 	 * Returns <tt>true</tt> if the repository contains no files.
 	 * @return <tt>true</tt> if the repository is empty
 	 */
 	public boolean isEmpty() {
 		return (files.isEmpty());
 	}
 
 	/**
 	 * Returns a <tt>SortedSet</tt> of {@link CvsRevision}s
 	 * in the repository, sorted from oldest to most recent.
 	 * 
 	 * @return all revisions in the repository.
 	 */
 	public SortedSet getRevisions() {
 		return revisions;
 	}
 
 	/**
 	 * Returns a <tt>SortedSet</tt> of all {@link Directory} objects
 	 * in the repository, ordered in tree order
 	 * @return a collection of <tt>Directory</tt> objects
 	 */
 	public SortedSet getDirectories() {
 		return getRoot().getSubdirectoriesRecursive();
 	}
 
 	/**
 	 * Returns the repository's root directory, or <tt>null</tt> if the
 	 * directory contains no files.
 	 * @return the root directory
 	 */
 	public Directory getRoot() {
 		return root;
 	}
 	
 	/**
	 * Sets the list of symbolic names contained in this CvsContent.
	 * @param symbolicNames
	 */
	public void setSymbolicNames(SortedSet symbolicNames)
	{
		this.symbolicNames = symbolicNames;
	}
	
	/**
 	 * Returns a list of {@link SymbolicName}s,
 	 * ordered from latest to oldest. 
 	 */
 	public SortedSet getSymbolicNames()
 	{
 		return symbolicNames;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public String toString() {
 		String result = "";
 		Iterator it = files.iterator();
 		CvsFile cf = null;
 		while (it.hasNext()) {
 			cf = (CvsFile) it.next();
 			result += cf.toString() + "\n";
 		}
 		return result;
 	}
 
 	/**
 	 * Returns a <tt>SortedSet</tt> of all {@link Author}s who have
 	 * committed to the repository, sorted by name.
 	 * @return a <tt>SortedSet</tt> of <tt>Author</tt>s
 	 */
 	public SortedSet getAuthors() {
 		return authors;
 	}
 
 	private void initRoot() {
 		if (files.isEmpty()) {
 			return;
 		}
 		CvsFile file = (CvsFile) files.first();
 		Directory dir = file.getDirectory();
 		while (!dir.isRoot()) {
 			dir = dir.getParent();
 		}
 		root = dir;
 	}
 
 	private void adjustStartAndEndDate(Date revisionDate) {
 		if (revisionDate == null) {
 			return;
 		}
 		if (firstDate == null || firstDate.compareTo(revisionDate) > 0) {
 			firstDate = revisionDate;
 		}
 		if (lastDate == null || lastDate.compareTo(revisionDate) < 0) {
 			lastDate = revisionDate;
 		}
 	}
 }
