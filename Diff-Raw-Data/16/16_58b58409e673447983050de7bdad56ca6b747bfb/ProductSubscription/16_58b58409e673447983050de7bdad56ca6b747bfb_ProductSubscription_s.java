 package com.redhat.qe.sm.data;
 
 import java.math.BigInteger;
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
 
 public class ProductSubscription extends AbstractCommandLineData {
	protected static String simpleDateFormat = "MM/dd/yyyy";	// 04/24/2011 https://bugzilla.redhat.com/show_bug.cgi?id=695234
 	
 	// abstraction fields
 	public String productName;
 	public BigInteger serialNumber;	// Long serialNumber;	// Integer serialNumber; // serialNumber=290624661330496 is out of range for an Integer
 	public Integer contractNumber;
 	public BigInteger accountNumber;
 	public Boolean isActive;
 	public Calendar startDate;
 	public Calendar endDate;
 	
 	public SubscriptionPool fromSubscriptionPool;
 	
 	public Boolean isExpired(){
 		return endDate.after(new Date());
 	}
 
 	
 	public ProductSubscription(Map<String, String> productData){
 		super(productData);
 		
 		if (this.productName == null)
 			this.productName = "";
 	}
 	
 	/**
 	 * UNTESTED/UNUSED METHOD FROM ssalevan
 	 * @param subscriptionLine
 	 * @throws ParseException
 	 */
 	public ProductSubscription(String subscriptionLine) throws ParseException{
 		super(null);
 		
 		String[] components = subscriptionLine.split("\\t");
 		
 		productName = components[0].trim();
 		isActive = components[1].toLowerCase().contains("true");
 		endDate = /*this.*/parseDateString(components[2].trim());
 	}
 	
 	/**
 	 * UNTESTED/UNUSED METHOD FROM ssalevan
 	 * @param productName
 	 * @param fromPool
 	 */
 	public ProductSubscription(String productName, SubscriptionPool fromPool){
 		super(null);
 		
 		this.productName = productName;
 		this.fromSubscriptionPool = fromPool;
 	}
 	
 	@Override
 	public String toString() {
 		
 		String string = "";
 		if (productName != null)	string += String.format(" %s='%s'", "productName",productName);
 		if (serialNumber != null)	string += String.format(" %s='%s'", "serialNumber",serialNumber);
 		if (contractNumber != null)	string += String.format(" %s='%s'", "contractNumber",contractNumber);
 		if (isActive != null)		string += String.format(" %s='%s'", "isActive",isActive);
 		if (startDate != null)		string += String.format(" %s='%s'", "startDate",formatDateString(startDate));
 		if (endDate != null)		string += String.format(" %s='%s'", "endDate",formatDateString(endDate));
 		if (fromSubscriptionPool != null)		string += String.format(" %s='%s'", "fromPool",fromSubscriptionPool);
 
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
 	
 	@Override
 	public boolean equals(Object obj){
 		//return ((ProductSubscription)obj).productName.contains(this.productName);	// I think we need to compare more attributes than using .contains(...) on the productName jsefler 7/13/2010
 
 		// assume the combination of productName and serialNumber is unique across all ProductSubscriptions
 		return	((ProductSubscription)obj).productName.equals(this.productName) &&
 				((ProductSubscription)obj).serialNumber.equals(this.serialNumber);
 	}
 	
 	/**
 	 * @param stdoutListOfConsumedProducts - stdout from "subscription-manager-cli list --consumed"
 	 * @return
 	 */
 	static public List<ProductSubscription> parse(String stdoutListOfConsumedProducts) {
 		/*
 		# subscription-manager-cli list --consumed
 		+-------------------------------------------+
     		Consumed Product Subscriptions
 		+-------------------------------------------+
 		
 		Name:               	High availability (cluster suite)
 		ContractNumber:       	0                        
 		SerialNumber:       	17                       
 		Active:             	True                     
 		Begins:             	2010-07-01               
 		Expires:            	2011-07-01   
 		
 		ProductName:        	Red Hat Enterprise Linux 6 Entitlement Alpha
 		ContractNumber:     	1970595                  
 		SerialNumber:       	1151289234191548136      
 		Active:             	True                     
 		Begins:             	2010-10-25               
 		Expires:            	2011-01-24   
 		
 		ProductName:        	Smart Management (RHN Management & Provisioning)
 		ContractNumber:     	12                       
 		AccountNumber:      	12331131231              
 		SerialNumber:       	11292428201405242        
 		Active:             	True                     
 		Begins:             	2010-12-14               
 		Expires:            	2011-12-15   
 		
 		ProductName:        	Awesome OS Server Bundled
 		ContractNumber:     	1391380943               
 		AccountNumber:      	None                     
 		SerialNumber:       	1669803970633823565      
 		Active:             	True                     
 		Begins:             	2011-04-07               
 		Expires:            	2011-04-08     
 		*/
 
 		Map<String,String> regexes = new HashMap<String,String>();
 
 
 		// abstraction field				regex pattern (with a capturing group) Note: the captured group will be trim()ed
 		regexes.put("productName",			"Name:(.*)");
 		regexes.put("serialNumber",			"SerialNumber:(.*)");
 		regexes.put("contractNumber",		"ContractNumber:(.*)");
 		regexes.put("accountNumber",		"AccountNumber:(.*)");
 		regexes.put("isActive",				"Active:(.*)");
 		regexes.put("startDate",			"Begins:(.*)");
 		regexes.put("endDate",				"Expires:(.*)");
 		
 		List<Map<String,String>> productList = new ArrayList<Map<String,String>>();
 		for(String field : regexes.keySet()){
 			Pattern pat = Pattern.compile(regexes.get(field), Pattern.MULTILINE);
 			addRegexMatchesToList(pat, stdoutListOfConsumedProducts, productList, field);
 		}
 		
 		List<ProductSubscription> productSubscriptions = new ArrayList<ProductSubscription>();
 		for(Map<String,String> productMap : productList)
 			productSubscriptions.add(new ProductSubscription(productMap));
 		return productSubscriptions;
 	}
 }
