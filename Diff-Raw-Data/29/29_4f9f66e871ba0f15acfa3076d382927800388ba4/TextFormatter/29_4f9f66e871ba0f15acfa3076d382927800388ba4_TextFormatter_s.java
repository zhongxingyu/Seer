 package org.kvj.bravo7.format;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.text.SpannableStringBuilder;
 
 public interface TextFormatter<T> {
 
	public static final Pattern eatAll = Pattern.compile(".*");
 
 	Pattern getPattern(T note, boolean selected);
 
 	void format(T note, SpannableStringBuilder sb, Matcher m, String text,
 			boolean selected);
 
 }
