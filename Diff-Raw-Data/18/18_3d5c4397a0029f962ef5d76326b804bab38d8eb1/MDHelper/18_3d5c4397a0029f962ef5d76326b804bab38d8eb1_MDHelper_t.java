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

	private static final String[] BAD_HTML = new String[] { "&", "\"", "<", ">" };
	private static final String[] GOOD_HTML = new String[] { "&amp;", "&quot;",
			"&lt;", "&gt;" };

 	public static Html process(String text) {
 		if (Strings.isNullOrEmpty(text)) {
 			return Html.empty();
 		}

		// Escape HTML chars since Markdown leaves them.
		text = replaceHtmlChars(text);

 		String formatted = processor().markdown(text);
 		return Html.apply(formatted);
 	}
 
 	private static MarkdownProcessor processor() {
 		return new MarkdownProcessor();
 	}

	private static String replaceHtmlChars(String text) {
		for (int i = 0; i < BAD_HTML.length; i++) {
			text = text.replace(BAD_HTML[i], GOOD_HTML[i]);
		}
		return text;
	}
 }
