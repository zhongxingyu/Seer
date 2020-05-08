 package de.jardas.drakensang.gui.util;
 
 import java.util.StringTokenizer;
 
 import org.apache.commons.lang.WordUtils;
 
 public class WordWrap {
	private static final String SEP = "\u0000";

 	public static String addNewlines(String in) {
 		return addNewlines(in, 80, "\n");
 	}
 
 	public static String addHtmlNewlines(String in) {
 		return "<html>" + addNewlines(in, 80, "<p>") + "</html>";
 	}
 
 	private static String addNewlines(String in, int length, String nl) {
		String src = in.replaceAll("\\\\n", SEP);
		final StringTokenizer tokens = new StringTokenizer(src, SEP, true);
 		final StringBuffer out = new StringBuffer();
 
 		while (tokens.hasMoreTokens()) {
			final String token = tokens.nextToken();
			if (SEP.equals(token)) {
				out.append(nl);
			} else {
				out.append(WordUtils.wrap(token, length, nl, false));
			}
 		}
 
 		return out.toString();
 	}
 }
