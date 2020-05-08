 package ru.xrm.app.config;
 
 import java.util.List;
 
 import ru.xrm.app.evaluators.ElementEvaluator;
 import ru.xrm.app.transformers.PropertyTransformer;
 import ru.xrm.app.walkers.ElementWalker;
 
 public class Entry {
 	String key;
 	String cssQuery;
 	List<CSSQueryArg> cssQueryArgs;
 	ElementWalker elementWalker;
 	ElementEvaluator elementEvaluator;
 	PropertyTransformer propertyTransformer;
 	
 	public Entry(String key, String cssQuery, List<CSSQueryArg> cssQueryArgs, 
 			ElementWalker elementWalker, ElementEvaluator elementEvaluator,PropertyTransformer propertyTransformer){
 		this.key = key;
 		this.cssQuery = cssQuery;
 		this.cssQueryArgs = cssQueryArgs;
 		this.elementWalker = elementWalker;
 		this.elementEvaluator = elementEvaluator;
 		this.propertyTransformer = propertyTransformer;
 	}
 
 	public String getKey() {
 		return key;
 	}
 
 	public void setKey(String key) {
 		this.key = key;
 	}
 
 	public String getCssQuery() {
 		String result;
		if (cssQueryArgs!=null && cssQueryArgs.size()>0){
 			result=String.format(cssQuery, cssQueryArgs.toArray());
 		}else{
 			result=cssQuery;
 		}
 		return result;
 	}
 
 	public void setCssQuery(String cssQuery) {
 		this.cssQuery = cssQuery;
 	}
 
 	public List<CSSQueryArg> getCssQueryArgs() {
 		return cssQueryArgs;
 	}
 
 	public void setCssQueryArgs(List<CSSQueryArg> cssQueryArgs) {
 		this.cssQueryArgs = cssQueryArgs;
 	}
 
 	public ElementWalker getElementWalker() {
 		return elementWalker;
 	}
 
 	public void setElementWalker(ElementWalker elementWalker) {
 		this.elementWalker = elementWalker;
 	}
 
 	public ElementEvaluator getElementEvaluator() {
 		return elementEvaluator;
 	}
 
 	public void setElementEvaluator(ElementEvaluator elementEvaluator) {
 		this.elementEvaluator = elementEvaluator;
 	}
 
 	public PropertyTransformer getPropertyTransformer() {
 		return propertyTransformer;
 	}
 
 	public void setPropertyTransformer(PropertyTransformer propertyTransformer) {
 		this.propertyTransformer = propertyTransformer;
 	}
 	
 }
