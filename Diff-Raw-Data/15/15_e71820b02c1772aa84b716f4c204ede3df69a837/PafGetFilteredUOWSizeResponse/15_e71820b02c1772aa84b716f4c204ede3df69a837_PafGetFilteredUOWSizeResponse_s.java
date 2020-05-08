 package com.pace.server.comm;
 
 
 public class PafGetFilteredUOWSizeResponse {
	private Integer uowCellCount = null;
 	private String[] emptyDimensions = null;
 
	public void setUowCellCount(Integer uowCellCount) {
 		this.uowCellCount = uowCellCount;
 	}
 
	public Integer getUowCellCount() {
 		return uowCellCount;
 	}
 
 	public void setEmptyDimensions(String[] emptyDimensions) {
 		this.emptyDimensions = emptyDimensions;
 	}
 
 	public String[] getEmptyDimensions() {
 		return emptyDimensions;
 	}
 	
 }
