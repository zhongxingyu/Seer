 package org.twuni.view;
 
 import java.util.Map;
 
 import org.antlr.stringtemplate.StringTemplate;
 import org.antlr.stringtemplate.StringTemplateGroup;
 
 public class StringTemplateView {
 
 	private final StringTemplate template;
 
 	public StringTemplateView( StringTemplateGroup parent, String name ) {
		this.template = parent.lookupTemplate( name );
 	}
 
 	public StringTemplateView( StringTemplateGroup parent, String name, Map<String, ?> model ) {
		this( parent, name );
 		template.setAttributes( model );
 	}
 
 	@Override
 	public String toString() {
 		return template.toString();
 	}
 
 }
