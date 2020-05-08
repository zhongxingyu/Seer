 package org.press.model;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class ShittyModel {
 	private Integer rowId;
 	private String name;
 	private String addy;
 	
 	public ShittyModel(){}
 
 	public ShittyModel(ResultSet rs) throws SQLException {
 		this.rowId = rs.getInt(1);
 		this.name = rs.getString(2);
		this.addy = rs.getString(3);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getAddy() {
 		return addy;
 	}
 
 	public void setAddy(String addy) {
 		this.addy = addy;
 	}
 
 	public Integer getRowId() {
 		return rowId;
 	}
 
 	public void setRowId(Integer rowId) {
 		this.rowId = rowId;
 	}
 
 }
