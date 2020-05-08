 package com.bk.util;
 
import java.util.Collections;
 import java.util.List;
 
 /**
  * @author Andrei Petraru
  * Mar 24, 2013
  */
 public class PaginatedHibernateSearch<AbstractEntity> {
 	private List<AbstractEntity> results;
 
 	private int resultsSize;
 
 	public List<AbstractEntity> getResults() {
		return Collections.unmodifiableList(results);
 	}
 
 	public void setResults(List<AbstractEntity> results) {
 		this.results = results;
 	}
 
 	public int getResultsSize() {
 		return resultsSize;
 	}
 
 	public void setResultsSize(int resultsSize) {
 		this.resultsSize = resultsSize;
 	}
 
 }
