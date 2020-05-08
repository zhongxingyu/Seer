 package net.bioclipse.brunn.pojos;
 
 public enum ConcUnit {
 	
 	UNIT("u"), 
 	MICRO_MOLAR("\u03bcM"), 
	MICRO_GRAM_PER_MILLI_LITER("\u03bcg/ml");
 	
 	private String type;
 	
 	private ConcUnit(String type) {
 		this.type=type;
 	}
 	
 	public String toString() {
 		return type;
 	}
 }
