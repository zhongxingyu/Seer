 package com.redhat.qe.sm.data;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import com.redhat.qe.tools.abstraction.AbstractCommandLineData;
 
 public class SubscriptionPool extends AbstractCommandLineData {
	protected static String simpleDateFormat = "MM/dd/yyyy";	// 04/24/2011 https://bugzilla.redhat.com/show_bug.cgi?id=695234
 
 	public SubscriptionPool() {
 		super(null);
 	}
 
 
 
 	// abstraction fields
 	public String subscriptionName;
 	public String productId;	// public String productSku;	// productSku was replaced by productId in subscription-manager-0.68-1.el6.i686  jsefler 7/13/2010
 	public String poolId;
 	public String quantity;	// public Integer quantity;	// can be "unlimited"
 	public Calendar endDate;
 	
 	public Calendar startDate;
 	public Boolean activeSubscription;
 	public Integer consumed;
 	public ArrayList<ProductSubscription> associatedProductIDs;
 	
 	
 	
 	public Boolean isConsumed(){
 		return (consumed > 0);
 	}
 	
 	public Boolean isExpired(){
 		return endDate.after(new Date());
 	}
 	
 	public void addProductID(String productID){
 		associatedProductIDs.add(new ProductSubscription(productID, this));
 	}
 	
 	@Override
 	public boolean equals(Object obj){
 		if (obj==null) return false;
 		// assumes productName is unique across all SubscriptionPools
 		// return ((SubscriptionPool)obj).subscriptionName.contains(this.subscriptionName);	// jsefler 7/13/2010: this is not correct  
 
 		// assumes productId is unique across all SubscriptionPools
 		// return ((SubscriptionPool)obj).productId.equals(this.productId);	// jsefler 7/16/2010: this is not correct when is more than contract/serial has been issued for the same productId so as to increase the customers total quantity of available entitlements
 		
 		// assumes poolId is unique across all SubscriptionPools
 		return ((SubscriptionPool)obj).poolId.equals(this.poolId);
 	}
 	
 	public SubscriptionPool(String subscriptionLine) throws ParseException{
 		super(null);
 		
 		String[] components = subscriptionLine.split("\\t");
 		
 		subscriptionName = components[0].trim();
 		endDate = /*this.*/parseDateString(components[1].trim());
 		poolId = components[2].trim();
 		quantity = components[3].trim();	// Integer.parseInt(components[3].trim());
 		associatedProductIDs = new ArrayList<ProductSubscription>();
 	}
 	
 	public SubscriptionPool(Calendar startDate,
 			Calendar endDate,
 			Boolean activeSubscription,
 			Integer consumed,
 			String quantity,	//Integer quantity,
 			String id,
 			String productId){
 		super(null);
 		
 		this.startDate = startDate;
 		this.endDate = endDate;
 		this.activeSubscription = activeSubscription;
 		this.consumed = consumed;
 		this.quantity = quantity;
 		this.poolId = id;
 		this.subscriptionName = productId;
 		associatedProductIDs = new ArrayList<ProductSubscription>();
 	}
 	
 	public SubscriptionPool(
 			String subscriptionName,
 			String productId,
 			String poolId,
 			String quantity,
 			String endDate) {
 		super(null);
 		
 		this.subscriptionName = subscriptionName;
 		this.productId = productId;
 		this.poolId = poolId;
 		this.quantity = quantity;
 		this.endDate = parseDateString(endDate);
 		associatedProductIDs = new ArrayList<ProductSubscription>();
 	}
 	
 	public SubscriptionPool(
 			String productId,
 			String poolId) {
 		super(null);
 		
 		this.productId = productId;
 		this.poolId = poolId;
 		associatedProductIDs = new ArrayList<ProductSubscription>();
 	}
 
 	public SubscriptionPool(Map<String, String> poolMap) {
 		super(poolMap);
 	}
 	
 	@Override
 	public String toString() {
 		
 //		public Date startDate;
 //		public Date endDate;
 //		public Boolean activeSubscription;
 //		public Integer consumed;
 //		public Integer quantity;
 //		public String poolId;
 //		public String subscriptionName;
 //		public String productId;
 		
 		String string = "";
 		if (subscriptionName != null)	string += String.format(" %s='%s'", "subscriptionName",subscriptionName);
 		if (productId != null)			string += String.format(" %s='%s'", "productId",productId);
 		if (poolId != null)				string += String.format(" %s='%s'", "poolId",poolId);
 		if (quantity != null)			string += String.format(" %s='%s'", "quantity",quantity);
 		if (consumed != null)			string += String.format(" %s='%s'", "consumed",consumed);
 		if (activeSubscription != null)	string += String.format(" %s='%s'", "activeSubscription",activeSubscription);
 		if (startDate != null)			string += String.format(" %s='%s'", "startDate",formatDateString(startDate));
 		if (endDate != null)			string += String.format(" %s='%s'", "endDate",formatDateString(endDate));
 
 
 		return string.trim();
 	}
 	
 	@Override
 	protected Calendar parseDateString(String dateString){
 		return parseDateString(dateString, simpleDateFormat);
 	}
 	
 	//@Override
 	public static String formatDateString(Calendar date){
 		DateFormat dateFormat = new SimpleDateFormat(simpleDateFormat);
 		return dateFormat.format(date.getTime());
 	}
 	
 	/**
 	 * @param stdoutListingOfAvailableSubscriptions - stdout from "subscription-manager-cli list --available"
 	 * @return
 	 */
 	static public List<SubscriptionPool> parse(String stdoutListingOfAvailableSubscriptions) {
 		/*
 		# subscription-manager-cli list --available
 		+-------------------------------------------+
     		Available Subscriptions
 		+-------------------------------------------+
 		
 		Name:              	Basic RHEL Server        
 		ProductId:         	MKT-simple-rhel-server-mkt
 		PoolId:            	2                        
 		quantity:          	10                       
 		Expires:           	2011-07-01     
 		
 		ProductName:       	Scalable File System (1-2 sockets)
 		ProductId:         	RH1414165                
 		PoolId:            	8a8aa80d2be34ec0012be505f5a20600
 		Quantity:          	1                        
 		Expires:           	2011-10-24  
 		
 		ProductName:       	Red Hat Enterprise Linux Entitlement Alpha (1-2 Sockets)
                         	(Unlimited Virtualization)
 		ProductId:         	RH1050830                
 		PoolId:            	8a8aa80d2be34ec0012be505f34d05f9
 		Quantity:          	1                        
 		Expires:           	2011-01-24   
 		
 		ProductName:       	Red Hat Enterprise Linux Server Entitlement Beta for
                         	Certified Engineers and System Administrators - NOT FOR SALE
 		ProductId:         	RH3036913                
 		PoolId:            	8a9b90882da9ac9f012da9e5e991000e
 		Quantity:          	9                        
 		Expires:           	2011-07-19   
 		*/
 
 		Map<String,String> regexes = new HashMap<String,String>();
 		
 		// abstraction field					regex pattern (with a capturing group) Note: the captured group will be trim()ed
 		//regexes.put("subscriptionName",			"ProductName:(.*)");	// FIXME: truncates subscriptionName value when it spans multiple lines
 		regexes.put("subscriptionName",			"ProductName:(.*(\\n.*?)+)^\\w+:");	// this assumes that ProductName is NOT last in its subscription grouping since ^\w+: represents the start of the next property
 		regexes.put("productId",				"ProductId:(.*)");
 		regexes.put("poolId",					"PoolId:(.*)");
 		regexes.put("quantity",					"Quantity:(.*)");	// https://bugzilla.redhat.com/show_bug.cgi?id=612730
 		regexes.put("endDate",					"Expires:(.*)");
 		
 		List<Map<String,String>> listOfAvailableSubscriptionMaps = new ArrayList<Map<String,String>>();
 		for(String field : regexes.keySet()){
 			Pattern pat = Pattern.compile(regexes.get(field), Pattern.MULTILINE);
 			addRegexMatchesToList(pat, stdoutListingOfAvailableSubscriptions, listOfAvailableSubscriptionMaps, field);
 		}
 		
 		// assemble a new List of SubscriptionPool from the List of Available Subscription Maps
 		List<SubscriptionPool> subscriptionPools = new ArrayList<SubscriptionPool>();
 		for(Map<String,String> poolMap : listOfAvailableSubscriptionMaps) {
 			// normalize newlines from subscriptionName when it spans multiple lines
 			String key = "subscriptionName", subscriptionName = poolMap.get(key);
 			if (subscriptionName!=null) {
 				poolMap.remove(key);
 				subscriptionName = subscriptionName.replaceAll("\\s*\\n\\s*", " ");
 				poolMap.put(key, subscriptionName);
 			}
 			subscriptionPools.add(new SubscriptionPool(poolMap));
 		}
 		return subscriptionPools;
 	}
 
 }
