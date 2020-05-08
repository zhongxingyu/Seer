 package com.attask.descriptiondashboard;
 
 import hudson.model.User;
 import hudson.plugins.git.GitChangeSet;
 import hudson.scm.ChangeLogSet;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * User: Joel Johnson
  * Date: 9/16/12
  * Time: 8:19 PM
  */
 public class Change implements Serializable {
 	private final String revision;
 	private final User author;
 	private final long timestamp;
 	private final String comment;
 	private final Set<String> changedFiles;
	private final String dateFormatted;
 
 	public static Change createChangeFromGitChangeSet(GitChangeSet gitChangeSet) {
 		String revision = gitChangeSet.getId();
 		User author = gitChangeSet.getAuthor();
 		String comment = gitChangeSet.getComment();
 		long timestamp = gitChangeSet.getTimestamp();
		String dateFormatted = gitChangeSet.getDate();
 		Collection<String> affectedPaths = gitChangeSet.getAffectedPaths();
 
		return new Change(revision, author, comment, timestamp, dateFormatted, affectedPaths);
 	}
 
 	public static Change createChangeFromChangeSet(ChangeLogSet.Entry changeSet) {
 		String revision = changeSet.getCommitId();
 		User author = changeSet.getAuthor();
 		String comment = changeSet.getMsg();
 		long timestamp = changeSet.getTimestamp();
		String dateFormatted = Row.SIMPLE_DATE_FORMAT.format(new Date(timestamp));
 		Collection<String> affectedPaths = changeSet.getAffectedPaths();
 
		return new Change(revision, author, comment, timestamp, dateFormatted, affectedPaths);
 	}
 
	public Change(String revision, User author, String comment, long timestamp, String dateFormatted, Collection<String> changedFiles) {
 		this.comment = comment;
 		this.timestamp = timestamp;
 		this.author = author;
 		this.revision = revision;
 		this.changedFiles = Collections.unmodifiableSet(new TreeSet<String>(changedFiles));
		this.dateFormatted = dateFormatted;
 	}
 
 	public String getRevision() {
 		return revision;
 	}
 
 	public User getAuthor() {
 		return author;
 	}
 
 	public long getTimestamp() {
 		return timestamp;
 	}
 
 	public String findTimestampFormatted() {
		return dateFormatted;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public Set<String> getChangedFiles() {
 		return changedFiles;
 	}
 }
