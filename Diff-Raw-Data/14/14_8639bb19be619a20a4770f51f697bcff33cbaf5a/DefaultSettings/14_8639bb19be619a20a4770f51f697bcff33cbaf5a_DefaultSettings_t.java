 package org.cee.highlighter;
 
 import java.net.URL;
 
 import org.cee.highlighter.ContentHighlighter.Settings;
 
 public class DefaultSettings extends Settings {
 	
	public static final String DEFAULT_HEADER_TEMPLATE = "classpath:/org/cee/highlighter/defaultHeaderTemplate.html";
 	public static final boolean DEFAULT_HIGHLIGHT_CONTENT_BLOCK = true;
	public static final String DEFAULT_CONTENT_BLOCK_TEMPLATE = "classpath:/org/cee/highlighter/defaultContentBlockTemplate.html";
 	public static final boolean DEFAULT_REWRITE_URLS = true;
 	public static final boolean DEFAULT_SHOW_BLOCK_METADATA = true;
	public static final String DEFAULT_METADATA_TEMPLATE = "classpath:/org/cee/highlighter/defaultMetadataTemplate.html";
	public static final String DEFAULT_METADATA_CONTAINER_TEMPLATE = "classpath:/org/cee/highlighter/defaultMetadataContainerTemplate.html";
	public static final String DEFAULT_METADATA_ICON_TEMPLATE = "classpath:/org/cee/highlighter/defaultMetadataIconTemplate.html";
	
	private DefaultSettings() {}
 	
 	public static Settings createDefaultSettings(URL baseUrl) {
 		return  new Settings(
 				DEFAULT_HEADER_TEMPLATE, 
 				DEFAULT_HIGHLIGHT_CONTENT_BLOCK, 
 				DEFAULT_CONTENT_BLOCK_TEMPLATE, 
 				baseUrl, 
 				DEFAULT_REWRITE_URLS, 
 				DEFAULT_SHOW_BLOCK_METADATA, 
 				DEFAULT_METADATA_TEMPLATE, 
 				DEFAULT_METADATA_CONTAINER_TEMPLATE, 
 				DEFAULT_METADATA_ICON_TEMPLATE);
 	}
 }
