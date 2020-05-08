 package com.pace.server.comm;
 
 
 public class PafGetFilteredUOWSizeResponse {
	private Long uowCellCount = null;
 	private String[] emptyDimensions = null;
 
	public void setUowCellCount(Long uowCellCount) {
 		this.uowCellCount = uowCellCount;
 	}
 
	public Long getUowCellCount() {
 		return uowCellCount;
 	}
 
 	public void setEmptyDimensions(String[] emptyDimensions) {
 		this.emptyDimensions = emptyDimensions;
 	}
 
 	public String[] getEmptyDimensions() {
 		return emptyDimensions;
 	}
 	
 }
