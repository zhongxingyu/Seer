 package com.redhat.qe.sm.tasks;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class Pool {
 	public Date startDate;
 	public Date endDate;
 	public Boolean activeSubscription;
 	public Integer consumed;
 	public Integer quantity;
 	public String poolId;
 	public String poolName;
 	
 	public ArrayList<ProductID> associatedProductIDs;
 	
 	private Date parseDateString(String dateString) throws ParseException{
 		//DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 		DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
 		return df.parse(dateString);
 	}
 	
 	public Boolean isConsumed(){
 		return (consumed > 0);
 	}
 	
 	public Boolean isExpired(){
 		return endDate.after(new Date());
 	}
 	
 	public void addProductID(String productID){
 		associatedProductIDs.add(new ProductID(productID, this));
 	}
 	
 	@Override
 	public boolean equals(Object obj){
 		return ((Pool)obj).poolName.contains(this.poolName);
 	}
 	
 	public Pool(String subscriptionLine) throws ParseException{
 		String[] components = subscriptionLine.split("\\t");
 		
 		poolName = components[0].trim();
 		endDate = this.parseDateString(components[1].trim());
 		poolId = components[2].trim();
 		quantity = Integer.parseInt(components[3].trim());
 		associatedProductIDs = new ArrayList<ProductID>();
 	}
 	
 	public Pool(Date startDate,
 			Date endDate,
 			Boolean activeSubscription,
 			Integer consumed,
 			Integer quantity,
 			String id,
 			String productId){
 		this.startDate = startDate;
 		this.endDate = endDate;
 		this.activeSubscription = activeSubscription;
 		this.consumed = consumed;
 		this.quantity = quantity;
 		this.poolId = id;
 		this.poolName = productId;
		associatedProductIDs = new ArrayList<ProductID>();
 	}
 	
 	public Pool(String poolName, String poolId){
 		this.poolName = poolName;
 		this.poolId = poolId;
		associatedProductIDs = new ArrayList<ProductID>();
 	}
 }
