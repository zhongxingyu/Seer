 
 package axirassa.webapp.mixins;
 
 import org.apache.tapestry5.MarkupWriter;
 import org.apache.tapestry5.annotations.Import;
 
 @Import(stylesheet = { "context:/css/axbutton.css" })
 public class AxButton {
 
 	void beginRender (MarkupWriter writer) {
 		writer.element("div", "class", "button");
 		writer.element("div", "class", "innerbutton");
 	}
 
 
 	void afterRender (MarkupWriter writer) {
 		writer.end(); // innerbutton
 		writer.end(); // button
 	}
 }
