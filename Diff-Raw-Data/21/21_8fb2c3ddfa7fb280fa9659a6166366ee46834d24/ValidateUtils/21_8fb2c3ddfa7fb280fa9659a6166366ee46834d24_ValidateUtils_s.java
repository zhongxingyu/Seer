 package com.cnnic.whois.util.validate;
 
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.cnnic.whois.util.IpUtil;
 
 public class ValidateUtils {
 
 	public static boolean validateDomainName(String domainName){
 		if (!isCommonInvalidStr(domainName)){
 			return false;
 		}
 		if(domainName.length() > 255){
 			return false;
 		}
 		if(!domainName.startsWith("xn--") && !verifyNameServer(domainName)){
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Verifying the String parameters
 	 * 
 	 * @param parm
 	 * @return The correct parity returns true, failure to return false
 	 */
 	public static boolean isCommonInvalidStr(String parm) {
 		String strReg = "^[\u0391-\uFFE5a-zA-Z\\d\\*]{1}([\u0391-\uFFE5\\w\\-\\.\\_\\*]*)$";
 		if (StringUtils.isBlank(parm)){
 			return false;
 		}
 		return parm.matches(strReg);
 	}
 	
 	public static boolean containPunctuation(String str) {
 		String strReg = "[`~!@#%^&\\*()+=|\\\\;',.\\/?]";
 		if (StringUtils.isBlank(strReg)){
 			return false;
 		}
 		return str.matches(strReg);
 	}
 	
 	/**
 	 * Verifying the NameServer parameters
 	 * 
 	 * @param queryPara
 	 * @return The correct parity returns true, failure to return false
 	 */
 	public static boolean verifyNameServer(String queryPara) {
 		if(StringUtils.isBlank(queryPara)){
 			return false;
 		}
 //		if (!isCommonInvalidStr(queryPara)){
 //			return false;
 //		}
 		if(queryPara.length() > 255){
 			return false;
 		}
 		return true;
 //		String fuzzyReg = "^(?!-.)(?!.*?-$)((\\*)?[\u0391-\uFFE50-9a-zA-Z-]{0,62}(\\*)?)+(\\.[\u0391-\uFFE50-9a-zA-Z-]{0,62}\\*?)*$";
 //		if (queryPara.matches(fuzzyReg))
 //			return true;
 //		return false;
 	}
 	
 	public static boolean verifyFuzzyDomain(String queryPara) {
 		if(StringUtils.isBlank(queryPara)){
 			return false;
 		}
 //		if (!isCommonInvalidStr(queryPara)){
 //			return false;
 //		}
 		if(queryPara.length() > 255){
 			return false;
 		}
 		String[] labels = queryPara.split(".");
 		for(String label:labels){
 			if(label.length()>62){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Verifying the IP parameters
 	 * 
 	 * @param ipStr
 	 * @param ipLengthStr
 	 * @return The correct parity returns true, failure to return false
 	 */
 	public static boolean verifyIP(String ipStr, String ipLengthStr) {
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
 	 * @param address
 	 * @return The correct parity returns true, failure to return false
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
 	 * transform Long to IP address
 	 * @param map
 	 * @return
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
 }
