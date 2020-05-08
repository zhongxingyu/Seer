 package org.jskat.extract;
 
 public enum ExtractConfiguration {
 
 	DONDORF_FRENCH(
 			"src/main/resources/org/jskat/samples/fixed/dondorf_french.svg",
			1027, 615, 2, 5, 13, "A23456789TJQK", "CDHSB"),
 
 	ISS_GERMAN("src/main/resources/org/jskat/samples/fixed/iss_german.svg",
 			1027, 615, 2, 5, 13, "A23456789TJQK", "CDHSB"),
 
 	ISS_FRENCH("src/main/resources/org/jskat/samples/fixed/iss_french.svg",
 			1027, 615, 2, 5, 13, "A23456789TJQK", "CDHSB"),
 
 	ISS_TOURNAMENT(
 			"src/main/resources/org/jskat/samples/fixed/iss_tournament.svg",
 			1027, 615, 2, 5, 13, "A23456789TJQK", "CDHSB"),
 
 	ORNAMENTAL_FRENCH(
 			"src/main/resources/org/jskat/samples/fixed/ornamental_french.svg",
 			1807, 945, 2, 5, 13, "A23456789TJQK", "CDHSB"),
 
 	TANGO_FRENCH("src/main/resources/org/jskat/samples/fixed/tango_french.svg",
 			1885, 975, 1, 5, 13, "A23456789TJQK", "CDHSB"),
 
 	WILLIAM_TELL(
 			"src/main/resources/org/jskat/samples/fixed/william-tell_german.svgz",
 			1664, 1005, 1, 5, 13, "AKQJT98765432", "HDSCB"),
 
 	XSKAT_GERMAN("src/main/resources/org/jskat/samples/fixed/xskat_german.svg",
 			1872, 1120, 1, 5, 13, "AKQJT98765432", "HDSCB");
 
 	public String location = "";
 	public int width = 0;
 	public int height = 0;
 	public float scale = 1;
 	public int rows = 0;
 	public int columns = 0;
 	public String rowSymbols = "";
 	public String columnSymbols = "";
 
 	ExtractConfiguration(String location, int width, int height, float scale,
 			int rows, int columns, String rowSymbols, String columnSymbols) {
 		this.location = location;
 		this.width = width;
 		this.height = height;
 		this.scale = scale;
 		this.rows = rows;
 		this.columns = columns;
 		this.rowSymbols = rowSymbols;
 		this.columnSymbols = columnSymbols;
 	}
 }
