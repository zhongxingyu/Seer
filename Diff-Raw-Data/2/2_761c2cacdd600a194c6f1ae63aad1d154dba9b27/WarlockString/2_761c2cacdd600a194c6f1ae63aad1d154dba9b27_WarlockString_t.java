 package cc.warlock.core.client;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class WarlockString {
 
 	private StringBuffer text = new StringBuffer();
 	private ArrayList<WarlockStringStyleRange> styles = new ArrayList<WarlockStringStyleRange>();
 	
 	public class WarlockStringStyleRange {
 		public int start;
 		public int length;
 		public IWarlockStyle style;
 		
 		public WarlockStringStyleRange(int start, int length, IWarlockStyle style) {
 			this.start = start;
 			this.length = length;
 			this.style = style;
 		}
 	}
 	
 	public WarlockString() {
 	}
 	
 	public WarlockString(CharSequence text) {
 		this.text.append(text);
 	}
 	
 	public WarlockString(String text) {
 		this.text.append(text);
 	}
 	
 	public WarlockString(String text, IWarlockStyle style) {
 		this.text.append(text);
 		addStyle(style);
 	}
 	
 	public String toString() {
 		return text.toString();
 	}
 	
 	public int lastIndexOf(String str) {
 		return this.text.lastIndexOf(str);
 	}
 	
 	public void append(String text) {
 		this.text.append(text);
 	}
 	
 	public void append(WarlockString string) {
 		int charCount = text.length();
 		text.append(string.toString());
 		for(WarlockStringStyleRange range : string.getStyles()) {
 			addStyle(charCount + range.start, range.length, range.style);
 		}
 	}
 	
 	public void addStyle(IWarlockStyle style) {
 		addStyle(0, text.length(), style);
 	}
 	
 	public void addStyle(int start, int length, IWarlockStyle style) {
 		for(WarlockStringStyleRange curStyle : styles) {
 			if(curStyle.style.equals(style)) {
 				// check if the new style is contained by an old style, or is overlapping the end of an old style
 				if(start > curStyle.start && curStyle.start + curStyle.length <= start) {
 					curStyle.length = Math.max(curStyle.length, start - curStyle.start + length);
 					return;
 				}
 				//TODO check if the new style is overlapping the beginning of an old style
 			}
 		}
 		styles.add(new WarlockStringStyleRange(start, length, style));
 	}
 	
 	public List<WarlockStringStyleRange> getStyles() {
 		return styles;
 	}
 	
 	public int length() {
 		return text.length();
 	}
 	
 	public void clear() {
 		text.setLength(0);
 		styles.clear();
 	}
 	
 	public WarlockString substring(int start) {
 		return substring(start, text.length());
 	}
 	
 	public WarlockString substring(int start, int end) {
 		WarlockString substring = new WarlockString(text.substring(start, end));
 		for(WarlockStringStyleRange style : styles) {
			if(style.start + style.length > start && style.start < end) {
 				int styleLength = Math.min(style.length, end - style.start);
 				int styleStart = Math.max(0, style.start - start);
 				substring.addStyle(styleStart, styleLength, style.style);
 			}
 		}
 		return substring;
 	}
 	
 	public WarlockString[] split(String regex) {
 		return split(regex,length()); // Limit should be the number of characters in the string
 	}
 	
 	public WarlockString[] split(String regex, int limit) {
 		Pattern p = Pattern.compile(regex);
 		Matcher m = p.matcher(toString());
 		ArrayList<WarlockString> parts = new ArrayList<WarlockString>();
 		int i = 0;
 		int start = 0;
 		int end = 0;
 		WarlockString part = null;
 
 		while (m.find(start) && i < limit) {
 			end = m.start();
 			// make sure that we actually have a substring to add
 			if(end != start) {
 				part = this.substring(start, end);
 				parts.add(part);
 				++i;
 			}
 			// set the start of the next substring
 			start = m.end();
 		}
 		if ((i == 0 || !m.hitEnd()) && i < limit) {
 			if (this.length() - start >= 1) {  // Redundent if... I know. But this looks better than all on one confusing line
 				part = this.substring(start);
 				parts.add(part);
 			}
 		}
 		return parts.toArray(new WarlockString[parts.size()]);
 	}
 }
