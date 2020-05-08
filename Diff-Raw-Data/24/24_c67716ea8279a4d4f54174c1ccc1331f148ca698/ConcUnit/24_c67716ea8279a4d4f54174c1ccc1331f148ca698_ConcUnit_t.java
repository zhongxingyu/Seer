 package net.bioclipse.brunn.pojos;
 
 public enum ConcUnit {
 	
 	UNIT("u"), 
 	MICRO_MOLAR("\u03bcM"), 
	MICRO_GRAM_PER_MILLI_LITER("\u03bcg/ml"),
	PERCENT("%");
 	
 	private String type;
 	
 	private ConcUnit(String type) {
 		this.type=type;
 	}
 	
 	public String toString() {
		if ( "u".equals(type) ) {
			return "U";
		}
 		return type;
 	}

	public static ConcUnit[] all() {
		return new ConcUnit[] { UNIT, 
				                MICRO_MOLAR,
				                MICRO_GRAM_PER_MILLI_LITER,
				                PERCENT };
	}
 }
