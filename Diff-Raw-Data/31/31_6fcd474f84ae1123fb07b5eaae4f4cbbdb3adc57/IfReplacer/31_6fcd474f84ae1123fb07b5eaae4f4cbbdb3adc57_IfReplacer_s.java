 package uk.co.brotherlogic.jarpur.replacers;
 
 import java.util.Map;
 
 public class IfReplacer extends Replacer {
 
 	Replacer elemts;
 	String param;
 
 	public IfReplacer(String param, Replacer elements) {
 		elemts = elements;
 		this.param = param;
 
 		System.err.println("IF: " + param);
 
 	}
 
 	@Override
 	public String process(Object ref, Map<String, Object> objectMap) {
 
 		System.out.println("IF TO RESOLVE = "
				+ param.substring(5, param.length() - 2));
 		Object obj = resolve(param.substring(5, param.length() - 2), objectMap);
		System.err.println("IFOBJ = " + obj);
 
 		return super.process(ref, objectMap);
 	}
 
 	@Override
 	public String toString() {
 		return "IF: " + param;
 	}
 
 }
