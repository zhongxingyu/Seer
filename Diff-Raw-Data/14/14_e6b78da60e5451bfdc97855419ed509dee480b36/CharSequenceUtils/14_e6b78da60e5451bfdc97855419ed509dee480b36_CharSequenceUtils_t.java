 package kkckkc.utils;
 
 import org.jetbrains.annotations.NotNull;
 
 public class CharSequenceUtils {
 	public static boolean startsWith(@NotNull CharSequence charSequence, @NotNull CharSequence potentialPrefix) {
 		int len = potentialPrefix.length();
 		if (charSequence.length() < len) return false;
 		
 		for (int i = 0; i < len; i++) {
 			if (charSequence.charAt(i) != potentialPrefix.charAt(i) || i >= charSequence.length()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
     public static boolean isWhitespace(@NotNull CharSequence charSequence) {
          if (charSequence.length() == 0) return true;
          for (int i = 0; i < charSequence.length(); i++) {
              char c = charSequence.charAt(i);
              if (! Character.isWhitespace(c)) return false;
          }
          return true;
      }

    public static CharSequence safeSubsequence(CharSequence charSequence, int start, int end) {
        return charSequence.subSequence(Math.max(0, start), Math.min(charSequence.length() - 1, end));
    }
 }
