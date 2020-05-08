 package de.metacoder.blog.mixins;
 
 import org.apache.tapestry5.Field;
 import org.apache.tapestry5.MarkupWriter;
 import org.apache.tapestry5.annotations.BeginRender;
 import org.apache.tapestry5.annotations.Import;
 import org.apache.tapestry5.annotations.InjectContainer;
 import org.apache.tapestry5.corelib.components.TextArea;
 
 @Import(
 		library =	{
					"context:scripts/tiny_mce/tiny_mce.js"
 					}
 		)
 public class TinyMCE {
 	
 	 	@InjectContainer
 	    private TextArea textArea;
 	
 		@BeginRender
 		public void enableTinyMCE(final MarkupWriter markupWriter){
 			markupWriter.writeRaw("<script type=\"text/javascript\">\n" +
 								"tinyMCE.init({\n" +
 								"mode : \"exact\", \n" +
 								"elements: \""+textArea.getClientId()+"\" \n" +
 								"});</script>");
 		}
 }
