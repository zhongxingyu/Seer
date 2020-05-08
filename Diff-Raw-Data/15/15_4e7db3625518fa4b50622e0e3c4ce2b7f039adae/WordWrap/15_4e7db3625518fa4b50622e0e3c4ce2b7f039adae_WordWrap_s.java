 package de.jardas.drakensang.gui.util;
 
 import java.util.StringTokenizer;
 
 import org.apache.commons.lang.WordUtils;
 
 public class WordWrap {
 	public static String addNewlines(String in) {
 		return addNewlines(in, 80, "\n");
 	}
 
 	public static String addHtmlNewlines(String in) {
 		return "<html>" + addNewlines(in, 80, "<p>") + "</html>";
 	}
 
 	private static String addNewlines(String in, int length, String nl) {
		final String src = in.replaceAll("\\\\n", nl);
		final StringTokenizer tokens = new StringTokenizer(src, nl, true);
 		final StringBuffer out = new StringBuffer();
 
 		while (tokens.hasMoreTokens()) {
			out.append(WordUtils.wrap(tokens.nextToken(), length, nl, false));
 		}
 
 		return out.toString();
 	}
 }
