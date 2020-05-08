 package org.eweb4j.mvc;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpSession;
 
 /**
  * TODO
  * @author weiwei l.weiwei@163.com
  * @date 2013-4-3 上午08:22:00
  */
 public class HttpSessionProxy {
 
 	private HttpSession session = null;
 	private Map<String, Object> attrs = new HashMap<String, Object>();
 	
 	public HttpSessionProxy(HttpSession session){
 		this.session = session;
 		
 		Enumeration<String> names = session.getAttributeNames();
 		while (names.hasMoreElements()){
 			String name = names.nextElement();
 			Object val = session.getAttribute(name);
 			attrs.put(name, val);
 		}
 	}
 	
 	public Object attr(String name){
 		return session.getAttribute(name);
 	}
 	
 	public Map<String, Object> attrs(){
 		return attrs;
 	}
 	
 }
