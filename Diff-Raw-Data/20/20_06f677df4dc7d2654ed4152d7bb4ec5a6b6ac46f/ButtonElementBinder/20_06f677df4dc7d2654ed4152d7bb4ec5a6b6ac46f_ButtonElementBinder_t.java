 package net.sf.javascribe.patterns.js.page.elements;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.JavascribeUtils;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.patterns.js.page.Binding;
 
 @Scannable
 @ElementBinder(elementType="button")
 public class ButtonElementBinder extends InputElementBinder {
 
 	@ElementBinding(bindingType="onclick")
 	public String buttonOnclickBinding(Binding binding,ElementBinderContext ctx) throws JavascribeException {
 		String ret = null;
 		Map<String,String> values = new HashMap<String,String>();
 		String target = binding.getTarget();
		String fn = BinderUtils.getTargetAccessString(target, ctx, true);
 		
		if (fn.endsWith("()")) {
			fn = fn.substring(0, fn.length()-2);
		}
 		values.put("element",binding.getElement());
		values.put("function",fn);
 
 		ret = JavascribeUtils.basicTemplating("js-mvvm-button-onclick-binding.txt", values);
 
 		return ret;
 	}
 	
 }
