 package com.cnnic.whois.util.validate;
 
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.cnnic.whois.util.IdnaUtil;
 import com.cnnic.whois.util.IpUtil;
 import com.google.common.net.InetAddresses;
 
 /***
  * Utils for domain and IP validation 
  *
  */
 public class ValidateUtils {
 	public static final String ACE_PREFIX = "xn--";
 	public static final String ACE_PREFIX_INSIDE = ".xn--";
 	public static final String ASTERISK = "*";
 	private static final String VALID_IDNA_CHAR = "a";
 	public static final int MAX_DOMAIN_LENGTH_WITHOUT_LAST_DOT = 253;
 	public static final int MIN_DOMAIN_LENGTH_WITHOUT_LAST_DOT = 3;
 	
 	/**
 	 * validate domain lenth,length is without last dot
 	 * @param domainWithoutLastDot:domain without last dot
 	 * @return true if valid,false if not
 	 */
 	private static boolean validateDomainLenth(String domainWithoutLastDot){
 		if(StringUtils.isBlank(domainWithoutLastDot)){
 			return false;
 		}
 		if(domainWithoutLastDot.length() < MIN_DOMAIN_LENGTH_WITHOUT_LAST_DOT
 				|| domainWithoutLastDot.length() > MAX_DOMAIN_LENGTH_WITHOUT_LAST_DOT){
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * validate domain puny name is valid idna
 	 * @param domainName 
 	 * @return true if is valid idna,false if not
 	 */
 	public static boolean validateDomainNameIsValidIdna(String domainName){
 		if(StringUtils.isBlank(domainName) || !domainName.contains(".")){
 			return false;
 		}
 		if(!domainName.startsWith(ACE_PREFIX) && isLdh(domainName)){
 			return true;
 		}
 		domainName = deleteLastPoint(domainName);
 		if(!validateDomainLenth(domainName)){
 			return false;
 		}
 		return IdnaUtil.isValidIdn(domainName);
 	}
 
 	/**
 	 * Verifying the String parameters
 	 * 
 	 * @param String parm
 	 * @return The correct param returns true, failure to return false
 	 */
 	public static boolean isCommonInvalidStr(String parm) {
 		String strReg = "^[\u0391-\uFFE5a-zA-Z\\d\\*]{1}([\u0391-\uFFE5\\w\\-\\.\\_\\*]*)$";
 		if (StringUtils.isBlank(parm)){
 			return false;
 		}
 		return parm.matches(strReg);
 	}
 	
 	/**
 	 * check is ldh domain:
 	 * can only contain letter/digit/-,and * for fuzzy query
 	 * can't begin or end with -
 	 * domain length:[1-255]
 	 * label length:[1-63]
 	 * can't contain continuous *:**
 	 * @param domain
 	 * @return true if is,false if not
 	 */
 	public static boolean isFuzzyLdh(String domain) {
 		if(StringUtils.isBlank(domain)){
 			return false;
 		}
		if(domain.indexOf("--") == 2){//'--' can't be third
			return false;
		}
 		domain = deleteLastPoint(domain);
		String domainWithoutLastPoint = deleteLastPoint(domain);
		if(!validateDomainLenth(domainWithoutLastPoint)){
 			return false;
 		}
 		String ldhReg = "^(?!-)(?!.*?-$)([0-9a-zA-Z][0-9a-zA-Z-\\*]{0,62})(\\.[0-9a-zA-Z-\\*]{1,63})*$";
 		if (domain.matches(ldhReg)){
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * is valid ldh
 	 * @param domain
 	 * @return boolean
 	 */
 	public static boolean isLdh(String domain) {
 		if(StringUtils.isBlank(domain)){
 			return false;
 		}
		if(domain.indexOf("--") == 2){//'--' can't be third
			return false;
		}
		String domainWithoutLastPoin = deleteLastPoint(domain);
		if(!validateDomainLenth(domainWithoutLastPoin)){
 			return false;
 		}
 		String ldhReg = "^(?!-)(?!.*?-$)([0-9a-zA-Z][0-9a-zA-Z-]{0,62}\\.)+([0-9a-zA-Z][0-9a-zA-Z-]{0,62})?$";
 		if (domain.matches(ldhReg)){
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * validate fuzzy domain :
 	 * valid : cnnic*.cn, xn--abc*.cn, abc.xn--a*.cn,
 	 * invalid : [U-label].xn--abc*.cn
 	 * @param domain
 	 * @return
 	 */
 	public static boolean validateFuzzyDomain(String domain) {
 		if(StringUtils.isBlank(domain)){
 			return false;
 		}
		String domainWithoutLastPoint = deleteLastPoint(domain);
 		if(!validateDomainLenth(domain)){
 			return false;
 		}
 		if(domain.contains("**")){
 			return false;
 		}
 		if(isFuzzyLdh(domain)){
 			return true;
 		}
		String domainWithoutAsterisk = domainWithoutLastPoint.replaceAll("\\*", VALID_IDNA_CHAR);
 		return IdnaUtil.isValidIdn(domainWithoutAsterisk);
 	}
 	
 	/**
 	 * Contain Punctuation in a str
 	 * @param String str
 	 * @return boolean
 	 */
 	public static boolean containPunctuation(String str) {
 		String strReg = "[`~!@#%^&\\*()+=|\\\\;',.\\/?]";
 		if (StringUtils.isBlank(strReg)){
 			return false;
 		}
 		return str.matches(strReg);
 	}
 	
 	/**
 	 * verify ip
 	 * @param ipStr
 	 * @return true if valid,false if not
 	 */
 	public static boolean verifyIP(String ipStr) {
 		String defaultIpLenthStr = "0";
 		return verifyIP(ipStr,defaultIpLenthStr);
 	}
 	
 	/**
 	 * Verifying the IP parameters
 	 * 
 	 * @param String ipStr
 	 * @param String ipLengthStr
 	 * @return The correct ipstr returns true, failure to return false
 	 */
 	public static boolean verifyIP(String ipStr, String ipLengthStr) {
 		if(!InetAddresses.isInetAddress(ipStr)){
 			return false;
 		}
 		boolean isIpV4 = isIpv4(ipStr);
 		boolean isIpV6 = isIPv6(ipStr);
 		if(!isIpV4 && ! isIpV6){
 			return false;
 		}
 		if (!ipLengthStr.matches("^[0-9]*$")){
 			return false;
 		}
 		if(Integer.parseInt(ipLengthStr) < 0){
 			return false;
 		}
 		if(isIpV4){
 			return Integer.parseInt(ipLengthStr) <= 32;
 		}else{
 			return Integer.parseInt(ipLengthStr) <= 128;
 		}
 	}
 	
 	/**
 	 * Verifying the IPv4 parameters
 	 * @param String address
 	 * @return The correct ipv4 returns true, failure to return false
 	 */
 	public static boolean isIpv4(String address) {
 		String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." 
                 + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." 
                 + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." 
                 + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$"; 
 		if (address.matches(regex))
 			return true;
 		return false;
 	}
 	
 	/**
 	 * Verifying the IPv6 parameters
 	 * 
 	 * @param String address
 	 * @return The correct ipv6 returns true, failure to return false
 	 */
 	public static boolean isIPv6(String address) {
 		boolean result = false;
 		String regHex = "(\\p{XDigit}{1,4})";
 
 		String regIPv6Full = "^(" + regHex + ":){7}" + regHex + "$";
 
 		String regIPv6AbWithColon = "^(" + regHex + "(:|::)){0,6}" + regHex
 				+ "$";
 		String regIPv6AbStartWithDoubleColon = "^(" + "::(" + regHex
 				+ ":){0,5}" + regHex + ")$";
 		String regIPv6 = "^(" + regIPv6Full + ")|("
 				+ regIPv6AbStartWithDoubleColon + ")|(" + regIPv6AbWithColon
 				+ ")$";
 
 		if (address.indexOf(":") != -1) {
 			if (address.length() <= 39) {
 				String addressTemp = address;
 				int doubleColon = 0;
 				if (address.equals("::"))
 					return true;
 				while (addressTemp.indexOf("::") != -1) {
 					addressTemp = addressTemp
 							.substring(addressTemp.indexOf("::") + 2,
 									addressTemp.length());
 					doubleColon++;
 				}
 				if (doubleColon <= 1) {
 					result = address.matches(regIPv6);
 				}
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * transform map of Long to IP address
 	 * @param map
 	 * @return map of IP range
 	 */
 	public static Map<String, Object> longToIP(Map<String, Object> map) {
 		Object ipversion = map.get("IP Version");
 
 		String startHightAddress = map.get("StartHighAddress").toString();
 		String startLowAddress = map.get("StartLowAddress").toString();
 		String endHighAddress = map.get("EndHighAddress").toString();
 		String endLowAddress = map.get("EndLowAddress").toString();
 
 		map.remove("StartHighAddress");
 		map.remove("StartLowAddress");
 		map.remove("EndHighAddress");
 		map.remove("EndLowAddress");
 		String startAddress = "";
 		String endAddress = "";
 		if (ipversion != null) {
 			if (ipversion.toString().indexOf("v6") != -1) {
 				startAddress = IpUtil.ipV6ToString(
 						Long.parseLong(startHightAddress),
 						Long.parseLong(startLowAddress));
 				endAddress = IpUtil.ipV6ToString(
 						Long.parseLong(endHighAddress),
 						Long.parseLong(endLowAddress));
 			} else {
 				startAddress = IpUtil.longtoipV4(Long
 						.parseLong(startLowAddress));
 				endAddress = IpUtil
 						.longtoipV4(Long.parseLong(endLowAddress));
 			}
 			map.put("Start Address", startAddress);
 			map.put("End Address", endAddress);
 		}
 		return map;
 	}
 	
 	/**
 	 * remove the last . in paraStr
 	 * @param paramStr
 	 * @return 
 	 */
 	public static String deleteLastPoint(String paramStr) {
 		if(StringUtils.isBlank(paramStr)){
 			return paramStr;
 		}
 		if(paramStr.length()<=1 || ! paramStr.endsWith(".")){
 			return paramStr;
 		}
 		return paramStr.substring(0, paramStr.length() - 1);
 	}
 }
