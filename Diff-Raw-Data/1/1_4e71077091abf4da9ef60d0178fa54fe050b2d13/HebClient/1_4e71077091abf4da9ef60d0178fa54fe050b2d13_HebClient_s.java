 
 package org.aigps.wqgps.module.heb;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.cxf.endpoint.Client;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import com.alibaba.fastjson.JSON;
 import com.ytincl.scdp.module.simplejson.service.SimpleJSONResponse;
 
 
 @Component
 public class HebClient {
 	private final static Log log = LogFactory.getLog(HebClient.class);
 	
 	@Value("${heb.wsdl.url}")
 	private String hebWsdlUrl;
 	@Value("${heb.wsdl.uid}")
 	private String hebWsdlUid;
 	@Value("${heb.wsdl.pwd}")
 	private String hebWsdlPwd;
 	private Client client;
 	
 	private String param = "{u:'%s',p:'%s',tp:'SortingMachines.UpdateOrderList',postmantel:'%s'}";
 
 	/**
 	 * {
 		"rate":"0/2",
 		"tp":2,
 		"itemsresult":[{data}...],
 		"rt":"OK",
 		"itemstitle":[[key,value]...],
 		"showtitle":[key...],
 		"rc":"1",
 		"otherinfo":""
 		}
 	 * @param phone
 	 * @return
 	 * @throws Exception
 	 */
 	public Object getYdRate(String phone) throws Exception {
 		try {
 //			phone = "15104566666";
 			if(client == null) {
 				client = DynamicClientFactory.newInstance().createClient(hebWsdlUrl);            
 			}
 			String inputStr = String.format(param, hebWsdlUid,hebWsdlPwd,phone);
             log.error("˵"+inputStr);
             
             Object[] out = client.invoke("invoke", new Object[]{inputStr});
             SimpleJSONResponse res = (SimpleJSONResponse) out[0];
             log.error("ص˵Ϣ"+res.toString());
 			return JSON.parse(res.toString());
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			return null;
 		}
 	}
 	
 }
 
