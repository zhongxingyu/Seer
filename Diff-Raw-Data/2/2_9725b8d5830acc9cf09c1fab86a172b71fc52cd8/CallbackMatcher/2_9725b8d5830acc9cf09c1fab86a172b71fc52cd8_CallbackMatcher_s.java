 package com.agileapes.couteau.strings.replace;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Helps with replacing the text in a given string using a callback, allowing for dynamic modification of
  * matched values.
  *
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (8/14/13, 4:59 PM)
  */
 public class CallbackMatcher {
 
     private final Pattern pattern;
     private final String text;
 
     public CallbackMatcher(String pattern, String text) {
         this(Pattern.compile(pattern), text);
     }
 
     public CallbackMatcher(Pattern pattern, String text) {
         this.pattern = pattern;
         this.text = text;
     }
 
     /**
      * Replaces the text wrapped in the object with the given callback's return value
      * @param callback    the callback that will determine what the replacement should be
      * @return the modified text
      */
     public String replace(ReplaceCallback callback) {
         final Matcher matcher = pattern.matcher(text);
         final List<MatchToken> tokens = new ArrayList<MatchToken>();
         int pos = 0;
         while (matcher.find()) {
             if (pos < matcher.start()) {
                 tokens.add(new MatchToken(text.substring(pos, matcher.start())));
             }
             tokens.add(new MatchToken(matcher.toMatchResult()));
             pos = matcher.end();
         }
        if (pos < text.length() - 1) {
             tokens.add(new MatchToken(text.substring(pos)));
         }
         final StringBuilder builder = new StringBuilder();
         for (final MatchToken token : tokens) {
             final String value;
             if (token.getMatchResult() != null) {
                 value = callback.replace(token.getMatchResult());
             } else {
                 value = token.getValue();
             }
             builder.append(value);
         }
         return builder.toString();
     }
 
 }
