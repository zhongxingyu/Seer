 package org.kvj.bravo7.format;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.text.SpannableStringBuilder;
 import android.text.style.ForegroundColorSpan;
 
 public class PlainTextFormatter<T> {
 
 	TextFormatter<T>[] formatters;
 
 	public PlainTextFormatter(TextFormatter<T>... formatters) {
 		this.formatters = formatters;
 	}
 
 	public static void addSpan(SpannableStringBuilder buffer, String text,
 			Object... span) {
 		if (null == text) {
 			return;
 		}
 		int start = buffer.length();
 		int end = start + text.length();
 		buffer.append(text);
 		if (null != span) {
 			for (int i = 0; i < span.length; i++) {
 				if (null != span[i]) {
 					buffer.setSpan(span[i], start, end,
 							SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
 				}
 			}
 		}
 	}
 
 	public void setFormatters(TextFormatter<T>[] formatters) {
 		this.formatters = formatters;
 	}
	
 	private void writePlainText(T note, SpannableStringBuilder sb,
 			int defColor, String text, int index, boolean selected) {
		if (index >= formatters.length || null == text) {
			addSpan(sb, text, new ForegroundColorSpan(defColor));
 			return;
 		}
 		TextFormatter<T> formatter = formatters[index];
 		Matcher m = null;
 		Pattern p = formatter.getPattern(note, selected);
 		if (null != p) {
 			m = p.matcher(text);
 		}
 		if (null == m || !m.find()) {
 			writePlainText(note, sb, defColor, text, index + 1, selected);
 			return;
 		}
 		do {
 			StringBuffer buffer = new StringBuffer();
 			m.appendReplacement(buffer, "");
 			if (0 != buffer.length()) {
 				writePlainText(note, sb, defColor, buffer.toString(),
 						index + 1, selected);
 			}
 			formatter.format(note, sb, m, m.group(), selected);
 		} while (m.find());
 		StringBuffer buffer = new StringBuffer();
 		m.appendTail(buffer);
 		if (0 != buffer.length()) {
 			writePlainText(note, sb, defColor, buffer.toString(), index + 1,
 					selected);
 		}
 	}
 
 	public void writePlainText(T note, SpannableStringBuilder sb, int defColor,
 			String text, boolean selected) {
 		writePlainText(note, sb, defColor, text, 0, selected);
 	}
 }
