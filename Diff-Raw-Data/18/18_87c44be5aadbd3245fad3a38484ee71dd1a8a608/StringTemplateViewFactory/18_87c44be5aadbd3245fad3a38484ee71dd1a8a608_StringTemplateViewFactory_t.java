 package org.twuni.view;
 
 import java.util.Map;
 
 import org.antlr.stringtemplate.StringTemplateGroup;
 
public class StringTemplateViewFactory implements ViewFactory {
 
 	private final StringTemplateGroup group;
 
 	public StringTemplateViewFactory( String templateDirectoryName ) {
 		this.group = new StringTemplateGroup( templateDirectoryName, getClass().getClassLoader().getResource( templateDirectoryName ).getPath() );
 	}
 
	@Override
 	public String createView( String name ) {
 		return new StringTemplateView( group, name ).toString();
 	}
 
	@Override
 	public String createView( String name, Map<String, ?> model ) {
 		return new StringTemplateView( group, name, model ).toString();
 	}
 
 }
