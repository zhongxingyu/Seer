 package com.tda.persistence.paginator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Paginator {
 	private int resultsPerPage;
 	private int pageIndex = 1;
 	private int totalResultsCount;
 	private Order order;
 	private String orderField;
 
 	public Paginator(int resultsPerPage) {
 		this.resultsPerPage = resultsPerPage;
 	}
 
 	public int getResultsPerPage() {
 		return resultsPerPage;
 	}
 
 	public void setResultsPerPage(int resultsPerPage) {
 		this.resultsPerPage = resultsPerPage;
 	}
 
 	public int getPageIndex() {
 		return pageIndex;
 	}
 
 	public void setPageIndex(int pageIndex) {
 		this.pageIndex = pageIndex;
 	}
 
 	public int getTotalResultsCount() {
 		return totalResultsCount;
 	}
 
 	public void setTotalResultsCount(int totalResultsCount) {
 		this.totalResultsCount = totalResultsCount;
 	}
 
 	public Order getOrder() {
 		return order;
 	}
 
 	public void setOrder(Order order) {
 		this.order = order;
 	}
 
 	public String getOrderField() {
 		return orderField;
 	}
 
 	public void setOrderField(String orderField) {
 		this.orderField = orderField;
 	}
 
 	public int getPageCount() {
 		if (this.totalResultsCount == 0)
 			return 0;
 
 		/* Get how many pages depending on the results */
 		return (int) Math.ceil(this.totalResultsCount
 				/ (double) this.resultsPerPage);
 	}
 
 	public boolean isLastPage() {
 		if (getPageCount() == 1)
 			return true;
 
		return getPageCount() == (getPageIndex() + 1);
 	}
 
 	public boolean isFirstPage() {
 		return 1 == getPageIndex();
 	}
 
 	public List<Integer> getPages() {
 		ArrayList<Integer> pages = new ArrayList<Integer>();
 		for (int i = 0; i < getPageCount(); i++)
 			pages.add(i + 1);
 
 		return pages;
 	}
 
 	public Integer getNextPage() {
 		return pageIndex + 1;
 	}
 
 	public Integer getPreviousPage() {
 		return pageIndex - 1;
 	}
 }
