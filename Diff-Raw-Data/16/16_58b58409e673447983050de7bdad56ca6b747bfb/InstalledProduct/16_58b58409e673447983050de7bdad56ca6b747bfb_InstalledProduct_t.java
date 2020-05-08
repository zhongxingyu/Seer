 package com.redhat.qe.sm.data;
 
 import java.math.BigInteger;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import com.redhat.qe.tools.abstraction.AbstractCommandLineData;
 
 public class InstalledProduct extends AbstractCommandLineData {
	protected static String simpleDateFormat = "MM/dd/yyyy";	// 04/24/2011 https://bugzilla.redhat.com/show_bug.cgi?id=695234  https://bugzilla.redhat.com/show_bug.cgi?id=699442
 	
 	// abstraction fields
 	public String productName;
 	public String status;
 	public Calendar expires;
 	public BigInteger subscription;
 	public Long contractNumber;
 	
 	public InstalledProduct(Map<String, String> productData) {
 		super(productData);
 	}
 	
 	
 	@Override
 	public String toString() {
 		
 //		public String productName;
 //		public String status;
 //		public Date expires;
 //		public Integer subscription;
 		
 		String string = "";
 		if (productName != null)		string += String.format(" %s='%s'", "productName",productName);
 		if (status != null)				string += String.format(" %s='%s'", "status",status);
 		if (expires != null)			string += String.format(" %s='%s'", "expires",formatDateString(expires));
 		if (subscription != null)		string += String.format(" %s='%s'", "subscription",subscription);
 		if (contractNumber != null)		string += String.format(" %s='%s'", "contractNumber",contractNumber);
 
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
 	 * @param stdoutListingOfProductCerts - stdout from "subscription-manager-cli list"
 	 * @return
 	 */
 	static public List<InstalledProduct> parse(String stdoutListingOfProductCerts) {
 		/*
 		# subscription-manager-cli list
 		+-------------------------------------------+
 		    Installed Product Status
 		+-------------------------------------------+
 
 		ProductName:        	Shared Storage (GFS)     
 		Status:             	Not Installed            
 		Expires:            	2011-07-01               
 		Subscription:       	17                       
 		ContractNumber:        	0   
 
 		ProductName:        	Red Hat Enterprise Linux High Availability (for RHEL 6 Entitlement)
 		Status:             	Not Subscribed           
 		Expires:            	                         
 		Subscription:       	                         
 		ContractNumber: 
 		
 		ProductName:        	Red Hat Enterprise Linux 6 Entitlement Alpha
 		Status:             	Subscribed               
 		Expires:            	2011-01-24               
 		Subscription:       	1151289234191548136      
 		ContractNumber:        	1970595  
 		*/
 		
 		Map<String,String> regexes = new HashMap<String,String>();
 		
 		// abstraction field				regex pattern (with a capturing group) Note: the captured group will be trim()ed
 		regexes.put("productName",			"ProductName:(.*)");
 		regexes.put("status",				"Status:(.*)");
 		regexes.put("expires",				"Expires:(.*)");
 		regexes.put("subscription",			"Subscription:(.*)");
 		regexes.put("contractNumber",		"ContractNumber:(.*)");
 		
 		List<Map<String,String>> productCertList = new ArrayList<Map<String,String>>();
 		for(String field : regexes.keySet()){
 			Pattern pat = Pattern.compile(regexes.get(field), Pattern.MULTILINE);
 			addRegexMatchesToList(pat, stdoutListingOfProductCerts, productCertList, field);
 		}
 		
 		List<InstalledProduct> productCerts = new ArrayList<InstalledProduct>();
 		for(Map<String,String> prodCertMap : productCertList)
 			productCerts.add(new InstalledProduct(prodCertMap));
 		return productCerts;
 	}
 }
