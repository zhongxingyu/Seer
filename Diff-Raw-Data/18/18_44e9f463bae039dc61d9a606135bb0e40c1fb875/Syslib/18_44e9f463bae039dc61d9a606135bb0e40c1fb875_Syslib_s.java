 package com.illmeyer.polygraph.syslib;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.illmeyer.polygraph.core.interfaces.Module;
 import com.illmeyer.polygraph.syslib.directives.BinaryPartDirective;
 import com.illmeyer.polygraph.syslib.directives.TextPartDirective;
 
 public class Syslib implements Module {
 
 	@Override
 	public void initialize() {
 		System.out.println("Initializing Syslib");
 	}
 
 	@Override
 	public Map<String, Object> createContext() {
 		Map<String,Object> result = new HashMap<String, Object>();
 		result.put("binarypart", new BinaryPartDirective());
 		result.put("textpart", new TextPartDirective());
 		return result;
 	}
 
 	@Override
 	public void destroy() {
		// TODO Auto-generated method stub
 	}
 
 }
