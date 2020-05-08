 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.codes.marker;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.StyleElement;
 import com.google.inject.Singleton;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public class GradientClassGenerator {
 	
 	private static final String NAME_PREFIX = "GENERATED_CLASS_"; 
 	
 	private int seed = 0;
 	
 	private Map<Set<String>, String> cache = new HashMap<Set<String>, String>();
 	
 	public String getClassName(Set<String> colors) {
 		
 		String cached = cache.get(colors);
 		if (cached == null) {
 			cached = generate(colors);
 			cache.put(colors, cached);
 		}
 		
 		return cached;
 	}
 	
 	private String generate(Set<String> colors) {
 
 		String name = NAME_PREFIX + seed++;
 		
 		StringBuilder cssBuilder = new StringBuilder("."+name+" { background: linear-gradient(");
 		Iterator<String> colorsIterator = colors.iterator();
 		while(colorsIterator.hasNext()) {
 			String color = colorsIterator.next();
 			cssBuilder.append(color);
 			if (colorsIterator.hasNext()) cssBuilder.append(",");
 		}
		cssBuilder.append(") !important;}");
 		
 		Log.trace("generated class "+cssBuilder.toString());
 		
 		addStyleElement(cssBuilder.toString());
 		
 		return name;
 		
 	}
 	
 	private void addStyleElement(String css) {
 		StyleElement element = Document.get().createStyleElement();
 		element.setType("text/css");
 		element.setInnerHTML(css);
 		element.setDisabled(false);
 		Document.get().getBody().appendChild(element);
 	}
 
 }
