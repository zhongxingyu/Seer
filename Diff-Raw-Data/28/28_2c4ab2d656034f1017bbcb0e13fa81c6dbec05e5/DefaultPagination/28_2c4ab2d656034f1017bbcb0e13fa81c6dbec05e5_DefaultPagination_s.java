 /**
  * DefaultPagination.java
  */
 package com.xone.model.hibernate.support;
 
 import java.util.ArrayList;
 import java.util.List;
 public class DefaultPagination implements Paginable {
 
 	private static final long serialVersionUID = 1L;
 	public static final int DEF_COUNT = 20;
 
 	protected int totalCount = 0;
 	protected int pageSize = 20;
 	protected int pageNo = 1;
 
 	public DefaultPagination() {
 		super();
 	}
 
 	public DefaultPagination(int pageNo, int pageSize, int totalCount) {
		pageSize = pageSize <= 0 ? 20 : pageSize;
 		if (totalCount <= 0) {
 			this.totalCount = 0;
 		} else {
 			this.totalCount = totalCount;
 		}
 		if (pageSize <= 0) {
 			this.pageSize = DEF_COUNT;
 		} else {
 			this.pageSize = pageSize;
 		}
 		if (pageNo <= 0) {
 			this.pageNo = 1;
 		} else {
 			this.pageNo = pageNo;
 		}
 		if (pageNo > this.getTotalPage()) {
 			this.pageNo = this.getTotalPage();
 		} else if ((this.pageNo - 1) * this.pageSize >= totalCount) {
 			this.pageNo = totalCount / pageSize;
 		}
 	}
 
 	/**
 	 * 调整分页参数，使合理化
 	 */
 	public void adjustPage() {
 		if (totalCount <= 0) {
 			totalCount = 0;
 		}
 		if (pageSize <= 0) {
 			pageSize = DEF_COUNT;
 		}
 		if (pageNo <= 0) {
 			pageNo = 1;
 		}
 		if (pageNo > this.getTotalPage()) {
 			pageNo = this.getTotalPage();
 		} else if ((pageNo - 1) * pageSize >= totalCount) {
 			pageNo = totalCount / pageSize;
 		}
 	}
 
 	public int getPageNo() {
 		return pageNo;
 	}
 
 	public int getPageSize() {
 		return pageSize;
 	}
 
 	public int getTotalCount() {
 		return totalCount;
 	}
 
 	public int getTotalPage() {
 		int totalPage = totalCount / pageSize;
 		if (totalCount % pageSize != 0 || totalPage == 0) {
 			totalPage++;
 		}
 		return totalPage;
 	}
 
 	public boolean isFirstPage() {
 		return pageNo <= 1;
 	}
 
 	public boolean isLastPage() {
 		return pageNo >= getTotalPage();
 	}
 
 	public int getNextPage() {
 		if (isLastPage()) {
 			return pageNo;
 		} else {
 			return pageNo + 1;
 		}
 	}
 
 	public int getPrePage() {
 		if (isFirstPage()) {
 			return pageNo;
 		} else {
 			return pageNo - 1;
 		}
 	}
 
 	public void setTotalCount(int totalCount) {
 		this.totalCount = totalCount;
 	}
 
 	public void setPageSize(int pageSize) {
 		this.pageSize = pageSize;
 	}
 
 	public void setPageNo(int pageNo) {
 		this.pageNo = pageNo;
 	}
 
	/*
	 * (non-Javadoc)
	 * @see net.everelegance.common.page.Paginable#getList()
	 */
 	public List<?> getList() {
 		return new ArrayList<Object>(1);
 	}
 
 }
