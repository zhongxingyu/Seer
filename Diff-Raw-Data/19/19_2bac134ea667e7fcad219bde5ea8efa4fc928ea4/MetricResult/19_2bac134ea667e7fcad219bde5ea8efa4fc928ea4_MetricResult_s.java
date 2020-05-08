 package org.cvsanaly.entities;
 
 import javax.persistence.*;
 
 @Entity
 @Table(name = "metrics")
 @NamedQuery(
 	  name = "getAllMetricResults",
 	  query = "Select metric from MetricResult metric"
 	)
 public class MetricResult {
 
 	@Id
 	private long id;
 
	@Column(name = "file_id")
 	private RepositoryFile file;
 
	@Column(name = "commit_id")
 	private Commit commit;
 
 	@Column(name = "sloc")
 	private int numberOfSourceCodeLines;
 	@Column(name = "loc")
 	private int numberOfLinesOfCode;
 	@Column(name = "ncomment")
 	private int numberOfComments;
 	@Column(name = "lcomment")
 	private int numberOfCommentedLines;
 	@Column(name = "lblank")
 	private int numberOfBlankLines;
 	@Column(name = "nfunctions")
 	private int numberOfFunctions;
 	
 	public long getId() {
 		return id;
 	}
 	
 	public void setId(long id) {
 		this.id = id;
 	}
 	
 	public RepositoryFile getFile() {
 		return file;
 	}
 	
 	public void setFile(RepositoryFile file) {
 		this.file = file;
 	}
 	
 	public Commit getCommit() {
 		return commit;
 	}
 	
 	public void setCommit(Commit commit) {
 		this.commit = commit;
 	}
 	
 	public int getNumberOfSourceCodeLines() {
 		return numberOfSourceCodeLines;
 	}
 	
 	public void setNumberOfSourceCodeLines(int numberOfSourceCodeLines) {
 		this.numberOfSourceCodeLines = numberOfSourceCodeLines;
 	}
 	
 	public int getNumberOfLinesOfCode() {
 		return numberOfLinesOfCode;
 	}
 	
 	public void setNumberOfLinesOfCode(int numberOfLinesOfCode) {
 		this.numberOfLinesOfCode = numberOfLinesOfCode;
 	}
 	
 	public int getNumberOfComments() {
 		return numberOfComments;
 	}
 	
 	public void setNumberOfComments(int numberOfComments) {
 		this.numberOfComments = numberOfComments;
 	}
 	
 	public int getNumberOfCommentedLines() {
 		return numberOfCommentedLines;
 	}
 	
 	public void setNumberOfCommentedLines(int numberOfCommentedLines) {
 		this.numberOfCommentedLines = numberOfCommentedLines;
 	}
 	
 	public int getNumberOfBlankLines() {
 		return numberOfBlankLines;
 	}
 	
 	public void setNumberOfBlankLines(int numberOfBlankLines) {
 		this.numberOfBlankLines = numberOfBlankLines;
 	}
 	
 	public int getNumberOfFunctions() {
 		return numberOfFunctions;
 	}
 	
 	public void setNumberOfFunctions(int numberOfFunctions) {
 		this.numberOfFunctions = numberOfFunctions;
 	}
 	
 }
