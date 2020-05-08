 package ui.util;
 
 import play.api.templates.Html;
 
 import com.google.common.base.Strings;
 import com.petebevin.markdown.MarkdownProcessor;
 
 /**
  * 
  * @author adericbourg
  * 
  */
 public final class MDHelper {
 	public static Html process(String text) {
 		if (Strings.isNullOrEmpty(text)) {
 			return Html.empty();
 		}
 		String formatted = processor().markdown(text);
 		return Html.apply(formatted);
 	}
 
 	private static MarkdownProcessor processor() {
 		return new MarkdownProcessor();
 	}
 }
