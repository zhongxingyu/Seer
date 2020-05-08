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
 		
 		values.put("element",binding.getElement());
		values.put("function",BinderUtils.getTargetAccessString(target, ctx, true));
 
 		ret = JavascribeUtils.basicTemplating("js-mvvm-button-onclick-binding.txt", values);
 
 		return ret;
 	}
 	
 }
