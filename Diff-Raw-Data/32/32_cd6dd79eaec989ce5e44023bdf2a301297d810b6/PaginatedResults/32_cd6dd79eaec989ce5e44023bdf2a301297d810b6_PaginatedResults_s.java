 /**
  * A generic PaginatedResults object to return paginated data from the DB.
  * Users would get one page of results from the DB, and can then request
  * additional pages (nextOffset = offset + limit) if more results are
  * available (totaNumberOfResults > offset + limit). Subclassed by
  * PaginatedQuestions and PaginatedSolutions.
  * 
  * @author Bennett Ng, 05/04/2013
  */
 
 package com.huskysoft.interviewannihilator.util;
 
 public abstract class PaginatedResults {
 	
 	/** total number of objects in the DB matching the request */
 	private int totalNumberOfResults;
 	
 	/** the requested number of objects */
 	private int limit;
 	
 	/** the requested offset */
 	private int offset;
 	
 	public PaginatedResults() {
 		
	};
 	
 	public PaginatedResults(int totalNumberOfResults, 
 	int limit, int offset) {
 		this.totalNumberOfResults = totalNumberOfResults;
 		this.limit = limit;
 		this.offset = offset;
 	}
 
 	public int getTotalNumberOfResults() {
 		return totalNumberOfResults;
 	}
 
 	public int getLimit() {
 		return limit;
 	}
 
 	public int getOffset() {
 		return offset;
 	}
 
 	public void setTotalNumberOfResults(int totalNumberOfResults) {
 		this.totalNumberOfResults = totalNumberOfResults;
 	}
 
 	public void setLimit(int limit) {
 		this.limit = limit;
 	}
 
 	public void setOffset(int offset) {
 		this.offset = offset;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass().equals(obj.getClass())) {
 			return false;
 		}
 		PaginatedResults other = (PaginatedResults) obj;
 		if (limit != other.limit) {
 			return false;
 		}
 		if (offset != other.offset) {
 			return false;
 		}
 		if (totalNumberOfResults != other.totalNumberOfResults) {
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + limit;
 		result = prime * result + offset;
 		result = prime * result + totalNumberOfResults;
 		return result;
 	}
 }
