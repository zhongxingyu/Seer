 package com.soundbite.gcharts;
 
import org.testng.util.Strings;
 
 /**
  * A DataTable column.
  * @author austin
  *
  */
 class Column {
 	private final ColumnType type;
 	private final String label;
 	
 	public Column(ColumnType type) {
 		this.type = type;
 		this.label = "";
 	}
 	
 	public Column(ColumnType type, String label) {
 		this.type = type;
 		this.label = label;
 	}
 
 	public ColumnType getType() {
 		return type;
 	}
 
 	public String getLabel() {
 		return label;
 	}
 
 	public boolean hasLabel() {
 		return !Strings.isNullOrEmpty(label);
 	}
 
 	
 }
