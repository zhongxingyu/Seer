 package de.metacoder.blog.components;
 
 import org.apache.tapestry5.*;
 import org.apache.tapestry5.annotations.*;
 import org.apache.tapestry5.ioc.annotations.*;
 import org.apache.tapestry5.BindingConstants;
 import org.apache.tapestry5.SymbolConstants;
 
 /**
  * Layout component for pages of application blog.
  */
@Import(
		library =	{
					"context:scripts/syntaxhighlight/shCore.js",
					"context:scripts/syntaxhighlight/shBrushPlain.js",
					"context:scripts/syntaxhighlight/shBrushBash.js",
					"context:scripts/syntaxhighlight/shBrushCss.js",
					"context:scripts/syntaxhighlight/shBrushJava.js",
					"context:scripts/syntaxhighlight/shBrushSql.js",
					"context:scripts/syntaxhighlight/shBrushXml.js",
					"context:scripts/syntaxhighlight/shBrushDiff.js",
					"context:scripts/syntaxhighlight/shBrushJScript.js",
					"context:scripts/syntaxhighlight/shBrushScala.js",
					"context:scripts/syntaxhighlight/shBrushCpp.js"
					},
		stylesheet = {
					"context:css/style.css",
					"context:css/syntaxhighlight/shCoreEclipse.css"
					})
 public class Layout {
 
 	@Property
 	@Inject
 	@Symbol(SymbolConstants.TAPESTRY_VERSION)
 	private String tapestryVersion;
	
 	@Property
 	@Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
 	private String title;
 
 	@Property
 	private String pageName;
 
 	@Property
 	@Parameter(defaultPrefix = BindingConstants.LITERAL)
 	private String sidebarTitle;
 
 	@Property
 	@Parameter(defaultPrefix = BindingConstants.LITERAL)
 	private Block sidebar;
 
 	@Inject
 	private ComponentResources resources;
 
 	@Property
 	@Inject
 	@Symbol(SymbolConstants.APPLICATION_VERSION)
 	private String appVersion;
 
 	public String getClassForPageName() {
 		return resources.getPageName().equalsIgnoreCase(pageName) ? "current_page_item"
 				: null;
 	}
 
 	public String[] getPageNames() {
 		return new String[] { "Index", "About", "Contact", "EditEntry" };
 	}
 }
