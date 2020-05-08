 package org.twuni.view;
 
import java.util.HashMap;
 import java.util.Map;
 
 import org.antlr.stringtemplate.StringTemplate;
 import org.antlr.stringtemplate.StringTemplateGroup;
 
 public class StringTemplateView {
 
 	private final StringTemplate template;
 
 	public StringTemplateView( StringTemplateGroup parent, String name ) {
		this( parent, name, new HashMap<String, Object>() );
 	}
 
 	public StringTemplateView( StringTemplateGroup parent, String name, Map<String, ?> model ) {
		this.template = parent.lookupTemplate( name );
 		template.setAttributes( model );
 	}
 
 	@Override
 	public String toString() {
 		return template.toString();
 	}
 
 }
