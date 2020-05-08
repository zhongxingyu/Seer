 package com.redhat.qe.sm.data;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.tools.SSHCommandRunner;
 import com.redhat.qe.tools.abstraction.AbstractCommandLineData;
 
 /**
  * @author jsefler
  * Notes: Top see what products are installed:
 * find /tmp/product/ -name '*.pem' -exec openssl x509 -in '{}' -text  \; | egrep -A1 "1.3.6.1.4.1.2312.9.1.*"
  */
 public class ProductCert extends AbstractCommandLineData {
 	protected static String simpleDateFormat = "MMM d HH:mm:ss yyyy z";	// Aug 23 08:42:00 2010 GMT
 
 	// abstraction fields
 	public BigInteger serialNumber;	// this is the key
 	public String id;
 	public String issuer;
 	public Calendar validityNotBefore;
 	public Calendar validityNotAfter;
 	public File file;
 	
 	public String serialString;
 	public ProductNamespace productNamespace;
 	protected String rawCertificate;
 	
 	// TODO get rid of these since they are in productNamespace
 	public String productName;	// comes from the ProductNamespace
 	public String productId;	// comes from the ProductNamespace (this is the hash for example the 69 in 69.pem)
 
 
 	public ProductCert(String rawCertificate, Map<String, String> certData){
 		super(certData);
 		this.serialNumber = new BigInteger(serialString.replaceAll(":", ""),16);	// strip out the colons and convert to a number
 		this.rawCertificate = rawCertificate;
 		List<ProductNamespace> productNamespaces = ProductNamespace.parse(this.rawCertificate);
 		if (productNamespaces.size()!=1) Assert.fail("Error: expected only one ProductNamespace when parsing raw ceritificate for ProductCert.");
 		this.productNamespace = productNamespaces.get(0);
 		this.productName = productNamespace.name;	// extract the product name
 		this.productId = productNamespace.id;		// extract the hash
 	}
 
 	
 	
 	@Override
 	public String toString() {
 		
 		String string = "";
 		if (productNamespace != null)	string += String.format(" %s",		productNamespace);
 		if (productName != null)		string += String.format(" %s='%s'", "productName",productName);
 		if (productId != null)			string += String.format(" %s='%s'", "productId",productId);
 		if (serialNumber != null)		string += String.format(" %s='%s'", "serialNumber",serialNumber);
 		if (id != null)					string += String.format(" %s='%s'", "id",id);
 		if (issuer != null)				string += String.format(" %s='%s'", "issuer",issuer);
 		if (validityNotBefore != null)	string += String.format(" %s='%s'", "validityNotBefore",formatDateString(validityNotBefore));
 		if (validityNotAfter != null)	string += String.format(" %s='%s'", "validityNotAfter",formatDateString(validityNotAfter));
 		if (file != null)				string += String.format(" %s='%s'", "file",file);
 
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
 
 		return	((ProductCert)obj).serialNumber.equals(this.serialNumber) &&
 				((ProductCert)obj).id.equals(this.id) &&
 				((ProductCert)obj).issuer.equals(this.issuer) &&
 				((ProductCert)obj).validityNotBefore.equals(this.validityNotBefore) &&
 				((ProductCert)obj).validityNotAfter.equals(this.validityNotAfter) &&
 				((ProductCert)obj).productName.equals(this.productName);
 	}
 
 // THIS IDEA WAS TRUMPED BY ADDING A NEW ABSTRACT file FIELD AND THEN INCLUDING A REGEX FOR IT IN THE parse METHOD
 //	static public ProductCert parse(SSHCommandRunner sshCommandRunner, File productCertFile) {
 //		sshCommandRunner.runCommandAndWaitWithoutLogging("openssl x509 -in "+productCertFile+" -noout -text");
 //		String certificate = sshCommandRunner.getStdout();
 //		ProductCert productCert = parse(certificate).get(0);
 //		productCert.file = productCertFile;
 //		return productCert;
 //	}
 	
 	/**
 	 * @param rawCertificates - OLD WAY: stdout from: find /etc/pki/product/ -name '*.pem' | xargs -I '{}' openssl x509 -in '{}' -noout -text
 	 * @param rawCertificates - stdout from: find /etc/pki/product/ -name '*.pem' -exec openssl x509 -in '{}' -noout -text \; -exec echo "    File: {}" \;
 	 * @return
 	 */
 	static public List<ProductCert> parse(String rawCertificates) {
 		
 		/* [root@jsefler-itclient01 ~]# openssl x509 -noout -text -in /etc/pki/product/2156.pem 
 		Certificate:
 		    Data:
 		        Version: 3 (0x2)
 		        Serial Number:
 		            b0:f1:44:bb:7f:b5:48:e1
 		        Signature Algorithm: sha1WithRSAEncryption
 		        Issuer: C=US, ST=North Carolina, O=Red Hat, Inc., OU=Red Hat Network, CN=Red Hat Entitlement Product Authority/emailAddress=ca-support@redhat.com
 		        Validity
 		            Not Before: Oct  5 14:10:38 2010 GMT
 		            Not After : Nov  4 14:10:38 2010 GMT
 		        Subject: CN=Red Hat Product ID [ed92ebfb-e599-4d15-8383-1fcaed979211]
 		        Subject Public Key Info:
 		            Public Key Algorithm: rsaEncryption
 		                Public-Key: (4096 bit)
 		                Modulus:
 		                    00:c6:3f:49:d3:8c:fe:13:37:72:c7:55:37:de:41:
 		                    5f:b4:bb:f4:9e:d3:52:d4:14:9e:ba:1a:19:2c:38:
 		                    6c:d7:cc:a4:b1:ea:46:ee:15:d7:b4:78:76:08:c1:
 		                    d0:10:3d:a9:42:a6:7f:ae:1b:96:ef:d8:dc:c8:26:
 		                    65:f6:45:ea:7b:5c:0e:74:5d:02:c1:a8:78:37:af:
 		                    f7:2f:32:17:10:42:b2:10:9c:4d:a9:0c:2d:35:cd:
 		                    88:54:4f
 		                Exponent: 65537 (0x10001)
 		        X509v3 extensions:
 		            X509v3 Basic Constraints: 
 		                CA:FALSE
 		            1.3.6.1.4.1.2312.9.1.2156.1: 
 		                .*Red Hat Enterprise Linux Entitlement Alpha
 		            1.3.6.1.4.1.2312.9.1.2156.2: 
 		                ..
 		            1.3.6.1.4.1.2312.9.1.2156.3: 
 		                ..
 		            1.3.6.1.4.1.2312.9.1.2156.4: 
 		                ..
 		    Signature Algorithm: sha1WithRSAEncryption
 		        86:a2:c3:72:5a:5a:61:df:f0:d4:0d:ca:ac:14:8c:8c:46:8b:
 		        7a:b8:46:53:40:6b:3b:15:dd:05:ec:3f:d4:d5:ab:6c:3c:ba:
 		        3e:34:ab:6e:b6:87:6b:34:9c:77:ae:67:96:60:5f:0e:d9:11:
 		        78:a6:91:22:b4:72:61:cc:13:f5:ec:4d:6e:61:21:42:73:85:
 		        15:81:8b:a6:51:67:08:2e
 		 */
 		
 		/* [root@jsefler-stage-6server product]# openssl x509 -text -in /etc/pki/product/69.pem 
 		Certificate:
 		    Data:
 		        Version: 3 (0x2)
 		        Serial Number:
 		            b0:f1:44:bb:7f:b5:49:ec
 		        Signature Algorithm: sha1WithRSAEncryption
 		        Issuer: C=US, ST=North Carolina, O=Red Hat, Inc., OU=Red Hat Network, CN=Red Hat Entitlement Product Authority/emailAddress=ca-support@redhat.com
 		        Validity
 		            Not Before: Apr 27 19:37:13 2011 GMT
 		            Not After : Apr 22 19:37:13 2031 GMT
 		        Subject: CN=Red Hat Product ID [a81db0cc-f72c-4386-a60d-dd6d1e037378]
 		        Subject Public Key Info:
 		            Public Key Algorithm: rsaEncryption
 		                Public-Key: (4096 bit)
 		                Modulus:
 		                    00:c6:3f:49:d3:8c:fe:13:37:72:c7:55:37:de:41:
 		                    5f:b4:bb:f4:9e:d3:52:d4:14:9e:ba:1a:19:2c:38:
 		                    65:f6:45:ea:7b:5c:0e:74:5d:02:c1:a8:78:37:af:
 		                    f7:2f:32:17:10:42:b2:10:9c:4d:a9:0c:2d:35:cd:
 		                    88:54:4f
 		                Exponent: 65537 (0x10001)
 		        X509v3 extensions:
 		            X509v3 Basic Constraints: 
 		                CA:FALSE
 		            1.3.6.1.4.1.2312.9.1.69.1: 
 		                .!Red Hat Enterprise Linux 6 Server
 		            1.3.6.1.4.1.2312.9.1.69.2: 
 		                ..6.1
 		            1.3.6.1.4.1.2312.9.1.69.3: 
 		                ..x86_64
 		            1.3.6.1.4.1.2312.9.1.69.4: 
 		                ..rhel-6,rhel-6-server
 		    Signature Algorithm: sha1WithRSAEncryption
 		        50:8c:61:f7:6c:37:66:52:b1:87:82:ac:fb:2d:24:62:40:d0:
 		        34:e8:e5:ba:6b:65:6a:76:fc:77:62:9e:d2:9d:7c:be:b9:38:
 		        ed:3f:2f:89:d4:f6:a7:0c:60:7c:a6:17:63:cb:39:13:28:fb:
 		        a8:bd:3d:64:9e:c3:13:af:d9:9c:9c:d9:3a:ac:6a:a5:7d:f8:
 		        1b:79:8b:59:12:09:72:e1
 		-----BEGIN CERTIFICATE-----
 		MIIGDzCCA/egAwIBAgIJALDxRLt/tUnsMA0GCSqGSIb3DQEBBQUAMIGuMQswCQYD
 		VQQGEwJVUzEXMBUGA1UECAwOTm9ydGggQ2Fyb2xpbmExFjAUBgNVBAoMDVJlZCBI
 		XYJsRbON5l4Umk6tcRpBrAT7BX04h6fbMErU0r3j9aiFqcCaXBUWMlfK1YGg8OHS
 		ommdlVFsLtPx/cny5ICiMxEWDS8eX5aVpKqdT2LyUD9RrkovEYlER9kWG4QnKOor
 		R9PGcixNdxYHpP4/LYmhhNfstBGBZIHtPy+J1PanDGB8phdjyzkTKPuovT1knsMT
 		r9mcnNk6rGqlffgbeYtZEgly4Q==
 		-----END CERTIFICATE-----
 		*/
 		
 		/* [root@jsefler-onprem-62server ~]# find /tmp/sm-stackingProductDir/ -name '*.pem' -exec openssl x509 -in '{}' -noout -text \; -exec echo "    File: {}" \;
 		Certificate:
 		    Data:
 		        Version: 3 (0x2)
 		        Serial Number: 168296333 (0xa07ff8d)
 		        Signature Algorithm: sha1WithRSAEncryption
 		        Issuer: CN=jsefler-onprem-62candlepin.usersys.redhat.com, C=US, L=Raleigh
 		        Validity
 		            Not Before: Sep 19 17:35:27 2011 GMT
 		            Not After : Sep 19 17:35:27 2021 GMT
 		        Subject: CN=100000000000002
 		        Subject Public Key Info:
 		            Public Key Algorithm: rsaEncryption
 		                Public-Key: (2048 bit)
 		                Modulus:
 		                    00:8b:2c:36:0d:17:ce:fd:e6:8d:4d:5a:ee:6e:40:
 		                    57:11:2d:89:37:b2:bb:10:52:e3:8a:0b:41:13:82:
 		                    33:60:85:3f:b4:aa:4b:81:54:71:42:fd:e4:55:c9:
 		                    87:37:02:95:94:07:6b:72:e2:d3:bc:5e:36:fb:31:
 		                    73:3b:af:23:da:e8:c7:45:e9:45:dd:f6:58:13:48:
 		                    15:94:23:7b:d9:d3:a2:d9:15:45:ca:ba:36:46:5d:
 		                    06:a1
 		                Exponent: 65537 (0x10001)
 		        X509v3 extensions:
 		            Netscape Cert Type: 
 		                SSL Client, S/MIME
 		            X509v3 Key Usage: 
 		                Digital Signature, Key Encipherment, Data Encipherment
 		            X509v3 Authority Key Identifier: 
 		                keyid:82:7C:66:96:9E:DB:01:A9:01:A4:32:EE:97:80:26:6D:1F:AC:03:66
 		                DirName:/CN=jsefler-onprem-62candlepin.usersys.redhat.com/C=US/L=Raleigh
 		                serial:BC:92:AE:6A:DE:86:B6:D6
 
 		            X509v3 Subject Key Identifier: 
 		                50:15:87:24:A3:FB:E4:A0:31:E6:70:E4:C2:06:70:82:92:0C:39:51
 		            X509v3 Extended Key Usage: 
 		                TLS Web Client Authentication
 		            1.3.6.1.4.1.2312.9.1.100000000000002.1: 
 		                ..Awesome OS for x86_64 Bits
 		            1.3.6.1.4.1.2312.9.1.100000000000002.3: 
 		                ..x86_64
 		            1.3.6.1.4.1.2312.9.1.100000000000002.2: 
 		                ..3.11
 		    Signature Algorithm: sha1WithRSAEncryption
 		        24:35:5d:c3:02:73:80:37:4e:e1:20:17:87:82:d2:32:6e:8b:
 		        33:7b:de:f5:eb:ec:e8:06:03:6d:c1:70:12:ca:d7:4d:1d:a5:
 		        13:7b:1d:60:3f:66:88:db:45:e0:a1:1f:38:e9:77:71:55:41:
 		        96:f5:76:47:c8:02:9c:38:a0:54:92:7c:c6:26:db:59:04:ec:
 		        e4:9d:63:8b:5d:4c:e4:c2:29:63:fc:27:40:e3:fc:42:ab:cf:
 		        48:de
 		    File: /tmp/sm-stackingProductDir/100000000000002_.pem
 		[root@jsefler-onprem-62server ~]# 
 		*/
 		
 		
 		List<ProductCert> productCerts = new ArrayList<ProductCert>();
 		
 		// begin by splitting the rawCertificates and processing each certificate individually
 		for (String rawCertificate : rawCertificates.split("Certificate:")) {
 			if (rawCertificate.trim().length()==0) continue;
 	
 			Map<String,String> regexes = new HashMap<String,String>();
 
 			// abstraction field				regex pattern (with a capturing group) Note: the captured group will be trim()ed
 			regexes.put("serialString",			"Serial Number:\\s*([\\d\\w:]+)");
 			regexes.put("id",					"Subject: CN=(.+)");
 			regexes.put("issuer",				"Issuer:\\s*(.*)");
 			regexes.put("validityNotBefore",	"Validity[\\n\\s\\w:]*Not Before\\s*:\\s*(.*)");
 			regexes.put("validityNotAfter",		"Validity[\\n\\s\\w:]*Not After\\s*:\\s*(.*)");
 			regexes.put("file",					"File: (.+)");
 
 			List<Map<String,String>> certDataList = new ArrayList<Map<String,String>>();
 			for(String field : regexes.keySet()){
 				Pattern pat = Pattern.compile(regexes.get(field), Pattern.MULTILINE);
 				addRegexMatchesToList(pat, rawCertificate, certDataList, field);
 			}
 			
 			// assert that there is only one group of certData found in the map
 			if (certDataList.size()!=1) Assert.fail("Error when parsing raw entitlement certificates.");
 			Map<String,String> certData = certDataList.get(0);
 			
 			// create a new EntitlementCert
 			productCerts.add(new ProductCert(rawCertificate, certData));
 		}
 		
 		return productCerts;
 
 // OLD PARSING - prone to stack overflows when calling addRegexMatchesToMap
 //		Map<String,String> regexes = new HashMap<String,String>();
 //		
 //		// abstraction field				regex pattern (with a capturing group)
 //		regexes.put("cn",					"Serial Number:\\s*([\\d\\w:]+).*(?:\\n.*?)*Subject: CN=(.+)");	// FIXME not quite right
 //		regexes.put("issuer",				"Serial Number:\\s*([\\d\\w:]+).*(?:\\n.*?)*Issuer:\\s*(.*),");
 //		regexes.put("validityNotBefore",	"Serial Number:\\s*([\\d\\w:]+).*(?:\\n.*?)*Validity[\\n\\s\\w:]*Not Before\\s*:\\s*(.*)");
 //		regexes.put("validityNotAfter",		"Serial Number:\\s*([\\d\\w:]+).*(?:\\n.*?)*Validity[\\n\\s\\w:]*Not After\\s*:\\s*(.*)");
 //		regexes.put("rawCertificate",		"Serial Number:\\s*([\\d\\w:]+).*((?:\\n.*?)*)Signature Algorithm:.*\\s+(?:([a-f]|[\\d]){2}:){10}");	// FIXME THIS IS ONLY PART OF THE CERT
 //		regexes.put("file",					"Serial Number:\\s*([\\d\\w:]+).*(?:\\n.*?)*File: (.*)");
 //		Map<String, Map<String,String>> productMap = new HashMap<String, Map<String,String>>();
 //		for(String field : regexes.keySet()){
 //			Pattern pat = Pattern.compile(regexes.get(field), Pattern.MULTILINE);
 //			addRegexMatchesToMap(pat, rawCertificates, productMap, field);
 //		}
 //		
 //		List<ProductCert> productCerts = new ArrayList<ProductCert>();
 //		for(String key : productMap.keySet()) {
 //			
 //			// convert the key inside the raw cert file (04:02:7b:dc:b7:fb:33) to a numeric serialNumber (11286372344531148)
 //			//Long serialNumber = Long.parseLong(key.replaceAll(":", ""), 16);
 //			BigInteger serialNumber = new BigInteger(key.replaceAll(":", ""),16);
 //		
 //			productCerts.add(new ProductCert(serialNumber, productMap.get(key)));
 //		}
 //		return productCerts;
 	}
 }
