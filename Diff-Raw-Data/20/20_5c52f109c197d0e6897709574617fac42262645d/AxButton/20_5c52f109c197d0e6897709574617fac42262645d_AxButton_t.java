 
 package axirassa.webapp.mixins;
 
import lombok.Getter;
import lombok.Setter;

 import org.apache.tapestry5.MarkupWriter;
 import org.apache.tapestry5.annotations.Import;
 import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.Parameter;
 
 @Import(stylesheet = { "context:/css/axbutton.css" })
 @MixinAfter
 public class AxButton {
 
	@Parameter
	@Getter
	@Setter
	private boolean tight;


 	void beginRender (MarkupWriter writer) {
		StringBuilder sb = new StringBuilder("button");
		
		if(tight)
			sb.append(" tight");
		
		writer.element("div", "class", sb.toString());
 		writer.element("div", "class", "innerbutton");
 	}
 
 
 	void afterRender (MarkupWriter writer) {
 		writer.end(); // innerbutton
 		writer.end(); // button
 	}
 }
