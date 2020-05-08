 package org.eweb4j.mvc;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 
 /**
  * TODO
  * @author weiwei l.weiwei@163.com
  * @date 2013-4-3 上午08:22:00
  */
 public class ServletContextProxy {
 
 	private ServletContext ctx = null;
 	private Map<String, Object> attrs = new HashMap<String, Object>();
 	
 	public ServletContextProxy(ServletContext ctx){
 		this.ctx = ctx;
 		
 		Enumeration<String> names = ctx.getAttributeNames();
 		while (names.hasMoreElements()){
 			String name = names.nextElement();
 			Object val = ctx.getAttribute(name);
 			attrs.put(name, val);
 		}
 	}
 	
 	public Object attr(String name){
 		return ctx.getAttribute(name);
 	}
 	
 	public Map<String, Object> attrs(){
 		return attrs;
 	}
 	
 }
