 package org.eweb4j.mvc;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 
 /**
  * TODO
  * @author weiwei l.weiwei@163.com
  * @date 2013-4-3 上午08:22:00
  */
 public class CookieProxy {
 
 	private Map<String, Object> attrs = new HashMap<String, Object>();
 	
 	public CookieProxy(Cookie[] cookies){
 		for (Cookie c : cookies){
 			String name = c.getName();
 			String val = c.getValue();
 			attrs.put(name, val);
 		}
 	}
 	
 	public Map<String, Object> attrs(){
 		return attrs;
 	}
 	
 }
