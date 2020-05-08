 package rhsm.data;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import com.redhat.qe.tools.abstraction.AbstractCommandLineData;
 
 /**
  * @author jsefler
  *
  */
 public class InstalledProduct extends AbstractCommandLineData {
 	protected static String simpleDateFormat = "MM/dd/yyyy";	// 04/24/2011 https://bugzilla.redhat.com/show_bug.cgi?id=695234  https://bugzilla.redhat.com/show_bug.cgi?id=699442
 	
 	/*
 	ProductName:        	Awesome OS for S390 Bits 
 	Version:            	3.11                     
 	Arch:               	s390x                    
 	Status:             	Not Subscribed           
 	Starts:             	09/19/2011               
 	Expires:            	09/20/2011   
 	*/
 	
 	// abstraction fields
 	public String productName;
 	public String productId;
 	public String version;
 	public String arch;
 	public String status;
 	public Calendar startDate;
 	public Calendar endDate;
 	
 	public InstalledProduct(Map<String, String> productData) {
 		super(productData);
 	}
 	
 	public InstalledProduct(String productName, String productId, String version, String arch, String status, Calendar startDate, Calendar endDate) {
 		super(null);
 		this.productName = productName;
 		this.productId = productId;
 		this.version = version;
 		this.arch = arch;
 		this.status = status;
 		this.startDate = startDate;
 		this.endDate = endDate;
 	}
 	
 	@Override
 	public String toString() {
 		
 		String string = "";
 		if (productName != null)		string += String.format(" %s='%s'", "productName",productName);
 		if (productId != null)			string += String.format(" %s='%s'", "productId",productId);
 		if (version != null)			string += String.format(" %s='%s'", "version",version);
 		if (arch != null)				string += String.format(" %s='%s'", "arch",arch);
 		if (status != null)				string += String.format(" %s='%s'", "status",status);
 		if (startDate != null)			string += String.format(" %s='%s'", "startDate",formatDateString(startDate));
 		if (endDate != null)			string += String.format(" %s='%s'", "endDate",formatDateString(endDate));
 		
 		return string.trim();
 	}
 
 // DELETEME: The super.equals was fixed which should mean that this Override is not needed anymore.
 //	@Override
 //	public boolean equals(Object obj){
 //		InstalledProduct that = (InstalledProduct) obj;
 //		
 //		if (that.productName!=null && !that.productName.equals(this.productName)) return false;
 //		if (this.productName!=null && !this.productName.equals(that.productName)) return false;
 //		
 //		if (that.version!=null && !that.version.equals(this.version)) return false;
 //		if (this.version!=null && !this.version.equals(that.version)) return false;
 //		
 //		if (that.arch!=null && !that.arch.equals(this.arch)) return false;
 //		if (this.arch!=null && !this.arch.equals(that.arch)) return false;
 //		
 //		if (that.status!=null && !that.status.equals(this.status)) return false;
 //		if (this.status!=null && !this.status.equals(that.status)) return false;
 //		
 //		if (that.startDate!=null && !that.startDate.equals(this.startDate)) return false;
 //		if (this.startDate!=null && !this.startDate.equals(that.startDate)) return false;
 //		
 //		if (that.endDate!=null && !that.endDate.equals(this.endDate)) return false;
 //		if (this.endDate!=null && !this.endDate.equals(that.endDate)) return false;
 //		
 //		return true;
 //	}
 	
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
 	 * @param stdoutListingOfInstalledProducts - stdout from "subscription-manager --list --installed"
 	 * @return
 	 */
 	static public List<InstalledProduct> parse(String stdoutListingOfInstalledProducts) {
 		/* [root@jsefler-onprem-62server ~]# subscription-manager list --installed
 		+-------------------------------------------+
 		    Installed Product Status
 		+-------------------------------------------+
 		
 		ProductName:        	Awesome OS Developer Basic
 		Version:            	1.0                      
 		Arch:               	ALL                      
 		Status:             	Not Subscribed           
 		Starts:             	09/19/2011               
 		Expires:            	09/20/2011               
 				
 		ProductName:        	Awesome OS for x86_64/i686/ia64/ppc/ppc64/s390x/s390 Bits
 		Version:            	3.11                     
 		Arch:               	x86_64,i686,ia64,ppc,ppc64,s390x,s390
 		Status:             	Not Subscribed           
 		Starts:             	09/19/2011               
 		Expires:            	09/20/2011  
 
 
 
 
 		Product Name:         	Red Hat Enterprise Linux Server
 		Product ID:           	69                       
 		Version:              	6.3                      
 		Arch:                 	x86_64                   
 		Status:               	Not Subscribed           
 		Starts:               	                         
 		Expires:              	                         
 
 		Product Name:         	Awesome OS Workstation Bits
 		Product ID:           	27060                    
 		Version:              	6.1                      
 		Arch:                 	ALL                      
 		Status:               	Subscribed               
 		Starts:               	04/30/2012               
 		Expires:              	04/30/2013  
 		
 		
 		Product Name:         	Load Balancing Bits
 		Product ID:           	37070
 		Version:              	1.0
 		Arch:                 	ALL
 		Status:               	Subscribed
 		Starts:               	06/26/2012
 		Ends:                 	06/26/2013
 
 		*/
 		
 		Map<String,String> regexes = new HashMap<String,String>();
 		
 		// abstraction field		regex pattern (with a capturing group) Note: the captured group will be trim()ed
		regexes.put("productName",	"^Product Name:(.*)");
 		regexes.put("productId",	"^Product ID:(.*)");
 		regexes.put("version",		"^Version:(.*)");
 		regexes.put("arch",			"^Arch:(.*)");
 		regexes.put("status",		"^Status:(.*)");
 		regexes.put("startDate",	"^Starts:(.*)");	// Bug 812373 - Terminology Change to Subscription-Manager list --installed & --consumed
 		regexes.put("endDate",		"^Ends:(.*)");		// Bug 812373 - Terminology Change to Subscription-Manager list --installed & --consumed
 		
 		List<Map<String,String>> productCertList = new ArrayList<Map<String,String>>();
 		for(String field : regexes.keySet()){
 			Pattern pat = Pattern.compile(regexes.get(field), Pattern.MULTILINE);
 			addRegexMatchesToList(pat, stdoutListingOfInstalledProducts, productCertList, field);
 		}
 		
 		List<InstalledProduct> productCerts = new ArrayList<InstalledProduct>();
		for(Map<String,String> prodCertMap : productCertList)
 			productCerts.add(new InstalledProduct(prodCertMap));
 		return productCerts;
 	}
 }
 
 
 // DELETEME 9/20/2011 PRE-IMPLEMENTATION OF Bug 736424 - CLI function "subscription-manager list --installed" reports TOO much information
 //public class InstalledProduct extends AbstractCommandLineData {
 //	protected static String simpleDateFormat = "MM/dd/yyyy";	// 04/24/2011 https://bugzilla.redhat.com/show_bug.cgi?id=695234  https://bugzilla.redhat.com/show_bug.cgi?id=699442
 //	
 //	// abstraction fields
 //	public String productName;
 //	public String status;
 //	public Calendar expires;
 //	public BigInteger serialNumber;	// subscription;	// name changed by bug https://bugzilla.redhat.com/show_bug.cgi?id=712415
 //	public Long contractNumber;
 //	public BigInteger accountNumber;
 //	
 //	public InstalledProduct(Map<String, String> productData) {
 //		super(productData);
 //	}
 //	
 //	public InstalledProduct(String productName, String status, Calendar expires, BigInteger serialNumber, Long contractNumber, BigInteger accountNumber) {
 //		super(null);
 //		this.productName = productName;
 //		this.status = status;
 //		this.expires = expires;
 //		this.serialNumber = serialNumber;
 //		this.contractNumber = contractNumber;
 //		this.accountNumber = accountNumber;
 //	}
 //	
 //	@Override
 //	public String toString() {
 //		
 //		String string = "";
 //		if (productName != null)		string += String.format(" %s='%s'", "productName",productName);
 //		if (status != null)				string += String.format(" %s='%s'", "status",status);
 //		if (expires != null)			string += String.format(" %s='%s'", "expires",formatDateString(expires));
 //		if (serialNumber != null)		string += String.format(" %s='%s'", "serialNumber",serialNumber);
 //		if (contractNumber != null)		string += String.format(" %s='%s'", "contractNumber",contractNumber);
 //		if (accountNumber != null)		string += String.format(" %s='%s'", "accountNumber",accountNumber);
 //
 //		return string.trim();
 //	}
 //	
 //	@Override
 //	public boolean equals(Object obj){
 //		InstalledProduct that = (InstalledProduct) obj;
 //		
 //		if (that.productName!=null && !that.productName.equals(this.productName)) return false;
 //		if (this.productName!=null && !this.productName.equals(that.productName)) return false;
 //		
 //		if (that.status!=null && !that.status.equals(this.status)) return false;
 //		if (this.status!=null && !this.status.equals(that.status)) return false;
 //		
 //		if (that.expires!=null && !that.expires.equals(this.expires)) return false;
 //		if (this.expires!=null && !this.expires.equals(that.expires)) return false;
 //		
 //		if (that.serialNumber!=null && !that.serialNumber.equals(this.serialNumber)) return false;
 //		if (this.serialNumber!=null && !this.serialNumber.equals(that.serialNumber)) return false;
 //		
 //		if (that.contractNumber!=null && !that.contractNumber.equals(this.contractNumber)) return false;
 //		if (this.contractNumber!=null && !this.contractNumber.equals(that.contractNumber)) return false;
 //		
 //		if (that.accountNumber!=null && !that.accountNumber.equals(this.accountNumber)) return false;
 //		if (this.accountNumber!=null && !this.accountNumber.equals(that.accountNumber)) return false;
 //		
 //		if (that.productName!=null && !that.productName.equals(this.productName)) return false;
 //		if (this.productName!=null && !this.productName.equals(that.productName)) return false;
 //		
 //		if (that.productName!=null && !that.productName.equals(this.productName)) return false;
 //		if (this.productName!=null && !this.productName.equals(that.productName)) return false;
 //		
 //		return true;
 //	}
 //	
 //	@Override
 //	protected Calendar parseDateString(String dateString){
 //		return parseDateString(dateString, simpleDateFormat);
 //	}
 //	
 //	//@Override
 //	public static String formatDateString(Calendar date){
 //		DateFormat dateFormat = new SimpleDateFormat(simpleDateFormat);
 //		return dateFormat.format(date.getTime());
 //	}
 //	
 //	/**
 //	 * @param stdoutListingOfProductCerts - stdout from "subscription-manager-cli list"
 //	 * @return
 //	 */
 //	static public List<InstalledProduct> parse(String stdoutListingOfProductCerts) {
 //		/*
 //		# subscription-manager-cli list
 //		+-------------------------------------------+
 //		    Installed Product Status
 //		+-------------------------------------------+
 //
 //		ProductName:        	Shared Storage (GFS)     
 //		Status:             	Not Installed            
 //		Expires:            	2011-07-01               
 //		Subscription:       	17                       
 //		ContractNumber:        	0   
 //
 //		ProductName:        	Red Hat Enterprise Linux High Availability (for RHEL 6 Entitlement)
 //		Status:             	Not Subscribed           
 //		Expires:            	                         
 //		Subscription:       	                         
 //		ContractNumber: 
 //		
 //		ProductName:        	Red Hat Enterprise Linux 6 Entitlement Alpha
 //		Status:             	Subscribed               
 //		Expires:            	2011-01-24               
 //		Subscription:       	1151289234191548136      
 //		ContractNumber:        	1970595  
 //		
 //		
 //		ProductName:        	Awesome OS Scalable Filesystem Bits
 //		Status:             	Subscribed               
 //		Expires:            	07/17/2012               
 //		SerialNumber:       	5945536885441836861      
 //		ContractNumber:     	2                        
 //		AccountNumber:      	12331131231 
 //		*/
 //		
 //		Map<String,String> regexes = new HashMap<String,String>();
 //		
 //		// abstraction field				regex pattern (with a capturing group) Note: the captured group will be trim()ed
 //		regexes.put("productName",			"^ProductName:(.*)");
 //		regexes.put("status",				"^Status:(.*)");
 //		regexes.put("expires",				"^Expires:(.*)");
 //		regexes.put("serialNumber",			"^SerialNumber:(.*)");
 //		regexes.put("contractNumber",		"^ContractNumber:(.*)");
 //		regexes.put("accountNumber",		"^AccountNumber:(.*)");
 //	
 //		List<Map<String,String>> productCertList = new ArrayList<Map<String,String>>();
 //		for(String field : regexes.keySet()){
 //			Pattern pat = Pattern.compile(regexes.get(field), Pattern.MULTILINE);
 //			addRegexMatchesToList(pat, stdoutListingOfProductCerts, productCertList, field);
 //		}
 //		
 //		List<InstalledProduct> productCerts = new ArrayList<InstalledProduct>();
 //		for(Map<String,String> prodCertMap : productCertList)
 //			productCerts.add(new InstalledProduct(prodCertMap));
 //		return productCerts;
 //	}
 //}
