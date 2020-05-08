 package com.williamvanderhoef.gilt.responses;
 
 import java.io.Serializable;
 import java.util.List;
 
 import com.williamvanderhoef.gilt.model.Sale;
 
 public class SalesResponse implements GetResponseBase, Serializable{
	
 	/** */
 	private static final long serialVersionUID = -5055078478329048109L;
	
	
	public List<Sale> sales;
 
 	/**
 	 * @return the sales
 	 */
 	public List<Sale> getSales() {
 		return sales;
 	}
 
 	/**
 	 * @param sales the sales to set
 	 */
 	public void setSales(List<Sale> sales) {
 		this.sales = sales;
 	}
	
	
 
 }
