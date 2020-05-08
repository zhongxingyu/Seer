 package com.ganesha.basicweb.model;
 
 import java.io.Serializable;
 
 public class Pagination implements Serializable {
 
 	private static final long serialVersionUID = 3191855158411865065L;
 
 	private Integer pageNumber;
 	private Integer rowsPerPage;
 	private Integer rowCount;
 	private int[] availableRowsPerPage = new int[] { 2, 5, 10, 20, 50, 100 };
 
	public Pagination() {
	}

	public Pagination(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}

 	public int[] getAvailableRowsPerPage() {
 		return availableRowsPerPage;
 	}
 
 	public Integer getPageNumber() {
 		return pageNumber;
 	}
 
 	public Integer getRowCount() {
 		return rowCount;
 	}
 
 	public Integer getRowsPerPage() {
 		return rowsPerPage;
 	}
 
 	public int getTotalPage() {
 		return (int) Math.ceil((double) rowCount / (double) rowsPerPage);
 	}
 
 	public void setPageNumber(Integer pageNumber) {
 		this.pageNumber = pageNumber;
 	}
 
 	public void setRowCount(Integer rowCount) {
 		this.rowCount = rowCount;
 	}
 
 	public void setRowsPerPage(Integer rowsPerPage) {
 		this.rowsPerPage = rowsPerPage;
 	}
}
