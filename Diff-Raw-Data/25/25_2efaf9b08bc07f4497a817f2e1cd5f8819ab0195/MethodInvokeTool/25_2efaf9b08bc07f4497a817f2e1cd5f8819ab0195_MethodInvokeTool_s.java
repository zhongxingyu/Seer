 package io.github.qingtian.freecall;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import EcpOpen.constant.Constants;
 import EcpOpen.util.date.DateUtil;
 import EcpOpen.util.encoding.DES3;
 import EcpOpen.util.encoding.EncryptUtil;
 
 public class MethodInvokeTool {
 
 	private static final Log log = LogFactory.getLog(MethodInvokeTool.class);
 
 	private static final MethodInvokeTool INSTANCE = new MethodInvokeTool();
 	private Map<String, String> paramMap = new HashMap<String, String>();
 
 	private MethodInvokeTool() {
 		Constants.spid = ConfigTool.getSpid();
 		Constants.serviceKey = ConfigTool.getServicekey();
 		Constants.appid = ConfigTool.getAppid();
 		Constants.appkey = ConfigTool.getAppkey();
 		Constants.type = "0";
 	}
 
 	public static MethodInvokeTool getInstance() {
 		return INSTANCE;
 	}
 
 	public void clear() {
 		paramMap.clear();
 	}
 
 	private String generalSignature(String spid, String serviceKey,
 			String appID, String appKey, String timeStamp) {
 		StringBuffer stringBuffer = new StringBuffer();
 		stringBuffer.append(spid).append(serviceKey).append(appID)
 				.append(appKey).append(timeStamp);
 		String signature = EncryptUtil.Encrypt(stringBuffer.toString())
 				.toLowerCase();
 		return signature;
 	}
 
 	private String buildXML(Map<String, String> parameterMap,
 			String sessionToken, OpenServiceType ost) throws Exception {
 		StringBuilder xmlData = new StringBuilder();
 		xmlData.append("<?xml version=" + "\"1.0\"" + " encoding="
 				+ "\"utf-8\"" + "?>" + "\r");
 		xmlData.append("<xml>" + "\r");
 
 		if (sessionToken == null || sessionToken == "") {
 			String SiCode = DES3.encrypt(Constants.spid + Constants.serviceKey,
 					Constants.serviceKey);
 			xmlData.append("<SiCode>" + SiCode + "</SiCode>" + "\r");
 		} else {
 			xmlData.append("<SessionToken>" + sessionToken + "</SessionToken>"
 					+ "\r");
 		}
 
 		String AppStr = Constants.appid + Constants.appkey;
 		String AppCode = DES3.encrypt(AppStr, Constants.appkey);
 		xmlData.append("<AppCode>" + AppCode + "</AppCode>" + "\r");
 		xmlData.append("<appID>" + Constants.appid + "</appID>" + "\r");
 		String timeStamp = DateUtil.generateTimeStamp();
 		xmlData.append("<TimeStamp>" + timeStamp + "</TimeStamp>" + "\r");
 		;
 
 		String sign = generalSignature(Constants.spid, Constants.serviceKey,
 				Constants.appid, Constants.appkey, timeStamp);
 		xmlData.append("<sign>" + sign + "</sign>" + "\r");
 		xmlData.append("<type>" + Constants.type + "</type>" + "\r");
 		xmlData.append("<no>" + UUID.randomUUID().toString() + "</no>" + "\r");
 		Set<String> nameSet = parameterMap.keySet();
 
 		if (ost == OpenServiceType.ECP) {
 			for (String name : nameSet) {
 				xmlData.append("<" + name + ">" + parameterMap.get(name) + "</"
 						+ name + ">" + "\r");
 			}
 		} else if (ost == OpenServiceType.EXTEND) {
 			xmlData.append("<function>" + parameterMap.get("function")
 					+ "</function>" + "\r");
 
 			xmlData.append("<RequestData>" + "\r");
 			for (String name : nameSet) {
 				xmlData.append("<" + name + ">" + parameterMap.get(name) + "</"
 						+ name + ">" + "\r");
 			}
 			xmlData.append("</RequestData>" + "\r");
 		}
 
 		xmlData.append("</xml>");
 
 		log.info("open xml data：");
 		log.info(xmlData.toString());
 
 		return xmlData.toString();
 	}
 
 	private String buildRequestData(String xml, String serviceTag)
 			throws Exception {
 		String xmlEncrypt = "";
 		StringBuilder soapRequestData = new StringBuilder();
 		xmlEncrypt = DES3.encrypt(xml).replace("\r", "");
 		int byteNum = xmlEncrypt.trim().getBytes().length;
 		soapRequestData.append("<?xml version=" + "\"1.0\"" + " encoding="
 				+ "\"utf-8\"" + "?>");
 		soapRequestData
 				.append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">");
 		soapRequestData.append("<soap12:Body>");
 		soapRequestData.append("<" + serviceTag
 				+ " xmlns=\"http://ws.open.jtang.com.cn\">");
 		soapRequestData.append("<byteNum>" + byteNum + "</byteNum>");
 		soapRequestData.append("<xmlString>" + xmlEncrypt.trim()
 				+ "</xmlString>");
 		soapRequestData.append("</" + serviceTag + ">");
 		soapRequestData.append("</soap12:Body>");
 		soapRequestData.append("</soap12:Envelope>");
 
 		log.info("soap message：");
 		log.info(soapRequestData.toString());
 
 		return soapRequestData.toString();
 	}
 
 	public String[] call(String ecpaccount, String caller, String called)
 			throws Exception {
 		Constants.extendUrl = ConfigTool.getExtendurl();
 		paramMap.clear();
 		paramMap.put("Account", ecpaccount);
 		paramMap.put("CallingParty", caller);
 		paramMap.put("CalledParty", called);
 		String xml = buildXML(paramMap, Constants.sessiontoken,
 				OpenServiceType.EXTEND);
 		String soapRequestData = buildRequestData(xml, "ExtendService");
 		return HttpTool.getInstance().postSoap(Constants.extendUrl,
 				soapRequestData);
 	}
 
 	public static void main(String[] args) throws Exception {
 		String ecpaccount = "";
 		String caller = "";
 		String called = "";
 		String[] rtn = MethodInvokeTool.getInstance().call(ecpaccount, caller,
 				called);
 		System.out.println(rtn[0]);
 		System.out.println(rtn[1]);
 	}
 
 }
