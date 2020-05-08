 package fat;
 
 import fit.*;
 
 public class TextToHtmlFixture extends ColumnFixture {
 	public String Text;
 
 	public String HTML() {
 		Text = unescapeAscii(Text);
 		return Fixture.escape(Text);
 	}
 
 	private String unescapeAscii(String text) {
 		text = text.replaceAll("\\\\n", "\n");
 		text = text.replaceAll("\\\\r", "\r");
 		return text;
 	}
 }
