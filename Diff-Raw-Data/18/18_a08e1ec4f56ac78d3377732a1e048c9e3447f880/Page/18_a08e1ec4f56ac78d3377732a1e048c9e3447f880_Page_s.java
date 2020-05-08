 package com.tobeface.modules.domain;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * 
  * @author loudyn
  * 
  * @param <T>
  */
 public class Page<T> implements Serializable {
 	private static final long serialVersionUID = 1L;
 	public static final List<String> ORDERS = Arrays.asList(new String[] { "asc", "desc" });
 
 	private int pageNo = 1;
 	private int pageSize = 10;
 
 	private String orderBy;
 	private String order;
 	private long totalCount = -1;
 
 	private List<T> result = Collections.emptyList();
 	private Map<String, Object> params = new HashMap<String, Object>();
 
 	public int getPageNo() {
 		return pageNo;
 	}
 
 	public Page<T> setPageNo(int pageNo) {
 		this.pageNo = pageNo;
 		if (this.pageNo < 1) {
 			this.pageNo = 1;
 		}
 		return this;
 	}
 
 	public int getPageSize() {
 		return pageSize;
 	}
 
 	public Page<T> setPageSize(int pageSize) {
 		this.pageSize = pageSize;
 		return this;
 	}
 
 	public String getOrderBy() {
 		return orderBy;
 	}
 
 	public Page<T> setOrderBy(String orderBy) {
 		this.orderBy = orderBy;
 		return this;
 	}
 
 	public String getOrder() {
 		return order;
 	}
 
 	public Page<T> setOrder(String order) {
 
 		String[] orders = StringUtils.split(StringUtils.lowerCase(order), ',');
 		for (String orderStr : orders) {
 			if (!ORDERS.contains(orderStr.toLowerCase())) {
 				throw new IllegalArgumentException("page's order must be asc or desc");
 			}
 		}
 
 		this.order = StringUtils.lowerCase(order);
 		return this;
 	}
 
 	public boolean isOrderBySetted() {
 		return (StringUtils.isNotBlank(orderBy) && StringUtils.isNotBlank(order));
 	}
 
 	public long getTotalCount() {
 		return totalCount;
 	}
 
 	public Page<T> setTotalCount(long totalCount) {
 		this.totalCount = totalCount;
 		return this;
 	}
 
 	public List<T> getResult() {
 		return result;
 	}
 
 	public Page<T> setResult(List<T> result) {
 		this.result = result;
 		return this;
 	}
 
 	public Map<String, Object> getParams() {
 		return params;
 	}
 
 	public void setParams(Map<String, Object> params) {
 		this.params = params;
 	}
 
 	public long getTotalPages() {
 		if (totalCount < 0) {
 			return -1;
 		}
 
 		long count = totalCount / pageSize;
 		if (totalCount % pageSize > 0) {
 			count++;
 		}
 		return count;
 	}
 
 	public int getFirst() {
 		return ((pageNo - 1) * pageSize) + 1;
 	}
 
	public boolean hasNext() {
 		return (pageNo + 1 <= getTotalPages());
 	}
 
 	public int getNextPage() {
		if (hasNext()) {
 			return pageNo + 1;
 		}
 		return pageNo;
 	}
 
	public boolean hasPre() {
 		return (pageNo - 1 >= 1);
 	}
 
 	public int getPrePage() {
		if (hasPre()) {
 			return pageNo - 1;
 		}
 		return pageNo;
 	}
 }
